package com.mlc.mlcgames.zombieday.managers;

import com.mlc.mlcgames.Gamesidebar;
import com.mlc.mlcgames.bank.utils.Gamemode;
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
        setLine(scoreboard, 2,miniMessage.deserialize(""));
        setLine(scoreboard, 3,null);
        setLine(scoreboard, 4,null);
        setLine(scoreboard, 5,null);
        setLine(scoreboard, 6,null);
        setLine(scoreboard, 7,null);
        setLine(scoreboard, 8,null);
        setLine(scoreboard, 9,null);
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
    }

    public static void uopdatesidebar(Player player){

    }
}
