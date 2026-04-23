package com.mlc.mlcgames.bank.utils;

import com.mlc.mlcgames.Teammanager;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentBuilder;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;

import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.mlc.mlcgames.Mlcgames.bankgame;
import static com.mlc.mlcgames.Mlcgames.miniMessage;


public class Bankgamebossbar {
    public static BossBar bankgamebossbar;
    public static int bossbarfulltime = 10;
    public static Component bossbarcomponent = Component.text("");
    public static void initBankgamebossbar() {
        bankgamebossbar = BossBar.bossBar(bossbarcomponent, 0, BossBar.Color.YELLOW, BossBar.Overlay.PROGRESS);
    }
    //🏹 🪓 🗡 🛡 ☀
    public static void setBossbarstring(){
        Component s = Component.text("");
        s = s.append(Component.text().append(getteamicon(Teammanager.bankgame_thiefteam)).color(NamedTextColor.RED));
        s = s.append(Component.text(bankgame.thiefscore + " : " + bankgame.policescore).color(NamedTextColor.WHITE));
        s = s.append(Component.text().append(getteamicon(Teammanager.bankgame_policeteam)).color(NamedTextColor.BLUE));


        bankgamebossbar.name(s);

    }
    public static void updateBossbar(){
        setBossbarstring();
        bankgamebossbar.progress((bankgame.remainTime /(float) bossbarfulltime));
    }

    public static Component getteamicon(Team t) {
        Component c = Component.text("");
        for (String p : t.getEntries()) {
            Player player = Bukkit.getPlayer(p);
            if (player != null) {
                switch (bankgame.playerJobs.get(player)){
                    case Jobs.sword -> c = c.append(Component.text("\uD83D\uDDE1"));
                    case Jobs.bow -> c = c.append(Component.text("\uD83C\uDFF9"));
                    case Jobs.crossbow -> c = c.append(Component.text("☀"));
                    case Jobs.wolfspawnegg -> c = c.append(Component.text("\uD83D\uDEE1"));
                    case Jobs.woodenaxe -> c = c.append(Component.text("\uD83E\uDE93"));
                }
            }
        }
        return c;
    }
}
