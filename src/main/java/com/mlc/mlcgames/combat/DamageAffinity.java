package com.mlc.mlcgames.combat;

/** Semantic result of an attack-type and armour-type match-up. */
public enum DamageAffinity {
    WEAK(2.0D), EFFECTIVE(1.5D), NORMAL(1.0D), RESIST(0.5D);

    private final double multiplier;

    DamageAffinity(double multiplier) { this.multiplier = multiplier; }

    public double multiplier() { return multiplier; }
}
