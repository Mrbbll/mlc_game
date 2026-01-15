package com.mlc.mlcgames.bank.utils;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import static com.mlc.mlcgames.Mlcgames.bankgame;

public class Jobselect {
    public static void selectplicejob(ItemStack itemStack, Player player) {
        if(itemStack.getType()== Material.STONE_SWORD){
                player.sendMessage("你选择了" + itemStack.getItemMeta().displayName());
                bankgame.playerJobs.put(player, "sword");
        }else if(itemStack.getType()== Material.CROSSBOW){
                player.sendMessage("你选择了" + itemStack.getItemMeta().displayName());
                bankgame.playerJobs.put(player, "crossbow");
        }else if(itemStack.getType()== Material.WOODEN_AXE){
                player.sendMessage("你选择了" + itemStack.getItemMeta().displayName());
                bankgame.playerJobs.put(player, "woodenaxe");
        }else if(itemStack.getType()== Material.WOLF_SPAWN_EGG){
                player.sendMessage("你选择了" + itemStack.getItemMeta().displayName());
                bankgame.playerJobs.put(player, "wolfspawnegg");
        }else if(itemStack.getType()== Material.BOW){
                player.sendMessage("你选择了" + itemStack.getItemMeta().displayName());
                bankgame.playerJobs.put(player, "bow");
        }

    }

    public static void selectthiefjob(ItemStack itemStack, Player player) {
        if(itemStack.getType()== Material.STONE_SWORD){
                player.sendMessage("你选择了" + itemStack.getItemMeta().displayName());
                bankgame.playerJobs.put(player, "sword");
        }else if(itemStack.getType()== Material.CROSSBOW){
                player.sendMessage("你选择了" + itemStack.getItemMeta().displayName());
                bankgame.playerJobs.put(player, "crossbow");
        }else if(itemStack.getType()== Material.WOODEN_AXE){
                player.sendMessage("你选择了" + itemStack.getItemMeta().displayName());
                bankgame.playerJobs.put(player, "woodenaxe");
        }else if(itemStack.getType()== Material.WOLF_SPAWN_EGG){
                player.sendMessage("你选择了" + itemStack.getItemMeta().displayName());
                bankgame.playerJobs.put(player, "wolfspawnegg");
        }else if(itemStack.getType()== Material.BOW){
                player.sendMessage("你选择了" + itemStack.getItemMeta().displayName());
                bankgame.playerJobs.put(player, "bow");
        }
    }
}
