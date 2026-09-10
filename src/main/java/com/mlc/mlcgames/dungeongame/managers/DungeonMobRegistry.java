package com.mlc.mlcgames.dungeongame.managers;

import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 加载 {@code plugins/mlcgames/dungeongame/mobs/<关卡>_<环境>.yml}。
 *
 * <p>reload 使用事务式替换：所有文件都成功解析后才发布新快照；任一文件错误时，
 * 已在运行的游戏和旧注册表都不会受到半成品配置影响。</p>
 */
final class DungeonMobRegistry {
    private static final Pattern FILE_NAME = Pattern.compile("^(\\d+)_(\\d+)\\.yml$", Pattern.CASE_INSENSITIVE);
    private static final List<String> DEFAULT_PACKS = List.of("1_1.yml", "2_1.yml", "3_1.yml");
    private static final Map<String, Attribute> ATTRIBUTES = Map.ofEntries(
            Map.entry("max-health", Attribute.MAX_HEALTH),
            Map.entry("armor", Attribute.ARMOR),
            Map.entry("armor-toughness", Attribute.ARMOR_TOUGHNESS),
            Map.entry("attack-damage", Attribute.ATTACK_DAMAGE),
            Map.entry("movement-speed", Attribute.MOVEMENT_SPEED),
            Map.entry("knockback-resistance", Attribute.KNOCKBACK_RESISTANCE),
            Map.entry("follow-range", Attribute.FOLLOW_RANGE)
    );

    private final JavaPlugin plugin;
    private final File directory;
    private Map<PackKey, DungeonMobPack> packs = Map.of();

    DungeonMobRegistry(JavaPlugin plugin) {
        this.plugin = plugin;
        directory = new File(plugin.getDataFolder(), "dungeongame/mobs");
        installDefaultPacks();
        reload();
    }

    /** 重新读取磁盘文件；成功时返回环境包数量。 */
    int reload() {
        Map<PackKey, DungeonMobPack> loaded = new HashMap<>();
        File[] files = directory.listFiles(file -> file.isFile()
                && file.getName().toLowerCase(Locale.ROOT).endsWith(".yml"));
        if (files == null) {
            throw new IllegalArgumentException("Unable to list dungeon mob directory: " + directory);
        }

        java.util.Arrays.sort(files, java.util.Comparator.comparing(File::getName));
        for (File file : files) {
            Matcher matcher = FILE_NAME.matcher(file.getName());
            if (!matcher.matches()) {
                throw new IllegalArgumentException("Mob file '" + file.getName()
                        + "' must use <dungeon-level>_<environment-set>.yml");
            }
            int dungeonLevel = Integer.parseInt(matcher.group(1));
            int environmentSet = Integer.parseInt(matcher.group(2));
            if (dungeonLevel < 1 || dungeonLevel > DungeonSession.DUNGEON_LEVELS) {
                throw new IllegalArgumentException("Mob file '" + file.getName()
                        + "' has dungeon level outside 1-" + DungeonSession.DUNGEON_LEVELS);
            }

            PackKey key = new PackKey(dungeonLevel, environmentSet);
            DungeonMobPack previous = loaded.put(key, parse(file, dungeonLevel, environmentSet));
            if (previous != null) {
                throw new IllegalArgumentException("Duplicate dungeon mob pack " + dungeonLevel + "_" + environmentSet);
            }
        }
        if (loaded.isEmpty()) {
            throw new IllegalArgumentException("No dungeon mob packs found in " + directory);
        }

        packs = Map.copyOf(loaded);
        plugin.getLogger().info("Loaded " + packs.size() + " dungeon mob environment packs from " + directory);
        return packs.size();
    }

    boolean hasPack(int dungeonLevel, int environmentSet) {
        return packs.containsKey(new PackKey(dungeonLevel, environmentSet));
    }

    DungeonMobPack requirePack(int dungeonLevel, int environmentSet) {
        DungeonMobPack pack = packs.get(new PackKey(dungeonLevel, environmentSet));
        if (pack == null) {
            throw new IllegalStateException("Missing mob pack dungeongame/mobs/"
                    + dungeonLevel + "_" + environmentSet + ".yml");
        }
        return pack;
    }

    private DungeonMobPack parse(File file, int dungeonLevel, int environmentSet) {
        YamlConfiguration yaml = new YamlConfiguration();
        try {
            yaml.load(file);
        } catch (IOException | InvalidConfigurationException exception) {
            throw new IllegalArgumentException("Unable to load mob file '" + file.getName() + "'", exception);
        }

        ConfigurationSection mobs = yaml.getConfigurationSection("mobs");
        if (mobs == null || mobs.getKeys(false).isEmpty()) {
            throw new IllegalArgumentException("Mob file '" + file.getName() + "' has no mobs section");
        }

        EnumMap<DungeonSession.MarkerKind, List<DungeonMobDefinition>> pools =
                new EnumMap<>(DungeonSession.MarkerKind.class);
        for (DungeonSession.MarkerKind kind : DungeonSession.MarkerKind.values()) {
            pools.put(kind, new ArrayList<>());
        }
        for (String id : mobs.getKeys(false)) {
            ConfigurationSection section = mobs.getConfigurationSection(id);
            if (section == null) {
                throw invalid(file, id, "must be a configuration section");
            }
            DungeonMobDefinition definition = parseDefinition(file, id, section);
            pools.get(definition.category()).add(definition);
        }
        for (DungeonSession.MarkerKind kind : DungeonSession.MarkerKind.values()) {
            if (pools.get(kind).isEmpty()) {
                throw new IllegalArgumentException("Mob file '" + file.getName() + "' has no "
                        + kind.name().toLowerCase(Locale.ROOT) + " definition");
            }
            pools.put(kind, List.copyOf(pools.get(kind)));
        }
        return new DungeonMobPack(dungeonLevel, environmentSet, Map.copyOf(pools));
    }

    private DungeonMobDefinition parseDefinition(File file, String id, ConfigurationSection section) {
        DungeonSession.MarkerKind category;
        try {
            category = DungeonSession.MarkerKind.valueOf(
                    requiredString(file, id, section, "category").toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw invalid(file, id, "category must be normal, elite or boss");
        }

        EntityType entityType;
        try {
            entityType = EntityType.valueOf(
                    requiredString(file, id, section, "entity-type").toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw invalid(file, id, "entity-type is not a Bukkit EntityType");
        }
        if (!entityType.isAlive()) throw invalid(file, id, "entity-type must be a living entity");

        int weight = section.getInt("weight", 1);
        if (weight < 1) throw invalid(file, id, "weight must be at least 1");
        String displayName = section.getString("name", "");
        boolean nameVisible = section.getBoolean("name-visible", !displayName.isBlank());
        boolean scaleWithDifficulty = section.getBoolean("scale-with-difficulty", true);
        Map<Attribute, Double> attributes = parseAttributes(file, id, section.getConfigurationSection("attributes"));
        DungeonMobDefinition.Equipment equipment = parseEquipment(
                file, id, section.getConfigurationSection("equipment"));
        return new DungeonMobDefinition(id, category, entityType, weight, displayName,
                nameVisible, scaleWithDifficulty, attributes, equipment);
    }

    private Map<Attribute, Double> parseAttributes(File file, String id, ConfigurationSection section) {
        if (section == null) return Map.of();
        Map<Attribute, Double> values = new HashMap<>();
        for (String key : section.getKeys(false)) {
            Attribute attribute = ATTRIBUTES.get(key.toLowerCase(Locale.ROOT));
            if (attribute == null) {
                throw invalid(file, id, "unknown attribute '" + key + "'");
            }
            double value = section.getDouble(key, Double.NaN);
            if (!Double.isFinite(value) || value < 0.0) {
                throw invalid(file, id, "attribute '" + key + "' must be a finite value >= 0");
            }
            if ((attribute == Attribute.MAX_HEALTH || attribute == Attribute.MOVEMENT_SPEED) && value <= 0.0) {
                throw invalid(file, id, "attribute '" + key + "' must be greater than 0");
            }
            if (attribute == Attribute.KNOCKBACK_RESISTANCE && value > 1.0) {
                throw invalid(file, id, "knockback-resistance must be between 0 and 1");
            }
            values.put(attribute, value);
        }
        return Map.copyOf(values);
    }

    private DungeonMobDefinition.Equipment parseEquipment(
            File file, String id, ConfigurationSection section) {
        if (section == null) {
            // 没有 equipment 段时延续旧行为：自动选择完整 CraftEngine 像素护甲套装。
            return new DungeonMobDefinition.Equipment("", "", "", "", "", "", true);
        }
        return new DungeonMobDefinition.Equipment(
                section.getString("helmet", ""),
                section.getString("chestplate", ""),
                section.getString("leggings", ""),
                section.getString("boots", ""),
                section.getString("main-hand", ""),
                section.getString("off-hand", ""),
                section.getBoolean("auto-pixel-armor", false));
    }

    private static String requiredString(File file, String id, ConfigurationSection section, String key) {
        String value = section.getString(key);
        if (value == null || value.isBlank()) throw invalid(file, id, "missing " + key);
        return value.trim();
    }

    private static IllegalArgumentException invalid(File file, String id, String message) {
        return new IllegalArgumentException("Mob '" + id + "' in '" + file.getName() + "' " + message);
    }

    private void installDefaultPacks() {
        try {
            Files.createDirectories(directory.toPath());
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to create dungeon mob directory: " + directory, exception);
        }
        for (String fileName : DEFAULT_PACKS) {
            File target = new File(directory, fileName);
            if (!target.exists()) {
                plugin.saveResource("dungeongame/mobs/" + fileName, false);
            }
        }
    }

    private record PackKey(int dungeonLevel, int environmentSet) { }
}
