package com.mlc.mlcgames.bank.listener;

import com.mlc.mlcgames.Teammanager;
import com.mlc.mlcgames.bank.items.Bankgameitemmanager;
import com.mlc.mlcgames.bank.items.Bankgameloottable;
import com.mlc.mlcgames.bank.utils.end.Endgame;
import com.mlc.mlcgames.bank.listener.clickprocess.Jobselect;
import com.mlc.mlcgames.bank.listener.clickprocess.Teamselect;
import com.mlc.mlcgames.bank.menus.bankmenus;
import com.mlc.mlcgames.bank.utils.Setplayerlaydown;
import io.papermc.paper.event.player.PlayerItemFrameChangeEvent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Team;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static com.mlc.mlcgames.Mlcgames.*;
import static com.mlc.mlcgames.Teammanager.bankgame_policeteam;
import static com.mlc.mlcgames.Teammanager.bankgame_thiefteam;
import static com.mlc.mlcgames.bank.Bankgamesidebar.updatesidebar;
import static com.mlc.mlcgames.bank.listener.clickprocess.Openmenu.Openbankmenu;


public class Bankgamelistener implements Listener {
    private final List<Material> disallowblocks = List.of(
            Material.CHEST,
            Material.ENDER_CHEST,
            Material.RED_BED,
            Material.HOPPER,
            Material.CAKE,
            Material.SMOKER,
            Material.FLOWER_POT,
            Material.BLAST_FURNACE,
            Material.FURNACE,
            Material.CHISELED_BOOKSHELF,
            Material.PINK_BED,
            Material.LIGHT_BLUE_BED,
            Material.WHITE_BED,
            Material.BLACK_BED,
            Material.POTTED_MANGROVE_PROPAGULE,
            Material.POTTED_LILY_OF_THE_VALLEY,
            Material.POTTED_BAMBOO,
            Material.POTTED_TORCHFLOWER,
            Material.POTTED_CORNFLOWER,
            Material.POTTED_FERN,
            Material.POTTED_AZURE_BLUET,
            Material.POTTED_PINK_TULIP,
            Material.POTTED_RED_TULIP,
            Material.POTTED_ORANGE_TULIP,
            Material.POTTED_DANDELION,
            Material.POTTED_BLUE_ORCHID,
            Material.POTTED_OAK_SAPLING,
            Material.POTTED_CACTUS,
            Material.WAXED_COPPER_GOLEM_STATUE,
            Material.POTTED_OPEN_EYEBLOSSOM
    );




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
    //倒下禁止交互
    @EventHandler
    public void onlaydowninteract(PlayerInteractEvent event){
        if(!bankgame.isStart){
            return;
        }
        Player player = event.getPlayer();
        if(player.hasPotionEffect(PotionEffectType.LUCK)){
            event.setCancelled(true);
        }

    }
    //防止打开其他东西
    @EventHandler
    public void onopenotherinv(PlayerInteractEvent event){
        if(!bankgame.isStart){
            return;
        }

        if (event.getClickedBlock() != null && disallowblocks.contains(event.getClickedBlock().getType())) {
            event.setCancelled(true);
        }
    }

    //防止破坏画
    @EventHandler
    public void ondamage(HangingBreakByEntityEvent event){
        if(!bankgame.isStart){
            return;
        }
        if(event.getEntity() instanceof Painting){
            event.setCancelled(true);
        }
    }
    //防止使用物品展示框
    @EventHandler
    public void onuseitemframe(PlayerItemFrameChangeEvent event){
        if(!bankgame.isStart){
            return;
        }
        event.setCancelled(true);
    }

    //召唤狗事件
    @EventHandler
    public void onusespawnegg(PlayerInteractEvent event){
        if(!bankgame.isStart){
            return;
        }
        if(!event.getAction().isRightClick() || event.getClickedBlock() == null || event.getItem() == null){
            return;
        }
        if (event.getItem().getType().equals(Bankgameitemmanager.wolfspawnegg.getType())) {
            Block block = event.getClickedBlock();
            Location location = block.getLocation().add(0.5,1,0.5);
            Entity entity = location.getWorld().spawnEntity(location, EntityType.WOLF);
            Wolf wolf = (Wolf) entity;
            wolf.setTamed(true);
            wolf.setOwner(event.getPlayer());
            Teammanager.addentityToTeam(Teammanager.getPlayerTeam(event.getPlayer()),wolf);

            wolf.setAdult();

            wolf.getEquipment().setChestplate(Bankgameitemmanager.wolf_armor);
            event.getItem().setAmount(event.getItem().getAmount()-1);
        }
    }



    @EventHandler
    public void inventoryclick(InventoryClickEvent event){
        if(bankgame.isStart){
            return;
        }
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
        Teammanager.removePlayerFromTeam(player);
        bankgame.players.remove(player);
        if (bankgame.isStart) {
            //保证有金条
            if(player.getInventory().contains(Bankgameitemmanager.golditem)){
                player.getInventory().remove(Bankgameitemmanager.golditem);
                player.dropItem(Bankgameitemmanager.golditem);
            }

            //最后一个玩家则停止游戏
            boolean isteamenpty = true;
            Team team = Teammanager.getPlayerTeam(player);
            for(String string :team.getEntries()){
                //如果没或者玩家，直接结束游戏
                Player player1 = Bukkit.getPlayer(string);
                    if(player1!=null && player1.getGameMode()!=GameMode.SPECTATOR){
                    isteamenpty = false;
                }
            }
            if(isteamenpty){
                if(team.equals(bankgame_policeteam)){
                    bankgame.thiefscore += 1000;
                }else if(team.equals(bankgame_thiefteam)){
                    bankgame.policescore += 1000;
                }
                Endgame.endgame();
                return;
            }

            bankgame.players.remove(player);
            Teammanager.removePlayerFromTeam(player);
            player.getInventory().clear();
            player.updateInventory();

        }

    }

    @EventHandler
    public void onjoin(PlayerJoinEvent event){
        if (bankgame.isStart) {
            Player player = event.getPlayer();
            player.setRespawnLocation(bankgame.lobbyLocation);
            player.setGameMode(GameMode.SPECTATOR);
            player.teleport(bankgame.spectatelocation);
        }

    }


    @EventHandler
    public void ondead(PlayerDeathEvent event){
        if(bankgame.isStart){
            Player player = event.getPlayer();
//            if(event.getDamageSource().getCausingEntity() instanceof Player causer){
//                event.deathMessage(miniMessage.deserialize( "<head:"+ causer.getName() + "> " + " <b>KILL<red> ❌ -><reset> " + " <head:"+ player.getName() + ">"));
//            }

            switch (bankgame.gamemode){
                case thiefvspolice -> {
                    event.setCancelled(true);
                    if(event.getDamageSource().getCausingEntity() instanceof Player causer){
                        instance.getServer().broadcast(miniMessage.deserialize( "<head:"+ causer.getName() + "> " + " <b>KILL<red> ❌ -><reset> " + " <head:"+ player.getName() + ">"));
                    }else {
                        instance.getServer().broadcast(miniMessage.deserialize("<b><red> ❌  <head:"+ player.getName() + "> "));
                    }

                    if(!player.hasPotionEffect(PotionEffectType.LUCK)){
                        new Setplayerlaydown(player);
                    }

                    //副手检测
                    ItemStack offhand = player.getInventory().getItemInOffHand();
                    if(offhand.equals(Bankgameitemmanager.golditem)){
                        player.getInventory().setItemInOffHand(null);
                        player.dropItem(offhand);
                    } else if (offhand.equals(Bankgameloottable.key)) {
                        player.getInventory().setItemInOffHand(null);
                        player.dropItem(offhand);
                    } else if (offhand.getType().equals(Material.EMERALD)) {
                        player.getInventory().setItemInOffHand(null);
                        player.dropItem(offhand);
                    } else if (offhand.equals(Bankgameloottable.diamond)) {
                        player.getInventory().setItemInOffHand(null);
                        player.dropItem(offhand);
                    }

                    ItemStack[] items = player.getInventory().getContents();
                    //物品栏检测
                    for(ItemStack itemStack : items){
                        if(itemStack == null){
                            continue;
                        }
                        if(itemStack.equals(Bankgameitemmanager.golditem)){
                            player.getInventory().remove(itemStack);
                            player.dropItem(itemStack);
                        } else if (itemStack.equals(Bankgameloottable.key)) {
                            player.getInventory().remove(itemStack);
                            player.dropItem(itemStack);
                        } else if (itemStack.getType().equals(Material.EMERALD)) {
                            player.getInventory().remove(itemStack);
                            player.dropItem(itemStack);
                        } else if (itemStack.getType().equals(Material.DIAMOND)) {
                            player.getInventory().remove(itemStack);
                            player.dropItem(itemStack);
                        }
                    }

//                    if(Teammanager.isPlayerInTeam(player, bankgame_policeteam)){
//                        bankgame.thiefscore+=1;

                    if(Teammanager.isPlayerInTeam(player, bankgame_thiefteam)){
                        bankgame.policescore+=1;
                    }

                    //如果没或者玩家，直接结束游戏
                    boolean isteamenpty = true;
                    Team team = Teammanager.getPlayerTeam(player);
                    for(String string :team.getEntries()){
                        Player player1 = Bukkit.getPlayer(string);
                        if(player1!=null && !player1.hasPotionEffect(PotionEffectType.LUCK)){
                            isteamenpty = false;
                        }
                    }
                    if(isteamenpty){
                        if(team.equals(bankgame_policeteam)){
                            bankgame.thiefscore += 2000;
                        }else if(team.equals(bankgame_thiefteam)){
                            bankgame.policescore += 2000;
                        }
                        Endgame.endgame();
                        player.teleportAsync(bankgame.bankgameLocation);
                    }
                }
                case thiefvsthief -> {
                    if(event.getDamageSource().getCausingEntity() instanceof Player causer){
                        event.deathMessage(miniMessage.deserialize( "<head:"+ causer.getName() + "> " + " <b>KILL<red> ❌ -><reset> " + " <head:"+ player.getName() + ">"));
                    }
                    if(Teammanager.isPlayerInTeam(player, bankgame_policeteam)){
                        bankgame.thiefscore+=10;

                    }else if(Teammanager.isPlayerInTeam(player, bankgame_thiefteam)){
                        bankgame.policescore+=10;
                    }
                }
            }
        }
    }
//    @EventHandler
//    public void onopendialog(Dialog)

    @EventHandler
    public void onplayeropenchest(PlayerInteractEvent event){
        if(!bankgame.isStart){
            return;
        }
        if(!event.getAction().isRightClick()){
            return;
        }
        if(event.getClickedBlock()==null){
            return;
        }
        if(event.getClickedBlock().getType()!=Material.BARREL){
            return;
        }
        for (Entity entity : event.getClickedBlock().getLocation().getNearbyEntities(0.1,0.1,0.1)){
            if(entity instanceof ItemDisplay){
                return;
            }
        }
        //往里面加随机物品
        Bankgameloottable.addrandomitem(event.getClickedBlock());
        //在附近生成标记
        event.getClickedBlock().getLocation().getWorld().spawnEntity(event.getClickedBlock().getLocation(), EntityType.ITEM_DISPLAY);

    }

}
