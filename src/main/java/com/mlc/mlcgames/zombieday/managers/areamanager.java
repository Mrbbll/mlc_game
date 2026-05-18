package com.mlc.mlcgames.zombieday.managers;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class areamanager {
    public static void initarea(Location location){
        if(!location.isChunkLoaded()){
            location.getChunk().load();
        }
        Block block = location.getBlock();
        Block block1 = block.getRelative(BlockFace.DOWN);
        Block block2 = block1.getRelative(BlockFace.DOWN);
        block1.setType(Material.COBBLESTONE_WALL);
        block2.setType(Material.COBBLESTONE_WALL);
    }

    public static void openarea(Location location){
        if(!location.isChunkLoaded()){
            location.getChunk().load();
        }
        Block block = location.getBlock();
        Block block1 = block.getRelative(BlockFace.DOWN);
        Block block2 = block1.getRelative(BlockFace.DOWN);
        block1.setType(Material.AIR);
        block2.setType(Material.AIR);
    }
}
