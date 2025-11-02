package com.hung.arkanoid.model.entities;

import com.hung.arkanoid.model.base.GameObject;
import com.hung.arkanoid.model.base.MovableObject;

/**
 * Ball entity used in the Arkanoid playfield.
 * Stores speed, damage, fireball state and attachment to the paddle. The
 * {@link #update(double)} method moves the ball whenever it is not attached
 * and keeps the velocity magnitude in sync with the configured speed
 * multiplier.
 */
public class Ball extends MovableObject {
    /** Base movement speed used as reference for all multipliers. */
    public static final double BASE_SPEED = 300;
    /** Default ball radius in world units. */
    public static final double BALL_RADIUS = 10;

    private double speed;
    private int damage;

    private double speedMultiplier = 1.0;
    private boolean isFireball = false;
    private boolean isAttachedToPaddle = true;

    /**
     * Creates a ball at the origin with default size, speed and an
     * initial up-right direction.
     */
    public Ball() {
        super();
        this.width = BALL_RADIUS * 2;
        this.height = BALL_RADIUS * 2;
        this.speed = BASE_SPEED;
        this.damage = 1;
        this.velocityX = speed / Math.sqrt(2);
        this.velocityY = -speed / Math.sqrt(2);
    }

    /**
     * Creates a ball with the given position, diameter, base speed, damage
     * and initial velocity vector.
     */
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

    /**
     * Detaches the ball from the paddle and gives it an initial upward
     * velocity if it was previously attached.
     */
    public void launch() {
        if (isAttachedToPaddle) {
            isAttachedToPaddle = false;
            double magnitude = BASE_SPEED * speedMultiplier;
            this.velocityY = -Math.abs(magnitude);
            if (this.velocityX == 0) {
                this.velocityX = magnitude / Math.sqrt(2);
            } else {
                updateSpeedVectors();
            }
        }
    }

    /**
     * Sets a speed multiplier used to scale the base speed. The value is
     * clamped between 0.5 and 2.0 and the velocity vector is re-normalised
     * to keep the same direction but adjusted magnitude.
     */
    public void setSpeedMultiplier(double multiplier) {
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

    /**
     * Resets attachment, speed multiplier, fireball state and velocity
     * back to their default values.
     */
    public void reset() {
        this.isAttachedToPaddle = true;
        this.speedMultiplier = 1.0;
        this.isFireball = false;
        this.velocityX = BASE_SPEED / Math.sqrt(2);
        this.velocityY = -BASE_SPEED / Math.sqrt(2);
    }

    public boolean isAttachedToPaddle() {
        return isAttachedToPaddle;
    }

    public void setAttachedToPaddle(boolean attached) {
        this.isAttachedToPaddle = attached;
        if (attached) {
            this.velocityX = 0;
            this.velocityY = 0;
        }
    }

    // Velocity accessors used by GameManager for collision response
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

    /**
     * Simple bounce helper that inverts the vertical component of the
     * velocity when colliding with another {@link GameObject}.
     */
    public void bounceOff(GameObject other) {
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
        if (inverseVx) {
            this.velocityX = -this.velocityX;
        }
        if (inverseVy) {
            this.velocityY = -this.velocityY;
        }
        double mag = Math.hypot(this.velocityX, this.velocityY);
        double desired = BASE_SPEED * speedMultiplier;
        if (mag != 0) {
            double scale = desired / mag;
            this.velocityX *= scale;
            this.velocityY *= scale;
        } else {
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
    public void render() {
        // Rendering is performed by the view layer (GameView).
    }
}
