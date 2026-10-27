package com.fooddelivery.ui.views;

import com.fooddelivery.model.Order;
import com.fooddelivery.model.User;
import com.fooddelivery.service.FoodDeliveryService;
import com.fooddelivery.ui.AppTheme;
import com.fooddelivery.ui.Icons;
import com.fooddelivery.ui.components.PillBadge;
import com.fooddelivery.ui.components.RoundCardPanel;
import com.fooddelivery.ui.components.SecondaryButton;
import com.fooddelivery.ui.components.StatePanel;
import com.fooddelivery.ui.components.StyledTable;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * MyOrdersView: Structured desktop table for customer order history,
 * showing status pills, totals, and opening OrderDetailsDialog.
 */
public class MyOrdersView extends JPanel {

    private final FoodDeliveryService api;
    private final User currentUser;
    private final Frame mainFrame;

    private final DefaultTableModel tableModel;
    private final StyledTable table;
    private final StatePanel statePanel;
    private final JScrollPane tableScroll;
    private final List<Order> currentOrders = new ArrayList<>();

    public MyOrdersView(FoodDeliveryService api, User currentUser, Frame mainFrame) {
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

        JLabel title = new JLabel("My Orders");
        title.setFont(AppTheme.fontHero());
        title.setForeground(AppTheme.ESPRESSO);

        JLabel subtitle = new JLabel("Track your active deliveries and past orders");
        subtitle.setFont(AppTheme.fontBody());
        subtitle.setForeground(AppTheme.TEXT_MUTED);

        titles.add(title);
        titles.add(Box.createVerticalStrut(4));
        titles.add(subtitle);
        header.add(titles, BorderLayout.WEST);

        SecondaryButton refreshBtn = new SecondaryButton("Refresh");
        refreshBtn.setIcon(Icons.refresh(14, Color.WHITE));
        refreshBtn.addActionListener(e -> loadOrders());
        header.add(refreshBtn, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // Center card with table
        RoundCardPanel card = new RoundCardPanel(new BorderLayout());
        card.setBorder(new EmptyBorder(12, 12, 12, 12));

        String[] cols = {"Order ID", "Restaurant", "City", "Status", "Total Amount", "Payment", "Review"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new StyledTable(tableModel);

        // Custom renderer for Status column (column index 3)
        table.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object val, boolean sel, boolean foc, int r, int c) {
                String status = String.valueOf(val);
                return PillBadge.status(status);
            }
        });

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    openSelectedOrderDetails();
                }
            }
        });

        tableScroll = StyledTable.wrap(table);

        statePanel = new StatePanel();
        statePanel.setVisible(false);

        card.add(statePanel, BorderLayout.NORTH);
        card.add(tableScroll, BorderLayout.CENTER);

        // Action bar at bottom of card
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 8));
        bottomBar.setOpaque(false);

        SecondaryButton viewBtn = new SecondaryButton("View Receipt & Review");
        viewBtn.addActionListener(e -> openSelectedOrderDetails());
        bottomBar.add(viewBtn);

        card.add(bottomBar, BorderLayout.SOUTH);
        add(card, BorderLayout.CENTER);

        loadOrders();
    }

    public void loadOrders() {
        tableScroll.setVisible(false);
        statePanel.setVisible(true);
        statePanel.showLoading("Loading your order history...");

        SwingWorker<List<Order>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Order> doInBackground() throws Exception {
                return api.getOrderHistory(currentUser.userId());
            }

            @Override
            protected void done() {
                try {
                    currentOrders.clear();
                    currentOrders.addAll(get());
                    statePanel.setVisible(false);
                    tableScroll.setVisible(true);
                    renderTable();
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    statePanel.showError(cause.getMessage(), MyOrdersView.this::loadOrders);
                }
            }
        };
        worker.execute();
    }

    private void renderTable() {
        tableModel.setRowCount(0);
        if (currentOrders.isEmpty()) {
            tableScroll.setVisible(false);
            statePanel.setVisible(true);
            statePanel.showEmpty("No orders yet", "You haven't placed any food orders yet. Explore our restaurant menus!");
            return;
        }

        for (Order o : currentOrders) {
            String reviewStr = o.rating() != null ? (o.rating() + " ★") : (o.canBeReviewed() ? "Rate Now" : "—");
            tableModel.addRow(new Object[]{
                    "#" + o.orderId(),
                    o.restaurantName(),
                    o.deliveryCity(),
                    o.status(),
                    String.format("₹%.2f", o.total()),
                    o.paymentMethod() + " (" + o.paymentStatus() + ")",
                    reviewStr
            });
        }
    }

    private void openSelectedOrderDetails() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= currentOrders.size()) {
            JOptionPane.showMessageDialog(this, "Please select an order from the table.", "Select Order", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Order selected = currentOrders.get(row);
        OrderDetailsDialog dlg = new OrderDetailsDialog(mainFrame, api, currentUser, selected, this::loadOrders);
        dlg.setVisible(true);
    }
}
