package com.fooddelivery.ui.views;

import com.fooddelivery.model.User;
import com.fooddelivery.service.FoodDeliveryService;
import com.fooddelivery.ui.AppTheme;
import com.fooddelivery.ui.Icons;
import com.fooddelivery.ui.components.PillBadge;
import com.fooddelivery.ui.components.PrimaryButton;
import com.fooddelivery.ui.components.RoundCardPanel;
import com.fooddelivery.ui.components.SecondaryButton;
import java.awt.*;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * ProfileView: Left pane with user avatar and credentials, phone number addition;
 * Right pane with navigational cards (My Orders, Addresses, Payment, Help).
 */
public class ProfileView extends JPanel {

    private final FoodDeliveryService api;
    private User currentUser;
    private final Consumer<String> onNavigate;

    private final JPanel phonesBox;
    private final JTextField newPhoneInput;

    public ProfileView(FoodDeliveryService api, User currentUser, Consumer<String> onNavigate) {
        super(new BorderLayout());
        this.api = api;
        this.currentUser = currentUser;
        this.onNavigate = onNavigate;

        setBackground(AppTheme.WARM_OFF_WHITE);
        setBorder(new EmptyBorder(28, 36, 36, 36));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 24, 0));

        JLabel title = new JLabel("My Account");
        title.setFont(AppTheme.fontHero());
        title.setForeground(AppTheme.ESPRESSO);
        header.add(title, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        // 2-Pane Desktop Layout
        JPanel split = new JPanel(new GridLayout(1, 2, 28, 0));
        split.setOpaque(false);

        // LEFT PANE: Profile Credentials & Phones
        RoundCardPanel leftCard = new RoundCardPanel(new BorderLayout(0, 16));
        leftCard.setBorder(new EmptyBorder(28, 28, 28, 28));

        JPanel leftContent = new JPanel();
        leftContent.setOpaque(false);
        leftContent.setLayout(new BoxLayout(leftContent, BoxLayout.Y_AXIS));

        // Big Avatar Circle
        String initial = currentUser.firstName().substring(0, 1);
        JLabel avatar = new JLabel(initial, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                AppTheme.enableAntialiasing(g2);
                g2.setColor(AppTheme.BUTTERCREAM);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        avatar.setPreferredSize(new Dimension(72, 72));
        avatar.setFont(new Font(AppTheme.fontHero().getName(), Font.BOLD, 30));
        avatar.setForeground(AppTheme.ESPRESSO);
        avatar.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel nameLbl = new JLabel(currentUser.fullName());
        nameLbl.setFont(AppTheme.fontSection());
        nameLbl.setForeground(AppTheme.TEXT_PRIMARY);

        JLabel emailLbl = new JLabel(currentUser.email());
        emailLbl.setFont(AppTheme.fontBody());
        emailLbl.setForeground(AppTheme.TEXT_MUTED);

        PillBadge roleBadge = new PillBadge(currentUser.role(), AppTheme.SERENITY_BLUE_LIGHT, AppTheme.SERENITY_BLUE_HOVER);

        leftContent.add(avatar);
        leftContent.add(Box.createVerticalStrut(14));
        leftContent.add(nameLbl);
        leftContent.add(Box.createVerticalStrut(4));
        leftContent.add(emailLbl);
        leftContent.add(Box.createVerticalStrut(10));
        leftContent.add(roleBadge);
        leftContent.add(Box.createVerticalStrut(24));
        leftContent.add(new JSeparator(SwingConstants.HORIZONTAL));
        leftContent.add(Box.createVerticalStrut(18));

        // Phone numbers section
        JLabel phoneTitle = new JLabel("Registered Phone Numbers:");
        phoneTitle.setFont(AppTheme.fontBodyBold());
        phoneTitle.setForeground(AppTheme.ESPRESSO);
        leftContent.add(phoneTitle);
        leftContent.add(Box.createVerticalStrut(10));

        phonesBox = new JPanel();
        phonesBox.setOpaque(false);
        phonesBox.setLayout(new BoxLayout(phonesBox, BoxLayout.Y_AXIS));
        renderPhones();
        leftContent.add(phonesBox);
        leftContent.add(Box.createVerticalStrut(16));

        // Add extra phone row
        JPanel addPhoneRow = new JPanel(new BorderLayout(8, 0));
        addPhoneRow.setOpaque(false);
        addPhoneRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        newPhoneInput = new JTextField();
        newPhoneInput.setFont(AppTheme.fontBody());
        newPhoneInput.setBackground(AppTheme.BUTTERCREAM);
        newPhoneInput.setForeground(AppTheme.TEXT_PRIMARY);
        newPhoneInput.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.BORDER_LIGHT, 1),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        newPhoneInput.setToolTipText("Enter 10-digit mobile");

        JButton addPhoneBtn = new JButton("+ Add Phone");
        addPhoneBtn.setFont(AppTheme.fontSmallBold());
        addPhoneBtn.setForeground(AppTheme.TEXT_WHITE);
        addPhoneBtn.setBackground(AppTheme.SERENITY_BLUE);
        addPhoneBtn.setFocusPainted(false);
        addPhoneBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addPhoneBtn.addActionListener(e -> performAddPhone());

        addPhoneRow.add(newPhoneInput, BorderLayout.CENTER);
        addPhoneRow.add(addPhoneBtn, BorderLayout.EAST);
        leftContent.add(addPhoneRow);

        leftCard.add(leftContent, BorderLayout.NORTH);
        split.add(leftCard);

        // RIGHT PANE: Navigational & Setting Cards
        JPanel rightCol = new JPanel();
        rightCol.setOpaque(false);
        rightCol.setLayout(new BoxLayout(rightCol, BoxLayout.Y_AXIS));

        if ("CUSTOMER".equalsIgnoreCase(currentUser.role())) {
            rightCol.add(createNavCard("My Orders", "View recent receipts, review past deliveries", Icons.orders(20, AppTheme.ESPRESSO), () -> {
                if (onNavigate != null) onNavigate.accept("orders");
            }));
            rightCol.add(Box.createVerticalStrut(16));
        }

        rightCol.add(createNavCard("Saved Delivery Addresses", "Manage your delivery locations and cities", Icons.location(20, AppTheme.ESPRESSO), () -> {
            JOptionPane.showMessageDialog(this, "Addresses can be selected or added directly during Checkout.", "Addresses", JOptionPane.INFORMATION_MESSAGE);
        }));
        rightCol.add(Box.createVerticalStrut(16));

        rightCol.add(createNavCard("Payment Methods", "UPI, Debit/Credit Card, and Cash on Delivery (COD) supported", Icons.card(20, AppTheme.ESPRESSO), () -> {
            JOptionPane.showMessageDialog(this, "All standard payments (UPI, Cards, Cash on Delivery) are active.", "Payment Methods", JOptionPane.INFORMATION_MESSAGE);
        }));
        rightCol.add(Box.createVerticalStrut(16));

        rightCol.add(createNavCard("Help & Support", "Need assistance with your delivery or order?", Icons.help(20, AppTheme.ESPRESSO), () -> {
            JOptionPane.showMessageDialog(this, "Food Delivery Support:\nEmail: support@fooddelivery.in\nPhone: 1800-FOOD-DEL", "Support", JOptionPane.INFORMATION_MESSAGE);
        }));

        split.add(rightCol);
        add(split, BorderLayout.CENTER);
    }

    private void renderPhones() {
        phonesBox.removeAll();
        if (currentUser.phones() == null || currentUser.phones().isEmpty()) {
            JLabel none = new JLabel("No phones registered yet.");
            none.setFont(AppTheme.fontBody());
            none.setForeground(AppTheme.TEXT_MUTED);
            phonesBox.add(none);
        } else {
            for (String p : currentUser.phones()) {
                JLabel item = new JLabel("•  " + p);
                item.setFont(AppTheme.fontBody());
                item.setForeground(AppTheme.TEXT_PRIMARY);
                phonesBox.add(item);
                phonesBox.add(Box.createVerticalStrut(4));
            }
        }
        phonesBox.revalidate();
        phonesBox.repaint();
    }

    private JPanel createNavCard(String title, String subtitle, Icon icon, Runnable onClick) {
        RoundCardPanel card = new RoundCardPanel(new BorderLayout(14, 0));
        card.setBorder(new EmptyBorder(16, 20, 16, 20));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        card.add(new JLabel(icon), BorderLayout.WEST);

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));

        JLabel t = new JLabel(title);
        t.setFont(AppTheme.fontCardTitle());
        t.setForeground(AppTheme.TEXT_PRIMARY);

        JLabel s = new JLabel(subtitle);
        s.setFont(AppTheme.fontSmall());
        s.setForeground(AppTheme.TEXT_MUTED);

        text.add(t);
        text.add(Box.createVerticalStrut(3));
        text.add(s);
        card.add(text, BorderLayout.CENTER);

        JLabel arrow = new JLabel("›");
        arrow.setFont(new Font(AppTheme.fontHero().getName(), Font.BOLD, 22));
        arrow.setForeground(AppTheme.TEXT_MUTED);
        card.add(arrow, BorderLayout.EAST);

        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (onClick != null) onClick.run();
            }
        });

        return card;
    }

    private void performAddPhone() {
        String phone = newPhoneInput.getText().trim();
        if (phone.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a valid 10-digit mobile number.", "Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                api.addPhone(currentUser.userId(), phone);
                // Refresh user object
                currentUser = api.login(currentUser.email());
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    newPhoneInput.setText("");
                    renderPhones();
                    JOptionPane.showMessageDialog(ProfileView.this, "Phone number saved successfully!");
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    JOptionPane.showMessageDialog(ProfileView.this, cause.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
}
