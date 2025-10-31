package com.hung.arkanoid.model.entities.brick;

import com.hung.arkanoid.game.GameManager;
import com.hung.arkanoid.model.entities.Ball;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

/**
 * Tests the interaction between ExplosiveBrick and GameManager.
 */
class ExplosiveBrickInteractionTest {

    @Test
    void explosiveBrick_onImpactTriggersExplosionWhenAboutToBeDestroyed() {
        GameManager gameManager = mock(GameManager.class);
        Ball ball = mock(Ball.class);

        ExplosiveBrick brick = new ExplosiveBrick(10, 20);
        // hitPoints is 1 by default; onImpact should call explodeBricksAround before takeHit reduces it

        brick.onImpact(gameManager, ball);

        verify(gameManager, times(1)).explodeBricksAround(brick);
        verifyNoMoreInteractions(gameManager);
    }
}

