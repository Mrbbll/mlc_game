package com.mlc.mlcgames.party.arcade.mining;

import com.mlc.mlcgames.party.core.Cuboid;
import com.mlc.mlcgames.party.core.PartyConfig;
import com.mlc.mlcgames.party.core.PartyGame;
import com.mlc.mlcgames.party.core.PartySession;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/** Source mining: a shared randomized 33x33x28 mine, five Silk Touch tools and coin ranking. */
public final class MiningGame implements PartyGame {
    private Cuboid mine;
    private final List<Material> oreRolls = new ArrayList<>();
    private final Map<Material, Integer> coinValues = new EnumMap<>(Material.class);
    private final Map<UUID, Integer> coins = new LinkedHashMap<>();
    private long prepareIndex;
    private int lastRankSecond = -10;
    private boolean finalized;

    @Override public boolean lastPlayerWins() { return false; }

    @Override public void validate(PartyConfig config, PartyConfig.Arena arena) {
        mine = arena.region("mine-area");
        int layers = config.integer("mine.layers", 1, 64);
        if (mine.maxX() - mine.minX() + 1 != 33 || mine.maxZ() - mine.minZ() + 1 != 33)
            throw new IllegalArgumentException("regions.mine-area 必须按原玩法保持 33x33 格宽度");
        if (mine.maxY() - mine.minY() + 1 < layers + 5)
            throw new IllegalArgumentException("regions.mine-area 高度至少需要 mine.layers + 5 格");
        if (mine.volume() > config.integer("limits.max-snapshot-blocks", 1, 1_000_000))
            throw new IllegalArgumentException("regions.mine-area 超出 limits.max-snapshot-blocks");
        oreRolls.clear();
        for (String name : config.rules.getStringList("mine.ore-rolls")) {
            Material material = Material.matchMaterial(name);
            if (material == null || !material.isBlock() || material.isAir()) throw new IllegalArgumentException("mine.ore-rolls 中有非法方块 " + name);
            oreRolls.add(material);
        }
        if (oreRolls.size() != 27) throw new IllegalArgumentException("mine.ore-rolls 必须包含原地图的 27 个随机结果");
        coinValues.clear();
        var values = config.rules.getConfigurationSection("coin-values");
        if (values == null) throw new IllegalArgumentException("game.yml 缺少 coin-values");
        for (String name : values.getKeys(false)) {
            Material material = Material.matchMaterial(name);
            int value = config.integer("coin-values." + name, 1, 1000);
            if (material == null || !material.isItem()) throw new IllegalArgumentException("coin-values 中有非法物品 " + name);
            coinValues.put(material, value);
        }
        config.integer("coin-convert-interval-seconds", 1, 30);
        config.integer("rank-interval-seconds", 1, 60);
    }

    @Override public List<Cuboid> writableRegions(PartyConfig.Arena arena) { return List.of(mine); }

    @Override public boolean prepare(PartySession session, int blockBudget) {
        int sizeX = mine.maxX() - mine.minX() + 1;
        int sizeZ = mine.maxZ() - mine.minZ() + 1;
        long volume = mine.volume();
        int layers = session.config.integer("mine.layers", 1, 64);
        long stop = Math.min(volume, prepareIndex + blockBudget);
        while (prepareIndex < stop) {
            long index = prepareIndex++;
            int x = mine.minX() + (int) (index % sizeX);
            int z = mine.minZ() + (int) ((index / sizeX) % sizeZ);
            int y = mine.minY() + (int) (index / ((long) sizeX * sizeZ));
            int layer = y - mine.minY();
            Material material;
            if (layer == 0) material = Material.BEDROCK;
            else if (layer <= layers) {
                int roll = ThreadLocalRandom.current().nextInt(51);
                material = roll < oreRolls.size() ? oreRolls.get(roll) : Material.STONE;
            } else if (layer <= layers + 2) material = Material.DIRT;
            else if (layer == layers + 3) material = Material.GRASS_BLOCK;
            else material = Material.AIR;
            session.set(session.arena.world().getBlockAt(x, y, z), material);
        }
        return prepareIndex >= volume;
    }

    @Override public void start(PartySession session) {
        prepareIndex = 0;
        coins.clear();
        finalized = false;
        lastRankSecond = -10;
        for (Player player : session.survivors()) {
            coins.put(player.getUniqueId(), 0);
            player.setGameMode(GameMode.SURVIVAL);
            player.setLevel(0);
            giveTools(session, player);
        }
    }

    private void giveTools(PartySession session, Player player) {
        Material[] tools = {Material.DIAMOND_PICKAXE, Material.DIAMOND_AXE, Material.DIAMOND_SHOVEL, Material.DIAMOND_HOE, Material.SHEARS};
        for (Material material : tools) {
            if (player.getInventory().contains(material)) continue;
            String key = switch (material) {
                case DIAMOND_PICKAXE -> "pickaxe";
                case DIAMOND_AXE -> "axe";
                case DIAMOND_SHOVEL -> "shovel";
                case DIAMOND_HOE -> "hoe";
                default -> "shears";
            };
            ItemStack tool = session.item(material, "items." + key);
            tool.addUnsafeEnchantment(Enchantment.EFFICIENCY, session.config.integer("tools.efficiency", 1, 10));
            tool.addUnsafeEnchantment(Enchantment.SILK_TOUCH, 1);
            player.getInventory().addItem(tool);
        }
    }

    @Override public boolean nativeBreak(PartySession session, Player player, org.bukkit.block.Block block) {
        return mine.contains(block.getX(), block.getY(), block.getZ());
    }

    @Override public boolean allowInventoryEdit(PartySession session, Player player) { return true; }
    @Override public boolean allowPickup(PartySession session, Player player, Item item) {
        return mine.contains(item.getLocation().getBlockX(), item.getLocation().getBlockY(), item.getLocation().getBlockZ());
    }
    @Override public boolean allowNaturalChanges(PartySession session) { return true; }
    @Override public boolean maintainFood(PartySession session) { return false; }

    @Override public void tick(PartySession session, int elapsed) {
        int seconds = elapsed / 20;
        if (elapsed % (session.config.integer("coin-convert-interval-seconds", 1, 30) * 20) == 0)
            for (Player player : session.players()) convertInventory(player);
        if (seconds > 0 && seconds % session.config.integer("rank-interval-seconds", 1, 60) == 0 && seconds != lastRankSecond) {
            lastRankSecond = seconds;
            showRanks(session);
        }
        for (Player player : session.players()) player.sendActionBar(session.config.message("status", "coins", coins.getOrDefault(player.getUniqueId(), 0), "seconds", Math.max(0, 61 - seconds)));
        if (session.survivors().size() <= 1) finishByCoins(session);
    }

    private void convertInventory(Player player) {
        int gained = 0;
        for (int slot = 0; slot < 36; slot++) {
            ItemStack item = player.getInventory().getItem(slot);
            if (item == null || item.getType().isAir()) continue;
            Integer value = coinValues.get(item.getType());
            if (value == null) continue;
            gained += value * item.getAmount();
            player.getInventory().setItem(slot, null);
        }
        if (gained > 0) {
            int total = coins.merge(player.getUniqueId(), gained, Integer::sum);
            player.giveExpLevels(gained);
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
            player.sendMessage("§6+" + gained + " 金币 §8| §e当前 " + total + " 金币");
        }
    }

    private List<Player> ordered(PartySession session) {
        return session.players().stream().sorted(Comparator.comparingInt((Player player) -> coins.getOrDefault(player.getUniqueId(), 0)).reversed()).toList();
    }

    private void showRanks(PartySession session) {
        List<Player> ordered = ordered(session);
        session.broadcast("rank-title");
        int index = 0;
        int rank = 0;
        int lastCoins = Integer.MIN_VALUE;
        for (Player player : ordered) {
            index++;
            int value = coins.getOrDefault(player.getUniqueId(), 0);
            if (value != lastCoins) rank = index;
            if (rank > 3) break;
            session.broadcast("rank-row", "rank", rank, "player", player.getName(), "coins", value);
            lastCoins = value;
        }
    }

    @Override public void timeout(PartySession session) { finishByCoins(session); }

    private void finishByCoins(PartySession session) {
        if (finalized) return;
        finalized = true;
        List<Player> ordered = ordered(session);
        int rank = 0;
        int index = 0;
        int lastCoins = Integer.MIN_VALUE;
        List<Player> winners = new ArrayList<>();
        int topCoins = ordered.isEmpty() ? 0 : coins.getOrDefault(ordered.get(0).getUniqueId(), 0);
        for (Player player : ordered) {
            int value = coins.getOrDefault(player.getUniqueId(), 0);
            index++;
            if (value != lastCoins) rank = index;
            if (rank <= 3) {
                session.points(player, 4 - rank);
                session.broadcast("rank-row", "rank", rank, "player", player.getName(), "coins", value);
            }
            if (value == topCoins) winners.add(player);
            lastCoins = value;
        }
        session.finish(winners);
    }

    @Override public void stopped(PartySession session) {
        coins.clear();
        finalized = false;
        lastRankSecond = -10;
    }
}
