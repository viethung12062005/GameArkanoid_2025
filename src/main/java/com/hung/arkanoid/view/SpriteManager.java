package com.hung.arkanoid.view;

import javafx.scene.image.Image;

import java.io.InputStream;

public final class SpriteManager {
    private SpriteManager() {}

    /**
     * Try several common resource paths and return the first Image found, or null.
     */
    public static Image loadResourceVariants(final String baseName) {
        if (baseName == null) throw new IllegalArgumentException("baseName must not be null");
        String[] exts = new String[] {".png", ".jpg", ".gif"};
        ClassLoader cl = SpriteManager.class.getClassLoader();
        for (String e : exts) {
            String resourcePath = "images/" + baseName + e; // classloader expects no leading '/'
            try (InputStream is = cl.getResourceAsStream(resourcePath)) {
                if (is != null) {
                    try { return new Image(is); } catch (Exception ex) { throw new IllegalStateException("Failed to read image resource: /" + resourcePath, ex); }
                }
            } catch (Exception ignored) {}
        }
        // Not found -> fail fast
        throw new IllegalStateException("Missing image resource under /images for baseName='" + baseName + "' (tried .png,.jpg,.gif)");
    }

    /**
     * Lightweight animated sprite state used by entities to track frames in a sprite sheet
     * arranged in columns (maxFrameX) and rows (maxFrameY).
     */
    public static final class AnimatedSpriteState {
        private final int maxFrameX;
        private final int maxFrameY;
        private int countX;
        private int countY;
        private final double frameDuration; // seconds per frame
        private double accumulator; // seconds accumulated since last frame advance

        public AnimatedSpriteState(final int maxFrameX, final int maxFrameY) {
            this(maxFrameX, maxFrameY, 0.1); // default 0.1s per frame
        }

        public AnimatedSpriteState(final int maxFrameX, final int maxFrameY, final double frameDurationSeconds) {
            this.maxFrameX = Math.max(1, maxFrameX);
            this.maxFrameY = Math.max(1, maxFrameY);
            this.countX = 0;
            this.countY = 0;
            this.frameDuration = frameDurationSeconds <= 0 ? 0.1 : frameDurationSeconds;
            this.accumulator = 0.0;
        }

        /**
         * Advance animation based on elapsed seconds.
         */
        public void update(double deltaSeconds) {
            if (deltaSeconds <= 0) return;
            accumulator += deltaSeconds;
            while (accumulator >= frameDuration) {
                accumulator -= frameDuration;
                countX++;
                if (countX >= maxFrameX) {
                    countX = 0;
                    countY++;
                    if (countY >= maxFrameY) {
                        countY = 0;
                    }
                }
            }
        }

        /** Backwards-compatible single-step update (advances by one frame). */
        public void update() { update(frameDuration); }

        /** Return linear frame index (row-major): countY * maxFrameX + countX */
        public int getFrameIndex() {
            return countY * maxFrameX + countX;
        }

        public int getCountX() { return countX; }
        public int getCountY() { return countY; }
    }

    /**
     * Slice a sprite sheet into frames arranged in columns x rows. Returns a list in row-major order.
     */
    public static java.util.List<javafx.scene.image.Image> sliceFrames(final Image sheet, final int cols, final int rows) {
        java.util.List<javafx.scene.image.Image> frames = new java.util.ArrayList<>();
        if (sheet == null || cols <= 0 || rows <= 0) return frames;
        try {
            final javafx.scene.image.PixelReader pr = sheet.getPixelReader();
            if (pr == null) return frames;
            int fw = (int) Math.round(sheet.getWidth() / cols);
            int fh = (int) Math.round(sheet.getHeight() / rows);
            for (int y = 0; y < rows; y++) {
                for (int x = 0; x < cols; x++) {
                    try {
                        javafx.scene.image.WritableImage wi = new javafx.scene.image.WritableImage(pr, x * fw, y * fh, fw, fh);
                        frames.add(wi);
                    } catch (Exception ignored) { }
                }
            }
        } catch (Exception ignored) {}
        return frames;
    }

    /**
     * Slice a single frame from a sprite sheet by column/row indices.
     */
    public static javafx.scene.image.Image sliceFrame(final Image sheet, final int col, final int row, final int cols, final int rows) {
        if (sheet == null || cols <= 0 || rows <= 0) return null;
        try {
            final javafx.scene.image.PixelReader pr = sheet.getPixelReader();
            if (pr == null) return null;
            int fw = (int) Math.round(sheet.getWidth() / cols);
            int fh = (int) Math.round(sheet.getHeight() / rows);
            return new javafx.scene.image.WritableImage(pr, col * fw, row * fh, fw, fh);
        } catch (Exception ignored) { return null; }
    }

    /**
     * Convenience: load a sprite sheet by baseName and slice a specific frame.
     */
    public static javafx.scene.image.Image getSpriteFrame(final String baseName, final int col, final int row, final int cols, final int rows) {
        Image sheet = loadResourceVariants(baseName);
        return sliceFrame(sheet, col, row, cols, rows);
    }
}
