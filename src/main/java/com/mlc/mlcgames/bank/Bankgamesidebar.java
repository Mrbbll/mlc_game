package com.mlc.mlcgames.bank;

import com.mlc.mlcgames.Gamesidebar;
import com.mlc.mlcgames.Teammanager;
import com.mlc.mlcgames.bank.utils.Gamemode;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.*;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import static com.mlc.mlcgames.Gamesidebar.*;
import static com.mlc.mlcgames.Mlcgames.*;

public class Bankgamesidebar {
    static Scoreboard scoreboard = sidebarscoreboard;
    static Objective objective = Gamesidebar.objective;


    public static void showsidebar(Player player){


        setLine(scoreboard, 1,miniMessage.deserialize("<b>模式："+ bankgame.gamemode.getChineseName()));
        if(bankgame.gamemode == Gamemode.thiefvsthief){
            setLine(scoreboard, 2,miniMessage.deserialize("<b><red>红队得分: "+ bankgame.thiefscore + "</red> <white>|</white> <blue>蓝队得分: "+bankgame.policescore));
        }
        else{
            setLine(scoreboard, 2,miniMessage.deserialize("<b><red>小偷得分: </b><#ff9500>" + bankgame.policescore + "<white>/</white><#ff9500>"+bankgame.requirescore));
        }
        setLine(scoreboard, 3,null);
        setLine(scoreboard, 4,null);
        setLine(scoreboard, 5,null);
        setLine(scoreboard, 6,null);
        setLine(scoreboard, 7,null);
        setLine(scoreboard, 8,null);
        setLine(scoreboard, 9,null);
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
    }

    public static void updatesidebar(){
        if(bankgame.gamemode == Gamemode.thiefvsthief){
            setLine(scoreboard, 2,miniMessage.deserialize("<b><red>红队得分 "+ bankgame.thiefscore + "</red> <white>|</white> <blue>蓝队得分 "+bankgame.policescore));
        }
        else{
            setLine(scoreboard, 2,miniMessage.deserialize("<#ff672b>小偷得分 <b><#ff9500>" + bankgame.policescore + "<white>/</white><#ff9500>"+bankgame.requirescore));
        }
        setLine(scoreboard, 3,miniMessage.deserialize("<#ff672b>剩余时间：<#ff9500>"+ bankgame.remainTime+"s"));
        setLine(scoreboard, 4,miniMessage.deserialize("红队：" + getredteamplayer()));
        setLine(scoreboard, 5,miniMessage.deserialize("蓝队：" + getblueteamplayer()));
        setLine(scoreboard, 6,null);
        setLine(scoreboard, 7,null);
        setLine(scoreboard, 8,null);
        setLine(scoreboard, 9,null);
    }

    private static String getredteamplayer() {
        Set<Player> redteam = Teammanager.getteamplayer(Teammanager.bankgame_thiefteam);
        StringBuilder redteamplayer = new StringBuilder();
        for(Player player : redteam){
            if(player.hasPotionEffect(PotionEffectType.LUCK)){
                redteamplayer.append("<#29292e><head:").append(player.getName()).append(">");
                continue;
            }
            redteamplayer.append ("<reset><head:").append(player.getName()).append(">");
        }
        return redteamplayer.toString();

    }
    private static String getblueteamplayer() {
        Set<Player> blue = Teammanager.getteamplayer(Teammanager.bankgame_policeteam);
        StringBuilder blueteamplayer = new StringBuilder();
        for(Player player : blue){
            if(player.hasPotionEffect(PotionEffectType.LUCK)){
                blueteamplayer.append("<#29292e><head:").append(player.getName()).append(">");
                continue;
            }
            blueteamplayer.append("<reset><head:").append(player.getName()).append(">");
        }
        return blueteamplayer.toString();
    }
}
