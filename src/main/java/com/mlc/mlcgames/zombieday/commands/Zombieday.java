package com.mlc.mlcgames.zombieday.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.mlc.mlcgames.zombieday.Item.itemlist;

public class Zombieday implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if(args.length == 0){
            sender.sendMessage("请输入命令参数");
            return false;
        }
        switch (args[0]){
            case "end":
                sender.sendMessage("结束游戏");
                break;
            case "reload":
                sender.sendMessage("重新加载配置");
                break;
            case "join":
                sender.sendMessage("加入游戏");
                break;
            case "start":
                sender.sendMessage("开始游戏");
                break;
            case "give":
                sender.sendMessage("调试");
                if(args.length < 2){
                    sender.sendMessage("请输入序号");
                    return false;
                }
                if(itemlist.size() <= Integer.parseInt(args[1])){
                    sender.sendMessage("序号超出范围");
                    return false;
                }
                ItemStack item = itemlist.get(Integer.parseInt(args[1]));
                Player player = (Player) sender;
                player.getInventory().addItem(item);
                break;
            default:
                sender.sendMessage("请输入正确的命令参数");
                break;
        }

        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {

        return List.of("end","reload","join","start","give");
    }
}
