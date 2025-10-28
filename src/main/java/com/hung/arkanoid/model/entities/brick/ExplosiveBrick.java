package com.hung.arkanoid.model.entities.brick;

import com.hung.arkanoid.game.GameManager;
import com.hung.arkanoid.model.entities.Ball;
import com.hung.arkanoid.model.entities.powerup.PowerUpType;

public class ExplosiveBrick extends Brick {
    public ExplosiveBrick(double x, double y) {
        super(x, y, BrickType.EXPLOSIVE);
        this.hitPoints = 1;
    }

    @Override
    public int getScoreValue() {
        return 150;
    }

    @Override
    public PowerUpType getPowerUpToSpawn() {
        return null;
    }

    @Override
    public void onImpact(GameManager gameManager, Ball ball) {
        if (this.hitPoints <= 1) {
            gameManager.explodeBricksAround(this);
        }
    }
}

