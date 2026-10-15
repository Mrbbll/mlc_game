package com.mlc.mlcgames.party.arcade.miner;

import com.mlc.mlcgames.party.core.*;
import com.mlc.mlcgames.party.racing.common.RaceGame;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.inventory.ItemStack;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/** Source map_game_2nd/miner: one shared random column copied to sealed player shafts. */
public final class MinerGame extends RaceGame {
    private static final Material[] TOOLS = {Material.DIAMOND_PICKAXE, Material.DIAMOND_AXE, Material.DIAMOND_SHOVEL, Material.DIAMOND_HOE, Material.SHEARS};
    private final List<MinerColumn> columns = new ArrayList<>();
    private final List<Material> palette = new ArrayList<>();
    private final Map<UUID, MinerColumn> assigned = new HashMap<>();
    private List<Material> sequence;
    private int cursor;
    private int usedColumns;
    private boolean building;
    private boolean prepared;

    @Override public void validate(PartyConfig config, PartyConfig.Arena arena) {
        super.validate(config, arena);
        int layers = config.integer("random-layers", 1, 256);
        int maximum = config.integer("players.max", 2, 64);
        List<Location> positions = arena.group("pillars");
        if (positions.size() < maximum) throw new IllegalArgumentException("pillars 点位数量必须至少为 players.max，避免玩家无矿柱可用");
        for (Location position : positions.subList(0, maximum)) {
            if (position.getX() != position.getBlockX() || position.getY() != position.getBlockY() || position.getZ() != position.getBlockZ()) throw new IllegalArgumentException("pillars 点位必须为矿柱底部基岩的整数坐标");
            MinerColumn column = new MinerColumn(position.getBlockX(), position.getBlockY(), position.getBlockZ(), layers);
            if (!arena.bounds().contains(column.bounds()) || column.bounds().maxY() >= arena.world().getMaxHeight()) throw new IllegalArgumentException("矿井的屏障外壳超出 bounds 或世界高度");
            if (!finish.contains(column.x(), column.floorY(), column.z())) throw new IllegalArgumentException("finish 必须包含每个矿柱的基岩底部");
            if (columns.stream().anyMatch(column::overlaps)) throw new IllegalArgumentException("矿井屏障外壳不可重叠，请扩大 pillars 间距");
            columns.add(column);
        }
        long volume = columns.stream().mapToLong(c -> c.bounds().volume()).sum();
        if (volume > config.integer("limits.max-snapshot-blocks", 1, 100000)) throw new IllegalArgumentException("全部矿井外壳超出快照方块上限");
        for (String name : config.rules.getStringList("block-palette")) {
            Material material = Material.matchMaterial(name);
            if (material == null || !material.isBlock() || material.isAir() || material == Material.BEDROCK || material == Material.BARRIER) throw new IllegalArgumentException("block-palette 含非法/不可挖方块：" + name);
            palette.add(material); // Deliberately keep duplicate entries: source rolls have equal weight.
        }
        if (palette.isEmpty() || palette.size() > 1000) throw new IllegalArgumentException("block-palette 需包含 1..1000 个方块条目");
        config.integer("tools.efficiency", 0, 10);
    }
    @Override public List<Cuboid> writableRegions(PartyConfig.Arena arena) { return columns.stream().map(MinerColumn::bounds).toList(); }
    @Override public boolean prepare(PartySession session, int budget) {
        if (prepared) return true;
        if (sequence == null) {
            usedColumns = session.players().size();
            sequence = new ArrayList<>();
            for (int y = 0; y < columns.getFirst().layers(); y++) sequence.add(palette.get(ThreadLocalRandom.current().nextInt(palette.size())));
        }
        building = true; // Baseline has already been durably captured before prepare is called.
        int shellSize = (int) columns.getFirst().bounds().volume();
        int centerSize = columns.getFirst().layers() + 5;
        int shellTotal = usedColumns * shellSize;
        int total = shellTotal + usedColumns * centerSize;
        for (int count = 0; count < budget && cursor < total; count++, cursor++) {
            if (cursor < shellTotal) {
                MinerColumn column = columns.get(cursor / shellSize);
                int local = cursor % shellSize;
                int y = local / 9, x = local % 9 / 3 - 1, z = local % 3 - 1;
                session.set(session.arena.world().getBlockAt(column.x() + x, column.floorY() + y, column.z() + z), Material.BARRIER);
            } else {
                int local = cursor - shellTotal;
                MinerColumn column = columns.get(local / centerSize);
                int y = local % centerSize;
                session.set(session.arena.world().getBlockAt(column.x(), column.floorY() + y, column.z()),
                        y == 0 ? Material.BEDROCK : y <= column.layers() ? sequence.get(y - 1) : Material.AIR);
            }
        }
        prepared = cursor == total;
        return prepared;
    }
    @Override public void start(PartySession session) {
        List<Player> players = new ArrayList<>(session.survivors());
        Collections.shuffle(players);
        for (int index = 0; index < players.size(); index++) {
            Player player = players.get(index);
            MinerColumn column = columns.get(index);
            assigned.put(player.getUniqueId(), column);
            player.setGameMode(GameMode.SURVIVAL);
            if (!player.teleport(new Location(session.arena.world(), column.x() + .5, column.startY(), column.z() + .5, 0, 90))) throw new IllegalStateException(session.config.text("teleport-failed"));
            equip(session, player);
        }
        super.start(session);
    }
    private void equip(PartySession session, Player player) {
        for (int slot = 0; slot < TOOLS.length; slot++) if (!player.getInventory().contains(TOOLS[slot])) {
            ItemStack tool = session.item(TOOLS[slot], "items.tool-" + (slot + 1));
            int level = session.config.integer("tools.efficiency", 0, 10);
            if (level > 0) tool.addUnsafeEnchantment(Enchantment.EFFICIENCY, level);
            tool.addUnsafeEnchantment(Enchantment.SILK_TOUCH, 1);
            player.getInventory().setItem(slot, tool);
        }
    }
    @Override public void tick(PartySession session, int elapsed) {
        if (elapsed % 20 == 0) for (Player player : session.survivors()) equip(session, player);
        super.tick(session, elapsed);
    }
    @Override public boolean breakBlock(PartySession session, Player player, Block block) {
        MinerColumn column = assigned.get(player.getUniqueId());
        return column != null && column.diggable(block.getX(), block.getY(), block.getZ()) && block.getType() != Material.BARRIER && block.getType() != Material.BEDROCK;
    }
    @Override public void hazard(PartySession session, Player player) { session.eliminate(player); }
    @Override public boolean physicsOnWrite(PartySession session) { return live(session); }
    @Override public boolean allowPhysics(PartySession session, Block block) { return live(session); }
    private boolean live(PartySession session) { return building && (session.state() == PartySession.State.PREPARING || session.state() == PartySession.State.COUNTDOWN || session.state() == PartySession.State.RUNNING); }
    @Override public boolean suppressArenaDrops() { return true; }
    @Override public boolean fadeBlock(PartySession session, Block block, Material material) {
        return live(session) && material == Material.AIR && (block.getType() == Material.SNOW || block.getType() == Material.SNOW_BLOCK || block.getType() == Material.FIRE);
    }
    @Override public boolean fallingBlockChange(PartySession session, EntityChangeBlockEvent event) {
        if (!live(session) || !(event.getEntity() instanceof FallingBlock falling)) return false;
        boolean insideShaft = columns.subList(0, usedColumns).stream().anyMatch(c -> c.x() == event.getBlock().getX() && c.z() == event.getBlock().getZ()
                && event.getBlock().getY() > c.floorY() && event.getBlock().getY() <= c.floorY() + c.layers());
        if (!insideShaft || !falling.getBlockData().getMaterial().hasGravity()) return false;
        if (!session.owns(falling)) {
            if (event.getBlock().getType() != falling.getBlockData().getMaterial()) return false;
            session.track(falling); falling.setDropItem(false); falling.setHurtEntities(false);
        }
        return true;
    }
}
