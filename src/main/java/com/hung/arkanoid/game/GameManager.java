package com.hung.arkanoid.game;

import com.hung.arkanoid.game.SoundManager;
import com.hung.arkanoid.model.entities.Ball;
import com.hung.arkanoid.model.entities.Paddle;
import com.hung.arkanoid.model.entities.brick.Brick;
import com.hung.arkanoid.model.entities.powerup.PowerUp;
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

    public static final double SCREEN_WIDTH = 800.0;
    public static final double SCREEN_HEIGHT = 600.0;

    private final Paddle paddle;

    // [CHANGED] Thay thế single Ball bằng List<Ball>
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
    private final SoundManager soundManager = new SoundManager();

    public GameManager() { this(1, 0); }

    public GameManager(int levelNumber) {
        this(levelNumber, 0);
    }

    public GameManager(int levelNumber, int currentScore) {
        this.currentLevel = levelNumber;
        this.score = currentScore; // Khởi tạo điểm với giá trị tích lũy
        this.bricks = LevelLoader.loadLevel(levelNumber);
        this.paddle = new Paddle(350, 550);
        resetBall();
    }

    // [ADDED] Helper để reset bóng về paddle
    private void resetBall() {
        balls.clear();
        Ball initialBall = new Ball(380, 530, Ball.BALL_RADIUS * 2, (int) Ball.BASE_SPEED, 1, 0, -Ball.BASE_SPEED/Math.sqrt(2));
        balls.add(initialBall);
    }

    public void setMouseControlled(boolean isMouseControlled) { this.isMouseControlled = isMouseControlled; }
    public void setPaddleTargetX(double x) { this.isMouseControlled = true; this.paddleTargetX = x; }

    public GameState getCurrentState() { return currentState; }
    public int getCurrentLevel() { return currentLevel; }
    public int getNextLevel() { return currentLevel + 1; }
    public void setState(GameState s) { this.currentState = s; }

    public void togglePause() {
        if (currentState == GameState.PLAYING) currentState = GameState.PAUSED;
        else if (currentState == GameState.PAUSED) currentState = GameState.PLAYING;
    }

    public void setPaddleMovingLeft(boolean v) {
        this.isMouseControlled = false;
        this.paddleMovingLeft = v;
        if (v) paddle.moveLeft(); else if (!paddleMovingRight) paddle.stopMoving();
    }
    public void setPaddleMovingRight(boolean v) {
        this.isMouseControlled = false;
        this.paddleMovingRight = v;
        if (v) paddle.moveRight(); else if (!paddleMovingLeft) paddle.stopMoving();
    }

    // [CHANGED] Launch tất cả các bóng đang dính
    public void launchBall() {
        for (Ball b : balls) {
            b.launch();
        }
    }

    public List<PowerUp> getPowerUps() { return powerUps; }
    public Paddle getPaddle() { return paddle; }

    // [CHANGED] Getter cho list bóng thay vì bóng đơn lẻ
    public List<Ball> getBalls() { return balls; }
    // Giữ lại getBall() để tương thích (trả về bóng đầu tiên)
    public Ball getBall() { return balls.isEmpty() ? null : balls.get(0); }

    public int getLives() { return lives; }
    public int getScore() { return score; }
    public List<Brick> getBricksSafe() { return new ArrayList<>(bricks); }
    public List<Effect> getEffects() { return effects; }
    public List<com.hung.arkanoid.model.entities.Torpedo> getTorpedoes() { return new ArrayList<>(torpedoes); }
    public void setMovingLeft(boolean v) { setPaddleMovingLeft(v); }
    public void setMovingRight(boolean v) { setPaddleMovingRight(v); }
    public void setPaddleX(double centerX) {
        paddle.setX(centerX - paddle.getWidth() / 2.0);
        checkPaddleBounds();
    }

    private void updateActiveEffects(double deltaSeconds) {
        if (deltaSeconds <= 0) return;
        Iterator<ActiveEffect> it = activeEffects.iterator();
        long now = System.currentTimeMillis();
        while (it.hasNext()) {
            ActiveEffect e = it.next();
            e.remaining -= deltaSeconds;
            if (e.remaining <= 0) {
                try { e.onExpire.run(); } catch (Exception ex) { System.err.println("Error onExpire: " + ex.getMessage()); }
                it.remove();
            }
        }
    }
    private void registerTimedEffect(String id, double duration, Runnable onExpire) {
        activeEffects.removeIf(e -> e.id.equals(id));
        activeEffects.add(new ActiveEffect(id, duration, onExpire));
    }

    public void applyExpandPaddle(double duration) { paddle.expand(); registerTimedEffect("EXPAND_PADDLE", duration, paddle::resetSize); }
    public void applyShrinkPaddle(double duration) { paddle.shrink(); registerTimedEffect("SHRINK_PADDLE", duration, paddle::resetSize); }
    public void applyActivateLasers(double duration) { paddle.setLasersActive(true); registerTimedEffect("LASERS", duration, () -> paddle.setLasersActive(false)); }
    public void applyActivateCatch(double duration) { paddle.setCatchActive(true); registerTimedEffect("CATCH", duration, () -> paddle.setCatchActive(false)); }

    // [CHANGED] Áp dụng hiệu ứng cho TẤT CẢ bóng
    public void applySetBallSpeedMultiplier(double multiplier, double duration) {
        for(Ball b : balls) b.setSpeedMultiplier(multiplier);
        registerTimedEffect("SPEED_MULTIPLIER", duration, () -> { for(Ball b : balls) b.setSpeedMultiplier(1.0); });
    }
    public void applyFireball(double duration) {
        for(Ball b : balls) b.activateFireball(true);
        registerTimedEffect("FIREBALL", duration, () -> { for(Ball b : balls) b.activateFireball(false); });
    }

    public void applyBarrier(double duration) { setBarrierActive(true); registerTimedEffect("BARRIER", duration, () -> setBarrierActive(false)); }

    public void fireTorpedo() {
        if (!paddle.areLasersActive()) return;
        if (!torpedoes.isEmpty()) return;
        double cx = paddle.getX() + paddle.getWidth() / 2.0;
        double topY = paddle.getY() - 1.0;
        com.hung.arkanoid.model.entities.Torpedo t = new com.hung.arkanoid.model.entities.Torpedo(cx, topY, -400.0);
        torpedoes.add(t);
        try { soundManager.playLaser(); } catch (Exception ignored) {}
    }

    public void addLife() { lives++; }
    public void activateBarrier(boolean active) { setBarrierActive(active); }

    // [CHANGED] Thực hiện logic spawn ball thật sự
    public void spawnExtraBalls(int count) {
        if (balls.isEmpty()) return;
        Ball ref = balls.get(0);
        for (int i = 0; i < count; i++) {
            // Copy thuộc tính từ bóng gốc
            Ball b = new Ball(ref.getX(), ref.getY(), ref.getWidth(), ref.getSpeed(), ref.getDamage(), ref.getVelocityX(), ref.getVelocityY());
            b.setAttachedToPaddle(false);
            b.setSpeedMultiplier(ref.isFireball() ? 1.0 : 1.0); // Copy trạng thái nếu cần, ở đây giữ đơn giản
            if (ref.isFireball()) b.activateFireball(true);

            // Tách góc di chuyển: xoay vector vận tốc +/- 30 độ
            double theta = Math.toRadians(30 * (i % 2 == 0 ? -1 : 1));
            double vx = ref.getVelocityX();
            double vy = ref.getVelocityY();
            // Nếu bóng gốc đang dính, cho bóng mới bay lên
            if (vx == 0 && vy == 0) {
                vx = Ball.BASE_SPEED * 0.5 * (i % 2 == 0 ? -1 : 1);
                vy = -Ball.BASE_SPEED * 0.866;
            } else {
                // Xoay vector
                double newVx = vx * Math.cos(theta) - vy * Math.sin(theta);
                double newVy = vx * Math.sin(theta) + vy * Math.cos(theta);
                vx = newVx; vy = newVy;
            }
            b.setVelocityX(vx);
            b.setVelocityY(vy);
            balls.add(b);
        }
    }

    public void addBrick(Brick brick) { if (brick != null) bricks.add(brick); }
    public void setBarrierActive(boolean active) { this.barrierActive = active; }
    public boolean isBarrierActive() { return this.barrierActive; }

    // [CHANGED] Update logic loop cho nhiều bóng
    public void update(double deltaSeconds) {
        if (currentState != GameState.PLAYING) return;
        if (deltaSeconds <= 0) deltaSeconds = 1.0 / 60.0;
        if (deltaSeconds > 0.5) deltaSeconds = 0.5;

        // 1. Update Paddle
        if (isMouseControlled) {
            paddle.setX(paddleTargetX - paddle.getWidth() / 2.0);
        } else {
            paddle.update(deltaSeconds);
        }
        checkPaddleBounds();

        // 2. Update Balls
        List<Ball> ballsToRemove = new ArrayList<>();
        // Copy list để tránh ConcurrentModificationException khi đang duyệt
        for (Ball b : new ArrayList<>(balls)) {
            updateSingleBall(b, deltaSeconds, ballsToRemove);
        }
        balls.removeAll(ballsToRemove);

        // Kiểm tra mất mạng (hết bóng)
        if (balls.isEmpty()) {
            lives--;
            if (lives <= 0) {
                try { soundManager.playGameOver(); } catch (Exception ignored) {}
                setState(GameState.GAME_OVER);
            } else {
                resetAfterLifeLost();
            }
        }

        // 3. Update Bricks, PowerUps, Effects, Torpedoes
        for (Brick br : bricks) br.update(deltaSeconds);
        updatePowerUps(deltaSeconds);
        updateEffects();

        Iterator<com.hung.arkanoid.model.entities.Torpedo> tIt = torpedoes.iterator();
        while (tIt.hasNext()) {
            com.hung.arkanoid.model.entities.Torpedo t = tIt.next();
            t.update(deltaSeconds);
            if (t.getY() + t.getHeight() < 0) tIt.remove();
        }
        handleTorpedoCollisions();
        updateActiveEffects(deltaSeconds);

        // 4. Check Level Cleared
        boolean cleared = bricks.stream().allMatch(Brick::isUnbreakable);
        if (cleared) {
            setState(GameState.LEVEL_CLEARED);
            SaveData.saveMaxLevelUnlocked(currentLevel + 1);
        }
    }

    // [ADDED] Logic update riêng cho từng quả bóng
    private void updateSingleBall(Ball ball, double deltaSeconds, List<Ball> ballsToRemove) {
        double ballPrevCX = 0, ballPrevCY = 0;
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

            // Check Walls (trả về true nếu bóng rơi)
            if (checkWallCollisions(ball)) {
                ballsToRemove.add(ball);
                return;
            }
            checkPaddleCollisions(ball, ballPrevCX, ballPrevCY, ballNewCX, ballNewCY);
            checkBrickCollisions(ball, ballPrevCX, ballPrevCY, ballNewCX, ballNewCY);
        } else {
            // Bóng dính vẫn check wall (để không bị kéo ra ngoài) và paddle
            checkWallCollisions(ball);
            checkPaddleCollisions(ball, 0,0,0,0);
        }
    }

    private void updatePowerUps(double deltaSeconds) {
        Iterator<PowerUp> iterator = powerUps.iterator();
        while (iterator.hasNext()) {
            PowerUp pu = iterator.next();
            pu.update(deltaSeconds);
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

    // [CHANGED] Check brick collision cho 1 ball cụ thể
    private void checkBrickCollisions(Ball ball, double x0, double y0, double x1, double y1) {
        if (ball == null) return;
        double radius = ball.getWidth() * 0.5;
        // Vị trí quét hiện tại (fx0, fy0) đến đích (fx1, fy1)
        double fx0 = x0, fy0 = y0, fx1 = x1, fy1 = y1;

        int loopCount = 0;
        final int MAX_LOOPS = 10;
        // Danh sách gạch đã xử lý trong frame này để tránh va chạm lặp lại (tránh kẹt)
        List<Brick> processedBricksInFrame = new ArrayList<>();

        while (loopCount++ < MAX_LOOPS) {
            Brick nearestBrick = null;
            SweepResult nearest = null;

            // 1. Tìm gạch va chạm gần nhất
            for (Brick b : bricks) {
                // Bỏ qua gạch đã vỡ
                if (b.isDestroyed()) continue;
                // LƯU Ý: Không bỏ qua Unbreakable khi có Fireball ở đây, để bóng có thể va chạm và nảy ra

                SweepResult res = computeSweepAgainstRect(fx0, fy0, fx1, fy1, radius, b.getX(), b.getY(), b.getWidth(), b.getHeight());
                if (res != null) {
                    if (nearest == null || res.t < nearest.t) {
                        nearest = res;
                        nearestBrick = b;
                    }
                }
            }

            // Không còn va chạm nào trên đường đi -> thoát vòng lặp
            if (nearest == null) break;

            // 2. Tìm tất cả gạch va chạm đồng thời (ví dụ: trúng góc giữa 2 viên)
            final double TOL = 1e-6;
            List<Brick> hitBricks = new ArrayList<>();
            List<SweepResult> hitResults = new ArrayList<>();

            for (Brick b2 : bricks) {
                if (b2.isDestroyed()) continue;
                SweepResult r2 = computeSweepAgainstRect(fx0, fy0, fx1, fy1, radius, b2.getX(), b2.getY(), b2.getWidth(), b2.getHeight());
                // Nếu cùng thời gian va chạm (t) với nearest
                if (r2 != null && Math.abs(r2.t - nearest.t) <= TOL) {
                    hitBricks.add(b2);
                    hitResults.add(r2);
                }
            }

            boolean combinedInvX = false;
            boolean combinedInvY = false;
            boolean anyNewHitProcessed = false;
            boolean shouldBounce = true; // Mặc định là nảy

            // 3. Xử lý tác động lên từng viên gạch bị trúng
            for (int i = 0; i < hitBricks.size(); i++) {
                Brick hb = hitBricks.get(i);

                // Nếu viên này đã xử lý trong frame này rồi thì bỏ qua (tránh loop vô hạn khi kẹt)
                if (processedBricksInFrame.contains(hb)) continue;
                processedBricksInFrame.add(hb);
                anyNewHitProcessed = true;

                SweepResult hr = hitResults.get(i);
                if (hr.inverseVx) combinedInvX = true;
                if (hr.inverseVy) combinedInvY = true;

                // Gạch nhận sát thương
                boolean wasDestroyedBefore = hb.isDestroyed();
                try { hb.onImpact(this, ball); } catch (Exception ignored) {}
                hb.takeHit(this, ball);

                // Âm thanh
                try {
                    if (hb.isUnbreakable() || (!wasDestroyedBefore && !hb.isDestroyed())) {
                        soundManager.playHardBrickHit();
                    } else {
                        soundManager.playBlockHit();
                    }
                } catch (Exception ignored) {}

                // Spawn PowerUp & Score
                if (hb.isDestroyed()) {
                    score += hb.getScoreValue();
                    com.hung.arkanoid.model.entities.powerup.PowerUpType pt = hb.getPowerUpToSpawn();
                    if (pt != null) spawnPowerUp(hb, pt);

                    if (hb.getType() == com.hung.arkanoid.model.entities.brick.BrickType.EXPLOSIVE) {
                        try { soundManager.playExplosion(); } catch (Exception ignored) {}
                    }

                    // Nếu là Fireball và gạch bị phá hủy (không phải Unbreakable), bóng sẽ xuyên qua -> KHÔNG nảy
                    if (ball.isFireball() && !hb.isUnbreakable()) {
                        shouldBounce = false;
                    }
                }
            }

            // Nếu không xử lý được va chạm mới nào (do kẹt), break để tránh treo game
            if (!anyNewHitProcessed) break;

            // 4. Phản xạ vận tốc & Cập nhật vị trí
            if (shouldBounce) {
                // Chỉ đảo chiều nếu cần nảy
                if (combinedInvX) ball.setVelocityX(-ball.getVelocityX());
                if (combinedInvY) ball.setVelocityY(-ball.getVelocityY());

                // Cập nhật điểm xuất phát mới là vị trí va chạm đã sửa (bật ra)
                fx0 = nearest.hitX;
                fy0 = nearest.hitY;
                fx1 = nearest.correctedX;
                fy1 = nearest.correctedY;
            } else {
                // Fireball xuyên táo (Drill through):
                // Giữ nguyên vận tốc.
                // Cập nhật điểm xuất phát mới là vị trí va chạm nhưng tiếp tục đi thẳng tới đích cũ
                // Dịch nhẹ một chút để không va chạm lại chính viên gạch vừa phá (dù nó đã isDestroyed=true sẽ bị filter ở vòng sau)
                double advance = 0.01;
                fx0 = nearest.hitX + (x1 - x0) * 0.001;
                fy0 = nearest.hitY + (y1 - y0) * 0.001;
                // fx1, fy1 giữ nguyên đích đến cũ để bóng bay xuyên qua
            }

            // Nếu khoảng cách còn lại quá nhỏ, dừng lại
            if (Math.hypot(fx1 - fx0, fy1 - fy0) < 1e-6) break;
        }

        // Cập nhật vị trí cuối cùng cho bóng (chuyển từ tâm về góc trái trên)
        ball.setX(fx1 - ball.getWidth() * 0.5);
        ball.setY(fy1 - ball.getHeight() * 0.5);

        // Dọn dẹp gạch vỡ khỏi list
        bricks.removeIf(Brick::isDestroyed);
    }

    // [CHANGED] Check wall collisions cho 1 ball cụ thể, trả về true nếu bóng rơi xuống đáy
    // [CHANGED] Sửa lỗi bóng dính tường bằng cách kiểm tra hướng vận tốc
    private boolean checkWallCollisions(Ball ball) {
        if (ball == null) return false;
        boolean dropped = false;

        // --- XỬ LÝ TƯỜNG TRÁI ---
        if (ball.getX() <= 0) {
            ball.setX(0); // Đẩy ra khỏi tường
            // Chỉ đảo chiều nếu bóng đang bay VỀ PHÍA TRÁI (vx < 0)
            if (ball.getVelocityX() < 0) {
                ball.reverseDx();
            }
        }
        // --- XỬ LÝ TƯỜNG PHẢI ---
        else if (ball.getX() + ball.getWidth() >= SCREEN_WIDTH) {
            ball.setX(SCREEN_WIDTH - ball.getWidth()); // Đẩy ra khỏi tường
            // Chỉ đảo chiều nếu bóng đang bay VỀ PHÍA PHẢI (vx > 0)
            if (ball.getVelocityX() > 0) {
                ball.reverseDx();
            }
        }

        // --- XỬ LÝ TƯỜNG TRÊN (TRẦN) ---
        if (ball.getY() <= 0) {
            ball.setY(0); // Đẩy xuống khỏi trần
            // Chỉ đảo chiều nếu bóng đang bay LÊN TRÊN (vy < 0)
            if (ball.getVelocityY() < 0) {
                ball.reverseDy();
            }
        }

        // --- XỬ LÝ ĐÁY MÀN HÌNH ---
        if (ball.getY() + ball.getHeight() >= SCREEN_HEIGHT) {
            if (barrierActive) {
                // Nếu có Barrier, nảy lên
                ball.setY(SCREEN_HEIGHT - ball.getHeight());
                // Chỉ nảy nếu bóng đang rơi xuống (vy > 0)
                if (ball.getVelocityY() > 0) {
                    ball.reverseDy();
                }
                setBarrierActive(false);
            } else {
                dropped = true; // Rơi xuống hố
            }
        }
        return dropped;
    }

    // [CHANGED] Check paddle cho 1 ball cụ thể
    private void checkPaddleCollisions(Ball ball, double x0, double y0, double x1, double y1) {
        if (ball == null || paddle == null) return;
        if (x1 == 0 && y1 == 0 && x0 == 0 && y0 == 0) {
            if (!ball.intersects(paddle)) return;
            try { soundManager.playPaddleHit(); } catch (Exception ignored) {}
            // Simple bounce fallback
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

        // Swept collision
        double radius = ball.getWidth() * 0.5;
        SweepResult res = computeSweepAgainstRect(x0, y0, x1, y1, radius, paddle.getX(), paddle.getY(), paddle.getWidth(), paddle.getHeight());
        if (res == null) return;

        try { soundManager.playPaddleHit(); } catch (Exception ignored) {}

        if (paddle.isCatchActive()) {
            ball.setAttachedToPaddle(true);
            ball.setX(paddle.getX() + paddle.getWidth() / 2 - ball.getWidth() / 2);
            ball.setY(paddle.getY() - ball.getHeight() - 1);
            return;
        }

        double rel = (res.hitX - (paddle.getX() + paddle.getWidth() / 2.0)) / (paddle.getWidth() / 2.0);
        rel = Math.max(-1, Math.min(1, rel));
        double maxAngle = Math.toRadians(75);
        double angle = rel * maxAngle;
        double speed = Math.hypot(ball.getVelocityX(), ball.getVelocityY());
        ball.setVelocityX(speed * Math.sin(angle));
        ball.setVelocityY(-Math.abs(speed * Math.cos(angle)));
        ball.setX(res.correctedX - ball.getWidth() * 0.5);
        ball.setY(res.correctedY - ball.getHeight() * 0.5);
    }

    private void checkPaddleBounds() {
        if (paddle == null) return;
        if (paddle.getX() < 0) paddle.setX(0);
        if (paddle.getX() + paddle.getWidth() > SCREEN_WIDTH) paddle.setX(SCREEN_WIDTH - paddle.getWidth());
    }

    private static final class SweepResult {
        double t; double hitX; double hitY; boolean inverseVx; boolean inverseVy; double correctedX; double correctedY;
    }

    // Hàm tính toán Sweep không đổi, giữ nguyên logic cũ
    private SweepResult computeSweepAgainstRect(double x0, double y0, double x1, double y1, double r, double rx, double ry, double rw, double rh) {
        final double EPS = 1e-9;
        double minXr = rx - r, minYr = ry - r, maxXr = rx + rw + r, maxYr = ry + rh + r;
        double dx = x1 - x0, dy = y1 - y0;
        double bestT = Double.POSITIVE_INFINITY;
        double hitX=0, hitY=0; boolean invX=false, invY=false;

        // Kiểm tra cạnh ngang (Trên/Dưới) - CÓ KIỂM TRA HƯỚNG
        if (Math.abs(dy) > EPS) {
            // Chỉ va chạm cạnh TRÊN nếu bóng đang đi XUỐNG (dy > 0)
            if (dy > 0) {
                double tTop = (minYr - y0) / dy; double xAtTop = x0 + tTop * dx;
                if (tTop >= -EPS && tTop <= 1.0 + EPS && xAtTop >= minXr - EPS && xAtTop <= maxXr + EPS) {
                    if (tTop < bestT) { bestT = tTop; hitX = xAtTop; hitY = minYr; invX=false; invY=true; }
                }
            }
            // Chỉ va chạm cạnh DƯỚI nếu bóng đang đi LÊN (dy < 0)
            else {
                double tBot = (maxYr - y0) / dy; double xAtBot = x0 + tBot * dx;
                if (tBot >= -EPS && tBot <= 1.0 + EPS && xAtBot >= minXr - EPS && xAtBot <= maxXr + EPS) {
                    if (tBot < bestT) { bestT = tBot; hitX = xAtBot; hitY = maxYr; invX=false; invY=true; }
                }
            }
        }

        // Kiểm tra cạnh dọc (Trái/Phải) - CÓ KIỂM TRA HƯỚNG
        if (Math.abs(dx) > EPS) {
            // Chỉ va chạm cạnh TRÁI nếu bóng đang đi sang PHẢI (dx > 0)
            if (dx > 0) {
                double tLeft = (minXr - x0) / dx; double yAtLeft = y0 + tLeft * dy;
                if (tLeft >= -EPS && tLeft <= 1.0 + EPS && yAtLeft >= minYr - EPS && yAtLeft <= maxYr + EPS) {
                    if (tLeft < bestT) { bestT = tLeft; hitX = minXr; hitY = yAtLeft; invX=true; invY=false; }
                    else if (Math.abs(tLeft - bestT) <= EPS) { invX=true; invY=true; } // Va chạm góc
                }
            }
            // Chỉ va chạm cạnh PHẢI nếu bóng đang đi sang TRÁI (dx < 0)
            else {
                double tRight = (maxXr - x0) / dx; double yAtRight = y0 + tRight * dy;
                if (tRight >= -EPS && tRight <= 1.0 + EPS && yAtRight >= minYr - EPS && yAtRight <= maxYr + EPS) {
                    if (tRight < bestT) { bestT = tRight; hitX = maxXr; hitY = yAtRight; invX=true; invY=false; }
                    else if (Math.abs(tRight - bestT) <= EPS) { invX=true; invY=true; }
                }
            }
        }

        // Xử lý trường hợp bóng đã nằm lọt bên trong (Overlap) - Giữ nguyên để đẩy bóng ra
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
        // [CHANGED] Reset list bóng về 1 quả
        resetBall();
        powerUps.clear();
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