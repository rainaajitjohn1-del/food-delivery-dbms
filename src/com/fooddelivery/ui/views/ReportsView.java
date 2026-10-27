package com.fooddelivery.ui.views;

import com.fooddelivery.service.FoodDeliveryService;
import com.fooddelivery.ui.AppTheme;
import com.fooddelivery.ui.Icons;
import com.fooddelivery.ui.components.RoundCardPanel;
import com.fooddelivery.ui.components.SecondaryButton;
import com.fooddelivery.ui.components.StatePanel;
import com.fooddelivery.ui.components.StyledTable;
import java.awt.*;
import java.util.List;
import java.util.concurrent.Callable;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

/**
 * ReportsView: Manager reports dashboard with 4 tabs (Sales, Top Items, Ratings, Agent Performance).
 * Uses row 0 as column headers as returned by the 4 report methods.
 */
public class ReportsView extends JPanel {

    private final FoodDeliveryService api;
    private final JTabbedPane tabbedPane;

    public ReportsView(FoodDeliveryService api) {
        super(new BorderLayout());
        this.api = api;

        setBackground(AppTheme.WARM_OFF_WHITE);
        setBorder(new EmptyBorder(24, 32, 32, 32));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 18, 0));

        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Analytics & Reports");
        title.setFont(AppTheme.fontHero());
        title.setForeground(AppTheme.ESPRESSO);

        JLabel subtitle = new JLabel("System views and performance analytics for sales, items, ratings, and fleet");
        subtitle.setFont(AppTheme.fontBody());
        subtitle.setForeground(AppTheme.TEXT_MUTED);

        titles.add(title);
        titles.add(Box.createVerticalStrut(4));
        titles.add(subtitle);
        header.add(titles, BorderLayout.WEST);

        add(header, BorderLayout.NORTH);

        // Center: 4-Tabbed Pane
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(AppTheme.fontBodyBold());
        tabbedPane.setBackground(AppTheme.WARM_OFF_WHITE);
        tabbedPane.setForeground(AppTheme.TEXT_PRIMARY);

        tabbedPane.addTab("Restaurant Sales", createReportTab("Sales", api::getSalesReport));
        tabbedPane.addTab("Top 10 Items", createReportTab("Top Items", api::getTopItems));
        tabbedPane.addTab("Restaurant Ratings", createReportTab("Ratings", api::getRestaurantRatings));
        tabbedPane.addTab("Agent Performance", createReportTab("Fleet", api::getAgentPerformance));

        RoundCardPanel card = new RoundCardPanel(new BorderLayout());
        card.setBorder(new EmptyBorder(12, 12, 12, 12));
        card.add(tabbedPane, BorderLayout.CENTER);

        add(card, BorderLayout.CENTER);
    }

    private JPanel createReportTab(String tabName, Callable<List<String[]>> reportFetcher) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(12, 8, 8, 8));

        DefaultTableModel model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        StyledTable table = new StyledTable(model);
        JScrollPane scroll = StyledTable.wrap(table);

        StatePanel statePanel = new StatePanel();
        statePanel.setVisible(false);

        panel.add(statePanel, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);

        // Top refresh row
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        topBar.setOpaque(false);
        topBar.setBorder(new EmptyBorder(0, 0, 8, 0));

        SecondaryButton refreshBtn = new SecondaryButton("Refresh " + tabName);
        refreshBtn.setIcon(Icons.refresh(12, Color.WHITE));
        topBar.add(refreshBtn);
        panel.add(topBar, BorderLayout.NORTH);

        Runnable loader = () -> {
            scroll.setVisible(false);
            statePanel.setVisible(true);
            statePanel.showLoading("Generating " + tabName + " report...");

            SwingWorker<List<String[]>, Void> worker = new SwingWorker<>() {
                @Override
                protected List<String[]> doInBackground() throws Exception {
                    return reportFetcher.call();
                }

                @Override
                protected void done() {
                    try {
                        List<String[]> data = get();
                        if (data == null || data.isEmpty()) {
                            statePanel.showEmpty("No data available", "No records found for this report.");
                            scroll.setVisible(false);
                            return;
                        }

                        // Row 0 is column headers
                        String[] headers = data.get(0);
                        model.setDataVector(new Object[0][headers.length], headers);

                        for (int i = 1; i < data.size(); i++) {
                            model.addRow(data.get(i));
                        }

                        statePanel.setVisible(false);
                        scroll.setVisible(true);
                    } catch (Exception ex) {
                        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                        statePanel.showError(cause.getMessage(), () -> {});
                    }
                }
            };
            worker.execute();
        };

        refreshBtn.addActionListener(e -> loader.run());
        loader.run();

        return panel;
    }
}
