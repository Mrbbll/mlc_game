package com.mlc.mlcgames.dungeongame.rooms;

import org.bukkit.Location;
import org.bukkit.World;

public class BridgeRoom extends SpecialRoom {
    public BridgeRoom(String name, World sourceWorld, Location corner1, Location corner2, int id) {
        super(name, sourceWorld, corner1, corner2, id);
    }
}
