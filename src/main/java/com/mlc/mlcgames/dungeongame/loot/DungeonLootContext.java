package com.mlc.mlcgames.dungeongame.loot;

import com.mlc.mlcgames.dungeongame.rooms.RoomType;

/** Stable input contract for the future loot randomizer. */
public record DungeonLootContext(int dungeonLevel, int floor, RoomType roomType, int qualityTier) {
    public static DungeonLootContext of(int dungeonLevel, int floor, RoomType type) {
        int roomBonus = switch (type) {
            case Normal -> 1;
            case Add -> 2;
            case Elit -> 3;
            case Boss -> 5;
            case Shop -> 2;
            default -> 0;
        };
        return new DungeonLootContext(dungeonLevel, floor, type,
                (dungeonLevel - 1) * 50 + floor * 10 + roomBonus);
    }
}
