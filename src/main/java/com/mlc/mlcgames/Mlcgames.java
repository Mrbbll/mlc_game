package com.mlc.mlcgames;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class Mlcgames extends JavaPlugin {

    public static JavaPlugin instance;
    public static FileConfiguration fileConfiguration;
    public static boolean isstart;
    public static int gamemode;
    public static List<Player> ingamepalyer = new ArrayList<>();
    public static List<Player> ateam = new ArrayList<>();
    public static List<Player> bteam = new ArrayList<>();
    public static Inventory ainv = Bukkit.createInventory(null,6*9);
    public static Inventory binv = Bukkit.createInventory(null,6*9);
    @Override
    public void onEnable() {
        // Plugin startup logic
        isstart = false;
        instance = this;
        gamemode = 0;
        fileConfiguration = this.getConfig();

        Bukkit.getPluginManager().registerEvents(new Gamelistener(),this);
        Objects.requireNonNull(Bukkit.getPluginCommand("mlcgame")).setExecutor((new mlcgame()));
        getLogger().info("\n\nmlcgame插件加载成功\n\n");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("\n\nmlcgame插件卸载成功\n\n");
    }



}
