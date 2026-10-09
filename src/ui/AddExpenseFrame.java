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
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Modern, clean Java Swing Add Expense Screen for ExpenseTracker.
 *
 * <h3>Design & Architecture</h3>
 * <ul>
 *   <li><b>Consistent Theme</b>: Dark Blue header, Emerald save button, Light Gray background.</li>
 *   <li><b>Layout Hygiene</b>: Centered card layout with responsive padding.</li>
 *   <li><b>Service Integration</b>: Enforces persistence and validation via {@link ExpenseService}.</li>
 * </ul>
 */
public class AddExpenseFrame extends JFrame {

    private static final String[] CATEGORIES = {
            "Food",
            "Travel",
            "Shopping",
            "Education",
            "Entertainment",
            "Bills",
            "Health",
            "Rent",
            "Other"
    };

    private final ExpenseService expenseService;
    private final DashboardFrame parentDashboard;

    private JTextField txtAmount;
    private JComboBox<String> cmbCategory;
    private JTextField txtDate;
    private JTextArea txtDescription;
    private JButton btnSave;
    private JButton btnClear;
    private JButton btnCancel;

    public AddExpenseFrame(DashboardFrame parentDashboard) {
        this.parentDashboard = parentDashboard;
        this.expenseService = new ExpenseServiceImpl();

        initFrame();
        initComponents();
    }

    public AddExpenseFrame() {
        this(null);
    }

    private void initFrame() {
        setTitle("Expense Tracker – Add Expense");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(480, 580);
        setMinimumSize(new Dimension(440, 540));
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
        JPanel outerWrapper = new JPanel(new GridBagLayout());
        outerWrapper.setBackground(UITheme.BG_LIGHT);
        outerWrapper.setBorder(new EmptyBorder(15, 20, 15, 20));

        JPanel cardPanel = UITheme.createCardPanel();
        cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.Y_AXIS));

        // 1. Header
        JLabel lblTitle = new JLabel("ADD NEW EXPENSE");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.PRIMARY_DARK);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSubtitle = new JLabel("Record a financial transaction into your account");
        lblSubtitle.setFont(UITheme.FONT_SUBTITLE);
        lblSubtitle.setForeground(UITheme.TEXT_MUTED);
        lblSubtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 2. Form Fields Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(UITheme.BG_CARD);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 0, 2, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Amount
        JLabel lblAmount = new JLabel("Amount (₹)");
        lblAmount.setFont(UITheme.FONT_LABEL);
        lblAmount.setForeground(UITheme.TEXT_MAIN);
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(lblAmount, gbc);

        txtAmount = UITheme.createTextField(20);
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 8, 0);
        formPanel.add(txtAmount, gbc);

        // Category
        JLabel lblCategory = new JLabel("Category");
        lblCategory.setFont(UITheme.FONT_LABEL);
        lblCategory.setForeground(UITheme.TEXT_MAIN);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.insets = new Insets(4, 0, 2, 0);
        formPanel.add(lblCategory, gbc);

        cmbCategory = new JComboBox<>(CATEGORIES);
        cmbCategory.setFont(UITheme.FONT_INPUT);
        cmbCategory.setBackground(UITheme.BG_CARD);
        cmbCategory.setForeground(UITheme.TEXT_MAIN);
        cmbCategory.setBorder(new CompoundBorder(
                new LineBorder(UITheme.BORDER_COLOR, 1, true),
                new EmptyBorder(4, 6, 4, 6)
        ));
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, 8, 0);
        formPanel.add(cmbCategory, gbc);

        // Date (YYYY-MM-DD)
        JLabel lblDate = new JLabel("Date (YYYY-MM-DD)");
        lblDate.setFont(UITheme.FONT_LABEL);
        lblDate.setForeground(UITheme.TEXT_MAIN);
        gbc.gridy = 4;
        gbc.insets = new Insets(4, 0, 2, 0);
        formPanel.add(lblDate, gbc);

        txtDate = UITheme.createTextField(20);
        txtDate.setText(LocalDate.now().toString());
        gbc.gridy = 5;
        gbc.insets = new Insets(0, 0, 8, 0);
        formPanel.add(txtDate, gbc);

        // Description (Optional)
        JLabel lblDescription = new JLabel("Description (Optional)");
        lblDescription.setFont(UITheme.FONT_LABEL);
        lblDescription.setForeground(UITheme.TEXT_MAIN);
        gbc.gridy = 6;
        gbc.insets = new Insets(4, 0, 2, 0);
        formPanel.add(lblDescription, gbc);

        txtDescription = new JTextArea(3, 20);
        txtDescription.setFont(UITheme.FONT_INPUT);
        txtDescription.setForeground(UITheme.TEXT_MAIN);
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        txtDescription.setBorder(new EmptyBorder(6, 8, 6, 8));

        JScrollPane scrollDesc = new JScrollPane(txtDescription);
        scrollDesc.setBorder(new LineBorder(UITheme.BORDER_COLOR, 1, true));
        gbc.gridy = 7;
        gbc.insets = new Insets(0, 0, 14, 0);
        formPanel.add(scrollDesc, gbc);

        // 3. Buttons Row
        JPanel buttonRow = new JPanel(new GridLayout(1, 3, 8, 0));
        buttonRow.setBackground(UITheme.BG_CARD);
        buttonRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        btnSave   = UITheme.createSuccessButton("Save");
        btnClear  = UITheme.createSecondaryButton("Clear");
        btnCancel = UITheme.createDangerButton("Cancel");

        buttonRow.add(btnSave);
        buttonRow.add(btnClear);
        buttonRow.add(btnCancel);

        btnSave.addActionListener(e -> handleSaveExpense());
        btnClear.addActionListener(e -> handleClear());
        btnCancel.addActionListener(e -> dispose());

        // Assembly
        cardPanel.add(lblTitle);
        cardPanel.add(Box.createVerticalStrut(4));
        cardPanel.add(lblSubtitle);
        cardPanel.add(Box.createVerticalStrut(14));
        cardPanel.add(formPanel);
        cardPanel.add(buttonRow);

        outerWrapper.add(cardPanel);
        add(outerWrapper, BorderLayout.CENTER);
    }

    private void handleSaveExpense() {
        int userId = SessionManager.getInstance().getCurrentUserId();
        if (userId <= 0) {
            showError("No active user session found. Please log in again.");
            return;
        }

        String amountText = txtAmount.getText().trim();
        if (amountText.isEmpty()) {
            showWarning("Please enter an expense amount.");
            txtAmount.requestFocusInWindow();
            return;
        }

        BigDecimal amount;
        try {
            amount = new BigDecimal(amountText);
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                showWarning("Amount must be greater than zero.");
                txtAmount.requestFocusInWindow();
                return;
            }
            if (amount.scale() > 2) {
                showWarning("Amount cannot have more than 2 decimal places.");
                txtAmount.requestFocusInWindow();
                return;
            }
        } catch (NumberFormatException ex) {
            showWarning("Invalid amount format. Please enter a numeric value (e.g. 150.50).");
            txtAmount.requestFocusInWindow();
            return;
        }

        String category = (String) cmbCategory.getSelectedItem();
        if (category == null || category.trim().isEmpty()) {
            showWarning("Please select a category.");
            cmbCategory.requestFocusInWindow();
            return;
        }

        String dateText = txtDate.getText().trim();
        if (dateText.isEmpty()) {
            showWarning("Please enter a date in YYYY-MM-DD format.");
            txtDate.requestFocusInWindow();
            return;
        }

        LocalDate expenseDate;
        try {
            expenseDate = LocalDate.parse(dateText, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException ex) {
            showWarning("Invalid date format. Please use YYYY-MM-DD (e.g. 2026-10-01).");
            txtDate.requestFocusInWindow();
            return;
        }

        String description = txtDescription.getText().trim();
        if (description.isEmpty()) {
            description = null;
        } else if (description.length() > 255) {
            showWarning("Description cannot exceed 255 characters.");
            txtDescription.requestFocusInWindow();
            return;
        }

        Expense newExpense = new Expense(userId, amount, category, expenseDate, description);

        setButtonsEnabled(false);

        SwingWorker<Boolean, Void> saveWorker = new SwingWorker<>() {
            private String errorMessage = null;

            @Override
            protected Boolean doInBackground() {
                try {
                    return expenseService.addExpense(newExpense);
                } catch (ServiceException ex) {
                    errorMessage = ex.getMessage();
                    return false;
                }
            }

            @Override
            protected void done() {
                setButtonsEnabled(true);
                try {
                    boolean success = get();
                    if (success) {
                        JOptionPane.showMessageDialog(
                                AddExpenseFrame.this,
                                "Expense added successfully!",
                                "Success",
                                JOptionPane.INFORMATION_MESSAGE
                        );
                        handleClear();
                        if (parentDashboard != null) {
                            parentDashboard.refreshDashboard();
                        }
                    } else {
                        String msg = (errorMessage != null)
                                ? errorMessage
                                : "Failed to record expense. Please try again.";
                        showError(msg);
                    }
                } catch (Exception ex) {
                    showError("An unexpected error occurred while saving: " + ex.getMessage());
                }
            }
        };

        saveWorker.execute();
    }

    private void handleClear() {
        txtAmount.setText("");
        cmbCategory.setSelectedIndex(0);
        txtDate.setText(LocalDate.now().toString());
        txtDescription.setText("");
        txtAmount.requestFocusInWindow();
    }

    private void setButtonsEnabled(boolean enabled) {
        btnSave.setEnabled(enabled);
        btnClear.setEnabled(enabled);
        btnCancel.setEnabled(enabled);
        btnSave.setText(enabled ? "Save" : "Saving...");
    }

    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "Validation Warning", JOptionPane.WARNING_MESSAGE);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
