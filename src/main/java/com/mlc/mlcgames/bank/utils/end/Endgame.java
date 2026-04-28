package com.mlc.mlcgames.bank.utils.end;

import com.mlc.mlcgames.Teammanager;
import com.mlc.mlcgames.bank.utils.Bankgame;
import com.mlc.mlcgames.bank.utils.Gamemode;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.scoreboard.Team;

import static com.mlc.mlcgames.Mlcgames.*;
import static com.mlc.mlcgames.bank.utils.Bankgame.gameendcountdown;
import static com.mlc.mlcgames.bank.utils.Bankgamebossbar.bankgamebossbar;

public class Endgame {
    public static void endgame(){

        //清除展示实体,实体，箭头,狗,潜匿贝
        bankgame.bankgameLocation.getWorld().getEntities().forEach(entity -> {
            if(entity instanceof ItemDisplay){
                Block block = entity.getLocation().getBlock();
                if(block.getType() == Material.BARREL){
                    entity.remove();
                }
            }else if(entity instanceof Item||entity instanceof Arrow||entity instanceof Wolf||entity instanceof Shulker){
                entity.remove();
            }

        });


        bankgame.isStart = false;
        bankgame.islockbreak = false;
        bankgame.islightbreak1 = false;
        bankgame.islightbreak2 = false;
        bankgame.ispowerbreak = false;
        bankgame.gameprepared = false;
        if(bankgame.breaklockevent!=null){
            bankgame.breaklockevent.cancel();
            bankgame.breaklockevent = null;
        }
        if(bankgame.lightbreakevent!=null){
            bankgame.lightbreakevent.cancel();
            bankgame.lightbreakevent = null;
        }
        if(bankgame.lightfixevent!=null){
            bankgame.lightfixevent.cancel();
            bankgame.lightfixevent = null;
        }

        if(gameendcountdown!=null){
            gameendcountdown.cancel();
            gameendcountdown = null;
        }
        if(bankgame.remainTime<=0) {instance.getServer().broadcast(miniMessage.deserialize("\n\n<b><red>时间结束⌚"));}
        bankgame.playerJobs.clear();
        for(Player player : bankgame.players){
            player.setRespawnLocation(bankgame.bankgameLocation,true);
            player.teleport(bankgame.bankgameLocation);
            player.setGameMode(GameMode.ADVENTURE);
            player.getInventory().clear();
            player.updateInventory();
            player.clearActivePotionEffects();
        }
        Teammanager.cleanTeam(Teammanager.bankgame_spectateteam);
        Teammanager.cleanTeam(Teammanager.bankgame_policeteam);
        Teammanager.cleanTeam(Teammanager.bankgame_thiefteam);

        //保证时间正常结束时小偷没嬴警察一定嬴
        if(bankgame.gamemode== Gamemode.thiefvspolice&&bankgame.remainTime<=0){
            bankgame.policescore+=9000;
        }
        String color = "white";
        if(bankgame.policescore > bankgame.thiefscore){
            bankgame.winnerteam = "蓝队";
            color = "#33beff";
        }else if(bankgame.policescore < bankgame.thiefscore){
            bankgame.winnerteam = "红队";
            color = "#ff000d";
        }else{
            bankgame.winnerteam = " -平- ";
        }
        instance.getServer().broadcast(miniMessage.deserialize("<bold><" + color + ">获胜队伍是" + bankgame.winnerteam) );
        bankgame.policescore = 0;
        bankgame.thiefscore = 0;
        //移除bossbar
        for(Player player : bankgame.players){
            bankgamebossbar.removeViewer(player);
        }
        //清空bankgameplayer
        bankgame.players.clear();

    }
}
