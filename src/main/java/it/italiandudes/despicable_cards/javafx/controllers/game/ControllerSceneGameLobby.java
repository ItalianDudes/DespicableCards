package it.italiandudes.despicable_cards.javafx.controllers.game;

import it.italiandudes.despicable_cards.data.card.BlackCard;
import it.italiandudes.despicable_cards.data.player.PlayerData;
import it.italiandudes.despicable_cards.data.player.PlayerDataManager;
import it.italiandudes.despicable_cards.features.DiscordRichPresenceManager;
import it.italiandudes.despicable_cards.javafx.Client;
import it.italiandudes.despicable_cards.javafx.JFXDefs;
import it.italiandudes.despicable_cards.javafx.scene.SceneMainMenu;
import it.italiandudes.despicable_cards.javafx.scene.game.SceneGameMaster;
import it.italiandudes.despicable_cards.javafx.scene.game.SceneGamePlayer;
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
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ToggleButton;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.Socket;

public final class ControllerSceneGameLobby {

    // Attributes
    private Socket connectionToServer = null;
    private PlayerData playerData = null;
    private final PlayerDataManager playersDataManager = new PlayerDataManager();
    private boolean ready = false;
    private volatile boolean configurationComplete = false;

    // Attributes Methods
    public void setPlayerData(@NotNull final PlayerData playerData) {
        if (this.playerData == null) this.playerData = playerData;
    }
    public void setConnectionToServer(@NotNull final Socket connectionToServer) {
        if (this.connectionToServer == null) this.connectionToServer = connectionToServer;
    }
    public void configurationComplete() {
        configurationComplete = true;
    }

    // Graphic Elements
    @FXML private Label labelLobbyName; // TODO
    @FXML private ListView<PlayerData> listViewPlayersList;
    @FXML private ToggleButton toggleButtonSwitchReady;

    // Initialize
    @FXML
    private void initialize() {
        DiscordRichPresenceManager.updateRichPresenceState(DiscordRichPresenceManager.States.IN_LOBBY);
        listViewPlayersList.setCellFactory(playerListEntryListView -> new ListCell<>() {
            @Override
            protected void updateItem(PlayerData entry, boolean empty) {
                super.updateItem(entry, empty);
                if (empty || entry == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                setText(entry.getUsername());
                if (entry.isReady()) {
                    setStyle("-fx-background-color: #00FF0099");
                } else {
                    setStyle("-fx-background-color: #FF000099");
                }
            }
        });
        JFXDefs.startServiceTask(() -> {
            while (!configurationComplete) Thread.onSpinWait();
            JFXDefs.startServiceTask(this::lobbyListener);
        });
    }

    // List Populator
    private void populateList(@NotNull JSONObject lobbyList) {
        JSONArray players = lobbyList.getJSONArray("players");
        playersDataManager.clear();
        for (int i=0; i<players.length(); i++) {
            JSONObject player = players.getJSONObject(i);
            playersDataManager.add(new PlayerData(player.getString("player"), player.getString("username"), player.getBoolean("ready")));
        }
        Platform.runLater(() -> listViewPlayersList.setItems(FXCollections.observableList(playersDataManager.getPlayersData())));
    }

    // Connection Closer
    private void closeConnection() {
        Logger.log("Lobby Close Connection Called!", Defs.LOGGER_CONTEXT);
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

    // Lobby Listener (NON-EDT)
    private void lobbyListener() {
        try {
            boolean stopLoop = false;
            while (!stopLoop && !connectionToServer.isClosed()) {
                JSONObject message = JSONSerializer.readJSONObject(connectionToServer.getInputStream());

                if (message.has("alive_request")) {
                    long timestamp = message.getLong("alive_request");
                    JSONSerializer.writeJSONObject(connectionToServer.getOutputStream(), SharedProtocols.getAliveResponse(timestamp));
                } else if (message.has("join")) {
                    PlayerData newPlayer = new PlayerData(message.getString("join"), message.getString("username"), false);
                    playersDataManager.add(newPlayer);
                    Platform.runLater(() -> listViewPlayersList.refresh());
                } else if (message.has("left")) {
                    PlayerData playerData = playersDataManager.getPlayerDataWithUUID(message.getString("left"));
                    if (playerData != null) {
                        playersDataManager.remove(playerData);
                        Platform.runLater(() -> listViewPlayersList.refresh());
                    }
                } else if (message.has("player")) {
                    PlayerData playerData = playersDataManager.getPlayerDataWithUUID(message.getString("player"));
                    if (playerData != null) {
                        playerData.setReady(message.getBoolean("ready"));
                        Platform.runLater(() -> listViewPlayersList.refresh());
                    }
                } else if (message.has("players")) {
                    populateList(message);
                } else if (message.has("state")) {
                    String state = message.getString("state");
                    switch (state) {
                        case "game" -> {
                            stopLoop = true;
                            JSONObject announcedRound = JSONSerializer.readJSONObject(connectionToServer.getInputStream());
                            PlayerData masterPlayerData = playersDataManager.getPlayerDataWithUUID(announcedRound.getString("master"));
                            if (masterPlayerData == null) throw new RuntimeException("Master not in PlayerManager");
                            JSONObject blackcardJSON = announcedRound.getJSONObject("blackcard");
                            BlackCard blackCard = new BlackCard(blackcardJSON.getString("card_id"), blackcardJSON.getString("content"), blackcardJSON.getInt("blanks"));
                            Platform.runLater(() -> {
                                if (playerData.equals(masterPlayerData)) Client.setScene(SceneGameMaster.getScene(playerData, blackCard, playersDataManager, connectionToServer));
                                else Client.setScene(SceneGamePlayer.getScene(masterPlayerData, playerData, blackCard, playersDataManager, connectionToServer));
                            });
                        } // OPEN GAME SCENE
                        case "lobby" -> {} // Ignore
                        case "close" -> {
                            stopLoop = true;
                            closeConnection();
                        }
                        default -> throw new RuntimeException("Invalid state \"" + state + "\"");
                    }
                } else throw new RuntimeException("Unexpected server message for this stage [LOBBY]");
            }
        } catch (Exception e) {
            Logger.log(e, Defs.LOGGER_CONTEXT);
            closeConnection();
        }
    }

    // EDT
    @FXML
    private void switchReady() {
        toggleButtonSwitchReady.setDisable(true);
        ready = !ready;
        if (ready) toggleButtonSwitchReady.setText("PRONTO");
        else toggleButtonSwitchReady.setText("NON PRONTO");
        JFXDefs.startServiceTask(() -> {
            try {
                JSONSerializer.writeJSONObject(connectionToServer.getOutputStream(), ClientProtocols.Lobby.getLobbyReady(ready));
                Platform.runLater(() -> toggleButtonSwitchReady.setDisable(false));
            } catch (Throwable e) {
                Logger.log(e, Defs.LOGGER_CONTEXT);
                closeConnection();
            }
        });
    }
}
