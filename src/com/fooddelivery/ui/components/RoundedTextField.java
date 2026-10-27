package com.fooddelivery.ui.components;

import com.fooddelivery.ui.AppTheme;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.JTextField;

/**
 * RoundedTextField: Custom text input field with 12px rounded corners,
 * Buttercream fill, thin darker-buttercream border, and Serenity Blue focus highlight.
 */
public class RoundedTextField extends JTextField implements FocusListener {

    private int cornerRadius = 12;
    private String placeholder = "";

    public RoundedTextField() {
        this("");
    }

    public RoundedTextField(String placeholder) {
        this(placeholder, 12);
    }

    public RoundedTextField(String placeholder, int cornerRadius) {
        this.placeholder = placeholder;
        this.cornerRadius = cornerRadius;

        setFont(AppTheme.fontBody());
        setForeground(AppTheme.TEXT_PRIMARY);
        setBackground(AppTheme.BUTTERCREAM);
        setCaretColor(AppTheme.TEXT_PRIMARY);
        setOpaque(false);
        setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 14, 8, 14));
        addFocusListener(this);
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
        repaint();
    }

    public void setCornerRadius(int r) {
        this.cornerRadius = r;
        repaint();
    }

    @Override
    public Insets getInsets() {
        return new Insets(8, 14, 8, 14);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        AppTheme.enableAntialiasing(g2);

        int w = getWidth();
        int h = getHeight();

        // 1. Fill Buttercream rounded background
        g2.setColor(isEnabled() ? AppTheme.BUTTERCREAM : new Color(0xEB, 0xE0, 0xC6));
        g2.fillRoundRect(1, 1, w - 2, h - 2, cornerRadius, cornerRadius);

        // 2. Draw border (Serenity Blue when focused, Darker Buttercream when unfocused)
        if (hasFocus()) {
            g2.setColor(AppTheme.SERENITY_BLUE);
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(1, 1, w - 3, h - 3, cornerRadius, cornerRadius);
        } else {
            g2.setColor(AppTheme.BUTTERCREAM_DARK);
            g2.setStroke(new BasicStroke(1.0f));
            g2.drawRoundRect(1, 1, w - 2, h - 2, cornerRadius, cornerRadius);
        }

        // 3. Draw placeholder if empty and not focused
        if (getText().isEmpty() && !hasFocus() && placeholder != null && !placeholder.isEmpty()) {
            g2.setColor(AppTheme.TEXT_MUTED);
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            int y = (h + fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString(placeholder, 14, y);
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
