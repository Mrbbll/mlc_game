package com.mlc.mlcgames.zombieday.managers;

import com.mlc.mlcgames.Gamesidebar;
import com.mlc.mlcgames.Teammanager;
import com.mlc.mlcgames.bank.utils.Gamemode;
import com.mlc.mlcgames.zombieday.Zombiedaygame;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import static com.mlc.mlcgames.Gamesidebar.setLine;
import static com.mlc.mlcgames.Gamesidebar.sidebarscoreboard;
import static com.mlc.mlcgames.Mlcgames.bankgame;
import static com.mlc.mlcgames.Mlcgames.miniMessage;

public class scoreboard {
    static Scoreboard scoreboard = sidebarscoreboard;
    static Objective objective = Gamesidebar.objective;


    public static void showsidebar(Player player){
        setLine(scoreboard, 1,miniMessage.deserialize("<b><#02a82e>ZOMBIE DAY"));
        setLine(scoreboard, 2,miniMessage.deserialize("<b>难度："+ Zombiedaygame.difficulty.withcolor()));
        setLine(scoreboard, 3,miniMessage.deserialize("轮数："+ Zombiedaygame.turn));
        setLine(scoreboard, 4,miniMessage.deserialize(getplayerheadicon()));
        setLine(scoreboard, 5,miniMessage.deserialize("剩余僵尸数："+ Zombiedaygame.zombiecount));
        setLine(scoreboard, 6,miniMessage.deserialize("time："+ Zombiedaygame.countdown));
        setLine(scoreboard, 7,miniMessage.deserialize("使用下蹲加切换副手打开垃圾桶"));
        setLine(scoreboard, 8,null);
        setLine(scoreboard, 9,null);
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
    }

    public static void updatesidebar(Player player){
        setLine(scoreboard, 1,miniMessage.deserialize("<b><#02a82e>ZOMBIE DAY"));
        setLine(scoreboard, 2,miniMessage.deserialize("<b>难度："+ Zombiedaygame.difficulty.withcolor()));
        setLine(scoreboard, 3,miniMessage.deserialize("轮数："+ Zombiedaygame.turn));
        setLine(scoreboard, 4,miniMessage.deserialize(getplayerheadicon()));
        setLine(scoreboard, 5,miniMessage.deserialize("剩余僵尸数："+ Zombiedaygame.zombiecount));
        setLine(scoreboard, 6,miniMessage.deserialize("time："+ Zombiedaygame.countdown));
        setLine(scoreboard, 7,miniMessage.deserialize("使用下蹲加切换副手打开垃圾桶"));
        setLine(scoreboard, 8,null);
        setLine(scoreboard, 9,null);
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
    }

    public static String getplayerheadicon(){
        StringBuilder sb = new StringBuilder();
        for(Player player: Teammanager.getteamplayer(Teammanager.zombieday_team)){
            String name = player.getName();
            switch (player.getGameMode()){
                case ADVENTURE:
                    sb.append("<reset><head:");
                    sb.append(name);
                    sb.append(">");
                    break;
                case SPECTATOR:
                    sb.append("<reset><red><head:");
                    sb.append(name);
                    sb.append(">");
                    break;
            }
        }
        return sb.toString();
    }
}
