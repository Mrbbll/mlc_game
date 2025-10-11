package com.mlc.mlcgames.bank;


import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;

import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.Objects;


import static com.mlc.mlcgames.Mlcgames.*;
import static com.mlc.mlcgames.Teammanager.team_1;
import static com.mlc.mlcgames.Teammanager.team_2;


public class Bank_gamelistener implements Listener {
    @EventHandler
    public void playerJoinEvent(PlayerJoinEvent event){

    }
    @EventHandler
    public void playerQuitEvent(PlayerQuitEvent event){
        ingamepalyer.remove(event.getPlayer());
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if(Bank_isstart) {
            Player player = event.getPlayer();
            player.setGameMode(GameMode.SPECTATOR);
            checkgameover(player);
        }
    }



    @EventHandler
    public void playerInteractEvent(PlayerInteractEvent event){
        if (event.getAction() != Action.RIGHT_CLICK_AIR) return;
        Player player = event.getPlayer();
        player.sendMessage("检测到右键");
        ItemStack itemStack = player.getInventory().getItemInMainHand();
        ItemMeta itemMeta = itemStack.getItemMeta();
        if(itemMeta.hasItemModel()){
            if(Objects.equals(itemMeta.getItemModel(), NamespacedKey.fromString("mlcgames:mlcmenu"))){
                player.sendMessage("检测到菜单右键");
                if(teammanager.isPlayerInTeam(player, team_1)){
                    player.sendMessage("打开菜单");
                    invmanger.openinv(player,"a");
                    event.setCancelled(true);
                }
                else if(teammanager.isPlayerInTeam(player, team_2)){
                    player.sendMessage("打开菜单");
                    invmanger.openinv(player,"b");
                    event.setCancelled(true);
                }
            }
            }
    }

    @EventHandler
    public void inventoryClickEvent(InventoryClickEvent event){
        Bukkit.broadcast(Component.text("检测有菜单受到点击"));
        Player player = (Player) event.getWhoClicked();
        InventoryView inventoryView = player.getOpenInventory();
        if(!inventoryView.title().equals(Component.text("a"))&&!inventoryView.title().equals(Component.text("b"))){
            player.sendMessage("不是目标菜单");
            return;
        }
        int num = event.getHotbarButton();
        if(num != -1){
            event.setCancelled(true);
        }
        player.sendMessage("检测目标菜单受到点击");

        ItemStack itemStack1 = event.getCurrentItem();
        invclickhander.clickhander(player,itemStack1);
        event.setCancelled(true);

    }


    private void checkgameover(Player player) {

    }
}
