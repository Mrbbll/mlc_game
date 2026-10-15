package com.mlc.mlcgames.party.racing.dropper;

import com.mlc.mlcgames.party.core.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.util.Vector;
import java.util.*;

/** Source: map_all/dropper. Ten ordered levels, retries and failure count; no invented race points. */
public final class DropperGame implements PartyGame {
    private final Map<UUID, Integer> levels = new HashMap<>();
    private final Map<UUID, Integer> failures = new HashMap<>();
    private final Map<UUID, Integer> lastDamage = new HashMap<>();
    private final Set<UUID> completed = new HashSet<>();
    private List<Location> starts;
    private final List<Cuboid> portals = new ArrayList<>();
    private Set<Material> portalMaterials;
    private double health;

    @Override public boolean lastPlayerWins() { return false; }
    @Override public void validate(PartyConfig config, PartyConfig.Arena arena) {
        starts = arena.group("level-starts");
        if (starts.size() != 10) throw new IllegalArgumentException("星跳水立方必须配置按关卡排序的 10 个 level-starts");
        health = config.number("attempt-health", 1, 20);
        portalMaterials = config.materials("portal-materials");
        for (int level = 1; level <= 10; level++) {
            Cuboid portal = arena.region("level-" + level + "-portal");
            Location start = starts.get(level - 1);
            if (portal.contains(start.getBlockX(), start.getBlockY(), start.getBlockZ())) throw new IllegalArgumentException("第 " + level + " 关起点不能位于本关传送门内");
            portals.add(portal);
        }
    }
    @Override public void start(PartySession session) {
        for (Player player : session.survivors()) {
            levels.put(player.getUniqueId(), 0);
            failures.put(player.getUniqueId(), 0);
            enterLevel(session, player);
        }
    }
    private void enterLevel(PartySession session, Player player) {
        player.setFallDistance(0);
        player.setFireTicks(0);
        player.setVelocity(new Vector());
        player.getInventory().clear();
        player.setHealth(Math.min(health, Objects.requireNonNull(player.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH)).getValue()));
        if (!player.teleport(starts.get(levels.get(player.getUniqueId())))) throw new IllegalStateException(session.config.text("teleport-failed"));
        player.sendMessage(session.config.message("dropper-level", "level", levels.get(player.getUniqueId()) + 1));
    }
    @Override public void tick(PartySession session, int elapsed) {
        for (Player player : List.copyOf(session.survivors())) {
            // The source uses regeneration with a two-health cap, once per second.
            if (elapsed % 20 == 0) player.setHealth(Math.min(health, Objects.requireNonNull(player.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH)).getValue()));
            Location location = player.getLocation();
            int level = levels.get(player.getUniqueId());
            if (!portals.get(level).contains(location.getBlockX(), location.getBlockY(), location.getBlockZ()) || !portalMaterials.contains(location.getBlock().getType())) continue;
            session.broadcast("dropper-passed", "player", player.getName(), "level", level + 1);
            if (level == 9) {
                completed.add(player.getUniqueId());
                session.broadcast("dropper-complete", "player", player.getName(), "failures", failures.get(player.getUniqueId()));
                session.retire(player);
            } else {
                levels.put(player.getUniqueId(), level + 1);
                enterLevel(session, player);
            }
        }
        if (session.survivors().isEmpty()) timeout(session);
    }
    @Override public void environmentalDamage(PartySession session, Player player, EntityDamageEvent event) {
        double damage = event.getFinalDamage();
        if (damage <= 0) return;
        int now = session.elapsed();
        if (now - lastDamage.getOrDefault(player.getUniqueId(), -1000) < 10) return;
        lastDamage.put(player.getUniqueId(), now);
        if (damage >= player.getHealth()) hazard(session, player);
        else player.setHealth(player.getHealth() - damage);
    }
    @Override public void hazard(PartySession session, Player player) {
        if (player.isDead()) { session.eliminate(player); return; }
        int count = failures.merge(player.getUniqueId(), 1, Integer::sum);
        player.sendMessage(session.config.message("dropper-failed", "failures", count));
        enterLevel(session, player);
    }
    @Override public void timeout(PartySession session) {
        session.finish(session.players().stream().filter(p -> completed.contains(p.getUniqueId())).toList());
    }
}
