package com.hung.arkanoid.model.entities;

import com.hung.arkanoid.model.base.MovableObject;
import com.hung.arkanoid.view.SpriteManager; // Thêm import này

public class Paddle extends MovableObject {
    public static final double BASE_WIDTH = 100.0;
    public static final double BASE_HEIGHT = 20.0;
    public static final double PADDLE_SPEED = 500.0; // Tăng tốc độ một chút cho mượt

    private double speed;
    private boolean lasersActive = false;
    private boolean catchActive = false;

    // Thêm bộ quản lý animation: 1 cột, 10 hàng (dựa trên ảnh resource thường thấy của Arkanoid)
    private final SpriteManager.AnimatedSpriteState animState = new SpriteManager.AnimatedSpriteState(1, 10, 0.1);

    public Paddle() {
        this(350, 550);
    }

    public Paddle(double x, double y) {
        super(x, y, BASE_WIDTH, BASE_HEIGHT, 0, 0);
        this.speed = PADDLE_SPEED;
    }

    public Paddle(double x, double y, double width, double height, double speed) {
        super(x, y, width, height, 0, 0);
        this.speed = speed;
    }

    // ... (Giữ nguyên các getter/setter cũ: getSpeed, setSpeed, moveLeft, moveRight, stopMoving) ...
    public double getSpeed() { return speed; }
    public void setSpeed(double speed) { this.speed = speed; }

    public void moveLeft() { this.velocityX = -Math.abs(speed); }
    public void moveRight() { this.velocityX = Math.abs(speed); }
    public void stopMoving() { this.velocityX = 0; }

    public void expand() {
        double centerX = this.x + this.width / 2.0;
        this.width = BASE_WIDTH * 1.5;
        this.x = centerX - this.width / 2.0;
    }

    public void shrink() {
        double centerX = this.x + this.width / 2.0;
        this.width = BASE_WIDTH * 0.75;
        this.x = centerX - this.width / 2.0;
    }

    public void reset() {
        double centerX = this.x + this.width / 2.0;
        this.width = BASE_WIDTH;
        this.x = centerX - this.width / 2.0;
        this.lasersActive = false;
        this.catchActive = false;
    }

    public void resetSize() {
        double centerX = this.x + this.width / 2.0;
        this.width = BASE_WIDTH;
        this.x = centerX - this.width / 2.0;
    }

    public boolean areLasersActive() { return lasersActive; }
    public void setLasersActive(boolean lasersActive) { this.lasersActive = lasersActive; }

    public boolean isCatchActive() { return catchActive; }
    public void setCatchActive(boolean catchActive) { this.catchActive = catchActive; }

    // --- PHẦN QUAN TRỌNG MỚI THÊM ---

    public int getAnimationIndex() {
        return animState.getFrameIndex();
    }

    @Override
    public void update(double delta) {
        super.update(delta);
        // Cập nhật animation mỗi frame
        animState.update(delta);
    }

    @Override
    public void render() {}
}