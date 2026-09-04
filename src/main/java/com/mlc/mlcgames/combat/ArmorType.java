/**
 * 文件说明：定义蔚蓝档案风格的防御类型枚举，并提供大小写无关的安全字符串解析。
 * 它与 Minecraft 原版护甲值无关，实际值由实体 PDC 单独保存。
 */
package com.mlc.mlcgames.combat;

import java.util.Locale;
import java.util.Optional;

/** Blue Archive style defensive attributes; unrelated to vanilla armour. */
public enum ArmorType {
    NONE,
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
