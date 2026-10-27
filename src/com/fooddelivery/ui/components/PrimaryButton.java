package com.fooddelivery.ui.components;

import com.fooddelivery.ui.AppTheme;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;

/**
 * Primary Pill Button: Espresso background (#3B2418) with Buttercream text (#F3E3C0).
 */
public class PrimaryButton extends JButton {

    private boolean isHovered = false;
    private boolean isPressed = false;
    private int cornerRadius = 999;

    public PrimaryButton(String text) {
        super(text);
        init();
    }

    public PrimaryButton(String text, int cornerRadius) {
        super(text);
        this.cornerRadius = cornerRadius;
        init();
    }

    private void init() {
        setFont(AppTheme.fontBodyBold());
        setForeground(AppTheme.TEXT_ON_DARK);
        setBackground(AppTheme.ESPRESSO);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (isEnabled()) {
                    isHovered = true;
                    repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                isPressed = false;
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (isEnabled()) {
                    isPressed = true;
                    repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                isPressed = false;
                repaint();
            }
        });
    }

    public void setCornerRadius(int r) {
        this.cornerRadius = r;
        repaint();
    }

    @Override
    public Insets getInsets() {
        return new Insets(10, 22, 10, 22);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        AppTheme.enableAntialiasing(g2);

        int w = getWidth();
        int h = getHeight();
        int r = (cornerRadius >= 999) ? h : cornerRadius;

        if (!isEnabled()) {
            g2.setColor(new Color(0xD0, 0xC6, 0xBC));
        } else if (isPressed) {
            g2.setColor(AppTheme.ESPRESSO_DARK);
        } else if (isHovered) {
            g2.setColor(AppTheme.ESPRESSO_HOVER);
        } else {
            g2.setColor(AppTheme.ESPRESSO);
        }

        g2.fillRoundRect(0, 0, w, h, r, r);

        // Draw text and icon
        FontMetrics fm = g2.getFontMetrics(getFont());
        int strWidth = fm.stringWidth(getText());
        int strHeight = fm.getAscent();

        int x = (w - strWidth) / 2;
        int y = (h + strHeight) / 2 - 2;

        if (getIcon() != null) {
            int iconWidth = getIcon().getIconWidth();
            int totalWidth = iconWidth + (getText().isEmpty() ? 0 : 8 + strWidth);
            int startX = (w - totalWidth) / 2;
            int iconY = (h - getIcon().getIconHeight()) / 2;
            getIcon().paintIcon(this, g2, startX, iconY);
            x = startX + iconWidth + 8;
        }

        if (!getText().isEmpty()) {
            g2.setFont(getFont());
            g2.setColor(isEnabled() ? AppTheme.TEXT_ON_DARK : new Color(0x80, 0x76, 0x6E));
            g2.drawString(getText(), x, y);
        }

        g2.dispose();
    }
}
