package com.mlc.mlcgames.dungeongame.listener;

import com.mlc.mlcgames.dungeongame.managers.DungeonGameManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public final class DungeonRoomListener implements Listener {
    private final DungeonGameManager gameManager;

    public DungeonRoomListener(DungeonGameManager gameManager) { this.gameManager = gameManager; }

    @EventHandler(ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        if (!event.hasChangedBlock()) return;
        gameManager.handlePlayerPosition(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeath(EntityDeathEvent event) {
        gameManager.handleMonsterDeath(event);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        gameManager.handlePlayerDeath(event);
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        gameManager.handlePlayerRespawn(event);
    }
}
