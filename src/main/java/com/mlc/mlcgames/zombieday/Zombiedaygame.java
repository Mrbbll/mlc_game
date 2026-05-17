package com.mlc.mlcgames.zombieday;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.*;

import static com.mlc.mlcgames.Mlcgames.instance;

public class Zombiedaygame {
    public static boolean isstart;
    public static int countdown=120;
    public static Set<Player> players;
    public static World gameworld = instance.getServer().getWorld("world");
    public static Location respawnloc;
    public static Location zombieloc1;
    public static Location zombieloc2;
    public static List<Location> zombieskylocs;
    public static int turn;
    public static List<Block> obstacles = new ArrayList<>();
    public static Map<Block,Float> obstaclebreaktime = new HashMap<>();
    public static int zombiecount;
    public static Difficulty difficulty = Difficulty.NORMAL;
}
