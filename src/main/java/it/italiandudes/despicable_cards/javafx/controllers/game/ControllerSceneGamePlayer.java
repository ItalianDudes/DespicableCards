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
import it.italiandudes.idl.javafx.alert.InformationAlert;
import it.italiandudes.idl.logger.Logger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.Socket;
import java.util.ArrayList;

public final class ControllerSceneGamePlayer {

    // Attributes
    private PlayerData masterPlayerData;
    private BlackCard blackCard;
    private Socket connectionToServer;
    private PlayerData playerData;
    private PlayerDataManager playersDataManager;
    private volatile boolean configurationComplete = false;
    private WhiteCard selectedWhitecard = null;

    // Attributes Methods
    public void setMasterPlayerData(@NotNull final PlayerData masterPlayerData) {
        if (this.masterPlayerData == null) this.masterPlayerData = masterPlayerData;
    }
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
    @FXML private Label labelMasterName;
    @FXML private TextArea textAreaBlackcard;
    @FXML private ListView<WhiteCard> listViewWhitecards;
    @FXML private ListView<WhiteCard> listViewChosenWhitecards;
    @FXML private Button buttonAddToChoices;
    @FXML private Button buttonRemoveFromChoices;
    @FXML private Button buttonMoveUp;
    @FXML private Button buttonMoveDown;
    @FXML private TextArea textAreaWhitecard;
    @FXML private Button buttonConfirmChoices;

    // Initialize
    @FXML
    private void initialize() {
        DiscordRichPresenceManager.updateRichPresenceState(DiscordRichPresenceManager.States.IN_GAME);
        textAreaWhitecard.textProperty().addListener((observableValue, oldValue, newValue) -> {
            if (selectedWhitecard != null && textAreaWhitecard.isEditable()) selectedWhitecard.setWildcardContent(newValue);
        });
        listViewWhitecards.getSelectionModel().selectedItemProperty().addListener((observableValue, oldValue, newValue) -> {
            selectedWhitecard = newValue;
            textAreaWhitecard.setEditable(false);
            if (newValue != null && !newValue.isWildcard()) textAreaWhitecard.setText(newValue.getContent());
            else if (newValue != null && newValue.isWildcard()) {
                textAreaWhitecard.clear();
                textAreaWhitecard.setEditable(true);
            } else textAreaWhitecard.clear();
        });
        listViewChosenWhitecards.getSelectionModel().selectedItemProperty().addListener((observableValue, oldValue, newValue) -> {
            selectedWhitecard = newValue;
            textAreaWhitecard.setEditable(false);
            if (newValue != null && !newValue.isWildcard()) textAreaWhitecard.setText(newValue.getContent());
            else if (newValue != null && newValue.isWildcard()) {
                textAreaWhitecard.clear();
                textAreaWhitecard.setEditable(true);
            } else textAreaWhitecard.clear();
        });
        JFXDefs.startServiceTask(() -> {
            while (!configurationComplete) Thread.onSpinWait();
            labelMasterName.setText("MASTER: " + masterPlayerData.getUsername());
            textAreaBlackcard.setText(blackCard.getContent());
            JFXDefs.startServiceTask(this::gameListener);
        });
    }

    // Methods
    private void disableAll() {
        buttonConfirmChoices.setDisable(true);
        buttonRemoveFromChoices.setDisable(true);
        buttonAddToChoices.setDisable(true);
        buttonMoveDown.setDisable(true);
        buttonMoveUp.setDisable(true);
        textAreaWhitecard.setDisable(true);
    }
    private void enableAll() {
        buttonConfirmChoices.setDisable(false);
        buttonRemoveFromChoices.setDisable(false);
        buttonAddToChoices.setDisable(false);
        buttonMoveDown.setDisable(false);
        buttonMoveUp.setDisable(false);
        textAreaWhitecard.setDisable(false);
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
                } else if (message.has("whitecards")) {
                    JSONArray whitecardsArray = message.getJSONArray("whitecards");
                    ArrayList<WhiteCard> whiteCards = new ArrayList<>();
                    for (int i=0; i<whitecardsArray.length(); i++) {
                        JSONObject whitecardJSON = whitecardsArray.getJSONObject(i);
                        WhiteCard whiteCard = new WhiteCard(whitecardJSON.getString("card_id"), whitecardJSON.getString("content"), whitecardJSON.getBoolean("wildcard"));
                        whiteCards.add(whiteCard);
                    }
                    Platform.runLater(() -> listViewWhitecards.setItems(FXCollections.observableList(whiteCards)));
                } else throw new RuntimeException("Unexpected server message for this stage [GAME]");
            }
        } catch (Exception e) {
            Logger.log(e, Defs.LOGGER_CONTEXT);
            closeConnection();
        }
    }

    // EDT
    @FXML
    private void confirmChoices() {
        disableAll();
        JFXDefs.startServiceTask(() -> {
            ArrayList<WhiteCardChoice> choices = new ArrayList<>();
            for (int i=0; i<listViewChosenWhitecards.getItems().size(); i++) {
                WhiteCard card = listViewChosenWhitecards.getItems().get(i);
                choices.add(new WhiteCardChoice(card, i, card.getWildcardContent()));
            }
            try {
                JSONSerializer.writeJSONObject(connectionToServer.getOutputStream(), ClientProtocols.Game.getSendWhitecards(choices));
                Platform.runLater(() -> {
                    buttonConfirmChoices.setText("IN ATTESA DEL MASTER...");
                    new InformationAlert(Client.getStage(), "SUCCESSO", "Invio Scelte", "Scelte inviate con successo al server!");
                });
            } catch (Exception e) {
                Logger.log(e);
                closeConnection();
            }
        });
    }
    @FXML
    private void moveUp() {
        WhiteCard selected = listViewChosenWhitecards.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        int index = listViewChosenWhitecards.getItems().indexOf(selected);
        if (index == 0) return;
        listViewChosenWhitecards.getItems().set(index, listViewChosenWhitecards.getItems().get(index-1));
        listViewChosenWhitecards.getItems().set(index-1, selected);
    }
    @FXML
    private void moveDown() {
        WhiteCard selected = listViewChosenWhitecards.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        int index = listViewChosenWhitecards.getItems().indexOf(selected);
        if (index == listViewChosenWhitecards.getItems().size()-1) return;
        listViewChosenWhitecards.getItems().set(index, listViewChosenWhitecards.getItems().get(index+1));
        listViewChosenWhitecards.getItems().set(index+1, selected);
    }
    @FXML
    private void addToChoices() {
        WhiteCard selected = listViewWhitecards.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        listViewChosenWhitecards.getItems().add(selected);
        listViewWhitecards.getItems().remove(selected);
        if (listViewChosenWhitecards.getItems().size() == blackCard.getBlanks()) {
            buttonAddToChoices.setDisable(true);
            buttonConfirmChoices.setDisable(false);
        }
        buttonRemoveFromChoices.setDisable(false);
    }
    @FXML
    private void removeFromChoices() {
        WhiteCard selected = listViewChosenWhitecards.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        listViewWhitecards.getItems().add(selected);
        listViewChosenWhitecards.getItems().remove(selected);
        if (listViewChosenWhitecards.getItems().isEmpty()) buttonRemoveFromChoices.setDisable(true);
        buttonAddToChoices.setDisable(false);
        buttonConfirmChoices.setDisable(true);
    }
}