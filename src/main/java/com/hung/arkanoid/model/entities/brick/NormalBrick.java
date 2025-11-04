package com.hung.arkanoid.model.entities.brick;

import com.hung.arkanoid.game.GameManager;
import com.hung.arkanoid.model.entities.Ball;
import com.hung.arkanoid.model.entities.powerup.PowerUpType;

import java.util.Random;

/*
 * ============================================================================
 * Project   : Arkanoid_OOP2025
 * Package   : com.hung.arkanoid.model.entities.brick
 * File Name : BrickFactory.java
 * Created On: 10/25/2025 at 9:19 PM
 * Author    : Trần Việt Hưng
 * ----------------------------------------------------------------------------
 * Copyright (c) 2025 Hung Tran.
 * All rights reserved.
 *
 * Description:
 *     This file is part of the Arkanoid Game project.
 *     It defines the BrickFactory class which is responsible for handling.
 *
 *
 * Revision History:
 *     Version 1.0  - Initial release.
 * ============================================================================
 */

public class NormalBrick extends Brick {
    private static final Random RNG = new Random();

    public NormalBrick(double x, double y) {
        super(x, y, BrickType.NORMAL);
        this.hitPoints = 1;
    }

    @Override
    public int getScoreValue() {
        return 10;
    }

    @Override
    public void onImpact(GameManager gameManager, Ball ball) {
        // No special behavior
    }

    @Override
    public PowerUpType getPowerUpToSpawn() {
        if (isDestroyed() && RNG.nextDouble() < 0.2) {
            PowerUpType[] types = PowerUpType.values();
            return types[RNG.nextInt(types.length)];
        }
        return null;
    }
}