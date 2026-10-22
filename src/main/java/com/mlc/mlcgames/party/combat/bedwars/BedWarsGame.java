package com.mlc.mlcgames.party.combat.bedwars;

import com.mlc.mlcgames.party.core.Cuboid;
import com.mlc.mlcgames.party.core.PartyConfig;
import com.mlc.mlcgames.party.core.PartyGame;
import com.mlc.mlcgames.party.core.PartySession;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Silverfish;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.Villager;
import org.bukkit.entity.WanderingTrader;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Locale;
import java.util.UUID;

/**
 * Bedwars rules migrated from the source datapack. The source's map reset functions are
 * represented as separate configured arenas; rule modes and team count are selected in game.yml.
 */
public final class BedWarsGame implements PartyGame {
    private static final NamespacedKey XP_RESOURCE_KEY = Objects.requireNonNull(NamespacedKey.fromString("mlcgames:bedwars_xp_value"));
    private static final NamespacedKey CLASS_SELECTOR_KEY = Objects.requireNonNull(NamespacedKey.fromString("mlcgames:bedwars_class_selector"));
    private static final NamespacedKey MAGIC_MILK_KEY = Objects.requireNonNull(NamespacedKey.fromString("mlcgames:bedwars_magic_milk"));
    private static final NamespacedKey ITEM_ACTION_KEY = Objects.requireNonNull(NamespacedKey.fromString("mlcgames:bedwars_item_action"));
    private static final NamespacedKey ITEM_TEAM_KEY = Objects.requireNonNull(NamespacedKey.fromString("mlcgames:bedwars_item_team"));
    private static final int SHOP_PAGE_SIZE = 45;
    private static final String[] TEAM_KEYS = {"red", "blue", "yellow", "green"};
    private static final Material[] TEAM_WOOL = {
            Material.RED_WOOL, Material.BLUE_WOOL, Material.YELLOW_WOOL, Material.LIME_WOOL
    };
    private static final Material[] TEAM_BEDS = {
            Material.RED_BED, Material.BLUE_BED, Material.YELLOW_BED, Material.LIME_BED
    };
    private static final NamedTextColor[] TEAM_COLORS = {
            NamedTextColor.RED, NamedTextColor.BLUE, NamedTextColor.YELLOW, NamedTextColor.GREEN
    };
    private static final Color[] LEATHER_COLORS = {
            Color.RED, Color.BLUE, Color.YELLOW, Color.LIME
    };
    private static final Material[] PICKAXES = {
            Material.WOODEN_PICKAXE, Material.STONE_PICKAXE, Material.IRON_PICKAXE, Material.DIAMOND_PICKAXE
    };
    private static final Material[] AXES = {
            Material.WOODEN_AXE, Material.STONE_AXE, Material.IRON_AXE, Material.DIAMOND_AXE
    };
    private static final List<Product> PRODUCTS = List.of(
            new Product("wool", Material.WHITE_WOOL, Material.IRON_INGOT, 4, 16, "item", 0),
            new Product("planks", Material.DARK_OAK_PLANKS, Material.GOLD_INGOT, 4, 16, "item", 0),
            new Product("endstone", Material.END_STONE, Material.IRON_INGOT, 24, 12, "item", 0),
            new Product("blast-glass", Material.WHITE_STAINED_GLASS, Material.IRON_INGOT, 12, 4, "item", 0),
            new Product("cut-sandstone", Material.CUT_SANDSTONE, Material.IRON_INGOT, 16, 16, "item", 0),
            new Product("obsidian", Material.OBSIDIAN, Material.EMERALD, 4, 4, "item", 0),
            new Product("ladders", Material.LADDER, Material.IRON_INGOT, 4, 16, "item", 0),
            new Product("shears", Material.SHEARS, Material.IRON_INGOT, 20, 1, "shears", 0),
            new Product("bridge-egg", Material.EGG, Material.EMERALD, 1, 1, "item", 0),
            new Product("stone-sword", Material.STONE_SWORD, Material.IRON_INGOT, 10, 1, "sword", 1),
            new Product("iron-sword", Material.IRON_SWORD, Material.GOLD_INGOT, 7, 1, "sword", 2),
            new Product("diamond-sword", Material.DIAMOND_SWORD, Material.EMERALD, 3, 1, "sword", 3),
            new Product("bow", Material.BOW, Material.GOLD_INGOT, 12, 1, "item", 0),
            new Product("bow-power", Material.BOW, Material.GOLD_INGOT, 20, 1, "item", 0),
            new Product("bow-quality", Material.BOW, Material.EMERALD, 6, 1, "item", 0),
            new Product("arrows", Material.ARROW, Material.GOLD_INGOT, 1, 4, "item", 0),
            new Product("poison-arrows", Material.TIPPED_ARROW, Material.GOLD_INGOT, 2, 4, "item", 0),
            new Product("invul-scroll", Material.PAPER, Material.EMERALD, 2, 1, "item", 0),
            new Product("pickaxe-wood", Material.WOODEN_PICKAXE, Material.IRON_INGOT, 10, 1, "pickaxe", 1),
            new Product("axe-wood", Material.WOODEN_AXE, Material.IRON_INGOT, 10, 1, "axe", 1),
            new Product("pickaxe-iron", Material.IRON_PICKAXE, Material.IRON_INGOT, 20, 1, "pickaxe", 2),
            new Product("axe-iron", Material.IRON_AXE, Material.IRON_INGOT, 20, 1, "axe", 2),
            new Product("pickaxe-diamond", Material.DIAMOND_PICKAXE, Material.GOLD_INGOT, 6, 1, "pickaxe", 3),
            new Product("axe-diamond", Material.DIAMOND_AXE, Material.GOLD_INGOT, 6, 1, "axe", 3),
            new Product("chainmail", Material.CHAINMAIL_BOOTS, Material.IRON_INGOT, 40, 1, "armor", 1),
            new Product("iron-armor", Material.IRON_BOOTS, Material.GOLD_INGOT, 12, 1, "armor", 2),
            new Product("diamond-armor", Material.DIAMOND_BOOTS, Material.EMERALD, 6, 1, "armor", 3),
            new Product("golden-apple", Material.GOLDEN_APPLE, Material.GOLD_INGOT, 3, 1, "item", 0),
            new Product("enchanted-apple", Material.ENCHANTED_GOLDEN_APPLE, Material.EMERALD, 3, 1, "item", 0),
            new Product("magic-milk", Material.MILK_BUCKET, Material.GOLD_INGOT, 4, 1, "item", 0),
            new Product("invisibility-potion", Material.POTION, Material.EMERALD, 2, 1, "invisibility-potion", 0),
            new Product("jump-potion", Material.POTION, Material.EMERALD, 1, 1, "jump-potion", 0),
            new Product("speed-potion", Material.POTION, Material.EMERALD, 1, 1, "speed-potion", 0),
            new Product("strength-potion", Material.POTION, Material.EMERALD, 3, 1, "strength-potion", 0),
            new Product("resistance-potion", Material.POTION, Material.EMERALD, 3, 1, "resistance-potion", 0),
            new Product("healing-potion", Material.POTION, Material.GOLD_INGOT, 3, 1, "healing-potion", 0),
            new Product("water-bucket", Material.WATER_BUCKET, Material.GOLD_INGOT, 4, 1, "item", 0),
            new Product("tnt-sheep", Material.SHEEP_SPAWN_EGG, Material.GOLD_INGOT, 8, 1, "item", 0),
            new Product("wind-charge", Material.WIND_CHARGE, Material.IRON_INGOT, 40, 1, "item", 0),
            new Product("ender-pearl", Material.ENDER_PEARL, Material.EMERALD, 4, 1, "item", 0),
            new Product("fireball", Material.BLAZE_POWDER, Material.IRON_INGOT, 40, 1, "item", 0),
            new Product("sponge", Material.SPONGE, Material.IRON_INGOT, 40, 1, "item", 0),
            new Product("shield", Material.SHIELD, Material.EMERALD, 2, 1, "item", 0),
            new Product("mace", Material.MACE, Material.DIAMOND, 4, 1, "item", 0, Material.EMERALD, 6),
            new Product("knockback-stick", Material.STICK, Material.GOLD_INGOT, 8, 1, "item", 0),
            new Product("feather", Material.FEATHER, Material.GOLD_INGOT, 6, 1, "item", 0),
            new Product("tower", Material.ZOMBIE_SPAWN_EGG, Material.IRON_INGOT, 48, 1, "item", 0),
            new Product("golem", Material.IRON_GOLEM_SPAWN_EGG, Material.IRON_INGOT, 120, 1, "item", 0),
            new Product("bugs", Material.SNOWBALL, Material.IRON_INGOT, 24, 2, "item", 0),
            new Product("return-scroll", Material.PAPER, Material.GOLD_INGOT, 12, 1, "item", 0),
            new Product("rescue-platform", Material.BLAZE_ROD, Material.GOLD_INGOT, 12, 1, "item", 0)
    );
    private static final List<Product> XP_PRODUCTS = List.of(
            new Product("wool", Material.WHITE_WOOL, Material.EXPERIENCE_BOTTLE, 4, 16, "item", 0),
            new Product("planks", Material.DARK_OAK_PLANKS, Material.EXPERIENCE_BOTTLE, 80, 16, "item", 0),
            new Product("endstone", Material.END_STONE, Material.EXPERIENCE_BOTTLE, 24, 12, "item", 0),
            new Product("blast-glass", Material.WHITE_STAINED_GLASS, Material.EXPERIENCE_BOTTLE, 12, 4, "item", 0),
            new Product("cut-sandstone", Material.CUT_SANDSTONE, Material.EXPERIENCE_BOTTLE, 16, 16, "item", 0),
            new Product("obsidian", Material.OBSIDIAN, Material.EXPERIENCE_BOTTLE, 1200, 4, "item", 0),
            new Product("ladders", Material.LADDER, Material.EXPERIENCE_BOTTLE, 4, 16, "item", 0),
            new Product("shears", Material.SHEARS, Material.EXPERIENCE_BOTTLE, 20, 1, "shears", 0),
            new Product("bridge-egg", Material.EGG, Material.EXPERIENCE_BOTTLE, 300, 1, "item", 0),
            new Product("stone-sword", Material.STONE_SWORD, Material.EXPERIENCE_BOTTLE, 10, 1, "sword", 1),
            new Product("iron-sword", Material.IRON_SWORD, Material.EXPERIENCE_BOTTLE, 140, 1, "sword", 2),
            new Product("diamond-sword", Material.DIAMOND_SWORD, Material.EXPERIENCE_BOTTLE, 900, 1, "sword", 3),
            new Product("bow", Material.BOW, Material.EXPERIENCE_BOTTLE, 240, 1, "item", 0),
            new Product("bow-power", Material.BOW, Material.EXPERIENCE_BOTTLE, 400, 1, "item", 0),
            new Product("bow-quality", Material.BOW, Material.EXPERIENCE_BOTTLE, 1800, 1, "item", 0),
            new Product("arrows", Material.ARROW, Material.EXPERIENCE_BOTTLE, 20, 4, "item", 0),
            new Product("poison-arrows", Material.TIPPED_ARROW, Material.EXPERIENCE_BOTTLE, 40, 4, "item", 0),
            new Product("invul-scroll", Material.PAPER, Material.EXPERIENCE_BOTTLE, 600, 1, "item", 0),
            new Product("pickaxe-wood", Material.WOODEN_PICKAXE, Material.EXPERIENCE_BOTTLE, 10, 1, "pickaxe", 1),
            new Product("axe-wood", Material.WOODEN_AXE, Material.EXPERIENCE_BOTTLE, 10, 1, "axe", 1),
            new Product("pickaxe-iron", Material.IRON_PICKAXE, Material.EXPERIENCE_BOTTLE, 20, 1, "pickaxe", 2),
            new Product("axe-iron", Material.IRON_AXE, Material.EXPERIENCE_BOTTLE, 20, 1, "axe", 2),
            new Product("pickaxe-diamond", Material.DIAMOND_PICKAXE, Material.EXPERIENCE_BOTTLE, 120, 1, "pickaxe", 3),
            new Product("axe-diamond", Material.DIAMOND_AXE, Material.EXPERIENCE_BOTTLE, 120, 1, "axe", 3),
            new Product("chainmail", Material.CHAINMAIL_BOOTS, Material.EXPERIENCE_BOTTLE, 40, 1, "armor", 1),
            new Product("iron-armor", Material.IRON_BOOTS, Material.EXPERIENCE_BOTTLE, 240, 1, "armor", 2),
            new Product("diamond-armor", Material.DIAMOND_BOOTS, Material.EXPERIENCE_BOTTLE, 1800, 1, "armor", 3),
            new Product("golden-apple", Material.GOLDEN_APPLE, Material.EXPERIENCE_BOTTLE, 60, 1, "item", 0),
            new Product("enchanted-apple", Material.ENCHANTED_GOLDEN_APPLE, Material.EXPERIENCE_BOTTLE, 900, 1, "item", 0),
            new Product("magic-milk", Material.MILK_BUCKET, Material.EXPERIENCE_BOTTLE, 80, 1, "item", 0),
            new Product("invisibility-potion", Material.POTION, Material.EXPERIENCE_BOTTLE, 600, 1, "invisibility-potion", 0),
            new Product("jump-potion", Material.POTION, Material.EXPERIENCE_BOTTLE, 300, 1, "jump-potion", 0),
            new Product("speed-potion", Material.POTION, Material.EXPERIENCE_BOTTLE, 300, 1, "speed-potion", 0),
            new Product("strength-potion", Material.POTION, Material.EXPERIENCE_BOTTLE, 900, 1, "strength-potion", 0),
            new Product("resistance-potion", Material.POTION, Material.EXPERIENCE_BOTTLE, 900, 1, "resistance-potion", 0),
            new Product("healing-potion", Material.POTION, Material.EXPERIENCE_BOTTLE, 60, 1, "healing-potion", 0),
            new Product("water-bucket", Material.WATER_BUCKET, Material.EXPERIENCE_BOTTLE, 80, 1, "item", 0),
            new Product("tnt-sheep", Material.SHEEP_SPAWN_EGG, Material.EXPERIENCE_BOTTLE, 160, 1, "item", 0),
            new Product("wind-charge", Material.WIND_CHARGE, Material.EXPERIENCE_BOTTLE, 40, 1, "item", 0),
            new Product("ender-pearl", Material.ENDER_PEARL, Material.EXPERIENCE_BOTTLE, 1200, 1, "item", 0),
            new Product("fireball", Material.BLAZE_POWDER, Material.EXPERIENCE_BOTTLE, 40, 1, "item", 0),
            new Product("sponge", Material.SPONGE, Material.EXPERIENCE_BOTTLE, 40, 1, "item", 0),
            new Product("shield", Material.SHIELD, Material.EXPERIENCE_BOTTLE, 600, 1, "item", 0),
            new Product("mace", Material.MACE, Material.DIAMOND, 4, 1, "item", 0, Material.EXPERIENCE_BOTTLE, 1800),
            new Product("knockback-stick", Material.STICK, Material.EXPERIENCE_BOTTLE, 160, 1, "item", 0),
            new Product("exchange-iron", Material.IRON_INGOT, Material.EXPERIENCE_BOTTLE, 4, 4, "xp-exchange", 0),
            new Product("exchange-gold", Material.GOLD_INGOT, Material.EXPERIENCE_BOTTLE, 20, 1, "xp-exchange", 0),
            new Product("exchange-emerald", Material.EMERALD, Material.EXPERIENCE_BOTTLE, 300, 1, "xp-exchange", 0),
            new Product("feather", Material.FEATHER, Material.EXPERIENCE_BOTTLE, 120, 1, "item", 0),
            new Product("tower", Material.ZOMBIE_SPAWN_EGG, Material.EXPERIENCE_BOTTLE, 48, 1, "item", 0),
            new Product("golem", Material.IRON_GOLEM_SPAWN_EGG, Material.EXPERIENCE_BOTTLE, 120, 1, "item", 0),
            new Product("bugs", Material.SNOWBALL, Material.EXPERIENCE_BOTTLE, 24, 2, "item", 0),
            new Product("return-scroll", Material.PAPER, Material.EXPERIENCE_BOTTLE, 240, 1, "item", 0),
            new Product("rescue-platform", Material.BLAZE_ROD, Material.EXPERIENCE_BOTTLE, 240, 1, "item", 0)
    );
    private static final List<Product> TEAM_UPGRADES = List.of(
            new Product("sharpness", Material.DIAMOND_SWORD, Material.DIAMOND, 0, 1, "upgrade", 0),
            new Product("forge", Material.BLAST_FURNACE, Material.DIAMOND, 0, 1, "upgrade", 0),
            new Product("protection", Material.IRON_CHESTPLATE, Material.DIAMOND, 0, 1, "upgrade", 0),
            new Product("haste", Material.GOLDEN_PICKAXE, Material.DIAMOND, 0, 1, "upgrade", 0),
            new Product("trap-mining", Material.WOODEN_PICKAXE, Material.DIAMOND, 0, 1, "upgrade", 0),
            new Product("trap-counter", Material.IRON_SWORD, Material.DIAMOND, 0, 1, "upgrade", 0),
            new Product("heal-pool", Material.BEACON, Material.DIAMOND, 0, 1, "upgrade", 0),
            new Product("fast-respawn", Material.CLOCK, Material.DIAMOND, 0, 1, "upgrade", 0),
            new Product("speed-team", Material.POTION, Material.DIAMOND, 6, 1, "upgrade", 0),
            new Product("jump-team", Material.POTION, Material.DIAMOND, 8, 1, "upgrade", 0),
            new Product("trap-blindness", Material.COBWEB, Material.DIAMOND, 0, 1, "upgrade", 0),
            new Product("trap-alarm", Material.BELL, Material.DIAMOND, 0, 1, "upgrade", 0)
    );

    private final Map<UUID, Integer> teams = new HashMap<>();
    private final Map<UUID, Integer> teamSlots = new HashMap<>();
    private final Map<UUID, Scoreboard> originalScoreboards = new HashMap<>();
    private final Map<UUID, Integer> pickaxeTiers = new HashMap<>();
    private final Map<UUID, Integer> axeTiers = new HashMap<>();
    private final Map<UUID, Integer> armorTiers = new HashMap<>();
    private final Set<UUID> shears = new HashSet<>();
    private final Set<UUID> finalOut = new HashSet<>();
    private final Map<UUID, Integer> respawnAt = new HashMap<>();
    private final Map<Integer, Integer[]> upgrades = new HashMap<>();
    private final Map<Integer, List<String>> teamTraps = new HashMap<>();
    private final Map<Integer, Integer> teamLives = new HashMap<>();
    private final Map<UUID, Integer> xpBalances = new HashMap<>();
    private final Map<UUID, Integer> classes = new HashMap<>();
    private final Map<UUID, Integer> zombieExpiresAt = new HashMap<>();
    private final Map<UUID, Integer> trapImmunityExpiresAt = new HashMap<>();
    private final Map<UUID, Integer> invulnerableUntil = new HashMap<>();
    private final Map<UUID, Double> previousKnockbackResistance = new HashMap<>();
    private final Map<UUID, Channel> returnChannels = new HashMap<>();
    private final Map<UUID, Integer> featherUseAt = new HashMap<>();
    private final Map<UUID, Integer> rescueCooldownUntil = new HashMap<>();
    private final Map<BlockPoint, Integer> rescueBlocksExpireAt = new HashMap<>();
    private final Map<UUID, Integer> supportExpiresAt = new HashMap<>();
    private final Map<UUID, Integer> supportTeams = new HashMap<>();
    private final Map<UUID, Integer> sheepDetonateAt = new HashMap<>();
    private final Map<UUID, Integer> bridgeEggExpireAt = new HashMap<>();
    private final List<TowerBuild> towerBuilds = new ArrayList<>();
    private final Map<BlockPoint, BlockData> permanentBedData = new HashMap<>();
    private final Map<BlockPoint, Integer> bedTeams = new HashMap<>();
    private final Map<Integer, List<Location>> beds = new HashMap<>();
    private final List<Location> zombieBases = new ArrayList<>();
    private final Map<Integer, Set<UUID>> trapInside = new HashMap<>();
    private final Map<Integer, Integer> forgeNextAt = new HashMap<>();
    private final Set<BlockPoint> placedBlocks = new HashSet<>();
    private final Set<Integer> destroyedBeds = new HashSet<>();
    private final Set<Material> buildingMaterials = new HashSet<>();
    private final List<Generator> generators = new ArrayList<>();
    private Mode mode = Mode.NORMAL;
    private String witherSpawnGroup = "wither-spawn";
    private int activeTeamCount = 4;
    private int nextZombieWaveAt;
    private Cuboid build;
    private Scoreboard scoreboard;
    private Set<Integer> liveTeams = Set.of();
    private boolean bedsClosed;
    private boolean finalBattle;
    private boolean gameStarted;

    @Override public boolean lastPlayerWins() { return false; }
    @Override public int maximumDurationSeconds() { return 3600; }

    @Override
    public void validate(PartyConfig config, PartyConfig.Arena arena) {
        build = arena.region("build");
        activeTeamCount = config.integer("team-count", 2, 4);
        if (activeTeamCount != 2 && activeTeamCount != 4)
            throw new IllegalArgumentException("team-count 只支持源码中的两队或四队");
        String configuredMode = config.rules.getString("mode", "normal").toLowerCase(Locale.ROOT);
        mode = switch (configuredMode) {
            case "normal" -> Mode.NORMAL;
            case "no-beds" -> Mode.NO_BEDS;
            case "unlocked" -> Mode.UNLOCKED;
            case "xp" -> Mode.XP;
            case "double-resources" -> Mode.DOUBLE_RESOURCES;
            case "class" -> Mode.CLASS;
            case "zombie-wave" -> Mode.ZOMBIE_WAVE;
            case "permanent-bed" -> Mode.PERMANENT_BED;
            default -> throw new IllegalArgumentException("mode 必须是 normal、no-beds、unlocked、xp、double-resources、class、zombie-wave 或 permanent-bed");
        };
        buildingMaterials.addAll(config.materials("building-materials"));
        config.integer("respawn-seconds", 2, 30);
        config.integer("events.bed-gone-seconds", 300, 2400);
        config.integer("events.final-battle-seconds", 600, 3000);
        config.integer("generator.iron-ticks", 10, 400);
        config.integer("generator.gold-ticks", 20, 1200);
        config.integer("generator.diamond-start-seconds", 15, 180);
        config.integer("generator.emerald-start-seconds", 15, 240);
        config.integer("shop-radius", 2, 12);
        config.integer("trap-radius", 4, 24);
        if (!arena.contains(arena.spectator())) throw new IllegalArgumentException("points.spectator 必须位于比赛 bounds 内");
        int playersPerTeam = (config.integer("players.max", 2, 64) + activeTeamCount - 1) / activeTeamCount;
        for (int team = 0; team < activeTeamCount; team++) {
            String key = TEAM_KEYS[team];
            if (arena.teamSpawns().getOrDefault(key, List.of()).size() < playersPerTeam)
                throw new IllegalArgumentException("points.team-spawns." + key + " 至少需要 " + playersPerTeam + " 个点位");
            List<Location> bed = arena.group("bed-" + key);
            if (bed.size() != 2) throw new IllegalArgumentException("points.groups.bed-" + key + " 必须配置床的两个方块位置");
            for (Location location : bed) if (!build.contains(location.getBlockX(), location.getBlockY(), location.getBlockZ()))
                throw new IllegalArgumentException("床的两个方块必须位于 regions.build 内");
            if (arena.group("iron-" + key).isEmpty() || arena.group("gold-" + key).isEmpty())
                throw new IllegalArgumentException("每队必须配置铁锭和金锭生成点");
        }
        for (int team = 0; team < activeTeamCount; team++) {
            List<Location> bed = arena.group("bed-" + TEAM_KEYS[team]);
            for (Location location : bed) if (location.getBlock().getType() != TEAM_BEDS[team])
                throw new IllegalArgumentException("points.groups.bed-" + TEAM_KEYS[team] + " 必须标记正确队伍颜色的床");
            int adjacency = Math.abs(bed.get(0).getBlockX() - bed.get(1).getBlockX())
                    + Math.abs(bed.get(0).getBlockY() - bed.get(1).getBlockY())
                    + Math.abs(bed.get(0).getBlockZ() - bed.get(1).getBlockZ());
            if (adjacency != 1)
                throw new IllegalArgumentException("points.groups.bed-" + TEAM_KEYS[team] + " 必须是相邻的两个床方块");
        }
        if (mode == Mode.ZOMBIE_WAVE) {
            for (int team = 0; team < TEAM_KEYS.length; team++) {
                List<Location> bed = arena.group("bed-" + TEAM_KEYS[team]);
                if (bed.size() != 2)
                    throw new IllegalArgumentException("僵尸潮需要配置四支队伍各自的两个床方块，以还原源地图的四个床标记");
                for (Location location : bed) {
                    if (!build.contains(location.getBlockX(), location.getBlockY(), location.getBlockZ()))
                        throw new IllegalArgumentException("僵尸潮的四组床点都必须位于 regions.build 内");
                    if (location.getBlock().getType() != TEAM_BEDS[team])
                        throw new IllegalArgumentException("points.groups.bed-" + TEAM_KEYS[team] + " 必须标记正确队伍颜色的床");
                }
                int adjacency = Math.abs(bed.get(0).getBlockX() - bed.get(1).getBlockX())
                        + Math.abs(bed.get(0).getBlockY() - bed.get(1).getBlockY())
                        + Math.abs(bed.get(0).getBlockZ() - bed.get(1).getBlockZ());
                if (adjacency != 1)
                    throw new IllegalArgumentException("points.groups.bed-" + TEAM_KEYS[team] + " 必须是相邻的两个床方块");
            }
        }
        arena.group("diamond");
        arena.group("emerald");
        arena.group("shops");
        witherSpawnGroup = config.rules.getString("events.wither-spawn-group", "wither-spawn");
        if (arena.group(witherSpawnGroup).size() != 1)
            throw new IllegalArgumentException("points.groups.wither-spawn 必须配置一个凋灵生成点");
        if (config.integer("events.final-battle-seconds", 600, 3000) <= config.integer("events.bed-gone-seconds", 300, 2400))
            throw new IllegalArgumentException("events.final-battle-seconds 必须晚于 events.bed-gone-seconds");
        if (config.integer("max-seconds", 10, maximumDurationSeconds())
                <= config.integer("events.final-battle-seconds", 600, 3000))
            throw new IllegalArgumentException("max-seconds 必须晚于 events.final-battle-seconds");
    }

    @Override public List<Cuboid> writableRegions(PartyConfig.Arena arena) { return List.of(build); }

    @Override
    public void start(PartySession session) {
        gameStarted = true;
        bedsClosed = false;
        finalBattle = false;
        placedBlocks.clear();
        bedTeams.clear();
        beds.clear();
        zombieBases.clear();
        destroyedBeds.clear();
        generators.clear();
        finalOut.clear();
        respawnAt.clear();
        teams.clear();
        teamSlots.clear();
        pickaxeTiers.clear();
        axeTiers.clear();
        armorTiers.clear();
        shears.clear();
        upgrades.clear();
        teamTraps.clear();
        forgeNextAt.clear();
        trapInside.clear();
        invulnerableUntil.clear();
        previousKnockbackResistance.clear();
        returnChannels.clear();
        featherUseAt.clear();
        rescueCooldownUntil.clear();
        rescueBlocksExpireAt.clear();
        supportExpiresAt.clear();
        supportTeams.clear();
        sheepDetonateAt.clear();
        bridgeEggExpireAt.clear();
        towerBuilds.clear();
        liveTeams = Set.of();
        for (int i = 0; i < 4; i++) {
            upgrades.put(i, new Integer[9]);
            Arrays.fill(upgrades.get(i), 0);
            teamTraps.put(i, new ArrayList<>());
            trapInside.put(i, new HashSet<>());
        }
        if (mode == Mode.UNLOCKED) {
            for (int team = 0; team < activeTeamCount; team++) {
                setUpgrade(team, 0, 1);
                setUpgrade(team, 1, 1);
                setUpgrade(team, 2, 1);
                setUpgrade(team, 3, 3);
                setUpgrade(team, 4, 1);
                setUpgrade(team, 7, 1);
                setUpgrade(team, 8, 1);
            }
        }

        List<Player> players = new ArrayList<>(session.players());
        Collections.shuffle(players);
        int playingTeams = Math.min(activeTeamCount, players.size());
        int[] teamCounts = new int[4];
        scoreboard = Objects.requireNonNull(Bukkit.getScoreboardManager()).getNewScoreboard();
        Team[] boardTeams = new Team[activeTeamCount];
        for (int i = 0; i < activeTeamCount; i++) {
            boardTeams[i] = scoreboard.registerNewTeam("bedwars_" + TEAM_KEYS[i]);
            boardTeams[i].color(TEAM_COLORS[i]);
            boardTeams[i].setAllowFriendlyFire(false);
        }
        session.resetPlayers();
        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            int team = i % playingTeams;
            teams.put(player.getUniqueId(), team);
            teamSlots.put(player.getUniqueId(), teamCounts[team]++);
            pickaxeTiers.put(player.getUniqueId(), 0);
            axeTiers.put(player.getUniqueId(), 0);
            armorTiers.put(player.getUniqueId(), 0);
            xpBalances.put(player.getUniqueId(), 0);
            classes.remove(player.getUniqueId());
            originalScoreboards.put(player.getUniqueId(), player.getScoreboard());
            boardTeams[team].addEntry(player.getName());
            player.setScoreboard(scoreboard);
            spawn(session, player);
        }
        liveTeams = Set.copyOf(teams.values());
        teamLives.clear();
        if (mode == Mode.PERMANENT_BED) {
            int lives = activeTeamCount == 2 ? 10 : 6;
            for (int team : liveTeams) teamLives.put(team, lives);
        }
        if (mode == Mode.UNLOCKED) {
            for (int team : liveTeams) forgeNextAt.put(team, 60 * 20);
        }
        nextZombieWaveAt = 120 * 20;
        zombieExpiresAt.clear();
        trapImmunityExpiresAt.clear();
        permanentBedData.clear();
        setupBeds(session);
        setupGenerators(session);
        if (mode == Mode.DOUBLE_RESOURCES) {
            for (Player player : players) player.getInventory().addItem(new ItemStack(Material.IRON_INGOT, 10));
        }
    }

    private void setupBeds(PartySession session) {
        for (int team = 0; team < activeTeamCount; team++) {
            List<Location> teamBeds = session.arena.groups().getOrDefault("bed-" + TEAM_KEYS[team], List.of()).stream().map(Location::clone).toList();
            for (Location location : teamBeds) {
                Block block = location.getBlock();
                if (block.getType() != TEAM_BEDS[team]) throw new IllegalArgumentException("points.groups.bed-" + TEAM_KEYS[team] + " 必须对应 " + TEAM_BEDS[team]);
                bedTeams.put(key(block), team);
                permanentBedData.put(key(block), block.getBlockData().clone());
            }
            beds.put(team, teamBeds);
            zombieBases.add(teamBeds.getFirst().clone().add(0.5, 1, 0.5));
            if (mode == Mode.NO_BEDS) {
                for (Location location : teamBeds) session.set(location.getBlock(), Material.AIR);
                destroyedBeds.add(team);
            }
        }
        for (int team = activeTeamCount; team < TEAM_KEYS.length; team++) {
            List<Location> teamBeds = session.arena.groups().getOrDefault("bed-" + TEAM_KEYS[team], List.of());
            if (teamBeds.size() == 2) zombieBases.add(teamBeds.getFirst().clone().add(0.5, 1, 0.5));
        }
        if (mode == Mode.NO_BEDS) bedsClosed = true;
    }

    private void setupGenerators(PartySession session) {
        for (int team = 0; team < activeTeamCount; team++) {
            String suffix = TEAM_KEYS[team];
            generators.add(new Generator(team, Material.IRON_INGOT, session.arena.group("iron-" + suffix)));
            generators.add(new Generator(team, Material.GOLD_INGOT, session.arena.group("gold-" + suffix)));
        }
        Generator diamond = new Generator(-1, Material.DIAMOND, session.arena.group("diamond"));
        diamond.nextTick = session.config.integer("generator.diamond-start-seconds", 15, 180) * 20;
        generators.add(diamond);
        Generator emerald = new Generator(-1, Material.EMERALD, session.arena.group("emerald"));
        emerald.nextTick = session.config.integer("generator.emerald-start-seconds", 15, 240) * 20;
        generators.add(emerald);
    }

    private void spawn(PartySession session, Player player) {
        int team = team(player);
        player.leaveVehicle();
        player.closeInventory();
        player.getInventory().clear();
        player.clearActivePotionEffects();
        player.setGameMode(GameMode.SURVIVAL);
        player.setAllowFlight(false);
        player.setGlowing(false);
        player.setFireTicks(0);
        player.setFallDistance(0);
        player.setVelocity(new org.bukkit.util.Vector());
        player.setHealth(maxHealth(player));
        List<Location> points = session.arena.teamSpawns().get(TEAM_KEYS[team]);
        int slot = teamSlots.getOrDefault(player.getUniqueId(), 0) % points.size();
        if (!player.teleport(points.get(slot))) throw new IllegalStateException("队伍出生点传送失败");
        if (mode != Mode.CLASS) {
            giveSword(session, player, 0);
            giveTools(player);
        } else {
            classes.remove(player.getUniqueId());
            giveClassSelector(session, player);
        }
        applyArmor(player);
        applyTeamEffects(player);
        if (mode == Mode.UNLOCKED) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 80, 0, true, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 80, 1, true, false));
        }
    }

    private void giveSword(PartySession session, Player player, int tier) {
        Material material = switch (tier) {
            case 1 -> Material.STONE_SWORD;
            case 2 -> Material.IRON_SWORD;
            case 3 -> Material.DIAMOND_SWORD;
            default -> Material.WOODEN_SWORD;
        };
        ItemStack sword = new ItemStack(material);
        int sharpness = upgrade(team(player), 0);
        if (sharpness > 0) sword.addUnsafeEnchantment(Enchantment.SHARPNESS, sharpness);
        player.getInventory().setItem(0, sword);
    }

    private void giveTools(Player player) {
        int pick = pickaxeTiers.getOrDefault(player.getUniqueId(), 0);
        int axe = axeTiers.getOrDefault(player.getUniqueId(), 0);
        player.getInventory().setItem(1, pick <= 0 ? null : new ItemStack(pickaxeMaterial(pick)));
        player.getInventory().setItem(2, axe <= 0 ? null : new ItemStack(axeMaterial(axe)));
        if (shears.contains(player.getUniqueId())) player.getInventory().setItem(3, new ItemStack(Material.SHEARS));
    }

    private Material pickaxeMaterial(int tier) {
        return switch (tier) {
            case 1 -> Material.WOODEN_PICKAXE;
            case 2 -> Material.IRON_PICKAXE;
            case 3 -> Material.DIAMOND_PICKAXE;
            default -> null;
        };
    }

    private Material axeMaterial(int tier) {
        return switch (tier) {
            case 1 -> Material.WOODEN_AXE;
            case 2 -> Material.IRON_AXE;
            case 3 -> Material.DIAMOND_AXE;
            default -> null;
        };
    }

    private void applyArmor(Player player) {
        int team = team(player);
        int tier = armorTiers.getOrDefault(player.getUniqueId(), 0);
        PlayerInventory inventory = player.getInventory();
        if (tier == 0) {
            inventory.setHelmet(leather(Material.LEATHER_HELMET, team));
            inventory.setChestplate(leather(Material.LEATHER_CHESTPLATE, team));
            inventory.setLeggings(leather(Material.LEATHER_LEGGINGS, team));
            inventory.setBoots(leather(Material.LEATHER_BOOTS, team));
        } else {
            Material helmet = tier == 1 ? Material.CHAINMAIL_HELMET : tier == 2 ? Material.IRON_HELMET : Material.DIAMOND_HELMET;
            Material chest = tier == 1 ? Material.CHAINMAIL_CHESTPLATE : tier == 2 ? Material.IRON_CHESTPLATE : Material.DIAMOND_CHESTPLATE;
            Material legs = tier == 1 ? Material.CHAINMAIL_LEGGINGS : tier == 2 ? Material.IRON_LEGGINGS : Material.DIAMOND_LEGGINGS;
            Material boots = tier == 1 ? Material.CHAINMAIL_BOOTS : tier == 2 ? Material.IRON_BOOTS : Material.DIAMOND_BOOTS;
            inventory.setHelmet(armorPiece(helmet, team));
            inventory.setChestplate(armorPiece(chest, team));
            inventory.setLeggings(armorPiece(legs, team));
            inventory.setBoots(armorPiece(boots, team));
        }
    }

    private ItemStack leather(Material material, int team) {
        ItemStack item = new ItemStack(material);
        LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
        meta.setColor(LEATHER_COLORS[team]);
        item.setItemMeta(meta);
        return armorPiece(item, team);
    }

    private ItemStack armorPiece(Material material, int team) {
        return armorPiece(new ItemStack(material), team);
    }

    private ItemStack armorPiece(ItemStack item, int team) {
        int protection = upgrade(team, 1);
        if (protection > 0) item.addUnsafeEnchantment(Enchantment.PROTECTION, protection);
        return item;
    }

    private void applyTeamEffects(Player player) {
        int team = team(player);
        int haste = upgrade(team, 2);
        if (haste > 0) player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 20 * 3600, haste - 1, true, false));
        if (upgrade(team, 7) > 0) player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 3600, 0, true, false));
        if (upgrade(team, 8) > 0) player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 20 * 3600, 0, true, false));
    }

    @Override
    public void tick(PartySession session, int elapsed) {
        if (!gameStarted) return;
        if (elapsed == 6 * 60 * 20) session.broadcast("diamond-speed-1");
        else if (elapsed == 9 * 60 * 20) session.broadcast("emerald-speed-1");
        else if (elapsed == 12 * 60 * 20) session.broadcast("diamond-speed-2");
        else if (elapsed == 15 * 60 * 20) session.broadcast("emerald-speed-2");
        else if (elapsed == 18 * 60 * 20) session.broadcast("diamond-speed-3");
        else if (elapsed == 21 * 60 * 20) session.broadcast("emerald-speed-3");
        maintainPermanentBeds(session);
        syncBeds(session);
        if (mode != Mode.PERMANENT_BED && !bedsClosed
                && elapsed >= session.config.integer("events.bed-gone-seconds", 300, 2400) * 20) closeBeds(session, null);
        if (!finalBattle && elapsed >= session.config.integer("events.final-battle-seconds", 600, 3000) * 20) {
            finalBattle = true;
            closeBeds(session, null);
            session.broadcast("final-battle");
            spawnFinalWithers(session);
        }
        if (mode == Mode.UNLOCKED && elapsed % 40 == 0) {
            for (Player player : session.survivors()) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 80, 0, true, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 80, 1, true, false));
            }
        }
        if (mode == Mode.ZOMBIE_WAVE && elapsed >= nextZombieWaveAt) {
            spawnZombieWave(session, elapsed);
            nextZombieWaveAt += 120 * 20;
        }
        expireZombies(elapsed);
        tickItemAbilities(session, elapsed);
        if (mode == Mode.XP) convertXpResources(session);
        checkRespawns(session, elapsed);
        tickGenerators(session, elapsed);
        tickTraps(session);
        tickHealPools(session);
        checkWinner(session);
    }

    private void syncBeds(PartySession session) {
        if (mode == Mode.PERMANENT_BED || mode == Mode.NO_BEDS) return;
        for (int team = 0; team < activeTeamCount; team++) {
            if (!bedAlive(team)) {
                if (!bedsClosed && liveTeams.contains(team)) bedDestroyed(session, team, null);
            }
        }
    }

    private void closeBeds(PartySession session, Player breaker) {
        if (mode == Mode.PERMANENT_BED) return;
        if (bedsClosed) return;
        bedsClosed = true;
        for (int team = 0; team < activeTeamCount; team++) if (liveTeams.contains(team)) bedDestroyed(session, team, breaker);
    }

    private void bedDestroyed(PartySession session, int team, Player breaker) {
        if (!destroyedBeds.add(team)) return;
        for (Location location : beds.getOrDefault(team, List.of())) {
            Block block = location.getBlock();
            bedTeams.remove(key(block));
            if (block.getType() == TEAM_BEDS[team] && session.writable(block)) session.set(block, Material.AIR);
        }
        bedTeams.entrySet().removeIf(entry -> entry.getValue() == team);
        session.broadcast("bed-destroyed", "team", session.config.text("teams." + TEAM_KEYS[team]),
                "player", breaker == null ? session.config.text("events.system") : breaker.getName());
    }

    private boolean bedAlive(int team) {
        List<Location> locations = beds.getOrDefault(team, List.of());
        return !destroyedBeds.contains(team) && locations.size() == 2
                && locations.stream().allMatch(l -> l.getBlock().getType() == TEAM_BEDS[team]);
    }

    private void tickGenerators(PartySession session, int elapsed) {
        for (Generator generator : generators) {
            if (elapsed < generator.nextTick) continue;
            int interval = generatorInterval(session, generator, elapsed);
            for (Location location : generator.points) {
                ItemStack stack = new ItemStack(generator.material);
                if (mode == Mode.XP && generator.material != Material.DIAMOND) markXpResource(stack);
                drop(session, location, stack);
            }
            generator.nextTick = elapsed + interval;
        }
        for (int team : liveTeams) {
            int forge = upgrade(team, 3);
            if (forge < 3 || elapsed < forgeNextAt.getOrDefault(team, Integer.MAX_VALUE)) continue;
            for (Location location : session.arena.group("gold-" + TEAM_KEYS[team])) {
                ItemStack emerald = new ItemStack(Material.EMERALD);
                if (mode == Mode.XP) markXpResource(emerald);
                drop(session, location, emerald);
            }
            forgeNextAt.put(team, elapsed + (forge >= 4 ? 30 : 60) * 20);
        }
    }

    private int generatorInterval(PartySession session, Generator generator, int elapsed) {
        if (generator.material == Material.IRON_INGOT || generator.material == Material.GOLD_INGOT) {
            int base = session.config.integer(generator.material == Material.IRON_INGOT ? "generator.iron-ticks" : "generator.gold-ticks",
                    generator.material == Material.IRON_INGOT ? 10 : 20, generator.material == Material.IRON_INGOT ? 400 : 1200);
            if (mode == Mode.DOUBLE_RESOURCES) base = Math.max(1, base / 2);
            int level = generator.team < 0 ? 0 : upgrade(generator.team, 3);
            if (generator.material == Material.IRON_INGOT && level >= 1) base = 30;
            if (generator.material == Material.GOLD_INGOT && level >= 2) base = 100;
            return Math.max(10, base);
        }
        if (generator.material == Material.DIAMOND) {
            int start = session.config.integer("generator.diamond-start-seconds", 15, 180);
            int seconds = elapsed < 6 * 60 * 20 ? start : elapsed < 12 * 60 * 20 ? 36 : elapsed < 18 * 60 * 20 ? 28 : 20;
            if (mode == Mode.DOUBLE_RESOURCES) seconds = Math.max(1, seconds / 2);
            return seconds * 20;
        }
        int start = session.config.integer("generator.emerald-start-seconds", 15, 240);
        int seconds = elapsed < 9 * 60 * 20 ? start : elapsed < 15 * 60 * 20 ? 50 : elapsed < 21 * 60 * 20 ? 40 : 30;
        if (mode == Mode.DOUBLE_RESOURCES) seconds = Math.max(1, seconds / 2);
        return seconds * 20;
    }

    private void drop(PartySession session, Location source, ItemStack stack) {
        Location location = source.clone().add(0.5, 0.2, 0.5);
        Item item = session.arena.world().dropItem(location, stack);
        item.setPickupDelay(0);
        item.setVelocity(new org.bukkit.util.Vector());
        session.track(item);
    }

    private void markXpResource(ItemStack stack) {
        int value = stack.getType() == Material.IRON_INGOT ? 1
                : stack.getType() == Material.GOLD_INGOT ? 20
                : stack.getType() == Material.EMERALD ? 300 : 0;
        if (value == 0) return;
        ItemMeta meta = stack.getItemMeta();
        meta.getPersistentDataContainer().set(XP_RESOURCE_KEY, PersistentDataType.INTEGER, value);
        stack.setItemMeta(meta);
    }

    private void convertXpResources(PartySession session) {
        for (Player player : session.survivors()) {
            PlayerInventory inventory = player.getInventory();
            int points = 0;
            ItemStack[] contents = inventory.getStorageContents();
            for (int slot = 0; slot < contents.length; slot++) {
                ItemStack stack = contents[slot];
                if (stack == null || stack.getType().isAir()) continue;
                Integer value = stack.getItemMeta().getPersistentDataContainer()
                        .get(XP_RESOURCE_KEY, PersistentDataType.INTEGER);
                if (value == null || value <= 0) continue;
                points = Math.addExact(points, Math.multiplyExact(value, stack.getAmount()));
                contents[slot] = null;
            }
            if (points > 0) {
                inventory.setStorageContents(contents);
                changeXp(session, player, points, true);
            }
        }
    }

    private void changeXp(PartySession session, Player player, int delta, boolean notify) {
        int current = xpBalances.getOrDefault(player.getUniqueId(), 0);
        int next = Math.max(0, Math.addExact(current, delta));
        xpBalances.put(player.getUniqueId(), next);
        if (notify && delta != 0) {
            String key = delta > 0 ? "xp.gained" : "xp.lost";
            player.sendActionBar(session.config.message(key, "amount", Math.abs(delta), "balance", next));
        }
    }

    private void maintainPermanentBeds(PartySession session) {
        if (mode != Mode.PERMANENT_BED) return;
        for (Map.Entry<BlockPoint, BlockData> entry : permanentBedData.entrySet()) {
            BlockPoint point = entry.getKey();
            Block block = point.world().getBlockAt(point.x(), point.y(), point.z());
            if (!session.writable(block)) continue;
            if (!block.getBlockData().getAsString().equals(entry.getValue().getAsString()))
                session.setData(block, entry.getValue().clone());
        }
    }

    private void spawnZombieWave(PartySession session, int elapsed) {
        for (Location base : zombieBases) {
            for (int[] offset : new int[][]{{0, 0}, {1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
                Zombie zombie = base.getWorld().spawn(base.clone().add(offset[0], 0, offset[1]), Zombie.class);
                zombie.setPersistent(true);
                zombie.setRemoveWhenFarAway(false);
                session.track(zombie);
                zombieExpiresAt.put(zombie.getUniqueId(), elapsed + 90 * 20);
            }
        }
        session.broadcast("zombie-wave");
    }

    private void spawnFinalWithers(PartySession session) {
        Location spawn = session.arena.group(session.config.rules.getString("events.wither-spawn-group", witherSpawnGroup)).getFirst();
        for (int team = 0; team < activeTeamCount; team++) {
            int owner = team;
            boolean alive = session.players().stream().anyMatch(player -> team(player) == owner && !finalOut.contains(player.getUniqueId()));
            if (!alive) continue;
            org.bukkit.entity.Wither wither = spawn.getWorld().spawn(spawn, org.bukkit.entity.Wither.class);
            wither.setCustomName("凋灵");
            wither.setCustomNameVisible(true);
            Team scoreboardTeam = scoreboard.getTeam("bedwars_" + TEAM_KEYS[team]);
            if (scoreboardTeam != null) scoreboardTeam.addEntry(wither.getUniqueId().toString());
            session.track(wither);
        }
    }

    private void expireZombies(int elapsed) {
        for (Map.Entry<UUID, Integer> entry : new ArrayList<>(zombieExpiresAt.entrySet())) {
            if (elapsed < entry.getValue()) continue;
            Entity zombie = Bukkit.getEntity(entry.getKey());
            if (zombie != null) zombie.remove();
            zombieExpiresAt.remove(entry.getKey());
        }
    }

    private void tickItemAbilities(PartySession session, int elapsed) {
        for (Map.Entry<UUID, Integer> entry : new ArrayList<>(invulnerableUntil.entrySet())) {
            if (elapsed < entry.getValue()) continue;
            Player player = Bukkit.getPlayer(entry.getKey());
            Double old = previousKnockbackResistance.remove(entry.getKey());
            if (player != null && old != null) {
                var attribute = player.getAttribute(org.bukkit.attribute.Attribute.KNOCKBACK_RESISTANCE);
                if (attribute != null) attribute.setBaseValue(old);
            }
            invulnerableUntil.remove(entry.getKey());
        }
        for (Map.Entry<UUID, Integer> entry : new ArrayList<>(featherUseAt.entrySet())) {
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player == null || !player.isOnline()) {
                featherUseAt.remove(entry.getKey());
                continue;
            }
            if (!isPlayable(player) || !"feather".equals(itemAction(player.getInventory().getItemInMainHand()))) {
                featherUseAt.remove(entry.getKey());
                player.sendMessage(session.config.message("feather-cancelled"));
                continue;
            }
            if (elapsed < entry.getValue()) continue;
            featherUseAt.remove(entry.getKey());
            consumeOne(player, player.getInventory().getItemInMainHand());
            player.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 20, 14, true, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 100, 0, true, false));
            player.sendMessage(session.config.message("feather-active"));
        }
        for (Map.Entry<UUID, Channel> entry : new ArrayList<>(returnChannels.entrySet())) {
            Player player = Bukkit.getPlayer(entry.getKey());
            Channel channel = entry.getValue();
            if (player == null || !player.isOnline() || !isPlayable(player)) {
                returnChannels.remove(entry.getKey());
                continue;
            }
            if (channel.start().getWorld() != player.getWorld()
                    || channel.start().distanceSquared(player.getLocation()) > 0.0101) {
                returnChannels.remove(entry.getKey());
                player.sendMessage(session.config.message("return-cancelled"));
                continue;
            }
            if (elapsed < channel.dueAt()) continue;
            returnChannels.remove(entry.getKey());
            List<Location> spawns = session.arena.teamSpawns().get(TEAM_KEYS[team(player)]);
            Location destination = spawns.get(teamSlots.getOrDefault(player.getUniqueId(), 0) % spawns.size());
            player.teleport(destination);
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 20, 1, true, false));
            player.sendMessage(session.config.message("return-completed"));
        }
        for (Map.Entry<BlockPoint, Integer> entry : new ArrayList<>(rescueBlocksExpireAt.entrySet())) {
            if (elapsed < entry.getValue()) continue;
            BlockPoint point = entry.getKey();
            Block block = point.world().getBlockAt(point.x(), point.y(), point.z());
            if (block.getType() == Material.SLIME_BLOCK && session.writable(block)) session.set(block, Material.AIR);
            rescueBlocksExpireAt.remove(point);
        }
        for (Map.Entry<UUID, Integer> entry : new ArrayList<>(sheepDetonateAt.entrySet())) {
            if (elapsed < entry.getValue()) continue;
            Entity entity = Bukkit.getEntity(entry.getKey());
            sheepDetonateAt.remove(entry.getKey());
            if (entity == null || !entity.isValid()) continue;
            Location location = entity.getLocation();
            simulateBlast(session, location, 69, true);
            location.getWorld().createExplosion(location, 3.0f, false, false, entity);
            entity.remove();
        }
        tickBridgeEggs(session, elapsed);
        tickTowers(session, elapsed);
        tickSupportEntities(session, elapsed);
    }

    private void tickBridgeEggs(PartySession session, int elapsed) {
        for (Map.Entry<UUID, Integer> entry : new ArrayList<>(bridgeEggExpireAt.entrySet())) {
            Entity entity = Bukkit.getEntity(entry.getKey());
            int expiresAt = entry.getValue();
            int bornAt = expiresAt - 30;
            if (entity == null || !entity.isValid() || elapsed >= expiresAt) {
                if (entity != null) entity.remove();
                bridgeEggExpireAt.remove(entry.getKey());
                continue;
            }
            int age = elapsed - bornAt;
            if (age < 2) continue;
            int itemTeam = entity.getPersistentDataContainer().getOrDefault(ITEM_TEAM_KEY, PersistentDataType.INTEGER, -1);
            Material wool = itemTeam >= 0 && itemTeam < TEAM_WOOL.length ? TEAM_WOOL[itemTeam] : Material.WHITE_WOOL;
            Location location = entity.getLocation();
            int y = location.getBlockY() - 2;
            for (int[] offset : new int[][]{{0, 0}, {1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
                Block block = location.getWorld().getBlockAt(location.getBlockX() + offset[0], y, location.getBlockZ() + offset[1]);
                if (block.getType() != Material.AIR || !session.writable(block) || bedTeams.containsKey(key(block))) continue;
                session.set(block, wool);
                placedBlocks.add(key(block));
            }
        }
    }

    private void tickTowers(PartySession session, int elapsed) {
        for (TowerBuild tower : new ArrayList<>(towerBuilds)) {
            int target = Math.min(tower.blocks.size(), tower.blocks.size() * Math.min(18, elapsed - tower.startedAt + 1) / 18);
            while (tower.nextIndex < target) {
                PlannedBlock planned = tower.blocks.get(tower.nextIndex++);
                Block block = planned.point().world().getBlockAt(planned.point().x(), planned.point().y(), planned.point().z());
                if (block.getType() != Material.AIR || !session.writable(block) || bedTeams.containsKey(key(block))) continue;
                session.set(block, planned.material());
                placedBlocks.add(key(block));
            }
            if (tower.nextIndex >= tower.blocks.size()) towerBuilds.remove(tower);
        }
    }

    private void tickSupportEntities(PartySession session, int elapsed) {
        for (Map.Entry<UUID, Integer> entry : new ArrayList<>(supportExpiresAt.entrySet())) {
            Entity entity = Bukkit.getEntity(entry.getKey());
            if (entity == null || !entity.isValid() || elapsed >= entry.getValue()) {
                if (entity != null) entity.remove();
                supportExpiresAt.remove(entry.getKey());
                supportTeams.remove(entry.getKey());
                continue;
            }
            int owner = supportTeams.getOrDefault(entry.getKey(), -1);
            if (entity instanceof org.bukkit.entity.Mob mob) {
                Player target = session.survivors().stream()
                        .filter(this::isPlayable).filter(player -> team(player) != owner)
                        .filter(player -> player.getWorld().equals(entity.getWorld()) && player.getLocation().distanceSquared(entity.getLocation()) <= 24 * 24)
                        .min(java.util.Comparator.comparingDouble(player -> player.getLocation().distanceSquared(entity.getLocation())))
                        .orElse(null);
                mob.setTarget(target);
            }
        }
    }

    private void simulateBlast(PartySession session, Location center, int strength, boolean big) {
        Set<BlockPoint> hit = new HashSet<>();
        for (int azimuth = 0; azimuth < 12; azimuth++) {
            double angle = azimuth * Math.PI / 6.0;
            for (int elevationIndex = 0; elevationIndex <= 12; elevationIndex++) {
                double elevation = -Math.PI / 2.0 + elevationIndex * Math.PI / 12.0;
                double dx = Math.cos(elevation) * Math.cos(angle);
                double dy = Math.sin(elevation);
                double dz = Math.cos(elevation) * Math.sin(angle);
                int remaining = strength;
                for (double distance = 0.5; remaining > 0; distance += 0.5) {
                    remaining -= 5;
                    Block block = center.getWorld().getBlockAt(
                            (int) Math.floor(center.getX() + dx * distance),
                            (int) Math.floor(center.getY() + dy * distance),
                            (int) Math.floor(center.getZ() + dz * distance));
                    Material material = block.getType();
                    if (material.isAir() || material == Material.FIRE || material == Material.WATER) continue;
                    int resistance = blastResistance(material, big);
                    if (resistance < 0) break;
                    remaining -= resistance;
                    if (remaining > 0 && hit.add(key(block)) && session.writable(block)) {
                        for (Item item : block.getWorld().getNearbyEntitiesByType(Item.class, block.getLocation().add(0.5, 0.5, 0.5), 1.2)) item.remove();
                        java.util.Collection<ItemStack> drops = block.getDrops();
                        session.set(block, Material.AIR);
                        for (ItemStack drop : drops) if (!drop.getType().isAir()) drop(session, block.getLocation(), drop);
                    }
                }
            }
        }
    }

    private int blastResistance(Material material, boolean big) {
        if (material == Material.WHITE_WOOL || material == Material.RED_WOOL || material == Material.BLUE_WOOL
                || material == Material.YELLOW_WOOL || material == Material.LIME_WOOL) return 3;
        if (material == Material.DARK_OAK_PLANKS || material == Material.LADDER || material == Material.CUT_SANDSTONE) return 10;
        if (big && material == Material.END_STONE) return 30;
        return -1;
    }

    private void createTower(PartySession session, Player player, int elapsed) {
        Location base = player.getLocation().getBlock().getLocation();
        double yaw = Math.toRadians(player.getLocation().getYaw());
        int forwardX = (int) Math.round(-Math.sin(yaw));
        int forwardZ = (int) Math.round(Math.cos(yaw));
        if (Math.abs(forwardX) >= Math.abs(forwardZ)) forwardZ = 0; else forwardX = 0;
        int rightX = -forwardZ;
        int rightZ = forwardX;
        List<PlannedBlock> blocks = new ArrayList<>();
        Material wool = TEAM_WOOL[team(player)];
        for (int y = 0; y < 7; y++) {
            for (int x = -2; x <= 2; x++) for (int z = -2; z <= 2; z++) {
                if (Math.abs(x) != 2 && Math.abs(z) != 2) continue;
                int wx = base.getBlockX() + rightX * x + forwardX * z;
                int wz = base.getBlockZ() + rightZ * x + forwardZ * z;
                blocks.add(new PlannedBlock(new BlockPoint(base.getWorld(), wx, base.getBlockY() + y, wz), wool));
            }
            int ladderX = base.getBlockX() + forwardX;
            int ladderZ = base.getBlockZ() + forwardZ;
            blocks.add(new PlannedBlock(new BlockPoint(base.getWorld(), ladderX, base.getBlockY() + y, ladderZ), Material.LADDER));
        }
        towerBuilds.add(new TowerBuild(blocks, elapsed));
        player.sendMessage(session.config.message("tower-placed"));
    }

    private void spawnGolem(PartySession session, Player player, int elapsed) {
        int team = team(player);
        List<UUID> owned = supportTeams.entrySet().stream().filter(entry -> entry.getValue() == team)
                .map(Map.Entry::getKey).filter(supportExpiresAt::containsKey)
                .sorted(java.util.Comparator.comparingInt(id -> supportExpiresAt.getOrDefault(id, Integer.MAX_VALUE))).toList();
        if (owned.size() >= 3) {
            UUID oldest = owned.get(0);
            Entity previous = Bukkit.getEntity(oldest);
            if (previous != null) previous.remove();
            supportTeams.remove(oldest);
            supportExpiresAt.remove(oldest);
        }
        Location spawn = player.getLocation().add(player.getLocation().getDirection().normalize().multiply(1.5));
        IronGolem golem = spawn.getWorld().spawn(spawn, IronGolem.class);
        golem.setPlayerCreated(false);
        golem.setPersistent(true);
        golem.setRemoveWhenFarAway(false);
        var health = golem.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH);
        if (health != null) health.setBaseValue(20.0);
        golem.setHealth(20.0);
        session.track(golem);
        supportExpiresAt.put(golem.getUniqueId(), elapsed + 120 * 20);
        supportTeams.put(golem.getUniqueId(), team);
        player.sendMessage(session.config.message("golem-placed"));
    }

    @Override
    public void entityDeath(PartySession session, EntityDeathEvent event) {
        UUID id = event.getEntity().getUniqueId();
        if (zombieExpiresAt.remove(id) != null) {
            event.getDrops().clear();
            event.getDrops().add(new ItemStack(Material.IRON_INGOT));
            return;
        }
        if (supportExpiresAt.remove(id) != null) {
            supportTeams.remove(id);
            event.getDrops().clear();
            event.setDroppedExp(0);
        }
    }

    private void checkRespawns(PartySession session, int elapsed) {
        for (Map.Entry<UUID, Integer> entry : new ArrayList<>(respawnAt.entrySet())) {
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player == null || !player.isOnline()) continue;
            int team = team(player);
            if (finalOut.contains(player.getUniqueId()) || !bedAlive(team)) {
                respawnAt.remove(player.getUniqueId());
                eliminate(session, player);
                continue;
            }
            if (elapsed < entry.getValue()) continue;
            respawnAt.remove(player.getUniqueId());
            player.setHealth(maxHealth(player));
            player.setFireTicks(0);
            player.setFallDistance(0);
            spawn(session, player);
            session.broadcast("respawned", "player", player.getName());
        }
    }

    private void tickTraps(PartySession session) {
        double triggerDistance = 8 * 8;
        double resetDistance = 12 * 12;
        trapImmunityExpiresAt.entrySet().removeIf(entry -> entry.getValue() <= session.elapsed());
        for (int team = 0; team < activeTeamCount; team++) {
            int defendingTeam = team;
            Set<UUID> known = trapInside.get(defendingTeam);
            known.removeIf(id -> {
                Player player = Bukkit.getPlayer(id);
                return player == null || !player.isOnline() || !nearBed(player.getLocation(), defendingTeam, resetDistance);
            });
            if (teamTraps.getOrDefault(defendingTeam, List.of()).isEmpty()) continue;
            for (Player player : session.survivors()) {
                UUID id = player.getUniqueId();
                if (team(player) == defendingTeam || respawnAt.containsKey(id) || finalOut.contains(id) || known.contains(id)) continue;
                if (trapImmunityExpiresAt.getOrDefault(id, 0) > session.elapsed()
                        || !nearBed(player.getLocation(), defendingTeam, triggerDistance)) continue;
                known.add(id);
                activateTrap(session, defendingTeam, player);
                break;
            }
        }
    }

    private void activateTrap(PartySession session, int team, Player intruder) {
        List<String> queue = teamTraps.get(team);
        if (queue == null || queue.isEmpty()) return;
        String trap = queue.remove(0);
        switch (trap) {
            case "trap-mining" -> intruder.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 5 * 20, 1, true, false));
            case "trap-blindness" -> {
                intruder.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 5 * 20, 2, true, false));
                intruder.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 8 * 20, 2, true, false));
            }
            case "trap-counter" -> {
                intruder.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 4 * 20, 0, true, false));
                for (Player member : teamPlayers(session, team)) {
                    member.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 4 * 20, 1, true, false));
                    member.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 4 * 20, 0, true, false));
                }
            }
            case "trap-alarm" -> intruder.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 10 * 20, 0, true, false));
            default -> { return; }
        }
        for (Player member : teamPlayers(session, team)) member.sendMessage(session.config.message("trap-triggered"));
    }

    private void tickHealPools(PartySession session) {
        for (Player player : session.survivors()) {
            if (respawnAt.containsKey(player.getUniqueId()) || finalOut.contains(player.getUniqueId())) continue;
            int level = upgrade(team(player), 4);
            if (level == 0 || !nearBed(player.getLocation(), team(player), level == 1 ? 12 * 12 : 16 * 16)) continue;
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 80, level - 1, true, false));
        }
    }

    private boolean nearBed(Location location, int team, double radiusSquared) {
        for (Location bed : beds.getOrDefault(team, List.of())) if (location.getWorld().equals(bed.getWorld()) && location.distanceSquared(bed) <= radiusSquared) return true;
        return false;
    }

    private void checkWinner(PartySession session) {
        List<Integer> active = new ArrayList<>();
        for (int team : liveTeams) {
            if (session.players().stream().anyMatch(player -> team(player) == team && !finalOut.contains(player.getUniqueId()))) active.add(team);
        }
        if (active.size() > 1) return;
        if (active.isEmpty()) session.finish(List.of());
        else session.finish(teamPlayers(session, active.getFirst()));
    }

    @Override public void timeout(PartySession session) { session.finish(List.of()); }

    @Override
    public boolean breakBlock(PartySession session, Player player, Block block) {
        if (!isPlayable(player)) return false;
        Integer bedTeam = bedTeams.get(key(block));
        if (bedTeam != null) {
            if (bedTeam == team(player)) {
                player.sendMessage(session.config.message("own-bed"));
                return false;
            }
            if (mode == Mode.PERMANENT_BED) {
                player.sendMessage(session.config.message("bed-permanent"));
                return false;
            }
            bedDestroyed(session, bedTeam, player);
            return true;
        }
        BlockPoint point = key(block);
        if (!placedBlocks.remove(point)) return false;
        Material type = block.getType();
        if (type != Material.AIR && type != Material.WATER && type != Material.LAVA) {
            Item drop = session.arena.world().dropItemNaturally(block.getLocation(), new ItemStack(type));
            drop.setPickupDelay(0);
            session.track(drop);
        }
        return true;
    }

    @Override
    public boolean placeBlock(PartySession session, Player player, Block block) {
        if (!isPlayable(player) || !buildingMaterials.contains(block.getType())) return false;
        if (block.getType().name().endsWith("_BED")) return false;
        placedBlocks.add(key(block));
        return true;
    }

    @Override public void hit(PartySession session, Player attacker, Player victim, EntityDamageByEntityEvent event) {
        if (returnChannels.remove(victim.getUniqueId()) != null)
            victim.sendMessage(session.config.message("return-cancelled"));
        if (event.getFinalDamage() > 0) victim.removePotionEffect(PotionEffectType.INVISIBILITY);
        if (!isPlayable(attacker) || !isPlayable(victim) || team(attacker) == team(victim)) return;
        attacker.removePotionEffect(PotionEffectType.INVISIBILITY);
        event.setCancelled(false);
    }

    @Override public void environmentalDamage(PartySession session, Player player, EntityDamageEvent event) {
        if (isPlayable(player)) {
            UUID id = player.getUniqueId();
            if (invulnerableUntil.getOrDefault(id, 0) > session.elapsed()) {
                event.setCancelled(true);
                return;
            }
            if (returnChannels.remove(id) != null) player.sendMessage(session.config.message("return-cancelled"));
            event.setCancelled(false);
            if (event.getFinalDamage() > 0) player.removePotionEffect(PotionEffectType.INVISIBILITY);
        }
    }

    @Override
    public void entityDamage(PartySession session, EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player && invulnerableUntil.getOrDefault(player.getUniqueId(), 0) > session.elapsed()) {
            event.setCancelled(true);
            return;
        }
        Integer owner = supportTeams.get(event.getEntity().getUniqueId());
        if (owner == null) return;
        Player attacker = event instanceof EntityDamageByEntityEvent hit ? playerDamager(hit.getDamager()) : null;
        if (attacker != null && team(attacker) == owner) event.setCancelled(true);
    }

    @Override
    public boolean lethalDamage(PartySession session, Player player, EntityDamageEvent event) {
        if (!isPlayable(player)) return true;
        Player killer = event instanceof EntityDamageByEntityEvent hit ? playerDamager(hit.getDamager()) : null;
        die(session, player, killer);
        return true;
    }

    @Override public void hazard(PartySession session, Player player) { if (isPlayable(player)) die(session, player, null); }

    private Player playerDamager(Entity damager) {
        if (damager instanceof Player player) return player;
        if (damager instanceof Projectile projectile && projectile.getShooter() instanceof Player player) return player;
        return null;
    }

    private void die(PartySession session, Player player, Player killer) {
        UUID id = player.getUniqueId();
        featherUseAt.remove(id);
        returnChannels.remove(id);
        int team = team(player);
        if (team < 0 || finalOut.contains(id) || respawnAt.containsKey(id)) return;
        boolean canRespawn = bedAlive(team) && !bedsClosed;
        int balanceAtDeath = xpBalances.getOrDefault(id, 0);
        int deathShare = balanceAtDeath / 2;
        if (mode == Mode.XP && canRespawn) {
            int lost = deathShare;
            if (lost > 0) changeXp(session, player, -lost, true);
        }
        if (mode == Mode.XP && killer != null && !killer.equals(player) && team(killer) != team) {
            int stolen = deathShare;
            if (stolen > 0) changeXp(session, killer, stolen, true);
        }
        dropInventory(session, player);
        pickaxeTiers.computeIfPresent(id, (ignored, level) -> level >= 2 ? level - 1 : level);
        axeTiers.computeIfPresent(id, (ignored, level) -> level >= 2 ? level - 1 : level);
        player.closeInventory();
        player.setHealth(maxHealth(player));
        player.setFireTicks(0);
        player.setFallDistance(0);
        player.setGameMode(GameMode.SPECTATOR);
        player.teleport(session.arena.spectator());
        player.setAllowFlight(true);
        if (!canRespawn) {
            finalOut.add(id);
            respawnAt.remove(id);
            session.eliminate(player);
            return;
        }
        if (mode == Mode.PERMANENT_BED) {
            int lives = teamLives.getOrDefault(team, 0);
            if (lives <= 0) {
                finalOut.add(id);
                session.eliminate(player);
                return;
            }
            teamLives.put(team, lives - 1);
        }
        int delay = session.config.integer("respawn-seconds", 2, 30);
        if (upgrade(team, 5) == 1) delay = Math.min(delay, 4);
        if (upgrade(team, 5) >= 2) delay = Math.min(delay, 2);
        respawnAt.put(id, session.elapsed() + delay * 20);
        session.broadcast("waiting-respawn", "player", player.getName(), "seconds", delay);
    }

    private void dropInventory(PartySession session, Player player) {
        PlayerInventory inventory = player.getInventory();
        for (ItemStack stack : inventory.getStorageContents()) {
            if (stack == null || stack.getType().isAir() || isPermanentTool(player, stack.getType())) continue;
            drop(session, player.getLocation(), stack.clone());
        }
        ItemStack offhand = inventory.getItemInOffHand();
        if (offhand != null && !offhand.getType().isAir()) drop(session, player.getLocation(), offhand.clone());
        List<ItemStack> keep = new ArrayList<>();
        for (ItemStack stack : inventory.getStorageContents()) {
            if (stack != null && !stack.getType().isAir() && isPermanentTool(player, stack.getType())) keep.add(stack.clone());
        }
        inventory.clear();
        inventory.setArmorContents(new ItemStack[4]);
        inventory.setItemInOffHand(null);
        for (ItemStack stack : keep) inventory.addItem(stack);
    }

    private boolean isPermanentTool(Player player, Material material) {
        UUID id = player.getUniqueId();
        int pick = pickaxeTiers.getOrDefault(id, 0);
        int axe = axeTiers.getOrDefault(id, 0);
        return pick >= 0 && material == pickaxeMaterial(pick)
                || axe >= 0 && material == axeMaterial(axe)
                || shears.contains(id) && material == Material.SHEARS;
    }

    private void eliminate(PartySession session, Player player) {
        finalOut.add(player.getUniqueId());
        respawnAt.remove(player.getUniqueId());
        session.eliminate(player);
    }

    @Override public void eliminated(PartySession session, Player player) {
        finalOut.add(player.getUniqueId());
        respawnAt.remove(player.getUniqueId());
    }

    @Override
    public void interact(PartySession session, PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!isPlayable(player)) {
            event.setCancelled(true);
            return;
        }
        if (event.getHand() != EquipmentSlot.HAND || !event.getAction().isRightClick()) return;
        ItemStack held = player.getInventory().getItemInMainHand();
        if (mode == Mode.CLASS && !held.getType().isAir() && held.hasItemMeta()
                && held.getItemMeta().getPersistentDataContainer().has(CLASS_SELECTOR_KEY, PersistentDataType.BYTE)) {
            event.setCancelled(true);
            openClassMenu(session, player);
            return;
        }
        String action = itemAction(held);
        if (action == null) return;
        switch (action) {
            case "bridge-egg", "bugs" -> { return; }
            case "fireball" -> {
                event.setCancelled(true);
                consumeOne(player, held);
                Fireball fireball = player.launchProjectile(Fireball.class);
                fireball.setShooter(player);
                fireball.setYield(0.0f);
                fireball.setIsIncendiary(false);
                fireball.getPersistentDataContainer().set(ITEM_ACTION_KEY, PersistentDataType.STRING, "fireball");
                session.track(fireball);
            }
            case "tnt-sheep" -> {
                event.setCancelled(true);
                consumeOne(player, held);
                Sheep sheep = player.getWorld().spawn(player.getLocation(), Sheep.class);
                sheep.customName(session.config.message("shop.products.tnt-sheep"));
                sheep.setCustomNameVisible(true);
                sheep.setSilent(true);
                sheep.setInvulnerable(true);
                sheep.setAI(false);
                sheep.setGravity(false);
                session.track(sheep);
                sheepDetonateAt.put(sheep.getUniqueId(), session.elapsed() + 6 * 20);
            }
            case "invul-scroll" -> {
                event.setCancelled(true);
                consumeOne(player, held);
                activateInvulnerability(session, player);
            }
            case "feather" -> {
                event.setCancelled(true);
                if (featherUseAt.containsKey(player.getUniqueId())) return;
                featherUseAt.put(player.getUniqueId(), session.elapsed() + 20);
            }
            case "tower" -> {
                event.setCancelled(true);
                consumeOne(player, held);
                createTower(session, player, session.elapsed());
            }
            case "golem" -> {
                event.setCancelled(true);
                consumeOne(player, held);
                spawnGolem(session, player, session.elapsed());
            }
            case "return-scroll" -> {
                event.setCancelled(true);
                consumeOne(player, held);
                returnChannels.put(player.getUniqueId(), new Channel(player.getLocation().clone(), session.elapsed() + 60));
                player.sendMessage(session.config.message("return-started"));
            }
            case "rescue-platform" -> {
                event.setCancelled(true);
                deployRescuePlatform(session, player);
            }
            default -> { }
        }
    }

    @Override
    public void consume(PartySession session, PlayerItemConsumeEvent event) {
        if (!event.getItem().hasItemMeta()
                || !event.getItem().getItemMeta().getPersistentDataContainer().has(MAGIC_MILK_KEY, PersistentDataType.BYTE)) return;
        trapImmunityExpiresAt.put(event.getPlayer().getUniqueId(), session.elapsed() + 60 * 20);
        event.getPlayer().sendActionBar(session.config.message("magic-milk-active"));
    }

    @Override
    public void interactEntity(PartySession session, Player player, Entity target) {
        if (!isPlayable(player)) return;
        if ((target instanceof Villager || target instanceof WanderingTrader) && nearShop(session, target.getLocation())) openShop(session, player);
    }

    @Override
    public void launched(PartySession session, Player player, Projectile projectile) {
        ItemStack held = player.getInventory().getItemInMainHand();
        String action = itemAction(held);
        if (action == null) action = itemAction(player.getInventory().getItemInOffHand());
        if (!"bridge-egg".equals(action) && !"bugs".equals(action)) return;
        projectile.getPersistentDataContainer().set(ITEM_ACTION_KEY, PersistentDataType.STRING, action);
        projectile.getPersistentDataContainer().set(ITEM_TEAM_KEY, PersistentDataType.INTEGER, team(player));
        if (projectile instanceof Egg) bridgeEggExpireAt.put(projectile.getUniqueId(), session.elapsed() + 30);
    }

    @Override
    public void projectile(PartySession session, org.bukkit.event.entity.ProjectileHitEvent event) {
        Projectile projectile = event.getEntity();
        String action = projectile.getPersistentDataContainer().get(ITEM_ACTION_KEY, PersistentDataType.STRING);
        if ("bugs".equals(action)) {
            int owner = projectile.getPersistentDataContainer().getOrDefault(ITEM_TEAM_KEY, PersistentDataType.INTEGER, -1);
            Location location = projectile.getLocation();
            Silverfish bug = location.getWorld().spawn(location, Silverfish.class);
            bug.setPersistent(true);
            bug.setRemoveWhenFarAway(false);
            session.track(bug);
            supportTeams.put(bug.getUniqueId(), owner);
            supportExpiresAt.put(bug.getUniqueId(), session.elapsed() + 45 * 20);
        }
        bridgeEggExpireAt.remove(projectile.getUniqueId());
    }

    private String itemAction(ItemStack item) {
        if (item == null || item.getType().isAir() || !item.hasItemMeta()) return null;
        return item.getItemMeta().getPersistentDataContainer().get(ITEM_ACTION_KEY, PersistentDataType.STRING);
    }

    private void consumeOne(Player player, ItemStack item) {
        if (item.getAmount() <= 1) player.getInventory().setItemInMainHand(null);
        else {
            item.setAmount(item.getAmount() - 1);
            player.getInventory().setItemInMainHand(item);
        }
    }

    private void activateInvulnerability(PartySession session, Player player) {
        UUID id = player.getUniqueId();
        var attribute = player.getAttribute(org.bukkit.attribute.Attribute.KNOCKBACK_RESISTANCE);
        if (attribute != null) {
            previousKnockbackResistance.putIfAbsent(id, attribute.getBaseValue());
            attribute.setBaseValue(1.0);
        }
        invulnerableUntil.put(id, session.elapsed() + 3 * 20);
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 3 * 20, 4, true, false));
        player.sendMessage(session.config.message("invul-active"));
    }

    private void deployRescuePlatform(PartySession session, Player player) {
        UUID id = player.getUniqueId();
        int readyAt = rescueCooldownUntil.getOrDefault(id, 0);
        if (session.elapsed() < readyAt) {
            player.sendMessage(session.config.message("platform-cooldown", "seconds", (readyAt - session.elapsed() + 19) / 20));
            return;
        }
        Location feet = player.getLocation().getBlock().getLocation();
        Block center = feet.clone().add(0, -1, 0).getBlock();
        if (!center.getType().isAir() || !session.writable(center)) {
            player.sendMessage(session.config.message("platform-unavailable"));
            return;
        }
        consumeOne(player, player.getInventory().getItemInMainHand());
        for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) {
            Block block = center.getRelative(dx, 0, dz);
            if (block.getType().isAir() && session.writable(block)) {
                session.set(block, Material.SLIME_BLOCK);
                rescueBlocksExpireAt.put(key(block), session.elapsed() + 15 * 20);
            }
        }
        rescueCooldownUntil.put(id, session.elapsed() + 45 * 20);
        player.sendMessage(session.config.message("platform-placed"));
    }

    @Override
    public boolean action(PartySession session, Player player, String[] args) {
        if (args.length == 0) return false;
        if (args[0].equalsIgnoreCase("class")) {
            if (mode != Mode.CLASS || !isPlayable(player)) player.sendMessage(session.config.message("class.unavailable"));
            else openClassMenu(session, player);
            return true;
        }
        if (!args[0].equalsIgnoreCase("shop")) return false;
        if (!isPlayable(player)) {
            player.sendMessage(session.config.message("shop.unavailable"));
            return true;
        }
        if (!nearShop(session, player.getLocation())) player.sendMessage(session.config.message("shop.too-far"));
        else openShop(session, player);
        return true;
    }

    private boolean nearShop(PartySession session, Location location) {
        int radius = session.config.integer("shop-radius", 2, 12);
        double distance = radius * radius;
        return session.arena.group("shops").stream().anyMatch(shop ->
                location.getWorld().equals(shop.getWorld()) && location.distanceSquared(shop) <= distance);
    }

    private void openShop(PartySession session, Player player) {
        openShop(session, player, 0);
    }

    private List<Product> shopProducts() {
        List<Product> products = new ArrayList<>(mode == Mode.XP ? XP_PRODUCTS : PRODUCTS);
        products.addAll(TEAM_UPGRADES);
        return products;
    }

    private void openShop(PartySession session, Player player, int page) {
        ShopHolder holder = new ShopHolder();
        Inventory inventory = Bukkit.createInventory(holder, 54, session.config.message("shop.title"));
        holder.inventory = inventory;
        List<Product> products = shopProducts();
        int pageCount = Math.max(1, (products.size() + SHOP_PAGE_SIZE - 1) / SHOP_PAGE_SIZE);
        holder.page = Math.max(0, Math.min(page, pageCount - 1));
        holder.pageCount = pageCount;
        for (int slot = 0; slot < SHOP_PAGE_SIZE && holder.page * SHOP_PAGE_SIZE + slot < products.size(); slot++) {
            Product product = products.get(holder.page * SHOP_PAGE_SIZE + slot);
            int team = team(player);
            ItemStack icon = new ItemStack(product.icon());
            ItemMeta meta = icon.getItemMeta();
            meta.displayName(session.config.message("shop.products." + product.id()));
            int cost = productCost(team, product);
            if (product.kind().equals("upgrade") && !upgradeAvailable(team, product.id()))
                meta.lore(List.of(session.config.message("shop.maxed")));
            else {
                List<net.kyori.adventure.text.Component> lore = new ArrayList<>();
                lore.add(session.config.message("shop.cost", "amount", cost,
                        "currency", session.config.text(currencyKey(product.currency()))));
                if (product.extraCurrency() != null && product.extraCost() > 0)
                    lore.add(session.config.message("shop.cost", "amount", product.extraCost(),
                            "currency", session.config.text(currencyKey(product.extraCurrency()))));
                if (mode == Mode.XP) lore.add(session.config.message("shop.xp-balance", "balance", xpBalances.getOrDefault(player.getUniqueId(), 0)));
                meta.lore(lore);
            }
            icon.setItemMeta(meta);
            inventory.setItem(slot, icon);
            holder.products.put(slot, product);
        }
        if (holder.page > 0) inventory.setItem(45, navigationItem(Material.ARROW, session.config.message("shop.previous-page")));
        inventory.setItem(49, navigationItem(Material.PAPER, session.config.message("shop.page", "current", holder.page + 1, "total", pageCount)));
        if (holder.page + 1 < pageCount) inventory.setItem(53, navigationItem(Material.ARROW, session.config.message("shop.next-page")));
        player.openInventory(inventory);
    }

    private ItemStack navigationItem(Material material, net.kyori.adventure.text.Component name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(name);
        item.setItemMeta(meta);
        return item;
    }

    private void openClassMenu(PartySession session, Player player) {
        if (!hasClassSelector(player)) {
            player.sendMessage(session.config.message("class.already-selected"));
            return;
        }
        ClassHolder holder = new ClassHolder();
        Inventory inventory = Bukkit.createInventory(holder, 27, session.config.message("class.title"));
        holder.inventory = inventory;
        int[] slots = {10, 12, 14, 16};
        Material[] icons = {Material.IRON_SWORD, Material.BOW, TEAM_WOOL[team(player)], Material.IRON_PICKAXE};
        String[] names = {"warrior", "archer", "builder", "miner"};
        for (int index = 0; index < slots.length; index++) {
            ItemStack icon = new ItemStack(icons[index]);
            ItemMeta meta = icon.getItemMeta();
            meta.displayName(session.config.message("class." + names[index]));
            icon.setItemMeta(meta);
            inventory.setItem(slots[index], icon);
            holder.classes.put(slots[index], index);
        }
        player.openInventory(inventory);
    }

    private boolean hasClassSelector(Player player) {
        return Arrays.stream(player.getInventory().getStorageContents()).filter(Objects::nonNull)
                .filter(ItemStack::hasItemMeta)
                .anyMatch(item -> item.getItemMeta().getPersistentDataContainer().has(CLASS_SELECTOR_KEY, PersistentDataType.BYTE));
    }

    private void giveClassSelector(PartySession session, Player player) {
        ItemStack selector = new ItemStack(Material.PAPER);
        ItemMeta meta = selector.getItemMeta();
        meta.displayName(session.config.message("class.selector"));
        meta.lore(List.of(session.config.message("class.selector-hint")));
        meta.getPersistentDataContainer().set(CLASS_SELECTOR_KEY, PersistentDataType.BYTE, (byte) 1);
        selector.setItemMeta(meta);
        player.getInventory().setItem(8, selector);
    }

    private void selectClass(PartySession session, Player player, int selected) {
        if (selected < 0 || selected > 3 || !hasClassSelector(player)) return;
        classes.put(player.getUniqueId(), selected);
        player.closeInventory();
        PlayerInventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getStorageContents().length; slot++) {
            ItemStack item = inventory.getItem(slot);
            if (item != null && item.hasItemMeta()
                    && item.getItemMeta().getPersistentDataContainer().has(CLASS_SELECTOR_KEY, PersistentDataType.BYTE))
                inventory.setItem(slot, null);
        }
        if (selected == 0) {
            giveSword(session, player, 2);
            give(player, new ItemStack(Material.GOLDEN_APPLE));
        } else if (selected == 1) {
            ItemStack bow = new ItemStack(Material.BOW);
            ItemMeta meta = bow.getItemMeta();
            meta.displayName(session.config.message("class.archer-bow"));
            bow.setItemMeta(meta);
            give(player, bow);
            give(player, new ItemStack(Material.ARROW, 24));
        } else if (selected == 2) {
            give(player, new ItemStack(TEAM_WOOL[team(player)], 32));
            give(player, new ItemStack(Material.DARK_OAK_PLANKS, 16));
            give(player, new ItemStack(Material.LADDER, 8));
        } else {
            give(player, new ItemStack(Material.IRON_PICKAXE));
            give(player, new ItemStack(Material.IRON_AXE));
            give(player, new ItemStack(TEAM_WOOL[team(player)], 8));
        }
        player.sendMessage(session.config.message("class.selected", "class", session.config.text("class." + new String[]{"warrior", "archer", "builder", "miner"}[selected])));
    }

    @Override
    public boolean allowInventory(PartySession session, InventoryClickEvent event) {
        Inventory top = event.getView().getTopInventory();
        if (top.getHolder() instanceof ClassHolder holder) {
            int rawSlot = event.getRawSlot();
            if (rawSlot >= 0 && rawSlot < top.getSize() && event.getWhoClicked() instanceof Player player) {
                Integer selected = holder.classes.get(rawSlot);
                if (selected != null) selectClass(session, player, selected);
            }
            return false;
        }
        if (top.getHolder() instanceof ShopHolder holder) {
            int rawSlot = event.getRawSlot();
            if (rawSlot >= 0 && rawSlot < top.getSize() && event.getClick().isLeftClick()) {
                if (rawSlot == 45 && holder.page > 0 && event.getWhoClicked() instanceof Player player) {
                    openShop(session, player, holder.page - 1);
                    return false;
                }
                if (rawSlot == 53 && holder.page + 1 < holder.pageCount && event.getWhoClicked() instanceof Player player) {
                    openShop(session, player, holder.page + 1);
                    return false;
                }
                Product product = holder.products.get(rawSlot);
                if (product != null && event.getWhoClicked() instanceof Player player) purchase(session, player, product, holder.page);
            }
            return false;
        }
        return event.getWhoClicked() instanceof Player player && isPlayable(player)
                && event.getView().getType() == InventoryType.CRAFTING
                && event.getClickedInventory() != null
                && event.getClickedInventory().equals(event.getWhoClicked().getInventory());
    }

    @Override
    public boolean allowDrag(PartySession session, InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof ClassHolder) return false;
        if (event.getView().getTopInventory().getHolder() instanceof ShopHolder) return false;
        return event.getWhoClicked() instanceof Player player && isPlayable(player)
                && event.getView().getType() == InventoryType.CRAFTING;
    }

    private void purchase(PartySession session, Player player, Product product, int page) {
        int team = team(player);
        if (team < 0 || !isPlayable(player)) return;
        if (product.kind().equals("shears") && shears.contains(player.getUniqueId())) {
            player.sendMessage(session.config.message("shop.already"));
            return;
        }
        if ((product.kind().equals("pickaxe") && product.tier() <= pickaxeTiers.getOrDefault(player.getUniqueId(), 0))
                || (product.kind().equals("axe") && product.tier() <= axeTiers.getOrDefault(player.getUniqueId(), 0))
                || (product.kind().equals("armor") && product.tier() <= armorTiers.getOrDefault(player.getUniqueId(), 0))) {
            player.sendMessage(session.config.message("shop.already"));
            return;
        }
        if ((product.kind().equals("pickaxe") && product.tier() > pickaxeTiers.getOrDefault(player.getUniqueId(), 0) + 1)
                || (product.kind().equals("axe") && product.tier() > axeTiers.getOrDefault(player.getUniqueId(), 0) + 1)) {
            player.sendMessage(session.config.message("shop.prerequisite"));
            return;
        }
        if (product.kind().equals("sword") && product.tier() <= swordTier(player)) {
            player.sendMessage(session.config.message("shop.already"));
            return;
        }
        if (product.kind().equals("upgrade") && !upgradeAvailable(team, product.id())) {
            player.sendMessage(session.config.message("shop.maxed"));
            return;
        }
        int cost = productCost(team, product);
        if (!hasCurrency(player, product.currency(), cost)) {
            sendInsufficient(session, player, product.currency(), cost);
            return;
        }
        if (product.extraCurrency() != null && product.extraCost() > 0
                && !hasCurrency(player, product.extraCurrency(), product.extraCost())) {
            sendInsufficient(session, player, product.extraCurrency(), product.extraCost());
            return;
        }
        if (product.kind().equals("upgrade") && !upgradeAvailable(team, product.id())) {
            player.sendMessage(session.config.message("shop.maxed"));
            return;
        }
        if (product.currency() != Material.EXPERIENCE_BOTTLE && !hasCurrency(player, product.currency(), cost)) {
            player.sendMessage(session.config.message("shop.insufficient", "amount", cost,
                    "currency", session.config.text(currencyKey(product.currency()))));
            return;
        }

        if (product.kind().equals("item") || product.kind().endsWith("-potion") || product.kind().equals("xp-exchange")) {
            Material material = product.id().equals("wool") ? TEAM_WOOL[team]
                    : product.id().equals("blast-glass") ? teamGlass(team) : product.icon();
            ItemStack reward = new ItemStack(material, product.amount());
            if (product.id().endsWith("-potion")) {
                reward = potion(product.id());
            }
            if (product.id().equals("bow-power")) reward.addEnchantment(Enchantment.POWER, 1);
            if (product.id().equals("bow-quality")) {
                reward.addEnchantment(Enchantment.POWER, 2);
                reward.addEnchantment(Enchantment.PUNCH, 1);
            }
            if (product.id().equals("poison-arrows")) {
                PotionMeta meta = (PotionMeta) reward.getItemMeta();
                meta.addCustomEffect(new PotionEffect(PotionEffectType.POISON, 10 * 20, 0, true, true), true);
                reward.setItemMeta(meta);
            }
            if (product.id().equals("knockback-stick")) reward.addEnchantment(Enchantment.KNOCKBACK, 2);
            if (isActionItem(product.id())) {
                ItemMeta meta = reward.getItemMeta();
                meta.getPersistentDataContainer().set(ITEM_ACTION_KEY, PersistentDataType.STRING, product.id());
                reward.setItemMeta(meta);
            }
            if (product.id().equals("magic-milk")) {
                ItemMeta meta = reward.getItemMeta();
                meta.getPersistentDataContainer().set(MAGIC_MILK_KEY, PersistentDataType.BYTE, (byte) 1);
                meta.displayName(session.config.message("shop.products.magic-milk"));
                reward.setItemMeta(meta);
            }
            if (!give(player, reward)) {
                player.sendMessage(session.config.message("shop.inventory-full"));
                return;
            }
        } else if (product.kind().equals("shears")) {
            shears.add(player.getUniqueId());
            player.getInventory().setItem(3, new ItemStack(Material.SHEARS));
        } else if (product.kind().equals("pickaxe")) {
            removeTools(player, PICKAXES);
            pickaxeTiers.put(player.getUniqueId(), product.tier());
            player.getInventory().setItem(1, new ItemStack(pickaxeMaterial(product.tier())));
        } else if (product.kind().equals("axe")) {
            removeTools(player, AXES);
            axeTiers.put(player.getUniqueId(), product.tier());
            player.getInventory().setItem(2, new ItemStack(axeMaterial(product.tier())));
        } else if (product.kind().equals("armor")) {
            armorTiers.put(player.getUniqueId(), product.tier());
            applyArmor(player);
        } else if (product.kind().equals("sword")) {
            for (Material sword : List.of(Material.WOODEN_SWORD, Material.STONE_SWORD, Material.IRON_SWORD, Material.DIAMOND_SWORD))
                player.getInventory().remove(sword);
            giveSword(session, player, product.tier());
        } else if (product.kind().equals("upgrade")) {
            buyUpgrade(session, team, product.id());
        }
        if (product.currency() == Material.EXPERIENCE_BOTTLE)
            changeXp(session, player, -cost, false);
        else removeCurrency(player, product.currency(), cost);
        if (product.extraCurrency() != null && product.extraCost() > 0) {
            if (product.extraCurrency() == Material.EXPERIENCE_BOTTLE)
                changeXp(session, player, -product.extraCost(), false);
            else removeCurrency(player, product.extraCurrency(), product.extraCost());
        }
        player.sendMessage(session.config.message("shop.purchased", "item", session.config.text("shop.products." + product.id())));
        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.7f, 1.2f);
        if (mode == Mode.XP) openShop(session, player, page);
    }

    private boolean isActionItem(String id) {
        return Set.of("bridge-egg", "fireball", "tnt-sheep", "invul-scroll", "feather", "tower", "golem",
                "bugs", "return-scroll", "rescue-platform").contains(id);
    }

    private Material teamGlass(int team) {
        return switch (team) {
            case 0 -> Material.RED_STAINED_GLASS;
            case 1 -> Material.BLUE_STAINED_GLASS;
            case 2 -> Material.YELLOW_STAINED_GLASS;
            case 3 -> Material.LIME_STAINED_GLASS;
            default -> Material.WHITE_STAINED_GLASS;
        };
    }

    private boolean hasCurrency(Player player, Material currency, int amount) {
        return currency == Material.EXPERIENCE_BOTTLE
                ? xpBalances.getOrDefault(player.getUniqueId(), 0) >= amount
                : count(player, currency) >= amount;
    }

    private void sendInsufficient(PartySession session, Player player, Material currency, int amount) {
        if (currency == Material.EXPERIENCE_BOTTLE)
            player.sendMessage(session.config.message("shop.insufficient-xp", "amount", amount,
                    "balance", xpBalances.getOrDefault(player.getUniqueId(), 0)));
        else player.sendMessage(session.config.message("shop.insufficient", "amount", amount,
                "currency", session.config.text(currencyKey(currency))));
    }

    private boolean give(Player player, ItemStack reward) {
        ItemStack[] before = cloneItems(player.getInventory().getStorageContents());
        Map<Integer, ItemStack> leftovers = player.getInventory().addItem(reward);
        if (leftovers.isEmpty()) return true;
        player.getInventory().setStorageContents(before);
        return false;
    }

    private ItemStack[] cloneItems(ItemStack[] items) {
        return Arrays.stream(items).map(item -> item == null ? null : item.clone()).toArray(ItemStack[]::new);
    }

    private ItemStack potion(String id) {
        ItemStack potion = new ItemStack(Material.POTION);
        org.bukkit.inventory.meta.PotionMeta meta = (org.bukkit.inventory.meta.PotionMeta) potion.getItemMeta();
        PotionEffectType effect;
        int duration = 20 * 30;
        int amplifier = 0;
        switch (id) {
            case "speed-potion" -> effect = PotionEffectType.SPEED;
            case "jump-potion" -> effect = PotionEffectType.JUMP_BOOST;
            case "invisibility-potion" -> effect = PotionEffectType.INVISIBILITY;
            case "strength-potion" -> { effect = PotionEffectType.STRENGTH; duration = 20 * 60; }
            case "resistance-potion" -> { effect = PotionEffectType.RESISTANCE; duration = 20 * 60; }
            case "healing-potion" -> { effect = PotionEffectType.INSTANT_HEALTH; duration = 1; amplifier = 1; }
            default -> throw new IllegalArgumentException("未知药水 " + id);
        }
        if (mode == Mode.XP) {
            if (id.equals("speed-potion")) { amplifier = 1; duration = 20 * 60; }
            if (id.equals("jump-potion")) { amplifier = 4; duration = 20 * 60; }
            if (id.equals("invisibility-potion")) duration = 20 * 30;
        }
        meta.addCustomEffect(new PotionEffect(effect, duration, amplifier, true, false), true);
        potion.setItemMeta(meta);
        return potion;
    }

    private void removeTools(Player player, Material[] tools) {
        for (Material material : tools) player.getInventory().remove(material);
    }

    private int swordTier(Player player) {
        for (int tier = 3; tier >= 0; tier--) if (player.getInventory().contains(switch (tier) {
            case 1 -> Material.STONE_SWORD;
            case 2 -> Material.IRON_SWORD;
            case 3 -> Material.DIAMOND_SWORD;
            default -> Material.WOODEN_SWORD;
        })) return tier;
        return -1;
    }

    private int count(Player player, Material type) {
        return Arrays.stream(player.getInventory().getStorageContents()).filter(Objects::nonNull)
                .filter(item -> item.getType() == type).mapToInt(ItemStack::getAmount).sum();
    }

    private void removeCurrency(Player player, Material type, int amount) {
        player.getInventory().removeItem(new ItemStack(type, amount));
    }

    private int productCost(int team, Product product) {
        if (!product.kind().equals("upgrade")) return product.cost();
        int level = upgrade(team, upgradeIndex(product.id()));
        if (product.id().startsWith("trap-")) return switch (teamTraps.getOrDefault(team, List.of()).size()) {
            case 0 -> 1;
            case 1 -> 2;
            default -> 4;
        };
        int[] costs = switch (product.id()) {
            case "sharpness" -> new int[]{8, 20, 30, 40};
            case "protection" -> new int[]{5, 10, 20, 30};
            case "haste" -> new int[]{4, 6};
            case "forge" -> new int[]{4, 8, 12, 16};
            case "heal-pool" -> new int[]{1, 2};
            case "fast-respawn" -> new int[]{5, 10};
            case "speed-team" -> new int[]{6};
            case "jump-team" -> new int[]{8};
            default -> new int[0];
        };
        if (product.id().equals("speed-team")) return 6;
        if (product.id().equals("jump-team")) return 8;
        return level >= costs.length ? 0 : costs[level];
    }

    private boolean upgradeAvailable(int team, String id) {
        if (id.startsWith("trap-")) return teamTraps.getOrDefault(team, List.of()).size() < 3;
        int maximum = switch (id) {
            case "sharpness", "protection", "forge" -> 4;
            case "haste", "heal-pool", "fast-respawn" -> 2;
            case "speed-team", "jump-team" -> 1;
            default -> 0;
        };
        return upgrade(team, upgradeIndex(id)) < maximum;
    }

    private int upgradeIndex(String id) {
        return switch (id) {
            case "sharpness" -> 0;
            case "protection" -> 1;
            case "haste" -> 2;
            case "forge" -> 3;
            case "heal-pool" -> 4;
            case "fast-respawn" -> 5;
            case "trap-mining", "trap-blindness", "trap-counter", "trap-alarm" -> 6;
            case "speed-team" -> 7;
            case "jump-team" -> 8;
            default -> throw new IllegalArgumentException("未知队伍升级 " + id);
        };
    }

    private int upgrade(int team, int index) {
        Integer[] levels = upgrades.get(team);
        return levels == null ? 0 : levels[index];
    }

    private void setUpgrade(int team, int index, int level) {
        upgrades.get(team)[index] = level;
    }

    private void buyUpgrade(PartySession session, int team, String id) {
        if (id.startsWith("trap-")) {
            List<String> queue = teamTraps.get(team);
            if (queue != null && queue.size() < 3) queue.add(id);
            for (Player member : teamPlayers(session, team)) member.sendMessage(session.config.message("shop.team-upgrade",
                    "upgrade", session.config.text("shop.products." + id)));
            return;
        }
        int index = upgradeIndex(id);
        setUpgrade(team, index, upgrade(team, index) + 1);
        if (id.equals("forge") && upgrade(team, index) >= 3)
            forgeNextAt.put(team, session.elapsed() + (upgrade(team, index) >= 4 ? 30 : 60) * 20);
        for (Player member : teamPlayers(session, team)) {
            if (id.equals("sharpness")) {
                int tier = swordTier(member);
                if (tier >= 0) giveSword(session, member, tier);
            } else if (id.equals("protection")) applyArmor(member);
            else if (id.equals("haste") || id.equals("speed-team") || id.equals("jump-team")) applyTeamEffects(member);
        }
        for (Player member : teamPlayers(session, team)) member.sendMessage(session.config.message("shop.team-upgrade",
                "upgrade", session.config.text("shop.products." + id)));
    }

    private String currencyKey(Material currency) {
        if (currency == Material.IRON_INGOT) return "currency.iron";
        if (currency == Material.GOLD_INGOT) return "currency.gold";
        if (currency == Material.DIAMOND) return "currency.diamond";
        if (currency == Material.EXPERIENCE_BOTTLE) return "currency.experience";
        return "currency.emerald";
    }

    @Override
    public boolean allowPickup(PartySession session, Player player, Item item) {
        return isPlayable(player) && session.arena.contains(item.getLocation());
    }

    @Override public boolean allowDrop(PartySession session, Player player, Item item) { return false; }
    @Override public boolean allowSwap(PartySession session, Player player) { return isPlayable(player); }
    @Override public boolean allowBlockInteraction(PartySession session, Player player, Block block) {
        if (!isPlayable(player) || !session.writable(block)) return false;
        Material held = player.getInventory().getItemInMainHand().getType();
        return buildingMaterials.contains(held) || held == Material.WATER_BUCKET;
    }
    @Override public boolean allowNaturalChanges(PartySession session) { return true; }

    @Override
    public void explode(PartySession session, EntityExplodeEvent event) {
        boolean shopFireball = event.getEntity() instanceof Fireball fireball
                && "fireball".equals(fireball.getPersistentDataContainer().get(ITEM_ACTION_KEY, PersistentDataType.STRING));
        if (shopFireball) {
            event.blockList().clear();
            simulateBlast(session, event.getLocation(), 35, false);
            return;
        }
        Player breaker = event.getEntity() instanceof Fireball fireball && fireball.getShooter() instanceof Player player
                ? player : null;
        for (Block block : List.copyOf(event.blockList())) {
            Integer bedTeam = bedTeams.get(key(block));
            if (bedTeam == null) continue;
            event.blockList().remove(block);
            if (mode == Mode.PERMANENT_BED) continue;
            bedDestroyed(session, bedTeam, breaker);
        }
    }

    @Override
    public void stopped(PartySession session) {
        gameStarted = false;
        for (Player player : session.players()) player.closeInventory();
        originalScoreboards.forEach((id, previous) -> {
            Player player = Bukkit.getPlayer(id);
            if (player != null && player.isOnline()) player.setScoreboard(previous);
        });
        originalScoreboards.clear();
    }

    private boolean isPlayable(Player player) {
        return team(player) >= 0 && !finalOut.contains(player.getUniqueId()) && !respawnAt.containsKey(player.getUniqueId());
    }

    private int team(Player player) { return teams.getOrDefault(player.getUniqueId(), -1); }

    private List<Player> teamPlayers(PartySession session, int team) {
        return session.players().stream().filter(player -> team(player) == team).toList();
    }

    private double maxHealth(Player player) {
        return Objects.requireNonNull(player.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH)).getValue();
    }

    private BlockPoint key(Block block) {
        return new BlockPoint(block.getWorld(), block.getX(), block.getY(), block.getZ());
    }

    private record BlockPoint(org.bukkit.World world, int x, int y, int z) {}
    private enum Mode { NORMAL, NO_BEDS, UNLOCKED, XP, DOUBLE_RESOURCES, CLASS, ZOMBIE_WAVE, PERMANENT_BED }
    private record PlannedBlock(BlockPoint point, Material material) {}
    private static final class TowerBuild {
        private final List<PlannedBlock> blocks;
        private final int startedAt;
        private int nextIndex;
        private TowerBuild(List<PlannedBlock> blocks, int startedAt) {
            this.blocks = blocks;
            this.startedAt = startedAt;
        }
    }
    private record Product(String id, Material icon, Material currency, int cost, int amount, String kind, int tier,
                           Material extraCurrency, int extraCost) {
        private Product(String id, Material icon, Material currency, int cost, int amount, String kind, int tier) {
            this(id, icon, currency, cost, amount, kind, tier, null, 0);
        }
    }

    private static final class Generator {
        private final int team;
        private final Material material;
        private final List<Location> points;
        private int nextTick = 1;

        private Generator(int team, Material material, List<Location> points) {
            this.team = team;
            this.material = material;
            this.points = List.copyOf(points);
        }
    }

    private static final class ShopHolder implements InventoryHolder {
        private Inventory inventory;
        private int page;
        private int pageCount;
        private final Map<Integer, Product> products = new LinkedHashMap<>();
        @Override public Inventory getInventory() { return inventory; }
    }

    private static final class ClassHolder implements InventoryHolder {
        private Inventory inventory;
        private final Map<Integer, Integer> classes = new LinkedHashMap<>();
        @Override public Inventory getInventory() { return inventory; }
    }

    private record Channel(Location start, int dueAt) {}
}
