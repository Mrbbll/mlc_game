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
    public Bank_gamestart(){
        Bukkit.broadcast(Component.text("游戏开始"));

        switch (bank_gamemode){
            //battlebox模式

            case 0:{
                isstart = true;
                //传送
                for(Player player:ingamepalyer){
                    player.teleport(bank_lobby);
                }

                //给东西
                for(Player player:ingamepalyer){
                    giveitem(player);
                }

                //出字

                final int[] i = {0};
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


                //计时

                //

            }

            //小偷模式
            case 1:{

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
                    }
                    case "STONE_SWORD":{
                        player.give(itemmanger.a_itemStack1);
                    }
                    case "CROSSBOW":{
                        player.give(itemmanger.a_itemStack1);
                    }
                    case "WOLF_SPAWN_EGG":{
                        player.give(itemmanger.a_itemStack1);
                    }
                    case "POTION":{
                        player.give(itemmanger.a_itemStack1);
                    }
                }
            };
        }
        if(teammanager.isPlayerInTeam(player, team_2)){
            Set<String> tags = player.getScoreboardTags();
            for(String tag : tags){
                switch (tag){
                    case "SHIELD":{
                        player.give(itemmanger.a_itemStack1);
                    }
                    case "STONE_SWORD":{
                        player.give(itemmanger.a_itemStack1);
                    }
                    case "CROSSBOW":{
                        player.give(itemmanger.a_itemStack1);
                    }
                    case "WOLF_SPAWN_EGG":{
                        player.give(itemmanger.a_itemStack1);
                    }
                    case "POTION":{
                        player.give(itemmanger.a_itemStack1);
                    }
                }
            };
        }
    }

}
