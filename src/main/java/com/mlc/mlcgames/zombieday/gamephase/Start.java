package com.mlc.mlcgames.zombieday.gamephase;

import com.mlc.mlcgames.Teammanager;
import com.mlc.mlcgames.zombieday.Item;
import com.mlc.mlcgames.zombieday.Zombiedaygame;
import com.mlc.mlcgames.zombieday.inv.*;
import com.mlc.mlcgames.zombieday.listener.Obstacle;
import com.mlc.mlcgames.zombieday.managers.SpawnManager;
import com.mlc.mlcgames.zombieday.managers.TurnManager;
import com.mlc.mlcgames.zombieday.managers.areamanager;
import com.mlc.mlcgames.zombieday.managers.scoreboard;
import com.mlc.mlcgames.zombieday.zombie.ZombieLoot;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

import static com.mlc.mlcgames.Mlcgames.miniMessage;
import static com.mlc.mlcgames.zombieday.Zombiedaygame.*;

public class Start {
    public static void start(){
        if(Zombiedaygame.isstart){
            return;
        }

        Zombiedaygame.players = Teammanager.getteamplayer(Teammanager.zombieday_team);

        for(Player player : Zombiedaygame.players){
            player.teleport(respawnloc);
            player.setRespawnLocation(respawnloc,true);
            player.sendMessage(miniMessage.deserialize("<b><#ff0033>ZOMBIEDAY START..."));
            player.getInventory().clear();
            player.setHealth(20);
//            player.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE,20*99999,0));
            Item.giveitem(player);
            player.setExp(0);
            Objects.requireNonNull(player.getAttribute(Attribute.MAX_HEALTH)).setBaseValue(20);
            Objects.requireNonNull(player.getAttribute(Attribute.MOVEMENT_SPEED)).setBaseValue(0.1);
            Objects.requireNonNull(player.getAttribute(Attribute.BLOCK_BREAK_SPEED)).setBaseValue(1);
            Objects.requireNonNull(player.getAttribute(Attribute.ARMOR)).setBaseValue(0);
            Objects.requireNonNull(player.getAttribute(Attribute.ATTACK_KNOCKBACK)).setBaseValue(0);
            scoreboard.showsidebar(player);
        }
        for(Entity entity : gameworld.getEntities()){
            if(entity instanceof Zombie||entity instanceof Wolf ||
                    entity instanceof org.bukkit.entity.Item ||
                    entity instanceof IronGolem ||entity instanceof Arrow||
                    entity instanceof Silverfish||entity instanceof Slime){
                entity.remove();
            }
        }
        EffectInv.init();
        ZombieLoot.init();
        Zombiedaygame.zombies = new HashSet<>();
        Zombiedaygame.zombiecount=0;
        Zombiedaygame.isstart = true;
        Zombiedaygame.turn = 0;
        Zombiedaygame.obstacles = new ArrayList<>();
        Zombiedaygame.obstaclebreaktime.clear();
        Obstacle.ObstaclefixeventListener();
        Obstacle.ObstaclebreakeventListener(respawnloc);
        TurnManager.turncycle();
        SpawnManager.updatezombiecount();
        areamanager.initarea(arealoc1);
        areamanager.initarea(arealoc2);
        areamanager.initarea(arealoc3);
        areamanager.initarea(arealoc4);
        Zombiedaygame.requirekeynum =1;

        //初始化药水商店
        PotionInv.init();
        BulletInv.init();
        FoodInv.init();
        ArmorInv.init();
        EffectInv.init();

//        for(Location location : Zombiedaygame.fixlocs){
//            new Obstacle(location);
//        }
    }
}
