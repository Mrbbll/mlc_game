package com.mlc.mlcgames;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import static com.mlc.mlcgames.Mlcgames.scoreboardManager;

public class Teammanager {
    public final Scoreboard scoreboard;

    public Teammanager() {
        this.scoreboard = scoreboardManager.getMainScoreboard();
    }

    //创新队伍
    public Team createTeam(String teamName, NamedTextColor color) {
        // 检查队伍是否已存在
        if (scoreboard.getTeam(teamName) != null) {
            return null;
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
        return team.hasEntry(player.getName());
    }

}
