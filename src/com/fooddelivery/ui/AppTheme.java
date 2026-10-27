package com.fooddelivery.ui;

import java.awt.*;
import java.io.File;
import java.io.InputStream;
import javax.swing.*;

/**
 * Design system tokens, color palettes, typography and UI constants
 * for the Food Delivery desktop application.
 */
public final class AppTheme {

    private AppTheme() {}

    // ------------------------------------------------------------- Palette
    public static final Color ESPRESSO        = new Color(0x3B, 0x24, 0x18); // Pantone 19-1110 TCX
    public static final Color ESPRESSO_HOVER  = new Color(0x4E, 0x30, 0x20);
    public static final Color ESPRESSO_DARK   = new Color(0x28, 0x18, 0x10);

    public static final Color BUTTERCREAM      = new Color(0xF3, 0xE3, 0xC0); // Pantone 12-0825 TCX
    public static final Color BUTTERCREAM_DARK = new Color(0xE8, 0xD4, 0xA8);
    public static final Color BUTTERCREAM_TINT = new Color(0xFB, 0xF5, 0xE6);

    public static final Color SERENITY_BLUE       = new Color(0x8F, 0xA7, 0xC8); // Pantone 15-4020 TCX
    public static final Color SERENITY_BLUE_HOVER = new Color(0x7C, 0x96, 0xBA);
    public static final Color SERENITY_BLUE_TINT  = new Color(0x8F, 0xA7, 0xC8, 38); // ~15% opacity for row hover
    public static final Color SERENITY_BLUE_LIGHT = new Color(0xDE, 0xE7, 0xF2);

    public static final Color WARM_OFF_WHITE = new Color(0xFF, 0xFC, 0xF5);
    public static final Color SURFACE_CARD   = new Color(0xFF, 0xFC, 0xF5);

    public static final Color TEXT_PRIMARY   = new Color(0x3B, 0x24, 0x18);
    public static final Color TEXT_MUTED     = new Color(0x8C, 0x7A, 0x6E);
    public static final Color TEXT_ON_DARK   = new Color(0xF3, 0xE3, 0xC0);
    public static final Color TEXT_WHITE     = Color.WHITE;

    public static final Color SUCCESS_GREEN  = new Color(0x38, 0x8E, 0x3C);
    public static final Color SUCCESS_TINT   = new Color(0xE8, 0xF5, 0xE9);
    public static final Color WARNING_AMBER  = new Color(0xF5, 0x7C, 0x00);
    public static final Color WARNING_TINT   = new Color(0xFF, 0xF3, 0xE0);
    public static final Color ERROR_RED      = new Color(0xD3, 0x2F, 0x2F);
    public static final Color ERROR_TINT     = new Color(0xFF, 0xEB, 0xEE);

    public static final Color BORDER_LIGHT   = new Color(0x3B, 0x24, 0x18, 25);
    public static final Color SHADOW_COLOR   = new Color(0x3B, 0x24, 0x18, 16);

    // ------------------------------------------------------------- Dimensions
    public static final int SIDEBAR_WIDTH = 220;
    public static final int RADIUS_PILL   = 999;
    public static final int RADIUS_BUTTON = 12;
    public static final int RADIUS_CARD   = 16;
    public static final int RADIUS_INPUT  = 12;
    public static final int GRID_UNIT     = 8;

    // ------------------------------------------------------------- Fonts
    private static String fontName = "SansSerif";
    private static boolean fontsInitialized = false;

    public static synchronized void initFonts() {
        if (fontsInitialized) return;
        fontsInitialized = true;
        try {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            String[] fontFiles = {
                "src/resources/fonts/PlusJakartaSans-Regular.ttf",
                "src/resources/fonts/PlusJakartaSans-SemiBold.ttf",
                "src/resources/fonts/PlusJakartaSans-Bold.ttf"
            };
            boolean loadedAny = false;
            for (String path : fontFiles) {
                File f = new File(path);
                if (f.exists()) {
                    Font font = Font.createFont(Font.TRUETYPE_FONT, f);
                    ge.registerFont(font);
                    if (font != null) {
                        fontName = font.getFamily();
                    }
                    loadedAny = true;
                } else {
                    // Try classpath
                    String resPath = "/" + path.replace("src/", "");
                    try (InputStream in = AppTheme.class.getResourceAsStream(resPath)) {
                        if (in != null) {
                            Font font = Font.createFont(Font.TRUETYPE_FONT, in);
                            ge.registerFont(font);
                            if (font != null) {
                                fontName = font.getFamily();
                            }
                            loadedAny = true;
                        }
                    }
                }
            }
            if (loadedAny) {
                System.out.println("Loaded typography font: " + fontName);
            }
        } catch (Exception e) {
            System.err.println("Notice: Could not load Plus Jakarta Sans font (" + e.getMessage() + "), using system SansSerif.");
            fontName = "SansSerif";
        }
    }

    public static String getFontName() {
        return fontName;
    }

    public static Font fontHero() {
        return new Font(fontName, Font.BOLD, 34);
    }

    public static Font fontSection() {
        return new Font(fontName, Font.BOLD, 22);
    }

    public static Font fontCardTitle() {
        return new Font(fontName, Font.BOLD, 17);
    }

    public static Font fontBody() {
        return new Font(fontName, Font.PLAIN, 14);
    }

    public static Font fontBodyBold() {
        return new Font(fontName, Font.BOLD, 14);
    }

    public static Font fontPrice() {
        return new Font(fontName, Font.BOLD, 18);
    }

    public static Font fontMetadata() {
        return new Font(fontName, Font.BOLD, 12);
    }

    public static Font fontSmall() {
        return new Font(fontName, Font.PLAIN, 12);
    }

    public static Font fontSmallBold() {
        return new Font(fontName, Font.BOLD, 11);
    }

    public static void enableAntialiasing(Graphics g) {
        if (g instanceof Graphics2D g2) {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        }
    }
}
