package com.hung.arkanoid.model.base;

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