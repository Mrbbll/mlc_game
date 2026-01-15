package com.mlc.mlcgames.bank.listener;

import com.mlc.mlcgames.Teammanager;
import com.mlc.mlcgames.bank.utils.Bankgame;
import com.mlc.mlcgames.bank.utils.Jobselect;
import com.mlc.mlcgames.bank.utils.Teamselect;
import com.mlc.mlcgames.menus.bank.bankmenus;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

import static com.mlc.mlcgames.Mlcgames.bankgame;
import static com.mlc.mlcgames.bank.utils.Openmenu.Openbankmenu;


public class Bankgamelistener implements Listener {
    @EventHandler
    public void onclick(PlayerInteractEvent event){
        if(!event.getAction().isRightClick()){
            return;
        }else{
            if(!bankgame.players.contains(event.getPlayer())){
                return;
            }
            ItemStack itemStack = event.getItem();
            if(itemStack == null){
                return;
            };
            if(itemStack.getType().equals(Material.ECHO_SHARD)){
                //根据玩家所处游戏阶段和队伍打开对应的菜单
                Openbankmenu(event.getPlayer());
            }
        }
    }

    @EventHandler
    public void inventoryclick(InventoryClickEvent event){
        if(event.getClickedInventory()==null){
            return;
        }

        Player player = (Player) event.getWhoClicked();
        if(event.getClickedInventory().equals(bankmenus.bankmenu)){
            player.sendMessage("你点击了选队菜单");
            ItemStack itemStack = event.getCurrentItem();
            if(itemStack == null){
                return;
            }else {
                Teamselect.selectteam(itemStack,player);
            }
            event.setCancelled(true);
            return;
        }
        else if(event.getClickedInventory().equals(bankmenus.plicemenu)){
            player.sendMessage("你点击了警察职业选择菜单");
            ItemStack itemStack = event.getCurrentItem();
            if(itemStack == null){
                return;}
            else {
                Jobselect.selectplicejob(itemStack,player);
                event.setCancelled(true);
            }
        }
        else if(event.getClickedInventory().equals(bankmenus.thifemenu)){
            player.sendMessage("你点击了小偷职业选择菜单");
            ItemStack itemStack = event.getCurrentItem();
            if(itemStack == null){
                return;}
            else {
                Jobselect.selectthiefjob(itemStack,player);
                event.setCancelled(true);
            }
        }

    }
}
