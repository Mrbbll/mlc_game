package com.mlc.mlcgames.bank;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;

import java.util.Set;


import static com.mlc.mlcgames.Mlcgames.*;
import static com.mlc.mlcgames.bank.Gameinit.bank_1;

public class Gameprepare {
    public Gameprepare(){
        //加入在队伍的玩家
        ingamepalyer.clear();
        ingamepalyer.addAll(teammanager.getteamplayer(team_1));
        ingamepalyer.addAll(teammanager.getteamplayer(team_2));

        for(Player player:ingamepalyer){
            //清状态，物品栏
            Set<String> tags = player.getScoreboardTags();
            for(String tag : tags) player.removeScoreboardTag(tag);
            player.clearActivePotionEffects();
            PlayerInventory inv = player.getInventory();
            inv.clear();
            teammanager.cleanTeam(team_1);
            teammanager.cleanTeam(team_2);

            //传送

            player.teleport(bank_1);

            //给菜单
            inv.setItem(0, itemmanger.mlcmenu);

        }
    }

}
