package com.mlc.mlcgames.bank;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import static com.mlc.mlcgames.Mlcgames.worlds;

public class Gameinit {
    public static Location bank_1;
    public static Location bank_2;
    public static Location bank_3;
    public static Location bank_4;
    public static Location bank_5;
    public static Location bank_6;
    public static Location bank_lobby;

    public Gameinit(){
        bank_1 = new Location(worlds.getFirst(),0,70,0);

    }
}
