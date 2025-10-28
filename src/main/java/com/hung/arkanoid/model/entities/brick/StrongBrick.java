package com.hung.arkanoid.model.entities.brick;

import com.hung.arkanoid.game.GameManager;
import com.hung.arkanoid.model.entities.Ball;
import com.hung.arkanoid.model.entities.powerup.PowerUpType;

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
        // No special behavior for now
    }

    @Override
    public PowerUpType getPowerUpToSpawn() {
        return null;
    }
}