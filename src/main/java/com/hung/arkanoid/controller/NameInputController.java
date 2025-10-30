package com.hung.arkanoid.controller;

import com.hung.arkanoid.Main;
import com.hung.arkanoid.audio.SoundManager;
import com.hung.arkanoid.game.SaveData;
import com.hung.arkanoid.view.Fonts;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class NameInputController {
    @FXML private TextField nameField;
    @FXML private Label lblStart;
    @FXML private Label lblCancel;

    private Main mainApp;

    public void setMainApp(Main mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    public void initialize() {
        // Style font
        if (nameField != null) nameField.setFont(Fonts.emulogic(16));
        setupButton(lblStart, this::onStartGame);
        setupButton(lblCancel, this::onCancel);
    }

    private void setupButton(Label label, Runnable action) {
        if (label == null) return;
        label.setFont(Fonts.emulogic(20));
        label.setOnMouseEntered(e -> {
            label.setScaleX(1.1);
            label.setScaleY(1.1);
            label.setStyle("-fx-text-fill: #00FFFF;");
        });
        label.setOnMouseExited(e -> {
            label.setScaleX(1.0);
            label.setScaleY(1.0);
            label.setStyle("-fx-text-fill: white;");
        });
        label.setOnMouseClicked(e -> {
            SoundManager.playLaser(); // Dùng sound static nếu có, hoặc gọi từ main
            action.run();
        });
    }

    private void onStartGame() {
        String name = nameField.getText();
        if (name == null || name.trim().isEmpty()) {
            name = "Player";
        }
        // Lưu tên và Reset level về 1
        SaveData.saveCurrentPlayerName(name);
        SaveData.resetProgress();

        if (mainApp != null) {
            mainApp.startGame(1);
        }
    }

    private void onCancel() {
        if (mainApp != null) {
            mainApp.showMenu();
        }
    }
}