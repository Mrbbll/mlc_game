package com.mlc.mlcgames.dungeongame.managers;

import com.mlc.mlcgames.dungeongame.loot.DungeonLootContext;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;

/** 清房后寻找安全落点并写入稳定的战利品上下文；实际奖池可在此服务内继续扩展。 */
final class DungeonLootChestService {
    private static final int MAX_LOCATION_ATTEMPTS = 96;

    private final JavaPlugin plugin;
    private final Random random;
    private final NamespacedKey levelKey;
    private final NamespacedKey floorKey;
    private final NamespacedKey roomTypeKey;
    private final NamespacedKey tierKey;

    DungeonLootChestService(JavaPlugin plugin, Random random) {
        this.plugin = plugin;
        this.random = random;
        levelKey = new NamespacedKey(plugin, "dungeon_loot_level");
        floorKey = new NamespacedKey(plugin, "dungeon_loot_floor");
        roomTypeKey = new NamespacedKey(plugin, "dungeon_loot_room_type");
        tierKey = new NamespacedKey(plugin, "dungeon_loot_tier");
    }

    void spawn(DungeonSession.Encounter encounter) {
        Location location = findOpenLocation(encounter.bounds);
        if (location == null) {
            plugin.getLogger().warning(
                    "No safe loot-chest position found in room '" + encounter.roomName + "'");
            return;
        }

        Block block = location.getBlock();
        block.setType(Material.CHEST, false);
        if (!(block.getState() instanceof Chest chest)) return;

        DungeonLootContext context = DungeonLootContext.of(
                encounter.dungeonLevel, encounter.floor, encounter.roomType);
        chest.getInventory().clear();
        chest.getPersistentDataContainer().set(levelKey, PersistentDataType.INTEGER, context.dungeonLevel());
        chest.getPersistentDataContainer().set(floorKey, PersistentDataType.INTEGER, context.floor());
        chest.getPersistentDataContainer().set(roomTypeKey, PersistentDataType.STRING, context.roomType().name());
        chest.getPersistentDataContainer().set(tierKey, PersistentDataType.INTEGER, context.qualityTier());
        chest.update(true, false);
    }

    /** 只接受脚下有实体方块且头顶两格为空的位置，不再用房间中心强行覆盖建筑。 */
    private Location findOpenLocation(Worldmanager.RoomBounds bounds) {
        for (int attempt = 0; attempt < MAX_LOCATION_ATTEMPTS; attempt++) {
            int x = random.nextInt(bounds.minX() + 2, bounds.maxX() - 2);
            int z = random.nextInt(bounds.minZ() + 2, bounds.maxZ() - 2);
            Location found = firstOpenAt(x, z, bounds);
            if (found != null) return found;
        }
        return null;
    }

    private static Location firstOpenAt(int x, int z, Worldmanager.RoomBounds bounds) {
        for (int y = bounds.minY() + 1; y < bounds.maxY() - 1; y++) {
            Block block = Worldmanager.dungeonWorld.getBlockAt(x, y, z);
            if (block.getType().isAir()
                    && block.getRelative(0, 1, 0).getType().isAir()
                    && !block.getRelative(0, -1, 0).getType().isAir()) {
                return block.getLocation();
            }
        }
        return null;
    }
}
