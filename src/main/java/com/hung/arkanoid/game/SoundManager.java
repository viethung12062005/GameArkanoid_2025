package com.hung.arkanoid.game;

import javafx.scene.media.AudioClip;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class SoundManager {
    private final Map<String, AudioClip> sounds = new HashMap<>();

    public SoundManager() {
        load();
    }

    private void load() {
        // try multiple names/extensions; resources in project are .mp3 — AudioClip supports mp3
        put("ball_paddle", "/sounds/ball_paddle.mp3");
        put("ball_block", "/sounds/ball_block.mp3");
        put("ball_hard", "/sounds/ball_hard_block.mp3");
        put("laser", "/sounds/gun.mp3");
        put("explosion", "/sounds/explosion.mp3");
        put("game_over", "/sounds/game_over.mp3");
        put("level_ready", "/sounds/level_ready.mp3");
        put("start", "/sounds/game_start.mp3");
    }

    private void put(String key, String resource) {
        try {
            // resource expected to be like "/sounds/Filename.ext" or "/Filename.ext"; normalize to last segment
            String name = resource;
            int lastSlash = name.lastIndexOf('/');
            if (lastSlash >= 0) name = name.substring(lastSlash + 1);
            URL u = getClass().getResource("/sounds/" + name);
            if (u == null) throw new IllegalStateException("Missing sound resource: /sounds/" + name);
            AudioClip ac = new AudioClip(u.toExternalForm());
            sounds.put(key, ac);
        } catch (Exception ignored) {
            throw new IllegalStateException("Failed to load sound resource: " + resource);
        }
    }

    public void play(String key) {
        AudioClip ac = sounds.get(key);
        if (ac != null) ac.play();
    }

    // Generic plays
    public void playPaddleHit() { play("ball_paddle"); }
    public void playBlockHit() { play("ball_block"); }
    public void playHardBlockHit() { play("ball_hard"); }
    public void playLaser() { play("laser"); }
    public void playExplosion() { play("explosion"); }
    public void playGameOver() { play("game_over"); }
    public void playLevelReady() { play("level_ready"); }

    // Backwards-compatible aliases expected by GameManager
    public void playHardBrickHit() { playHardBlockHit(); }
    public void playBallPaddle() { playPaddleHit(); }
    public void playBallBlock() { playBlockHit(); }
}
