package com.mlc.mlcgames.dungeongame.gamephase;

import com.mlc.mlcgames.dungeongame.Dungeongame;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.block.Biome;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/** 初始化：从 dungeongame.yml 读取配置并组装房间列表。 */
public class dungeongameinit {
    public static void init(){

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
                return List.of();
            }
        });
        wc.environment(World.Environment.CUSTOM);

    }
}
