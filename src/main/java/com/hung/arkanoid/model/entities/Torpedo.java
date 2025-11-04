package com.hung.arkanoid.model.entities;

import com.hung.arkanoid.model.base.MovableObject;

/**
 * Simple upward-moving projectile fired by paddle when lasers are active.
 */
public class Torpedo extends MovableObject {
    private boolean toBeRemoved = false;

    public static final double WIDTH = 8;
    public static final double HEIGHT = 18;

    public Torpedo(double centerX, double topY, double speedY) {
        super(centerX - WIDTH * 0.5, topY, WIDTH, HEIGHT, 0, speedY);
    }

    public boolean isToBeRemoved() { return toBeRemoved; }
    public void setToBeRemoved(boolean v) { this.toBeRemoved = v; }

    @Override
    public void update(double delta) {
        // move according to velocity
        move(delta);
    }

    @Override
    public void render() {
        // rendering is handled by GameView
    }
}

