package com.hung.arkanoid.view;

import javafx.scene.text.Font;

import java.io.InputStream;

public final class Fonts {
    private static Font emulogic;

    private Fonts() {}

    public static Font emulogic(double size) {
        if (emulogic == null) {
            try (InputStream is = Fonts.class.getResourceAsStream("/images/Emulogic-zrEw.ttf")) {
                if (is == null) throw new IllegalStateException("Missing font resource: /images/Emulogic-zrEw.ttf");
                emulogic = Font.loadFont(is, size);
            } catch (Exception e) {
                throw new IllegalStateException("Failed to load font Emulogic: " + e.getMessage(), e);
            }
        }
        return emulogic;
    }
}
