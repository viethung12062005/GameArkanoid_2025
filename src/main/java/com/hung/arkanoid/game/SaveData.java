package com.hung.arkanoid.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.prefs.Preferences;

public final class SaveData {
    private static final Preferences PREFS = Preferences.userNodeForPackage(SaveData.class);

    private static final String KEY_MAX_LEVEL = "max_level_unlocked";
    private static final String KEY_CURRENT_PLAYER = "current_player_name";

    private static final String KEY_HS_SCORE_PREFIX = "highscore_score_";
    private static final String KEY_HS_NAME_PREFIX = "highscore_name_";

    private SaveData() {}

    // --- Level Logic ---
    public static int loadMaxLevelUnlocked() {
        return PREFS.getInt(KEY_MAX_LEVEL, 1);
    }

    public static void saveMaxLevelUnlocked(int level) {
        if (level <= 0) return;
        int current = loadMaxLevelUnlocked();
        // Chỉ lưu nếu level mới cao hơn level cũ (cho Continue), hoặc nếu reset về 1
        if (level > current || level == 1) {
            PREFS.putInt(KEY_MAX_LEVEL, level);
        }
    }

    public static void resetProgress() {
        PREFS.putInt(KEY_MAX_LEVEL, 1);
    }

    // --- Player Name Logic ---
    public static String getCurrentPlayerName() {
        return PREFS.get(KEY_CURRENT_PLAYER, "Player");
    }

    public static void saveCurrentPlayerName(String name) {
        if (name == null || name.trim().isEmpty()) name = "Player";
        PREFS.put(KEY_CURRENT_PLAYER, name);
    }

    // --- High Score Logic ---
    public static class HighScoreEntry implements Comparable<HighScoreEntry> {
        public String name;
        public int score;

        public HighScoreEntry(String name, int score) {
            this.name = name;
            this.score = score;
        }

        @Override
        public int compareTo(HighScoreEntry other) {
            return other.score - this.score; // Sắp xếp giảm dần
        }
    }

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
     * Cập nhật High Score:
     * - Nếu tên đã tồn tại: Cập nhật điểm nếu điểm mới cao hơn.
     * - Nếu tên chưa tồn tại: Thêm mới.
     * - Sau đó sắp xếp và giữ Top 5.
     */
    public static void updateHighScore(String name, int score) {
        if (score <= 0) return;

        List<HighScoreEntry> list = loadHighScores();
        boolean found = false;

        // 1. Kiểm tra xem tên này đã có chưa
        for (HighScoreEntry entry : list) {
            if (entry.name.equalsIgnoreCase(name)) {
                // Nếu có rồi và điểm mới cao hơn -> Cập nhật
                if (score > entry.score) {
                    entry.score = score;
                }
                found = true;
                break;
            }
        }

        // 2. Nếu chưa có thì thêm mới
        if (!found) {
            list.add(new HighScoreEntry(name, score));
        }

        // 3. Sắp xếp lại
        Collections.sort(list);

        // 4. Giữ Top 5
        if (list.size() > 5) {
            list = list.subList(0, 5);
        }

        // 5. Lưu lại vào bộ nhớ
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