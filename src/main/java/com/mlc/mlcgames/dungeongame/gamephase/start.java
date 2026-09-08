package com.mlc.mlcgames.dungeongame.gamephase;

import com.mlc.mlcgames.dungeongame.managers.DungeonGameManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class start {
    public static void startgame(CommandSender sender) {
        if (sender instanceof Player player) DungeonGameManager.get().startGame(player);
        else sender.sendMessage("Only a player can start the dungeon.");
    }

}
