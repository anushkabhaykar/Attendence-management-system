package view;

import controller.DashboardController;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import model.Attendance;
import model.User;
import service.AuthenticationService;
import util.UIUtils;

/**
 * Main College Dashboard Screen presenting live biometric attendance statistics,
 * analytical charts, recent events, and sidebar navigation.
 * Location: src/view/DashboardFrame.java
 */
public class DashboardFrame extends JFrame {

    private final DashboardController controller;
    private JPanel statCardTotal;
    private JPanel statCardPresent;
    private JPanel statCardAbsent;
    private JPanel statCardRate;

    private JLabel lblTotalVal;
    private JLabel lblPresentVal;
    private JLabel lblAbsentVal;
    private JLabel lblRateVal;

    private DefaultTableModel recentTableModel;
    private JTable recentTable;
    private AttendanceDistributionPanel chartPanel;
    private JPanel bannerPanel;
    private JLabel lblSidebarDate;
    private LocalDate currentLocalDate = LocalDate.now();

    public DashboardFrame() {
        setTitle("CampusFace - Attendance Management Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1180, 750);
        setMinimumSize(new Dimension(1000, 680));
        setLocationRelativeTo(null);
        getContentPane().setBackground(UIUtils.BG_LIGHT);

        controller = new DashboardController(this);

        setLayout(new BorderLayout());

        // 1. Top Navigation Bar
        add(createTopBar(), BorderLayout.NORTH);

        // 2. Sidebar Navigation
        add(createSidebar(), BorderLayout.WEST);

        // 3. Center Dashboard Content
        add(createMainContent(), BorderLayout.CENTER);

        // Load initial data
        controller.loadDashboardData();
    }

    private JPanel createTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Color.WHITE);
        bar.setPreferredSize(new Dimension(0, 64));
        bar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIUtils.BORDER));

        // Left Branding
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 14));
        left.setOpaque(false);

        JLabel lblLogo = new JLabel("🎓");
        lblLogo.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 26));

        JLabel lblTitle = new JLabel("COLLEGE ATTENDANCE MANAGEMENT SYSTEM");
        lblTitle.setFont(UIUtils.FONT_HEADER);
        lblTitle.setForeground(UIUtils.PRIMARY);

        left.add(lblLogo);
        left.add(lblTitle);
        bar.add(left, BorderLayout.WEST);

        // Right User Profile & Logout
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 14));
        right.setOpaque(false);

        User current = AuthenticationService.getInstance().getCurrentUser();
        String userDisplay = current != null ? current.getFullName() : "Admin";
        String roleDisplay = current != null ? "[" + current.getRole() + "]" : "[ADMIN]";

        JLabel lblUser = new JLabel("👤 " + userDisplay + " " + roleDisplay);
        lblUser.setFont(UIUtils.FONT_BOLD);
        lblUser.setForeground(UIUtils.TEXT_DARK);

        JButton btnLogout = UIUtils.createDangerButton("Logout");
        btnLogout.setPreferredSize(new Dimension(90, 34));
        btnLogout.addActionListener(e -> controller.handleLogout());

        right.add(lblUser);
        right.add(btnLogout);
        bar.add(right, BorderLayout.EAST);

        return bar;
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(UIUtils.PRIMARY);
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBorder(BorderFactory.createEmptyBorder(20, 12, 20, 12));

        JLabel lblNav = new JLabel("NAVIGATION");
        lblNav.setFont(UIUtils.FONT_SMALL);
        lblNav.setForeground(new Color(191, 219, 254));
        lblNav.setAlignmentX(LEFT_ALIGNMENT);
        sidebar.add(lblNav);
        sidebar.add(Box.createVerticalStrut(15));

        // Nav Buttons
        sidebar.add(createSidebarButton("🏠 Dashboard", true, e -> controller.loadDashboardData()));
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(createSidebarButton("👨‍🎓 Students", false, e -> new StudentFrame().setVisible(true)));
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(createSidebarButton("📷 Face Recognition", false, e -> new RecognitionFrame(DashboardFrame.this).setVisible(true)));
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(createSidebarButton("📝 Attendance", false, e -> new AttendanceFrame().setVisible(true)));
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(createSidebarButton("📊 Reports", false, e -> new ReportsFrame().setVisible(true)));

        sidebar.add(Box.createVerticalGlue());

        // System Live Date and Clock Tag (Changes dynamically every day)
        lblSidebarDate = new JLabel();
        lblSidebarDate.setFont(UIUtils.FONT_SMALL);
        lblSidebarDate.setForeground(new Color(191, 219, 254));
        lblSidebarDate.setAlignmentX(LEFT_ALIGNMENT);
        updateSidebarDateLabel();
        sidebar.add(lblSidebarDate);

        // Timer updating live clock and detecting day change
        javax.swing.Timer dateTimer = new javax.swing.Timer(1000, e -> {
            LocalDate today = LocalDate.now();
            updateSidebarDateLabel();
            if (!today.equals(currentLocalDate)) {
                currentLocalDate = today;
                refreshDashboard();
            }
        });
        dateTimer.start();

        return sidebar;
    }

    private void updateSidebarDateLabel() {
        LocalDate today = LocalDate.now();
        String timeStr = java.time.LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        String todayStr = today.format(DateTimeFormatter.ofPattern("EEEE, MMM dd"));
        if (lblSidebarDate != null) {
            lblSidebarDate.setText("<html>" + todayStr + "<br><span style='color:#93c5fd; font-size:11px;'>⏱ " + timeStr + "</span></html>");
        }
    }

    private JButton createSidebarButton(String text, boolean active, java.awt.event.ActionListener al) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (active) {
                    g2.setColor(UIUtils.ACCENT);
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(45, 85, 175));
                } else {
                    g2.setColor(UIUtils.PRIMARY);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(UIUtils.FONT_BOLD);
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(196, 40));
        btn.setPreferredSize(new Dimension(196, 40));
        btn.addActionListener(al);
        return btn;
    }

    private JPanel createMainContent() {
        JPanel main = new JPanel(new BorderLayout(20, 20));
        main.setBackground(UIUtils.BG_LIGHT);
        main.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header greeting
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Attendance Overview");
        lblTitle.setFont(UIUtils.FONT_TITLE);
        lblTitle.setForeground(UIUtils.TEXT_DARK);

        JLabel lblSubtitle = new JLabel("Real-time biometric attendance metrics and summary records");
        lblSubtitle.setFont(UIUtils.FONT_REGULAR);
        lblSubtitle.setForeground(UIUtils.TEXT_MUTED);

        JPanel titleBlock = new JPanel(new GridLayout(2, 1, 0, 2));
        titleBlock.setOpaque(false);
        titleBlock.add(lblTitle);
        titleBlock.add(lblSubtitle);
        headerPanel.add(titleBlock, BorderLayout.WEST);

        // Quick Launch Buttons
        JPanel actionBlock = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionBlock.setOpaque(false);

        JButton btnStartCam = UIUtils.createAccentButton("📷 Open Face Recognition");
        btnStartCam.addActionListener(e -> new RecognitionFrame(DashboardFrame.this).setVisible(true));

        JButton btnRegularAttendance = UIUtils.createPrimaryButton("📝 Regular Attendance");
        btnRegularAttendance.addActionListener(e -> new AttendanceFrame().setVisible(true));

        JButton btnRefresh = UIUtils.createSecondaryButton("🔄 Refresh");
        btnRefresh.addActionListener(e -> controller.loadDashboardData());

        actionBlock.add(btnStartCam);
        actionBlock.add(btnRegularAttendance);
        actionBlock.add(btnRefresh);
        headerPanel.add(actionBlock, BorderLayout.EAST);

        // Success / Event Notification Banner (Initially Hidden)
        bannerPanel = new JPanel(new BorderLayout());
        bannerPanel.setOpaque(false);
        bannerPanel.setVisible(false);

        JPanel headerBox = new JPanel();
        headerBox.setLayout(new BoxLayout(headerBox, BoxLayout.Y_AXIS));
        headerBox.setOpaque(false);
        headerBox.add(headerPanel);
        headerBox.add(Box.createVerticalStrut(10));
        headerBox.add(bannerPanel);

        // Metrics Grid (4 Stat Cards)
        JPanel statsGrid = new JPanel(new GridLayout(1, 4, 15, 0));
        statsGrid.setOpaque(false);
        statsGrid.setPreferredSize(new Dimension(0, 115));

        statCardTotal = UIUtils.createStatCard("Total Students", "0", "Enrolled in college", UIUtils.ACCENT);
        statCardPresent = UIUtils.createStatCard("Present Today", "0", "Marked in classes", UIUtils.SUCCESS);
        statCardAbsent = UIUtils.createStatCard("Absent Today", "0", "Pending check-in", UIUtils.DANGER);
        statCardRate = UIUtils.createStatCard("Attendance Rate", "0%", "Today's campus turnout", UIUtils.WARNING);

        statsGrid.add(statCardTotal);
        statsGrid.add(statCardPresent);
        statsGrid.add(statCardAbsent);
        statsGrid.add(statCardRate);

        JPanel topContainer = new JPanel(new BorderLayout(0, 15));
        topContainer.setOpaque(false);
        topContainer.add(headerBox, BorderLayout.NORTH);
        topContainer.add(statsGrid, BorderLayout.CENTER);

        main.add(topContainer, BorderLayout.NORTH);

        // Center Area: Left Chart + Right Recent Table
        JPanel centerGrid = new JPanel(new GridLayout(1, 2, 20, 0));
        centerGrid.setOpaque(false);

        // 1. Chart Card
        JPanel chartCard = UIUtils.createCardPanel();
        chartCard.setLayout(new BorderLayout(0, 10));

        JLabel lblChartTitle = new JLabel("Today's Attendance Statistics");
        lblChartTitle.setFont(UIUtils.FONT_HEADER);
        lblChartTitle.setForeground(UIUtils.TEXT_DARK);
        chartCard.add(lblChartTitle, BorderLayout.NORTH);

        chartPanel = new AttendanceDistributionPanel();
        chartCard.add(chartPanel, BorderLayout.CENTER);
        centerGrid.add(chartCard);

        // 2. Recent Events Card
        JPanel tableCard = UIUtils.createCardPanel();
        tableCard.setLayout(new BorderLayout(0, 10));

        JLabel lblTableTitle = new JLabel("Recent Attendance Check-ins");
        lblTableTitle.setFont(UIUtils.FONT_HEADER);
        lblTableTitle.setForeground(UIUtils.TEXT_DARK);
        tableCard.add(lblTableTitle, BorderLayout.NORTH);

        String[] cols = {"Date", "Time", "Roll No", "Student Name", "Status", "Method"};
        recentTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        recentTable = new JTable(recentTableModel);
        UIUtils.styleTable(recentTable);
        recentTable.getColumnModel().getColumn(4).setCellRenderer(new UIUtils.StatusBadgeRenderer());

        JScrollPane scroll = new JScrollPane(recentTable);
        scroll.setBorder(BorderFactory.createLineBorder(UIUtils.BORDER, 1));
        scroll.getViewport().setBackground(Color.WHITE);
        tableCard.add(scroll, BorderLayout.CENTER);
        centerGrid.add(tableCard);

        main.add(centerGrid, BorderLayout.CENTER);

        return main;
    }

    public void updateMetrics(int total, int present, int absent, int pct) {
        // Update labels on stat cards
        ((JLabel) statCardTotal.getComponent(1)).setText(String.valueOf(total));
        ((JLabel) statCardPresent.getComponent(1)).setText(String.valueOf(present));
        ((JLabel) statCardAbsent.getComponent(1)).setText(String.valueOf(absent));
        ((JLabel) statCardRate.getComponent(1)).setText(pct + "%");

        if (chartPanel != null) {
            chartPanel.setStats(total, present, absent);
        }
    }

    public void updateRecentTable(List<Attendance> list) {
        recentTableModel.setRowCount(0);
        if (list != null) {
            for (Attendance a : list) {
                recentTableModel.addRow(new Object[]{
                        a.getAttendanceDate() != null ? a.getAttendanceDate().toString() : "-",
                        a.getAttendanceTime() != null ? a.getAttendanceTime().toString() : "-",
                        a.getStudentRollNo(),
                        a.getStudentName(),
                        a.getStatus(),
                        a.getMethod()
                });
            }
        }
    }

    public DashboardController getController() {
        return controller;
    }

    public void refreshDashboard() {
        if (controller != null) {
            controller.loadDashboardData();
        }
    }

    public void showAttendanceSuccessBanner(String studentName, String rollNo, String status) {
        SwingUtilities.invokeLater(() -> {
            if (bannerPanel != null) {
                bannerPanel.removeAll();

                JPanel card = new JPanel(new BorderLayout(15, 0));
                card.setBackground(new Color(240, 253, 244)); // Light Emerald/Green
                card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(134, 239, 172), 1),
                        BorderFactory.createEmptyBorder(10, 16, 10, 16)
                ));

                JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
                left.setOpaque(false);

                JLabel lblIcon = new JLabel("✔");
                lblIcon.setFont(new Font("Segoe UI", Font.BOLD, 18));
                lblIcon.setForeground(UIUtils.SUCCESS);

                String timeStr = java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
                JLabel lblMsg = new JLabel("<html><b>Face Biometric Attendance Marked:</b> <span style='color:#15803d; font-weight:bold;'>"
                        + studentName + "</span> (Roll No: <b>" + rollNo + "</b>) — Status: <span style='color:#15803d; font-weight:bold;'>" + status
                        + "</span> | Time: " + timeStr + " | Method: Face Recognition.</html>");
                lblMsg.setFont(UIUtils.FONT_REGULAR);
                lblMsg.setForeground(UIUtils.TEXT_DARK);

                left.add(lblIcon);
                left.add(lblMsg);
                card.add(left, BorderLayout.CENTER);

                JButton btnClose = new JButton("✕");
                btnClose.setFont(new Font("Segoe UI", Font.BOLD, 12));
                btnClose.setForeground(UIUtils.TEXT_MUTED);
                btnClose.setContentAreaFilled(false);
                btnClose.setBorderPainted(false);
                btnClose.setFocusPainted(false);
                btnClose.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
                btnClose.addActionListener(e -> bannerPanel.setVisible(false));
                card.add(btnClose, BorderLayout.EAST);

                bannerPanel.add(card, BorderLayout.CENTER);
                bannerPanel.setVisible(true);
                bannerPanel.revalidate();
                bannerPanel.repaint();

                javax.swing.Timer timer = new javax.swing.Timer(8000, ev -> bannerPanel.setVisible(false));
                timer.setRepeats(false);
                timer.start();
            }
        });
    }

    /**
     * Modern Java2D Attendance Donut & Bar Statistics Chart Component.
     */
    private static class AttendanceDistributionPanel extends JPanel {
        private int total = 0;
        private int present = 0;
        private int absent = 0;

        public AttendanceDistributionPanel() {
            setOpaque(false);
        }

        public void setStats(int total, int present, int absent) {
            this.total = total;
            this.present = present;
            this.absent = absent;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            if (total == 0) {
                g2.setColor(UIUtils.TEXT_MUTED);
                g2.setFont(UIUtils.FONT_REGULAR);
                g2.drawString("No attendance data recorded yet.", w / 4, h / 2);
                g2.dispose();
                return;
            }

            int diameter = Math.min(w / 2, h - 60);
            int cx = w / 4;
            int cy = h / 2 - 10;
            int x = cx - diameter / 2;
            int y = cy - diameter / 2;

            double presentAngle = ((double) present / total) * 360.0;
            double absentAngle = 360.0 - presentAngle;

            // Draw Donut Present Segment
            g2.setColor(UIUtils.SUCCESS);
            g2.fillArc(x, y, diameter, diameter, 90, -(int) Math.round(presentAngle));

            // Draw Donut Absent Segment
            g2.setColor(UIUtils.DANGER);
            g2.fillArc(x, y, diameter, diameter, 90 - (int) Math.round(presentAngle), -(int) Math.round(absentAngle));

            // Inner hole for Donut effect
            int hole = (int)(diameter * 0.62);
            g2.setColor(UIUtils.CARD_BG);
            g2.fillOval(cx - hole / 2, cy - hole / 2, hole, hole);

            // Center percentage text
            int pct = (int) Math.round(((double) present / total) * 100.0);
            g2.setColor(UIUtils.TEXT_DARK);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 22));
            String pctText = pct + "%";
            int textW = g2.getFontMetrics().stringWidth(pctText);
            g2.drawString(pctText, cx - textW / 2, cy + 4);

            g2.setFont(UIUtils.FONT_SMALL);
            g2.setColor(UIUtils.TEXT_MUTED);
            String lbl = "Present";
            g2.drawString(lbl, cx - g2.getFontMetrics().stringWidth(lbl) / 2, cy + 20);

            // Right Legend
            int legX = w / 2 + 30;
            int legY = h / 3;

            // Present Legend
            g2.setColor(UIUtils.SUCCESS);
            g2.fillRoundRect(legX, legY, 14, 14, 4, 4);
            g2.setColor(UIUtils.TEXT_DARK);
            g2.setFont(UIUtils.FONT_BOLD);
            g2.drawString("Present: " + present, legX + 24, legY + 12);

            // Absent Legend
            legY += 32;
            g2.setColor(UIUtils.DANGER);
            g2.fillRoundRect(legX, legY, 14, 14, 4, 4);
            g2.setColor(UIUtils.TEXT_DARK);
            g2.setFont(UIUtils.FONT_BOLD);
            g2.drawString("Absent: " + absent, legX + 24, legY + 12);

            // Total Legend
            legY += 32;
            g2.setColor(UIUtils.ACCENT);
            g2.fillRoundRect(legX, legY, 14, 14, 4, 4);
            g2.setColor(UIUtils.TEXT_DARK);
            g2.setFont(UIUtils.FONT_BOLD);
            g2.drawString("Total: " + total, legX + 24, legY + 12);

            g2.dispose();
        }
    }
}
