package com.mlc.mlcgames.dungeongame.gamephase;

import com.mlc.mlcgames.dungeongame.Dungeongame;
import com.mlc.mlcgames.dungeongame.managers.Worldmanager;
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

        Worldmanager.createDungeonWorld();


    }
}
