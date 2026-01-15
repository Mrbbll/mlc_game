package com.mlc.mlcgames;

import com.mlc.mlcgames.bank.utils.Bankgame;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.ScoreboardManager;

import java.util.ArrayList;
import java.util.List;

public final class Mlcgames extends JavaPlugin {

    public static JavaPlugin instance;
    public static FileConfiguration fileConfiguration;

    public static MiniMessage miniMessage;
    public static ScoreboardManager scoreboardManager;


    public static Bankgame bankgame = new Bankgame();


    @Override
    public void onEnable() {


        //minimessage初始化
        miniMessage = MiniMessage.miniMessage();

        //初始化数值
        instance = this;
        fileConfiguration = this.getConfig();

        //管理器初始化
        scoreboardManager = Bukkit.getScoreboardManager();
        Teammanager.initTeammanager();
        Task.runtask();//注册监听，命令
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
