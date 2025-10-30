package com.hung.arkanoid.model.entities.brick;

import com.hung.arkanoid.game.GameManager;
import com.hung.arkanoid.model.entities.Ball;
import com.hung.arkanoid.model.entities.powerup.PowerUpType;

public class UnbreakableBrick extends Brick {
    public UnbreakableBrick(double x, double y) {
        super(x, y, BrickType.UNBREAKABLE);
        // very high hitPoints, but Brick.takeHit() early-returns for UNBREAKABLE so these values are never used
        this.hitPoints = Integer.MAX_VALUE;
        this.destroyed = false;
    }

    @Override
    public int getScoreValue() {
        // Unbreakable bricks never give score because they cannot be destroyed
        return 0;
    }

    @Override
    public void onImpact(GameManager gameManager, Ball ball) {
        // Make absolutely sure this brick stays intact regardless of any prior state
        this.destroyed = false;
        this.hitPoints = Integer.MAX_VALUE;
    }

    @Override
    public PowerUpType getPowerUpToSpawn() {
        // Unbreakable bricks never spawn powerups, but returning null is safe for shared collision code
        return null;
    }
}
