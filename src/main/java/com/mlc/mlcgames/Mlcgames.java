package com.mlc.mlcgames;

import net.kyori.adventure.text.minimessage.MiniMessage;
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
    public static Inventory ainv;
    public static Inventory binv;
    public static MiniMessage miniMessage;

    @Override
    public void onEnable() {
        //minimessage初始化
        miniMessage = MiniMessage.miniMessage();

        //初始化数值
        isstart = false;
        instance = this;
        gamemode = 0;
        fileConfiguration = this.getConfig();
        ainv = Bukkit.createInventory(null,6*9,miniMessage.deserialize("<!i><color:#38deff>a</color>"));
        binv = Bukkit.createInventory(null,6*9,miniMessage.deserialize("<!i><color:#38deff>b</color>"));
        //初始化选择界面
        new Initinv(ainv,1);
        new Initinv(binv,2);



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
