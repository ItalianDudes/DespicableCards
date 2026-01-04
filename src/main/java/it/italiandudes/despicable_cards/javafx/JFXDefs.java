package it.italiandudes.despicable_cards.javafx;

import it.italiandudes.despicable_cards.utils.CalendarEventManager;
import it.italiandudes.despicable_cards.utils.Defs;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public final class JFXDefs {

    // Service Starter
    public static void startServiceTask(@NotNull final Runnable runnable) {
        new Service<Void>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<>() {
                    @Override
                    protected Void call() {
                        runnable.run();
                        return null;
                    }
                };
            }
        }.start();
    }

    // App Info
    public static final class AppInfo {
        public static final String NAME = "DespicableCards";
        public static final Image LOGO = CalendarEventManager.getEventLogo();
    }

    // System Info
    public static final class SystemGraphicInfo {
        public static final Rectangle2D SCREEN_RESOLUTION = Screen.getPrimary().getBounds();
        public static final double SCREEN_WIDTH = SCREEN_RESOLUTION.getWidth();
        public static final double SCREEN_HEIGHT = SCREEN_RESOLUTION.getHeight();
    }

    // Resource Locations
    public static final class Resources {

        // FXML Location
        public static final class FXML {
            private static final String FXML_DIR = Defs.Resources.PROJECT_RESOURCES_ROOT + "fxml/";
            public static final String FXML_LOADING = FXML_DIR + "SceneLoading.fxml";
            public static final String FXML_MAIN_MENU = FXML_DIR + "SceneMainMenu.fxml";
            public static final String FXML_PROMPT_HOST = FXML_DIR + "ScenePromptHost.fxml";
            public static final String FXML_PROMPT_JOIN = FXML_DIR + "ScenePromptJoin.fxml";
            public static final String FXML_SETTINGS_EDITOR = FXML_DIR + "SceneSettingsEditor.fxml";

            // Game
            public static final class Game {
                private static final String GAME_DIR = FXML_DIR + "game/";
                public static final String FXML_LOBBY = GAME_DIR + "SceneGameLobby.fxml";
                public static final String FXML_PLAYER = GAME_DIR + "SceneGamePlayer.fxml";
                public static final String FXML_MASTER = GAME_DIR + "SceneGameMaster.fxml";
                public static final String FXML_WINNER = GAME_DIR + "SceneGameWinner.fxml";
            }
        }

        // GIF Location
        public static final class GIF {
            private static final String GIF_DIR = Defs.Resources.PROJECT_RESOURCES_ROOT + "gif/";
            public static final String GIF_LOADING = GIF_DIR + "loading.gif";
        }

        // CSS Location
        public static final class CSS {
            private static final String CSS_DIR = Defs.Resources.PROJECT_RESOURCES_ROOT + "css/";
            public static final String CSS_LIGHT_THEME = CSS_DIR + "light_theme.css";
            public static final String CSS_DARK_THEME = CSS_DIR + "dark_theme.css";
        }

        // Image Location
        public static final class Image {
            private static final String IMAGE_DIR = Defs.Resources.PROJECT_RESOURCES_ROOT + "image/";
            public static final class Logo {
                private static final String LOGO_DIR = IMAGE_DIR + "logo/";
                public static final String LOGO_MAIN = LOGO_DIR + "main.png";
                public static final String LOGO_HALLOWEEN = LOGO_DIR + "halloween.png";
                public static final String LOGO_XMAS = LOGO_DIR + "xmas.png";
            }
            public static final String IMAGE_FILE_EXPLORER = IMAGE_DIR + "file-explorer.png";
            public static final String IMAGE_PLAY = IMAGE_DIR + "play.png";
            public static final String IMAGE_STOP = IMAGE_DIR + "stop.png";
        }

    }

}
