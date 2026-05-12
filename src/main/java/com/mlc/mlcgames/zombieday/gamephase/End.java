package com.mlc.mlcgames.zombieday.gamephase;

import com.mlc.mlcgames.zombieday.Zombiedaygame;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public class End {
    public static void end(){
        Zombiedaygame.isstart = false;
        for(Player player : Zombiedaygame.players) {
            player.sendActionBar(Component.text("游戏结束"));
            player.clearActivePotionEffects();
        }
    }

}
