package it.italiandudes.despicable_cards.javafx.controllers;

import com.sun.javafx.application.HostServicesDelegate;
import it.italiandudes.despicable_cards.DespicableCards;
import it.italiandudes.despicable_cards.features.DiscordRichPresenceManager;
import it.italiandudes.despicable_cards.javafx.Client;
import it.italiandudes.despicable_cards.javafx.JFXDefs;
import it.italiandudes.despicable_cards.javafx.scene.SceneLoading;
import it.italiandudes.despicable_cards.javafx.scene.SceneSettingsEditor;
import it.italiandudes.despicable_cards.utils.Defs;
import it.italiandudes.despicable_cards.utils.Updater;
import it.italiandudes.idl.handler.JarHandler;
import it.italiandudes.idl.javafx.alert.ConfirmationAlert;
import it.italiandudes.idl.javafx.alert.ErrorAlert;
import it.italiandudes.idl.javafx.alert.InformationAlert;
import it.italiandudes.idl.javafx.alert.YesNoAlert;
import it.italiandudes.idl.javafx.components.SceneController;
import it.italiandudes.idl.logger.Logger;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.stage.FileChooser;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.jar.Attributes;

public final class ControllerSceneMainMenu {

    // Methods
    @SuppressWarnings("DuplicatedCode")
    private void updateApp(@NotNull final SceneController thisScene, @NotNull final String latestVersion) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Aggiornamento DespicableCards");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Java Executable File", "*.jar"));
        assert Defs.CURRENT_PLATFORM != null;
        fileChooser.setInitialFileName(Defs.APP_FILE_NAME+"-"+latestVersion+"-"+Defs.CURRENT_PLATFORM.getManifestTargetPlatform().toUpperCase()+".jar");
        fileChooser.setInitialDirectory(new File(Defs.JAR_POSITION).getParentFile());
        File fileNewApp;
        try {
            fileNewApp = fileChooser.showSaveDialog(Client.getStage().getScene().getWindow());
        } catch (IllegalArgumentException e) {
            fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
            fileNewApp = fileChooser.showSaveDialog(Client.getStage().getScene().getWindow());
        }
        if (fileNewApp == null) {
            Client.setScene(thisScene);
            return;
        }
        File finalFileNewApp = fileNewApp;
        new Service<Void>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<>() {
                    @Override
                    protected Void call() {
                        try {
                            Updater.downloadNewVersion(finalFileNewApp.getAbsoluteFile().getParent() + File.separator + Defs.APP_FILE_NAME + "-" + latestVersion + "-" + Defs.CURRENT_PLATFORM.getManifestTargetPlatform().toUpperCase() + ".jar");
                            Platform.runLater(() -> {
                                if (new ConfirmationAlert(Client.getStage(), "AGGIORNAMENTO", "Aggiornamento", "Download della nuova versione completato! Vuoi chiudere questa app?").result) {
                                    Client.exit();
                                } else {
                                    Client.setScene(thisScene);
                                }
                            });
                        } catch (IOException e) {
                            Logger.log(e, Defs.LOGGER_CONTEXT);
                            Platform.runLater(() -> {
                                new ErrorAlert(Client.getStage(),"ERRORE", "Errore di Download", "Si e' verificato un errore durante il download della nuova versione dell'app.");
                                Client.setScene(thisScene);
                            });
                        } catch (URISyntaxException e) {
                            Logger.log(e, Defs.LOGGER_CONTEXT);
                            Platform.runLater(() -> {
                                new ErrorAlert(Client.getStage(),"ERRORE", "Errore di Download", "Si e' verificato un errore durante la validazione del link per il download della nuova versione dell'app.");
                                Client.setScene(thisScene);
                            });
                        }
                        return null;
                    }
                };
            }
        }.start();
    }
    private void downloadLauncher(@NotNull final SceneController thisScene) {
        JFXDefs.startServiceTask(() -> {
            String latestVersion = null;
            try {
                URL url = new URI("https://github.com/ItalianDudes/ID_Launcher/releases/latest").toURL();
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.getResponseCode();
                connection.disconnect();
                latestVersion = connection.getURL().toString().split("/tag/")[1];
            } catch (IOException e) {
                Logger.log(e, Defs.LOGGER_CONTEXT);
                Platform.runLater(() -> {
                    new ErrorAlert(Client.getStage(),"ERRORE", "Errore di Connessione", "Si e' verificato un errore durante la connessione a GitHub.");
                    Client.setScene(thisScene);
                });
                return;
            } catch (URISyntaxException e) {
                Logger.log(e, Defs.LOGGER_CONTEXT);
                Platform.runLater(() -> {
                    new ErrorAlert(Client.getStage(),"ERRORE", "Errore di Connessione", "Si e' verificato un errore durante la validazione del link a GitHub.");
                    Client.setScene(thisScene);
                });
            }
            if (latestVersion == null) return;
            final String finalLatestVersion = latestVersion;
            Platform.runLater(() -> {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Download ID Launcher");
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Java Executable File", "*.jar"));
                fileChooser.setInitialFileName("ID_Launcher"+"-"+ finalLatestVersion +".jar");
                fileChooser.setInitialDirectory(new File(Defs.JAR_POSITION).getParentFile());
                File launcherDest;
                try {
                    launcherDest = fileChooser.showSaveDialog(Client.getStage().getScene().getWindow());
                } catch (IllegalArgumentException e) {
                    fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
                    launcherDest = fileChooser.showSaveDialog(Client.getStage().getScene().getWindow());
                }
                if (launcherDest == null) {
                    Client.setScene(thisScene);
                    return;
                }

                final File finalLauncherDest = launcherDest;
                JFXDefs.startServiceTask(() -> {
                    String downloadURL = "https://github.com/ItalianDudes/ID_Launcher/releases/latest/download/ID_Launcher-"+ finalLatestVersion +".jar";
                    try {
                        URL url = new URI(downloadURL).toURL();
                        InputStream is = url.openConnection().getInputStream();
                        Files.copy(is, Paths.get(finalLauncherDest.getAbsolutePath()), StandardCopyOption.REPLACE_EXISTING);
                        is.close();
                    } catch (IOException e) {
                        Logger.log(e, Defs.LOGGER_CONTEXT);
                        Platform.runLater(() -> {
                            new ErrorAlert(Client.getStage(),"ERRORE", "Errore di Download", "Si e' verificato un errore durante il download del launcher.");
                            Client.setScene(thisScene);
                        });
                        return;
                    } catch (URISyntaxException e) {
                        Logger.log(e, Defs.LOGGER_CONTEXT);
                        Platform.runLater(() -> {
                            new ErrorAlert(Client.getStage(),"ERRORE", "Errore di Download", "Si e' verificato un errore durante la validazione del link di download del launcher.");
                            Client.setScene(thisScene);
                        });
                    }
                    Platform.runLater(() -> {
                        if (new YesNoAlert(Client.getStage(),"CHIUDERE?", "Chiusura App", "Il launcher e' stato scaricato. Vuoi chiudere il D&D Visualizer?").result) {
                            Client.exit();
                        }
                    });
                });
            });
        });
    }

    // Graphic Elements
    @FXML private ImageView imageViewLogo;
    @FXML private Button buttonUpdater;

    // Initialize
    @FXML
    private void initialize() {
        imageViewLogo.setImage(JFXDefs.AppInfo.LOGO);
        DiscordRichPresenceManager.updateRichPresenceState(DiscordRichPresenceManager.States.MENU);
        if (DespicableCards.isStartedFromLauncher()) {
            buttonUpdater.setDisable(true);
        }
    }

    // EDT
    @FXML
    private void hostGame() {}
    @FXML
    private void joinGame() {}
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
            Logger.log(e);
            new ErrorAlert(Client.getStage(),"ERRORE", "Errore Interno", "Si e' verificato un errore durante l'apertura del browser predefinito.\nIl link alla pagina e' comunque disponibile negli appunti di sistema.");
        }
    }
    @FXML
    private void checkForUpdates() {
        if (Defs.CURRENT_PLATFORM == null) {
            boolean result = new YesNoAlert(Client.getStage(),"ERRORE", "Errore di Validazione","Impossibile aggiornare l'app poiche' non e' possibile riconoscere la piattaforma corrente.\nPuoi scaricare la versione corretta al tuo dispositivo al link " + Updater.LATEST_PAGE + ".\nSe vuoi andare ora alla pagina per l'aggiornamento tramite browser predefinito clicca \"Si\".").result;
            if (!result) return;
            ClipboardContent link = new ClipboardContent();
            link.putString(Updater.LATEST_PAGE);
            Client.getSystemClipboard().setContent(link);
            try {
                HostServicesDelegate.getInstance(Client.getApplicationInstance()).showDocument(Updater.LATEST_PAGE);
            } catch (Exception e) {
                Logger.log(e);
                new ErrorAlert(Client.getStage(),"ERRORE", "Errore Interno", "Si e' verificato un errore durante l'apertura del browser predefinito.\nIl link alla pagina e' comunque disponibile negli appunti di sistema.");
            }
        }

        SceneController thisScene = Client.getScene();
        Client.setScene(SceneLoading.getScene());
        if (!DespicableCards.isStartedFromLauncher()) {
            boolean response = new YesNoAlert(Client.getStage(),"NOVITA", "ItalianDudes Launcher", "C'e' una novita'!\nE' possibile scaricare un launcher che gestisca tutte le applicazioni sviluppate da ItalianDudes.\nIl launcher permette una migliore gestione degli aggiornamenti e permette di sapere le novita' sugli ultimi aggiornamenti.\nVuoi scaricare il launcher?").result;
            if (response) {
                downloadLauncher(thisScene);
                return;
            }
        }
        JFXDefs.startServiceTask(() -> {
            String latestVersion = Updater.getLatestVersion();
            if (latestVersion == null) {
                Platform.runLater(() -> {
                    new ErrorAlert(Client.getStage(),"ERRORE", "Errore di Connessione", "Si e' verificato un errore durante il controllo della versione.");
                    Client.setScene(thisScene);
                });
                return;
            }

            String currentVersion = null;
            try {
                Attributes attributes = JarHandler.ManifestReader.readJarManifest(Defs.JAR_POSITION);
                currentVersion = JarHandler.ManifestReader.getValue(attributes, "Version");
            } catch (IOException e) {
                Logger.log(e, Defs.LOGGER_CONTEXT);
            }

            if (Updater.getLatestVersion(currentVersion, latestVersion).equals(currentVersion)) {
                Platform.runLater(() -> {
                    new InformationAlert(Client.getStage(),"AGGIORNAMENTO", "Controllo Versione", "La versione corrente e' la piu' recente.");
                    Client.setScene(thisScene);
                });
                return;
            }

            String finalCurrentVersion = currentVersion;
            Platform.runLater(() -> {
                if (new ConfirmationAlert(Client.getStage(),"AGGIORNAMENTO", "Trovata Nuova Versione", "E' stata trovata una nuova versione. Vuoi scaricarla?\nVersione Corrente: "+ finalCurrentVersion +"\nNuova Versione: "+latestVersion).result) {
                    updateApp(thisScene, latestVersion);
                } else {
                    Platform.runLater(() -> Client.setScene(thisScene));
                }

            });
        });
    }
    @FXML
    private void openSettingsEditor() {
        Client.setScene(SceneSettingsEditor.getScene());
    }
}
