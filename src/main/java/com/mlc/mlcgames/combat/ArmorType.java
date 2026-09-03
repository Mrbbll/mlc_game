package com.mlc.mlcgames.combat;

import java.util.Locale;
import java.util.Optional;

/** Blue Archive style defensive attributes; unrelated to vanilla armour. */
public enum ArmorType {
    LIGHT,
    HEAVY,
    SPECIAL,
    ELASTIC;

    public static Optional<ArmorType> parse(String value) {
        if (value == null || value.isBlank()) return Optional.empty();
        try {
            return Optional.of(valueOf(value.trim().toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException ignored) {
            return Optional.empty();
        }
    }
}
