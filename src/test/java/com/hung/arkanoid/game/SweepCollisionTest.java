package com.hung.arkanoid.game;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the private computeSweepAgainstRect method in GameManager
 * using Java reflection.
 */
class SweepCollisionTest {

    private static Method computeSweepMethod;

    @BeforeAll
    static void setupReflection() throws Exception {
        computeSweepMethod = GameManager.class.getDeclaredMethod(
                "computeSweepAgainstRect",
                double.class, double.class, double.class, double.class,
                double.class, double.class, double.class, double.class, double.class);
        computeSweepMethod.setAccessible(true);
    }

    private Object invokeSweep(double x0, double y0, double x1, double y1,
                               double r, double rx, double ry, double rw, double rh) throws InvocationTargetException, IllegalAccessException {
        GameManager gm = new GameManager(1, 0, new SoundManager());
        return computeSweepMethod.invoke(gm, x0, y0, x1, y1, r, rx, ry, rw, rh);
    }

    private double getFieldD(Object obj, String name) throws Exception {
        var f = obj.getClass().getDeclaredField(name);
        f.setAccessible(true);
        return f.getDouble(obj);
    }

    private boolean getFieldB(Object obj, String name) throws Exception {
        var f = obj.getClass().getDeclaredField(name);
        f.setAccessible(true);
        return f.getBoolean(obj);
    }

    @Test
    void horizontalImpact_hitsTopEdge_invertsVy() throws Exception {
        // Ball center moves vertically down toward top edge of brick
        double r = 5;
        double rx = 100, ry = 100, rw = 54, rh = 20; // brick
        double x0 = 120, y0 = 80;   // start above brick
        double x1 = 120, y1 = 120;  // end below brick (moving down)

        Object res = invokeSweep(x0, y0, x1, y1, r, rx, ry, rw, rh);
        assertNotNull(res, "Expected a collision with top edge");

        double t = getFieldD(res, "t");
        boolean inverseVx = getFieldB(res, "inverseVx");
        boolean inverseVy = getFieldB(res, "inverseVy");

        assertTrue(t > 0 && t <= 1.0);
        assertTrue(inverseVy, "Vertical component should be inverted");
        assertFalse(inverseVx, "Horizontal component should not be inverted");
    }

    @Test
    void verticalImpact_hitsLeftEdge_invertsVx() throws Exception {
        double r = 5;
        double rx = 100, ry = 100, rw = 54, rh = 20; // brick
        double x0 = 80, y0 = 110;   // left of brick
        double x1 = 130, y1 = 110;  // crossing brick horizontally

        Object res = invokeSweep(x0, y0, x1, y1, r, rx, ry, rw, rh);
        assertNotNull(res, "Expected a collision with left edge");

        double t = getFieldD(res, "t");
        boolean inverseVx = getFieldB(res, "inverseVx");
        boolean inverseVy = getFieldB(res, "inverseVy");

        assertTrue(t > 0 && t <= 1.0);
        assertTrue(inverseVx, "Horizontal component should be inverted");
        // vertical may remain unchanged; do not assert true
    }

    @Test
    void noCollision_returnsNull() throws Exception {
        double r = 5;
        double rx = 100, ry = 100, rw = 54, rh = 20; // brick
        double x0 = 10, y0 = 10;   // far away
        double x1 = 20, y1 = 20;   // still far away and moving away

        Object res = invokeSweep(x0, y0, x1, y1, r, rx, ry, rw, rh);
        assertNull(res, "No collision expected when moving away");
    }

    @Test
    void overlappingState_insideBrick_tEqualsOne() throws Exception {
        double r = 0; // treat x,y as center inside rect
        double rx = 100, ry = 100, rw = 54, rh = 20; // brick
        double x0 = 120, y0 = 110; // inside rect
        double x1 = 140, y1 = 130; // moving somewhere

        Object res = invokeSweep(x0, y0, x1, y1, r, rx, ry, rw, rh);
        assertNotNull(res, "Overlapping state should yield a collision result");

        double t = getFieldD(res, "t");
        assertEquals(1.0, t, 1e-6, "Overlapping resolution should use t=1.0");
    }
}

