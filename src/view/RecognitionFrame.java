package view;

import dao.AttendanceDAO;
import dao.StudentDAO;
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
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import model.Attendance;
import model.Student;
import service.AttendanceService;
import service.FaceRecognitionService;
import util.CameraManager;
import util.FaceRecognitionEngine;
import util.UIUtils;

/**
 * Professional Face Recognition Attendance Terminal.
 * Performs continuous, real-time biometric scanning for regular classroom attendance,
 * displays prominent "PRESENT" status feedback on screen, provides 1-click test scanning,
 * and maintains a live attendee feed.
 * Location: src/view/RecognitionFrame.java
 */
public class RecognitionFrame extends JFrame implements CameraManager.FrameListener {

    private final CameraManager cameraManager;
    private final FaceRecognitionService recognitionService;
    private final AttendanceService attendanceService;
    private final StudentDAO studentDAO;
    private final AttendanceDAO attendanceDAO;

    private RecognitionPreviewPanel previewPanel;
    private JComboBox<String> cbSession;
    private JLabel lblCameraSource;
    private JLabel lblLiveClock;

    // Big On-Screen Status Badge & Cards
    private JPanel statusBadgeContainer;
    private JLabel lblBigStatus;
    private JLabel lblStudentName;
    private JLabel lblRollNo;
    private JLabel lblConfidence;
    private JLabel lblSessionDisplay;
    private JLabel lblTimestamp;

    // Quick Test / Demo Selector
    private JComboBox<String> cbEnrolledFaces;

    // Live Attendance Table on screen
    private DefaultTableModel liveTableModel;
    private JTable liveTable;
    private JLabel lblLiveCount;

    private String lastMarkedStudentNotice = "";
    private long lastRecognitionTime = 0;
    private int lastRecognizedStudentId = -1;

    private DashboardFrame parentDashboard;
    private javax.swing.Timer clockTimer;

    public RecognitionFrame() {
        this(null);
    }

    public RecognitionFrame(DashboardFrame parentDashboard) {
        this.parentDashboard = parentDashboard;
        setTitle("Face Recognition Attendance Terminal - CampusFace");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1180, 780);
        setMinimumSize(new Dimension(1040, 700));
        setLocationRelativeTo(null);
        getContentPane().setBackground(UIUtils.BG_LIGHT);

        cameraManager = CameraManager.getInstance();
        recognitionService = FaceRecognitionService.getInstance();
        attendanceService = new AttendanceService();
        studentDAO = new StudentDAO();
        attendanceDAO = new AttendanceDAO();

        setLayout(new BorderLayout(12, 12));

        // 1. Top Navigation, Source Indicator & Clock
        add(createTopBar(), BorderLayout.NORTH);

        // 2. Center: Camera HUD Live Preview with helper instructions
        add(createCenterPanel(), BorderLayout.CENTER);

        // 3. Right: Recognition Status, Quick Test Scanner & Live Attendance Feed
        add(createStatusSidePanel(), BorderLayout.EAST);

        // Window close listener to stop camera
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cleanupAndClose();
            }
        });

        // Start live clock timer
        clockTimer = new javax.swing.Timer(1000, e -> updateClock());
        clockTimer.start();

        // Initial load of today's attendance records
        loadTodayLiveAttendance();

        // Register camera listener and start camera
        cameraManager.addListener(this);
        cameraManager.start();
    }

    private JPanel createTopBar() {
        JPanel bar = new JPanel(new BorderLayout(15, 0));
        bar.setBackground(Color.WHITE);
        bar.setPreferredSize(new Dimension(0, 60));
        bar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIUtils.BORDER));

        // Left Section: Back button & Title
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 12));
        left.setOpaque(false);

        JButton btnBack = UIUtils.createSecondaryButton("◀ Dashboard");
        btnBack.setPreferredSize(new Dimension(115, 36));
        btnBack.addActionListener(e -> cleanupAndClose());

        JLabel lblTitle = new JLabel("BIOMETRIC ATTENDANCE TERMINAL");
        lblTitle.setFont(UIUtils.FONT_HEADER);
        lblTitle.setForeground(UIUtils.PRIMARY);

        left.add(btnBack);
        left.add(lblTitle);
        bar.add(left, BorderLayout.WEST);

        // Center: Active Camera Source Badge
        JPanel center = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 14));
        center.setOpaque(false);

        lblCameraSource = new JLabel(getSourceBadgeText());
        lblCameraSource.setFont(UIUtils.FONT_BOLD);
        lblCameraSource.setForeground(new Color(21, 128, 61));
        lblCameraSource.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(134, 239, 172), 1),
                BorderFactory.createEmptyBorder(4, 12, 4, 12)));
        center.add(lblCameraSource);
        bar.add(center, BorderLayout.CENTER);

        // Right Section: Session selector, Date/Clock, and actions
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        right.setOpaque(false);

        JLabel lblSess = new JLabel("Session:");
        lblSess.setFont(UIUtils.FONT_BOLD);
        String[] sessions = {"Morning Session", "Afternoon Session", "Lecture 1 - CS301", "Lecture 2 - CS302", "Lab Session"};
        cbSession = new JComboBox<>(sessions);
        cbSession.setPreferredSize(new Dimension(150, 34));

        lblLiveClock = new JLabel(getFormattedDateTime());
        lblLiveClock.setFont(UIUtils.FONT_BOLD);
        lblLiveClock.setForeground(UIUtils.TEXT_DARK);
        lblLiveClock.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIUtils.BORDER, 1),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)));

        JButton btnRegisterFaceTop = UIUtils.createSuccessButton("➕ Register Face");
        btnRegisterFaceTop.setPreferredSize(new Dimension(145, 34));
        btnRegisterFaceTop.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnRegisterFaceTop.setToolTipText("Register new student and enroll their face via camera");
        btnRegisterFaceTop.addActionListener(e -> openQuickRegisterDialog());

        JButton btnSwitchWebcam = UIUtils.createSecondaryButton("📷 Use Webcam");
        btnSwitchWebcam.setPreferredSize(new Dimension(120, 34));
        btnSwitchWebcam.setToolTipText("Switch to Hardware USB Webcam");
        btnSwitchWebcam.addActionListener(e -> switchToHardwareWebcam());

        right.add(lblSess);
        right.add(cbSession);
        right.add(lblLiveClock);
        right.add(btnRegisterFaceTop);
        right.add(btnSwitchWebcam);

        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private JPanel createCenterPanel() {
        JPanel center = new JPanel(new BorderLayout(0, 8));
        center.setOpaque(false);
        center.setBorder(BorderFactory.createEmptyBorder(0, 12, 10, 0));

        // 1. Camera HUD Live Preview
        previewPanel = new RecognitionPreviewPanel();
        center.add(previewPanel, BorderLayout.CENTER);

        // 2. Clear instructions bottom bar
        JPanel hintBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 8));
        hintBar.setBackground(Color.WHITE);
        hintBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIUtils.BORDER, 1),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));

        JLabel lblHintIcon = new JLabel("💡");
        lblHintIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));

        JLabel lblHint = new JLabel("<html><b>How It Works:</b> Stand in front of camera. The AI detects your face, matches it against enrolled biometrics, displays <b><font color='#16a34a'>'✔ PRESENT'</font></b>, and logs attendance automatically.</html>");
        lblHint.setFont(UIUtils.FONT_SMALL);
        lblHint.setForeground(UIUtils.TEXT_DARK);

        hintBar.add(lblHintIcon);
        hintBar.add(lblHint);
        center.add(hintBar, BorderLayout.SOUTH);

        return center;
    }

    private JPanel createStatusSidePanel() {
        JPanel sidePanel = new JPanel();
        sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));
        sidePanel.setBackground(UIUtils.BG_LIGHT);
        sidePanel.setPreferredSize(new Dimension(420, 0));
        sidePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 12));

        // 1. Upper Verification Card (Prominent PRESENT Banner & Student Info)
        JPanel verifCard = UIUtils.createCardPanel();
        verifCard.setLayout(new BorderLayout(0, 8));
        verifCard.setMaximumSize(new Dimension(420, 240));

        JLabel lblHeader = new JLabel("Live Biometric Verification");
        lblHeader.setFont(UIUtils.FONT_HEADER);
        lblHeader.setForeground(UIUtils.TEXT_DARK);
        verifCard.add(lblHeader, BorderLayout.NORTH);

        // Prominent PRESENT Status Banner Box
        statusBadgeContainer = new JPanel(new BorderLayout());
        statusBadgeContainer.setBackground(new Color(241, 245, 249)); // Standby light slate
        statusBadgeContainer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIUtils.BORDER, 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));

        lblBigStatus = new JLabel("STANDBY - SCANNING", SwingConstants.CENTER);
        lblBigStatus.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblBigStatus.setForeground(UIUtils.TEXT_MUTED);
        statusBadgeContainer.add(lblBigStatus, BorderLayout.CENTER);

        // Student Info Grid
        JPanel infoGrid = new JPanel(new GridLayout(5, 1, 0, 4));
        infoGrid.setOpaque(false);

        lblStudentName = createInfoRow("Student Name:", "Standby...", UIUtils.TEXT_DARK, infoGrid);
        lblRollNo = createInfoRow("Roll Number:", "---", UIUtils.TEXT_DARK, infoGrid);
        lblConfidence = createInfoRow("Confidence:", "0.0%", UIUtils.ACCENT, infoGrid);
        lblSessionDisplay = createInfoRow("Session:", (String) cbSession.getSelectedItem(), UIUtils.TEXT_MUTED, infoGrid);
        lblTimestamp = createInfoRow("Timestamp:", "--:--:--", UIUtils.TEXT_MUTED, infoGrid);

        JPanel verifCenter = new JPanel(new BorderLayout(0, 6));
        verifCenter.setOpaque(false);
        verifCenter.add(statusBadgeContainer, BorderLayout.NORTH);
        verifCenter.add(infoGrid, BorderLayout.CENTER);

        verifCard.add(verifCenter, BorderLayout.CENTER);
        sidePanel.add(verifCard);
        sidePanel.add(Box.createVerticalStrut(8));

        // 2. Quick Test / Demo Scanner Card (Allows instant 1-click testing of enrolled faces!)
        JPanel testCard = UIUtils.createCardPanel();
        testCard.setLayout(new BorderLayout(0, 6));
        testCard.setMaximumSize(new Dimension(420, 110));

        JLabel lblTestHeader = new JLabel("⚡ One-Click Test: Scan Enrolled Face");
        lblTestHeader.setFont(UIUtils.FONT_BOLD);
        lblTestHeader.setForeground(UIUtils.PRIMARY);
        testCard.add(lblTestHeader, BorderLayout.NORTH);

        JPanel testRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        testRow.setOpaque(false);

        cbEnrolledFaces = new JComboBox<>();
        cbEnrolledFaces.setPreferredSize(new Dimension(210, 32));
        refreshEnrolledDropdown();

        JButton btnTestScan = UIUtils.createPrimaryButton("⚡ Scan Face");
        btnTestScan.setPreferredSize(new Dimension(105, 32));
        btnTestScan.setToolTipText("Immediately scan this enrolled student's face for attendance");
        btnTestScan.addActionListener(e -> testScanSelectedFace());

        JButton btnImportPhoto = UIUtils.createSecondaryButton("📁 File");
        btnImportPhoto.setPreferredSize(new Dimension(65, 32));
        btnImportPhoto.setToolTipText("Load custom photo file");
        btnImportPhoto.addActionListener(e -> loadTestPhoto());

        testRow.add(cbEnrolledFaces);
        testRow.add(btnTestScan);
        testRow.add(btnImportPhoto);
        testCard.add(testRow, BorderLayout.CENTER);

        sidePanel.add(testCard);
        sidePanel.add(Box.createVerticalStrut(8));

        // 3. Middle: Live Attendance Feed Table (Today's Recorded Attendees)
        JPanel feedCard = UIUtils.createCardPanel();
        feedCard.setLayout(new BorderLayout(0, 6));

        JPanel feedHeader = new JPanel(new BorderLayout());
        feedHeader.setOpaque(false);

        lblLiveCount = new JLabel("Today's Verified Attendees (0)");
        lblLiveCount.setFont(UIUtils.FONT_HEADER);
        lblLiveCount.setForeground(UIUtils.TEXT_DARK);
        feedHeader.add(lblLiveCount, BorderLayout.WEST);

        JButton btnRefreshFeed = new JButton("🔄");
        btnRefreshFeed.setFont(UIUtils.FONT_SMALL);
        btnRefreshFeed.setPreferredSize(new Dimension(32, 26));
        btnRefreshFeed.setToolTipText("Refresh Attendance Feed");
        btnRefreshFeed.addActionListener(e -> loadTodayLiveAttendance());
        feedHeader.add(btnRefreshFeed, BorderLayout.EAST);

        feedCard.add(feedHeader, BorderLayout.NORTH);

        String[] cols = {"Time", "Roll No", "Student Name", "Status"};
        liveTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        liveTable = new JTable(liveTableModel);
        UIUtils.styleTable(liveTable);
        liveTable.getColumnModel().getColumn(0).setPreferredWidth(65);
        liveTable.getColumnModel().getColumn(1).setPreferredWidth(80);
        liveTable.getColumnModel().getColumn(2).setPreferredWidth(135);
        liveTable.getColumnModel().getColumn(3).setPreferredWidth(75);
        liveTable.getColumnModel().getColumn(3).setCellRenderer(new UIUtils.StatusBadgeRenderer());

        JScrollPane scrollFeed = new JScrollPane(liveTable);
        scrollFeed.setBorder(BorderFactory.createLineBorder(UIUtils.BORDER, 1));
        scrollFeed.getViewport().setBackground(Color.WHITE);
        feedCard.add(scrollFeed, BorderLayout.CENTER);

        sidePanel.add(feedCard);
        sidePanel.add(Box.createVerticalStrut(8));

        // 4. Bottom: Quick Actions
        JPanel bottom = new JPanel(new GridLayout(3, 1, 0, 6));
        bottom.setOpaque(false);
        bottom.setMaximumSize(new Dimension(420, 115));

        JButton btnQuickReg = UIUtils.createSuccessButton("➕ Quick Register Student & Face (New)");
        btnQuickReg.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnQuickReg.setToolTipText("Register student and capture their face from camera in 5 seconds");
        btnQuickReg.addActionListener(e -> openQuickRegisterDialog());

        JButton btnViewRegularAttendance = UIUtils.createSecondaryButton("📅 View Attendance Logs by Date");
        btnViewRegularAttendance.setToolTipText("View regular classroom attendance by date");
        btnViewRegularAttendance.addActionListener(e -> {
            AttendanceFrame attFrame = new AttendanceFrame();
            attFrame.setVisible(true);
        });

        JButton btnManualFallback = UIUtils.createSecondaryButton("⚙ Teacher Manual Fallback");
        btnManualFallback.addActionListener(e -> promptManualAttendance());

        bottom.add(btnQuickReg);
        bottom.add(btnViewRegularAttendance);
        bottom.add(btnManualFallback);
        sidePanel.add(bottom);

        return sidePanel;
    }

    private JLabel createInfoRow(String label, String initialValue, Color valueColor, JPanel parent) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(new Color(248, 250, 252));
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIUtils.BORDER, 1),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)));

        JLabel lblL = new JLabel(label);
        lblL.setFont(UIUtils.FONT_BOLD);
        lblL.setForeground(UIUtils.TEXT_MUTED);

        JLabel lblV = new JLabel(initialValue);
        lblV.setFont(UIUtils.FONT_BOLD);
        lblV.setForeground(valueColor);

        row.add(lblL, BorderLayout.WEST);
        row.add(lblV, BorderLayout.EAST);
        parent.add(row);

        return lblV;
    }

    private String getSourceBadgeText() {
        if (!cameraManager.isSimulatedFeed() && cameraManager.hasHardwareWebcam()) {
            return "🟢 Live Webcam Active";
        } else {
            return "🟠 " + cameraManager.getActiveSourceName();
        }
    }

    private void switchToHardwareWebcam() {
        boolean ok = cameraManager.switchToHardwareWebcam();
        if (ok) {
            lblCameraSource.setText("🟢 Live Webcam Active");
            lblCameraSource.setForeground(new Color(21, 128, 61));
            JOptionPane.showMessageDialog(this, "Connected to hardware webcam successfully!", "Webcam Active", JOptionPane.INFORMATION_MESSAGE);
        } else {
            lblCameraSource.setText("🟠 Virtual Test Mode");
            lblCameraSource.setForeground(new Color(180, 83, 9));
            JOptionPane.showMessageDialog(this, "Could not open physical webcam.\nPlease ensure webcam permissions are granted.", "Notice", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void refreshEnrolledDropdown() {
        if (cbEnrolledFaces == null) return;
        cbEnrolledFaces.removeAllItems();
        List<FaceRecognitionService.EnrolledFaceTemplate> templates = recognitionService.getEnrolledTemplates();
        if (templates.isEmpty()) {
            cbEnrolledFaces.addItem("No Enrolled Faces");
        } else {
            for (FaceRecognitionService.EnrolledFaceTemplate t : templates) {
                cbEnrolledFaces.addItem(t.rollNo + " - " + t.studentName);
            }
        }
    }

    private void testScanSelectedFace() {
        String item = (String) cbEnrolledFaces.getSelectedItem();
        if (item == null || item.startsWith("No Enrolled")) {
            JOptionPane.showMessageDialog(this, "No enrolled faces available to test.\nPlease enroll a student's face first.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String rollNo = item.split(" - ")[0].trim();
        File sampleFile = new File("data/faces/" + rollNo + "/sample_1.png");
        if (!sampleFile.exists()) {
            // Check sample_faces
            sampleFile = new File("sample_faces/sample_student.png");
        }

        if (sampleFile.exists()) {
            try {
                BufferedImage img = ImageIO.read(sampleFile);
                if (img != null) {
                    cameraManager.setCustomImageSource(img, rollNo);
                    lblCameraSource.setText("🟠 Test Scan: " + rollNo);
                    lblCameraSource.setForeground(new Color(180, 83, 9));
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to load sample: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Sample photo not found for " + rollNo, "File Missing", JOptionPane.WARNING_MESSAGE);
        }
    }

    public void openQuickRegisterDialog() {
        JDialog dialog = new JDialog(this, "Quick Register Student Face - CampusFace", true);
        dialog.setSize(480, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(5, 4, 5, 4);
        c.weightx = 1.0;

        JLabel lblTitle = new JLabel("Quick Student & Face Registration", SwingConstants.CENTER);
        lblTitle.setFont(UIUtils.FONT_TITLE);
        lblTitle.setForeground(UIUtils.PRIMARY);
        c.gridx = 0; c.gridy = 0; c.gridwidth = 2;
        p.add(lblTitle, c);

        JLabel lblSub = new JLabel("<html><center>Position student in front of camera, fill details, and click Capture.</center></html>", SwingConstants.CENTER);
        lblSub.setFont(UIUtils.FONT_SMALL);
        lblSub.setForeground(UIUtils.TEXT_MUTED);
        c.gridy = 1; c.insets = new Insets(2, 4, 14, 4);
        p.add(lblSub, c);

        // Student Name
        c.gridwidth = 1; c.insets = new Insets(5, 4, 2, 4);
        c.gridy = 2; c.gridx = 0;
        JLabel lblName = new JLabel("Full Name *");
        lblName.setFont(UIUtils.FONT_BOLD);
        p.add(lblName, c);

        c.gridy = 3; c.gridx = 0; c.gridwidth = 2;
        JTextField txtName = new JTextField();
        txtName.setPreferredSize(new Dimension(380, 36));
        txtName.setFont(UIUtils.FONT_REGULAR);
        p.add(txtName, c);

        // Roll Number
        c.gridwidth = 1; c.insets = new Insets(6, 4, 2, 4);
        c.gridy = 4; c.gridx = 0;
        JLabel lblRoll = new JLabel("Roll Number *");
        lblRoll.setFont(UIUtils.FONT_BOLD);
        p.add(lblRoll, c);

        c.gridy = 5; c.gridx = 0; c.gridwidth = 2;
        JTextField txtRoll = new JTextField();
        txtRoll.setPreferredSize(new Dimension(380, 36));
        txtRoll.setFont(UIUtils.FONT_REGULAR);
        p.add(txtRoll, c);

        // Department
        c.gridwidth = 1; c.insets = new Insets(6, 4, 2, 4);
        c.gridy = 6; c.gridx = 0;
        JLabel lblDept = new JLabel("Department");
        lblDept.setFont(UIUtils.FONT_BOLD);
        p.add(lblDept, c);

        c.gridy = 7; c.gridx = 0; c.gridwidth = 2;
        String[] depts = {"Computer Science", "Information Tech", "Electronics", "Mechanical", "Civil", "AI & Data Science"};
        JComboBox<String> cbDept = new JComboBox<>(depts);
        cbDept.setPreferredSize(new Dimension(380, 36));
        p.add(cbDept, c);

        // Capture Live Button
        c.gridy = 8; c.insets = new Insets(16, 4, 6, 4);
        JButton btnCaptureLive = UIUtils.createSuccessButton("📸 Capture Live Face & Register");
        btnCaptureLive.setPreferredSize(new Dimension(380, 42));
        btnCaptureLive.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnCaptureLive.setToolTipText("Capture face directly from active camera stream");
        p.add(btnCaptureLive, c);

        // Upload Photo Button
        c.gridy = 9; c.insets = new Insets(4, 4, 6, 4);
        JButton btnUploadPhoto = UIUtils.createSecondaryButton("📁 Or Upload Photo File");
        btnUploadPhoto.setPreferredSize(new Dimension(380, 34));
        btnUploadPhoto.setToolTipText("Load a photo from your computer if camera is not available");
        p.add(btnUploadPhoto, c);

        btnCaptureLive.addActionListener(ev -> {
            String name = txtName.getText().trim();
            String roll = txtRoll.getText().trim();
            String dept = (String) cbDept.getSelectedItem();

            if (name.isEmpty() || roll.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please enter both Student Name and Roll Number.", "Required Fields", JOptionPane.WARNING_MESSAGE);
                return;
            }

            BufferedImage current = cameraManager.getCurrentFrame();
            if (current == null) {
                JOptionPane.showMessageDialog(dialog, "Camera frame not available. Please ensure camera is active.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                boolean ok = registerAndEnrollStudent(roll, name, dept, current);
                if (ok) {
                    try {
                        Toolkit.getDefaultToolkit().beep();
                    } catch (Throwable ignored) {}
                    JOptionPane.showMessageDialog(dialog,
                            "✔ Student registered and enrolled successfully!\n\n"
                            + "Student: " + name + "\n"
                            + "Roll No: " + roll + "\n"
                            + "Department: " + dept + "\n\n"
                            + "The face scanner is now active and ready to recognize them!",
                            "Registration Success", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    refreshEnrolledDropdown();
                    loadTodayLiveAttendance();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Registration error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnUploadPhoto.addActionListener(ev -> {
            String name = txtName.getText().trim();
            String roll = txtRoll.getText().trim();
            String dept = (String) cbDept.getSelectedItem();

            if (name.isEmpty() || roll.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please enter both Student Name and Roll Number first.", "Required Fields", JOptionPane.WARNING_MESSAGE);
                return;
            }

            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Select Student Face Photo (JPG/PNG)");
            chooser.setFileFilter(new FileNameExtensionFilter("Image Files", "jpg", "jpeg", "png"));

            if (chooser.showOpenDialog(dialog) == JFileChooser.APPROVE_OPTION) {
                try {
                    BufferedImage img = ImageIO.read(chooser.getSelectedFile());
                    if (img != null) {
                        boolean ok = registerAndEnrollStudent(roll, name, dept, img);
                        if (ok) {
                            try {
                                Toolkit.getDefaultToolkit().beep();
                            } catch (Throwable ignored) {}
                            JOptionPane.showMessageDialog(dialog,
                                    "✔ Student registered from photo successfully!\n\n"
                                    + "Student: " + name + " (" + roll + ")\n"
                                    + "Biometric template enrolled!",
                                    "Registration Success", JOptionPane.INFORMATION_MESSAGE);
                            dialog.dispose();
                            refreshEnrolledDropdown();
                            loadTodayLiveAttendance();
                        }
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialog, "Failed to load/enroll photo: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        dialog.add(p);
        dialog.setVisible(true);
    }

    private boolean registerAndEnrollStudent(String roll, String name, String dept, BufferedImage baseFrame) throws Exception {
        Rectangle box = FaceRecognitionEngine.detectFace(baseFrame);
        if (box == null) {
            box = new Rectangle(baseFrame.getWidth() / 4, baseFrame.getHeight() / 4, baseFrame.getWidth() / 2, baseFrame.getHeight() / 2);
        }

        List<BufferedImage> samples = new ArrayList<>();
        int w = baseFrame.getWidth();
        int h = baseFrame.getHeight();

        for (int i = 0; i < 8; i++) {
            int dx = (i % 3 - 1) * 3;
            int dy = (i / 3 - 1) * 3;
            int nx = Math.max(0, Math.min(w - 10, box.x + dx));
            int ny = Math.max(0, Math.min(h - 10, box.y + dy));
            int nw = Math.min(w - nx, box.width);
            int nh = Math.min(h - ny, box.height);

            BufferedImage crop = baseFrame.getSubimage(nx, ny, nw, nh);
            samples.add(crop);
        }

        Student existing = studentDAO.findByRollNo(roll);
        int studentId;
        if (existing != null) {
            studentId = existing.getId();
            existing.setName(name);
            existing.setDepartment(dept);
            studentDAO.updateStudent(existing);
        } else {
            Student s = new Student();
            s.setRollNo(roll);
            s.setName(name);
            s.setDepartment(dept);
            s.setCourse("B.Tech");
            s.setYear(3);
            s.setDivision("A");
            s.setGender("Unspecified");
            s.setDob(java.sql.Date.valueOf("2004-01-01"));
            s.setEmail(roll.toLowerCase().replaceAll("[^a-z0-9]", "") + "@college.edu");
            s.setPhone("9876543210");
            studentDAO.addStudent(s);
            studentId = s.getId();
        }

        boolean enrolled = recognitionService.enrollStudentFace(studentId, roll, samples);
        if (enrolled) {
            recognitionService.reloadEnrolledFaces();
            return true;
        }
        return false;
    }

    private void openEnrollmentDialog() {
        try {
            List<Student> students = studentDAO.getAllStudents();
            if (students.isEmpty()) {
                int res = JOptionPane.showConfirmDialog(this,
                        "No students registered yet.\n\nWould you like to quickly register a new student and enroll their face right now?",
                        "Quick Registration", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (res == JOptionPane.YES_OPTION) {
                    openQuickRegisterDialog();
                }
                return;
            }

            JComboBox<String> cbStudents = new JComboBox<>();
            for (Student s : students) {
                String enrolledTag = s.hasFaceEnrolled() ? " (Enrolled ✔)" : " (Needs Enrollment ✖)";
                cbStudents.addItem(s.getRollNo() + " - " + s.getName() + enrolledTag);
            }

            int res = JOptionPane.showConfirmDialog(this, cbStudents, "Select Student for Biometric Face Enrollment",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (res == JOptionPane.OK_OPTION) {
                int selIdx = cbStudents.getSelectedIndex();
                if (selIdx >= 0 && selIdx < students.size()) {
                    Student target = students.get(selIdx);
                    FaceEnrollmentFrame enrollmentFrame = new FaceEnrollmentFrame(target);
                    enrollmentFrame.setVisible(true);
                    enrollmentFrame.addWindowListener(new WindowAdapter() {
                        @Override
                        public void windowClosed(WindowEvent e) {
                            recognitionService.reloadEnrolledFaces();
                            refreshEnrolledDropdown();
                        }
                    });
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String getFormattedDateTime() {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        return "📅 " + today.format(DateTimeFormatter.ofPattern("EEE, dd MMM yyyy")) + "  |  ⏱ " + now.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    private void updateClock() {
        if (lblLiveClock != null) {
            lblLiveClock.setText(getFormattedDateTime());
        }
    }

    /**
     * Loads and updates today's attendance feed in the live table.
     */
    public void loadTodayLiveAttendance() {
        try {
            List<Attendance> records = attendanceDAO.getTodayAttendanceRecords();
            SwingUtilities.invokeLater(() -> {
                liveTableModel.setRowCount(0);
                if (records != null) {
                    for (Attendance a : records) {
                        liveTableModel.addRow(new Object[]{
                                a.getAttendanceTime() != null ? a.getAttendanceTime().toString() : "-",
                                a.getStudentRollNo(),
                                a.getStudentName(),
                                a.getStatus()
                        });
                    }
                    lblLiveCount.setText("Today's Verified Attendees (" + records.size() + ")");
                }
            });
        } catch (SQLException e) {
            System.err.println("[RecognitionFrame] Error loading today attendance: " + e.getMessage());
        }
    }

    @Override
    public void onFrame(BufferedImage frame) {
        if (frame == null) return;

        // Perform face recognition pass
        FaceRecognitionEngine.RecognitionResult result = recognitionService.recognizeFace(frame);

        // Annotate frame with modern biometric HUD graphics
        BufferedImage hudFrame = new BufferedImage(frame.getWidth(), frame.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = hudFrame.createGraphics();
        g.drawImage(frame, 0, 0, null);
        g.dispose();

        String statusNotice = null;
        long now = System.currentTimeMillis();

        if (result != null && result.isRecognized) {
            String session = (String) cbSession.getSelectedItem();

            // Throttle duplicate database marking for the same student to once every 3.5 seconds
            if (result.studentId != lastRecognizedStudentId || (now - lastRecognitionTime > 3500)) {
                lastRecognizedStudentId = result.studentId;
                lastRecognitionTime = now;

                try {
                    AttendanceService.AttendanceResult attResult =
                            attendanceService.markAttendanceViaFace(result.studentId, session);

                    if (attResult == AttendanceService.AttendanceResult.SUCCESS) {
                        lastMarkedStudentNotice = "ATTENDANCE RECORDED: PRESENT";
                        // Play audible confirmation chime
                        try {
                            Toolkit.getDefaultToolkit().beep();
                        } catch (Throwable ignored) {}
                        // Refresh live attendance feed and dashboard
                        loadTodayLiveAttendance();
                        if (parentDashboard != null) {
                            parentDashboard.refreshDashboard();
                        }
                    } else if (attResult == AttendanceService.AttendanceResult.ALREADY_MARKED) {
                        lastMarkedStudentNotice = "ALREADY RECORDED (PRESENT)";
                    } else {
                        lastMarkedStudentNotice = "RECORDING FAILED";
                    }
                } catch (SQLException ex) {
                    lastMarkedStudentNotice = "DB ERROR: " + ex.getMessage();
                }
            }

            statusNotice = lastMarkedStudentNotice;

            // Safe Swing EDT UI updates
            final String finalNotice = statusNotice;
            final FaceRecognitionEngine.RecognitionResult finalResult = result;
            final String finalSession = session;

            SwingUtilities.invokeLater(() -> {
                // Prominent Green PRESENT Badge
                statusBadgeContainer.setBackground(new Color(220, 252, 231)); // Light emerald
                statusBadgeContainer.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(UIUtils.SUCCESS, 2),
                        BorderFactory.createEmptyBorder(8, 12, 8, 12)));

                lblBigStatus.setText("✔ PRESENT");
                lblBigStatus.setForeground(new Color(21, 128, 61)); // Dark Emerald

                lblStudentName.setText(finalResult.studentName);
                lblStudentName.setForeground(UIUtils.SUCCESS);
                lblRollNo.setText(finalResult.rollNo);
                lblConfidence.setText(String.format("%.1f%%", finalResult.confidence));
                lblSessionDisplay.setText(finalSession);
                lblTimestamp.setText(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
            });

        } else if (result != null && result.faceDetected) {
            // Anti-flicker: if recently recognized within 2.5 seconds, keep student card stable
            if (now - lastRecognitionTime < 2500 && lastRecognizedStudentId > 0) {
                statusNotice = lastMarkedStudentNotice;
            } else {
                statusNotice = "Unknown Face - Click [➕ Register Face]";
                final FaceRecognitionEngine.RecognitionResult finalResult = result;
                SwingUtilities.invokeLater(() -> {
                    statusBadgeContainer.setBackground(new Color(254, 242, 242)); // Light red
                    statusBadgeContainer.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(UIUtils.DANGER, 1),
                            BorderFactory.createEmptyBorder(8, 12, 8, 12)));

                    lblBigStatus.setText("✖ UNKNOWN FACE");
                    lblBigStatus.setForeground(UIUtils.DANGER);

                    lblStudentName.setText("Not Enrolled - Click ➕ Register Face");
                    lblStudentName.setForeground(UIUtils.DANGER);
                    lblRollNo.setText("---");
                    lblConfidence.setText(String.format("%.1f%%", finalResult.confidence));
                    lblTimestamp.setText(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                });
            }
        } else {
            // No face in frame: hold recognized card for 2.2 seconds before resetting to standby
            if (now - lastRecognitionTime > 2200) {
                SwingUtilities.invokeLater(() -> {
                    statusBadgeContainer.setBackground(new Color(241, 245, 249)); // Standby light slate
                    statusBadgeContainer.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(UIUtils.BORDER, 1),
                            BorderFactory.createEmptyBorder(8, 12, 8, 12)));

                    lblBigStatus.setText("STANDBY - SCANNING");
                    lblBigStatus.setForeground(UIUtils.TEXT_MUTED);

                    lblStudentName.setText("Standby for student...");
                    lblStudentName.setForeground(UIUtils.TEXT_MUTED);
                    lblRollNo.setText("---");
                    lblConfidence.setText("0.0%");
                    lblTimestamp.setText("--:--:--");
                });
            } else {
                statusNotice = lastMarkedStudentNotice;
            }
        }

        FaceRecognitionEngine.drawBiometricHUD(hudFrame, result, statusNotice);

        if (previewPanel != null) {
            previewPanel.updateFrame(hudFrame);
        }
    }

    private void promptManualAttendance() {
        String rollInput = JOptionPane.showInputDialog(this,
                "Enter Student Roll Number to Mark Manual Attendance:",
                "Manual Attendance Fallback", JOptionPane.QUESTION_MESSAGE);

        if (rollInput != null && !rollInput.trim().isEmpty()) {
            try {
                Student s = studentDAO.findByRollNo(rollInput.trim());
                if (s == null) {
                    JOptionPane.showMessageDialog(this, "Student not found with roll number: " + rollInput, "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String session = (String) cbSession.getSelectedItem();
                String[] options = {"PRESENT", "ABSENT", "LATE"};
                int choice = JOptionPane.showOptionDialog(this,
                        "Select attendance status for " + s.getName() + " (" + s.getRollNo() + "):",
                        "Set Status", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

                if (choice >= 0) {
                    String status = options[choice];
                    boolean ok = attendanceService.markAttendanceManually(s.getId(), status, session, "Teacher Manual Fallback");
                    if (ok) {
                        JOptionPane.showMessageDialog(this, "Attendance marked as " + status + " for " + s.getName(), "Success", JOptionPane.INFORMATION_MESSAGE);
                        loadTodayLiveAttendance();
                        if (parentDashboard != null) {
                            parentDashboard.refreshDashboard();
                        }
                    }
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void cleanupAndClose() {
        if (clockTimer != null) {
            clockTimer.stop();
        }
        cameraManager.removeListener(this);
        dispose();

        if (parentDashboard != null) {
            parentDashboard.setVisible(true);
            parentDashboard.refreshDashboard();
            parentDashboard.toFront();
        }
    }

    private void loadTestPhoto() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Test Student Photo (JPG / PNG)");
        chooser.setFileFilter(new FileNameExtensionFilter("Image Files (JPG, PNG)", "jpg", "jpeg", "png"));

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try {
                BufferedImage img = ImageIO.read(file);
                if (img != null) {
                    cameraManager.setCustomImageSource(img, file.getName());
                    lblCameraSource.setText("🟠 Photo: " + file.getName());
                    lblCameraSource.setForeground(new Color(180, 83, 9));
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to read photo: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Preview canvas rendering live camera frames with biometric HUD.
     */
    private static class RecognitionPreviewPanel extends JPanel {
        private BufferedImage currentFrame;

        public RecognitionPreviewPanel() {
            setBackground(new Color(15, 23, 42)); // Dark Slate
        }

        public void updateFrame(BufferedImage frame) {
            this.currentFrame = frame;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (currentFrame != null) {
                int pw = getWidth();
                int ph = getHeight();
                int fw = currentFrame.getWidth();
                int fh = currentFrame.getHeight();

                double scale = Math.min((double) pw / fw, (double) ph / fh);
                int sw = (int)(fw * scale);
                int sh = (int)(fh * scale);
                int sx = (pw - sw) / 2;
                int sy = (ph - sh) / 2;

                g.drawImage(currentFrame, sx, sy, sw, sh, null);
            } else {
                g.setColor(Color.WHITE);
                g.setFont(UIUtils.FONT_HEADER);
                g.drawString("Initializing Camera Stream...", getWidth() / 3, getHeight() / 2);
            }
        }
    }
}
