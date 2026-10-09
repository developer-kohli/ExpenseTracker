package ui;

import model.User;
import service.ServiceException;
import service.UserService;
import service.UserServiceImpl;
import util.SessionManager;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Optional;

/**
 * Modern, clean Java Swing Login Screen for ExpenseTracker.
 *
 * <h3>Design & Architecture</h3>
 * <ul>
 *   <li><b>Consistent Theme</b>: Dark Blue primary, Blue accent, Light Gray background.</li>
 *   <li><b>Responsive Layout</b>: Card container using {@link GridBagLayout} and {@link BoxLayout}.</li>
 *   <li><b>Decoupled Architecture</b>: Interacts solely with {@link UserService}.</li>
 *   <li><b>Session Management</b>: Authenticated user saved to {@link SessionManager}.</li>
 * </ul>
 */
public class LoginFrame extends JFrame {

    private final UserService userService;

    private JTextField txtEmail;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JButton btnRegister;
    private JButton btnExit;

    public LoginFrame() {
        this.userService = new UserServiceImpl();
        initFrame();
        initComponents();
    }

    private void initFrame() {
        setTitle("Expense Tracker – Sign In");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(440, 520);
        setMinimumSize(new Dimension(400, 480));
        setLocationRelativeTo(null); // Center on screen
        getContentPane().setBackground(UITheme.BG_LIGHT);
        setLayout(new BorderLayout());
    }

    private void initComponents() {
        JPanel outerWrapper = new JPanel(new GridBagLayout());
        outerWrapper.setBackground(UITheme.BG_LIGHT);
        outerWrapper.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel cardPanel = UITheme.createCardPanel();
        cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.Y_AXIS));

        // 1. Header
        JLabel lblTitle = new JLabel("EXPENSE TRACKER");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.PRIMARY_DARK);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSubtitle = new JLabel("Manage & Track Your Personal Finances");
        lblSubtitle.setFont(UITheme.FONT_SUBTITLE);
        lblSubtitle.setForeground(UITheme.TEXT_MUTED);
        lblSubtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 2. Form Fields
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(UITheme.BG_CARD);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 0, 4, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Email
        JLabel lblEmail = new JLabel("Email Address");
        lblEmail.setFont(UITheme.FONT_LABEL);
        lblEmail.setForeground(UITheme.TEXT_MAIN);
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(lblEmail, gbc);

        txtEmail = UITheme.createTextField(20);
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 10, 0);
        formPanel.add(txtEmail, gbc);

        // Password
        JLabel lblPassword = new JLabel("Password");
        lblPassword.setFont(UITheme.FONT_LABEL);
        lblPassword.setForeground(UITheme.TEXT_MAIN);
        gbc.gridy = 2;
        gbc.insets = new Insets(4, 0, 4, 0);
        formPanel.add(lblPassword, gbc);

        txtPassword = UITheme.createPasswordField(20);
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, 16, 0);
        formPanel.add(txtPassword, gbc);

        // 3. Buttons
        btnLogin = UITheme.createPrimaryButton("Login");
        btnLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btnLogin.setAlignmentX(Component.CENTER_ALIGNMENT);

        btnRegister = UITheme.createSuccessButton("Create New Account");
        btnRegister.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btnRegister.setAlignmentX(Component.CENTER_ALIGNMENT);

        btnExit = new JButton("Exit Application");
        btnExit.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnExit.setForeground(UITheme.TEXT_MUTED);
        btnExit.setContentAreaFilled(false);
        btnExit.setBorderPainted(false);
        btnExit.setFocusPainted(false);
        btnExit.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnExit.setAlignmentX(Component.CENTER_ALIGNMENT);

        btnExit.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btnExit.setForeground(UITheme.DANGER_RED);
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                btnExit.setForeground(UITheme.TEXT_MUTED);
            }
        });

        // Enter key listeners
        KeyAdapter enterSubmit = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    handleLogin();
                }
            }
        };
        txtEmail.addKeyListener(enterSubmit);
        txtPassword.addKeyListener(enterSubmit);

        btnLogin.addActionListener(e -> handleLogin());
        btnRegister.addActionListener(e -> handleRegister());
        btnExit.addActionListener(e -> handleExit());

        // Assembly
        cardPanel.add(lblTitle);
        cardPanel.add(Box.createVerticalStrut(4));
        cardPanel.add(lblSubtitle);
        cardPanel.add(Box.createVerticalStrut(20));
        cardPanel.add(formPanel);
        cardPanel.add(btnLogin);
        cardPanel.add(Box.createVerticalStrut(10));
        cardPanel.add(btnRegister);
        cardPanel.add(Box.createVerticalStrut(10));
        cardPanel.add(btnExit);

        outerWrapper.add(cardPanel);
        add(outerWrapper, BorderLayout.CENTER);
    }

    private void handleLogin() {
        String email = txtEmail.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        if (email.isEmpty()) {
            showWarning("Please enter your email address.");
            txtEmail.requestFocusInWindow();
            return;
        }

        if (!email.contains("@") || !email.contains(".")) {
            showWarning("Please enter a valid email address.");
            txtEmail.requestFocusInWindow();
            return;
        }

        if (password.isEmpty()) {
            showWarning("Please enter your password.");
            txtPassword.requestFocusInWindow();
            return;
        }

        setButtonsEnabled(false);

        SwingWorker<Optional<User>, Void> loginWorker = new SwingWorker<>() {
            private String errorMessage = null;

            @Override
            protected Optional<User> doInBackground() {
                try {
                    return userService.login(email, password);
                } catch (ServiceException ex) {
                    errorMessage = ex.getMessage();
                    return Optional.empty();
                }
            }

            @Override
            protected void done() {
                setButtonsEnabled(true);
                try {
                    Optional<User> authResult = get();
                    if (authResult.isPresent()) {
                        User loggedInUser = authResult.get();
                        SessionManager.getInstance().setCurrentUser(loggedInUser);

                        SwingUtilities.invokeLater(() -> {
                            new DashboardFrame().setVisible(true);
                            dispose();
                        });
                    } else {
                        String msg = (errorMessage != null) ? errorMessage : "Invalid email or password. Please try again.";
                        showError(msg);
                        txtPassword.setText("");
                        txtPassword.requestFocusInWindow();
                    }
                } catch (Exception ex) {
                    showError("An unexpected error occurred during login: " + ex.getMessage());
                }
            }
        };

        loginWorker.execute();
    }

    private void handleRegister() {
        SwingUtilities.invokeLater(() -> new RegisterFrame().setVisible(true));
    }

    private void handleExit() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to exit Expense Tracker?",
                "Exit Confirmation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (confirm == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }

    private void setButtonsEnabled(boolean enabled) {
        btnLogin.setEnabled(enabled);
        btnRegister.setEnabled(enabled);
        btnExit.setEnabled(enabled);
        btnLogin.setText(enabled ? "Login" : "Signing In...");
    }

    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "Validation Error", JOptionPane.WARNING_MESSAGE);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Login Failed", JOptionPane.ERROR_MESSAGE);
    }
}
