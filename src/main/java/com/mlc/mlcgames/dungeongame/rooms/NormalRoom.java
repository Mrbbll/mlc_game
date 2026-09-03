package com.mlc.mlcgames.dungeongame.rooms;

import org.bukkit.Location;
import org.bukkit.World;

public class NormalRoom extends Room {
    public NormalRoom(RoomType roomType, String name, World sourceWorld, Location corner1, Location corner2, int id) {
        super(roomType,name, sourceWorld, corner1, corner2, id);
    }
}
