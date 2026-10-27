package com.fooddelivery.ui.components;

import com.fooddelivery.ui.AppTheme;
import com.fooddelivery.ui.Icons;
import java.awt.*;
import java.awt.event.ActionListener;
import javax.swing.*;

/**
 * StatePanel: Displays Loading, Empty, or Error states with actions.
 */
public class StatePanel extends JPanel {

    private final JLabel iconLabel;
    private final JLabel titleLabel;
    private final JLabel descLabel;
    private final PrimaryButton actionBtn;

    public StatePanel() {
        super();
        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(40, 24, 40, 24));

        iconLabel = new JLabel();
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        titleLabel = new JLabel("", SwingConstants.CENTER);
        titleLabel.setFont(AppTheme.fontSection());
        titleLabel.setForeground(AppTheme.TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        descLabel = new JLabel("", SwingConstants.CENTER);
        descLabel.setFont(AppTheme.fontBody());
        descLabel.setForeground(AppTheme.TEXT_MUTED);
        descLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        actionBtn = new PrimaryButton("Retry");
        actionBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        actionBtn.setVisible(false);

        add(Box.createVerticalGlue());
        add(iconLabel);
        add(Box.createVerticalStrut(14));
        add(titleLabel);
        add(Box.createVerticalStrut(6));
        add(descLabel);
        add(Box.createVerticalStrut(18));
        add(actionBtn);
        add(Box.createVerticalGlue());
    }

    public void showLoading(String message) {
        iconLabel.setIcon(Icons.refresh(36, AppTheme.SERENITY_BLUE));
        titleLabel.setText(message != null ? message : "Loading...");
        descLabel.setText("Talking to MySQL database, please wait...");
        actionBtn.setVisible(false);
        revalidate();
        repaint();
    }

    public void showEmpty(String title, String description) {
        iconLabel.setIcon(Icons.orders(40, AppTheme.SERENITY_BLUE));
        titleLabel.setText(title);
        descLabel.setText(description);
        actionBtn.setVisible(false);
        revalidate();
        repaint();
    }

    public void showError(String message, Runnable onRetry) {
        iconLabel.setIcon(Icons.help(40, AppTheme.ERROR_RED));
        titleLabel.setText("Something went wrong");
        descLabel.setText("<html><center style='max-width:400px;'>" + message + "</center></html>");
        if (onRetry != null) {
            actionBtn.setText("Try Again");
            actionBtn.setIcon(Icons.refresh(14, AppTheme.BUTTERCREAM));
            for (ActionListener al : actionBtn.getActionListeners()) {
                actionBtn.removeActionListener(al);
            }
            actionBtn.addActionListener(e -> onRetry.run());
            actionBtn.setVisible(true);
        } else {
            actionBtn.setVisible(false);
        }
        revalidate();
        repaint();
    }
}
