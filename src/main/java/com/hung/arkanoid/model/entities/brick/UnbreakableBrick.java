package com.hung.arkanoid.model.entities.brick;

import com.hung.arkanoid.game.GameManager;
import com.hung.arkanoid.model.entities.Ball;
import com.hung.arkanoid.model.entities.powerup.PowerUpType;

public class UnbreakableBrick extends Brick {
    public UnbreakableBrick(double x, double y) {
        super(x, y, BrickType.UNBREAKABLE);
        this.hitPoints = Integer.MAX_VALUE;
    }

    @Override
    public int getScoreValue() {
        return 0;
    }

    @Override
    public void onImpact(GameManager gameManager, Ball ball) {
        // No effect
    }

    @Override
    public PowerUpType getPowerUpToSpawn() {
        return null;
    }
}

