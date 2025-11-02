package com.hung.arkanoid.model.entities.powerup;

import com.hung.arkanoid.game.GameManager;

public class FireBallPowerUp extends PowerUp {
    private static final double DURATION = 8.0;

    public FireBallPowerUp(double x, double y) {
        super(x, y, PowerUpType.FIRE_BALL);
    }

    @Override
    public void applyEffect(GameManager gameManager) {
        gameManager.applyFireball(DURATION);
    }
}
