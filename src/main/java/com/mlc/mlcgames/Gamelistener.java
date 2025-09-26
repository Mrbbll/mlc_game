package com.mlc.mlcgames;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Objects;

import static com.mlc.mlcgames.Mlcgames.ateam;
import static com.mlc.mlcgames.Mlcgames.bteam;


public class Gamelistener implements Listener {
    @EventHandler
    public void playerJoinEvent(PlayerJoinEvent event){

    }
    @EventHandler
    public void playerQuitEvent(PlayerQuitEvent event){

    }
    @EventHandler
    public void playerInteractEvent(PlayerInteractEvent event){
        if (event.getAction() != Action.RIGHT_CLICK_AIR) return;
        Player player = event.getPlayer();
        ItemStack itemStack = player.getInventory().getItemInMainHand();
        ItemMeta itemMeta = itemStack.getItemMeta();
        if(itemMeta.hasItemModel()){
            if(Objects.equals(itemMeta.getItemModel(), NamespacedKey.fromString("mlc:mlcmenu"))){
                if(ateam.contains(player)){
                    new openinv(player,"a");
                }
                else if(bteam.contains(player)){
                    new openinv(player,"a");
                }
            }
            }
    }
    @EventHandler
    public void inventoryClickEvent(InventoryClickEvent event){

    }
}
