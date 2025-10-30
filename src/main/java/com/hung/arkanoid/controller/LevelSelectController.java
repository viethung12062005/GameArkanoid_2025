package com.hung.arkanoid.controller;

import com.hung.arkanoid.Main;
import com.hung.arkanoid.audio.SoundManager;
import com.hung.arkanoid.game.LevelLoader;
import com.hung.arkanoid.game.SaveData;
import com.hung.arkanoid.view.Fonts;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.TilePane;

public class LevelSelectController {
    @FXML private TilePane levelSelectBox;
    @FXML private Label lblBack;

    private Main mainApp;

    public void setMainApp(Main mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    public void initialize() {
        // Setup nút Back
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

    private void loadLevels() {
        if (levelSelectBox == null) return;
        levelSelectBox.getChildren().clear();

        int maxUnlocked = SaveData.loadMaxLevelUnlocked();
        int totalLevels = LevelLoader.MAX_LEVELS;

        for (int i = 1; i <= totalLevels; i++) {
            Label levelLabel = new Label(String.valueOf(i));
            levelLabel.getStyleClass().add("level-item"); // Sử dụng style ô vuông
            levelLabel.setPrefSize(60, 60); // Kích thước ô to hơn chút cho đẹp
            levelLabel.setFont(Fonts.emulogic(20)); // Font to rõ

            final int level = i;

            if (i <= maxUnlocked) {
                // Level đã mở khóa -> Có hiệu ứng và click được
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
                        // Bắt đầu game tại level đã chọn với điểm 0 (vì chọn màn chơi riêng lẻ)
                        mainApp.startGame(level);
                    }
                });
            } else {
                // Level chưa mở -> Disable
                levelLabel.setDisable(true);
            }

            levelSelectBox.getChildren().add(levelLabel);
        }
    }

    private void onBackClick() {
        if (mainApp != null) {
            mainApp.showMenu();
        }
    }
}