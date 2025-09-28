package com.mlc.mlcgames;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.apache.commons.lang3.ObjectUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Objects;

import static com.mlc.mlcgames.Mlcgames.*;


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
                    new openinv(player,"b");
                }
            }
            }
    }
    @EventHandler
    public void inventoryClickEvent(InventoryClickEvent event){
        Inventory inventory = event.getClickedInventory();
        Player player = (Player) event.getWhoClicked();
        InventoryView inv = player.getOpenInventory();
        if(!inv.title().equals(miniMessage.deserialize("<!i><color:#38deff>a</color>"))||!inv.title().equals(miniMessage.deserialize("<!i><color:#38deff>b</color>"))){
            return;
        };
        int num = event.getHotbarButton();
        if(num != -1){
            event.setCancelled(true);
        }
        ItemStack itemStack1 = event.getCurrentItem();
        if(itemStack1==null){return;}
        Material material = itemStack1.getType();
        switch (material){
            case Material.AIR:{
                return;
            }
            case Material.STONE_SWORD:{
                player.addScoreboardTag("sword");
            }

        }

    }
}
