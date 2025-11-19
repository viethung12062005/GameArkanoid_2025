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
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.geometry.VPos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Renders the main Arkanoid playfield.
 * <p>
 * This class translates logical game coordinates from {@link GameManager}
 * into on-screen pixels, draws the animated background, border, paddle,
 * balls, bricks, power-ups, HUD text and simple effects.
 */
public class GameView {
    // Image cache is reserved for potential future reuse of images.
    private final Map<String, Image> imageCache = new HashMap<>();

    // Sprite sheet for power-ups and the individual frames sliced from it.
    private Image powerUpSpriteSheet;
    private List<Image> powerUpFrames = new ArrayList<>();
    private final Map<com.hung.arkanoid.model.entities.powerup.PowerUpType, List<Image>> powerUpFramesByType =
        new java.util.EnumMap<>(com.hung.arkanoid.model.entities.powerup.PowerUpType.class);

    // Images
    private Image borderVerticalImg;
    private Image borderPartVerticalImg;
    private Image pipeImg;
    private Image ballImg;
    private Image ballShadowImg;
    private Image paddleStdImg, paddleWideImg, paddleGunImg, paddleMiniImg;
    private Image torpedoImg;
    private Image blockShadowImg;
    private Image goldBlockImg, grayBlockImg, whiteBlockImg, orangeBlockImg, cyanBlockImg, limeBlockImg, redBlockImg, blueBlockImg, magentaBlockImg, yellowBlockImg;
    private Image ulCornerImg, urCornerImg;
    private Image topDoorImg;
    private Image openDoorMapImg;
    private Image bonusBlockShadowImg;
    private Image paddleStdShadowImg, paddleWideShadowImg, paddleGunShadowImg;

    // Patterns
    private ImagePattern bkgPatternFill1, bkgPatternFill2, bkgPatternFill3, bkgPatternFill4;
    private ImagePattern borderPatternFill;
    private ImagePattern pipePatternFill;

    // Font
    private final Font scoreFont = Fonts.emulogic(18);
    private final Font highScoreFont = Fonts.emulogic(20);

    // Layout Constants (Based on Main.java)
    private static final double TOP_UI_BAR_HEIGHT = 85.0;
    private static final double BOTTOM_UI_BAR_HEIGHT = 40.0;
    private static final double SIDE_BORDER_WIDTH = 22.0;

    // View Dimensions
    private static final double VIEW_WIDTH = 800.0;
    private static final double VIEW_HEIGHT = 600.0;

    // Play Area
    private static final double PLAY_AREA_X = SIDE_BORDER_WIDTH;
    private static final double PLAY_AREA_Y = TOP_UI_BAR_HEIGHT;
    private static final double PLAY_AREA_WIDTH = VIEW_WIDTH - (2 * SIDE_BORDER_WIDTH);
    private static final double PLAY_AREA_HEIGHT = VIEW_HEIGHT - TOP_UI_BAR_HEIGHT; // Extend to bottom

    // Scaling factors (GameWorld 560x740 -> View Play Area)
    private static final double SCALE_X = PLAY_AREA_WIDTH / com.hung.arkanoid.game.GameManager.SCREEN_WIDTH;
    private static final double SCALE_Y = PLAY_AREA_HEIGHT / com.hung.arkanoid.game.GameManager.SCREEN_HEIGHT;

    // Door state
    private double nextLevelDoorAlpha = 1.0;
    private boolean nextLevelDoorOpen = false;

    /**
     * Creates a new view instance and loads all required image resources.
     */
    public GameView() {
        loadNewResources();
    }

    /**
     * Loads an image from the classpath using {@link SpriteManager}.
     * The provided path may include a folder and extension; the base
     * file name is extracted and resolved under <code>/images</code>.
     *
     * @param path resource path, e.g. "/images/ball.png"
     * @return the loaded image, never {@code null}
     */
    private Image loadImage(String path) {
        if (path == null) throw new IllegalArgumentException("path must not be null");
        String name = path;
        int lastSlash = name.lastIndexOf('/');
        if (lastSlash >= 0) name = name.substring(lastSlash + 1);
        if (name.startsWith("/")) name = name.substring(1);
        int dot = name.lastIndexOf('.');
        if (dot > 0) name = name.substring(0, dot);

        Image img = SpriteManager.loadResourceVariants(name);
        if (img == null) {
            throw new IllegalStateException("Missing image: " + name);
        }
        return img;
    }

    /**
     * Loads all textures and patterns used by the game view.
     * This mirrors the original resource loading logic from Main.java
     * but relies on the {@link SpriteManager} helper.
     */
    private void loadNewResources() {
        try {
            // Load PowerUps
            Map<com.hung.arkanoid.model.entities.powerup.PowerUpType, String> mapping = new java.util.EnumMap<>(com.hung.arkanoid.model.entities.powerup.PowerUpType.class);
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
                Image sheet = loadImage("/images/" + entry.getValue() + ".png");
                powerUpFramesByType.put(entry.getKey(), SpriteManager.sliceFrames(sheet, 5, 4));
            }
            powerUpFrames = powerUpFramesByType.get(com.hung.arkanoid.model.entities.powerup.PowerUpType.CATCH);

            // Load Game Assets
            ballImg = loadImage("/images/ball.png");
            ballShadowImg = loadImage("/images/ball_shadow.png");
            paddleStdImg = loadImage("/images/paddle_std.png");
            paddleWideImg = loadImage("/images/paddle_wide.png");
            paddleGunImg = loadImage("/images/paddle_gun.png");
            paddleMiniImg = loadImage("/images/paddle_std.png");
            torpedoImg = loadImage("/images/torpedo.png");

            paddleStdShadowImg = loadImage("/images/paddle_std_shadow.png");
            paddleWideShadowImg = loadImage("/images/paddle_wide_shadow.png");
            paddleGunShadowImg = loadImage("/images/paddle_gun_shadow.png");

            // UI & Environment
            borderVerticalImg = loadImage("/images/borderVertical.png");
            borderPartVerticalImg = loadImage("/images/borderPartVertical.png");
            pipeImg = loadImage("/images/pipe.png");
            ulCornerImg = loadImage("/images/upperLeftCorner.png");
            urCornerImg = loadImage("/images/upperRightCorner.png");
            topDoorImg = loadImage("/images/topDoor.png");
            openDoorMapImg = loadImage("/images/open_door_map.png");

            // Backgrounds
            bkgPatternFill1 = new ImagePattern(loadImage("/images/backgroundPattern_1.png"), 0, 0, 68, 117, false);
            bkgPatternFill2 = new ImagePattern(loadImage("/images/backgroundPattern_2.png"), 0, 0, 64, 64, false);
            bkgPatternFill3 = new ImagePattern(loadImage("/images/backgroundPattern_3.png"), 0, 0, 64, 64, false);
            bkgPatternFill4 = new ImagePattern(loadImage("/images/backgroundPattern_4.png"), 0, 0, 64, 64, false);

            // Patterns
            if (borderVerticalImg != null) borderPatternFill = new ImagePattern(borderVerticalImg, 0, 0, 20, 113, false);
            if (pipeImg != null) pipePatternFill = new ImagePattern(pipeImg, 0, 0, 5, 17, false);

            // Blocks
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
            bonusBlockShadowImg = loadImage("/images/bonus_block_shadow.png");

        } catch (Exception ex) {
            throw new IllegalStateException("Failed to load required game resources: " + ex.getMessage(), ex);
        }
    }

    // Scaling helpers from game space (GameManager.SCREEN_WIDTH/HEIGHT)
    // to view coordinates (800x600 canvas).
    private double scaleX(double gameX) { return (gameX * SCALE_X) + PLAY_AREA_X; }
    private double scaleY(double gameY) { return (gameY * SCALE_Y) + PLAY_AREA_Y; }
    private double scaleW(double gameW) { return gameW * SCALE_X; }
    private double scaleH(double gameH) { return gameH * SCALE_Y; }

    /**
     * Renders one frame of the game using the state provided by
     * the {@link GameManager}. This method is called once per
     * animation tick by the JavaFX application.
     *
     * @param gc graphics context of the canvas
     * @param gm game manager providing current entities and score
     */
    public void render(GraphicsContext gc, GameManager gm) {
        // 1. Clear the whole canvas with black.
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, VIEW_WIDTH, VIEW_HEIGHT);

        // 2. Paint the level-dependent background pattern.
        int level = gm.getCurrentLevel();
        ImagePattern currentPattern = switch ((level - 1) % 4) {
            case 0 -> bkgPatternFill1;
            case 1 -> bkgPatternFill2;
            case 2 -> bkgPatternFill3;
            case 3 -> bkgPatternFill4;
            default -> bkgPatternFill1;
        };

        if (currentPattern != null) {
            gc.setFill(currentPattern);
            // Draw pattern starting from below top bar
            gc.fillRect(PLAY_AREA_X, TOP_UI_BAR_HEIGHT, PLAY_AREA_WIDTH, VIEW_HEIGHT - TOP_UI_BAR_HEIGHT);
        }

        // 3. Add subtle shadow strips along the top and left edges
        //    to match the visual style of the reference implementation.
        gc.setFill(Color.rgb(0, 0, 0, 0.3));
        // Vertical shadow on left side of play area
        gc.fillRect(PLAY_AREA_X, TOP_UI_BAR_HEIGHT, 40, VIEW_HEIGHT - TOP_UI_BAR_HEIGHT);
        // Horizontal shadow at top of play area
        gc.fillRect(PLAY_AREA_X, TOP_UI_BAR_HEIGHT, PLAY_AREA_WIDTH, 20);

        // 4. Draw shadows for blocks, power-ups, paddle and balls.
        //    Shadows are drawn in a clipped region so they never overlap the HUD.
        gc.save();
        // Clip to play area to prevent drawing over UI
        gc.beginPath();
        gc.rect(PLAY_AREA_X, PLAY_AREA_Y, PLAY_AREA_WIDTH, PLAY_AREA_HEIGHT);
        gc.closePath();
        gc.clip();

        // Offset for shadows
        double shadowOffX = 10 * SCALE_X;
        double shadowOffY = 10 * SCALE_Y;

        // Block Shadows
        for (Brick b : gm.getBricksSafe()) {
            if (!b.isDestroyed()) {
                gc.drawImage(blockShadowImg, scaleX(b.getX()) + shadowOffX/2, scaleY(b.getY()) + shadowOffY/2, scaleW(b.getWidth()), scaleH(b.getHeight()));
            }
        }
        // PowerUp Shadows
        for (PowerUp pu : gm.getPowerUps()) {
            gc.drawImage(bonusBlockShadowImg, scaleX(pu.getX()) + shadowOffX/2, scaleY(pu.getY()) + shadowOffY/2, scaleW(pu.getWidth()), scaleH(pu.getHeight()));
        }
        // Paddle Shadow
        Paddle p = gm.getPaddle();
        if (gm.getLives() > 0) {
            double px = scaleX(p.getX());
            double py = scaleY(p.getY());
            double pw = scaleW(p.getWidth());
            double ph = scaleH(p.getHeight());
            Image pShadow = paddleStdShadowImg;
            if (p.areLasersActive()) pShadow = paddleGunShadowImg;
            else if (p.isCatchActive()) pShadow = paddleWideShadowImg;
            if (pShadow != null) gc.drawImage(pShadow, px + shadowOffX, py + shadowOffY, pw, ph);
        }
        // Ball Shadow
        for (Ball b : gm.getBalls()) {
            gc.drawImage(ballShadowImg, scaleX(b.getX()) + shadowOffX, scaleY(b.getY()) + shadowOffY, scaleW(b.getWidth()), scaleH(b.getHeight()));
        }
        gc.restore();

        // 5. Draw all gameplay entities (torpedoes, bricks, power-ups, balls, paddle).
        gc.save();
        gc.beginPath();
        gc.rect(PLAY_AREA_X, PLAY_AREA_Y, PLAY_AREA_WIDTH, PLAY_AREA_HEIGHT);
        gc.closePath();
        gc.clip();

        // Torpedoes
        for (var t : gm.getTorpedoes()) {
            gc.drawImage(torpedoImg, scaleX(t.getX()), scaleY(t.getY()), scaleW(t.getWidth()), scaleH(t.getHeight()));
        }

        // Bricks
        for (Brick b : gm.getBricksSafe()) {
            if (!b.isDestroyed()) {
                Image img = getBrickImage(b);
                if (img != null) gc.drawImage(img, scaleX(b.getX()), scaleY(b.getY()), scaleW(b.getWidth()), scaleH(b.getHeight()));
            }
        }

        // PowerUps
        for (PowerUp pu : gm.getPowerUps()) {
            List<Image> frames = powerUpFramesByType.getOrDefault(pu.getType(), powerUpFrames);
            if (frames != null && !frames.isEmpty()) {
                int idx = pu.getAnimationIndex();
                gc.drawImage(frames.get(idx % frames.size()), scaleX(pu.getX()), scaleY(pu.getY()), scaleW(pu.getWidth()), scaleH(pu.getHeight()));
            }
        }

        // Balls
        for (Ball b : gm.getBalls()) {
            gc.drawImage(ballImg, scaleX(b.getX()), scaleY(b.getY()), scaleW(b.getWidth()), scaleH(b.getHeight()));
        }

        // Paddle
        if (gm.getLives() > 0) {
            Image pImg = paddleStdImg;
            if (p.areLasersActive()) pImg = paddleGunImg;
            else if (p.isCatchActive()) pImg = paddleWideImg;

            // Use sprite sheet logic if needed, here simple image
            // Note: Main uses an animated sprite map, here we use static images for simplicity or need sprite sheet logic
            // Assuming static images loaded for now as per constructor
            gc.drawImage(pImg, scaleX(p.getX()), scaleY(p.getY()), scaleW(p.getWidth()), scaleH(p.getHeight()));
        }

        // Effects
        for (Effect e : gm.getEffects()) {
            // Effects render in game coordinates, need to handle scaling inside Effect or translate GC
            // For simplicity, assuming Effect handles its own relative rendering or is skipped for now
            // To fix: Translate GC to play area and scale
            gc.save();
            gc.translate(PLAY_AREA_X, PLAY_AREA_Y);
            gc.scale(SCALE_X, SCALE_Y);
            e.render(gc);
            gc.restore();
        }
        gc.restore();

        // 6. Draw frame / border (pipes, vertical borders, doors).
        drawBorder(gc);

        // 7. Draw HUD text (high score label, score, lives, state overlays).
        // Top Area Black fill for UI
        // Reference: High Score centered, Score right aligned?
        // Main.java: HIGH SCORE center, current Score right aligned
        gc.setFill(Color.rgb(229, 2, 1)); // High Score Red
        gc.setFont(highScoreFont);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.TOP);
        gc.fillText("HIGH SCORE", VIEW_WIDTH * 0.5, 10);

        gc.setFill(Color.WHITE);
        // Dummy Highscore
        gc.fillText("50000", VIEW_WIDTH * 0.5, 35);

        // Current Score (Right aligned roughly)
        gc.setTextAlign(TextAlignment.RIGHT);
        gc.fillText(String.valueOf(gm.getScore()), VIEW_WIDTH - 150, 35); // Adjust X

        // 8. Draw Lives (Bottom Left)
        double startX = SIDE_BORDER_WIDTH + 5;
        double lifeY = VIEW_HEIGHT - 30;
        for (int i = 0; i < gm.getLives(); i++) {
            gc.drawImage(paddleMiniImg, startX + i * 45, lifeY, 40, 11); // Size from Main.java
        }

        // 9. State Overlays
        if (gm.getCurrentState() == GameManager.GameState.GAME_OVER) {
            gc.setFill(Color.rgb(216, 216, 216));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText("GAME OVER", VIEW_WIDTH * 0.5, VIEW_HEIGHT * 0.6);
        } else if (gm.getCurrentState() == GameManager.GameState.LEVEL_CLEARED) {
            gc.setFill(Color.rgb(216, 216, 216));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText("ROUND " + gm.getNextLevel(), VIEW_WIDTH * 0.5, VIEW_HEIGHT * 0.6);
        }
    }

    /**
     * Draws the decorative border, top pipe and level exit door
     * surrounding the playfield. The coordinates are adapted from
     * the reference Main.java implementation.
     */
    private void drawBorder(GraphicsContext gc) {
        // --- Dimensions from Main.java adapted to View ---
        // Main.java: INSET = 22 (Border Width), UPPER_INSET = 85
        // Pipe height = 17, Pipe Y = 68 (85 - 17)

        double borderW = SIDE_BORDER_WIDTH;
        double upperInset = TOP_UI_BAR_HEIGHT;

        // 1. Draw Vertical Borders (Left & Right)
        if (borderPatternFill != null) {
            gc.setFill(borderPatternFill);
            // Left Column
            gc.fillRect(0, upperInset, borderW, VIEW_HEIGHT - upperInset);
            // Right Column
            gc.fillRect(VIEW_WIDTH - borderW, upperInset, borderW, VIEW_HEIGHT - upperInset);
        }

        // 2. Draw Top Pipe (Horizontal)
        // Logic: Pipe is at y = 68, height = 17.
        double pipeH = 17;
        double pipeY = upperInset - pipeH; // 85 - 17 = 68

        if (pipePatternFill != null) {
            gc.setFill(pipePatternFill);
            // Left segment (Border to Door 1)
            // Main.java uses hardcoded 100 for door pos.
            // We scale 100 relative to view width approx?
            // Let's stick to fixed 100px from side to match style
            double doorX1 = 100;
            double doorX2 = VIEW_WIDTH - 100 - topDoorImg.getWidth();

            // Segment 1: Left Edge to Door 1
            // Start after corner (approx 20px?) -> Corner is drawn at 2.5
            gc.fillRect(borderW - 5, pipeY, doorX1 - (borderW - 5), pipeH);

            // Segment 2: Between Doors
            double startMid = doorX1 + topDoorImg.getWidth();
            gc.fillRect(startMid, pipeY, doorX2 - startMid, pipeH);

            // Segment 3: Door 2 to Right Edge
            double startRight = doorX2 + topDoorImg.getWidth();
            gc.fillRect(startRight, pipeY, (VIEW_WIDTH - borderW + 5) - startRight, pipeH);
        }

        // 3. Draw Corners (Upper Left & Right)
        // Main.java: ulCornerImg at (2.5, 67.5)
        // corner is 15x20.
        // Adjust Y to match pipeY approx.
        if (ulCornerImg != null) {
            gc.drawImage(ulCornerImg, 0, pipeY, borderW, 20);
        }
        if (urCornerImg != null) {
            gc.drawImage(urCornerImg, VIEW_WIDTH - borderW, pipeY, borderW, 20);
        }

        // 4. Draw Top Doors
        if (topDoorImg != null) {
            double doorY = pipeY - 3; // Slight offset up
            // Left Door
            gc.drawImage(topDoorImg, 100, doorY);
            // Right Door
            gc.drawImage(topDoorImg, VIEW_WIDTH - 100 - topDoorImg.getWidth(), doorY);
        }

        // 5. Draw Exit Door (Next Level Door) - Bottom Right vertical
        // Main.java logic:
        // If open: animate opacity/slide.
        // Here simplistic drawing:
        if (borderPartVerticalImg != null && openDoorMapImg != null) {
            // It is drawn over the Right Border
            double exitDoorY = upperInset + 565 * SCALE_Y; // Scale the Y position
            // Just draw static closed door part for now or 'borderPartVertical'
            // If you want the 'Open Door' graphic from resources/images/open_door_map.png
            // It's a sprite sheet. Frame 0 is closed.

            // Draw over the right border pattern
            // gc.drawImage(borderPartVerticalImg, VIEW_WIDTH - borderW, exitDoorY);

            // Draw the door sprite (Frame 0)
            // Source: 0,0, 20, 71. Dest: RightBorderX, Y
            gc.drawImage(openDoorMapImg, 0, 0, 20, 71, VIEW_WIDTH - borderW, VIEW_HEIGHT - 120, borderW, 71); // Approx Y pos at bottom
        }
    }

    /**
     * Returns the appropriate brick sprite based on its type or,
     * for normal bricks, based on their logical colour.
     */
    private Image getBrickImage(Brick b) {
        if (b instanceof com.hung.arkanoid.model.entities.brick.NormalBrick nb) {
            return switch (nb.getColorStyle()) {
                case "YELLOW", "YLLW" -> yellowBlockImg;
                case "LIME" -> limeBlockImg;
                case "ORANGE", "ORNG" -> orangeBlockImg;
                case "CYAN" -> cyanBlockImg;
                case "WHITE", "WHIT" -> whiteBlockImg;
                case "MAGENTA", "MGNT" -> magentaBlockImg;
                default -> blueBlockImg;
            };
        }
        return switch (b.getType()) {
            case UNBREAKABLE -> goldBlockImg;
            case STRONG -> grayBlockImg;
            case EXPLOSIVE -> redBlockImg;
            default -> blueBlockImg;
        };
    }
}
