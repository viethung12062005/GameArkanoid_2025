package com.hung.arkanoid.controller;

import com.hung.arkanoid.game.GameManager;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;

public class GameController {
    private final GameManager gameManager;

    public GameController(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    public void setupInputHandlers(Scene scene) {
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.LEFT || event.getCode() == KeyCode.A) {
                gameManager.setMovingLeft(true);
            }
            if (event.getCode() == KeyCode.RIGHT || event.getCode() == KeyCode.D) {
                gameManager.setMovingRight(true);
            }
        });

        scene.setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.LEFT || event.getCode() == KeyCode.A) {
                gameManager.setMovingLeft(false);
            }
            if (event.getCode() == KeyCode.RIGHT || event.getCode() == KeyCode.D) {
                gameManager.setMovingRight(false);
            }
        });
    }
}
