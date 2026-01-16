package com.mlc.mlcgames.bank.commmand;

import com.mlc.mlcgames.bank.utils.Bankgame;
import com.mlc.mlcgames.bank.utils.Gamemode;
import com.mlc.mlcgames.bank.utils.Gameprepare;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static com.mlc.mlcgames.Mlcgames.bankgame;

public class bankgameprepare implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if(bankgame.isStart||bankgame.gameprepared){
            sender.sendMessage(Component.text("游戏已准备或已开始，不能准备", NamedTextColor.RED));
            return false;
        }
        if(sender instanceof Player){
            if(args.length!=0){
                if(!sender.isOp()){
                    return false;
                }
                switch (args[0]){
                    case "0":
                        bankgame.gamemode= Gamemode.thiefvsthief;
                        break;
                    case "1":
                        bankgame.gamemode= Gamemode.thiefvspolice;
                        break;
                    default:
                        bankgame.gamemode= Gamemode.thiefvsthief;
                        break;
                }
                return false;
            }

            Player player = (Player) sender;
            player.setGameMode(GameMode.ADVENTURE);
            Gameprepare.prepare(player,bankgame.gamemode);
        }
        return false;
    }
}
