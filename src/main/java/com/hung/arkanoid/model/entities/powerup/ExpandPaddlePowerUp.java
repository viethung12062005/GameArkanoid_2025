package com.hung.arkanoid.model.entities.powerup;

import com.hung.arkanoid.game.GameManager;

/**
 * Power-up that temporarily increases the paddle width.
 */
public class ExpandPaddlePowerUp extends PowerUp {
    private static final double DURATION = 10.0; // seconds

    public ExpandPaddlePowerUp(double x, double y) {
        super(x, y, PowerUpType.EXPAND);
    }

    @Override
    public void applyEffect(GameManager gameManager) {
        gameManager.applyExpandPaddle(DURATION);
    }
}
