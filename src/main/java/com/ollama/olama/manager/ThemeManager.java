package com.ollama.olama.manager;

import javafx.scene.Scene;

/**
 * Manages application themes and font settings.
 */
public class ThemeManager {
    
    private static final String BASE_STYLES = "/com/ollama/olama/styles.css";
    private static final String THEME_PATH = "/com/ollama/olama/themes/";
    
    /**
     * Applies theme and font settings to a scene.
     */
    public static void applyTheme(Scene scene, String theme, String fontFamily, int fontSize) {
        if (scene == null) return;
        
        // Clear existing stylesheets
        scene.getStylesheets().clear();
        
        // Add base styles
        scene.getStylesheets().add(ThemeManager.class.getResource(BASE_STYLES).toExternalForm());
        
        // Add theme-specific styles
        String themeFile = THEME_PATH + theme.toLowerCase() + "-theme.css";
        try {
            String themeUrl = ThemeManager.class.getResource(themeFile).toExternalForm();
            scene.getStylesheets().add(themeUrl);
        } catch (Exception e) {
            System.err.println("Theme not found: " + themeFile + ", using default");
        }
        
        // Apply font settings
        applyFontSettings(scene, fontFamily, fontSize);
    }
    
    /**
     * Applies only font settings to a scene.
     */
    public static void applyFontSettings(Scene scene, String fontFamily, int fontSize) {
        if (scene == null) return;
        
        String fontStyle = String.format(
            "-fx-font-family: '%s'; -fx-font-size: %dpx;",
            fontFamily.equals("System") ? "System" : fontFamily,
            fontSize
        );
        
        scene.getRoot().setStyle(fontStyle);
    }
    
    /**
     * Gets available theme names.
     */
    public static String[] getAvailableThemes() {
        return new String[]{"Light", "Dark", "Blue"};
    }
    
    /**
     * Gets available font families.
     */
    public static String[] getAvailableFonts() {
        return new String[]{
            "System", "Arial", "Helvetica", "Times New Roman", 
            "Courier New", "Verdana", "Georgia", "Comic Sans MS"
        };
    }
}