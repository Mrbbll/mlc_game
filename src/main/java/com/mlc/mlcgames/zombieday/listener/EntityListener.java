package com.mlc.mlcgames.zombieday.listener;

import com.mlc.mlcgames.Teammanager;
import com.mlc.mlcgames.zombieday.Zombiedaygame;
import com.mlc.mlcgames.zombieday.gamephase.End;
import com.mlc.mlcgames.zombieday.inv.ArmorInv;
import com.mlc.mlcgames.zombieday.inv.BulletInv;
import com.mlc.mlcgames.zombieday.inv.FoodInv;
import com.mlc.mlcgames.zombieday.inv.PotionInv;
import com.mlc.mlcgames.zombieday.managers.areamanager;
import com.mlc.mlcgames.zombieday.zombie.ZombieLoot;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Merchant;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;


import java.util.ArrayList;
import java.util.List;

import static com.mlc.mlcgames.Mlcgames.*;
import static com.mlc.mlcgames.zombieday.Zombiedaygame.gameworld;
import static com.mlc.mlcgames.zombieday.zombie.Spwaner.zombie_type;

public class EntityListener implements Listener {
    @EventHandler
    public static void onblockbreak(BlockBreakEvent event){
        if(!Zombiedaygame.isstart){
            return;
        }
        Player player = event.getPlayer();
        if(!Teammanager.isPlayerInTeam(player,Teammanager.zombieday_team)){
            return;
        };
        Material material = event.getBlock().getType();
        if(material.equals(Material.OAK_LOG)){
            event.getPlayer().getInventory().addItem(ItemStack.of(material));
            event.setCancelled(true);
            return;
        }
        else if(material.equals(Material.GRAVEL)) {
            event.getPlayer().getInventory().addItem(ItemStack.of(Material.FLINT));
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler
    public static void onplayerdied(PlayerDeathEvent event){
        if(!Zombiedaygame.isstart){
            return;
        }
        Player player = event.getPlayer();
        if(!Teammanager.isPlayerInTeam(player,Teammanager.zombieday_team)){
            return;
        };
        player.setGameMode(GameMode.SPECTATOR);

        World world = player.getWorld();
        Location location = player.getLocation();
        Zombie zombie = (Zombie) world.spawnEntity(location, EntityType.ZOMBIE);
        zombie.setAdult();

        zombie.getEquipment().setHelmet(player.getInventory().getHelmet());
        zombie.getEquipment().setChestplate(player.getInventory().getChestplate());
        zombie.getEquipment().setLeggings(player.getInventory().getLeggings());
        zombie.getEquipment().setBoots(player.getInventory().getBoots());
        zombie.getEquipment().setItemInMainHand(player.getInventory().getItemInMainHand());
        zombie.getEquipment().setItemInOffHand(player.getInventory().getItemInOffHand());
        zombie.customName(miniMessage.deserialize(player.getName()));
        zombie.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING,9999,1,true,false));
        server.broadcast(miniMessage.deserialize(player.getName()+"死亡"));
        new saveListener(player, zombie);

        boolean isplayeralldie = true;
        for(Player p:Zombiedaygame.players){
            if(p.getGameMode().equals(GameMode.ADVENTURE)){
                isplayeralldie = false;
                break;
            }
        }
        if(isplayeralldie){
            End.end();
        }

    }

    @EventHandler
    public static void onsaveplayer(PlayerInteractEntityEvent event){
        if(!Zombiedaygame.isstart){
            return;
        }
        if(!Teammanager.isPlayerInTeam(event.getPlayer(),Teammanager.zombieday_team)){
            return;
        };
        if(event.getRightClicked() instanceof Zombie zombie){
            ItemStack itemStack = event.getPlayer().getInventory().getItemInMainHand();
            if(itemStack.getType().equals(Material.GOLDEN_APPLE)){
                Player saver = event.getPlayer();
                saveplayer(zombie,saver);
            }
        }
    }

    private static void saveplayer(Entity entity,Player saver) {
        String name = entity.getName();
        Player player = Bukkit.getPlayer(name);
        entity.setInvulnerable(true);
        Zombie zombie = (Zombie) entity;
        zombie.setAI(false);

        if(player!=null && Teammanager.isPlayerInTeam(player,Teammanager.zombieday_team)) {
            BukkitTask task = new BukkitRunnable(){

                @Override
                public void run() {
                    if(player.isOnline()&&entity.isValid()&&Zombiedaygame.isstart){
                        player.setGameMode(GameMode.ADVENTURE);
                        player.teleport(entity);
                        entity.remove();
                        server.broadcast(miniMessage.deserialize(saver.getName()+"救起"+player.getName()));
                    }
                }
            }.runTaskLater(instance,60);

        }
        else {
            entity.getWorld().spawnEntity(entity.getLocation(), EntityType.VILLAGER);
            entity.remove();
        }
    }
    @EventHandler
    private static void entitydie(EntityDeathEvent event){
        if(!Zombiedaygame.isstart){
            return;
        }
        event.setDroppedExp(0);
        if(event.getEntity() instanceof Zombie zombie){
            event.getDrops().clear();
            PersistentDataContainer pdc =  zombie.getPersistentDataContainer();
            String type = pdc.getOrDefault(zombie_type, PersistentDataType.STRING,"null");
            List<ItemStack> customDrops;
            switch (type){
                case "normal":

                    Zombiedaygame.zombies.remove(zombie);
                    Zombiedaygame.zombiecount=Zombiedaygame.zombies.size();

                    customDrops = ZombieLoot.generateLoot(type);
                    event.getDrops().addAll(customDrops);
                    break;
                case "fast":

                    Zombiedaygame.zombies.remove(zombie);
                    Zombiedaygame.zombiecount=Zombiedaygame.zombies.size();

                    customDrops = ZombieLoot.generateLoot(type);
                    event.getDrops().addAll(customDrops);
                    break;
                case "highjump":

                    Zombiedaygame.zombies.remove(zombie);
                    Zombiedaygame.zombiecount=Zombiedaygame.zombies.size();

                    customDrops = ZombieLoot.generateLoot(type);
                    event.getDrops().addAll(customDrops);
                    break;
                case "police":

                    Zombiedaygame.zombies.remove(zombie);
                    Zombiedaygame.zombiecount=Zombiedaygame.zombies.size();

                    customDrops = ZombieLoot.generateLoot(type);
                    event.getDrops().addAll(customDrops);
                    break;
                case "rich":

                    Zombiedaygame.zombies.remove(zombie);
                    Zombiedaygame.zombiecount=Zombiedaygame.zombies.size();
                    customDrops = ZombieLoot.generateLoot(type);
                    event.getDrops().addAll(customDrops);
                    break;
                case "boss":
                    Zombiedaygame.zombies.remove(zombie);
                    Zombiedaygame.zombiecount=Zombiedaygame.zombies.size();

                    customDrops = ZombieLoot.generateLoot(type);
                    event.getDrops().addAll(customDrops);
                    break;
                case "null":
                    break;
            }
        }
    }
    @EventHandler
    public static void onplayerleave(PlayerQuitEvent event){
        if(!Zombiedaygame.isstart){
            return;
        }
        Player player = event.getPlayer();
        if(!Teammanager.isPlayerInTeam(player,Teammanager.zombieday_team)){
            return;
        }else {
            Teammanager.removePlayerFromTeam(player);
            Zombiedaygame.players.remove(player);
        }
        if(Zombiedaygame.players.isEmpty()){
            End.end();
        }
    }

    @EventHandler
    public static void openshopandarea(InventoryOpenEvent event){
        if (!Zombiedaygame.isstart){
            return;
        }
        if(event.getInventory().getType().equals(InventoryType.BEACON)&&Teammanager.isPlayerInTeam((Player) event.getPlayer(),Teammanager.zombieday_team)){
            event.setCancelled(true);
            Block block = event.getPlayer().getTargetBlock(null,4).getRelative(BlockFace.DOWN);
            if(block.getType().equals(Material.COPPER_BLOCK)){
                BulletInv.open((Player) event.getPlayer());
                return;
            } else if (block.getType().equals(Material.IRON_BLOCK)) {
                PotionInv.open((Player) event.getPlayer());
                return;
            } else if (block.getType().equals(Material.HAY_BLOCK)) {
                FoodInv.open((Player) event.getPlayer());
                return;
            } else if (block.getType().equals(Material.WHITE_WOOL)) {
                ArmorInv.open((Player) event.getPlayer());
                return;
            } else if (block.getType().equals(Material.COBBLESTONE_WALL)) {
                ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
                if (item.getType().equals(Material.TRIAL_KEY)) {
                    int keynum = item.getAmount();
                    if(keynum>=Zombiedaygame.requirekeynum){
                        item.setAmount(keynum-Zombiedaygame.requirekeynum);
                        areamanager.openarea(block.getLocation());
                        Zombiedaygame.requirekeynum++;
                        server.broadcast(
                                miniMessage.deserialize("玩家"+event.getPlayer().getName()+"打开了新区域")
                        );
                        server.broadcast(
                                miniMessage.deserialize("现在需要"+Zombiedaygame.requirekeynum+"个钥匙打开新区域")
                        );
                    }else {
                        event.getPlayer().sendMessage(miniMessage.deserialize("你需要"+Zombiedaygame.requirekeynum+"个钥匙打开该区域"));
                    }
                }
            }

        }
    }

}
