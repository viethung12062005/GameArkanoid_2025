package com.hung.arkanoid.view.effects;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class ExplosionEffect implements Effect {
    private double x;
    private double y;
    private double duration = 0.5; // seconds
    private double lifeTimer = 0.0;
    private double maxRadius = 100.0;

    public ExplosionEffect(double x, double y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public void update() {
        // assume 60 FPS for timer step; GameManager.update() will call this every frame
        lifeTimer += 1.0 / 60.0;
    }

    @Override
    public boolean isFinished() {
        return lifeTimer >= duration;
    }

    @Override
    public void render(GraphicsContext gc) {
        double progress = Math.min(1.0, lifeTimer / duration);
        double currentRadius = progress * maxRadius;
        double opacity = Math.max(0.0, 1.0 - progress);
        gc.setFill(new Color(1.0, 0.8, 0.0, opacity));
        gc.fillOval(x - currentRadius, y - currentRadius, currentRadius * 2, currentRadius * 2);
        // inner bright core
        gc.setFill(new Color(1.0, 1.0, 0.6, opacity * 0.6));
        gc.fillOval(x - currentRadius * 0.4, y - currentRadius * 0.4, currentRadius * 0.8, currentRadius * 0.8);
    }
}

