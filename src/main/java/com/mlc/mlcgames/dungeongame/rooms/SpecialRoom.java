package com.mlc.mlcgames.dungeongame.rooms;

import org.bukkit.Location;
import org.bukkit.World;

public class SpecialRoom extends Room {
    public SpecialRoom(String name, World sourceWorld, Location corner1, Location corner2, int id) {
        super(name, sourceWorld, corner1, corner2, id);
    }
}
