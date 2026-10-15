package com.mlc.mlcgames.party.racing.mutouren;

import com.mlc.mlcgames.party.core.*;
import com.mlc.mlcgames.party.racing.common.RaceGame;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.entity.Pillager;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/** Source mutouren: warning, random freeze, safe start, movement retry and completion order. */
public final class MutourenGame extends RaceGame {
    private Cuboid safe;
    private Cuboid gate;
    private FreezeClock clock;
    private Pillager judge;
    private boolean prepared;
    private final Map<UUID, Location> anchors = new HashMap<>();

    @Override public void validate(PartyConfig config, PartyConfig.Arena arena) {
        super.validate(config, arena);
        safe = arena.region("safe");
        gate = arena.region("gate");
        if (arena.group("judge").size() != 1) throw new IllegalArgumentException("木头人 judge 点位必须恰好一个");
        if (gate.volume() > config.integer("limits.blocks-per-tick", 1, 2000)) throw new IllegalArgumentException("木头人起跑门超过每 tick 修改上限");
        for (Location spawn : arena.spawns()) if (!safe.contains(spawn.getBlockX(), spawn.getBlockY(), spawn.getBlockZ())) throw new IllegalArgumentException("木头人出生点必须位于 safe 区域内");
        for (String phase : List.of("first-moving", "moving", "frozen")) {
            int min = config.integer(phase + ".min-seconds", 1, 120);
            config.integer(phase + ".max-seconds", min, 120);
        }
        config.number("movement-tolerance", 0, .1);
        config.integer("progress-interval-seconds", 1, 60);
        if (!List.of("X", "-X", "Z", "-Z").contains(config.rules.getString("progress-axis"))) throw new IllegalArgumentException("progress-axis 必须为 X、-X、Z 或 -Z");
    }
    @Override public List<Cuboid> writableRegions(PartyConfig.Arena arena) { return List.of(arena.region("gate")); }
    @Override public boolean prepare(PartySession session, int budget) {
        if (!prepared) { fillGate(session, Material.OAK_FENCE); prepared = true; }
        return true;
    }
    private void fillGate(PartySession session, Material material) {
        for (int x = gate.minX(); x <= gate.maxX(); x++) for (int y = gate.minY(); y <= gate.maxY(); y++) for (int z = gate.minZ(); z <= gate.maxZ(); z++) session.set(session.arena.world().getBlockAt(x, y, z), material);
    }
    private int duration(PartySession session, String phase) {
        return ThreadLocalRandom.current().nextInt(session.config.integer(phase + ".min-seconds", 1, 120), session.config.integer(phase + ".max-seconds", 1, 120) + 1) * 20;
    }
    @Override public void start(PartySession session) {
        super.start(session);
        fillGate(session, Material.AIR);
        clock = new FreezeClock(duration(session, "first-moving"));
        judge = session.arena.world().spawn(session.arena.group("judge").getFirst(), Pillager.class, entity -> {
            entity.setAI(false); entity.setSilent(true); entity.setInvulnerable(true); entity.setGlowing(true); entity.setPersistent(false);
            entity.customName(session.config.message("judge-name")); entity.setCustomNameVisible(true);
        });
        session.track(judge);
    }
    @Override public void tick(PartySession session, int elapsed) {
        super.tick(session, elapsed);
        if (session.state() != PartySession.State.RUNNING) return;
        if (clock.due(elapsed)) {
            boolean freezing = !clock.frozen();
            clock.transition(elapsed, duration(session, freezing ? "frozen" : "moving"));
            anchors.clear();
            if (freezing) for (Player player : session.survivors()) anchors.put(player.getUniqueId(), player.getLocation().clone());
            session.broadcast(freezing ? "freeze-start" : "freeze-end");
        }
        if (elapsed % 20 == 0) {
            int seconds = (clock.remaining(elapsed) + 19) / 20;
            if (!clock.frozen() && seconds >= 1 && seconds <= 3) {
                String key = switch (seconds) { case 3 -> "signals.count"; case 2 -> "signals.name"; default -> "signals.stop"; };
                for (Player player : session.players()) player.showTitle(Title.title(session.config.message(key), session.config.message("signals.warning"), Title.Times.times(Duration.ZERO, Duration.ofSeconds(1), Duration.ZERO)));
            }
            for (Player player : session.survivors()) player.sendActionBar(session.config.message(clock.frozen() ? "frozen-status" : "moving-status", "seconds", seconds));
        }
        if (judge != null && judge.isValid()) session.survivors().stream().min(Comparator.comparingDouble(p -> p.getLocation().distanceSquared(judge.getLocation()))).ifPresent(p -> {
            Location facing = judge.getLocation().setDirection(p.getEyeLocation().toVector().subtract(judge.getEyeLocation().toVector()));
            judge.setRotation(facing.getYaw(), facing.getPitch());
        });
        if (elapsed % (session.config.integer("progress-interval-seconds", 1, 60) * 20) == 0) showProgress(session);
    }
    @Override public void moved(PartySession session, PlayerMoveEvent event) {
        if (clock == null || !clock.frozen() || event.getTo() == null) return;
        Location to = event.getTo();
        if (safe.contains(to.getBlockX(), to.getBlockY(), to.getBlockZ())) return;
        Location anchor = anchors.computeIfAbsent(event.getPlayer().getUniqueId(), key -> event.getFrom().clone());
        double tolerance = session.config.number("movement-tolerance", 0, .1);
        if (to.getWorld().equals(anchor.getWorld()) && to.distanceSquared(anchor) <= tolerance * tolerance) return;
        Player player = event.getPlayer();
        Location start = starts.get(player.getUniqueId());
        event.setTo(start.clone());
        player.setVelocity(new Vector()); player.setFallDistance(0); player.getInventory().clear();
        anchors.put(player.getUniqueId(), start.clone());
        player.sendMessage(session.config.message("moved-while-frozen"));
    }
    @Override public void hazard(PartySession session, Player player) {
        super.hazard(session, player);
        anchors.put(player.getUniqueId(), player.getLocation().clone());
    }
    private void showProgress(PartySession session) {
        Map<UUID, Integer> progress = new LinkedHashMap<>();
        String axis = session.config.rules.getString("progress-axis", "X");
        for (Player player : session.survivors()) {
            Location loc = player.getLocation(), start = starts.get(player.getUniqueId());
            double value = axis.endsWith("X") ? loc.getX() - start.getX() : loc.getZ() - start.getZ();
            if (axis.startsWith("-")) value = -value;
            progress.put(player.getUniqueId(), (int) Math.floor(value * 100));
        }
        for (Rankings.Row<UUID> row : Rankings.descending(progress)) {
            if (row.rank() > 3) break;
            Player player = Bukkit.getPlayer(row.id());
            if (player != null) session.broadcast("progress-rank", "rank", row.rank(), "player", player.getName(), "distance", String.format(Locale.ROOT, "%.1f", row.score() / 100.0));
        }
    }
}
