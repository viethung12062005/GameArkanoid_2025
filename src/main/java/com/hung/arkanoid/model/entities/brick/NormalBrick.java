package com.hung.arkanoid.model.entities.brick;

public class NormalBrick extends Brick {
    public NormalBrick() {
        super();
        this.width = 60;
        this.height = 20;
        this.hitPoints = 1;
    }

    public NormalBrick(double x, double y) {
        super(x, y, 60, 20, 1);
    }
}