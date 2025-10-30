package com.hung.arkanoid.model.entities.brick;

public class BrickFactory {

    private BrickFactory() {}

    public static Brick create(String key, double x, double y) {
        if (key == null) return null;

        String upperKey = key.toUpperCase();

        // Xử lý Gạch Thường có màu (Format: "NORMAL_COLOR")
        if (upperKey.startsWith("NORMAL")) {
            String color = "BLUE"; // Mặc định
            if (upperKey.contains("_")) {
                color = upperKey.substring(upperKey.indexOf("_") + 1);
            }
            return new NormalBrick(x, y, color);
        }

        // Xử lý các loại gạch đặc biệt
        return switch (upperKey) {
            case "STRONG" -> new StrongBrick(x, y);
            case "UNBREAKABLE" -> new UnbreakableBrick(x, y);
            case "EXPLOSIVE" -> new ExplosiveBrick(x, y);
            default -> new NormalBrick(x, y, "BLUE");
        };
    }
}