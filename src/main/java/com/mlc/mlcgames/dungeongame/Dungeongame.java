package com.mlc.mlcgames.dungeongame;

import com.mlc.mlcgames.dungeongame.rooms.*;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** 地牢游戏全局状态（模仿 sandgame 的 Sandgame 类）。 */
//schem文件保存命名规范：floor_set_type_variant.schem
//floor=关卡(1-3)，set=环境预设，type=房间类型，variant=同类型变种
//例如第一关、环境预设1、Shop房、第一个变种：1_1_shop_1.schem
//com.mlc.mlcgames.dungeongame.rooms.RoomType
//1_1_bridge_x_1.schem  # 东西方向
//1_1_bridge_z_1.schem  # 南北方向
public class Dungeongame {
    public static boolean isstart;
    public static List<NormalRoom> NormaRoom_List;
    public static List<SpecialRoom> SpecialRoom_List;
    public static List<StartRoom> StartRoom_List;
    public static List<EndRoom> EndRoom_List;
    public static List<BossRoom> BossRoom_List;
    public static List<BridgeRoom> BridgeRoom_List;

    public static World gameworld;
    public static int mapsize = 7;
    public static int specialRoomChance = 70;
    public static DungeonDifficulty difficulty = DungeonDifficulty.NORMAL;
    public static int currentLevel = 1;
    public static int currentFloor = 1;
    public static boolean lastGameWon;
    public static UUID lastWinner;
    public static final Set<Player> participants = new HashSet<>();

    public static Map<Player,Integer> player_life;
    public static Map<Player,Integer> player_money;
    public static Map<Player,Float> player_mana;

}
