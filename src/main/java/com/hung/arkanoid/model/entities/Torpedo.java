package com.hung.arkanoid.model.entities;

import com.hung.arkanoid.model.base.MovableObject;

/**
 * Simple upward-moving projectile fired by the paddle when lasers are active.
 * The game logic is responsible for removing torpedoes when they leave the
 * playfield or hit a brick.
 */
public class Torpedo extends MovableObject {
    private boolean toBeRemoved = false;

    public static final double WIDTH = 8;
    public static final double HEIGHT = 18;

    /**
     * Creates a torpedo centered on the given X coordinate at the provided
     * Y position, moving vertically with the supplied speed.
     *
     * @param centerX horizontal centre of the projectile
     * @param topY    top Y position
     * @param speedY  vertical speed (typically negative to move upwards)
     */
    public Torpedo(double centerX, double topY, double speedY) {
        super(centerX - WIDTH * 0.5, topY, WIDTH, HEIGHT, 0, speedY);
    }

    public boolean isToBeRemoved() {
        return toBeRemoved;
    }

    public void setToBeRemoved(boolean value) {
        this.toBeRemoved = value;
    }

    @Override
    public void update(double delta) {
        // Move according to velocity; additional removal logic is handled
        // by the game manager when torpedoes leave the visible area.
        move(delta);
    }

    @Override
    public void render() {
        // Rendering is handled by the view layer (GameView).
    }
}
