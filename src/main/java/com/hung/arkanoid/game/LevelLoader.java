package com.hung.arkanoid.game;

import com.hung.arkanoid.model.entities.brick.Brick;
import com.hung.arkanoid.model.entities.brick.BrickFactory;

import java.util.ArrayList;
import java.util.List;

public final class LevelLoader {
    // Increase supported levels to 32 (mirrors Constants.LEVEL_MAP count in original reference)
    public static final int MAX_LEVELS = 32;

    private LevelLoader() {}

    public static List<Brick> loadLevel(int levelNumber) {
        if (levelNumber <= 0) levelNumber = 1;
        if (levelNumber > MAX_LEVELS) levelNumber = ((levelNumber - 1) % MAX_LEVELS) + 1;

        return switch (levelNumber) {
            case 1 -> loadLevel1();
            case 2 -> loadLevel2();
            case 3 -> loadLevel3();
            default -> generateAlgorithmicLevel(levelNumber);
        };
    }

    // Keep original handcrafted levels for 1-3
    private static List<Brick> loadLevel1() {
        List<Brick> bricks = new ArrayList<>();
        double startX = 60;
        double startY = 60;
        double spacingX = Brick.BRICK_WIDTH + 6;
        double spacingY = Brick.BRICK_HEIGHT + 6;

        // simple rows of normal bricks
        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 8; c++) {
                bricks.add(BrickFactory.create("NORMAL", startX + c * spacingX, startY + r * spacingY));
            }
        }
        // a few strong bricks in middle
        bricks.add(BrickFactory.create("STRONG", startX + 3 * spacingX, startY + 4 * spacingY));
        bricks.add(BrickFactory.create("STRONG", startX + 4 * spacingX, startY + 4 * spacingY));
        // one powerup guaranteed brick
        bricks.add(BrickFactory.create("NORMAL", startX + 7 * spacingX, startY + 2 * spacingY)); // replaced POWERUP_GUARANTEED with NORMAL
        return bricks;
    }

    private static List<Brick> loadLevel2() {
        List<Brick> bricks = new ArrayList<>();
        double startX = 60;
        double startY = 60;
        double spacingX = Brick.BRICK_WIDTH + 6;
        double spacingY = Brick.BRICK_HEIGHT + 6;

        // alternating pattern with some unbreakables and explosives
        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 9; c++) {
                String key;
                if (r == 2 && c % 3 == 0) key = "UNBREAKABLE";
                else if ((r + c) % 5 == 0) key = "EXPLOSIVE";
                else key = "NORMAL";
                bricks.add(BrickFactory.create(key, startX + c * spacingX, startY + r * spacingY));
            }
        }
        // a few strong bricks
        bricks.add(BrickFactory.create("STRONG", startX + 2 * spacingX, startY + 1 * spacingY));
        bricks.add(BrickFactory.create("STRONG", startX + 6 * spacingX, startY + 1 * spacingY));
        return bricks;
    }

    private static List<Brick> loadLevel3() {
        List<Brick> bricks = new ArrayList<>();
        double startX = 60;
        double startY = 40;
        double spacingX = Brick.BRICK_WIDTH + 6;
        double spacingY = Brick.BRICK_HEIGHT + 6;

        // dense strong brick field with pockets of powerups
        for (int r = 0; r < 6; r++) {
            for (int c = 0; c < 10; c++) {
                String key;
                if (r % 2 == 0 && c % 4 == 0) key = "NORMAL"; // removed POWERUP_GUARANTEED
                else if (c % 5 == 0) key = "EXPLOSIVE";
                else key = "STRONG";
                bricks.add(BrickFactory.create(key, startX + c * spacingX, startY + r * spacingY));
            }
        }
        return bricks;
    }

    // Algorithmic generator for higher levels inspired by Constants.LEVEL_MAP patterns
    private static List<Brick> generateAlgorithmicLevel(int levelNumber) {
        List<Brick> bricks = new ArrayList<>();
        double startX = 40;
        double startY = 40;
        double spacingX = Brick.BRICK_WIDTH + 6;
        double spacingY = Brick.BRICK_HEIGHT + 6;

        // Determine grid size that grows with level but remains reasonable
        int rows = Math.min(12, 3 + (levelNumber / 2));
        int cols = Math.min(13, 6 + (levelNumber / 4));

        int pattern = levelNumber % 6; // choose a pattern

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                String key;
                switch (pattern) {
                    case 0 -> { // stripes: alternating strong/normal rows
                        key = (r % 2 == 0) ? "STRONG" : "NORMAL";
                        if (c % 7 == 0 && r % 3 == 0) key = "NORMAL"; // removed POWERUP_GUARANTEED
                    }
                    case 1 -> { // checkerboard of normal/strong with some unbreakable anchors
                        key = ((r + c) % 2 == 0) ? "NORMAL" : "STRONG";
                        if (r == 0 || r == rows - 1) key = "UNBREAKABLE";
                    }
                    case 2 -> { // pyramid: strong in center
                        int centerC = cols / 2;
                        int dist = Math.abs(c - centerC);
                        key = (dist <= Math.max(0, (cols / 8))) ? "STRONG" : "NORMAL";
                        if (r > rows - 4 && (c % 5 == 0)) key = "NORMAL"; // removed POWERUP_GUARANTEED
                    }
                    case 3 -> { // columns of alternating types
                        key = (c % 3 == 0) ? "UNBREAKABLE" : (c % 3 == 1) ? "EXPLOSIVE" : "NORMAL";
                    }
                    case 4 -> { // border strong, inner normal
                        boolean border = r == 0 || r == rows - 1 || c == 0 || c == cols - 1;
                        key = border ? "STRONG" : "NORMAL";
                        if (!border && (r + c) % 8 == 0) key = "NORMAL"; // removed POWERUP_GUARANTEED
                    }
                    default -> { // sparse powerups + mix
                        if (r % 4 == 0 && c % 4 == 0) key = "NORMAL"; // removed POWERUP_GUARANTEED
                        else if ((r + c) % 5 == 0) key = "STRONG";
                        else key = "NORMAL";
                    }
                }
                // Occasionally insert an EXPLOSIVE for variety at higher levels
                if (levelNumber > 12 && (r + c) % 11 == 0) key = "EXPLOSIVE";

                double x = startX + c * spacingX;
                double y = startY + r * spacingY;
                bricks.add(BrickFactory.create(key, x, y));
            }
        }

        // Removed guaranteed-powerup bricks for higher levels per request

        return bricks;
    }
}
