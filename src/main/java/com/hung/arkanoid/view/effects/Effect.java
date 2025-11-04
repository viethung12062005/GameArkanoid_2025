package com.hung.arkanoid.view.effects;

import javafx.scene.canvas.GraphicsContext;

public interface Effect {
    void update();
    void render(GraphicsContext gc);
    boolean isFinished();
}

