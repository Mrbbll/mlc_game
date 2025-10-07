package com.mlc.mlcgames.bank;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;

import java.util.Set;


import static com.mlc.mlcgames.Gameinit.bank_lobby;
import static com.mlc.mlcgames.Mlcgames.*;
import static com.mlc.mlcgames.Teammanager.team_1;
import static com.mlc.mlcgames.Teammanager.team_2;

public class Bank_gameprepare {
    public Bank_gameprepare(){
        //加入在队伍的玩家
        ingamepalyer.addAll(teammanager.getteamplayer(team_1));
        ingamepalyer.addAll(teammanager.getteamplayer(team_2));
        Bukkit.broadcast(Component.text("队列加队ok"));

        for(Player player:ingamepalyer){
            //清状态，物品栏
            player.sendMessage(Component.text("prepare"));
            Set<String> tags = player.getScoreboardTags();
            for(String tag : tags) player.removeScoreboardTag(tag);

            player.clearActivePotionEffects();
            PlayerInventory inv = player.getInventory();
            inv.clear();
            //传送

            player.teleport(bank_lobby);
            Bukkit.broadcast(Component.text("传送ok"));

            //给菜单
            inv.setItem(0, itemmanger.mlcmenu);
            Bukkit.broadcast(Component.text("菜单ok"));

        }
    }

}
