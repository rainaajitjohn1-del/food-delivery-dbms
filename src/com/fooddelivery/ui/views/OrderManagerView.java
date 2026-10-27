package com.fooddelivery.ui.views;

import com.fooddelivery.model.Order;
import com.fooddelivery.model.Restaurant;
import com.fooddelivery.model.User;
import com.fooddelivery.service.FoodDeliveryService;
import com.fooddelivery.ui.AppTheme;
import com.fooddelivery.ui.Icons;
import com.fooddelivery.ui.components.PillBadge;
import com.fooddelivery.ui.components.PrimaryButton;
import com.fooddelivery.ui.components.RoundCardPanel;
import com.fooddelivery.ui.components.SecondaryButton;
import com.fooddelivery.ui.components.StatePanel;
import com.fooddelivery.ui.components.StyledTable;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * OrderManagerView: Manager view for restaurant order fulfillment, advancing status,
 * and dispatching delivery agents.
 */
public class OrderManagerView extends JPanel {

    private final FoodDeliveryService api;
    private final User currentUser;
    private final Frame mainFrame;

    private Restaurant myRestaurant;
    private final DefaultTableModel tableModel;
    private final StyledTable table;
    private final StatePanel statePanel;
    private final JScrollPane tableScroll;
    private final List<Order> orders = new ArrayList<>();
    private final JLabel subtitleLabel;

    public OrderManagerView(FoodDeliveryService api, User currentUser, Frame mainFrame) {
        super(new BorderLayout());
        this.api = api;
        this.currentUser = currentUser;
        this.mainFrame = mainFrame;

        setBackground(AppTheme.WARM_OFF_WHITE);
        setBorder(new EmptyBorder(24, 32, 32, 32));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 18, 0));

        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Order Management Queue");
        title.setFont(AppTheme.fontHero());
        title.setForeground(AppTheme.ESPRESSO);

        subtitleLabel = new JLabel("Loading restaurant queue...");
        subtitleLabel.setFont(AppTheme.fontBody());
        subtitleLabel.setForeground(AppTheme.TEXT_MUTED);

        titles.add(title);
        titles.add(Box.createVerticalStrut(4));
        titles.add(subtitleLabel);
        header.add(titles, BorderLayout.WEST);

        SecondaryButton refreshBtn = new SecondaryButton("Refresh Queue");
        refreshBtn.setIcon(Icons.refresh(14, Color.WHITE));
        refreshBtn.addActionListener(e -> loadOrders());
        header.add(refreshBtn, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // Center card with table
        RoundCardPanel card = new RoundCardPanel(new BorderLayout());
        card.setBorder(new EmptyBorder(12, 12, 12, 12));

        String[] cols = {"Order ID", "Customer", "City", "Status", "Total Amount", "Payment", "Workflow Action"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new StyledTable(tableModel);

        // Status pill renderer
        table.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object val, boolean sel, boolean foc, int r, int c) {
                return PillBadge.status(String.valueOf(val));
            }
        });

        tableScroll = StyledTable.wrap(table);

        statePanel = new StatePanel();
        statePanel.setVisible(false);

        card.add(statePanel, BorderLayout.NORTH);
        card.add(tableScroll, BorderLayout.CENTER);

        // Bottom Action Bar
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 8));
        bottomBar.setOpaque(false);

        PrimaryButton advanceBtn = new PrimaryButton("Advance Workflow / Assign Agent");
        advanceBtn.addActionListener(e -> advanceSelectedOrder());
        bottomBar.add(advanceBtn);

        card.add(bottomBar, BorderLayout.SOUTH);
        add(card, BorderLayout.CENTER);

        loadOrders();
    }

    private void loadOrders() {
        tableScroll.setVisible(false);
        statePanel.setVisible(true);
        statePanel.showLoading("Loading orders for your restaurant...");

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                myRestaurant = api.getMyRestaurant(currentUser.userId());
                orders.clear();
                if (myRestaurant != null) {
                    orders.addAll(api.getRestaurantOrders(myRestaurant.restaurantId()));
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    if (myRestaurant != null) {
                        subtitleLabel.setText("Active orders for: " + myRestaurant.name());
                    }
                    statePanel.setVisible(false);
                    tableScroll.setVisible(true);
                    renderOrders();
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    statePanel.showError(cause.getMessage(), OrderManagerView.this::loadOrders);
                }
            }
        };
        worker.execute();
    }

    private void renderOrders() {
        tableModel.setRowCount(0);
        if (orders.isEmpty()) {
            tableScroll.setVisible(false);
            statePanel.setVisible(true);
            statePanel.showEmpty("No orders in queue", "No customer orders have been placed for your restaurant yet.");
            return;
        }

        for (Order o : orders) {
            String nextAction;
            switch (o.status()) {
                case "Placed" -> nextAction = "Start Preparing →";
                case "Preparing" -> nextAction = "Assign Delivery Agent →";
                case "Out for Delivery" -> nextAction = "Waiting for Agent...";
                case "Delivered" -> nextAction = "Completed ✓";
                default -> nextAction = "—";
            }

            tableModel.addRow(new Object[]{
                    "#" + o.orderId(),
                    o.customerName(),
                    o.deliveryCity(),
                    o.status(),
                    String.format("₹%.2f", o.total()),
                    o.paymentMethod() + " (" + o.paymentStatus() + ")",
                    nextAction
            });
        }
    }

    private void advanceSelectedOrder() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= orders.size()) {
            JOptionPane.showMessageDialog(this, "Please select an order from the table.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Order o = orders.get(row);
        String status = o.status();

        if ("Placed".equals(status)) {
            // Update to Preparing
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Move Order #" + o.orderId() + " to 'Preparing'?",
                    "Start Preparation",
                    JOptionPane.YES_NO_OPTION
            );
            if (confirm != JOptionPane.YES_OPTION) return;

            SwingWorker<Void, Void> w = new SwingWorker<>() {
                @Override
                protected Void doInBackground() throws Exception {
                    api.updateOrderStatus(o.orderId(), "Preparing");
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get();
                        loadOrders();
                    } catch (Exception ex) {
                        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                        JOptionPane.showMessageDialog(OrderManagerView.this, cause.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            w.execute();
        } else if ("Preparing".equals(status)) {
            // Assign delivery agent dialog
            AssignAgentDialog dlg = new AssignAgentDialog(mainFrame, api, o, this::loadOrders);
            dlg.setVisible(true);
        } else if ("Out for Delivery".equals(status)) {
            JOptionPane.showMessageDialog(this,
                    "Order #" + o.orderId() + " is currently Out for Delivery with the assigned agent.\nThe agent will mark it Delivered upon arrival.",
                    "Active Delivery",
                    JOptionPane.INFORMATION_MESSAGE);
        } else if ("Delivered".equals(status)) {
            JOptionPane.showMessageDialog(this, "Order #" + o.orderId() + " is already delivered and completed.", "Completed", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
