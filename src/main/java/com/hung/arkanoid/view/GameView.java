package com.hung.arkanoid.view;

import com.hung.arkanoid.game.GameManager;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class GameView {
    public void render(GraphicsContext gc, GameManager gameManager) {
        // Clear the screen
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, 800, 600);

        // Draw the paddle
        gc.setFill(Color.WHITE);
        gc.fillRect(gameManager.getPaddleX(), gameManager.getPaddleY(), 100, 20);
    }
}
