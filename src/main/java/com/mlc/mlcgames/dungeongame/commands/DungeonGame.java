package com.mlc.mlcgames.dungeongame.commands;

import com.mlc.mlcgames.Mlcgames;
import com.mlc.mlcgames.dungeongame.Dungeongame;
import com.mlc.mlcgames.dungeongame.gamephase.dungeongameinit;
import com.mlc.mlcgames.dungeongame.gamephase.start;
import com.mlc.mlcgames.dungeongame.managers.Room;
import com.mlc.mlcgames.dungeongame.managers.Worldmanager;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DungeonGame implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] args) {
        if (args.length == 0) {
            sender.sendMessage("用法: /dungeongame start|list|pos1|pos2|setstart|reload|clean");
            return false;
        }
        switch (args[0].toLowerCase()) {
            case "start" -> start.startgame(sender);
            case "list" -> listRooms(sender);
            case "pos1" -> setCorner(sender, args, 1);
            case "pos2" -> setCorner(sender, args, 2);
            case "setstart" -> setStart(sender, args);
            case "reload" -> reload(sender);
            case "clean" -> clean(sender);
            default -> sender.sendMessage("未知子命令: " + args[0]);
        }
        return false;
    }

    private void listRooms(CommandSender sender) {
        if (Dungeongame.rooms.isEmpty()) {
            sender.sendMessage("[Dungeongame] 还没有房间，先用 /dungeongame pos1/pos2 <房间名> 框选");
            return;
        }
        for (Room room : Dungeongame.rooms) {
            String mark = (room == Dungeongame.start_room) ? "  <-- 出生房间" : "";
            sender.sendMessage(room.getName() + "  " + room.getSourceWorld().getName()
                    + "  " + room.getSizeX() + "x" + room.getSizeY() + "x" + room.getSizeZ() + mark);
        }
    }

    private void setCorner(CommandSender sender, String[] args, int corner) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage("该命令只能由玩家执行");
            return;
        }
        if (args.length < 2) {
            sender.sendMessage("用法: /dungeongame pos" + corner + " <房间名>");
            return;
        }
        String name = args[1];
        Block block = p.getTargetBlockExact(6);
        if (block == null) {
            sender.sendMessage("请对准一个方块");
            return;
        }
        Mlcgames.dungeonConfiguration.set("rooms." + name + ".world", p.getWorld().getName());
        Mlcgames.dungeonConfiguration.set("rooms." + name + ".corner_" + corner,
                block.getX() + "," + block.getY() + "," + block.getZ());
        Mlcgames.configManager.saveConfig("dungeongame.yml");
        sender.sendMessage("[Dungeongame] 房间 " + name + " 的 corner_" + corner
                + " 已设为 " + block.getX() + "," + block.getY() + "," + block.getZ());
    }

    private void setStart(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("用法: /dungeongame setstart <房间名>");
            return;
        }
        Mlcgames.dungeonConfiguration.set("start_room", args[1]);
        Mlcgames.configManager.saveConfig("dungeongame.yml");
        dungeongameinit.init();
        sender.sendMessage("[Dungeongame] start_room 已设为 " + args[1]);
    }

    private void reload(CommandSender sender) {
        Mlcgames.configManager.reloadConfig("dungeongame.yml");
        dungeongameinit.init();
        sender.sendMessage("[Dungeongame] 配置已重载，当前 " + Dungeongame.rooms.size() + " 个房间");
    }

    private void clean(CommandSender sender) {
        if (Dungeongame.last_world == null) {
            sender.sendMessage("[Dungeongame] 还没有生成过地牢");
            return;
        }
        Worldmanager.unloadAndDelete(Dungeongame.last_world);
        sender.sendMessage("[Dungeongame] 已清理 " + Dungeongame.last_world);
        Dungeongame.last_world = null;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] args) {
        if (args.length == 1) {
            return List.of("start", "list", "pos1", "pos2", "setstart", "reload", "clean");
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("pos1")
                || args[0].equalsIgnoreCase("pos2")
                || args[0].equalsIgnoreCase("setstart"))) {
            List<String> names = new ArrayList<>();
            for (Room room : Dungeongame.rooms) names.add(room.getName());
            return names;
        }
        return List.of();
    }
}
