package com.mlc.mlcgames.bank.utils;

import net.kyori.adventure.text.Component;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.List;

import static com.mlc.mlcgames.Mlcgames.instance;


public class Fillutils {
    public static List<Block> replaceblocks = new ArrayList<>();

    public static void getreplaceblock(Location location1, Location location2, Material material1,Material material2){
        int minX = Math.min(location1.getBlockX(), location2.getBlockX());
        int maxX = Math.max(location1.getBlockX(), location2.getBlockX());
        int minY = Math.min(location1.getBlockY(), location2.getBlockY());
        int maxY = Math.max(location1.getBlockY(), location2.getBlockY());
        int minZ = Math.min(location1.getBlockZ(), location2.getBlockZ());
        int maxZ = Math.max(location1.getBlockZ(), location2.getBlockZ());
        replaceblocks.clear();
        for(int i = minX;i<=maxX;i++){
            for(int j = minY;j<=maxY;j++){
                for(int k = minZ;k<=maxZ;k++){
                    Chunk chunk = location1.getWorld().getChunkAt(i,k);
                    if(!chunk.isLoaded()){
                        chunk.load();
                    }
                    Material material = location1.getWorld().getBlockAt(i,j,k).getType();
                    if(material == material1){
                        replaceblocks.add(location1.getWorld().getBlockAt(i,j,k));
                    }
                }
            }
        }
    }



    public static void replaceblock(Material material){
        instance.getServer().broadcast(Component.text("替换中"));
        for(Block block : replaceblocks){
            block.setType(material);
        }
        instance.getServer().broadcast(Component.text("替换成功"));
        }


}
