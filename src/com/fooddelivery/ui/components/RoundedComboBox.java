package com.fooddelivery.ui.components;

import com.fooddelivery.ui.AppTheme;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;

/**
 * RoundedComboBox: Dropdown with 12px rounded corners, Buttercream fill,
 * and clean arrow button rendering.
 */
public class RoundedComboBox<E> extends JComboBox<E> {

    public RoundedComboBox(E[] items) {
        super(items);
        init();
    }

    public RoundedComboBox() {
        super();
        init();
    }

    private void init() {
        setFont(AppTheme.fontBody());
        setBackground(AppTheme.BUTTERCREAM);
        setForeground(AppTheme.TEXT_PRIMARY);
        setOpaque(false);
        setBorder(new EmptyBorder(4, 10, 4, 10));

        setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton btn = new JButton() {
                    @Override
                    protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        AppTheme.enableAntialiasing(g2);
                        int w = getWidth();
                        int h = getHeight();

                        // Down arrow
                        g2.setColor(AppTheme.ESPRESSO);
                        int[] xPoints = {w / 2 - 4, w / 2 + 4, w / 2};
                        int[] yPoints = {h / 2 - 2, h / 2 - 2, h / 2 + 3};
                        g2.fillPolygon(xPoints, yPoints, 3);
                        g2.dispose();
                    }
                };
                btn.setContentAreaFilled(false);
                btn.setBorderPainted(false);
                btn.setFocusPainted(false);
                btn.setOpaque(false);
                return btn;
            }

            @Override
            public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
                // Background painted in container paintComponent
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        AppTheme.enableAntialiasing(g2);

        int w = getWidth();
        int h = getHeight();
        int r = 12;

        g2.setColor(AppTheme.BUTTERCREAM);
        g2.fillRoundRect(1, 1, w - 2, h - 2, r, r);

        if (hasFocus()) {
            g2.setColor(AppTheme.SERENITY_BLUE);
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(1, 1, w - 3, h - 3, r, r);
        } else {
            g2.setColor(AppTheme.BUTTERCREAM_DARK);
            g2.setStroke(new BasicStroke(1.0f));
            g2.drawRoundRect(1, 1, w - 2, h - 2, r, r);
        }

        g2.dispose();
        super.paintComponent(g);
    }
}
