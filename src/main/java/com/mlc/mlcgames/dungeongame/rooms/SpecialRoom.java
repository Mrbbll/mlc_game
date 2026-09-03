package com.mlc.mlcgames.dungeongame.rooms;

import org.bukkit.Location;
import org.bukkit.World;

public class SpecialRoom extends Room {
    public SpecialRoom(RoomType roomType, String name, World sourceWorld, Location corner1, Location corner2, int id) {
        super(roomType, name, sourceWorld, corner1, corner2, id);
    }
}
