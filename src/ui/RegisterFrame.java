package ui;

import service.ServiceException;
import service.UserService;
import service.UserServiceImpl;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.regex.Pattern;

/**
 * Modern, clean Java Swing User Registration Screen for ExpenseTracker.
 *
 * <h3>Design & Architecture</h3>
 * <ul>
 *   <li><b>Consistent Theme</b>: Dark Blue primary, Emerald register action, Light Gray background.</li>
 *   <li><b>Responsive Layout</b>: Card container using {@link GridBagLayout} and {@link BoxLayout}.</li>
 *   <li><b>Decoupled Architecture</b>: Communicates solely through {@link UserService}.</li>
 * </ul>
 */
public class RegisterFrame extends JFrame {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private final UserService userService;

    private JTextField txtName;
    private JTextField txtEmail;
    private JPasswordField txtPassword;
    private JPasswordField txtConfirmPassword;
    private JButton btnRegister;
    private JButton btnClear;
    private JButton btnBackToLogin;

    public RegisterFrame() {
        this.userService = new UserServiceImpl();
        initFrame();
        initComponents();
    }

    private void initFrame() {
        setTitle("Expense Tracker – Register New Account");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(460, 600);
        setMinimumSize(new Dimension(420, 560));
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
        JLabel lblTitle = new JLabel("CREATE ACCOUNT");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.PRIMARY_DARK);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSubtitle = new JLabel("Join Expense Tracker to manage your spending");
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

        // Name
        JLabel lblName = new JLabel("Full Name");
        lblName.setFont(UITheme.FONT_LABEL);
        lblName.setForeground(UITheme.TEXT_MAIN);
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(lblName, gbc);

        txtName = UITheme.createTextField(20);
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 8, 0);
        formPanel.add(txtName, gbc);

        // Email
        JLabel lblEmail = new JLabel("Email Address");
        lblEmail.setFont(UITheme.FONT_LABEL);
        lblEmail.setForeground(UITheme.TEXT_MAIN);
        gbc.gridy = 2;
        gbc.insets = new Insets(4, 0, 2, 0);
        formPanel.add(lblEmail, gbc);

        txtEmail = UITheme.createTextField(20);
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, 8, 0);
        formPanel.add(txtEmail, gbc);

        // Password
        JLabel lblPassword = new JLabel("Password (min 6 characters)");
        lblPassword.setFont(UITheme.FONT_LABEL);
        lblPassword.setForeground(UITheme.TEXT_MAIN);
        gbc.gridy = 4;
        gbc.insets = new Insets(4, 0, 2, 0);
        formPanel.add(lblPassword, gbc);

        txtPassword = UITheme.createPasswordField(20);
        gbc.gridy = 5;
        gbc.insets = new Insets(0, 0, 8, 0);
        formPanel.add(txtPassword, gbc);

        // Confirm Password
        JLabel lblConfirmPassword = new JLabel("Confirm Password");
        lblConfirmPassword.setFont(UITheme.FONT_LABEL);
        lblConfirmPassword.setForeground(UITheme.TEXT_MAIN);
        gbc.gridy = 6;
        gbc.insets = new Insets(4, 0, 2, 0);
        formPanel.add(lblConfirmPassword, gbc);

        txtConfirmPassword = UITheme.createPasswordField(20);
        gbc.gridy = 7;
        gbc.insets = new Insets(0, 0, 14, 0);
        formPanel.add(txtConfirmPassword, gbc);

        // 3. Buttons Row
        JPanel buttonRow = new JPanel(new GridLayout(1, 2, 10, 0));
        buttonRow.setBackground(UITheme.BG_CARD);
        buttonRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        btnRegister = UITheme.createSuccessButton("Register");
        btnClear = UITheme.createSecondaryButton("Clear");
        buttonRow.add(btnRegister);
        buttonRow.add(btnClear);

        btnBackToLogin = new JButton("Already have an account? Back to Login");
        btnBackToLogin.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnBackToLogin.setForeground(UITheme.TEXT_LINK);
        btnBackToLogin.setContentAreaFilled(false);
        btnBackToLogin.setBorderPainted(false);
        btnBackToLogin.setFocusPainted(false);
        btnBackToLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBackToLogin.setAlignmentX(Component.CENTER_ALIGNMENT);

        btnBackToLogin.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btnBackToLogin.setForeground(UITheme.ACCENT_BLUE);
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                btnBackToLogin.setForeground(UITheme.TEXT_LINK);
            }
        });

        // Enter key submission
        KeyAdapter enterSubmit = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    handleRegister();
                }
            }
        };
        txtName.addKeyListener(enterSubmit);
        txtEmail.addKeyListener(enterSubmit);
        txtPassword.addKeyListener(enterSubmit);
        txtConfirmPassword.addKeyListener(enterSubmit);

        btnRegister.addActionListener(e -> handleRegister());
        btnClear.addActionListener(e -> handleClear());
        btnBackToLogin.addActionListener(e -> handleBackToLogin());

        // Assemble
        cardPanel.add(lblTitle);
        cardPanel.add(Box.createVerticalStrut(4));
        cardPanel.add(lblSubtitle);
        cardPanel.add(Box.createVerticalStrut(16));
        cardPanel.add(formPanel);
        cardPanel.add(buttonRow);
        cardPanel.add(Box.createVerticalStrut(10));
        cardPanel.add(btnBackToLogin);

        outerWrapper.add(cardPanel);
        add(outerWrapper, BorderLayout.CENTER);
    }

    private void handleRegister() {
        String name = txtName.getText().trim();
        String email = txtEmail.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();
        String confirmPassword = new String(txtConfirmPassword.getPassword()).trim();

        if (name.isEmpty()) {
            showWarning("Name cannot be empty.");
            txtName.requestFocusInWindow();
            return;
        }

        if (email.isEmpty()) {
            showWarning("Email cannot be empty.");
            txtEmail.requestFocusInWindow();
            return;
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            showWarning("Please enter a valid email address (e.g. user@example.com).");
            txtEmail.requestFocusInWindow();
            return;
        }

        if (password.isEmpty()) {
            showWarning("Password cannot be empty.");
            txtPassword.requestFocusInWindow();
            return;
        }

        if (password.length() < 6) {
            showWarning("Password must be at least 6 characters long.");
            txtPassword.requestFocusInWindow();
            return;
        }

        if (!password.equals(confirmPassword)) {
            showWarning("Passwords do not match. Please verify and try again.");
            txtConfirmPassword.setText("");
            txtConfirmPassword.requestFocusInWindow();
            return;
        }

        setButtonsEnabled(false);

        SwingWorker<Boolean, Void> registerWorker = new SwingWorker<>() {
            private String errorMessage = null;

            @Override
            protected Boolean doInBackground() {
                try {
                    return userService.registerUser(name, email, password);
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
                                RegisterFrame.this,
                                "Registration successful! You can now log in with your credentials.",
                                "Account Created",
                                JOptionPane.INFORMATION_MESSAGE
                        );
                        SwingUtilities.invokeLater(() -> {
                            new LoginFrame().setVisible(true);
                            dispose();
                        });
                    } else {
                        String msg = (errorMessage != null)
                                ? errorMessage
                                : "Registration failed. Please check your details and try again.";
                        showError(msg);
                    }
                } catch (Exception ex) {
                    showError("An unexpected error occurred during registration: " + ex.getMessage());
                }
            }
        };

        registerWorker.execute();
    }

    private void handleClear() {
        txtName.setText("");
        txtEmail.setText("");
        txtPassword.setText("");
        txtConfirmPassword.setText("");
        txtName.requestFocusInWindow();
    }

    private void handleBackToLogin() {
        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
            dispose();
        });
    }

    private void setButtonsEnabled(boolean enabled) {
        btnRegister.setEnabled(enabled);
        btnClear.setEnabled(enabled);
        btnBackToLogin.setEnabled(enabled);
        btnRegister.setText(enabled ? "Register" : "Creating Account...");
    }

    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "Validation Error", JOptionPane.WARNING_MESSAGE);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Registration Error", JOptionPane.ERROR_MESSAGE);
    }
}
