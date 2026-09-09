/**
 * 文件说明：统一读写实体 PDC 中的 mlcgames:armor_type 防御类型标签。
 * 因此同一种 Minecraft 实体可以拥有不同的防御类型，且数据可随实体保存。
 */
package com.mlc.mlcgames.combat.entity;

import com.mlc.mlcgames.combat.ArmorType;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

/** Owns PDC access for per-entity Blue Archive armour types. */
public final class ArmorTypeService {
    private final NamespacedKey armorTypeKey;

    public ArmorTypeService(JavaPlugin plugin) {
        armorTypeKey = new NamespacedKey(plugin, "armor_type");
    }

    public ArmorType getArmorType(Entity entity) {
        if (!(entity instanceof LivingEntity livingEntity) || livingEntity.getEquipment() == null) {
            return ArmorType.NONE;
        }
        ItemStack helmet = livingEntity.getEquipment().getHelmet();
        if (helmet == null || helmet.getType().isAir()) {
            return ArmorType.NONE;
        }
        return ArmorType.parse(helmet.getItemMeta().getPersistentDataContainer().get(armorTypeKey, PersistentDataType.STRING))
                .orElse(ArmorType.NONE);
    }
    public boolean hasArmorType(Entity entity) { return getArmorType(entity) != ArmorType.NONE; }
    public boolean setArmorType(Entity entity, ArmorType armorType) {
        ItemStack helmet = helmet(entity);
        if (helmet == null) return false;
        var meta = helmet.getItemMeta();
        meta.getPersistentDataContainer().set(armorTypeKey, PersistentDataType.STRING, armorType.name());
        helmet.setItemMeta(meta);
        ((LivingEntity) entity).getEquipment().setHelmet(helmet);
        return true;
    }
    public boolean clearArmorType(Entity entity) {
        ItemStack helmet = helmet(entity);
        if (helmet == null) return false;
        var meta = helmet.getItemMeta();
        meta.getPersistentDataContainer().remove(armorTypeKey);
        helmet.setItemMeta(meta);
        ((LivingEntity) entity).getEquipment().setHelmet(helmet);
        return true;
    }

    private ItemStack helmet(Entity entity) {
        if (!(entity instanceof LivingEntity livingEntity) || livingEntity.getEquipment() == null) return null;
        ItemStack helmet = livingEntity.getEquipment().getHelmet();
        return helmet == null || helmet.getType().isAir() ? null : helmet;
    }
}
