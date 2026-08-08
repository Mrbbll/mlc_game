package com.mlc.mlcgames.sandgame;

import com.mlc.mlcgames.Teammanager;
import com.mlc.mlcgames.sandgame.listener.Sand_Game_Listener;
import com.mlc.mlcgames.sandgame.managers.StealTimer;
import com.mlc.mlcgames.sandgame.managers.Timer;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.*;

public class Sandgame {
    public static boolean isstart;
    public static int countdown = 0;
    public static Timer timer;
    public static StealTimer stealtimer;
    public static int team_1_sand_count = 0;
    public static int team_2_sand_count = 0;
    public static Location team_1_loc;
    public static Location team_2_loc;
    public static Location ready_loc;
    public static Location sand_spawn_loc;
    public static Location item_spawn_loc_1;
    public static Location item_spawn_loc_2;
    public static Location team_1_sand_loc;
    public static Location team_2_sand_loc;
    public static Location money_spawn_loc_1;
    public static Location money_spawn_loc_2;
    public static Location money_spawn_loc_3;
    public static Location money_spawn_loc_4;

    public static Map<Player,Integer> player_kill_count = new HashMap<>();
    public static Map<Player,Integer> player_money = new HashMap<>();
    public static Component start_msg;
    public static Component end_msg_1;
    public static Component end_msg_2;
    public static Component end_msg_3;
    public static Component shop_success_msg;
    public static Component shop_fail_msg;

    public static Component sand_spawn_msg;
    public static Component team_join_msg_1;
    public static Component team_join_msg_2;
    public static Component team_1_sand_steal_msg;
    public static Component team_2_sand_steal_msg;
    public static Component team_1_sand_bring_msg;
    public static Component team_2_sand_bring_msg;

    public static List<Block> player_placed_blocks = new ArrayList<>();

    public static Sand_Game_Listener sandGameListener = new Sand_Game_Listener();

    public static boolean isSandgamePlayer(Player player){
        return Teammanager.isPlayerInTeam(player, Teammanager.sandgame_team_1)
                || Teammanager.isPlayerInTeam(player, Teammanager.sandgame_team_2)
                || Teammanager.isPlayerInTeam(player, Teammanager.sandgame_prepareteam);
    }
    public static Set<Player> getSandgamePlayer(){
        Set<Player> players = new HashSet<>();
        players.addAll(Teammanager.getteamplayer(Teammanager.sandgame_team_1));
        players.addAll(Teammanager.getteamplayer(Teammanager.sandgame_team_2));
        return players;
    }
    public static Player getTopKiller() {
        Player top = null;
        int max = 0;
        for (Map.Entry<Player, Integer> entry : player_kill_count.entrySet()) {
            if (entry.getValue() > max) {
                max = entry.getValue();
                top = entry.getKey();
            }
        }
        return top;
    }
}
