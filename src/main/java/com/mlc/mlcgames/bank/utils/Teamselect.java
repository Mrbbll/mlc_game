package com.mlc.mlcgames.bank.utils;

import com.mlc.mlcgames.Teammanager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Teamselect {
        public static void selectteam(ItemStack itemStack, Player player){
            //如果玩家点击的物品不是混凝土，返回
            if(!(itemStack.getType() == Material.RED_CONCRETE || itemStack.getType() == Material.LIGHT_BLUE_CONCRETE)){
                return;
            }
            else if(itemStack.getType() == Material.RED_CONCRETE){
                player.sendMessage("你选择了" + itemStack.getItemMeta().displayName());
                Teammanager.addPlayerToTeam(Teammanager.bankgame_thifeteam, player);
            }else if(itemStack.getType() == Material.LIGHT_BLUE_CONCRETE){
                player.sendMessage("你选择了" + itemStack.getItemMeta().displayName());
                Teammanager.addPlayerToTeam(Teammanager.bankgame_pliceteam, player);
            }
        }
}
