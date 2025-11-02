package com.hung.arkanoid.model.entities.brick;

import com.hung.arkanoid.game.GameManager;
import com.hung.arkanoid.model.entities.Ball;
import com.hung.arkanoid.model.entities.powerup.PowerUpType;

/**
 * Brick that cannot be destroyed by normal ball hits.
 * Unbreakable bricks use {@link Integer#MAX_VALUE} hit points and always
 * return a score value of zero.
 */
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
        // Unbreakable bricks ignore hits apart from visual feedback.
    }

    @Override
    public PowerUpType getPowerUpToSpawn() {
        return null;
    }
}
