package com.mlc.mlcgames.sandgame.gamephase;

import com.mlc.mlcgames.Teammanager;
import com.mlc.mlcgames.sandgame.Sandgame;
import com.mlc.mlcgames.sandgame.items.itemmanager;
import com.mlc.mlcgames.sandgame.managers.StealTimer;
import com.mlc.mlcgames.sandgame.managers.Timer;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;

import static com.mlc.mlcgames.Mlcgames.instance;

public class start {

    public static void startgame(){
        if (Sandgame.isstart) return;

        Sandgame.isstart=true;
        Sandgame.countdown=30;
        Sandgame.timer=new Timer();
        Sandgame.stealtimer=new StealTimer();
        Sandgame.timer.runTaskTimer(instance,20,20);
        Sandgame.stealtimer.runTaskTimer(instance,20,1);
        Sandgame.player_money.clear();
        Sandgame.player_kill_count.clear();
        Sandgame.team_1_sand_count = 10;
        Sandgame.team_2_sand_count = 10;
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
        instance.getServer().broadcast(Sandgame.start_msg);
    }

    private static void initstate(Player player) {
        player.getInventory().clear();
        player.setFoodLevel(20);
        player.setHealth(20);
        player.clearActivePotionEffects();
        player.playSound(player, Sound.ENTITY_ENDER_DRAGON_GROWL,1.0f,0.2f);
        player.setGameMode(GameMode.ADVENTURE);
    }
}
