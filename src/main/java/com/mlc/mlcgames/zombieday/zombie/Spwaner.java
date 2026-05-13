package com.mlc.mlcgames.zombieday.zombie;

import io.papermc.paper.threadedregions.scheduler.EntityScheduler;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Zombie;

import java.util.Objects;

public class Spwaner {

    public static void spawnzombie(Location location, int count,int damage,int health){
        World world = location.getWorld();
        for(int i = 0;i<count;i++){
            Zombie zombie = (Zombie) world.spawnEntity(location, EntityType.ZOMBIE);
            zombie.setLootTable(new ZombieLootTable());
            Objects.requireNonNull(zombie.getAttribute(Attribute.ATTACK_DAMAGE)).setBaseValue(damage);
            Objects.requireNonNull(zombie.getAttribute(Attribute.MAX_HEALTH)).setBaseValue(health);
//            EntityScheduler entityScheduler = zombie.getScheduler();

        }
    }
}
