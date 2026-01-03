package it.italiandudes.despicable_cards.javafx.controllers;

import it.italiandudes.despicable_cards.data.player.PlayerData;
import it.italiandudes.despicable_cards.data.enums.ServerRejectReason;
import it.italiandudes.despicable_cards.javafx.Client;
import it.italiandudes.despicable_cards.javafx.scene.game.SceneGameLobby;
import it.italiandudes.despicable_cards.protocol.ClientProtocols;
import it.italiandudes.despicable_cards.utils.JSONSerializer;
import it.italiandudes.idl.javafx.JFXUtils;
import it.italiandudes.idl.javafx.UIElementConfigurator;
import it.italiandudes.idl.javafx.alert.ErrorAlert;
import it.italiandudes.idl.logger.Logger;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public final class ControllerScenePromptJoin {

    // Graphic Elements
    @FXML private TextField textFieldUsername;
    @FXML private TextField passwordFieldServerPassword;
    @FXML private TextField textFieldAddress;
    @FXML private Spinner<Integer> spinnerPort;
    @FXML private Button buttonBack;
    @FXML private Button buttonConnect;

    // Initialize
    @FXML
    private void initialize() {
        spinnerPort.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 65535, 45000, 1));
        spinnerPort.getEditor().setTextFormatter(UIElementConfigurator.configureNewIntegerTextFormatter());
    }

    // Methods
    private void disableAll() {
        textFieldUsername.setDisable(true);
        passwordFieldServerPassword.setDisable(true);
        textFieldAddress.setDisable(true);
        spinnerPort.setDisable(true);
        buttonBack.setDisable(true);
        buttonConnect.setDisable(true);
    }
    private void enableAll() {
        textFieldUsername.setDisable(false);
        passwordFieldServerPassword.setDisable(false);
        textFieldAddress.setDisable(false);
        spinnerPort.setDisable(false);
        buttonBack.setDisable(false);
        buttonConnect.setDisable(false);
    }

    // EDT
    @FXML
    private void connect() {
        disableAll();
        String username = textFieldUsername.getText();
        String password = passwordFieldServerPassword.getText();
        if (password.trim().isBlank()) password = null;
        if (password != null) password = DigestUtils.sha512Hex(password);
        if (username.trim().isBlank()) {
            new ErrorAlert(Client.getStage(), "ERRORE", "Errore di Inserimento", "Il campo \"Nome Utente\" e' obbligatorio.");
            enableAll();
            return;
        }
        String address = textFieldAddress.getText();
        if (address.trim().isBlank()) {
            new ErrorAlert(Client.getStage(), "ERRORE", "Errore di Inserimento", "Il campo \"Indirizzo\" e' obbligatorio.");
            enableAll();
            return;
        }
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
        String finalPassword = password != null ? DigestUtils.sha512Hex(password) : null;
        JFXUtils.startVoidServiceTask(() -> {
            Socket socket = null;
            try {
                socket = new Socket(address, port);
                JSONSerializer.writeJSONObject(socket.getOutputStream(), ClientProtocols.Handshake.getRequest(username, finalPassword));
                JSONObject response = JSONSerializer.readJSONObject(socket.getInputStream());
                if (response.has("uuid")) {
                    String playerUuid = response.getString("uuid");
                    PlayerData playerData = new PlayerData(playerUuid, username, false);
                    Socket finalSocket = socket;
                    Platform.runLater(() -> {
                        Client.setScene(SceneGameLobby.getScene(playerData, finalSocket));
                        back();
                    });
                } else if (response.has("reject")) {
                    String reject = response.getString("reject");
                    ServerRejectReason reason = ServerRejectReason.valueOf(reject);
                    Platform.runLater(() -> {
                        switch (reason) {
                            case LOBBY_FULL -> new ErrorAlert(Client.getStage(), "ERRORE", "Lobby Piena", "La lobby e' piena.");
                            case INVALID_PASSWORD -> new ErrorAlert(Client.getStage(), "ERRORE", "Password Invalida", "La password inserita non e' corretta.");
                            case INVALID_USERNAME -> new ErrorAlert(Client.getStage(), "ERRORE", "Username Invalido", "Il nome utente fornito contiene caratteri non consentiti.");
                            case NOT_IN_LOBBY -> new ErrorAlert(Client.getStage(), "ERRORE", "Server in Gioco", "Impossibile entrare nella lobby: il server e' in fase di gioco.");
                            case USERNAME_TAKEN -> new ErrorAlert(Client.getStage(), "ERRORE", "Username Occupato", "Questo nome utente e' gia' in uso.");
                            default -> new ErrorAlert(Client.getStage(), "ERRORE", "Errore Sconosciuto", "Si e' verificato un errore sconosciuto.");
                        }
                        enableAll();
                    });
                } else throw new IOException("Invalid protocol");
            } catch (UnknownHostException | IllegalArgumentException e) {
                Logger.log(e);
                try {
                    if (socket != null) socket.close();
                } catch (Exception ignored) {}
                Platform.runLater(() -> {
                    new ErrorAlert(Client.getStage(), "ERRORE", "Errore di Rete", "Host non trovato, porta errata o protocollo non rispettato.");
                    enableAll();
                });
            } catch (IOException e) {
                Logger.log(e);
                try {
                    if (socket != null) socket.close();
                } catch (Exception ignored) {}
                Platform.runLater(() -> {
                    new ErrorAlert(Client.getStage(), "ERRORE", "Errore di Rete", "Si e' verificato un errore durante la connessione.");
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
