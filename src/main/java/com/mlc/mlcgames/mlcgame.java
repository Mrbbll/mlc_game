package com.mlc.mlcgames;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import static com.mlc.mlcgames.Mlcgames.gamemode;
import static com.mlc.mlcgames.Mlcgames.isstart;

public class mlcgame implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if(args.length<=1){
            return false;
        }
        switch (args[0]){
            case "prepare":{
                new Gameprepare();
            }
            case "start":{
                isstart = true;
                new Gamestart();
            }
            case "gamemode":{
                if (isstart){
                    return false;
                }
                if (args[1].matches("^[-+]?\\d+$")){
                    gamemode = Integer.parseInt(args[1]);
                }
            }
            case "kit":{
                if(args[1].matches("^[-+]?\\d+$")){
                    gamemode = Integer.parseInt(args[1]);
                }
            }
        }
        return false;
    }
}
