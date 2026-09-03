package com.mlc.mlcgames.combat.config;

import com.mlc.mlcgames.combat.AttackType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/** Maps external item identifiers, including CraftEngine identifiers, to attack types. */
public final class WeaponConfig {
    private final JavaPlugin plugin;
    private final Map<String, AttackType> weaponTypes = new HashMap<>();

    public WeaponConfig(JavaPlugin plugin) { this.plugin = plugin; reload(); }

    public void reload() {
        plugin.saveResource("weapons.yml", false);
        FileConfiguration configuration = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "weapons.yml"));
        weaponTypes.clear();
        ConfigurationSection weapons = configuration.getConfigurationSection("weapons");
        if (weapons == null) return;
        for (String itemId : weapons.getKeys(false)) {
            String value = weapons.getString(itemId + ".attack-type");
            AttackType.parse(value).ifPresentOrElse(
                    type -> weaponTypes.put(itemId.toLowerCase(Locale.ROOT), type),
                    () -> plugin.getLogger().warning("Invalid attack type for weapon '" + itemId + "': " + value));
        }
    }

    public Optional<AttackType> getAttackType(String itemId) {
        if (itemId == null || itemId.isBlank()) return Optional.empty();
        return Optional.ofNullable(weaponTypes.get(itemId.toLowerCase(Locale.ROOT)));
    }
}
