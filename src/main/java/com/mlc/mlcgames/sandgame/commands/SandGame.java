package com.mlc.mlcgames.sandgame.commands;

import com.mlc.mlcgames.sandgame.gamephase.end;
import com.mlc.mlcgames.sandgame.gamephase.sandgameinit;
import com.mlc.mlcgames.sandgame.gamephase.start;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;

public class SandGame implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        if(strings.length==0){
            commandSender.sendMessage("Usage: /sandgame start/end/reload");
            return false;
        }
        if(strings[0].equals("start")){
            start.startgame();
        }
        if(strings[0].equals("end")){
            end.endgame();
        }
        if(strings[0].equals("reload")){
            try {
                sandgameinit.init();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        return List.of("start","end","reload");
    }
}
