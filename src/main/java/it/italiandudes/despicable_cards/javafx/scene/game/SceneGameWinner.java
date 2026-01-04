package it.italiandudes.despicable_cards.javafx.scene.game;

import it.italiandudes.despicable_cards.data.card.BlackCard;
import it.italiandudes.despicable_cards.data.card.WhiteCardsChoiceCollection;
import it.italiandudes.despicable_cards.data.player.PlayerData;
import it.italiandudes.despicable_cards.data.player.PlayerDataManager;
import it.italiandudes.despicable_cards.javafx.Client;
import it.italiandudes.despicable_cards.javafx.JFXDefs;
import it.italiandudes.despicable_cards.javafx.controllers.game.ControllerSceneGameWinner;
import it.italiandudes.despicable_cards.utils.Defs;
import it.italiandudes.idl.javafx.components.SceneController;
import it.italiandudes.idl.logger.Logger;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Objects;

public final class SceneGameWinner {

    // Scene Generator
    @NotNull
    public static SceneController getScene(
            @NotNull final PlayerData playerData, @Nullable final PlayerData winnerPlayerData, @NotNull final BlackCard blackCard,
            @NotNull final ArrayList<WhiteCardsChoiceCollection> playersChoices, @NotNull final PlayerDataManager playersDataManager, @NotNull Socket connectionToServer) {
        return Objects.requireNonNull(genScene(playerData, winnerPlayerData, blackCard, playersChoices, playersDataManager, connectionToServer));
    }
    @Nullable
    private static SceneController genScene(
            @NotNull final PlayerData playerData, @Nullable final PlayerData winnerPlayerData, @NotNull final BlackCard blackCard,
            @NotNull final ArrayList<WhiteCardsChoiceCollection> playersChoices, @NotNull final PlayerDataManager playersDataManager, @NotNull Socket connectionToServer) {
        try {
            FXMLLoader loader = new FXMLLoader(Defs.Resources.get(JFXDefs.Resources.FXML.Game.FXML_WINNER));
            Parent root = loader.load();
            ControllerSceneGameWinner controller = loader.getController();
            controller.setPlayerData(playerData);
            controller.setBlackCard(blackCard);
            controller.setWinnerPlayerData(winnerPlayerData);
            controller.setPlayersChoices(playersChoices);
            controller.setPlayersDataManager(playersDataManager);
            controller.setConnectionToServer(connectionToServer);
            controller.configurationComplete();
            return new SceneController(root, controller);
        } catch (IOException e) {
            Logger.log(e, Defs.LOGGER_CONTEXT);
            Client.exit(-1);
            return null;
        }
    }
}
