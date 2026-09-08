package com.mlc.mlcgames.dungeongame.listener;

import com.mlc.mlcgames.dungeongame.managers.DungeonGameManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public final class DungeonRoomListener implements Listener {
    private final DungeonGameManager gameManager;

    public DungeonRoomListener(DungeonGameManager gameManager) { this.gameManager = gameManager; }

    @EventHandler(ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        if (!event.hasChangedBlock()) return;
        gameManager.handlePlayerPosition(event.getPlayer());
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        gameManager.handleMonsterDeath(event.getEntity().getUniqueId());
    }
}
