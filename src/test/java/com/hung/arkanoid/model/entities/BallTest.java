package com.hung.arkanoid.model.entities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BallTest {

    @Test
    void defaultConstructor_initializesPositionVelocityAndFlags() {
        Ball ball = new Ball();

        assertEquals(Ball.BALL_RADIUS * 2, ball.getWidth(), 1e-6);
        assertEquals(Ball.BALL_RADIUS * 2, ball.getHeight(), 1e-6);
        assertTrue(ball.isAttachedToPaddle(), "Ball should start attached to paddle");

        double expectedMag = Ball.BASE_SPEED;
        double vx = ball.getVelocityX();
        double vy = ball.getVelocityY();
        double mag = Math.hypot(vx, vy);
        assertEquals(expectedMag, mag, 1e-6);
        assertTrue(vx > 0);
        assertTrue(vy < 0);
    }

    @Test
    void update_movesByVelocityTimesDeltaWhenNotAttached() {
        Ball ball = new Ball();
        ball.setAttachedToPaddle(false);
        ball.setVelocityX(100);
        ball.setVelocityY(50);

        double x0 = ball.getX();
        double y0 = ball.getY();

        ball.update(0.5); // half a second

        assertEquals(x0 + 100 * 0.5, ball.getX(), 1e-6);
        assertEquals(y0 + 50 * 0.5, ball.getY(), 1e-6);
    }

    @Test
    void reverseDx_and_reverseDy_invertVelocityComponents() {
        Ball ball = new Ball();
        ball.setAttachedToPaddle(false);
        ball.setVelocityX(30);
        ball.setVelocityY(-40);

        ball.reverseDx();
        assertEquals(-30, ball.getVelocityX(), 1e-6);
        assertEquals(-40, ball.getVelocityY(), 1e-6);

        ball.reverseDy();
        assertEquals(-30, ball.getVelocityX(), 1e-6);
        assertEquals(40, ball.getVelocityY(), 1e-6);
    }

    @Test
    void setSpeedMultiplier_clampsAndRescalesVelocity() {
        Ball ball = new Ball();
        ball.setAttachedToPaddle(false);
        ball.setVelocityX(Ball.BASE_SPEED);
        ball.setVelocityY(0);

        ball.setSpeedMultiplier(0.1); // below minimum -> clamp to 0.5
        assertEquals(0.5 * Ball.BASE_SPEED, Math.hypot(ball.getVelocityX(), ball.getVelocityY()), 1e-6);

        ball.setSpeedMultiplier(5.0); // above maximum -> clamp to 2.0
        assertEquals(2.0 * Ball.BASE_SPEED, Math.hypot(ball.getVelocityX(), ball.getVelocityY()), 1e-6);

        ball.setSpeedMultiplier(1.5); // within range
        assertEquals(1.5 * Ball.BASE_SPEED, Math.hypot(ball.getVelocityX(), ball.getVelocityY()), 1e-6);
    }

    @Test
    void launch_detachesFromPaddle_andGivesUpwardVelocity() {
        Ball ball = new Ball();
        assertTrue(ball.isAttachedToPaddle());

        ball.launch();

        assertFalse(ball.isAttachedToPaddle());
        assertTrue(ball.getVelocityY() <= 0, "Ball should be moving upward after launch");
        double expectedMag = Ball.BASE_SPEED; // multiplier default is 1.0
        double mag = Math.hypot(ball.getVelocityX(), ball.getVelocityY());
        assertEquals(expectedMag, mag, 1e-6);
    }
}
