package com.mlc.mlcgames.bank.menus;

import com.mlc.mlcgames.bank.items.Bankgameitemmanager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;

public class bankmenus {
    public static Inventory bankmenu;
    public static Inventory policemenu;
    public static Inventory thiefmenu;
    public static void init(){
        bankmenu = Bukkit.createInventory(null, 9, Component.text("队伍选择菜单"));
        policemenu = Bukkit.createInventory(null,9, Component.text("职业选择菜单"));
        thiefmenu = Bukkit.createInventory(null,9, Component.text("职业选择菜单"));

        bankmenu.setItem(0, Bankgameitemmanager.redconcrete);
        bankmenu.setItem(1, Bankgameitemmanager.lightblueconcrete);
        bankmenu.setItem(2, Bankgameitemmanager.whiteconcrete);
        bankmenu.setItem(8, Bankgameitemmanager.netheritesword);

        policemenu.setItem(0, Bankgameitemmanager.police_stonesword);
        policemenu.setItem(1, Bankgameitemmanager.police_bow);
        policemenu.setItem(2, Bankgameitemmanager.police_woodenaxe);
        policemenu.setItem(3, Bankgameitemmanager.police_crossbow);
        policemenu.setItem(4, Bankgameitemmanager.police_wolfspawnegg);

        thiefmenu.setItem(0, Bankgameitemmanager.thief_stonesword);
        thiefmenu.setItem(1, Bankgameitemmanager.thief_bow);
        thiefmenu.setItem(2, Bankgameitemmanager.thief_woodenaxe);
        thiefmenu.setItem(3, Bankgameitemmanager.thief_crossbow);
        thiefmenu.setItem(4, Bankgameitemmanager.thief_wolfspawnegg);


    }
}
