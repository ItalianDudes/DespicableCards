package it.italiandudes.despicable_cards.utils;

import it.italiandudes.despicable_cards.DespicableCards;
import it.italiandudes.idl.common.TargetPlatform;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;

public final class Defs {

    // App File Name
    public static final String APP_FILE_NAME = "DespicableCards";

    // Current Platform
    @Nullable public static final TargetPlatform CURRENT_PLATFORM = TargetPlatform.getCurrentPlatform();

    // Logger Context
    public static final String LOGGER_CONTEXT = "DespicableCards";
    public static final String SERVER_LOGGER_CONTEXT = "Server";

    // DB Version
    public static final String DB_VERSION = "1.0";

    // Jar App Position
    public static final String JAR_POSITION;
    static {
        try {
            JAR_POSITION = new File(DespicableCards.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getAbsolutePath();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    // Limits
    public static final int MAX_WHITECARDS = 10;
    public static final int MAX_PLAYERS_LIMIT = 10;

    // JSON Settings
    public static final class SettingsKeys {
        public static final String ENABLE_DARK_MODE = "enableDarkMode";
        public static final String ENABLE_DISCORD_RICH_PRESENCE = "enableDiscordRichPresence";
    }

    // Resources Location
    public static final class Resources {

        // Project Resources Root
        public static final String PROJECT_RESOURCES_ROOT = "/it/italiandudes/despicable_cards/resources/";

        //Resource Getters
        public static URL get(@NotNull final String resourceConst) {
            return Objects.requireNonNull(DespicableCards.class.getResource(resourceConst));
        }
        public static InputStream getAsStream(@NotNull final String resourceConst) {
            return Objects.requireNonNull(DespicableCards.class.getResourceAsStream(resourceConst));
        }

        // JSON
        public static final class JSON {
            public static final String CLIENT_SETTINGS = "settings.json";
            public static final String DEFAULT_JSON_SETTINGS = PROJECT_RESOURCES_ROOT + "json/" + CLIENT_SETTINGS;
        }

        // Images
        public static final class Image {
            private static final String IMAGE_DIR = PROJECT_RESOURCES_ROOT + "image/";
            public static final String IMAGE_DARK_MODE = IMAGE_DIR + "dark_mode.png";
            public static final String IMAGE_LIGHT_MODE = IMAGE_DIR + "light_mode.png";
            public static final String IMAGE_TICK = IMAGE_DIR + "tick.png";
            public static final String IMAGE_CROSS = IMAGE_DIR + "cross.png";
            public static final String IMAGE_WUMPUS = IMAGE_DIR + "wumpus.png";
            public static final String IMAGE_NO_WUMPUS = IMAGE_DIR + "no_wumpus.png";
        }

        // SQL
        public static final class SQL {
            private static final String SQL_DIR = PROJECT_RESOURCES_ROOT + "sql/";
            public static final String SQL_DATABASE = SQL_DIR + "database.sql";
        }
    }
}
