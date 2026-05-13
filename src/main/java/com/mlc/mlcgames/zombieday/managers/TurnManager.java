package com.mlc.mlcgames.zombieday.managers;

import com.mlc.mlcgames.zombieday.Zombiedaygame;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import static com.mlc.mlcgames.Mlcgames.instance;

public class TurnManager {
    private static int countdown = 20*120;
    private static World world = Bukkit.getWorld("World");

    public static void turncycle(){
        BukkitTask cycle = new BukkitRunnable(){

            @Override
            public void run() {
                if(!Zombiedaygame.isstart){
                    this.cancel();
                }
                countdown--;
                if(countdown%60==45){
                    world.setTime(0);
                } else if (countdown%60==0) {
                    world.setTime(12000);
                }
                if(countdown%120==0){
                    Zombiedaygame.turn++;
                }
            }


        }.runTaskTimer(instance,0,20);
    }
}
