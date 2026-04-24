package com.mlc.mlcgames.bank.utils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

public class Fillutils {
    public static void replaceblock(Location location1, Location location2, Material material1,Material material2){
        int x = location1.getBlockX();
        int y = location1.getBlockY();
        int z = location1.getBlockZ();
        int x2 = location2.getBlockX();
        int y2 = location2.getBlockY();
        int z2 = location2.getBlockZ();
        for(int i = x;i<=x2;i++){
            for(int j = y;j<=y2;j++){
                for(int k = z;k<=z2;k++){
                    Material material = location1.getWorld().getBlockAt(i,j,k).getType();
                    if(material == material1){
                        location1.getWorld().getBlockAt(i,j,k).setType(material2);

                    }
                }
            }
        }
    }

}
