package com.hung.arkanoid.model.entities.powerup;

import com.hung.arkanoid.game.GameManager;

public class ExtraLifePowerUp extends PowerUp {
    public ExtraLifePowerUp(double x, double y) {
        super(x, y, PowerUpType.EXTRA_LIFE);
    }

    @Override
    public void applyEffect(GameManager gameManager) {
        gameManager.addLife();
    }
}
