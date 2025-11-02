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

/**
 * Controller for the main menu screen.
 * It wires menu labels to navigation actions such as starting a new game,
 * continuing via the level select screen, opening the high-score view,
 * showing instructions, or exiting the application.
 */
public class MenuController implements Initializable {
    @FXML private Label lblNewGame;
    @FXML private Label lblContinue;
    @FXML private Label lblScores;
    @FXML private Label lblExit;
    @FXML private Label lblInstructions;

    private Main mainApp;

    /**
     * Initializes the menu by loading sound resources and wiring each menu
     * label to its corresponding navigation or system action.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            SoundManager.load();
        } catch (Exception ignored) {
            // If sounds fail to load, the menu still functions but will be silent.
        }

        setupMenuItem(lblNewGame, () -> {
            if (mainApp != null) {
                mainApp.showNameInput();
            }
        });

        setupMenuItem(lblContinue, () -> {
            if (mainApp != null) {
                mainApp.showLevelSelect();
            }
        });

        setupMenuItem(lblScores, () -> {
            if (mainApp != null) {
                mainApp.showHighScores();
            }
        });

        setupMenuItem(lblInstructions, () -> {
            if (mainApp != null) {
                mainApp.showInstructions();
            }
        });

        setupMenuItem(lblExit, () -> {
            Platform.exit();
            System.exit(0);
        });
    }

    /**
     * Injects the main application so this controller can request screen changes.
     *
     * @param mainApp main application instance
     */
    public void setMainApp(Main mainApp) {
        this.mainApp = mainApp;
    }

    /**
     * Applies common styling and hover/click behavior to a menu label.
     *
     * @param label  label to configure
     * @param action callback executed when the label is clicked
     */
    private void setupMenuItem(Label label, Runnable action) {
        if (label == null) {
            return;
        }
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