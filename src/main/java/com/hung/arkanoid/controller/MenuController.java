package com.hung.arkanoid.controller;

import com.hung.arkanoid.Main;
import com.hung.arkanoid.audio.SoundManager;
import com.hung.arkanoid.view.Fonts;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

public class MenuController implements Initializable {
    @FXML private Label lblNewGame;
    @FXML private Label lblContinue;
    @FXML private Label lblScores;
    @FXML private Label lblExit;
    @FXML private Label lblInstructions;

    private Main mainApp;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try { SoundManager.load(); } catch (Exception e) {}

        // Nút New Game -> Chuyển sang màn hình nhập tên
        setupMenuItem(lblNewGame, () -> {
            if (mainApp != null) mainApp.showNameInput();
        });

        // Nút Continue -> Chuyển sang màn hình chọn Level (Level Select)
        setupMenuItem(lblContinue, () -> {
            if (mainApp != null) mainApp.showLevelSelect();
        });

        setupMenuItem(lblScores, () -> {
            if (mainApp != null) mainApp.showHighScores();
        });
        setupMenuItem(lblInstructions, () -> {
            if (mainApp != null) mainApp.showInstructions();
        });
        setupMenuItem(lblExit, () -> {
            Platform.exit();
            System.exit(0);
        });
    }

    public void setMainApp(Main mainApp) {
        this.mainApp = mainApp;
    }

    private void setupMenuItem(Label label, Runnable action) {
        if (label == null) return;
        label.setFont(Fonts.emulogic(24));
        label.setOnMouseEntered(e -> {
            label.setScaleX(1.1);
            label.setScaleY(1.1);
        });
        label.setOnMouseExited(e -> {
            label.setScaleX(1.0);
            label.setScaleY(1.0);
        });
        label.setOnMouseClicked(e -> {
            SoundManager.playLaser();
            action.run();
        });
    }
}