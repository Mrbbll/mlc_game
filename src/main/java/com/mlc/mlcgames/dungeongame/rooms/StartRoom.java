package com.mlc.mlcgames.dungeongame.rooms;

import org.bukkit.Location;
import org.bukkit.World;

public class StartRoom extends Room{
    public StartRoom(RoomType type, String name, World sourceWorld, Location corner1, Location corner2, int id) {
        super(type, name, sourceWorld, corner1, corner2, id);
    }
}
