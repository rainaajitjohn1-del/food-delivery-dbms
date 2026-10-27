package com.fooddelivery.ui;

import com.fooddelivery.model.Restaurant;
import com.fooddelivery.model.User;
import com.fooddelivery.service.FoodDeliveryService;
import com.fooddelivery.ui.navigation.Sidebar;
import com.fooddelivery.ui.views.*;
import java.awt.*;
import java.util.Map;
import javax.swing.*;

/**
 * MainFrame: Primary desktop application window (1200x760, minimum 1000x640).
 * Coordinates the 220px fixed sidebar, content panel switching, role routing,
 * and view transitions.
 */
public class MainFrame extends JFrame {

    private final FoodDeliveryService api;
    private User currentUser;

    private final JPanel rootPanel;
    private final Sidebar sidebar;
    private final JPanel contentContainer;

    // View caching & active state
    private Restaurant activeRestaurant;

    public MainFrame() {
        super("Good Food — Delivery Desktop Application");
        this.api = new FoodDeliveryService();

        AppTheme.initFonts();

        setSize(1200, 760);
        setMinimumSize(new Dimension(1000, 640));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(AppTheme.WARM_OFF_WHITE);

        sidebar = new Sidebar(this::handleNavSelection, this::handleLogout);
        sidebar.setVisible(false);

        contentContainer = new JPanel(new BorderLayout());
        contentContainer.setBackground(AppTheme.WARM_OFF_WHITE);

        rootPanel.add(sidebar, BorderLayout.WEST);
        rootPanel.add(contentContainer, BorderLayout.CENTER);

        setContentPane(rootPanel);

        showLogin();
    }

    // ------------------------------------------------------------- Auth Flow
    public void showLogin() {
        this.currentUser = null;
        sidebar.setUser(null);
        sidebar.setVisible(false);

        LoginView loginView = new LoginView(api, this::handleLoginSuccess, this::showRegister);
        switchContent(loginView);
    }

    public void showRegister() {
        sidebar.setVisible(false);
        RegisterView regView = new RegisterView(api, this::handleLoginSuccess, this::showLogin);
        switchContent(regView);
    }

    private void handleLoginSuccess(User user) {
        this.currentUser = user;
        sidebar.setUser(user);
        sidebar.setVisible(true);

        String role = user.role();
        if ("CUSTOMER".equalsIgnoreCase(role)) {
            sidebar.selectItem("restaurants");
            showCustomerHome();
        } else if ("MANAGER".equalsIgnoreCase(role)) {
            sidebar.selectItem("manager_orders");
            showManagerOrders();
        } else if ("AGENT".equalsIgnoreCase(role)) {
            sidebar.selectItem("agent_deliveries");
            showAgentDeliveries();
        } else {
            showCustomerHome();
        }
    }

    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to log out?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (confirm == JOptionPane.YES_OPTION) {
            showLogin();
        }
    }

    // ------------------------------------------------------------- Navigation Routing
    private void handleNavSelection(String navId) {
        switch (navId) {
            case "restaurants" -> showCustomerHome();
            case "cart" -> {
                if (activeRestaurant != null) {
                    showMenuAndCart(activeRestaurant);
                } else {
                    showCustomerHome();
                }
            }
            case "orders" -> showMyOrders();
            case "profile" -> showProfile();
            case "manager_orders" -> showManagerOrders();
            case "manager_menu" -> showManagerMenu();
            case "reports" -> showReports();
            case "agent_deliveries" -> showAgentDeliveries();
            default -> showCustomerHome();
        }
    }

    // ------------------------------------------------------------- Screen Launchers
    public void showCustomerHome() {
        CustomerHomeView view = new CustomerHomeView(api, currentUser, this::showMenuAndCart);
        switchContent(view);
    }

    public void showMenuAndCart(Restaurant r) {
        this.activeRestaurant = r;
        MenuCartSplitView view = new MenuCartSplitView(api, currentUser, r, this::showCheckout, this::showCustomerHome);
        switchContent(view);
    }

    public void showCheckout(Restaurant r, Map<Integer, Integer> cart) {
        CheckoutView view = new CheckoutView(api, currentUser, r, cart, orderId -> {
            JOptionPane.showMessageDialog(this,
                    "Order #" + orderId + " placed successfully!\nYour meal is now being prepared.",
                    "Order Confirmed",
                    JOptionPane.INFORMATION_MESSAGE);
            sidebar.selectItem("orders");
            showMyOrders();
        }, () -> showMenuAndCart(r));
        switchContent(view);
    }

    public void showMyOrders() {
        MyOrdersView view = new MyOrdersView(api, currentUser, this);
        switchContent(view);
    }

    public void showProfile() {
        ProfileView view = new ProfileView(api, currentUser, this::handleNavSelection);
        switchContent(view);
    }

    public void showManagerOrders() {
        OrderManagerView view = new OrderManagerView(api, currentUser, this);
        switchContent(view);
    }

    public void showManagerMenu() {
        MenuManagerView view = new MenuManagerView(api, currentUser, this);
        switchContent(view);
    }

    public void showReports() {
        ReportsView view = new ReportsView(api);
        switchContent(view);
    }

    public void showAgentDeliveries() {
        AgentDeliveriesView view = new AgentDeliveriesView(api, currentUser);
        switchContent(view);
    }

    private void switchContent(JComponent newView) {
        contentContainer.removeAll();
        contentContainer.add(newView, BorderLayout.CENTER);
        contentContainer.revalidate();
        contentContainer.repaint();
    }
}
