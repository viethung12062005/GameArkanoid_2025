package com.hung.arkanoid.model.entities.powerup;

import com.hung.arkanoid.game.GameManager;

public class FastBallPowerUp extends PowerUp {
    private static final double DURATION = 8.0;

    public FastBallPowerUp(double x, double y) {
        super(x, y, PowerUpType.FAST_BALL);
    }

    @Override
    public void applyEffect(GameManager gameManager) {
        gameManager.applySetBallSpeedMultiplier(1.5, DURATION);
    }
}
