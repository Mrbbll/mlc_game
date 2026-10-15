package com.mlc.mlcgames.party.racing.common;

import com.mlc.mlcgames.party.core.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.Vector;

import java.util.*;

/** Shared race lifecycle, not a replacement for each race's mechanics. */
public abstract class RaceGame implements PartyGame {
    protected final Map<UUID, Location> starts = new HashMap<>();
    protected final Map<UUID, Location> checkpoints = new HashMap<>();
    protected final FinishOrder<UUID> order = new FinishOrder<>();
    protected Cuboid finish;
    private Set<Material> finishMaterials;
    private int deadline = Integer.MAX_VALUE;

    @Override public boolean lastPlayerWins() { return false; }
    @Override public void validate(PartyConfig config, PartyConfig.Arena arena) {
        finish = arena.region("finish");
        finishMaterials = config.materials("finish-materials");
        config.integer("shorten-after-finishers", 0, 64);
        config.integer("shortened-seconds", 1, 600);
        config.integer("finish-points.first", 0, 100);
        config.integer("finish-points.second", 0, 100);
        config.integer("finish-points.third", 0, 100);
        config.integer("finish-points.remaining", 0, 100);
    }
    @Override public void start(PartySession session) {
        for (Player player : session.survivors()) starts.put(player.getUniqueId(), player.getLocation().clone());
    }
    @Override public void tick(PartySession session, int elapsed) {
        if (elapsed % 20 == 0) {
            int remaining = Math.max(0, (Math.min(deadline, session.config.integer("max-seconds", 10, 7200) * 20) - elapsed + 19) / 20);
            for (Player player : session.players()) player.sendActionBar(session.config.message("race-status", "seconds", remaining, "finishers", order.count()));
        }
        for (Player player : List.copyOf(session.survivors())) {
            Location feet = player.getLocation().subtract(0, 1, 0);
            if (finish.contains(feet.getBlockX(), feet.getBlockY(), feet.getBlockZ()) && finishMaterials.contains(feet.getBlock().getType())) complete(session, player);
            else if (session.config.rules.getStringList("elimination-blocks").contains(player.getLocation().getBlock().getType().name())) hazard(session, player);
        }
        if (session.survivors().isEmpty() || elapsed >= deadline) timeout(session);
    }
    protected void complete(PartySession session, Player player) {
        int rank = order.finish(player.getUniqueId());
        if (rank == 0) return;
        String key = switch (rank) { case 1 -> "first"; case 2 -> "second"; case 3 -> "third"; default -> "remaining"; };
        int points = session.config.integer("finish-points." + key, 0, 100);
        session.points(player, points);
        session.retire(player);
        session.broadcast("race-finished", "player", player.getName(), "rank", rank, "points", points);
        int threshold = session.config.integer("shorten-after-finishers", 0, 64);
        if (threshold > 0 && rank == threshold) {
            deadline = Math.min(deadline, session.elapsed() + session.config.integer("shortened-seconds", 1, 600) * 20);
            session.broadcast("race-shortened", "seconds", session.config.integer("shortened-seconds", 1, 600));
        }
    }
    @Override public void hazard(PartySession session, Player player) {
        if (player.isDead()) { session.eliminate(player); return; }
        Location start = checkpoints.getOrDefault(player.getUniqueId(), starts.get(player.getUniqueId()));
        if (start == null) throw new IllegalStateException("跑酷起点缺失");
        player.setFallDistance(0);
        player.setFireTicks(0);
        player.setVelocity(new Vector());
        if (!player.teleport(start)) throw new IllegalStateException(session.config.text("teleport-failed"));
        player.sendMessage(session.config.message("race-retry"));
    }
    @Override public void timeout(PartySession session) {
        Map<UUID, Integer> places = order.places();
        int best = session.players().stream().filter(p -> places.containsKey(p.getUniqueId())).mapToInt(p -> places.get(p.getUniqueId())).min().orElse(0);
        session.finish(session.players().stream().filter(p -> places.getOrDefault(p.getUniqueId(), -1) == best).toList());
    }
    @Override public void hit(PartySession session, Player attacker, Player victim, EntityDamageByEntityEvent event) {
        if (!session.config.rules.getBoolean("pvp-knockback")) return;
        Vector direction = victim.getLocation().toVector().subtract(attacker.getLocation().toVector()).setY(0);
        if (direction.lengthSquared() > 0.001) victim.setVelocity(direction.normalize().multiply(0.4).setY(0.3));
    }
}
