package com.mlc.mlcgames.sandgame.listener;

import com.mlc.mlcgames.Teammanager;
import com.mlc.mlcgames.sandgame.Sandgame;
import com.mlc.mlcgames.sandgame.menus.SandGameMenu;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import static com.mlc.mlcgames.Mlcgames.miniMessage;

public class Sand_Game_Teamselect_Listener implements Listener {
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

}
