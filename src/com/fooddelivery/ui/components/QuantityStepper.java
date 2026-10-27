package com.fooddelivery.ui.components;

import com.fooddelivery.ui.AppTheme;
import com.fooddelivery.ui.Icons;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.*;

/**
 * QuantityStepper: [-] Count [+] with small circular outline buttons.
 */
public class QuantityStepper extends JPanel {

    private int value;
    private final int min;
    private final int max;
    private final JLabel countLabel;
    private final List<Consumer<Integer>> listeners = new ArrayList<>();

    public QuantityStepper(int initial, int min, int max) {
        super(new FlowLayout(FlowLayout.CENTER, 6, 0));
        setOpaque(false);
        this.value = initial;
        this.min = min;
        this.max = max;

        CircleButton minusBtn = new CircleButton(false);
        CircleButton plusBtn = new CircleButton(true);

        countLabel = new JLabel(String.valueOf(value), SwingConstants.CENTER);
        countLabel.setFont(AppTheme.fontBodyBold());
        countLabel.setForeground(AppTheme.TEXT_PRIMARY);
        countLabel.setPreferredSize(new Dimension(28, 28));

        minusBtn.addActionListener(e -> {
            if (value > min) {
                setValue(value - 1);
                notifyListeners();
            }
        });

        plusBtn.addActionListener(e -> {
            if (value < max) {
                setValue(value + 1);
                notifyListeners();
            }
        });

        add(minusBtn);
        add(countLabel);
        add(plusBtn);
    }

    public int getValue() {
        return value;
    }

    public void setValue(int v) {
        this.value = Math.max(min, Math.min(max, v));
        countLabel.setText(String.valueOf(this.value));
        repaint();
    }

    public void addChangeListener(Consumer<Integer> listener) {
        listeners.add(listener);
    }

    private void notifyListeners() {
        for (Consumer<Integer> l : listeners) {
            l.accept(value);
        }
    }

    private static class CircleButton extends JButton {
        private final boolean isPlus;
        private boolean isHovered = false;

        public CircleButton(boolean isPlus) {
            this.isPlus = isPlus;
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setOpaque(false);
            setPreferredSize(new Dimension(28, 28));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    isHovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            AppTheme.enableAntialiasing(g2);

            int w = getWidth();
            int h = getHeight();

            if (isHovered) {
                g2.setColor(AppTheme.BUTTERCREAM);
                g2.fillOval(2, 2, w - 4, h - 4);
            }

            g2.setColor(AppTheme.SERENITY_BLUE);
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawOval(2, 2, w - 4, h - 4);

            Icon icon = isPlus ? Icons.plus(12, AppTheme.ESPRESSO) : Icons.minus(12, AppTheme.ESPRESSO);
            int ix = (w - icon.getIconWidth()) / 2;
            int iy = (h - icon.getIconHeight()) / 2;
            icon.paintIcon(this, g2, ix, iy);

            g2.dispose();
        }
    }
}
