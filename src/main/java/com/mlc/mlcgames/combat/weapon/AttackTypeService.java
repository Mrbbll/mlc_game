/**
 * 文件说明：解析并管理武器/伤害来源的攻击类型。
 * 解析优先级为直接 PDC 类型、CraftEngine 物品 ID 映射、PDC 备用物品 ID，最后才使用配置默认值。
 */
package com.mlc.mlcgames.combat.weapon;

import com.mlc.mlcgames.combat.AttackType;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Optional;

/** 从物品或伤害来源的 PDC 读取攻击类型。 */
public final class AttackTypeService {
    private final NamespacedKey attackTypeKey;
    private final AttackType defaultAttackType;

    public AttackTypeService(JavaPlugin plugin, AttackType defaultAttackType) {
        attackTypeKey = new NamespacedKey(plugin, "attack_type");
        this.defaultAttackType = defaultAttackType;
    }

    public AttackType getAttackType(ItemStack item) {
        if (item == null || item.getType().isAir() || !item.hasItemMeta()) return defaultAttackType;
        ItemMeta meta = item.getItemMeta();
        return AttackType.parse(meta.getPersistentDataContainer().get(attackTypeKey, PersistentDataType.STRING))
                .orElse(defaultAttackType);
    }

    public AttackType getAttackType(Entity entity) {
        return AttackType.parse(entity.getPersistentDataContainer().get(attackTypeKey, PersistentDataType.STRING))
                .orElse(AttackType.NONE);
    }

    /** 返回伤害来源是否保存过攻击类型；显式 NONE 也属于有效的投射物快照。 */
    public boolean hasAttackType(Entity entity) {
        return AttackType.parse(entity.getPersistentDataContainer().get(attackTypeKey, PersistentDataType.STRING)).isPresent();
    }

    public void setAttackType(ItemStack item, AttackType attackType) {
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(attackTypeKey, PersistentDataType.STRING, attackType.name());
        item.setItemMeta(meta);
    }
    public void clearAttackType(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().remove(attackTypeKey);
        item.setItemMeta(meta);
    }
    public void copyAttackTypeTo(Entity entity, AttackType attackType) {
        entity.getPersistentDataContainer().set(attackTypeKey, PersistentDataType.STRING, attackType.name());
    }
}
