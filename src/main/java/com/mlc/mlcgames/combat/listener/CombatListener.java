package com.mlc.mlcgames.combat.listener;

import com.mlc.mlcgames.combat.CombatService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/** Thin Bukkit event adapter for the combat service. */
public final class CombatListener implements Listener {
    private final CombatService combatService;
    public CombatListener(CombatService combatService) { this.combatService = combatService; }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) { combatService.handle(event); }
}
