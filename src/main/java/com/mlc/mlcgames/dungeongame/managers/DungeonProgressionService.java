package com.mlc.mlcgames.dungeongame.managers;

import com.mlc.mlcgames.dungeongame.Dungeongame;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

/**
 * 建立并执行“当前层 → 下一层 → 最终胜利”的推进链。
 *
 * <p>传送服务只处理关卡关系与玩家位置，不了解怪物如何生成；战斗服务在清房时仅需调用
 * {@link #activate(DungeonSession.ProgressionPortal)}。</p>
 */
final class DungeonProgressionService {
    interface Listener {
        void onStageEntered(Player player);
        void onVictory(Player player);
    }

    private static final double PORTAL_RADIUS_SQUARED = 2.25 * 2.25;
    private final DungeonSession session;
    private final Listener listener;

    DungeonProgressionService(DungeonSession session, Listener listener) {
        this.session = session;
        this.listener = listener;
    }

    /**
     * 所有布局完成后一次性串起十五层。没有战斗标记的 End 房会立即开放出口；
     * 有遭遇的终点房则把出口引用交给战斗服务，在清房时解锁。
     */
    void initializePortals() {
        session.portals.clear();
        for (int index = 0; index < session.stages.size(); index++) {
            DungeonSession.Stage source = session.stages.get(index);
            DungeonSession.Stage target = index + 1 < session.stages.size()
                    ? session.stages.get(index + 1) : null;
            DungeonSession.ProgressionPortal portal = new DungeonSession.ProgressionPortal(source, target);
            source.portal = portal;
            session.portals.add(portal);
            if (source.terminalEncounter != null) {
                source.terminalEncounter.progressionPortal = portal;
            } else {
                activate(portal);
            }
        }
    }

    /** 若玩家触发了一个已开放出口则返回 true，让调用方停止本次房间检测。 */
    boolean handlePlayerPosition(Player player) {
        for (DungeonSession.ProgressionPortal portal : session.portals) {
            if (portal.active && isNear(player.getLocation(), portal.location)) {
                use(player, portal);
                return true;
            }
        }
        return false;
    }

    /** 幂等解锁：重复的死亡事件或清理调用不会重复修改方块和状态。 */
    void activate(DungeonSession.ProgressionPortal portal) {
        if (portal == null || portal.active) return;
        Location marker = portal.source.portalMarker.clone();
        Material material = DungeonConfiguration.material(
                "progression.portal-block", Material.RESPAWN_ANCHOR);
        if (material.isAir()) material = Material.RESPAWN_ANCHOR;
        marker.getBlock().setType(material, false);
        portal.location = marker.add(0.5, 0.5, 0.5);
        portal.active = true;
    }

    private void use(Player player, DungeonSession.ProgressionPortal portal) {
        if (portal.victory) {
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0F, 1.0F);
            listener.onVictory(player);
            return;
        }

        DungeonSession.Stage target = portal.target;
        Dungeongame.currentLevel = target.dungeonLevel;
        Dungeongame.currentFloor = target.floor;
        player.teleport(target.start);
        player.sendMessage("§b进入地牢第 " + target.dungeonLevel + " 关，第 " + target.floor + " 层。");
        listener.onStageEntered(player);
    }

    private static boolean isNear(Location player, Location portal) {
        if (portal == null || player.getWorld() != portal.getWorld()) return false;
        double dx = player.getX() - portal.getX();
        double dz = player.getZ() - portal.getZ();
        return dx * dx + dz * dz <= PORTAL_RADIUS_SQUARED
                && Math.abs(player.getY() - portal.getY()) <= 2.5;
    }
}
