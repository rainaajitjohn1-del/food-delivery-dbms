package com.fooddelivery.ui.components;

import com.fooddelivery.ui.AppTheme;
import com.fooddelivery.ui.Icons;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.Icon;
import javax.swing.JTextField;

/**
 * SearchField: Pill-shaped Buttercream search field with leading search icon.
 */
public class SearchField extends JTextField implements FocusListener {

    private String placeholder = "Search for restaurants, cuisines, dishes...";
    private final Icon searchIcon = Icons.search(16, AppTheme.TEXT_MUTED);

    public SearchField() {
        this("Search for restaurants, cuisines, dishes...");
    }

    public SearchField(String placeholder) {
        this.placeholder = placeholder;
        setFont(AppTheme.fontBody());
        setForeground(AppTheme.TEXT_PRIMARY);
        setBackground(AppTheme.BUTTERCREAM);
        setCaretColor(AppTheme.TEXT_PRIMARY);
        setOpaque(false);
        setBorder(null);
        addFocusListener(this);
    }

    public void setPlaceholder(String p) {
        this.placeholder = p;
        repaint();
    }

    @Override
    public Insets getInsets() {
        return new Insets(8, 38, 8, 16);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        AppTheme.enableAntialiasing(g2);

        int w = getWidth();
        int h = getHeight();

        // Fill Buttercream pill
        g2.setColor(AppTheme.BUTTERCREAM);
        g2.fillRoundRect(0, 0, w, h, h, h);

        // Border
        if (hasFocus()) {
            g2.setColor(AppTheme.SERENITY_BLUE);
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(1, 1, w - 2, h - 2, h - 2, h - 2);
        } else {
            g2.setColor(AppTheme.BUTTERCREAM_DARK);
            g2.setStroke(new BasicStroke(1.0f));
            g2.drawRoundRect(1, 1, w - 2, h - 2, h - 2, h - 2);
        }

        // Draw search icon
        int iconY = (h - searchIcon.getIconHeight()) / 2;
        searchIcon.paintIcon(this, g2, 14, iconY);

        // Draw placeholder if empty
        if (getText().isEmpty() && !hasFocus()) {
            g2.setColor(AppTheme.TEXT_MUTED);
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            int y = (h + fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString(placeholder, 38, y);
        }

        g2.dispose();
        super.paintComponent(g);
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
