package com.mlc.mlcgames.zombieday.gamephase;

import com.mlc.mlcgames.Teammanager;
import com.mlc.mlcgames.zombieday.Zombiedaygame;
import com.mlc.mlcgames.zombieday.listener.Obstacle;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import static com.mlc.mlcgames.zombieday.Zombiedaygame.respawnloc;

public class Start {
    public static void start(){
        Zombiedaygame.players = Teammanager.getteamplayer(Teammanager.zombieday_team);

        for(Player player : Zombiedaygame.players){
            player.teleport(respawnloc);
            player.sendMessage("开始游戏");
        }
        Zombiedaygame.isstart = true;

        for(Location location : Zombiedaygame.fixlocs){
            new Obstacle(location);
        }
    }
}
