package com.hung.arkanoid.model.base;

/**
 * Abstract base class for all game objects in the Arkanoid game.
 * Encapsulates position, size, and basic collision detection.
 */
public abstract class GameObject {
    protected double x;
    protected double y;
    protected double width;
    protected double height;

    public GameObject() {
    }

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

    public double left() {
        return x;
    }

    public double right() {
        return x + width;
    }

    public double top() {
        return y;
    }

    public double bottom() {
        return y + height;
    }

    public boolean intersects(GameObject other) {
        return this.right() > other.left() &&
               this.left() < other.right() &&
               this.bottom() > other.top() &&
               this.top() < other.bottom();
    }

    /**
     * Update the game object state.
     * @param delta delta time in seconds (or arbitrary time unit) since last update
     */
    public abstract void update(double delta);

    /**
     * Render the game object.
     * Concrete rendering logic will be implemented in subclasses.
     */
    public abstract void render();
}