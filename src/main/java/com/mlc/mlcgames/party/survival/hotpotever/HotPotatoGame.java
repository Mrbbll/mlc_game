package com.mlc.mlcgames.party.survival.hotpotever;

import com.mlc.mlcgames.party.core.*;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/** Source: hotpotever; holder/time thresholds match summon.mcfunction. */
public final class HotPotatoGame implements PartyGame {
    private final Set<UUID> holders = new HashSet<>();
    private int nextRound;
    private int explosionAt;
    private int mode;
    private final Map<UUID, Integer> lastPass = new HashMap<>();
    private final Map<UUID, Integer> firedAt = new HashMap<>();
    private ThresholdTable holderCounts;
    private ThresholdTable durations;

    @Override public void validate(PartyConfig config, PartyConfig.Arena arena) {
        config.integer("mode", 0, 4);
        config.integer("between-round-seconds", 1, 30);
        config.integer("pass-cooldown-ticks", 0, 100);
        config.integer("projectile-cooldown-ticks", 1, 200);
        holderCounts = config.thresholds("holder-counts");
        durations = config.thresholds("explosion-seconds");
        for (int players = 2; players <= 64; players++) {
            if (holderCounts.value(players) >= players || durations.value(players) > 600) throw new IllegalArgumentException("持有者人数必须小于总人数，爆炸时间最多 600 秒");
        }
    }
    @Override public void start(PartySession session) {
        mode = session.config.integer("mode", 0, 4);
        if (mode == 0) mode = ThreadLocalRandom.current().nextInt(1, 5);
        nextRound = session.config.integer("between-round-seconds", 1, 30) * 20;
    }
    @Override public void tick(PartySession session, int elapsed) {
        holders.removeIf(id -> session.survivors().stream().noneMatch(p -> p.getUniqueId().equals(id)));
        if (elapsed >= nextRound && explosionAt == 0) selectHolders(session, elapsed);
        if (explosionAt > 0 && elapsed >= explosionAt) {
            for (Player player : List.copyOf(session.survivors())) if (holders.contains(player.getUniqueId())) session.eliminate(player);
            holders.clear();
            explosionAt = 0;
            nextRound = elapsed + session.config.integer("between-round-seconds", 1, 30) * 20;
            session.broadcast("round-break", "seconds", session.config.integer("between-round-seconds", 1, 30));
        }
        if (elapsed % 20 == 0) {
            for (Player player : session.survivors()) {
                boolean holder = holders.contains(player.getUniqueId());
                player.setGlowing(true);
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, holder ? 1 : 0, true, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 40, 0, true, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, 40, 0, true, false));
                if (holder) equip(session, player);
                player.sendActionBar(session.config.message(holder ? "holder-status" : "runner-status", "seconds", Math.max(0, (explosionAt - elapsed + 19) / 20)));
            }
        }
    }
    private void selectHolders(PartySession session, int elapsed) {
        List<Player> players = new ArrayList<>(session.survivors());
        if (players.size() < 2) return;
        Collections.shuffle(players);
        int n = players.size();
        int count = holderCounts.value(n);
        for (Player p : players.subList(0, Math.min(count, n - 1))) { holders.add(p.getUniqueId()); equip(session, p); }
        explosionAt = elapsed + durations.value(n) * 20;
        session.broadcast("holders", "players", String.join(", ", players.stream().filter(p -> holders.contains(p.getUniqueId())).map(Player::getName).toList()));
    }
    private void equip(PartySession session, Player player) {
        player.getInventory().setItem(0, session.item(Material.POTATO, "items.potato"));
        Material weapon = switch (mode) { case 2 -> Material.BOW; case 3 -> Material.TRIDENT; case 4 -> Material.CROSSBOW; default -> null; };
        if (weapon != null && !player.getInventory().contains(weapon)) {
            ItemStack item = session.item(weapon, "items.weapon");
            if (mode == 4) item.addUnsafeEnchantment(Enchantment.QUICK_CHARGE, 5);
            player.getInventory().setItem(1, item);
        }
        if ((mode == 2 || mode == 4) && !player.getInventory().contains(Material.ARROW)) player.getInventory().setItem(8, new ItemStack(Material.ARROW));
    }
    @Override public void hit(PartySession session, Player attacker, Player victim, EntityDamageByEntityEvent event) {
        if (!holders.contains(attacker.getUniqueId()) || holders.contains(victim.getUniqueId()) || explosionAt == 0) return;
        boolean projectile = event.getDamager() instanceof Projectile;
        if (projectile && mode == 1) return;
        if (session.elapsed() - lastPass.getOrDefault(attacker.getUniqueId(), -1000) < session.config.integer("pass-cooldown-ticks", 0, 100)) return;
        holders.remove(attacker.getUniqueId());
        holders.add(victim.getUniqueId());
        lastPass.put(victim.getUniqueId(), session.elapsed());
        attacker.getInventory().clear();
        equip(session, victim);
        session.broadcast("passed", "from", attacker.getName(), "to", victim.getName());
    }
    @Override public void eliminated(PartySession session, Player player) { holders.remove(player.getUniqueId()); }
    @Override public boolean launch(PartySession session, Player player) {
        if (!holders.contains(player.getUniqueId()) || mode == 1) return false;
        int cooldown = session.config.integer("projectile-cooldown-ticks", 1, 200);
        if (session.elapsed() - firedAt.getOrDefault(player.getUniqueId(), -1000) < cooldown) return false;
        firedAt.put(player.getUniqueId(), session.elapsed());
        return true;
    }
}
