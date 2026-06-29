package edu.univ.erp.ui.auth;

import com.formdev.flatlaf.FlatClientProperties;
import edu.univ.erp.auth.AuthDAO;
import edu.univ.erp.auth.AuthDAOImpl;
import edu.univ.erp.auth.AuthException;
import edu.univ.erp.auth.UserSession;
import edu.univ.erp.domain.User;
import edu.univ.erp.ui.admin.AdminDashboard;
import edu.univ.erp.ui.instructor.InstructorDashboard;
import edu.univ.erp.ui.student.StudentDashboard;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.sql.SQLException;

public class LoginWindow extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JLabel statusLabel;
    private AuthDAO authDAO;
    private Timer lockoutTimer;

    public LoginWindow() {
        this.authDAO = new AuthDAOImpl();

        setTitle("University ERP - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 500);
        setLocationRelativeTo(null);

        // --- 1. MAIN BACKGROUND PANEL ---
        // This panel paints the image across the entire window
        JPanel backgroundPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                // NEW WAY: Load from the src folder (Classpath)
                // inside paintComponent method

// 1. CHANGE ".jpg" TO ".png" (matching your file tree)
// 2. Ensure the path is just "/download.png" (root path)
                URL imgUrl = getClass().getResource("/download.jpg");

                if (imgUrl != null) {
                    ImageIcon icon = new ImageIcon(imgUrl);
                    Image img = icon.getImage();
                    if (img != null) {
                        g.drawImage(img, 0, 0, getWidth(), getHeight(), this);
                    }
                } else {
                    // Debugging hint
                    System.err.println("Still can't find image! Check if file is in 'src' root and named download.png");
                    g.setColor(new Color(50, 50, 50));
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };
        // Use GridBagLayout to center the login card
        backgroundPanel.setLayout(new GridBagLayout());
        setContentPane(backgroundPanel);

        // --- 2. LOGIN CARD (The floating box) ---
        JPanel loginCard = new JPanel(new GridBagLayout());
        // Semi-transparent white background (240, 240, 240, alpha=230)
        loginCard.setBackground(new Color(240, 240, 240, 230));
        // Add rounded corners using FlatLaf style
        loginCard.putClientProperty(FlatClientProperties.STYLE, "arc: 20");
        loginCard.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40)); // Padding inside the card

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 0, 10, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;

        // --- Components inside the Card ---

        // Title
        JLabel titleLabel = new JLabel("University ERP");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setForeground(new Color(50, 50, 50));
        gbc.gridy = 0;
        loginCard.add(titleLabel, gbc);

        // Subtitle
        JLabel subLabel = new JLabel("Login to your account");
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subLabel.setHorizontalAlignment(SwingConstants.CENTER);
        subLabel.setForeground(Color.GRAY);
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 20, 0); // Extra space below subtitle
        loginCard.add(subLabel, gbc);

        // Reset Insets
        gbc.insets = new Insets(5, 0, 5, 0);

        // Username
        gbc.gridy = 2;
        loginCard.add(new JLabel("Username"), gbc);

        usernameField = new JTextField(20);
        usernameField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter username");
        usernameField.setPreferredSize(new Dimension(250, 35));
        gbc.gridy = 3;
        loginCard.add(usernameField, gbc);

        // Password
        gbc.gridy = 4;
        loginCard.add(new JLabel("Password"), gbc);

        passwordField = new JPasswordField(20);
        passwordField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter password");
        passwordField.putClientProperty(FlatClientProperties.STYLE, "showRevealButton: true"); // Eye Icon
        passwordField.setPreferredSize(new Dimension(250, 35));
        gbc.gridy = 5;
        loginCard.add(passwordField, gbc);

        // Login Button
        loginButton = new JButton("Login");
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginButton.setBackground(new Color(0, 102, 204)); // Blue
        loginButton.setForeground(Color.WHITE);
        loginButton.setPreferredSize(new Dimension(250, 40));
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        gbc.gridy = 6;
        gbc.insets = new Insets(20, 0, 10, 0); // Space above button
        loginCard.add(loginButton, gbc);

        // Status Label
        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusLabel.setForeground(Color.RED);
        gbc.gridy = 7;
        loginCard.add(statusLabel, gbc);

        // --- Add Card to Background ---
        backgroundPanel.add(loginCard);

        // --- Listeners ---
        loginButton.addActionListener(e -> performLogin());
        getRootPane().setDefaultButton(loginButton);
    }

    private void performLogin() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Username and password required.");
            return;
        }

        try {
            User user = authDAO.login(username, password);
            UserSession.getInstance().createUserSession(user);
            openRoleBasedDashboard(user.getRole());
            this.dispose();

        } catch (AuthException ex) {
            if (ex.getWaitSeconds() > 0) {
                startCountdown(ex.getWaitSeconds());
            } else {
                statusLabel.setText(ex.getMessage());
            }
        } catch (SQLException ex) {
            statusLabel.setText("Database Error: " + ex.getMessage());
        }
    }

    private void startCountdown(long seconds) {
        loginButton.setEnabled(false);
        usernameField.setEnabled(false);
        passwordField.setEnabled(false);

        lockoutTimer = new Timer(1000, new ActionListener() {
            long timeLeft = seconds;
            @Override
            public void actionPerformed(ActionEvent e) {
                if (timeLeft > 0) {
                    statusLabel.setText("Locked! Wait " + timeLeft + "s");
                    timeLeft--;
                } else {
                    ((Timer)e.getSource()).stop();
                    loginButton.setEnabled(true);
                    usernameField.setEnabled(true);
                    passwordField.setEnabled(true);
                    statusLabel.setText("Unlocked. Try again.");
                    statusLabel.setForeground(new Color(0, 128, 0));
                }
            }
        });
        lockoutTimer.setInitialDelay(0);
        lockoutTimer.start();
    }

    private void openRoleBasedDashboard(String role) {
        if (role.equals("Student")) {
            new StudentDashboard().setVisible(true);
        } else if (role.equals("Instructor")) {
            new InstructorDashboard().setVisible(true);
        } else if (role.equals("Admin")) {
            new AdminDashboard().setVisible(true);
        }
    }
}