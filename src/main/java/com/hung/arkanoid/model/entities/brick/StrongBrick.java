package com.hung.arkanoid.model.entities.brick;

public class StrongBrick extends Brick {
    public StrongBrick() {
        super();
        this.width = 60;
        this.height = 20;
        this.hitPoints = 3;
    }

    public StrongBrick(double x, double y) {
        super(x, y, 60, 20, 3);
    }
}