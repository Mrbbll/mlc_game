package com.mlc.mlcgames.combat.config;

import com.mlc.mlcgames.combat.ArmorType;
import com.mlc.mlcgames.combat.AttackType;
import com.mlc.mlcgames.combat.DamageAffinity;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Locale;
import java.util.Optional;

/** Loads combat configuration without exposing Bukkit configuration to combat services. */
public final class CombatConfig {
    private final JavaPlugin plugin;
    private FileConfiguration configuration;

    public CombatConfig(JavaPlugin plugin) { this.plugin = plugin; reload(); }

    public void reload() {
        plugin.saveResource("combat.yml", false);
        configuration = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "combat.yml"));
    }

    public boolean enabled() { return configuration.getBoolean("enabled", true); }
    public Optional<AttackType> defaultAttackType() { return AttackType.parse(configuration.getString("defaults.attack-type")); }
    public Optional<ArmorType> defaultArmorType() { return ArmorType.parse(configuration.getString("defaults.armor-type")); }
    public double multiplier(DamageAffinity affinity) { return configuration.getDouble("multipliers." + affinity.name(), affinity.multiplier()); }

    public Optional<DamageAffinity> configuredAffinity(AttackType attackType, ArmorType armorType) {
        String value = configuration.getString("affinity." + attackType.name() + "." + armorType.name());
        if (value == null || value.isBlank()) return Optional.empty();
        try {
            return Optional.of(DamageAffinity.valueOf(value.trim().toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException exception) {
            plugin.getLogger().warning("Invalid combat affinity '" + value + "' for " + attackType + " -> " + armorType);
            return Optional.empty();
        }
    }
}
