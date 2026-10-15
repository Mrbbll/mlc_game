package com.mlc.mlcgames.party.core;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;

/** A fresh, validated configuration snapshot is used for each session. */
public final class PartyConfig {
    public final String id;
    public final YamlConfiguration rules;
    private final YamlConfiguration language;
    private final YamlConfiguration locations;

    public PartyConfig(JavaPlugin plugin, String id, String path) {
        this.id = id;
        rules = load(plugin, path + "/game.yml");
        language = load(plugin, path + "/language.yml");
        locations = load(plugin, path + "/locations.yml");
    }

    public static YamlConfiguration load(JavaPlugin plugin, String path) {
        File file = new File(plugin.getDataFolder(), path);
        if (!file.exists()) plugin.saveResource(path, false);
        YamlConfiguration config = new YamlConfiguration();
        try { config.load(file); }
        catch (Exception e) { throw new IllegalArgumentException(path + ": " + e.getMessage(), e); }
        if (config.getInt("schema-version") != 1) throw new IllegalArgumentException(path + ": schema-version 必须为 1");
        return config;
    }

    public String text(String key, Object... values) {
        String text = language.getString(key);
        if (text == null) throw new IllegalArgumentException(id + "/language.yml 缺少 " + key);
        for (int i = 0; i < values.length; i += 2) text = text.replace("{" + values[i] + "}", String.valueOf(values[i + 1]));
        return text;
    }

    public Component message(String key, Object... values) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(text(key, values));
    }

    public int integer(String key, int min, int max) {
        Object value = rules.get(key);
        if (!(value instanceof Number number) || !Double.isFinite(number.doubleValue()) || number.doubleValue() != number.intValue()
                || number.intValue() < min || number.intValue() > max) {
            throw new IllegalArgumentException(id + "/game.yml " + key + " 必须为 " + min + ".." + max + " 的整数");
        }
        return number.intValue();
    }

    public Set<Material> materials(String key) {
        Set<Material> result = EnumSet.noneOf(Material.class);
        for (String name : rules.getStringList(key)) {
            if (name.startsWith("#")) {
                NamespacedKey tagKey = NamespacedKey.fromString(name.substring(1));
                Tag<Material> tag = tagKey == null ? null : Bukkit.getTag(Tag.REGISTRY_BLOCKS, tagKey, Material.class);
                if (tag == null) throw new IllegalArgumentException(key + ": 未知方块标签 " + name);
                result.addAll(tag.getValues());
                continue;
            }
            Material material = Material.matchMaterial(name);
            if (material == null || !material.isBlock() || material.isAir()) throw new IllegalArgumentException(key + ": 非法方块 " + name);
            result.add(material);
        }
        if (result.isEmpty()) throw new IllegalArgumentException(key + " 不能为空");
        return result;
    }

    public double number(String key, double min, double max) {
        Object value = rules.get(key);
        if (!(value instanceof Number n) || !Double.isFinite(n.doubleValue()) || n.doubleValue() < min || n.doubleValue() > max) throw new IllegalArgumentException(key + " 必须为 " + min + ".." + max + " 的数值");
        return n.doubleValue();
    }

    public Material itemMaterial(String key) {
        Material value = Material.matchMaterial(rules.getString(key, ""));
        if (value == null || !value.isItem() || value.isAir()) throw new IllegalArgumentException(key + ": 非法物品类型");
        return value;
    }

    public ThresholdTable thresholds(String key) {
        List<ThresholdTable.Entry> entries = new ArrayList<>();
        for (Map<?, ?> row : rules.getMapList(key)) {
            if (!(row.get("min-players") instanceof Number min) || !(row.get("value") instanceof Number value)
                    || min.doubleValue() != min.intValue() || value.doubleValue() != value.intValue()) throw new IllegalArgumentException(key + " 必须包含整数 min-players 和 value");
            entries.add(new ThresholdTable.Entry(min.intValue(), value.intValue()));
        }
        return new ThresholdTable(entries);
    }

    public Arena arena(String name) {
        if (!rules.getBoolean("enabled")) throw new IllegalArgumentException(text("disabled"));
        ConfigurationSection map = locations.getConfigurationSection("maps." + name);
        if (map == null || !map.getBoolean("configured")) throw new IllegalArgumentException(text("map-unconfigured", "map", name));
        World world = Bukkit.getWorld(map.getString("world", ""));
        if (world == null) throw new IllegalArgumentException("maps." + name + ".world 未加载");
        Cuboid bounds = region(map, "bounds", world);
        long volume;
        try { volume = bounds.volume(); }
        catch (ArithmeticException e) { throw new IllegalArgumentException("地图区域体积溢出"); }
        if (volume > integer("limits.max-arena-blocks", 1, 10_000_000)) throw new IllegalArgumentException("地图区域超出 limits.max-arena-blocks");
        Map<String, Cuboid> regions = new LinkedHashMap<>();
        ConfigurationSection section = map.getConfigurationSection("regions");
        if (section != null) for (String key : section.getKeys(false)) {
            Cuboid region = region(section, key, world);
            if (!bounds.contains(region)) throw new IllegalArgumentException("regions." + key + " 超出地图区域");
            regions.put(key, region);
        }
        List<Location> spawns = new ArrayList<>();
        Set<String> usedSpawns = new HashSet<>();
        for (Map<?, ?> spawn : map.getMapList("points.spawns")) {
            Location location = point(spawn, world, "points.spawns");
            if (!bounds.contains(location.getBlockX(), location.getBlockY(), location.getBlockZ())) throw new IllegalArgumentException("出生点超出地图区域");
            if (!usedSpawns.add(location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ())) throw new IllegalArgumentException("points.spawns 存在重复方块位置");
            spawns.add(location);
        }
        if (spawns.size() < integer("players.max", 2, 64)) throw new IllegalArgumentException("points.spawns 数量必须达到 players.max，避免重叠出生");
        Location waiting = point(require(map, "points.waiting").getValues(false), world, "points.waiting");
        Location spectator = point(require(map, "points.spectator").getValues(false), world, "points.spectator");
        Location exit = point(require(map, "points.exit").getValues(false), world, "points.exit");
        if (bounds.contains(exit.getBlockX(), exit.getBlockY(), exit.getBlockZ()) || bounds.contains(waiting.getBlockX(), waiting.getBlockY(), waiting.getBlockZ())) throw new IllegalArgumentException("等待点和返回点必须位于比赛区域 bounds 外，避免恢复场地时困住玩家");
        Map<String, List<Location>> teamSpawns = new HashMap<>();
        ConfigurationSection teams = map.getConfigurationSection("points.team-spawns");
        if (teams != null) for (String team : teams.getKeys(false)) {
            List<Location> points = new ArrayList<>();
            for (Map<?, ?> entry : teams.getMapList(team)) {
                Location point = point(entry, world, "points.team-spawns." + team);
                if (!bounds.contains(point.getBlockX(), point.getBlockY(), point.getBlockZ())) throw new IllegalArgumentException("队伍出生点超出地图区域");
                points.add(point);
            }
            teamSpawns.put(team, List.copyOf(points));
        }
        Map<String, List<Location>> groups = new HashMap<>();
        ConfigurationSection pointGroups = map.getConfigurationSection("points.groups");
        if (pointGroups != null) for (String group : pointGroups.getKeys(false)) {
            List<Location> points = new ArrayList<>();
            Set<String> used = new HashSet<>();
            for (Map<?, ?> entry : pointGroups.getMapList(group)) {
                Location location = point(entry, world, "points.groups." + group);
                if (!bounds.contains(location.getBlockX(), location.getBlockY(), location.getBlockZ())) throw new IllegalArgumentException("特殊点位超出地图区域：" + group);
                String position = location.getX() + "," + location.getY() + "," + location.getZ();
                if (!used.add(position)) throw new IllegalArgumentException("特殊点位重复：" + group);
                points.add(location);
            }
            groups.put(group, List.copyOf(points));
        }
        return new Arena(world, bounds, Map.copyOf(regions), List.copyOf(spawns), waiting, spectator, exit, Map.copyOf(teamSpawns), Map.copyOf(groups));
    }

    private static Cuboid region(ConfigurationSection section, String path, World world) {
        Location first = point(require(section, path + ".corner-1").getValues(false), world, path + ".corner-1");
        Location second = point(require(section, path + ".corner-2").getValues(false), world, path + ".corner-2");
        for (Location loc : List.of(first, second)) {
            if (loc.getX() != loc.getBlockX() || loc.getY() != loc.getBlockY() || loc.getZ() != loc.getBlockZ()) throw new IllegalArgumentException(path + " 角点必须为整数方块坐标");
        }
        return Cuboid.between(first.getBlockX(), first.getBlockY(), first.getBlockZ(), second.getBlockX(), second.getBlockY(), second.getBlockZ());
    }

    private static ConfigurationSection require(ConfigurationSection section, String path) {
        ConfigurationSection result = section.getConfigurationSection(path);
        if (result == null) throw new IllegalArgumentException(path + " 缺失");
        return result;
    }

    private static Location point(Map<?, ?> map, World world, String path) {
        double x = coordinate(map, "x", path), y = coordinate(map, "y", path), z = coordinate(map, "z", path);
        if (Math.abs(x) > 29_999_984 || Math.abs(z) > 29_999_984 || y < world.getMinHeight() || y >= world.getMaxHeight()) throw new IllegalArgumentException(path + " 超出世界范围");
        float yaw = map.containsKey("yaw") ? (float) coordinate(map, "yaw", path) : 0;
        float pitch = map.containsKey("pitch") ? (float) coordinate(map, "pitch", path) : 0;
        if (!Float.isFinite(yaw) || !Float.isFinite(pitch) || Math.abs(pitch) > 90) throw new IllegalArgumentException(path + " 朝向非法或 pitch 超出 -90..90");
        return new Location(world, x, y, z, yaw, pitch);
    }

    private static double coordinate(Map<?, ?> map, String key, String path) {
        if (!(map.get(key) instanceof Number value) || !Double.isFinite(value.doubleValue())) throw new IllegalArgumentException(path + "." + key + " 必须配置有限数值");
        return value.doubleValue();
    }

    public record Arena(World world, Cuboid bounds, Map<String, Cuboid> regions, List<Location> spawns,
                        Location waiting, Location spectator, Location exit, Map<String, List<Location>> teamSpawns, Map<String, List<Location>> groups) {
        public boolean contains(Location loc) {
            return world.equals(loc.getWorld()) && bounds.contains(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        }
        public Cuboid region(String key) {
            Cuboid result = regions.get(key);
            if (result == null) throw new IllegalArgumentException("locations.yml 缺少 regions." + key);
            return result;
        }
        public List<Location> group(String key) {
            List<Location> result = groups.get(key);
            if (result == null || result.isEmpty()) throw new IllegalArgumentException("locations.yml 缺少 points.groups." + key + " 点位列表");
            return result;
        }
    }
}
