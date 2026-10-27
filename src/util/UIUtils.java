package util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Modern UI Design System and helper utilities for Swing components.
 * Provides curated colors, fonts, stat cards, styled buttons, and table renderers.
 * Location: src/util/UIUtils.java
 */
public class UIUtils {

    // Color Palette
    public static final Color PRIMARY = new Color(30, 58, 138);       // Navy Blue #1E3A8A
    public static final Color PRIMARY_HOVER = new Color(23, 37, 84);  // Darker Navy #172554
    public static final Color ACCENT = new Color(37, 99, 235);        // Royal Blue #2563EB
    public static final Color ACCENT_LIGHT = new Color(219, 234, 254);// Soft Blue #DBEAFE
    public static final Color BG_LIGHT = new Color(248, 250, 252);     // #F8FAFC
    public static final Color CARD_BG = Color.WHITE;
    public static final Color BORDER = new Color(226, 232, 240);      // #E2E8F0
    public static final Color TEXT_DARK = new Color(15, 23, 42);      // #0F172A
    public static final Color TEXT_MUTED = new Color(100, 116, 139);   // #64748B
    public static final Color SUCCESS = new Color(16, 185, 129);      // Green #10B981
    public static final Color DANGER = new Color(239, 68, 68);        // Red #EF4444
    public static final Color WARNING = new Color(245, 158, 11);      // Amber #F59E0B

    // Typography
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_SUBHEADER = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_STAT_NUMBER = new Font("Segoe UI", Font.BOLD, 28);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);

    /**
     * Creates a styled modern button with rounded corners and hover effect.
     */
    public static JButton createButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(bg.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(bg.brighter());
                } else {
                    g2.setColor(bg);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_BOLD);
        btn.setForeground(fg);
        btn.setBackground(bg);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setMargin(new Insets(8, 16, 8, 16));
        return btn;
    }

    public static JButton createPrimaryButton(String text) {
        return createButton(text, PRIMARY, Color.WHITE);
    }

    public static JButton createAccentButton(String text) {
        return createButton(text, ACCENT, Color.WHITE);
    }

    public static JButton createSuccessButton(String text) {
        return createButton(text, SUCCESS, Color.WHITE);
    }

    public static JButton createDangerButton(String text) {
        return createButton(text, DANGER, Color.WHITE);
    }

    public static JButton createSecondaryButton(String text) {
        JButton btn = createButton(text, new Color(241, 245, 249), TEXT_DARK);
        btn.setBorder(BorderFactory.createLineBorder(BORDER, 1));
        return btn;
    }

    /**
     * Creates a container panel styled like a modern elevated card.
     */
    public static JPanel createCardPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2.setColor(BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        return panel;
    }

    /**
     * Creates a statistical metric card for the Dashboard.
     */
    public static JPanel createStatCard(String title, String value, String note, Color accentColor) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2.setColor(BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);

                // Top accent strip
                g2.setColor(accentColor);
                g2.fillRoundRect(0, 0, getWidth() - 1, 6, 14, 14);
                g2.fillRect(0, 4, getWidth() - 1, 3);

                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setLayout(null);
        card.setPreferredSize(new Dimension(210, 115));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(FONT_SUBHEADER);
        lblTitle.setForeground(TEXT_MUTED);
        lblTitle.setBounds(16, 16, 180, 18);

        JLabel lblValue = new JLabel(value);
        lblValue.setFont(FONT_STAT_NUMBER);
        lblValue.setForeground(TEXT_DARK);
        lblValue.setBounds(16, 38, 180, 36);

        JLabel lblNote = new JLabel(note);
        lblNote.setFont(FONT_SMALL);
        lblNote.setForeground(accentColor.darker());
        lblNote.setBounds(16, 78, 180, 16);

        card.add(lblTitle);
        card.add(lblValue);
        card.add(lblNote);

        return card;
    }

    /**
     * Styles JTable with header styling, row height, and alternating colors.
     */
    public static void styleTable(JTable table) {
        table.setRowHeight(34);
        table.setFont(FONT_REGULAR);
        table.setShowGrid(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(241, 245, 249));
        table.setSelectionBackground(new Color(238, 242, 255));
        table.setSelectionForeground(TEXT_DARK);

        // Header
        table.getTableHeader().setFont(FONT_BOLD);
        table.getTableHeader().setBackground(new Color(248, 250, 252));
        table.getTableHeader().setForeground(TEXT_MUTED);
        table.getTableHeader().setPreferredSize(new Dimension(0, 38));
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));
    }

    /**
     * Custom TableCellRenderer rendering attendance status as styled color badges.
     */
    public static class StatusBadgeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int col) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            lbl.setFont(FONT_BOLD);
            String status = value != null ? value.toString() : "";

            if ("PRESENT".equalsIgnoreCase(status)) {
                lbl.setForeground(SUCCESS);
                lbl.setText("● PRESENT");
            } else if ("ABSENT".equalsIgnoreCase(status)) {
                lbl.setForeground(DANGER);
                lbl.setText("● ABSENT");
            } else if ("LATE".equalsIgnoreCase(status)) {
                lbl.setForeground(WARNING);
                lbl.setText("● LATE");
            } else {
                lbl.setForeground(TEXT_MUTED);
            }
            return lbl;
        }
    }
}
