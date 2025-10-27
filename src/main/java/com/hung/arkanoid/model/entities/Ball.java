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
    private double speed;
    private int damage;
    public Ball() {
        super();
        this.width = 12;
        this.height = 12;
        this.speed = 300;
        this.damage = 1;
        this.velocityX = speed/Math.sqrt(2);
        this.velocityY = -speed/Math.sqrt(2);
    }

    public Ball(double x, double y, double diameter, double speed, int damage, double velocityX, double velocityY) {
        super(x, y, diameter, diameter, velocityX, velocityY);
        this.damage = damage;
        this.speed = speed;
        setSpeed(this.speed); // Đảm bảo velocityX/velocityY có magnitude = speed
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        if (Double.isNaN(speed) || speed < 0) {
            throw new IllegalArgumentException("Speed must be >= 0");
        }
        this.speed = speed;
        double vx = this.velocityX;
        double vy = this.velocityY;
        double mag = Math.hypot(vx, vy);
        if (mag == 0) {
            // Chọn hướng mặc định khi vận tốc ban đầu là 0 (theo góc 45 độ lên trên bên phải)
            this.velocityX = speed/Math.sqrt(2);
            this.velocityY = -speed/Math.sqrt(2);
        } else {
            double scale = speed / mag;
            this.velocityX = vx * scale;
            this.velocityY = vy * scale;
        }
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

    public void bounceOff(GameObject other) {
        // Simple bounce: invert Y velocity by default
        this.velocityY = -this.velocityY;
    }

    public boolean checkCollision(GameObject other) {
        return this.intersects(other);
    }

    @Override
    public void update(double delta) {
        super.move(delta);
        // Keep constant speed magnitude
        double vx = this.velocityX;
        double vy = this.velocityY;;
        double mag = Math.sqrt(vx * vx + vy * vy);
        if (mag == 0) return;
        double scale = speed / mag;
        this.velocityX = vx * scale;
        this.velocityY = vy * scale;
    }

    @Override
    public void render() {}
}
