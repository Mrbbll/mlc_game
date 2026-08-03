package com.mlc.mlcgames.sandgame.gamephase;

import com.mlc.mlcgames.Teammanager;
import com.mlc.mlcgames.sandgame.Sandgame;
import com.mlc.mlcgames.sandgame.items.itemmanager;
import com.mlc.mlcgames.sandgame.managers.Timer;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;

import static com.mlc.mlcgames.Mlcgames.instance;

public class start {

    public static void startgame(){
        if (Sandgame.isstart) return;

        Sandgame.isstart=true;
        Sandgame.countdown=60;
        Sandgame.timer=new Timer();
        // period=20 才是 1 秒一次，原来 period=1（50ms）导致 60 秒倒计时 3 秒就走完
        Sandgame.timer.runTaskTimer(instance,20,20);
        Sandgame.player_money.clear();
        Sandgame.player_kill_count.clear();
        // 沙量从未初始化（恒 0），第一轮计时结束就直接判平局
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

    }

    private static void initstate(Player player) {
        player.getInventory().clear();
        player.setFoodLevel(20);
        player.setHealth(20);
        player.clearActivePotionEffects();
        // 冒险模式：配合物品 CanDestroy/CanPlaceOn 限制破坏与放置
        player.setGameMode(GameMode.ADVENTURE);
    }
}
