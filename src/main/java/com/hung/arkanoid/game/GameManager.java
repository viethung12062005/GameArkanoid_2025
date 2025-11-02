package com.hung.arkanoid.game;

import com.hung.arkanoid.model.entities.Ball;
import com.hung.arkanoid.model.entities.Paddle;
import com.hung.arkanoid.model.entities.brick.Brick;
import com.hung.arkanoid.model.entities.powerup.PowerUp;
import com.hung.arkanoid.model.entities.powerup.PowerUpFactory;
import com.hung.arkanoid.model.entities.powerup.PowerUpType;
import javafx.geometry.Rectangle2D;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

public class GameManager {
    public enum GameState { PLAYING, PAUSED, LEVEL_CLEARED, GAME_OVER }

    private GameState currentState = GameState.PLAYING;
    private int currentLevel;

    public static final double SCREEN_WIDTH = 800;
    public static final double SCREEN_HEIGHT = 600;

    private final Paddle paddle;
    private final Ball ball;

    private boolean paddleMovingLeft = false;
    private boolean paddleMovingRight = false;

    private final List<Brick> bricks;

    private int lives = 3;
    private int score = 0;

    private final List<PowerUp> powerUps = new ArrayList<>();

    private boolean barrierActive = false;

    // Timed effects
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

    public GameManager(int levelNumber) {
        this.currentLevel = levelNumber;
        this.bricks = LevelLoader.loadLevel(levelNumber);
        this.paddle = new Paddle(350, 550);
        this.ball = new Ball(380, 530, Ball.BALL_RADIUS * 2, (int) Ball.BASE_SPEED, 1, 0, -Ball.BASE_SPEED/Math.sqrt(2));
    }

    public GameState getCurrentState() { return currentState; }
    public int getCurrentLevel() { return currentLevel; }
    public int getNextLevel() { return currentLevel + 1; }

    public void setState(GameState s) { this.currentState = s; }

    public void togglePause() {
        if (currentState == GameState.PLAYING) currentState = GameState.PAUSED;
        else if (currentState == GameState.PAUSED) currentState = GameState.PLAYING;
    }

    public void setPaddleMovingLeft(boolean v) {
        this.paddleMovingLeft = v;
        if (v) paddle.moveLeft(); else if (!paddleMovingRight) paddle.stopMoving();
    }

    public void setPaddleMovingRight(boolean v) {
        this.paddleMovingRight = v;
        if (v) paddle.moveRight(); else if (!paddleMovingLeft) paddle.stopMoving();
    }

    public void launchBall() {
        ball.launch();
    }

    public List<PowerUp> getPowerUps() { return powerUps; }
    public Paddle getPaddle() { return paddle; }
    public Ball getBall() { return ball; }
    public int getLives() { return lives; }
    public int getScore() { return score; }

    public List<Brick> getBricksSafe() { return new ArrayList<>(bricks); }

    private void updateActiveEffects(double delta) {
        Iterator<ActiveEffect> it = activeEffects.iterator();
        while (it.hasNext()) {
            ActiveEffect e = it.next();
            e.remaining -= delta;
            if (e.remaining <= 0) {
                try { e.onExpire.run(); } catch (Exception ex) { System.err.println("Error running onExpire for effect " + e.id + ": " + ex.getMessage()); }
                it.remove();
            }
        }
    }

    private void registerTimedEffect(String id, double duration, Runnable onExpire) {
        activeEffects.removeIf(e -> e.id.equals(id));
        activeEffects.add(new ActiveEffect(id, duration, onExpire));
    }

    // Convenience methods
    public void applyExpandPaddle(double duration) { paddle.expand(); registerTimedEffect("EXPAND_PADDLE", duration, paddle::reset); }
    public void applyShrinkPaddle(double duration) { paddle.shrink(); registerTimedEffect("SHRINK_PADDLE", duration, paddle::reset); }
    public void applyActivateLasers(double duration) { paddle.setLasersActive(true); registerTimedEffect("LASERS", duration, () -> paddle.setLasersActive(false)); }
    public void applyActivateCatch(double duration) { paddle.setCatchActive(true); registerTimedEffect("CATCH", duration, () -> paddle.setCatchActive(false)); }
    public void applySetBallSpeedMultiplier(double multiplier, double duration) { ball.setSpeedMultiplier(multiplier); registerTimedEffect("SPEED_MULTIPLIER", duration, () -> ball.setSpeedMultiplier(1.0)); }
    public void applyFireball(double duration) { ball.activateFireball(true); registerTimedEffect("FIREBALL", duration, () -> ball.activateFireball(false)); }
    public void applyBarrier(double duration) { setBarrierActive(true); registerTimedEffect("BARRIER", duration, () -> setBarrierActive(false)); }

    public void addLife() { lives++; }
    public void activateBarrier(boolean active) { setBarrierActive(active); }

    public void spawnExtraBalls(int count) { System.out.println("Spawning extra balls: " + count + " (placeholder)"); }
    public void addBrick(Brick brick) { if (brick != null) bricks.add(brick); }

    // Added setter/getter for barrierActive (was missing and caused compile errors)
    public void setBarrierActive(boolean active) { this.barrierActive = active; }
    public boolean isBarrierActive() { return this.barrierActive; }

    public void update() {
        if (currentState != GameState.PLAYING) return;

        // update paddle and ball
        paddle.update(1.0);
        ball.update(1.0);

        // update bricks
        for (Brick b : bricks) b.update(1.0);

        // update powerups
        updatePowerUps();

        // timed effects
        updateActiveEffects(1.0);

        // collisions
        checkWallCollisions();
        checkPaddleCollisions();
        checkBrickCollisions();

        // check level cleared: when only unbreakable bricks remain
        boolean cleared = bricks.stream().allMatch(Brick::isUnbreakable);
        if (cleared) {
            setState(GameState.LEVEL_CLEARED);
            SaveData.saveMaxLevelUnlocked(currentLevel + 1);
        }
    }

    private void updatePowerUps() {
        Iterator<PowerUp> iterator = powerUps.iterator();
        while (iterator.hasNext()) {
            PowerUp pu = iterator.next();
            pu.update(1.0);
            if (paddle.intersects(pu)) {
                pu.applyEffect(this);
                pu.setConsumed(true);
            }
            if (pu.isConsumed() || pu.getY() > SCREEN_HEIGHT) iterator.remove();
        }
    }

    private void checkBrickCollisions() {
        if (ball == null) return;
        Iterator<Brick> it = bricks.iterator();
        while (it.hasNext()) {
            Brick brick = it.next();
            if (ball.intersects(brick)) {
                brick.onImpact(this, ball);
                brick.takeHit(this, ball);
                if (!ball.isFireball()) ball.reverseDy();
                if (brick.isDestroyed()) {
                    score += brick.getScoreValue();
                    PowerUpType typeToSpawn = brick.getPowerUpToSpawn();
                    if (typeToSpawn != null) spawnPowerUp(brick, typeToSpawn);
                }
                break;
            }
        }
        bricks.removeIf(Brick::isDestroyed);
    }

    private void spawnPowerUp(Brick brick, PowerUpType type) {
        PowerUp pu = PowerUpFactory.createPowerUp(type, brick.getX(), brick.getY());
        if (pu != null) powerUps.add(pu);
    }

    private void checkWallCollisions() {
        if (ball == null) return;
        if (ball.getX() <= 0 || ball.getX() + ball.getWidth() >= SCREEN_WIDTH) ball.reverseDx();
        if (ball.getY() <= 0) ball.reverseDy();
        if (ball.getY() + ball.getHeight() >= SCREEN_HEIGHT) {
            // ball fell
            if (barrierActive) {
                ball.reverseDy();
                setBarrierActive(false);
            } else {
                lives--;
                if (lives <= 0) {
                    setState(GameState.GAME_OVER);
                } else {
                    resetAfterLifeLost();
                }
            }
        }
    }

    private void checkPaddleCollisions() {
        if (ball == null || paddle == null) return;
        if (ball.intersects(paddle)) {
            if (paddle.isCatchActive()) {
                ball.setAttachedToPaddle(true);
                ball.setX(paddle.getX() + paddle.getWidth() / 2 - ball.getWidth() / 2);
                ball.setY(paddle.getY() - ball.getHeight() - 1);
            } else {
                ball.bounceOff(paddle);
            }
        }
    }

    private void resetAfterLifeLost() {
        paddle.reset();
        paddle.setX(SCREEN_WIDTH / 2 - paddle.getWidth() / 2);
        paddle.setY(SCREEN_HEIGHT - 50);
        ball.reset();
        ball.setX(paddle.getX() + paddle.getWidth() / 2 - ball.getWidth() / 2);
        ball.setY(paddle.getY() - ball.getHeight() - 1);
        powerUps.clear();
    }


    public void explodeBricksAround(Brick sourceBrick) {
        Deque<Brick> queue = new ArrayDeque<>();
        queue.add(sourceBrick);
        while (!queue.isEmpty()) {
            Brick center = queue.poll();
            double areaX = center.getX() - Brick.BRICK_WIDTH;
            double areaY = center.getY() - Brick.BRICK_HEIGHT;
            double areaW = Brick.BRICK_WIDTH * 3;
            double areaH = Brick.BRICK_HEIGHT * 3;
            Rectangle2D area = new Rectangle2D(areaX, areaY, areaW, areaH);
            for (Brick b : new ArrayList<>(bricks)) {
                if (b == center) continue;
                if (b.isDestroyed() || b.isUnbreakable()) continue;
                boolean intersects = b.right() > area.getMinX() && b.left() < area.getMaxX()
                        && b.bottom() > area.getMinY() && b.top() < area.getMaxY();
                if (intersects) {
                    b.setDestroyed(true);
                    score += b.getScoreValue();
                    PowerUpType t = b.getPowerUpToSpawn();
                    if (t != null) spawnPowerUp(b, t);
                    if (b.getType() == com.hung.arkanoid.model.entities.brick.BrickType.EXPLOSIVE) queue.add(b);
                }
            }
        }
    }
}
