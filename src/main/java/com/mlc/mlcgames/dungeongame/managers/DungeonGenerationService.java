package com.mlc.mlcgames.dungeongame.managers;

import com.mlc.mlcgames.Mlcgames;
import com.mlc.mlcgames.dungeongame.Dungeongame;
import com.mlc.mlcgames.dungeongame.rooms.Room;
import com.mlc.mlcgames.dungeongame.rooms.RoomType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * 负责分 tick 生成 3×5 个布局，并把易失的 RoomSpawner 静态结果转换成绝对坐标快照。
 *
 * <p>服务只产出 {@link DungeonSession} 数据，不决定游戏何时进入运行态，也不传送玩家；
 * 生命周期决定权仍由 {@link DungeonGameManager} 持有。</p>
 */
final class DungeonGenerationService {
    interface Listener {
        void onCompleted(Player owner);
        void onFailed(Player owner, RuntimeException exception);
    }

    private final JavaPlugin plugin;
    private final DungeonSession session;
    private final Random random;
    private final DungeonMobRegistry mobRegistry;
    private BukkitTask task;
    private boolean generating;

    DungeonGenerationService(JavaPlugin plugin, DungeonSession session, Random random,
                             DungeonMobRegistry mobRegistry) {
        this.plugin = plugin;
        this.session = session;
        this.random = random;
        this.mobRegistry = mobRegistry;
    }

    boolean isGenerating() {
        return generating;
    }

    /**
     * 启动分批生成。调用方必须先创建好地下城世界并清空旧会话。
     * 每个 tick 只生成一层，避免一次性粘贴十五层阻塞主线程。
     */
    void start(Player owner, Listener listener) {
        if (generating) throw new IllegalStateException("Dungeon generation is already running");
        generating = true;
        scheduleAllStages(owner, listener);
    }

    /** 可重复调用；用于手动结束、生成异常和插件关闭时收回调度任务。 */
    void cancel() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        generating = false;
    }

    private void scheduleAllStages(Player owner, Listener listener) {
        int baseX = Mlcgames.dungeonConfiguration.getInt("layout.origin.x", 0);
        int baseZ = Mlcgames.dungeonConfiguration.getInt("layout.origin.z", 0);
        int spacingX = Math.max(1600, Mlcgames.dungeonConfiguration.getInt("layout.stage-spacing.x", 2048));
        int spacingZ = Math.max(1600, Mlcgames.dungeonConfiguration.getInt("layout.stage-spacing.z", 2048));

        task = new BukkitRunnable() {
            private int index;
            private int loadedLevel;
            private int selectedEnvironmentSet;

            @Override
            public void run() {
                try {
                    if (index >= DungeonSession.TOTAL_STAGES) {
                        cancel();
                        task = null;
                        generating = false;
                        listener.onCompleted(owner);
                        return;
                    }

                    int dungeonLevel = index / DungeonSession.FLOORS_PER_LEVEL + 1;
                    int floor = index % DungeonSession.FLOORS_PER_LEVEL + 1;
                    if (loadedLevel != dungeonLevel) {
                        selectedEnvironmentSet = selectCompleteEnvironmentSet(dungeonLevel);
                        loadedLevel = dungeonLevel;
                    }

                    // 十五层共享一个世界，因此每层生成前都要切换本层的布局原点。
                    Worldmanager.layoutOriginX = baseX + (floor - 1) * spacingX;
                    Worldmanager.layoutOriginZ = baseZ + (dungeonLevel - 1) * spacingZ;
                    RoomSpawner.generateRooms(Dungeongame.mapsize, dungeonLevel, floor,
                            Dungeongame.specialRoomChance, random);
                    session.generatedRoomCount += RoomSpawner.roomList.size();
                    session.stages.add(captureStage(dungeonLevel, floor, selectedEnvironmentSet));
                    index++;

                    if (owner.isOnline()) {
                        owner.sendActionBar("§a地牢生成进度：" + index + "/" + DungeonSession.TOTAL_STAGES
                                + "（关卡 " + dungeonLevel + "，环境 " + selectedEnvironmentSet + "）");
                    }
                } catch (RuntimeException exception) {
                    cancel();
                    task = null;
                    generating = false;
                    listener.onFailed(owner, exception);
                }
            }
        }.runTaskTimer(plugin, 1L, 1L);
    }

    /** 随机尝试环境预设，但只接受 Start/End/Boss/桥等必需模板完整的集合。 */
    private int selectCompleteEnvironmentSet(int dungeonLevel) {
        List<Integer> availableSets = Roommanager.getAvailableEnvironmentSets(dungeonLevel);
        if (availableSets.isEmpty()) {
            throw new IllegalStateException("No environment sets found for dungeon floor " + dungeonLevel);
        }

        List<Integer> candidates = new ArrayList<>(availableSets);
        Collections.shuffle(candidates, random);
        List<String> rejected = new ArrayList<>();
        for (int candidate : candidates) {
            Roommanager.loadSchematicTemplates(dungeonLevel, candidate);
            List<String> missing = Roommanager.getMissingRequiredTemplates(dungeonLevel);
            if (!missing.isEmpty()) {
                rejected.add("set " + candidate + " missing " + String.join(", ", missing));
                continue;
            }
            if (!mobRegistry.hasPack(dungeonLevel, candidate)) {
                rejected.add("set " + candidate + " missing mobs/" + dungeonLevel + "_" + candidate + ".yml");
                continue;
            }
            return candidate;
        }
        throw new IllegalStateException("Dungeon floor " + dungeonLevel
                + " has no complete environment preset: " + String.join("; ", rejected));
    }

    /**
     * RoomSpawner 的表会在生成下一层时被覆盖，所以必须在此处立即保存房间边界、门洞和标记坐标。
     */
    private DungeonSession.Stage captureStage(int dungeonLevel, int floor, int environmentSet) {
        // 环境包是不可变快照；运行中 reload 只影响之后开始的新游戏。
        DungeonMobPack mobPack = mobRegistry.requirePack(dungeonLevel, environmentSet);
        Room startRoom = RoomSpawner.roomList.stream()
                .filter(room -> room.getType() == RoomType.Start)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Stage has no start room"));
        RoomType terminalType = floor == DungeonSession.FLOORS_PER_LEVEL ? RoomType.Boss : RoomType.End;
        Room terminalRoom = RoomSpawner.roomList.stream()
                .filter(room -> room.getType() == terminalType)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Level " + dungeonLevel + " floor " + floor
                        + " has no " + terminalType + " room"));

        DungeonSession.Encounter terminalEncounter = null;
        Material replacement = DungeonConfiguration.material("markers.replacement", Material.POLISHED_BLACKSTONE);
        for (Room room : RoomSpawner.roomList) {
            Worldmanager.RoomBounds bounds = Worldmanager.getRoomBounds(room);
            List<DungeonSession.SpawnMarker> markers = discoverSpawnMarkers(bounds, replacement);
            if (markers.isEmpty()) continue;
            DungeonSession.Encounter encounter = new DungeonSession.Encounter(
                    room, dungeonLevel, floor, environmentSet, mobPack, bounds,
                    Worldmanager.getRoomDoors(room), List.copyOf(markers));
            session.encounters.add(encounter);
            if (room == terminalRoom) terminalEncounter = encounter;
        }

        long bossMarkers = terminalEncounter == null ? 0 : terminalEncounter.markers.stream()
                .filter(marker -> marker.kind() == DungeonSession.MarkerKind.BOSS)
                .count();
        if (floor == DungeonSession.FLOORS_PER_LEVEL && bossMarkers != 1) {
            throw new IllegalStateException("Boss schematic '" + terminalRoom.getName()
                    + "' must contain exactly one CREAKING_HEART marker, found " + bossMarkers);
        }

        Worldmanager.RoomBounds terminalBounds = Worldmanager.getRoomBounds(terminalRoom);
        Location portalMarker = discoverPortalMarker(terminalRoom, terminalBounds);
        return new DungeonSession.Stage(dungeonLevel, floor, environmentSet,
                Worldmanager.getRoomSpawn(startRoom), portalMarker, terminalEncounter);
    }

    /** 扫描后立刻替换刷怪标记，避免玩家看到或触发原版刷怪笼行为。 */
    private List<DungeonSession.SpawnMarker> discoverSpawnMarkers(
            Worldmanager.RoomBounds bounds, Material replacement) {
        List<DungeonSession.SpawnMarker> markers = new ArrayList<>();
        for (int x = bounds.minX(); x < bounds.maxX(); x++) {
            for (int y = bounds.minY(); y <= bounds.maxY(); y++) {
                for (int z = bounds.minZ(); z < bounds.maxZ(); z++) {
                    Block block = Worldmanager.dungeonWorld.getBlockAt(x, y, z);
                    DungeonSession.MarkerKind kind = markerKind(block.getType());
                    if (kind == null) continue;
                    markers.add(new DungeonSession.SpawnMarker(
                            block.getLocation().add(0.5, 1.0, 0.5), kind));
                    block.setType(replacement, false);
                }
            }
        }
        return markers;
    }

    /**
     * 终点模板必须提供唯一坐标。先移除标记，等该层解锁后再由推进服务恢复传送方块。
     */
    private Location discoverPortalMarker(Room terminalRoom, Worldmanager.RoomBounds bounds) {
        Material markerMaterial = DungeonConfiguration.material(
                "progression.marker-block", Material.RESPAWN_ANCHOR);
        Material inactiveMaterial = DungeonConfiguration.material("progression.inactive-block", Material.AIR);
        List<Block> found = new ArrayList<>();
        for (int x = bounds.minX(); x < bounds.maxX(); x++) {
            for (int y = bounds.minY(); y <= bounds.maxY(); y++) {
                for (int z = bounds.minZ(); z < bounds.maxZ(); z++) {
                    Block block = Worldmanager.dungeonWorld.getBlockAt(x, y, z);
                    if (block.getType() == markerMaterial) found.add(block);
                }
            }
        }
        if (found.size() != 1) {
            throw new IllegalStateException("Terminal schematic '" + terminalRoom.getName()
                    + "' must contain exactly one " + markerMaterial
                    + " portal marker, found " + found.size());
        }
        Block marker = found.getFirst();
        Location location = marker.getLocation();
        marker.setType(inactiveMaterial, false);
        return location;
    }

    private static DungeonSession.MarkerKind markerKind(Material material) {
        if (material == Material.SPAWNER) return DungeonSession.MarkerKind.NORMAL;
        if (material == Material.TRIAL_SPAWNER) return DungeonSession.MarkerKind.ELITE;
        if (material == Material.CREAKING_HEART) return DungeonSession.MarkerKind.BOSS;
        return null;
    }
}
