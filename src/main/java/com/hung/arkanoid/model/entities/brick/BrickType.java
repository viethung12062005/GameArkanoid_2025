package com.hung.arkanoid.model.entities.brick;

/*
 * ============================================================================
 * Project   : Arkanoid_OOP2025
 * Package   : com.hung.arkanoid.model.entities.brick
 * File Name : BrickType.java
 * Created On: 10/25/2025 at 9:14 PM
 * Author    : Trần Việt Hưng
 * ----------------------------------------------------------------------------
 * Copyright (c) 2025 Hung Tran.
 * All rights reserved.
 *
 * Description:
 *     This file is part of the Arkanoid Game project.
 *     It defines the BrickType class which is responsible for handling.
 *
 * Revision History:
 *     Version 1.0  - Initial release.
 * ============================================================================
 */

public enum BrickType {
    NORMAL(60, 20, 1),
    STRONG(60, 20, 3),
    WIDE(120, 20, 2);

    private final double width;
    private final double height;
    private final int hitPoints;

    BrickType(double width, double height, int hitPoints) {
        this.width = width;
        this.height = height;
        this.hitPoints = hitPoints;
    }

    public double getWidth() { return width; }
    public double getHeight() { return height; }
    public int getHitPoints() { return hitPoints; }

    public Brick create(double x, double y) {
        return switch (this) {
            case NORMAL -> new NormalBrick(x, y);
            case STRONG -> new StrongBrick(x, y);
            case WIDE -> new Brick(x, y, width, height, hitPoints);
            default -> new Brick(x, y, width, height, hitPoints);
        };
    }
}