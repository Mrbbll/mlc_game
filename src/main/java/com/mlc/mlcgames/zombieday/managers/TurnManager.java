package com.mlc.mlcgames.zombieday.managers;

import com.mlc.mlcgames.Teammanager;
import com.mlc.mlcgames.zombieday.Difficulty;
import com.mlc.mlcgames.zombieday.Zombiedaygame;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import static com.mlc.mlcgames.Mlcgames.*;
import static com.mlc.mlcgames.zombieday.Zombiedaygame.countdown;

public class TurnManager {

    private static final World world = Bukkit.getWorld("World");
    private static Difficulty difficulty;

    public static void turncycle(){
        difficulty = Zombiedaygame.difficulty;

        BukkitTask cycle = new BukkitRunnable(){

            @Override
            public void run() {
                if(!Zombiedaygame.isstart){
                    this.cancel();
                }
                countdown--;
                if(countdown==90){
                    //入夜
                    if (world != null) {
                        world.setTime(15000);
                    }
                };

                if (countdown==0) {
                    //日出
                    if (world != null) {
                        world.setTime(0);
                    }
                    countdown = 120;
                }

                if(Zombiedaygame.zombiecount==0||(countdown==1&&Zombiedaygame.zombiecount<=5)){
                    Zombiedaygame.turn++;
                    server.broadcast(miniMessage.deserialize("Turn "+Zombiedaygame.turn));
                    SpawnManager.Spawnzombie(Zombiedaygame.turn);
                }
                for(Player player: Teammanager.getteamplayer(Teammanager.zombieday_team)){
                        scoreboard.updatesidebar(player);
                }
            }


        }.runTaskTimer(instance,0,20);
    }
}
