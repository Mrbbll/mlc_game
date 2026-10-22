package com.mlc.mlcgames.party.racing.car_race;

import com.mlc.mlcgames.party.core.*;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import java.util.*;

/** Source map_game_2nd/car_race: summon mount, wait for all riders, three laps, input-controlled speed. */
public final class PigRaceGame implements PartyGame {
    private final Set<Pig> pigs = new HashSet<>();
    private final Set<UUID> spawned = new HashSet<>();
    private final Map<UUID, Integer> speed = new HashMap<>();
    private final Map<UUID, Integer> laps = new HashMap<>();
    private final FinishOrder<UUID> order = new FinishOrder<>();
    private Cuboid gate, finish, launch, slow;
    private Location lapReturn;
    private Set<Material> finishMaterials, launchMaterials, slowMaterials;
    private int prepared, readyTicks;

    @Override public boolean lastPlayerWins() { return false; }
    @Override public boolean hasReadyPhase() { return true; }
    @Override public void validate(PartyConfig config, PartyConfig.Arena arena) {
        gate = arena.region("gate"); finish = arena.region("finish"); launch = arena.region("launch-pad"); slow = arena.region("slow-pad");
        if (gate.volume() > config.integer("limits.blocks-per-tick", 1, 2000)) throw new IllegalArgumentException("起跑门不能大于每 tick 方块预算");
        List<Location> returns = arena.group("lap-return");
        if (returns.size() != 1) throw new IllegalArgumentException("lap-return 必须恰好一个返回点");
        lapReturn = returns.getFirst();
        for (Location position : arena.spawns()) {
            if (inside(finish, position.clone().subtract(0, 1, 0))) throw new IllegalArgumentException("出生点不能位于终点线上");
            if (inside(gate, position) || inside(gate, position.clone().add(0, 1, 0))) throw new IllegalArgumentException("出生点不能被起跑门包围");
        }
        if (inside(finish, lapReturn.clone().subtract(0, 1, 0))) throw new IllegalArgumentException("lap-return 不能位于终点线上，避免反复计圈");
        if (inside(gate, lapReturn) || inside(gate, lapReturn.clone().add(0, 1, 0))) throw new IllegalArgumentException("lap-return 不能位于起跑门内");
        config.integer("laps", 1, 100); config.integer("ready-timeout-seconds", 10, 600); config.integer("finishers-to-end", 1, 64);
        config.number("remount-reach", 1, 5); config.number("speed.attribute-factor", .0001, .1);
        for (String key : List.of("acceleration", "brake", "friction", "cap", "slow-threshold", "slow-penalty")) config.integer("speed." + key, key.equals("friction") ? 0 : 1, 1000);
        config.integer("launch-duration-ticks", 1, 200); config.integer("launch-amplifier", 0, 20);
        for (int rank = 1; rank <= 5; rank++) config.integer("finish-points.rank-" + rank, 0, 100);
        finishMaterials = config.materials("finish-materials"); launchMaterials = config.materials("launch-materials"); slowMaterials = config.materials("slow-materials");
    }
    private static boolean inside(Cuboid region, Location point) { return region.contains(point.getBlockX(), point.getBlockY(), point.getBlockZ()); }
    @Override public List<Cuboid> writableRegions(PartyConfig.Arena arena) { return List.of(gate); }
    @Override public boolean prepare(PartySession session, int budget) {
        for (int count = 0; count < budget && prepared < gate.volume(); count++, prepared++) {
            int widthZ = gate.maxZ() - gate.minZ() + 1, height = gate.maxY() - gate.minY() + 1;
            int x = gate.minX() + prepared / (height * widthZ), y = gate.minY() + prepared / widthZ % height, z = gate.minZ() + prepared % widthZ;
            session.set(session.arena.world().getBlockAt(x, y, z), Material.ACACIA_FENCE);
        }
        return prepared == gate.volume();
    }
    @Override public void setupReady(PartySession session) {
        for (Player player : session.players()) {
            player.getInventory().setItem(0, session.item(Material.MINECART, "items.summon"));
            player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, (session.config.integer("ready-timeout-seconds", 10, 600) + 60) * 20, 0, true, false));
        }
        session.broadcast("car-ready");
    }
    @Override public boolean ready(PartySession session) {
        if (++readyTicks >= session.config.integer("ready-timeout-seconds", 10, 600) * 20) { session.broadcast("car-ready-timeout"); session.stop(); return false; }
        return !session.survivors().isEmpty() && session.survivors().stream().allMatch(p -> p.getVehicle() instanceof Pig pig && pigs.contains(pig));
    }
    @Override public void swap(PartySession session, PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        if (session.state() != PartySession.State.READY || spawned.contains(player.getUniqueId()) || player.getInventory().getItemInMainHand().getType() != Material.MINECART) return;
        Pig pig = session.arena.world().spawn(player.getLocation(), Pig.class, entity -> {
            entity.setAdult(); entity.setSaddle(true); entity.setInvulnerable(true); entity.setSilent(true); entity.setPersistent(false);
            Objects.requireNonNull(entity.getAttribute(Attribute.MOVEMENT_SPEED)).setBaseValue(0);
            entity.customName(session.config.message("car-name")); entity.setCustomNameVisible(true);
        });
        pigs.add(pig); session.track(pig); spawned.add(player.getUniqueId());
        player.getInventory().setItemInMainHand(null);
        player.getInventory().setItemInOffHand(session.item(Material.CARROT_ON_A_STICK, "items.steering"));
        if (!pig.addPassenger(player)) throw new IllegalStateException("坐骑挂载失败");
    }
    @Override public void interact(PartySession session, PlayerInteractEvent event) {
        if (event.getPlayer().getInventory().getItemInMainHand().getType() == Material.MINECART) event.setUseItemInHand(Event.Result.DENY);
    }
    @Override public void interactEntity(PartySession session, Player player, Entity entity) {
        if (entity instanceof Pig pig && pigs.contains(pig) && pig.getPassengers().isEmpty() && player.getVehicle() == null
                && player.getLocation().distanceSquared(pig.getLocation()) <= Math.pow(session.config.number("remount-reach", 1, 5), 2)
                && player.hasLineOfSight(pig)) pig.addPassenger(player);
    }
    @Override public void start(PartySession session) {
        for (int x = gate.minX(); x <= gate.maxX(); x++) for (int y = gate.minY(); y <= gate.maxY(); y++) for (int z = gate.minZ(); z <= gate.maxZ(); z++) session.set(session.arena.world().getBlockAt(x, y, z), Material.AIR);
    }
    @Override public void tick(PartySession session, int elapsed) {
        for (Player player : List.copyOf(session.survivors())) {
            Pig pig = player.getVehicle() instanceof Pig mounted && pigs.contains(mounted) && mounted.isValid() ? mounted : null;
            if (pig == null) {
                double reach = session.config.number("remount-reach", 1, 5);
                pig = pigs.stream().filter(p -> p.isValid() && p.getPassengers().isEmpty() && p.getWorld().equals(player.getWorld()) && p.getLocation().distanceSquared(player.getLocation()) <= reach * reach)
                        .min(Comparator.comparingDouble(p -> p.getLocation().distanceSquared(player.getLocation()))).orElse(null);
                if (pig == null || !pig.addPassenger(player)) { player.sendMessage(session.config.message("car-lost")); session.eliminate(player); continue; }
            }
            Location feet = pig.getLocation().subtract(0, 1, 0), pad = pig.getLocation().subtract(0, .5, 0);
            if (inside(finish, feet) && finishMaterials.contains(feet.getBlock().getType())) {
                int lap = laps.merge(player.getUniqueId(), 1, Integer::sum);
                session.broadcast("car-lap", "player", player.getName(), "lap", lap);
                if (lap >= session.config.integer("laps", 1, 100)) {
                    int rank = order.finish(player.getUniqueId());
                    int points = rank <= 5 ? session.config.integer("finish-points.rank-" + rank, 0, 100) : 0;
                    session.points(player, points); pig.remove(); pigs.remove(pig); session.retire(player);
                    session.broadcast("car-finished", "player", player.getName(), "rank", rank, "points", points);
                    if (rank >= session.config.integer("finishers-to-end", 1, 64)) { timeout(session); return; }
                    continue;
                }
                if (!pig.teleport(lapReturn)) throw new IllegalStateException(session.config.text("teleport-failed"));
                pad = pig.getLocation().subtract(0, .5, 0);
            }
            if (inside(launch, pad) && launchMaterials.contains(pad.getBlock().getType())) pig.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION,
                    session.config.integer("launch-duration-ticks", 1, 200), session.config.integer("launch-amplifier", 0, 20), true, false));
            Input input = player.getCurrentInput();
            PigSpeed.Step step = PigSpeed.step(speed.getOrDefault(player.getUniqueId(), 0), input.isBackward(), input.isForward(), input.isJump(),
                    inside(slow, pad) && slowMaterials.contains(pad.getBlock().getType()),
                    session.config.integer("speed.acceleration", 1, 1000), session.config.integer("speed.brake", 1, 1000), session.config.integer("speed.friction", 0, 1000),
                    session.config.integer("speed.cap", 1, 1000), session.config.integer("speed.slow-threshold", 1, 1000), session.config.integer("speed.slow-penalty", 1, 1000));
            Objects.requireNonNull(pig.getAttribute(Attribute.MOVEMENT_SPEED)).setBaseValue(step.applied() * session.config.number("speed.attribute-factor", .0001, .1));
            speed.put(player.getUniqueId(), step.next());
            if (elapsed % 20 == 0) {
                player.getInventory().setItemInOffHand(session.item(Material.CARROT_ON_A_STICK, "items.steering"));
                player.sendActionBar(session.config.message("car-status", "speed", step.next(), "lap", laps.getOrDefault(player.getUniqueId(), 0), "laps", session.config.integer("laps", 1, 100)));
            }
        }
        pigs.removeIf(p -> !p.isValid());
        for (Pig pig : pigs) if (pig.getPassengers().isEmpty()) Objects.requireNonNull(pig.getAttribute(Attribute.MOVEMENT_SPEED)).setBaseValue(0);
        if (session.survivors().size() <= 1) timeout(session);
    }
    @Override public void eliminated(PartySession session, Player player) {
        // Session.eliminate dismounts first; all unused pigs remain eligible for nearby remounting.
        speed.remove(player.getUniqueId());
    }
    @Override public void timeout(PartySession session) {
        Set<UUID> finishers = order.places().keySet();
        session.finish(session.players().stream().filter(p -> finishers.contains(p.getUniqueId())).toList());
    }
}
