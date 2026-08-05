package com.mlc.mlcgames.sandgame.managers;

import com.mlc.mlcgames.Gamesidebar;
import com.mlc.mlcgames.Teammanager;
import com.mlc.mlcgames.sandgame.Sandgame;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import static com.mlc.mlcgames.Gamesidebar.setLine;
import static com.mlc.mlcgames.Gamesidebar.sidebarscoreboard;
import static com.mlc.mlcgames.Mlcgames.miniMessage;

public class Sidebarmanager {

    static Scoreboard scoreboard = sidebarscoreboard;
    static Objective objective = Gamesidebar.objective;


    public static void showsidebar(){
        setLine(scoreboard, 1,miniMessage.deserialize("<b><#02a82e>SAND GAME"));
        setLine(scoreboard, 2,miniMessage.deserialize(""));
        setLine(scoreboard, 3,miniMessage.deserialize(""));
        setLine(scoreboard, 4,miniMessage.deserialize(get_team_1_playerheadicon()));
        setLine(scoreboard, 5,miniMessage.deserialize(get_team_2_playerheadicon()));
        setLine(scoreboard, 6,miniMessage.deserialize("time："+Sandgame.countdown));
        setLine(scoreboard, 7,miniMessage.deserialize("<red>红队沙:<b>" + Sandgame.team_1_sand_count + " <blue>蓝队沙:<b>" + Sandgame.team_2_sand_count));
        setLine(scoreboard, 8,null);
        setLine(scoreboard, 9,null);
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
    }

    public static void updatesidebar(){
        showsidebar();
    }

    public static String get_team_1_playerheadicon(){
        StringBuilder sb = new StringBuilder();
        for(Player player: Teammanager.getteamplayer(Teammanager.sandgame_team_1)){
            String name = player.getName();
            if(player.getGameMode().equals(GameMode.SPECTATOR)){
                sb.append("<reset><red><head:");
                sb.append(name);
                sb.append(">");
            }else {
                sb.append("<reset><head:");
                sb.append(name);
                sb.append(">");
                break;
            }
        }
        return sb.toString();
    }

    public static String get_team_2_playerheadicon(){
        StringBuilder sb = new StringBuilder();
        for(Player player: Teammanager.getteamplayer(Teammanager.sandgame_team_2)){
            String name = player.getName();
            if(player.getGameMode().equals(GameMode.SPECTATOR)){
                sb.append("<reset><red><head:");
                sb.append(name);
                sb.append(">");
            }else {
                sb.append("<reset><head:");
                sb.append(name);
                sb.append(">");
                break;
            }
        }
        return sb.toString();
    }
}