/**
 * 文件说明：检测 CraftEngine 插件是否已加载并处于启用状态。
 * 其他 CE 适配类通过它安全降级，避免 CraftEngine 缺失时影响普通战斗。
 */
package com.mlc.mlcgames.combat.integration.craftengine;

import org.bukkit.Bukkit;

/** Runtime availability check that keeps CraftEngine optional for the combat core. */
public final class CraftEngineHook {
    public boolean isAvailable() {
        var plugin = Bukkit.getPluginManager().getPlugin("CraftEngine");
        return plugin != null && plugin.isEnabled();
    }
}
