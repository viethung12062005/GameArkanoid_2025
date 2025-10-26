package com.hung.arkanoid.model.entities.brick;

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