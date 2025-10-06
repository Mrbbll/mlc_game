package com.mlc.mlcgames;

import com.mlc.mlcgames.bank.Gameprepare;
import com.mlc.mlcgames.bank.Gamestart;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import static com.mlc.mlcgames.Mlcgames.*;
import static com.mlc.mlcgames.Teammanager.team_1;
import static com.mlc.mlcgames.Teammanager.team_2;

public class mlcgame_bank implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if(args.length<1){
            Bukkit.broadcast(Component.text("false"));
            return false;
        }
        switch (args[0]){
            case "prepare":{
                Bukkit.broadcast(Component.text("prepare"));
                ingamepalyer.clear();
                new Gameprepare();
                break;
            }
            case "start":{
                isstart = true;
                new Gamestart();
                break;
            }
            case "gamemode":{
                if (isstart){
                    return false;
                }
                if (args[1].matches("^[-+]?\\d+$")){
                    gamemode = Integer.parseInt(args[1]);
                }
                break;
            }
            case "kit":{
                if(args[1].matches("^[-+]?\\d+$")){
                    gamemode = Integer.parseInt(args[1]);
                }
                break;
            }
        }
        return false;
    }
}
