package com.hung.arkanoid.model.entities.powerup;

import com.hung.arkanoid.game.GameManager;

public class MultiBallPowerUp extends PowerUp {
    public MultiBallPowerUp(double x, double y) {
        super(x, y, PowerUpType.MULTI_BALL);
    }

    @Override
    public void applyEffect(GameManager gameManager) {
        gameManager.spawnExtraBalls(2);
    }
}
