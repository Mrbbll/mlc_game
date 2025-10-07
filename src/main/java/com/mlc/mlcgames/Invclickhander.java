package com.mlc.mlcgames;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.Set;

public class Invclickhander {
    public Invclickhander(){
    }

    public void clickhander(Player player, ItemStack itemStack){
        if(itemStack==null){
            player.sendMessage("点击空物品");
            return;
        }
        Material material = itemStack.getType();
        switch (material){
            case Material.AIR:{
                player.sendMessage("点击空气");
                return;
            }
            case Material.STONE_SWORD:{
                Set<String> tags = player.getScoreboardTags();
                for(String tag : tags) player.removeScoreboardTag(tag);
                player.addScoreboardTag("STONE_SWORD");
                break;
            }
            case Material.SHIELD:{
                Set<String> tags = player.getScoreboardTags();
                for(String tag : tags) player.removeScoreboardTag(tag);
                player.addScoreboardTag("SHIELD");
                break;
            }
            case Material.CROSSBOW:{
                Set<String> tags = player.getScoreboardTags();
                for(String tag : tags) player.removeScoreboardTag(tag);
                player.addScoreboardTag("CROSSBOW");
                break;
            }
            case Material.WOLF_SPAWN_EGG:{
                Set<String> tags = player.getScoreboardTags();
                for(String tag : tags) player.removeScoreboardTag(tag);
                player.addScoreboardTag("WOLF_SPAWN_EGG");
                break;
            }
            case Material.POTION:{
                Set<String> tags = player.getScoreboardTags();
                for(String tag : tags) player.removeScoreboardTag(tag);
                player.addScoreboardTag("POTION");
                break;
            }
        }

    }

}
