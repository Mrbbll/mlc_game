package com.mlc.mlcgames.zombieday.managers;

import org.bukkit.Location;

public class SpawnPoint {
    public Location location;
    public boolean islock = true;
    public SpawnPoint(Location location){
        this.location = location;
    }

    public boolean islocked(){
        return islock;
    }

    public static  void Spawnzombie(int turn){



    }
}
