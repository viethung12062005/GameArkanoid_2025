package com.hung.arkanoid.model.entities.powerup;

import com.hung.arkanoid.game.GameManager;

public class SlowBallPowerUp extends PowerUp {
    private static final double DURATION = 8.0;

    public SlowBallPowerUp(double x, double y) {
        super(x, y, PowerUpType.SLOW_BALL);
    }

    @Override
    public void applyEffect(GameManager gameManager) {
        gameManager.applySetBallSpeedMultiplier(0.5, DURATION);
    }
}
