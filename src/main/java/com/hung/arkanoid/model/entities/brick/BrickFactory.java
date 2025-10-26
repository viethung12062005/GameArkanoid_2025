package com.hung.arkanoid.model.entities.brick;

/*
 * ============================================================================
 * Project   : Arkanoid_OOP2025
 * Package   : com.hung.arkanoid.model.entities.brick
 * File Name : BrickFactory.java
 * Created On: 10/25/2025 at 9:19 PM
 * Author    : Trần Việt Hưng
 * ----------------------------------------------------------------------------
 * Copyright (c) 2025 Hung Tran.
 * All rights reserved.
 *
 * Description:
 *     This file is part of the Arkanoid Game project.
 *     It defines the BrickFactory class which is responsible for handling.
 *
 *
 * Revision History:
 *     Version 1.0  - Initial release.
 * ============================================================================
 */

import java.lang.reflect.Constructor;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

public class BrickFactory {
    private static final Map<String, BiFunction<Double, Double, Brick>> registry = new ConcurrentHashMap<>();

    static {
        // Register default brick types
    }

    private BrickFactory() {
        // Private constructor to prevent instantiation
    }

    public static void register(String key, BiFunction<Double, Double, Brick> constructor) {
        registry.put(key.toLowerCase(Locale.ROOT), constructor);
    }

    public static void register(String key, Class<? extends Brick> brickClass){
        registry.put(normalized(key), (x, y) -> instantiate(brickClass, x, y));
    }

    public static Brick create(String key, double x, double y) {
        if (key == null) {
            throw new IllegalArgumentException("Brick key must not be null");
        }

        String normalizedKey = normalized(key);
        BiFunction<Double, Double, Brick> creator = registry.get(normalizedKey);

        if (creator != null) {
            return creator.apply(x, y);
        }

        // fallback to enum if not found in registry
        try {
            BrickType type = BrickType.valueOf(normalizedKey);
            return type.create(x, y);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Unknown brick type: " + key);
        }
    }

    private static String normalized(String key) {
        return key.trim().toUpperCase(Locale.ROOT);
    }

    private static Brick instantiate(Class<? extends Brick> brickClass, double x, double y) {
        try {
            Constructor<? extends Brick> brickConstructor = brickClass.getConstructor(double.class, double.class);
            return brickConstructor.newInstance(x, y);
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException("Failed to instantiate brick of type: " + brickClass.getName(), e);
        }
    }
}
