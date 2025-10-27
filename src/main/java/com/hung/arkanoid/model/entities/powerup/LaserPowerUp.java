package com.hung.arkanoid.model.entities.powerup;

import com.hung.arkanoid.game.GameManager;

public class LaserPowerUp extends PowerUp {
    private static final double DURATION = 10.0;

    public LaserPowerUp(double x, double y) {
        super(x, y, PowerUpType.LASER);
    }

    @Override
    public void applyEffect(GameManager gameManager) {
        gameManager.applyActivateLasers(DURATION);
    }
}
