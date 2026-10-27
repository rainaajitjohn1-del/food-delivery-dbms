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
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.Path2D;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * LoginView: Desktop split layout.
 * Left: Espresso branded showcase panel with typography and feature highlights sharing a single left edge.
 * Right: Warm off-white area with centered authentication form card and demo logins.
 */
public class LoginView extends JPanel {

    private final FoodDeliveryService api;
    private final Consumer<User> onLoginSuccess;
    private final Runnable onGoToRegister;

    private final RoundedTextField emailInput;
    private final PrimaryButton signInBtn;
    private final JLabel errorLabel;

    public LoginView(FoodDeliveryService api, Consumer<User> onLoginSuccess, Runnable onGoToRegister) {
        super(new GridLayout(1, 2));
        this.api = api;
        this.onLoginSuccess = onLoginSuccess;
        this.onGoToRegister = onGoToRegister;

        // -------------------------------------------------------------
        // LEFT PANEL: Espresso Branding Showcase
        // -------------------------------------------------------------
        JPanel leftPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                AppTheme.enableAntialiasing(g2);
                int w = getWidth();
                int h = getHeight();

                // Solid espresso fill
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

        JLabel heroHeading = new JLabel("<html>Good Food<br>Better Days.</html>");
        heroHeading.setFont(AppTheme.fontHero());
        heroHeading.setForeground(AppTheme.BUTTERCREAM);
        heroHeading.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel heroSubtitle = new JLabel("<html><div style='width:280px;color:#DED0C0;line-height:1.3;'>Fresh meals, happy moments.<br>Delivered directly to your door.</div></html>");
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
        leftContent.add(createPropRow("Multi-restaurant desktop ordering"));
        leftContent.add(Box.createVerticalStrut(14));
        leftContent.add(createPropRow("Real-time manager order workflow"));
        leftContent.add(Box.createVerticalStrut(14));
        leftContent.add(createPropRow("Fleet assignment & live delivery tracking"));

        leftPanel.add(leftContent, BorderLayout.CENTER);
        add(leftPanel);

        // -------------------------------------------------------------
        // RIGHT PANEL: Authentication Form Card
        // -------------------------------------------------------------
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBackground(AppTheme.WARM_OFF_WHITE);

        // Card with equal 40px left/right and 36px top/bottom padding
        RoundCardPanel card = new RoundCardPanel(new BorderLayout());
        card.setPreferredSize(new Dimension(450, 480));
        card.setMinimumSize(new Dimension(400, 460));
        card.setBorder(new EmptyBorder(36, 40, 36, 40));

        // Form container: ALL children share LEFT_ALIGNMENT to avoid BoxLayout off-centering
        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        // 1. Title & Subtitle
        JLabel title = new JLabel("Sign In");
        title.setFont(AppTheme.fontSection());
        title.setForeground(AppTheme.ESPRESSO);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Sign in with your registered email.");
        subtitle.setFont(AppTheme.fontBody());
        subtitle.setForeground(AppTheme.TEXT_MUTED);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        form.add(title);
        form.add(Box.createVerticalStrut(6));
        form.add(subtitle);
        form.add(Box.createVerticalStrut(24)); // 24px between subtitle and presets

        // 2. Test Login Presets - exact same left edge and width
        JLabel demoTitle = new JLabel("Test Login Presets (Click to autofill):");
        demoTitle.setFont(AppTheme.fontSmallBold());
        demoTitle.setForeground(AppTheme.TEXT_MUTED);
        demoTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(demoTitle);
        form.add(Box.createVerticalStrut(8));

        // GridLayout ensures buttons start at x=0 (no FlowLayout indentation) and span full column width
        JPanel chips = new JPanel(new GridLayout(1, 3, 10, 0));
        chips.setOpaque(false);
        chips.setAlignmentX(Component.LEFT_ALIGNMENT);
        chips.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        chips.add(createDemoChip("Customer", "arjun.nair@gmail.com"));
        chips.add(createDemoChip("Manager", "rahul.menon@spicehub.in"));
        chips.add(createDemoChip("Agent", "imran.khan@fooddel.in"));
        form.add(chips);
        form.add(Box.createVerticalStrut(20)); // 20px between presets and Email field

        // 3. Email Form Field - aligned exactly flush with field
        JPanel emailCol = new JPanel(new BorderLayout(0, 4));
        emailCol.setOpaque(false);
        emailCol.setAlignmentX(Component.LEFT_ALIGNMENT);
        emailCol.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));

        JLabel emailLbl = new JLabel("Email Address");
        emailLbl.setFont(AppTheme.fontBodyBold());
        emailLbl.setForeground(AppTheme.TEXT_PRIMARY);

        emailInput = new RoundedTextField("Enter your email address", 12);
        emailInput.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    performLogin();
                }
            }
        });

        emailCol.add(emailLbl, BorderLayout.NORTH);
        emailCol.add(emailInput, BorderLayout.CENTER);
        form.add(emailCol);

        // Error message banner
        errorLabel = new JLabel("", SwingConstants.LEFT);
        errorLabel.setFont(AppTheme.fontSmallBold());
        errorLabel.setForeground(AppTheme.ERROR_RED);
        errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        errorLabel.setVisible(false);
        form.add(errorLabel);

        form.add(Box.createVerticalStrut(20)); // 20px between field and Sign In button

        // 4. Sign In Button
        signInBtn = new PrimaryButton("Sign In");
        signInBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        signInBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        signInBtn.addActionListener(e -> performLogin());
        form.add(signInBtn);
        form.add(Box.createVerticalStrut(16)); // 16px between button and footer

        // 5. Footer Row ("New to Good Food? Create account")
        // Uses LEFT_ALIGNMENT with full column width so BoxLayout doesn't offset other components,
        // while FlowLayout.CENTER centers the link under the button.
        JPanel regRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        regRow.setOpaque(false);
        regRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        regRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));

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

    private PillChipButton createDemoChip(String role, String email) {
        PillChipButton btn = new PillChipButton(role);
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
