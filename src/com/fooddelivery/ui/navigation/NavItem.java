package com.fooddelivery.ui.navigation;

import com.fooddelivery.ui.AppTheme;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * NavItem: Individual navigation item for the 220px espresso sidebar.
 */
public class NavItem extends JPanel {

    private final String id;
    private final String title;
    private final Icon iconActive;
    private final Icon iconInactive;
    private boolean active = false;
    private boolean hovered = false;
    private final JLabel label;

    public NavItem(String id, String title, Icon iconActive, Icon iconInactive, Consumer<String> onSelect) {
        super(new BorderLayout(12, 0));
        this.id = id;
        this.title = title;
        this.iconActive = iconActive;
        this.iconInactive = iconInactive;

        setOpaque(false);
        setBorder(new EmptyBorder(10, 16, 10, 16));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setPreferredSize(new Dimension(AppTheme.SIDEBAR_WIDTH - 24, 44));
        setMaximumSize(new Dimension(AppTheme.SIDEBAR_WIDTH - 24, 44));

        label = new JLabel(title);
        label.setFont(AppTheme.fontBodyBold());
        label.setIcon(iconInactive);
        label.setForeground(AppTheme.BUTTERCREAM);

        add(label, BorderLayout.CENTER);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!active) {
                    hovered = true;
                    repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hovered = false;
                repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (onSelect != null) {
                    onSelect.accept(id);
                }
            }
        });
    }

    public String getId() {
        return id;
    }

    public void setActive(boolean a) {
        this.active = a;
        if (active) {
            label.setForeground(AppTheme.ESPRESSO);
            label.setIcon(iconActive);
        } else {
            label.setForeground(AppTheme.BUTTERCREAM);
            label.setIcon(iconInactive);
        }
        repaint();
    }

    public boolean isActive() {
        return active;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        AppTheme.enableAntialiasing(g2);

        int w = getWidth();
        int h = getHeight();
        int r = 12;

        if (active) {
            g2.setColor(AppTheme.BUTTERCREAM);
            g2.fillRoundRect(0, 0, w, h, r, r);
        } else if (hovered) {
            g2.setColor(new Color(0x4E, 0x30, 0x20));
            g2.fillRoundRect(0, 0, w, h, r, r);
        }

        g2.dispose();
        super.paintComponent(g);
    }
}
