package com.hung.arkanoid.model.entities.powerup;

/*
 * ============================================================================
 * Project   : Arkanoid_OOP2025
 * Package   : com.hung.arkanoid.model.entities.powerup
 * File Name : PowerUp.java
 * Created On: 10/27/2025 at 9:07 AM
 * Author    : Trần Việt Hưng
 * ----------------------------------------------------------------------------
 * Copyright (c) 2025 Hung Tran.
 * All rights reserved.
 *
 * Description:
 *     This file is part of the Arkanoid Game project.
 *     It defines the PowerUp class which is responsible for handling
 *     .
 *
 * Revision History:
 *     Version 1.0  - Initial release.
 * ============================================================================
 */

import com.hung.arkanoid.game.GameManager;
import com.hung.arkanoid.model.base.MovableObject;

public abstract class PowerUp extends MovableObject {
    public static final double POWERUP_WIDTH = 40;
    public static final double POWERUP_HEIGHT = 20;
    public static final double FALL_SPEED = 2.5;

    protected PowerUpType type;
    private boolean consumed = false;

    public PowerUp(double x, double y, PowerUpType type) {
        // initialize with vertical fall speed
        super(x, y, POWERUP_WIDTH, POWERUP_HEIGHT, 0, FALL_SPEED);
        this.type = type;
    }

    /**
     * Apply the powerup effect to the game.
     */
    public abstract void applyEffect(GameManager gameManager);

    @Override
    public void update(double delta) {
        // use MovableObject's update which moves according to velocity
        super.update(delta);
    }

    @Override
    public void render() {
        // rendering is handled by the game's view; keep empty here as placeholder
    }

    public boolean isConsumed() {
        return consumed;
    }

    public void setConsumed(boolean consumed) {
        this.consumed = consumed;
    }

    public PowerUpType getType() {
        return type;
    }
}
