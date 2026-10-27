package com.fooddelivery.ui.components;

import com.fooddelivery.ui.AppTheme;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.Icon;
import javax.swing.JButton;

/**
 * PillChipButton: Fully rounded pill button (radius = height) with Buttercream fill,
 * Serenity Blue border, hover/pressed states, and focus outline.
 */
public class PillChipButton extends JButton implements FocusListener {

    private boolean isHovered = false;
    private boolean isPressed = false;
    private boolean isSelectedChip = false;

    public PillChipButton(String text) {
        super(text);
        init();
    }

    private void init() {
        setFont(AppTheme.fontSmallBold());
        setForeground(AppTheme.ESPRESSO);
        setBackground(AppTheme.BUTTERCREAM);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        addFocusListener(this);
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

    public PillChipButton(String text, Icon icon) {
        super(text, icon);
        init();
    }

    public void setSelectedChip(boolean selected) {
        this.isSelectedChip = selected;
        repaint();
    }

    @Override
    public Insets getInsets() {
        return new Insets(6, 14, 6, 14);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        AppTheme.enableAntialiasing(g2);

        int w = getWidth();
        int h = getHeight();
        int r = h; // Fully rounded pill shape

        // 1. Background Fill
        if (!isEnabled()) {
            g2.setColor(new Color(0xEB, 0xE0, 0xC6));
        } else if (isSelectedChip) {
            g2.setColor(AppTheme.ESPRESSO);
        } else if (isPressed) {
            g2.setColor(AppTheme.BUTTERCREAM_DARK);
        } else if (isHovered) {
            g2.setColor(AppTheme.BUTTERCREAM_TINT);
        } else {
            g2.setColor(AppTheme.BUTTERCREAM);
        }
        g2.fillRoundRect(1, 1, w - 2, h - 2, r, r);

        // 2. Border
        if (hasFocus() || isSelectedChip) {
            g2.setColor(isSelectedChip ? AppTheme.ESPRESSO : AppTheme.SERENITY_BLUE);
            g2.setStroke(new BasicStroke(1.8f));
            g2.drawRoundRect(1, 1, w - 3, h - 3, r, r);
        } else {
            g2.setColor(AppTheme.SERENITY_BLUE);
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawRoundRect(1, 1, w - 2, h - 2, r, r);
        }

        // 3. Text and Icon
        String text = getText() != null ? getText() : "";
        FontMetrics fm = g2.getFontMetrics(getFont());
        int strW = text.isEmpty() ? 0 : fm.stringWidth(text);
        int iconW = getIcon() != null ? getIcon().getIconWidth() : 0;
        int gap = (iconW > 0 && !text.isEmpty()) ? 6 : 0;
        int totalW = iconW + gap + strW;
        int startX = (w - totalW) / 2;

        if (getIcon() != null) {
            int iconY = (h - getIcon().getIconHeight()) / 2;
            getIcon().paintIcon(this, g2, startX, iconY);
        }

        if (!text.isEmpty()) {
            int x = startX + iconW + gap;
            int y = (h + fm.getAscent() - fm.getDescent()) / 2;
            g2.setFont(getFont());
            Color textCol = isSelectedChip ? AppTheme.BUTTERCREAM : AppTheme.ESPRESSO;
            g2.setColor(isEnabled() ? textCol : AppTheme.TEXT_MUTED);
            g2.drawString(text, x, y);
        }

        g2.dispose();
    }

    @Override
    public void focusGained(FocusEvent e) {
        repaint();
    }

    @Override
    public void focusLost(FocusEvent e) {
        repaint();
    }
}
