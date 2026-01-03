package it.italiandudes.despicable_cards.javafx.scene.game;

import it.italiandudes.despicable_cards.data.player.PlayerData;
import it.italiandudes.despicable_cards.javafx.Client;
import it.italiandudes.despicable_cards.javafx.JFXDefs;
import it.italiandudes.despicable_cards.javafx.controllers.game.ControllerSceneGameLobby;
import it.italiandudes.despicable_cards.utils.Defs;
import it.italiandudes.idl.javafx.components.SceneController;
import it.italiandudes.idl.logger.Logger;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.Socket;
import java.util.Objects;

public final class SceneGameLobby {

    // Scene Generator
    @NotNull
    public static SceneController getScene(@NotNull final PlayerData playerData, @NotNull final Socket serverSocket) {
        return Objects.requireNonNull(genScene(playerData, serverSocket));
    }
    @Nullable
    private static SceneController genScene(@NotNull final PlayerData playerData, @NotNull final Socket serverSocket) {
        try {
            FXMLLoader loader = new FXMLLoader(Defs.Resources.get(JFXDefs.Resources.FXML.Game.FXML_LOBBY));
            Parent root = loader.load();
            ControllerSceneGameLobby controller = loader.getController();
            controller.setPlayerData(playerData);
            controller.setConnectionToServer(serverSocket);
            controller.configurationComplete();
            return new SceneController(root, controller);
        } catch (IOException e) {
            Logger.log(e, Defs.LOGGER_CONTEXT);
            Client.exit(-1);
            return null;
        }
    }
}
