package com.mlc.mlcgames.zombieday.gamephase;

import com.mlc.mlcgames.zombieday.Zombiedaygame;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.TitlePart;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import static com.mlc.mlcgames.Mlcgames.miniMessage;

public class End {
    public static void end(){
        Zombiedaygame.isstart = false;
        Zombiedaygame.turn = 0;
        Zombiedaygame.zombiecount = 0;
        Zombiedaygame.countdown = 120;
        for(Player player : Zombiedaygame.players) {
            player.sendTitlePart(TitlePart.TITLE,miniMessage.deserialize("游戏结束"));
            player.clearActivePotionEffects();
            player.getInventory().clear();
            player.setExp(0);
        }
        Zombiedaygame.players.clear();
        for(Block block : Zombiedaygame.obstacles){
            block.setType(Material.AIR);
        }
        Zombiedaygame.obstaclebreaktime.clear();
    }
}
