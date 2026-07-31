package com.mlc.mlcgames;

import com.mlc.mlcgames.Listener.gamelistener;
import com.mlc.mlcgames.bank.commmand.bankgameend;
import com.mlc.mlcgames.bank.commmand.bankgameprepare;
import com.mlc.mlcgames.bank.commmand.bankgamereload;
import com.mlc.mlcgames.bank.items.Bankgameloottable;
import com.mlc.mlcgames.bank.listener.Bankgamelistener;
import com.mlc.mlcgames.bank.utils.Bankgamebossbar;
import com.mlc.mlcgames.bank.utils.Bankgameinit;
import com.mlc.mlcgames.bank.items.Bankgameitemmanager;
import com.mlc.mlcgames.bank.menus.bankmenus;
import com.mlc.mlcgames.commands.reload;
import com.mlc.mlcgames.sandgame.gamephase.sandgameinit;
import com.mlc.mlcgames.utils.item.GunHitListener;
import com.mlc.mlcgames.zombieday.commands.Zombieday;
import com.mlc.mlcgames.zombieday.gamephase.Init;
import com.mlc.mlcgames.zombieday.listener.EntityListener;
import com.mlc.mlcgames.zombieday.listener.Gunuse;
import com.mlc.mlcgames.zombieday.listener.throwitem;
import org.bukkit.Bukkit;

import java.io.IOException;
import java.util.Objects;

import static com.mlc.mlcgames.Mlcgames.instance;

public class Task {
    public static void runtask() throws IOException {

        //事件注册
        Bukkit.getPluginManager().registerEvents(new Bankgamelistener() ,instance);
        Bukkit.getPluginManager().registerEvents(new gamelistener(),instance);
        Bukkit.getPluginManager().registerEvents(new Gunuse(),instance);
        Bukkit.getPluginManager().registerEvents(new throwitem(),instance);
        Bukkit.getPluginManager().registerEvents(new EntityListener(),instance);
        Bukkit.getPluginManager().registerEvents(new GunHitListener(),instance);
        //命令注册
        Objects.requireNonNull(instance.getCommand("reload")).setExecutor(new reload());
        Objects.requireNonNull(instance.getCommand("bankgameprepare")).setExecutor(new bankgameprepare());
        Objects.requireNonNull(instance.getCommand("bankgamereload")).setExecutor(new bankgamereload());
        Objects.requireNonNull(instance.getCommand("bankgameend")).setExecutor(new bankgameend());
        Objects.requireNonNull(instance.getCommand("zombieday")).setExecutor(new Zombieday());

        Objects.requireNonNull(instance.getCommand("bankgameprepare")).setTabCompleter(new bankgameprepare());
        Objects.requireNonNull(instance.getCommand("zombieday")).setTabCompleter(new Zombieday());
        //物品初始化
        Bankgameitemmanager.inititem();
        Bankgameloottable.init();
        //bossbar初始化
        Bankgamebossbar.initBankgamebossbar();

        //菜单初始化
        bankmenus.init();

        //游戏初始化
        Bankgameinit.init();
        Init.init();
        sandgameinit.initgame();
    }
}
