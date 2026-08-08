package com.mlc.mlcgames.sandgame.menus;

import com.mlc.mlcgames.sandgame.items.itemmanager;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

import static com.mlc.mlcgames.Mlcgames.instance;
import static com.mlc.mlcgames.Mlcgames.miniMessage;

public class SandGameMenu {
    public static Inventory gamemenu;
    public static void init(){
        gamemenu = instance.getServer().createInventory(null, InventoryType.HOPPER,miniMessage.deserialize("team selecter"));
        gamemenu.setItem(1,itemmanager.team_1_wool);
        gamemenu.setItem(3,itemmanager.team_2_wool);
    }
}
