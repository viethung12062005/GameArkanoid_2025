package com.hung.arkanoid.game;

import com.hung.arkanoid.model.entities.Ball;
import com.hung.arkanoid.model.entities.Paddle;
import com.hung.arkanoid.model.entities.brick.Brick;
import com.hung.arkanoid.model.entities.brick.BrickType;
import com.hung.arkanoid.model.entities.powerup.PowerUp;
import com.hung.arkanoid.model.entities.powerup.PowerUpFactory;
import com.hung.arkanoid.model.entities.powerup.PowerUpType;
import com.hung.arkanoid.view.effects.Effect;
import com.hung.arkanoid.view.effects.ExplosionEffect;
import javafx.geometry.Rectangle2D;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

/**
 * Core game-logic coordinator for a single Arkanoid level.
 * <p>
 * Manages the {@link Paddle}, one or more {@link Ball} instances, bricks,
 * power-ups, torpedoes, timed effects and score/lives state. The view layer
 * drives this class by repeatedly calling {@link #update(double)}.
 */
public class GameManager {

    /** Overall high-level state of the current level. */
    public enum GameState { PLAYING, PAUSED, LEVEL_CLEARED, GAME_OVER }

    public static final double SCREEN_WIDTH = 800.0;
    public static final double SCREEN_HEIGHT = 600.0;

    private GameState currentState = GameState.PLAYING;
    private final int currentLevel;

    private final Paddle paddle;
    private final List<Ball> balls = new ArrayList<>();

    private double paddleTargetX;
    private boolean isMouseControlled = false;
    private boolean paddleMovingLeft = false;
    private boolean paddleMovingRight = false;

    private final List<Brick> bricks;
    private int lives = 3;
    private int score = 0;

    private final List<PowerUp> powerUps = new ArrayList<>();
    private boolean barrierActive = false;
    private final List<com.hung.arkanoid.model.entities.Torpedo> torpedoes = new ArrayList<>();

    /**
     * Small internal record used to track a time-limited gameplay effect
     * such as expanded paddle, lasers or ball speed multipliers.
     */
    private static class ActiveEffect {
        String id;
        double remaining;
        Runnable onExpire;

        ActiveEffect(String id, double remaining, Runnable onExpire) {
            this.id = id;
            this.remaining = remaining;
            this.onExpire = onExpire;
        }
    }

    private final List<ActiveEffect> activeEffects = new ArrayList<>();
    private final List<Effect> effects = new ArrayList<>();
    private final SoundManager soundManager;

    /**
     * Creates a new game manager for level 1 with score 0 and a fresh sound manager.
     */
    public GameManager() {
        this(1, 0, new SoundManager());
    }

    /**
     * Creates a new game manager for the given level with score 0 and a fresh sound manager.
     *
     * @param levelNumber level index starting at 1
     */
    public GameManager(int levelNumber) {
        this(levelNumber, 0, new SoundManager());
    }

    /**
     * Creates a new game manager for the given level and starting score using
     * a fresh sound manager instance.
     *
     * @param levelNumber  level index starting at 1
     * @param currentScore current score carried into this level
     */
    public GameManager(int levelNumber, int currentScore) {
        this(levelNumber, currentScore, new SoundManager());
    }

    /**
     * Primary constructor used by tests and callers that want to inject a
     * custom {@link SoundManager} implementation.
     *
     * @param levelNumber  level index starting at 1
     * @param currentScore current score carried into this level
     * @param soundManager sound manager instance used to play SFX
     */
    public GameManager(int levelNumber, int currentScore, SoundManager soundManager) {
        this.currentLevel = levelNumber;
        this.score = currentScore;
        this.bricks = LevelLoader.loadLevel(levelNumber);
        this.paddle = new Paddle(350, 550);
        this.soundManager = soundManager != null ? soundManager : new SoundManager();
        resetBall();
    }

    /**
     * Resets the ball list to a single ball attached above the paddle.
     */
    private void resetBall() {
        balls.clear();
        Ball initialBall = new Ball(380, 530, Ball.BALL_RADIUS * 2, (int) Ball.BASE_SPEED,
                                    1, 0, -Ball.BASE_SPEED / Math.sqrt(2));
        balls.add(initialBall);
    }

    public void setMouseControlled(boolean isMouseControlled) {
        this.isMouseControlled = isMouseControlled;
    }

    /**
     * Sets the horizontal target for mouse-controlled paddle movement.
     *
     * @param x new mouse x-coordinate
     */
    public void setPaddleTargetX(double x) {
        this.isMouseControlled = true;
        this.paddleTargetX = x;
    }

    public GameState getCurrentState() {
        return currentState;
    }

    public int getCurrentLevel() {
        return currentLevel;
    }

    public int getNextLevel() {
        return currentLevel + 1;
    }

    public void setState(GameState s) {
        this.currentState = s;
    }

    /**
     * Toggles between {@link GameState#PLAYING} and {@link GameState#PAUSED}.
     */
    public void togglePause() {
        if (currentState == GameState.PLAYING) {
            currentState = GameState.PAUSED;
        } else if (currentState == GameState.PAUSED) {
            currentState = GameState.PLAYING;
        }
    }

    /**
     * Enables or disables continuous left movement when using keyboard control.
     */
    public void setPaddleMovingLeft(boolean value) {
        this.isMouseControlled = false;
        this.paddleMovingLeft = value;
        if (value) {
            paddle.moveLeft();
        } else if (!paddleMovingRight) {
            paddle.stopMoving();
        }
    }

    /**
     * Enables or disables continuous right movement when using keyboard control.
     */
    public void setPaddleMovingRight(boolean value) {
        this.isMouseControlled = false;
        this.paddleMovingRight = value;
        if (value) {
            paddle.moveRight();
        } else if (!paddleMovingLeft) {
            paddle.stopMoving();
        }
    }

    /**
     * Launches all balls that are currently attached to the paddle.
     */
    public void launchBall() {
        for (Ball b : balls) {
            b.launch();
        }
    }

    public List<PowerUp> getPowerUps() {
        return powerUps;
    }

    public Paddle getPaddle() {
        return paddle;
    }

    /**
     * Returns the list of active balls. Modifications to the returned
     * list affect the internal state.
     */
    public List<Ball> getBalls() {
        return balls;
    }

    /**
     * Convenience accessor returning the first ball in the list or {@code null}
     * if there are currently no balls in play.
     */
    public Ball getBall() {
        return balls.isEmpty() ? null : balls.get(0);
    }

    public int getLives() {
        return lives;
    }

    public int getScore() {
        return score;
    }

    /**
     * Returns a defensive copy of the bricks list so callers cannot mutate
     * internal state directly.
     */
    public List<Brick> getBricksSafe() {
        return new ArrayList<>(bricks);
    }

    public List<Effect> getEffects() {
        return effects;
    }

    /**
     * Returns a defensive copy of the torpedo list.
     */
    public List<com.hung.arkanoid.model.entities.Torpedo> getTorpedoes() {
        return new ArrayList<>(torpedoes);
    }

    /**
     * Legacy alias kept for compatibility; delegates to
     * {@link #setPaddleMovingLeft(boolean)}.
     */
    public void setMovingLeft(boolean value) {
        setPaddleMovingLeft(value);
    }

    /**
     * Legacy alias kept for compatibility; delegates to
     * {@link #setPaddleMovingRight(boolean)}.
     */
    public void setMovingRight(boolean value) {
        setPaddleMovingRight(value);
    }

    /**
     * Places the paddle horizontally so that its center is at the given x-coordinate
     * and then clamps it inside the screen bounds.
     */
    public void setPaddleX(double centerX) {
        paddle.setX(centerX - paddle.getWidth() / 2.0);
        checkPaddleBounds();
    }

    /**
     * Updates all active timed effects and runs expiration callbacks when their
     * remaining duration reaches zero.
     */
    private void updateActiveEffects(double deltaSeconds) {
        if (deltaSeconds <= 0) {
            return;
        }
        Iterator<ActiveEffect> it = activeEffects.iterator();
        while (it.hasNext()) {
            ActiveEffect effect = it.next();
            effect.remaining -= deltaSeconds;
            if (effect.remaining <= 0) {
                try {
                    effect.onExpire.run();
                } catch (Exception ex) {
                    System.err.println("Error executing onExpire: " + ex.getMessage());
                }
                it.remove();
            }
        }
    }

    /**
     * Registers a new time-limited effect or replaces any existing
     * effect with the same identifier.
     *
     * @param id        stable identifier for this effect type
     * @param duration  lifetime in seconds
     * @param onExpire  callback invoked exactly once when the effect expires
     */
    private void registerTimedEffect(String id, double duration, Runnable onExpire) {
        activeEffects.removeIf(e -> e.id.equals(id));
        activeEffects.add(new ActiveEffect(id, duration, onExpire));
    }

    public void applyExpandPaddle(double duration) {
        paddle.expand();
        registerTimedEffect("EXPAND_PADDLE", duration, paddle::resetSize);
    }

    public void applyShrinkPaddle(double duration) {
        paddle.shrink();
        registerTimedEffect("SHRINK_PADDLE", duration, paddle::resetSize);
    }

    public void applyActivateLasers(double duration) {
        paddle.setLasersActive(true);
        registerTimedEffect("LASERS", duration, () -> paddle.setLasersActive(false));
    }

    public void applyActivateCatch(double duration) {
        paddle.setCatchActive(true);
        registerTimedEffect("CATCH", duration, () -> paddle.setCatchActive(false));
    }

    /**
     * Temporarily multiplies the speed of all balls and automatically
     * restores the original speed when the effect expires.
     */
    public void applySetBallSpeedMultiplier(double multiplier, double duration) {
        for (Ball b : balls) {
            b.setSpeedMultiplier(multiplier);
        }
        registerTimedEffect("SPEED_MULTIPLIER", duration,
                () -> {
                    for (Ball b : balls) {
                        b.setSpeedMultiplier(1.0);
                    }
                });
    }

    /**
     * Enables fireball mode on all balls for the given duration.
     * Fireballs drill through bricks without bouncing.
     */
    public void applyFireball(double duration) {
        for (Ball b : balls) {
            b.activateFireball(true);
        }
        registerTimedEffect("FIREBALL", duration,
                () -> {
                    for (Ball b : balls) {
                        b.activateFireball(false);
                    }
                });
    }

    public void applyBarrier(double duration) {
        setBarrierActive(true);
        registerTimedEffect("BARRIER", duration, () -> setBarrierActive(false));
    }

    /**
     * Fires a single torpedo from the center of the paddle if lasers are active
     * and there is currently no other torpedo in flight.
     */
    public void fireTorpedo() {
        if (!paddle.areLasersActive()) {
            return;
        }
        if (!torpedoes.isEmpty()) {
            return;
        }
        double centerX = paddle.getX() + paddle.getWidth() / 2.0;
        double topY = paddle.getY() - 1.0;
        com.hung.arkanoid.model.entities.Torpedo torpedo =
                new com.hung.arkanoid.model.entities.Torpedo(centerX, topY, -400.0);
        torpedoes.add(torpedo);
        try {
            soundManager.playLaser();
        } catch (Exception ignored) {
        }
    }

    public void addLife() {
        lives++;
    }

    public void activateBarrier(boolean active) {
        setBarrierActive(active);
    }

    /**
     * Spawns the given number of additional balls based on the first ball's
     * position and velocity. Extra balls are given angled directions so they
     * spread out instead of following an identical trajectory.
     */
    public void spawnExtraBalls(int count) {
        if (balls.isEmpty()) {
            return;
        }
        Ball reference = balls.get(0);
        for (int i = 0; i < count; i++) {
            Ball ball = new Ball(reference.getX(), reference.getY(), reference.getWidth(),
                                  reference.getSpeed(), reference.getDamage(),
                                  reference.getVelocityX(), reference.getVelocityY());
            ball.setAttachedToPaddle(false);
            if (reference.isFireball()) {
                ball.activateFireball(true);
            }

            // Rotate the reference velocity vector by +/- 30 degrees to fan out balls.
            double theta = Math.toRadians(30 * (i % 2 == 0 ? -1 : 1));
            double vx = reference.getVelocityX();
            double vy = reference.getVelocityY();

            if (vx == 0 && vy == 0) {
                // If the reference ball is still attached, give new balls an upward speed.
                vx = Ball.BASE_SPEED * 0.5 * (i % 2 == 0 ? -1 : 1);
                vy = -Ball.BASE_SPEED * 0.866;
            } else {
                double newVx = vx * Math.cos(theta) - vy * Math.sin(theta);
                double newVy = vx * Math.sin(theta) + vy * Math.cos(theta);
                vx = newVx;
                vy = newVy;
            }
            ball.setVelocityX(vx);
            ball.setVelocityY(vy);
            balls.add(ball);
        }
    }

    public void addBrick(Brick brick) {
        if (brick != null) {
            bricks.add(brick);
        }
    }

    public void setBarrierActive(boolean active) {
        this.barrierActive = active;
    }

    public boolean isBarrierActive() {
        return this.barrierActive;
    }

    /**
     * Main per-frame update method. Advances paddle, balls, bricks, power-ups,
     * torpedoes, visual effects and timed effects, and transitions to
     * {@link GameState#LEVEL_CLEARED} or {@link GameState#GAME_OVER} when needed.
     *
     * @param deltaSeconds time elapsed since last update in seconds
     */
    public void update(double deltaSeconds) {
        if (currentState != GameState.PLAYING) {
            return;
        }
        if (deltaSeconds <= 0) {
            deltaSeconds = 1.0 / 60.0;
        }
        if (deltaSeconds > 0.5) {
            deltaSeconds = 0.5;
        }

        // 1. Update paddle position (mouse-controlled or velocity based).
        if (isMouseControlled) {
            paddle.setX(paddleTargetX - paddle.getWidth() / 2.0);
        } else {
            paddle.update(deltaSeconds);
        }
        checkPaddleBounds();

        // 2. Update balls with per-ball collision handling.
        List<Ball> ballsToRemove = new ArrayList<>();
        for (Ball ball : new ArrayList<>(balls)) {
            updateSingleBall(ball, deltaSeconds, ballsToRemove);
        }
        balls.removeAll(ballsToRemove);

        // If all balls are lost, decrement lives and possibly end the game.
        if (balls.isEmpty()) {
            lives--;
            if (lives <= 0) {
                try {
                    soundManager.playGameOver();
                } catch (Exception ignored) {
                }
                setState(GameState.GAME_OVER);
            } else {
                resetAfterLifeLost();
            }
        }

        // 3. Update bricks, power-ups, visual effects, and torpedoes.
        for (Brick brick : bricks) {
            brick.update(deltaSeconds);
        }
        updatePowerUps(deltaSeconds);
        updateEffects();

        Iterator<com.hung.arkanoid.model.entities.Torpedo> tIt = torpedoes.iterator();
        while (tIt.hasNext()) {
            com.hung.arkanoid.model.entities.Torpedo torpedo = tIt.next();
            torpedo.update(deltaSeconds);
            if (torpedo.getY() + torpedo.getHeight() < 0) {
                tIt.remove();
            }
        }
        handleTorpedoCollisions();
        updateActiveEffects(deltaSeconds);

        // 4. Check if all remaining bricks are unbreakable => level cleared.
        boolean cleared = bricks.stream().allMatch(Brick::isUnbreakable);
        if (cleared) {
            setState(GameState.LEVEL_CLEARED);
            SaveData.saveMaxLevelUnlocked(currentLevel + 1);
        }
    }

    /**
     * Updates a single ball including movement and collision against walls,
     * paddle and bricks. Balls that fall below the bottom are added to
     * {@code ballsToRemove}.
     */
    private void updateSingleBall(Ball ball, double deltaSeconds, List<Ball> ballsToRemove) {
        double ballPrevCX = 0;
        double ballPrevCY = 0;

        if (ball.isAttachedToPaddle()) {
            ball.setX(paddle.getX() + paddle.getWidth() / 2 - ball.getWidth() / 2);
            ball.setY(paddle.getY() - ball.getHeight() - 1);
        } else {
            ballPrevCX = ball.getX() + ball.getWidth() * 0.5;
            ballPrevCY = ball.getY() + ball.getHeight() * 0.5;
            ball.update(deltaSeconds);
        }

        if (!ball.isAttachedToPaddle()) {
            double ballNewCX = ball.getX() + ball.getWidth() * 0.5;
            double ballNewCY = ball.getY() + ball.getHeight() * 0.5;

            // If the ball hits the bottom and no barrier is active, mark it for removal.
            if (checkWallCollisions(ball)) {
                ballsToRemove.add(ball);
                return;
            }
            checkPaddleCollisions(ball, ballPrevCX, ballPrevCY, ballNewCX, ballNewCY);
            checkBrickCollisions(ball, ballPrevCX, ballPrevCY, ballNewCX, ballNewCY);
        } else {
            // Still keep the ball within walls and allow it to stick to the paddle.
            checkWallCollisions(ball);
            checkPaddleCollisions(ball, 0, 0, 0, 0);
        }
    }

    /**
     * Updates active power-ups, applies their effects when collected by the paddle
     * and removes them when consumed or when they fall below the bottom of the screen.
     */
    private void updatePowerUps(double deltaSeconds) {
        Iterator<PowerUp> iterator = powerUps.iterator();
        while (iterator.hasNext()) {
            PowerUp powerUp = iterator.next();
            powerUp.update(deltaSeconds);
            if (paddle.right() > powerUp.left() && paddle.left() < powerUp.right()
                    && paddle.bottom() > powerUp.top() && paddle.top() < powerUp.bottom()) {
                powerUp.applyEffect(this);
                powerUp.consume();
            }
            if (powerUp.isConsumed() || powerUp.getY() > SCREEN_HEIGHT) {
                iterator.remove();
            }
        }
    }

    /**
     * Updates view-layer visual effects and removes finished ones.
     */
    private void updateEffects() {
        Iterator<Effect> it = effects.iterator();
        while (it.hasNext()) {
            Effect effect = it.next();
            effect.update();
            if (effect.isFinished()) {
                it.remove();
            }
        }
    }

    /**
     * Handles torpedo/brick intersections, applying damage and score, spawning
     * power-ups, and removing torpedoes once they hit any brick.
     */
    private void handleTorpedoCollisions() {
        if (torpedoes.isEmpty()) {
            return;
        }
        Iterator<com.hung.arkanoid.model.entities.Torpedo> tIt = torpedoes.iterator();
        while (tIt.hasNext()) {
            com.hung.arkanoid.model.entities.Torpedo torpedo = tIt.next();
            boolean removed = false;
            for (Brick brick : new ArrayList<>(bricks)) {
                if (brick.isDestroyed()) {
                    continue;
                }
                boolean intersects =
                        torpedo.getX() + torpedo.getWidth() >= brick.getX()
                                && torpedo.getX() <= brick.getX() + brick.getWidth()
                                && torpedo.getY() <= brick.getY() + brick.getHeight()
                                && torpedo.getY() + torpedo.getHeight() >= brick.getY();
                if (intersects) {
                    brick.takeHit(this, null);
                    if (brick.isDestroyed()) {
                        score += brick.getScoreValue();
                        PowerUpType type = brick.getPowerUpToSpawn();
                        if (type != null) {
                            spawnPowerUp(brick, type);
                        }
                        try {
                            soundManager.playExplosion();
                        } catch (Exception ignored) {
                        }
                    } else {
                        try {
                            soundManager.playHardBrickHit();
                        } catch (Exception ignored) {
                        }
                    }
                    removed = true;
                    break;
                }
            }
            if (removed) {
                tIt.remove();
            }
        }
    }

    /**
     * Brick collision handler using a Swept AABB (axis-aligned bounding box)
     * approach. The ball is treated as a moving circle whose center moves
     * from (x0, y0) to (x1, y1); the bricks are static rectangles inflated
     * by the ball radius. The method resolves the earliest collision, applies
     * brick damage and optionally repeats to handle multiple hits in a single
     * frame (multi-hit loop).
     */
    private void checkBrickCollisions(Ball ball, double x0, double y0, double x1, double y1) {
        if (ball == null) {
            return;
        }
        double radius = ball.getWidth() * 0.5;
        double fx0 = x0;
        double fy0 = y0;
        double fx1 = x1;
        double fy1 = y1;

        int loopCount = 0;
        final int MAX_LOOPS = 10;
        List<Brick> processedBricksInFrame = new ArrayList<>();

        while (loopCount++ < MAX_LOOPS) {
            Brick nearestBrick = null;
            SweepResult nearest = null;

            // Step 1: find the closest brick hit along the swept segment.
            for (Brick brick : bricks) {
                if (brick.isDestroyed()) {
                    continue;
                }
                SweepResult res = computeSweepAgainstRect(
                        fx0, fy0, fx1, fy1, radius,
                        brick.getX(), brick.getY(), brick.getWidth(), brick.getHeight());
                if (res != null && (nearest == null || res.t < nearest.t)) {
                    nearest = res;
                    nearestBrick = brick;
                }
            }

            if (nearest == null) {
                break; // no more collisions along the remaining path
            }

            // Step 2: collect all bricks that are hit at the same time T (corner/gap cases).
            final double TOL = 1e-6;
            List<Brick> hitBricks = new ArrayList<>();
            List<SweepResult> hitResults = new ArrayList<>();
            for (Brick brick : bricks) {
                if (brick.isDestroyed()) {
                    continue;
                }
                SweepResult res = computeSweepAgainstRect(
                        fx0, fy0, fx1, fy1, radius,
                        brick.getX(), brick.getY(), brick.getWidth(), brick.getHeight());
                if (res != null && Math.abs(res.t - nearest.t) <= TOL) {
                    hitBricks.add(brick);
                    hitResults.add(res);
                }
            }

            boolean combinedInvX = false;
            boolean combinedInvY = false;
            boolean anyNewHitProcessed = false;
            boolean shouldBounce = true; // ball normally bounces unless fireball drilling through

            // Step 3: apply impact logic to each brick that was hit at time T.
            for (int i = 0; i < hitBricks.size(); i++) {
                Brick hitBrick = hitBricks.get(i);
                if (processedBricksInFrame.contains(hitBrick)) {
                    continue;
                }
                processedBricksInFrame.add(hitBrick);
                anyNewHitProcessed = true;

                SweepResult res = hitResults.get(i);
                if (res.inverseVx) {
                    combinedInvX = true;
                }
                if (res.inverseVy) {
                    combinedInvY = true;
                }

                boolean wasDestroyedBefore = hitBrick.isDestroyed();
                try {
                    hitBrick.onImpact(this, ball);
                } catch (Exception ignored) {
                }
                hitBrick.takeHit(this, ball);

                // Choose block or hard-block sound based on destruction and type.
                try {
                    if (hitBrick.isUnbreakable() || (!wasDestroyedBefore && !hitBrick.isDestroyed())) {
                        soundManager.playHardBrickHit();
                    } else {
                        soundManager.playBlockHit();
                    }
                } catch (Exception ignored) {
                }

                if (hitBrick.isDestroyed()) {
                    score += hitBrick.getScoreValue();
                    PowerUpType type = hitBrick.getPowerUpToSpawn();
                    if (type != null) {
                        spawnPowerUp(hitBrick, type);
                    }

                    if (hitBrick.getType() == com.hung.arkanoid.model.entities.brick.BrickType.EXPLOSIVE) {
                        try {
                            soundManager.playExplosion();
                        } catch (Exception ignored) {
                        }
                    }

                    // Fireballs drill through non-unbreakable bricks without bouncing.
                    if (ball.isFireball() && !hitBrick.isUnbreakable()) {
                        shouldBounce = false;
                    }
                }
            }

            if (!anyNewHitProcessed) {
                break; // avoid potential infinite loops in degenerate setups
            }

            // Step 4: reflect ball velocity and advance along the remaining segment.
            if (shouldBounce) {
                if (combinedInvX) {
                    ball.setVelocityX(-ball.getVelocityX());
                }
                if (combinedInvY) {
                    ball.setVelocityY(-ball.getVelocityY());
                }
                fx0 = nearest.hitX;
                fy0 = nearest.hitY;
                fx1 = nearest.correctedX;
                fy1 = nearest.correctedY;
            } else {
                // Drill-through path for fireball: continue towards original end point.
                fx0 = nearest.hitX + (x1 - x0) * 0.001;
                fy0 = nearest.hitY + (y1 - y0) * 0.001;
            }

            if (Math.hypot(fx1 - fx0, fy1 - fy0) < 1e-6) {
                break;
            }
        }

        // Place the ball at the final swept position (convert from center to top-left).
        ball.setX(fx1 - ball.getWidth() * 0.5);
        ball.setY(fy1 - ball.getHeight() * 0.5);

        bricks.removeIf(Brick::isDestroyed);
    }

    /**
     * Checks collisions between the ball and the playfield walls.
     * Reflects its velocity on side and top walls. When the ball reaches
     * the bottom, either activates the barrier bounce or marks the ball as
     * dropped.
     *
     * @return {@code true} if the ball fell below the bottom border
     */
    private boolean checkWallCollisions(Ball ball) {
        if (ball == null) {
            return false;
        }
        boolean dropped = false;

        // Left wall: only reflect if the ball is moving towards the wall.
        if (ball.getX() <= 0) {
            ball.setX(0);
            if (ball.getVelocityX() < 0) {
                ball.reverseDx();
            }
        } else if (ball.getX() + ball.getWidth() >= SCREEN_WIDTH) {
            // Right wall
            ball.setX(SCREEN_WIDTH - ball.getWidth());
            if (ball.getVelocityX() > 0) {
                ball.reverseDx();
            }
        }

        // Top wall.
        if (ball.getY() <= 0) {
            ball.setY(0);
            if (ball.getVelocityY() < 0) {
                ball.reverseDy();
            }
        }

        // Bottom border.
        if (ball.getY() + ball.getHeight() >= SCREEN_HEIGHT) {
            if (barrierActive) {
                ball.setY(SCREEN_HEIGHT - ball.getHeight());
                if (ball.getVelocityY() > 0) {
                    ball.reverseDy();
                }
                setBarrierActive(false);
            } else {
                dropped = true;
            }
        }
        return dropped;
    }

    /**
     * Handles paddle/ball collisions, supporting both simple AABB intersection
     * and a precise swept collision when previous and new positions are known.
     * It also applies the catch power-up and controls bounce angle based on
     * the hit position on the paddle.
     */
    private void checkPaddleCollisions(Ball ball, double x0, double y0, double x1, double y1) {
        if (ball == null || paddle == null) {
            return;
        }

        // Fallback AABB hit test when there is no trajectory information.
        if (x1 == 0 && y1 == 0 && x0 == 0 && y0 == 0) {
            if (!ball.intersects(paddle)) {
                return;
            }
            try {
                soundManager.playPaddleHit();
            } catch (Exception ignored) {
            }

            double relative = ((ball.getX() + ball.getWidth() / 2.0)
                               - (paddle.getX() + paddle.getWidth() / 2.0))
                              / (paddle.getWidth() / 2.0);
            relative = Math.max(-1, Math.min(1, relative));
            double maxAngle = Math.toRadians(75);
            double angle = relative * maxAngle;
            double speed = Math.hypot(ball.getVelocityX(), ball.getVelocityY());
            ball.setVelocityX(speed * Math.sin(angle));
            ball.setVelocityY(-Math.abs(speed * Math.cos(angle)));

            if (paddle.isCatchActive()) {
                ball.setAttachedToPaddle(true);
                ball.setX(paddle.getX() + paddle.getWidth() / 2 - ball.getWidth() / 2);
                ball.setY(paddle.getY() - ball.getHeight() - 1);
            }
            return;
        }

        // Swept collision against paddle rectangle.
        double radius = ball.getWidth() * 0.5;
        SweepResult res = computeSweepAgainstRect(
                x0, y0, x1, y1, radius,
                paddle.getX(), paddle.getY(), paddle.getWidth(), paddle.getHeight());
        if (res == null) {
            return;
        }

        try {
            soundManager.playPaddleHit();
        } catch (Exception ignored) {
        }

        if (paddle.isCatchActive()) {
            ball.setAttachedToPaddle(true);
            ball.setX(paddle.getX() + paddle.getWidth() / 2 - ball.getWidth() / 2);
            ball.setY(paddle.getY() - ball.getHeight() - 1);
            return;
        }

        double rel = (res.hitX - (paddle.getX() + paddle.getWidth() / 2.0))
                     / (paddle.getWidth() / 2.0);
        rel = Math.max(-1, Math.min(1, rel));
        double maxAngle = Math.toRadians(75);
        double angle = rel * maxAngle;
        double speed = Math.hypot(ball.getVelocityX(), ball.getVelocityY());
        ball.setVelocityX(speed * Math.sin(angle));
        ball.setVelocityY(-Math.abs(speed * Math.cos(angle)));
        ball.setX(res.correctedX - ball.getWidth() * 0.5);
        ball.setY(res.correctedY - ball.getHeight() * 0.5);
    }

    /**
     * Ensures the paddle remains fully inside the horizontal screen bounds.
     */
    private void checkPaddleBounds() {
        if (paddle == null) {
            return;
        }
        if (paddle.getX() < 0) {
            paddle.setX(0);
        }
        if (paddle.getX() + paddle.getWidth() > SCREEN_WIDTH) {
            paddle.setX(SCREEN_WIDTH - paddle.getWidth());
        }
    }

    /**
     * Helper data structure returned by {@link #computeSweepAgainstRect(double, double, double, double, double, double, double, double, double)}
     * representing a collision along a swept circle-versus-rectangle test.
     */
    private static final class SweepResult {
        double t;
        double hitX;
        double hitY;
        boolean inverseVx;
        boolean inverseVy;
        double correctedX;
        double correctedY;
    }

    /**
     * Swept AABB helper used by {@link #checkBrickCollisions(Ball, double, double, double, double)}
     * and {@link #checkPaddleCollisions(Ball, double, double, double, double)}.
     * <p>
     * The ball is modeled as a circle with center moving from (x0, y0) to (x1, y1)
     * and radius {@code r}. The rectangle is defined by (rx, ry, rw, rh) and is
     * expanded by {@code r} so that the circle problem reduces to a point-vs-rect
     * sweep. The method returns the earliest hit time {@code t} in [0,1], the
     * collision point (hitX, hitY), flags indicating which velocity components
     * must be inverted and a corrected end position that reflects the ball out
     * of the rectangle.
     */
    private SweepResult computeSweepAgainstRect(double x0, double y0, double x1, double y1,
                                                double r,
                                                double rx, double ry, double rw, double rh) {
        final double EPS = 1e-9;
        double minXr = rx - r;
        double minYr = ry - r;
        double maxXr = rx + rw + r;
        double maxYr = ry + rh + r;
        double dx = x1 - x0;
        double dy = y1 - y0;
        double bestT = Double.POSITIVE_INFINITY;
        double hitX = 0;
        double hitY = 0;
        boolean invX = false;
        boolean invY = false;

        // Horizontal (top/bottom) tests – only if the ball is moving in the
        // appropriate vertical direction to hit that edge.
        if (Math.abs(dy) > EPS) {
            if (dy > 0) {
                double tTop = (minYr - y0) / dy;
                double xAtTop = x0 + tTop * dx;
                if (tTop >= -EPS && tTop <= 1.0 + EPS && xAtTop >= minXr - EPS && xAtTop <= maxXr + EPS) {
                    if (tTop < bestT) {
                        bestT = tTop;
                        hitX = xAtTop;
                        hitY = minYr;
                        invX = false;
                        invY = true;
                    }
                }
            } else {
                double tBottom = (maxYr - y0) / dy;
                double xAtBottom = x0 + tBottom * dx;
                if (tBottom >= -EPS && tBottom <= 1.0 + EPS && xAtBottom >= minXr - EPS && xAtBottom <= maxXr + EPS) {
                    if (tBottom < bestT) {
                        bestT = tBottom;
                        hitX = xAtBottom;
                        hitY = maxYr;
                        invX = false;
                        invY = true;
                    }
                }
            }
        }

        // Vertical (left/right) tests – again only when moving toward that edge.
        if (Math.abs(dx) > EPS) {
            if (dx > 0) {
                double tLeft = (minXr - x0) / dx;
                double yAtLeft = y0 + tLeft * dy;
                if (tLeft >= -EPS && tLeft <= 1.0 + EPS && yAtLeft >= minYr - EPS && yAtLeft <= maxYr + EPS) {
                    if (tLeft < bestT) {
                        bestT = tLeft;
                        hitX = minXr;
                        hitY = yAtLeft;
                        invX = true;
                        invY = false;
                    } else if (Math.abs(tLeft - bestT) <= EPS) {
                        // Corner hit: invert both components.
                        invX = true;
                        invY = true;
                    }
                }
            } else {
                double tRight = (maxXr - x0) / dx;
                double yAtRight = y0 + tRight * dy;
                if (tRight >= -EPS && tRight <= 1.0 + EPS && yAtRight >= minYr - EPS && yAtRight <= maxYr + EPS) {
                    if (tRight < bestT) {
                        bestT = tRight;
                        hitX = maxXr;
                        hitY = yAtRight;
                        invX = true;
                        invY = false;
                    } else if (Math.abs(tRight - bestT) <= EPS) {
                        invX = true;
                        invY = true;
                    }
                }
            }
        }

        // Handle overlap case where the end point already lies inside the rectangle.
        if (!Double.isFinite(bestT)) {
            if (x0 >= minXr - EPS && x0 <= maxXr + EPS && y0 >= minYr - EPS && y0 <= maxYr + EPS) {
                double distanceLeft = Math.abs(x1 - minXr);
                double distanceRight = Math.abs(maxXr - x1);
                double distanceTop = Math.abs(y1 - minYr);
                double distanceBottom = Math.abs(maxYr - y1);
                double m = Math.min(Math.min(distanceLeft, distanceRight), Math.min(distanceTop, distanceBottom));
                if (m == distanceLeft) {
                    hitX = minXr;
                    hitY = y1;
                    invX = true;
                    invY = false;
                } else if (m == distanceRight) {
                    hitX = maxXr;
                    hitY = y1;
                    invX = true;
                    invY = false;
                } else if (m == distanceTop) {
                    hitX = x1;
                    hitY = minYr;
                    invX = false;
                    invY = true;
                } else {
                    hitX = x1;
                    hitY = maxYr;
                    invX = false;
                    invY = true;
                }
                bestT = 1.0;
            } else {
                return null;
            }
        }

        SweepResult result = new SweepResult();
        result.t = bestT;
        result.hitX = hitX;
        result.hitY = hitY;
        result.inverseVx = invX;
        result.inverseVy = invY;

        double remX = x1 - hitX;
        double remY = y1 - hitY;
        result.correctedX = hitX + (invX ? -remX : remX);
        result.correctedY = hitY + (invY ? -remY : remY);
        return result;
    }

    /**
     * Triggers a chain explosion around the given source brick. Nearby bricks
     * are destroyed, score and power-ups are applied, and further chain
     * explosions are scheduled when explosive bricks are hit.
     */
    public void explodeBricksAround(Brick sourceBrick) {
        Deque<Brick> queue = new ArrayDeque<>();
        queue.add(sourceBrick);
        while (!queue.isEmpty()) {
            Brick center = queue.poll();
            double centerX = center.getX() + center.getWidth() / 2.0;
            double centerY = center.getY() + center.getHeight() / 2.0;
            effects.add(new ExplosionEffect(centerX, centerY));

            double areaX = center.getX() - Brick.BRICK_WIDTH;
            double areaY = center.getY() - Brick.BRICK_HEIGHT;
            double areaW = Brick.BRICK_WIDTH * 3;
            double areaH = Brick.BRICK_HEIGHT * 3;
            Rectangle2D area = new Rectangle2D(areaX, areaY, areaW, areaH);

            for (Brick brick : new ArrayList<>(bricks)) {
                if (brick == center || brick.isDestroyed() || brick.isUnbreakable()) {
                    continue;
                }
                boolean intersects =
                        brick.right() > area.getMinX() && brick.left() < area.getMaxX()
                                && brick.bottom() > area.getMinY() && brick.top() < area.getMaxY();
                if (intersects) {
                    brick.setDestroyed(true);
                    double bx = brick.getX() + brick.getWidth() / 2.0;
                    double by = brick.getY() + brick.getHeight() / 2.0;
                    effects.add(new ExplosionEffect(bx, by));
                    score += brick.getScoreValue();
                    PowerUpType type = brick.getPowerUpToSpawn();
                    if (type != null) {
                        spawnPowerUp(brick, type);
                    }
                    if (brick.getType() == BrickType.EXPLOSIVE) {
                        queue.add(brick);
                    }
                }
            }
        }
    }

    /**
     * Resets paddle, balls and transient visuals after the player loses a life.
     */
    private void resetAfterLifeLost() {
        paddle.reset();
        paddle.setX(SCREEN_WIDTH / 2 - paddle.getWidth() / 2);
        paddle.setY(SCREEN_HEIGHT - 50);
        resetBall();
        powerUps.clear();
        effects.clear();
    }

    private void spawnPowerUp(Brick brick, PowerUpType type) {
        double x = brick.getX();
        double y = brick.getY();
        try {
            PowerUp powerUp = PowerUpFactory.createPowerUp(type, x, y);
            if (powerUp != null) {
                powerUps.add(powerUp);
            }
        } catch (Exception ex) {
            System.err.println("Failed to spawn powerup " + type + " at (" + x + "," + y + "): " + ex.getMessage());
        }
    }
}

