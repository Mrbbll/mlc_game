package com.mlc.mlcgames.bank;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;
import java.util.Set;

import static com.mlc.mlcgames.Gameinit.*;
import static com.mlc.mlcgames.Mlcgames.*;
import static com.mlc.mlcgames.Teammanager.team_1;
import static com.mlc.mlcgames.Teammanager.team_2;

public class Bank_gamestart {
    public final Bank_bossbar bankBossbar = new Bank_bossbar();
    int[] time = {0};
    int timemax = 10;

    final int[] i = {0};

    public Bank_gamestart(){

        Bukkit.broadcast(Component.text("游戏开始"));
        switch (bank_gamemode){
            //battlebox模式
            case 0:{
                isstart = true;
                Bukkit.broadcast(Component.text("battlebox model"));
                //传送
                for(Player player:ingamepalyer){
                    player.teleport(bank_lobby);
                }

                //给东西
                for(Player player:ingamepalyer){
                    giveitem(player);
                }

                //出字


                new BukkitRunnable() {
                    @Override
                    public void run() {
                        for (Player player1:ingamepalyer){
                            player1.showTitle(Title.title(Component.text(5-i[0]),Component.text(""), Title.Times.times(Duration.ZERO,Duration.ofSeconds(1),Duration.ZERO)));
                            if(i[0] > 4 ) this.cancel();
                            i[0]++;
                        }
                    }
                }.runTaskTimer(instance,20,20);
                //开门
                for(Player player:ingamepalyer){
                    player.showTitle(Title.title(Component.text("start"),Component.text("")));
                }
                //计时和bossbar

                bankBossbar.init();
                for(Player player :ingamepalyer){
                    player.showBossBar(bankBossbar.bossBar);
                }

                //每次重新设置值
                time[0] = 10;


                //倒计时结束
                new BukkitRunnable(){
                    @Override
                    public void run() {
                        if(time[0] <= 0){
                            new Bank_gameend().endgame(bankBossbar.bossBar);
                            this.cancel();
                        }
                        bankBossbar.progress = (float) time[0]--/ timemax;
                        bankBossbar.bossBar.progress(bankBossbar.progress);
                    }
                }.runTaskTimer(instance,0,20);


                //抢东西结束
                new BukkitRunnable(){
                    @Override
                    public void run() {
                        if(time[0] <= 0){
                            new Bank_gameend().endgame(bankBossbar.bossBar);
                            this.cancel();
                        }
                        bankBossbar.progress = (float) time[0]--/ timemax;
                        bankBossbar.bossBar.progress(bankBossbar.progress);
                    }
                }.runTaskTimer(instance,0,20);
                break;


            }

            //小偷模式
            case 1:{
                Bukkit.broadcast(Component.text("thief model"));
                break;
            }
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
