package com.hung.arkanoid.game;

import javafx.scene.media.AudioClip;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Centralized sound manager for the Arkanoid game.
 * Responsible for loading audio clips from the {@code /sounds} resource
 * folder and exposing simple helper methods to play common game sound
 * effects such as paddle hits, brick impacts and explosions.
 */
public class SoundManager {

    private final Map<String, AudioClip> sounds = new HashMap<>();

    /**
     * Creates a new sound manager and eagerly loads all configured
     * sound resources.
     */
    public SoundManager() {
        load();
    }

    /**
     * Loads the built-in sound resources and registers them under
     * well-known keys used by the rest of the game code.
     */
    private void load() {
        // Resources in the project are stored as MP3 files under /sounds.
        put("ball_paddle", "/sounds/ball_paddle.mp3");
        put("ball_block", "/sounds/ball_block.mp3");
        put("ball_hard", "/sounds/ball_hard_block.mp3");
        put("laser", "/sounds/gun.mp3");
        put("explosion", "/sounds/explosion.mp3");
        put("game_over", "/sounds/game_over.mp3");
        put("level_ready", "/sounds/level_ready.mp3");
        put("start", "/sounds/game_start.mp3");
    }

    /**
     * Resolves the given resource path to an {@link AudioClip} and stores
     * it in the internal map under the provided key.
     * The resource path may contain a directory prefix, but only the final
     * file name segment is used; the clip is always loaded from the
     * {@code /sounds} classpath folder.
     *
     * @param key      logical identifier used by {@link #play(String)}
     * @param resource resource path or file name
     */
    private void put(String key, String resource) {
        try {
            // Normalize to the last path segment to avoid duplicated /sounds parts.
            String name = resource;
            int lastSlash = name.lastIndexOf('/');
            if (lastSlash >= 0) {
                name = name.substring(lastSlash + 1);
            }

            URL url = getClass().getResource("/sounds/" + name);
            if (url == null) {
                throw new IllegalStateException("Missing sound resource: /sounds/" + name);
            }

            AudioClip clip = new AudioClip(url.toExternalForm());
            sounds.put(key, clip);
        } catch (Exception ex) {
            // Fail fast and clearly when a sound cannot be loaded.
            throw new IllegalStateException("Failed to load sound resource: " + resource, ex);
        }
    }

    /**
     * Plays a sound previously registered under the given key.
     * If no sound is associated with the key, this method is a no-op.
     *
     * @param key identifier of the sound to play
     */
    public void play(String key) {
        AudioClip clip = sounds.get(key);
        if (clip != null) {
            clip.play();
        }
    }

    // Generic convenience methods ------------------------------------------------

    /** Plays the standard ball-paddle collision sound. */
    public void playPaddleHit() { play("ball_paddle"); }

    /** Plays the standard brick-destroyed sound. */
    public void playBlockHit() { play("ball_block"); }

    /** Plays the sound for hitting a hard/strong brick. */
    public void playHardBlockHit() { play("ball_hard"); }

    /** Plays the torpedo/laser firing sound. */
    public void playLaser() { play("laser"); }

    /** Plays the explosion sound effect. */
    public void playExplosion() { play("explosion"); }

    /** Plays the game over sound effect. */
    public void playGameOver() { play("game_over"); }

    /** Plays the level ready / intro sound for a new level. */
    public void playLevelReady() { play("level_ready"); }

    // Backwards-compatible aliases expected by GameManager -----------------------

    /** Alias kept for legacy callers expecting a "hard brick" hit sound. */
    public void playHardBrickHit() { playHardBlockHit(); }

    /** Alias mapping older naming to the paddle hit sound. */
    public void playBallPaddle() { playPaddleHit(); }

    /** Alias mapping older naming to the brick hit sound. */
    public void playBallBlock() { playBlockHit(); }
}
