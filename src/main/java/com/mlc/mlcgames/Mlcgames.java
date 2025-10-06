package com.mlc.mlcgames;

import com.mlc.mlcgames.bank.Gamelistener;
import com.mlc.mlcgames.bank.Initinv;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class Mlcgames extends JavaPlugin {

    public static JavaPlugin instance;
    public static FileConfiguration fileConfiguration;
    public static boolean isstart;
    public static int gamemode;
    public static List<Player> ingamepalyer = new ArrayList<>();
    public static List<World> worlds;

    public static Teammanager teammanager;
    public static Itemmanger itemmanger;
    public static Inventory ainv;
    public static Inventory binv;
    public static MiniMessage miniMessage;


    public static ScoreboardManager scoreboardManager;

    @Override
    public void onEnable() {

        worlds = Bukkit.getWorlds();
        //minimessage初始化
        miniMessage = MiniMessage.miniMessage();

        //初始化数值
        isstart = false;
        instance = this;
        gamemode = 0;
        fileConfiguration = this.getConfig();

        itemmanger = new Itemmanger();
        ainv = Bukkit.createInventory(null,6*9,miniMessage.deserialize("<!i><color:#38deff>a</color>"));
        binv = Bukkit.createInventory(null,6*9,miniMessage.deserialize("<!i><color:#38deff>b</color>"));

        scoreboardManager = Bukkit.getScoreboardManager();
        teammanager = new Teammanager();



        //初始化选择界面
        new Initinv(ainv,1);
        new Initinv(binv,2);


        Bukkit.getPluginManager().registerEvents(new Gamelistener(),this);
        Objects.requireNonNull(Bukkit.getPluginCommand("mlcgame_bank")).setExecutor((new mlcgame_bank()));
        getLogger().info("\n\nmlcgame插件加载成功\n\n");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("\n\nmlcgame插件卸载成功\n\n");
    }

}
