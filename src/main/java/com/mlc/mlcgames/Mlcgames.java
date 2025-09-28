package com.mlc.mlcgames;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

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

    public static Team ateam;
    public static Team bteam;
    public static Teammanager teammanager = new Teammanager();
    public static Inventory ainv;
    public static Inventory binv;
    public static MiniMessage miniMessage;
    public static ItemStack mlcmenu;
    public static ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();

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
        ateam = teammanager.createTeam("a", NamedTextColor.AQUA);
        bteam = teammanager.createTeam("a", NamedTextColor.RED);


        mlcmenu= ItemStack.of(Material.ECHO_SHARD);
        ItemMeta itemMeta = mlcmenu.getItemMeta();
        itemMeta.setItemModel(NamespacedKey.fromString("mlcgames:mlcmenu"));
        itemMeta.itemName(Component.text("菜单", TextColor.fromHexString("#eea468")));

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
