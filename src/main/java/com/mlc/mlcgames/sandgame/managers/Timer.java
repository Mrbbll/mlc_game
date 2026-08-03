package com.mlc.mlcgames.sandgame.managers;

import com.mlc.mlcgames.sandgame.Sandgame;
import com.mlc.mlcgames.sandgame.gamephase.end;
import com.mlc.mlcgames.sandgame.items.itemmanager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class Timer extends BukkitRunnable {
    @Override
    public void run() {
        if(Sandgame.isstart){
            Sandgame.countdown--;
            // forEach 的 money+=5 只改局部参数不会写回 Map，必须 replaceAll
            Sandgame.player_money.replaceAll((player, money) -> money + 5);
            for (Player player : Bukkit.getOnlinePlayers()) {
                sidebarmanager.updatesidebar(player);
            }
            if(Sandgame.countdown<=0){
                Sandgame.countdown = 30;
                itemmanager.spawnsand(Sandgame.sand_spawn_loc);
                itemmanager.spawnitem(Sandgame.item_spawn_loc_1);
                itemmanager.spawnitem(Sandgame.item_spawn_loc_2);
                Sandgame.team_1_sand_count--;
                Sandgame.team_2_sand_count--;
                if(Sandgame.team_1_sand_count<=0&&Sandgame.team_2_sand_count<=0){
                    end.endgame_with_winner(3);
                }else if(Sandgame.team_1_sand_count<=0){
                    end.endgame_with_winner(1);
                }else if(Sandgame.team_2_sand_count<=0){
                    end.endgame_with_winner(2);
                }
            }
        }else {
            this.cancel();
        }
    }
}
