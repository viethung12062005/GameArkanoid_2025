package com.hung.arkanoid.audio;

import javafx.scene.media.AudioClip;

/**
 * Central audio utility for the Arkanoid game.
 * This class loads short {@link AudioClip} sound effects from the classpath
 * and exposes simple static {@code play*} methods that can be invoked from
 * anywhere in the game logic without needing to manage clip instances.
 * All audio files are expected to be located under the {@code /sounds}
 * resource folder.
 */
public final class SoundManager {

    private static final String SOUND_RESOURCE_PATH = "/sounds/";

    private static AudioClip ballPaddle;
    private static AudioClip ballBlock;
    private static AudioClip ballHardBlock;
    private static AudioClip explosion;
    private static AudioClip laser;
    private static AudioClip gameOver;

    /**
     * Utility class constructor. Prevents instantiation.
     */
    private SoundManager() {
        // no-op
    }

    /**
     * Loads all sound effects required by the game from the {@code /sounds} folder.
     * This method must be called once during application startup before any
     * of the play methods are used.
     *
     * @throws IllegalStateException if a required sound resource is missing or
     *                               cannot be loaded
     */
    public static void load() {
        ballPaddle = loadClip("ball_paddle.wav");
        ballBlock = loadClip("ball_block.wav");
        ballHardBlock = loadClip("ball_hard_block.wav");
        explosion = loadClip("explosion.wav");
        laser = loadClip("gun.wav");
        gameOver = loadClip("game_over.wav");
    }

    /**
     * Loads a single {@link AudioClip} from the {@code /sounds} resource folder.
     *
     * @param name name of the audio file including extension (for example {@code "ball_block.wav"})
     * @return initialized {@link AudioClip} instance
     * @throws IllegalStateException if the resource cannot be found or loaded
     */
    private static AudioClip loadClip(String name) {
        try {
            var url = SoundManager.class.getResource(SOUND_RESOURCE_PATH + name);
            if (url == null) {
                throw new IllegalStateException("Missing sound resource: " + SOUND_RESOURCE_PATH + name);
            }
            return new AudioClip(url.toExternalForm());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load sound: " + name + " -> " + e.getMessage(), e);
        }
    }

    /**
     * Plays the sound used when the ball hits the paddle.
     * If the clip has not been loaded or failed to load, this method does nothing.
     */
    public static void playBallPaddle() {
        if (ballPaddle != null) {
            ballPaddle.play();
        }
    }

    /**
     * Plays the sound used when the ball hits a normal brick.
     * If the clip has not been loaded or failed to load, this method does nothing.
     */
    public static void playBallBlock() {
        if (ballBlock != null) {
            ballBlock.play();
        }
    }

    /**
     * Plays the sound used when the ball hits a hard or unbreakable brick.
     * If the clip has not been loaded or failed to load, this method does nothing.
     */
    public static void playBallHardBlock() {
        if (ballHardBlock != null) {
            ballHardBlock.play();
        }
    }

    /**
     * Plays the sound used for brick explosions.
     * If the clip has not been loaded or failed to load, this method does nothing.
     */
    public static void playExplosion() {
        if (explosion != null) {
            explosion.play();
        }
    }

    /**
     * Plays the sound used when the paddle fires a torpedo/laser.
     * If the clip has not been loaded or failed to load, this method does nothing.
     */
    public static void playLaser() {
        if (laser != null) {
            laser.play();
        }
    }

    /**
     * Plays the sound used when the game is over.
     * If the clip has not been loaded or failed to load, this method does nothing.
     */
    public static void playGameOver() {
        if (gameOver != null) {
            gameOver.play();
        }
    }
}
