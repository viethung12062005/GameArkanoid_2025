package com.hung.arkanoid;

import com.hung.arkanoid.controller.MenuController;
import com.hung.arkanoid.controller.GameController;
import com.hung.arkanoid.game.GameManager;
import com.hung.arkanoid.view.GameView;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    private Stage primaryStage;
    private Scene menuScene;
    private AnimationTimer gameLoop;
    private long lastUpdateNano = -1L;

    @Override
    public void start(Stage stage) throws Exception {
        this.primaryStage = stage;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainMenu.fxml"));
        Parent root = loader.load();
        MenuController menuController = loader.getController();
        menuController.setMainApp(this);
        // store controller on the scene's user data for quick access
        this.menuScene = new Scene(root, 800, 600);
        this.menuScene.setUserData(menuController);
        // load menu stylesheet
        try {
            menuScene.getStylesheets().add(getClass().getResource("/styles/menu.css").toExternalForm());
        } catch (Exception ex) {
            System.err.println("Could not load menu stylesheet: " + ex.getMessage());
        }

        primaryStage.setTitle("Arkanoid");
        primaryStage.setScene(menuScene);
        primaryStage.show();
    }

    public void startGame(int levelNumber) {
        // create game objects
        GameManager gameManager = new GameManager(levelNumber);
        GameView gameView = new GameView();
        GameController gameController = new GameController(gameManager, this);

        // create canvas and scene
        Canvas canvas = new Canvas(800, 600);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        Group root = new Group(canvas);
        Scene gameScene = new Scene(root, 800, 600, Color.BLACK);

        // input handlers
        gameController.setupInputHandlers(gameScene);

        // game loop
        if (gameLoop != null) gameLoop.stop();
        // reset lastUpdateNano so the first frame uses a stable small delta
        lastUpdateNano = -1L;
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                // compute delta seconds since last frame
                double deltaSeconds;
                if (lastUpdateNano <= 0) {
                    deltaSeconds = 1.0 / 60.0; // assume 60 fps for first frame
                } else {
                    deltaSeconds = (now - lastUpdateNano) / 1_000_000_000.0;
                }
                lastUpdateNano = now;

                // let controller update (mouse -> paddle), then game logic
                gameController.update();
                gameManager.update(deltaSeconds);
                gameView.render(gc, gameManager);

                // check state transitions
                switch (gameManager.getCurrentState()) {
                    case LEVEL_CLEARED -> {
                        stop();
                        startGame(gameManager.getNextLevel());
                    }
                    case GAME_OVER -> {
                        stop();
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
        // refresh level buttons using stored controller
        Object ud = menuScene.getUserData();
        if (ud instanceof MenuController mc) {
            mc.refreshLevelButtons();
        }
        primaryStage.setScene(menuScene);
    }

    public void showInstructions() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Instructions.fxml"));
            Parent root = loader.load();
            Scene instructionsScene = new Scene(root, 600, 400);
            Stage instructionsStage = new Stage();
            instructionsStage.initOwner(primaryStage);
            instructionsStage.initModality(Modality.APPLICATION_MODAL);
            instructionsStage.setTitle("Hướng dẫn");
            instructionsStage.setScene(instructionsScene);
            instructionsStage.show();
        } catch (Exception ex) {
            System.err.println("Failed to open instructions: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
