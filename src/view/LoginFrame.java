package view;

import controller.LoginController;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import util.DBConnection;
import util.UIUtils;

/**
 * Modern Login Screen with College Branding, credentials validation,
 * and database status indicator.
 * Location: src/view/LoginFrame.java
 */
public class LoginFrame extends JFrame {

    private final JTextField txtUsername;
    private final JPasswordField txtPassword;
    private final JButton btnLogin;
    private final JLabel lblDbStatus;
    private final LoginController controller;

    public LoginFrame() {
        setTitle("CampusFace - Face Recognition Attendance System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 690);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(UIUtils.BG_LIGHT);

        controller = new LoginController(this);

        // Main layout
        setLayout(new BorderLayout());

        JPanel rootPanel = new JPanel(new GridBagLayout());
        rootPanel.setBackground(UIUtils.BG_LIGHT);
        rootPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Elevated Login Card
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UIUtils.CARD_BG);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.setColor(UIUtils.BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setLayout(new GridBagLayout());
        card.setPreferredSize(new Dimension(420, 610));
        card.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 6, 5, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.weightx = 1.0;

        // College Logo & Header Panel
        JPanel logoPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UIUtils.PRIMARY);
                g2.fillOval(getWidth() / 2 - 28, 0, 56, 56);

                // Cap icon representation
                g2.setColor(Color.WHITE);
                int cx = getWidth() / 2;
                int[] px = {cx - 18, cx, cx + 18, cx};
                int[] py = {24, 14, 24, 34};
                g2.fillPolygon(px, py, 4);
                g2.fillRect(cx - 12, 32, 24, 8);
                g2.dispose();
            }
        };
        logoPanel.setOpaque(false);
        logoPanel.setPreferredSize(new Dimension(320, 60));

        gbc.gridy = 0;
        card.add(logoPanel, gbc);

        JLabel lblTitle = new JLabel("COLLEGE OF ENGINEERING", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTitle.setForeground(UIUtils.PRIMARY);
        gbc.gridy = 1;
        card.add(lblTitle, gbc);

        JLabel lblSub = new JLabel("Face Recognition Attendance System", SwingConstants.CENTER);
        lblSub.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblSub.setForeground(UIUtils.TEXT_DARK);
        gbc.gridy = 2;
        card.add(lblSub, gbc);

        // --- DIRECT ACTION KIOSK BUTTONS (INSTANT ACCESS) ---
        JPanel directKioskPanel = new JPanel(new GridLayout(2, 1, 0, 6));
        directKioskPanel.setOpaque(false);

        JButton btnDirectScanner = UIUtils.createSuccessButton("📸 START FACE ATTENDANCE (SCANNER)");
        btnDirectScanner.setPreferredSize(new Dimension(320, 42));
        btnDirectScanner.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnDirectScanner.setToolTipText("Immediately launch camera to scan faces and record attendance");
        btnDirectScanner.addActionListener(e -> {
            RecognitionFrame rec = new RecognitionFrame();
            rec.setVisible(true);
        });

        JButton btnQuickRegister = UIUtils.createAccentButton("➕ Quick Register New Student Face");
        btnQuickRegister.setPreferredSize(new Dimension(320, 36));
        btnQuickRegister.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnQuickRegister.setToolTipText("Add new student and enroll their face via camera in 5 seconds");
        btnQuickRegister.addActionListener(e -> {
            RecognitionFrame rec = new RecognitionFrame();
            rec.setVisible(true);
            rec.openQuickRegisterDialog();
        });

        directKioskPanel.add(btnDirectScanner);
        directKioskPanel.add(btnQuickRegister);

        gbc.gridy = 3;
        gbc.insets = new Insets(10, 6, 8, 6);
        card.add(directKioskPanel, gbc);

        // Divider
        JLabel lblDivider = new JLabel("─── OR TEACHER / ADMIN LOGIN ───", SwingConstants.CENTER);
        lblDivider.setFont(UIUtils.FONT_SMALL);
        lblDivider.setForeground(UIUtils.TEXT_MUTED);
        gbc.gridy = 4;
        gbc.insets = new Insets(4, 6, 8, 6);
        card.add(lblDivider, gbc);

        // Username Field
        gbc.insets = new Insets(2, 6, 2, 6);
        JLabel lblUser = new JLabel("Username");
        lblUser.setFont(UIUtils.FONT_BOLD);
        lblUser.setForeground(UIUtils.TEXT_DARK);
        gbc.gridy = 5;
        card.add(lblUser, gbc);

        txtUsername = new JTextField("admin");
        txtUsername.setFont(UIUtils.FONT_REGULAR);
        txtUsername.setPreferredSize(new Dimension(320, 34));
        txtUsername.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIUtils.BORDER, 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        gbc.gridy = 6;
        card.add(txtUsername, gbc);

        // Password Field
        JLabel lblPass = new JLabel("Password");
        lblPass.setFont(UIUtils.FONT_BOLD);
        lblPass.setForeground(UIUtils.TEXT_DARK);
        gbc.gridy = 7;
        card.add(lblPass, gbc);

        txtPassword = new JPasswordField("admin123");
        txtPassword.setFont(UIUtils.FONT_REGULAR);
        txtPassword.setPreferredSize(new Dimension(320, 34));
        txtPassword.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIUtils.BORDER, 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        gbc.gridy = 8;
        card.add(txtPassword, gbc);

        // Login Button
        gbc.insets = new Insets(10, 6, 4, 6);
        btnLogin = UIUtils.createPrimaryButton("LOGIN TO DASHBOARD");
        btnLogin.setPreferredSize(new Dimension(320, 38));
        gbc.gridy = 9;
        card.add(btnLogin, gbc);

        // Sign Up Button
        JButton btnSignUp = UIUtils.createSecondaryButton("➕ Create New Account (Sign Up)");
        btnSignUp.setPreferredSize(new Dimension(320, 36));
        gbc.gridy = 10;
        gbc.insets = new Insets(4, 6, 8, 6);
        card.add(btnSignUp, gbc);

        // Database Connectivity Indicator
        boolean dbOk = DBConnection.testConnection();
        String statusText = dbOk
                ? "● Database: Connected (" + DBConnection.getDisplayInfo() + ")"
                : "● Database: Disconnected (Check " + (DBConnection.isSqlite() ? "SQLite File" : "MySQL Server") + ")";
        lblDbStatus = new JLabel(statusText, SwingConstants.CENTER);
        lblDbStatus.setFont(UIUtils.FONT_SMALL);
        lblDbStatus.setForeground(dbOk ? UIUtils.SUCCESS : UIUtils.DANGER);
        gbc.gridy = 11;
        gbc.insets = new Insets(6, 6, 0, 6);
        card.add(lblDbStatus, gbc);

        rootPanel.add(card);
        add(rootPanel, BorderLayout.CENTER);

        // Event Listeners
        btnLogin.addActionListener(e -> controller.handleLogin(txtUsername.getText(), new String(txtPassword.getPassword())));
        btnSignUp.addActionListener(e -> openSignUpDialog());
        txtPassword.addActionListener(e -> btnLogin.doClick());
        txtUsername.addActionListener(e -> txtPassword.requestFocusInWindow());
    }

    private void openSignUpDialog() {
        javax.swing.JDialog dialog = new javax.swing.JDialog(this, "Sign Up - CampusFace Registration", true);
        dialog.setSize(420, 520);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel p = new JPanel(new java.awt.GridBagLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        java.awt.GridBagConstraints c = new java.awt.GridBagConstraints();
        c.fill = java.awt.GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        c.insets = new Insets(4, 4, 4, 4);

        JLabel lblTitle = new JLabel("Create System Account", SwingConstants.CENTER);
        lblTitle.setFont(UIUtils.FONT_HEADER);
        lblTitle.setForeground(UIUtils.PRIMARY);
        c.gridx = 0; c.gridy = 0; c.gridwidth = 2;
        p.add(lblTitle, c);

        JLabel lblSub = new JLabel("Register as Teacher or Administrator", SwingConstants.CENTER);
        lblSub.setFont(UIUtils.FONT_SMALL);
        lblSub.setForeground(UIUtils.TEXT_MUTED);
        c.gridy = 1; c.insets = new Insets(2, 4, 15, 4);
        p.add(lblSub, c);

        c.gridwidth = 1; c.insets = new Insets(4, 4, 2, 4);

        // Full Name
        c.gridy = 2; c.gridx = 0;
        p.add(new JLabel("Full Name:"), c);
        JTextField txtFullName = new JTextField();
        c.gridy = 3;
        p.add(txtFullName, c);

        // Username
        c.gridy = 4;
        p.add(new JLabel("Username:"), c);
        JTextField txtNewUser = new JTextField();
        c.gridy = 5;
        p.add(txtNewUser, c);

        // Role
        c.gridy = 6;
        p.add(new JLabel("Role:"), c);
        javax.swing.JComboBox<String> cbRole = new javax.swing.JComboBox<>(new String[]{"TEACHER", "ADMIN"});
        c.gridy = 7;
        p.add(cbRole, c);

        // Password
        c.gridy = 8;
        p.add(new JLabel("Password:"), c);
        JPasswordField txtNewPass = new JPasswordField();
        c.gridy = 9;
        p.add(txtNewPass, c);

        // Confirm Password
        c.gridy = 10;
        p.add(new JLabel("Confirm Password:"), c);
        JPasswordField txtConfirmPass = new JPasswordField();
        c.gridy = 11;
        p.add(txtConfirmPass, c);

        // Register Button
        c.gridy = 12; c.insets = new Insets(16, 4, 6, 4);
        JButton btnRegister = UIUtils.createPrimaryButton("Register Account");
        p.add(btnRegister, c);

        btnRegister.addActionListener(ev -> {
            String fn = txtFullName.getText();
            String un = txtNewUser.getText();
            String pwd = new String(txtNewPass.getPassword());
            String cpwd = new String(txtConfirmPass.getPassword());
            String role = (String) cbRole.getSelectedItem();

            boolean success = controller.handleSignUp(fn, un, pwd, cpwd, role);
            if (success) {
                txtUsername.setText(un);
                txtPassword.setText("");
                txtPassword.requestFocusInWindow();
                dialog.dispose();
            }
        });

        dialog.getContentPane().add(p);
        dialog.setVisible(true);
    }
}

