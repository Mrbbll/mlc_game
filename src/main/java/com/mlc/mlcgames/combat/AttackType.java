package com.mlc.mlcgames.combat;

import java.util.Locale;
import java.util.Optional;

/** Blue Archive style attack attributes. */
public enum AttackType {
    EXPLOSIVE,
    PIERCING,
    MYSTIC,
    SONIC;

    public static Optional<AttackType> parse(String value) {
        if (value == null || value.isBlank()) return Optional.empty();
        try {
            return Optional.of(valueOf(value.trim().toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException ignored) {
            return Optional.empty();
        }
    }
}
