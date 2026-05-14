package com.mlc.mlcgames.zombieday;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.*;

public class Zombiedaygame {
    public static boolean isstart;
    public static Set<Player> players;

    public static Location respawnloc;
    public static Set<Location> zombielocs;
    public static int turn;
    public static List<Block> obstacles = new ArrayList<>();
    public static Map<Block,Float> obstaclebreaktime = new HashMap<>();

    public static Difficuty difficuty;
}
