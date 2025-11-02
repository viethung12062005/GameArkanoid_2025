package com.hung.arkanoid.model.base;

/**
 * Base class for all logical game objects in the Arkanoid model layer.
 * Stores a simple axis-aligned bounding box defined by position {@code (x, y)}
 * and {@code width}/{@code height}. Subclasses add behaviour such as
 * movement, collision responses and rendering.
 */
public abstract class GameObject {
    /** Left X coordinate of the object. */
    protected double x;
    /** Top Y coordinate of the object. */
    protected double y;
    /** Width of the object in world units. */
    protected double width;
    /** Height of the object in world units. */
    protected double height;

    /**
     * Creates an uninitialized game object with zero-sized bounds.
     */
    public GameObject() {
    }

    /**
     * Creates a game object with the given position and size.
     *
     * @param x      left coordinate
     * @param y      top coordinate
     * @param width  object width
     * @param height object height
     */
    public GameObject(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    /** @return left edge (min X) of the object's bounds. */
    public double left() {
        return x;
    }

    /** @return right edge (max X) of the object's bounds. */
    public double right() {
        return x + width;
    }

    /** @return top edge (min Y) of the object's bounds. */
    public double top() {
        return y;
    }

    /** @return bottom edge (max Y) of the object's bounds. */
    public double bottom() {
        return y + height;
    }

    /**
     * Basic axis-aligned bounding-box intersection test with another
     * {@link GameObject}.
     *
     * @param other other game object
     * @return {@code true} if both bounding boxes overlap
     */
    public boolean intersects(GameObject other) {
        return this.right() > other.left() &&
               this.left() < other.right() &&
               this.bottom() > other.top() &&
               this.top() < other.bottom();
    }

    /**
     * Updates the internal state of this game object.
     *
     * @param delta time elapsed since the previous update in seconds
     */
    public abstract void update(double delta);

    /**
     * Renders this game object. Concrete subclasses decide how and where
     * they draw themselves (for example through a view or graphics context).
     */
    public abstract void render();
}