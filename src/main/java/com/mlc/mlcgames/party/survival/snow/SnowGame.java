package com.mlc.mlcgames.party.survival.snow;

import com.mlc.mlcgames.party.core.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/** Shared SNOW rules, with one separately configured game directory per source mode. */
public abstract class SnowGame implements PartyGame {
    protected final boolean runningFloor;
    protected final boolean digging;
    protected final boolean explosiveArrows;
    private Set<Material> materials;
    private Cuboid floor;
    private final Map<Block, Integer> falling = new LinkedHashMap<>();
    private final Queue<Block> shrinking = new ArrayDeque<>();
    private int edge;
    private int layer;

    protected SnowGame(boolean runningFloor, boolean digging, boolean explosiveArrows) {
        this.runningFloor = runningFloor;
        this.digging = digging;
        this.explosiveArrows = explosiveArrows;
    }
    @Override public void validate(PartyConfig config, PartyConfig.Arena arena) {
        floor = arena.region("floor");
        materials = config.materials("floor-materials");
        config.integer("fall-delay-ticks", 1, 100);
        config.integer("running-warmup-seconds", 0, 60);
        config.integer("shrink-start-seconds", 0, 7200);
        config.integer("shrink-step-seconds", 1, 60);
        config.integer("arrow-radius", 0, 3);
        config.integer("arrows-per-second", 1, 64);
    }
    @Override public List<Cuboid> writableRegions(PartyConfig.Arena arena) { return List.of(arena.region("floor")); }
    @Override public void start(PartySession session) {
        for (Player player : session.survivors()) {
            if (digging) {
                player.setGameMode(GameMode.SURVIVAL);
                Material[] tools = {Material.DIAMOND_PICKAXE, Material.DIAMOND_AXE, Material.DIAMOND_SHOVEL};
                for (int i = 0; i < tools.length; i++) {
                    ItemStack item = session.item(tools[i], "items.tool");
                    item.addUnsafeEnchantment(Enchantment.EFFICIENCY, 15);
                    player.getInventory().setItem(i, item);
                }
            }
            if (explosiveArrows) player.getInventory().setItem(0, session.item(Material.BOW, "items.bow"));
        }
    }
    private boolean mineable(PartySession session, Block block) { return session.writable(block) && materials.contains(block.getType()); }
    @Override public boolean breakBlock(PartySession session, Player player, Block block) { return digging && mineable(session, block); }
    @Override public boolean instantBreak(PartySession session, Player player, Block block) { return digging && mineable(session, block); }
    @Override public void projectile(PartySession session, ProjectileHitEvent event) {
        if (!explosiveArrows || event.getHitBlock() == null) return;
        Block hit = event.getHitBlock();
        int radius = session.config.integer("arrow-radius", 0, 3);
        for (int x = -radius; x <= radius; x++) for (int z = -radius; z <= radius; z++) {
            Block block = hit.getRelative(x, 0, z);
            if (mineable(session, block)) session.set(block, Material.AIR);
        }
    }
    @Override public void tick(PartySession session, int elapsed) {
        if (explosiveArrows && elapsed % 20 == 0) for (Player player : session.survivors()) player.getInventory().setItem(8, new ItemStack(Material.ARROW, session.config.integer("arrows-per-second", 1, 64)));
        if (runningFloor && elapsed >= session.config.integer("running-warmup-seconds", 0, 60) * 20) {
            for (Player player : session.survivors()) {
                Location loc = player.getLocation();
                // Source checks the centre and four offsets; resulting footprint covers block edges.
                for (double[] offset : new double[][]{{0, 0}, {-.6, 0}, {.6, 0}, {0, -.6}, {0, .6}}) {
                    Block block = loc.clone().add(offset[0], -1, offset[1]).getBlock();
                    if (mineable(session, block)) falling.putIfAbsent(block, elapsed + session.config.integer("fall-delay-ticks", 1, 100));
                }
            }
        }
        int budget = session.config.integer("limits.blocks-per-tick", 1, 2000);
        Iterator<Map.Entry<Block, Integer>> iterator = falling.entrySet().iterator();
        while (iterator.hasNext() && budget > 0) {
            Map.Entry<Block, Integer> entry = iterator.next();
            if (entry.getValue() > elapsed) continue;
            if (mineable(session, entry.getKey())) { session.set(entry.getKey(), Material.AIR); budget--; }
            iterator.remove();
        }
        int start = session.config.integer("shrink-start-seconds", 0, 7200) * 20;
        int step = session.config.integer("shrink-step-seconds", 1, 60) * 20;
        if (!runningFloor || digging) {
            if (elapsed >= start && (elapsed - start) % step == 0 && shrinking.isEmpty()) queueEdge(session);
            while (!shrinking.isEmpty() && budget-- > 0) {
                Block block = shrinking.remove();
                if (mineable(session, block)) session.set(block, Material.AIR);
            }
        }
    }
    private void queueEdge(PartySession session) {
        int x1 = floor.minX() + layer, x2 = floor.maxX() - layer;
        int z1 = floor.minZ() + layer, z2 = floor.maxZ() - layer;
        if (x2 - x1 < 2 || z2 - z1 < 2) return;
        for (int y = floor.minY(); y <= floor.maxY(); y++) {
            if (edge < 2) for (int z = z1; z <= z2; z++) shrinking.add(session.arena.world().getBlockAt(edge == 0 ? x1 : x2, y, z));
            else for (int x = x1; x <= x2; x++) shrinking.add(session.arena.world().getBlockAt(x, y, edge == 2 ? z1 : z2));
        }
        edge = (edge + 1) % 4;
        if (edge == 0) layer++;
    }
}
