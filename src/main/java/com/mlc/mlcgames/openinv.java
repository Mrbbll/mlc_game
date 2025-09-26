package com.mlc.mlcgames;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import static com.mlc.mlcgames.Mlcgames.ainv;
import static com.mlc.mlcgames.Mlcgames.binv;

public class openinv {
    public openinv(Player player,String team){
        Inventory inventory = Bukkit.createInventory(player,9*5);
        switch (team){
            case "a":{
                player.openInventory(ainv);
            }
            case "b":{
                player.openInventory(binv);
            }
        }
    }

}
