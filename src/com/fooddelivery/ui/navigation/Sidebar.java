package com.fooddelivery.ui.navigation;

import com.fooddelivery.model.User;
import com.fooddelivery.ui.AppTheme;
import com.fooddelivery.ui.Icons;
import com.fooddelivery.ui.components.PillBadge;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Fixed 220px Left Espresso Sidebar Navigation (#3B2418).
 * Houses application brand, role-based nav items with outline icons,
 * current user profile card, and logout action.
 */
public class Sidebar extends JPanel {

    private final Consumer<String> onNavigate;
    private final Runnable onLogout;
    private final List<NavItem> navItems = new ArrayList<>();
    private final JPanel itemsPanel;
    private final JPanel userBottomPanel;
    private User currentUser;

    public Sidebar(Consumer<String> onNavigate, Runnable onLogout) {
        super(new BorderLayout());
        this.onNavigate = onNavigate;
        this.onLogout = onLogout;

        setPreferredSize(new Dimension(AppTheme.SIDEBAR_WIDTH, 0));
        setMinimumSize(new Dimension(AppTheme.SIDEBAR_WIDTH, 0));
        setMaximumSize(new Dimension(AppTheme.SIDEBAR_WIDTH, Integer.MAX_VALUE));
        setBackground(AppTheme.ESPRESSO);

        // Brand Header (Top)
        JPanel brandPanel = new JPanel(new BorderLayout(10, 0));
        brandPanel.setOpaque(false);
        brandPanel.setBorder(new EmptyBorder(28, 20, 24, 20));

        JLabel logoIcon = new JLabel(Icons.heart(26, AppTheme.BUTTERCREAM));
        JPanel brandText = new JPanel();
        brandText.setOpaque(false);
        brandText.setLayout(new BoxLayout(brandText, BoxLayout.Y_AXIS));

        JLabel brandTitle = new JLabel("Good Food");
        brandTitle.setFont(AppTheme.fontCardTitle());
        brandTitle.setForeground(AppTheme.BUTTERCREAM);

        JLabel brandSub = new JLabel("DESKTOP");
        brandSub.setFont(AppTheme.fontSmallBold());
        brandSub.setForeground(AppTheme.SERENITY_BLUE);

        brandText.add(brandTitle);
        brandText.add(brandSub);

        brandPanel.add(logoIcon, BorderLayout.WEST);
        brandPanel.add(brandText, BorderLayout.CENTER);
        add(brandPanel, BorderLayout.NORTH);

        // Center Nav Items
        itemsPanel = new JPanel();
        itemsPanel.setOpaque(false);
        itemsPanel.setLayout(new BoxLayout(itemsPanel, BoxLayout.Y_AXIS));
        itemsPanel.setBorder(new EmptyBorder(8, 12, 8, 12));

        JScrollPane scroll = new JScrollPane(itemsPanel);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        add(scroll, BorderLayout.CENTER);

        // Bottom User Profile & Logout
        userBottomPanel = new JPanel();
        userBottomPanel.setOpaque(false);
        userBottomPanel.setLayout(new BoxLayout(userBottomPanel, BoxLayout.Y_AXIS));
        userBottomPanel.setBorder(new EmptyBorder(12, 14, 20, 14));
        add(userBottomPanel, BorderLayout.SOUTH);
    }

    public void setUser(User user) {
        this.currentUser = user;
        rebuildNav();
        rebuildUserBottom();
    }

    public void selectItem(String id) {
        for (NavItem item : navItems) {
            item.setActive(item.getId().equals(id));
        }
    }

    private void rebuildNav() {
        itemsPanel.removeAll();
        navItems.clear();

        if (currentUser == null) {
            itemsPanel.revalidate();
            itemsPanel.repaint();
            return;
        }

        String role = currentUser.role();
        if ("CUSTOMER".equalsIgnoreCase(role)) {
            addNav("restaurants", "Restaurants", Icons.home(18, AppTheme.ESPRESSO), Icons.home(18, AppTheme.BUTTERCREAM));
            addNav("cart", "Menu & Cart", Icons.cart(18, AppTheme.ESPRESSO), Icons.cart(18, AppTheme.BUTTERCREAM));
            addNav("orders", "My Orders", Icons.orders(18, AppTheme.ESPRESSO), Icons.orders(18, AppTheme.BUTTERCREAM));
            addNav("profile", "Profile", Icons.user(18, AppTheme.ESPRESSO), Icons.user(18, AppTheme.BUTTERCREAM));
        } else if ("MANAGER".equalsIgnoreCase(role)) {
            addNav("manager_orders", "Order Queue", Icons.orders(18, AppTheme.ESPRESSO), Icons.orders(18, AppTheme.BUTTERCREAM));
            addNav("manager_menu", "Menu Manager", Icons.card(18, AppTheme.ESPRESSO), Icons.card(18, AppTheme.BUTTERCREAM));
            addNav("reports", "Reports", Icons.reports(18, AppTheme.ESPRESSO), Icons.reports(18, AppTheme.BUTTERCREAM));
            addNav("profile", "Profile", Icons.user(18, AppTheme.ESPRESSO), Icons.user(18, AppTheme.BUTTERCREAM));
        } else if ("AGENT".equalsIgnoreCase(role)) {
            addNav("agent_deliveries", "My Deliveries", Icons.delivery(18, AppTheme.ESPRESSO), Icons.delivery(18, AppTheme.BUTTERCREAM));
            addNav("profile", "Profile", Icons.user(18, AppTheme.ESPRESSO), Icons.user(18, AppTheme.BUTTERCREAM));
        }

        itemsPanel.revalidate();
        itemsPanel.repaint();
    }

    private void addNav(String id, String label, Icon activeIcon, Icon inactiveIcon) {
        NavItem item = new NavItem(id, label, activeIcon, inactiveIcon, selectedId -> {
            selectItem(selectedId);
            if (onNavigate != null) onNavigate.accept(selectedId);
        });
        navItems.add(item);
        itemsPanel.add(item);
        itemsPanel.add(Box.createVerticalStrut(6));
    }

    private void rebuildUserBottom() {
        userBottomPanel.removeAll();
        if (currentUser == null) {
            userBottomPanel.revalidate();
            userBottomPanel.repaint();
            return;
        }

        // Divider
        JSeparator sep = new JSeparator(SwingConstants.HORIZONTAL);
        sep.setForeground(new Color(0xF3, 0xE3, 0xC0, 40));
        userBottomPanel.add(sep);
        userBottomPanel.add(Box.createVerticalStrut(12));

        // User profile card
        JPanel card = new JPanel(new BorderLayout(10, 0));
        card.setOpaque(false);

        String initials = getInitials(currentUser.fullName());
        JLabel avatar = new JLabel(initials, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                AppTheme.enableAntialiasing(g2);
                g2.setColor(AppTheme.BUTTERCREAM);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        avatar.setPreferredSize(new Dimension(36, 36));
        avatar.setFont(AppTheme.fontBodyBold());
        avatar.setForeground(AppTheme.ESPRESSO);

        JPanel details = new JPanel();
        details.setOpaque(false);
        details.setLayout(new BoxLayout(details, BoxLayout.Y_AXIS));

        JLabel nameLabel = new JLabel(currentUser.fullName());
        nameLabel.setFont(AppTheme.fontBodyBold());
        nameLabel.setForeground(AppTheme.BUTTERCREAM);

        JLabel roleLabel = new JLabel(currentUser.role());
        roleLabel.setFont(AppTheme.fontSmallBold());
        roleLabel.setForeground(AppTheme.SERENITY_BLUE);

        details.add(nameLabel);
        details.add(Box.createVerticalStrut(2));
        details.add(roleLabel);

        card.add(avatar, BorderLayout.WEST);
        card.add(details, BorderLayout.CENTER);
        userBottomPanel.add(card);
        userBottomPanel.add(Box.createVerticalStrut(12));

        // Logout Row
        JPanel logoutRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        logoutRow.setOpaque(false);
        logoutRow.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel logoutIcon = new JLabel(Icons.logout(16, AppTheme.SERENITY_BLUE));
        JLabel logoutLabel = new JLabel("Log out");
        logoutLabel.setFont(AppTheme.fontBody());
        logoutLabel.setForeground(AppTheme.SERENITY_BLUE);

        logoutRow.add(logoutIcon);
        logoutRow.add(logoutLabel);

        logoutRow.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (onLogout != null) onLogout.run();
            }
        });

        userBottomPanel.add(logoutRow);
        userBottomPanel.revalidate();
        userBottomPanel.repaint();
    }

    private String getInitials(String name) {
        if (name == null || name.isBlank()) return "U";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        return ("" + parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
    }
}
