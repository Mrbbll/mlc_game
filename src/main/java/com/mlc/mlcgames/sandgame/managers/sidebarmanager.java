package com.mlc.mlcgames.sandgame.managers;

import com.mlc.mlcgames.Gamesidebar;
import com.mlc.mlcgames.Teammanager;
import com.mlc.mlcgames.sandgame.Sandgame;
import com.mlc.mlcgames.zombieday.Zombiedaygame;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import static com.mlc.mlcgames.Gamesidebar.setLine;
import static com.mlc.mlcgames.Gamesidebar.sidebarscoreboard;
import static com.mlc.mlcgames.Mlcgames.miniMessage;

public class sidebarmanager {

    static Scoreboard scoreboard = sidebarscoreboard;
    static Objective objective = Gamesidebar.objective;


    public static void showsidebar(Player player){
        setLine(scoreboard, 1,miniMessage.deserialize("<b><#02a82e>SAND GAME"));
        setLine(scoreboard, 2,miniMessage.deserialize(""));
        setLine(scoreboard, 3,miniMessage.deserialize(""));
        setLine(scoreboard, 4,miniMessage.deserialize(get_team_1_playerheadicon()));
        setLine(scoreboard, 5,miniMessage.deserialize(get_team_2_playerheadicon()));
        setLine(scoreboard, 6,miniMessage.deserialize("time："+Sandgame.countdown));
        setLine(scoreboard, 7,miniMessage.deserialize("money:"+ Sandgame.player_money.getOrDefault(player,0)));
        setLine(scoreboard, 8,null);
        setLine(scoreboard, 9,null);
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
    }

    public static void updatesidebar(Player player){
        setLine(scoreboard, 1,miniMessage.deserialize("<b><#02a82e>SAND GAME"));
        setLine(scoreboard, 2,miniMessage.deserialize(""));
        setLine(scoreboard, 3,miniMessage.deserialize(""));
        setLine(scoreboard, 4,miniMessage.deserialize(get_team_1_playerheadicon()));
        setLine(scoreboard, 5,miniMessage.deserialize(get_team_2_playerheadicon()));
        setLine(scoreboard, 6,miniMessage.deserialize("time："+Sandgame.countdown));
        setLine(scoreboard, 7,miniMessage.deserialize("money:"+ Sandgame.player_money.getOrDefault(player,0)));
        setLine(scoreboard, 8,null);
        setLine(scoreboard, 9,null);
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
    }

    public static String get_team_1_playerheadicon(){
        StringBuilder sb = new StringBuilder();
        for(Player player: Teammanager.getteamplayer(Teammanager.sandgame_team_1)){
            String name = player.getName();
                sb.append("<reset><head:");
                sb.append(name);
                sb.append(">");
                break;
        }
        return sb.toString();
    }

    public static String get_team_2_playerheadicon(){
        StringBuilder sb = new StringBuilder();
        for(Player player: Teammanager.getteamplayer(Teammanager.sandgame_team_2)){
            String name = player.getName();
            sb.append("<reset><head:");
            sb.append(name);
            sb.append(">");
            break;
        }
        return sb.toString();
    }
}
