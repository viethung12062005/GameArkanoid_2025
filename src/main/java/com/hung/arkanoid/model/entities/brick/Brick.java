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

import com.hung.arkanoid.model.base.GameObject;

public class Brick extends GameObject {
    protected int hitPoints;

    public Brick() {
        super();
    }

    public Brick(double x, double y, double width, double height, int hitPoints) {
        super(x, y, width, height);
        this.hitPoints = hitPoints;
    }

    public int getHitPoints() {
        return hitPoints;
    }

    public void setHitPoints(int hitPoints) {
        this.hitPoints = hitPoints;
    }

    public boolean takeHit(int damage) {
        if (hitPoints <= 0) {
            return false;
        }
        this.hitPoints -= damage;
        return isDestroyed();
    }

    public boolean isDestroyed() {
        return hitPoints <= 0;
    }


    @Override
    public void update(double delta) {
    }

    @Override
    public void render() {
    }
}