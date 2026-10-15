package com.mlc.mlcgames.party.exploration.finder;

import com.mlc.mlcgames.party.core.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

/** Source finder: glowing click/attack eggs, player-count spawn thresholds, dense ranking. */
public final class FinderGame implements PartyGame {
    private record Egg(Interaction hitbox, ItemDisplay display) {}
    private final Map<UUID, Egg> eggs = new HashMap<>();
    private final Map<UUID, Integer> collected = new LinkedHashMap<>();
    private final Map<UUID, Location> starts = new HashMap<>();
    private ThresholdTable counts;
    private int finishAt = Integer.MAX_VALUE;

    @Override public boolean lastPlayerWins() { return false; }
    @Override public void validate(PartyConfig config, PartyConfig.Arena arena) {
        if (arena.group("eggs").size() > 1000) throw new IllegalArgumentException("彩蛋点位最多 1000 个");
        counts = config.thresholds("egg-counts");
        config.number("collection-reach", 0.5, 6);
        config.integer("all-found-seconds", 1, 30);
        config.integer("rank-interval-seconds", 1, 120);
        config.integer("rank-points.first", 0, 100);
        config.integer("rank-points.second", 0, 100);
        config.integer("rank-points.third", 0, 100);
    }
    @Override public void start(PartySession session) {
        for (Player player : session.players()) {
            collected.put(player.getUniqueId(), 0);
            starts.put(player.getUniqueId(), player.getLocation().clone());
            player.setTotalExperience(0); player.setLevel(0); player.setExp(0);
        }
        List<Location> points = new ArrayList<>(session.arena.group("eggs"));
        Collections.shuffle(points);
        int count = Math.min(points.size(), counts.value(session.players().size()));
        for (Location point : points.subList(0, count)) {
            Interaction hitbox = session.arena.world().spawn(point, Interaction.class, entity -> {
                entity.setInteractionWidth(1); entity.setInteractionHeight(1); entity.setResponsive(true); entity.setPersistent(false); entity.setGlowing(true);
            });
            session.track(hitbox);
            ItemDisplay display = session.arena.world().spawn(point.clone().add(0, .5, 0), ItemDisplay.class, entity -> {
                entity.setItemStack(new ItemStack(Material.EGG));
                entity.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.FIXED);
                entity.setBillboard(Display.Billboard.VERTICAL);
                entity.setGlowing(true); entity.setInvulnerable(true); entity.setPersistent(false);
            });
            session.track(display);
            eggs.put(hitbox.getUniqueId(), new Egg(hitbox, display));
        }
    }
    @Override public void interactEntity(PartySession session, Player player, Entity target) {
        Egg egg = eggs.get(target.getUniqueId());
        if (egg == null || !session.active(player)) return;
        double reach = session.config.number("collection-reach", .5, 6);
        if (player.getEyeLocation().distanceSquared(target.getLocation().add(0, .5, 0)) > reach * reach || !player.hasLineOfSight(target)) return;
        if (eggs.remove(target.getUniqueId()) == null) return;
        egg.hitbox.remove(); egg.display.remove();
        int count = collected.merge(player.getUniqueId(), 1, Integer::sum);
        ItemStack coin = session.item(Material.SUNFLOWER, "items.egg");
        ItemMeta meta = coin.getItemMeta(); meta.setItemModel(NamespacedKey.minecraft("egg")); meta.setMaxStackSize(99); coin.setItemMeta(meta);
        player.getInventory().addItem(coin);
        player.setLevel(count);
        session.broadcast("egg-found", "player", player.getName(), "count", count);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
        if (eggs.isEmpty()) {
            finishAt = Math.min(finishAt, session.elapsed() + session.config.integer("all-found-seconds", 1, 30) * 20);
            session.broadcast("all-eggs-found", "seconds", session.config.integer("all-found-seconds", 1, 30));
        }
    }
    @Override public void tick(PartySession session, int elapsed) {
        if (elapsed >= finishAt) { timeout(session); return; }
        if (elapsed % 20 == 0) for (Player player : session.survivors()) player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, 40, 0, true, false));
        if (elapsed % (session.config.integer("rank-interval-seconds", 1, 120) * 20) == 0) showRanks(session, false);
    }
    private List<Rankings.Row<UUID>> showRanks(PartySession session, boolean award) {
        Map<UUID, Integer> eligible = new LinkedHashMap<>();
        for (Player player : session.players()) eligible.put(player.getUniqueId(), collected.getOrDefault(player.getUniqueId(), 0));
        List<Rankings.Row<UUID>> ranks = Rankings.descending(eligible);
        for (Rankings.Row<UUID> row : ranks) {
            Player player = Bukkit.getPlayer(row.id());
            if (player == null) continue;
            if (award) session.points(player, row.rank() <= 3 ? session.config.integer("rank-points." + (row.rank() == 1 ? "first" : row.rank() == 2 ? "second" : "third"), 0, 100) : 0);
            if (row.rank() <= 3) session.broadcast("egg-rank", "rank", row.rank(), "player", player.getName(), "count", row.score());
        }
        return ranks;
    }
    @Override public void timeout(PartySession session) {
        List<Rankings.Row<UUID>> ranks = showRanks(session, true);
        Set<UUID> winners = new HashSet<>();
        for (Rankings.Row<UUID> row : ranks) if (row.rank() == 1) winners.add(row.id());
        session.finish(session.players().stream().filter(p -> winners.contains(p.getUniqueId())).toList());
    }
    @Override public void hazard(PartySession session, Player player) {
        if (player.isDead()) { session.eliminate(player); return; }
        player.setFallDistance(0); player.setFireTicks(0); player.setVelocity(new Vector());
        if (!player.teleport(starts.get(player.getUniqueId()))) throw new IllegalStateException(session.config.text("teleport-failed"));
    }
}
