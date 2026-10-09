# ExpenseTracker – Complete Quality Assurance & System Test Plan

**Project**: ExpenseTracker Desktop Application (Java Swing + JDBC + MySQL)  
**Version**: 1.0.0  
**Test Suite**: Functional, Concurrency, Security, and Edge-Case Tests

---

## Test Execution Matrix

| Test ID | Test Scenario | Input Data | Expected Result | Actual Result | Status |
| :--- | :--- | :--- | :--- | :--- | :---: |
| **TC-01** | New user registration | Name: `Alice`<br>Email: `alice@example.com`<br>Password: `secret123`<br>Confirm: `secret123` | User is registered in database, password hashed with SHA-256; success modal appears; navigates to `LoginFrame`. | _[Pending / As Expected]_ | `[ ] PASS`<br>`[ ] FAIL` |
| **TC-02** | Duplicate registration | Name: `Alice Clone`<br>Email: `alice@example.com`<br>Password: `secret123`<br>Confirm: `secret123` | System catches `DuplicateEmailException` (MySQL 1062) and displays warning: *"An account with email 'alice@example.com' already exists."* Registration aborted. | _[Pending / As Expected]_ | `[ ] PASS`<br>`[ ] FAIL` |
| **TC-03** | Login with valid credentials | Email: `alice@example.com`<br>Password: `secret123` | Authenticates successfully; user saved in `SessionManager`; password wiped from memory; `DashboardFrame` opens displaying *"Welcome, Alice"*; `LoginFrame` closed. | _[Pending / As Expected]_ | `[ ] PASS`<br>`[ ] FAIL` |
| **TC-04** | Login with invalid credentials | Email: `alice@example.com`<br>Password: `wrongpassword` | Authentication fails; displays modal *"Invalid email or password. Please try again."*; password field is cleared and focused; remains on `LoginFrame`. | _[Pending / As Expected]_ | `[ ] PASS`<br>`[ ] FAIL` |
| **TC-05** | Add valid expense | Amount: `450.00`<br>Category: `Food`<br>Date: `2026-10-01`<br>Description: `Team lunch` | Record inserted into `expenses` with current `user_id`; displays *"Expense added successfully!"*; form resets; parent dashboard totals increment. | _[Pending / As Expected]_ | `[ ] PASS`<br>`[ ] FAIL` |
| **TC-06** | Add invalid expense (negative amount & invalid date) | Amount: `-50.00`<br>Category: `Food`<br>Date: `invalid-date` | Validation fails before database call; shows *"Amount must be greater than zero."* or date parsing warning; record is not saved. | _[Pending / As Expected]_ | `[ ] PASS`<br>`[ ] FAIL` |
| **TC-07** | Edit existing expense | Select expense #1<br>Change Amount to `600.00`<br>Change Category to `Bills` | Row updated via `updateExpense(WHERE id = ? AND user_id = ?)`; success confirmation shown; `JTable` and dashboard totals update immediately. | _[Pending / As Expected]_ | `[ ] PASS`<br>`[ ] FAIL` |
| **TC-08** | Delete expense | Select expense #1<br>Click Delete | Modal confirmation appears (*"Are you sure you want to delete this expense?"*). On click **Yes**, row deleted via `DELETE WHERE id = ? AND user_id = ?`; table refreshes; total recalculated. | _[Pending / As Expected]_ | `[ ] PASS`<br>`[ ] FAIL` |
| **TC-09** | View expense history | Click `Expense History` from Dashboard | `ExpenseHistoryFrame` opens; loads ledger via `SwingWorker`; displays `ID`, `Amount (₹)`, `Category`, `Date (dd-MMM-yyyy)`, and `Description` with alternating row colors. | _[Pending / As Expected]_ | `[ ] PASS`<br>`[ ] FAIL` |
| **TC-10** | Calculate total expenses | User with expenses: `450.00`, `600.00`, `250.00` | Dashboard card displays exact `BigDecimal` sum: `₹ 1,300.00`; count shows `3 records`. | _[Pending / As Expected]_ | `[ ] PASS`<br>`[ ] FAIL` |
| **TC-11** | Category summary | User has multiple Food, Travel, and Bills expenses | `CategorySummaryFrame` aggregates totals grouped by category, computes accurate percentage shares (`% of Total`), and sorts categories descending by expenditure. | _[Pending / As Expected]_ | `[ ] PASS`<br>`[ ] FAIL` |
| **TC-12** | Date filtering | Start Date: `2026-10-01`<br>End Date: `2026-10-15` | `DateFilterFrame` executes parameterized date-range query; table populates only transactions within range; header updates `Range Total: ₹ X,XXX.XX`. | _[Pending / As Expected]_ | `[ ] PASS`<br>`[ ] FAIL` |
| **TC-13** | Logout | Click `Logout` button from header | Confirmation modal appears; on confirmation, `SessionManager.logout()` sets user to `null`; `DashboardFrame` closes; `LoginFrame` is displayed. | _[Pending / As Expected]_ | `[ ] PASS`<br>`[ ] FAIL` |
| **TC-14** | Login as another user | Email: `bob@example.com`<br>Password: `secretBob1` | Authenticates Bob; `SessionManager` updates to Bob's ID; Dashboard opens displaying *"Welcome, Bob"*. | _[Pending / As Expected]_ | `[ ] PASS`<br>`[ ] FAIL` |
| **TC-15** | Multi-tenant isolation (Verify users cannot see each other's expenses) | Log in as Bob | Bob views Expense History, Dashboard, and Categories; sees **only** Bob's transactions (`user_id = Bob's ID`); zero records belonging to Alice are visible or accessible. | _[Pending / As Expected]_ | `[ ] PASS`<br>`[ ] FAIL` |
| **TC-16** | Database unavailable / Server down | Stop MySQL service or supply invalid port in `db.properties` | Application does not crash with raw stack traces; shows user-friendly error *"Login failed due to a database error. Please try again."*; developer details logged to `System.err`. | _[Pending / As Expected]_ | `[ ] PASS`<br>`[ ] FAIL` |
| **TC-17** | Empty expense database (New user with 0 expenses) | Log in as brand-new user | `ExpenseHistoryFrame` and `ManageExpensesFrame` display clean empty-state card (*"No Expenses Recorded Yet"*); Dashboard totals display `₹ 0.00` and `0 records` without `NullPointerException`. | _[Pending / As Expected]_ | `[ ] PASS`<br>`[ ] FAIL` |
| **TC-18** | Multiple expenses handling | Add 50+ expenses across different categories and dates | `JTable` displays all records smoothly with vertical scrollpane; status bar shows accurate count and cumulative amount; no memory leaks. | _[Pending / As Expected]_ | `[ ] PASS`<br>`[ ] FAIL` |
| **TC-19** | Multithreaded non-blocking loading | Click `Refresh` on Dashboard / Expense History | Database queries execute inside `SwingWorker` in background; animated `JProgressBar` displays status; window remains draggable, clickable, and responsive without freezing EDT. | _[Pending / As Expected]_ | `[ ] PASS`<br>`[ ] FAIL` |
| **TC-20** | Dashboard concurrent refresh | Add or delete an expense in child frame; return to Dashboard | `windowActivated` event or `Refresh` button triggers `refreshDashboard()`; `CompletableFuture.allOf()` runs 3 concurrent queries in parallel and updates all KPI cards automatically. | _[Pending / As Expected]_ | `[ ] PASS`<br>`[ ] FAIL` |

---

## Detailed Test Case Specifications

### TC-01: New User Registration
* **Objective**: Verify that a new user can register with valid credentials.
* **Preconditions**: Database running; email `alice@example.com` not yet registered.
* **Input**:
  - Full Name: `Alice Johnson`
  - Email: `alice@example.com`
  - Password: `Password@123`
  - Confirm Password: `Password@123`
* **Steps**:
  1. Open application (`Main.java`).
  2. Click **Create New Account** on `LoginFrame`.
  3. Enter input values and click **Register**.
* **Expected Result**: Success dialog displayed; record inserted into `users` table with hashed password; redirects to `LoginFrame`.
* **Actual Result**: `___________________________________________________`
* **Status**: `[ ] Pass` &nbsp; `[ ] Fail`

---

### TC-02: Duplicate Email Registration Prevention
* **Objective**: Verify that the application prevents registering an email that already exists.
* **Preconditions**: `alice@example.com` already exists in `users` table.
* **Input**:
  - Full Name: `Alice Two`
  - Email: `alice@example.com`
  - Password: `Password@123`
  - Confirm Password: `Password@123`
* **Steps**:
  1. Open `RegisterFrame`.
  2. Enter existing email and valid passwords.
  3. Click **Register**.
* **Expected Result**: Warning popup: *"An account with email 'alice@example.com' already exists. Please use a different email or log in."* No duplicate row created.
* **Actual Result**: `___________________________________________________`
* **Status**: `[ ] Pass` &nbsp; `[ ] Fail`

---

### TC-03: Login with Valid Credentials
* **Objective**: Verify successful authentication and session initialization.
* **Preconditions**: Registered user `alice@example.com` with password `Password@123`.
* **Input**: Email = `alice@example.com`, Password = `Password@123`.
* **Steps**:
  1. Enter credentials on `LoginFrame`.
  2. Click **Login** or press **Enter**.
* **Expected Result**: `SessionManager` receives user; `DashboardFrame` opens displaying *"Welcome, Alice Johnson"*; `LoginFrame` disposes.
* **Actual Result**: `___________________________________________________`
* **Status**: `[ ] Pass` &nbsp; `[ ] Fail`

---

### TC-04: Login with Invalid Credentials
* **Objective**: Verify rejection of incorrect password or non-existent email.
* **Input**: Email = `alice@example.com`, Password = `WrongPassword`.
* **Steps**:
  1. Enter incorrect password on `LoginFrame`.
  2. Click **Login**.
* **Expected Result**: Error dialog *"Invalid email or password. Please try again."*; password field cleared; focus reset.
* **Actual Result**: `___________________________________________________`
* **Status**: `[ ] Pass` &nbsp; `[ ] Fail`

---

### TC-05: Add Valid Expense
* **Objective**: Verify recording an expense transaction.
* **Preconditions**: User logged in.
* **Input**:
  - Amount: `750.50`
  - Category: `Food`
  - Date: `2026-10-01`
  - Description: `Dinner with client`
* **Steps**:
  1. Click **➕ Add Expense** on Dashboard.
  2. Enter inputs and click **Save**.
* **Expected Result**: Success dialog; form cleared; dashboard totals increment by ₹750.50.
* **Actual Result**: `___________________________________________________`
* **Status**: `[ ] Pass` &nbsp; `[ ] Fail`

---

### TC-06: Add Invalid Expense (Negative / Zero / Formatting)
* **Objective**: Verify input validation prevents saving bad data.
* **Input**: Amount = `-100.00`, Category = `Food`, Date = `2026-10-01`.
* **Steps**:
  1. Enter negative amount on `AddExpenseFrame`.
  2. Click **Save**.
* **Expected Result**: Warning popup: *"Amount must be greater than zero."* Record rejected.
* **Actual Result**: `___________________________________________________`
* **Status**: `[ ] Pass` &nbsp; `[ ] Fail`

---

### TC-07: Edit Existing Expense
* **Objective**: Verify modifying amount, category, date, and description.
* **Preconditions**: At least one expense exists for active user.
* **Input**: Select expense #1; change Amount to `950.00`, Category to `Travel`.
* **Steps**:
  1. Open **✏️ Edit / Delete** from Dashboard.
  2. Select row and click **✏️ Edit Selected**.
  3. Modify values and click **Save**.
* **Expected Result**: Dialog *"Expense #1 updated successfully!"*; `JTable` updates to new values.
* **Actual Result**: `___________________________________________________`
* **Status**: `[ ] Pass` &nbsp; `[ ] Fail`

---

### TC-08: Delete Expense with Confirmation
* **Objective**: Verify deletion removes row and recalculates totals.
* **Steps**:
  1. Open `ManageExpensesFrame`.
  2. Select an expense row and click **🗑️ Delete Selected**.
  3. Click **Yes** on confirmation dialog.
* **Expected Result**: Expense deleted in MySQL; row removed from table; total expense recalculates.
* **Actual Result**: `___________________________________________________`
* **Status**: `[ ] Pass` &nbsp; `[ ] Fail`

---

### TC-09: View Expense History Ledger
* **Objective**: Verify tabular presentation of all user expenses.
* **Steps**: Click **📋 Expense History** on Dashboard.
* **Expected Result**: Read-only `JTable` displays `ID`, `Amount (₹)`, `Category`, `Date`, `Description`. Column alignments: currency right-aligned, dates centered.
* **Actual Result**: `___________________________________________________`
* **Status**: `[ ] Pass` &nbsp; `[ ] Fail`

---

### TC-10: Total Expense Calculation Accuracy
* **Objective**: Verify exact monetary precision using `BigDecimal`.
* **Input**: User has expenses of `120.35`, `80.65`, `50.00`.
* **Expected Result**: Exact mathematical sum `₹ 251.00` without IEEE-754 floating-point rounding errors.
* **Actual Result**: `___________________________________________________`
* **Status**: `[ ] Pass` &nbsp; `[ ] Fail`

---

### TC-11: Category Summary & Share Distribution
* **Objective**: Verify spending grouped by category with percentages.
* **Steps**: Click **📊 Category Summary** on Dashboard.
* **Expected Result**: Table shows each category, transaction count, total sum, and percentage of total spend (`% of Total`); highlights top spending category.
* **Actual Result**: `___________________________________________________`
* **Status**: `[ ] Pass` &nbsp; `[ ] Fail`

---

### TC-12: Date-Range Query Filtering
* **Objective**: Verify transactions filter within start and end dates.
* **Input**: Start Date = `2026-10-01`, End Date = `2026-10-10`.
* **Steps**:
  1. Click **📅 Date Filter** on Dashboard.
  2. Enter date range and click **🔍 Search**.
* **Expected Result**: Only expenses dated between Oct 1 and Oct 10 appear; range total displayed in header.
* **Actual Result**: `___________________________________________________`
* **Status**: `[ ] Pass` &nbsp; `[ ] Fail`

---

### TC-13: User Logout & Session Invalidation
* **Objective**: Verify clean termination of user session.
* **Steps**: Click **🚪 Logout** on Dashboard header and confirm.
* **Expected Result**: `SessionManager` clears `currentUser = null`; `DashboardFrame` closes; `LoginFrame` displays.
* **Actual Result**: `___________________________________________________`
* **Status**: `[ ] Pass` &nbsp; `[ ] Fail`

---

### TC-14: Login as Another User
* **Objective**: Verify multi-user session switching.
* **Input**: User 2 credentials (`bob@example.com`).
* **Steps**: Log in as Bob.
* **Expected Result**: Dashboard displays *"Welcome, Bob"*; metrics load Bob's independent account stats.
* **Actual Result**: `___________________________________________________`
* **Status**: `[ ] Pass` &nbsp; `[ ] Fail`

---

### TC-15: Multi-Tenant Data Isolation Enforcement
* **Objective**: Verify User A cannot access User B's expenses.
* **Preconditions**: Alice has 5 expenses; Bob has 2 expenses.
* **Steps**: Log in as Bob and inspect Dashboard, History, Categories, and Filters.
* **Expected Result**: Bob sees only his 2 expenses. Alice's records are completely invisible and unmodifiable.
* **Actual Result**: `___________________________________________________`
* **Status**: `[ ] Pass` &nbsp; `[ ] Fail`

---

### TC-16: Database Unavailable Handling
* **Objective**: Verify graceful degradation when MySQL database server is offline.
* **Preconditions**: MySQL service stopped or invalid password in `db.properties`.
* **Steps**: Attempt to log in or refresh dashboard.
* **Expected Result**: Shows user-friendly dialog: *"Login failed due to a database error. Please try again."* GUI does not crash.
* **Actual Result**: `___________________________________________________`
* **Status**: `[ ] Pass` &nbsp; `[ ] Fail`

---

### TC-17: Empty Database Handling (Zero Records)
* **Objective**: Verify UI behavior when a user has no recorded transactions.
* **Steps**: Log in as a newly registered user and open History / Categories / Manage screens.
* **Expected Result**: Empty-state placeholder cards (*"No Expenses Recorded Yet"*) display cleanly without `NullPointerException`.
* **Actual Result**: `___________________________________________________`
* **Status**: `[ ] Pass` &nbsp; `[ ] Fail`

---

### TC-18: Large Dataset & Multiple Records Performance
* **Objective**: Verify table rendering and summary calculation with 100+ transactions.
* **Steps**: Populate 100+ expenses and browse Expense History.
* **Expected Result**: Table scrolls smoothly; memory consumption remains stable; calculations remain accurate.
* **Actual Result**: `___________________________________________________`
* **Status**: `[ ] Pass` &nbsp; `[ ] Fail`

---

### TC-19: Multithreaded Background Loading with SwingWorker
* **Objective**: Verify UI responsiveness during database retrieval.
* **Steps**: Click **Refresh** on Dashboard or History frame.
* **Expected Result**: Indeterminate `JProgressBar` displays progress; window remains draggable, animated, and interactive without freezing the Event Dispatch Thread (EDT).
* **Actual Result**: `___________________________________________________`
* **Status**: `[ ] Pass` &nbsp; `[ ] Fail`

---

### TC-20: Automatic Concurrent Dashboard Refresh
* **Objective**: Verify live metric updates via parallel asynchronous tasks.
* **Steps**: Add or delete an expense in `AddExpenseFrame` / `ManageExpensesFrame`, then switch back to Dashboard.
* **Expected Result**: `windowActivated` triggers `refreshDashboard()`; `CompletableFuture.allOf()` fetches Total, Count, and Month Total concurrently, updating all 3 KPI cards automatically.
* **Actual Result**: `___________________________________________________`
* **Status**: `[ ] Pass` &nbsp; `[ ] Fail`
