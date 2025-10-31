package com.hung.arkanoid.model.entities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PaddleTest {

    @Test
    void moveLeft_and_moveRight_setVelocityXToPaddleSpeedWithCorrectSign() {
        Paddle paddle = new Paddle(100, 100);

        paddle.moveLeft();
        assertEquals(-Paddle.PADDLE_SPEED, paddle.getVelocityX(), 1e-6);

        paddle.moveRight();
        assertEquals(Paddle.PADDLE_SPEED, paddle.getVelocityX(), 1e-6);

        paddle.stopMoving();
        assertEquals(0.0, paddle.getVelocityX(), 1e-6);
    }

    @Test
    void expand_increasesWidthAndKeepsCenterX() {
        Paddle paddle = new Paddle(100, 100);
        double centerX = paddle.getX() + paddle.getWidth() / 2.0;

        paddle.expand();

        assertEquals(Paddle.BASE_WIDTH * 1.5, paddle.getWidth(), 1e-6);
        double newCenterX = paddle.getX() + paddle.getWidth() / 2.0;
        assertEquals(centerX, newCenterX, 1e-6, "Center X should be preserved when expanding");
    }

    @Test
    void shrink_decreasesWidthAndKeepsCenterX() {
        Paddle paddle = new Paddle(100, 100);
        double centerX = paddle.getX() + paddle.getWidth() / 2.0;

        paddle.shrink();

        assertEquals(Paddle.BASE_WIDTH * 0.75, paddle.getWidth(), 1e-6);
        double newCenterX = paddle.getX() + paddle.getWidth() / 2.0;
        assertEquals(centerX, newCenterX, 1e-6, "Center X should be preserved when shrinking");
    }

    @Test
    void reset_restoresBaseWidthAndDisablesEffects() {
        Paddle paddle = new Paddle(100, 100);
        paddle.expand();
        paddle.setLasersActive(true);
        paddle.setCatchActive(true);

        paddle.reset();

        assertEquals(Paddle.BASE_WIDTH, paddle.getWidth(), 1e-6);
        assertFalse(paddle.areLasersActive());
        assertFalse(paddle.isCatchActive());
    }
}

