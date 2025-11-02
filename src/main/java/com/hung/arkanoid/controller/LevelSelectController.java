package com.hung.arkanoid.controller;

import com.hung.arkanoid.Main;
import com.hung.arkanoid.audio.SoundManager;
import com.hung.arkanoid.game.LevelLoader;
import com.hung.arkanoid.game.SaveData;
import com.hung.arkanoid.view.Fonts;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.TilePane;

/**
 * Controller for the level selection screen.
 * It renders one label per level in a {@link TilePane}, enabling click
 * interaction only for levels that are unlocked according to {@link SaveData}.
 */
public class LevelSelectController {
    @FXML private TilePane levelSelectBox;
    @FXML private Label lblBack;

    private Main mainApp;

    /**
     * Injects the main application so that this controller can start
     * a level or navigate back to the main menu.
     *
     * @param mainApp main application instance
     */
    public void setMainApp(Main mainApp) {
        this.mainApp = mainApp;
    }

    /**
     * Initializes the level selection view by configuring the back button
     * and populating the level grid based on unlocked progress.
     */
    @FXML
    public void initialize() {
        // Setup Back button
        if (lblBack != null) {
            lblBack.setFont(Fonts.emulogic(24));
            lblBack.setOnMouseClicked(e -> onBackClick());
            lblBack.setOnMouseEntered(e -> {
                lblBack.setScaleX(1.1);
                lblBack.setScaleY(1.1);
            });
            lblBack.setOnMouseExited(e -> {
                lblBack.setScaleX(1.0);
                lblBack.setScaleY(1.0);
            });
        }

        loadLevels();
    }

    /**
     * Populates the {@link TilePane} with labels representing each level.
     * Unlocked levels are interactive, while locked levels are disabled.
     */
    private void loadLevels() {
        if (levelSelectBox == null) {
            return;
        }
        levelSelectBox.getChildren().clear();

        int maxUnlocked = SaveData.loadMaxLevelUnlocked();
        int totalLevels = LevelLoader.MAX_LEVELS;

        for (int i = 1; i <= totalLevels; i++) {
            Label levelLabel = new Label(String.valueOf(i));
            levelLabel.getStyleClass().add("level-item"); // Use square style for level item
            levelLabel.setPrefSize(60, 60); // Slightly larger square size for better appearance
            levelLabel.setFont(Fonts.emulogic(20)); // Clear and large font

            final int level = i;

            if (i <= maxUnlocked) {
                // Unlocked level: highlight on hover and start game on click.
                levelLabel.setOnMouseEntered(e -> {
                    levelLabel.setScaleX(1.1);
                    levelLabel.setScaleY(1.1);
                });
                levelLabel.setOnMouseExited(e -> {
                    levelLabel.setScaleX(1.0);
                    levelLabel.setScaleY(1.0);
                });
                levelLabel.setOnMouseClicked(e -> {
                    SoundManager.playLaser();
                    if (mainApp != null) {
                        // Start the selected level as a standalone game with score reset.
                        mainApp.startGame(level);
                    }
                });
            } else {
                // Locked level: visually disabled and not clickable.
                levelLabel.setDisable(true);
            }

            levelSelectBox.getChildren().add(levelLabel);
        }
    }

    /**
     * Navigates back to the main menu.
     */
    private void onBackClick() {
        if (mainApp != null) {
            mainApp.showMenu();
        }
    }
}