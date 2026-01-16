package com.mlc.mlcgames.bank.utils;

import com.mlc.mlcgames.Teammanager;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import static com.mlc.mlcgames.Mlcgames.*;
import static com.mlc.mlcgames.bank.utils.Bankgame.gameendcountdown;

public class Startgame {

        public static void startgame(){
            bankgame.isStart = true;
            switch (bankgame.gamemode){
                case thiefvsthief -> {bankgame.remainTime = fileConfiguration.getInt("bankgame.tvtgametime");}
                case thiefvspolice -> {bankgame.remainTime = fileConfiguration.getInt("bankgame.tvpgametime");}
                default -> {bankgame.remainTime = 10;}
            }


            //分别传送
            for(Player player : bankgame.players){
                player.getInventory().clear();
                player.updateInventory();
                if(Teammanager.getPlayerTeam(player).equals(Teammanager.bankgame_thiefteam)){
                    player.setRespawnLocation(bankgame.thiefteamLocation);
                    player.teleport(bankgame.thiefteamLocation);
                }else if(Teammanager.getPlayerTeam(player).equals(Teammanager.bankgame_policeteam)){
                    player.setRespawnLocation(bankgame.policeteamLocation);
                    player.teleport(bankgame.policeteamLocation);
                }else if(Teammanager.getPlayerTeam(player).equals(Teammanager.bankgame_spectateteam)) {
                    player.setRespawnLocation(bankgame.spectatelocation);
                    player.teleport(bankgame.spectatelocation);
                    player.setGameMode(GameMode.SPECTATOR);
                }
            }
            //给游戏物品,分游戏模式
            for(Player player : bankgame.players){
                switch (bankgame.gamemode){
                    case thiefvsthief ->             {
                        if(Teammanager.getPlayerTeam(player).equals(Teammanager.bankgame_thiefteam)){
                            Bankgameitemmanager.givethiefitem(player);
                        }else if(Teammanager.getPlayerTeam(player).equals(Teammanager.bankgame_policeteam)){
                            player.getInventory().addItem(Bankgameitemmanager.police_stonesword);
                            Bankgameitemmanager.givethiefitem(player);
                        }
                    }
                    case thiefvspolice -> {
                        if(Teammanager.getPlayerTeam(player).equals(Teammanager.bankgame_thiefteam)){
                            Bankgameitemmanager.givepoliceitem(player);
                        }else if(Teammanager.getPlayerTeam(player).equals(Teammanager.bankgame_policeteam)){
                            player.getInventory().addItem(Bankgameitemmanager.police_stonesword);
                            Bankgameitemmanager.givethiefitem(player);
                        }
                    }
                }
            }

            //清除残留射出的箭
            for(Entity entity : bankgame.bankgameLocation.getWorld().getEntities()){
                if(entity instanceof org.bukkit.entity.Arrow){
                    entity.remove();
                }
            }

            //开始倒计时
            gameendcountdown = new BukkitRunnable() {
                @Override
                public void run() {
                    if(bankgame.remainTime > 0){
                        bankgame.remainTime--;
                    }else {
                        //倒计时结束，结束游戏
                        Endgame.endgame();
                        cancel();
                    }
                }
            }.runTaskTimer(instance, 0, 20);
        }
}
