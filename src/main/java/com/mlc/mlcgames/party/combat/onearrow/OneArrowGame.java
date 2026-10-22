package com.mlc.mlcgames.party.combat.onearrow;

import com.mlc.mlcgames.party.core.PartyConfig;
import com.mlc.mlcgames.party.core.PartyGame;
import com.mlc.mlcgames.party.core.PartySession;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/** Source one_arrow: lethal shots, temporary stealth, timed random drops and events. */
public final class OneArrowGame implements PartyGame {
    private static final NamespacedKey ITEM_KEY = NamespacedKey.fromString("mlcgames:party_one_arrow_item");
    private static final NamespacedKey WEAPON_KEY = NamespacedKey.fromString("mlcgames:party_one_arrow_weapon");
    private final Map<UUID, Integer> ranks = new HashMap<>();
    private final Map<UUID, Integer> forcedInvisibleUntil = new HashMap<>();
    private final Set<UUID> sneakInvisible = new HashSet<>();
    private int nextDrop;
    private int nextEvent;

    @Override public void validate(PartyConfig config, PartyConfig.Arena arena) {
        config.integer("max-seconds", 10, 3600);
        config.integer("tool-drop-interval-seconds", 1, 120);
        config.integer("random-event-interval-seconds", 1, 120);
        config.integer("random-drop-lifetime-seconds", 1, 600);
        arena.group("tool-drops");
    }

    @Override public int maximumDurationSeconds() { return 3600; }

    @Override public void start(PartySession session) {
        ranks.clear();
        forcedInvisibleUntil.clear();
        sneakInvisible.clear();
        nextDrop = session.config.integer("tool-drop-interval-seconds", 1, 120) * 20;
        nextEvent = session.config.integer("random-event-interval-seconds", 1, 120) * 20;
        for (Player player : session.survivors()) {
            player.getInventory().setItem(0, named(session, Material.BOW, "items.bow"));
            player.getInventory().setItem(8, named(session, Material.ARROW, "items.arrow"));
        }
    }

    @Override public void tick(PartySession session, int elapsed) {
        for (Player player : session.survivors()) {
            ItemStack arrows = player.getInventory().getItem(8);
            if (arrows == null || arrows.getType() != Material.ARROW || arrows.getAmount() == 0)
                player.getInventory().setItem(8, named(session, Material.ARROW, "items.arrow"));
            UUID id = player.getUniqueId();
            if (player.isSneaking()) {
                sneakInvisible.add(id);
                player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 5, 0, true, false, false));
            } else if (sneakInvisible.remove(id) && forcedInvisibleUntil.getOrDefault(id, 0) <= elapsed) {
                player.removePotionEffect(PotionEffectType.INVISIBILITY);
            }
            if (elapsed % 20 == 0) player.sendActionBar(session.config.message("status", "seconds", Math.max(0, session.config.integer("max-seconds", 10, 3600) - elapsed / 20), "players", session.survivors().size()));
        }
        if (elapsed >= nextDrop) {
            spawnDrop(session);
            nextDrop = elapsed + session.config.integer("tool-drop-interval-seconds", 1, 120) * 20;
        }
        if (elapsed >= nextEvent) {
            randomEvent(session, elapsed);
            nextEvent = elapsed + session.config.integer("random-event-interval-seconds", 1, 120) * 20;
        }
        int lifetime = session.config.integer("random-drop-lifetime-seconds", 1, 600) * 20;
        for (Item item : session.arena.world().getEntitiesByClass(Item.class))
            if (session.owns(item) && item.getTicksLived() > lifetime) item.remove();
    }

    private ItemStack named(PartySession session, Material material, String key) {
        ItemStack item = session.item(material, key);
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(ITEM_KEY, PersistentDataType.STRING, "starter");
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack loot(PartySession session, Material material, String label) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(session.config.message(label));
        meta.setUnbreakable(true);
        meta.getPersistentDataContainer().set(ITEM_KEY, PersistentDataType.STRING, label);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack potion(PartySession session, String label, PotionEffect... effects) {
        ItemStack item = loot(session, Material.POTION, label);
        PotionMeta meta = (PotionMeta) item.getItemMeta();
        for (PotionEffect effect : effects) meta.addCustomEffect(effect, true);
        item.setItemMeta(meta);
        return item;
    }

    private void spawnDrop(PartySession session) {
        int roll = ThreadLocalRandom.current().nextInt(1, 16);
        if (roll > 9) return;
        List<Location> points = session.arena.group("tool-drops");
        Location location = points.get(ThreadLocalRandom.current().nextInt(points.size())).clone().add(.5, .2, .5);
        ItemStack item = switch (roll) {
            case 1 -> loot(session, Material.ENDER_PEARL, "loot.ender-pearl");
            case 2 -> loot(session, Material.SPECTRAL_ARROW, "loot.spectral-arrow");
            case 3 -> {
                ItemStack crossbow = loot(session, Material.CROSSBOW, "loot.crossbow");
                crossbow.addUnsafeEnchantment(Enchantment.QUICK_CHARGE, 5);
                yield crossbow;
            }
            case 4 -> loot(session, Material.GOLDEN_APPLE, "loot.golden-apple");
            case 5 -> potion(session, "loot.turtle-potion",
                    new PotionEffect(PotionEffectType.SLOWNESS, 100, 0),
                    new PotionEffect(PotionEffectType.RESISTANCE, 100, 5));
            case 6 -> potion(session, "loot.invisibility-potion",
                    new PotionEffect(PotionEffectType.INVISIBILITY, 100, 0));
            case 7 -> loot(session, Material.GLOW_INK_SAC, "loot.glow");
            case 8 -> {
                ItemStack sword = loot(session, Material.IRON_SWORD, "loot.one-shot-sword");
                ItemMeta meta = sword.getItemMeta();
                meta.getPersistentDataContainer().set(WEAPON_KEY, PersistentDataType.BYTE, (byte) 1);
                sword.setItemMeta(meta);
                yield sword;
            }
            case 9 -> loot(session, Material.SHIELD, "loot.shield");
            default -> throw new IllegalStateException("unreachable loot roll");
        };
        Item dropped = session.arena.world().dropItem(location, item);
        dropped.setGlowing(true);
        dropped.setPickupDelay(0);
        session.track(dropped);
    }

    private void randomEvent(PartySession session, int elapsed) {
        int roll = ThreadLocalRandom.current().nextInt(1, 11);
        if (roll > 7) { session.broadcast("event-none"); return; }
        PotionEffectType type = switch (roll) {
            case 1, 7 -> PotionEffectType.GLOWING;
            case 2 -> PotionEffectType.POISON;
            case 3 -> PotionEffectType.JUMP_BOOST;
            case 4 -> PotionEffectType.SPEED;
            case 5 -> PotionEffectType.RESISTANCE;
            case 6 -> PotionEffectType.INVISIBILITY;
            default -> throw new IllegalStateException("unreachable event roll");
        };
        int seconds = switch (roll) { case 1 -> 6; case 2 -> 5; case 3, 4, 5 -> 3; default -> 5; };
        int amplifier = switch (roll) { case 1, 6, 7, 2 -> 0; case 3 -> 5; case 4 -> 1; case 5 -> 5; default -> 0; };
        session.broadcast("event-" + roll);
        for (Player player : session.survivors()) {
            player.addPotionEffect(new PotionEffect(type, seconds * 20, amplifier, true, false, false));
            if (roll == 6) forcedInvisibleUntil.put(player.getUniqueId(), elapsed + seconds * 20);
        }
    }

    @Override public boolean allowInventoryEdit(PartySession session, Player player) { return true; }
    @Override public boolean allowSwap(PartySession session, Player player) { return true; }
    @Override public boolean allowDrop(PartySession session, Player player, ItemStack item) { return false; }
    @Override public boolean allowPickup(PartySession session, Player player, Item item) {
        ItemStack stack = item.getItemStack();
        ItemMeta meta = stack.getItemMeta();
        return meta != null && meta.getPersistentDataContainer().has(ITEM_KEY, PersistentDataType.STRING);
    }

    @Override public void interact(PartySession session, PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        ItemStack hand = event.getItem();
        if (hand == null || hand.getType() != Material.GLOW_INK_SAC || !marked(hand, "loot.glow")) return;
        event.setCancelled(true);
        ItemStack held = event.getHand() == org.bukkit.inventory.EquipmentSlot.OFF_HAND
                ? event.getPlayer().getInventory().getItemInOffHand() : event.getPlayer().getInventory().getItemInMainHand();
        if (held.getAmount() <= 1) {
            if (event.getHand() == org.bukkit.inventory.EquipmentSlot.OFF_HAND)
                event.getPlayer().getInventory().setItemInOffHand(new ItemStack(Material.AIR));
            else event.getPlayer().getInventory().setItemInMainHand(new ItemStack(Material.AIR));
        } else held.setAmount(held.getAmount() - 1);
        for (Player target : session.survivors()) if (!target.equals(event.getPlayer())) target.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 60, 0, true, false, false));
        event.getPlayer().sendMessage(session.config.text("glow-used"));
    }

    @Override public void consume(PartySession session, org.bukkit.event.player.PlayerItemConsumeEvent event) {
        if (marked(event.getItem(), "loot.invisibility-potion"))
            forcedInvisibleUntil.put(event.getPlayer().getUniqueId(), session.elapsed() + 100);
    }

    @Override public void hit(PartySession session, Player attacker, Player victim, EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Arrow) {
            if (victim.hasPotionEffect(PotionEffectType.RESISTANCE)) { event.setCancelled(false); return; }
            event.setCancelled(true);
            session.eliminate(victim);
            return;
        }
        ItemStack weapon = attacker.getInventory().getItemInMainHand();
        if (weapon.getType() == Material.IRON_SWORD && weapon.getItemMeta() != null
                && weapon.getItemMeta().getPersistentDataContainer().has(WEAPON_KEY, PersistentDataType.BYTE)) {
            event.setCancelled(true);
            session.eliminate(victim);
            attacker.getInventory().setItemInMainHand(null);
        } else event.setCancelled(false);
    }

    @Override public boolean lethalDamage(PartySession session, Player player, EntityDamageEvent event) {
        return player.hasPotionEffect(PotionEffectType.RESISTANCE);
    }

    @Override public void eliminated(PartySession session, Player player) {
        int remaining = session.survivors().size();
        if (remaining <= 2 && !ranks.containsKey(player.getUniqueId())) {
            int points = remaining == 2 ? 1 : remaining == 1 ? 2 : 0;
            if (points > 0) { ranks.put(player.getUniqueId(), points); session.points(player, points); }
        }
        if (remaining == 1) {
            Player winner = session.survivors().get(0);
            ranks.put(winner.getUniqueId(), 3);
            session.points(winner, 3);
        }
    }

    private boolean marked(ItemStack item, String value) {
        return item.getItemMeta() != null && value.equals(item.getItemMeta().getPersistentDataContainer().get(ITEM_KEY, PersistentDataType.STRING));
    }

    @Override public void stopped(PartySession session) {
        for (Player player : session.players()) player.removePotionEffect(PotionEffectType.INVISIBILITY);
        ranks.clear();
        forcedInvisibleUntil.clear();
        sneakInvisible.clear();
    }
}
