package com.mlc.mlcgames.zombieday.gamephase;

import com.mlc.mlcgames.Teammanager;
import com.mlc.mlcgames.zombieday.Item;
import com.mlc.mlcgames.zombieday.Zombiedaygame;
import com.mlc.mlcgames.zombieday.listener.Obstacle;
import com.mlc.mlcgames.zombieday.managers.TurnManager;
import com.mlc.mlcgames.zombieday.managers.scoreboard;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.HashMap;

import static com.mlc.mlcgames.zombieday.Zombiedaygame.gameworld;
import static com.mlc.mlcgames.zombieday.Zombiedaygame.respawnloc;

public class Start {
    public static void start(){
        if(Zombiedaygame.isstart){
            return;
        }

        Zombiedaygame.players = Teammanager.getteamplayer(Teammanager.zombieday_team);

        for(Player player : Zombiedaygame.players){
            player.teleport(respawnloc);
            player.sendMessage("开始游戏");
            player.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE,20*99999,0));
            Item.giveitem(player);
            player.setExp(0);
            scoreboard.showsidebar(player);
        }
        for(Entity entity : gameworld.getEntities()){
            if(entity instanceof Zombie){
                entity.remove();
            }
        }
        Zombiedaygame.isstart = true;
        Zombiedaygame.turn = 0;
        Zombiedaygame.obstacles = new ArrayList<>();
        Zombiedaygame.obstaclebreaktime = new HashMap<>();
        Obstacle.ObstaclefixeventListener();
        Obstacle.ObstaclebreakeventListener(respawnloc);
        TurnManager.turncycle();
//        for(Location location : Zombiedaygame.fixlocs){
//            new Obstacle(location);
//        }
    }
}
