package com.hung.arkanoid.model.entities.powerup;

import com.hung.arkanoid.game.GameManager;

public class CatchBallPowerUp extends PowerUp {
    private static final double DURATION = 10.0;

    public CatchBallPowerUp(double x, double y) {
        super(x, y, PowerUpType.CATCH);
    }

    @Override
    public void applyEffect(GameManager gameManager) {
        gameManager.applyActivateCatch(DURATION);
    }
}
