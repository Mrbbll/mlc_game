package com.mlc.mlcgames.bank.menus;

import com.mlc.mlcgames.bank.items.Bankgameitemmanager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

//    public static void updatebankmenu(Team team,Inventory menu){
//            ItemStack item1 = Objects.requireNonNull(menu.getItem(0)).clone();
//            ItemStack item2 = Objects.requireNonNull(menu.getItem(1)).clone();
//            ItemStack item3 = Objects.requireNonNull(menu.getItem(2)).clone();
//            ItemStack item4 = Objects.requireNonNull(menu.getItem(3)).clone();
//            ItemStack item5 = Objects.requireNonNull(menu.getItem(4)).clone();
//            ItemMeta itemMeta = item1.getItemMeta();
//            List<Component> lore = new ArrayList<>();
//            for()
//
//
//
//
//            menu.setItem(0,item1);
//            menu.setItem(1,item2);
//            menu.setItem(2,item3);
//            menu.setItem(3,item4);
//            menu.setItem(4,item5);
//
//
//    }

    public static void resetjobselectmenu(){
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
