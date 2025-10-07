package com.mlc.mlcgames;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;
import org.checkerframework.checker.units.qual.C;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static com.mlc.mlcgames.Mlcgames.scoreboardManager;

public class Teammanager {
    public final Scoreboard scoreboard;
    public static Team team_1;
    public static Team team_2;
    public static Team team_3;
    public static Team team_4;
    public static Team team_5;
    public static Team team_6;
    public static Team team_7;
    public static Team team_8;

    public Teammanager() {
        this.scoreboard = scoreboardManager.getMainScoreboard();
        this.clearallTeam();
        team_1 = this.createTeam("AQUA", NamedTextColor.AQUA);
        team_1.displayName(Component.text("青队"));
        team_2 = this.createTeam("RED", NamedTextColor.RED);
        team_2.displayName(Component.text("红队"));
        team_3 = this.createTeam("GOLD", NamedTextColor.GOLD);
        team_3.displayName(Component.text("橙队"));
        team_4 = this.createTeam("YELLOW", NamedTextColor.YELLOW);
        team_4.displayName(Component.text("黄队"));
        team_5 = this.createTeam("GREEN", NamedTextColor.GREEN);
        team_5.displayName(Component.text("绿队"));
        team_6 = this.createTeam("LIGHT_PURPLE", NamedTextColor.LIGHT_PURPLE);
        team_6.displayName(Component.text("紫队"));
        team_7 = this.createTeam("BLUE", NamedTextColor.BLUE);
        team_7.displayName(Component.text("蓝队"));
        team_8 = this.createTeam("WHITE", NamedTextColor.WHITE);
        team_8.displayName(Component.text("白队"));



    }

    //清除队伍
    public void clearallTeam(){
        for (Team team : scoreboard.getTeams()) {
            team.unregister();
        }
    }


    //创新队伍
    public Team createTeam(String teamName, NamedTextColor color) {
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
        team.displayName(Component.text(teamName)); // 显示名称带颜色
        return team;
    }

    //加队伍
    public boolean addPlayerToTeam(Team team, Player player) {

        // 加入新队伍
        team.addEntry(player.getName());
        player.setScoreboard(scoreboard); // 确保玩家使用这个计分板
        return true;
    }

    //清空队伍
    public void cleanTeam(Team team) {
        for (String entry : team.getEntries()) team.removeEntry(entry);
    }

    //检查是否存在
    public boolean isPlayerInTeam(Player player, Team team) {
        return team.hasPlayer(player);
    }

    public Set<Player> getteamplayer(Team team){
        Set<Player> players = new HashSet<>();
        for (String entry : team.getEntries()) players.add(Bukkit.getPlayer(entry));
        return players;
    }

    public Team getPlayerTeam(Player player) {
        return scoreboard.getEntryTeam(player.getName());

    }

}
