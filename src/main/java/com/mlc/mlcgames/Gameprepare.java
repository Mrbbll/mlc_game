package com.mlc.mlcgames;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scoreboard.Team;

import static com.mlc.mlcgames.Mlcgames.*;

public class Gameprepare {
    public Gameprepare(){
        for(Player player:ingamepalyer){
            //清状态，物品栏
            player.clearActivePotionEffects();
            PlayerInventory inv = player.getInventory();
            inv.clear();
            teammanager.cleanTeam(ateam);
            teammanager.cleanTeam(bteam);

            //传送


            //给菜单
            inv.setItem(0, mlcmenu);

        }
    }

}
