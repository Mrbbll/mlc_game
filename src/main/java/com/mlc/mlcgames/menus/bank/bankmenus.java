package com.mlc.mlcgames.menus.bank;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;

public class bankmenus {
    public static Inventory bankmenu;
    public static Inventory plicemenu;
    public static Inventory thifemenu;
    public static void init(){
        bankmenu = Bukkit.createInventory(null,9*5, Component.text("队伍选择菜单"));
        plicemenu = Bukkit.createInventory(null,9*5, Component.text("职业选择菜单"));
        thifemenu = Bukkit.createInventory(null,9*5, Component.text("职业选择菜单"));
    }
}
