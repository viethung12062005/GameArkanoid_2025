package com.hung.arkanoid.model.entities.brick;

import com.hung.arkanoid.game.GameManager;
import com.hung.arkanoid.model.entities.Ball;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BrickTest {

    @Test
    void normalBrick_losesHitPointsAndGetsDestroyed() {
        NormalBrick brick = new NormalBrick(10, 20);
        GameManager gm = new GameManager(1);
        Ball ball = new Ball();

        assertEquals(1, brick.getHitPoints());
        assertFalse(brick.isDestroyed());

        brick.takeHit(gm, ball);

        assertTrue(brick.isDestroyed());
        assertEquals(0, brick.getHitPoints());
    }

    @Test
    void strongBrick_requiresMultipleHitsBeforeDestroyed() {
        StrongBrick brick = new StrongBrick(10, 20);
        GameManager gm = new GameManager(1);
        Ball ball = new Ball();

        assertEquals(3, brick.getHitPoints());
        assertFalse(brick.isDestroyed());

        brick.takeHit(gm, ball);
        assertEquals(2, brick.getHitPoints());
        assertFalse(brick.isDestroyed());

        brick.takeHit(gm, ball);
        assertEquals(1, brick.getHitPoints());
        assertFalse(brick.isDestroyed());

        brick.takeHit(gm, ball);
        assertEquals(0, brick.getHitPoints());
        assertTrue(brick.isDestroyed());
    }

    @Test
    void unbreakableBrick_doesNotLoseHitPointsOrGetDestroyed() {
        UnbreakableBrick brick = new UnbreakableBrick(10, 20);
        GameManager gm = new GameManager(1);
        Ball ball = new Ball();

        int initialHp = brick.getHitPoints();
        assertFalse(brick.isDestroyed());

        brick.takeHit(gm, ball);
        brick.takeHit(gm, ball);

        assertEquals(initialHp, brick.getHitPoints());
        assertFalse(brick.isDestroyed());
    }
}

