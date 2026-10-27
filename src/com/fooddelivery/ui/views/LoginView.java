package com.fooddelivery.ui.views;

import com.fooddelivery.model.User;
import com.fooddelivery.service.FoodDeliveryService;
import com.fooddelivery.ui.AppTheme;
import com.fooddelivery.ui.Icons;
import com.fooddelivery.ui.components.PrimaryButton;
import com.fooddelivery.ui.components.RoundCardPanel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.Path2D;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * LoginView: Desktop split layout.
 * Left: Espresso branded showcase panel with typography and decorative accents.
 * Right: Warm off-white area with centered authentication form card and demo logins.
 */
public class LoginView extends JPanel {

    private final FoodDeliveryService api;
    private final Consumer<User> onLoginSuccess;
    private final Runnable onGoToRegister;

    private final JTextField emailInput;
    private final PrimaryButton signInBtn;
    private final JLabel errorLabel;

    public LoginView(FoodDeliveryService api, Consumer<User> onLoginSuccess, Runnable onGoToRegister) {
        super(new GridLayout(1, 2));
        this.api = api;
        this.onLoginSuccess = onLoginSuccess;
        this.onGoToRegister = onGoToRegister;

        // LEFT PANEL: Espresso Branding Showcase
        JPanel leftPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                AppTheme.enableAntialiasing(g2);
                int w = getWidth();
                int h = getHeight();

                // Espresso base
                g2.setColor(AppTheme.ESPRESSO);
                g2.fillRect(0, 0, w, h);

                // Organic Serenity Blue curved background blob
                g2.setColor(new Color(0x8F, 0xA7, 0xC8, 45));
                Path2D.Double blob = new Path2D.Double();
                blob.moveTo(w * 0.2, h);
                blob.curveTo(w * 0.5, h * 0.6, w * 0.3, h * 0.2, w, h * 0.1);
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

        JLabel heroHeading = new JLabel("<html>Good Food<br>Better Days.</html>");
        heroHeading.setFont(AppTheme.fontHero());
        heroHeading.setForeground(AppTheme.BUTTERCREAM);

        JLabel heroSubtitle = new JLabel("<html>Fresh meals, happy moments.<br>Delivered directly to your door.</html>");
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

        // Value props
        leftContent.add(createPropRow("Multi-restaurant desktop ordering"));
        leftContent.add(Box.createVerticalStrut(12));
        leftContent.add(createPropRow("Real-time manager order workflow"));
        leftContent.add(Box.createVerticalStrut(12));
        leftContent.add(createPropRow("Fleet assignment & live delivery tracking"));

        leftPanel.add(leftContent, BorderLayout.CENTER);
        add(leftPanel);

        // RIGHT PANEL: Form Card on Warm Off-White
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBackground(AppTheme.WARM_OFF_WHITE);

        RoundCardPanel card = new RoundCardPanel(new BorderLayout());
        card.setPreferredSize(new Dimension(440, 520));
        card.setMinimumSize(new Dimension(400, 480));
        card.setBorder(new EmptyBorder(32, 36, 32, 36));

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Sign In");
        title.setFont(AppTheme.fontSection());
        title.setForeground(AppTheme.ESPRESSO);

        JLabel subtitle = new JLabel("Access your customer, manager, or fleet portal");
        subtitle.setFont(AppTheme.fontBody());
        subtitle.setForeground(AppTheme.TEXT_MUTED);

        form.add(title);
        form.add(Box.createVerticalStrut(4));
        form.add(subtitle);
        form.add(Box.createVerticalStrut(22));

        // Quick demo test accounts
        JLabel demoTitle = new JLabel("Test Login Presets (Click to fill):");
        demoTitle.setFont(AppTheme.fontSmallBold());
        demoTitle.setForeground(AppTheme.TEXT_MUTED);
        form.add(demoTitle);
        form.add(Box.createVerticalStrut(8));

        JPanel chips = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        chips.setOpaque(false);
        chips.add(createDemoChip("Customer", "arjun.nair@gmail.com"));
        chips.add(createDemoChip("Manager", "rahul.menon@spicehub.in"));
        chips.add(createDemoChip("Agent", "imran.khan@fooddel.in"));
        form.add(chips);
        form.add(Box.createVerticalStrut(20));

        // Email Form
        JLabel emailLbl = new JLabel("Email Address");
        emailLbl.setFont(AppTheme.fontBodyBold());
        emailLbl.setForeground(AppTheme.TEXT_PRIMARY);

        emailInput = new JTextField();
        emailInput.setFont(AppTheme.fontBody());
        emailInput.setForeground(AppTheme.TEXT_PRIMARY);
        emailInput.setBackground(AppTheme.BUTTERCREAM);
        emailInput.setCaretColor(AppTheme.TEXT_PRIMARY);
        emailInput.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.BORDER_LIGHT, 1),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));
        emailInput.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        emailInput.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    performLogin();
                }
            }
        });

        form.add(emailLbl);
        form.add(Box.createVerticalStrut(6));
        form.add(emailInput);
        form.add(Box.createVerticalStrut(14));

        // Error message banner
        errorLabel = new JLabel("", SwingConstants.CENTER);
        errorLabel.setFont(AppTheme.fontSmallBold());
        errorLabel.setForeground(AppTheme.ERROR_RED);
        errorLabel.setVisible(false);
        form.add(errorLabel);
        form.add(Box.createVerticalStrut(12));

        // Sign In Button
        signInBtn = new PrimaryButton("Sign In");
        signInBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        signInBtn.addActionListener(e -> performLogin());
        form.add(signInBtn);
        form.add(Box.createVerticalStrut(20));

        // Register link
        JPanel regRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        regRow.setOpaque(false);
        JLabel noAcc = new JLabel("New to Good Food?");
        noAcc.setFont(AppTheme.fontBody());
        noAcc.setForeground(AppTheme.TEXT_MUTED);

        JButton regLink = new JButton("Create account");
        regLink.setFont(AppTheme.fontBodyBold());
        regLink.setForeground(AppTheme.SERENITY_BLUE);
        regLink.setBorderPainted(false);
        regLink.setContentAreaFilled(false);
        regLink.setFocusPainted(false);
        regLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        regLink.addActionListener(e -> {
            if (onGoToRegister != null) onGoToRegister.run();
        });

        regRow.add(noAcc);
        regRow.add(regLink);
        form.add(regRow);

        card.add(form, BorderLayout.CENTER);
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

    private JButton createDemoChip(String role, String email) {
        JButton btn = new JButton(role);
        btn.setFont(AppTheme.fontSmallBold());
        btn.setForeground(AppTheme.ESPRESSO);
        btn.setBackground(AppTheme.BUTTERCREAM);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.SERENITY_BLUE, 1),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> {
            emailInput.setText(email);
            clearError();
        });
        return btn;
    }

    private void performLogin() {
        String email = emailInput.getText().trim();
        if (email.isEmpty()) {
            showError("Please enter your email address.");
            return;
        }

        clearError();
        signInBtn.setEnabled(false);
        signInBtn.setText("Signing in...");

        SwingWorker<User, Void> worker = new SwingWorker<>() {
            @Override
            protected User doInBackground() throws Exception {
                return api.login(email);
            }

            @Override
            protected void done() {
                signInBtn.setEnabled(true);
                signInBtn.setText("Sign In");
                try {
                    User user = get();
                    if (onLoginSuccess != null) {
                        onLoginSuccess.accept(user);
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
        errorLabel.setText("<html><center style='max-width:320px;'>" + msg + "</center></html>");
        errorLabel.setVisible(true);
        revalidate();
        repaint();
    }

    private void clearError() {
        errorLabel.setText("");
        errorLabel.setVisible(false);
    }
}
