package com.hung.arkanoid.model.base;

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

public abstract class MovableObject extends GameObject {
    protected double velocityX;
    protected double velocityY;

    public MovableObject() {
        super();
    }

    public MovableObject(double x, double y, double width, double height, double velocityX, double velocityY) {
        super(x, y, width, height);
        this.velocityX = velocityX;
        this.velocityY = velocityY;
    }

    public double getVelocityX() {
        return velocityX;
    }

    public void setVelocityX(double velocityX) {
        this.velocityX = velocityX;
    }

    public double getVelocityY() {
        return velocityY;
    }

    public void setVelocityY(double velocityY) {
        this.velocityY = velocityY;
    }

    /**
     * Move object according to velocity and elapsed time delta (in seconds).
     */
    public void move(double delta) {
        this.x += this.velocityX * delta;
        this.y += this.velocityY * delta;
    }

    @Override
    public void update(double delta) {
        move(delta);
    }
}