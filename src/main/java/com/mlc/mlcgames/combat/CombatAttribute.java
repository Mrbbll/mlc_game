/**
 * 文件说明：集中声明 MLCGame 将使用的 CraftEngine 数值属性 ID。
 * 命中、闪避、暴击、增伤和减伤等连续数值在此统一命名；攻击/防御类型仍由 PDC 标签管理。
 */
package com.mlc.mlcgames.combat;

/**
 * Canonical CraftEngine numeric attribute IDs used by the combat module.
 * Type tags such as {@link AttackType} and {@link ArmorType} intentionally do not belong here.
 */
public enum CombatAttribute {
    ACCURACY("mlcgame:accuracy"),
    EVASION("mlcgame:evasion"),
    CRITICAL_CHANCE("mlcgame:critical_chance"),
    CRITICAL_RESISTANCE("mlcgame:critical_resistance"),
    CRITICAL_MULTIPLIER("mlcgame:critical_multiplier"),
    DAMAGE_BONUS("mlcgame:damage_bonus"),
    DAMAGE_REDUCTION("mlcgame:damage_reduction");

    private final String id;

    CombatAttribute(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }
}
