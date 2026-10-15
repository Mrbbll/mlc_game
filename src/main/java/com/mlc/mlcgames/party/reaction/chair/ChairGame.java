package com.mlc.mlcgames.party.reaction.chair;

import com.mlc.mlcgames.party.core.*;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/** Source chair: threshold seat counts, random preparation, moving/wrapping minecarts. */
public final class ChairGame implements PartyGame {
    private enum Phase { PREPARING, CLAIMING, BREAK }
    private final Map<Minecart, Vector> carts = new LinkedHashMap<>();
    private ThresholdTable counts;
    private Cuboid travel;
    private Phase phase;
    private int until;

    @Override public boolean lastPlayerWins() { return false; }
    @Override public void validate(PartyConfig config, PartyConfig.Arena arena) {
        counts = config.thresholds("chair-counts");
        travel = arena.region("cart-travel");
        int max = config.integer("players.max", 2, 64);
        for (int n = 2; n <= max; n++) if (counts.value(n) >= n) throw new IllegalArgumentException("每轮椅子数必须少于存活人数");
        int needed = 0;
        for (int n = 2; n <= max; n++) needed = Math.max(needed, counts.value(n));
        if (arena.group("chairs").size() < needed) throw new IllegalArgumentException("椅子候选点位至少需要 " + needed + " 个");
        if (travel.maxX() - travel.minX() < 4 || travel.maxZ() - travel.minZ() < 4) throw new IllegalArgumentException("矿车运动区域长宽至少 5 格");
        config.integer("prepare.min-seconds", 1, 60);
        config.integer("prepare.max-seconds", config.integer("prepare.min-seconds", 1, 60), 60);
        config.integer("claim-seconds", 1, 60);
        config.integer("break-seconds", 1, 30);
        config.number("velocity.min", .01, 3);
        config.number("velocity.max", config.number("velocity.min", .01, 3), 3);
        config.number("cart-max-speed", .1, 3);
        config.number("restart-speed", 0, .5);
        config.number("claim-reach", .5, 6);
        config.integer("rank-points.first", 0, 100);
        config.integer("rank-points.second", 0, 100);
        config.integer("rank-points.third", 0, 100);
        for (Location point : arena.group("chairs")) if (!travel.contains(point.getBlockX(), point.getBlockY(), point.getBlockZ())) throw new IllegalArgumentException("椅子生成点必须在 cart-travel 区域内");
    }
    @Override public void start(PartySession session) { prepareRound(session); }
    private void prepareRound(PartySession session) {
        clearCarts();
        phase = Phase.PREPARING;
        until = session.elapsed() + ThreadLocalRandom.current().nextInt(session.config.integer("prepare.min-seconds", 1, 60), session.config.integer("prepare.max-seconds", 1, 60) + 1) * 20;
        session.broadcast("chairs-preparing", "count", counts.value(session.survivors().size()));
    }
    private double speed(PartySession session) {
        double low = session.config.number("velocity.min", .01, 3), high = session.config.number("velocity.max", .01, 3);
        double speed = low == high ? low : ThreadLocalRandom.current().nextDouble(low, high);
        return ThreadLocalRandom.current().nextBoolean() ? speed : -speed;
    }
    private void spawnChairs(PartySession session) {
        List<Location> points = new ArrayList<>(session.arena.group("chairs"));
        Collections.shuffle(points);
        for (Location point : points.subList(0, counts.value(session.survivors().size()))) {
            Minecart cart = session.arena.world().spawn(point, Minecart.class, entity -> {
                entity.setInvulnerable(true); entity.setPersistent(false); entity.setMaxSpeed(session.config.number("cart-max-speed", .1, 3));
                entity.customName(session.config.message("chair-name")); entity.setCustomNameVisible(true);
            });
            session.track(cart);
            carts.put(cart, new Vector(speed(session), 0, speed(session)));
        }
        phase = Phase.CLAIMING;
        until = session.elapsed() + session.config.integer("claim-seconds", 1, 60) * 20;
        session.broadcast("chairs-ready");
    }
    @Override public void tick(PartySession session, int elapsed) {
        List<Player> alive = session.survivors();
        if (alive.size() <= 1) {
            for (Player player : alive) session.points(player, session.config.integer("rank-points.first", 0, 100));
            session.finish(alive); return;
        }
        for (Map.Entry<Minecart, Vector> entry : carts.entrySet()) {
            Minecart cart = entry.getKey();
            if (!cart.isValid()) continue;
            Location position = cart.getLocation();
            Location destination = position.clone();
            if (position.getX() < travel.minX() + .2) destination.setX(travel.maxX() - .8);
            if (position.getX() > travel.maxX() + .8) destination.setX(travel.minX() + 1);
            if (position.getZ() < travel.minZ() + .2) destination.setZ(travel.maxZ() - .8);
            if (position.getZ() > travel.maxZ() + .8) destination.setZ(travel.minZ() + 1);
            if (!destination.equals(position)) {
                Vector velocity = cart.getVelocity();
                // Paper 1.21.10+ retains passengers by default, as vanilla does.
                if (!cart.teleport(destination)) throw new IllegalStateException("椅子边界传送被阻止");
                cart.setVelocity(velocity);
            }
            Vector velocity = cart.getVelocity();
            double restart = session.config.number("restart-speed", 0, .5);
            if (Math.abs(velocity.getX()) <= restart && Math.abs(velocity.getY()) <= restart && Math.abs(velocity.getZ()) <= restart) cart.setVelocity(entry.getValue().clone());
        }
        if (elapsed < until) return;
        if (phase == Phase.PREPARING) spawnChairs(session);
        else if (phase == Phase.BREAK) prepareRound(session);
        else {
            Set<UUID> seated = new HashSet<>();
            for (Minecart cart : carts.keySet()) for (Entity passenger : cart.getPassengers()) if (passenger instanceof Player player) seated.add(player.getUniqueId());
            for (Player player : List.copyOf(alive)) if (!seated.contains(player.getUniqueId())) session.eliminate(player);
            phase = Phase.BREAK;
            until = elapsed + session.config.integer("break-seconds", 1, 30) * 20;
            session.broadcast("chair-round-ended");
        }
    }
    @Override public void interactEntity(PartySession session, Player player, Entity target) {
        if (phase != Phase.CLAIMING || !(target instanceof Minecart cart) || !carts.containsKey(cart) || !cart.getPassengers().isEmpty() || player.isInsideVehicle()) return;
        double reach = session.config.number("claim-reach", .5, 6);
        if (player.getLocation().distanceSquared(cart.getLocation()) > reach * reach || !player.hasLineOfSight(cart)) return;
        cart.addPassenger(player);
    }
    @Override public void attackEntity(PartySession session, Player player, Entity target) { /* Source seats by right-click only. */ }
    @Override public void eliminated(PartySession session, Player player) {
        int remaining = session.survivors().size();
        if (remaining <= 2) session.points(player, session.config.integer(remaining <= 1 ? "rank-points.second" : "rank-points.third", 0, 100));
    }
    private void clearCarts() {
        for (Minecart cart : carts.keySet()) { cart.eject(); cart.remove(); }
        carts.clear();
    }
}
