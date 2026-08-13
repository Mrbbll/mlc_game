package com.mlc.mlcgames;

import com.github.retrooper.packetevents.PacketEvents;
import com.mlc.mlcgames.bank.utils.Bankgame;
import com.mlc.mlcgames.sandgame.listener.Packetlistener;
import com.mlc.mlcgames.utils.file.ConfigManager;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import io.papermc.paper.registry.event.RegistryEvents;
import io.papermc.paper.registry.keys.DialogKeys;
import io.papermc.paper.registry.keys.EnchantmentKeys;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Server;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.ScoreboardManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public final class Mlcgames extends JavaPlugin {

    public static JavaPlugin instance;
    public static Server server;
    public static FileConfiguration fileConfiguration;
    public static FileConfiguration zombiedayConfiguration;
    public static FileConfiguration sandgameConfiguration;
    public static FileConfiguration dungeonConfiguration;
    public static ConfigManager configManager;

    public static MiniMessage miniMessage;
    public static ScoreboardManager scoreboardManager;


    public static Bankgame bankgame = new Bankgame();
    @Override
    public void onLoad(){
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().load();
    }


    @Override
    public void onEnable() {

        PacketEvents.getAPI().init();
        PacketEvents.getAPI().getEventManager().registerListener(new Packetlistener());
        //minimessage初始化
        miniMessage = MiniMessage.miniMessage();
        //配置文件初始化
        configManager = new ConfigManager(this);
        saveResource("zombieday.yml", false);


        //初始化数值
        instance = this;
        server = Bukkit.getServer();
        fileConfiguration = this.getConfig();

        zombiedayConfiguration = configManager.loadConfig("zombieday.yml");
        sandgameConfiguration = configManager.loadConfig("sandgame.yml");
        dungeonConfiguration = configManager.loadConfig("dungeongame.yml");

        //管理器初始化
        scoreboardManager = Bukkit.getScoreboardManager();
        Teammanager.initTeammanager();
        Gamesidebar.init();
        try {
            Task.runtask();//注册监听，命令
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        getLogger().info("\n\nmlcgame插件加载成功\n\n");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        PacketEvents.getAPI().terminate();
        getLogger().info("\n\nmlcgame插件卸载成功\n\n");
    }

//    @Override
//    public void bootstrap(BootstrapContext context) {
//        context.getLifecycleManager().registerEventHandler(
//            RegistryEvents.DIALOG.entryAdd()
//                .newHandler(event -> {
//                    event.builder()
//                            .type(DialogType.notice())
//                            .base(DialogBase.builder(Component.text("自定义快速操作标题"))
//                                    .build());
//                    }).filter(DialogKeys.create(NamespacedKey.minecraft("quick_actions")))
//        );
//    }
}


//bankgame
//选队，中途不能加队
//prepare
//start，计时
//时间到或人全走了end
