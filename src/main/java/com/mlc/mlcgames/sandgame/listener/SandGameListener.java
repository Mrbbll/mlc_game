package com.mlc.mlcgames.sandgame.listener;

import com.mlc.mlcgames.Teammanager;
import com.mlc.mlcgames.sandgame.Sandgame;
import com.mlc.mlcgames.sandgame.menus.SandGameMenu;
import com.mlc.mlcgames.sandgame.menus.ShopMenu;
import io.papermc.paper.event.player.PlayerPickItemEvent;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import static com.mlc.mlcgames.Mlcgames.miniMessage;

public class SandGameListener implements Listener {
    @EventHandler
    public void onPlayerClickInventory(InventoryClickEvent event){
        if(event.getInventory().equals(SandGameMenu.gamemenu)){
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            if(Sandgame.isstart){
                return;
            }
            ItemStack clickeditem = event.getCurrentItem();
            if(clickeditem == null) {
                return;
            }else if(clickeditem.getType().equals(Material.RED_WOOL)){
                Teammanager.addPlayerToTeam(Teammanager.sandgame_team_1,player);
                player.sendMessage(miniMessage.deserialize("team 1"));
            }else if(clickeditem.getType().equals(Material.BLUE_WOOL)){
                Teammanager.addPlayerToTeam(Teammanager.sandgame_team_2,player);
                player.sendMessage(miniMessage.deserialize("team 2"));
            }
        }
    }

    @EventHandler
    public void onPlayerClickDropper(InventoryOpenEvent event){
        if(!Sandgame.isstart){
            return;
        }
        Player player = (Player) event.getPlayer();
        if(Teammanager.isPlayerInTeam(player,Teammanager.sandgame_team_1)
                ||Teammanager.isPlayerInTeam(player,Teammanager.sandgame_team_2)
                ||Teammanager.isPlayerInTeam(player,Teammanager.sandgame_prepareteam)){

            if(event.getInventory().getType().equals(InventoryType.DROPPER)){
                event.setCancelled(true);

                player.openInventory(SandGameMenu.gamemenu);

            }
        }
    }

    @EventHandler
    public void onPlayerDie(PlayerDeathEvent event){
        Player player = event.getEntity();
        ItemStack offhanditem = player.getInventory().getItemInOffHand();
        Inventory inventory = player.getInventory();
        if(offhanditem.getType().equals(Material.SAND)) {
            player.getLocation().getWorld().dropItem(player.getLocation(),offhanditem);
            offhanditem.setAmount(0);
        }

        for(ItemStack item:inventory.getContents()){
            if (item != null && item.getType().equals(Material.SAND)) {
                player.getLocation().getWorld().dropItem(player.getLocation(), item);
                item.setAmount(0);
            }
        }


        Player killer = player.getKiller();
        Sandgame.player_kill_count.put(killer,Sandgame.player_kill_count.getOrDefault(killer,0)+1);
    }

    @EventHandler
    public void onplayergetdamagebyplayer(EntityDamageByEntityEvent event){
        if(!Sandgame.isstart){
            return;
        }

        if(event.getEntity() instanceof Player player){
            if(Teammanager.isPlayerInTeam(player,Teammanager.sandgame_team_1)
                    ||Teammanager.isPlayerInTeam(player,Teammanager.sandgame_team_2)){
                Entity damager = event.getDamager();
             if(damager instanceof  Player){
                 player.removePotionEffect(PotionEffectType.INVISIBILITY);
             }
            }
        }
    }

    @EventHandler
    public void onPlayerClickShopMenu(InventoryClickEvent event){
        if(event.getInventory().equals(ShopMenu.shop_menu)){
            event.setCancelled(true);


        }
    }

    @EventHandler
    public void onplayeropenshop(InventoryOpenEvent event){
        if(!Sandgame.isstart){
            return;
        }
        Player player = (Player) event.getPlayer();
        if(Teammanager.isPlayerInTeam(player,Teammanager.sandgame_team_1)
                ||Teammanager.isPlayerInTeam(player,Teammanager.sandgame_team_2)
                ||Teammanager.isPlayerInTeam(player,Teammanager.sandgame_prepareteam)){

            if(event.getInventory().getType().equals(InventoryType.BARREL)){
                event.setCancelled(true);

                player.openInventory(ShopMenu.shop_menu);

            }
        }
    }

    @EventHandler
    public void onplayerpickupmoney(EntityPickupItemEvent event){
        if(!Sandgame.isstart){
            return;
        }
        Entity entity = event.getEntity();
        if(entity instanceof Player player){
            if(Teammanager.isPlayerInTeam(player,Teammanager.sandgame_team_1)
                    ||Teammanager.isPlayerInTeam(player,Teammanager.sandgame_team_2)
                    ||Teammanager.isPlayerInTeam(player,Teammanager.sandgame_prepareteam)){
                ItemStack item = event.getItem().getItemStack();
                if(item.getType().equals(Material.EMERALD)){
                    event.setCancelled(true);
                    item.setAmount(0);
                    Sandgame.player_money.put(player,Sandgame.player_money.getOrDefault(player,0)+item.getAmount()*50);
                }
            }
        }

    }
}
