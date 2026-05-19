package com.mlc.mlcgames.zombieday.inv;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class TrushCan {
    public static Inventory inventory = Bukkit.createInventory(null, 9*6, Component.text("TrushCan"));
    public static void open(Player player){
        player.openInventory(inventory);
    }
    public static void clear(){
        inventory.clear();
    }
}
