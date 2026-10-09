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
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

/**
 * Modern, clean Java Swing Edit Expense Screen for ExpenseTracker.
 *
 * <h3>Design & Architecture</h3>
 * <ul>
 *   <li><b>Consistent Theme</b>: Dark Blue header, Emerald Save button, Slate Reset button.</li>
 *   <li><b>Ownership Enforced</b>: Scoped to {@link SessionManager#getCurrentUserId()}.</li>
 * </ul>
 */
public class EditExpenseFrame extends JFrame {

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
    private final Runnable onUpdateCallback;
    private Expense currentExpense;

    private JComboBox<ExpenseItem> cmbExpenseSelector;
    private JLabel lblExpenseId;
    private JTextField txtAmount;
    private JComboBox<String> cmbCategory;
    private JTextField txtDate;
    private JTextArea txtDescription;
    private JButton btnSave;
    private JButton btnReset;
    private JButton btnCancel;

    public EditExpenseFrame(Expense expense, Runnable onUpdateCallback) {
        this.expenseService = new ExpenseServiceImpl();
        this.currentExpense = expense;
        this.onUpdateCallback = onUpdateCallback;

        initFrame();
        initComponents();

        if (expense != null) {
            populateFields(expense);
        } else {
            loadUserExpensesDropdown();
        }
    }

    public EditExpenseFrame(int expenseId, Runnable onUpdateCallback) {
        this(null, onUpdateCallback);
        loadExpenseById(expenseId);
    }

    public EditExpenseFrame() {
        this(null, null);
    }

    private void initFrame() {
        setTitle("Expense Tracker – Edit Expense");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(500, 640);
        setMinimumSize(new Dimension(460, 580));
        setLocationRelativeTo(null); // Center on screen
        getContentPane().setBackground(UITheme.BG_LIGHT);
        setLayout(new BorderLayout());
    }

    private void initComponents() {
        JPanel outerWrapper = new JPanel(new GridBagLayout());
        outerWrapper.setBackground(UITheme.BG_LIGHT);
        outerWrapper.setBorder(new EmptyBorder(15, 20, 15, 20));

        JPanel cardPanel = UITheme.createCardPanel();
        cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.Y_AXIS));

        // 1. Header
        JLabel lblTitle = new JLabel("EDIT EXPENSE");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.PRIMARY_DARK);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSubtitle = new JLabel("Modify and update transaction details");
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

        int row = 0;

        if (currentExpense == null) {
            JLabel lblSelect = new JLabel("Select Expense to Edit");
            lblSelect.setFont(UITheme.FONT_LABEL);
            lblSelect.setForeground(UITheme.TEXT_MAIN);
            gbc.gridx = 0;
            gbc.gridy = row++;
            formPanel.add(lblSelect, gbc);

            cmbExpenseSelector = new JComboBox<>();
            cmbExpenseSelector.setFont(UITheme.FONT_INPUT);
            cmbExpenseSelector.setBackground(UITheme.BG_CARD);
            cmbExpenseSelector.addActionListener(e -> {
                ExpenseItem selected = (ExpenseItem) cmbExpenseSelector.getSelectedItem();
                if (selected != null && selected.expense != null) {
                    populateFields(selected.expense);
                }
            });

            gbc.gridy = row++;
            gbc.insets = new Insets(0, 0, 8, 0);
            formPanel.add(cmbExpenseSelector, gbc);
        }

        // Expense ID Display
        JLabel lblIdLabel = new JLabel("Expense ID");
        lblIdLabel.setFont(UITheme.FONT_LABEL);
        lblIdLabel.setForeground(UITheme.TEXT_MAIN);
        gbc.gridx = 0;
        gbc.gridy = row++;
        gbc.insets = new Insets(4, 0, 2, 0);
        formPanel.add(lblIdLabel, gbc);

        lblExpenseId = new JLabel(currentExpense != null ? String.valueOf(currentExpense.getId()) : "—");
        lblExpenseId.setFont(UITheme.FONT_INPUT);
        lblExpenseId.setForeground(UITheme.TEXT_MUTED);
        lblExpenseId.setBorder(new CompoundBorder(
                new LineBorder(UITheme.BORDER_COLOR, 1, true),
                new EmptyBorder(6, 10, 6, 10)
        ));
        gbc.gridy = row++;
        gbc.insets = new Insets(0, 0, 8, 0);
        formPanel.add(lblExpenseId, gbc);

        // Amount
        JLabel lblAmount = new JLabel("Amount (₹)");
        lblAmount.setFont(UITheme.FONT_LABEL);
        lblAmount.setForeground(UITheme.TEXT_MAIN);
        gbc.gridy = row++;
        gbc.insets = new Insets(4, 0, 2, 0);
        formPanel.add(lblAmount, gbc);

        txtAmount = UITheme.createTextField(20);
        gbc.gridy = row++;
        gbc.insets = new Insets(0, 0, 8, 0);
        formPanel.add(txtAmount, gbc);

        // Category
        JLabel lblCategory = new JLabel("Category");
        lblCategory.setFont(UITheme.FONT_LABEL);
        lblCategory.setForeground(UITheme.TEXT_MAIN);
        gbc.gridy = row++;
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
        gbc.gridy = row++;
        gbc.insets = new Insets(0, 0, 8, 0);
        formPanel.add(cmbCategory, gbc);

        // Date (YYYY-MM-DD)
        JLabel lblDate = new JLabel("Date (YYYY-MM-DD)");
        lblDate.setFont(UITheme.FONT_LABEL);
        lblDate.setForeground(UITheme.TEXT_MAIN);
        gbc.gridy = row++;
        gbc.insets = new Insets(4, 0, 2, 0);
        formPanel.add(lblDate, gbc);

        txtDate = UITheme.createTextField(20);
        gbc.gridy = row++;
        gbc.insets = new Insets(0, 0, 8, 0);
        formPanel.add(txtDate, gbc);

        // Description
        JLabel lblDescription = new JLabel("Description (Optional)");
        lblDescription.setFont(UITheme.FONT_LABEL);
        lblDescription.setForeground(UITheme.TEXT_MAIN);
        gbc.gridy = row++;
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
        gbc.gridy = row++;
        gbc.insets = new Insets(0, 0, 14, 0);
        formPanel.add(scrollDesc, gbc);

        // 3. Action Buttons Panel (Save Changes, Reset, Cancel)
        JPanel buttonRow = new JPanel(new GridLayout(1, 3, 8, 0));
        buttonRow.setBackground(UITheme.BG_CARD);
        buttonRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        btnSave   = UITheme.createSuccessButton("Save");
        btnReset  = UITheme.createSecondaryButton("Reset");
        btnCancel = UITheme.createDangerButton("Cancel");

        buttonRow.add(btnSave);
        buttonRow.add(btnReset);
        buttonRow.add(btnCancel);

        btnSave.addActionListener(e -> handleSaveChanges());
        btnReset.addActionListener(e -> handleReset());
        btnCancel.addActionListener(e -> dispose());

        cardPanel.add(lblTitle);
        cardPanel.add(Box.createVerticalStrut(4));
        cardPanel.add(lblSubtitle);
        cardPanel.add(Box.createVerticalStrut(14));
        cardPanel.add(formPanel);
        cardPanel.add(buttonRow);

        outerWrapper.add(cardPanel);
        add(outerWrapper, BorderLayout.CENTER);
    }

    private void populateFields(Expense exp) {
        this.currentExpense = exp;
        if (exp == null) return;

        lblExpenseId.setText(String.valueOf(exp.getId()));
        txtAmount.setText(exp.getAmount() != null ? exp.getAmount().toPlainString() : "");
        cmbCategory.setSelectedItem(exp.getCategory());
        txtDate.setText(exp.getExpenseDate() != null ? exp.getExpenseDate().toString() : "");
        txtDescription.setText(exp.getDescription() != null ? exp.getDescription() : "");
    }

    private void loadExpenseById(int expenseId) {
        int userId = SessionManager.getInstance().getCurrentUserId();
        if (userId <= 0) return;

        SwingWorker<Optional<Expense>, Void> worker = new SwingWorker<>() {
            @Override
            protected Optional<Expense> doInBackground() {
                try {
                    return expenseService.getExpenseById(expenseId, userId);
                } catch (ServiceException e) {
                    return Optional.empty();
                }
            }

            @Override
            protected void done() {
                try {
                    Optional<Expense> expOpt = get();
                    if (expOpt.isPresent()) {
                        populateFields(expOpt.get());
                    } else {
                        showError("Could not find expense #" + expenseId + " for your account.");
                        dispose();
                    }
                } catch (Exception ex) {
                    showError("Error loading expense: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void loadUserExpensesDropdown() {
        int userId = SessionManager.getInstance().getCurrentUserId();
        if (userId <= 0) return;

        SwingWorker<List<Expense>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Expense> doInBackground() {
                try {
                    return expenseService.getAllExpenses(userId);
                } catch (ServiceException e) {
                    return List.of();
                }
            }

            @Override
            protected void done() {
                try {
                    List<Expense> list = get();
                    cmbExpenseSelector.removeAllItems();
                    if (list.isEmpty()) {
                        showWarning("You do not have any expenses to edit.");
                        dispose();
                        return;
                    }
                    for (Expense exp : list) {
                        cmbExpenseSelector.addItem(new ExpenseItem(exp));
                    }
                    if (!list.isEmpty()) {
                        populateFields(list.get(0));
                    }
                } catch (Exception ignored) {
                }
            }
        };
        worker.execute();
    }

    private void handleSaveChanges() {
        int userId = SessionManager.getInstance().getCurrentUserId();
        if (userId <= 0) {
            showError("User session expired. Please log in again.");
            return;
        }

        if (currentExpense == null || currentExpense.getId() <= 0) {
            showWarning("No valid expense selected for editing.");
            return;
        }

        String amountStr = txtAmount.getText().trim();
        if (amountStr.isEmpty()) {
            showWarning("Amount cannot be empty.");
            txtAmount.requestFocusInWindow();
            return;
        }

        BigDecimal amount;
        try {
            amount = new BigDecimal(amountStr);
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                showWarning("Amount must be greater than zero.");
                txtAmount.requestFocusInWindow();
                return;
            }
            if (amount.scale() > 2) {
                showWarning("Amount cannot exceed 2 decimal places.");
                txtAmount.requestFocusInWindow();
                return;
            }
        } catch (NumberFormatException ex) {
            showWarning("Please enter a valid numeric amount (e.g. 150.50).");
            txtAmount.requestFocusInWindow();
            return;
        }

        String category = (String) cmbCategory.getSelectedItem();
        if (category == null || category.trim().isEmpty()) {
            showWarning("Please select a category.");
            cmbCategory.requestFocusInWindow();
            return;
        }

        String dateStr = txtDate.getText().trim();
        if (dateStr.isEmpty()) {
            showWarning("Date cannot be empty.");
            txtDate.requestFocusInWindow();
            return;
        }

        LocalDate expenseDate;
        try {
            expenseDate = LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
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

        Expense updatedExpense = new Expense(
                currentExpense.getId(),
                userId,
                amount,
                category,
                expenseDate,
                description
        );

        setButtonsEnabled(false);

        SwingWorker<Boolean, Void> updateWorker = new SwingWorker<>() {
            private String errorMessage = null;

            @Override
            protected Boolean doInBackground() {
                try {
                    return expenseService.updateExpense(updatedExpense);
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
                        currentExpense = updatedExpense;
                        JOptionPane.showMessageDialog(
                                EditExpenseFrame.this,
                                "Expense #" + updatedExpense.getId() + " updated successfully!",
                                "Update Successful",
                                JOptionPane.INFORMATION_MESSAGE
                        );

                        if (onUpdateCallback != null) {
                            onUpdateCallback.run();
                        }

                        dispose();
                    } else {
                        String msg = (errorMessage != null)
                                ? errorMessage
                                : "Failed to update expense. Please try again.";
                        showError(msg);
                    }
                } catch (Exception ex) {
                    showError("An error occurred while saving: " + ex.getMessage());
                }
            }
        };

        updateWorker.execute();
    }

    private void handleReset() {
        if (currentExpense != null) {
            populateFields(currentExpense);
        }
    }

    private void setButtonsEnabled(boolean enabled) {
        btnSave.setEnabled(enabled);
        btnReset.setEnabled(enabled);
        btnCancel.setEnabled(enabled);
        btnSave.setText(enabled ? "Save" : "Saving...");
    }

    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "Validation Warning", JOptionPane.WARNING_MESSAGE);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private static class ExpenseItem {
        final Expense expense;

        ExpenseItem(Expense expense) {
            this.expense = expense;
        }

        @Override
        public String toString() {
            if (expense == null) return "—";
            return String.format("#%d | %s | ₹%s | %s",
                    expense.getId(),
                    expense.getCategory(),
                    expense.getAmount() != null ? expense.getAmount().toPlainString() : "0.00",
                    expense.getExpenseDate() != null ? expense.getExpenseDate().toString() : "—");
        }
    }
}
