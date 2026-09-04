/**
 * 文件说明：战斗模块的主伤害管线。
 * 它解析攻击来源、武器类型和目标防御类型，计算相性倍率后修改 Bukkit 伤害事件；
 * CraftEngine 只负责数值属性，本类不重复实现 CraftEngine 的伤害公式。
 */
package com.mlc.mlcgames.combat;

import com.mlc.mlcgames.combat.config.CombatConfig;
import com.mlc.mlcgames.combat.entity.ArmorTypeService;
import com.mlc.mlcgames.combat.integration.craftengine.CraftEngineAttributeBridge;
import com.mlc.mlcgames.combat.weapon.AttackTypeService;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.projectiles.ProjectileSource;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** Contains the damage pipeline; listeners only hand Bukkit events to this service. */
public final class CombatService {
    private final CombatConfig config;
    private final ArmorTypeService armorTypeService;
    private final AttackTypeService attackTypeService;
    private final TypeEffectivenessService effectivenessService;
    private final CraftEngineAttributeBridge craftEngineAttributeBridge;
    private final Set<UUID> debugPlayers = new HashSet<>();

    public CombatService(CombatConfig config, ArmorTypeService armorTypeService, AttackTypeService attackTypeService,
                         TypeEffectivenessService effectivenessService, CraftEngineAttributeBridge craftEngineAttributeBridge) {
        this.config = config;
        this.armorTypeService = armorTypeService;
        this.attackTypeService = attackTypeService;
        this.effectivenessService = effectivenessService;
        this.craftEngineAttributeBridge = craftEngineAttributeBridge;
    }

    public void handle(EntityDamageByEntityEvent event) {
        if (!config.enabled() || event.isCancelled() || !(event.getEntity() instanceof LivingEntity victim)) return;
        AttackType attackType = resolveAttackType(event.getDamager());
        ArmorType armorType = armorTypeService.getArmorType(victim);
        DamageAffinity affinity = effectivenessService.resolve(attackType, armorType);
        double originalDamage = event.getDamage();
        double multiplier = effectivenessService.multiplier(affinity);
        event.setDamage(originalDamage * multiplier);
        sendDebug(event.getDamager(), victim, attackType, armorType, affinity, multiplier, originalDamage);
    }

    public AttackType resolveAttackType(Entity damager) {
        AttackType directType = attackTypeService.getAttackType(damager);
        if (damager instanceof Projectile && attackTypeService.hasAttackType(damager)) {
            return directType;
        }
        if (directType != AttackType.NONE) return directType;
        if (damager instanceof Projectile projectile && projectile.getShooter() instanceof LivingEntity shooter) {
            return attackTypeService.getAttackType(shooter.getEquipment().getItemInMainHand());
        }
        if (damager instanceof LivingEntity livingDamager) return attackTypeService.getAttackType(livingDamager.getEquipment().getItemInMainHand());
        return AttackType.NONE;
    }

    public void copyAttackTypeToProjectile(Projectile projectile) {
        ProjectileSource source = projectile.getShooter();
        if (source instanceof LivingEntity shooter) {
            attackTypeService.copyAttackTypeTo(projectile,
                    attackTypeService.getAttackType(shooter.getEquipment().getItemInMainHand()));
        }
    }

    public boolean toggleDebug(UUID playerId) {
        if (!debugPlayers.add(playerId)) {
            debugPlayers.remove(playerId);
            return false;
        }
        return true;
    }

    private void sendDebug(Entity damager, LivingEntity victim, AttackType attackType, ArmorType armorType,
                           DamageAffinity affinity, double multiplier, double originalDamage) {
        Entity attacker = damager instanceof Projectile projectile && projectile.getShooter() instanceof Entity shooter ? shooter : damager;
        if (attacker instanceof Player player && debugPlayers.contains(player.getUniqueId())) {
            String ceValues = "";
            if (attacker instanceof LivingEntity livingAttacker) {
                var attack = craftEngineAttributeBridge.getEntityValue(livingAttacker, CombatAttribute.ATTACK);
                var defense = craftEngineAttributeBridge.getEntityValue(victim, CombatAttribute.DEFENSE);
                if (attack.isPresent() || defense.isPresent()) {
                    ceValues = ", ceAttack=" + (attack.isPresent() ? attack.getAsDouble() : "unset")
                            + ", ceDefense=" + (defense.isPresent() ? defense.getAsDouble() : "unset");
                }
            }
            player.sendMessage("[MLC Combat] attacker=" + attacker.getName() + ", victim=" + victim.getName()
                    + ", attackType=" + attackType + ", armorType=" + armorType + ", affinity=" + affinity
                    + ", multiplier=" + multiplier + ", original=" + originalDamage + ", final=" + (originalDamage * multiplier)
                    + ceValues);
        }
    }
}
