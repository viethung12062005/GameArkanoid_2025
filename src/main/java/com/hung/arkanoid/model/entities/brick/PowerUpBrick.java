package com.hung.arkanoid.model.entities.brick;

import com.hung.arkanoid.game.GameManager;
import com.hung.arkanoid.model.entities.Ball;
import com.hung.arkanoid.model.entities.powerup.PowerUpType;

import java.util.Random;

public class PowerUpBrick extends Brick {
    private static final Random RNG = new Random();

    public PowerUpBrick(double x, double y) {
        super(x, y, BrickType.POWERUP_GUARANTEED);
        this.hitPoints = 1;
    }

    @Override
    public int getScoreValue() {
        return 100;
    }

    @Override
    public void onImpact(GameManager gameManager, Ball ball) {
        // no special effect
    }

    @Override
    public PowerUpType getPowerUpToSpawn() {
        if (isDestroyed()) {
            PowerUpType[] types = PowerUpType.values();
            return types[RNG.nextInt(types.length)];
        }
        return null;
    }
}

