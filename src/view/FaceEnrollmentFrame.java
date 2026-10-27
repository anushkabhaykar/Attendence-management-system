package view;

import dao.StudentDAO;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.Timer;
import javax.swing.filechooser.FileNameExtensionFilter;
import model.Student;
import service.FaceRecognitionService;
import util.CameraManager;
import util.FaceRecognitionEngine;
import util.UIUtils;

/**
 * Biometric Face Enrollment Screen.
 * Provides One-Click Auto-Capture of 10 face sample angles, generates normalized LBPH facial encodings,
 * and saves them into the relational database.
 * Location: src/view/FaceEnrollmentFrame.java
 */
public class FaceEnrollmentFrame extends JFrame implements CameraManager.FrameListener {

    private final Student student;
    private final StudentDAO studentDAO;
    private final FaceRecognitionService recognitionService;
    private final CameraManager cameraManager;

    private final List<BufferedImage> capturedSamples = new ArrayList<>();
    private static final int REQUIRED_SAMPLES = 10;

    private CameraPreviewPanel previewPanel;
    private JLabel lblSampleCount;
    private JLabel lblCameraStatus;
    private JProgressBar progressBar;
    private JPanel sampleThumbnailsPanel;
    private JButton btnAutoCapture;
    private JButton btnCapture;
    private JButton btnSave;
    private JButton btnReset;
    private JButton btnLoadPhoto;

    private Timer autoCaptureTimer;

    public FaceEnrollmentFrame(Student student) {
        this.student = student;
        this.studentDAO = new StudentDAO();
        this.recognitionService = FaceRecognitionService.getInstance();
        this.cameraManager = CameraManager.getInstance();

        setTitle("Biometric Face Enrollment - " + student.getName() + " (" + student.getRollNo() + ")");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(960, 720);
        setMinimumSize(new Dimension(880, 640));
        setLocationRelativeTo(null);
        getContentPane().setBackground(UIUtils.BG_LIGHT);

        setLayout(new BorderLayout(15, 15));

        // 1. Top Details Header
        add(createHeaderPanel(), BorderLayout.NORTH);

        // 2. Center: Camera Preview Panel
        previewPanel = new CameraPreviewPanel();
        add(previewPanel, BorderLayout.CENTER);

        // 3. Right: Enrollment Controls and Captured Thumbnails
        add(createControlsPanel(), BorderLayout.EAST);

        // Window close listener to stop camera
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (autoCaptureTimer != null && autoCaptureTimer.isRunning()) {
                    autoCaptureTimer.stop();
                }
                cameraManager.removeListener(FaceEnrollmentFrame.this);
            }
        });

        // Register camera listener and start camera
        cameraManager.addListener(this);
        cameraManager.start();
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 0));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIUtils.BORDER));
        panel.setPreferredSize(new Dimension(0, 54));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 12));
        left.setOpaque(false);

        JLabel lblTitle = new JLabel("FACE BIOMETRIC ENROLLMENT");
        lblTitle.setFont(UIUtils.FONT_HEADER);
        lblTitle.setForeground(UIUtils.PRIMARY);

        JLabel lblStudent = new JLabel(String.format("Student: %s  |  Roll No: %s  |  Dept: %s",
                student.getName(), student.getRollNo(), student.getDepartment()));
        lblStudent.setFont(UIUtils.FONT_BOLD);
        lblStudent.setForeground(UIUtils.TEXT_DARK);

        left.add(lblTitle);
        left.add(new JLabel("  •  "));
        left.add(lblStudent);
        panel.add(left, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 12));
        right.setOpaque(false);

        lblCameraStatus = new JLabel("📷 " + cameraManager.getActiveSourceName());
        lblCameraStatus.setFont(UIUtils.FONT_SMALL);
        lblCameraStatus.setForeground(UIUtils.SUCCESS);
        right.add(lblCameraStatus);

        panel.add(right, BorderLayout.EAST);

        return panel;
    }

    private JPanel createControlsPanel() {
        JPanel card = UIUtils.createCardPanel();
        card.setPreferredSize(new Dimension(350, 0));
        card.setLayout(new BorderLayout(0, 10));

        JPanel top = new JPanel(new GridLayout(4, 1, 0, 4));
        top.setOpaque(false);

        JLabel lblInstructions = new JLabel("Enrollment Guide:");
        lblInstructions.setFont(UIUtils.FONT_HEADER);

        JLabel lblStep1 = new JLabel("1. Look at camera & align face inside guide oval.");
        lblStep1.setFont(UIUtils.FONT_SMALL);
        lblStep1.setForeground(UIUtils.TEXT_MUTED);

        JLabel lblStep2 = new JLabel("2. Click '⚡ Auto-Capture 10 Samples' (recommended).");
        lblStep2.setFont(UIUtils.FONT_SMALL);
        lblStep2.setForeground(UIUtils.TEXT_MUTED);

        JLabel lblStep3 = new JLabel("3. Move head slightly (center, tilt, smile) during capture.");
        lblStep3.setFont(UIUtils.FONT_SMALL);
        lblStep3.setForeground(UIUtils.TEXT_MUTED);

        top.add(lblInstructions);
        top.add(lblStep1);
        top.add(lblStep2);
        top.add(lblStep3);

        card.add(top, BorderLayout.NORTH);

        // Center: Progress and Samples Gallery
        JPanel center = new JPanel(new BorderLayout(0, 8));
        center.setOpaque(false);

        lblSampleCount = new JLabel("Face Samples Captured: 0 / " + REQUIRED_SAMPLES);
        lblSampleCount.setFont(UIUtils.FONT_BOLD);
        lblSampleCount.setForeground(UIUtils.TEXT_DARK);

        progressBar = new JProgressBar(0, REQUIRED_SAMPLES);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        progressBar.setForeground(UIUtils.SUCCESS);
        progressBar.setPreferredSize(new Dimension(0, 22));

        JPanel progressPanel = new JPanel(new GridLayout(2, 1, 0, 4));
        progressPanel.setOpaque(false);
        progressPanel.add(lblSampleCount);
        progressPanel.add(progressBar);
        center.add(progressPanel, BorderLayout.NORTH);

        // Thumbnail gallery (2 columns x 5 rows grid)
        sampleThumbnailsPanel = new JPanel(new GridLayout(5, 2, 6, 6));
        sampleThumbnailsPanel.setOpaque(false);
        for (int i = 0; i < REQUIRED_SAMPLES; i++) {
            sampleThumbnailsPanel.add(createSamplePlaceholder(i + 1));
        }
        center.add(sampleThumbnailsPanel, BorderLayout.CENTER);

        card.add(center, BorderLayout.CENTER);

        // Bottom Actions
        JPanel bottom = new JPanel(new GridLayout(5, 1, 0, 6));
        bottom.setOpaque(false);

        btnAutoCapture = UIUtils.createPrimaryButton("⚡ Auto-Capture 10 Samples (1-Click)");
        btnCapture = UIUtils.createSecondaryButton("📸 Capture 1 Sample Manually");
        btnSave = UIUtils.createSuccessButton("💾 Save Face Data");
        btnSave.setEnabled(false);

        btnReset = UIUtils.createDangerButton("🔄 Reset Samples");
        btnLoadPhoto = UIUtils.createSecondaryButton("📁 Import Photo File");

        btnAutoCapture.addActionListener(e -> startAutoCapture());
        btnCapture.addActionListener(e -> captureSample());
        btnSave.addActionListener(e -> saveBiometricData());
        btnReset.addActionListener(e -> resetSamples());
        btnLoadPhoto.addActionListener(e -> loadCustomPhoto());

        bottom.add(btnAutoCapture);
        bottom.add(btnCapture);
        bottom.add(btnSave);
        bottom.add(btnReset);
        bottom.add(btnLoadPhoto);

        card.add(bottom, BorderLayout.SOUTH);

        return card;
    }

    private JPanel createSamplePlaceholder(int index) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(241, 245, 249));
        p.setBorder(BorderFactory.createLineBorder(UIUtils.BORDER, 1));
        JLabel l = new JLabel("#" + index, JLabel.CENTER);
        l.setFont(UIUtils.FONT_SMALL);
        l.setForeground(UIUtils.TEXT_MUTED);
        p.add(l, BorderLayout.CENTER);
        return p;
    }

    private void startAutoCapture() {
        if (capturedSamples.size() >= REQUIRED_SAMPLES) {
            resetSamples();
        }

        btnAutoCapture.setEnabled(false);
        btnCapture.setEnabled(false);

        autoCaptureTimer = new Timer(380, null);
        autoCaptureTimer.addActionListener(e -> {
            boolean captured = captureSample();
            if (!captured || capturedSamples.size() >= REQUIRED_SAMPLES) {
                autoCaptureTimer.stop();
                btnAutoCapture.setEnabled(true);
                btnCapture.setEnabled(capturedSamples.size() < REQUIRED_SAMPLES);
                if (capturedSamples.size() >= REQUIRED_SAMPLES) {
                    try {
                        Toolkit.getDefaultToolkit().beep();
                    } catch (Throwable ignored) {}
                    int opt = JOptionPane.showConfirmDialog(FaceEnrollmentFrame.this,
                            "All 10 biometric samples captured successfully!\n\nSave face data to database now?",
                            "Samples Complete", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                    if (opt == JOptionPane.YES_OPTION) {
                        saveBiometricData();
                    }
                }
            }
        });
        autoCaptureTimer.start();
    }

    private boolean captureSample() {
        BufferedImage frame = cameraManager.getCurrentFrame();
        if (frame == null) {
            JOptionPane.showMessageDialog(this, "No frame available from camera.\nPlease ensure camera is active.", "Camera Notice", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        if (capturedSamples.size() >= REQUIRED_SAMPLES) {
            return false;
        }

        // Make an independent copy of the frame
        BufferedImage copy = new BufferedImage(frame.getWidth(), frame.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = copy.createGraphics();
        g.drawImage(frame, 0, 0, null);
        g.dispose();

        capturedSamples.add(copy);

        int count = capturedSamples.size();
        lblSampleCount.setText("Face Samples Captured: " + count + " / " + REQUIRED_SAMPLES);
        progressBar.setValue(count);

        // Update thumbnail
        if (count - 1 < sampleThumbnailsPanel.getComponentCount()) {
            JPanel slot = (JPanel) sampleThumbnailsPanel.getComponent(count - 1);
            slot.removeAll();

            // Create scaled thumbnail
            BufferedImage thumb = new BufferedImage(48, 48, BufferedImage.TYPE_INT_RGB);
            Graphics2D tg = thumb.createGraphics();
            tg.drawImage(copy, 0, 0, 48, 48, null);
            tg.setColor(UIUtils.SUCCESS);
            tg.drawRect(0, 0, 47, 47);
            tg.dispose();

            JLabel thumbLabel = new JLabel(new javax.swing.ImageIcon(thumb));
            slot.add(thumbLabel, BorderLayout.CENTER);
            slot.revalidate();
            slot.repaint();
        }

        if (count >= REQUIRED_SAMPLES) {
            btnSave.setEnabled(true);
            btnCapture.setEnabled(false);
            btnAutoCapture.setEnabled(false);
        }

        return true;
    }

    private void resetSamples() {
        if (autoCaptureTimer != null && autoCaptureTimer.isRunning()) {
            autoCaptureTimer.stop();
        }
        capturedSamples.clear();
        lblSampleCount.setText("Face Samples Captured: 0 / " + REQUIRED_SAMPLES);
        progressBar.setValue(0);
        btnSave.setEnabled(false);
        btnCapture.setEnabled(true);
        btnAutoCapture.setEnabled(true);

        sampleThumbnailsPanel.removeAll();
        for (int i = 0; i < REQUIRED_SAMPLES; i++) {
            sampleThumbnailsPanel.add(createSamplePlaceholder(i + 1));
        }
        sampleThumbnailsPanel.revalidate();
        sampleThumbnailsPanel.repaint();
    }

    private void saveBiometricData() {
        try {
            boolean success = recognitionService.enrollStudentFace(student.getId(), student.getRollNo(), capturedSamples);
            if (success) {
                JOptionPane.showMessageDialog(this,
                        "✔ Biometric enrollment completed successfully for " + student.getName() + " (" + student.getRollNo() + ")!\n\n"
                      + "• Features: " + FaceRecognitionEngine.FEATURE_LENGTH + "-D LBPH encoding generated.\n"
                      + "• Samples: 10 multi-angle biometric templates saved to database.\n"
                      + "• Ready: Student can now scan their face for attendance.",
                        "Enrollment Complete", JOptionPane.INFORMATION_MESSAGE);
                cameraManager.removeListener(this);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to save face enrollment to database.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Enrollment error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadCustomPhoto() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Student Photo File");
        chooser.setFileFilter(new FileNameExtensionFilter("Image Files (JPG, PNG)", "jpg", "jpeg", "png"));

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selected = chooser.getSelectedFile();
            try {
                BufferedImage img = ImageIO.read(selected);
                if (img != null) {
                    cameraManager.setCustomImageSource(img, selected.getName());
                    lblCameraStatus.setText("🖼 Photo: " + selected.getName());
                    JOptionPane.showMessageDialog(this, "Custom photo loaded as virtual camera feed!\nClick 'Auto-Capture' to generate samples.", "Photo Loaded", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to read image file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    @Override
    public void onFrame(BufferedImage frame) {
        if (previewPanel != null) {
            previewPanel.updateFrame(frame);
        }
    }

    /**
     * Camera Preview Canvas with Biometric Alignment Guide.
     */
    private static class CameraPreviewPanel extends JPanel {
        private BufferedImage currentFrame;

        public CameraPreviewPanel() {
            setBackground(new Color(15, 23, 42)); // Slate dark
            setPreferredSize(new Dimension(560, 480));
        }

        public void updateFrame(BufferedImage frame) {
            this.currentFrame = frame;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (currentFrame != null) {
                // Scale frame to fit panel while keeping aspect ratio
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

                // Render Facial Alignment Oval Guide
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int ovalW = (int)(sw * 0.45);
                int ovalH = (int)(sh * 0.62);
                int ovalX = sx + (sw - ovalW) / 2;
                int ovalY = sy + (sh - ovalH) / 2 - 10;

                g2.setColor(new Color(16, 185, 129, 140)); // Emerald guide line
                g2.setStroke(new java.awt.BasicStroke(2.5f));
                g2.drawOval(ovalX, ovalY, ovalW, ovalH);

                // Corner brackets
                int cLen = 25;
                g2.setColor(new Color(59, 130, 246, 200));
                g2.drawLine(ovalX - 10, ovalY - 10, ovalX - 10 + cLen, ovalY - 10);
                g2.drawLine(ovalX - 10, ovalY - 10, ovalX - 10, ovalY - 10 + cLen);
                g2.drawLine(ovalX + ovalW + 10, ovalY - 10, ovalX + ovalW + 10 - cLen, ovalY - 10);
                g2.drawLine(ovalX + ovalW + 10, ovalY - 10, ovalX + ovalW + 10, ovalY - 10 + cLen);

                // Guide label tag
                g2.setColor(new Color(15, 23, 42, 210));
                g2.fillRoundRect(sx + sw / 2 - 110, sy + sh - 40, 220, 26, 6, 6);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                g2.drawString("Align face inside oval guide", sx + sw / 2 - 80, sy + sh - 22);

                g2.dispose();
            } else {
                g.setColor(Color.WHITE);
                g.setFont(UIUtils.FONT_HEADER);
                g.drawString("Connecting to Camera...", getWidth() / 3, getHeight() / 2);
            }
        }
    }
}
