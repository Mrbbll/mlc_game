package com.mlc.mlcgames;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;

import static com.mlc.mlcgames.Mlcgames.ingamepalyer;

public class Gameprepare {
    public Gameprepare(){
        for(Player player:ingamepalyer){
            player.clearActivePotionEffects();
            PlayerInventory inv = player.getInventory();
            inv.clear();
            inv.setItem(0, ItemStack.of(Material.AIR));
        }
    }

}
