package it.italiandudes.despicable_cards.javafx.utils;

import it.italiandudes.despicable_cards.javafx.JFXDefs;
import it.italiandudes.despicable_cards.utils.Defs;
import javafx.scene.Scene;
import org.jetbrains.annotations.NotNull;

public final class ThemeHandler {

    // Config Theme
    private static String configTheme = null;

    // Methods
    public static void setConfigTheme() {
        if (Settings.getSettings().getBoolean(Defs.SettingsKeys.ENABLE_DARK_MODE)) {
            configTheme = Defs.Resources.get(JFXDefs.Resources.CSS.CSS_DARK_THEME).toExternalForm();
        } else {
            configTheme = Defs.Resources.get(JFXDefs.Resources.CSS.CSS_LIGHT_THEME).toExternalForm();
        }
    }

    // Config Theme
    public static void loadConfigTheme(@NotNull final Scene scene) {
        if (configTheme == null) setConfigTheme();
        scene.getStylesheets().clear();
        scene.getStylesheets().add(configTheme);
    }

    // Light Theme
    public static void loadLightTheme(@NotNull final Scene scene) {
        scene.getStylesheets().clear();
        scene.getStylesheets().add(Defs.Resources.get(JFXDefs.Resources.CSS.CSS_LIGHT_THEME).toExternalForm());
    }

    // Dark Theme
    public static void loadDarkTheme(@NotNull final Scene scene) {
        scene.getStylesheets().clear();
        scene.getStylesheets().add(Defs.Resources.get(JFXDefs.Resources.CSS.CSS_DARK_THEME).toExternalForm());
    }

}
