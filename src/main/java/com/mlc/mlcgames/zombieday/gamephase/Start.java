package com.mlc.mlcgames.zombieday.gamephase;

import com.mlc.mlcgames.Teammanager;
import com.mlc.mlcgames.zombieday.Item;
import com.mlc.mlcgames.zombieday.Zombiedaygame;
import com.mlc.mlcgames.zombieday.listener.Obstacle;
import com.mlc.mlcgames.zombieday.managers.TurnManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;

import static com.mlc.mlcgames.zombieday.Zombiedaygame.respawnloc;

public class Start {
    public static void start(){
        Zombiedaygame.players = Teammanager.getteamplayer(Teammanager.zombieday_team);

        for(Player player : Zombiedaygame.players){
            player.teleport(respawnloc);
            player.sendMessage("开始游戏");
            player.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE,20*99999,0));
            Item.giveitem(player);
            player.setExp(0);
        }
        Zombiedaygame.isstart = true;
        Zombiedaygame.turn = 0;
        Zombiedaygame.obstacles = new ArrayList<>();
        Obstacle.ObstaclefixeventListener();
        Obstacle.ObstaclebreakeventListener(respawnloc);
        TurnManager.turncycle();
//        for(Location location : Zombiedaygame.fixlocs){
//            new Obstacle(location);
//        }
    }
}
