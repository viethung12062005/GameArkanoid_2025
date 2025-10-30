package com.hung.arkanoid.game;

import com.hung.arkanoid.game.SoundManager;
import com.hung.arkanoid.model.entities.Ball;
import com.hung.arkanoid.model.entities.Paddle;
import com.hung.arkanoid.model.entities.brick.Brick;
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

public class GameManager {
    public enum GameState { PLAYING, PAUSED, LEVEL_CLEARED, GAME_OVER }

    private GameState currentState = GameState.PLAYING;
    private final int currentLevel;

    // Use world dimensions matching the window (800x600)
    public static final double SCREEN_WIDTH = 800.0;
    public static final double SCREEN_HEIGHT = 600.0;

    private final Paddle paddle;
    private final Ball ball;

    // Mouse control fields
    private double paddleTargetX;
    private boolean isMouseControlled = false;

    private boolean paddleMovingLeft = false;
    private boolean paddleMovingRight = false;

    private final List<Brick> bricks;

    private int lives = 3;
    private int score = 0;

    private final List<PowerUp> powerUps = new ArrayList<>();

    private boolean barrierActive = false;

    // Torpedoes
    private final List<com.hung.arkanoid.model.entities.Torpedo> torpedoes = new ArrayList<>();

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

    // Visual effects (particles, explosion, etc.)
    private final List<Effect> effects = new ArrayList<>();

    // Sound manager
    private final SoundManager soundManager = new SoundManager();

    // Backwards-compatible default constructor (defaults to level 1)
    public GameManager() {
        this(1);
    }

    public GameManager(int levelNumber) {
        this.currentLevel = levelNumber;
        this.bricks = LevelLoader.loadLevel(levelNumber);
        this.paddle = new Paddle(350, 550);
        this.ball = new Ball(380, 530, Ball.BALL_RADIUS * 2, (int) Ball.BASE_SPEED, 1, 0, -Ball.BASE_SPEED/Math.sqrt(2));
    }

    // Mouse control setters
    public void setMouseControlled(boolean isMouseControlled) { this.isMouseControlled = isMouseControlled; }
    public void setPaddleTargetX(double x) { this.isMouseControlled = true; this.paddleTargetX = x; }

    @SuppressWarnings("unused")
    public GameState getCurrentState() { return currentState; }
    @SuppressWarnings("unused")
    public int getCurrentLevel() { return currentLevel; }
    @SuppressWarnings("unused")
    public int getNextLevel() { return currentLevel + 1; }

    public void setState(GameState s) { this.currentState = s; }

    @SuppressWarnings("unused")
    public void togglePause() {
        if (currentState == GameState.PLAYING) currentState = GameState.PAUSED;
        else if (currentState == GameState.PAUSED) currentState = GameState.PLAYING;
    }

    public void setPaddleMovingLeft(boolean v) {
        this.isMouseControlled = false; // keyboard takes over
        this.paddleMovingLeft = v;
        if (v) paddle.moveLeft(); else if (!paddleMovingRight) paddle.stopMoving();
    }

    public void setPaddleMovingRight(boolean v) {
        this.isMouseControlled = false; // keyboard takes over
        this.paddleMovingRight = v;
        if (v) paddle.moveRight(); else if (!paddleMovingLeft) paddle.stopMoving();
    }

    @SuppressWarnings("unused")
    public void launchBall() {
        ball.launch();
    }

    @SuppressWarnings("unused")
    public List<PowerUp> getPowerUps() { return powerUps; }
    @SuppressWarnings("unused")
    public Paddle getPaddle() { return paddle; }
    @SuppressWarnings("unused")
    public Ball getBall() { return ball; }
    @SuppressWarnings("unused")
    public int getLives() { return lives; }
    @SuppressWarnings("unused")
    public int getScore() { return score; }

    @SuppressWarnings("unused")
    public List<Brick> getBricksSafe() { return new ArrayList<>(bricks); }

    // Expose visual effects to the view
    public List<Effect> getEffects() { return effects; }

    // Expose torpedoes for rendering
    public List<com.hung.arkanoid.model.entities.Torpedo> getTorpedoes() { return new ArrayList<>(torpedoes); }

    // Backwards-compatible input API used by GameController
    public void setMovingLeft(boolean v) { setPaddleMovingLeft(v); }
    public void setMovingRight(boolean v) { setPaddleMovingRight(v); }

    // Called by GameController to set paddle X based on mouse/keyboard target center
    public void setPaddleX(double centerX) {
        paddle.setX(centerX - paddle.getWidth() / 2.0);
        checkPaddleBounds();
    }

    // updateActiveEffects: decrement remaining time by deltaSeconds (seconds)
    private void updateActiveEffects(double deltaSeconds) {
        if (deltaSeconds <= 0) return;
        Iterator<ActiveEffect> it = activeEffects.iterator();
        long now = System.currentTimeMillis();
        while (it.hasNext()) {
            ActiveEffect e = it.next();
            // Debug: show remaining before/after decrement for tracing timing issues
            System.out.println("[DEBUG] Effect tick: id=" + e.id + " before=" + String.format("%.3f", e.remaining) + "s delta=" + String.format("%.3f", deltaSeconds) + "s at " + now);
            e.remaining -= deltaSeconds;
            System.out.println("[DEBUG] Effect tick: id=" + e.id + " after=" + String.format("%.3f", e.remaining) + "s at " + now);
            if (e.remaining <= 0) {
                try {
                    System.out.println("[DEBUG] Effect expiring now: id=" + e.id + " at " + now);
                    e.onExpire.run();
                } catch (Exception ex) {
                    System.err.println("Error running onExpire for effect " + e.id + ": " + ex.getMessage());
                }
                it.remove();
            }
        }
    }

    private void registerTimedEffect(String id, double duration, Runnable onExpire) {
        System.out.println("[DEBUG] registerTimedEffect: id=" + id + " duration=" + duration);
        activeEffects.removeIf(e -> e.id.equals(id));
        activeEffects.add(new ActiveEffect(id, duration, onExpire));
    }

    // Convenience methods
    public void applyExpandPaddle(double duration) { paddle.expand(); registerTimedEffect("EXPAND_PADDLE", duration, paddle::resetSize); }
    public void applyShrinkPaddle(double duration) { paddle.shrink(); registerTimedEffect("SHRINK_PADDLE", duration, paddle::resetSize); }
    public void applyActivateLasers(double duration) { paddle.setLasersActive(true); registerTimedEffect("LASERS", duration, () -> paddle.setLasersActive(false)); }
    public void applyActivateCatch(double duration) { paddle.setCatchActive(true); registerTimedEffect("CATCH", duration, () -> paddle.setCatchActive(false)); }
    public void applySetBallSpeedMultiplier(double multiplier, double duration) { ball.setSpeedMultiplier(multiplier); registerTimedEffect("SPEED_MULTIPLIER", duration, () -> ball.setSpeedMultiplier(1.0)); }
    public void applyFireball(double duration) { ball.activateFireball(true); registerTimedEffect("FIREBALL", duration, () -> ball.activateFireball(false)); }
    public void applyBarrier(double duration) { setBarrierActive(true); registerTimedEffect("BARRIER", duration, () -> setBarrierActive(false)); }

    // Fire a torpedo from paddle center if lasers are active
    public void fireTorpedo() {
        if (!paddle.areLasersActive()) return;
        if (!torpedoes.isEmpty()) return; // single-shot like original behavior
        double cx = paddle.getX() + paddle.getWidth() / 2.0;
        double topY = paddle.getY() - 1.0;
        com.hung.arkanoid.model.entities.Torpedo t = new com.hung.arkanoid.model.entities.Torpedo(cx, topY, -400.0);
        torpedoes.add(t);
        try { soundManager.playLaser(); } catch (Exception ignored) {}
    }

    public void addLife() { lives++; }
    @SuppressWarnings("unused")
    public void activateBarrier(boolean active) { setBarrierActive(active); }

    public void spawnExtraBalls(int count) { System.out.println("Spawning extra balls: " + count + " (placeholder)"); }
    public void addBrick(Brick brick) { if (brick != null) bricks.add(brick); }

    // Added setter/getter for barrierActive (was missing and caused compile errors)
    public void setBarrierActive(boolean active) { this.barrierActive = active; }
    public boolean isBarrierActive() { return this.barrierActive; }

    /**
     * Update the game state. The delta parameter is the elapsed time in seconds since the
     * previous update call (derived from AnimationTimer nanoTime). All durations used by
     * powerups and timed effects are treated as seconds.
     */
    public void update(double deltaSeconds) {
        if (currentState != GameState.PLAYING) return;

        // clamp delta to avoid huge jumps
        if (deltaSeconds <= 0) deltaSeconds = 1.0 / 60.0;
        if (deltaSeconds > 0.5) deltaSeconds = 0.5;

        // Paddle movement: mouse or keyboard
        if (isMouseControlled) {
            // center paddle on mouse target X
            paddle.setX(paddleTargetX - paddle.getWidth() / 2.0);
        } else {
            // keyboard-controlled
            paddle.update(deltaSeconds);
        }

        // Ensure paddle is inside screen AFTER moving
        checkPaddleBounds();

        // Ball follows paddle if attached; otherwise perform movement with swept collision support
        double ballPrevCX = 0, ballPrevCY = 0;
        if (ball.isAttachedToPaddle()) {
            ball.setX(paddle.getX() + paddle.getWidth() / 2 - ball.getWidth() / 2);
            ball.setY(paddle.getY() - ball.getHeight() - 1);
        } else {
            // record previous center (convert from left/top to center)
            ballPrevCX = ball.getX() + ball.getWidth() * 0.5;
            ballPrevCY = ball.getY() + ball.getHeight() * 0.5;
            // move ball position according to velocity
            ball.update(deltaSeconds);
        }

        // update bricks
        for (Brick b : bricks) b.update(deltaSeconds);

        // update powerups
        updatePowerUps(deltaSeconds);

        // update visual effects
        updateEffects();

        // update torpedoes with deltaSeconds and remove off-screen
        Iterator<com.hung.arkanoid.model.entities.Torpedo> tIt = torpedoes.iterator();
        while (tIt.hasNext()) {
            com.hung.arkanoid.model.entities.Torpedo t = tIt.next();
            t.update(deltaSeconds);
            if (t.getY() + t.getHeight() < 0) tIt.remove();
        }

        // handle torpedo collisions (bricks/enemies)
        handleTorpedoCollisions();

        // timed effects
        updateActiveEffects(deltaSeconds);

        // collisions (use swept test between previous and current ball positions when applicable)
        if (!ball.isAttachedToPaddle()) {
            double ballNewCX = ball.getX() + ball.getWidth() * 0.5;
            double ballNewCY = ball.getY() + ball.getHeight() * 0.5;
             // walls
             checkWallCollisions();
             // paddle and bricks using swept detection
            checkPaddleCollisions(ballPrevCX, ballPrevCY, ballNewCX, ballNewCY);
            checkBrickCollisions(ballPrevCX, ballPrevCY, ballNewCX, ballNewCY);
         } else {
            // no movement -> still check walls/paddle normally
            checkWallCollisions();
            checkPaddleCollisions();
            checkBrickCollisions();
        }

        // check level cleared: when only unbreakable bricks remain
        boolean cleared = bricks.stream().allMatch(Brick::isUnbreakable);
        if (cleared) {
            setState(GameState.LEVEL_CLEARED);
            SaveData.saveMaxLevelUnlocked(currentLevel + 1);
        }
    }

    private void updatePowerUps(double deltaSeconds) {
        Iterator<PowerUp> iterator = powerUps.iterator();
        while (iterator.hasNext()) {
            PowerUp pu = iterator.next();
            pu.update(deltaSeconds);
            // check collision between paddle bounds and powerup bounds
            if (paddle.right() > pu.left() && paddle.left() < pu.right() && paddle.bottom() > pu.top() && paddle.top() < pu.bottom()) {
                pu.applyEffect(this);
                pu.consume();
            }
            if (pu.isConsumed() || pu.getY() > SCREEN_HEIGHT) iterator.remove();
        }
    }

    private void updateEffects() {
        Iterator<Effect> it = effects.iterator();
        while (it.hasNext()) {
            Effect e = it.next();
            e.update();
            if (e.isFinished()) it.remove();
        }
    }

    // Torpedo collision checks: with bricks
    private void handleTorpedoCollisions() {
        if (torpedoes.isEmpty()) return;
        Iterator<com.hung.arkanoid.model.entities.Torpedo> tIt = torpedoes.iterator();
        while (tIt.hasNext()) {
            com.hung.arkanoid.model.entities.Torpedo t = tIt.next();
            boolean removed = false;
            for (Brick b : new ArrayList<>(bricks)) {
                if (b.isDestroyed()) continue;
                if (t.getX() + t.getWidth() >= b.getX() && t.getX() <= b.getX() + b.getWidth()
                        && t.getY() <= b.getY() + b.getHeight() && t.getY() + t.getHeight() >= b.getY()) {
                    b.takeHit(this, null);
                    if (b.isDestroyed()) {
                        score += b.getScoreValue();
                        PowerUpType pu = b.getPowerUpToSpawn();
                        if (pu != null) spawnPowerUp(b, pu);
                        try { soundManager.playExplosion(); } catch (Exception ignored) {}
                    } else {
                        try { soundManager.playHardBrickHit(); } catch (Exception ignored) {}
                    }
                    removed = true;
                    break;
                }
            }
            if (removed) tIt.remove();
        }
    }

    private void checkBrickCollisions(double x0, double y0, double x1, double y1) {
        if (ball == null) return;
        double radius = ball.getWidth() * 0.5;
        // We'll iterate: the ball may hit several bricks in one move; handle sequentially
        double fx0 = x0, fy0 = y0, fx1 = x1, fy1 = y1;
        while (true) {
            Brick nearestBrick = null;
            SweepResult nearest = null;
            // find earliest hit among bricks
            for (Brick b : bricks) {
                if (b.isDestroyed() || b.isUnbreakable() && ball.isFireball()) continue; // fireball passes through non-unbreakable? keep logic
                double bx0 = b.getX();
                double by0 = b.getY();
                SweepResult res = computeSweepAgainstRect(fx0, fy0, fx1, fy1, radius, bx0, by0, b.getWidth(), b.getHeight());
                if (res != null) {
                    if (nearest == null || res.t < nearest.t) { nearest = res; nearestBrick = b; }
                }
            }
            if (nearest == null) break; // no hit

            // process hit(s) with nearestBrick at nearest.hitX/Y
            // Collect all bricks hit at the same earliest time (to handle corner-double-hits)
            final double TOL = 1e-6;
            List<Brick> hitBricks = new ArrayList<>();
            List<SweepResult> hitResults = new ArrayList<>();
            for (Brick b2 : bricks) {
                if (b2.isDestroyed() || (b2.isUnbreakable() && ball.isFireball())) continue;
                SweepResult r2 = computeSweepAgainstRect(fx0, fy0, fx1, fy1, radius, b2.getX(), b2.getY(), b2.getWidth(), b2.getHeight());
                if (r2 != null && Math.abs(r2.t - nearest.t) <= TOL) {
                    hitBricks.add(b2);
                    hitResults.add(r2);
                }
            }

            // First, call onImpact for all hit bricks (some may trigger explosions/etc.)
            for (Brick hb : hitBricks) {
                try { hb.onImpact(this, ball); } catch (Exception ex) { System.err.println("Error onImpact: " + ex.getMessage()); }
            }

            // Then apply hits and collect combined reflection flags and scoring
            boolean combinedInvX = false, combinedInvY = false;
            for (int i = 0; i < hitBricks.size(); i++) {
                Brick hb = hitBricks.get(i);
                SweepResult hr = hitResults.get(i);
                boolean wasDestroyedBefore = hb.isDestroyed();
                hb.takeHit(this, ball);
                if (hr.inverseVx) combinedInvX = true;
                if (hr.inverseVy) combinedInvY = true;
                try {
                    if (hb.isUnbreakable() || (!wasDestroyedBefore && !hb.isDestroyed())) soundManager.playHardBrickHit(); else soundManager.playBlockHit();
                } catch (Exception ignored) {}
                if (hb.isDestroyed()) {
                    score += hb.getScoreValue();
                    PowerUpType pt = hb.getPowerUpToSpawn();
                    if (pt != null) spawnPowerUp(hb, pt);
                    if (hb.getType() == com.hung.arkanoid.model.entities.brick.BrickType.EXPLOSIVE) {
                        try { soundManager.playExplosion(); } catch (Exception ignored) {}
                    }
                }
            }

            // reflect velocity components according to combined flags
            if (!(ball.isFireball() && !nearestBrick.isUnbreakable())) {
                if (combinedInvX) ball.setVelocityX(-ball.getVelocityX());
                if (combinedInvY) ball.setVelocityY(-ball.getVelocityY());
            }

            // prepare for next iteration: move origin to hit point, set next target to corrected point
            fx0 = nearest.hitX;
            fy0 = nearest.hitY;
            fx1 = nearest.correctedX;
            fy1 = nearest.correctedY;
            // if the corrected vector is negligible, stop
            if (Math.hypot(fx1 - fx0, fy1 - fy0) < 1e-6) break;
        }

        // finally set ball position to last fx1,fy1 (converted from center to left/top)
        ball.setX(fx1 - ball.getWidth() * 0.5);
        ball.setY(fy1 - ball.getHeight() * 0.5);
         // cleanup destroyed bricks
         bricks.removeIf(Brick::isDestroyed);
    }

    // Keep a simple no-arg brick collision handler for compatibility with earlier code paths
    private void checkBrickCollisions() {
        if (ball == null) return;
        Iterator<Brick> it = bricks.iterator();
        while (it.hasNext()) {
            Brick brick = it.next();
            if (ball.intersects(brick)) {
                // 1. Apply impact/effects
                brick.onImpact(this, ball);
                // 2. Take hit
                boolean wasDestroyedBefore = brick.isDestroyed();
                brick.takeHit(this, ball);

                // play block hit or hard hit
                try {
                    if (brick.isUnbreakable() || (!wasDestroyedBefore && !brick.isDestroyed())) {
                        soundManager.playHardBrickHit();
                    } else {
                        soundManager.playBlockHit();
                    }
                } catch (Exception ignored) {}

                // 3. Bounce logic
                if (!(ball.isFireball() && !brick.isUnbreakable())) {
                    ball.reverseDy();
                }
                // 4. If destroyed, award points and maybe spawn powerup
                if (brick.isDestroyed()) {
                    score += brick.getScoreValue();
                    com.hung.arkanoid.model.entities.powerup.PowerUpType typeToSpawn = brick.getPowerUpToSpawn();
                    if (typeToSpawn != null) spawnPowerUp(brick, typeToSpawn);
                    // explosive sound if needed
                    if (brick.getType() == com.hung.arkanoid.model.entities.brick.BrickType.EXPLOSIVE) {
                        try { soundManager.playExplosion(); } catch (Exception ignored) {}
                    }
                }
                break;
            }
        }
        bricks.removeIf(Brick::isDestroyed);
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
                    try { soundManager.playGameOver(); } catch (Exception ignored) {}
                    setState(GameState.GAME_OVER);
                } else {
                    resetAfterLifeLost();
                }
            }
        }
    }

    private void checkPaddleCollisions() { checkPaddleCollisions(0,0,0,0); }

    private void checkPaddleCollisions(double x0, double y0, double x1, double y1) {
        if (ball == null || paddle == null) return;
        if (x1 == 0 && y1 == 0 && x0 == 0 && y0 == 0) {
            // fallback to simple intersect
            if (!ball.intersects(paddle)) return;
            try { soundManager.playPaddleHit(); } catch (Exception ignored) {}
            double relative = ((ball.getX() + ball.getWidth() / 2.0) - (paddle.getX() + paddle.getWidth() / 2.0)) / (paddle.getWidth() / 2.0);
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
        double radius = ball.getWidth() * 0.5;
        SweepResult res = computeSweepAgainstRect(x0, y0, x1, y1, radius, paddle.getX(), paddle.getY(), paddle.getWidth(), paddle.getHeight());
        if (res == null) return;
        // hit detected
        try { soundManager.playPaddleHit(); } catch (Exception ignored) {}
        // sticky / catch
        if (paddle.isCatchActive()) {
            ball.setAttachedToPaddle(true);
            ball.setX(paddle.getX() + paddle.getWidth() / 2 - ball.getWidth() / 2);
            ball.setY(paddle.getY() - ball.getHeight() - 1);
            return;
        }
        // influence by paddle movement: preserve previous behavior by adjusting angle
        double pbx = paddle.getX();
        // compute relative hit pos
        double rel = (res.hitX - (paddle.getX() + paddle.getWidth() / 2.0)) / (paddle.getWidth() / 2.0);
        rel = Math.max(-1, Math.min(1, rel));
        double maxAngle = Math.toRadians(75);
        double angle = rel * maxAngle;
        double speed = Math.hypot(ball.getVelocityX(), ball.getVelocityY());
        ball.setVelocityX(speed * Math.sin(angle));
        ball.setVelocityY(-Math.abs(speed * Math.cos(angle)));
        // position ball at corrected point (converted center -> left/top)
        ball.setX(res.correctedX - ball.getWidth() * 0.5);
        ball.setY(res.correctedY - ball.getHeight() * 0.5);
    }

    // Ensure paddle stays inside screen
    private void checkPaddleBounds() {
        if (paddle == null) return;
        if (paddle.getX() < 0) paddle.setX(0);
        if (paddle.getX() + paddle.getWidth() > SCREEN_WIDTH) paddle.setX(SCREEN_WIDTH - paddle.getWidth());
    }

    // Helper class for sweep results
    private static final class SweepResult {
        double t; double hitX; double hitY; boolean inverseVx; boolean inverseVy; double correctedX; double correctedY;
    }

    private SweepResult computeSweepAgainstRect(double x0, double y0, double x1, double y1, double r, double rx, double ry, double rw, double rh) {
        final double EPS = 1e-9;
        double minXr = rx - r, minYr = ry - r, maxXr = rx + rw + r, maxYr = ry + rh + r;
        double dx = x1 - x0, dy = y1 - y0;
        double bestT = Double.POSITIVE_INFINITY;
        double hitX=0, hitY=0; boolean invX=false, invY=false;
        // horizontal edges
        if (Math.abs(dy) > EPS) {
            double tTop = (minYr - y0) / dy; double xAtTop = x0 + tTop * dx;
            if (tTop >= -EPS && tTop <= 1.0 + EPS && xAtTop >= minXr - EPS && xAtTop <= maxXr + EPS) {
                if (tTop < bestT) { bestT = tTop; hitX = xAtTop; hitY = minYr; invX=false; invY=true; }
            }
            double tBot = (maxYr - y0) / dy; double xAtBot = x0 + tBot * dx;
            if (tBot >= -EPS && tBot <= 1.0 + EPS && xAtBot >= minXr - EPS && xAtBot <= maxXr + EPS) {
                if (tBot < bestT) { bestT = tBot; hitX = xAtBot; hitY = maxYr; invX=false; invY=true; }
            }
        }
        if (Math.abs(dx) > EPS) {
            double tLeft = (minXr - x0) / dx; double yAtLeft = y0 + tLeft * dy;
            if (tLeft >= -EPS && tLeft <= 1.0 + EPS && yAtLeft >= minYr - EPS && yAtLeft <= maxYr + EPS) {
                if (tLeft < bestT) { bestT = tLeft; hitX = minXr; hitY = yAtLeft; invX=true; invY=false; }
                else if (Math.abs(tLeft - bestT) <= EPS) { invX=true; invY=true; }
            }
            double tRight = (maxXr - x0) / dx; double yAtRight = y0 + tRight * dy;
            if (tRight >= -EPS && tRight <= 1.0 + EPS && yAtRight >= minYr - EPS && yAtRight <= maxYr + EPS) {
                if (tRight < bestT) { bestT = tRight; hitX = maxXr; hitY = yAtRight; invX=true; invY=false; }
                else if (Math.abs(tRight - bestT) <= EPS) { invX=true; invY=true; }
            }
        }
        if (!Double.isFinite(bestT)) {
            if (x1 >= minXr - EPS && x1 <= maxXr + EPS && y1 >= minYr - EPS && y1 <= maxYr + EPS) {
                double dl = Math.abs(x1 - minXr), dr = Math.abs(maxXr - x1), dt = Math.abs(y1 - minYr), db = Math.abs(maxYr - y1);
                double m = Math.min(Math.min(dl, dr), Math.min(dt, db));
                if (m == dl) { hitX = minXr; hitY = y1; invX = true; invY = false; }
                else if (m == dr) { hitX = maxXr; hitY = y1; invX = true; invY = false; }
                else if (m == dt) { hitX = x1; hitY = minYr; invX = false; invY = true; }
                else { hitX = x1; hitY = maxYr; invX = false; invY = true; }
                bestT = 1.0;
            } else return null;
        }
        SweepResult res = new SweepResult(); res.t = bestT; res.hitX = hitX; res.hitY = hitY; res.inverseVx = invX; res.inverseVy = invY;
        double remX = x1 - hitX, remY = y1 - hitY; res.correctedX = hitX + (invX ? -remX : remX); res.correctedY = hitY + (invY ? -remY : remY);
        return res;
    }

    public void explodeBricksAround(Brick sourceBrick) {
        Deque<Brick> queue = new ArrayDeque<>();
        queue.add(sourceBrick);
        while (!queue.isEmpty()) {
            Brick center = queue.poll();

            // create visual explosion at center brick
            double centerX = center.getX() + center.getWidth() / 2.0;
            double centerY = center.getY() + center.getHeight() / 2.0;
            effects.add(new ExplosionEffect(centerX, centerY));

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
                    // visual explosion for this brick as well
                    double bx = b.getX() + b.getWidth() / 2.0;
                    double by = b.getY() + b.getHeight() / 2.0;
                    effects.add(new ExplosionEffect(bx, by));

                    score += b.getScoreValue();
                    PowerUpType t = b.getPowerUpToSpawn();
                    if (t != null) spawnPowerUp(b, t);
                    if (b.getType() == com.hung.arkanoid.model.entities.brick.BrickType.EXPLOSIVE) queue.add(b);
                }
            }
        }
    }

    private void resetAfterLifeLost() {
        paddle.reset();
        paddle.setX(SCREEN_WIDTH / 2 - paddle.getWidth() / 2);
        paddle.setY(SCREEN_HEIGHT - 50);
        ball.reset();
        ball.setAttachedToPaddle(true);
        ball.setX(paddle.getX() + paddle.getWidth() / 2 - ball.getWidth() / 2);
        ball.setY(paddle.getY() - ball.getHeight() - 1);
        powerUps.clear();
        // clear visual effects as well
        effects.clear();
    }

    private void spawnPowerUp(Brick brick, com.hung.arkanoid.model.entities.powerup.PowerUpType type) {
        double x = brick.getX();
        double y = brick.getY();
        try {
            com.hung.arkanoid.model.entities.powerup.PowerUp pu = com.hung.arkanoid.model.entities.powerup.PowerUpFactory.createPowerUp(type, x, y);
            if (pu != null) {
                powerUps.add(pu);
            }
        } catch (Exception ex) {
            System.err.println("Failed to spawn powerup " + type + " at (" + x + "," + y + "): " + ex.getMessage());
        }
    }
}
