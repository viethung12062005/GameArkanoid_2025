package com.hung.arkanoid.game;

import java.util.prefs.Preferences;

public final class SaveData {
    private static final Preferences PREFS = Preferences.userNodeForPackage(SaveData.class);
    private static final String KEY_MAX_LEVEL = "max_level_unlocked";

    private SaveData() {}

    public static int loadMaxLevelUnlocked() {
        return PREFS.getInt(KEY_MAX_LEVEL, 1);
    }

    public static void saveMaxLevelUnlocked(int level) {
        if (level <= 0) return;
        int current = loadMaxLevelUnlocked();
        if (level > current) {
            PREFS.putInt(KEY_MAX_LEVEL, level);
        }
    }
}

