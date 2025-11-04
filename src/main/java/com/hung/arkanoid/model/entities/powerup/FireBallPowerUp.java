package com.hung.arkanoid.model.entities.powerup;

import com.hung.arkanoid.game.GameManager;

/**
 * Power-up that turns all balls into fireballs, allowing them to pass
 * through bricks for a limited duration.
 */
public class FireBallPowerUp extends PowerUp {
    private static final double DURATION = 8.0; // seconds

    public FireBallPowerUp(double x, double y) {
        super(x, y, PowerUpType.FIRE_BALL);
    }

    @Override
    public void applyEffect(GameManager gameManager) {
        gameManager.applyFireball(DURATION);
    }
}
