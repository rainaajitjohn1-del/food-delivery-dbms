package com.fooddelivery.ui.views;

import com.fooddelivery.model.Restaurant;
import com.fooddelivery.model.User;
import com.fooddelivery.service.FoodDeliveryService;
import com.fooddelivery.ui.AppTheme;
import com.fooddelivery.ui.Icons;
import com.fooddelivery.ui.components.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * CustomerHomeView: Restaurants screen with top hero banner, search bar,
 * category chips strip, and responsive restaurant cards grid.
 */
public class CustomerHomeView extends JPanel {

    private final FoodDeliveryService api;
    private final User currentUser;
    private final Consumer<Restaurant> onSelectRestaurant;

    private final JPanel cardsContainer;
    private final StatePanel statePanel;
    private final SearchField searchField;
    private final List<Restaurant> allRestaurants = new ArrayList<>();
    private String selectedCategory = "All";

    public CustomerHomeView(FoodDeliveryService api, User currentUser, Consumer<Restaurant> onSelectRestaurant) {
        super(new BorderLayout());
        this.api = api;
        this.currentUser = currentUser;
        this.onSelectRestaurant = onSelectRestaurant;

        setBackground(AppTheme.WARM_OFF_WHITE);

        // Content wrapped in scroll pane
        JPanel scrollContent = new JPanel();
        scrollContent.setOpaque(false);
        scrollContent.setLayout(new BoxLayout(scrollContent, BoxLayout.Y_AXIS));
        scrollContent.setBorder(new EmptyBorder(24, 32, 32, 32));

        // 1. Top Search & Profile Bar
        JPanel topBar = new JPanel(new BorderLayout(16, 0));
        topBar.setOpaque(false);
        topBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        searchField = new SearchField("Search restaurants, dishes, cuisines...");
        searchField.setPreferredSize(new Dimension(450, 44));
        searchField.addActionListener(e -> filterRestaurants());
        topBar.add(searchField, BorderLayout.WEST);

        JPanel userChip = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        userChip.setOpaque(false);
        JLabel welcomeMsg = new JLabel("Hi, " + currentUser.firstName() + "!");
        welcomeMsg.setFont(AppTheme.fontBodyBold());
        welcomeMsg.setForeground(AppTheme.ESPRESSO);
        JLabel userAvatar = new JLabel(currentUser.firstName().substring(0, 1), SwingConstants.CENTER) {
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
        userAvatar.setPreferredSize(new Dimension(34, 34));
        userAvatar.setFont(AppTheme.fontBodyBold());
        userAvatar.setForeground(AppTheme.ESPRESSO);

        userChip.add(welcomeMsg);
        userChip.add(userAvatar);
        topBar.add(userChip, BorderLayout.EAST);

        scrollContent.add(topBar);
        scrollContent.add(Box.createVerticalStrut(20));

        // 2. Wide Hero Banner
        HeroBanner banner = new HeroBanner(() -> {
            if (!allRestaurants.isEmpty()) {
                onSelectRestaurant.accept(allRestaurants.get(0));
            }
        });
        banner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));
        scrollContent.add(banner);
        scrollContent.add(Box.createVerticalStrut(24));

        // 3. Category Filter Chips Strip
        JPanel chipsStrip = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        chipsStrip.setOpaque(false);
        chipsStrip.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        String[] categories = {"All", "Indian", "Chinese", "Biryani", "Fast Food", "Dessert"};
        for (String cat : categories) {
            chipsStrip.add(createCategoryChip(cat));
        }
        scrollContent.add(chipsStrip);
        scrollContent.add(Box.createVerticalStrut(24));

        // 4. Section Title Row
        JPanel sectionRow = new JPanel(new BorderLayout());
        sectionRow.setOpaque(false);
        sectionRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        JLabel sectionTitle = new JLabel("Popular Near You");
        sectionTitle.setFont(AppTheme.fontSection());
        sectionTitle.setForeground(AppTheme.ESPRESSO);
        sectionRow.add(sectionTitle, BorderLayout.WEST);

        scrollContent.add(sectionRow);
        scrollContent.add(Box.createVerticalStrut(16));

        // 5. Cards Container or State Panel
        cardsContainer = new JPanel(new GridLayout(0, 3, 20, 20));
        cardsContainer.setOpaque(false);

        statePanel = new StatePanel();
        statePanel.setVisible(false);

        scrollContent.add(statePanel);
        scrollContent.add(cardsContainer);

        JScrollPane sp = new JScrollPane(scrollContent);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.setBorder(null);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        add(sp, BorderLayout.CENTER);

        loadRestaurants();
    }

    private JButton createCategoryChip(String category) {
        JButton btn = new JButton(category);
        btn.setFont(AppTheme.fontSmallBold());
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        updateChipStyle(btn, category.equals(selectedCategory));

        btn.addActionListener(e -> {
            selectedCategory = category;
            // update all chips in parent
            Container parent = btn.getParent();
            if (parent != null) {
                for (Component c : parent.getComponents()) {
                    if (c instanceof JButton b) {
                        updateChipStyle(b, b.getText().equals(selectedCategory));
                    }
                }
            }
            filterRestaurants();
        });
        return btn;
    }

    private void updateChipStyle(JButton btn, boolean isSelected) {
        if (isSelected) {
            btn.setBackground(AppTheme.ESPRESSO);
            btn.setForeground(AppTheme.BUTTERCREAM);
            btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(AppTheme.ESPRESSO, 1),
                    BorderFactory.createEmptyBorder(6, 16, 6, 16)
            ));
        } else {
            btn.setBackground(AppTheme.WARM_OFF_WHITE);
            btn.setForeground(AppTheme.TEXT_PRIMARY);
            btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(AppTheme.BORDER_LIGHT, 1),
                    BorderFactory.createEmptyBorder(6, 16, 6, 16)
            ));
        }
    }

    public void loadRestaurants() {
        cardsContainer.setVisible(false);
        statePanel.setVisible(true);
        statePanel.showLoading("Loading restaurants...");

        SwingWorker<List<Restaurant>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Restaurant> doInBackground() throws Exception {
                return api.getRestaurants();
            }

            @Override
            protected void done() {
                try {
                    allRestaurants.clear();
                    allRestaurants.addAll(get());
                    statePanel.setVisible(false);
                    cardsContainer.setVisible(true);
                    filterRestaurants();
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    statePanel.showError(cause.getMessage(), CustomerHomeView.this::loadRestaurants);
                }
            }
        };
        worker.execute();
    }

    private void filterRestaurants() {
        String query = searchField.getText().trim().toLowerCase();
        List<Restaurant> filtered = new ArrayList<>();

        for (Restaurant r : allRestaurants) {
            boolean matchesName = query.isEmpty() || r.name().toLowerCase().contains(query);
            filtered.add(r);
        }

        renderGrid(filtered);
    }

    private void renderGrid(List<Restaurant> list) {
        cardsContainer.removeAll();

        if (list.isEmpty()) {
            statePanel.setVisible(true);
            statePanel.showEmpty("No restaurants found", "Try searching for a different name or category.");
            cardsContainer.setVisible(false);
            return;
        }

        statePanel.setVisible(false);
        cardsContainer.setVisible(true);

        for (Restaurant r : list) {
            cardsContainer.add(createRestaurantCard(r));
        }

        cardsContainer.revalidate();
        cardsContainer.repaint();
    }

    private JPanel createRestaurantCard(Restaurant r) {
        RoundCardPanel card = new RoundCardPanel(new BorderLayout(0, 12));
        card.setBorder(new EmptyBorder(16, 16, 16, 16));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Thumbnail placeholder banner (Cream background with Initial glyph)
        JPanel thumb = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                AppTheme.enableAntialiasing(g2);
                g2.setColor(AppTheme.BUTTERCREAM);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        thumb.setPreferredSize(new Dimension(200, 120));
        thumb.setOpaque(false);

        JLabel glyph = new JLabel(r.name().substring(0, 1));
        glyph.setFont(new Font(AppTheme.fontHero().getName(), Font.BOLD, 42));
        glyph.setForeground(AppTheme.ESPRESSO);
        thumb.add(glyph);

        card.add(thumb, BorderLayout.NORTH);

        // Details
        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

        JLabel nameLbl = new JLabel(r.name());
        nameLbl.setFont(AppTheme.fontCardTitle());
        nameLbl.setForeground(AppTheme.TEXT_PRIMARY);

        // Pills row: time ("30 min") + rating ("4.6 ★")
        JPanel pillsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        pillsRow.setOpaque(false);
        pillsRow.add(PillBadge.time("30 min"));

        String ratingStr = r.avgRating() != null ? String.format("%.1f ★", r.avgRating()) : "New";
        pillsRow.add(PillBadge.rating(ratingStr));

        if (r.avgRating() != null && r.avgRating() >= 4.5) {
            pillsRow.add(PillBadge.bestseller());
        }

        info.add(nameLbl);
        info.add(Box.createVerticalStrut(8));
        info.add(pillsRow);

        card.add(info, BorderLayout.CENTER);

        // Action button
        SecondaryButton viewBtn = new SecondaryButton("View Menu");
        viewBtn.addActionListener(e -> {
            if (onSelectRestaurant != null) onSelectRestaurant.accept(r);
        });
        card.add(viewBtn, BorderLayout.SOUTH);

        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (onSelectRestaurant != null) onSelectRestaurant.accept(r);
            }
        });

        return card;
    }
}
