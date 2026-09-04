/**
 * 文件说明：定义蔚蓝档案风格的攻击类型枚举，并提供大小写无关的安全字符串解析。
 * 该类型是离散标签，不属于 CraftEngine 的数值属性系统。
 */
package com.mlc.mlcgames.combat;

import java.util.Locale;
import java.util.Optional;

/** Blue Archive style attack attributes. */
public enum AttackType {
    NONE,
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
