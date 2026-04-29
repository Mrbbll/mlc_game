package com.mlc.mlcgames.bank.utils.prepare;

import com.mlc.mlcgames.bank.Bankgamesidebar;
import com.mlc.mlcgames.bank.items.Bankgameitemmanager;
import com.mlc.mlcgames.bank.utils.Gamemode;
import net.kyori.adventure.text.Component;
import org.bukkit.GameMode;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;

import static com.mlc.mlcgames.Mlcgames.bankgame;
import static com.mlc.mlcgames.Mlcgames.miniMessage;
import static com.mlc.mlcgames.bank.utils.Bankgamebossbar.bankgamebossbar;

public class Gameprepare {
    public static void prepare(Player player, Gamemode gamemode){
        Bankgamesidebar.showsidebar(player);

        switch (gamemode){
            case thiefvsthief:
                //传送
                player.teleport(bankgame.bankgameLocation);
                //设置玩家
                player.setGameMode(GameMode.ADVENTURE);
                AttributeInstance attributeInstance = player.getAttribute(Attribute.MAX_HEALTH);
                if (attributeInstance != null) {
                    attributeInstance.setBaseValue(20);
                }
                player.setHealth(20);
                player.clearActivePotionEffects();
                player.setFoodLevel(20);
                player.getInventory().clear();
                player.updateInventory();
                player.give(Bankgameitemmanager.bankgamemenu);
                player.sendMessage(miniMessage.deserialize("\n\n<bold><#fffb00>右键菜单打开选队界面"));
                if(!bankgame.players.contains(player)){
                    bankgame.players.add(player);
                }
                break;
            case thiefvspolice:
                //传送
                player.teleport(bankgame.bankgameLocation);
                //设置玩家
                player.setGameMode(GameMode.ADVENTURE);
                AttributeInstance attributeInstance1 = player.getAttribute(Attribute.MAX_HEALTH);
                if (attributeInstance1 != null) {
                    attributeInstance1.setBaseValue(20);
                }
                player.setHealth(20);
                player.clearActivePotionEffects();
                player.setFoodLevel(20);
                player.getInventory().clear();
                player.updateInventory();
                player.give(Bankgameitemmanager.bankgamemenu);
                player.sendMessage(miniMessage.deserialize("\n\n<bold><#fffb00>右键菜单打开选队界面"));
                if(!bankgame.players.contains(player)){
                    bankgame.players.add(player);
                }
                break;
        }

    }
}
