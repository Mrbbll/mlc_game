package com.mlc.mlcgames.dungeongame.gamephase;

import com.mlc.mlcgames.Mlcgames;
import com.mlc.mlcgames.dungeongame.Dungeongame;
import com.mlc.mlcgames.dungeongame.managers.Room;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.util.Objects;

/** 初始化：从 dungeongame.yml 读取配置并组装房间列表。 */
public class dungeongameinit {
    public static void init() {
        Dungeongame.isstart = false;
        Dungeongame.grid_size = Math.max(1, Mlcgames.dungeonConfiguration.getInt("grid_size", 3));
        Dungeongame.floor_y = Mlcgames.dungeonConfiguration.getInt("floor_y", 60);
        Dungeongame.capture_entities = Mlcgames.dungeonConfiguration.getBoolean("capture_entities", false);
        Dungeongame.random_rotation = Mlcgames.dungeonConfiguration.getBoolean("random_rotation", true);

        Dungeongame.rooms.clear();
        Dungeongame.start_room = null;

        ConfigurationSection section = Mlcgames.dungeonConfiguration.getConfigurationSection("rooms");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                Room room = parseRoom(key, section.getConfigurationSection(key));
                if (room != null) Dungeongame.rooms.add(room);
            }
        }

        String startName = Mlcgames.dungeonConfiguration.getString("start_room", "");
        if (startName != null && !startName.isBlank()) {
            for (Room room : Dungeongame.rooms) {
                if (room.getName().equalsIgnoreCase(startName)) {
                    Dungeongame.start_room = room;
                    break;
                }
            }
            if (Dungeongame.start_room == null) {
                Bukkit.getLogger().warning("[Dungeongame] start_room 找不到: " + startName);
            }
        }

        Bukkit.getLogger().info("[Dungeongame] 已加载 " + Dungeongame.rooms.size() + " 个房间");
    }

    private static Room parseRoom(String name, ConfigurationSection cfg) {
        if (cfg == null) return null;
        String worldName = cfg.getString("world");
        if (worldName == null) {
            Bukkit.getLogger().warning("[Dungeongame] 房间 " + name + " 缺少 world");
            return null;
        }
        World world = getOrLoadWorld(worldName);
        if (world == null) {
            Bukkit.getLogger().warning("[Dungeongame] 房间 " + name + " 的世界未加载: " + worldName);
            return null;
        }
        Location a = parseCorner(cfg.getString("corner_1"));
        Location b = parseCorner(cfg.getString("corner_2"));
        if (a == null || b == null) {
            Bukkit.getLogger().warning("[Dungeongame] 房间 " + name + " 的 corner_1/corner_2 格式应为 \"x,y,z\"");
            return null;
        }
        return new Room(name, world, a, b);
    }

    private static Location parseCorner(String s) {
        if (s == null) return null;
        String[] p = s.split(",");
        if (p.length != 3) return null;
        try {
            return new Location(null,
                    Integer.parseInt(p[0].trim()),
                    Integer.parseInt(p[1].trim()),
                    Integer.parseInt(p[2].trim()));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** 模板世界不在线时，如果世界文件夹存在就自动加载它。 */
    private static World getOrLoadWorld(String name) {
        World world = Bukkit.getWorld(name);
        if (world != null) return world;
        File folder = new File(Objects.requireNonNull(Bukkit.getWorldContainer()), name);
        if (folder.exists() && folder.isDirectory()) {
            try {
                return new WorldCreator(name).createWorld();
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }
}
