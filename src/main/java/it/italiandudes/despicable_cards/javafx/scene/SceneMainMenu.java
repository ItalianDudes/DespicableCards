package it.italiandudes.despicable_cards.javafx.scene;

import it.italiandudes.despicable_cards.javafx.Client;
import it.italiandudes.despicable_cards.javafx.JFXDefs;
import it.italiandudes.despicable_cards.javafx.controllers.ControllerSceneMainMenu;
import it.italiandudes.despicable_cards.utils.Defs;
import it.italiandudes.idl.javafx.components.SceneController;
import it.italiandudes.idl.logger.Logger;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Objects;

public final class SceneMainMenu {

    // Scene Generator
    @NotNull
    public static SceneController getScene() {
        return Objects.requireNonNull(genScene());
    }
    @Nullable
    private static SceneController genScene() {
        try {
            FXMLLoader loader = new FXMLLoader(Defs.Resources.get(JFXDefs.Resources.FXML.FXML_MAIN_MENU));
            Parent root = loader.load();
            ControllerSceneMainMenu controller = loader.getController();
            return new SceneController(root, controller);
        } catch (IOException e) {
            Logger.log(e, Defs.LOGGER_CONTEXT);
            Client.exit(-1);
            return null;
        }
    }
}
