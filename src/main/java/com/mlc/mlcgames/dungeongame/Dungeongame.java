package com.mlc.mlcgames.dungeongame;

import com.mlc.mlcgames.dungeongame.managers.Room;

import java.util.ArrayList;
import java.util.List;

/** 地牢游戏全局状态（模仿 sandgame 的 Sandgame 类）。 */
public class Dungeongame {
    public static boolean isstart;
    public static boolean capture_entities;
    public static boolean random_rotation = true;
    public static int grid_size = 3;
    public static int floor_y = 60;
    public static String world_prefix = "dungeon";
    public static String last_world;
    public static Room start_room;
    public static List<Room> rooms = new ArrayList<>();
}
