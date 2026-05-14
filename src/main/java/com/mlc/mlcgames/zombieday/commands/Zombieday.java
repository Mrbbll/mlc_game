package com.mlc.mlcgames.zombieday.commands;

import com.mlc.mlcgames.zombieday.Difficuty;
import com.mlc.mlcgames.zombieday.Zombiedaygame;
import com.mlc.mlcgames.zombieday.gamephase.End;
import com.mlc.mlcgames.zombieday.gamephase.Start;
import com.mlc.mlcgames.zombieday.zombie.Spwaner;
import com.mlc.mlcgames.zombieday.zombie.Zombietype;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.mlc.mlcgames.Mlcgames.miniMessage;
import static com.mlc.mlcgames.Mlcgames.server;
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
                End.end();
                sender.sendMessage("结束游戏");
                break;
            case "reload":
                sender.sendMessage("重新加载配置");
                break;
            case "difficuty":
                sender.sendMessage("设置难度等级");
                if(args.length < 2){
                    sender.sendMessage("请输入难度等级");
                    return false;
                }
                Zombiedaygame.difficuty = Difficuty.valueOf(args[1]);
                server.broadcast(miniMessage.deserialize("难度等级已设置为<b> " + Zombiedaygame.difficuty.withcolor()));
                break;
            case "spawn":
                Player player = (Player) sender;
                sender.sendMessage("生成测试僵尸");
                if(args.length < 2){
                    sender.sendMessage("请输入僵尸类型");
                    return false;
                }
                if(Zombietype.values().length <= Integer.parseInt(args[1])){
                    sender.sendMessage("请输入正确的僵尸类型");
                    return false;
                }
                Zombietype type = Zombietype.valueOf(args[1]);
                Spwaner.spawnzombie(player.getLocation(),1,5,20,1, type);
                break;
            case "start":
                Start.start();
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
                Player player1 = (Player) sender;
                player1.getInventory().addItem(item);
                break;
            default:
                sender.sendMessage("请输入正确的命令参数");
                break;
        }

        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {

        return List.of("end","reload","join","start","give","difficuty","spawn");
    }
}
