package com.mlc.mlcgames.sandgame.menus;

import org.bukkit.inventory.Inventory;

import static com.mlc.mlcgames.Mlcgames.instance;
import static com.mlc.mlcgames.Mlcgames.miniMessage;

public class ShopMenu {
    public static Inventory shop_menu;
    public static void init(){
        shop_menu = instance.getServer().createInventory(null, 9, miniMessage.deserialize("shop_menu"));
    }
}
