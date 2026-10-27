package com.fooddelivery.ui.components;

import com.fooddelivery.ui.AppTheme;
import com.fooddelivery.ui.Icons;
import java.awt.*;
import java.awt.geom.Path2D;
import javax.swing.*;

/**
 * HeroBanner: Wide espresso hero banner with serenity blue decorative blob accent,
 * headline "Good Food Better Days", subtitle, and "Get Started" pill button.
 */
public class HeroBanner extends JPanel {

    private final SecondaryButton actionBtn;

    public HeroBanner(Runnable onAction) {
        super(null);
        setOpaque(false);
        setPreferredSize(new Dimension(800, 180));
        setMinimumSize(new Dimension(600, 160));

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(24, 36, 24, 36));

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Good Food Better Days");
        title.setFont(AppTheme.fontHero());
        title.setForeground(AppTheme.BUTTERCREAM);

        JLabel subtitle = new JLabel("Fresh meals, happy moments. Delivered to your doorstep.");
        subtitle.setFont(AppTheme.fontBody());
        subtitle.setForeground(new Color(0xDE, 0xD0, 0xC0));

        content.add(title);
        content.add(Box.createVerticalStrut(8));
        content.add(subtitle);
        content.add(Box.createVerticalStrut(16));

        actionBtn = new SecondaryButton("Explore Menu");
        actionBtn.setIcon(Icons.search(14, Color.WHITE));
        if (onAction != null) {
            actionBtn.addActionListener(e -> onAction.run());
        }

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        btnRow.setOpaque(false);
        btnRow.add(actionBtn);
        content.add(btnRow);

        add(content, BorderLayout.CENTER);
    }

    public void setButtonText(String text) {
        actionBtn.setText(text);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        AppTheme.enableAntialiasing(g2);

        int w = getWidth();
        int h = getHeight();
        int r = 20;

        // Clip to rounded rectangle
        Shape clip = new java.awt.geom.RoundRectangle2D.Double(0, 0, w, h, r, r);
        g2.setClip(clip);

        // Espresso background
        g2.setColor(AppTheme.ESPRESSO);
        g2.fillRect(0, 0, w, h);

        // Decorative Serenity Blue organic blob on the right side
        g2.setColor(new Color(0x8F, 0xA7, 0xC8, 55));
        Path2D.Double blob = new Path2D.Double();
        blob.moveTo(w * 0.65, 0);
        blob.curveTo(w * 0.70, h * 0.4, w * 0.55, h * 0.8, w * 0.8, h);
        blob.lineTo(w, h);
        blob.lineTo(w, 0);
        blob.closePath();
        g2.fill(blob);

        // Inner glowing curve
        g2.setColor(new Color(0xF3, 0xE3, 0xC0, 30));
        Path2D.Double blob2 = new Path2D.Double();
        blob2.moveTo(w * 0.78, 0);
        blob2.curveTo(w * 0.82, h * 0.5, w * 0.70, h * 0.9, w * 0.92, h);
        blob2.lineTo(w, h);
        blob2.lineTo(w, 0);
        blob2.closePath();
        g2.fill(blob2);

        g2.dispose();
        super.paintComponent(g);
    }
}
