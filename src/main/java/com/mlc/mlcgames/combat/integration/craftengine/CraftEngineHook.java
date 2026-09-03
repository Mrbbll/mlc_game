package com.mlc.mlcgames.combat.integration.craftengine;

import org.bukkit.Bukkit;

/** Runtime availability check that keeps CraftEngine optional for the combat core. */
public final class CraftEngineHook {
    public boolean isAvailable() {
        var plugin = Bukkit.getPluginManager().getPlugin("CraftEngine");
        return plugin != null && plugin.isEnabled();
    }
}
