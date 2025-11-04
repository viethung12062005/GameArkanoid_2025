package com.hung.arkanoid.audio;

import javafx.scene.media.AudioClip;

/**
 * Minimal SoundManager that loads audio clips from resources and exposes play methods.
 */
public final class SoundManager {
    private static AudioClip ballPaddle;
    private static AudioClip ballBlock;
    private static AudioClip ballHardBlock;
    private static AudioClip explosion;
    private static AudioClip laser;
    private static AudioClip gameOver;

    private SoundManager() {}

    public static void load() {
        ballPaddle = loadClip("ball_paddle.wav");
        ballBlock = loadClip("ball_block.wav");
        ballHardBlock = loadClip("ball_hard_block.wav");
        explosion = loadClip("explosion.wav");
        laser = loadClip("gun.wav");
        gameOver = loadClip("game_over.wav");
    }

    private static AudioClip loadClip(String name) {
        try {
            var u = SoundManager.class.getResource("/sounds/" + name);
            if (u == null) throw new IllegalStateException("Missing sound resource: /sounds/" + name);
            return new AudioClip(u.toExternalForm());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load sound: " + name + " -> " + e.getMessage(), e);
        }
    }

    public static void playBallPaddle() { if (ballPaddle != null) ballPaddle.play(); }
    public static void playBallBlock() { if (ballBlock != null) ballBlock.play(); }
    public static void playBallHardBlock() { if (ballHardBlock != null) ballHardBlock.play(); }
    public static void playExplosion() { if (explosion != null) explosion.play(); }
    public static void playLaser() { if (laser != null) laser.play(); }
    public static void playGameOver() { if (gameOver != null) gameOver.play(); }
}
