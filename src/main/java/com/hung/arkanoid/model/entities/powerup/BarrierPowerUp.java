package com.hung.arkanoid.model.entities.powerup;

import com.hung.arkanoid.game.GameManager;

/**
 * Power-up that spawns a temporary barrier at the bottom of the
 * playfield, preventing balls from being lost.
 */
public class BarrierPowerUp extends PowerUp {
    private static final double DURATION = 12.0; // seconds

    public BarrierPowerUp(double x, double y) {
        super(x, y, PowerUpType.BARRIER);
    }

    @Override
    public void applyEffect(GameManager gameManager) {
        gameManager.applyBarrier(DURATION);
    }
}
