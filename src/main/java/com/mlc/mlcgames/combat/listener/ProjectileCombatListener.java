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
