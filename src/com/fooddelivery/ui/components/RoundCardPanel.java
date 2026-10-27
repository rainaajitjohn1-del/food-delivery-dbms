package com.fooddelivery.ui.components;

import com.fooddelivery.ui.AppTheme;
import java.awt.*;
import javax.swing.JPanel;

/**
 * Card container panel with Warm Off-White surface, 16px corner radius,
 * subtle border and soft drop shadow.
 */
public class RoundCardPanel extends JPanel {

    private int radius = AppTheme.RADIUS_CARD;
    private Color backgroundColor = AppTheme.WARM_OFF_WHITE;
    private boolean drawShadow = true;
    private boolean drawBorder = true;

    public RoundCardPanel() {
        this(new BorderLayout());
    }

    public RoundCardPanel(LayoutManager layout) {
        super(layout);
        setOpaque(false);
    }

    public RoundCardPanel(Color bg, int radius) {
        this(new BorderLayout());
        this.backgroundColor = bg;
        this.radius = radius;
    }

    public void setBackgroundColor(Color c) {
        this.backgroundColor = c;
        repaint();
    }

    public void setDrawShadow(boolean s) {
        this.drawShadow = s;
        repaint();
    }

    public void setDrawBorder(boolean b) {
        this.drawBorder = b;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        AppTheme.enableAntialiasing(g2);

        int w = getWidth();
        int h = getHeight();
        int shadowOffset = drawShadow ? 3 : 0;
        int shadowBlur = drawShadow ? 2 : 0;

        int cardW = w - (drawShadow ? 6 : 2);
        int cardH = h - (drawShadow ? 6 : 2);
        int cardX = drawShadow ? 3 : 1;
        int cardY = drawShadow ? 2 : 1;

        if (drawShadow) {
            // Soft drop shadow
            g2.setColor(AppTheme.SHADOW_COLOR);
            g2.fillRoundRect(cardX, cardY + shadowOffset, cardW, cardH, radius, radius);
        }

        // Card body
        g2.setColor(backgroundColor != null ? backgroundColor : AppTheme.WARM_OFF_WHITE);
        g2.fillRoundRect(cardX, cardY, cardW, cardH, radius, radius);

        // Subtle border
        if (drawBorder) {
            g2.setColor(AppTheme.BORDER_LIGHT);
            g2.setStroke(new BasicStroke(1.0f));
            g2.drawRoundRect(cardX, cardY, cardW, cardH, radius, radius);
        }

        g2.dispose();
        super.paintComponent(g);
    }
}
