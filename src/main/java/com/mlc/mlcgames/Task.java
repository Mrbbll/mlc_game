package com.mlc.mlcgames;

import com.mlc.mlcgames.bank.listener.Bankgamelistener;
import com.mlc.mlcgames.bank.utils.Bankgame;
import com.mlc.mlcgames.bank.utils.Bankgameinit;
import com.mlc.mlcgames.menus.bank.bankmenus;
import org.bukkit.Bukkit;

import java.util.Objects;

import static com.mlc.mlcgames.Mlcgames.instance;

public class Task {
    public static void runtask(){

        //事件注册
        Bukkit.getPluginManager().registerEvents(new Bankgamelistener() ,instance);


        //菜单初始化
        bankmenus.init();


        //游戏初始化
        Bankgameinit.init();
    }
}
