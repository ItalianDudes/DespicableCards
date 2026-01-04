package it.italiandudes.despicable_cards.javafx.controllers.game;

import it.italiandudes.despicable_cards.data.card.BlackCard;
import it.italiandudes.despicable_cards.data.card.WhiteCardChoice;
import it.italiandudes.despicable_cards.data.card.WhiteCardsChoiceCollection;
import it.italiandudes.despicable_cards.data.player.PlayerData;
import it.italiandudes.despicable_cards.data.player.PlayerDataManager;
import it.italiandudes.despicable_cards.features.DiscordRichPresenceManager;
import it.italiandudes.despicable_cards.javafx.Client;
import it.italiandudes.despicable_cards.javafx.JFXDefs;
import it.italiandudes.despicable_cards.javafx.scene.SceneMainMenu;
import it.italiandudes.despicable_cards.javafx.scene.game.SceneGameLobby;
import it.italiandudes.despicable_cards.javafx.scene.game.SceneGameMaster;
import it.italiandudes.despicable_cards.javafx.scene.game.SceneGamePlayer;
import it.italiandudes.despicable_cards.protocol.SharedProtocols;
import it.italiandudes.despicable_cards.server.ServerInstance;
import it.italiandudes.despicable_cards.utils.Defs;
import it.italiandudes.despicable_cards.utils.JSONSerializer;
import it.italiandudes.idl.javafx.alert.ErrorAlert;
import it.italiandudes.idl.logger.Logger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.net.Socket;
import java.util.ArrayList;

public final class ControllerSceneGameWinner {

    // Attributes
    private BlackCard blackCard;
    private Socket connectionToServer;
    private PlayerData playerData;
    private PlayerData winnerPlayerData;
    private PlayerDataManager playersDataManager;
    private ArrayList<WhiteCardsChoiceCollection> playersChoices;
    private String nextState = null;
    private volatile boolean configurationComplete = false;

    // Attributes Methods
    public void setBlackCard(@NotNull final BlackCard blackCard) {
        if (this.blackCard == null) this.blackCard = blackCard;
    }
    public void setConnectionToServer(@NotNull final Socket connectionToServer) {
        if (this.connectionToServer == null) this.connectionToServer = connectionToServer;
    }
    public void setPlayersChoices(@NotNull final ArrayList<WhiteCardsChoiceCollection> playersChoices) {
        if (this.playersChoices == null) this.playersChoices = playersChoices;
    }
    public void setWinnerPlayerData(@Nullable final PlayerData winnerPlayerData) {
        if (this.winnerPlayerData == null) this.winnerPlayerData = winnerPlayerData;
    }
    public void setPlayerData(@NotNull final PlayerData playerData) {
        if (this.playerData == null) this.playerData = playerData;
    }
    public void setPlayersDataManager(@NotNull final PlayerDataManager playersDataManager) {
        if (this.playersDataManager == null) this.playersDataManager = playersDataManager;
    }
    public void configurationComplete() {
        configurationComplete = true;
    }

    // Graphic Elements
    @FXML private Label labelWinner;
    @FXML private TextArea textAreaBlackcard;
    @FXML private ListView<WhiteCardsChoiceCollection> listViewChoices;
    @FXML private ListView<WhiteCardChoice> listViewChoiceWhitecards;
    @FXML private TextArea textAreaWhitecard;
    @FXML private Button buttonNext;

    // Initialize
    @FXML
    private void initialize() {
        DiscordRichPresenceManager.updateRichPresenceState(DiscordRichPresenceManager.States.IN_GAME);
        listViewChoices.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(WhiteCardsChoiceCollection entry, boolean empty) {
                super.updateItem(entry, empty);
                if (empty || entry == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                setText(entry.toString());
                if (entry.playerUuid().equals(winnerPlayerData.getUuid())) {
                    setStyle("-fx-background-color: #FFC30099;-fx-font-weight: bold;");
                }
            }
        });
        listViewChoices.getSelectionModel().selectedItemProperty().addListener((observableValue, oldValue, newValue) -> listViewChoiceWhitecards.setItems(FXCollections.observableList(newValue.whiteCardChoices())));
        listViewChoiceWhitecards.getSelectionModel().selectedItemProperty().addListener((observableValue, oldValue, newValue) -> {
            if (newValue != null) textAreaWhitecard.setText(newValue.whiteCard().isWildcard() ? newValue.wildcardContent() : newValue.whiteCard().getContent());
            else textAreaWhitecard.clear();
        });
        JFXDefs.startServiceTask(() -> {
            try {
                JSONObject nextState = JSONSerializer.readJSONObject(connectionToServer.getInputStream());
                this.nextState = nextState.getString("state");
            } catch (Exception e) {
                Logger.log(e);
                closeConnection();
            }
            while (!configurationComplete) Thread.onSpinWait();
            labelWinner.setText("VINCITORE ROUND: "+ winnerPlayerData.getUsername());
            textAreaBlackcard.setText(blackCard.getContent());
            listViewChoices.setItems(FXCollections.observableList(playersChoices));
            Platform.runLater(() -> buttonNext.setDisable(false));
        });
    }

    // Connection Closer
    private void closeConnection() {
        Logger.log("Game Close Connection Called!", Defs.LOGGER_CONTEXT);
        try {
            JSONSerializer.writeJSONObject(connectionToServer.getOutputStream(), SharedProtocols.getConnectionClose(null));
        } catch (Exception ignored) {}
        try {
            connectionToServer.close();
        } catch (Exception ignored) {}
        if (ServerInstance.getInstance() != null) {
            ServerInstance.stopInstance();
        }
        Platform.runLater(() -> {
            new ErrorAlert(Client.getStage(), "ERRORE", "Errore di Rete", "Si e' verificato un errore di rete, ritorno al menu principale.");
            Client.setScene(SceneMainMenu.getScene());
        });
    }

    // EDT
    @FXML
    private void next() {
        switch (nextState) {
            case "game" -> {
                try {
                    JSONObject announcedRound = JSONSerializer.readJSONObject(connectionToServer.getInputStream());
                    PlayerData masterPlayerData = playersDataManager.getPlayerDataWithUUID(announcedRound.getString("master"));
                    if (masterPlayerData == null) throw new RuntimeException("Master not in PlayerManager");
                    JSONObject blackcardJSON = announcedRound.getJSONObject("blackcard");
                    BlackCard blackCard = new BlackCard(blackcardJSON.getString("card_id"), blackcardJSON.getString("content"), blackcardJSON.getInt("blanks"));
                    Platform.runLater(() -> {
                        if (playerData.equals(masterPlayerData)) Client.setScene(SceneGameMaster.getScene(playerData, blackCard, playersDataManager, connectionToServer));
                        else Client.setScene(SceneGamePlayer.getScene(masterPlayerData, playerData, blackCard, playersDataManager, connectionToServer));
                    });
                } catch (Exception e) {
                    Logger.log(e);
                    closeConnection();
                }
            }
            case "lobby" -> Client.setScene(SceneGameLobby.getScene(playerData, connectionToServer));
            default -> closeConnection();
        }

    }
}