package util;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Camera Manager handling Webcam video streaming with seamless fallback to
 * simulated/image feeds for environments without physical cameras or for testing.
 * Location: src/util/CameraManager.java
 */
public class CameraManager {

    public interface FrameListener {
        void onFrame(BufferedImage frame);
    }

    private static CameraManager instance;

    private Webcam webcam;
    private final List<FrameListener> listeners = new CopyOnWriteArrayList<>();
    private final AtomicBoolean running = new AtomicBoolean(false);
    private Thread captureThread;
    private BufferedImage currentFrame;
    private BufferedImage customImageSource = null;
    private String customSourceName = "Virtual Feed";
    private boolean useSimulatedFeed = false;
    private double simAngle = 0.0;

    private CameraManager() {
        initWebcam();
    }

    public static synchronized CameraManager getInstance() {
        if (instance == null) {
            instance = new CameraManager();
        }
        return instance;
    }

    private void initWebcam() {
        try {
            List<Webcam> webcams = Webcam.getWebcams();
            if (webcams != null && !webcams.isEmpty()) {
                webcam = webcams.get(0);
                Dimension[] nonStandardResolutions = new Dimension[] {
                        WebcamResolution.VGA.getSize(),
                        WebcamResolution.QVGA.getSize()
                };
                webcam.setCustomViewSizes(nonStandardResolutions);
                webcam.setViewSize(WebcamResolution.VGA.getSize());
                useSimulatedFeed = false;
                System.out.println("[CameraManager] Hardware webcam detected: " + webcam.getName());
            } else {
                useSimulatedFeed = true;
                System.out.println("[CameraManager] No hardware webcam found. Defaulting to Simulated Feed.");
            }
        } catch (Throwable t) {
            useSimulatedFeed = true;
            System.out.println("[CameraManager] Webcam driver init notice: " + t.getMessage() + ". Using Virtual Camera.");
        }
    }

    public synchronized void start() {
        if (running.get()) return;

        running.set(true);

        if (!useSimulatedFeed && webcam != null) {
            try {
                if (!webcam.isOpen()) {
                    webcam.open();
                }
            } catch (Exception e) {
                System.err.println("[CameraManager] Error opening hardware webcam: " + e.getMessage() + ". Falling back to virtual feed.");
                useSimulatedFeed = true;
            }
        }

        captureThread = new Thread(this::captureLoop, "CameraCaptureThread");
        captureThread.setDaemon(true);
        captureThread.start();
    }

    private void captureLoop() {
        while (running.get()) {
            BufferedImage frame = null;

            if (!useSimulatedFeed && webcam != null) {
                try {
                    if (!webcam.isOpen()) {
                        webcam.open();
                    }
                    if (webcam.isOpen()) {
                        frame = webcam.getImage();
                    }
                } catch (Exception e) {
                    System.err.println("[CameraManager] Webcam frame read error: " + e.getMessage());
                }
            }

            if (frame == null) {
                frame = generateSimulatedFrame();
            }

            if (frame != null) {
                currentFrame = frame;
                notifyListeners(frame);
            }

            try {
                Thread.sleep(40); // ~25 FPS
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    private BufferedImage generateSimulatedFrame() {
        int width = 640;
        int height = 480;

        if (customImageSource != null) {
            if (customImageSource.getWidth() <= 140 && customImageSource.getHeight() <= 140) {
                // If it's already a cropped thumbnail/sample, center it inside 640x480 frame for live display
                BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                Graphics2D g = img.createGraphics();
                g.setColor(new Color(24, 30, 44));
                g.fillRect(0, 0, width, height);

                int scale = 3;
                int sw = customImageSource.getWidth() * scale;
                int sh = customImageSource.getHeight() * scale;
                int sx = (width - sw) / 2;
                int sy = (height - sh) / 2;

                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g.drawImage(customImageSource, sx, sy, sw, sh, null);

                // Watermark tag
                g.setFont(new Font("Segoe UI", Font.BOLD, 13));
                g.setColor(new Color(255, 255, 255, 210));
                g.drawString("PHOTO TEST MODE: " + customSourceName, 20, 30);
                g.dispose();
                return img;
            }

            // Render custom loaded photo centered
            BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = img.createGraphics();
            g.setColor(new Color(24, 30, 44));
            g.fillRect(0, 0, width, height);

            int iw = customImageSource.getWidth();
            int ih = customImageSource.getHeight();
            double scale = Math.min((double) width / iw, (double) height / ih);
            int sw = (int)(iw * scale);
            int sh = (int)(ih * scale);
            int sx = (width - sw) / 2;
            int sy = (height - sh) / 2;

            g.drawImage(customImageSource, sx, sy, sw, sh, null);

            // Watermark
            g.setFont(new Font("Segoe UI", Font.BOLD, 13));
            g.setColor(new Color(255, 255, 255, 210));
            g.drawString("PHOTO TEST MODE: " + customSourceName, 20, 30);
            g.dispose();
            return img;
        }

        // Generate synthetic facial feed
        BufferedImage frame = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = frame.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Background
        g.setColor(new Color(230, 238, 248));
        g.fillRect(0, 0, width, height);

        // Animated face position
        simAngle += 0.05;
        int offsetX = (int)(Math.sin(simAngle) * 20);
        int offsetY = (int)(Math.cos(simAngle * 0.7) * 10);

        int cx = width / 2 + offsetX;
        int cy = height / 2 - 20 + offsetY;

        // Neck and Body
        g.setColor(new Color(30, 58, 138));
        g.fillRoundRect(cx - 90, cy + 130, 180, 150, 40, 40);

        // Head (Skin tone matching YCbCr cluster)
        g.setColor(new Color(235, 185, 150));
        g.fillOval(cx - 65, cy - 80, 130, 170);

        // Hair
        g.setColor(new Color(40, 25, 20));
        g.fillArc(cx - 70, cy - 100, 140, 120, 0, 180);

        // Eyes
        g.setColor(new Color(30, 30, 30));
        g.fillOval(cx - 35, cy - 10, 14, 10);
        g.fillOval(cx + 21, cy - 10, 14, 10);

        // Nose
        g.setColor(new Color(210, 160, 130));
        g.fillOval(cx - 4, cy + 15, 8, 18);

        // Mouth / Smile
        g.setColor(new Color(180, 70, 70));
        g.fillArc(cx - 22, cy + 42, 44, 18, 0, -180);

        // Banner tag
        g.setColor(new Color(15, 23, 42, 190));
        g.fillRoundRect(15, 15, 360, 32, 8, 8);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Segoe UI", Font.BOLD, 12));
        g.drawString("SIMULATED DEMO FEED (Hardware Camera Standby)", 25, 36);

        g.dispose();
        return frame;
    }

    private void notifyListeners(BufferedImage frame) {
        for (FrameListener listener : listeners) {
            try {
                listener.onFrame(frame);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized void stop() {
        running.set(false);
        if (captureThread != null) {
            captureThread.interrupt();
            captureThread = null;
        }
        if (webcam != null && webcam.isOpen()) {
            try {
                webcam.close();
            } catch (Exception ignored) {
            }
        }
    }

    public void addListener(FrameListener listener) {
        listeners.add(listener);
    }

    public void removeListener(FrameListener listener) {
        listeners.remove(listener);
    }

    public BufferedImage getCurrentFrame() {
        return currentFrame;
    }

    public void setCustomImageSource(BufferedImage image) {
        setCustomImageSource(image, "Custom Image");
    }

    public void setCustomImageSource(BufferedImage image, String sourceName) {
        this.customImageSource = image;
        this.customSourceName = (sourceName != null) ? sourceName : "Custom Photo";
        this.useSimulatedFeed = true;
    }

    public void setUseSimulatedFeed(boolean simulated) {
        this.useSimulatedFeed = simulated;
        this.customImageSource = null;
        this.customSourceName = "Virtual Feed";
    }

    /**
     * Attempts to switch back to physical hardware webcam.
     */
    public boolean switchToHardwareWebcam() {
        if (webcam == null) {
            initWebcam();
        }
        if (webcam != null) {
            try {
                if (!webcam.isOpen()) {
                    webcam.open();
                }
                this.useSimulatedFeed = false;
                this.customImageSource = null;
                return true;
            } catch (Exception e) {
                System.err.println("[CameraManager] Could not open hardware webcam: " + e.getMessage());
                this.useSimulatedFeed = true;
                return false;
            }
        }
        return false;
    }

    public boolean hasHardwareWebcam() {
        return webcam != null;
    }

    public String getActiveSourceName() {
        if (!useSimulatedFeed && webcam != null && webcam.isOpen()) {
            return "Live Webcam (" + webcam.getName() + ")";
        } else if (customImageSource != null) {
            return "Photo Feed: " + customSourceName;
        } else {
            return "Simulated Virtual Camera";
        }
    }

    public boolean isSimulatedFeed() {
        return useSimulatedFeed;
    }
}
