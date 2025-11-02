package com.hung.arkanoid.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * Persistence helper for user progress and high scores.
 * Uses {@link Preferences} as a lightweight storage backend to keep track of
 * the highest unlocked level, the current player name and a top-5 high-score
 * table.
 */
public final class SaveData {

    private static final Preferences PREFS = Preferences.userNodeForPackage(SaveData.class);

    private static final String KEY_MAX_LEVEL = "max_level_unlocked";
    private static final String KEY_CURRENT_PLAYER = "current_player_name";

    private static final String KEY_HS_SCORE_PREFIX = "highscore_score_";
    private static final String KEY_HS_NAME_PREFIX = "highscore_name_";

    private SaveData() {
        // Utility class; prevent instantiation.
    }

    // --- Level Progress Logic -------------------------------------------------

    /**
     * Reads the highest level index the player has unlocked so far.
     *
     * @return highest unlocked level, defaults to 1 when not present
     */
    public static int loadMaxLevelUnlocked() {
        return PREFS.getInt(KEY_MAX_LEVEL, 1);
    }

    /**
     * Persists the highest level index the player has unlocked.
     * The stored value is only updated when the new level is strictly higher
     * than the previous value or when resetting back to level 1.
     *
     * @param level new highest unlocked level
     */
    public static void saveMaxLevelUnlocked(int level) {
        if (level <= 0) {
            return;
        }
        int current = loadMaxLevelUnlocked();
        if (level > current || level == 1) {
            PREFS.putInt(KEY_MAX_LEVEL, level);
        }
    }

    /**
     * Resets the progression to level 1.
     */
    public static void resetProgress() {
        PREFS.putInt(KEY_MAX_LEVEL, 1);
    }

    // --- Player Name Logic ----------------------------------------------------

    /**
     * Returns the last used player name or {@code "Player"} as a default.
     *
     * @return current player name from preferences
     */
    public static String getCurrentPlayerName() {
        return PREFS.get(KEY_CURRENT_PLAYER, "Player");
    }

    /**
     * Stores the current player name. Empty or {@code null} values are
     * normalized to the default name "Player".
     *
     * @param name name to persist
     */
    public static void saveCurrentPlayerName(String name) {
        if (name == null || name.trim().isEmpty()) {
            name = "Player";
        }
        PREFS.put(KEY_CURRENT_PLAYER, name);
    }

    // --- High Score Logic -----------------------------------------------------

    /**
     * Simple value object representing a high-score entry.
     * High scores are ordered in descending score order.
     */
    public static class HighScoreEntry implements Comparable<HighScoreEntry> {
        public String name;
        public int score;

        public HighScoreEntry(String name, int score) {
            this.name = name;
            this.score = score;
        }

        @Override
        public int compareTo(HighScoreEntry other) {
            // Sort in descending order by score.
            return Integer.compare(other.score, this.score);
        }
    }

    /**
     * Loads up to five high-score entries from the preference store.
     *
     * @return list of stored high-score entries ordered as they were saved
     */
    public static List<HighScoreEntry> loadHighScores() {
        List<HighScoreEntry> list = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            int score = PREFS.getInt(KEY_HS_SCORE_PREFIX + i, 0);
            String name = PREFS.get(KEY_HS_NAME_PREFIX + i, "---");
            if (score > 0) {
                list.add(new HighScoreEntry(name, score));
            }
        }
        return list;
    }

    /**
     * Updates the high-score table with a new entry.
     * Rules:
     * If the name already exists, only the best (highest) score is kept.</li>
     * If the name is new, the entry is added.
     * After the update, scores are sorted in descending order and only
     * the top 5 entries are persisted.
     *
     * @param name  player name
     * @param score achieved score; non-positive values are ignored
     */
    public static void updateHighScore(String name, int score) {
        if (score <= 0) {
            return;
        }

        List<HighScoreEntry> list = loadHighScores();
        boolean found = false;

        // Step 1: check if the player name already exists in the table.
        for (HighScoreEntry entry : list) {
            if (entry.name.equalsIgnoreCase(name)) {
                // If an existing entry is found, keep the better score.
                if (score > entry.score) {
                    entry.score = score;
                }
                found = true;
                break;
            }
        }

        // Step 2: if not found, append a new entry.
        if (!found) {
            list.add(new HighScoreEntry(name, score));
        }

        // Step 3: sort in descending order (see compareTo).
        Collections.sort(list);

        // Step 4: keep only top 5 scores.
        if (list.size() > 5) {
            list = list.subList(0, 5);
        }

        // Step 5: persist updated table back to preferences.
        for (int i = 0; i < 5; i++) {
            if (i < list.size()) {
                PREFS.putInt(KEY_HS_SCORE_PREFIX + i, list.get(i).score);
                PREFS.put(KEY_HS_NAME_PREFIX + i, list.get(i).name);
            } else {
                PREFS.remove(KEY_HS_SCORE_PREFIX + i);
                PREFS.remove(KEY_HS_NAME_PREFIX + i);
            }
        }
    }
}