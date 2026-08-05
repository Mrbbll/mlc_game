package com.mlc.mlcgames.dungeongame;

import com.mlc.mlcgames.dungeongame.rooms.NormalRoom;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

/** 地牢游戏全局状态（模仿 sandgame 的 Sandgame 类）。 */
public class Dungeongame {
    public static boolean isstart;
    public static List<NormalRoom> NormaRoom_List;
    public static World gameworld;


    public static Map<Player,Integer> player_life;
    public static Map<Player,Integer> player_money;
    public static Map<Player,Float> player_mana;

}
