package com.fooddelivery.ui.views;

import com.fooddelivery.model.Order;
import com.fooddelivery.model.OrderLine;
import com.fooddelivery.model.User;
import com.fooddelivery.service.FoodDeliveryService;
import com.fooddelivery.ui.AppTheme;
import com.fooddelivery.ui.Icons;
import com.fooddelivery.ui.components.PillBadge;
import com.fooddelivery.ui.components.PrimaryButton;
import com.fooddelivery.ui.components.RoundCardPanel;
import com.fooddelivery.ui.components.StyledTable;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

/**
 * OrderDetailsDialog: Itemized receipt view with totals and interactive 1-5 star review.
 */
public class OrderDetailsDialog extends JDialog {

    private final FoodDeliveryService api;
    private final User currentUser;
    private Order order;
    private final Runnable onReviewSubmitted;

    private int selectedRating = 5;
    private final JButton[] starButtons = new JButton[5];
    private PrimaryButton submitReviewBtn;
    private JPanel reviewSection;
    private JLabel statusNotice;

    public OrderDetailsDialog(Frame owner, FoodDeliveryService api, User currentUser, Order order, Runnable onReviewSubmitted) {
        super(owner, "Order #" + order.orderId() + " Receipt", true);
        this.api = api;
        this.currentUser = currentUser;
        this.order = order;
        this.onReviewSubmitted = onReviewSubmitted;

        setSize(600, 680);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        getContentPane().setBackground(AppTheme.WARM_OFF_WHITE);

        RoundCardPanel card = new RoundCardPanel(new BorderLayout(0, 16));
        card.setBorder(new EmptyBorder(24, 28, 24, 28));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Order #" + order.orderId());
        title.setFont(AppTheme.fontSection());
        title.setForeground(AppTheme.ESPRESSO);

        JLabel restName = new JLabel("From: " + order.restaurantName() + " • " + order.deliveryCity());
        restName.setFont(AppTheme.fontBody());
        restName.setForeground(AppTheme.TEXT_MUTED);

        titles.add(title);
        titles.add(Box.createVerticalStrut(4));
        titles.add(restName);

        header.add(titles, BorderLayout.CENTER);
        header.add(PillBadge.status(order.status()), BorderLayout.EAST);
        card.add(header, BorderLayout.NORTH);

        // Center: Itemized lines table
        String[] cols = {"Item", "Qty", "Price", "Total"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        StyledTable table = new StyledTable(model);
        JScrollPane scroll = StyledTable.wrap(table);
        scroll.setPreferredSize(new Dimension(500, 220));

        card.add(scroll, BorderLayout.CENTER);

        // Bottom: Totals & Review section
        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));

        // Totals Breakdown
        JPanel totals = new JPanel(new GridLayout(2, 2, 8, 4));
        totals.setOpaque(false);
        totals.setBorder(new EmptyBorder(8, 0, 8, 0));

        totals.add(new JLabel("Subtotal: ₹" + String.format("%.2f", order.subtotal())));
        totals.add(new JLabel("Payment: " + order.paymentMethod() + " (" + order.paymentStatus() + ")"));
        totals.add(new JLabel("Discount: " + order.discountPercent() + "%" + (order.couponCode() != null ? " (" + order.couponCode() + ")" : "")));
        JLabel grand = new JLabel("Total: ₹" + String.format("%.2f", order.total()));
        grand.setFont(AppTheme.fontPrice());
        grand.setForeground(AppTheme.ESPRESSO);
        totals.add(grand);

        bottom.add(totals);
        bottom.add(Box.createVerticalStrut(10));
        bottom.add(new JSeparator(SwingConstants.HORIZONTAL));
        bottom.add(Box.createVerticalStrut(10));

        // Review Section
        reviewSection = new JPanel();
        reviewSection.setOpaque(false);
        reviewSection.setLayout(new BoxLayout(reviewSection, BoxLayout.Y_AXIS));

        buildReviewUI();
        bottom.add(reviewSection);

        card.add(bottom, BorderLayout.SOUTH);
        add(card, BorderLayout.CENTER);

        loadItems(model);
    }

    private void buildReviewUI() {
        reviewSection.removeAll();

        if (order.rating() != null) {
            JLabel reviewedLbl = new JLabel("Your Review: " + order.rating() + " ★", SwingConstants.CENTER);
            reviewedLbl.setFont(AppTheme.fontBodyBold());
            reviewedLbl.setForeground(AppTheme.SUCCESS_GREEN);
            reviewedLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
            reviewSection.add(reviewedLbl);
        } else if (order.canBeReviewed()) {
            JLabel rateTitle = new JLabel("Rate this delivery & food (1 to 5 Stars):");
            rateTitle.setFont(AppTheme.fontBodyBold());
            rateTitle.setForeground(AppTheme.ESPRESSO);
            rateTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
            reviewSection.add(rateTitle);
            reviewSection.add(Box.createVerticalStrut(8));

            JPanel starsRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
            starsRow.setOpaque(false);

            for (int i = 0; i < 5; i++) {
                final int ratingVal = i + 1;
                JButton star = new JButton();
                star.setContentAreaFilled(false);
                star.setBorderPainted(false);
                star.setFocusPainted(false);
                star.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                starButtons[i] = star;

                star.addActionListener(e -> {
                    selectedRating = ratingVal;
                    updateStarIcons();
                });
                starsRow.add(star);
            }
            updateStarIcons();
            reviewSection.add(starsRow);
            reviewSection.add(Box.createVerticalStrut(12));

            submitReviewBtn = new PrimaryButton("Submit Review");
            submitReviewBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
            submitReviewBtn.addActionListener(e -> submitReview());
            reviewSection.add(submitReviewBtn);
        } else {
            statusNotice = new JLabel("Reviews open after order is Delivered (Current: " + order.status() + ")", SwingConstants.CENTER);
            statusNotice.setFont(AppTheme.fontSmall());
            statusNotice.setForeground(AppTheme.TEXT_MUTED);
            statusNotice.setAlignmentX(Component.CENTER_ALIGNMENT);
            reviewSection.add(statusNotice);
        }

        reviewSection.revalidate();
        reviewSection.repaint();
    }

    private void updateStarIcons() {
        for (int i = 0; i < 5; i++) {
            boolean filled = (i < selectedRating);
            starButtons[i].setIcon(Icons.star(24, filled ? AppTheme.WARNING_AMBER : AppTheme.TEXT_MUTED, filled));
        }
    }

    private void loadItems(DefaultTableModel model) {
        SwingWorker<List<OrderLine>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<OrderLine> doInBackground() throws Exception {
                return api.getOrderItems(order.orderId());
            }

            @Override
            protected void done() {
                try {
                    List<OrderLine> lines = get();
                    for (OrderLine line : lines) {
                        model.addRow(new Object[]{
                                line.itemName(),
                                line.quantity(),
                                String.format("₹%.2f", line.price()),
                                String.format("₹%.2f", line.lineTotal())
                        });
                    }
                } catch (Exception ignored) { }
            }
        };
        worker.execute();
    }

    private void submitReview() {
        submitReviewBtn.setEnabled(false);
        submitReviewBtn.setText("Submitting...");

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                api.addReview(currentUser.userId(), order.orderId(), selectedRating);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    JOptionPane.showMessageDialog(OrderDetailsDialog.this, "Thank you! Your " + selectedRating + " ★ review was recorded.");
                    if (onReviewSubmitted != null) onReviewSubmitted.run();
                    dispose();
                } catch (Exception ex) {
                    submitReviewBtn.setEnabled(true);
                    submitReviewBtn.setText("Submit Review");
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    JOptionPane.showMessageDialog(OrderDetailsDialog.this, cause.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
}
