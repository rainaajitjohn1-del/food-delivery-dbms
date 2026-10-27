package com.fooddelivery.ui;

import java.awt.*;
import java.awt.geom.*;
import javax.swing.Icon;

/**
 * Pure Java2D vector icon renderer for high-DPI desktop rendering.
 * Provides custom scalable icons without external asset dependencies.
 */
public final class Icons {

    private Icons() {}

    public static Icon home(int size, Color color) {
        return createIcon(size, color, (g2, w, h) -> {
            Path2D.Double p = new Path2D.Double();
            p.moveTo(w * 0.5, h * 0.15);
            p.lineTo(w * 0.85, h * 0.45);
            p.lineTo(w * 0.75, h * 0.45);
            p.lineTo(w * 0.75, h * 0.85);
            p.lineTo(w * 0.25, h * 0.85);
            p.lineTo(w * 0.25, h * 0.45);
            p.lineTo(w * 0.15, h * 0.45);
            p.closePath();
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(p);
            // Door
            g2.draw(new Rectangle2D.Double(w * 0.4, h * 0.58, w * 0.2, h * 0.27));
        });
    }

    public static Icon search(int size, Color color) {
        return createIcon(size, color, (g2, w, h) -> {
            g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            double r = w * 0.32;
            g2.draw(new Ellipse2D.Double(w * 0.18, h * 0.18, r * 2, r * 2));
            g2.draw(new Line2D.Double(w * 0.65, h * 0.65, w * 0.85, h * 0.85));
        });
    }

    public static Icon heart(int size, Color color) {
        return createIcon(size, color, (g2, w, h) -> {
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            Path2D.Double p = new Path2D.Double();
            p.moveTo(w * 0.5, h * 0.78);
            p.curveTo(w * 0.15, h * 0.55, w * 0.12, h * 0.25, w * 0.32, h * 0.22);
            p.curveTo(w * 0.44, h * 0.20, w * 0.5, h * 0.32, w * 0.5, h * 0.32);
            p.curveTo(w * 0.5, h * 0.32, w * 0.56, h * 0.20, w * 0.68, h * 0.22);
            p.curveTo(w * 0.88, h * 0.25, w * 0.85, h * 0.55, w * 0.5, h * 0.78);
            g2.draw(p);
        });
    }

    public static Icon cart(int size, Color color) {
        return createIcon(size, color, (g2, w, h) -> {
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            Path2D.Double basket = new Path2D.Double();
            basket.moveTo(w * 0.15, h * 0.25);
            basket.lineTo(w * 0.28, h * 0.25);
            basket.lineTo(w * 0.38, h * 0.65);
            basket.lineTo(w * 0.80, h * 0.65);
            basket.lineTo(w * 0.88, h * 0.35);
            basket.lineTo(w * 0.30, h * 0.35);
            g2.draw(basket);
            // wheels
            double wr = w * 0.07;
            g2.fill(new Ellipse2D.Double(w * 0.42 - wr, h * 0.78 - wr, wr * 2, wr * 2));
            g2.fill(new Ellipse2D.Double(w * 0.75 - wr, h * 0.78 - wr, wr * 2, wr * 2));
        });
    }

    public static Icon user(int size, Color color) {
        return createIcon(size, color, (g2, w, h) -> {
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            // head
            double hr = w * 0.22;
            g2.draw(new Ellipse2D.Double(w * 0.5 - hr, h * 0.16, hr * 2, hr * 2));
            // body
            Path2D.Double body = new Path2D.Double();
            body.moveTo(w * 0.20, h * 0.82);
            body.curveTo(w * 0.22, h * 0.60, w * 0.78, h * 0.60, w * 0.80, h * 0.82);
            g2.draw(body);
        });
    }

    public static Icon plus(int size, Color color) {
        return createIcon(size, color, (g2, w, h) -> {
            g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(new Line2D.Double(w * 0.5, h * 0.22, w * 0.5, h * 0.78));
            g2.draw(new Line2D.Double(w * 0.22, h * 0.5, w * 0.78, h * 0.5));
        });
    }

    public static Icon minus(int size, Color color) {
        return createIcon(size, color, (g2, w, h) -> {
            g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(new Line2D.Double(w * 0.24, h * 0.5, w * 0.76, h * 0.5));
        });
    }

    public static Icon orders(int size, Color color) {
        return createIcon(size, color, (g2, w, h) -> {
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            // clipboard
            g2.draw(new RoundRectangle2D.Double(w * 0.22, h * 0.18, w * 0.56, h * 0.68, 6, 6));
            // clip on top
            g2.draw(new RoundRectangle2D.Double(w * 0.38, h * 0.10, w * 0.24, h * 0.12, 3, 3));
            // lines
            g2.draw(new Line2D.Double(w * 0.35, h * 0.40, w * 0.65, h * 0.40));
            g2.draw(new Line2D.Double(w * 0.35, h * 0.54, w * 0.65, h * 0.54));
            g2.draw(new Line2D.Double(w * 0.35, h * 0.68, w * 0.55, h * 0.68));
        });
    }

    public static Icon location(int size, Color color) {
        return createIcon(size, color, (g2, w, h) -> {
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            Path2D.Double pin = new Path2D.Double();
            pin.moveTo(w * 0.5, h * 0.86);
            pin.curveTo(w * 0.20, h * 0.55, w * 0.20, h * 0.20, w * 0.5, h * 0.20);
            pin.curveTo(w * 0.80, h * 0.20, w * 0.80, h * 0.55, w * 0.5, h * 0.86);
            pin.closePath();
            g2.draw(pin);
            double ir = w * 0.10;
            g2.draw(new Ellipse2D.Double(w * 0.5 - ir, h * 0.42 - ir, ir * 2, ir * 2));
        });
    }

    public static Icon card(int size, Color color) {
        return createIcon(size, color, (g2, w, h) -> {
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(new RoundRectangle2D.Double(w * 0.15, h * 0.24, w * 0.70, h * 0.52, 6, 6));
            g2.draw(new Line2D.Double(w * 0.15, h * 0.42, w * 0.85, h * 0.42));
            g2.fillRect((int)(w * 0.25), (int)(h * 0.55), (int)(w * 0.16), (int)(h * 0.10));
        });
    }

    public static Icon help(int size, Color color) {
        return createIcon(size, color, (g2, w, h) -> {
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(new Ellipse2D.Double(w * 0.15, h * 0.15, w * 0.70, h * 0.70));
            // question mark
            Path2D.Double q = new Path2D.Double();
            q.moveTo(w * 0.38, h * 0.38);
            q.curveTo(w * 0.38, h * 0.28, w * 0.62, h * 0.28, w * 0.62, h * 0.40);
            q.curveTo(w * 0.62, h * 0.50, w * 0.50, h * 0.52, w * 0.50, h * 0.60);
            g2.draw(q);
            g2.fill(new Ellipse2D.Double(w * 0.47, h * 0.68, w * 0.06, w * 0.06));
        });
    }

    public static Icon settings(int size, Color color) {
        return createIcon(size, color, (g2, w, h) -> {
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(new Ellipse2D.Double(w * 0.35, h * 0.35, w * 0.30, h * 0.30));
            int spokes = 6;
            for (int i = 0; i < spokes; i++) {
                double angle = i * (Math.PI / 3);
                double x1 = w * 0.5 + Math.cos(angle) * w * 0.25;
                double y1 = h * 0.5 + Math.sin(angle) * h * 0.25;
                double x2 = w * 0.5 + Math.cos(angle) * w * 0.42;
                double y2 = h * 0.5 + Math.sin(angle) * h * 0.42;
                g2.draw(new Line2D.Double(x1, y1, x2, y2));
            }
        });
    }

    public static Icon delivery(int size, Color color) {
        return createIcon(size, color, (g2, w, h) -> {
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            // Truck body
            g2.draw(new RoundRectangle2D.Double(w * 0.12, h * 0.30, w * 0.50, h * 0.36, 4, 4));
            // Cabin
            Path2D.Double cab = new Path2D.Double();
            cab.moveTo(w * 0.62, h * 0.40);
            cab.lineTo(w * 0.78, h * 0.40);
            cab.lineTo(w * 0.86, h * 0.54);
            cab.lineTo(w * 0.86, h * 0.66);
            cab.lineTo(w * 0.62, h * 0.66);
            cab.closePath();
            g2.draw(cab);
            // Wheels
            double wr = w * 0.07;
            g2.draw(new Ellipse2D.Double(w * 0.26 - wr, h * 0.72 - wr, wr * 2, wr * 2));
            g2.draw(new Ellipse2D.Double(w * 0.72 - wr, h * 0.72 - wr, wr * 2, wr * 2));
        });
    }

    public static Icon reports(int size, Color color) {
        return createIcon(size, color, (g2, w, h) -> {
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(new Line2D.Double(w * 0.18, h * 0.82, w * 0.82, h * 0.82));
            g2.draw(new Line2D.Double(w * 0.18, h * 0.18, w * 0.18, h * 0.82));
            // Bars
            g2.fillRect((int)(w * 0.28), (int)(h * 0.50), (int)(w * 0.12), (int)(h * 0.32));
            g2.fillRect((int)(w * 0.46), (int)(h * 0.34), (int)(w * 0.12), (int)(h * 0.48));
            g2.fillRect((int)(w * 0.64), (int)(h * 0.22), (int)(w * 0.12), (int)(h * 0.60));
        });
    }

    public static Icon star(int size, Color color, boolean filled) {
        return createIcon(size, color, (g2, w, h) -> {
            Path2D.Double star = new Path2D.Double();
            double cx = w * 0.5;
            double cy = h * 0.5;
            double rOuter = w * 0.42;
            double rInner = w * 0.20;
            for (int i = 0; i < 10; i++) {
                double r = (i % 2 == 0) ? rOuter : rInner;
                double angle = -Math.PI / 2 + i * (Math.PI / 5);
                double x = cx + Math.cos(angle) * r;
                double y = cy + Math.sin(angle) * r;
                if (i == 0) star.moveTo(x, y);
                else star.lineTo(x, y);
            }
            star.closePath();
            if (filled) {
                g2.fill(star);
            } else {
                g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.draw(star);
            }
        });
    }

    public static Icon check(int size, Color color) {
        return createIcon(size, color, (g2, w, h) -> {
            g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            Path2D.Double p = new Path2D.Double();
            p.moveTo(w * 0.20, h * 0.52);
            p.lineTo(w * 0.42, h * 0.74);
            p.lineTo(w * 0.80, h * 0.26);
            g2.draw(p);
        });
    }

    public static Icon back(int size, Color color) {
        return createIcon(size, color, (g2, w, h) -> {
            g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(new Line2D.Double(w * 0.75, h * 0.50, w * 0.25, h * 0.50));
            Path2D.Double arr = new Path2D.Double();
            arr.moveTo(w * 0.45, h * 0.28);
            arr.lineTo(w * 0.23, h * 0.50);
            arr.lineTo(w * 0.45, h * 0.72);
            g2.draw(arr);
        });
    }

    public static Icon logout(int size, Color color) {
        return createIcon(size, color, (g2, w, h) -> {
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            // Door frame
            Path2D.Double door = new Path2D.Double();
            door.moveTo(w * 0.50, h * 0.20);
            door.lineTo(w * 0.25, h * 0.20);
            door.lineTo(w * 0.25, h * 0.80);
            door.lineTo(w * 0.50, h * 0.80);
            g2.draw(door);
            // Arrow out
            g2.draw(new Line2D.Double(w * 0.40, h * 0.50, w * 0.80, h * 0.50));
            g2.draw(new Line2D.Double(w * 0.65, h * 0.35, w * 0.80, h * 0.50));
            g2.draw(new Line2D.Double(w * 0.65, h * 0.65, w * 0.80, h * 0.50));
        });
    }

    public static Icon refresh(int size, Color color) {
        return createIcon(size, color, (g2, w, h) -> {
            g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(new Arc2D.Double(w * 0.18, h * 0.18, w * 0.64, h * 0.64, 45, 270, Arc2D.OPEN));
            Path2D.Double arr = new Path2D.Double();
            arr.moveTo(w * 0.65, h * 0.12);
            arr.lineTo(w * 0.82, h * 0.25);
            arr.lineTo(w * 0.65, h * 0.38);
            g2.draw(arr);
        });
    }

    @FunctionalInterface
    public interface IconPainter {
        void paint(Graphics2D g2, double w, double h);
    }

    private static Icon createIcon(int size, Color color, IconPainter painter) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                AppTheme.enableAntialiasing(g2);
                g2.translate(x, y);
                g2.setColor(color != null ? color : c.getForeground());
                painter.paint(g2, size, size);
                g2.dispose();
            }

            @Override
            public int getIconWidth() { return size; }

            @Override
            public int getIconHeight() { return size; }
        };
    }
}
