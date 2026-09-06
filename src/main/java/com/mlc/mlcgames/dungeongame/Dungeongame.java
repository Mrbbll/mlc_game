package com.mlc.mlcgames.dungeongame;

import com.mlc.mlcgames.dungeongame.rooms.*;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

/** 地牢游戏全局状态（模仿 sandgame 的 Sandgame 类）。 */
//schem文件保存命名规范：floor_set_type_num.schem
//例如第一层，第一类型地牢，特殊房间Shop，第一个变体：1_1_shop_1
//com.mlc.mlcgames.dungeongame.rooms.RoomType
public class Dungeongame {
    public static boolean isstart;
    public static List<NormalRoom> NormaRoom_List;
    public static List<SpecialRoom> SpecialRoom_List;
    public static List<StartRoom> StartRoom_List;
    public static List<EndRoom> EndRoom_List;
    public static List<BossRoom> BossRoom_List;
    public static List<BridgeRoom> BridgeRoom_List;

    public static World gameworld;
    public static int mapsize = 4;
    public static int difficulty = 1;

    public static Map<Player,Integer> player_life;
    public static Map<Player,Integer> player_money;
    public static Map<Player,Float> player_mana;

}
