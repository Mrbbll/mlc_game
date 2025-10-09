package com.mlc.mlcgames.bank;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;
import java.util.Set;

import static com.mlc.mlcgames.Gameinit.*;
import static com.mlc.mlcgames.Mlcgames.*;
import static com.mlc.mlcgames.Teammanager.team_1;
import static com.mlc.mlcgames.Teammanager.team_2;

public class Bank_gamestart {
    public final Bank_bossbar bankBossbar = new Bank_bossbar();
    int[] time = {0,0};
    int timemax;
    public BukkitTask bossbar_timer;
    public BukkitTask title_timer;

    public Bank_gamestart(){
    }

    public void start(){
        Bukkit.broadcast(Component.text("游戏开始"));
        switch (bank_gamemode){
            //battlebox模式
            case 0:{
                //每次重新设置值
                time[0] = 0;//title
                time[1] = 10;//bossbar and game time
                timemax = time[1];

                isstart = true;
                Bukkit.broadcast(Component.text("battlebox model"));
                //传送和设置重生点
                for(Player player:ingamepalyer){
                    player.teleport(bank_lobby);
                    player.setRespawnLocation(bank_respawn);
                }

                //给东西
                for(Player player:ingamepalyer){
                    giveitem(player);
                }

                //出字
                title_timer = new BukkitRunnable() {
                    @Override
                    public void run() {
                        for (Player player:ingamepalyer){
                            player.showTitle(Title.title(Component.text(5-time[0]),Component.text(""), Title.Times.times(Duration.ZERO,Duration.ofSeconds(1),Duration.ZERO)));
                            if(time[0] > 3 ) this.cancel();
                            time[0]++;
                        }
                    }
                }.runTaskTimer(instance,20,20);

                //开门

                //计时和bossbar
                bankBossbar.init();
                for(Player player :ingamepalyer){
                    player.showTitle(Title.title(Component.text("game start!!"),Component.text("")));
                    player.showBossBar(bankBossbar.bossBar);
                }


                //倒计时结束
                bossbar_timer = new BukkitRunnable(){
                    @Override
                    public void run() {
                        if(time[1] <= 0){
                            new Bank_gameend().endgame(bankBossbar.bossBar);
                            this.cancel();
                        }
                        bankBossbar.progress = (float) time[1]--/ timemax;
                        bankBossbar.bossBar.progress(bankBossbar.progress);
                    }
                }.runTaskTimer(instance,100,20);

            break;
            }

            //小偷模式
            case 1:{
                Bukkit.broadcast(Component.text("thief model"));
                break;
            }
        }
    }

    public void endtimer(){
        if (bossbar_timer != null && !bossbar_timer.isCancelled()) {
            bossbar_timer.cancel();
            bossbar_timer = null; // 清空引用
        }
    }

    public void giveitem(Player player){

        if(teammanager.isPlayerInTeam(player, team_1)){
            Set<String> tags = player.getScoreboardTags();
            for(String tag : tags){
                switch (tag){
                    case "SHIELD":{
                        player.give(itemmanger.a_itemStack1);
                        break;
                    }
                    case "STONE_SWORD":{
                        player.give(itemmanger.a_itemStack2);
                        break;
                    }
                    case "CROSSBOW":{
                        player.give(itemmanger.a_itemStack3);
                        break;
                    }
                    case "WOLF_SPAWN_EGG":{
                        player.give(itemmanger.a_itemStack4);
                        break;
                    }
                    case "POTION":{
                        player.give(itemmanger.a_itemStack5);
                        break;
                    }
                }
            }
        }
        if(teammanager.isPlayerInTeam(player, team_2)){
            Set<String> tags = player.getScoreboardTags();
            for(String tag : tags){
                switch (tag){
                    case "SHIELD":{
                        player.give(itemmanger.a_itemStack1);
                        break;
                    }
                    case "STONE_SWORD":{
                        player.give(itemmanger.a_itemStack2);
                        break;
                    }
                    case "CROSSBOW":{
                        player.give(itemmanger.a_itemStack3);
                        break;
                    }
                    case "WOLF_SPAWN_EGG":{
                        player.give(itemmanger.a_itemStack4);
                        break;
                    }
                    case "POTION":{
                        player.give(itemmanger.a_itemStack5);
                        break;
                    }
                }
            }
        }
    }

}
