package com.mlc.mlcgames.party.survival.believer;

import com.mlc.mlcgames.party.core.*;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/** Source: minecraft:believer. Random markers advance the highest remaining block. */
public final class BelieverGame implements PartyGame {
    private Cuboid floor;
    private List<Block> cells;
    private int prepared;

    @Override public void validate(PartyConfig config, PartyConfig.Arena arena) {
        floor = arena.region("floor");
        if (floor.minY() != floor.maxY()) throw new IllegalArgumentException("信仰方块 floor 必须为单层地板");
        config.integer("changes-per-step", 1, config.integer("limits.blocks-per-tick", 1, 2000));
        config.integer("step-ticks", 1, 200);
        config.integer("warmup-seconds", 0, 60);
    }

    @Override public List<Cuboid> writableRegions(PartyConfig.Arena arena) { return List.of(arena.region("floor")); }

    @Override public boolean prepare(PartySession session, int budget) {
        if (cells == null) {
            cells = new ArrayList<>();
            for (int x = floor.minX(); x <= floor.maxX(); x++) for (int z = floor.minZ(); z <= floor.maxZ(); z++) cells.add(session.arena.world().getBlockAt(x, floor.minY(), z));
        }
        int end = Math.min(cells.size(), prepared + budget);
        for (; prepared < end; prepared++) session.set(cells.get(prepared), Material.WHITE_WOOL);
        return prepared == cells.size();
    }

    @Override public void start(PartySession session) {}

    @Override public void tick(PartySession session, int elapsed) {
        if (elapsed < session.config.integer("warmup-seconds", 0, 60) * 20 || elapsed % session.config.integer("step-ticks", 1, 200) != 0) return;
        for (int i = 0; i < session.config.integer("changes-per-step", 1, 1000) && !cells.isEmpty(); i++) {
            Block block = cells.get(ThreadLocalRandom.current().nextInt(cells.size()));
            Material next = switch (block.getType()) {
                case WHITE_WOOL -> Material.YELLOW_WOOL;
                case YELLOW_WOOL -> Material.ORANGE_WOOL;
                case ORANGE_WOOL -> Material.RED_WOOL;
                default -> Material.AIR;
            };
            session.set(block, next);
        }
    }
}
