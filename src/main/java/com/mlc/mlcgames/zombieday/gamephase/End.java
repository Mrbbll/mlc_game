package com.mlc.mlcgames.zombieday.gamephase;

import com.mlc.mlcgames.zombieday.Zombiedaygame;
import com.mlc.mlcgames.zombieday.managers.areamanager;
import com.mlc.mlcgames.zombieday.managers.scoreboard;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.TitlePart;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.*;

import java.util.HashSet;
import java.util.Objects;

import static com.mlc.mlcgames.Mlcgames.miniMessage;
import static com.mlc.mlcgames.Mlcgames.scoreboardManager;
import static com.mlc.mlcgames.zombieday.Zombiedaygame.*;

public class End {
    public static void end(){
        Zombiedaygame.isstart = false;
        Zombiedaygame.turn = 0;
        Zombiedaygame.zombiecount = 0;
        Zombiedaygame.countdown = 120;
        Zombiedaygame.requirekeynum =1;
        for(Player player : Zombiedaygame.players) {
            player.sendTitlePart(TitlePart.TITLE,miniMessage.deserialize("游戏结束"));
            player.clearActivePotionEffects();
            player.getInventory().clear();
            player.setExp(0);
            player.setGameMode(GameMode.ADVENTURE);
            player.teleport(Zombiedaygame.prepareloc);
            scoreboard.updatesidebar(player);
            Objects.requireNonNull(player.getAttribute(Attribute.MAX_HEALTH)).setBaseValue(20);
            Objects.requireNonNull(player.getAttribute(Attribute.MOVEMENT_SPEED)).setBaseValue(0.1);
            Objects.requireNonNull(player.getAttribute(Attribute.BLOCK_BREAK_SPEED)).setBaseValue(1);
            Objects.requireNonNull(player.getAttribute(Attribute.ARMOR)).setBaseValue(0);
            Objects.requireNonNull(player.getAttribute(Attribute.ATTACK_KNOCKBACK)).setBaseValue(0);
        }
        for(Zombie zombie : Zombiedaygame.zombies){
            if(zombie.isValid()){
                zombie.remove();
            }
        }

        for(Entity entity : gameworld.getEntities()){
            if(entity instanceof Zombie||entity instanceof Wolf||entity instanceof Item || entity instanceof  IronGolem||entity instanceof Arrow){
                entity.remove();
            }
        }
        Zombiedaygame.players.clear();
        for(Block block : Zombiedaygame.obstacles){
            block.setType(Material.AIR);
        }
        areamanager.initarea(arealoc1);
        areamanager.initarea(arealoc2);
        areamanager.initarea(arealoc3);
        areamanager.initarea(arealoc4);
        Zombiedaygame.obstaclebreaktime.clear();
    }
}
