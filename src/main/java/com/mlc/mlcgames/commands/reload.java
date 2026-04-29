package com.mlc.mlcgames.commands;

import com.mlc.mlcgames.Gamesidebar;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;


import static com.mlc.mlcgames.Mlcgames.fileConfiguration;
import static com.mlc.mlcgames.Mlcgames.instance;

//没写好
public class reload implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        instance.reloadConfig();
        fileConfiguration = instance.getConfig();
        for(Player player : instance.getServer().getOnlinePlayers()){
            Gamesidebar.showsidebar(player);
        }
        return false;
    }
}
