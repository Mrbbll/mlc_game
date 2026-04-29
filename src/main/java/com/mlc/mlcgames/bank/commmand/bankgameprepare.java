package com.mlc.mlcgames.bank.commmand;

import com.mlc.mlcgames.bank.Bankgamesidebar;
import com.mlc.mlcgames.bank.utils.Gamemode;
import com.mlc.mlcgames.bank.utils.prepare.Gameprepare;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.mlc.mlcgames.Mlcgames.bankgame;

public class bankgameprepare implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if(!sender.isOp()){
            return false;
        }
        if(bankgame.isStart||bankgame.gameprepared){
            sender.sendMessage(Component.text("游戏已准备或已开始，不能准备", NamedTextColor.RED));
            return false;
        }
        for(Player player: Bukkit.getOnlinePlayers()){
            if(args.length!=0){
                switch (args[0]){
                    case "thiefvsthief":
                        bankgame.gamemode= Gamemode.thiefvsthief;
                        break;
                    case "thiefvspolice":
                        bankgame.gamemode= Gamemode.thiefvspolice;
                        break;
                    default:
                        if (bankgame.gamemode==Gamemode.thiefvspolice){
                            break;
                        }
                        else {
                            bankgame.gamemode= Gamemode.thiefvsthief;
                        }
                        break;
                }
            }
            Bankgamesidebar.showsidebar(player);
            player.setGameMode(GameMode.ADVENTURE);
            Gameprepare.prepare(player,bankgame.gamemode);
        }
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        return List.of("thiefvsthief","thiefvspolice");
    }
}
