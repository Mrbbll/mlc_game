package com.mlc.mlcgames.bank.utils.start;

import com.mlc.mlcgames.Teammanager;
import com.mlc.mlcgames.bank.items.Bankgameitemmanager;
import com.mlc.mlcgames.bank.utils.Bankgame;
import com.mlc.mlcgames.bank.utils.Bankgamebossbar;
import com.mlc.mlcgames.bank.utils.end.Endgame;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;

import static com.mlc.mlcgames.Mlcgames.*;
import static com.mlc.mlcgames.bank.utils.Bankgame.gameendcountdown;
import static com.mlc.mlcgames.bank.utils.Bankgamebossbar.bankgamebossbar;

public class Startgame {

        public static void startgame(){
            bankgame.isStart = true;
            switch (bankgame.gamemode){
                case thiefvsthief -> {bankgame.remainTime = fileConfiguration.getInt("bankgame.tvtgametime");}
                case thiefvspolice -> {bankgame.remainTime = fileConfiguration.getInt("bankgame.tvpgametime");}
                default -> {bankgame.remainTime = 10;}
            }
            Bankgamebossbar.bossbarfulltime = bankgame.remainTime;
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

                //显示bossbar
                bankgamebossbar.addViewer(player);
            }
            //给游戏物品,分游戏模式
            for(Player player : bankgame.players){
                switch (bankgame.gamemode){
                    case thiefvsthief ->             {
                        if(Teammanager.getPlayerTeam(player).equals(Teammanager.bankgame_thiefteam)){
                            Bankgameitemmanager.givethiefitem(player);
                        }else if(Teammanager.getPlayerTeam(player).equals(Teammanager.bankgame_policeteam)){
                            Bankgameitemmanager.givethiefitem(player);
                        }
                    }
                    case thiefvspolice -> {
                        if(Teammanager.getPlayerTeam(player).equals(Teammanager.bankgame_thiefteam)){
                            Bankgameitemmanager.givepoliceitem(player);
                        }else if(Teammanager.getPlayerTeam(player).equals(Teammanager.bankgame_policeteam)){
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
                        //更新bossbar
                        Bankgamebossbar.updateBossbar();
                    }else {
                        //倒计时结束，结束游戏
                        Endgame.endgame();
                        cancel();
                    }
                }
            }.runTaskTimer(instance, 0, 20);

            //监听事件
            bankgame.Openlocklistener();


            //灯光破坏事件
            bankgame.Lightningbreaklistener();
            //灯光修复事件
            //开锁加速事件
            //道具事件
        }


}
