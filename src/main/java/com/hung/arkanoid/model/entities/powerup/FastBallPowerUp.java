package com.hung.arkanoid.model.entities.powerup;

import com.hung.arkanoid.game.GameManager;

/**
 * Power-up that temporarily increases ball speed via a multiplier.
 */
public class FastBallPowerUp extends PowerUp {
    private static final double DURATION = 8.0; // seconds

    public FastBallPowerUp(double x, double y) {
        super(x, y, PowerUpType.FAST_BALL);
    }

    @Override
    public void applyEffect(GameManager gameManager) {
        gameManager.applySetBallSpeedMultiplier(1.5, DURATION);
    }
}
