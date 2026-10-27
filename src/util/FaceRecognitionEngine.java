package util;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.imageio.ImageIO;

/**
 * High-performance Face Detection and LBPH (Local Binary Pattern Histogram)
 * Face Recognition Engine tailored for Java Desktop applications.
 *
 * Implements the standard OpenCV LBPH biometric feature extraction algorithm:
 * 1. Face region detection & normalization
 * 2. 8-Neighbor Local Binary Pattern encoding
 * 3. Spatial grid cell histogram concatenation
 * 4. Chi-Square / Euclidean distance metric comparison
 * 5. Confidence scoring and HUD overlay rendering
 *
 * Location: src/util/FaceRecognitionEngine.java
 */
public class FaceRecognitionEngine {

    public static final int TARGET_WIDTH = 100;
    public static final int TARGET_HEIGHT = 100;
    public static final int GRID_X = 8;
    public static final int GRID_Y = 8;
    public static final int HISTOGRAM_BINS = 16; // 16 quantized LBP bins per cell
    public static final int FEATURE_LENGTH = GRID_X * GRID_Y * HISTOGRAM_BINS; // 1024 features
    public static final double RECOGNITION_THRESHOLD = 11.0; // Chi-Square distance threshold calibrated for 64 LBPH cells

    /**
     * Data transfer object representing the result of a face recognition pass.
     */
    public static class RecognitionResult {
        public boolean faceDetected;
        public Rectangle faceBox;
        public int studentId = -1;
        public String rollNo = null;
        public String studentName = null;
        public double confidence = 0.0;
        public boolean isRecognized = false;
        public String statusMessage = "No Face Detected";

        public RecognitionResult(boolean faceDetected, Rectangle faceBox) {
            this.faceDetected = faceDetected;
            this.faceBox = faceBox;
        }
    }

    /**
     * Detects face rectangle in an image.
     * Uses skin-tone chromaticity segmentation (YCbCr) with facial geometry validation,
     * with center-guidance fallback.
     */
    public static Rectangle detectFace(BufferedImage image) {
        if (image == null) return null;
        int w = image.getWidth();
        int h = image.getHeight();

        // If image is already a cropped face thumbnail or sample (<= 140x140), return its full bounds
        if (w <= 140 && h <= 140) {
            return new Rectangle(0, 0, w, h);
        }

        int minX = w, maxX = 0, minY = h, maxY = 0;
        int skinPixels = 0;

        // Bounding box bounds for detection
        int step = 2; // sample every 2 pixels for speed
        for (int y = h / 8; y < (h * 7) / 8; y += step) {
            for (int x = w / 6; x < (w * 5) / 6; x += step) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                if (isSkinColor(r, g, b)) {
                    skinPixels++;
                    if (x < minX) minX = x;
                    if (x > maxX) maxX = x;
                    if (y < minY) minY = y;
                    if (y > maxY) maxY = y;
                }
            }
        }

        int boxW = maxX - minX;
        int boxH = maxY - minY;

        // If sufficient skin pixels found and within facial proportions
        if (skinPixels > (w * h) / 120 && boxW > w / 6 && boxH > h / 6) {
            // Normalize aspect ratio to 1:1.2 approximately
            int centerX = (minX + maxX) / 2;
            int centerY = (minY + maxY) / 2;
            int size = Math.max(boxW, boxH);
            int fx = Math.max(0, centerX - size / 2);
            int fy = Math.max(0, centerY - (int)(size * 0.55));
            int fw = Math.min(w - fx, size);
            int fh = Math.min(h - fy, (int)(size * 1.15));
            if (fw > 20 && fh > 20) {
                return new Rectangle(fx, fy, fw, fh);
            }
        }

        // Default center frame guidance
        int cw = (int)(w * 0.45);
        int ch = (int)(h * 0.55);
        int cx = (w - cw) / 2;
        int cy = (h - ch) / 2;
        return new Rectangle(cx, cy, cw, ch);
    }

    /**
     * Checks if RGB values correspond to human skin chromaticity range in YCbCr.
     */
    private static boolean isSkinColor(int r, int g, int b) {
        // YCbCr conversion
        double y = 0.299 * r + 0.587 * g + 0.114 * b;
        double cb = 128 - 0.168736 * r - 0.331264 * g + 0.5 * b;
        double cr = 128 + 0.5 * r - 0.418688 * g - 0.081312 * b;

        return (cb >= 75 && cb <= 138 && cr >= 128 && cr <= 178 && y >= 40);
    }

    /**
     * Normalizes and crops face region into standard 100x100 grayscale image.
     */
    public static BufferedImage normalizeFace(BufferedImage original, Rectangle box) {
        if (original == null) return null;
        if (box == null) {
            box = new Rectangle(0, 0, original.getWidth(), original.getHeight());
        }

        int x = Math.max(0, box.x);
        int y = Math.max(0, box.y);
        int w = Math.min(original.getWidth() - x, box.width);
        int h = Math.min(original.getHeight() - y, box.height);

        if (w <= 0 || h <= 0) {
            x = 0; y = 0;
            w = original.getWidth();
            h = original.getHeight();
        }

        BufferedImage cropped = original.getSubimage(x, y, w, h);
        BufferedImage normalized = new BufferedImage(TARGET_WIDTH, TARGET_HEIGHT, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = normalized.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(cropped, 0, 0, TARGET_WIDTH, TARGET_HEIGHT, null);
        g.dispose();

        // Apply histogram equalization for lighting invariance
        equalizeHistogram(normalized);

        return normalized;
    }

    /**
     * Enhances contrast via Grayscale Histogram Equalization.
     */
    private static void equalizeHistogram(BufferedImage grayImg) {
        int w = grayImg.getWidth();
        int h = grayImg.getHeight();
        int[] histogram = new int[256];

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int val = grayImg.getRaster().getSample(x, y, 0);
                histogram[val]++;
            }
        }

        int total = w * h;
        int[] cdf = new int[256];
        int sum = 0;
        for (int i = 0; i < 256; i++) {
            sum += histogram[i];
            cdf[i] = (sum * 255) / total;
        }

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int val = grayImg.getRaster().getSample(x, y, 0);
                grayImg.getRaster().setSample(x, y, 0, cdf[val]);
            }
        }
    }

    /**
     * Computes the Local Binary Patterns Histogram (LBPH) feature vector
     * for a normalized grayscale face image.
     */
    public static double[] extractLBPHFeatures(BufferedImage normalizedGrayFace) {
        if (normalizedGrayFace == null) return new double[FEATURE_LENGTH];

        int w = normalizedGrayFace.getWidth();
        int h = normalizedGrayFace.getHeight();
        int cellW = w / GRID_X;
        int cellH = h / GRID_Y;

        double[] featureVector = new double[FEATURE_LENGTH];
        int vectorIdx = 0;

        for (int gy = 0; gy < GRID_Y; gy++) {
            for (int gx = 0; gx < GRID_X; gx++) {
                double[] cellHist = new double[HISTOGRAM_BINS];

                int startX = gx * cellW;
                int startY = gy * cellH;
                int endX = Math.min(w - 1, startX + cellW);
                int endY = Math.min(h - 1, startY + cellH);

                for (int y = Math.max(1, startY); y < endY; y++) {
                    for (int x = Math.max(1, startX); x < endX; x++) {
                        int center = normalizedGrayFace.getRaster().getSample(x, y, 0);

                        int code = 0;
                        if (normalizedGrayFace.getRaster().getSample(x - 1, y - 1, 0) >= center) code |= (1 << 7);
                        if (normalizedGrayFace.getRaster().getSample(x,     y - 1, 0) >= center) code |= (1 << 6);
                        if (normalizedGrayFace.getRaster().getSample(x + 1, y - 1, 0) >= center) code |= (1 << 5);
                        if (normalizedGrayFace.getRaster().getSample(x + 1, y,     0) >= center) code |= (1 << 4);
                        if (normalizedGrayFace.getRaster().getSample(x + 1, y + 1, 0) >= center) code |= (1 << 3);
                        if (normalizedGrayFace.getRaster().getSample(x,     y + 1, 0) >= center) code |= (1 << 2);
                        if (normalizedGrayFace.getRaster().getSample(x - 1, y + 1, 0) >= center) code |= (1 << 1);
                        if (normalizedGrayFace.getRaster().getSample(x - 1, y,     0) >= center) code |= (1 << 0);

                        // Quantize 256 LBP codes into HISTOGRAM_BINS
                        int bin = (code * HISTOGRAM_BINS) / 256;
                        if (bin >= HISTOGRAM_BINS) bin = HISTOGRAM_BINS - 1;
                        cellHist[bin]++;
                    }
                }

                // L1 Normalize cell histogram
                double total = 0.0;
                for (double v : cellHist) total += v;
                if (total > 0.0) {
                    for (int b = 0; b < HISTOGRAM_BINS; b++) {
                        cellHist[b] /= total;
                    }
                }

                // Append to full feature vector
                for (int b = 0; b < HISTOGRAM_BINS; b++) {
                    featureVector[vectorIdx++] = cellHist[b];
                }
            }
        }

        return featureVector;
    }

    /**
     * Calculates Chi-Square distance between two feature vectors.
     * Lower distance means closer match.
     */
    public static double computeChiSquareDistance(double[] vecA, double[] vecB) {
        if (vecA == null || vecB == null || vecA.length != vecB.length) {
            return Double.MAX_VALUE;
        }
        double distance = 0.0;
        double eps = 1e-10;
        for (int i = 0; i < vecA.length; i++) {
            double diff = vecA[i] - vecB[i];
            double sum = vecA[i] + vecB[i] + eps;
            distance += (diff * diff) / sum;
        }
        return distance * 0.5;
    }

    /**
     * Converts Chi-Square distance into a user-friendly Confidence Percentage (0% to 100%).
     */
    public static double calculateConfidence(double distance) {
        if (distance <= 0.0) {
            return 99.4;
        }
        if (distance >= RECOGNITION_THRESHOLD) {
            double excess = distance - RECOGNITION_THRESHOLD;
            double decay = Math.max(0.0, 1.0 - (excess / (RECOGNITION_THRESHOLD * 0.6)));
            return Math.round(decay * 48.0 * 10.0) / 10.0;
        } else {
            double normalized = 1.0 - (distance / RECOGNITION_THRESHOLD);
            double confidence = 60.0 + (normalized * 39.4);
            return Math.min(99.4, Math.round(confidence * 10.0) / 10.0);
        }
    }

    /**
     * Renders modern biometric HUD graphics on the camera frame:
     * - Bounding box (Green for recognized, Red for unknown, Cyan for scan mode)
     * - Overlay cards with Student Name, Roll No, Confidence Score, and Status
     */
    public static void drawBiometricHUD(BufferedImage frame, RecognitionResult result, String customNotice) {
        if (frame == null) return;
        Graphics2D g = frame.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        Rectangle box = result != null && result.faceBox != null ? result.faceBox :
                new Rectangle(frame.getWidth() / 4, frame.getHeight() / 4, frame.getWidth() / 2, frame.getHeight() / 2);

        Color mainColor;
        String titleText;
        String subText;
        boolean recognized = (result != null && result.isRecognized);

        if (recognized) {
            mainColor = new Color(16, 185, 129); // Modern Emerald Green
            titleText = "✔ PRESENT: " + result.studentName;
            subText = "Roll: " + result.rollNo + " | Match: " + String.format("%.1f%%", result.confidence)
                    + (customNotice != null ? " | " + customNotice : "");
        } else if (result != null && result.faceDetected) {
            mainColor = new Color(239, 68, 68); // Red
            titleText = "Unknown Face";
            subText = customNotice != null ? customNotice : "No Enrolled Match Found in Database";
        } else {
            mainColor = new Color(59, 130, 246); // Blue Scan Mode
            titleText = "Face AI Biometric Terminal";
            subText = "Position face within scanning target frame";
        }

        // 1. Top HUD Ribbon
        int ribbonH = 34;
        g.setColor(new Color(15, 23, 42, 220));
        g.fillRoundRect(12, 10, frame.getWidth() - 24, ribbonH, 8, 8);
        g.setColor(mainColor);
        g.setStroke(new BasicStroke(1.5f));
        g.drawRoundRect(12, 10, frame.getWidth() - 24, ribbonH, 8, 8);

        // Status indicator dot
        g.fillOval(26, 21, 12, 12);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Segoe UI", Font.BOLD, 13));
        if (recognized) {
            g.drawString("STATUS: PRESENT", 46, 29);
            g.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            g.setColor(new Color(226, 232, 240));
            g.drawString(" |  Student: " + result.studentName + " (" + result.rollNo + ")  |  Biometric Match: " + String.format("%.1f%%", result.confidence), 170, 29);
        } else if (result != null && result.faceDetected) {
            g.drawString("STATUS: UNKNOWN FACE", 46, 29);
            g.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            g.setColor(new Color(226, 232, 240));
            g.drawString(" |  No biometric enrollment found for this face", 200, 29);
        } else {
            g.drawString("STATUS: READY / SCANNING", 46, 29);
            g.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            g.setColor(new Color(203, 213, 225));
            g.drawString(" |  Stand in front of the camera to record attendance", 220, 29);
        }

        // 2. Draw HUD Corner Brackets around face
        g.setColor(mainColor);
        g.setStroke(new BasicStroke(3.5f));
        int cornerLen = Math.min(box.width / 4, 32);

        // Top-Left
        g.drawLine(box.x, box.y, box.x + cornerLen, box.y);
        g.drawLine(box.x, box.y, box.x, box.y + cornerLen);
        // Top-Right
        g.drawLine(box.x + box.width, box.y, box.x + box.width - cornerLen, box.y);
        g.drawLine(box.x + box.width, box.y, box.x + box.width, box.y + cornerLen);
        // Bottom-Left
        g.drawLine(box.x, box.y + box.height, box.x + cornerLen, box.y + box.height);
        g.drawLine(box.x, box.y + box.height, box.x, box.y + box.height - cornerLen);
        // Bottom-Right
        g.drawLine(box.x + box.width, box.y + box.height, box.x + box.width - cornerLen, box.y + box.height);
        g.drawLine(box.x + box.width, box.y + box.height, box.x + box.width, box.y + box.height - cornerLen);

        // Subtle face bounding box tint
        g.setColor(new Color(mainColor.getRed(), mainColor.getGreen(), mainColor.getBlue(), 35));
        g.fillRect(box.x, box.y, box.width, box.height);

        // 3. Header info badge above face box
        int badgeH = recognized ? 56 : 48;
        int badgeW = Math.max(box.width, 260);
        int badgeX = Math.max(10, Math.min(frame.getWidth() - badgeW - 10, box.x + (box.width - badgeW) / 2));
        int badgeY = Math.max(50, box.y - badgeH - 10);

        // Background pill
        g.setColor(new Color(15, 23, 42, 225)); // Dark Slate
        g.fillRoundRect(badgeX, badgeY, badgeW, badgeH, 12, 12);
        g.setColor(mainColor);
        g.setStroke(new BasicStroke(recognized ? 2.5f : 1.5f));
        g.drawRoundRect(badgeX, badgeY, badgeW, badgeH, 12, 12);

        // If recognized, draw prominent bold green "PRESENT" tag
        if (recognized) {
            g.setColor(new Color(16, 185, 129));
            g.fillRoundRect(badgeX + 8, badgeY + 8, 88, 22, 6, 6);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Segoe UI", Font.BOLD, 13));
            g.drawString("✔ PRESENT", badgeX + 14, badgeY + 24);

            g.setColor(Color.WHITE);
            g.setFont(new Font("Segoe UI", Font.BOLD, 14));
            g.drawString(result.studentName, badgeX + 104, badgeY + 25);

            g.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            g.setColor(new Color(226, 232, 240));
            g.drawString(subText, badgeX + 12, badgeY + 46);
        } else {
            // Text inside badge for non-recognized
            g.setColor(Color.WHITE);
            g.setFont(new Font("Segoe UI", Font.BOLD, 14));
            g.drawString(titleText, badgeX + 12, badgeY + 22);

            g.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            g.setColor(new Color(226, 232, 240));
            g.drawString(subText, badgeX + 12, badgeY + 40);
        }

        g.dispose();
    }
}
