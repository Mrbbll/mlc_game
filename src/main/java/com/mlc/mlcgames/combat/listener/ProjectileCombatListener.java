/**
 * 文件说明：投射物发射监听器。
 * 发射时把当前武器的攻击类型写入投射物 PDC，保证玩家之后切换武器也不会改变该投射物属性。
 */
package com.mlc.mlcgames.combat.listener;

import com.mlc.mlcgames.combat.CombatService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;

/** Stores the firing weapon's attack type on each projectile at launch time. */
public final class ProjectileCombatListener implements Listener {
    private final CombatService combatService;
    public ProjectileCombatListener(CombatService combatService) { this.combatService = combatService; }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onProjectileLaunch(ProjectileLaunchEvent event) { combatService.copyAttackTypeToProjectile(event.getEntity()); }
}
