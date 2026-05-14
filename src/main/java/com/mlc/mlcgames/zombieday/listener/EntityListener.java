package com.mlc.mlcgames.zombieday.listener;

import com.mlc.mlcgames.Teammanager;
import com.mlc.mlcgames.zombieday.Zombiedaygame;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;


import static com.mlc.mlcgames.Mlcgames.*;
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

        if(player!=null && Teammanager.isPlayerInTeam(player,Teammanager.zombieday_team)) {
            player.setGameMode(GameMode.ADVENTURE);
            entity.remove();
            server.broadcast(miniMessage.deserialize(saver.getName()+"救起"+player.getName()));
        }
        else {
            entity.getWorld().spawnEntity(entity.getLocation(), EntityType.PLAYER);
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
            switch (type){
                case "normal":
                    break;
                case "fast":
                    break;
                case "highjump":
                    break;
                case "police":
                    break;
                case "rich":
                    break;
            }
        }
    }
}
