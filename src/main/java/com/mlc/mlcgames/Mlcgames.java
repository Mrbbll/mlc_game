package com.mlc.mlcgames;

import com.mlc.mlcgames.bank.utils.Bankgame;
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
