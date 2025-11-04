package com.hung.arkanoid.view;

import com.hung.arkanoid.game.GameManager;
import com.hung.arkanoid.model.entities.Ball;
import com.hung.arkanoid.model.entities.Paddle;
import com.hung.arkanoid.model.entities.powerup.PowerUp;
import com.hung.arkanoid.model.entities.powerup.PowerUpType;
import com.hung.arkanoid.model.entities.brick.Brick;
import com.hung.arkanoid.model.entities.brick.BrickType;
import com.hung.arkanoid.view.effects.Effect;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.text.Font;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameView {
    private final Map<String, Image> imageCache = new HashMap<>();
    private final Map<String, Image> brickImageCache = new HashMap<>();

    private Image powerUpSpriteSheet;
    private List<Image> powerUpFrames = new ArrayList<>(); // sliced frames (20)
    private final java.util.Map<com.hung.arkanoid.model.entities.powerup.PowerUpType, java.util.List<Image>> powerUpFramesByType = new java.util.EnumMap<>(com.hung.arkanoid.model.entities.powerup.PowerUpType.class);

    private Image backgroundSprite;
    private Image borderVerticalImg;
    private Image pipeImg;

    private Image ballImg;
    private Image ballShadowImg;
    private Image paddleStdImg, paddleWideImg, paddleGunImg, paddleMiniImg;
    private Image torpedoImg;

    private Image blockShadowImg;

    private Image goldBlockImg, grayBlockImg, whiteBlockImg, orangeBlockImg, cyanBlockImg, limeBlockImg, redBlockImg, blueBlockImg, magentaBlockImg, yellowBlockImg;

    private Image explosionMapImg;
    // UI border images
    private Image borderPartVerticalImg;
    private Image ulCornerImg;
    private Image urCornerImg;
    private Image topDoorImg;
    private Image openDoorMapImg;

    // Patterns
    private ImagePattern bkgPatternFill1, bkgPatternFill2, bkgPatternFill3, bkgPatternFill4, borderPatternFill, pipePatternFill;

    // Font used for score and UI text
    private final javafx.scene.text.Font scoreFont = Fonts.emulogic(18);

    // layout constants (match Main.java UI sizes and scale play area to world size)
    private static final double TOP_UI_BAR_HEIGHT = 85.0; // as in Main.java
    private static final double BOTTOM_UI_BAR_HEIGHT = 40.0;
    private static final double SIDE_BORDER_WIDTH = 22.0;
    private static final double PLAY_AREA_X = SIDE_BORDER_WIDTH;
    private static final double PLAY_AREA_Y = TOP_UI_BAR_HEIGHT;
    // View (canvas) size is 800x600 created in Main.startGame; play area is inset inside it
    private static final double VIEW_WIDTH = 800.0;
    private static final double VIEW_HEIGHT = 600.0;
    private static final double PLAY_AREA_WIDTH = VIEW_WIDTH - (2 * SIDE_BORDER_WIDTH);
    private static final double PLAY_AREA_HEIGHT = VIEW_HEIGHT - TOP_UI_BAR_HEIGHT - BOTTOM_UI_BAR_HEIGHT;
    // scale from game world (GameManager.SCREEN_WIDTH/HEIGHT = 560x740) to view play area
    private static final double SCALE_X = PLAY_AREA_WIDTH / com.hung.arkanoid.game.GameManager.SCREEN_WIDTH;
    private static final double SCALE_Y = PLAY_AREA_HEIGHT / com.hung.arkanoid.game.GameManager.SCREEN_HEIGHT;

    // Door animation state
    private int openDoorFrame = 0;
    private int openDoorFrameCounter = 0;
    private double nextLevelDoorAlpha = 1.0;

    public GameView() {
        loadNewResources();
    }

    private Image loadImage(String path) {
        if (path == null) throw new IllegalArgumentException("path must not be null");
        // extract basename (filename without extension)
        String name = path;
        int lastSlash = name.lastIndexOf('/');
        if (lastSlash >= 0) name = name.substring(lastSlash + 1);
        if (name.startsWith("/")) name = name.substring(1);
        int dot = name.lastIndexOf('.');
        if (dot > 0) name = name.substring(0, dot);
        // Delegate to SpriteManager (strict) which will throw if missing
        Image img = SpriteManager.loadResourceVariants(name);
        if (img == null) { // defensive check; loadResourceVariants should throw on missing
            throw new IllegalStateException("Missing image: " + name);
        }
        return img;
    }

    private void loadNewResources() {
        // Try loading required assets strictly from resources (/images)
        try {
            // Load all bonus map variants and slice them into 5x4 frames, map them to PowerUpType
            java.util.Map<com.hung.arkanoid.model.entities.powerup.PowerUpType, String> mapping = new java.util.EnumMap<>(com.hung.arkanoid.model.entities.powerup.PowerUpType.class);
            mapping.put(com.hung.arkanoid.model.entities.powerup.PowerUpType.CATCH, "block_map_bonus_c");
            mapping.put(com.hung.arkanoid.model.entities.powerup.PowerUpType.MULTI_BALL, "block_map_bonus_d");
            mapping.put(com.hung.arkanoid.model.entities.powerup.PowerUpType.EXPAND, "block_map_bonus_f");
            mapping.put(com.hung.arkanoid.model.entities.powerup.PowerUpType.LASER, "block_map_bonus_l");
            mapping.put(com.hung.arkanoid.model.entities.powerup.PowerUpType.SLOW_BALL, "block_map_bonus_s");
            mapping.put(com.hung.arkanoid.model.entities.powerup.PowerUpType.EXTRA_LIFE, "block_map_bonus_p");
            mapping.put(com.hung.arkanoid.model.entities.powerup.PowerUpType.FAST_BALL, "block_map_bonus_b");
            mapping.put(com.hung.arkanoid.model.entities.powerup.PowerUpType.FIRE_BALL, "block_map_bonus_b");
            mapping.put(com.hung.arkanoid.model.entities.powerup.PowerUpType.SHRINK, "block_map_bonus_f");
            mapping.put(com.hung.arkanoid.model.entities.powerup.PowerUpType.BARRIER, "block_map_bonus_b");

            for (var entry : mapping.entrySet()) {
                String baseName = entry.getValue();
                Image sheet = loadImage("/images/" + baseName + ".png");
                java.util.List<Image> frames = SpriteManager.sliceFrames(sheet, 5, 4);
                powerUpFramesByType.put(entry.getKey(), frames);
            }
            // Populate a generic default frames list (use CATCH mapping guaranteed above)
            powerUpFrames = powerUpFramesByType.get(com.hung.arkanoid.model.entities.powerup.PowerUpType.CATCH);

            // load paddle/power/block images (strict)
            ballImg = loadImage("/images/ball.png");
            ballShadowImg = loadImage("/images/ball_shadow.png");
            paddleStdImg = loadImage("/images/paddle_std.png");
            paddleWideImg = loadImage("/images/paddle_wide.png");
            paddleGunImg = loadImage("/images/paddle_gun.png");
            paddleMiniImg = loadImage("/images/paddle_std.png");
            torpedoImg = loadImage("/images/torpedo.png");

            borderVerticalImg = loadImage("/images/borderVertical.png");
            pipeImg = loadImage("/images/pipe.png");
            // Load four background pattern variants
            Image bg1 = loadImage("/images/backgroundPattern_1.png");
            Image bg2 = loadImage("/images/backgroundPattern_2.png");
            Image bg3 = loadImage("/images/backgroundPattern_3.png");
            Image bg4 = loadImage("/images/backgroundPattern_4.png");
            // Create ImagePatterns for tiled backgrounds (sizes chosen to match original assets)
            bkgPatternFill1 = new ImagePattern(bg1, 0, 0, 68, 117, false);
            bkgPatternFill2 = new ImagePattern(bg2, 0, 0, 64, 64, false);
            bkgPatternFill3 = new ImagePattern(bg3, 0, 0, 64, 64, false);
            bkgPatternFill4 = new ImagePattern(bg4, 0, 0, 64, 64, false);

            goldBlockImg = loadImage("/images/goldBlock.png");
            grayBlockImg = loadImage("/images/grayBlock.png");
            whiteBlockImg = loadImage("/images/whiteBlock.png");
            orangeBlockImg = loadImage("/images/orangeBlock.png");
            cyanBlockImg = loadImage("/images/cyanBlock.png");
            limeBlockImg = loadImage("/images/limeBlock.png");
            redBlockImg = loadImage("/images/redBlock.png");
            blueBlockImg = loadImage("/images/blueBlock.png");
            magentaBlockImg = loadImage("/images/magentaBlock.png");
            yellowBlockImg = loadImage("/images/yellowBlock.png");

            blockShadowImg = loadImage("/images/block_shadow.png");
            explosionMapImg = loadImage("/images/explosion_map.png");
            // load border/ui extras
            borderPartVerticalImg = loadImage("/images/borderPartVertical.png");
            ulCornerImg = loadImage("/images/upperLeftCorner.png");
            urCornerImg = loadImage("/images/upperRightCorner.png");
            topDoorImg = loadImage("/images/topDoor.png");
            openDoorMapImg = loadImage("/images/open_door_map.png");

            // create patterns for borders and pipes
            if (borderVerticalImg != null) borderPatternFill = new ImagePattern(borderVerticalImg, 0, 0, 20, 113, false);
            if (pipeImg != null) pipePatternFill = new ImagePattern(pipeImg, 0, 0, 5, 17, false);
        } catch (Exception ex) {
            // Fail fast: rethrow to indicate missing required resources
            throw new IllegalStateException("Failed to load required game resources: " + ex.getMessage(), ex);
        }
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
        if (borderPatternFill != null) {
            gc.setFill(borderPatternFill);
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
        ImagePattern[] patterns = new ImagePattern[] { bkgPatternFill1, bkgPatternFill2, bkgPatternFill3, bkgPatternFill4 };
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
            if (blockShadowImg != null) gc.drawImage(blockShadowImg, bx + 4, by + 4, bw, bh);
        }

        // draw powerups (animated frames)
        for (PowerUp pu : gm.getPowerUps()) {
            double px = scaleX(pu.getX());
            double py = scaleY(pu.getY());
            double pw = scaleW(pu.getWidth());
            double ph = scaleH(pu.getHeight());
            Image frame = null;
            // pick frames list for this powerup type
            java.util.List<Image> framesForType = powerUpFramesByType.getOrDefault(pu.getType(), powerUpFrames);
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
                case UNBREAKABLE -> img = goldBlockImg;
                case STRONG -> img = grayBlockImg;
                case EXPLOSIVE -> img = redBlockImg;
                default -> img = blueBlockImg;
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
            Image paddleImg = paddleStdImg;
            if (p.areLasersActive()) paddleImg = paddleGunImg;
            else if (p.isCatchActive()) paddleImg = paddleWideImg;
            if (paddleImg != null) gc.drawImage(paddleImg, px, py, pw, ph);
            else {
                gc.setFill(Color.DARKGRAY);
                gc.fillRoundRect(px, py, pw, ph, 6, 6);
            }
        }

        // draw ball(s)
        for (Ball b : List.of(gm.getBall())) {
            double bx = scaleX(b.getX());
            double by = scaleY(b.getY());
            double bw = scaleW(b.getWidth());
            double bh = scaleH(b.getHeight());
            if (ballShadowImg != null) gc.drawImage(ballShadowImg, bx + 3, by + 3, bw, bh);
            if (ballImg != null) gc.drawImage(ballImg, bx, by, bw, bh);
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
            if (torpedoImg != null) gc.drawImage(torpedoImg, tx, ty, tw, th);
            else { gc.setFill(Color.RED); gc.fillRect(tx, ty, tw, th); }
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
            if (paddleMiniImg != null) gc.drawImage(paddleMiniImg, lx, y, LIFE_ICON_W, LIFE_ICON_H);
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
        if (pipePatternFill != null && topDoorImg != null) {
            gc.setFill(pipePatternFill);
            gc.fillRect(17, 68, 83, 17);
            double midX = 100 + topDoorImg.getWidth();
            gc.fillRect(midX, 68, WIDTH - 200 - 2 * topDoorImg.getWidth(), 17);
            gc.fillRect(WIDTH - 100, 68, 83, 17);
        }

        // Draw vertical borders
        if (borderPatternFill != null) {
            gc.setFill(borderPatternFill);
            gc.fillRect(0, UPPER_INSET, 20, HEIGHT - UPPER_INSET);
            gc.fillRect(WIDTH - 20, UPPER_INSET, 20, HEIGHT - UPPER_INSET);
        }

        // Draw border corners
        if (ulCornerImg != null) gc.drawImage(ulCornerImg, 2.5, 67.5);
        if (urCornerImg != null) gc.drawImage(urCornerImg, WIDTH - (urCornerImg.getWidth()) - 2.5, 67.5);

        // Draw vertical border parts tiled; if level cleared, animate opening on the right side
        boolean doorOpen = false; // use game state to decide; try checking one frame of openDoorMap presence
        // We can't access GameManager here, so approximate: if openDoorMapImg exists, animate once per render cycle
        if (borderPartVerticalImg != null) {
            for (int i = 0; i < 6; i++) {
                gc.drawImage(borderPartVerticalImg, 0, UPPER_INSET + i * 113);
                gc.drawImage(borderPartVerticalImg, WIDTH - 20, UPPER_INSET + i * 113);
            }
            // animate bottom-right part if openDoorMap exists
            if (openDoorMapImg != null) {
                // advance frame every 6 renders
                openDoorFrameCounter = (openDoorFrameCounter + 1) % 6;
                if (openDoorFrameCounter == 0) openDoorFrame = (openDoorFrame + 1) % Math.max(1, (int)(openDoorMapImg.getWidth()/20));
                // fade logic
                if (nextLevelDoorAlpha > 0.01) nextLevelDoorAlpha -= 0.01;
                gc.save();
                gc.setGlobalAlpha(nextLevelDoorAlpha);
                gc.drawImage(borderPartVerticalImg, WIDTH - 20, UPPER_INSET + 565);
                gc.restore();

                // draw open door frame from sprite strip
                gc.drawImage(openDoorMapImg, openDoorFrame * 20, 0, 20, 71, WIDTH - 20, UPPER_INSET + 565, 20, 71);
            }
        }
    }
}
