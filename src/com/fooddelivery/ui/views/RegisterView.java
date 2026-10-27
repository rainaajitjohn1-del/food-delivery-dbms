package com.fooddelivery.ui.views;

import com.fooddelivery.model.User;
import com.fooddelivery.service.FoodDeliveryService;
import com.fooddelivery.ui.AppTheme;
import com.fooddelivery.ui.Icons;
import com.fooddelivery.ui.components.PrimaryButton;
import com.fooddelivery.ui.components.RoundCardPanel;
import java.awt.*;
import java.awt.geom.Path2D;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * RegisterView: Desktop split layout.
 * Left: Branded Espresso panel with typography and feature highlights.
 * Right: Warm off-white area with customer signup form card.
 */
public class RegisterView extends JPanel {

    private final FoodDeliveryService api;
    private final Consumer<User> onRegisterSuccess;
    private final Runnable onGoToLogin;

    private final JTextField firstNameInput;
    private final JTextField lastNameInput;
    private final JTextField emailInput;
    private final JTextField phoneInput;
    private final JComboBox<String> roleCombo;
    private final PrimaryButton registerBtn;
    private final JLabel errorLabel;
    private final JLabel roleInfoLabel;

    public RegisterView(FoodDeliveryService api, Consumer<User> onRegisterSuccess, Runnable onGoToLogin) {
        super(new GridLayout(1, 2));
        this.api = api;
        this.onRegisterSuccess = onRegisterSuccess;
        this.onGoToLogin = onGoToLogin;

        // LEFT PANEL: Espresso Branding
        JPanel leftPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                AppTheme.enableAntialiasing(g2);
                int w = getWidth();
                int h = getHeight();

                g2.setColor(AppTheme.ESPRESSO);
                g2.fillRect(0, 0, w, h);

                g2.setColor(new Color(0x8F, 0xA7, 0xC8, 45));
                Path2D.Double blob = new Path2D.Double();
                blob.moveTo(w * 0.15, h);
                blob.curveTo(w * 0.45, h * 0.55, w * 0.25, h * 0.25, w, h * 0.15);
                blob.lineTo(w, h);
                blob.closePath();
                g2.fill(blob);

                g2.dispose();
                super.paintComponent(g);
            }
        };
        leftPanel.setBorder(new EmptyBorder(60, 60, 60, 60));

        JPanel leftContent = new JPanel();
        leftContent.setOpaque(false);
        leftContent.setLayout(new BoxLayout(leftContent, BoxLayout.Y_AXIS));

        JLabel logo = new JLabel(Icons.heart(44, AppTheme.BUTTERCREAM));
        JLabel brandName = new JLabel("Good Food");
        brandName.setFont(AppTheme.fontCardTitle());
        brandName.setForeground(AppTheme.SERENITY_BLUE);

        JLabel heroHeading = new JLabel("<html>Join the Food<br>Experience.</html>");
        heroHeading.setFont(AppTheme.fontHero());
        heroHeading.setForeground(AppTheme.BUTTERCREAM);

        JLabel heroSubtitle = new JLabel("<html>Create a customer account to browse<br>menus, place orders, and review food.</html>");
        heroSubtitle.setFont(AppTheme.fontBody());
        heroSubtitle.setForeground(new Color(0xDE, 0xD0, 0xC0));

        leftContent.add(logo);
        leftContent.add(Box.createVerticalStrut(12));
        leftContent.add(brandName);
        leftContent.add(Box.createVerticalStrut(28));
        leftContent.add(heroHeading);
        leftContent.add(Box.createVerticalStrut(16));
        leftContent.add(heroSubtitle);
        leftContent.add(Box.createVerticalStrut(36));

        leftContent.add(createPropRow("Instant customer account creation"));
        leftContent.add(Box.createVerticalStrut(12));
        leftContent.add(createPropRow("Access all top-rated restaurants"));
        leftContent.add(Box.createVerticalStrut(12));
        leftContent.add(createPropRow("Transparent order workflow & discounts"));

        leftPanel.add(leftContent, BorderLayout.CENTER);
        add(leftPanel);

        // RIGHT PANEL: Form Card on Warm Off-White
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBackground(AppTheme.WARM_OFF_WHITE);

        RoundCardPanel card = new RoundCardPanel(new BorderLayout());
        card.setPreferredSize(new Dimension(460, 580));
        card.setMinimumSize(new Dimension(420, 540));
        card.setBorder(new EmptyBorder(28, 36, 28, 36));

        JPanel inner = new JPanel();
        inner.setOpaque(false);
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);

        JButton backBtn = new JButton();
        backBtn.setIcon(Icons.back(18, AppTheme.ESPRESSO));
        backBtn.setBorderPainted(false);
        backBtn.setContentAreaFilled(false);
        backBtn.setFocusPainted(false);
        backBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backBtn.addActionListener(e -> {
            if (onGoToLogin != null) onGoToLogin.run();
        });
        topRow.add(backBtn, BorderLayout.WEST);

        JLabel title = new JLabel("Create Account", SwingConstants.CENTER);
        title.setFont(AppTheme.fontSection());
        title.setForeground(AppTheme.ESPRESSO);
        topRow.add(title, BorderLayout.CENTER);
        topRow.add(Box.createHorizontalStrut(24), BorderLayout.EAST);

        inner.add(topRow);
        inner.add(Box.createVerticalStrut(18));

        // Names Row
        JPanel namesRow = new JPanel(new GridLayout(1, 2, 10, 0));
        namesRow.setOpaque(false);
        firstNameInput = createInputField();
        lastNameInput = createInputField();

        namesRow.add(createFieldColumn("First Name", firstNameInput));
        namesRow.add(createFieldColumn("Last Name", lastNameInput));
        inner.add(namesRow);
        inner.add(Box.createVerticalStrut(10));

        // Email
        emailInput = createInputField();
        inner.add(createFieldColumn("Email Address", emailInput));
        inner.add(Box.createVerticalStrut(10));

        // Phone
        phoneInput = createInputField();
        inner.add(createFieldColumn("Mobile Phone (10 digits)", phoneInput));
        inner.add(Box.createVerticalStrut(10));

        // Role Selector
        roleCombo = new JComboBox<>(new String[]{"Customer", "Manager", "Delivery Agent"});
        roleCombo.setFont(AppTheme.fontBody());
        roleCombo.setBackground(AppTheme.BUTTERCREAM);
        roleCombo.setForeground(AppTheme.TEXT_PRIMARY);
        roleCombo.setPreferredSize(new Dimension(roleCombo.getPreferredSize().width, 38));

        roleInfoLabel = new JLabel("Customer accounts can immediately order food.");
        roleInfoLabel.setFont(AppTheme.fontSmall());
        roleInfoLabel.setForeground(AppTheme.TEXT_MUTED);

        roleCombo.addActionListener(e -> {
            String selected = (String) roleCombo.getSelectedItem();
            if ("Customer".equals(selected)) {
                roleInfoLabel.setText("Customer accounts can immediately order food.");
            } else {
                roleInfoLabel.setText("Staff accounts (Manager/Agent) are pre-provisioned in sample data.");
            }
        });

        inner.add(createFieldColumn("Account Role", roleCombo));
        inner.add(Box.createVerticalStrut(3));
        inner.add(roleInfoLabel);
        inner.add(Box.createVerticalStrut(12));

        // Error message banner
        errorLabel = new JLabel("", SwingConstants.CENTER);
        errorLabel.setFont(AppTheme.fontSmallBold());
        errorLabel.setForeground(AppTheme.ERROR_RED);
        errorLabel.setVisible(false);
        inner.add(errorLabel);
        inner.add(Box.createVerticalStrut(10));

        // Register Button
        registerBtn = new PrimaryButton("Register");
        registerBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        registerBtn.addActionListener(e -> performRegister());
        inner.add(registerBtn);
        inner.add(Box.createVerticalStrut(14));

        // Link back to login
        JPanel backToLogin = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        backToLogin.setOpaque(false);
        JLabel exist = new JLabel("Already registered?");
        exist.setFont(AppTheme.fontBody());
        exist.setForeground(AppTheme.TEXT_MUTED);

        JButton signLink = new JButton("Sign in here");
        signLink.setFont(AppTheme.fontBodyBold());
        signLink.setForeground(AppTheme.SERENITY_BLUE);
        signLink.setBorderPainted(false);
        signLink.setContentAreaFilled(false);
        signLink.setFocusPainted(false);
        signLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        signLink.addActionListener(e -> {
            if (onGoToLogin != null) onGoToLogin.run();
        });

        backToLogin.add(exist);
        backToLogin.add(signLink);
        inner.add(backToLogin);

        card.add(inner, BorderLayout.CENTER);
        rightPanel.add(card);
        add(rightPanel);
    }

    private JPanel createPropRow(String text) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        p.setOpaque(false);
        JLabel dot = new JLabel(Icons.check(14, AppTheme.SERENITY_BLUE));
        JLabel lbl = new JLabel(text);
        lbl.setFont(AppTheme.fontBody());
        lbl.setForeground(AppTheme.BUTTERCREAM);
        p.add(dot);
        p.add(lbl);
        return p;
    }

    private JTextField createInputField() {
        JTextField tf = new JTextField();
        tf.setFont(AppTheme.fontBody());
        tf.setForeground(AppTheme.TEXT_PRIMARY);
        tf.setBackground(AppTheme.BUTTERCREAM);
        tf.setCaretColor(AppTheme.TEXT_PRIMARY);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.BORDER_LIGHT, 1),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        return tf;
    }

    private JPanel createFieldColumn(String labelText, JComponent input) {
        JPanel col = new JPanel();
        col.setOpaque(false);
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));

        JLabel lbl = new JLabel(labelText);
        lbl.setFont(AppTheme.fontSmallBold());
        lbl.setForeground(AppTheme.TEXT_PRIMARY);
        col.add(lbl);
        col.add(Box.createVerticalStrut(3));
        col.add(input);
        return col;
    }

    private void performRegister() {
        String first = firstNameInput.getText().trim();
        String last = lastNameInput.getText().trim();
        String email = emailInput.getText().trim();
        String phone = phoneInput.getText().trim();
        String role = (String) roleCombo.getSelectedItem();

        if (first.isEmpty() || last.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            showError("Please fill in all registration fields.");
            return;
        }

        if (!"Customer".equalsIgnoreCase(role)) {
            showError("Manager and Agent accounts are provisioned with assigned restaurants/vehicles. Please log in using the demo presets.");
            return;
        }

        clearError();
        registerBtn.setEnabled(false);
        registerBtn.setText("Registering...");

        SwingWorker<User, Void> worker = new SwingWorker<>() {
            @Override
            protected User doInBackground() throws Exception {
                api.registerCustomer(first, last, email, phone);
                return api.login(email);
            }

            @Override
            protected void done() {
                registerBtn.setEnabled(true);
                registerBtn.setText("Register");
                try {
                    User newUser = get();
                    if (onRegisterSuccess != null) {
                        onRegisterSuccess.accept(newUser);
                    }
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    showError(cause.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void showError(String msg) {
        errorLabel.setText("<html><center style='max-width:360px;'>" + msg + "</center></html>");
        errorLabel.setVisible(true);
        revalidate();
        repaint();
    }

    private void clearError() {
        errorLabel.setText("");
        errorLabel.setVisible(false);
    }
}
