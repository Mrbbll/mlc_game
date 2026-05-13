package com.mlc.mlcgames.zombieday.listener;

import com.mlc.mlcgames.zombieday.Zombiedaygame;
import org.bukkit.GameMode;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import static com.mlc.mlcgames.Mlcgames.instance;

public class saveListener {

    public saveListener(Player player, Zombie zombie){
        //让玩家不能乱动
        BukkitTask savetask = new BukkitRunnable(){
            @Override
            public void run() {
                if(player==null||!zombie.isValid()|| !Zombiedaygame.isstart){
                    this.cancel();
                    return;
                }
                player.setGameMode(GameMode.SPECTATOR);

                player.setSpectatorTarget(zombie);

            }
        }.runTaskTimer(instance,0,2);
    }
}
