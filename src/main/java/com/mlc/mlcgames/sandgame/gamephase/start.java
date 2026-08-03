package com.mlc.mlcgames.sandgame.gamephase;

import com.mlc.mlcgames.Teammanager;
import com.mlc.mlcgames.sandgame.Sandgame;
import com.mlc.mlcgames.sandgame.items.itemmanager;
import com.mlc.mlcgames.sandgame.managers.Timer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;

import static com.mlc.mlcgames.Mlcgames.instance;

public class start {

    public static void startgame(){
        if (Sandgame.isstart) return;

        Sandgame.isstart=true;
        Sandgame.countdown=60;
        Sandgame.timer=new Timer();
        Sandgame.timer.runTaskTimer(instance,20,1);
        Sandgame.player_money.clear();
        Sandgame.player_kill_count.clear();
        for(Player player: Teammanager.getteamplayer(Teammanager.sandgame_team_1)){
            initstate(player);
            CompletableFuture<Boolean> completableFuture = player.teleportAsync(Sandgame.team_1_loc);
            completableFuture.thenAccept(aBoolean -> {
                if(aBoolean){
                    itemmanager.giveBaseItems(player);
                }
            });
        }
        for(Player player: Teammanager.getteamplayer(Teammanager.sandgame_team_2)){
            initstate(player);
            CompletableFuture<Boolean> completableFuture = player.teleportAsync(Sandgame.team_2_loc);
            completableFuture.thenAccept(aBoolean -> {
                if(aBoolean){
                    itemmanager.giveBaseItems(player);
                }
            });
        }
        Bukkit.getPluginManager().registerEvents(Sandgame.sandGameListener,instance);

    }

    private static void initstate(Player player) {
        player.getInventory().clear();
        player.setFoodLevel(20);
        player.setHealth(20);
        player.clearActivePotionEffects();

    }
}
