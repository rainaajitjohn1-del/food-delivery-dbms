package com.fooddelivery.ui.views;

import com.fooddelivery.model.MenuItem;
import com.fooddelivery.model.Restaurant;
import com.fooddelivery.model.User;
import com.fooddelivery.service.FoodDeliveryService;
import com.fooddelivery.ui.AppTheme;
import com.fooddelivery.ui.Icons;
import com.fooddelivery.ui.components.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.BiConsumer;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * MenuCartSplitView: 2-column desktop layout.
 * Left: Restaurant header, Tabs (Menu, Reviews, About), food item list with Serenity Blue "+" buttons.
 * Right: Sticky ~340px Cart panel with quantity steppers, coupon field, live totals, and Checkout button.
 */
public class MenuCartSplitView extends JPanel {

    private final FoodDeliveryService api;
    private final User currentUser;
    private final Restaurant restaurant;
    private final BiConsumer<Restaurant, Map<Integer, Integer>> onProceedToCheckout;
    private final Runnable onBackToRestaurants;

    // Cart state: itemId -> quantity
    private final Map<Integer, Integer> cart = new LinkedHashMap<>();
    private final Map<Integer, MenuItem> itemCache = new HashMap<>();
    private String couponCode = "";
    private double calculatedTotal = 0.0;
    private double subtotal = 0.0;

    // UI elements
    private final JPanel menuItemsContainer;
    private final StatePanel statePanel;
    private final JPanel cartItemsList;
    private final JLabel subtotalLabel;
    private final JLabel discountLabel;
    private final JLabel totalLabel;
    private final PrimaryButton checkoutBtn;
    private final RoundedTextField couponInput;
    private final JLabel emptyCartNotice;

    public MenuCartSplitView(FoodDeliveryService api, User currentUser, Restaurant restaurant,
                             BiConsumer<Restaurant, Map<Integer, Integer>> onProceedToCheckout,
                             Runnable onBackToRestaurants) {
        super(new BorderLayout());
        this.api = api;
        this.currentUser = currentUser;
        this.restaurant = restaurant;
        this.onProceedToCheckout = onProceedToCheckout;
        this.onBackToRestaurants = onBackToRestaurants;

        setBackground(AppTheme.WARM_OFF_WHITE);

        // Top Header Row with Back button & Restaurant Info
        JPanel topHeader = new JPanel(new BorderLayout(16, 0));
        topHeader.setOpaque(false);
        topHeader.setBorder(new EmptyBorder(20, 28, 12, 28));

        PillChipButton backBtn = new PillChipButton(" All Restaurants", Icons.back(16, AppTheme.ESPRESSO));
        backBtn.addActionListener(e -> onBackToRestaurants.run());

        JPanel titleBlock = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        titleBlock.setOpaque(false);

        JLabel restName = new JLabel(restaurant.name());
        restName.setFont(AppTheme.fontSection());
        restName.setForeground(AppTheme.ESPRESSO);

        String ratingStr = restaurant.avgRating() != null ? String.format("%.1f ★", restaurant.avgRating()) : "New";
        PillBadge ratingBadge = PillBadge.rating(ratingStr);

        titleBlock.add(restName);
        titleBlock.add(ratingBadge);

        topHeader.add(backBtn, BorderLayout.WEST);
        topHeader.add(titleBlock, BorderLayout.CENTER);

        add(topHeader, BorderLayout.NORTH);

        // Split Main View: Left Menu Pane + Right Sticky Cart Pane
        JPanel splitPane = new JPanel(new BorderLayout(24, 0));
        splitPane.setOpaque(false);
        splitPane.setBorder(new EmptyBorder(8, 28, 24, 28));

        // LEFT PANE: Menu Items
        JPanel leftPane = new JPanel(new BorderLayout());
        leftPane.setOpaque(false);

        // Tabs Header (Menu, Reviews, About)
        JPanel tabsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        tabsRow.setOpaque(false);
        tabsRow.setBorder(new EmptyBorder(0, 4, 12, 4));

        JLabel menuTab = new JLabel("Menu");
        menuTab.setFont(AppTheme.fontBodyBold());
        menuTab.setForeground(AppTheme.ESPRESSO);
        menuTab.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, AppTheme.ESPRESSO));

        JLabel reviewsTab = new JLabel("Reviews");
        reviewsTab.setFont(AppTheme.fontBody());
        reviewsTab.setForeground(AppTheme.TEXT_MUTED);

        JLabel aboutTab = new JLabel("About");
        aboutTab.setFont(AppTheme.fontBody());
        aboutTab.setForeground(AppTheme.TEXT_MUTED);

        tabsRow.add(menuTab);
        tabsRow.add(reviewsTab);
        tabsRow.add(aboutTab);
        leftPane.add(tabsRow, BorderLayout.NORTH);

        menuItemsContainer = new JPanel();
        menuItemsContainer.setOpaque(false);
        menuItemsContainer.setLayout(new BoxLayout(menuItemsContainer, BoxLayout.Y_AXIS));

        statePanel = new StatePanel();
        statePanel.setVisible(false);

        JPanel leftScrollContent = new JPanel(new BorderLayout());
        leftScrollContent.setOpaque(false);
        leftScrollContent.add(statePanel, BorderLayout.NORTH);
        leftScrollContent.add(menuItemsContainer, BorderLayout.CENTER);

        JScrollPane menuScroll = new JScrollPane(leftScrollContent);
        menuScroll.setOpaque(false);
        menuScroll.getViewport().setOpaque(false);
        menuScroll.setBorder(null);
        menuScroll.getVerticalScrollBar().setUnitIncrement(16);
        leftPane.add(menuScroll, BorderLayout.CENTER);

        splitPane.add(leftPane, BorderLayout.CENTER);

        // RIGHT PANE: Sticky Cart (320px width)
        RoundCardPanel cartPanel = new RoundCardPanel(new BorderLayout(0, 16));
        cartPanel.setPreferredSize(new Dimension(320, 0));
        cartPanel.setMinimumSize(new Dimension(300, 0));
        cartPanel.setBorder(new EmptyBorder(20, 18, 20, 18));

        // Cart Header
        JPanel cartTop = new JPanel(new BorderLayout());
        cartTop.setOpaque(false);
        JLabel cartTitle = new JLabel("Your Cart");
        cartTitle.setFont(AppTheme.fontCardTitle());
        cartTitle.setForeground(AppTheme.ESPRESSO);
        cartTitle.setIcon(Icons.cart(20, AppTheme.ESPRESSO));
        cartTop.add(cartTitle, BorderLayout.WEST);
        cartPanel.add(cartTop, BorderLayout.NORTH);

        // Cart Items Scroll
        cartItemsList = new JPanel();
        cartItemsList.setOpaque(false);
        cartItemsList.setLayout(new BoxLayout(cartItemsList, BoxLayout.Y_AXIS));

        emptyCartNotice = new JLabel("<html><center style='color:#8C7A6E;'><br><br>Your cart is empty.<br>Add delicious items from the menu!</center></html>", SwingConstants.CENTER);
        emptyCartNotice.setFont(AppTheme.fontBody());
        cartItemsList.add(emptyCartNotice);

        JScrollPane cartScroll = new JScrollPane(cartItemsList);
        cartScroll.setOpaque(false);
        cartScroll.getViewport().setOpaque(false);
        cartScroll.setBorder(null);
        cartPanel.add(cartScroll, BorderLayout.CENTER);

        // Cart Summary & Checkout Button
        JPanel cartBottom = new JPanel();
        cartBottom.setOpaque(false);
        cartBottom.setLayout(new BoxLayout(cartBottom, BoxLayout.Y_AXIS));

        // Coupon row
        JPanel couponRow = new JPanel(new BorderLayout(6, 0));
        couponRow.setOpaque(false);
        couponInput = new RoundedTextField("Enter coupon (e.g. FEST10)", 12);
        couponInput.setPreferredSize(new Dimension(170, 36));

        SecondaryButton applyCouponBtn = new SecondaryButton("Apply", 999);
        applyCouponBtn.addActionListener(e -> {
            couponCode = couponInput.getText().trim();
            updateTotals();
        });

        couponRow.add(couponInput, BorderLayout.CENTER);
        couponRow.add(applyCouponBtn, BorderLayout.EAST);
        cartBottom.add(couponRow);
        cartBottom.add(Box.createVerticalStrut(12));

        // Cost Breakdown Rows
        subtotalLabel = new JLabel("₹0.00", SwingConstants.RIGHT);
        subtotalLabel.setFont(AppTheme.fontBodyBold());
        discountLabel = new JLabel("-₹0.00", SwingConstants.RIGHT);
        discountLabel.setFont(AppTheme.fontBodyBold());
        discountLabel.setForeground(AppTheme.SUCCESS_GREEN);
        totalLabel = new JLabel("₹0.00", SwingConstants.RIGHT);
        totalLabel.setFont(AppTheme.fontPrice());
        totalLabel.setForeground(AppTheme.ESPRESSO);

        cartBottom.add(createSummaryRow("Subtotal", subtotalLabel));
        cartBottom.add(Box.createVerticalStrut(6));
        cartBottom.add(createSummaryRow("Delivery Fee", new JLabel("FREE", SwingConstants.RIGHT)));
        cartBottom.add(Box.createVerticalStrut(6));
        cartBottom.add(createSummaryRow("Coupon Discount", discountLabel));
        cartBottom.add(Box.createVerticalStrut(10));
        cartBottom.add(new JSeparator(SwingConstants.HORIZONTAL));
        cartBottom.add(Box.createVerticalStrut(10));
        cartBottom.add(createSummaryRow("Total", totalLabel));
        cartBottom.add(Box.createVerticalStrut(16));

        checkoutBtn = new PrimaryButton("Proceed to Checkout");
        checkoutBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        checkoutBtn.setEnabled(false);
        checkoutBtn.addActionListener(e -> {
            if (!cart.isEmpty() && onProceedToCheckout != null) {
                onProceedToCheckout.accept(restaurant, new LinkedHashMap<>(cart));
            }
        });
        cartBottom.add(checkoutBtn);

        cartPanel.add(cartBottom, BorderLayout.SOUTH);
        splitPane.add(cartPanel, BorderLayout.EAST);

        add(splitPane, BorderLayout.CENTER);

        loadMenu();
    }

    private JPanel createSummaryRow(String label, JComponent valueComp) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        JLabel lbl = new JLabel(label);
        lbl.setFont(AppTheme.fontBody());
        lbl.setForeground(AppTheme.TEXT_MUTED);
        row.add(lbl, BorderLayout.WEST);
        row.add(valueComp, BorderLayout.EAST);
        return row;
    }

    private void loadMenu() {
        menuItemsContainer.setVisible(false);
        statePanel.setVisible(true);
        statePanel.showLoading("Loading menu items...");

        SwingWorker<List<MenuItem>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<MenuItem> doInBackground() throws Exception {
                return api.getMenu(restaurant.restaurantId());
            }

            @Override
            protected void done() {
                try {
                    List<MenuItem> items = get();
                    itemCache.clear();
                    for (MenuItem m : items) {
                        itemCache.put(m.itemId(), m);
                    }
                    statePanel.setVisible(false);
                    renderMenu(items);
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    statePanel.showError(cause.getMessage(), MenuCartSplitView.this::loadMenu);
                }
            }
        };
        worker.execute();
    }

    private void renderMenu(List<MenuItem> items) {
        menuItemsContainer.removeAll();

        if (items.isEmpty()) {
            statePanel.setVisible(true);
            statePanel.showEmpty("No menu items found", "This restaurant has not listed any dishes yet.");
            return;
        }

        menuItemsContainer.setVisible(true);

        for (MenuItem item : items) {
            menuItemsContainer.add(createFoodCard(item));
            menuItemsContainer.add(Box.createVerticalStrut(14));
        }

        menuItemsContainer.revalidate();
        menuItemsContainer.repaint();
    }

    private JPanel createFoodCard(MenuItem item) {
        RoundCardPanel card = new RoundCardPanel(new BorderLayout(16, 0));
        card.setBorder(new EmptyBorder(14, 16, 14, 16));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        // Rounded thumbnail placeholder
        JPanel thumb = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                AppTheme.enableAntialiasing(g2);
                g2.setColor(AppTheme.BUTTERCREAM);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        thumb.setPreferredSize(new Dimension(64, 64));
        thumb.setOpaque(false);

        JLabel glyph = new JLabel(item.itemName().substring(0, 1));
        glyph.setFont(AppTheme.fontCardTitle());
        glyph.setForeground(AppTheme.ESPRESSO);
        thumb.add(glyph);
        card.add(thumb, BorderLayout.WEST);

        // Center info
        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

        JLabel nameLbl = new JLabel(item.itemName());
        nameLbl.setFont(AppTheme.fontCardTitle());
        nameLbl.setForeground(AppTheme.TEXT_PRIMARY);

        JPanel catPrice = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        catPrice.setOpaque(false);
        JLabel catLbl = new JLabel(item.category());
        catLbl.setFont(AppTheme.fontSmall());
        catLbl.setForeground(AppTheme.TEXT_MUTED);

        JLabel priceLbl = new JLabel(String.format("₹%.2f", item.price()));
        priceLbl.setFont(AppTheme.fontPrice());
        priceLbl.setForeground(AppTheme.ESPRESSO);

        catPrice.add(catLbl);
        catPrice.add(new JLabel("•"));
        catPrice.add(priceLbl);

        info.add(nameLbl);
        info.add(Box.createVerticalStrut(4));
        info.add(catPrice);

        card.add(info, BorderLayout.CENTER);

        // Right "+" Add button
        JButton addBtn = new JButton();
        addBtn.setIcon(Icons.plus(16, Color.WHITE));
        addBtn.setBackground(AppTheme.SERENITY_BLUE);
        addBtn.setFocusPainted(false);
        addBtn.setContentAreaFilled(false);
        addBtn.setBorderPainted(false);
        addBtn.setPreferredSize(new Dimension(38, 38));
        addBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addBtn.addActionListener(e -> addItemToCart(item));

        // Custom round paint for add button
        JPanel btnHolder = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                AppTheme.enableAntialiasing(g2);
                g2.setColor(AppTheme.SERENITY_BLUE);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnHolder.setOpaque(false);
        btnHolder.setPreferredSize(new Dimension(38, 38));
        btnHolder.add(addBtn);

        card.add(btnHolder, BorderLayout.EAST);
        return card;
    }

    private void addItemToCart(MenuItem item) {
        int current = cart.getOrDefault(item.itemId(), 0);
        cart.put(item.itemId(), current + 1);
        refreshCartUI();
    }

    private void refreshCartUI() {
        cartItemsList.removeAll();

        if (cart.isEmpty()) {
            cartItemsList.add(emptyCartNotice);
            checkoutBtn.setEnabled(false);
            subtotal = 0;
            calculatedTotal = 0;
            subtotalLabel.setText("₹0.00");
            discountLabel.setText("-₹0.00");
            totalLabel.setText("₹0.00");
            cartItemsList.revalidate();
            cartItemsList.repaint();
            return;
        }

        checkoutBtn.setEnabled(true);
        subtotal = 0.0;

        for (Map.Entry<Integer, Integer> entry : cart.entrySet()) {
            int itemId = entry.getKey();
            int qty = entry.getValue();
            MenuItem m = itemCache.get(itemId);
            if (m == null) continue;

            subtotal += m.price() * qty;

            JPanel row = new JPanel(new BorderLayout(8, 0));
            row.setOpaque(false);
            row.setBorder(new EmptyBorder(8, 0, 8, 0));

            JPanel left = new JPanel();
            left.setOpaque(false);
            left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

            JLabel title = new JLabel(m.itemName());
            title.setFont(AppTheme.fontBodyBold());
            title.setForeground(AppTheme.TEXT_PRIMARY);

            JLabel price = new JLabel(String.format("₹%.2f", m.price() * qty));
            price.setFont(AppTheme.fontSmallBold());
            price.setForeground(AppTheme.TEXT_MUTED);

            left.add(title);
            left.add(price);
            row.add(left, BorderLayout.CENTER);

            // Stepper
            QuantityStepper stepper = new QuantityStepper(qty, 0, 20);
            stepper.addChangeListener(newQty -> {
                if (newQty <= 0) {
                    cart.remove(itemId);
                } else {
                    cart.put(itemId, newQty);
                }
                refreshCartUI();
            });

            row.add(stepper, BorderLayout.EAST);
            cartItemsList.add(row);
            cartItemsList.add(new JSeparator(SwingConstants.HORIZONTAL));
        }

        cartItemsList.revalidate();
        cartItemsList.repaint();

        updateTotals();
    }

    private void updateTotals() {
        if (cart.isEmpty()) return;

        subtotalLabel.setText(String.format("₹%.2f", subtotal));

        SwingWorker<Double, Void> worker = new SwingWorker<>() {
            @Override
            protected Double doInBackground() throws Exception {
                return api.getTotal(cart, couponCode);
            }

            @Override
            protected void done() {
                try {
                    calculatedTotal = get();
                    double discount = Math.max(0.0, subtotal - calculatedTotal);
                    discountLabel.setText(String.format("-₹%.2f", discount));
                    totalLabel.setText(String.format("₹%.2f", calculatedTotal));
                } catch (Exception ex) {
                    // Fallback to subtotal if coupon invalid
                    discountLabel.setText("-₹0.00");
                    totalLabel.setText(String.format("₹%.2f", subtotal));
                }
            }
        };
        worker.execute();
    }
}
