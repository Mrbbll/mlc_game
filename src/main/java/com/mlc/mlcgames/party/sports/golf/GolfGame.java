package com.mlc.mlcgames.party.sports.golf;

import com.mlc.mlcgames.party.core.*;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Consumable;
import io.papermc.paper.datacomponent.item.consumable.ItemUseAnimation;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import java.util.*;

/** Source map_game_2nd/golf: random turns, six clubs, energy scoring, three selectable courses. */
public final class GolfGame implements PartyGame {
    private static final Material[] CLUBS = {Material.STICK, Material.WOODEN_SHOVEL, Material.STONE_SHOVEL,
            Material.GOLDEN_SHOVEL, Material.IRON_SHOVEL, Material.DIAMOND_SHOVEL};
    private record Course(Cuboid bounds, Cuboid goal, Location playerStart, Location ballStart) {}
    private final List<Course> courses = new ArrayList<>();
    private final Map<UUID, GolfScorecard> scores = new LinkedHashMap<>();
    private final Deque<UUID> waiting = new ArrayDeque<>();
    private final Set<Material> goals = EnumSet.noneOf(Material.class);
    private final Set<Material> hazards = EnumSet.noneOf(Material.class);
    private int courseIndex;
    private UUID current;
    private Zombie ball;
    private int nextTurnAt;
    private int turnEnds;
    private int lastShot = -1000;
    private int lastControl = -1000;

    @Override public boolean lastPlayerWins() { return false; }
    @Override public int maximumDurationSeconds() { return 86400; }
    @Override public void validate(PartyConfig config, PartyConfig.Arena arena) {
        if (!arena.contains(arena.spectator())) throw new IllegalArgumentException("高尔夫旁观点必须位于 bounds 内");
        List<Location> playerStarts = arena.group("course-starts");
        List<Location> ballStarts = arena.group("ball-starts");
        if (playerStarts.size() != 3 || ballStarts.size() != 3) throw new IllegalArgumentException("高尔夫需按球场顺序配置各 3 个 course-starts 和 ball-starts");
        for (int index = 0; index < 3; index++) {
            Cuboid bounds = arena.region("course-" + (index + 1));
            Cuboid goal = arena.region("course-" + (index + 1) + "-goal");
            if (!bounds.contains(goal)) throw new IllegalArgumentException("进洞区域必须位于对应球场内");
            for (Location point : List.of(playerStarts.get(index), ballStarts.get(index)))
                if (!bounds.contains(point.getBlockX(), point.getBlockY(), point.getBlockZ())) throw new IllegalArgumentException("球场起点和球位必须位于对应球场内");
            courses.add(new Course(bounds, goal, playerStarts.get(index), ballStarts.get(index)));
        }
        config.integer("selected-course", 1, 3);
        config.integer("turn-seconds", 10, 600);
        config.integer("course-preview-seconds", 1, 30);
        config.integer("shot-cooldown-ticks", 1, 40);
        config.number("control-drink-seconds", .1, 5);
        config.number("hit-reach", 1, 6);
        config.number("knockback.per-level", .05, 2);
        config.number("knockback.vertical", 0, 2);
        config.integer("hole-base-score", 1, 10000);
        config.integer("winner-points", 0, 100);
        for (int level = 1; level <= 6; level++) config.integer("club-energy.level-" + level, 1, 100);
        goals.addAll(config.materials("goal-materials"));
        hazards.addAll(config.materials("hazard-materials"));
    }
    @Override public void start(PartySession session) {
        for (Player player : session.players()) scores.put(player.getUniqueId(), new GolfScorecard());
        courseIndex = session.config.rules.getBoolean("play-all-courses", true) ? 0 : session.config.integer("selected-course", 1, 3) - 1;
        previewCourse(session);
    }
    private void watch(PartySession session, Player player) {
        player.getInventory().clear();
        if (!player.isDead()) {
            player.setGameMode(GameMode.SPECTATOR);
            if (!player.teleport(session.arena.spectator())) throw new IllegalStateException(session.config.text("teleport-failed"));
        }
    }
    private void previewCourse(PartySession session) {
        removeBall();
        current = null;
        List<Player> players = new ArrayList<>(session.survivors());
        Collections.shuffle(players);
        waiting.clear();
        for (Player player : players) { waiting.add(player.getUniqueId()); watch(session, player); }
        nextTurnAt = session.elapsed() + session.config.integer("course-preview-seconds", 1, 30) * 20;
        session.broadcast("golf-course", "course", courseIndex + 1, "seconds", session.config.integer("course-preview-seconds", 1, 30));
    }
    private Player golfer(PartySession session) {
        Player player = current == null ? null : Bukkit.getPlayer(current);
        return player != null && session.active(player) ? player : null;
    }
    private void nextTurn(PartySession session) {
        removeBall();
        Player previous = golfer(session);
        if (previous != null) watch(session, previous);
        current = null;
        Player player = null;
        while (!waiting.isEmpty() && player == null) {
            Player candidate = Bukkit.getPlayer(waiting.removeFirst());
            if (candidate != null && session.active(candidate)) player = candidate;
        }
        if (player == null) {
            if (!session.config.rules.getBoolean("play-all-courses", true) || courseIndex == 2) { timeout(session); return; }
            courseIndex++;
            previewCourse(session);
            return;
        }
        current = player.getUniqueId();
        lastShot = lastControl = -1000;
        turnEnds = session.elapsed() + session.config.integer("turn-seconds", 10, 600) * 20;
        player.setGameMode(GameMode.ADVENTURE);
        back(session, player);
        for (int index = 0; index < CLUBS.length; index++) {
            ItemStack club = session.item(CLUBS[index], "items.club-" + (index + 1));
            club.addUnsafeEnchantment(Enchantment.KNOCKBACK, index + 1);
            player.getInventory().setItem(index, club);
        }
        for (int slot = 6; slot <= 8; slot++) {
            ItemStack control = session.item(Material.POTION, slot == 6 ? "items.back" : slot == 7 ? "items.retry" : "items.skip");
            control.setData(DataComponentTypes.CONSUMABLE, Consumable.consumable()
                    .consumeSeconds((float) session.config.number("control-drink-seconds", .1, 5)).animation(ItemUseAnimation.DRINK).build());
            player.getInventory().setItem(slot, control);
        }
        spawnBall(session);
        session.broadcast("golf-turn", "player", player.getName());
    }
    private void back(PartySession session, Player player) {
        player.setFallDistance(0); player.setFireTicks(0); player.setVelocity(new Vector());
        if (!player.teleport(courses.get(courseIndex).playerStart)) throw new IllegalStateException(session.config.text("teleport-failed"));
    }
    private void removeBall() { if (ball != null) { ball.remove(); ball = null; } }
    private void spawnBall(PartySession session) {
        removeBall();
        ball = session.arena.world().spawn(courses.get(courseIndex).ballStart, Zombie.class, entity -> {
            entity.setAI(false); entity.setSilent(true); entity.setInvulnerable(true); entity.setPersistent(false);
            entity.setGlowing(true); entity.setShouldBurnInDay(false); entity.setCanPickupItems(false);
            entity.setRemoveWhenFarAway(false); entity.customName(session.config.message("ball-name")); entity.setCustomNameVisible(true);
            Objects.requireNonNull(entity.getAttribute(Attribute.KNOCKBACK_RESISTANCE)).setBaseValue(0);
            Objects.requireNonNull(entity.getAttribute(Attribute.MOVEMENT_SPEED)).setBaseValue(0);
            entity.getEquipment().setHelmet(new ItemStack(Material.PLAYER_HEAD));
            entity.getEquipment().setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
            entity.getEquipment().setLeggings(new ItemStack(Material.LEATHER_LEGGINGS));
            entity.getEquipment().setBoots(new ItemStack(Material.LEATHER_BOOTS));
        });
        session.track(ball);
    }
    private void retry(PartySession session, boolean returnPlayer) {
        Player player = golfer(session);
        if (player == null) return;
        scores.get(current).retry(session.config.rules.getBoolean("reset-energy-on-retry", true));
        spawnBall(session);
        if (returnPlayer) back(session, player);
        session.broadcast("golf-retry", "player", player.getName());
    }
    @Override public void tick(PartySession session, int elapsed) {
        if (current == null) { if (elapsed >= nextTurnAt) nextTurn(session); return; }
        Player player = golfer(session);
        if (player == null) { nextTurn(session); return; }
        if (elapsed >= turnEnds) { session.broadcast("golf-turn-timeout", "player", player.getName()); nextTurn(session); return; }
        if (elapsed % 20 == 0) {
            for (Player member : session.survivors()) {
                member.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, 60, 0, true, false));
                member.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 60, 0, true, false));
                member.sendActionBar(session.config.message("golf-status", "player", player.getName(), "seconds", (turnEnds - elapsed + 19) / 20,
                        "energy", scores.get(current).energy()));
            }
            if (ball == null || !ball.isValid() || !inside(courses.get(courseIndex).bounds, ball.getLocation())) { retry(session, false); return; }
            Block floor = ball.getLocation().subtract(0, 1, 0).getBlock();
            if (inside(courses.get(courseIndex).goal, floor.getLocation()) && goals.contains(floor.getType())) {
                int earned = scores.get(current).hole(session.config.integer("hole-base-score", 1, 10000));
                session.broadcast("golf-hole", "player", player.getName(), "score", earned, "total", scores.get(current).total());
                nextTurn(session);
            } else if (hazards.contains(floor.getType())) retry(session, false);
        }
    }
    private static boolean inside(Cuboid region, Location location) { return region.contains(location.getBlockX(), location.getBlockY(), location.getBlockZ()); }
    @Override public void attackEntity(PartySession session, Player player, Entity target) {
        if (!player.getUniqueId().equals(current) || target != ball || session.elapsed() - lastShot < session.config.integer("shot-cooldown-ticks", 1, 40)) return;
        double reach = session.config.number("hit-reach", 1, 6);
        if (player.getEyeLocation().distanceSquared(ball.getLocation().add(0, .9, 0)) > reach * reach || !player.hasLineOfSight(ball)) return;
        int slot = player.getInventory().getHeldItemSlot();
        if (slot < 0 || slot >= CLUBS.length || player.getInventory().getItemInMainHand().getType() != CLUBS[slot]) return;
        Vector direction = player.getLocation().getDirection().setY(0);
        if (direction.lengthSquared() < .001) return;
        lastShot = session.elapsed();
        GolfScorecard score = scores.get(current);
        int cost = session.config.integer("club-energy.level-" + (slot + 1), 1, 100);
        score.shot(cost);
        ball.setVelocity(direction.normalize().multiply((slot + 1) * session.config.number("knockback.per-level", .05, 2))
                .setY(session.config.number("knockback.vertical", 0, 2)));
        player.sendMessage(session.config.message("golf-shot", "cost", cost, "energy", score.energy()));
    }
    @Override public void consume(PartySession session, PlayerItemConsumeEvent event) {
        event.setCancelled(true); // Retain the reusable control potion; no glass bottle or potion effect.
        if (event.getHand() != EquipmentSlot.HAND || event.getItem().getType() != Material.POTION || !event.getPlayer().getUniqueId().equals(current)) return;
        int slot = event.getPlayer().getInventory().getHeldItemSlot();
        if (slot < 6 || slot > 8) return;
        if (session.elapsed() - lastControl < 20) return;
        lastControl = session.elapsed();
        if (slot == 6) back(session, event.getPlayer());
        if (slot == 7) retry(session, true);
        if (slot == 8) { session.broadcast("golf-skipped", "player", event.getPlayer().getName()); nextTurn(session); }
    }
    @Override public void hazard(PartySession session, Player player) {
        if (player.isDead()) { session.eliminate(player); return; }
        if (player.getUniqueId().equals(current)) back(session, player);
        else watch(session, player);
    }
    @Override public void timeout(PartySession session) {
        int best = Math.max(0, session.players().stream().mapToInt(p -> scores.getOrDefault(p.getUniqueId(), new GolfScorecard()).total()).max().orElse(0));
        List<Player> winners = new ArrayList<>();
        for (Player player : session.players()) {
            int total = scores.getOrDefault(player.getUniqueId(), new GolfScorecard()).total();
            session.broadcast("golf-total", "player", player.getName(), "score", total);
            if (total == best) { winners.add(player); session.points(player, session.config.integer("winner-points", 0, 100)); }
            else session.points(player, 0);
        }
        session.finish(winners);
    }
}
