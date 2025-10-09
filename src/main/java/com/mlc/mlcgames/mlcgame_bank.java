package com.mlc.mlcgames;

import com.mlc.mlcgames.bank.Bank_gameprepare;
import com.mlc.mlcgames.bank.Bank_gamestart;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static com.mlc.mlcgames.Mlcgames.*;
import static com.mlc.mlcgames.Gameinit.*;

public class mlcgame_bank implements CommandExecutor {
    public final Bank_gameprepare bankGameprepare = new Bank_gameprepare();
    public final Bank_gamestart bankGamestart = new Bank_gamestart();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if(args.length<1){
            Bukkit.broadcast(Component.text("false"));
            return false;
        }
        switch (args[0]){
            case "prepare":{

                Player player = (Player) sender;
                sender.sendMessage(player.getWorld().getName());
                Bukkit.broadcast(Component.text("prepare"));
                ingamepalyer.clear();
                bankGameprepare.start();
                break;
            }
            case "start":{
                isstart = true;
                bankGamestart.start();
                break;
            }
            case "gamemode":{
                if (isstart){
                    return false;
                }
                if (args[1].matches("^[-+]?\\d+$")){
                    bank_gamemode = Integer.parseInt(args[1]);
                }
                break;
            }
            case "kit":{
                if(args[1].matches("^[-+]?\\d+$")){
                    bank_gamemode = Integer.parseInt(args[1]);
                }
                break;
            }
        }
        return false;
    }
}
