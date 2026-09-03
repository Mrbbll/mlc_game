package com.mlc.mlcgames.combat;

/**
 * Canonical CraftEngine numeric attribute IDs used by the combat module.
 * Type tags such as {@link AttackType} and {@link ArmorType} intentionally do not belong here.
 */
public enum CombatAttribute {
    ATTACK("mlcgame:attack"),
    DEFENSE("mlcgame:defense"),
    CRITICAL_CHANCE("mlcgame:critical_chance"),
    CRITICAL_DAMAGE("mlcgame:critical_damage"),
    ARMOR_PENETRATION("mlcgame:armor_penetration"),
    DAMAGE_BONUS("mlcgame:damage_bonus"),
    HEALING_BONUS("mlcgame:healing_bonus");

    private final String id;

    CombatAttribute(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }
}
