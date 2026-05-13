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
                if(countdown%120==90){
                    //入夜
                    world.setTime(15000);
                } else if (countdown%120==0) {
                    //日出
                    world.setTime(0);
                }
                if(countdown%120==0){
                    Zombiedaygame.turn++;
                    SpawnPoint.Spawnzombie(Zombiedaygame.turn);
                }
            }


        }.runTaskTimer(instance,0,20);
    }
}
