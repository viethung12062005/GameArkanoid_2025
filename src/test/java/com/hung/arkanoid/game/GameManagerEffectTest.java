package com.hung.arkanoid.game;

import com.hung.arkanoid.model.entities.Paddle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GameManagerEffectTest {

    private SoundManager soundManager;
    private GameManager gm;

    @BeforeEach
    void setUp() {
        soundManager = mock(SoundManager.class);
        gm = new GameManager(1, 0, soundManager);
    }

    @SuppressWarnings("unchecked")
    private List<?> getActiveEffects() throws Exception {
        Field f = GameManager.class.getDeclaredField("activeEffects");
        f.setAccessible(true);
        return (List<?>) f.get(gm);
    }

    private Object getFirstActiveEffect() throws Exception {
        List<?> list = getActiveEffects();
        assertFalse(list.isEmpty(), "Expected at least one active effect");
        return list.get(0);
    }

    @Test
    void applyExpandPaddle_registersEffectWithDurationAndResetSizeRunnable() throws Exception {
        Paddle paddle = gm.getPaddle();
        double originalWidth = paddle.getWidth();

        gm.applyExpandPaddle(10.0);

        Object effect = getFirstActiveEffect();
        Class<?> cls = effect.getClass();

        Field idField = cls.getDeclaredField("id");
        idField.setAccessible(true);
        String id = (String) idField.get(effect);

        Field remainingField = cls.getDeclaredField("remaining");
        remainingField.setAccessible(true);
        double remaining = (double) remainingField.get(effect);

        Field onExpireField = cls.getDeclaredField("onExpire");
        onExpireField.setAccessible(true);
        Runnable onExpire = (Runnable) onExpireField.get(effect);

        assertEquals("EXPAND_PADDLE", id);
        assertEquals(10.0, remaining, 1e-6);

        // verify onExpire resets paddle size
        paddle.expand();
        assertTrue(paddle.getWidth() > originalWidth);

        onExpire.run();
        assertEquals(originalWidth, paddle.getWidth(), 1e-6);
    }

    @Test
    void effectExpiresAfterTime_andOnExpireIsExecuted() throws Exception {
        Paddle spyPaddle = spy(gm.getPaddle());
        // swap the internal paddle reference with a spy to verify resetSize is invoked
        Field paddleField = GameManager.class.getDeclaredField("paddle");
        paddleField.setAccessible(true);
        paddleField.set(gm, spyPaddle);

        gm.applyShrinkPaddle(1.0);
        assertFalse(getActiveEffects().isEmpty());

        // Simulate delta time > duration
        gm.update(0.5);
        gm.update(0.6);

        assertTrue(getActiveEffects().isEmpty(), "Effect list should be empty after expiration");
        verify(spyPaddle, atLeastOnce()).resetSize();
    }

    @Test
    void applyingNewEffectWithSameId_replacesPreviousEffect() throws Exception {
        gm.applyShrinkPaddle(5.0);  // registers id "SHRINK_PADDLE"
        List<?> afterShrink = getActiveEffects();
        assertEquals(1, afterShrink.size());

        Object shrinkEffect = afterShrink.get(0);

        gm.applyShrinkPaddle(2.0);  // re-register with same id, should replace previous
        List<?> afterSecondShrink = getActiveEffects();
        assertEquals(1, afterSecondShrink.size());

        Object shrinkEffect2 = afterSecondShrink.get(0);
        assertNotSame(shrinkEffect, shrinkEffect2, "Second shrink effect should replace the first");
    }
}
