package com.fooddelivery.ui.components;

import com.fooddelivery.ui.AppTheme;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;

/**
 * Secondary Pill Button: Serenity Blue background (#8FA7C8) with White text.
 */
public class SecondaryButton extends JButton {

    private boolean isHovered = false;
    private boolean isPressed = false;
    private int cornerRadius = 999;

    public SecondaryButton(String text) {
        super(text);
        init();
    }

    public SecondaryButton(String text, int cornerRadius) {
        super(text);
        this.cornerRadius = cornerRadius;
        init();
    }

    private void init() {
        setFont(AppTheme.fontBodyBold());
        setForeground(AppTheme.TEXT_WHITE);
        setBackground(AppTheme.SERENITY_BLUE);
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

        addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                repaint();
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
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
        return new Insets(8, 18, 8, 18);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        AppTheme.enableAntialiasing(g2);

        int w = getWidth();
        int h = getHeight();
        int r = (cornerRadius >= 999) ? h : cornerRadius;

        if (!isEnabled()) {
            g2.setColor(new Color(0xD0, 0xD8, 0xE4));
        } else if (isPressed) {
            g2.setColor(new Color(0x6A, 0x82, 0xA4));
        } else if (isHovered) {
            g2.setColor(AppTheme.SERENITY_BLUE_HOVER);
        } else {
            g2.setColor(AppTheme.SERENITY_BLUE);
        }

        g2.fillRoundRect(0, 0, w, h, r, r);

        // Focus ring
        if (hasFocus()) {
            g2.setColor(AppTheme.ESPRESSO);
            g2.setStroke(new BasicStroke(1.8f));
            g2.drawRoundRect(1, 1, w - 3, h - 3, r, r);
        }

        FontMetrics fm = g2.getFontMetrics(getFont());
        int strWidth = fm.stringWidth(getText());
        int strHeight = fm.getAscent();

        int x = (w - strWidth) / 2;
        int y = (h + strHeight) / 2 - 2;

        if (getIcon() != null) {
            int iconWidth = getIcon().getIconWidth();
            int totalWidth = iconWidth + (getText().isEmpty() ? 0 : 6 + strWidth);
            int startX = (w - totalWidth) / 2;
            int iconY = (h - getIcon().getIconHeight()) / 2;
            getIcon().paintIcon(this, g2, startX, iconY);
            x = startX + iconWidth + 6;
        }

        if (!getText().isEmpty()) {
            g2.setFont(getFont());
            g2.setColor(isEnabled() ? AppTheme.TEXT_WHITE : new Color(0x8A, 0x94, 0xA0));
            g2.drawString(getText(), x, y);
        }

        g2.dispose();
    }
}
