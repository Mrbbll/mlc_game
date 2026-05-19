package com.mlc.mlcgames.zombieday.managers;

import com.mlc.mlcgames.Teammanager;
import com.mlc.mlcgames.zombieday.Difficulty;
import com.mlc.mlcgames.zombieday.Zombiedaygame;
import com.mlc.mlcgames.zombieday.gamephase.End;
import com.mlc.mlcgames.zombieday.inv.TrushCan;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import static com.mlc.mlcgames.Mlcgames.*;
import static com.mlc.mlcgames.zombieday.Zombiedaygame.countdown;
import static com.mlc.mlcgames.zombieday.Zombiedaygame.turn;

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
                    return;
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
                    for(Player player: Teammanager.getteamplayer(Teammanager.zombieday_team)){
                        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION,20*15,0));
                    }
                    countdown = 120;
                }
                if(Zombiedaygame.zombiecount==0||(countdown==1&&Zombiedaygame.zombiecount<=5)){
                    if (turn<=29) {
                        turn++;
                        server.broadcast(miniMessage.deserialize("<b><#ff0033>Turn <#ff246d>"+ turn));
                        SpawnManager.Spawnzombie(turn);
                    }
                }
                for(Player player: Teammanager.getteamplayer(Teammanager.zombieday_team)){
                        scoreboard.updatesidebar(player);
                }
                if(Zombiedaygame.zombiecount<=0&&turn>=30){
                    End.end();
                }
            }
        }.runTaskTimer(instance,0,20);
    }
}
