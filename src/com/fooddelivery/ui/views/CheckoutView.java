package com.fooddelivery.ui.views;

import com.fooddelivery.model.Address;
import com.fooddelivery.model.MenuItem;
import com.fooddelivery.model.Restaurant;
import com.fooddelivery.model.User;
import com.fooddelivery.service.FoodDeliveryService;
import com.fooddelivery.ui.AppTheme;
import com.fooddelivery.ui.Icons;
import com.fooddelivery.ui.components.PillChipButton;
import com.fooddelivery.ui.components.PrimaryButton;
import com.fooddelivery.ui.components.RoundCardPanel;
import com.fooddelivery.ui.components.RoundedComboBox;
import com.fooddelivery.ui.components.RoundedTextField;
import com.fooddelivery.ui.components.SecondaryButton;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * CheckoutView: 2-column layout for address selection/creation, payment method,
 * order summary, and order placement via placeOrder().
 */
public class CheckoutView extends JPanel {

    private final FoodDeliveryService api;
    private final User currentUser;
    private final Restaurant restaurant;
    private final Map<Integer, Integer> cart;
    private final Consumer<Integer> onOrderPlacedSuccess;
    private final Runnable onBackToMenu;

    private final RoundedComboBox<Address> addressCombo;
    private final RoundedTextField newCityInput;
    private final ButtonGroup paymentGroup;
    private String selectedPaymentMethod = "UPI";
    private final JLabel subtotalLbl;
    private final JLabel totalLbl;
    private final JLabel errorLabel;
    private final PrimaryButton placeOrderBtn;
    private final Map<Integer, MenuItem> itemDetails = new HashMap<>();

    public CheckoutView(FoodDeliveryService api, User currentUser, Restaurant restaurant,
                        Map<Integer, Integer> cart, Consumer<Integer> onOrderPlacedSuccess,
                        Runnable onBackToMenu) {
        super(new BorderLayout());
        this.api = api;
        this.currentUser = currentUser;
        this.restaurant = restaurant;
        this.cart = cart;
        this.onOrderPlacedSuccess = onOrderPlacedSuccess;
        this.onBackToMenu = onBackToMenu;

        setBackground(AppTheme.WARM_OFF_WHITE);

        // Header
        JPanel topHeader = new JPanel(new BorderLayout());
        topHeader.setOpaque(false);
        topHeader.setBorder(new EmptyBorder(24, 32, 16, 32));

        PillChipButton backBtn = new PillChipButton(" Back to Menu", Icons.back(16, AppTheme.ESPRESSO));
        backBtn.addActionListener(e -> onBackToMenu.run());

        JLabel headerTitle = new JLabel("Checkout");
        headerTitle.setFont(AppTheme.fontHero());
        headerTitle.setForeground(AppTheme.ESPRESSO);

        topHeader.add(backBtn, BorderLayout.WEST);
        topHeader.add(headerTitle, BorderLayout.CENTER);
        add(topHeader, BorderLayout.NORTH);

        // 2-Column Split: Left Address/Payment, Right 320px Summary
        JPanel contentRow = new JPanel(new BorderLayout(24, 0));
        contentRow.setOpaque(false);
        contentRow.setBorder(new EmptyBorder(8, 32, 32, 32));

        // LEFT COLUMN: Address & Payment
        RoundCardPanel leftCard = new RoundCardPanel(new BorderLayout());
        leftCard.setBorder(new EmptyBorder(24, 24, 24, 24));

        JPanel leftContent = new JPanel();
        leftContent.setOpaque(false);
        leftContent.setLayout(new BoxLayout(leftContent, BoxLayout.Y_AXIS));

        // 1. Delivery Address Section
        JLabel addrSection = new JLabel("1. Delivery Address");
        addrSection.setFont(AppTheme.fontSection());
        addrSection.setForeground(AppTheme.ESPRESSO);
        leftContent.add(addrSection);
        leftContent.add(Box.createVerticalStrut(12));

        addressCombo = new RoundedComboBox<>();
        addressCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        leftContent.add(addressCombo);
        leftContent.add(Box.createVerticalStrut(12));

        // Inline Add Address
        JPanel addAddrRow = new JPanel(new BorderLayout(8, 0));
        addAddrRow.setOpaque(false);
        addAddrRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        newCityInput = new RoundedTextField("Enter city or area name", 12);

        SecondaryButton addCityBtn = new SecondaryButton("+ Add City", 999);
        addCityBtn.addActionListener(e -> addNewAddress());

        addAddrRow.add(newCityInput, BorderLayout.CENTER);
        addAddrRow.add(addCityBtn, BorderLayout.EAST);
        leftContent.add(addAddrRow);
        leftContent.add(Box.createVerticalStrut(28));

        // 2. Payment Method Section
        JLabel paySection = new JLabel("2. Payment Method");
        paySection.setFont(AppTheme.fontSection());
        paySection.setForeground(AppTheme.ESPRESSO);
        leftContent.add(paySection);
        leftContent.add(Box.createVerticalStrut(12));

        paymentGroup = new ButtonGroup();
        JPanel payOptions = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        payOptions.setOpaque(false);

        for (String pm : FoodDeliveryService.PAYMENT_METHODS) {
            JRadioButton rb = new JRadioButton(pm, pm.equals(selectedPaymentMethod));
            rb.setFont(AppTheme.fontBodyBold());
            rb.setForeground(AppTheme.TEXT_PRIMARY);
            rb.setOpaque(false);
            rb.addActionListener(e -> selectedPaymentMethod = pm);
            paymentGroup.add(rb);
            payOptions.add(rb);
        }
        leftContent.add(payOptions);

        leftCard.add(leftContent, BorderLayout.NORTH);
        contentRow.add(leftCard, BorderLayout.CENTER);

        // RIGHT COLUMN: Order Summary & Place Order (320px)
        RoundCardPanel rightCard = new RoundCardPanel(new BorderLayout(0, 16));
        rightCard.setPreferredSize(new Dimension(320, 0));
        rightCard.setMinimumSize(new Dimension(300, 0));
        rightCard.setBorder(new EmptyBorder(24, 20, 24, 20));

        JLabel summaryTitle = new JLabel("Order Summary");
        summaryTitle.setFont(AppTheme.fontSection());
        summaryTitle.setForeground(AppTheme.ESPRESSO);
        rightCard.add(summaryTitle, BorderLayout.NORTH);

        JPanel rightContent = new JPanel();
        rightContent.setOpaque(false);
        rightContent.setLayout(new BoxLayout(rightContent, BoxLayout.Y_AXIS));

        JLabel restLbl = new JLabel("From: " + restaurant.name());
        restLbl.setFont(AppTheme.fontBodyBold());
        restLbl.setForeground(AppTheme.TEXT_MUTED);
        rightContent.add(restLbl);
        rightContent.add(Box.createVerticalStrut(12));

        // Items list
        JPanel itemsBox = new JPanel();
        itemsBox.setOpaque(false);
        itemsBox.setLayout(new BoxLayout(itemsBox, BoxLayout.Y_AXIS));

        for (Map.Entry<Integer, Integer> entry : cart.entrySet()) {
            JLabel line = new JLabel("• Item #" + entry.getKey() + "  × " + entry.getValue());
            line.setFont(AppTheme.fontBody());
            line.setForeground(AppTheme.TEXT_PRIMARY);
            itemsBox.add(line);
            itemsBox.add(Box.createVerticalStrut(4));
        }
        rightContent.add(itemsBox);
        rightContent.add(Box.createVerticalStrut(16));
        rightContent.add(new JSeparator(SwingConstants.HORIZONTAL));
        rightContent.add(Box.createVerticalStrut(16));

        subtotalLbl = new JLabel("Calculating...", SwingConstants.RIGHT);
        subtotalLbl.setFont(AppTheme.fontBodyBold());
        totalLbl = new JLabel("Calculating...", SwingConstants.RIGHT);
        totalLbl.setFont(AppTheme.fontPrice());
        totalLbl.setForeground(AppTheme.ESPRESSO);

        rightContent.add(createRow("Subtotal", subtotalLbl));
        rightContent.add(Box.createVerticalStrut(8));
        rightContent.add(createRow("Delivery Fee", new JLabel("FREE", SwingConstants.RIGHT)));
        rightContent.add(Box.createVerticalStrut(12));
        rightContent.add(new JSeparator(SwingConstants.HORIZONTAL));
        rightContent.add(Box.createVerticalStrut(12));
        rightContent.add(createRow("Total Amount", totalLbl));
        rightContent.add(Box.createVerticalStrut(16));

        errorLabel = new JLabel("", SwingConstants.CENTER);
        errorLabel.setFont(AppTheme.fontSmallBold());
        errorLabel.setForeground(AppTheme.ERROR_RED);
        errorLabel.setVisible(false);
        rightContent.add(errorLabel);
        rightContent.add(Box.createVerticalStrut(12));

        placeOrderBtn = new PrimaryButton("Place Order");
        placeOrderBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        placeOrderBtn.addActionListener(e -> performPlaceOrder());
        rightContent.add(placeOrderBtn);

        rightCard.add(rightContent, BorderLayout.CENTER);
        contentRow.add(rightCard);

        add(contentRow, BorderLayout.CENTER);

        loadAddresses();
        loadTotals();
    }

    private JPanel createRow(String label, JComponent comp) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        JLabel l = new JLabel(label);
        l.setFont(AppTheme.fontBody());
        l.setForeground(AppTheme.TEXT_MUTED);
        p.add(l, BorderLayout.WEST);
        p.add(comp, BorderLayout.EAST);
        return p;
    }

    private void loadAddresses() {
        SwingWorker<List<Address>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Address> doInBackground() throws Exception {
                return api.getAddresses(currentUser.userId());
            }

            @Override
            protected void done() {
                try {
                    List<Address> list = get();
                    addressCombo.removeAllItems();
                    for (Address a : list) {
                        addressCombo.addItem(a);
                    }
                    if (list.isEmpty()) {
                        errorLabel.setText("Please add a delivery city/address before placing order.");
                        errorLabel.setVisible(true);
                        placeOrderBtn.setEnabled(false);
                    } else {
                        errorLabel.setVisible(false);
                        placeOrderBtn.setEnabled(true);
                    }
                } catch (Exception ex) {
                    errorLabel.setText("Could not load addresses: " + ex.getMessage());
                    errorLabel.setVisible(true);
                }
            }
        };
        worker.execute();
    }

    private void addNewAddress() {
        String city = newCityInput.getText().trim();
        if (city.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a valid city name.", "Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        SwingWorker<Address, Void> worker = new SwingWorker<>() {
            @Override
            protected Address doInBackground() throws Exception {
                return api.addAddress(currentUser.userId(), city);
            }

            @Override
            protected void done() {
                try {
                    Address created = get();
                    newCityInput.setText("");
                    loadAddresses();
                    JOptionPane.showMessageDialog(CheckoutView.this, "Address added successfully: " + created.city());
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    JOptionPane.showMessageDialog(CheckoutView.this, cause.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void loadTotals() {
        SwingWorker<Double, Void> worker = new SwingWorker<>() {
            @Override
            protected Double doInBackground() throws Exception {
                return api.getTotal(cart, null);
            }

            @Override
            protected void done() {
                try {
                    double tot = get();
                    subtotalLbl.setText(String.format("₹%.2f", tot));
                    totalLbl.setText(String.format("₹%.2f", tot));
                } catch (Exception ex) {
                    subtotalLbl.setText("Error");
                    totalLbl.setText("Error");
                }
            }
        };
        worker.execute();
    }

    private void performPlaceOrder() {
        Address addr = (Address) addressCombo.getSelectedItem();
        if (addr == null) {
            errorLabel.setText("Please select or add a delivery address.");
            errorLabel.setVisible(true);
            return;
        }

        errorLabel.setVisible(false);
        placeOrderBtn.setEnabled(false);
        placeOrderBtn.setText("Placing order...");

        SwingWorker<Integer, Void> worker = new SwingWorker<>() {
            @Override
            protected Integer doInBackground() throws Exception {
                return api.placeOrder(
                        currentUser.userId(),
                        addr.addressNo(),
                        restaurant.restaurantId(),
                        cart,
                        null,
                        selectedPaymentMethod
                );
            }

            @Override
            protected void done() {
                placeOrderBtn.setEnabled(true);
                placeOrderBtn.setText("Place Order");
                try {
                    int orderId = get();
                    if (onOrderPlacedSuccess != null) {
                        onOrderPlacedSuccess.accept(orderId);
                    }
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    errorLabel.setText("<html><center style='color:#D32F2F;'>" + cause.getMessage() + "</center></html>");
                    errorLabel.setVisible(true);
                }
            }
        };
        worker.execute();
    }
}
