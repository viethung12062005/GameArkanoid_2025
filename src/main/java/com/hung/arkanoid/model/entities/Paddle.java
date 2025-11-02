package com.hung.arkanoid.model.entities;

import com.hung.arkanoid.model.base.MovableObject;

/**
 * Player-controlled paddle at the bottom of the playfield.
 * The paddle can move horizontally, change width when power-ups are
 * applied and expose flags for lasers and catch effects.
 */
public class Paddle extends MovableObject {
    public static final double BASE_WIDTH = 100.0;
    public static final double BASE_HEIGHT = 20.0;
    public static final double PADDLE_SPEED = 8.0;

    private double speed;

    private boolean lasersActive = false;
    private boolean catchActive = false;

    /**
     * Creates a paddle with default size located at the origin.
     */
    public Paddle() {
        super();
        this.speed = 300;
        this.width = BASE_WIDTH;
        this.height = BASE_HEIGHT;
    }

    /**
     * Creates a paddle with default size positioned at the given
     * coordinates.
     */
    public Paddle(double x, double y) {
        super(x, y, BASE_WIDTH, BASE_HEIGHT, 0, 0);
        this.speed = 300;
    }

    /**
     * Creates a paddle with explicit bounds and movement speed.
     */
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

    /**
     * Starts moving the paddle to the left at a fixed speed.
     */
    public void moveLeft() {
        this.velocityX = -Math.abs(PADDLE_SPEED);
    }

    /**
     * Starts moving the paddle to the right at a fixed speed.
     */
    public void moveRight() {
        this.velocityX = Math.abs(PADDLE_SPEED);
    }

    /**
     * Stops horizontal paddle movement.
     */
    public void stopMoving() {
        this.velocityX = 0;
    }

    /**
     * Expands the paddle width while preserving its centre position.
     */
    public void expand() {
        double centerX = this.x + this.width / 2.0;
        this.width = BASE_WIDTH * 1.5;
        this.x = centerX - this.width / 2.0;
    }

    /**
     * Shrinks the paddle width while preserving its centre position.
     */
    public void shrink() {
        double centerX = this.x + this.width / 2.0;
        this.width = BASE_WIDTH * 0.75;
        this.x = centerX - this.width / 2.0;
    }

    /**
     * Resets paddle size and disables lasers and catch effects.
     */
    public void reset() {
        double centerX = this.x + this.width / 2.0;
        this.width = BASE_WIDTH;
        this.x = centerX - this.width / 2.0;
        this.lasersActive = false;
        this.catchActive = false;
    }

    /**
     * Resets only the paddle width while keeping active effect flags.
     * Used when size-changing effects expire.
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
    }

    public boolean isCatchActive() {
        return catchActive;
    }

    public void setCatchActive(boolean catchActive) {
        this.catchActive = catchActive;
    }

    @Override
    public void render() {
        // Rendering is handled by the view layer.
    }

    @Override
    public void update(double delta) {
        super.update(delta);
    }
}
