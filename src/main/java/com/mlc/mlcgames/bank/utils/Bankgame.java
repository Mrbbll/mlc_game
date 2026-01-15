package com.mlc.mlcgames.bank.utils;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public class Bankgame {
    public int remainTime;
    public List<Player> players;
    public boolean isStart;
    public String winnerteam;
    public Location bankgameLocation;
    public Location pliceteamLocation;
    public Location theifteamLocation;
    public Map<Player, String> playerJobs;
}
