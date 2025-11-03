package com.hung.arkanoid.model.entities.powerup;

import com.hung.arkanoid.game.GameManager;
import com.hung.arkanoid.model.base.MovableObject;
import com.hung.arkanoid.view.SpriteManager;

/**
 * Base class for all falling power-up entities.
 * Power-ups descend vertically from the point where a brick was destroyed
 * and, once collected by the paddle, apply an effect via
 * {@link #applyEffect(GameManager)}. Animation frames are provided by
 * {@link SpriteManager.AnimatedSpriteState}.
 */
public abstract class PowerUp extends MovableObject {
    /** Logical width of a power-up sprite. */
    public static final double POWERUP_WIDTH = 50;
    /** Logical height of a power-up sprite. */
    public static final double POWERUP_HEIGHT = 25;
    /** Downward movement speed in world units per second. */
    public static final double FALL_SPEED = 250;

    protected PowerUpType type;
    private boolean consumed = false;

    // Animation state (5 columns x 4 rows = 20 frames in the bonus sprite sheet).
    private final SpriteManager.AnimatedSpriteState animState = new SpriteManager.AnimatedSpriteState(5, 4);

    /**
     * Creates a new power-up instance at the given coordinates.
     *
     * @param x    left coordinate
     * @param y    top coordinate
     * @param type logical power-up type
     */
    public PowerUp(double x, double y, PowerUpType type) {
        // initialize with vertical fall speed
        super(x, y, POWERUP_WIDTH, POWERUP_HEIGHT, 0, FALL_SPEED);
        this.type = type;
    }

    /**
     * Applies the concrete power-up effect to the given game manager.
     * Implementations typically register a timed effect or immediately
     * change some aspect of the game state.
     */
    public abstract void applyEffect(GameManager gameManager);

    @Override
    public void update(double delta) {
        // Move according to velocity and advance sprite animation.
        super.update(delta);
        animState.update(delta);
    }

    @Override
    public void render() {
        // Rendering is handled by the view layer (GameView).
    }

    public boolean isConsumed() {
        return consumed;
    }

    public void setConsumed(boolean consumed) {
        this.consumed = consumed;
    }

    /** Marks this power-up as consumed by the paddle. */
    public void consume() { setConsumed(true); }

    public PowerUpType getType() {
        return type;
    }

    /**
     * Returns the current animation frame index used by the view to
     * slice the appropriate sub-image from the sprite sheet.
     */
    public int getAnimationIndex() {
        return animState.getFrameIndex();
    }
}
