package com.mlc.mlcgames.party.core;

import com.sk89q.jnbt.*;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.SideEffectSet;
import com.sk89q.worldedit.world.block.BaseBlock;
import org.enginehub.linbus.stream.LinBinaryIO;
import org.enginehub.linbus.tree.LinRootEntry;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import java.io.*;
import java.util.Base64;

/** Full durable block state, including container contents, signs and other tile data. */
public record BlockSnapshot(String data, String nbt) {
    public static BlockSnapshot capture(Block block) {
        String encoded = null;
        if (block.getState() instanceof TileState) {
            var full = BukkitAdapter.adapt(block.getWorld()).getFullBlock(BlockVector3.at(block.getX(), block.getY(), block.getZ()));
            CompoundTag tag = full.getNbtData();
            if (tag == null) throw new IllegalStateException("无法完整记录方块实体：" + block.getLocation());
            try {
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                try (DataOutputStream output = new DataOutputStream(bytes)) {
                    LinBinaryIO.write(output, new LinRootEntry("", tag.toLinTag()));
                }
                encoded = Base64.getEncoder().encodeToString(bytes.toByteArray());
            } catch (IOException e) { throw new IllegalStateException("无法序列化方块实体", e); }
        }
        return new BlockSnapshot(block.getBlockData().getAsString(), encoded);
    }
    private CompoundTag tag() {
        if (nbt == null || nbt.isEmpty()) return null;
        if (nbt.length() > 16_777_216) throw new IllegalArgumentException("方块实体数据过大");
        try (DataInputStream input = new DataInputStream(new ByteArrayInputStream(Base64.getDecoder().decode(nbt)))) {
            return new CompoundTag(LinRootEntry.readFrom(LinBinaryIO.read(input)).value());
        } catch (IOException e) { throw new IllegalArgumentException("方块实体数据损坏", e); }
    }
    public void validate() { Bukkit.createBlockData(data); tag(); }
    public void apply(Block block, boolean physics) {
        if (nbt == null || nbt.isEmpty()) { block.setBlockData(Bukkit.createBlockData(data), physics); return; }
        try {
            BaseBlock full = new BaseBlock(BukkitAdapter.adapt(Bukkit.createBlockData(data)), tag());
            BukkitAdapter.adapt(block.getWorld()).setBlock(BlockVector3.at(block.getX(), block.getY(), block.getZ()), full, SideEffectSet.none());
        } catch (Exception e) { throw new IllegalStateException("无法恢复完整方块：" + block.getLocation(), e); }
    }
}
