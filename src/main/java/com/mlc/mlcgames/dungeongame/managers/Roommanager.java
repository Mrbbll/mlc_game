package com.mlc.mlcgames.dungeongame.managers;

import com.mlc.mlcgames.dungeongame.rooms.Room;
import com.mlc.mlcgames.dungeongame.rooms.RoomType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import com.sk89q.worldedit.WorldEdit;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Roommanager {
    private static final Pattern SCHEMATIC_NAME = Pattern.compile("^(\\d+)_(\\d+)_([a-zA-Z]+)_(\\d+)\\.schem$", Pattern.CASE_INSENSITIVE);
    private static final Pattern BRIDGE_SCHEMATIC_NAME = Pattern.compile("^(\\d+)_(\\d+)_bridge_([xz])_(\\d+)\\.schem$", Pattern.CASE_INSENSITIVE);
    public static final Map<Integer, List<Room>> NormanlroomMap = new HashMap<>();
    public static final Map<Integer, List<Room>> SpecialroomMap = new HashMap<>();
    public static final Map<Integer, List<Room>> EndroomMap = new HashMap<>();
    public static final Map<Integer, List<Room>> StartroomMap = new HashMap<>();
    public static final Map<Integer, List<Room>> BossroomMap = new HashMap<>();
    public static final Map<Integer, List<Room>> BridgeXroomMap = new HashMap<>();
    public static final Map<Integer, List<Room>> BridgeZroomMap = new HashMap<>();

    public static Room getRoomNalmanroomlist(int floortype){
        return getNormalRoom(floortype, new Random());
    }

    public static Room getRoomSpecialroomlist(int floortype){
        return getSpecialRoom(floortype, new Random());
    }

    public static Room getRoomEndroomlist(int floortype){
        return getEndRoom(floortype, new Random());
    }

    public static Room getRoomStartroomlist(int floortype){
        return getStartRoom(floortype, new Random());
    }

    public static Room getRoomBossroomlist(int floortype){
        return getBossRoom(floortype, new Random());
    }

    public static Room getNormalRoom(int floorType, Random random) { return pick(NormanlroomMap, floorType, random, "normal"); }
    public static Room getSpecialRoom(int floorType, Random random) { return pick(SpecialroomMap, floorType, random, "special"); }
    public static Room getEndRoom(int floorType, Random random) { return pick(EndroomMap, floorType, random, "end"); }
    public static Room getStartRoom(int floorType, Random random) { return pick(StartroomMap, floorType, random, "start"); }
    public static Room getBossRoom(int floorType, Random random) { return pick(BossroomMap, floorType, random, "boss"); }
    public static Room getBridgeRoom(int floorType, BridgeAxis axis, Random random) {
        return pick(axis == BridgeAxis.X ? BridgeXroomMap : BridgeZroomMap, floorType, random, "bridge_" + axis.name().toLowerCase(Locale.ROOT));
    }

    private static Room pick(Map<Integer, List<Room>> rooms, int floorType, Random random, String kind) {
        List<Room> choices = rooms.get(floorType);
        if (choices == null || choices.isEmpty()) {
            throw new IllegalStateException("No " + kind + " room template is registered for floor " + floorType);
        }
        return choices.get(random.nextInt(choices.size()));
    }

    /**
     * Loads template cuboids from dungeongame.yml. A missing rooms section is
     * valid while the server owner is still preparing templates.
     *
     * @return number of templates that were loaded
     */
    public static int loadTemplates(FileConfiguration configuration) {
        clearTemplates();
        ConfigurationSection rooms = configuration.getConfigurationSection("rooms");
        if (rooms == null) return 0;

        int loaded = 0;
        for (String name : rooms.getKeys(false)) {
            ConfigurationSection room = rooms.getConfigurationSection(name);
            if (room == null) continue;

            int floor = room.getInt("floor", 1);
            RoomType type;
            try {
                type = RoomType.valueOf(require(room, "type"));
            } catch (IllegalArgumentException exception) {
                throw new IllegalArgumentException("Room '" + name + "' has an unknown type", exception);
            }
            World sourceWorld = Bukkit.getWorld(require(room, "world"));
            if (sourceWorld == null) {
                throw new IllegalArgumentException("Room '" + name + "' references a world that is not loaded");
            }

            Room template = new Room(type, name, sourceWorld,
                    parseLocation(sourceWorld, require(room, "corner_1"), name, "corner_1"),
                    parseLocation(sourceWorld, require(room, "corner_2"), name, "corner_2"), loaded + 1);
            poolFor(type).computeIfAbsent(floor, ignored -> new ArrayList<>()).add(template);
            loaded++;
        }
        return loaded;
    }

    public static void clearTemplates() {
        NormanlroomMap.clear();
        SpecialroomMap.clear();
        EndroomMap.clear();
        StartroomMap.clear();
        BossroomMap.clear();
        BridgeXroomMap.clear();
        BridgeZroomMap.clear();
    }

    /**
     * Reads FAWE's schematic directory using floor_set_type_variant.schem.
     * Example: 1_1_shop_1.schem is a floor-one, set-one Shop variant.
     */
    public static int loadSchematicTemplates(int dungeonSet) {
        return loadSchematicTemplates(WorldEdit.getInstance().getSchematicsFolderPath().toFile(), dungeonSet);
    }

    static int loadSchematicTemplates(File directory, int dungeonSet) {
        clearTemplates();
        if (!directory.isDirectory()) {
            throw new IllegalArgumentException("FAWE schematic directory does not exist: " + directory);
        }

        int[] loaded = {0};
        try (var files = Files.list(directory.toPath())) {
            files.filter(Files::isRegularFile).forEach(path -> {
                String fileName = path.getFileName().toString();
                Matcher bridgeMatcher = BRIDGE_SCHEMATIC_NAME.matcher(fileName);
                if (bridgeMatcher.matches()) {
                    int floor = Integer.parseInt(bridgeMatcher.group(1));
                    int fileSet = Integer.parseInt(bridgeMatcher.group(2));
                    if (fileSet != dungeonSet) return;
                    BridgeAxis axis = BridgeAxis.valueOf(bridgeMatcher.group(3).toUpperCase(Locale.ROOT));
                    Room bridge = new Room(RoomType.Bridge, fileName, path.toFile(), loaded[0] + 1);
                    (axis == BridgeAxis.X ? BridgeXroomMap : BridgeZroomMap)
                            .computeIfAbsent(floor, ignored -> new ArrayList<>()).add(bridge);
                    loaded[0]++;
                    return;
                }

                Matcher matcher = SCHEMATIC_NAME.matcher(fileName);
                if (!matcher.matches()) return;

                int floor = Integer.parseInt(matcher.group(1));
                int fileSet = Integer.parseInt(matcher.group(2));
                if (fileSet != dungeonSet) return;

                RoomType type = parseType(matcher.group(3), fileName);
                if (type == RoomType.Bridge) {
                    throw new IllegalArgumentException("Bridge schematic '" + fileName + "' must include _x_ or _z_");
                }
                Room room = new Room(type, fileName, path.toFile(), loaded[0] + 1);
                poolFor(type).computeIfAbsent(floor, ignored -> new ArrayList<>()).add(room);
                loaded[0]++;
            });
        } catch (IOException exception) {
            throw new IllegalArgumentException("Unable to list FAWE schematic directory: " + directory, exception);
        }
        return loaded[0];
    }

    private static Map<Integer, List<Room>> poolFor(RoomType type) {
        return switch (type) {
            case Normal -> NormanlroomMap;
            case Start -> StartroomMap;
            case End -> EndroomMap;
            case Boss -> BossroomMap;
            case Elit, Shop, Add -> SpecialroomMap;
            case Bridge -> throw new IllegalArgumentException("Bridge templates are not room templates yet");
        };
    }

    public enum BridgeAxis { X, Z }

    private static RoomType parseType(String rawType, String fileName) {
        String normalized = rawType.substring(0, 1).toUpperCase(Locale.ROOT)
                + rawType.substring(1).toLowerCase(Locale.ROOT);
        try {
            return RoomType.valueOf(normalized);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Schematic '" + fileName + "' has an unknown room type", exception);
        }
    }

    private static String require(ConfigurationSection section, String key) {
        String value = section.getString(key);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Room '" + section.getName() + "' is missing " + key);
        }
        return value;
    }

    private static Location parseLocation(World world, String raw, String roomName, String key) {
        String[] parts = raw.split(",");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Room '" + roomName + "' has an invalid " + key + "; expected x,y,z");
        }
        try {
            return new Location(world, Integer.parseInt(parts[0].trim()), Integer.parseInt(parts[1].trim()),
                    Integer.parseInt(parts[2].trim()));
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Room '" + roomName + "' has an invalid " + key + "; expected integers", exception);
        }
    }
}
