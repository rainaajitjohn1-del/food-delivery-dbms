package com.fooddelivery.ui.views;

import com.fooddelivery.model.User;
import com.fooddelivery.service.FoodDeliveryService;
import com.fooddelivery.ui.AppTheme;
import com.fooddelivery.ui.Icons;
import com.fooddelivery.ui.components.PillChipButton;
import com.fooddelivery.ui.components.PrimaryButton;
import com.fooddelivery.ui.components.RoundCardPanel;
import com.fooddelivery.ui.components.RoundedTextField;
import java.awt.*;
import java.awt.geom.Path2D;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * RegisterView: Desktop split layout.
 * Left: Branded Espresso panel with typography and feature highlights sharing a single left edge.
 * Right: Warm off-white area with customer signup form card.
 */
public class RegisterView extends JPanel {

    private final FoodDeliveryService api;
    private final Consumer<User> onRegisterSuccess;
    private final Runnable onGoToLogin;

    private final RoundedTextField firstNameInput;
    private final RoundedTextField lastNameInput;
    private final RoundedTextField emailInput;
    private final RoundedTextField phoneInput;
    private final PrimaryButton registerBtn;
    private final JLabel errorLabel;

    public RegisterView(FoodDeliveryService api, Consumer<User> onRegisterSuccess, Runnable onGoToLogin) {
        super(new GridLayout(1, 2));
        this.api = api;
        this.onRegisterSuccess = onRegisterSuccess;
        this.onGoToLogin = onGoToLogin;

        // -------------------------------------------------------------
        // LEFT PANEL: Branded Espresso Showcase
        // -------------------------------------------------------------
        JPanel leftPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                // Reliable espresso fill
                Graphics2D g2 = (Graphics2D) g.create();
                AppTheme.enableAntialiasing(g2);
                int w = getWidth();
                int h = getHeight();

                g2.setColor(AppTheme.ESPRESSO);
                g2.fillRect(0, 0, w, h);

                // Subtle organic Serenity Blue curve at bottom corner (low alpha, stays below text)
                g2.setColor(new Color(0x8F, 0xA7, 0xC8, 25));
                Path2D.Double blob = new Path2D.Double();
                blob.moveTo(w * 0.35, h);
                blob.curveTo(w * 0.6, h * 0.88, w * 0.7, h * 0.82, w, h * 0.78);
                blob.lineTo(w, h);
                blob.closePath();
                g2.fill(blob);

                g2.dispose();
            }
        };
        leftPanel.setOpaque(true);
        leftPanel.setBackground(AppTheme.ESPRESSO);
        leftPanel.setBorder(new EmptyBorder(48, 48, 48, 36));

        JPanel leftContent = new JPanel();
        leftContent.setOpaque(false);
        leftContent.setLayout(new BoxLayout(leftContent, BoxLayout.Y_AXIS));

        // 1. Logo & Brand - all strictly left-aligned sharing one edge
        JLabel logo = new JLabel(Icons.heart(40, AppTheme.BUTTERCREAM));
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel brandName = new JLabel("Good Food");
        brandName.setFont(AppTheme.fontCardTitle());
        brandName.setForeground(AppTheme.SERENITY_BLUE);
        brandName.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel heroHeading = new JLabel("<html>Join the Food<br>Experience.</html>");
        heroHeading.setFont(AppTheme.fontHero());
        heroHeading.setForeground(AppTheme.BUTTERCREAM);
        heroHeading.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel heroSubtitle = new JLabel("<html><div style='width:280px;color:#DED0C0;line-height:1.3;'>Create a customer account to browse menus, place orders, and review food.</div></html>");
        heroSubtitle.setFont(AppTheme.fontBody());
        heroSubtitle.setForeground(new Color(0xDE, 0xD0, 0xC0));
        heroSubtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        leftContent.add(logo);
        leftContent.add(Box.createVerticalStrut(10));
        leftContent.add(brandName);
        leftContent.add(Box.createVerticalStrut(24));
        leftContent.add(heroHeading);
        leftContent.add(Box.createVerticalStrut(14));
        leftContent.add(heroSubtitle);
        leftContent.add(Box.createVerticalStrut(32));

        // 2. Checklist Props - share the exact same left alignment
        leftContent.add(createPropRow("Instant customer account creation"));
        leftContent.add(Box.createVerticalStrut(14));
        leftContent.add(createPropRow("Access all top-rated restaurants"));
        leftContent.add(Box.createVerticalStrut(14));
        leftContent.add(createPropRow("Transparent order workflow & discounts"));

        leftPanel.add(leftContent, BorderLayout.CENTER);
        add(leftPanel);

        // -------------------------------------------------------------
        // RIGHT PANEL: Customer Signup Card
        // -------------------------------------------------------------
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBackground(AppTheme.WARM_OFF_WHITE);

        // Card with equal 40px left/right and 36px top/bottom padding
        RoundCardPanel card = new RoundCardPanel(new BorderLayout());
        card.setPreferredSize(new Dimension(450, 490));
        card.setMinimumSize(new Dimension(400, 460));
        card.setBorder(new EmptyBorder(36, 40, 36, 40));

        // Form container: ALL children share LEFT_ALIGNMENT to avoid BoxLayout off-centering
        JPanel inner = new JPanel();
        inner.setOpaque(false);
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));

        // Symmetrical Top Row with Centered Title
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        topRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        topRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        PillChipButton backBtn = new PillChipButton("", Icons.back(18, AppTheme.ESPRESSO));
        backBtn.setPreferredSize(new Dimension(36, 36));
        backBtn.addActionListener(e -> {
            if (onGoToLogin != null) onGoToLogin.run();
        });
        topRow.add(backBtn, BorderLayout.WEST);

        JLabel title = new JLabel("Create Account", SwingConstants.CENTER);
        title.setFont(AppTheme.fontSection());
        title.setForeground(AppTheme.ESPRESSO);
        topRow.add(title, BorderLayout.CENTER);

        // Matching invisible spacer on the right so title stays 100% centered
        JLabel rightSpacer = new JLabel();
        rightSpacer.setPreferredSize(new Dimension(36, 36));
        topRow.add(rightSpacer, BorderLayout.EAST);

        inner.add(topRow);
        inner.add(Box.createVerticalStrut(20));

        // Form Fields
        // 1. Names Row
        JPanel namesRow = new JPanel(new GridLayout(1, 2, 12, 0));
        namesRow.setOpaque(false);
        namesRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        namesRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));

        firstNameInput = createInputField();
        lastNameInput = createInputField();

        namesRow.add(createFieldColumn("First Name", firstNameInput));
        namesRow.add(createFieldColumn("Last Name", lastNameInput));
        inner.add(namesRow);
        inner.add(Box.createVerticalStrut(12));

        // 2. Email Field
        emailInput = createInputField();
        JPanel emailCol = createFieldColumn("Email Address", emailInput);
        emailCol.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));
        inner.add(emailCol);
        inner.add(Box.createVerticalStrut(12));

        // 3. Mobile Phone Field
        phoneInput = createInputField();
        JPanel phoneCol = createFieldColumn("Mobile Phone (10 digits)", phoneInput);
        phoneCol.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));
        inner.add(phoneCol);

        // Error message banner
        errorLabel = new JLabel("", SwingConstants.LEFT);
        errorLabel.setFont(AppTheme.fontSmallBold());
        errorLabel.setForeground(AppTheme.ERROR_RED);
        errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        errorLabel.setVisible(false);
        inner.add(errorLabel);

        inner.add(Box.createVerticalStrut(18));

        // Register Button
        registerBtn = new PrimaryButton("Create Account");
        registerBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        registerBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        registerBtn.addActionListener(e -> performRegister());
        inner.add(registerBtn);
        inner.add(Box.createVerticalStrut(16));

        // Link back to login - LEFT_ALIGNMENT with full column width so it centers under button
        JPanel backToLogin = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        backToLogin.setOpaque(false);
        backToLogin.setAlignmentX(Component.LEFT_ALIGNMENT);
        backToLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));

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
        JPanel p = new JPanel(new BorderLayout(10, 0));
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(360, 40));

        JLabel dot = new JLabel(Icons.check(14, AppTheme.SERENITY_BLUE));
        dot.setVerticalAlignment(SwingConstants.CENTER);

        JLabel lbl = new JLabel("<html><div style='width:260px;color:#F3E3C0;line-height:1.2;'>" + text + "</div></html>");
        lbl.setFont(AppTheme.fontBody());
        lbl.setForeground(AppTheme.BUTTERCREAM);

        p.add(dot, BorderLayout.WEST);
        p.add(lbl, BorderLayout.CENTER);
        return p;
    }

    private RoundedTextField createInputField() {
        return new RoundedTextField("", 12);
    }

    private JPanel createFieldColumn(String labelText, JComponent input) {
        JPanel col = new JPanel(new BorderLayout(0, 4));
        col.setOpaque(false);
        col.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lbl = new JLabel(labelText);
        lbl.setFont(AppTheme.fontSmallBold());
        lbl.setForeground(AppTheme.TEXT_PRIMARY);
        col.add(lbl, BorderLayout.NORTH);
        col.add(input, BorderLayout.CENTER);
        return col;
    }

    private void performRegister() {
        String first = firstNameInput.getText().trim();
        String last = lastNameInput.getText().trim();
        String email = emailInput.getText().trim();
        String phone = phoneInput.getText().trim();

        if (first.isEmpty() || last.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            showError("Please fill in all registration fields.");
            return;
        }

        clearError();
        registerBtn.setEnabled(false);
        registerBtn.setText("Registering...");

        SwingWorker<User, Void> worker = new SwingWorker<>() {
            @Override
            protected User doInBackground() throws Exception {
                // Register customer and immediately log in
                api.registerCustomer(first, last, email, phone);
                return api.login(email);
            }

            @Override
            protected void done() {
                registerBtn.setEnabled(true);
                registerBtn.setText("Create Account");
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
        errorLabel.setText("<html><div style='max-width:320px;color:#D32F2F;'>" + msg + "</div></html>");
        errorLabel.setVisible(true);
        revalidate();
        repaint();
    }

    private void clearError() {
        errorLabel.setText("");
        errorLabel.setVisible(false);
    }
}
