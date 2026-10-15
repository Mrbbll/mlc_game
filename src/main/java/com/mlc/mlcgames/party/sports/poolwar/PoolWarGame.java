package com.mlc.mlcgames.party.sports.poolwar;

import com.mlc.mlcgames.party.core.*;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import java.util.*;

/** Source: map_all poolwar. Stamina is measured in server ticks, not oxygen bubbles. */
public final class PoolWarGame implements PartyGame {
    private final Map<UUID, PoolStamina> stamina = new HashMap<>();
    private final Map<UUID, Integer> lastHit = new HashMap<>();
    private final Set<Block> frozen = new HashSet<>();
    private Cuboid pool;

    @Override public void validate(PartyConfig config, PartyConfig.Arena arena) {
        pool = arena.region("pool");
        config.integer("stamina.initial", 1, 10000);
        int minimum = config.integer("stamina.minimum-capacity", 1, 10000);
        if (minimum > config.integer("stamina.initial", 1, 10000)) throw new IllegalArgumentException("最低体力上限不能高于初始体力");
        config.integer("stamina.capacity-loss-per-second", 1, minimum);
        config.integer("hit-cooldown-ticks", 1, 100);
        config.number("knockback.horizontal", 0.1, 4);
        config.number("knockback.vertical", 0, 2);
        for (String rank : List.of("first", "second", "third")) config.integer("rank-points." + rank, 0, 100);
    }
    @Override public List<Cuboid> writableRegions(PartyConfig.Arena arena) { return List.of(arena.region("pool")); }
    @Override public void start(PartySession session) {
        for (Player player : session.survivors()) {
            stamina.put(player.getUniqueId(), new PoolStamina(session.config.integer("stamina.initial", 1, 10000)));
            ItemStack boots = session.item(Material.LEATHER_BOOTS, "items.boots");
            boots.addUnsafeEnchantment(Enchantment.FROST_WALKER, 1);
            boots.addUnsafeEnchantment(Enchantment.BINDING_CURSE, 1);
            LeatherArmorMeta meta = (LeatherArmorMeta) boots.getItemMeta();
            meta.setColor(org.bukkit.Color.fromRGB(1105614));
            boots.setItemMeta(meta);
            player.getInventory().setBoots(boots);
            ItemStack stick = session.item(Material.STICK, "items.stick");
            stick.addUnsafeEnchantment(Enchantment.KNOCKBACK, 2);
            player.getInventory().setItem(0, stick);
        }
    }
    @Override public void tick(PartySession session, int elapsed) {
        for (Player player : List.copyOf(session.survivors())) {
            Block feet = player.getLocation().getBlock();
            boolean water = feet.getType() == Material.WATER;
            boolean resting = feet.getType() == Material.AIR && feet.getRelative(0, 1, 0).getType() == Material.AIR;
            PoolStamina value = stamina.get(player.getUniqueId());
            value.tick(water, resting, elapsed % 20 == 0,
                    session.config.integer("stamina.minimum-capacity", 1, 10000),
                    session.config.integer("stamina.capacity-loss-per-second", 1, 10000));
            if (water && value.remaining() <= 60 || resting && value.remaining() <= 80)
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 20, water ? 3 : value.remaining() <= 50 ? 2 : 1, true, false));
            if (elapsed % 5 == 0) player.sendActionBar(session.config.message(water ? "swimming" : resting && value.remaining() < value.capacity() ? "resting" : "healthy",
                    "stamina", value.remaining(), "capacity", value.capacity()));
            if (water && value.remaining() <= 0) session.eliminate(player);
        }
    }
    @Override public boolean formBlock(PartySession session, Player player, Block block, Material material) {
        if (!pool.contains(block.getX(), block.getY(), block.getZ()) || block.getType() != Material.WATER || material != Material.FROSTED_ICE) return false;
        frozen.add(block);
        return true;
    }
    @Override public boolean fadeBlock(PartySession session, Block block, Material material) {
        if (block.getType() != Material.FROSTED_ICE || !frozen.contains(block)) return false;
        if (material == Material.WATER) { frozen.remove(block); return true; }
        return material == Material.FROSTED_ICE; // Vanilla advances the age before melting.
    }
    @Override public void hit(PartySession session, Player attacker, Player victim, EntityDamageByEntityEvent event) {
        if (event.getDamager() != attacker || attacker.getInventory().getItemInMainHand().getType() != Material.STICK) return;
        int now = session.elapsed();
        if (now - lastHit.getOrDefault(victim.getUniqueId(), -1000) < session.config.integer("hit-cooldown-ticks", 1, 100)) return;
        lastHit.put(victim.getUniqueId(), now);
        Vector direction = attacker.getLocation().getDirection().setY(0);
        if (direction.lengthSquared() > 0.001) victim.setVelocity(direction.normalize().multiply(session.config.number("knockback.horizontal", 0.1, 4))
                .setY(session.config.number("knockback.vertical", 0, 2)));
    }
    @Override public void eliminated(PartySession session, Player player) {
        int left = session.survivors().size();
        if (left <= 2) session.points(player, session.config.integer("rank-points." + (left == 0 ? "first" : left == 1 ? "second" : "third"), 0, 100));
        if (left == 1) session.points(session.survivors().getFirst(), session.config.integer("rank-points.first", 0, 100));
    }
}
