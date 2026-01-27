package com.mlc.mlcgames.bank.listener;

import com.mlc.mlcgames.Teammanager;
import com.mlc.mlcgames.bank.utils.Endgame;
import com.mlc.mlcgames.bank.utils.Jobselect;
import com.mlc.mlcgames.bank.utils.Teamselect;
import com.mlc.mlcgames.bank.menus.bankmenus;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInputEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Team;

import java.util.Objects;

import static com.mlc.mlcgames.Mlcgames.bankgame;
import static com.mlc.mlcgames.bank.utils.Bankgame.gameendcountdown;
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
                if(itemStack.getItemMeta().hasItemModel() && Objects.equals(itemStack.getItemMeta().getItemModel(), NamespacedKey.fromString("mlcgames:mlcmenu"))){
                    //根据玩家所处游戏阶段和队伍打开对应的菜单
                    Openbankmenu(event.getPlayer(),bankgame.gamemode);
                }
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
//            player.sendMessage("你点击了选队菜单");
            ItemStack itemStack = event.getCurrentItem();
            if(itemStack == null){
                return;
            }else {
                Teamselect.selectteam(itemStack,player);
            }
            event.setCancelled(true);
            return;
        }
        else if(event.getClickedInventory().equals(bankmenus.policemenu)){
//            player.sendMessage("你点击了警察职业选择菜单");
            ItemStack itemStack = event.getCurrentItem();
            if(itemStack == null){
                return;}
            else {
                Jobselect.selectplicejob(itemStack,player);
                event.setCancelled(true);
            }
        }
        else if(event.getClickedInventory().equals(bankmenus.thiefmenu)){
//            player.sendMessage("你点击了小偷职业选择菜单");
            ItemStack itemStack = event.getCurrentItem();
            if(itemStack == null){
                return;}
            else {
                Jobselect.selectthiefjob(itemStack,player);
                event.setCancelled(true);
            }
        }

    }

    @EventHandler
    public  void onquit(PlayerQuitEvent event){
        Player player = event.getPlayer();
        bankgame.players.remove(player);
        Teammanager.removePlayerFromTeam(player);
        player.getInventory().clear();
        player.updateInventory();
        //最后一个玩家则停止游戏
        if(bankgame.players.isEmpty()){
            Endgame.endgame();
            if(gameendcountdown!=null){
                gameendcountdown.cancel();
            }
        }

    }

    @EventHandler
    public void onjoin(PlayerJoinEvent event){
        Player player = event.getPlayer();
        player.setRespawnLocation(bankgame.lobbyLocation);
        player.setGameMode(GameMode.ADVENTURE);
        player.teleport(bankgame.lobbyLocation);

    }

    @EventHandler
    public void ondead(PlayerDeathEvent event){
        if(bankgame.isStart){
            Player player = event.getPlayer();
            switch (bankgame.gamemode){
                case thiefvspolice -> {
                    player.setGameMode(GameMode.SPECTATOR);
                    if(Teammanager.isPlayerInTeam(player,Teammanager.bankgame_policeteam)){
                        bankgame.thiefscore+=1;

                    }else if(Teammanager.isPlayerInTeam(player,Teammanager.bankgame_thiefteam)){
                        bankgame.policescore+=1;
                    }
                }
                case thiefvsthief -> {
                    if(Teammanager.isPlayerInTeam(player,Teammanager.bankgame_policeteam)){
                        bankgame.thiefscore+=10;

                    }else if(Teammanager.isPlayerInTeam(player,Teammanager.bankgame_thiefteam)){
                        bankgame.policescore+=10;
                    }
                }
            }
        }
    }
//    @EventHandler
//    public void onopendialog(Dialog)


}
