package com.hung.arkanoid.model.entities;

/*
 * ============================================================================
 * Project   : Arkanoid_OOP2025
 * Package   : com.hung.arkanoid.model
 * File Name : Ball.java
 * Created On: 10/26/2025 at 2:46 PM
 * Author    : Trần Việt Hưng
 * ----------------------------------------------------------------------------
 * Copyright (c) 2025 Hung Tran.
 * All rights reserved.
 *
 * Description:
 *     This file is part of the Arkanoid Game project.
 *     It defines the Ball class which is responsible for handling
 *     .
 *
 * Revision History:
 *     Version 1.0  - Initial release.
 * ============================================================================
 */

import com.hung.arkanoid.model.base.GameObject;
import com.hung.arkanoid.model.base.MovableObject;

public class Ball extends MovableObject {
    public static final double BASE_SPEED = 200;
    public static final double BALL_RADIUS = 10;

    private double speed;
    private int damage;

    private double speedMultiplier = 1.0;
    private boolean isFireball = false;
    private boolean isAttachedToPaddle = true;

    public Ball() {
        super();
        this.width = BALL_RADIUS * 2;
        this.height = BALL_RADIUS * 2;
        this.speed = BASE_SPEED;
        this.damage = 1;
        // default direction up-right
        this.velocityX = speed/Math.sqrt(2);
        this.velocityY = -speed/Math.sqrt(2);
    }

    public Ball(double x, double y, double diameter, double speed, int damage, double velocityX, double velocityY) {
        super(x, y, diameter, diameter, velocityX, velocityY);
        this.damage = damage;
        this.speed = speed;
        setSpeed(this.speed); // ensure velocity magnitude matches speed
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        if (Double.isNaN(speed) || speed < 0) {
            throw new IllegalArgumentException("Speed must be >= 0");
        }
        this.speed = speed;
        updateSpeedVectors();
    }

    public int getDamage() {
        return damage;
    }

    public void setDamage(int damage) {
        if (damage < 0) {
            throw new IllegalArgumentException("Damage must be >= 0");
        }
        this.damage = damage;
    }

    public void launch() {
        if (isAttachedToPaddle) {
            isAttachedToPaddle = false;
            double magnitude = BASE_SPEED * speedMultiplier;
            // set upward dy to launch
            this.velocityY = -Math.abs(magnitude);
            // if no horizontal velocity, give a small default
            if (this.velocityX == 0) this.velocityX = magnitude / Math.sqrt(2);
            else updateSpeedVectors();
        }
    }

    public void setSpeedMultiplier(double multiplier) {
        // clamp between 0.5 and 2.0
        double clamped = Math.max(0.5, Math.min(2.0, multiplier));
        this.speedMultiplier = clamped;
        updateSpeedVectors();
    }

    private void updateSpeedVectors() {
        double mag = BASE_SPEED * speedMultiplier;
        double vx = this.velocityX;
        double vy = this.velocityY;
        double curMag = Math.hypot(vx, vy);
        if (curMag == 0) {
            // default upwards-right
            this.velocityX = mag / Math.sqrt(2);
            this.velocityY = -mag / Math.sqrt(2);
        } else {
            double scale = mag / curMag;
            this.velocityX = vx * scale;
            this.velocityY = vy * scale;
        }
    }

    public void activateFireball(boolean active) {
        this.isFireball = active;
    }

    public boolean isFireball() {
        return isFireball;
    }

    public void reset() {
        this.isAttachedToPaddle = true;
        this.speedMultiplier = 1.0;
        this.isFireball = false;
        // reset velocity to default
        this.velocityX = BASE_SPEED/Math.sqrt(2);
        this.velocityY = -BASE_SPEED/Math.sqrt(2);
    }

    public boolean isAttachedToPaddle() {
        return isAttachedToPaddle;
    }

    public void setAttachedToPaddle(boolean attached) {
        this.isAttachedToPaddle = attached;
        if (attached) {
            // zero velocities while attached
            this.velocityX = 0;
            this.velocityY = 0;
        }
    }

    // New velocity accessors used by GameManager for collision response
    public double getVelocityX() { return this.velocityX; }
    public double getVelocityY() { return this.velocityY; }
    public void setVelocityX(double vx) { this.velocityX = vx; }
    public void setVelocityY(double vy) { this.velocityY = vy; }

    public void reverseDx() {
        this.velocityX = -this.velocityX;
    }

    public void reverseDy() {
        this.velocityY = -this.velocityY;
    }

    public void bounceOff(GameObject other) {
        // Simple bounce: invert Y velocity by default
        this.reverseDy();
    }

    // Center helpers (convenience for swept collision handling)
    public double getCenterX() { return this.x + this.width * 0.5; }
    public double getCenterY() { return this.y + this.height * 0.5; }
    public void setCenterX(double cx) { this.x = cx - this.width * 0.5; }
    public void setCenterY(double cy) { this.y = cy - this.height * 0.5; }

    /**
     * Apply collision response: invert velocity components according to flags and
     * place ball at corrected center position. Velocity magnitude is preserved
     * (rescaled to BASE_SPEED * speedMultiplier).
     */
    public void applyCollisionResponse(boolean inverseVx, boolean inverseVy, double correctedCenterX, double correctedCenterY) {
        if (inverseVx) this.velocityX = -this.velocityX;
        if (inverseVy) this.velocityY = -this.velocityY;
        // ensure magnitude preserved
        double mag = Math.hypot(this.velocityX, this.velocityY);
        double desired = BASE_SPEED * speedMultiplier;
        if (mag != 0) {
            double scale = desired / mag;
            this.velocityX *= scale;
            this.velocityY *= scale;
        } else {
            // fallback: assign upward velocity
            this.velocityX = desired / Math.sqrt(2);
            this.velocityY = -desired / Math.sqrt(2);
        }
        setCenterX(correctedCenterX);
        setCenterY(correctedCenterY);
    }

    public boolean checkCollision(GameObject other) {
        return this.intersects(other);
    }

    @Override
    public void update(double delta) {
        if (!isAttachedToPaddle) {
            super.update(delta);
            // keep constant speed magnitude according to current multiplier
            double vx = this.velocityX;
            double vy = this.velocityY;
            double mag = Math.hypot(vx, vy);
            double desired = BASE_SPEED * speedMultiplier;
            if (mag != 0) {
                double scale = desired / mag;
                this.velocityX = vx * scale;
                this.velocityY = vy * scale;
            }
        }
    }

    @Override
    public void render() {}
}
