package com.mlc.mlcgames.bank.commmand;

import com.mlc.mlcgames.bank.utils.end.Endgame;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class bankgameend implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        Endgame.endgame();
        return false;
    }
}
