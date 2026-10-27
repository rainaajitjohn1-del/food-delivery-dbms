package com.fooddelivery.ui.views;

import com.fooddelivery.model.Order;
import com.fooddelivery.model.User;
import com.fooddelivery.service.FoodDeliveryService;
import com.fooddelivery.ui.AppTheme;
import com.fooddelivery.ui.components.PrimaryButton;
import com.fooddelivery.ui.components.RoundCardPanel;
import com.fooddelivery.ui.components.StatePanel;
import com.fooddelivery.ui.components.StyledTable;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

/**
 * AssignAgentDialog: Modal dialog to choose an available agent and call assignDelivery().
 */
public class AssignAgentDialog extends JDialog {

    private final FoodDeliveryService api;
    private final Order order;
    private final Runnable onAssignedSuccess;

    private final DefaultTableModel tableModel;
    private final StyledTable table;
    private final StatePanel statePanel;
    private final JScrollPane tableScroll;
    private final List<User> agents = new ArrayList<>();
    private final PrimaryButton assignBtn;

    public AssignAgentDialog(Frame owner, FoodDeliveryService api, Order order, Runnable onAssignedSuccess) {
        super(owner, "Assign Delivery Agent for Order #" + order.orderId(), true);
        this.api = api;
        this.order = order;
        this.onAssignedSuccess = onAssignedSuccess;

        setSize(520, 500);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        getContentPane().setBackground(AppTheme.WARM_OFF_WHITE);

        RoundCardPanel card = new RoundCardPanel(new BorderLayout(0, 16));
        card.setBorder(new EmptyBorder(24, 24, 24, 24));

        // Header
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Assign Delivery Agent");
        title.setFont(AppTheme.fontSection());
        title.setForeground(AppTheme.ESPRESSO);

        JLabel subtitle = new JLabel("<html>Order #" + order.orderId() + " (" + order.deliveryCity() + ").<br>Assigning an agent will advance status to <b>Out for Delivery</b>.</html>");
        subtitle.setFont(AppTheme.fontSmall());
        subtitle.setForeground(AppTheme.TEXT_MUTED);

        header.add(title);
        header.add(Box.createVerticalStrut(4));
        header.add(subtitle);
        card.add(header, BorderLayout.NORTH);

        // Center Table of Available Agents
        String[] cols = {"Agent ID", "Name", "Email"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new StyledTable(tableModel);
        tableScroll = StyledTable.wrap(table);

        statePanel = new StatePanel();
        statePanel.setVisible(false);

        card.add(statePanel, BorderLayout.NORTH);
        card.add(tableScroll, BorderLayout.CENTER);

        // Bottom Assign Button
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        bottom.setOpaque(false);

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(AppTheme.fontBody());
        cancelBtn.addActionListener(e -> dispose());
        bottom.add(cancelBtn);

        assignBtn = new PrimaryButton("Assign to Order");
        assignBtn.addActionListener(e -> performAssign());
        bottom.add(assignBtn);

        card.add(bottom, BorderLayout.SOUTH);
        add(card, BorderLayout.CENTER);

        loadAgents();
    }

    private void loadAgents() {
        tableScroll.setVisible(false);
        statePanel.setVisible(true);
        statePanel.showLoading("Finding available delivery agents...");

        SwingWorker<List<User>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<User> doInBackground() throws Exception {
                return api.getAvailableAgents();
            }

            @Override
            protected void done() {
                try {
                    agents.clear();
                    agents.addAll(get());
                    statePanel.setVisible(false);
                    tableScroll.setVisible(true);
                    renderAgents();
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    statePanel.showError(cause.getMessage(), AssignAgentDialog.this::loadAgents);
                }
            }
        };
        worker.execute();
    }

    private void renderAgents() {
        tableModel.setRowCount(0);
        if (agents.isEmpty()) {
            tableScroll.setVisible(false);
            statePanel.setVisible(true);
            statePanel.showEmpty("No available agents", "All delivery agents are currently busy delivering orders.");
            assignBtn.setEnabled(false);
            return;
        }

        assignBtn.setEnabled(true);
        for (User u : agents) {
            tableModel.addRow(new Object[]{
                    "#" + u.userId(),
                    u.fullName(),
                    u.email()
            });
        }
    }

    private void performAssign() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= agents.size()) {
            JOptionPane.showMessageDialog(this, "Please select an agent from the list.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        User agent = agents.get(row);
        assignBtn.setEnabled(false);
        assignBtn.setText("Assigning...");

        SwingWorker<Integer, Void> worker = new SwingWorker<>() {
            @Override
            protected Integer doInBackground() throws Exception {
                return api.assignDelivery(order.orderId(), agent.userId());
            }

            @Override
            protected void done() {
                try {
                    int deliveryId = get();
                    JOptionPane.showMessageDialog(AssignAgentDialog.this,
                            "Agent " + agent.fullName() + " assigned! Delivery #" + deliveryId + " created.\nOrder is now Out for Delivery.");
                    if (onAssignedSuccess != null) onAssignedSuccess.run();
                    dispose();
                } catch (Exception ex) {
                    assignBtn.setEnabled(true);
                    assignBtn.setText("Assign to Order");
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    JOptionPane.showMessageDialog(AssignAgentDialog.this, cause.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
}
