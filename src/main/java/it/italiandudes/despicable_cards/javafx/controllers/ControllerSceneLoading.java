package it.italiandudes.despicable_cards.javafx.controllers;

import it.italiandudes.despicable_cards.javafx.JFXDefs;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public final class ControllerSceneLoading {

    // Attributes
    public static final Image LOADING_GIF = new Image(JFXDefs.Resources.GIF.GIF_LOADING);

    // Graphic Elements
    @FXML private ImageView imageViewLoading;
    @FXML private ImageView imageViewLogo;

    //Initialize
    @FXML
    private void initialize() {
        imageViewLoading.setImage(LOADING_GIF);
        imageViewLogo.setImage(JFXDefs.AppInfo.LOGO);
    }
}