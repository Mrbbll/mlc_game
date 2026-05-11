package com.mlc.mlcgames.zombieday.listener;

import com.mlc.mlcgames.zombieday.Zombiedaygame;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import static com.mlc.mlcgames.Mlcgames.instance;

public class Obstacle {
    private int durability = 9;

    public Obstacle(Location location){

        BukkitTask fixtask = new BukkitRunnable(){
            @Override
            public void run() {
                if(!Zombiedaygame.isstart){
                    this.cancel();
                }
                boolean hasplayer = false;
                for(Entity entity: location.getNearbyEntities(1.5,1,1.5)){
                    if(entity instanceof Player){
                        hasplayer = true;
                        break;
                    }
                }
                if(hasplayer && durability < 9){
                    durability++;
                    fixevent();
                }
            }
        }.runTaskTimer(instance,0,20);

        BukkitTask breaktask = new BukkitRunnable(){
            @Override
            public void run() {
                if(!Zombiedaygame.isstart){
                    this.cancel();
                }
                for(Entity entity: location.getNearbyEntities(1.5,1,1.5)){
                    if(!(entity instanceof Player)){
                        if(durability>0){
                            durability--;
                            breakevent();
                        }
                        return;
                    }
                }
            }
        }.runTaskTimer(instance,0,20);

    }

    private void breakevent() {

    }

    private void fixevent() {

    }

    ;
}
