package com.hung.arkanoid.model.entities.brick;

import com.hung.arkanoid.game.GameManager;
import com.hung.arkanoid.model.entities.Ball;
import com.hung.arkanoid.model.entities.powerup.PowerUpType;

import java.util.Random;

public class NormalBrick extends Brick {
    private static final Random RNG = new Random();

    // Mặc định là BLUE nếu không chỉ định
    private String colorStyle = "BLUE";

    public NormalBrick(double x, double y) {
        this(x, y, "BLUE");
    }

    public NormalBrick(double x, double y, String colorStyle) {
        super(x, y, BrickType.NORMAL);
        this.colorStyle = colorStyle;
        this.hitPoints = 1;
    }

    public String getColorStyle() {
        return colorStyle;
    }

    @Override
    public int getScoreValue() {
        return 10;
    }

    @Override
    public void onImpact(GameManager gameManager, Ball ball) {
        // No special behavior
    }

    @Override
    public PowerUpType getPowerUpToSpawn() {
        if (isDestroyed() && RNG.nextDouble() < 0.2) {
            PowerUpType[] types = PowerUpType.values();
            return types[RNG.nextInt(types.length)];
        }
        return null;
    }
}