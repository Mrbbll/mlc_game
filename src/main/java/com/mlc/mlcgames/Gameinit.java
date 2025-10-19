package com.mlc.mlcgames;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import static com.mlc.mlcgames.Mlcgames.Bank_isstart;

public class Gameinit {


    public static int bank_gamemode;
    public static Location bank_1;
    public static Location bank_2;
    public static Location bank_3;
    public static Location bank_4;
    public static Location bank_5;
    public static Location bank_6;
    public static Location bank_lobby;
    public static Location bank_respawn;
    public Gameinit(){
        bank_1 = new Location(Bukkit.getWorld("world"),0,70,0);


        bank_lobby = new Location(Bukkit.getWorld("world"),0 , 70,0 );
        bank_respawn = new Location(Bukkit.getWorld("world"),0 , 70,0);
        bank_gamemode = 0;
        Bank_isstart = false;


    }
}
