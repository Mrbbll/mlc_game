package com.mlc.mlcgames.bank.utils;

import com.mlc.mlcgames.Teammanager;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.mlc.mlcgames.Mlcgames.bankgame;
import static com.mlc.mlcgames.Mlcgames.miniMessage;


public class Bankgamebossbar {
    public static BossBar bankgamebossbar;
    public static String bossbarstring = "";
    public static int bossbarfulltime = 10;
    public static Component bossbarcomponent = Component.text("");
    public static void initBankgamebossbar() {
        bankgamebossbar = BossBar.bossBar(bossbarcomponent, 0, BossBar.Color.BLUE, BossBar.Overlay.PROGRESS);
    }
    public static void setBossbarstring(String string){
        bossbarstring = string;
        bossbarcomponent = Component.text(bossbarstring, NamedTextColor.WHITE);
        bankgamebossbar.name(bossbarcomponent);

    }
    public static void updateBossbar(){
        bankgamebossbar.progress((bankgame.remainTime /(float) bossbarfulltime));
    }
}
