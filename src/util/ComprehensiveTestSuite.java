package util;

import dao.ExpenseDAO;
import dao.MySQLExpenseDAO;
import dao.UserDAO;
import dao.MySQLUserDAO;
import model.Expense;
import model.User;
import service.ExpenseService;
import service.ExpenseServiceImpl;
import service.ServiceException;
import service.UserService;
import service.UserServiceImpl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ComprehensiveTestSuite {
    private static int passedCount = 0;
    private static int failedCount = 0;

    private static void check(String testName, boolean condition, String details) {
        if (condition) {
            System.out.println("  [PASS] " + testName + " - " + details);
            passedCount++;
        } else {
            System.err.println("  [FAIL] " + testName + " - " + details);
            failedCount++;
        }
    }

    public static void main(String[] args) {
        System.out.println("===============================================================");
        System.out.println("         EXPENSE TRACKER - FULL FEATURE VERIFICATION");
        System.out.println("===============================================================\n");

        UserService userService = new UserServiceImpl();
        ExpenseService expenseService = new ExpenseServiceImpl();
        ExpenseDAO expenseDAO = new MySQLExpenseDAO();
        UserDAO userDAO = new MySQLUserDAO();

        String testEmailA = "test_alice_" + System.currentTimeMillis() + "@example.com";
        String testEmailB = "test_bob_" + System.currentTimeMillis() + "@example.com";
        String rawPassword = "SecurePassword123";

        // 1. New User Registration
        try {
            boolean registeredA = userService.registerUser("Alice Tester", testEmailA, rawPassword);
            check("TC-01: User A Registration", registeredA, "Registered successfully for " + testEmailA);
        } catch (Exception e) {
            check("TC-01: User A Registration", false, e.getMessage());
        }

        // 2. Duplicate Registration Rejection
        try {
            userService.registerUser("Alice Duplicate", testEmailA, rawPassword);
            check("TC-02: Duplicate Email Rejection", false, "Allowed duplicate email registration unexpectedly.");
        } catch (ServiceException e) {
            check("TC-02: Duplicate Email Rejection", true, "Properly blocked duplicate registration: " + e.getMessage());
        } catch (Exception e) {
            check("TC-02: Duplicate Email Rejection", false, e.getMessage());
        }

        // 3. Login with Valid Credentials
        User userA = null;
        try {
            Optional<User> loggedIn = userService.login(testEmailA, rawPassword);
            if (loggedIn.isPresent()) {
                userA = loggedIn.get();
                check("TC-03: Valid Login", true, "Logged in successfully as ID " + userA.getId() + " (" + userA.getName() + ")");
            } else {
                check("TC-03: Valid Login", false, "Returned empty optional");
            }
        } catch (Exception e) {
            check("TC-03: Valid Login", false, e.getMessage());
        }

        // 4. Login with Invalid Credentials
        try {
            Optional<User> loggedIn = userService.login(testEmailA, "WrongPassword!");
            check("TC-04: Invalid Credentials Rejection", loggedIn.isEmpty(), "Properly returned empty optional for wrong credentials.");
        } catch (Exception e) {
            check("TC-04: Invalid Credentials Rejection", false, e.getMessage());
        }

        // 5. Register & Login User B (for Multi-Tenant Security Tests)
        User userB = null;
        try {
            userService.registerUser("Bob Tester", testEmailB, rawPassword);
            Optional<User> optB = userService.login(testEmailB, rawPassword);
            if (optB.isPresent()) {
                userB = optB.get();
                check("Setup: User B Registration & Login", true, "User B ID: " + userB.getId());
            }
        } catch (Exception e) {
            check("Setup: User B Registration & Login", false, e.getMessage());
        }

        int userAId = (userA != null) ? userA.getId() : -1;
        int userBId = (userB != null) ? userB.getId() : -1;

        // 6. Add Valid Expenses for User A
        try {
            boolean a1 = expenseService.addExpense(new Expense(userAId, new BigDecimal("1500.50"), "Food", LocalDate.now(), "Grocery shopping"));
            boolean a2 = expenseService.addExpense(new Expense(userAId, new BigDecimal("3200.00"), "Rent", LocalDate.now().minusDays(2), "Room rent advance"));
            boolean a3 = expenseService.addExpense(new Expense(userAId, new BigDecimal("450.75"), "Transport", LocalDate.now().minusDays(5), "Metro recharge"));
            check("TC-05: Add Valid Expenses", a1 && a2 && a3, "Added 3 expenses successfully for User A");
        } catch (Exception e) {
            check("TC-05: Add Valid Expenses", false, e.getMessage());
        }

        // 7. Add Invalid Expenses Validation (Negative amount, blank category, null date)
        try {
            expenseService.addExpense(new Expense(userAId, new BigDecimal("-100.00"), "Food", LocalDate.now(), "Negative"));
            check("TC-06: Negative Amount Validation", false, "Allowed negative amount");
        } catch (ServiceException e) {
            check("TC-06: Negative Amount Validation", true, "Rejected negative amount: " + e.getMessage());
        } catch (Exception e) {
            check("TC-06: Negative Amount Validation", false, e.getMessage());
        }

        try {
            expenseService.addExpense(new Expense(userAId, new BigDecimal("100.00"), "", LocalDate.now(), "Empty category"));
            check("TC-06b: Empty Category Validation", false, "Allowed empty category");
        } catch (ServiceException e) {
            check("TC-06b: Empty Category Validation", true, "Rejected empty category: " + e.getMessage());
        } catch (Exception e) {
            check("TC-06b: Empty Category Validation", false, e.getMessage());
        }

        // 8. View Expense History for User A
        List<Expense> listA = null;
        try {
            listA = expenseService.getAllExpenses(userAId);
            check("TC-07: Get Expense History", listA.size() == 3, "Retrieved exactly " + listA.size() + " expenses for User A");
        } catch (Exception e) {
            check("TC-07: Get Expense History", false, e.getMessage());
        }

        // 9. Dashboard Calculations for User A
        try {
            BigDecimal totalA = expenseService.calculateTotalExpenses(userAId);
            int countA = expenseService.getExpenseCount(userAId);
            BigDecimal currentMonthA = expenseService.calculateCurrentMonthExpenses(userAId);
            BigDecimal expectedTotal = new BigDecimal("5151.25");
            check("TC-08: Total Calculation", totalA.compareTo(expectedTotal) == 0, "Total is Rs. " + totalA);
            check("TC-09: Count Calculation", countA == 3, "Count is " + countA);
            check("TC-10: Current Month Calculation", currentMonthA.compareTo(BigDecimal.ZERO) > 0, "Current month total is Rs. " + currentMonthA);
        } catch (Exception e) {
            check("TC-08..10: Calculations", false, e.getMessage());
        }

        // 10. Category Breakdown
        try {
            List<Expense> foodExpenses = expenseService.getExpensesByCategory(userAId, "Food");
            List<Expense> rentExpenses = expenseService.getExpensesByCategory(userAId, "Rent");
            check("TC-11: Category Filter & Breakdown", foodExpenses.size() == 1 && rentExpenses.size() == 1, 
                  "Food: " + foodExpenses.size() + ", Rent: " + rentExpenses.size());
        } catch (Exception e) {
            check("TC-11: Category Filter & Breakdown", false, e.getMessage());
        }

        // 11. Date Range Filtering
        try {
            LocalDate start = LocalDate.now().minusDays(3);
            LocalDate end = LocalDate.now();
            List<Expense> filtered = expenseService.getExpensesByDateRange(userAId, start, end);
            check("TC-12: Date Filtering", filtered.size() == 2, "Filtered count in last 3 days: " + filtered.size() + " (Expected 2: Food & Rent)");
        } catch (Exception e) {
            check("TC-12: Date Filtering", false, e.getMessage());
        }

        // 12. Edit Existing Expense
        Expense expToEdit = (listA != null && !listA.isEmpty()) ? listA.get(0) : null;
        try {
            if (expToEdit != null) {
                expToEdit.setAmount(new BigDecimal("1800.00"));
                expToEdit.setDescription("Updated grocery bill");
                boolean updated = expenseService.updateExpense(expToEdit);
                Optional<Expense> refetched = expenseService.getExpenseById(expToEdit.getId(), userAId);
                check("TC-13: Edit Expense", updated && refetched.isPresent() && refetched.get().getAmount().compareTo(new BigDecimal("1800.00")) == 0, 
                      "Updated amount to: Rs. " + (refetched.isPresent() ? refetched.get().getAmount() : 0));
            }
        } catch (Exception e) {
            check("TC-13: Edit Expense", false, e.getMessage());
        }

        // 13. Multi-Tenant Security & Isolation (Crucial!)
        try {
            List<Expense> listB = expenseService.getAllExpenses(userBId);
            check("TC-14: User Isolation (List)", listB.isEmpty(), "User B cannot see User A's expenses (Count: " + listB.size() + ")");
            
            // Try to view User A's expense using User B's ID
            if (expToEdit != null) {
                Optional<Expense> crossView = expenseService.getExpenseById(expToEdit.getId(), userBId);
                check("TC-15: Cross-User View Protection", crossView.isEmpty(), "User B blocked from viewing User A's expense by ID");
                
                // Try to delete User A's expense using User B's ID
                try {
                    boolean crossDelete = expenseService.deleteExpense(expToEdit.getId(), userBId);
                    check("TC-16: Cross-User Delete Protection", !crossDelete, "User B blocked from deleting User A's expense");
                } catch (ServiceException e) {
                    check("TC-16: Cross-User Delete Protection", true, "Service blocked unauthorized delete attempt: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            check("TC-14..16: Security Isolation", false, e.getMessage());
        }

        // 14. Delete Expense for Owner
        try {
            if (expToEdit != null) {
                boolean deleted = expenseService.deleteExpense(expToEdit.getId(), userAId);
                List<Expense> remaining = expenseService.getAllExpenses(userAId);
                check("TC-17: Delete Expense", deleted && remaining.size() == 2, "Expense deleted successfully. Remaining: " + remaining.size());
            }
        } catch (Exception e) {
            check("TC-17: Delete Expense", false, e.getMessage());
        }

        System.out.println("\n===============================================================");
        System.out.println("TEST RESULTS: " + passedCount + " PASSED, " + failedCount + " FAILED");
        System.out.println("===============================================================\n");

        if (failedCount == 0) {
            System.out.println(">>> ALL FEATURES, VALIDATIONS & SECURITY POLICIES PASSED SUCCESSFULLY! <<<");
        }
    }
}
