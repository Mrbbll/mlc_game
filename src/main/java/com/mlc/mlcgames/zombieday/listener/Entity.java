package com.mlc.mlcgames.zombieday.listener;

import com.destroystokyo.paper.profile.ProfileProperty;
import com.mlc.mlcgames.Teammanager;
import com.mlc.mlcgames.zombieday.Zombiedaygame;
import io.papermc.paper.datacomponent.DataComponentType;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ResolvableProfile;
import net.kyori.adventure.text.object.PlayerHeadObjectContents;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.net.http.WebSocket;
import java.util.UUID;

import static com.mlc.mlcgames.Mlcgames.miniMessage;

public class Entity implements WebSocket.Listener {
    @EventHandler
    public static void onplayerdied(PlayerDeathEvent event){
        if(!Zombiedaygame.isstart){
            return;
        }
        Player player = event.getPlayer();
        if(!Teammanager.isPlayerInTeam(event.getPlayer(),Teammanager.zombieday_team)){
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
        zombie.customName(miniMessage.deserialize(player.getName()+"变成的僵尸"));
        zombie.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING,9999,1,true,false));
    }

    @EventHandler
    public static void onsaveplayer(PlayerInteractEntityEvent event){
        if(event.getRightClicked() instanceof Zombie zombie){
            ItemStack itemStack = event.getPlayer().getInventory().getItemInMainHand();
            if(itemStack.getType().equals(Material.GOLDEN_APPLE)){
                saveplayer();
            }
        }
    }

    private static void saveplayer(Entity entity) {
    }
}
