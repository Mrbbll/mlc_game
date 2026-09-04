/**
 * 文件说明：定义攻击类型与防御类型相遇后的语义结果及默认倍率。
 * 实际倍率可由 combat.yml 覆盖，保留语义枚举可供后续显示 Weak、Resist 等反馈。
 */
package com.mlc.mlcgames.combat;

/** Semantic result of an attack-type and armour-type match-up. */
public enum DamageAffinity {
    WEAK(2.0D), EFFECTIVE(1.5D), NORMAL(1.0D), RESIST(0.5D);

    private final double multiplier;

    DamageAffinity(double multiplier) { this.multiplier = multiplier; }

    public double multiplier() { return multiplier; }
}
