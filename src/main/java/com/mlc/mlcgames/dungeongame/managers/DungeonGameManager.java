package com.mlc.mlcgames.dungeongame.managers;

import com.mlc.mlcgames.Mlcgames;
import com.mlc.mlcgames.dungeongame.Dungeongame;
import com.mlc.mlcgames.dungeongame.loot.DungeonLootContext;
import com.mlc.mlcgames.dungeongame.mobs.CraftEngineMobEquipment;
import com.mlc.mlcgames.dungeongame.rooms.Room;
import com.mlc.mlcgames.dungeongame.rooms.RoomType;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

/** Owns all 3x5 dungeon stages, encounters and progression portals in one world. */
public final class DungeonGameManager {
    public static final int DUNGEON_LEVELS = 3;
    public static final int FLOORS_PER_LEVEL = 5;
    private static final int TOTAL_STAGES = DUNGEON_LEVELS * FLOORS_PER_LEVEL;

    private enum MarkerKind { NORMAL, ELITE, BOSS }
    private enum EncounterState { WAITING, ACTIVE, CLEARED }
    private record SpawnMarker(Location location, MarkerKind kind) { }

    private static final class Encounter {
        final String roomName;
        final RoomType roomType;
        final int dungeonLevel;
        final int floor;
        final Worldmanager.RoomBounds bounds;
        final List<Worldmanager.DoorBounds> doors;
        final List<SpawnMarker> markers;
        final Set<UUID> monsters = new HashSet<>();
        EncounterState state = EncounterState.WAITING;
        ProgressionPortal progressionPortal;

        Encounter(Room room, int dungeonLevel, int floor, Worldmanager.RoomBounds bounds,
                  List<Worldmanager.DoorBounds> doors, List<SpawnMarker> markers) {
            this.roomName = room.getName();
            this.roomType = room.getType();
            this.dungeonLevel = dungeonLevel;
            this.floor = floor;
            this.bounds = bounds;
            this.doors = doors;
            this.markers = markers;
        }
    }

    private static final class DungeonStage {
        final int dungeonLevel;
        final int floor;
        final int environmentSet;
        final Location start;
        final Worldmanager.RoomBounds terminalBounds;
        final Encounter terminalEncounter;
        ProgressionPortal portal;

        DungeonStage(int dungeonLevel, int floor, int environmentSet, Location start,
                     Worldmanager.RoomBounds terminalBounds, Encounter terminalEncounter) {
            this.dungeonLevel = dungeonLevel;
            this.floor = floor;
            this.environmentSet = environmentSet;
            this.start = start;
            this.terminalBounds = terminalBounds;
            this.terminalEncounter = terminalEncounter;
        }
    }

    private static final class ProgressionPortal {
        final DungeonStage source;
        final DungeonStage target;
        final boolean victory;
        Location location;
        boolean active;

        ProgressionPortal(DungeonStage source, DungeonStage target, boolean victory) {
            this.source = source;
            this.target = target;
            this.victory = victory;
        }
    }

    private static DungeonGameManager instance;
    private final JavaPlugin plugin;
    private final Random random = new Random();
    private final CraftEngineMobEquipment equipment = new CraftEngineMobEquipment();
    private final List<DungeonStage> stages = new ArrayList<>();
    private final List<Encounter> encounters = new ArrayList<>();
    private final List<ProgressionPortal> portals = new ArrayList<>();
    private final Map<UUID, Encounter> monsterOwners = new HashMap<>();
    private final NamespacedKey lootLevelKey;
    private final NamespacedKey lootFloorKey;
    private final NamespacedKey lootRoomTypeKey;
    private final NamespacedKey lootTierKey;
    private BukkitTask generationTask;
    private boolean generating;
    private int generatedRoomCount;

    private DungeonGameManager(JavaPlugin plugin) {
        this.plugin = plugin;
        lootLevelKey = new NamespacedKey(plugin, "dungeon_loot_level");
        lootFloorKey = new NamespacedKey(plugin, "dungeon_loot_floor");
        lootRoomTypeKey = new NamespacedKey(plugin, "dungeon_loot_room_type");
        lootTierKey = new NamespacedKey(plugin, "dungeon_loot_tier");
    }

    public static DungeonGameManager initialize(JavaPlugin plugin) {
        if (instance == null) instance = new DungeonGameManager(plugin);
        return instance;
    }

    public static DungeonGameManager get() {
        if (instance == null) throw new IllegalStateException("DungeonGameManager is not initialized");
        return instance;
    }

    public boolean isRunningOrGenerating() {
        return Dungeongame.isstart || generating;
    }

    public String status() {
        if (generating) return "正在生成 " + stages.size() + "/" + TOTAL_STAGES + " 个地牢";
        if (!Dungeongame.isstart) return "未开始";
        return "运行中，共 " + stages.size() + " 个地牢、" + generatedRoomCount + " 个房间";
    }

    public void startGame(Player owner) {
        if (isRunningOrGenerating()) {
            owner.sendMessage("§c地牢游戏已经开始或正在生成。");
            return;
        }
        try {
            clearRuntimeState();
            Dungeongame.lastGameWon = false;
            Dungeongame.lastWinner = null;
            Dungeongame.currentLevel = 1;
            Dungeongame.currentFloor = 1;
            Worldmanager.createDungeonWorld();
            generating = true;
            owner.sendMessage("§e开始生成 3 关 × 每关 5 层地牢，请稍候……");
            scheduleAllStages(owner);
        } catch (RuntimeException exception) {
            abortGeneration(owner, exception);
        }
    }

    private void scheduleAllStages(Player owner) {
        int baseX = Mlcgames.dungeonConfiguration.getInt("layout.origin.x", 0);
        int baseZ = Mlcgames.dungeonConfiguration.getInt("layout.origin.z", 0);
        int spacingX = Math.max(1600, Mlcgames.dungeonConfiguration.getInt("layout.stage-spacing.x", 2048));
        int spacingZ = Math.max(1600, Mlcgames.dungeonConfiguration.getInt("layout.stage-spacing.z", 2048));

        generationTask = new BukkitRunnable() {
            private int index;
            private int loadedLevel;
            private int selectedEnvironmentSet;

            @Override
            public void run() {
                try {
                    if (index >= TOTAL_STAGES) {
                        cancel();
                        generationTask = null;
                        finishGeneration(owner);
                        return;
                    }
                    int dungeonLevel = index / FLOORS_PER_LEVEL + 1;
                    int floor = index % FLOORS_PER_LEVEL + 1;
                    if (loadedLevel != dungeonLevel) {
                        List<Integer> availableSets = Roommanager.getAvailableEnvironmentSets(dungeonLevel);
                        if (availableSets.isEmpty()) {
                            throw new IllegalStateException("No environment sets found for dungeon floor " + dungeonLevel);
                        }
                        List<Integer> candidates = new ArrayList<>(availableSets);
                        Collections.shuffle(candidates, random);
                        List<String> rejected = new ArrayList<>();
                        selectedEnvironmentSet = -1;
                        for (int candidate : candidates) {
                            Roommanager.loadSchematicTemplates(dungeonLevel, candidate);
                            List<String> missing = Roommanager.getMissingRequiredTemplates(dungeonLevel);
                            if (missing.isEmpty()) {
                                selectedEnvironmentSet = candidate;
                                break;
                            }
                            rejected.add("set " + candidate + " missing " + String.join(", ", missing));
                        }
                        if (selectedEnvironmentSet < 0) {
                            throw new IllegalStateException("Dungeon floor " + dungeonLevel
                                    + " has no complete environment preset: " + String.join("; ", rejected));
                        }
                        loadedLevel = dungeonLevel;
                    }
                    Worldmanager.layoutOriginX = baseX + (floor - 1) * spacingX;
                    Worldmanager.layoutOriginZ = baseZ + (dungeonLevel - 1) * spacingZ;
                    RoomSpawner.generateRooms(Dungeongame.mapsize, dungeonLevel, floor,
                            Dungeongame.specialRoomChance, random);
                    generatedRoomCount += RoomSpawner.roomList.size();
                    stages.add(captureStage(dungeonLevel, floor, selectedEnvironmentSet));
                    index++;
                    if (owner.isOnline()) owner.sendActionBar("§a地牢生成进度：" + index + "/" + TOTAL_STAGES
                            + "（关卡 " + dungeonLevel + "，环境 " + selectedEnvironmentSet + "）");
                } catch (RuntimeException exception) {
                    cancel();
                    generationTask = null;
                    abortGeneration(owner, exception);
                }
            }
        }.runTaskTimer(plugin, 1L, 1L);
    }

    private DungeonStage captureStage(int dungeonLevel, int floor, int environmentSet) {
        Room startRoom = RoomSpawner.roomList.stream()
                .filter(room -> room.getType() == RoomType.Start).findFirst()
                .orElseThrow(() -> new IllegalStateException("Stage has no start room"));
        RoomType terminalType = floor == FLOORS_PER_LEVEL ? RoomType.Boss : RoomType.End;
        Room terminalRoom = RoomSpawner.roomList.stream()
                .filter(room -> room.getType() == terminalType).findFirst()
                .orElseThrow(() -> new IllegalStateException("Level " + dungeonLevel + " floor " + floor
                        + " has no " + terminalType + " room"));

        Encounter terminalEncounter = null;
        Material replacement = configuredMaterial("markers.replacement", Material.POLISHED_BLACKSTONE);
        for (Room room : RoomSpawner.roomList) {
            Worldmanager.RoomBounds bounds = Worldmanager.getRoomBounds(room);
            List<SpawnMarker> markers = discoverMarkers(bounds, replacement);
            if (markers.isEmpty()) continue;
            Encounter encounter = new Encounter(room, dungeonLevel, floor, bounds,
                    Worldmanager.getRoomDoors(room), List.copyOf(markers));
            encounters.add(encounter);
            if (room == terminalRoom) terminalEncounter = encounter;
        }

        if (floor == FLOORS_PER_LEVEL && (terminalEncounter == null
                || terminalEncounter.markers.stream().noneMatch(marker -> marker.kind() == MarkerKind.BOSS))) {
            throw new IllegalStateException("Boss schematic '" + terminalRoom.getName()
                    + "' must contain at least one CREAKING_HEART marker");
        }
        return new DungeonStage(dungeonLevel, floor, environmentSet, Worldmanager.getRoomSpawn(startRoom),
                Worldmanager.getRoomBounds(terminalRoom), terminalEncounter);
    }

    private List<SpawnMarker> discoverMarkers(Worldmanager.RoomBounds bounds, Material replacement) {
        List<SpawnMarker> markers = new ArrayList<>();
        for (int x = bounds.minX(); x < bounds.maxX(); x++) {
            for (int y = bounds.minY(); y <= bounds.maxY(); y++) {
                for (int z = bounds.minZ(); z < bounds.maxZ(); z++) {
                    Block block = Worldmanager.dungeonWorld.getBlockAt(x, y, z);
                    MarkerKind kind = markerKind(block.getType());
                    if (kind == null) continue;
                    markers.add(new SpawnMarker(block.getLocation().add(0.5, 1.0, 0.5), kind));
                    block.setType(replacement, false);
                }
            }
        }
        return markers;
    }

    private void finishGeneration(Player owner) {
        for (int index = 0; index < stages.size(); index++) {
            DungeonStage stage = stages.get(index);
            DungeonStage target = index + 1 < stages.size() ? stages.get(index + 1) : null;
            ProgressionPortal portal = new ProgressionPortal(stage, target, target == null);
            stage.portal = portal;
            portals.add(portal);
            if (stage.terminalEncounter != null) stage.terminalEncounter.progressionPortal = portal;
            else spawnPortal(portal);
        }
        generating = false;
        Dungeongame.isstart = true;
        if (owner.isOnline()) {
            enter(owner);
            owner.sendMessage("§a已在同一世界生成全部 15 个地牢，共 " + generatedRoomCount + " 个房间。");
        } else {
            Bukkit.broadcastMessage("§a全部 15 个地牢已生成，可以使用 /dungeongame enter 加入。");
        }
    }

    private void abortGeneration(Player owner, RuntimeException exception) {
        generating = false;
        Dungeongame.isstart = false;
        if (generationTask != null) {
            generationTask.cancel();
            generationTask = null;
        }
        clearRuntimeState();
        plugin.getLogger().severe("Dungeon generation failed: " + exception.getMessage());
        if (owner != null && owner.isOnline()) owner.sendMessage("§c地牢生成失败：" + exception.getMessage());
    }

    public void endGame() {
        if (generationTask != null) {
            generationTask.cancel();
            generationTask = null;
        }
        generating = false;
        List<UUID> monsterIds = new ArrayList<>(monsterOwners.keySet());
        monsterOwners.clear();
        for (UUID uuid : monsterIds) {
            Entity entity = Bukkit.getEntity(uuid);
            if (entity != null) entity.remove();
        }
        for (Encounter encounter : encounters) {
            if (encounter.state == EncounterState.ACTIVE) Worldmanager.setRoomDoors(encounter.doors, Material.AIR);
        }
        World fallback = Bukkit.getWorlds().stream().filter(world -> world != Worldmanager.dungeonWorld).findFirst().orElse(null);
        if (fallback != null) {
            for (Player player : new ArrayList<>(Dungeongame.participants)) {
                if (player.isOnline()) player.teleport(fallback.getSpawnLocation());
            }
        }
        clearRuntimeState();
        Dungeongame.isstart = false;
    }

    private void clearRuntimeState() {
        stages.clear();
        encounters.clear();
        portals.clear();
        monsterOwners.clear();
        Dungeongame.participants.clear();
        generatedRoomCount = 0;
    }

    public void enter(Player player) {
        if (!Dungeongame.isstart || stages.isEmpty()) {
            player.sendMessage(generating ? "§e地牢仍在生成，请稍候。" : "§c地牢游戏尚未开始。");
            return;
        }
        Dungeongame.participants.add(player);
        Dungeongame.currentLevel = 1;
        Dungeongame.currentFloor = 1;
        player.teleport(stages.getFirst().start);
        player.sendMessage("§a进入地牢第 1 关，第 1 层。");
        Bukkit.getScheduler().runTask(plugin, () -> handlePlayerPosition(player));
    }

    public void handlePlayerPosition(Player player) {
        if (!Dungeongame.isstart || player.getWorld() != Worldmanager.dungeonWorld) return;
        for (ProgressionPortal portal : portals) {
            if (portal.active && isNearPortal(player.getLocation(), portal.location)) {
                usePortal(player, portal);
                return;
            }
        }
        for (Encounter encounter : encounters) {
            if (encounter.state == EncounterState.WAITING && encounter.bounds.contains(player.getLocation())) {
                activate(encounter);
                return;
            }
        }
    }

    private void usePortal(Player player, ProgressionPortal portal) {
        if (portal.victory) {
            Dungeongame.lastGameWon = true;
            Dungeongame.lastWinner = player.getUniqueId();
            Bukkit.broadcastMessage("§6" + player.getName() + " 完成了全部 3×5 个地牢，游戏胜利！");
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0F, 1.0F);
            endGame();
            return;
        }
        DungeonStage target = portal.target;
        Dungeongame.currentLevel = target.dungeonLevel;
        Dungeongame.currentFloor = target.floor;
        player.teleport(target.start);
        player.sendMessage("§b进入地牢第 " + target.dungeonLevel + " 关，第 " + target.floor + " 层。");
        Bukkit.getScheduler().runTask(plugin, () -> handlePlayerPosition(player));
    }

    private static boolean isNearPortal(Location player, Location portal) {
        if (portal == null || player.getWorld() != portal.getWorld()) return false;
        double dx = player.getX() - portal.getX();
        double dz = player.getZ() - portal.getZ();
        return dx * dx + dz * dz <= 5.0625 && Math.abs(player.getY() - portal.getY()) <= 2.5;
    }

    public void handleMonsterDeath(UUID uuid) {
        Encounter encounter = monsterOwners.remove(uuid);
        if (encounter == null) return;
        encounter.monsters.remove(uuid);
        if (encounter.monsters.isEmpty()) clearEncounter(encounter);
    }

    private void activate(Encounter encounter) {
        encounter.state = EncounterState.ACTIVE;
        Worldmanager.setRoomDoors(encounter.doors,
                configuredMaterial("encounters.closed-door-block", Material.BARRIER));
        for (SpawnMarker marker : encounter.markers) {
            EntityType type = randomEntityType(encounter, marker.kind);
            Entity spawned = marker.location.getWorld().spawnEntity(marker.location, type);
            if (!(spawned instanceof LivingEntity living)) {
                spawned.remove();
                continue;
            }
            living.setPersistent(true);
            living.setRemoveWhenFarAway(false);
            equipment.equip(living, Dungeongame.difficulty);
            scale(living, marker.kind, encounter.dungeonLevel);
            encounter.monsters.add(living.getUniqueId());
            monsterOwners.put(living.getUniqueId(), encounter);
        }
        if (encounter.monsters.isEmpty()) clearEncounter(encounter);
    }

    private void scale(LivingEntity entity, MarkerKind kind, int dungeonLevel) {
        String difficulty = Dungeongame.difficulty.name().toLowerCase(Locale.ROOT);
        double healthMultiplier = Mlcgames.dungeonConfiguration.getDouble("difficulty." + difficulty + ".health-multiplier",
                Dungeongame.difficulty.healthMultiplier());
        double damageMultiplier = Mlcgames.dungeonConfiguration.getDouble("difficulty." + difficulty + ".damage-multiplier",
                Dungeongame.difficulty.damageMultiplier());
        double levelStepMultiplier = Mlcgames.dungeonConfiguration.getDouble(
                "progression.level-stat-multiplier", 1.5);
        if (levelStepMultiplier <= 0.0) levelStepMultiplier = 1.5;
        double levelMultiplier = Math.pow(levelStepMultiplier, Math.max(0, dungeonLevel - 1));
        double encounterMultiplier = switch (kind) { case NORMAL -> 1.0; case ELITE -> 1.75; case BOSS -> 4.0; };
        var maxHealth = entity.getAttribute(Attribute.MAX_HEALTH);
        if (maxHealth != null) {
            maxHealth.setBaseValue(Math.max(1.0,
                    maxHealth.getBaseValue() * healthMultiplier * levelMultiplier * encounterMultiplier));
            entity.setHealth(maxHealth.getValue());
        }
        var attack = entity.getAttribute(Attribute.ATTACK_DAMAGE);
        if (attack != null) attack.setBaseValue(Math.max(0.0,
                attack.getBaseValue() * damageMultiplier * levelMultiplier * Math.sqrt(encounterMultiplier)));
    }

    private void clearEncounter(Encounter encounter) {
        encounter.state = EncounterState.CLEARED;
        Worldmanager.setRoomDoors(encounter.doors, Material.AIR);
        if (encounter.progressionPortal != null) spawnPortal(encounter.progressionPortal);
        spawnLootChest(encounter);
    }

    private void spawnPortal(ProgressionPortal portal) {
        if (portal.active) return;
        Location location = findOpenLocation(portal.source.terminalBounds, true);
        Material material = configuredMaterial("progression.portal-block", Material.RESPAWN_ANCHOR);
        if (material.isAir()) material = Material.RESPAWN_ANCHOR;
        location.getBlock().setType(material, false);
        portal.location = location.add(0.5, 0.5, 0.5);
        portal.active = true;
    }

    private void spawnLootChest(Encounter encounter) {
        Location location = findOpenLocation(encounter.bounds, false);
        Block block = location.getBlock();
        block.setType(Material.CHEST, false);
        if (block.getState() instanceof Chest chest) {
            DungeonLootContext context = DungeonLootContext.of(
                    encounter.dungeonLevel, encounter.floor, encounter.roomType);
            chest.getInventory().clear();
            chest.getPersistentDataContainer().set(lootLevelKey, PersistentDataType.INTEGER, context.dungeonLevel());
            chest.getPersistentDataContainer().set(lootFloorKey, PersistentDataType.INTEGER, context.floor());
            chest.getPersistentDataContainer().set(lootRoomTypeKey, PersistentDataType.STRING, context.roomType().name());
            chest.getPersistentDataContainer().set(lootTierKey, PersistentDataType.INTEGER, context.qualityTier());
            chest.update(true, false);
        }
    }

    private Location findOpenLocation(Worldmanager.RoomBounds bounds, boolean preferCentre) {
        if (preferCentre) {
            int centreX = (bounds.minX() + bounds.maxX()) / 2;
            int centreZ = (bounds.minZ() + bounds.maxZ()) / 2;
            Location centre = firstOpenAt(centreX, centreZ, bounds);
            if (centre != null) return centre;
        }
        for (int attempt = 0; attempt < 96; attempt++) {
            int x = random.nextInt(bounds.minX() + 2, bounds.maxX() - 2);
            int z = random.nextInt(bounds.minZ() + 2, bounds.maxZ() - 2);
            Location found = firstOpenAt(x, z, bounds);
            if (found != null) return found;
        }
        return new Location(Worldmanager.dungeonWorld, (bounds.minX() + bounds.maxX()) / 2.0,
                bounds.minY() + 1, (bounds.minZ() + bounds.maxZ()) / 2.0);
    }

    private static Location firstOpenAt(int x, int z, Worldmanager.RoomBounds bounds) {
        for (int y = bounds.minY() + 1; y < bounds.maxY() - 1; y++) {
            Block block = Worldmanager.dungeonWorld.getBlockAt(x, y, z);
            if (block.getType().isAir() && block.getRelative(0, 1, 0).getType().isAir()
                    && !block.getRelative(0, -1, 0).getType().isAir()) return block.getLocation();
        }
        return null;
    }

    private EntityType randomEntityType(Encounter encounter, MarkerKind kind) {
        String suffix = kind.name().toLowerCase(Locale.ROOT);
        List<String> configured = Mlcgames.dungeonConfiguration.getStringList(
                "mobs.level-" + encounter.dungeonLevel + ".floor-" + encounter.floor + "." + suffix);
        if (configured.isEmpty()) {
            configured = Mlcgames.dungeonConfiguration.getStringList("mobs.floor-" + encounter.floor + "." + suffix);
        }
        if (configured.isEmpty()) configured = Mlcgames.dungeonConfiguration.getStringList("mobs.default." + suffix);
        List<String> values = configured.isEmpty() ? defaults(kind) : configured;
        List<EntityType> valid = values.stream().map(DungeonGameManager::livingEntityType).filter(Objects::nonNull).toList();
        if (valid.isEmpty()) {
            plugin.getLogger().warning("No valid mob types for level " + encounter.dungeonLevel
                    + " floor " + encounter.floor + " " + suffix + "; using built-in defaults");
            valid = defaults(kind).stream().map(DungeonGameManager::livingEntityType).filter(Objects::nonNull).toList();
        }
        return valid.get(random.nextInt(valid.size()));
    }

    private static List<String> defaults(MarkerKind kind) {
        return switch (kind) {
            case NORMAL -> List.of("ZOMBIE", "SKELETON", "SPIDER");
            case ELITE -> List.of("WITHER_SKELETON", "HUSK");
            case BOSS -> List.of("RAVAGER");
        };
    }

    private static EntityType livingEntityType(String name) {
        if (name == null) return null;
        try {
            EntityType type = EntityType.valueOf(name.trim().toUpperCase(Locale.ROOT));
            return type.isAlive() ? type : null;
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private static MarkerKind markerKind(Material material) {
        if (material == Material.SPAWNER) return MarkerKind.NORMAL;
        if (material == Material.TRIAL_SPAWNER) return MarkerKind.ELITE;
        if (material == Material.CREAKING_HEART) return MarkerKind.BOSS;
        return null;
    }

    private static Material configuredMaterial(String path, Material fallback) {
        Material material = Material.matchMaterial(Mlcgames.dungeonConfiguration.getString(path, fallback.name()));
        return material == null ? fallback : material;
    }
}
