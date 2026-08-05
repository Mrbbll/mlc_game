package com.mlc.mlcgames.dungeongame.gamephase;

import com.mlc.mlcgames.dungeongame.Dungeongame;
import com.mlc.mlcgames.dungeongame.managers.Room;
import com.mlc.mlcgames.dungeongame.managers.RoomCombiner;
import com.mlc.mlcgames.dungeongame.managers.Worldmanager;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/** 地牢开始：创建新世界并把随机房间组合拼接进去。 */
public class start {
    public static void startgame(CommandSender sender) {
        if (Dungeongame.rooms.isEmpty()) {
            sender.sendMessage("[Dungeongame] 没有可用的房间，先框选房间: /dungeongame pos1/pos2 <房间名>");
            return;
        }
        if (Dungeongame.isstart) {
            sender.sendMessage("[Dungeongame] 正在生成地牢中，请稍候");
            return;
        }
        Dungeongame.isstart = true;
        try {
            String worldName = Dungeongame.world_prefix + "_" + System.currentTimeMillis();
            Worldmanager.unloadAndDelete(worldName); // 清理同名残留世界
            World world = Worldmanager.createWorld(worldName);
            Dungeongame.last_world = worldName;

            List<Room> pool = new ArrayList<>(Dungeongame.rooms);
            Room startRoom = Dungeongame.start_room != null ? Dungeongame.start_room : pool.get(0);
//            RoomCombiner.combine(world, pool, startRoom);

            // 出生点放在起点房间中心
            int sx = startRoom.getSizeX() / 2;
            int sz = startRoom.getSizeZ() / 2;
            Location spawn = new Location(world, sx, Dungeongame.floor_y + 2, sz);
            world.setSpawnLocation(spawn);

            if (sender instanceof Player p) {
                p.teleportAsync(spawn);
            }
            sender.sendMessage("[Dungeongame] 地牢已生成: " + worldName);
        } finally {
            Dungeongame.isstart = false;
        }
    }
}
