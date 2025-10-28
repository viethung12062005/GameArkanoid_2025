package com.hung.arkanoid.model.entities.brick;

import com.hung.arkanoid.game.GameManager;
import com.hung.arkanoid.model.base.GameObject;
import com.hung.arkanoid.model.entities.Ball;
import com.hung.arkanoid.model.entities.powerup.PowerUpType;

public abstract class Brick extends GameObject {
    public static final double BRICK_WIDTH = 70;
    public static final double BRICK_HEIGHT = 25;

    protected int hitPoints;
    protected BrickType type;
    protected boolean destroyed = false;

    public Brick(double x, double y, BrickType type) {
        super(x, y, BRICK_WIDTH, BRICK_HEIGHT);
        this.type = type;
    }

    public void takeHit(GameManager gameManager, Ball ball) {
        if (isUnbreakable()) return;
        this.hitPoints -= (ball != null ? ball.getDamage() : 1);
        if (this.hitPoints <= 0) {
            this.destroyed = true;
        }
    }

    public abstract PowerUpType getPowerUpToSpawn();

    public abstract void onImpact(GameManager gameManager, Ball ball);

    public abstract int getScoreValue();

    public boolean isDestroyed() {
        return destroyed;
    }

    public void setDestroyed(boolean destroyed) {
        this.destroyed = destroyed;
    }

    public int getHitPoints() {
        return hitPoints;
    }

    public void setHitPoints(int hitPoints) {
        this.hitPoints = hitPoints;
    }

    public BrickType getType() {
        return type;
    }

    public boolean isUnbreakable() {
        return this.type == BrickType.UNBREAKABLE;
    }

    @Override
    public void update(double delta) {}

    @Override
    public void render() {}
}