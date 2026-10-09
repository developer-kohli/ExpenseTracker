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
import java.math.RoundingMode;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Modern, clean Java Swing Category Summary Screen for ExpenseTracker.
 *
 * <h3>Design & Architecture</h3>
 * <ul>
 *   <li><b>Consistent Theme</b>: Styled Dark Blue table headers, alternating row colors.</li>
 *   <li><b>Asynchronous Aggregation</b>: Runs background category calculations via {@link SwingWorker}.</li>
 * </ul>
 */
public class CategorySummaryFrame extends JFrame {

    private static final String[] COLUMN_NAMES = {
            "Category", "Transactions", "Total Amount", "Share of Total"
    };

    private final ExpenseService expenseService;

    private JTable table;
    private DefaultTableModel tableModel;
    private JLabel lblGrandTotal;
    private JLabel lblTopCategory;
    private JLabel lblFooterNote;
    private JProgressBar progressBar;
    private JButton btnRefresh;
    private JButton btnClose;
    private JPanel tableContainer;
    private JPanel emptyStatePanel;

    public CategorySummaryFrame() {
        this.expenseService = new ExpenseServiceImpl();

        initFrame();
        initComponents();
        loadCategorySummary();
    }

    private void initFrame() {
        setTitle("Expense Tracker – Category Summary");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(800, 540);
        setMinimumSize(new Dimension(680, 460));
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

        JLabel lblTitle = new JLabel("CATEGORY SUMMARY");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.PRIMARY_DARK);

        JLabel lblSubtitle = new JLabel("Breakdown of total expenses aggregated by category");
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

        lblTopCategory = new JLabel("Top: —");
        lblTopCategory.setFont(UITheme.FONT_SUBTITLE);
        lblTopCategory.setForeground(UITheme.TEXT_MUTED);

        lblGrandTotal = new JLabel("Grand Total: ₹ 0.00");
        lblGrandTotal.setFont(UITheme.FONT_SECTION);
        lblGrandTotal.setForeground(UITheme.PRIMARY_DARK);

        summaryBadges.add(lblTopCategory);
        summaryBadges.add(lblGrandTotal);

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
        JPanel bottomBar = new JPanel(new BorderLayout(10, 0));
        bottomBar.setBackground(UITheme.BG_CARD);
        bottomBar.setBorder(new CompoundBorder(
                new LineBorder(UITheme.BORDER_COLOR, 1, false),
                new EmptyBorder(12, 24, 12, 24)
        ));

        JPanel leftStatusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftStatusPanel.setBackground(UITheme.BG_CARD);

        lblFooterNote = new JLabel("Loading category statistics...");
        lblFooterNote.setFont(UITheme.FONT_SUBTITLE);
        lblFooterNote.setForeground(UITheme.TEXT_MUTED);

        progressBar = new JProgressBar();
        progressBar.setPreferredSize(new Dimension(130, 14));
        progressBar.setVisible(false);

        leftStatusPanel.add(lblFooterNote);
        leftStatusPanel.add(progressBar);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(UITheme.BG_CARD);

        btnRefresh = UITheme.createPrimaryButton("Refresh");
        btnClose   = UITheme.createSecondaryButton("Close");

        btnRefresh.addActionListener(e -> loadCategorySummary());
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
                setBorder(new EmptyBorder(0, 10, 0, 10));

                if (col == 0) {
                    setHorizontalAlignment(SwingConstants.LEFT);
                    setFont(new Font("Segoe UI", Font.BOLD, 13));
                } else if (col == 1) {
                    setHorizontalAlignment(SwingConstants.CENTER);
                    setFont(UITheme.FONT_TABLE_ROW);
                } else if (col == 2) {
                    setHorizontalAlignment(SwingConstants.RIGHT);
                    setFont(new Font("Segoe UI", Font.BOLD, 13));
                } else {
                    setHorizontalAlignment(SwingConstants.RIGHT);
                    setFont(UITheme.FONT_TABLE_ROW);
                }
                return c;
            }
        });

        table.getColumnModel().getColumn(0).setPreferredWidth(200);
        table.getColumnModel().getColumn(1).setPreferredWidth(120);
        table.getColumnModel().getColumn(2).setPreferredWidth(180);
        table.getColumnModel().getColumn(3).setPreferredWidth(120);
    }

    private JPanel createEmptyStatePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(UITheme.BG_CARD);
        panel.setBorder(new CompoundBorder(
                new LineBorder(UITheme.BORDER_COLOR, 1, true),
                new EmptyBorder(40, 20, 40, 20)
        ));

        JLabel lblIcon = new JLabel("📊");
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));
        lblIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblEmptyTitle = new JLabel("No Category Data Available");
        lblEmptyTitle.setFont(UITheme.FONT_TITLE);
        lblEmptyTitle.setForeground(UITheme.TEXT_MAIN);
        lblEmptyTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblEmptyDesc = new JLabel("There are no recorded expenses to calculate category totals.");
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

    public void loadCategorySummary() {
        int userId = SessionManager.getInstance().getCurrentUserId();
        if (userId <= 0) {
            JOptionPane.showMessageDialog(this, "User session expired. Please log in again.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        btnRefresh.setEnabled(false);
        progressBar.setIndeterminate(true);
        progressBar.setVisible(true);
        lblFooterNote.setText("Aggregating category totals from database...");

        SwingWorker<CategoryAggregatedResult, Void> worker = new SwingWorker<>() {
            @Override
            protected CategoryAggregatedResult doInBackground() throws Exception {
                List<Expense> expenses = expenseService.getAllExpenses(userId);
                if (expenses == null || expenses.isEmpty()) {
                    return new CategoryAggregatedResult(Collections.emptyList(), BigDecimal.ZERO, null);
                }

                BigDecimal grandTotal = BigDecimal.ZERO;
                Map<String, CategoryStat> statMap = new LinkedHashMap<>();

                for (Expense exp : expenses) {
                    String cat = (exp.getCategory() != null && !exp.getCategory().trim().isEmpty())
                            ? exp.getCategory().trim()
                            : "Uncategorized";

                    BigDecimal amt = (exp.getAmount() != null) ? exp.getAmount() : BigDecimal.ZERO;
                    grandTotal = grandTotal.add(amt);

                    CategoryStat stat = statMap.computeIfAbsent(cat, k -> new CategoryStat(k));
                    stat.count++;
                    stat.total = stat.total.add(amt);
                }

                List<CategoryStat> sortedStats = new ArrayList<>(statMap.values());
                sortedStats.sort((a, b) -> b.total.compareTo(a.total));

                CategoryStat topStat = sortedStats.isEmpty() ? null : sortedStats.get(0);
                return new CategoryAggregatedResult(sortedStats, grandTotal, topStat);
            }

            @Override
            protected void done() {
                btnRefresh.setEnabled(true);
                progressBar.setVisible(false);
                progressBar.setIndeterminate(false);

                try {
                    CategoryAggregatedResult result = get();
                    tableModel.setRowCount(0);
                    CardLayout cardLayout = (CardLayout) tableContainer.getLayout();

                    if (result.stats.isEmpty() || result.grandTotal.compareTo(BigDecimal.ZERO) <= 0) {
                        lblGrandTotal.setText("Grand Total: ₹ 0.00");
                        lblTopCategory.setText("Top: —");
                        lblFooterNote.setText("No recorded expenses found.");
                        cardLayout.show(tableContainer, "EMPTY");
                    } else {
                        lblGrandTotal.setText("Grand Total: " + formatCurrency(result.grandTotal));
                        if (result.topCategory != null) {
                            lblTopCategory.setText(String.format("Top: %s (%s)",
                                    result.topCategory.category,
                                    formatCurrency(result.topCategory.total)));
                        }

                        for (CategoryStat stat : result.stats) {
                            String formattedAmount = formatCurrency(stat.total);
                            String countStr = stat.count + (stat.count == 1 ? " record" : " records");

                            String shareStr = "0.0%";
                            if (result.grandTotal.compareTo(BigDecimal.ZERO) > 0) {
                                BigDecimal percentage = stat.total
                                        .multiply(new BigDecimal("100"))
                                        .divide(result.grandTotal, 1, RoundingMode.HALF_UP);
                                shareStr = percentage.toPlainString() + "%";
                            }

                            tableModel.addRow(new Object[]{
                                    stat.category,
                                    countStr,
                                    formattedAmount,
                                    shareStr
                            });
                        }

                        lblFooterNote.setText(String.format("Calculated %d categories across %s total spend.",
                                result.stats.size(), formatCurrency(result.grandTotal)));
                        cardLayout.show(tableContainer, "TABLE");
                    }
                } catch (InterruptedException | ExecutionException ex) {
                    lblFooterNote.setText("Failed to calculate category totals.");
                    JOptionPane.showMessageDialog(CategorySummaryFrame.this,
                            "Failed to load category summary: " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        worker.execute();
    }

    private String formatCurrency(BigDecimal amount) {
        if (amount == null) return "₹ 0.00";
        return "₹ " + String.format(Locale.ENGLISH, "%,.2f", amount);
    }

    private static class CategoryStat {
        final String category;
        int count = 0;
        BigDecimal total = BigDecimal.ZERO;

        CategoryStat(String category) {
            this.category = category;
        }
    }

    private static class CategoryAggregatedResult {
        final List<CategoryStat> stats;
        final BigDecimal grandTotal;
        final CategoryStat topCategory;

        CategoryAggregatedResult(List<CategoryStat> stats, BigDecimal grandTotal, CategoryStat topCategory) {
            this.stats = stats;
            this.grandTotal = grandTotal;
            this.topCategory = topCategory;
        }
    }
}
