package com.fooddelivery.ui;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Entry point for the Food Delivery desktop application.
 */
public class FoodDeliveryApp {

    public static void main(String[] args) {
        // Apply clean system properties for crisp rendering on macOS and modern displays
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("apple.awt.application.name", "Good Food");
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) { }

        // Initialize custom Plus Jakarta Sans fonts with SansSerif fallback
        AppTheme.initFonts();

        // Launch UI on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
