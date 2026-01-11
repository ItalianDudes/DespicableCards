package it.italiandudes.despicable_cards.javafx.controllers;

import com.sun.javafx.application.HostServicesDelegate;
import it.italiandudes.despicable_cards.features.DiscordRichPresenceManager;
import it.italiandudes.despicable_cards.javafx.Client;
import it.italiandudes.despicable_cards.javafx.JFXDefs;
import it.italiandudes.despicable_cards.javafx.scene.ScenePromptHost;
import it.italiandudes.despicable_cards.javafx.scene.ScenePromptJoin;
import it.italiandudes.despicable_cards.javafx.scene.SceneSettingsEditor;
import it.italiandudes.despicable_cards.utils.Defs;
import it.italiandudes.idl.javafx.alert.ErrorAlert;
import it.italiandudes.idl.javafx.alert.YesNoAlert;
import it.italiandudes.idl.javafx.components.SceneController;
import it.italiandudes.idl.logger.Logger;
import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.stage.Stage;

public final class ControllerSceneMainMenu {

    // Graphic Elements
    @FXML private ImageView imageViewLogo;

    // Initialize
    @FXML
    private void initialize() {
        imageViewLogo.setImage(JFXDefs.AppInfo.LOGO);
        DiscordRichPresenceManager.updateRichPresenceState(DiscordRichPresenceManager.States.MENU);
    }

    // EDT
    @FXML
    private void hostGame() {
        SceneController hostController = ScenePromptHost.getScene();
        Stage hostPopup = Client.initPopupStage(hostController);
        hostPopup.showAndWait();
    }
    @FXML
    private void joinGame() {
        SceneController joinController = ScenePromptJoin.getScene();
        Stage joinPopup = Client.initPopupStage(joinController);
        joinPopup.showAndWait();
    }
    @FXML
    private void showReportBanner() {
        ClipboardContent link = new ClipboardContent();
        String url = "https://github.com/ItalianDudes/" + Defs.APP_FILE_NAME + "/issues";
        link.putString(url);
        Client.getSystemClipboard().setContent(link);
        boolean result = new YesNoAlert(Client.getStage(),"INFO", "Grazie!", "ItalianDudes e' sempre felice di ricevere segnalazioni da parte degli utenti circa le sue applicazioni.\nE' stato aggiunto alla tua clipboard di sistema il link per accedere alla pagina github per aggiungere il tuo report riguardante problemi o idee varie.\nPremi \"Si\" per aprire direttamente il link nel browser predefinito.\nGrazie ancora!").result;
        try {
            if (result && Client.getApplicationInstance() != null) HostServicesDelegate.getInstance(Client.getApplicationInstance()).showDocument(url);
        } catch (Exception e) {
            Logger.log(e, Defs.LOGGER_CONTEXT);
            new ErrorAlert(Client.getStage(),"ERRORE", "Errore Interno", "Si e' verificato un errore durante l'apertura del browser predefinito.\nIl link alla pagina e' comunque disponibile negli appunti di sistema.");
        }
    }
    @FXML
    private void openSettingsEditor() {
        Client.setScene(SceneSettingsEditor.getScene());
    }
}
