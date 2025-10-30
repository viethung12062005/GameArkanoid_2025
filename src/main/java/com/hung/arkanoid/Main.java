package com.hung.arkanoid;

import com.hung.arkanoid.controller.MenuController;
import com.hung.arkanoid.controller.InstructionsController;
import com.hung.arkanoid.controller.HighScoreController;
import com.hung.arkanoid.controller.GameController;
import com.hung.arkanoid.game.GameManager;
import com.hung.arkanoid.view.GameView;
import com.hung.arkanoid.game.SaveData;
import com.hung.arkanoid.controller.NameInputController;
import com.hung.arkanoid.controller.LevelSelectController;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class Main extends Application {
    private Stage primaryStage;
    private Scene menuScene;
    private AnimationTimer gameLoop;
    private long lastUpdateNano = -1L;

    @Override
    public void start(Stage stage) throws Exception {
        this.primaryStage = stage;
        // Load Menu
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainMenu.fxml"));
        Parent root = loader.load();
        MenuController menuController = loader.getController();
        menuController.setMainApp(this);

        this.menuScene = new Scene(root, 800, 600);
        // Store controller để dùng lại khi quay về menu
        this.menuScene.setUserData(menuController);

        // Load CSS nếu cần
        try {
            menuScene.getStylesheets().add(getClass().getResource("/styles/menu.css").toExternalForm());
        } catch (Exception e) { System.err.println("Lỗi load CSS menu"); }

        primaryStage.setTitle("Arkanoid");
        primaryStage.setScene(menuScene);
        primaryStage.show();
    }

    // [OVERLOAD 2] Hàm startGame chính có tham số điểm
    public void startGame(int levelNumber, int currentScore) {
        // Truyền điểm tích lũy vào GameManager
        GameManager gameManager = new GameManager(levelNumber, currentScore);
        GameView gameView = new GameView();
        GameController gameController = new GameController(gameManager, this);

        Canvas canvas = new Canvas(800, 600);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        Group root = new Group(canvas);
        Scene gameScene = new Scene(root, 800, 600, Color.BLACK);

        gameController.setupInputHandlers(gameScene);

        if (gameLoop != null) gameLoop.stop();
        lastUpdateNano = -1L;

        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                double deltaSeconds = (lastUpdateNano <= 0) ? 1.0 / 60.0 : (now - lastUpdateNano) / 1e9;
                lastUpdateNano = now;

                gameController.update();
                gameManager.update(deltaSeconds);
                gameView.render(gc, gameManager);

                switch (gameManager.getCurrentState()) {
                    case LEVEL_CLEARED -> {
                        stop();
                        // Lấy điểm tích lũy
                        int accumulatedScore = gameManager.getScore();

                        // Cập nhật High Score ngay lập tức
                        String currentPlayer = SaveData.getCurrentPlayerName();
                        SaveData.updateHighScore(currentPlayer, accumulatedScore);

                        // Chuyển sang màn tiếp theo với điểm số ĐƯỢC GIỮ NGUYÊN
                        startGame(gameManager.getNextLevel(), accumulatedScore);
                    }
                    case GAME_OVER -> {
                        stop();
                        // Cập nhật High Score lần cuối
                        String currentPlayer = SaveData.getCurrentPlayerName();
                        SaveData.updateHighScore(currentPlayer, gameManager.getScore());

                        showMenu();
                    }
                    default -> {}
                }
            }
        };
        gameLoop.start();
        primaryStage.setScene(gameScene);
    }

    public void showMenu() {
        if (gameLoop != null) gameLoop.stop();
        primaryStage.setScene(menuScene);
    }

    public void showInstructions() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Instructions.fxml"));
            Parent root = loader.load();

            // Lấy controller và truyền Main vào
            InstructionsController controller = loader.getController();
            controller.setMainApp(this);

            // Đặt Scene mới lên primaryStage thay vì tạo cửa sổ mới
            Scene scene = new Scene(root, 800, 600);
            // Có thể add CSS chung
            try { scene.getStylesheets().add(getClass().getResource("/styles/menu.css").toExternalForm()); } catch(Exception e){}

            primaryStage.setScene(scene);
        } catch (Exception ex) {
            System.err.println("Không mở được Hướng dẫn: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void showLevelSelect() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LevelSelect.fxml"));
            Parent root = loader.load();

            // Lấy controller và truyền MainApp vào để có thể gọi startGame
            LevelSelectController controller = loader.getController();
            controller.setMainApp(this);

            Scene scene = new Scene(root, 800, 600);
            // Load CSS để đảm bảo giao diện đồng bộ
            try {
                scene.getStylesheets().add(getClass().getResource("/styles/menu.css").toExternalForm());
            } catch (Exception e) {
                System.err.println("Error loading css: " + e.getMessage());
            }

            primaryStage.setScene(scene);
        } catch (Exception ex) {
            System.err.println("Failed to open Level Select: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void showHighScores() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/HighScore.fxml"));
            Parent root = loader.load();

            // Lấy controller và truyền Main vào
            HighScoreController controller = loader.getController();
            controller.setMainApp(this);

            Scene scene = new Scene(root, 800, 600);
            primaryStage.setScene(scene);
        } catch (Exception ex) {
            System.err.println("Không mở được High Scores: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // Thêm hàm start game từ tham số level
    public void startGame(int levelNumber) {
        startGame(levelNumber, 0);
    }

    public void showNameInput() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/NameInput.fxml"));
            Parent root = loader.load();
            NameInputController controller = loader.getController();
            controller.setMainApp(this);
            Scene scene = new Scene(root, 800, 600);
            primaryStage.setScene(scene);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}