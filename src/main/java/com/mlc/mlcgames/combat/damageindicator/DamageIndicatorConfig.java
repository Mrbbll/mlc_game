/**
 * 文件说明：读取 damage-indicator.yml 中的伤害浮字开关、显示文本、可见距离与动画参数。
 * 此配置独立于 CraftEngine，不会读取或修改 CraftEngine 的 config.yml。
 */
package com.mlc.mlcgames.combat.damageindicator;

import com.mlc.mlcgames.combat.DamageAffinity;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.EnumMap;
import java.util.Map;

/** Immutable-at-use view of the MLCGame floating damage indicator configuration. */
public final class DamageIndicatorConfig {
    private static final Map<DamageAffinity, String> DEFAULT_FORMATS = createDefaultFormats();

    private final JavaPlugin plugin;
    private FileConfiguration configuration;

    public DamageIndicatorConfig(JavaPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        plugin.saveResource("damage-indicator.yml", false);
        configuration = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "damage-indicator.yml"));
    }

    public boolean enabled() {
        return configuration.getBoolean("enabled", true);
    }

    public double viewDistance() {
        return Math.max(1.0d, configuration.getDouble("view-distance", 32.0d));
    }

    public double heightOffset() {
        return configuration.getDouble("position.height-offset", 0.55d);
    }

    public double horizontalSpread() {
        return Math.max(0.0d, configuration.getDouble("position.horizontal-spread", 0.35d));
    }

    public String format(DamageAffinity affinity) {
        return configuration.getString("formats." + affinity.name(), DEFAULT_FORMATS.get(affinity));
    }

    public String missFormat() {
        return configuration.getString("miss-format", "<white><bold>MISS</bold>");
    }

    public float spawnScale() {
        return (float) configuration.getDouble("animation.spawn-scale", 0.10d);
    }

    public float popScale() {
        return (float) configuration.getDouble("animation.pop-scale", 1.25d);
    }

    public float settleScale() {
        return (float) configuration.getDouble("animation.settle-scale", 1.00d);
    }

    public long popDelay() {
        return Math.max(0L, configuration.getLong("animation.pop-delay", 1L));
    }

    public long settleDelay() {
        return Math.max(0L, configuration.getLong("animation.settle-delay", 5L));
    }

    public long shrinkDelay() {
        return Math.max(0L, configuration.getLong("animation.shrink-delay", 16L));
    }

    public long removeDelay() {
        return Math.max(1L, configuration.getLong("animation.remove-delay", 19L));
    }

    private static Map<DamageAffinity, String> createDefaultFormats() {
        Map<DamageAffinity, String> formats = new EnumMap<>(DamageAffinity.class);
        formats.put(DamageAffinity.WEAK, "<red><bold>WEAK!</bold> <white><damage></white>");
        formats.put(DamageAffinity.EFFECTIVE, "<gold><bold>EFFECTIVE!</bold> <white><damage></white>");
        formats.put(DamageAffinity.NORMAL, "<gray>NORMAL <white><damage></white>");
        formats.put(DamageAffinity.RESIST, "<blue><bold>RESIST</bold> <white><damage></white>");
        return formats;
    }
}
