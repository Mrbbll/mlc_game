package com.mlc.mlcgames.zombieday.gamephase;

import com.mlc.mlcgames.zombieday.Zombiedaygame;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.TitlePart;
import org.bukkit.entity.Player;

import static com.mlc.mlcgames.Mlcgames.miniMessage;

public class End {
    public static void end(){
        Zombiedaygame.isstart = false;
        Zombiedaygame.turn = 0;
        for(Player player : Zombiedaygame.players) {
            player.sendTitlePart(TitlePart.TITLE,miniMessage.deserialize("游戏结束"));
            player.clearActivePotionEffects();
            player.getInventory().clear();
            player.setExp(0);
        }
    }
}
