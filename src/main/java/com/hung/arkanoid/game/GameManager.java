package com.hung.arkanoid.game;

public class GameManager {
    private double paddleX = 350;
    private double paddleY = 550;
    private boolean movingLeft = false;
    private boolean movingRight = false;

    public double getPaddleX() {
        return paddleX;
    }

    public double getPaddleY() {
        return paddleY;
    }

    public void setMovingLeft(boolean value) {
        movingLeft = value;
    }

    public void setMovingRight(boolean value) {
        movingRight = value;
    }

    public void update() {
        if (movingLeft) {
            paddleX -= 5;
        }
        if (movingRight) {
            paddleX += 5;
        }
        // Keep the paddle within screen boundaries
        paddleX = Math.max(0, Math.min(paddleX, 800 - 100));
    }
}
