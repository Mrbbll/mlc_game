package com.mlc.mlcgames.dungeongame.managers;

import com.mlc.mlcgames.dungeongame.rooms.Room;
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
import java.util.Random;

public class Worldmanager {
    public static World dungeonWorld;

    public static void createDungeonWorld() {
        WorldCreator wc = WorldCreator.name("Dungeongame");
        wc.generateStructures(false);
        wc.bonusChest(false);
        wc.hardcore(false);
        wc.biomeProvider(new BiomeProvider() {
            @Override
            public @NotNull Biome getBiome(@NotNull WorldInfo worldInfo, int x, int y, int z) {
                return Biome.THE_VOID;
            }

            @Override
            public @NotNull List<Biome> getBiomes(@NotNull WorldInfo worldInfo) {
                return List.of(Biome.THE_VOID);
            }
        });
        wc.environment(World.Environment.NORMAL);
        wc.generator(new ChunkGenerator() {
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

        dungeonWorld = wc.createWorld();
    }

    public static void deleteDungeonWorld() {
        Bukkit.unloadWorld(dungeonWorld,false);
        File worldFolder = dungeonWorld.getWorldFolder();
        boolean isSuccess = worldFolder.delete();
        if(isSuccess){
            System.out.println("Dungeon World deleted successfully.");
        }else {
            System.out.println("Dungeon World delete failed.");
        }
    }

    public static void putRoomInWorld(Room room, RoomSpawner.Locpoint locpoint){


    }
}
