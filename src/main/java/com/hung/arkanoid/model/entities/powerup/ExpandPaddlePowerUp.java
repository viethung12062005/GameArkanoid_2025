package com.hung.arkanoid.model.entities.powerup;

import com.hung.arkanoid.game.GameManager;

public class ExpandPaddlePowerUp extends PowerUp {
    private static final double DURATION = 10.0; // duration in update units

    public ExpandPaddlePowerUp(double x, double y) {
        super(x, y, PowerUpType.EXPAND);
    }

    @Override
    public void applyEffect(GameManager gameManager) {
        gameManager.applyExpandPaddle(DURATION);
    }
}
