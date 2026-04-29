package com.mlc.mlcgames;


import io.papermc.paper.scoreboard.numbers.NumberFormat;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.List;
import java.util.Objects;

import static com.mlc.mlcgames.Mlcgames.*;

public class Gamesidebar {
    public static Scoreboard sidebarscoreboard = scoreboardManager.getMainScoreboard();
    public static Objective objective;
    private static Team sidebar_team_1;
    private static Team sidebar_team_2;
    private static Team sidebar_team_3;
    private static Team sidebar_team_4;
    private static Team sidebar_team_5;
    private static Team sidebar_team_6;
    private static Team sidebar_team_7;
    private static Team sidebar_team_8;
    private static Team sidebar_team_9;
    private static List<String> lines = List.of("","","","","","","","","","");


    public static String line1 = "line1";
    public static String line2 = "line2";
    public static String line3 = "line3";
    public static String line4 = "line4";
    public static String line5 = "line5";
    public static String line6 = "line6";
    public static String line7 = "line7";
    public static String line8 = "line8";
    public static String line9 = "line9";


       public static void init(){

        if (sidebarscoreboard.getObjective("mlcgame") == null) {
            objective = sidebarscoreboard.registerNewObjective(
                    "mlcgame",
                    Criteria.DUMMY,
                    miniMessage.deserialize("<b><#ffde21>MLC Games"),
                    RenderType.INTEGER);
            objective.setDisplaySlot(DisplaySlot.SIDEBAR);
            objective.numberFormat(NumberFormat.blank());
        }else{
            objective = sidebarscoreboard.getObjective("mlcgame");
            if (objective != null) {
                objective.unregister();
                objective = sidebarscoreboard.registerNewObjective(
                        "mlcgame",
                        Criteria.DUMMY,
                        miniMessage.deserialize("<b><#ffde21>MLC Games"),
                        RenderType.INTEGER);
                objective.setDisplaySlot(DisplaySlot.SIDEBAR);
                objective.numberFormat(NumberFormat.blank());
            }
        }

        sidebar_team_1 = createTeam("team_1");
        initteam(objective, sidebar_team_1,1);
        sidebar_team_2 = createTeam("team_2");
        initteam(objective, sidebar_team_2,2);
        sidebar_team_3 = createTeam("team_3");
        initteam(objective, sidebar_team_3,3);
        sidebar_team_4 = createTeam("team_4");
        initteam(objective, sidebar_team_4,4);
        sidebar_team_5 = createTeam("team_5");
        initteam(objective, sidebar_team_5,5);
        sidebar_team_6 = createTeam("team_6");
        initteam(objective, sidebar_team_6,6);
        sidebar_team_7 = createTeam("team_7");
        initteam(objective, sidebar_team_7,7);
        sidebar_team_8 = createTeam("team_8");
        initteam(objective, sidebar_team_8,8);
        sidebar_team_9 = createTeam("team_9");
        initteam(objective, sidebar_team_9,9);
        objective.setAutoUpdateDisplay(true);
        readconfigline();
    }

    public static void showsidebar(Player player){
           readconfigline();
           player.setScoreboard(sidebarscoreboard);
    }


    public static Team createTeam(String string) {
//       if(sidebarscoreboard.getTeam(string) != null){
//           Objects.requireNonNull(sidebarscoreboard.getTeam(string)).unregister();
//       }
       return sidebarscoreboard.registerNewTeam(string);
    }


    public static void initteam(Objective objective, Team team,int line){
           team.displayName(null);
           team.removeEntry("§" + line);
           team.addEntry("§" + line);

           objective.getScore("§" + line).setScore( 10 - line );
    }


    public static void setLine(Scoreboard sidebarscoreboard, int line, Component component){
        Team team = sidebarscoreboard.getTeam("team_" + line);
        if (team != null) {
            team.prefix(component);
        }
    }

    public static void readconfigline(){
           List<?> line = fileConfiguration.getList("scoreboard", lines);

           for(int i = 1;i <= line.size();i++){
               String oneline = (String) line.get(i-1);
               setLine(sidebarscoreboard, i, miniMessage.deserialize(oneline));
           }
    }
}
