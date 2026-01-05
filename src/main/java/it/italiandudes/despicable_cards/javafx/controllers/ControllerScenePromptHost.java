package it.italiandudes.despicable_cards.javafx.controllers;

import it.italiandudes.despicable_cards.data.player.PlayerData;
import it.italiandudes.despicable_cards.exceptions.NotEnoughBlackcardsException;
import it.italiandudes.despicable_cards.exceptions.NotEnoughWhitecardsException;
import it.italiandudes.despicable_cards.javafx.Client;
import it.italiandudes.despicable_cards.javafx.scene.game.SceneGameLobby;
import it.italiandudes.despicable_cards.protocol.ClientProtocols;
import it.italiandudes.despicable_cards.server.ServerInstance;
import it.italiandudes.despicable_cards.utils.Defs;
import it.italiandudes.despicable_cards.utils.JSONSerializer;
import it.italiandudes.idl.javafx.JFXUtils;
import it.italiandudes.idl.javafx.UIElementConfigurator;
import it.italiandudes.idl.javafx.alert.ErrorAlert;
import it.italiandudes.idl.logger.Logger;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.net.Socket;

public final class ControllerScenePromptHost {

    // Graphic Elements
    @FXML private TextField textFieldUsername;
    @FXML private TextField passwordFieldServerPassword;
    @FXML private CheckBox checkBoxRestoreWhitecards;
    @FXML private CheckBox checkBoxRestoreBlackcards;
    @FXML private Spinner<Integer> spinnerMaxPlayers;
    @FXML private Spinner<Integer> spinnerMaxRounds;
    @FXML private Spinner<Integer> spinnerPort;
    @FXML private Button buttonBack;
    @FXML private Button buttonCreateLobby;

    // Initialize
    @FXML
    private void initialize() {
        spinnerMaxPlayers.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(Defs.MIN_PLAYER_LIMIT, Defs.MAX_PLAYERS_LIMIT));
        spinnerMaxRounds.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, Defs.MAX_ROUNDS_LIMIT));
        spinnerPort.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 65535, 45000, 1));
        spinnerPort.getEditor().setTextFormatter(UIElementConfigurator.configureNewIntegerTextFormatter());
        checkBoxRestoreWhitecards.setTooltip(new Tooltip("Le carte bianche usate verranno ripristinate comunque in caso finissero le carte bianche disponibili."));
        checkBoxRestoreBlackcards.setTooltip(new Tooltip("Le carte nere usate verranno ripristinate comunque in caso finissero le carte nere disponibili."));
    }

    // Methods
    private void disableAll() {
        checkBoxRestoreWhitecards.setDisable(true);
        checkBoxRestoreBlackcards.setDisable(true);
        textFieldUsername.setDisable(true);
        passwordFieldServerPassword.setDisable(true);
        spinnerMaxPlayers.setDisable(true);
        spinnerPort.setDisable(true);
        spinnerMaxRounds.setDisable(true);
        buttonBack.setDisable(true);
        buttonCreateLobby.setDisable(true);
    }
    private void enableAll() {
        checkBoxRestoreWhitecards.setDisable(false);
        checkBoxRestoreBlackcards.setDisable(false);
        textFieldUsername.setDisable(false);
        passwordFieldServerPassword.setDisable(false);
        spinnerMaxPlayers.setDisable(false);
        spinnerPort.setDisable(false);
        spinnerMaxRounds.setDisable(false);
        buttonBack.setDisable(false);
        buttonCreateLobby.setDisable(false);
    }

    // EDT
    @FXML
    private void createLobby() {
        disableAll();
        boolean restoreWhitecards = checkBoxRestoreWhitecards.isSelected();
        boolean restoreBlackcards = checkBoxRestoreBlackcards.isSelected();
        String username = textFieldUsername.getText();
        if (username.trim().isBlank()) {
            new ErrorAlert(Client.getStage(), "ERRORE", "Errore di Inserimento", "Il campo \"Nome Utente\" e' obbligatorio.");
            enableAll();
            return;
        }
        int maxPlayers = spinnerMaxPlayers.getValue();
        int maxRounds = spinnerMaxRounds.getValue();
        String password = passwordFieldServerPassword.getText();
        if (password.trim().isBlank()) password = null;
        if (password != null) password = DigestUtils.sha512Hex(password);
        String portText = spinnerPort.getEditor().getText();
        int port;
        try {
            port = Integer.parseInt(portText);
            if (port <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            new ErrorAlert(Client.getStage(), "ERRORE", "Errore di Inserimento", "La porta deve essere un numero intero compreso da 1 e 65535 estremi inclusi.");
            enableAll();
            return;
        }
        String finalPassword = password;
        JFXUtils.startVoidServiceTask(() -> {
            try {
                ServerInstance.newInstance(port, maxPlayers, maxRounds, finalPassword, restoreWhitecards, restoreBlackcards);
            } catch (IOException e) {
                Logger.log(e, Defs.SERVER_LOGGER_CONTEXT);
                Platform.runLater(() -> {
                    new ErrorAlert(Client.getStage(), "ERRORE", "Errore di IO", "Si e' verificato un errore durante l'accesso alla cartella dei pacchetti carte.");
                    enableAll();
                });
            } catch (NotEnoughWhitecardsException e) {
                Logger.log(e, Defs.SERVER_LOGGER_CONTEXT);
                Platform.runLater(() -> {
                    new ErrorAlert(Client.getStage(), "ERRORE", "Carte Bianche Insufficienti", "Le carte bianche caricate non sono sufficienti (MIN " + Defs.MIN_WHITECARDS_LOADED + ").");
                    enableAll();
                });
            } catch (NotEnoughBlackcardsException e) {
                Logger.log(e, Defs.SERVER_LOGGER_CONTEXT);
                Platform.runLater(() -> {
                    new ErrorAlert(Client.getStage(), "ERRORE", "Carte Nere Insufficienti", "Le carte nere caricate non sono sufficienti (MIN " + Defs.MIN_BLACKCARDS_LOADED + ").");
                    enableAll();
                });
            }
            Socket socket = null;
            try {
                socket = new Socket("127.0.0.1", port);
                String sha512password = finalPassword != null ? DigestUtils.sha512Hex(finalPassword) : null;
                JSONSerializer.writeJSONObject(socket.getOutputStream(), ClientProtocols.Handshake.getRequest(username, sha512password));
                JSONObject response = JSONSerializer.readJSONObject(socket.getInputStream());
                String uuid = response.getString("uuid");
                PlayerData playerData = new PlayerData(uuid, username, false);
                Socket finalSocket = socket;
                Platform.runLater(() -> {
                    Client.setScene(SceneGameLobby.getScene(playerData, finalSocket));
                    back();
                });
            } catch (Exception e) {
                Logger.log(e, Defs.LOGGER_CONTEXT);
                try {
                    if (socket != null) socket.close();
                } catch (Exception ignored) {}
                ServerInstance.stopInstance();
                Platform.runLater(() -> {
                    new ErrorAlert(Client.getStage(), "ERRORE", "Errore di Rete", "Si e' verificato un errore di rete durante la connessione al server locale.");
                    enableAll();
                });
            }
        });
    }
    @FXML
    private void back() {
        buttonBack.getScene().getWindow().hide();
    }
}
