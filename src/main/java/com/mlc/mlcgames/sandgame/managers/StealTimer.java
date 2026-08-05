package com.mlc.mlcgames.sandgame.managers;

import com.mlc.mlcgames.Teammanager;
import com.mlc.mlcgames.sandgame.Sandgame;
import com.mlc.mlcgames.sandgame.gamephase.end;
import com.mlc.mlcgames.sandgame.items.itemmanager;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

import static com.mlc.mlcgames.Mlcgames.instance;


public class StealTimer extends BukkitRunnable {
    Map<Player,Integer> steal_time_map = new HashMap<>();
    Player team_1_steal_player = null;
    Player team_2_steal_player = null;
    int remaintime_1 = 60;
    int remaintime_2 = 60;
    @Override
    public void run() {
        if(!Sandgame.isstart){
            this.cancel();
        }
        for(Player player : Sandgame.team_1_sand_loc.getNearbyPlayers(1.5)){
            if(!Teammanager.isPlayerInTeam(player,Teammanager.team_2)){
                return;
            }
            if(!player.getGameMode().equals(GameMode.ADVENTURE)){
                return;
            }
            if (player == team_1_steal_player){
                remaintime_1 = steal_time_map.getOrDefault(player,60)-1;
                if(remaintime_1 <= 0){
                    remaintime_1 = 60;
                    Sandgame.team_1_sand_count--;
                    player.give(itemmanager.sand);
                    instance.getServer().broadcast(Sandgame.team_1_sand_steal_msg);
                    if(Sandgame.team_1_sand_count==0){
                        end.endgame_with_winner(2);
                    }
                    break;
                }
                steal_time_map.put(player,remaintime_1);
            }else{
                remaintime_1 = 60-1;
                team_1_steal_player=player;
                steal_time_map.put(player,remaintime_1);
            }
        }
        for(Player player : Sandgame.team_2_sand_loc.getNearbyPlayers(1.5)){
            if(!Teammanager.isPlayerInTeam(player,Teammanager.team_1)){
                return;
            }
            if(!player.getGameMode().equals(GameMode.ADVENTURE)){
                return;
            }
            if (player == team_2_steal_player){
                remaintime_2 = steal_time_map.getOrDefault(player,60)-1;
                if(remaintime_2 <= 0){
                    remaintime_2 = 60;
                    Sandgame.team_2_sand_count--;
                    player.give(itemmanager.sand);
                    instance.getServer().broadcast(Sandgame.team_2_sand_steal_msg);
                    if(Sandgame.team_2_sand_count==0){
                        end.endgame_with_winner(1);
                    }
                    break;
                }
                steal_time_map.put(player,remaintime_2);
            }else{
                remaintime_2 = 60-1;
                team_2_steal_player=player;
                steal_time_map.put(player,remaintime_2);
            }
        }
    }
}
