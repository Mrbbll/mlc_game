package com.mlc.mlcgames.Listener;

import com.mlc.mlcgames.Gamesidebar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scoreboard.DisplaySlot;

import static com.mlc.mlcgames.Gamesidebar.sidebarscoreboard;
import static com.mlc.mlcgames.Mlcgames.bankgame;

public class gamelistener implements Listener {
    @EventHandler
    public void onplayerjoin(PlayerJoinEvent event){
        Player player = event.getPlayer();
        player.teleport(bankgame.lobbyLocation);
        Gamesidebar.showsidebar(player);
    }
}
