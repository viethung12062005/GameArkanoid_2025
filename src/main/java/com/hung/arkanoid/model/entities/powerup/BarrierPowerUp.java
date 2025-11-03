package com.hung.arkanoid.model.entities.powerup;

import com.hung.arkanoid.game.GameManager;

public class BarrierPowerUp extends PowerUp {
    private static final double DURATION = 12.0;

    public BarrierPowerUp(double x, double y) {
        super(x, y, PowerUpType.BARRIER);
    }

    @Override
    public void applyEffect(GameManager gameManager) {
        gameManager.applyBarrier(DURATION);
    }
}
