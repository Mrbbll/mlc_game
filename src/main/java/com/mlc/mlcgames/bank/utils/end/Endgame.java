package com.mlc.mlcgames.bank.utils.end;

import com.mlc.mlcgames.Teammanager;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

import static com.mlc.mlcgames.Mlcgames.bankgame;
import static com.mlc.mlcgames.Mlcgames.instance;
import static com.mlc.mlcgames.bank.utils.Bankgame.gameendcountdown;
import static com.mlc.mlcgames.bank.utils.Bankgamebossbar.bankgamebossbar;

public class Endgame {
    public static void endgame(){
        bankgame.isStart = false;
        bankgame.gameprepared = false;
        if(gameendcountdown!=null){
            gameendcountdown.cancel();
        }
        instance.getServer().broadcast(net.kyori.adventure.text.Component.text("\n\n\n时间结束", NamedTextColor.RED));
        bankgame.playerJobs.clear();
        for(Player player : bankgame.players){
            player.setRespawnLocation(bankgame.lobbyLocation);
            player.teleport(bankgame.lobbyLocation);
            player.setGameMode(GameMode.ADVENTURE);
            player.getInventory().clear();
            player.updateInventory();
        }
        Teammanager.cleanTeam(Teammanager.bankgame_spectateteam);
        Teammanager.cleanTeam(Teammanager.bankgame_policeteam);
        Teammanager.cleanTeam(Teammanager.bankgame_thiefteam);
        if(bankgame.policescore > bankgame.thiefscore){
            bankgame.winnerteam = "蓝队";
        }else if(bankgame.policescore < bankgame.thiefscore){
            bankgame.winnerteam = "红队";
        }else{
            bankgame.winnerteam = " -平- ";
        }
        instance.getServer().broadcast(net.kyori.adventure.text.Component.text("\n\n\n获胜队伍是" + bankgame.winnerteam, NamedTextColor.GREEN));
        bankgame.policescore = 0;
        bankgame.thiefscore = 0;
        //移除bossbar
        for(Player player : bankgame.players){
            bankgamebossbar.removeViewer(player);
        }
    }

    public static void endgame(Team team){
        bankgame.isStart = false;
        bankgame.gameprepared = false;
        if(gameendcountdown!=null){
            gameendcountdown.cancel();
        }
        instance.getServer().broadcast(net.kyori.adventure.text.Component.text("\n\n\n获胜队伍是" + bankgame.winnerteam, NamedTextColor.GREEN));
        bankgame.playerJobs.clear();
        for(Player player : bankgame.players){
            player.teleport(bankgame.lobbyLocation);
            player.setGameMode(GameMode.ADVENTURE);
            player.getInventory().clear();

        }
        Teammanager.cleanTeam(Teammanager.bankgame_spectateteam);
        Teammanager.cleanTeam(Teammanager.bankgame_policeteam);
        Teammanager.cleanTeam(Teammanager.bankgame_thiefteam);
        bankgame.policescore = 0;
        bankgame.thiefscore = 0;
    }
}
