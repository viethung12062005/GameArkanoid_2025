package com.hung.arkanoid.model.entities.powerup;

/**
 * Factory responsible for instantiating concrete {@link PowerUp}
 * implementations from a {@link PowerUpType} value.
 */
public class PowerUpFactory {

    private PowerUpFactory() {
        // Utility class; no instances.
    }

    /**
     * Creates a new power-up of the given type positioned at (x, y).
     *
     * @param type power-up type to create
     * @param x    left coordinate
     * @param y    top coordinate
     * @return a new power-up instance; never {@code null}
     */
    public static PowerUp createPowerUp(PowerUpType type, double x, double y) {
        return switch (type) {
            case EXPAND -> new ExpandPaddlePowerUp(x, y);
            case SHRINK -> new ShrinkPaddlePowerUp(x, y);
            case LASER -> new LaserPowerUp(x, y);
            case MULTI_BALL -> new MultiBallPowerUp(x, y);
            case CATCH -> new CatchBallPowerUp(x, y);
            case SLOW_BALL -> new SlowBallPowerUp(x, y);
            case FAST_BALL -> new FastBallPowerUp(x, y);
            case FIRE_BALL -> new FireBallPowerUp(x, y);
            case BARRIER -> new BarrierPowerUp(x, y);
            case EXTRA_LIFE -> new ExtraLifePowerUp(x, y);
        };
    }
}
