package com.hung.arkanoid.controller;

/*
 * ============================================================================
 * Project   : Arkanoid_OOP2025
 * Package   : com.hung.arkanoid.controller
 * File Name : MenuController.java
 * Created On: 10/29/2025 at 12:26 PM
 * Author    : Trần Việt Hưng
 * ----------------------------------------------------------------------------
 * Copyright (c) 2025 Hung Tran.
 * All rights reserved.
 *
 * Description:
 *     This file is part of the Arkanoid Game project.
 *     It defines the MenuController class which is responsible for handling
 *     .
 *
 * Revision History:
 *     Version 1.0  - Initial release.
 * ============================================================================
 */
import com.hung.arkanoid.Main;
import com.hung.arkanoid.game.LevelLoader;
import com.hung.arkanoid.game.SaveData;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;

import java.net.URL;
import java.util.ResourceBundle;

public class MenuController implements Initializable {
    @FXML
    private Button startButton; // optional, kept for FXML compatibility
    @FXML
    private Button scoresButton; // optional
    @FXML
    private Button exitButton;
    @FXML
    private javafx.scene.layout.TilePane levelSelectBox;
    @FXML
    private javafx.scene.control.Button instructionsButton;

    private Main mainApp;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Will populate once mainApp is set and refreshLevelButtons is called
    }

    public void setMainApp(Main mainApp) {
        this.mainApp = mainApp;
        // store controller in the scene user data so Main can access it later
        if (levelSelectBox != null && levelSelectBox.getScene() != null) {
            levelSelectBox.getScene().setUserData(this);
        }
        refreshLevelButtons();
    }

    @FXML
    public void refreshLevelButtons() {
        if (levelSelectBox == null) return;
        levelSelectBox.getChildren().clear();
        int maxUnlocked = SaveData.loadMaxLevelUnlocked();
        // Use LevelLoader.MAX_LEVELS which we maintain in this project
        int totalLevels = LevelLoader.MAX_LEVELS;
        for (int i = 1; i <= totalLevels; i++) {
            Button b = new Button("Level " + i);
            b.getStyleClass().add("level-button");
            final int level = i;
            b.setOnAction(evt -> {
                playClickSound();
                if (mainApp != null) mainApp.startGame(level);
            });
            b.setDisable(i > maxUnlocked);
            levelSelectBox.getChildren().add(b);
        }
    }

    @FXML
    private void onExitButtonClick(ActionEvent event) {
        Platform.exit();
    }

    @FXML
    private void onStartButtonClick(ActionEvent event) {
        playClickSound();
        if (mainApp != null) mainApp.startGame(1);
    }

    @FXML
    private void onScoresButtonClick(ActionEvent event) {
        playClickSound();
        System.out.println("High Scores Clicked!");
    }

    @FXML
    private void onInstructionsClick(ActionEvent event) {
        playClickSound();
        if (mainApp != null) mainApp.showInstructions();
    }

    // small helper to play click (placeholder)
    private void playClickSound() { System.out.println("PLAY CLICK SOUND"); }
}
