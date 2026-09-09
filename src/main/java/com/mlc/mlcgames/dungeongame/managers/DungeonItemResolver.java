package com.mlc.mlcgames.dungeongame.managers;

import net.momirealms.craftengine.bukkit.item.BukkitItem;
import net.momirealms.craftengine.bukkit.item.BukkitItemManager;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/** 把装备配置 ID 转成物品；同时支持原版材质名和 CraftEngine namespaced ID。 */
final class DungeonItemResolver {
    private final JavaPlugin plugin;
    private final Set<String> warnedIds = new HashSet<>();

    DungeonItemResolver(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /** 空 ID 表示该装备槽应保持为空。解析失败也返回 null，但每个 ID 只警告一次。 */
    ItemStack resolve(String rawId) {
        if (rawId == null || rawId.isBlank()) return null;
        String id = rawId.trim();

        Material vanilla = Material.matchMaterial(id);
        if (vanilla == null) vanilla = Material.matchMaterial(id.toUpperCase(Locale.ROOT));
        if (vanilla != null && !vanilla.isAir()) return new ItemStack(vanilla);

        if (Bukkit.getPluginManager().getPlugin("CraftEngine") != null && id.contains(":")) {
            try {
                BukkitItem wrapped = BukkitItemManager.instance().createCustomWrappedItem(Key.of(id), null);
                ItemStack result = wrapped == null ? null : wrapped.getBukkitItem();
                if (result != null && !result.getType().isAir()) return result.clone();
            } catch (RuntimeException | LinkageError exception) {
                warn(id, "CraftEngine failed to build it: " + exception.getClass().getSimpleName());
                return null;
            }
        }

        warn(id, "it is neither a vanilla Material nor an available CraftEngine item");
        return null;
    }

    private void warn(String id, String reason) {
        if (warnedIds.add(id)) {
            plugin.getLogger().warning("Unable to resolve dungeon mob equipment '" + id + "': " + reason);
        }
    }
}
