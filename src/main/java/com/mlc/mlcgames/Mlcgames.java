package com.mlc.mlcgames;

import com.mlc.mlcgames.bank.Bank_gamelistener;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.ScoreboardManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class Mlcgames extends JavaPlugin {

    public static JavaPlugin instance;
    public static FileConfiguration fileConfiguration;
    public static boolean isstart;

    public static List<Player> ingamepalyer = new ArrayList<>();

    public static Teammanager teammanager;
    public static Itemmanger itemmanger;
    public static MiniMessage miniMessage;
    public static Invmanger invmanger;
    public static Invclickhander invclickhander;

    public static ScoreboardManager scoreboardManager;



    @Override
    public void onEnable() {


        //minimessage初始化
        miniMessage = MiniMessage.miniMessage();

        //初始化数值
        isstart = false;
        instance = this;

        fileConfiguration = this.getConfig();

        //管理器初始化
        itemmanger = new Itemmanger();
        scoreboardManager = Bukkit.getScoreboardManager();
        teammanager = new Teammanager();
        invmanger = new Invmanger();
        invclickhander = new Invclickhander();
        new Gameinit();


        Bukkit.getPluginManager().registerEvents(new Bank_gamelistener(),this);
        Objects.requireNonNull(Bukkit.getPluginCommand("mlcgame_bank")).setExecutor((new mlcgame_bank()));
        getLogger().info("\n\nmlcgame插件加载成功\n\n");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("\n\nmlcgame插件卸载成功\n\n");
    }

}


//bankgame
//选队，中途不能加队
//prepare
//start，计时
//时间到或人全走了end
