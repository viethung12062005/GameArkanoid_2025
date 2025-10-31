package com.hung.arkanoid.game;

import com.hung.arkanoid.model.entities.Ball;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

class GameManagerTest {

    private SoundManager soundManager;

    @BeforeEach
    void setUp() {
        soundManager = mock(SoundManager.class);
    }

    @Test
    void constructor_initializesScoreFromCurrentScoreParameter() {
        GameManager gm = new GameManager(1, 1234, soundManager);
        assertEquals(1234, gm.getScore());
    }

    @Test
    void ballDrop_decreasesLivesAndResetsWhenStillHasLives() {
        GameManager gm = new GameManager(1, 0, soundManager);
        int initialLives = gm.getLives();

        Ball ball = gm.getBall();
        ball.setAttachedToPaddle(false);
        ball.setY(GameManager.SCREEN_HEIGHT + ball.getHeight());

        gm.update(0.016);

        assertEquals(initialLives - 1, gm.getLives());
        assertNotNull(gm.getBall());
        assertNotEquals(GameManager.GameState.GAME_OVER, gm.getCurrentState());
        // Should not play game over sound yet
        Mockito.verify(soundManager, Mockito.never()).playGameOver();
    }

    @Test
    void livesReachZero_setsGameOverState_andPlaysGameOverSound() {
        GameManager gm = new GameManager(1, 0, soundManager);

        // Force lives down to 1
        while (gm.getLives() > 1) {
            Ball ball = gm.getBall();
            ball.setAttachedToPaddle(false);
            ball.setY(GameManager.SCREEN_HEIGHT + ball.getHeight());
            gm.update(0.016);
        }

        Ball lastBall = gm.getBall();
        lastBall.setAttachedToPaddle(false);
        lastBall.setY(GameManager.SCREEN_HEIGHT + lastBall.getHeight());
        gm.update(0.016);

        assertEquals(GameManager.GameState.GAME_OVER, gm.getCurrentState());
        verify(soundManager).playGameOver();
        verifyNoMoreInteractions(soundManager);
    }

    @Test
    void spawnExtraBalls_increasesNumberOfBalls() {
        GameManager gm = new GameManager(1, 0, soundManager);
        int before = gm.getBalls().size();

        gm.spawnExtraBalls(2);

        int after = gm.getBalls().size();
        assertEquals(before + 2, after);
    }
}
