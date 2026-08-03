package com.mlc.mlcgames.sandgame.menus;

import com.mlc.mlcgames.sandgame.items.shopmenuitem;
import org.bukkit.inventory.Inventory;

import static com.mlc.mlcgames.Mlcgames.instance;
import static com.mlc.mlcgames.Mlcgames.miniMessage;

public class ShopMenu {
    public static Inventory shop_menu;
    public static void init(){
        shop_menu = instance.getServer().createInventory(null, 9*6, miniMessage.deserialize("shop_menu"));
        shop_menu.setItem(0, shopmenuitem.wool);
        shop_menu.setItem(1, shopmenuitem.sand);
        shop_menu.setItem(2, shopmenuitem.stonesword);
        shop_menu.setItem(3, shopmenuitem.beef);
        shop_menu.setItem(4, shopmenuitem.cobweb);
        shop_menu.setItem(5, shopmenuitem.bow);
        shop_menu.setItem(6, shopmenuitem.arrow);
        shop_menu.setItem(7, shopmenuitem.goldenapple);
    }
}
