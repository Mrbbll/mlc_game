package com.mlc.mlcgames;

import com.mlc.mlcgames.bank.commmand.bankgameend;
import com.mlc.mlcgames.bank.commmand.bankgameprepare;
import com.mlc.mlcgames.bank.commmand.bankgamereload;
import com.mlc.mlcgames.bank.listener.Bankgamelistener;
import com.mlc.mlcgames.bank.utils.Bankgamebossbar;
import com.mlc.mlcgames.bank.utils.Bankgamedialog;
import com.mlc.mlcgames.bank.utils.Bankgameinit;
import com.mlc.mlcgames.bank.utils.Bankgameitemmanager;
import com.mlc.mlcgames.bank.menus.bankmenus;
import com.mlc.mlcgames.hook.placeholderapi;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.registry.RegistryBuilderFactory;
import io.papermc.paper.registry.data.dialog.DialogInstancesProvider;
import org.bukkit.Bukkit;

import java.util.Objects;

import static com.mlc.mlcgames.Mlcgames.instance;

public class Task {
    public static void runtask(){

        //事件注册
        Bukkit.getPluginManager().registerEvents(new Bankgamelistener() ,instance);

        //命令注册
        Objects.requireNonNull(instance.getCommand("bankgameprepare")).setExecutor(new bankgameprepare());
        Objects.requireNonNull(instance.getCommand("bankgamereload")).setExecutor(new bankgamereload());
        Objects.requireNonNull(instance.getCommand("bankgameend")).setExecutor(new bankgameend());

        //placeholderapi注册
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new placeholderapi().register();
            System.out.println("\n\nmlcdomain placeholder registered\n\n");
        }

        //物品初始化
        Bankgameitemmanager.inititem();
        //bossbar初始化
        Bankgamebossbar.initBankgamebossbar();

        //菜单初始化
        bankmenus.init();
        //对话框初始化
        Bankgamedialog.create();

        //游戏初始化
        Bankgameinit.init();
    }
}
