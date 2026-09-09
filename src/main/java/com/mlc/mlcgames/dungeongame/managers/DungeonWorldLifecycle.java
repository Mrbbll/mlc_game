package com.mlc.mlcgames.dungeongame.managers;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.block.Biome;
import org.bukkit.generator.BiomeProvider;
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
import java.util.List;

/** 只负责地牢世界的创建、卸载和受保护删除。 */
final class DungeonWorldLifecycle {
    private static final String WORLD_NAME = "Dungeongame";

    private DungeonWorldLifecycle() { }

    static void create() {
        delete();
        WorldCreator creator = WorldCreator.name(WORLD_NAME);
        creator.generateStructures(false);
        creator.bonusChest(false);
        creator.hardcore(false);
        creator.biomeProvider(new BiomeProvider() {
            @Override
            public @NotNull Biome getBiome(@NotNull WorldInfo worldInfo, int x, int y, int z) {
                return Biome.THE_VOID;
            }

            @Override
            public @NotNull List<Biome> getBiomes(@NotNull WorldInfo worldInfo) {
                return List.of(Biome.THE_VOID);
            }
        });
        creator.environment(World.Environment.NORMAL);
        creator.generator(new ChunkGenerator() {
            @Override
            public boolean shouldGenerateNoise() {
                return super.shouldGenerateNoise();
            }

            @Override
            public boolean shouldGenerateSurface() {
                return super.shouldGenerateSurface();
            }

            @Override
            public boolean shouldGenerateCaves() {
                return super.shouldGenerateCaves();
            }

            @Override
            public boolean shouldGenerateMobs() {
                return super.shouldGenerateMobs();
            }

            @Override
            public boolean shouldGenerateDecorations() {
                return super.shouldGenerateDecorations();
            }

            @Override
            public boolean shouldGenerateStructures() {
                return super.shouldGenerateStructures();
            }
        });
        Worldmanager.dungeonWorld = creator.createWorld();
    }

    static void delete() {
        World existing = Bukkit.getWorld(WORLD_NAME);
        File worldFolder = existing != null
                ? existing.getWorldFolder()
                : new File(Bukkit.getWorldContainer(), WORLD_NAME);
        if (!worldFolder.exists()) return;

        verifyTarget(worldFolder, existing);
        if (existing != null && !Bukkit.unloadWorld(existing, false)) {
            throw new IllegalStateException("Unable to unload the current dungeon world");
        }
        try {
            Files.walkFileTree(worldFolder.toPath(), new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path directory, IOException exception) throws IOException {
                    if (exception != null) throw exception;
                    Files.delete(directory);
                    return FileVisitResult.CONTINUE;
                }
            });
            Worldmanager.dungeonWorld = null;
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to delete the previous dungeon world", exception);
        }
    }

    /**
     * 删除前同时核对名称和位置。即使外部传入了错误目录，也不能删除世界容器或其他世界。
     */
    private static void verifyTarget(File folder, World loadedWorld) {
        Path root = Bukkit.getWorldContainer().toPath().toAbsolutePath().normalize();
        Path target = folder.toPath().toAbsolutePath().normalize();
        boolean namedDungeon = target.getFileName() != null
                && WORLD_NAME.equalsIgnoreCase(target.getFileName().toString());
        boolean exactLoadedWorldFolder = loadedWorld != null
                && target.equals(loadedWorld.getWorldFolder().toPath().toAbsolutePath().normalize());
        boolean insideWorldContainer = target.startsWith(root) && !target.equals(root);
        if (!namedDungeon || (!exactLoadedWorldFolder && !insideWorldContainer)) {
            throw new IllegalArgumentException(
                    "Refusing to delete a world folder outside the named dungeon target: " + target);
        }
    }
}
