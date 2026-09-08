package com.mlc.mlcgames.dungeongame;

import java.util.Locale;

public enum DungeonDifficulty {
    EASY("简单", 0.75, 0.75),
    NORMAL("普通", 1.0, 1.0),
    HARD("困难", 1.5, 1.35);

    private final String displayName;
    private final double healthMultiplier;
    private final double damageMultiplier;

    DungeonDifficulty(String displayName, double healthMultiplier, double damageMultiplier) {
        this.displayName = displayName;
        this.healthMultiplier = healthMultiplier;
        this.damageMultiplier = damageMultiplier;
    }

    public String displayName() { return displayName; }
    public double healthMultiplier() { return healthMultiplier; }
    public double damageMultiplier() { return damageMultiplier; }
    public DungeonDifficulty next() { return values()[(ordinal() + 1) % values().length]; }
    public DungeonDifficulty previous() { return values()[(ordinal() + values().length - 1) % values().length]; }

    public static DungeonDifficulty parse(String value) {
        if (value == null) return NORMAL;
        try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return NORMAL;
        }
    }
}
