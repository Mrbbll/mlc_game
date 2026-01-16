package com.mlc.mlcgames.bank.commmand;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import static com.mlc.mlcgames.Mlcgames.fileConfiguration;
import static com.mlc.mlcgames.Mlcgames.instance;

public class bankgamereload implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        instance.reloadConfig();
        fileConfiguration = instance.getConfig();

        return false;
    }
}
