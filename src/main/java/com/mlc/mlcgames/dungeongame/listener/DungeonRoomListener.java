package com.mlc.mlcgames.dungeongame.listener;

import com.mlc.mlcgames.dungeongame.managers.DungeonGameManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public final class DungeonRoomListener implements Listener {
    private final DungeonGameManager gameManager;

    public DungeonRoomListener(DungeonGameManager gameManager) { this.gameManager = gameManager; }

    @EventHandler(ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        // PlayerTeleportEvent 继承 PlayerMoveEvent。传送只代表位置被外部改变，不能当作
        // 玩家主动走进新房间，否则任意插件传送都可能触发刷怪并把整支队伍拉过去。
        if (event instanceof PlayerTeleportEvent) return;
        if (!event.hasChangedBlock()) return;
        gameManager.handlePlayerPosition(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event) {
        // MONITOR 阶段只记录已确定未取消的传送；下一 tick 再按最终世界同步显示状态。
        // 此入口绝不检查传送点或房间，因此不会触发刷怪和队伍集结。
        gameManager.handlePlayerTeleport(event.getPlayer());
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
