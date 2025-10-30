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
import com.hung.arkanoid.game.GameManager;
import com.hung.arkanoid.view.GameView;
import com.hung.arkanoid.controller.GameController;
import javafx.animation.AnimationTimer;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class MenuController implements Initializable {
    @FXML
    private Button startButton;
    @FXML
    private Button scoresButton;
    @FXML
    private Button exitButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialization logic if needed
    }

    @FXML
    private void onStartButtonClick(ActionEvent event) {
        playClickSound();

        Stage stage = (Stage) startButton.getScene().getWindow();

        // Create game components
        GameManager gameManager = new GameManager();
        GameView gameView = new GameView();
        GameController gameController = new GameController(gameManager);

        // Create canvas and scene
        Canvas canvas = new Canvas(800, 600);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        Group root = new Group(canvas);
        Scene gameScene = new Scene(root, 800, 600, Color.BLACK);

        // Set up input handlers
        gameController.setupInputHandlers(gameScene);

        // Create game loop
        AnimationTimer gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                gameManager.update();
                gameView.render(gc, gameManager);
            }
        };
        gameLoop.start();

        // Set the game scene
        stage.setScene(gameScene);
    }

    @FXML
    private void onScoresButtonClick(ActionEvent event) {
        playClickSound();
        System.out.println("High Scores Clicked!");
    }

    @FXML
    private void onExitButtonClick(ActionEvent event) {
        playClickSound();
        Stage stage = (Stage) exitButton.getScene().getWindow();
        stage.close();
        System.exit(0);
    }

    private void playClickSound() {
        System.out.println("PLAY CLICK SOUND");
    }
}

