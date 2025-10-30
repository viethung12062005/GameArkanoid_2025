package com.hung.arkanoid.controller;

import com.hung.arkanoid.Main;
import com.hung.arkanoid.game.SaveData;
import com.hung.arkanoid.view.Fonts;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;

import java.util.List;

public class HighScoreController {
    @FXML private GridPane highScoreGrid;
    @FXML private Label lblBack;

    private Main mainApp;

    public void setMainApp(Main mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    public void initialize() {
        if (lblBack != null) {
            lblBack.setFont(Fonts.emulogic(24));
            lblBack.setOnMouseClicked(e -> onBackClick());
            lblBack.setOnMouseEntered(e -> lblBack.setScaleX(1.1));
            lblBack.setOnMouseExited(e -> lblBack.setScaleX(1.0));
        }
        loadHighScores();
    }

    private void loadHighScores() {
        List<SaveData.HighScoreEntry> scores = SaveData.loadHighScores();

        // Xóa dữ liệu cũ
        highScoreGrid.getChildren().removeIf(node -> GridPane.getRowIndex(node) != null && GridPane.getRowIndex(node) > 0);

        if (scores.isEmpty()) {
            // Căn giữa thông báo NO DATA
            Label emptyLabel = createCellLabel("NO DATA", Color.WHITE, HPos.CENTER);
            highScoreGrid.add(emptyLabel, 1, 1);
            GridPane.setColumnSpan(emptyLabel, 3); // Gộp 3 cột
        } else {
            for (int i = 0; i < scores.size(); i++) {
                SaveData.HighScoreEntry entry = scores.get(i);
                int row = i + 1;

                // Rank: CENTER
                String rankStr = (i + 1) + getOrdinal(i + 1);
                Label lblRank = createCellLabel(rankStr, getColorForRank(i), HPos.CENTER);
                highScoreGrid.add(lblRank, 0, row);

                // Name: CENTER (Thay đổi từ LEFT sang CENTER để thẳng hàng với Header)
                Label lblName = createCellLabel(entry.name, Color.WHITE, HPos.CENTER);
                highScoreGrid.add(lblName, 1, row);

                // Score: CENTER (Thay đổi từ RIGHT sang CENTER)
                Label lblScore = createCellLabel(String.format("%06d", entry.score), Color.web("#FFD700"), HPos.CENTER);
                highScoreGrid.add(lblScore, 2, row);
            }
        }
    }

    private Label createCellLabel(String text, Color color, HPos hPos) {
        Label label = new Label(text);
        label.setFont(Fonts.emulogic(18));
        label.setTextFill(color);
        GridPane.setValignment(label, VPos.CENTER);
        GridPane.setHalignment(label, hPos);
        return label;
    }

    private Color getColorForRank(int index) {
        return switch (index) {
            case 0 -> Color.web("#FF4500"); // Top 1
            case 1 -> Color.web("#FFA500"); // Top 2
            case 2 -> Color.web("#FFFF00"); // Top 3
            default -> Color.web("#ADD8E6");
        };
    }

    private String getOrdinal(int i) {
        return switch (i % 10) {
            case 1 -> (i % 100 == 11) ? "TH" : "ST";
            case 2 -> (i % 100 == 12) ? "TH" : "ND";
            case 3 -> (i % 100 == 13) ? "TH" : "RD";
            default -> "TH";
        };
    }

    private void onBackClick() {
        if (mainApp != null) mainApp.showMenu();
    }
}