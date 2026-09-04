/**
 * 文件说明：集中声明 MLCGame 将使用的 CraftEngine 数值属性 ID。
 * 攻击力、防御力、暴击和穿透等连续数值在此统一命名；攻击/防御类型仍由 PDC 标签管理。
 */
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
