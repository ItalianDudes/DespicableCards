package it.italiandudes.despicable_cards.javafx.controllers.game;

import it.italiandudes.despicable_cards.data.card.BlackCard;
import it.italiandudes.despicable_cards.data.card.WhiteCard;
import it.italiandudes.despicable_cards.data.card.WhiteCardChoice;
import it.italiandudes.despicable_cards.data.card.WhiteCardsChoiceCollection;
import it.italiandudes.despicable_cards.data.player.PlayerData;
import it.italiandudes.despicable_cards.data.player.PlayerDataManager;
import it.italiandudes.despicable_cards.features.DiscordRichPresenceManager;
import it.italiandudes.despicable_cards.javafx.Client;
import it.italiandudes.despicable_cards.javafx.JFXDefs;
import it.italiandudes.despicable_cards.javafx.scene.SceneMainMenu;
import it.italiandudes.despicable_cards.javafx.scene.game.SceneGameWinner;
import it.italiandudes.despicable_cards.protocol.ClientProtocols;
import it.italiandudes.despicable_cards.protocol.SharedProtocols;
import it.italiandudes.despicable_cards.server.ServerInstance;
import it.italiandudes.despicable_cards.utils.Defs;
import it.italiandudes.despicable_cards.utils.JSONSerializer;
import it.italiandudes.idl.javafx.alert.ErrorAlert;
import it.italiandudes.idl.logger.Logger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.Socket;
import java.util.ArrayList;

public final class ControllerSceneGameMaster {

    // Attributes
    private BlackCard blackCard;
    private Socket connectionToServer;
    private PlayerData playerData;
    private PlayerDataManager playersDataManager;
    private volatile boolean configurationComplete = false;

    // Attributes Methods
    public void setBlackCard(@NotNull final BlackCard blackCard) {
        if (this.blackCard == null) this.blackCard = blackCard;
    }
    public void setConnectionToServer(@NotNull final Socket connectionToServer) {
        if (this.connectionToServer == null) this.connectionToServer = connectionToServer;
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
    @FXML private TextArea textAreaBlackcard;
    @FXML private ListView<WhiteCardsChoiceCollection> listViewChoices;
    @FXML private ListView<WhiteCardChoice> listViewChoiceWhitecards;
    @FXML private TextArea textAreaWhitecard;
    @FXML private Button buttonSelectWinner;

    // Initialize
    @FXML
    private void initialize() {
        DiscordRichPresenceManager.updateRichPresenceState(DiscordRichPresenceManager.States.IN_GAME);
        listViewChoices.getSelectionModel().selectedItemProperty().addListener((observableValue, oldValue, newValue) -> {
            listViewChoiceWhitecards.setItems(FXCollections.observableList(newValue.whiteCardChoices()));
            if (!buttonSelectWinner.isDisable()) {
                buttonSelectWinner.setText("ELEGGI VINCITRICE LA SCELTA" + newValue);
            }
        });
        listViewChoiceWhitecards.getSelectionModel().selectedItemProperty().addListener((observableValue, oldValue, newValue) -> {
            if (newValue != null) textAreaWhitecard.setText(newValue.whiteCard().isWildcard() ? newValue.wildcardContent() : newValue.whiteCard().getContent());
            else textAreaWhitecard.clear();
        });
        JFXDefs.startServiceTask(() -> {
            while (!configurationComplete) Thread.onSpinWait();
            textAreaBlackcard.setText(blackCard.getContent());
            JFXDefs.startServiceTask(this::gameListener);
        });
    }

    // Game Listener (NON-EDT)
    private void gameListener() {
        try {
            boolean stopLoop = false;
            while (!stopLoop && !connectionToServer.isClosed()) {
                JSONObject message = JSONSerializer.readJSONObject(connectionToServer.getInputStream());

                if (message.has("alive_request")) {
                    long timestamp = message.getLong("alive_request");
                    JSONSerializer.writeJSONObject(connectionToServer.getOutputStream(), SharedProtocols.getAliveResponse(timestamp));
                } else if (message.has("left")) {
                    // TODO
                    closeConnection();
                } else if (message.has("winner")) {
                    stopLoop = true;
                    String winnerUuid = message.getString("winner");

                    JSONArray playersChoicesArray = message.getJSONArray("players_choices");
                    ArrayList<WhiteCardsChoiceCollection> playersChoices = new ArrayList<>();
                    for (int i=0; i<playersChoicesArray.length(); i++) {
                        JSONObject playerChoiceJSON = playersChoicesArray.getJSONObject(i);
                        String playerUuid = playerChoiceJSON.getString("player");
                        JSONArray combinationArray = playerChoiceJSON.getJSONArray("combination");
                        ArrayList<WhiteCardChoice> whiteCardChoices = new ArrayList<>();
                        for (int j=0; j<combinationArray.length(); j++) {
                            JSONObject combinationJSON = combinationArray.getJSONObject(j);
                            String wildcardContent = combinationJSON.has("wildcard_content") ? combinationJSON.getString("wildcard_content") : null;
                            WhiteCardChoice choice = new WhiteCardChoice(new WhiteCard(combinationJSON.getString("card_id"), combinationJSON.getString("content"), combinationJSON.getBoolean("wildcard")), combinationJSON.getInt("order_index"), wildcardContent);
                            whiteCardChoices.add(choice);
                        }
                        playersChoices.add(new WhiteCardsChoiceCollection(i, playerUuid, whiteCardChoices));
                    }

                    PlayerData winnerPlayerData = winnerUuid != null ? playersDataManager.getPlayerDataWithUUID(winnerUuid) : null;
                    if (winnerUuid != null && winnerPlayerData == null) throw new RuntimeException("Winner UUID not found in PlayerManager");
                    Platform.runLater(() -> Client.setScene(SceneGameWinner.getScene(playerData, winnerPlayerData, blackCard, playersChoices, playersDataManager, connectionToServer)));
                } else if (message.has("players_choices")) {
                    JSONArray playersChoicesArray = message.getJSONArray("players_choices");
                    ArrayList<WhiteCardsChoiceCollection> playersChoices = new ArrayList<>();
                    for (int i=0; i<playersChoicesArray.length(); i++) {
                        JSONObject playerChoiceJSON = playersChoicesArray.getJSONObject(i);
                        String playerUuid = playerChoiceJSON.getString("player");
                        JSONArray combinationArray = playerChoiceJSON.getJSONArray("combination");
                        ArrayList<WhiteCardChoice> whiteCardChoices = new ArrayList<>();
                        for (int j=0; j<combinationArray.length(); j++) {
                            JSONObject combinationJSON = combinationArray.getJSONObject(j);
                            String wildcardContent = combinationJSON.has("wildcard_content") ? combinationJSON.getString("wildcard_content") : null;
                            WhiteCardChoice choice = new WhiteCardChoice(new WhiteCard(combinationJSON.getString("card_id"), combinationJSON.getString("content"), combinationJSON.getBoolean("wildcard")), combinationJSON.getInt("order_index"), wildcardContent);
                            whiteCardChoices.add(choice);
                        }
                        playersChoices.add(new WhiteCardsChoiceCollection(i, playerUuid, whiteCardChoices));
                    }
                    Platform.runLater(() -> {
                        listViewChoices.setItems(FXCollections.observableList(playersChoices));
                        buttonSelectWinner.setDisable(false);
                    });
                } else if (message.has("whitecards")) { // Just ignore it...
                } else throw new RuntimeException("Unexpected server message for this stage [GAME]");
            }
        } catch (Exception e) {
            Logger.log(e, Defs.LOGGER_CONTEXT);
            closeConnection();
        }
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
    private void selectWinner() {
        buttonSelectWinner.setDisable(true);
        WhiteCardsChoiceCollection winnerChoice = listViewChoices.getSelectionModel().getSelectedItem();
        if (winnerChoice == null) {
            buttonSelectWinner.setDisable(false);
            new ErrorAlert(Client.getStage(), "ERRORE", "Scelta non selezionata", "Selezionare la scelta vincente.");
            return;
        }
        JFXDefs.startServiceTask(() -> {
            try {
                JSONSerializer.writeJSONObject(connectionToServer.getOutputStream(), ClientProtocols.Game.getMasterSendWinnerChoice(winnerChoice.playerUuid()));
            } catch (Exception e) {
                Logger.log(e);
                closeConnection();
            }
        });
    }
}