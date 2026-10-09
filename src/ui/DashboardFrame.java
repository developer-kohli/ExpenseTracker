package ui;

import model.User;
import service.ExpenseService;
import service.ExpenseServiceImpl;
import service.ServiceException;
import util.SessionManager;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Modern, clean Java Swing Dashboard Frame for ExpenseTracker.
 *
 * <h3>Design & Architecture</h3>
 * <ul>
 *   <li><b>Attractive Summary Cards</b>: Total Expenses, Number of Expenses, Current Month Expenses.</li>
 *   <li><b>Navigation Buttons</b>: Add Expense, Expense History, Category Summary, Date Filter, Logout.</li>
 *   <li><b>User Greeting Header</b>: Displays {@code Welcome, <username>}.</li>
 *   <li><b>Service Layer Calculations</b>: All mathematical computations, date intervals,
 *       and SQL aggregations reside strictly within {@link ExpenseService}.</li>
 *   <li><b>Automatic Synchronized Refresh</b>: Re-calculates and displays live statistics
 *       after expenses are added, edited, or deleted.</li>
 * </ul>
 */
public class DashboardFrame extends JFrame {

    // ----------------------------------------------------------------
    // Service Dependency (Encapsulated)
    // ----------------------------------------------------------------
    private final ExpenseService expenseService;

    // ----------------------------------------------------------------
    // UI Metric Components
    // ----------------------------------------------------------------
    private JLabel lblTotalValue;
    private JLabel lblCountValue;
    private JLabel lblMonthValue;
    private JLabel lblMonthSubtitle;
    private JLabel lblUserGreeting;
    private JButton btnRefresh;
    private JProgressBar progressBar;
    private JLabel lblLoadingStatus;

    /**
     * Constructs the DashboardFrame and initializes GUI components.
     */
    public DashboardFrame() {
        this.expenseService = new ExpenseServiceImpl();

        // Redirect to Login if no active session
        if (!SessionManager.getInstance().isLoggedIn()) {
            SwingUtilities.invokeLater(() -> {
                new LoginFrame().setVisible(true);
                dispose();
            });
            return;
        }

        initFrame();
        initComponents();
        refreshDashboard();
    }

    /**
     * Configures the main window frame properties.
     */
    private void initFrame() {
        setTitle("Expense Tracker – Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(940, 640);
        setMinimumSize(new Dimension(860, 580));
        setLocationRelativeTo(null); // Center on screen
        getContentPane().setBackground(UITheme.BG_LIGHT);
        setLayout(new BorderLayout());

        // Automatically refresh statistics when returning focus to the Dashboard
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowActivated(WindowEvent e) {
                refreshDashboard();
            }
        });
    }

    /**
     * Builds and arranges all dashboard UI sections.
     */
    private void initComponents() {
        add(createHeaderPanel(), BorderLayout.NORTH);

        JPanel mainContent = new JPanel();
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));
        mainContent.setBackground(UITheme.BG_LIGHT);
        mainContent.setBorder(new EmptyBorder(18, 24, 18, 24));

        // 1. Summary Cards Section
        mainContent.add(createSectionHeader("Overview Summary"));
        mainContent.add(Box.createVerticalStrut(10));
        mainContent.add(createSummaryCardsPanel());
        mainContent.add(Box.createVerticalStrut(22));

        // 2. Navigation Actions Section
        mainContent.add(createSectionHeader("Quick Actions"));
        mainContent.add(Box.createVerticalStrut(10));
        mainContent.add(createNavigationPanel());

        add(mainContent, BorderLayout.CENTER);
        add(createFooterStatusPanel(), BorderLayout.SOUTH);
    }

    /**
     * Creates the top navigation header banner.
     */
    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.BG_CARD);
        header.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER_COLOR),
                new EmptyBorder(14, 24, 14, 24)
        ));

        JLabel lblTitle = new JLabel("EXPENSE TRACKER");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(Color.BLACK);
        header.add(lblTitle, BorderLayout.WEST);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        rightPanel.setBackground(UITheme.BG_CARD);

        User currentUser = SessionManager.getInstance().getCurrentUser();
        String username = (currentUser != null && currentUser.getName() != null)
                ? currentUser.getName()
                : "User";

        // Display "Welcome, <username>"
        lblUserGreeting = new JLabel("Welcome, " + username);
        lblUserGreeting.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblUserGreeting.setForeground(Color.BLACK);

        btnRefresh = UITheme.createSecondaryButton("Refresh");
        btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnRefresh.setBorder(new CompoundBorder(
                new LineBorder(new Color(148, 163, 184), 1, true),
                new EmptyBorder(6, 12, 6, 12)
        ));
        btnRefresh.addActionListener(e -> refreshDashboard());

        JButton btnLogout = UITheme.createDangerButton("Logout");
        btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnLogout.setBorder(new CompoundBorder(
                new LineBorder(new Color(252, 165, 165), 1, true),
                new EmptyBorder(6, 14, 6, 14)
        ));
        btnLogout.addActionListener(e -> handleLogout());

        rightPanel.add(lblUserGreeting);
        rightPanel.add(btnRefresh);
        rightPanel.add(btnLogout);

        header.add(rightPanel, BorderLayout.EAST);
        return header;
    }

    /**
     * Creates the 3 KPI summary cards.
     */
    private JPanel createSummaryCardsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 16, 0));
        panel.setBackground(UITheme.BG_LIGHT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        // Card 1: Total Expenses
        lblTotalValue = new JLabel("₹ 0.00");
        JPanel cardTotal = createCard("TOTAL EXPENSES", lblTotalValue, "All-time cumulative total", UITheme.PRIMARY_DARK);

        // Card 2: Number of Expenses
        lblCountValue = new JLabel("0 records");
        JPanel cardCount = createCard("NUMBER OF EXPENSES", lblCountValue, "Total entries recorded", UITheme.SUCCESS_GREEN);

        // Card 3: Current Month Expenses
        lblMonthValue = new JLabel("₹ 0.00");
        String currentMonthName = LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM yyyy"));
        lblMonthSubtitle = new JLabel("Expenses in " + currentMonthName);
        JPanel cardMonth = createCardWithSubtitle("CURRENT MONTH", lblMonthValue, lblMonthSubtitle, UITheme.ACCENT_BLUE);

        panel.add(cardTotal);
        panel.add(cardCount);
        panel.add(cardMonth);

        return panel;
    }

    private JPanel createCard(String title, JLabel valueLabel, String subtitleText, Color accentColor) {
        JLabel lblSub = new JLabel(subtitleText);
        return createCardWithSubtitle(title, valueLabel, lblSub, accentColor);
    }

    private JPanel createCardWithSubtitle(String title, JLabel valueLabel, JLabel subtitleLabel, Color accentColor) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(UITheme.BG_CARD);
        card.setBorder(new CompoundBorder(
                new LineBorder(UITheme.BORDER_COLOR, 1, true),
                new EmptyBorder(16, 18, 16, 18)
        ));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(UITheme.FONT_LABEL);
        lblTitle.setForeground(accentColor);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valueLabel.setForeground(UITheme.TEXT_MAIN);

        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subtitleLabel.setForeground(UITheme.TEXT_MUTED);

        card.add(lblTitle);
        card.add(Box.createVerticalStrut(8));
        card.add(valueLabel);
        card.add(Box.createVerticalStrut(4));
        card.add(subtitleLabel);

        return card;
    }

    /**
     * Creates navigation action buttons for Add Expense, Expense History,
     * Category Summary, Date Filter, and Logout.
     */
    private JPanel createNavigationPanel() {
        JPanel grid = new JPanel(new GridLayout(2, 3, 16, 16));
        grid.setBackground(UITheme.BG_LIGHT);

        JButton btnAddExpense = createNavButton("➕ Add Expense", "Record a new expense item", UITheme.ACCENT_BLUE);
        JButton btnHistory    = createNavButton("📋 Expense History", "View complete ledger of transactions", new Color(14, 165, 233));
        JButton btnManage     = createNavButton("✏️ Edit / Delete", "Modify or remove existing expenses", new Color(124, 58, 237));
        JButton btnCategory   = createNavButton("📊 Category Summary", "Inspect expenses grouped by category", UITheme.SUCCESS_GREEN);
        JButton btnDateFilter = createNavButton("📅 Date Filter", "Filter expenses across date intervals", new Color(217, 119, 6));
        JButton btnLogout     = createNavButton("🚪 Logout", "End session and sign out", UITheme.DANGER_RED);

        btnAddExpense.addActionListener(e -> openAddExpense());
        btnHistory.addActionListener(e -> openExpenseHistory());
        btnManage.addActionListener(e -> openManageExpenses());
        btnCategory.addActionListener(e -> openCategorySummary());
        btnDateFilter.addActionListener(e -> openDateFilter());
        btnLogout.addActionListener(e -> handleLogout());

        grid.add(btnAddExpense);
        grid.add(btnHistory);
        grid.add(btnManage);
        grid.add(btnCategory);
        grid.add(btnDateFilter);
        grid.add(btnLogout);

        return grid;
    }

    private JButton createNavButton(String title, String description, Color accentColor) {
        JButton btn = new JButton();
        btn.setLayout(new BorderLayout());
        btn.setBackground(UITheme.BG_CARD);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new CompoundBorder(
                new LineBorder(UITheme.BORDER_COLOR, 1, true),
                new EmptyBorder(16, 16, 16, 16)
        ));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(UITheme.FONT_BUTTON);
        lblTitle.setForeground(accentColor);

        JLabel lblDesc = new JLabel(description);
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblDesc.setForeground(UITheme.TEXT_MUTED);

        btn.add(lblTitle, BorderLayout.NORTH);
        btn.add(lblDesc, BorderLayout.SOUTH);

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(241, 245, 249));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(UITheme.BG_CARD);
            }
        });

        return btn;
    }

    private JLabel createSectionHeader(String title) {
        JLabel label = new JLabel(title);
        label.setFont(UITheme.FONT_SECTION);
        label.setForeground(UITheme.TEXT_MAIN);
        return label;
    }

    private JPanel createFooterStatusPanel() {
        JPanel footer = new JPanel(new BorderLayout(10, 0));
        footer.setBackground(UITheme.BG_CARD);
        footer.setBorder(new CompoundBorder(
                new LineBorder(UITheme.BORDER_COLOR, 1, false),
                new EmptyBorder(8, 24, 8, 24)
        ));

        lblLoadingStatus = new JLabel("Ready");
        lblLoadingStatus.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblLoadingStatus.setForeground(UITheme.TEXT_MUTED);

        progressBar = new JProgressBar();
        progressBar.setPreferredSize(new Dimension(140, 14));
        progressBar.setVisible(false);

        footer.add(lblLoadingStatus, BorderLayout.WEST);
        footer.add(progressBar, BorderLayout.EAST);

        return footer;
    }

    /**
     * Refreshes summary statistics asynchronously from the service layer.
     * All mathematical and database aggregations are performed by {@link ExpenseService}.
     */
    public void refreshDashboard() {
        int userId = SessionManager.getInstance().getCurrentUserId();
        if (userId <= 0) return;

        btnRefresh.setEnabled(false);
        progressBar.setIndeterminate(true);
        progressBar.setVisible(true);
        lblLoadingStatus.setText("Updating statistics from service layer...");

        SwingWorker<DashboardSummaryData, Void> worker = new SwingWorker<>() {
            @Override
            protected DashboardSummaryData doInBackground() {
                // All calculations delegated strictly to ExpenseService methods
                CompletableFuture<BigDecimal> futureTotal = CompletableFuture.supplyAsync(() -> {
                    try {
                        return expenseService.calculateTotalExpenses(userId);
                    } catch (ServiceException e) {
                        return BigDecimal.ZERO;
                    }
                });

                CompletableFuture<Integer> futureCount = CompletableFuture.supplyAsync(() -> {
                    try {
                        return expenseService.getExpenseCount(userId);
                    } catch (ServiceException e) {
                        return 0;
                    }
                });

                CompletableFuture<BigDecimal> futureMonth = CompletableFuture.supplyAsync(() -> {
                    try {
                        return expenseService.calculateCurrentMonthExpenses(userId);
                    } catch (ServiceException e) {
                        return BigDecimal.ZERO;
                    }
                });

                CompletableFuture.allOf(futureTotal, futureCount, futureMonth).join();

                BigDecimal total = futureTotal.getNow(BigDecimal.ZERO);
                int count = futureCount.getNow(0);
                BigDecimal monthTotal = futureMonth.getNow(BigDecimal.ZERO);

                return new DashboardSummaryData(total, count, monthTotal);
            }

            @Override
            protected void done() {
                btnRefresh.setEnabled(true);
                progressBar.setVisible(false);
                progressBar.setIndeterminate(false);

                try {
                    DashboardSummaryData data = get();
                    lblTotalValue.setText(formatCurrency(data.total));
                    lblCountValue.setText(data.count + (data.count == 1 ? " record" : " records"));
                    lblMonthValue.setText(formatCurrency(data.monthTotal));
                    lblLoadingStatus.setText("Dashboard up to date.");
                } catch (InterruptedException | ExecutionException ex) {
                    lblLoadingStatus.setText("Failed to retrieve dashboard totals.");
                }
            }
        };

        worker.execute();
    }

    private String formatCurrency(BigDecimal amount) {
        if (amount == null) return "₹ 0.00";
        return "₹ " + String.format(Locale.ENGLISH, "%,.2f", amount);
    }

    // ----------------------------------------------------------------
    // Navigation Action Handlers
    // ----------------------------------------------------------------

    private void openAddExpense() {
        SwingUtilities.invokeLater(() -> new AddExpenseFrame(this).setVisible(true));
    }

    private void openExpenseHistory() {
        SwingUtilities.invokeLater(() -> new ExpenseHistoryFrame().setVisible(true));
    }

    private void openManageExpenses() {
        SwingUtilities.invokeLater(() -> new ManageExpensesFrame(this).setVisible(true));
    }

    private void openCategorySummary() {
        SwingUtilities.invokeLater(() -> new CategorySummaryFrame().setVisible(true));
    }

    private void openDateFilter() {
        SwingUtilities.invokeLater(() -> new DateFilterFrame().setVisible(true));
    }

    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to log out?",
                "Logout Confirmation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (confirm == JOptionPane.YES_OPTION) {
            SessionManager.getInstance().logout();
            SwingUtilities.invokeLater(() -> {
                new LoginFrame().setVisible(true);
                dispose();
            });
        }
    }

    /**
     * DTO container for summary card metrics.
     */
    private static class DashboardSummaryData {
        final BigDecimal total;
        final int count;
        final BigDecimal monthTotal;

        DashboardSummaryData(BigDecimal total, int count, BigDecimal monthTotal) {
            this.total = (total != null) ? total : BigDecimal.ZERO;
            this.count = count;
            this.monthTotal = (monthTotal != null) ? monthTotal : BigDecimal.ZERO;
        }
    }
}
