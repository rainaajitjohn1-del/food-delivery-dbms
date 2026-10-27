package com.fooddelivery.ui.components;

import com.fooddelivery.ui.AppTheme;
import java.awt.*;
import javax.swing.Icon;
import javax.swing.JComponent;

/**
 * PillBadge: Rounded metric or status badge ("30 min", "4.6 ★", "Bestseller", "Placed", etc.)
 */
public class PillBadge extends JComponent {

    private String text;
    private Color bg;
    private Color fg;
    private Icon icon;

    public PillBadge(String text, Color bg, Color fg) {
        this(text, bg, fg, null);
    }

    public PillBadge(String text, Color bg, Color fg, Icon icon) {
        this.text = text;
        this.bg = bg;
        this.fg = fg;
        this.icon = icon;
        setFont(AppTheme.fontMetadata());
        setOpaque(false);
    }

    public static PillBadge time(String text) {
        return new PillBadge(text, AppTheme.BUTTERCREAM, AppTheme.TEXT_PRIMARY);
    }

    public static PillBadge rating(String text) {
        return new PillBadge(text, AppTheme.SERENITY_BLUE, AppTheme.TEXT_WHITE);
    }

    public static PillBadge bestseller() {
        return new PillBadge("Bestseller", AppTheme.ESPRESSO, AppTheme.BUTTERCREAM);
    }

    public static PillBadge status(String status) {
        if (status == null) status = "Unknown";
        Color bg;
        Color fg = AppTheme.TEXT_WHITE;
        switch (status) {
            case "Delivered" -> { bg = AppTheme.SUCCESS_GREEN; }
            case "Preparing" -> { bg = AppTheme.WARNING_AMBER; }
            case "Out for Delivery" -> { bg = new Color(0x3F, 0x51, 0xB5); }
            case "Placed" -> { bg = AppTheme.SERENITY_BLUE; }
            case "Assigned" -> { bg = new Color(0x60, 0x7D, 0x8B); }
            default -> {
                bg = AppTheme.BUTTERCREAM;
                fg = AppTheme.TEXT_PRIMARY;
            }
        }
        return new PillBadge(status, bg, fg);
    }

    public void setText(String text) {
        this.text = text;
        revalidate();
        repaint();
    }

    public void setColors(Color bg, Color fg) {
        this.bg = bg;
        this.fg = fg;
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        FontMetrics fm = getFontMetrics(getFont());
        int w = fm.stringWidth(text) + 20;
        if (icon != null) w += icon.getIconWidth() + 4;
        int h = Math.max(fm.getHeight() + 8, 24);
        return new Dimension(w, h);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        AppTheme.enableAntialiasing(g2);

        int w = getWidth();
        int h = getHeight();

        g2.setColor(bg);
        g2.fillRoundRect(0, 0, w, h, h, h);

        FontMetrics fm = g2.getFontMetrics(getFont());
        int strW = fm.stringWidth(text);
        int totalContent = strW + (icon != null ? icon.getIconWidth() + 4 : 0);

        int x = (w - totalContent) / 2;
        int y = (h + fm.getAscent() - fm.getDescent()) / 2;

        if (icon != null) {
            int iconY = (h - icon.getIconHeight()) / 2;
            icon.paintIcon(this, g2, x, iconY);
            x += icon.getIconWidth() + 4;
        }

        g2.setColor(fg);
        g2.setFont(getFont());
        g2.drawString(text, x, y);

        g2.dispose();
    }
}
