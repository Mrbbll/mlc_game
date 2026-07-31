package com.mlc.mlcgames.sandgame.gamephase;

import com.mlc.mlcgames.Teammanager;
import com.mlc.mlcgames.sandgame.Sandgame;
import com.mlc.mlcgames.sandgame.managers.Timer;
import org.bukkit.entity.Player;

import static com.mlc.mlcgames.Mlcgames.instance;

public class start {

    public static void startgame(){
        Sandgame.isstart=true;
        Sandgame.countdown=60;
        Sandgame.timer=new Timer();
        Sandgame.timer.runTaskTimer(instance,20,1);
        Sandgame.player_money.clear();
        Sandgame.player_kill_count.clear();
        for(Player player: Teammanager.getteamplayer(Teammanager.sandgame_team_1)){
            player.getInventory().clear();
            player.clearActivePotionEffects();
            player.teleportAsync(Sandgame.team_1_loc);
        }
        for(Player player: Teammanager.getteamplayer(Teammanager.sandgame_team_2)){
            player.getInventory().clear();
            player.clearActivePotionEffects();
            player.teleportAsync(Sandgame.team_2_loc);
        }

    }
}
