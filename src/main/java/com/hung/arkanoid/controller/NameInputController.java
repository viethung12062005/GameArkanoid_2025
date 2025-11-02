package com.hung.arkanoid.controller;

import com.hung.arkanoid.Main;
import com.hung.arkanoid.audio.SoundManager;
import com.hung.arkanoid.game.SaveData;
import com.hung.arkanoid.view.Fonts;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/**
 * Controller for the name input screen shown before starting a new game.
 * It collects the player name, persists it via {@link SaveData} and
 * then starts the first level using the {@link Main} application.
 */
public class NameInputController {
    @FXML private TextField nameField;
    @FXML private Label lblStart;
    @FXML private Label lblCancel;

    private Main mainApp;

    /**
     * Injects the main application reference so this controller can
     * navigate to the game screen or back to the main menu.
     *
     * @param mainApp main application instance
     */
    public void setMainApp(Main mainApp) {
        this.mainApp = mainApp;
    }

    /**
     * Initializes the UI components after FXML loading.
     * Configures the font for the input field and wires the start/cancel buttons.
     */
    @FXML
    public void initialize() {
        if (nameField != null) {
            nameField.setFont(Fonts.emulogic(16));
        }
        setupButton(lblStart, this::onStartGame);
        setupButton(lblCancel, this::onCancel);
    }

    /**
     * Applies common styling and mouse interaction behavior to a label
     * so that it behaves like a menu button.
     *
     * @param label  label to configure
     * @param action action to execute when the label is clicked
     */
    private void setupButton(Label label, Runnable action) {
        if (label == null) {
            return;
        }
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
            // Play a click sound before executing the associated action.
            SoundManager.playLaser();
            action.run();
        });
    }

    /**
     * Validates the entered player name, persists it and starts the game at level 1.
     * If the input is empty, a default name "Player" is used.
     */
    private void onStartGame() {
        String name = nameField.getText();
        if (name == null || name.trim().isEmpty()) {
            name = "Player";
        }
        SaveData.saveCurrentPlayerName(name);
        SaveData.resetProgress();

        if (mainApp != null) {
            mainApp.startGame(1);
        }
    }

    /**
     * Navigates back to the main menu without starting a game.
     */
    private void onCancel() {
        if (mainApp != null) {
            mainApp.showMenu();
        }
    }
}