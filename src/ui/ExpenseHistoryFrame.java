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
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

/**
 * Modern, clean Java Swing Expense History Screen for ExpenseTracker.
 *
 * <h3>Design & Architecture</h3>
 * <ul>
 *   <li><b>Consistent Theme</b>: Styled table headers in Dark Blue, alternating row colors.</li>
 *   <li><b>Responsiveness</b>: Non-blocking asynchronous loading with {@link SwingWorker}.</li>
 *   <li><b>Multi-Tenant Security</b>: Scoped to active {@link SessionManager} user.</li>
 * </ul>
 */
public class ExpenseHistoryFrame extends JFrame {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MMM-yyyy");

    private static final String[] COLUMN_NAMES = {
            "ID", "Amount", "Category", "Date", "Description"
    };

    private final ExpenseService expenseService;

    private JTable table;
    private DefaultTableModel tableModel;
    private JLabel lblStatus;
    private JProgressBar progressBar;
    private JButton btnRefresh;
    private JButton btnClose;
    private JPanel emptyStatePanel;
    private JPanel tableContainer;

    public ExpenseHistoryFrame() {
        this.expenseService = new ExpenseServiceImpl();

        initFrame();
        initComponents();
        loadExpenses();
    }

    private void initFrame() {
        setTitle("Expense Tracker – Expense History");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(880, 560);
        setMinimumSize(new Dimension(740, 480));
        setLocationRelativeTo(null); // Center on screen
        getContentPane().setBackground(UITheme.BG_LIGHT);
        setLayout(new BorderLayout());
    }

    private void initComponents() {
        // 1. Top Header Banner
        JPanel topHeader = new JPanel(new BorderLayout());
        topHeader.setBackground(UITheme.BG_CARD);
        topHeader.setBorder(new CompoundBorder(
                new LineBorder(UITheme.BORDER_COLOR, 1, false),
                new EmptyBorder(16, 24, 16, 24)
        ));

        JLabel lblTitle = new JLabel("EXPENSE HISTORY");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.PRIMARY_DARK);

        JLabel lblSubtitle = new JLabel("Complete transaction ledger for your account");
        lblSubtitle.setFont(UITheme.FONT_SUBTITLE);
        lblSubtitle.setForeground(UITheme.TEXT_MUTED);

        JPanel headerText = new JPanel();
        headerText.setLayout(new BoxLayout(headerText, BoxLayout.Y_AXIS));
        headerText.setBackground(UITheme.BG_CARD);
        headerText.add(lblTitle);
        headerText.add(Box.createVerticalStrut(2));
        headerText.add(lblSubtitle);

        topHeader.add(headerText, BorderLayout.WEST);

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
        JPanel bottomBar = new JPanel(new BorderLayout(10, 0));
        bottomBar.setBackground(UITheme.BG_CARD);
        bottomBar.setBorder(new CompoundBorder(
                new LineBorder(UITheme.BORDER_COLOR, 1, false),
                new EmptyBorder(12, 24, 12, 24)
        ));

        JPanel leftStatusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftStatusPanel.setBackground(UITheme.BG_CARD);

        lblStatus = new JLabel("Loading expenses...");
        lblStatus.setFont(UITheme.FONT_SUBTITLE);
        lblStatus.setForeground(UITheme.TEXT_MUTED);

        progressBar = new JProgressBar();
        progressBar.setPreferredSize(new Dimension(130, 14));
        progressBar.setVisible(false);

        leftStatusPanel.add(lblStatus);
        leftStatusPanel.add(progressBar);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(UITheme.BG_CARD);

        btnRefresh = UITheme.createPrimaryButton("Refresh");
        btnClose   = UITheme.createSecondaryButton("Close");

        btnRefresh.addActionListener(e -> loadExpenses());
        btnClose.addActionListener(e -> dispose());

        buttonPanel.add(btnRefresh);
        buttonPanel.add(btnClose);

        bottomBar.add(leftStatusPanel, BorderLayout.WEST);
        bottomBar.add(buttonPanel, BorderLayout.EAST);

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

        JLabel lblIcon = new JLabel("📝");
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));
        lblIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblEmptyTitle = new JLabel("No Expenses Recorded Yet");
        lblEmptyTitle.setFont(UITheme.FONT_TITLE);
        lblEmptyTitle.setForeground(UITheme.TEXT_MAIN);
        lblEmptyTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblEmptyDesc = new JLabel("You have not added any expense entries. Click 'Add Expense' on your dashboard to record one.");
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
            lblStatus.setText("Error: User session expired. Please log in again.");
            return;
        }

        btnRefresh.setEnabled(false);
        progressBar.setIndeterminate(true);
        progressBar.setVisible(true);
        lblStatus.setText("Fetching expenses from database...");

        SwingWorker<List<Expense>, Void> worker = new SwingWorker<>() {
            private String errorMessage = null;

            @Override
            protected List<Expense> doInBackground() {
                try {
                    return expenseService.getAllExpenses(userId);
                } catch (ServiceException e) {
                    errorMessage = e.getMessage();
                    return null;
                }
            }

            @Override
            protected void done() {
                btnRefresh.setEnabled(true);
                progressBar.setVisible(false);
                progressBar.setIndeterminate(false);

                try {
                    List<Expense> expenses = get();
                    tableModel.setRowCount(0);

                    CardLayout cardLayout = (CardLayout) tableContainer.getLayout();

                    if (expenses == null) {
                        lblStatus.setText("Failed to load expenses: " + (errorMessage != null ? errorMessage : "Unknown error"));
                        cardLayout.show(tableContainer, "EMPTY");
                        return;
                    }

                    if (expenses.isEmpty()) {
                        lblStatus.setText("No expense records found.");
                        cardLayout.show(tableContainer, "EMPTY");
                    } else {
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

                        lblStatus.setText(String.format("Showing %d record(s) | Total: %s",
                                expenses.size(), formatCurrency(total)));
                        cardLayout.show(tableContainer, "TABLE");
                    }
                } catch (InterruptedException | ExecutionException ex) {
                    lblStatus.setText("Unexpected error loading data: " + ex.getMessage());
                }
            }
        };

        worker.execute();
    }

    private String formatCurrency(BigDecimal amount) {
        if (amount == null) return "₹ 0.00";
        return "₹ " + String.format(Locale.ENGLISH, "%,.2f", amount);
    }
}
