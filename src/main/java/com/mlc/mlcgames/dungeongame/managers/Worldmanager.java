package com.mlc.mlcgames.dungeongame.managers;

import com.mlc.mlcgames.dungeongame.rooms.Room;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.block.Biome;
import org.bukkit.block.structure.Mirror;
import org.bukkit.block.structure.StructureRotation;
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
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;

public class Worldmanager {
    public static World dungeonWorld;
    public static int layoutOriginX = 0;
    public static int layoutOriginY = 80;
    public static int layoutOriginZ = 0;
    /** Every room template must fit inside this slot. */
    public static int layoutCellSize = 64;

    public static void createDungeonWorld() {
        World existing = Bukkit.getWorld("Dungeongame");
        if (existing != null) {
            dungeonWorld = existing;
            return;
        }
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
        if (dungeonWorld == null) {
            throw new IllegalStateException("Dungeon world has not been created");
        }
        if (room.getSizeX() > layoutCellSize || room.getSizeZ() > layoutCellSize) {
            throw new IllegalArgumentException("Room '" + room.getName() + "' is larger than layout.cell-size " + layoutCellSize);
        }
        int x = layoutOriginX + locpoint.x * layoutCellSize;
        int z = layoutOriginZ + locpoint.y * layoutCellSize;
        if (room.isSchematic()) {
            try (EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(dungeonWorld))) {
                Operations.complete(new ClipboardHolder(room.getClipboard()).createPaste(editSession)
                        .to(BlockVector3.at(x, layoutOriginY, z)).ignoreAirBlocks(false).build());
            } catch (Exception exception) {
                throw new IllegalStateException("Unable to paste schematic room '" + room.getName() + "'", exception);
            }
            return;
        }
        room.getStructure().place(new org.bukkit.Location(dungeonWorld, x, layoutOriginY, z),
                true, StructureRotation.NONE, Mirror.NONE, -1, 1.0F, new Random());
    }
}
