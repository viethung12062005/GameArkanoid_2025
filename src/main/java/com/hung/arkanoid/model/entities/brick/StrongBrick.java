package com.hung.arkanoid.model.entities.brick;

import com.hung.arkanoid.game.GameManager;
import com.hung.arkanoid.model.entities.Ball;
import com.hung.arkanoid.model.entities.powerup.PowerUpType;

/**
 * Brick that requires multiple hits to be destroyed.
 * Strong bricks start with three hit points and award more score than
 * normal bricks.
 */
public class StrongBrick extends Brick {

    public StrongBrick(double x, double y) {
        super(x, y, BrickType.STRONG);
        this.hitPoints = 3;
    }

    @Override
    public int getScoreValue() {
        return 50;
    }

    @Override
    public void onImpact(GameManager gameManager, Ball ball) {
        // Strong bricks currently have no extra behaviour on impact.
    }

    @Override
    public PowerUpType getPowerUpToSpawn() {
        return null;
    }
}