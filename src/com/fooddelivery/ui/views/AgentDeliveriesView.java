package com.fooddelivery.ui.views;

import com.fooddelivery.model.Delivery;
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
 * AgentDeliveriesView: Delivery Agent screen displaying assigned deliveries,
 * cash-on-delivery collection notice, and "Mark Delivered" action.
 */
public class AgentDeliveriesView extends JPanel {

    private final FoodDeliveryService api;
    private final User currentUser;

    private final DefaultTableModel tableModel;
    private final StyledTable table;
    private final StatePanel statePanel;
    private final JScrollPane tableScroll;
    private final List<Delivery> deliveries = new ArrayList<>();

    public AgentDeliveriesView(FoodDeliveryService api, User currentUser) {
        super(new BorderLayout());
        this.api = api;
        this.currentUser = currentUser;

        setBackground(AppTheme.WARM_OFF_WHITE);
        setBorder(new EmptyBorder(24, 32, 32, 32));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 18, 0));

        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("My Deliveries");
        title.setFont(AppTheme.fontHero());
        title.setForeground(AppTheme.ESPRESSO);

        JLabel subtitle = new JLabel("Deliveries assigned to you. Mark orders delivered upon customer handover.");
        subtitle.setFont(AppTheme.fontBody());
        subtitle.setForeground(AppTheme.TEXT_MUTED);

        titles.add(title);
        titles.add(Box.createVerticalStrut(4));
        titles.add(subtitle);
        header.add(titles, BorderLayout.WEST);

        SecondaryButton refreshBtn = new SecondaryButton("Refresh");
        refreshBtn.setIcon(Icons.refresh(14, Color.WHITE));
        refreshBtn.addActionListener(e -> loadDeliveries());
        header.add(refreshBtn, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // Center Table Card
        RoundCardPanel card = new RoundCardPanel(new BorderLayout());
        card.setBorder(new EmptyBorder(12, 12, 12, 12));

        String[] cols = {"Delivery ID", "Order ID", "Customer", "City", "Restaurant", "Total", "Cash To Collect", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new StyledTable(tableModel);

        // Custom status renderer for status (column 7)
        table.getColumnModel().getColumn(7).setCellRenderer(new DefaultTableCellRenderer() {
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

        // Bottom Action Bar: Mark Delivered
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 8));
        bottomBar.setOpaque(false);

        PrimaryButton deliverBtn = new PrimaryButton("Mark Order Delivered");
        deliverBtn.setIcon(Icons.check(16, AppTheme.BUTTERCREAM));
        deliverBtn.addActionListener(e -> performMarkDelivered());
        bottomBar.add(deliverBtn);

        card.add(bottomBar, BorderLayout.SOUTH);
        add(card, BorderLayout.CENTER);

        loadDeliveries();
    }

    public void loadDeliveries() {
        tableScroll.setVisible(false);
        statePanel.setVisible(true);
        statePanel.showLoading("Loading your assigned deliveries...");

        SwingWorker<List<Delivery>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Delivery> doInBackground() throws Exception {
                return api.getMyDeliveries(currentUser.userId());
            }

            @Override
            protected void done() {
                try {
                    deliveries.clear();
                    deliveries.addAll(get());
                    statePanel.setVisible(false);
                    tableScroll.setVisible(true);
                    renderTable();
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    statePanel.showError(cause.getMessage(), AgentDeliveriesView.this::loadDeliveries);
                }
            }
        };
        worker.execute();
    }

    private void renderTable() {
        tableModel.setRowCount(0);
        if (deliveries.isEmpty()) {
            tableScroll.setVisible(false);
            statePanel.setVisible(true);
            statePanel.showEmpty("No active deliveries", "You currently have no assigned delivery runs. Check back soon!");
            return;
        }

        for (Delivery d : deliveries) {
            String cashHint = d.collectCash() ? ("COLLECT ₹" + String.format("%.2f", d.total()) + " CASH") : "Prepaid ✓";

            tableModel.addRow(new Object[]{
                    "#" + d.deliveryId(),
                    "#" + d.orderId(),
                    d.customerName(),
                    d.deliveryCity(),
                    d.restaurantName(),
                    String.format("₹%.2f", d.total()),
                    cashHint,
                    d.status()
            });
        }
    }

    private void performMarkDelivered() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= deliveries.size()) {
            JOptionPane.showMessageDialog(this, "Please select a delivery from the table.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Delivery d = deliveries.get(row);
        if ("Delivered".equalsIgnoreCase(d.status())) {
            JOptionPane.showMessageDialog(this, "Delivery #" + d.deliveryId() + " is already marked as Delivered.", "Completed", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String confirmMsg = "Mark Delivery #" + d.deliveryId() + " as Delivered to " + d.customerName() + "?";
        if (d.collectCash()) {
            confirmMsg += "\n\nIMPORTANT: Have you collected ₹" + String.format("%.2f", d.total()) + " in cash?";
        }

        int confirm = JOptionPane.showConfirmDialog(this, confirmMsg, "Confirm Delivery Completion", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                api.markDelivered(d.deliveryId());
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    JOptionPane.showMessageDialog(AgentDeliveriesView.this, "Delivery #" + d.deliveryId() + " successfully completed!");
                    loadDeliveries();
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    JOptionPane.showMessageDialog(AgentDeliveriesView.this, cause.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
}
