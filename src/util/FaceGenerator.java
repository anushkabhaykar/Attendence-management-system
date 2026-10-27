package util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/**
 * Utility to generate a realistic synthetic student face image for offline testing.
 */
public class FaceGenerator {

    public static void main(String[] args) {
        generateSampleFace("sample_faces/sample_student.png");
    }

    public static void generateSampleFace(String outputPath) {
        int w = 320, h = 320;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Background
        g.setColor(new Color(240, 244, 250));
        g.fillRect(0, 0, w, h);

        int cx = w / 2;
        int cy = h / 2;

        // Clothes / shoulders
        g.setColor(new Color(30, 58, 138));
        g.fillRoundRect(cx - 95, cy + 90, 190, 100, 30, 30);

        // Neck
        g.setColor(new Color(230, 175, 140));
        g.fillRect(cx - 25, cy + 50, 50, 45);

        // Head (Skin tone in standard cluster)
        g.fillOval(cx - 65, cy - 80, 130, 160);

        // Hair
        g.setColor(new Color(35, 20, 15));
        g.fillArc(cx - 70, cy - 95, 140, 110, 0, 180);

        // Eyes
        g.setColor(new Color(20, 20, 20));
        g.fillOval(cx - 35, cy - 10, 14, 10);
        g.fillOval(cx + 21, cy - 10, 14, 10);

        // Nose
        g.setColor(new Color(205, 150, 120));
        g.fillOval(cx - 4, cy + 12, 8, 16);

        // Smile
        g.setColor(new Color(175, 60, 60));
        g.fillArc(cx - 20, cy + 38, 40, 16, 0, -180);

        g.dispose();

        try {
            File out = new File(outputPath);
            out.getParentFile().mkdirs();
            ImageIO.write(img, "PNG", out);
            System.out.println("[FaceGenerator] Generated: " + out.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
