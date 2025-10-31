package com.hung.arkanoid.game;

import com.hung.arkanoid.game.SaveData.HighScoreEntry;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SaveDataTest {

    @Test
    void highScoreEntry_compareTo_sortsByScoreDescending() {
        HighScoreEntry a = new HighScoreEntry("A", 100);
        HighScoreEntry b = new HighScoreEntry("B", 200);
        HighScoreEntry c = new HighScoreEntry("C", 50);

        List<HighScoreEntry> list = new ArrayList<>();
        list.add(a);
        list.add(b);
        list.add(c);

        Collections.sort(list);

        assertEquals("B", list.get(0).name);
        assertEquals("A", list.get(1).name);
        assertEquals("C", list.get(2).name);
    }

    @Test
    void updateHighScore_addUpdateAndKeepTopFive() {
        // NOTE: Preferences are JVM-global; avoid assuming a completely empty state.
        // Instead, capture initial size and reason about relative behavior.
        java.util.List<HighScoreEntry> initial = SaveData.loadHighScores();

        // Scenario A: Add or update Alice
        SaveData.updateHighScore("Alice", 1000);
        java.util.List<HighScoreEntry> scores = SaveData.loadHighScores();
        assertTrue(scores.stream().anyMatch(e -> e.name.equals("Alice") && e.score >= 1000));

        // Scenario B: Update same name with higher score
        SaveData.updateHighScore("Alice", 1500);
        scores = SaveData.loadHighScores();
        HighScoreEntry alice = scores.stream().filter(e -> e.name.equals("Alice")).findFirst().orElseThrow();
        assertEquals(1500, alice.score);

        // Add more players
        SaveData.updateHighScore("Bob", 900);
        SaveData.updateHighScore("Carol", 800);
        SaveData.updateHighScore("Dave", 700);
        SaveData.updateHighScore("Eve", 600);

        scores = SaveData.loadHighScores();
        assertTrue(scores.size() <= 5, "Highscore list should never exceed 5 entries");

        // Scenario C: Add sixth entry and ensure only top 5 kept
        SaveData.updateHighScore("Frank", 650);
        scores = SaveData.loadHighScores();
        assertTrue(scores.size() <= 5, "Highscore list should keep top 5 only");

        // Scores should be sorted descending
        int previous = Integer.MAX_VALUE;
        for (HighScoreEntry entry : scores) {
            assertTrue(entry.score <= previous);
            previous = entry.score;
        }
    }
}
