package ui;

import model.Expense;
import service.ExpenseService;
import service.ExpenseServiceImpl;
import service.ServiceException;
import util.SessionManager;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Modern, clean Java Swing Expense Management Screen for ExpenseTracker.
 *
 * <h3>Design & Architecture</h3>
 * <ul>
 *   <li><b>Consistent Theme</b>: Styled table headers in Dark Blue, Edit (Blue) & Delete (Red) buttons.</li>
 *   <li><b>Secure Deletion</b>: Enforces {@code userId} ownership checks.</li>
 * </ul>
 */
public class ManageExpensesFrame extends JFrame {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MMM-yyyy");

    private static final String[] COLUMN_NAMES = {
            "ID", "Amount", "Category", "Date", "Description"
    };

    private final ExpenseService expenseService;
    private final DashboardFrame parentDashboard;
    private final List<Expense> loadedExpensesList = new ArrayList<>();

    private JTable table;
    private DefaultTableModel tableModel;
    private JLabel lblTotalSummary;
    private JLabel lblRecordCount;
    private JButton btnEdit;
    private JButton btnDelete;
    private JButton btnRefresh;
    private JButton btnClose;
    private JPanel tableContainer;
    private JPanel emptyStatePanel;

    public ManageExpensesFrame(DashboardFrame parentDashboard) {
        this.parentDashboard = parentDashboard;
        this.expenseService = new ExpenseServiceImpl();

        initFrame();
        initComponents();
        loadExpenses();
    }

    public ManageExpensesFrame() {
        this(null);
    }

    private void initFrame() {
        setTitle("Expense Tracker – Manage Expenses");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(920, 600);
        setMinimumSize(new Dimension(780, 500));
        setLocationRelativeTo(null); // Center on screen
        getContentPane().setBackground(UITheme.BG_LIGHT);
        setLayout(new BorderLayout());

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if (parentDashboard != null) {
                    parentDashboard.refreshDashboard();
                }
            }
        });
    }

    private void initComponents() {
        // 1. Top Header Banner
        JPanel topHeader = new JPanel(new BorderLayout());
        topHeader.setBackground(UITheme.BG_CARD);
        topHeader.setBorder(new CompoundBorder(
                new LineBorder(UITheme.BORDER_COLOR, 1, false),
                new EmptyBorder(16, 24, 16, 24)
        ));

        JLabel lblTitle = new JLabel("MANAGE EXPENSES");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.PRIMARY_DARK);

        JLabel lblSubtitle = new JLabel("Select any transaction record to modify details or remove permanently");
        lblSubtitle.setFont(UITheme.FONT_SUBTITLE);
        lblSubtitle.setForeground(UITheme.TEXT_MUTED);

        JPanel headerText = new JPanel();
        headerText.setLayout(new BoxLayout(headerText, BoxLayout.Y_AXIS));
        headerText.setBackground(UITheme.BG_CARD);
        headerText.add(lblTitle);
        headerText.add(Box.createVerticalStrut(2));
        headerText.add(lblSubtitle);

        topHeader.add(headerText, BorderLayout.WEST);

        // Top-right summary indicators
        JPanel summaryBadges = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        summaryBadges.setBackground(UITheme.BG_CARD);

        lblRecordCount = new JLabel("0 records");
        lblRecordCount.setFont(UITheme.FONT_SUBTITLE);
        lblRecordCount.setForeground(UITheme.TEXT_MUTED);

        lblTotalSummary = new JLabel("Total: ₹ 0.00");
        lblTotalSummary.setFont(UITheme.FONT_SECTION);
        lblTotalSummary.setForeground(UITheme.PRIMARY_DARK);

        summaryBadges.add(lblRecordCount);
        summaryBadges.add(lblTotalSummary);

        topHeader.add(summaryBadges, BorderLayout.EAST);

        // 2. Table & Model Setup
        tableModel = new DefaultTableModel(COLUMN_NAMES, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        UITheme.styleTable(table);
        setupColumnRenderers();

        JScrollPane tableScrollPane = new JScrollPane(table);
        tableScrollPane.setBorder(new LineBorder(UITheme.BORDER_COLOR, 1));
        tableScrollPane.getViewport().setBackground(Color.WHITE);

        emptyStatePanel = createEmptyStatePanel();

        tableContainer = new JPanel(new CardLayout());
        tableContainer.setBackground(UITheme.BG_LIGHT);
        tableContainer.setBorder(new EmptyBorder(16, 24, 8, 24));
        tableContainer.add(tableScrollPane, "TABLE");
        tableContainer.add(emptyStatePanel, "EMPTY");

        // 3. Bottom Action Bar
        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setBackground(UITheme.BG_CARD);
        bottomBar.setBorder(new CompoundBorder(
                new LineBorder(UITheme.BORDER_COLOR, 1, false),
                new EmptyBorder(12, 24, 12, 24)
        ));

        JPanel leftActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftActions.setBackground(UITheme.BG_CARD);

        btnEdit = UITheme.createPrimaryButton("✏️ Edit Selected");
        btnDelete = UITheme.createDangerButton("🗑️ Delete Selected");

        btnEdit.addActionListener(e -> handleEditSelected());
        btnDelete.addActionListener(e -> handleDeleteSelected());

        leftActions.add(btnEdit);
        leftActions.add(btnDelete);

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightActions.setBackground(UITheme.BG_CARD);

        btnRefresh = UITheme.createPrimaryButton("Refresh");
        btnClose = UITheme.createSecondaryButton("Close");

        btnRefresh.addActionListener(e -> loadExpenses());
        btnClose.addActionListener(e -> dispose());

        rightActions.add(btnRefresh);
        rightActions.add(btnClose);

        bottomBar.add(leftActions, BorderLayout.WEST);
        bottomBar.add(rightActions, BorderLayout.EAST);

        add(topHeader, BorderLayout.NORTH);
        add(tableContainer, BorderLayout.CENTER);
        add(bottomBar, BorderLayout.SOUTH);
    }

    private void setupColumnRenderers() {
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int col) {
                Component c = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, col);
                if (!isSelected) {
                    c.setBackground((row % 2 == 0) ? Color.WHITE : UITheme.ROW_ALT_COLOR);
                    c.setForeground(UITheme.TEXT_MAIN);
                } else {
                    c.setBackground(UITheme.SELECTION_COLOR);
                    c.setForeground(UITheme.PRIMARY_DARK);
                }
                setBorder(new EmptyBorder(0, 8, 0, 8));

                if (col == 0 || col == 3) {
                    setHorizontalAlignment(SwingConstants.CENTER);
                } else if (col == 1) {
                    setHorizontalAlignment(SwingConstants.RIGHT);
                } else {
                    setHorizontalAlignment(SwingConstants.LEFT);
                }
                return c;
            }
        });

        table.getColumnModel().getColumn(0).setPreferredWidth(60);
        table.getColumnModel().getColumn(0).setMaxWidth(80);
        table.getColumnModel().getColumn(1).setPreferredWidth(120);
        table.getColumnModel().getColumn(2).setPreferredWidth(130);
        table.getColumnModel().getColumn(3).setPreferredWidth(120);
        table.getColumnModel().getColumn(4).setPreferredWidth(350);
    }

    private JPanel createEmptyStatePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(UITheme.BG_CARD);
        panel.setBorder(new CompoundBorder(
                new LineBorder(UITheme.BORDER_COLOR, 1, true),
                new EmptyBorder(40, 20, 40, 20)
        ));

        JLabel lblIcon = new JLabel("🗑️");
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));
        lblIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblEmptyTitle = new JLabel("No Expenses Available to Manage");
        lblEmptyTitle.setFont(UITheme.FONT_TITLE);
        lblEmptyTitle.setForeground(UITheme.TEXT_MAIN);
        lblEmptyTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblEmptyDesc = new JLabel("Your expense ledger is empty. Add new expenses from the dashboard to edit or delete them.");
        lblEmptyDesc.setFont(UITheme.FONT_SUBTITLE);
        lblEmptyDesc.setForeground(UITheme.TEXT_MUTED);
        lblEmptyDesc.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(Box.createVerticalGlue());
        panel.add(lblIcon);
        panel.add(Box.createVerticalStrut(10));
        panel.add(lblEmptyTitle);
        panel.add(Box.createVerticalStrut(6));
        panel.add(lblEmptyDesc);
        panel.add(Box.createVerticalGlue());

        return panel;
    }

    public void loadExpenses() {
        int userId = SessionManager.getInstance().getCurrentUserId();
        if (userId <= 0) {
            showError("User session expired. Please log in again.");
            return;
        }

        btnRefresh.setEnabled(false);

        SwingWorker<List<Expense>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Expense> doInBackground() throws Exception {
                return expenseService.getAllExpenses(userId);
            }

            @Override
            protected void done() {
                btnRefresh.setEnabled(true);
                try {
                    List<Expense> expenses = get();
                    loadedExpensesList.clear();
                    tableModel.setRowCount(0);

                    CardLayout cardLayout = (CardLayout) tableContainer.getLayout();

                    if (expenses == null || expenses.isEmpty()) {
                        lblRecordCount.setText("0 records");
                        lblTotalSummary.setText("Total: ₹ 0.00");
                        btnEdit.setEnabled(false);
                        btnDelete.setEnabled(false);
                        cardLayout.show(tableContainer, "EMPTY");
                    } else {
                        loadedExpensesList.addAll(expenses);
                        BigDecimal total = BigDecimal.ZERO;

                        for (Expense exp : expenses) {
                            String formattedAmount = formatCurrency(exp.getAmount());
                            String formattedDate = (exp.getExpenseDate() != null)
                                    ? exp.getExpenseDate().format(DATE_FORMATTER)
                                    : "N/A";
                            String desc = (exp.getDescription() != null && !exp.getDescription().isEmpty())
                                    ? exp.getDescription()
                                    : "—";

                            tableModel.addRow(new Object[]{
                                    exp.getId(),
                                    formattedAmount,
                                    exp.getCategory(),
                                    formattedDate,
                                    desc
                            });

                            if (exp.getAmount() != null) {
                                total = total.add(exp.getAmount());
                            }
                        }

                        lblRecordCount.setText(expenses.size() + (expenses.size() == 1 ? " record" : " records"));
                        lblTotalSummary.setText("Total: " + formatCurrency(total));
                        btnEdit.setEnabled(true);
                        btnDelete.setEnabled(true);
                        cardLayout.show(tableContainer, "TABLE");
                    }
                } catch (Exception ex) {
                    showError("Failed to load expenses: " + ex.getMessage());
                }
            }
        };

        worker.execute();
    }

    private void handleEditSelected() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            showWarning("Please select an expense from the table to edit.");
            return;
        }

        if (selectedRow < loadedExpensesList.size()) {
            Expense selectedExpense = loadedExpensesList.get(selectedRow);
            SwingUtilities.invokeLater(() -> {
                new EditExpenseFrame(selectedExpense, this::loadExpenses).setVisible(true);
            });
        }
    }

    private void handleDeleteSelected() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            showWarning("Please select an expense from the table to delete.");
            return;
        }

        int userId = SessionManager.getInstance().getCurrentUserId();
        if (userId <= 0) {
            showError("User session is not active. Please log in again.");
            return;
        }

        int expenseId = (int) tableModel.getValueAt(selectedRow, 0);
        String amountStr = (String) tableModel.getValueAt(selectedRow, 1);
        String category = (String) tableModel.getValueAt(selectedRow, 2);

        int confirm = JOptionPane.showConfirmDialog(
                this,
                String.format("Are you sure you want to delete this expense?\n\nID: #%d\nCategory: %s\nAmount: %s",
                        expenseId, category, amountStr),
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        btnDelete.setEnabled(false);
        btnEdit.setEnabled(false);

        SwingWorker<Boolean, Void> deleteWorker = new SwingWorker<>() {
            private String errorMessage = null;

            @Override
            protected Boolean doInBackground() {
                try {
                    return expenseService.deleteExpense(expenseId, userId);
                } catch (ServiceException ex) {
                    errorMessage = ex.getMessage();
                    return false;
                }
            }

            @Override
            protected void done() {
                try {
                    boolean success = get();
                    if (success) {
                        JOptionPane.showMessageDialog(
                                ManageExpensesFrame.this,
                                "Expense #" + expenseId + " has been deleted successfully.",
                                "Expense Deleted",
                                JOptionPane.INFORMATION_MESSAGE
                        );
                        loadExpenses();
                        if (parentDashboard != null) {
                            parentDashboard.refreshDashboard();
                        }
                    } else {
                        String msg = (errorMessage != null)
                                ? errorMessage
                                : "Failed to delete the selected expense. Please try again.";
                        showError(msg);
                    }
                } catch (Exception ex) {
                    showError("An error occurred during deletion: " + ex.getMessage());
                }
            }
        };

        deleteWorker.execute();
    }

    private String formatCurrency(BigDecimal amount) {
        if (amount == null) return "₹ 0.00";
        return "₹ " + String.format(Locale.ENGLISH, "%,.2f", amount);
    }

    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "Action Required", JOptionPane.WARNING_MESSAGE);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
