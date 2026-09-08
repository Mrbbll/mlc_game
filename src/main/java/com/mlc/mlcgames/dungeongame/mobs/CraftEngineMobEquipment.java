package com.mlc.mlcgames.dungeongame.mobs;

import com.mlc.mlcgames.Mlcgames;
import com.mlc.mlcgames.dungeongame.DungeonDifficulty;
import net.momirealms.craftengine.bukkit.item.BukkitItem;
import net.momirealms.craftengine.bukkit.item.BukkitItemManager;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

/** Equips difficulty-specific CraftEngine items whose CE definitions own the attributes. */
public final class CraftEngineMobEquipment {
    private boolean warned;

    public void equip(LivingEntity entity, DungeonDifficulty difficulty) {
        EntityEquipment equipment = entity.getEquipment();
        if (equipment == null) return;
        String path = "difficulty." + difficulty.name().toLowerCase() + ".armor.";
        ItemStack helmet = build(Mlcgames.dungeonConfiguration.getString(path + "helmet", ""));
        ItemStack chestplate = build(Mlcgames.dungeonConfiguration.getString(path + "chestplate", ""));
        if (helmet != null) equipment.setHelmet(helmet);
        if (chestplate != null) equipment.setChestplate(chestplate);
        equipment.setHelmetDropChance(0.0F);
        equipment.setChestplateDropChance(0.0F);
    }

    private ItemStack build(String id) {
        if (id == null || id.isBlank()) return null;
        if (Bukkit.getPluginManager().getPlugin("CraftEngine") == null) {
            warn("CraftEngine is unavailable; dungeon mob armour was not equipped");
            return null;
        }
        try {
            BukkitItem item = BukkitItemManager.instance().createCustomWrappedItem(Key.of(id), null);
            ItemStack result = item.getBukkitItem();
            if (result == null || result.getType().isAir()) warn("Unknown CraftEngine dungeon armour item: " + id);
            return result == null ? null : result.clone();
        } catch (RuntimeException | LinkageError exception) {
            warn("Unable to build CraftEngine dungeon armour item '" + id + "': " + exception.getClass().getSimpleName());
            return null;
        }
    }

    private void warn(String message) {
        if (warned) return;
        warned = true;
        Mlcgames.instance.getLogger().warning(message);
    }
}
