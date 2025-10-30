package com.hung.arkanoid.view;

import com.hung.arkanoid.game.GameManager;
import com.hung.arkanoid.model.entities.Ball;
import com.hung.arkanoid.model.entities.Paddle;
import com.hung.arkanoid.model.entities.powerup.PowerUp;
import com.hung.arkanoid.model.entities.brick.Brick;
import com.hung.arkanoid.view.effects.Effect;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameView {
    private final UIResources ui;

    private final java.util.Map<com.hung.arkanoid.model.entities.powerup.PowerUpType, java.util.List<Image>> powerUpFramesByType = new java.util.EnumMap<>(com.hung.arkanoid.model.entities.powerup.PowerUpType.class);
    private List<Image> defaultPowerUpFrames = new ArrayList<>();

    // Font used for score and UI text
    private final javafx.scene.text.Font scoreFont = Fonts.emulogic(18);

    private static final double TOP_UI_BAR_HEIGHT = 85.0;
    private static final double BOTTOM_UI_BAR_HEIGHT = 40.0;
    private static final double SIDE_BORDER_WIDTH = 22.0;
    private static final double PLAY_AREA_X = SIDE_BORDER_WIDTH;
    private static final double PLAY_AREA_Y = TOP_UI_BAR_HEIGHT;
    private static final double VIEW_WIDTH = 800.0;
    private static final double VIEW_HEIGHT = 600.0;
    private static final double PLAY_AREA_WIDTH = VIEW_WIDTH - (2 * SIDE_BORDER_WIDTH);
    private static final double PLAY_AREA_HEIGHT = VIEW_HEIGHT - TOP_UI_BAR_HEIGHT - BOTTOM_UI_BAR_HEIGHT;
    private static final double SCALE_X = PLAY_AREA_WIDTH / com.hung.arkanoid.game.GameManager.SCREEN_WIDTH;
    private static final double SCALE_Y = PLAY_AREA_HEIGHT / com.hung.arkanoid.game.GameManager.SCREEN_HEIGHT;

    // Door animation state
    private int openDoorFrame = 0;
    private int openDoorFrameCounter = 0;
    private double nextLevelDoorAlpha = 1.0;

    public GameView() {
        // load shared UI resources once
        this.ui = UIResources.load();

        // prepare power-up sprite frames from the already-loaded bonus block maps
        java.util.Map<com.hung.arkanoid.model.entities.powerup.PowerUpType, Image> sheets = new java.util.EnumMap<>(com.hung.arkanoid.model.entities.powerup.PowerUpType.class);
        sheets.put(com.hung.arkanoid.model.entities.powerup.PowerUpType.CATCH, ui.bonusBlockCMapImg);
        sheets.put(com.hung.arkanoid.model.entities.powerup.PowerUpType.MULTI_BALL, ui.bonusBlockDMapImg);
        sheets.put(com.hung.arkanoid.model.entities.powerup.PowerUpType.EXPAND, ui.bonusBlockFMapImg);
        sheets.put(com.hung.arkanoid.model.entities.powerup.PowerUpType.LASER, ui.bonusBlockLMapImg);
        sheets.put(com.hung.arkanoid.model.entities.powerup.PowerUpType.SLOW_BALL, ui.bonusBlockSMapImg);
        sheets.put(com.hung.arkanoid.model.entities.powerup.PowerUpType.EXTRA_LIFE, ui.bonusBlockPMapImg);
        sheets.put(com.hung.arkanoid.model.entities.powerup.PowerUpType.FAST_BALL, ui.bonusBlockBMapImg);
        sheets.put(com.hung.arkanoid.model.entities.powerup.PowerUpType.FIRE_BALL, ui.bonusBlockBMapImg);
        sheets.put(com.hung.arkanoid.model.entities.powerup.PowerUpType.SHRINK, ui.bonusBlockFMapImg);
        sheets.put(com.hung.arkanoid.model.entities.powerup.PowerUpType.BARRIER, ui.bonusBlockBMapImg);

        for (var entry : sheets.entrySet()) {
            Image sheet = entry.getValue();
            if (sheet == null) continue;
            java.util.List<Image> frames = SpriteManager.sliceFrames(sheet, 5, 4);
            powerUpFramesByType.put(entry.getKey(), frames);
        }
        // default frames: use CATCH set if available
        defaultPowerUpFrames = powerUpFramesByType.getOrDefault(
                com.hung.arkanoid.model.entities.powerup.PowerUpType.CATCH,
                new ArrayList<>()
        );
    }

    // scaling helpers
    private double scaleX(double gameX) { return (gameX * SCALE_X) + PLAY_AREA_X; }
    private double scaleY(double gameY) { return (gameY * SCALE_Y) + PLAY_AREA_Y; }
    private double scaleW(double gameW) { return gameW * SCALE_X; }
    private double scaleH(double gameH) { return gameH * SCALE_Y; }

    public void render(GraphicsContext gc, GameManager gm) {
        // PowerUps have per-entity animation state (PowerUp.getAnimationIndex()); no global update needed here

        // Layer 1: clear screen & draw UI frame/background
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, VIEW_WIDTH, VIEW_HEIGHT);

        // draw left/right borders
        if (ui.borderPatternFill != null) {
            gc.setFill(ui.borderPatternFill);
            gc.fillRect(0, 0, SIDE_BORDER_WIDTH, VIEW_HEIGHT);
            gc.fillRect(VIEW_WIDTH - SIDE_BORDER_WIDTH, 0, SIDE_BORDER_WIDTH, VIEW_HEIGHT);
        } else {
            gc.setFill(Color.DARKSLATEGRAY);
            gc.fillRect(0, 0, SIDE_BORDER_WIDTH, VIEW_HEIGHT);
            gc.fillRect(VIEW_WIDTH - SIDE_BORDER_WIDTH, 0, SIDE_BORDER_WIDTH, VIEW_HEIGHT);
        }

        // Draw decorative UI border pieces (pipes, corners and door animation)
        drawBorder(gc);

        // Top UI bar
        gc.setFill(Color.rgb(20, 20, 20));
        gc.fillRect(SIDE_BORDER_WIDTH, 0, VIEW_WIDTH - 2 * SIDE_BORDER_WIDTH, TOP_UI_BAR_HEIGHT);

        // Bottom UI bar
        gc.setFill(Color.rgb(18, 18, 18));
        gc.fillRect(SIDE_BORDER_WIDTH, VIEW_HEIGHT - BOTTOM_UI_BAR_HEIGHT, VIEW_WIDTH - 2 * SIDE_BORDER_WIDTH, BOTTOM_UI_BAR_HEIGHT);

        // Score / Highscore text
        gc.setFill(Color.WHITE);
        gc.setFont(scoreFont);
        gc.fillText("SCORE", SIDE_BORDER_WIDTH + 10, 10);
        gc.fillText(String.valueOf(gm.getScore()), SIDE_BORDER_WIDTH + 10, 30);

        // Layer 2: draw play area background chosen by current level (cycle through 4 patterns)
        int level = 1;
        try { level = gm.getCurrentLevel(); } catch (Exception ignored) {}
        // Round-robin selection across the 4 patterns: (level-1) % 4
        ImagePattern[] patterns = new ImagePattern[] { ui.bkgPatternFill1, ui.bkgPatternFill2, ui.bkgPatternFill3, ui.bkgPatternFill4 };
        ImagePattern chosen = null;
        if (patterns.length > 0) {
            chosen = patterns[(Math.max(1, level) - 1) % patterns.length];
        }
        // fallback: pick first available pattern if chosen is null
        if (chosen == null) {
            for (ImagePattern p : patterns) { if (p != null) { chosen = p; break; } }
        }
        if (chosen != null) gc.setFill(chosen); else gc.setFill(Color.web("#0b2a66"));
        gc.fillRect(PLAY_AREA_X, PLAY_AREA_Y, PLAY_AREA_WIDTH, PLAY_AREA_HEIGHT);

        // Draw subtle shadows to match reference: left shadow + top strip
        gc.save();
        gc.setGlobalAlpha(0.3);
        gc.setFill(Color.BLACK);
        gc.fillRect(PLAY_AREA_X, PLAY_AREA_Y, 40, PLAY_AREA_HEIGHT);
        gc.fillRect(PLAY_AREA_X, PLAY_AREA_Y, PLAY_AREA_WIDTH, 20);
        gc.restore();

        // Layer 3: draw world scaled
        gc.save();
        // clip to play area
        gc.beginPath();
        gc.rect(PLAY_AREA_X, PLAY_AREA_Y, PLAY_AREA_WIDTH, PLAY_AREA_HEIGHT);
        gc.closePath();
        gc.clip();

        // draw bricks (shadows first)
        for (Brick b : gm.getBricksSafe()) {
            double bx = scaleX(b.getX());
            double by = scaleY(b.getY());
            double bw = scaleW(b.getWidth());
            double bh = scaleH(b.getHeight());
            if (ui.blockShadowImg != null) gc.drawImage(ui.blockShadowImg, bx + 4, by + 4, bw, bh);
        }

        // draw powerups (animated frames)
        for (PowerUp pu : gm.getPowerUps()) {
            double px = scaleX(pu.getX());
            double py = scaleY(pu.getY());
            double pw = scaleW(pu.getWidth());
            double ph = scaleH(pu.getHeight());
            Image frame = null;
            // pick frames list for this powerup type
            java.util.List<Image> framesForType = powerUpFramesByType.getOrDefault(pu.getType(), defaultPowerUpFrames);
            if (framesForType != null && !framesForType.isEmpty()) {
                int idx = pu.getAnimationIndex(); if (idx < 0) idx = 0;
                frame = framesForType.get(idx % framesForType.size());
            }
             if (frame != null) gc.drawImage(frame, px, py, pw, ph);
             else {
                // fallback capsule
                gc.setFill(Color.LIME);
                gc.fillRoundRect(px, py, pw, ph, 6, 6);
                gc.setFill(Color.BLACK);
                gc.fillText(pu.getType().name().substring(0,1), px + pw/2 - 4, py + ph/2 - 6);
            }
        }

        // draw bricks (actual)
        for (Brick b : gm.getBricksSafe()) {
            double bx = scaleX(b.getX());
            double by = scaleY(b.getY());
            double bw = scaleW(b.getWidth());
            double bh = scaleH(b.getHeight());
            Image img = null;
            switch (b.getType()) {
                case UNBREAKABLE -> img = ui.yellowBlockImg; // gold removed; render unbreakable as yellow
                case STRONG -> img = ui.grayBlockImg;
                case EXPLOSIVE -> img = ui.redBlockImg;
                default -> img = ui.blueBlockImg;
            }
            if (img != null) gc.drawImage(img, bx, by, bw, bh);
            else {
                gc.setFill(Color.MAGENTA);
                gc.fillRect(bx, by, bw, bh);
            }
        }

        // draw effects (explosions)
        for (Effect e : gm.getEffects()) {
            // effects are in game coordinates; render them directly (no scaling) by mapping center
            // ExplosionEffect renders using absolute pixel positions; adapt by converting game->screen
            // We'll assume ExplosionEffect uses game coords; so shift/scale by play area
            // For safety, call render on a translated gc
            gc.save();
            e.render(gc);
            gc.restore();
        }

        // draw paddle
        Paddle p = gm.getPaddle();
        if (p != null) {
            double px = scaleX(p.getX());
            double py = scaleY(p.getY());
            double pw = scaleW(p.getWidth());
            double ph = scaleH(p.getHeight());

            // Xác định bộ frame cần dùng
            List<Image> currentFrames;
            if (p.areLasersActive()) {
                // Ưu tiên 1: Laser
                currentFrames = paddleGunFrames;
            } else if (p.getWidth() > Paddle.BASE_WIDTH + 1) {
                // Ưu tiên 2: Đang mở rộng (Expanded) - kiểm tra chiều rộng thay vì cờ Catch
                currentFrames = paddleWideFrames;
            } else {
                // Mặc định
                currentFrames = paddleStdFrames;
            }

            // Lấy frame animation hiện tại
            Image frameToDraw = null;
            if (currentFrames != null && !currentFrames.isEmpty()) {
                int animIndex = p.getAnimationIndex();
                frameToDraw = currentFrames.get(animIndex % currentFrames.size());
            }

            // Vẽ paddle
            if (frameToDraw != null) {
                gc.drawImage(frameToDraw, px, py, pw, ph);
            } else {
                // Fallback nếu không có ảnh
                gc.setFill(p.areLasersActive() ? Color.RED : Color.CYAN);
                gc.fillRoundRect(px, py, pw, ph, 10, 10);
            }
        }

        // draw ball(s)
        for (Ball b : List.of(gm.getBall())) {
            double bx = scaleX(b.getX());
            double by = scaleY(b.getY());
            double bw = scaleW(b.getWidth());
            double bh = scaleH(b.getHeight());
            if (ui.ballShadowImg != null) gc.drawImage(ui.ballShadowImg, bx + 3, by + 3, bw, bh);
            if (ui.ballImg != null) gc.drawImage(ui.ballImg, bx, by, bw, bh);
            else {
                gc.setFill(Color.AQUA);
                gc.fillOval(bx, by, bw, bh);
            }
        }

        // draw torpedoes
        for (com.hung.arkanoid.model.entities.Torpedo t : gm.getTorpedoes()) {
            double tx = scaleX(t.getX());
            double ty = scaleY(t.getY());
            double tw = scaleW(t.getWidth());
            double th = scaleH(t.getHeight());
            if (ui.torpedoImg != null) gc.drawImage(ui.torpedoImg, tx, ty, tw, th);
            else { gc.setFill(Color.RED); gc.fillRect(tx, ty, tw, th); }
        }

        // draw barrier (safety line) if active
        if (gm.isBarrierActive()) {
            double barrierYGame = GameManager.BARRIER_Y;
            double barrierY = scaleY(barrierYGame);
            double x = PLAY_AREA_X;
            double w = PLAY_AREA_WIDTH;
            double h = 6.0; // 6px thick in view space
            gc.save();
            gc.setGlobalAlpha(0.8);
            if (ui.borderPatternFill != null) {
                gc.setFill(ui.borderPatternFill);
            } else {
                gc.setFill(Color.CYAN);
            }
            gc.fillRect(x, barrierY, w, h);
            // glow outline
            gc.setGlobalAlpha(1.0);
            gc.setStroke(Color.AQUA);
            gc.setLineWidth(2.0);
            gc.strokeRect(x, barrierY, w, h);
            gc.restore();
        }

        gc.restore(); // restore after clipping

        // Layer 4: UI overlay (lives)
        int lives = gm.getLives();
        // Match Main.java: INSET + 2 and HEIGHT - 30
        double startX = SIDE_BORDER_WIDTH + 2;
        double y = VIEW_HEIGHT - 30.0;
        final double LIFE_ICON_W = 36.0;
        final double LIFE_ICON_H = 16.0;
        for (int i = 0; i < lives; i++) {
            double lx = startX + i * 42;
            if (ui.paddleMiniImg != null) gc.drawImage(ui.paddleMiniImg, lx, y, LIFE_ICON_W, LIFE_ICON_H);
            else {
                gc.setFill(Color.WHITE);
                gc.fillRect(startX + i * 42, y, 30, 10);
            }
        }

        // State overlay: only render for GAME_OVER and LEVEL_CLEARED — PAUSED is handled by GameController overlay
        if (gm.getCurrentState() == GameManager.GameState.GAME_OVER || gm.getCurrentState() == GameManager.GameState.LEVEL_CLEARED) {
            gc.setFill(new Color(0,0,0,0.7));
            gc.fillRect(0,0,VIEW_WIDTH,VIEW_HEIGHT);
            gc.setFill(Color.WHITE);
            gc.setFont(scoreFont);
            String text = gm.getCurrentState() == GameManager.GameState.GAME_OVER ? "GAME OVER" : "LEVEL CLEARED";
            gc.fillText(text, VIEW_WIDTH/2.0 - 60, VIEW_HEIGHT/2.0);
        }
    }

    // Draw UI borders (pipes, vertical tiled parts, corners and optional opening door animation)
    private void drawBorder(GraphicsContext gc) {
        final double WIDTH = VIEW_WIDTH;
        final double HEIGHT = VIEW_HEIGHT;
        final double UPPER_INSET = 68.0; // visual offset used by original layout

        // Draw top pipes
        if (ui.pipePatternFill != null && ui.topDoorImg != null) {
            gc.setFill(ui.pipePatternFill);
            gc.fillRect(17, 68, 83, 17);
            double midX = 100 + ui.topDoorImg.getWidth();
            gc.fillRect(midX, 68, WIDTH - 200 - 2 * ui.topDoorImg.getWidth(), 17);
            gc.fillRect(WIDTH - 100, 68, 83, 17);
        }

        // Draw vertical borders
        if (ui.borderPatternFill != null) {
            gc.setFill(ui.borderPatternFill);
            gc.fillRect(0, UPPER_INSET, 20, HEIGHT - UPPER_INSET);
            gc.fillRect(WIDTH - 20, UPPER_INSET, 20, HEIGHT - UPPER_INSET);
        }

        // Draw border corners
        if (ui.ulCornerImg != null) gc.drawImage(ui.ulCornerImg, 2.5, 67.5);
        if (ui.urCornerImg != null) gc.drawImage(ui.urCornerImg, WIDTH - (ui.urCornerImg.getWidth()) - 2.5, 67.5);

        // Draw vertical border parts tiled; if level cleared, animate opening on the right side
        boolean doorOpen = false; // use game state to decide; try checking one frame of openDoorMap presence
        // We can't access GameManager here, so approximate: if openDoorMapImg exists, animate once per render cycle
        if (ui.borderPartVerticalImg != null) {
            for (int i = 0; i < 6; i++) {
                gc.drawImage(ui.borderPartVerticalImg, 0, UPPER_INSET + i * 113);
                gc.drawImage(ui.borderPartVerticalImg, WIDTH - 20, UPPER_INSET + i * 113);
            }
            // animate bottom-right part if openDoorMap exists
            if (ui.openDoorMapImg != null) {
                // advance frame every 6 renders
                openDoorFrameCounter = (openDoorFrameCounter + 1) % 6;
                if (openDoorFrameCounter == 0) openDoorFrame = (openDoorFrame + 1) % Math.max(1, (int)(ui.openDoorMapImg.getWidth()/20));
                // fade logic
                if (nextLevelDoorAlpha > 0.01) nextLevelDoorAlpha -= 0.01;
                gc.save();
                gc.setGlobalAlpha(nextLevelDoorAlpha);
                gc.drawImage(ui.borderPartVerticalImg, WIDTH - 20, UPPER_INSET + 565);
                gc.restore();

                // draw open door frame from sprite strip
                gc.drawImage(ui.openDoorMapImg, openDoorFrame * 20, 0, 20, 71, WIDTH - 20, UPPER_INSET + 565, 20, 71);
            }
        }
    }
}
