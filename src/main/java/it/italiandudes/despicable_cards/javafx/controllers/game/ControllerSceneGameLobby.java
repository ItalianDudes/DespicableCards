package it.italiandudes.despicable_cards.javafx.controllers.game;

import it.italiandudes.despicable_cards.features.DiscordRichPresenceManager;
import it.italiandudes.despicable_cards.javafx.components.PlayerListEntry;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ToggleButton;

public final class ControllerSceneGameLobby {

    // Attributes

    // Graphic Elements
    @FXML private ListView<PlayerListEntry> listViewPlayersList;
    @FXML private ToggleButton toggleButtonSwitchReady;

    // Initialize
    @FXML
    private void initialize() {
        DiscordRichPresenceManager.updateRichPresenceState(DiscordRichPresenceManager.States.IN_LOBBY);
        listViewPlayersList.setCellFactory(playerListEntryListView -> new ListCell<>() {
            private final ChangeListener<Boolean> readyListener = (obs, oldValue, newValue) -> updateStyle();
            @Override
            protected void updateItem(PlayerListEntry entry, boolean empty) {
                super.updateItem(entry, empty);
                if (empty || entry == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                setText(entry.getName());
                entry.readyProperty().removeListener(readyListener);
                entry.readyProperty().addListener(readyListener);
                updateStyle();
            }
            private void updateStyle() {
                PlayerListEntry entry= getItem();
                if (entry == null) return;
                if (entry.isReady()) {
                    setStyle("-fx-background-color: #00FF0099");
                } else {
                    setStyle("-fx-background-color: #FF000099");
                }
            }
        });
    }

    // EDT
    @FXML
    private void switchReady() {}
}
