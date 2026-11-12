package com.mlc.mlcgames.party.core;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/** Durable arena baseline; captured before the first block is changed. */
public final class BlockJournal {
    private final File file;
    private final World world;
    private final List<String> baseline = new ArrayList<>();
    private final List<Cuboid> regions;
    private final int maxBlocks;
    private final Set<Long> seen = new HashSet<>();
    private int regionIndex;
    private long regionCursor;
    private CompletableFuture<Void> saving;
    private int cursor;

    public BlockJournal(File file, World world, List<Cuboid> regions, int maxBlocks) {
        this.file = file;
        this.world = world;
        this.regions = List.copyOf(regions);
        this.maxBlocks = maxBlocks;
        for (Cuboid region : regions) {
            if (region.volume() > maxBlocks) throw new IllegalArgumentException("可修改区域超过 limits.max-snapshot-blocks");
        }
    }

    private BlockJournal(File file, World world, List<String> baseline) {
        this.file = file;
        this.world = world;
        this.regions = List.of();
        this.maxBlocks = 0;
        this.baseline.addAll(baseline);
    }

    public static BlockJournal recover(File file) throws Exception {
        YamlConfiguration data = new YamlConfiguration();
        data.load(file);
        World world = Bukkit.getWorld(UUID.fromString(Objects.requireNonNull(data.getString("world"))));
        if (world == null) throw new IllegalStateException("恢复场地的世界尚未加载；journal.yml 已保留");
        List<String> blocks = data.getStringList("blocks");
        // Validate all entries before attempting recovery.
        for (String row : blocks) {
            String[] entry = row.split("\\|", 3);
            String[] xyz = entry[0].split(",");
            if (entry.length < 2 || xyz.length != 3) throw new IllegalArgumentException("场地恢复记录格式错误");
            for (String coordinate : xyz) Integer.parseInt(coordinate);
            new BlockSnapshot(entry[1], entry.length == 3 ? entry[2] : null).validate();
        }
        return new BlockJournal(file, world, blocks);
    }

    public boolean capture(int budget) throws IOException {
        if (saving != null) {
            if (!saving.isDone()) return false;
            awaitSave();
            return true;
        }
        while (budget-- > 0 && regionIndex < regions.size()) {
            Cuboid region = regions.get(regionIndex);
            long widthZ = (long) region.maxZ() - region.minZ() + 1;
            long height = (long) region.maxY() - region.minY() + 1;
            long index = regionCursor++;
            int x = region.minX() + (int) (index / (height * widthZ));
            int y = region.minY() + (int) (index / widthZ % height);
            int z = region.minZ() + (int) (index % widthZ);
            if (regionCursor == region.volume()) { regionIndex++; regionCursor = 0; }
            long key = ((long) x & 0x3ffffff) << 38 | ((long) z & 0x3ffffff) << 12 | ((long) y & 0xfff);
            if (!seen.add(key)) continue;
            if (seen.size() > maxBlocks) throw new IllegalArgumentException("可修改区域总量超过 limits.max-snapshot-blocks");
            Block block = world.getBlockAt(x, y, z);
            BlockSnapshot snapshot = BlockSnapshot.capture(block);
            baseline.add(block.getX() + "," + block.getY() + "," + block.getZ() + "|" + snapshot.data() + (snapshot.nbt() == null ? "" : "|" + snapshot.nbt()));
        }
        if (regionIndex < regions.size()) return false;
        seen.clear();
        String worldId = world.getUID().toString();
        saving = CompletableFuture.runAsync(() -> {
            try { save(worldId); }
            catch (IOException e) { throw new CompletionException(e); }
        });
        return false;
    }

    private void save(String worldId) throws IOException {
        YamlConfiguration data = new YamlConfiguration();
        data.set("world", worldId);
        data.set("blocks", baseline);
        File parent = file.getParentFile();
        if (!parent.isDirectory() && !parent.mkdirs()) throw new IOException("无法创建 " + parent);
        File temporary = new File(parent, file.getName() + ".tmp");
        data.save(temporary);
        java.nio.file.Files.move(temporary.toPath(), file.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
    }

    private void awaitSave() throws IOException {
        try { saving.join(); }
        catch (CompletionException e) { throw new IOException("无法保存场地恢复记录", e.getCause()); }
    }

    /** Wait for an in-flight save before discarding an unused baseline. */
    public boolean discardCapture() throws IOException {
        if (saving == null) return true;
        if (!saving.isDone()) return false;
        awaitSave();
        delete();
        return true;
    }

    public void discardCaptureOnShutdown() throws IOException {
        if (saving != null) { awaitSave(); delete(); }
    }

    public void rewind() { cursor = 0; }

    public boolean restore(int budget) {
        int end = BatchWindow.end(baseline.size(), cursor, budget);
        for (; cursor < end; cursor++) {
            String[] entry = baseline.get(cursor).split("\\|", 3);
            String[] xyz = entry[0].split(",");
            new BlockSnapshot(entry[1], entry.length == 3 ? entry[2] : null).apply(world.getBlockAt(Integer.parseInt(xyz[0]), Integer.parseInt(xyz[1]), Integer.parseInt(xyz[2])), false);
        }
        return cursor == baseline.size();
    }

    public void delete() throws IOException { java.nio.file.Files.deleteIfExists(file.toPath()); }
    public int size() { return baseline.size(); }
}
