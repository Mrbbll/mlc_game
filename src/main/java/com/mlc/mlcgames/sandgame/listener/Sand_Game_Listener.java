package com.mlc.mlcgames.sandgame.listener;

import com.mlc.mlcgames.Teammanager;
import com.mlc.mlcgames.sandgame.Sandgame;
import com.mlc.mlcgames.sandgame.items.itemmanager;
import com.mlc.mlcgames.sandgame.items.shopmenuitem;
import com.mlc.mlcgames.sandgame.menus.ShopMenu;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
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
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Team;

import java.util.Map;

import static com.mlc.mlcgames.Mlcgames.instance;
import static com.mlc.mlcgames.Mlcgames.miniMessage;

public class Sand_Game_Listener implements Listener {

    @EventHandler
    public void onPlayerDie(PlayerDeathEvent event){
        if(!Sandgame.isstart){
            return;
        }
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

        // 死亡后进入旁观模式，5 秒倒计时后复活回本队位置
        player.setGameMode(GameMode.SPECTATOR);
        new BukkitRunnable() {
            int seconds = 5;
            @Override
            public void run() {
                if(!Sandgame.isstart || !player.isOnline()){
                    this.cancel();
                    return;
                }
                seconds--;
                if(seconds <= 0){
                    respawnPlayer(player);
                    this.cancel();
                    return;
                }
                player.sendActionBar(miniMessage.deserialize("<yellow>复活倒计时：<b>" + seconds + "s"));
            }
        }.runTaskTimer(instance, 20, 20);
    }

    private void respawnPlayer(Player player){
        if(!Sandgame.isstart || !player.isOnline()){
            return;
        }
        player.setGameMode(GameMode.ADVENTURE);
        player.setHealth(20);
        player.setFoodLevel(20);
        player.clearActivePotionEffects();
        player.teleport(playerTeamLoc(player));
    }

    private Location playerTeamLoc(Player player){
        Team team = Teammanager.getPlayerTeam(player);
        if(team.equals(Teammanager.sandgame_team_1)){
            return Sandgame.team_1_loc;
        }
        return Sandgame.team_2_loc;
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
        // 只处理点在商店格子上的点击，避免玩家在自己背包栏点击被误判为购买
        if(event.getClickedInventory() != null && event.getClickedInventory().equals(ShopMenu.shop_menu)){
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            if(!Sandgame.isstart){
                return;
            }
            ItemStack clickeditem = event.getCurrentItem();
            if(clickeditem == null){
                return;
            }
            Integer price = shopmenuitem.prices.get(clickeditem.getType());
            if(price == null){
                return;
            }
            int money = Sandgame.player_money.getOrDefault(player,0);
            if(money < price){
                player.sendMessage(Sandgame.shop_fail_msg);
                player.playSound(player, Sound.BLOCK_ANVIL_BREAK,1.0f,1.5f);
                return;
            }
            Sandgame.player_money.put(player, money - price);

            Map<Integer, ItemStack> leftover = player.getInventory().addItem(clickeditem.clone());
            leftover.values().forEach(item -> player.getWorld().dropItemNaturally(player.getLocation(), item));
            player.sendMessage(Sandgame.shop_success_msg);
            player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP,1.0f,2.0f);
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
                player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP,1.0f,0.2f);
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
                    int amount = item.getAmount();
                    item.setAmount(0);
                    Sandgame.player_money.put(player,Sandgame.player_money.getOrDefault(player,0)+amount*50);
                }
            }
        }

    }
}
