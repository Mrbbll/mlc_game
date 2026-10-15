package com.mlc.mlcgames.party.survival.repel;

import com.mlc.mlcgames.party.core.*;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.*;

/** Source: repel/start, givethings, main (including portal-frame launch pads). */
public final class RepelGame implements PartyGame {
    private final Map<UUID, Integer> hitAt = new HashMap<>();
    @Override public void validate(PartyConfig config, PartyConfig.Arena arena) {
        config.integer("weapon-delay-seconds", 0, 60);
        config.integer("arrows", 1, 64);
        config.integer("hit-cooldown-ticks", 1, 40);
        config.number("knockback.horizontal", 0.1, 4);
        config.number("knockback.vertical", 0.1, 2);
        arena.region("launch-pad");
        config.integer("launch-pad.duration-ticks", 1, 200);
        config.integer("launch-pad.amplifier", 0, 30);
    }
    @Override public void start(PartySession session) {}
    @Override public void tick(PartySession session, int elapsed) {
        int delay = session.config.integer("weapon-delay-seconds", 0, 60) * 20;
        if (elapsed == Math.max(1, delay)) {
            for (Player player : session.survivors()) {
                ItemStack stick = session.item(Material.STICK, "items.stick");
                stick.addUnsafeEnchantment(Enchantment.KNOCKBACK, 1);
                player.getInventory().setItem(0, stick);
                ItemStack bow = session.item(Material.BOW, "items.bow");
                bow.addUnsafeEnchantment(Enchantment.PUNCH, 1);
                player.getInventory().setItem(1, bow);
                player.getInventory().setItem(8, new ItemStack(Material.ARROW, session.config.integer("arrows", 1, 64)));
            }
            session.broadcast("weapons-ready");
        }
        for (Player player : session.survivors()) {
            org.bukkit.Location feet = player.getLocation().subtract(0, 0.2, 0);
            boolean launch = session.arena.regions().entrySet().stream().anyMatch(e -> e.getKey().startsWith("launch-pad") && e.getValue().contains(feet.getBlockX(), feet.getBlockY(), feet.getBlockZ()));
            if (launch) player.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, session.config.integer("launch-pad.duration-ticks", 1, 200), session.config.integer("launch-pad.amplifier", 0, 30), true, false));
        }
    }
    @Override public void hit(PartySession session, Player attacker, Player victim, EntityDamageByEntityEvent event) {
        if (session.elapsed() < session.config.integer("weapon-delay-seconds", 0, 60) * 20) return;
        if (!(event.getDamager() instanceof Projectile) && attacker.getInventory().getItemInMainHand().getType() != Material.STICK) return;
        if (session.elapsed() - hitAt.getOrDefault(victim.getUniqueId(), -100) < session.config.integer("hit-cooldown-ticks", 1, 40)) return;
        hitAt.put(victim.getUniqueId(), session.elapsed());
        Vector direction = victim.getLocation().toVector().subtract(attacker.getLocation().toVector()).setY(0);
        if (direction.lengthSquared() < 0.001) direction = attacker.getLocation().getDirection().setY(0);
        if (direction.lengthSquared() < 0.001) direction.setX(1);
        victim.setVelocity(direction.normalize().multiply(session.config.number("knockback.horizontal", 0.1, 4)).setY(session.config.number("knockback.vertical", 0.1, 2)));
    }
}
