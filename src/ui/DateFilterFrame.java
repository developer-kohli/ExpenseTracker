package ui;

import model.Expense;
import service.ExpenseService;
import service.ExpenseServiceImpl;
import util.SessionManager;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

/**
 * Modern, clean Java Swing Date Range Filtering Screen for ExpenseTracker.
 *
 * <h3>Design & Architecture</h3>
 * <ul>
 *   <li><b>Consistent Theme</b>: Styled table headers in Dark Blue, alternating row colors.</li>
 *   <li><b>Date Filtering</b>: Safe range queries via {@link ExpenseService}.</li>
 * </ul>
 */
public class DateFilterFrame extends JFrame {

    private static final DateTimeFormatter DISPLAY_DATE_FMT = DateTimeFormatter.ofPattern("dd-MMM-yyyy");

    private static final String[] COLUMN_NAMES = {
            "ID", "Amount", "Category", "Date", "Description"
    };

    private final ExpenseService expenseService;

    private JTextField txtStartDate;
    private JTextField txtEndDate;
    private JButton btnSearch;
    private JButton btnClear;
    private JButton btnClose;

    private JTable table;
    private DefaultTableModel tableModel;
    private JLabel lblRangeTotal;
    private JLabel lblRecordCount;
    private JLabel lblFooter;
    private JProgressBar progressBar;
    private JPanel tableContainer;
    private JPanel emptyStatePanel;

    public DateFilterFrame() {
        this.expenseService = new ExpenseServiceImpl();

        initFrame();
        initComponents();
        handleSearch();
    }

    private void initFrame() {
        setTitle("Expense Tracker – Date Range Filter");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(900, 600);
        setMinimumSize(new Dimension(760, 500));
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

        JLabel lblTitle = new JLabel("DATE RANGE FILTER");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.PRIMARY_DARK);

        JLabel lblSubtitle = new JLabel("Query and inspect transactions within custom calendar dates");
        lblSubtitle.setFont(UITheme.FONT_SUBTITLE);
        lblSubtitle.setForeground(UITheme.TEXT_MUTED);

        JPanel headerText = new JPanel();
        headerText.setLayout(new BoxLayout(headerText, BoxLayout.Y_AXIS));
        headerText.setBackground(UITheme.BG_CARD);
        headerText.add(lblTitle);
        headerText.add(Box.createVerticalStrut(2));
        headerText.add(lblSubtitle);

        topHeader.add(headerText, BorderLayout.WEST);

        JPanel summaryBadges = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        summaryBadges.setBackground(UITheme.BG_CARD);

        lblRecordCount = new JLabel("0 records");
        lblRecordCount.setFont(UITheme.FONT_SUBTITLE);
        lblRecordCount.setForeground(UITheme.TEXT_MUTED);

        lblRangeTotal = new JLabel("Range Total: ₹ 0.00");
        lblRangeTotal.setFont(UITheme.FONT_SECTION);
        lblRangeTotal.setForeground(UITheme.PRIMARY_DARK);

        summaryBadges.add(lblRecordCount);
        summaryBadges.add(lblRangeTotal);

        topHeader.add(summaryBadges, BorderLayout.EAST);

        // 2. Filter Controls Card
        JPanel filterCard = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        filterCard.setBackground(UITheme.BG_CARD);
        filterCard.setBorder(new CompoundBorder(
                new LineBorder(UITheme.BORDER_COLOR, 1, false),
                new EmptyBorder(8, 24, 8, 24)
        ));

        JLabel lblStart = new JLabel("Start Date (YYYY-MM-DD):");
        lblStart.setFont(UITheme.FONT_LABEL);
        lblStart.setForeground(UITheme.TEXT_MAIN);

        LocalDate now = LocalDate.now();
        txtStartDate = UITheme.createTextField(10);
        txtStartDate.setText(now.withDayOfMonth(1).toString());

        JLabel lblEnd = new JLabel("End Date (YYYY-MM-DD):");
        lblEnd.setFont(UITheme.FONT_LABEL);
        lblEnd.setForeground(UITheme.TEXT_MAIN);

        txtEndDate = UITheme.createTextField(10);
        txtEndDate.setText(now.toString());

        btnSearch = UITheme.createPrimaryButton("🔍 Search");
        btnClear  = UITheme.createSecondaryButton("🔄 Reset");

        btnSearch.addActionListener(e -> handleSearch());
        btnClear.addActionListener(e -> handleReset());

        filterCard.add(lblStart);
        filterCard.add(txtStartDate);
        filterCard.add(lblEnd);
        filterCard.add(txtEndDate);
        filterCard.add(btnSearch);
        filterCard.add(btnClear);

        // 3. Table & Model Setup
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

        // 4. Bottom Action Bar
        JPanel bottomBar = new JPanel(new BorderLayout(10, 0));
        bottomBar.setBackground(UITheme.BG_CARD);
        bottomBar.setBorder(new CompoundBorder(
                new LineBorder(UITheme.BORDER_COLOR, 1, false),
                new EmptyBorder(12, 24, 12, 24)
        ));

        JPanel leftStatusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftStatusPanel.setBackground(UITheme.BG_CARD);

        lblFooter = new JLabel("Ready");
        lblFooter.setFont(UITheme.FONT_SUBTITLE);
        lblFooter.setForeground(UITheme.TEXT_MUTED);

        progressBar = new JProgressBar();
        progressBar.setPreferredSize(new Dimension(130, 14));
        progressBar.setVisible(false);

        leftStatusPanel.add(lblFooter);
        leftStatusPanel.add(progressBar);

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightActions.setBackground(UITheme.BG_CARD);

        btnClose = UITheme.createSecondaryButton("Close");
        btnClose.addActionListener(e -> dispose());
        rightActions.add(btnClose);

        bottomBar.add(leftStatusPanel, BorderLayout.WEST);
        bottomBar.add(rightActions, BorderLayout.EAST);

        JPanel northWrapper = new JPanel();
        northWrapper.setLayout(new BoxLayout(northWrapper, BoxLayout.Y_AXIS));
        northWrapper.add(topHeader);
        northWrapper.add(filterCard);

        add(northWrapper, BorderLayout.NORTH);
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

        JLabel lblIcon = new JLabel("📅");
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));
        lblIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblEmptyTitle = new JLabel("No Expenses in Selected Date Range");
        lblEmptyTitle.setFont(UITheme.FONT_TITLE);
        lblEmptyTitle.setForeground(UITheme.TEXT_MAIN);
        lblEmptyTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblEmptyDesc = new JLabel("No transactions were found between the specified start and end dates.");
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

    private void handleSearch() {
        int userId = SessionManager.getInstance().getCurrentUserId();
        if (userId <= 0) {
            JOptionPane.showMessageDialog(this, "User session expired. Please log in again.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String startStr = txtStartDate.getText().trim();
        String endStr   = txtEndDate.getText().trim();

        if (startStr.isEmpty()) {
            showWarning("Please enter a Start Date.");
            txtStartDate.requestFocusInWindow();
            return;
        }

        if (endStr.isEmpty()) {
            showWarning("Please enter an End Date.");
            txtEndDate.requestFocusInWindow();
            return;
        }

        LocalDate startDate;
        LocalDate endDate;

        try {
            startDate = LocalDate.parse(startStr, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException ex) {
            showWarning("Invalid Start Date format. Please use YYYY-MM-DD (e.g. 2026-10-01).");
            txtStartDate.requestFocusInWindow();
            return;
        }

        try {
            endDate = LocalDate.parse(endStr, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException ex) {
            showWarning("Invalid End Date format. Please use YYYY-MM-DD (e.g. 2026-10-01).");
            txtEndDate.requestFocusInWindow();
            return;
        }

        if (startDate.isAfter(endDate)) {
            showWarning("Start Date cannot be after End Date.\nStart: " + startDate + "\nEnd: " + endDate);
            txtStartDate.requestFocusInWindow();
            return;
        }

        btnSearch.setEnabled(false);
        progressBar.setIndeterminate(true);
        progressBar.setVisible(true);
        lblFooter.setText("Filtering expenses between " + startDate + " and " + endDate + "...");

        SwingWorker<List<Expense>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Expense> doInBackground() throws Exception {
                return expenseService.getExpensesByDateRange(userId, startDate, endDate);
            }

            @Override
            protected void done() {
                btnSearch.setEnabled(true);
                progressBar.setVisible(false);
                progressBar.setIndeterminate(false);

                try {
                    List<Expense> expenses = get();
                    tableModel.setRowCount(0);
                    CardLayout cardLayout = (CardLayout) tableContainer.getLayout();

                    if (expenses == null || expenses.isEmpty()) {
                        lblRecordCount.setText("0 records");
                        lblRangeTotal.setText("Range Total: ₹ 0.00");
                        lblFooter.setText("No records found for the specified range.");
                        cardLayout.show(tableContainer, "EMPTY");
                    } else {
                        BigDecimal total = BigDecimal.ZERO;

                        for (Expense exp : expenses) {
                            String formattedAmount = formatCurrency(exp.getAmount());
                            String formattedDate = (exp.getExpenseDate() != null)
                                    ? exp.getExpenseDate().format(DISPLAY_DATE_FMT)
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
                        lblRangeTotal.setText("Range Total: " + formatCurrency(total));
                        lblFooter.setText(String.format("Displaying %d record(s) matching date range.", expenses.size()));
                        cardLayout.show(tableContainer, "TABLE");
                    }
                } catch (InterruptedException | ExecutionException ex) {
                    lblFooter.setText("Failed to filter expenses.");
                    JOptionPane.showMessageDialog(DateFilterFrame.this,
                            "Failed to filter expenses: " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        worker.execute();
    }

    private void handleReset() {
        LocalDate now = LocalDate.now();
        txtStartDate.setText(now.withDayOfMonth(1).toString());
        txtEndDate.setText(now.toString());
        handleSearch();
    }

    private String formatCurrency(BigDecimal amount) {
        if (amount == null) return "₹ 0.00";
        return "₹ " + String.format(Locale.ENGLISH, "%,.2f", amount);
    }

    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "Validation Error", JOptionPane.WARNING_MESSAGE);
    }
}
