package com.mlc.mlcgames.combat.integration.craftengine;

import net.momirealms.craftengine.bukkit.api.CraftEngineItems;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Optional;

/** Resolves a CraftEngine custom item ID using the 26.8.2 public Bukkit API. */
public final class CraftEngineItemResolver {
    private final JavaPlugin plugin;
    private final CraftEngineHook hook;
    private boolean failureReported;

    public CraftEngineItemResolver(JavaPlugin plugin, CraftEngineHook hook) {
        this.plugin = plugin;
        this.hook = hook;
    }

    public Optional<String> resolveItemId(ItemStack item) {
        if (item == null || item.getType().isAir() || !hook.isAvailable()) {
            return Optional.empty();
        }
        try {
            return Optional.ofNullable(CraftEngineItems.getCustomItemId(item)).map(key -> key.asString());
        } catch (LinkageError | RuntimeException exception) {
            if (!failureReported) {
                failureReported = true;
                plugin.getLogger().warning("CraftEngine item integration is unavailable; falling back to MLCGame PDC item IDs. "
                        + exception.getClass().getSimpleName());
            }
            return Optional.empty();
        }
    }
}
