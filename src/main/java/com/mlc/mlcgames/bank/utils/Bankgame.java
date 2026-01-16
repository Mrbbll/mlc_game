package com.mlc.mlcgames.bank.utils;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Bankgame {
    public int remainTime;
    public Gamemode gamemode;
    public List<Player> players;
    public boolean isStart;
    public boolean gameprepared;
    public int policescore;
    public int thiefscore;
    public String winnerteam;
    public Location bankgameLocation;
    public Location policeteamLocation;
    public Location thiefteamLocation;
    public Location spectatelocation;
    public Location lobbyLocation;
    public Map<Player, Jobs> playerJobs = new HashMap<>();
    public static BukkitTask gameendcountdown;
}
