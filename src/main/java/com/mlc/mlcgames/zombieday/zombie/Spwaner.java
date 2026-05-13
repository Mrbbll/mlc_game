package com.mlc.mlcgames.zombieday.zombie;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Zombie;

public class Spwaner {

    public static void spawnzombie(Location location, int count){
        World world = location.getWorld();
        for(int i = 0;i<count;i++){

            Zombie zombie = (Zombie) world.spawnEntity(location, EntityType.ZOMBIE);

            zombie.setLootTable(new ZombieLootTable());
        }
    }
}
