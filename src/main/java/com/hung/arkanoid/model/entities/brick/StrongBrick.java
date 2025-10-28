package com.hung.arkanoid.model.entities.brick;

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
    public StrongBrick() {
        super();
        this.width = 60;
        this.height = 20;
        this.hitPoints = 3;
    }

    public StrongBrick(double x, double y) {
        super(x, y, 60, 20, 3);
    }
}