package com.mlc.mlcgames;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.HashSet;
import java.util.Set;

import static com.mlc.mlcgames.Mlcgames.scoreboardManager;

public class Teammanager {
    public static Scoreboard scoreboard;

    public static Team bankgame_prepareteam;
    public static Team bankgame_policeteam;
    public static Team bankgame_thiefteam;
    public static Team bankgame_spectateteam;
    public static Team bankgame_powerlocteam;
    public static Team bankgame_goldlocteam;
    public static Team bankgame_outlocteam;

    public static Team zombieday_team;

    public static Team sandgame_prepareteam;
    public static Team sandgame_team_1;
    public static Team sandgame_team_2;

    public static Team dungeongame_team;
    public static Team dungeongame_prepareteam;

    public static Team team_1;
    public static Team team_2;
    public static Team team_3;
    public static Team team_4;
    public static Team team_5;
    public static Team team_6;
    public static Team team_7;
    public static Team team_8;
    public static Team team_9;

    public static void initTeammanager() {
        scoreboard = scoreboardManager.getMainScoreboard();
        scoreboard.clearSlot(DisplaySlot.SIDEBAR);
        clearallTeam();
        bankgame_prepareteam = createTeam("bankgame_prepareteam", NamedTextColor.YELLOW);
        bankgame_policeteam = createTeam("bankgame_pliceteam", NamedTextColor.AQUA);
        bankgame_thiefteam = createTeam("bankgame_thifeteam", NamedTextColor.RED);
        bankgame_spectateteam = createTeam("bankgame_spectateteam", NamedTextColor.GRAY);
        bankgame_powerlocteam = createTeam("bankgame_powerlocteam", NamedTextColor.YELLOW);
        bankgame_goldlocteam = createTeam("bankgame_goldlocteam", NamedTextColor.GOLD);
        bankgame_outlocteam = createTeam("bankgame_outlocteam", NamedTextColor.GREEN);

        bankgame_thiefteam.setOption(Team.Option.NAME_TAG_VISIBILITY,Team.OptionStatus.FOR_OTHER_TEAMS);
        bankgame_policeteam.setOption(Team.Option.NAME_TAG_VISIBILITY,Team.OptionStatus.FOR_OTHER_TEAMS);

        zombieday_team = createTeam("zombieday_team", NamedTextColor.GREEN);


        sandgame_prepareteam = createTeam("sandgame_prepareteam", NamedTextColor.YELLOW);
        sandgame_team_1 = createTeam("sandgame_team_1", NamedTextColor.RED);
        sandgame_team_2 = createTeam("sandgame_team_2", NamedTextColor.BLUE);

        dungeongame_team = createTeam("dungeongame_team", NamedTextColor.GREEN);
        dungeongame_prepareteam = createTeam("dungeongame_prepareteam", NamedTextColor.GRAY);

        team_1 = createTeam("AQUA", NamedTextColor.AQUA);
        team_1.displayName(Component.text("青队"));
        team_2 = createTeam("RED", NamedTextColor.RED);
        team_2.displayName(Component.text("红队"));
        team_3 = createTeam("GOLD", NamedTextColor.GOLD);
        team_3.displayName(Component.text("橙队"));
        team_4 = createTeam("YELLOW", NamedTextColor.YELLOW);
        team_4.displayName(Component.text("黄队"));
        team_5 = createTeam("GREEN", NamedTextColor.GREEN);
        team_5.displayName(Component.text("绿队"));
        team_6 = createTeam("LIGHT_PURPLE", NamedTextColor.LIGHT_PURPLE);
        team_6.displayName(Component.text("紫队"));
        team_7 = createTeam("BLUE", NamedTextColor.BLUE);
        team_7.displayName(Component.text("蓝队"));
        team_8 = createTeam("DARK_BLUE", NamedTextColor.DARK_BLUE);
        team_8.displayName(Component.text("深蓝队"));
        team_9 = createTeam("WHITE", NamedTextColor.WHITE);
        team_9.displayName(Component.text("白队"));
    }

    //清除队伍
    public static void clearallTeam(){
        for (Team team : scoreboard.getTeams()) {
            team.unregister();
        }
    }
    //移除玩家的队伍
    public static void removePlayerFromTeam(Player player) {
        Team team = getPlayerTeam(player);
        if (team != null) {
            team.removeEntry(player.getName());
        }
    }

    //创新队伍
    public static Team createTeam(String teamName, NamedTextColor color) {
        // 检查队伍是否已存在
        if (scoreboard.getTeam(teamName) != null) {
            return scoreboard.getTeam(teamName);
        }

        // 创建新队伍
        Team team = scoreboard.registerNewTeam(teamName);

        // 设置队伍属性
        team.color(color);
        team.setAllowFriendlyFire(false); // 禁止友方伤害
        team.setCanSeeFriendlyInvisibles(true); // 可以看到隐身的队友
        return team;
    }

    //加队伍
    public static boolean addPlayerToTeam(Team team, Player player) {
        // 加入新队伍
        team.addEntry(player.getName());
        player.setScoreboard(scoreboard); // 确保玩家使用这个计分板
        return true;
    }

    public static boolean addentityToTeam(Team team, Entity entity) {
        team.addEntity(entity);

        return true;
    }

    //清空队伍
    public static void cleanTeam(Team team) {
        for (String entry : team.getEntries()) team.removeEntry(entry);
    }

    //检查是否存在
    public static boolean isPlayerInTeam(Player player, Team team) {
        return team.hasPlayer(player);
    }

    //获取队伍玩家
    public static Set<Player> getteamplayer(Team team){
        Set<Player> players = new HashSet<>();
        for (String entry : team.getEntries()) {
            Player player = Bukkit.getPlayer(entry);
            if (player != null) {
                players.add(player);
            }
        }
        return players;
    }

    //获取玩家队伍
    public static Team getPlayerTeam(Player player) {
        return scoreboard.getEntryTeam(player.getName());

    }


}
