package com.fooddelivery.ui.views;

import com.fooddelivery.model.MenuItem;
import com.fooddelivery.model.Restaurant;
import com.fooddelivery.model.User;
import com.fooddelivery.service.FoodDeliveryService;
import com.fooddelivery.ui.AppTheme;
import com.fooddelivery.ui.Icons;
import com.fooddelivery.ui.components.PrimaryButton;
import com.fooddelivery.ui.components.RoundCardPanel;
import com.fooddelivery.ui.components.RoundedComboBox;
import com.fooddelivery.ui.components.RoundedTextField;
import com.fooddelivery.ui.components.SecondaryButton;
import com.fooddelivery.ui.components.StatePanel;
import com.fooddelivery.ui.components.StyledTable;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

/**
 * MenuManagerView: Manager screen for complete Menu CRUD operations
 * (addMenuItem, updateMenuItem, deleteMenuItem).
 */
public class MenuManagerView extends JPanel {

    private final FoodDeliveryService api;
    private final User currentUser;
    private final Frame mainFrame;

    private Restaurant myRestaurant;
    private final DefaultTableModel tableModel;
    private final StyledTable table;
    private final StatePanel statePanel;
    private final JScrollPane tableScroll;
    private final List<MenuItem> menuItems = new ArrayList<>();
    private final JLabel headerSubtitle;

    public MenuManagerView(FoodDeliveryService api, User currentUser, Frame mainFrame) {
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

        JLabel title = new JLabel("Menu Management");
        title.setFont(AppTheme.fontHero());
        title.setForeground(AppTheme.ESPRESSO);

        headerSubtitle = new JLabel("Loading restaurant information...");
        headerSubtitle.setFont(AppTheme.fontBody());
        headerSubtitle.setForeground(AppTheme.TEXT_MUTED);

        titles.add(title);
        titles.add(Box.createVerticalStrut(4));
        titles.add(headerSubtitle);
        header.add(titles, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);

        SecondaryButton refreshBtn = new SecondaryButton("Refresh");
        refreshBtn.setIcon(Icons.refresh(14, Color.WHITE));
        refreshBtn.addActionListener(e -> loadMenuData());
        actions.add(refreshBtn);

        PrimaryButton addBtn = new PrimaryButton("+ Add Menu Item");
        addBtn.addActionListener(e -> showItemDialog(null));
        actions.add(addBtn);

        header.add(actions, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Center card table
        RoundCardPanel card = new RoundCardPanel(new BorderLayout());
        card.setBorder(new EmptyBorder(12, 12, 12, 12));

        String[] cols = {"Item ID", "Dish Name", "Category", "Price (₹)"};
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

        // Bottom Action Bar: Edit & Delete
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 8));
        bottomBar.setOpaque(false);

        SecondaryButton editBtn = new SecondaryButton("Edit Selected");
        editBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0 || row >= menuItems.size()) {
                JOptionPane.showMessageDialog(this, "Please select an item to edit.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            showItemDialog(menuItems.get(row));
        });
        bottomBar.add(editBtn);

        JButton deleteBtn = new JButton("Delete Item") {
            private boolean isHovered = false;
            private boolean isPressed = false;
            {
                setFont(AppTheme.fontBodyBold());
                setForeground(AppTheme.ERROR_RED);
                setContentAreaFilled(false);
                setBorderPainted(false);
                setFocusPainted(false);
                setOpaque(false);
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                addFocusListener(new java.awt.event.FocusAdapter() {
                    @Override
                    public void focusGained(java.awt.event.FocusEvent e) { repaint(); }
                    @Override
                    public void focusLost(java.awt.event.FocusEvent e) { repaint(); }
                });
                addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseEntered(java.awt.event.MouseEvent e) { if (isEnabled()) { isHovered = true; repaint(); } }
                    @Override
                    public void mouseExited(java.awt.event.MouseEvent e) { isHovered = false; isPressed = false; repaint(); }
                    @Override
                    public void mousePressed(java.awt.event.MouseEvent e) { if (isEnabled()) { isPressed = true; repaint(); } }
                    @Override
                    public void mouseReleased(java.awt.event.MouseEvent e) { isPressed = false; repaint(); }
                });
            }

            @Override
            public Insets getInsets() {
                return new Insets(8, 18, 8, 18);
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                AppTheme.enableAntialiasing(g2);
                int w = getWidth();
                int h = getHeight();
                int r = h;

                if (!isEnabled()) {
                    g2.setColor(new Color(0xF4, 0xEE, 0xEE));
                } else if (isPressed) {
                    g2.setColor(new Color(0xF8, 0xCE, 0xCE));
                } else if (isHovered) {
                    g2.setColor(new Color(0xFD, 0xEE, 0xEE));
                } else {
                    g2.setColor(AppTheme.ERROR_TINT);
                }
                g2.fillRoundRect(1, 1, w - 2, h - 2, r, r);

                if (hasFocus()) {
                    g2.setColor(AppTheme.SERENITY_BLUE);
                    g2.setStroke(new BasicStroke(1.8f));
                    g2.drawRoundRect(1, 1, w - 3, h - 3, r, r);
                } else {
                    g2.setColor(AppTheme.ERROR_RED);
                    g2.setStroke(new BasicStroke(1.2f));
                    g2.drawRoundRect(1, 1, w - 2, h - 2, r, r);
                }

                FontMetrics fm = g2.getFontMetrics(getFont());
                int strW = fm.stringWidth(getText());
                int x = (w - strW) / 2;
                int y = (h + fm.getAscent() - fm.getDescent()) / 2;

                g2.setFont(getFont());
                g2.setColor(isEnabled() ? AppTheme.ERROR_RED : AppTheme.TEXT_MUTED);
                g2.drawString(getText(), x, y);

                g2.dispose();
            }
        };
        deleteBtn.addActionListener(e -> performDeleteItem());
        bottomBar.add(deleteBtn);

        card.add(bottomBar, BorderLayout.SOUTH);
        add(card, BorderLayout.CENTER);

        loadMenuData();
    }

    private void loadMenuData() {
        tableScroll.setVisible(false);
        statePanel.setVisible(true);
        statePanel.showLoading("Loading restaurant and menu...");

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                myRestaurant = api.getMyRestaurant(currentUser.userId());
                menuItems.clear();
                if (myRestaurant != null) {
                    menuItems.addAll(api.getMenu(myRestaurant.restaurantId()));
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    if (myRestaurant != null) {
                        headerSubtitle.setText("Managing menu for: " + myRestaurant.name());
                    }
                    statePanel.setVisible(false);
                    tableScroll.setVisible(true);
                    renderTable();
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    statePanel.showError(cause.getMessage(), MenuManagerView.this::loadMenuData);
                }
            }
        };
        worker.execute();
    }

    private void renderTable() {
        tableModel.setRowCount(0);
        if (menuItems.isEmpty()) {
            tableScroll.setVisible(false);
            statePanel.setVisible(true);
            statePanel.showEmpty("No menu items yet", "Click '+ Add Menu Item' above to create your first dish.");
            return;
        }

        for (MenuItem item : menuItems) {
            tableModel.addRow(new Object[]{
                    "#" + item.itemId(),
                    item.itemName(),
                    item.category(),
                    String.format("₹%.2f", item.price())
            });
        }
    }

    private void showItemDialog(MenuItem existing) {
        if (myRestaurant == null) {
            JOptionPane.showMessageDialog(this, "No restaurant found for this manager account.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean isEdit = (existing != null);
        JDialog dialog = new JDialog(mainFrame, isEdit ? "Edit Menu Item" : "Add Menu Item", true);
        dialog.setSize(440, 360);
        dialog.setLocationRelativeTo(mainFrame);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(AppTheme.WARM_OFF_WHITE);

        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(24, 28, 24, 28));

        RoundedTextField nameField = new RoundedTextField(isEdit ? existing.itemName() : "", 12);
        nameField.setPreferredSize(new Dimension(380, 40));
        nameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        RoundedComboBox<String> catBox = new RoundedComboBox<>(FoodDeliveryService.CATEGORIES);
        catBox.setPreferredSize(new Dimension(380, 40));
        catBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        if (isEdit) catBox.setSelectedItem(existing.category());

        RoundedTextField priceField = new RoundedTextField(isEdit ? String.valueOf(existing.price()) : "", 12);
        priceField.setPreferredSize(new Dimension(380, 40));
        priceField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        p.add(new JLabel("Dish Name:"));
        p.add(Box.createVerticalStrut(4));
        p.add(nameField);
        p.add(Box.createVerticalStrut(12));

        p.add(new JLabel("Category:"));
        p.add(Box.createVerticalStrut(4));
        p.add(catBox);
        p.add(Box.createVerticalStrut(12));

        p.add(new JLabel("Price (₹):"));
        p.add(Box.createVerticalStrut(4));
        p.add(priceField);
        p.add(Box.createVerticalStrut(20));

        PrimaryButton saveBtn = new PrimaryButton(isEdit ? "Update Dish" : "Create Dish");
        saveBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        saveBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String cat = (String) catBox.getSelectedItem();
            double price;
            try {
                price = Double.parseDouble(priceField.getText().trim());
                if (price <= 0) throw new NumberFormatException();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Please enter a valid positive price.", "Invalid Price", JOptionPane.WARNING_MESSAGE);
                return;
            }

            SwingWorker<Void, Void> w = new SwingWorker<>() {
                @Override
                protected Void doInBackground() throws Exception {
                    if (isEdit) {
                        api.updateMenuItem(existing.itemId(), name, price, cat);
                    } else {
                        api.addMenuItem(myRestaurant.restaurantId(), name, price, cat);
                    }
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get();
                        dialog.dispose();
                        loadMenuData();
                    } catch (Exception ex) {
                        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                        JOptionPane.showMessageDialog(dialog, cause.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            w.execute();
        });

        p.add(saveBtn);
        dialog.add(p, BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    private void performDeleteItem() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= menuItems.size()) {
            JOptionPane.showMessageDialog(this, "Please select an item to delete.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        MenuItem item = menuItems.get(row);
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete '" + item.itemName() + "'?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (confirm != JOptionPane.YES_OPTION) return;

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                api.deleteMenuItem(item.itemId());
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    loadMenuData();
                    JOptionPane.showMessageDialog(MenuManagerView.this, "Item deleted successfully.");
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    JOptionPane.showMessageDialog(MenuManagerView.this, cause.getMessage(), "Cannot Delete", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
}
