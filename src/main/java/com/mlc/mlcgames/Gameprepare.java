package com.mlc.mlcgames;

import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;

import java.util.Set;


import static com.mlc.mlcgames.Mlcgames.*;

public class Gameprepare {
    public Gameprepare(){
        //加入在队伍的玩家
        ingamepalyer.addAll(teammanager.getteamplayer(ateam));
        ingamepalyer.addAll(teammanager.getteamplayer(bteam));

        for(Player player:ingamepalyer){
            //清状态，物品栏
            Set<String> tags = player.getScoreboardTags();
            for(String tag : tags) player.removeScoreboardTag(tag);
            player.clearActivePotionEffects();
            PlayerInventory inv = player.getInventory();
            inv.clear();
            teammanager.cleanTeam(ateam);
            teammanager.cleanTeam(bteam);

            //传送


            //给菜单
            inv.setItem(0, itemmanger.mlcmenu);

        }
    }

}
