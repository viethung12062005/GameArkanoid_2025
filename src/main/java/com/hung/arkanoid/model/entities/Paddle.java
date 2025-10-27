package com.hung.arkanoid.model.entities;

/*
 * ============================================================================  
 * Project   : Arkanoid_OOP2025
 * Package   : com.hung.arkanoid.model.entities
 * File Name : Paddle.java
 * Created On: 10/26/2025 at 3:06 PM
 * Author    : Trần Việt Hưng
 * ----------------------------------------------------------------------------
 * Copyright (c) 2025 Hung Tran.
 * All rights reserved.
 * 
 * Description:
 *     This file is part of the Arkanoid Game project.
 *     It defines the Paddle class which is responsible for handling
 *     .
 * 
 * Revision History:
 *     Version 1.0  - Initial release.
 * ============================================================================
 */

import com.hung.arkanoid.model.base.MovableObject;

public class Paddle extends MovableObject {
    private double speed;

    public Paddle() {
        super();
        this.speed = 300;
        this.width = 80;
        this.height = 20;
    }

    public Paddle(double x, double y, double width, double height, double speed) {
        super(x, y, width, height, 0, 0);
        this.speed = speed;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public void moveLeft() {
        this.velocityX = -Math.abs(speed);
    }

    public void moveRight() {
        this.velocityX = Math.abs(speed);
    }

    @Override
    public void render() {}

    @Override
    public void update(double delta) {
        // Move horizontally according to velocity and delta
        super.update(delta);
        // TODO: clamp position to screen bounds (GameManager will provide screen size)
    }
}
