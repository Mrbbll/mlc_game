package com.mlc.mlcgames.combat.weapon;

import com.mlc.mlcgames.combat.AttackType;
import com.mlc.mlcgames.combat.config.WeaponConfig;
import com.mlc.mlcgames.combat.integration.craftengine.CraftEngineItemResolver;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Optional;

/** Resolves attack types from MLCGame PDC data first, then an external-item ID mapping. */
public final class AttackTypeService {
    private final NamespacedKey attackTypeKey;
    private final NamespacedKey itemIdKey;
    private final Optional<AttackType> defaultAttackType;
    private final WeaponConfig weaponConfig;
    private final CraftEngineItemResolver craftEngineItemResolver;

    public AttackTypeService(JavaPlugin plugin, WeaponConfig weaponConfig, Optional<AttackType> defaultAttackType,
                             CraftEngineItemResolver craftEngineItemResolver) {
        attackTypeKey = new NamespacedKey(plugin, "attack_type");
        itemIdKey = new NamespacedKey(plugin, "item_id");
        this.weaponConfig = weaponConfig;
        this.defaultAttackType = defaultAttackType;
        this.craftEngineItemResolver = craftEngineItemResolver;
    }

    public Optional<AttackType> getAttackType(ItemStack item) {
        if (item == null || item.getType().isAir() || !item.hasItemMeta()) return defaultAttackType;
        ItemMeta meta = item.getItemMeta();
        Optional<AttackType> directType = AttackType.parse(meta.getPersistentDataContainer().get(attackTypeKey, PersistentDataType.STRING));
        if (directType.isPresent()) return directType;
        String itemId = craftEngineItemResolver.resolveItemId(item)
                .orElseGet(() -> meta.getPersistentDataContainer().get(itemIdKey, PersistentDataType.STRING));
        return weaponConfig.getAttackType(itemId).or(() -> defaultAttackType);
    }

    public Optional<AttackType> getAttackType(Entity entity) {
        return AttackType.parse(entity.getPersistentDataContainer().get(attackTypeKey, PersistentDataType.STRING));
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
    /** Stores an external/custom item ID used by {@code weapons.yml}. */
    public void setItemId(ItemStack item, String itemId) {
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(itemIdKey, PersistentDataType.STRING, itemId);
        item.setItemMeta(meta);
    }
    public void copyAttackTypeTo(Entity entity, AttackType attackType) {
        entity.getPersistentDataContainer().set(attackTypeKey, PersistentDataType.STRING, attackType.name());
    }
}
