package com.hung.arkanoid.model.base;

/**
 * Base class for game objects that have a linear velocity.
 * Extends {@link GameObject} by adding horizontal and vertical velocity
 * components and a default {@link #update(double)} implementation that
 * advances the position using these velocities.
 */
public abstract class MovableObject extends GameObject {

    /** Horizontal speed in world units per second. */
    protected double velocityX;
    /** Vertical speed in world units per second. */
    protected double velocityY;

    /**
     * Creates a movable object with zero size and zero velocity.
     */
    public MovableObject() {
        super();
    }

    /**
     * Creates a movable object with the given bounds and initial velocity.
     *
     * @param x         left coordinate
     * @param y         top coordinate
     * @param width     object width
     * @param height    object height
     * @param velocityX horizontal speed
     * @param velocityY vertical speed
     */
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
     * Moves the object according to its current velocity and the elapsed
     * time step.
     *
     * @param delta time delta in seconds
     */
    public void move(double delta) {
        this.x += this.velocityX * delta;
        this.y += this.velocityY * delta;
    }

    /**
     * Default update implementation for movable objects simply delegates
     * to {@link #move(double)}.
     */
    @Override
    public void update(double delta) {
        move(delta);
    }
}