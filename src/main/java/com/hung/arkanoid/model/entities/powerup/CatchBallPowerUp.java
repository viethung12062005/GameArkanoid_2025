package com.hung.arkanoid.model.entities.powerup;

import com.hung.arkanoid.game.GameManager;

/**
 * Power-up that enables the "catch" behaviour on the paddle so balls
 * can temporarily stick on impact.
 */
public class CatchBallPowerUp extends PowerUp {
    private static final double DURATION = 10.0; // seconds

    public CatchBallPowerUp(double x, double y) {
        super(x, y, PowerUpType.CATCH);
    }

    @Override
    public void applyEffect(GameManager gameManager) {
        gameManager.applyActivateCatch(DURATION);
    }
}
