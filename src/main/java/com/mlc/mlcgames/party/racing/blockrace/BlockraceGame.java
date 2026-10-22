package com.mlc.mlcgames.party.racing.blockrace;

import com.mlc.mlcgames.party.core.Cuboid;
import com.mlc.mlcgames.party.core.PartyConfig;
import com.mlc.mlcgames.party.core.PartySession;
import com.mlc.mlcgames.party.racing.common.RaceGame;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Locale;

/** Source blockrace: survive, gather and craft while crossing the course. */
public final class BlockraceGame extends RaceGame {
    private Cuboid finish;
    private Cuboid reset;
    private int deadline;

    @Override public void validate(PartyConfig config, PartyConfig.Arena arena) {
        finish = arena.region("finish");
        reset = arena.region("reset");
        arena.region("build");
        if (Material.matchMaterial("TEST_BLOCK") == null) throw new IllegalArgumentException("当前服务端缺少 TEST_BLOCK 方块");
        config.integer("max-seconds", 10, maximumDurationSeconds());
        config.integer("shorten-after-finishers", 1, 64);
        config.integer("shortened-seconds", 1, 600);
        config.integer("finish-points.first", 0, 100);
        config.integer("finish-points.second", 0, 100);
        config.integer("finish-points.third", 0, 100);
        config.integer("finish-points.remaining", 0, 100);
    }

    @Override public List<Cuboid> writableRegions(PartyConfig.Arena arena) {
        return List.of(arena.region("build"));
    }

    @Override public void start(PartySession session) {
        super.start(session);
        deadline = session.config.integer("max-seconds", 10, maximumDurationSeconds()) * 20;
        for (Player player : session.survivors()) {
            player.setGameMode(GameMode.SURVIVAL);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "recipe give " + player.getName() + " *");
        }
    }

    @Override public void tick(PartySession session, int elapsed) {
        if (elapsed % 20 == 0) {
            int seconds = Math.max(0, (deadline - elapsed + 19) / 20);
            for (Player player : session.players()) player.sendActionBar(session.config.message("race-status", "seconds", seconds, "finishers", order.count()));
            for (Player player : session.survivors()) player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 40, 1, true, false));
        }

        for (Player player : List.copyOf(session.survivors())) {
            Location location = player.getLocation();
            if (reset.contains(location.getBlockX(), location.getBlockY(), location.getBlockZ())) {
                returnToStart(session, player);
                continue;
            }
            Block feet = location.clone().subtract(0, 1, 0).getBlock();
            String state = feet.getBlockData().getAsString().toLowerCase(Locale.ROOT);
            if (finish.contains(feet.getX(), feet.getY(), feet.getZ())
                    && feet.getType().name().equals("TEST_BLOCK")
                    && state.endsWith("[mode=accept]")) finish(session, player);
        }

        if (session.survivors().isEmpty() || elapsed >= deadline) timeout(session);
    }

    private void finish(PartySession session, Player player) {
        int rank = order.finish(player.getUniqueId());
        if (rank == 0) return;
        int points = pointsFor(session, rank);
        session.points(player, points);
        session.retire(player);
        session.broadcast("race-finished", "player", player.getName(), "rank", rank, "points", points);

        if (rank == session.config.integer("shorten-after-finishers", 1, 64)) {
            int seconds = session.config.integer("shortened-seconds", 1, 600);
            if (deadline - session.elapsed() >= seconds * 20) {
                deadline = session.elapsed() + seconds * 20;
                session.broadcast("race-shortened", "seconds", seconds);
            }
        }
    }

    private static int pointsFor(PartySession session, int rank) {
        String place = switch (rank) {
            case 1 -> "first";
            case 2 -> "second";
            case 3 -> "third";
            default -> "remaining";
        };
        return session.config.integer("finish-points." + place, 0, 100);
    }

    @Override public void hazard(PartySession session, Player player) {
        if (player.isDead()) { session.eliminate(player); return; }
        returnToStart(session, player);
    }

    private void returnToStart(PartySession session, Player player) {
        Location start = starts.get(player.getUniqueId());
        if (start == null) throw new IllegalStateException("跑酷起点缺失");
        player.setFallDistance(0);
        player.setFireTicks(0);
        player.setVelocity(new Vector());
        player.setHealth(player.getAttribute(Attribute.MAX_HEALTH).getValue());
        player.setFoodLevel(20);
        player.setSaturation(20);
        if (!player.teleport(start)) throw new IllegalStateException(session.config.text("teleport-failed"));
        player.playSound(start, Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
        player.sendMessage(session.config.message("race-retry"));
    }

    @Override public boolean nativeBreak(PartySession session, Player player, Block block) { return true; }
    @Override public boolean placeBlock(PartySession session, Player player, Block block) { return true; }
    @Override public boolean allowInventory(PartySession session, Player player) { return true; }
    @Override public boolean allowBlockInteraction(PartySession session, Player player, Block block) { return true; }
    @Override public boolean allowPickup(PartySession session, Player player, Item item) { return true; }
    @Override public boolean allowDrop(PartySession session, Player player, ItemStack item) { return true; }
    @Override public boolean allowSwap(PartySession session, Player player) { return true; }
    @Override public boolean allowNaturalChanges(PartySession session) { return true; }
    @Override public boolean maintainFood(PartySession session) { return false; }
    @Override public void fish(PartySession session, PlayerFishEvent event) {}
    @Override public boolean retainFishingHook() { return true; }

    @Override public void environmentalDamage(PartySession session, Player player, EntityDamageEvent event) {
        event.setCancelled(false);
    }

    @Override public boolean lethalDamage(PartySession session, Player player, EntityDamageEvent event) {
        hazard(session, player);
        return true;
    }

    @Override public void hit(PartySession session, Player attacker, Player victim, EntityDamageByEntityEvent event) {
        // Source blockrace disables friendly fire for everyone in its game team.
    }
}
