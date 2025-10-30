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
    public static final double BASE_WIDTH = 100.0;
    public static final double BASE_HEIGHT = 20.0;
    public static final double PADDLE_SPEED = 8.0;

    private double speed;

    private boolean lasersActive = false;
    private boolean catchActive = false;

    public Paddle() {
        super();
        this.speed = 300;
        this.width = BASE_WIDTH;
        this.height = BASE_HEIGHT;
    }

    public Paddle(double x, double y) {
        super(x, y, BASE_WIDTH, BASE_HEIGHT, 0, 0);
        this.speed = 300;
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

    // Movement controls using PADDLE_SPEED units per frame (or per update scale)
    public void moveLeft() {
        this.velocityX = -Math.abs(PADDLE_SPEED);
    }

    public void moveRight() {
        this.velocityX = Math.abs(PADDLE_SPEED);
    }

    public void stopMoving() {
        this.velocityX = 0;
    }

    public void expand() {
        double centerX = this.x + this.width / 2.0;
        this.width = BASE_WIDTH * 1.5;
        // keep center position
        this.x = centerX - this.width / 2.0;
    }

    public void shrink() {
        double centerX = this.x + this.width / 2.0;
        this.width = BASE_WIDTH * 0.75;
        this.x = centerX - this.width / 2.0;
    }

    public void reset() {
        double centerX = this.x + this.width / 2.0;
        this.width = BASE_WIDTH;
        this.x = centerX - this.width / 2.0;
        this.lasersActive = false;
        this.catchActive = false;
    }

    /**
     * Reset paddle size only (do not touch lasers/catch flags).
     * Used when size-changing powerups expire so we don't inadvertently
     * disable other active effects like lasers.
     */
    public void resetSize() {
        double centerX = this.x + this.width / 2.0;
        this.width = BASE_WIDTH;
        this.x = centerX - this.width / 2.0;
    }

    public boolean areLasersActive() {
        return lasersActive;
    }

    public void setLasersActive(boolean lasersActive) {
        this.lasersActive = lasersActive;
        // Debug log to trace laser activation/deactivation
        System.out.println("[DEBUG] Paddle.setLasersActive -> " + lasersActive + " at " + System.currentTimeMillis());
        // Print brief stack trace to identify caller
        StackTraceElement[] st = Thread.currentThread().getStackTrace();
        for (int i = 2; i < Math.min(st.length, 8); i++) {
            System.out.println("    at " + st[i]);
        }
    }

    public boolean isCatchActive() {
        return catchActive;
    }

    public void setCatchActive(boolean catchActive) {
        this.catchActive = catchActive;
    }

    @Override
    public void render() {}

    @Override
    public void update(double delta) {
        super.update(delta);
    }
}
