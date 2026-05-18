package com.mlc.mlcgames.zombieday.listener;

import com.mlc.mlcgames.Teammanager;
import com.mlc.mlcgames.utils.item.Throwable;
import com.mlc.mlcgames.zombieday.Item;
import com.mlc.mlcgames.zombieday.Zombiedaygame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class throwitem implements Listener {
    @EventHandler
    public void onThrowable(PlayerInteractEvent event){
        if(!Zombiedaygame.isstart){
            return;
        }
        if(event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK){
            return;
        }
        Player player = event.getPlayer();
        if(!Teammanager.isPlayerInTeam(player, Teammanager.zombieday_team)){
            return;
        }
        ItemStack item = event.getItem();
        if(item == null){
            return;
        }
        if(item.getItemMeta().getPersistentDataContainer().getOrDefault(Item.itemtype, PersistentDataType.STRING,"null").equals("grenade")){
            int amount = item.getAmount();
            if(amount <= 0){
                return;
            }
            item.setAmount(amount-1);
            Throwable.throwgrenade(player);
        }
    }
}
