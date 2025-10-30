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

import com.hung.arkanoid.model.base.MovableObject;

public abstract class PowerUp extends MovableObject {
    public PowerUp(double x, double y, double width, double height, double velocityX, double velocityY) {
        super(x, y, width, height, velocityX, velocityY);
    }

    public abstract void activate(); // Kích hoạt hiệu ứng của PowerUp
}

