/**
 * 文件说明：维护攻击类型 × 防御类型的集中相性矩阵，并从 combat.yml 读取覆盖值和倍率。
 * 监听器不直接编写克制判断，避免相性规则分散在多个事件处理器中。
 */
package com.mlc.mlcgames.combat;

import com.mlc.mlcgames.combat.config.CombatConfig;

import java.util.EnumMap;
import java.util.Map;

/** Central match-up matrix. Config entries override the built-in BA-style defaults. */
public final class TypeEffectivenessService {
    private final CombatConfig config;
    private final Map<AttackType, Map<ArmorType, DamageAffinity>> defaults = new EnumMap<>(AttackType.class);

    public TypeEffectivenessService(CombatConfig config) {
        this.config = config;
        put(AttackType.EXPLOSIVE, ArmorType.LIGHT, DamageAffinity.WEAK);
        put(AttackType.EXPLOSIVE, ArmorType.HEAVY, DamageAffinity.NORMAL);
        put(AttackType.EXPLOSIVE, ArmorType.SPECIAL, DamageAffinity.RESIST);
        put(AttackType.EXPLOSIVE, ArmorType.ELASTIC, DamageAffinity.RESIST);
        put(AttackType.PIERCING, ArmorType.LIGHT, DamageAffinity.RESIST);
        put(AttackType.PIERCING, ArmorType.HEAVY, DamageAffinity.WEAK);
        put(AttackType.PIERCING, ArmorType.SPECIAL, DamageAffinity.NORMAL);
        put(AttackType.PIERCING, ArmorType.ELASTIC, DamageAffinity.NORMAL);
        put(AttackType.MYSTIC, ArmorType.LIGHT, DamageAffinity.NORMAL);
        put(AttackType.MYSTIC, ArmorType.HEAVY, DamageAffinity.RESIST);
        put(AttackType.MYSTIC, ArmorType.SPECIAL, DamageAffinity.WEAK);
        put(AttackType.MYSTIC, ArmorType.ELASTIC, DamageAffinity.NORMAL);
        put(AttackType.SONIC, ArmorType.LIGHT, DamageAffinity.NORMAL);
        put(AttackType.SONIC, ArmorType.HEAVY, DamageAffinity.RESIST);
        put(AttackType.SONIC, ArmorType.SPECIAL, DamageAffinity.EFFECTIVE);
        put(AttackType.SONIC, ArmorType.ELASTIC, DamageAffinity.WEAK);
    }

    public DamageAffinity resolve(AttackType attackType, ArmorType armorType) {
        if (attackType == AttackType.NONE || armorType == ArmorType.NONE) {
            return DamageAffinity.NORMAL;
        }
        return config.configuredAffinity(attackType, armorType).orElse(defaults.get(attackType).get(armorType));
    }
    public double multiplier(DamageAffinity affinity) { return config.multiplier(affinity); }
    private void put(AttackType attackType, ArmorType armorType, DamageAffinity affinity) {
        defaults.computeIfAbsent(attackType, ignored -> new EnumMap<>(ArmorType.class)).put(armorType, affinity);
    }
}
