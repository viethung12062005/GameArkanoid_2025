package com.hung.arkanoid.model.entities.brick;

import com.hung.arkanoid.game.GameManager;
import com.hung.arkanoid.model.base.GameObject;
import com.hung.arkanoid.model.entities.Ball;
import com.hung.arkanoid.model.entities.powerup.PowerUpType;

/**
 * Abstract base class for all brick types in the playfield.
 * A brick is represented by a fixed-size {@link GameObject} with a
 * {@link BrickType}, hit points and destruction state. Subclasses define
 * score value, special impact behaviour and optional power-ups to spawn
 * when destroyed.
 */
public abstract class Brick extends GameObject {
    /** Logical width of a brick in world units. */
    public static final double BRICK_WIDTH = 54;
    /** Logical height of a brick in world units. */
    public static final double BRICK_HEIGHT = 20;

    protected int hitPoints;
    protected BrickType type;
    protected boolean destroyed = false;

    /**
     * Creates a brick at the specified coordinates with the given type
     * and standard brick dimensions.
     *
     * @param x    left coordinate
     * @param y    top coordinate
     * @param type brick type used for logic and rendering
     */
    public Brick(double x, double y, BrickType type) {
        super(x, y, BRICK_WIDTH, BRICK_HEIGHT);
        this.type = type;
    }

    /**
     * Applies damage to this brick from the given ball.
     * <p>
     * Unbreakable bricks simply ignore hits. For the others, the
     * {@code hitPoints} counter is decreased by the ball damage (or 1
     * when the ball reference is {@code null}) and the brick is marked
     * as destroyed when this counter reaches zero.
     */
    public void takeHit(GameManager gameManager, Ball ball) {
        if (isUnbreakable()) {
            return;
        }
        this.hitPoints -= (ball != null ? ball.getDamage() : 1);
        if (this.hitPoints <= 0) {
            this.destroyed = true;
        }
    }

    /**
     * Returns a power-up type to spawn when this brick is destroyed, or
     * {@code null} when no power-up should be created.
     */
    public abstract PowerUpType getPowerUpToSpawn();

    /**
     * Hook called when the brick is hit by a ball. Subclasses can
     * implement special behaviour such as explosions.
     */
    public abstract void onImpact(GameManager gameManager, Ball ball);

    /**
     * Returns the score value awarded for destroying this brick.
     */
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

    /**
     * @return {@code true} when this brick cannot be destroyed by ball
     * hits (used for gold / metal bricks).
     */
    public boolean isUnbreakable() {
        return this.type == BrickType.UNBREAKABLE;
    }

    @Override
    public void update(double delta) {
        // Bricks are static; all behaviour is handled on impact.
    }

    @Override
    public void render() {
        // Rendering is delegated to the view layer.
    }
}