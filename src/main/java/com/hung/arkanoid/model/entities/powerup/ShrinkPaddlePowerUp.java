package com.hung.arkanoid.model.entities.powerup;

import com.hung.arkanoid.game.GameManager;

/**
 * Power-up that temporarily reduces the paddle width.
 */
public class ShrinkPaddlePowerUp extends PowerUp {
    private static final double DURATION = 10.0; // seconds

    public ShrinkPaddlePowerUp(double x, double y) {
        super(x, y, PowerUpType.SHRINK);
    }

    @Override
    public void applyEffect(GameManager gameManager) {
        gameManager.applyShrinkPaddle(DURATION);
    }
}
