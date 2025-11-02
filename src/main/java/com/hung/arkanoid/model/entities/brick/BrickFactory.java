package com.hung.arkanoid.model.entities.brick;

/**
 * Factory for constructing concrete {@link Brick} instances from
 * string-based keys.
 * Keys are case-insensitive and support both named types such as
 * {@code "STRONG"} and colour-specific normal bricks such as
 * {@code "NORMAL_BLUE"}.
 */
public class BrickFactory {

    private BrickFactory() {
        // Utility class; no instances.
    }

    /**
     * Creates a brick at the given coordinates based on the supplied key.
     *
     * @param key textual identifier for the brick type (for example
     *            {@code "NORMAL_BLUE"} or {@code "EXPLOSIVE"})
     * @param x   left coordinate
     * @param y   top coordinate
     * @return a new {@link Brick} instance, or {@code null} when the key
     * is {@code null}
     */
    public static Brick create(String key, double x, double y) {
        if (key == null) {
            return null;
        }

        String upperKey = key.toUpperCase();

        // Handle coloured normal bricks (format: "NORMAL_COLOR").
        if (upperKey.startsWith("NORMAL")) {
            String color = "BLUE"; // default colour
            if (upperKey.contains("_")) {
                color = upperKey.substring(upperKey.indexOf("_") + 1);
            }
            return new NormalBrick(x, y, color);
        }

        // Handle special brick types.
        return switch (upperKey) {
            case "STRONG" -> new StrongBrick(x, y);
            case "UNBREAKABLE" -> new UnbreakableBrick(x, y);
            case "EXPLOSIVE" -> new ExplosiveBrick(x, y);
            default -> new NormalBrick(x, y, "BLUE");
        };
    }
}