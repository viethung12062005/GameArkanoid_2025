package com.hung.arkanoid.game;

import com.hung.arkanoid.model.entities.Torpedo;
import com.hung.arkanoid.model.entities.brick.Brick;
import com.hung.arkanoid.model.entities.brick.NormalBrick;
import com.hung.arkanoid.model.entities.brick.UnbreakableBrick;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class TorpedoCollisionTest {

    private SoundManager soundManager;
    private GameManager gm;

    @BeforeEach
    void setUp() {
        soundManager = mock(SoundManager.class);
        gm = new GameManager(1, 0, soundManager);
    }

    @SuppressWarnings("unchecked")
    private List<Torpedo> getTorpedoesInternal() throws Exception {
        Field f = GameManager.class.getDeclaredField("torpedoes");
        f.setAccessible(true);
        return (List<Torpedo>) f.get(gm);
    }

    @SuppressWarnings("unchecked")
    private List<Brick> getBricksInternal() throws Exception {
        Field f = GameManager.class.getDeclaredField("bricks");
        f.setAccessible(true);
        return (List<Brick>) f.get(gm);
    }

    @Test
    void fireTorpedo_addsTorpedoToListWhenLasersActive() throws Exception {
        gm.getPaddle().setLasersActive(true);

        gm.fireTorpedo();

        List<Torpedo> torpedoes = getTorpedoesInternal();
        assertEquals(1, torpedoes.size());
    }

    @Test
    void torpedoHitsNormalBrick_destroysBrickAndIncreasesScoreAndRemovesTorpedo() throws Exception {
        gm.getPaddle().setLasersActive(true);
        gm.fireTorpedo();
        List<Torpedo> torpedoes = getTorpedoesInternal();
        assertFalse(torpedoes.isEmpty());
        Torpedo t = torpedoes.get(0);

        // Position brick directly in front of torpedo path
        List<Brick> bricks = getBricksInternal();
        bricks.clear();
        NormalBrick brick = new NormalBrick(t.getX(), t.getY() - 5, "BLUE");
        bricks.add(brick);

        int initialScore = gm.getScore();

        // Manually invoke collision handler via reflection
        Field f = GameManager.class.getDeclaredField("torpedoes");
        f.setAccessible(true);
        // ensure torpedo still in list
        torpedoes = getTorpedoesInternal();
        assertEquals(1, torpedoes.size());

        // call private handleTorpedoCollisions
        var m = GameManager.class.getDeclaredMethod("handleTorpedoCollisions");
        m.setAccessible(true);
        m.invoke(gm);

        assertTrue(brick.isDestroyed());
        assertEquals(initialScore + brick.getScoreValue(), gm.getScore());
        assertTrue(getTorpedoesInternal().isEmpty(), "Torpedo should be removed after hit");
    }

    @Test
    void torpedoHitsUnbreakableBrick_brickStaysAndTorpedoRemoved() throws Exception {
        gm.getPaddle().setLasersActive(true);
        gm.fireTorpedo();
        List<Torpedo> torpedoes = getTorpedoesInternal();
        assertFalse(torpedoes.isEmpty());
        Torpedo t = torpedoes.get(0);

        // Place Unbreakable brick in path
        List<Brick> bricks = getBricksInternal();
        bricks.clear();
        UnbreakableBrick brick = new UnbreakableBrick(t.getX(), t.getY() - 5);
        bricks.add(brick);

        var m = GameManager.class.getDeclaredMethod("handleTorpedoCollisions");
        m.setAccessible(true);
        m.invoke(gm);

        assertFalse(brick.isDestroyed(), "Unbreakable brick should not be destroyed by torpedo");
        assertTrue(getTorpedoesInternal().isEmpty(), "Torpedo should be removed after collision");
    }
}

