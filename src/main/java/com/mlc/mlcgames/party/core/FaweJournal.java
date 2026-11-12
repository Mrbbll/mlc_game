package com.mlc.mlcgames.party.core;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.util.SideEffectSet;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/** Persistent per-region FAWE schematics, with a separate active recovery marker. */
public final class FaweJournal {
    private final Path marker;
    private final Path template;
    private final com.sk89q.worldedit.world.World world;
    private final List<Cuboid> regions;
    private CompletableFuture<Void> restore;

    private FaweJournal(Path marker, Path template, World world, List<Cuboid> regions) {
        this.marker = marker;
        this.template = template;
        this.world = BukkitAdapter.adapt(world);
        this.regions = List.copyOf(regions);
    }

    private static String key(UUID world, List<Cuboid> regions) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest((world + ":" + regions).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) { throw new IllegalStateException(e); }
    }

    private static void checkSize(List<Cuboid> regions, int max) {
        long size = 0;
        for (Cuboid region : regions) {
            size = Math.addExact(size, region.volume());
            if (size > max) throw new IllegalArgumentException("可修改区域总量超过 limits.max-snapshot-blocks（重叠区域分别计算）");
        }
    }

    private static BlockVector3 min(Cuboid r) { return BlockVector3.at(r.minX(), r.minY(), r.minZ()); }
    private static BlockVector3 max(Cuboid r) { return BlockVector3.at(r.maxX(), r.maxY(), r.maxZ()); }

    /** Called off-thread after the Bukkit world has been adapted on the server thread. */
    public static void saveTemplate(Path root, UUID worldId, com.sk89q.worldedit.world.World world, List<Cuboid> regions, int limit) throws Exception {
        checkSize(regions, limit);
        Path base = root.resolve(key(worldId, regions));
        Files.createDirectories(base);
        String version = UUID.randomUUID().toString();
        Path dir = base.resolve(version);
        Files.createDirectories(dir);
        for (int i = 0; i < regions.size(); i++) {
            Cuboid r = regions.get(i);
            CuboidRegion region = new CuboidRegion(world, min(r), max(r));
            try (Clipboard clipboard = new BlockArrayClipboard(region)) {
                clipboard.setOrigin(min(r));
                ForwardExtentCopy copy = new ForwardExtentCopy(world, region, clipboard, min(r));
                copy.setCopyingEntities(false);
                copy.setCopyingBiomes(false);
                Operations.complete(copy);
                try (OutputStream out = Files.newOutputStream(dir.resolve(i + ".schem"));
                     var writer = BuiltInClipboardFormat.SPONGE_V3_SCHEMATIC.getWriter(out)) { writer.write(clipboard); }
            }
        }
        Path temporary = base.resolve("latest.tmp");
        Files.writeString(temporary, version, StandardCharsets.UTF_8);
        Files.move(temporary, base.resolve("latest"), StandardCopyOption.REPLACE_EXISTING);
    }

    public static FaweJournal begin(Path root, World world, List<Cuboid> regions, int limit) throws IOException {
        checkSize(regions, limit);
        Path base = root.resolve("templates").resolve(key(world.getUID(), regions));
        Path latest = base.resolve("latest");
        if (!Files.isRegularFile(latest)) throw new IllegalStateException("未保存场地结构，请先使用 /party save <游戏> [地图]");
        String version = Files.readString(latest, StandardCharsets.UTF_8).trim();
        UUID.fromString(version);
        Path template = base.resolve(version);
        for (int i = 0; i < regions.size(); i++) if (!Files.isRegularFile(template.resolve(i + ".schem"))) throw new IOException("场地结构文件缺失：" + template);
        Files.createDirectories(root);
        Path marker = root.resolve("active.dat"), temporary = root.resolve("active.dat.tmp");
        if (Files.exists(marker)) throw new IOException("已有待恢复的场地记录");
        try (DataOutputStream out = new DataOutputStream(Files.newOutputStream(temporary))) {
            out.writeUTF(world.getUID().toString());
            out.writeUTF(key(world.getUID(), regions));
            out.writeUTF(version);
            out.writeInt(regions.size());
            for (Cuboid r : regions) { out.writeInt(r.minX()); out.writeInt(r.minY()); out.writeInt(r.minZ()); out.writeInt(r.maxX()); out.writeInt(r.maxY()); out.writeInt(r.maxZ()); }
        }
        Files.move(temporary, marker, StandardCopyOption.REPLACE_EXISTING);
        return new FaweJournal(marker, template, world, regions);
    }

    public static FaweJournal recover(Path root) throws IOException {
        Path marker = root.resolve("active.dat");
        try (DataInputStream in = new DataInputStream(Files.newInputStream(marker))) {
            UUID id = UUID.fromString(in.readUTF());
            String storedKey = in.readUTF(), version = in.readUTF();
            UUID.fromString(version);
            int count = in.readInt();
            if (count < 0 || count > 10000) throw new IOException("场地恢复区域数量非法");
            List<Cuboid> regions = new ArrayList<>();
            for (int i = 0; i < count; i++) regions.add(new Cuboid(in.readInt(), in.readInt(), in.readInt(), in.readInt(), in.readInt(), in.readInt()));
            if (!key(id, regions).equals(storedKey)) throw new IOException("场地恢复记录校验失败");
            World world = Bukkit.getWorld(id);
            if (world == null) throw new IOException("恢复场地的世界尚未加载");
            return new FaweJournal(marker, root.resolve("templates").resolve(storedKey).resolve(version), world, regions);
        }
    }

    public boolean restore() {
        if (restore == null) restore = CompletableFuture.runAsync(() -> {
            try (EditSession edit = WorldEdit.getInstance().newEditSessionBuilder().world(world).maxBlocks(-1).fastMode(true).setSideEffectSet(SideEffectSet.none()).build()) {
                for (int i = 0; i < regions.size(); i++) {
                    try (InputStream input = Files.newInputStream(template.resolve(i + ".schem"));
                         var reader = BuiltInClipboardFormat.SPONGE_V3_SCHEMATIC.getReader(input);
                         Clipboard clipboard = reader.read()) {
                        var copy = new ForwardExtentCopy(clipboard, clipboard.getRegion(), clipboard.getMinimumPoint(), edit, min(regions.get(i)));
                        copy.setCopyingEntities(false);
                        copy.setCopyingBiomes(false);
                        Operations.complete(copy);
                    }
                }
            } catch (Exception e) { throw new CompletionException(e); }
        });
        if (!restore.isDone()) return false;
        restore.join();
        return true;
    }

    public void rewind() { if (restore == null || restore.isDone()) restore = null; }
    public void delete() throws IOException { Files.deleteIfExists(marker); }
}
