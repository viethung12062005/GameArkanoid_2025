package com.hung.arkanoid.model.entities.powerup;

import com.hung.arkanoid.game.GameManager;

/**
 * Power-up that temporarily decreases ball speed via a multiplier.
 */
public class SlowBallPowerUp extends PowerUp {
    private static final double DURATION = 8.0; // seconds

    public SlowBallPowerUp(double x, double y) {
        super(x, y, PowerUpType.SLOW_BALL);
    }

    @Override
    public void applyEffect(GameManager gameManager) {
        gameManager.applySetBallSpeedMultiplier(0.5, DURATION);
    }
}
