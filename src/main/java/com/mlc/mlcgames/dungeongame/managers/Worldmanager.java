package com.mlc.mlcgames.dungeongame.managers;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Random;

/** 世界管理：创建全空(虚空)新世界、卸载并删除世界。 */
public class Worldmanager {

    /** 创建(或取回)一个全空的新世界，关闭结构生成。 */
    public static World createWorld(String name) {
        World world = Bukkit.getWorld(name);
        if (world != null) return world;

        WorldCreator creator = new WorldCreator(name);
        creator.generator(new VoidGenerator());
        creator.generateStructures(false);
        return creator.createWorld();
    }

    /** 把世界里的玩家踢回主世界，然后卸载并删除这个世界的文件夹。 */
    public static void unloadAndDelete(String name) {
        World world = Bukkit.getWorld(name);
        if (world == null) return;
        world.getPlayers().forEach(p -> p.teleportAsync(Bukkit.getWorlds().get(0).getSpawnLocation()));
        if (Bukkit.unloadWorld(world, false)) {
            deleteFolder(world.getWorldFolder());
        }
    }

    private static void deleteFolder(File folder) {
        if (folder == null || !folder.exists()) return;
        try {
            Files.walkFileTree(folder.toPath(), new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException ignored) {
        }
    }

    /** 虚空生成器：不生成任何方块，世界一开始是全空的。 */
    public static class VoidGenerator extends ChunkGenerator {
        @Override
        public void generateSurface(@NotNull WorldInfo worldInfo, @NotNull Random random, int x, int z,
                                    @NotNull ChunkData chunkData) {
        }

        @Override
        public void generateBedrock(@NotNull WorldInfo worldInfo, @NotNull Random random, int x, int z,
                                    @NotNull ChunkData chunkData) {
        }
    }
}
