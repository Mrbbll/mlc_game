package com.mlc.mlcgames.party.arcade.fishing;

import com.mlc.mlcgames.party.core.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/** Source map_game_3rd/fishing; native bites, source loot pools and immediate coin conversion. */
public final class FishingGame implements PartyGame {
    private final Map<UUID, Integer> coins = new LinkedHashMap<>();
    private final Map<UUID, Location> starts = new HashMap<>();
    private final Map<UUID, FishHook> hooks = new HashMap<>();
    private FishingLoot loot;
    private Cuboid water;
    private World fishingWorld;

    @Override public boolean lastPlayerWins() { return false; }
    @Override public boolean retainFishingHook() { return true; }
    @Override public void validate(PartyConfig config, PartyConfig.Arena arena) {
        water = arena.region("fishing-water");
        fishingWorld = arena.world();
        config.integer("lure-level", 0, 5);
        config.integer("rank-interval-seconds", 1, 120);
        for (String rank : List.of("first", "second", "third")) config.integer("rank-points." + rank, 0, 100);
        loot = new FishingLoot(pool(config, "fish"), pool(config, "junk"), pool(config, "treasure"), config.number("treasure-condition-chance", 0, 1));
    }
    private FishingLoot.Pool pool(PartyConfig config, String name) {
        List<FishingLoot.Entry> entries = new ArrayList<>();
        for (Map<?, ?> row : config.rules.getMapList("loot." + name)) {
            Object materialName = row.get("material");
            Material material = materialName instanceof String s ? Material.matchMaterial(s) : null;
            if (material == null || !material.isItem() || material.isAir() || !(row.get("weight") instanceof Number n)
                    || n.doubleValue() != n.intValue()) throw new IllegalArgumentException("loot." + name + " 必须配置有效 material 和整数 weight");
            Object jungleOnly = row.get("jungle-only");
            if (jungleOnly != null && !(jungleOnly instanceof Boolean)) throw new IllegalArgumentException("jungle-only 必须为布尔值");
            int value = config.integer("coin-values." + material.name(), 0, 1000);
            config.text("catches." + material.name());
            entries.add(new FishingLoot.Entry(new FishingLoot.Catch(material.name(), value), n.intValue(), Boolean.TRUE.equals(jungleOnly)));
        }
        return new FishingLoot.Pool(config.integer("pool-weights." + name, 1, 10000), entries);
    }
    @Override public void start(PartySession session) {
        for (Player player : session.survivors()) {
            coins.put(player.getUniqueId(), 0);
            starts.put(player.getUniqueId(), player.getLocation().clone());
            player.setTotalExperience(0); player.setLevel(0); player.setExp(0);
            rod(session, player);
        }
    }
    private void rod(PartySession session, Player player) {
        if (player.getInventory().contains(Material.FISHING_ROD)) return;
        ItemStack rod = session.item(Material.FISHING_ROD, "items.rod");
        int level = session.config.integer("lure-level", 0, 5);
        if (level > 0) rod.addUnsafeEnchantment(Enchantment.LURE, level);
        player.getInventory().setItem(0, rod);
    }
    private boolean inWaterArea(Location point) { return fishingWorld.equals(point.getWorld()) && water.contains(point.getBlockX(), point.getBlockY(), point.getBlockZ()); }
    @Override public void fish(PartySession session, PlayerFishEvent event) {
        Player player = event.getPlayer();
        FishHook hook = event.getHook();
        if (event.getState() == PlayerFishEvent.State.FISHING) {
            FishHook previous = hooks.put(player.getUniqueId(), hook);
            if (previous != null && previous != hook) previous.remove();
            session.track(hook);
            return;
        }
        if (hooks.get(player.getUniqueId()) != hook) { event.setCancelled(true); hook.remove(); return; }
        if (event.getState() == PlayerFishEvent.State.CAUGHT_ENTITY) {
            event.setCancelled(true); hook.remove(); hooks.remove(player.getUniqueId()); return;
        }
        if (event.getState() == PlayerFishEvent.State.BITE && !inWaterArea(hook.getLocation())) { event.setCancelled(true); return; }
        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH) return;
        event.setCancelled(true);
        event.setExpToDrop(0);
        if (event.getCaught() != null) event.getCaught().remove();
        Location caughtAt = hook.getLocation();
        hook.remove(); hooks.remove(player.getUniqueId());
        if (!inWaterArea(caughtAt)) { player.sendMessage(session.config.message("outside-fishing-water")); return; }
        String biome = caughtAt.getBlock().getBiome().getKey().toString();
        FishingLoot.Catch caught = loot.draw(ThreadLocalRandom.current(), Set.of("minecraft:jungle", "minecraft:sparse_jungle", "minecraft:bamboo_jungle").contains(biome));
        int total = coins.merge(player.getUniqueId(), caught.coins(), Math::addExact);
        // Source converts catches immediately; equipment/enchantments on caught items never enter play.
        player.sendMessage(session.config.message("fishing-caught", "catch", session.config.text("catches." + caught.material()), "coins", caught.coins(), "total", total));
        player.setLevel(total);
        ItemStack coin = session.item(Material.SUNFLOWER, "items.coin");
        ItemMeta meta = coin.getItemMeta(); meta.setMaxStackSize(99); coin.setItemMeta(meta);
        int remaining = caught.coins();
        while (remaining > 0) { coin.setAmount(Math.min(99, remaining)); player.getInventory().addItem(coin.clone()); remaining -= coin.getAmount(); }
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
    }
    @Override public void tick(PartySession session, int elapsed) {
        for (Iterator<Map.Entry<UUID, FishHook>> it = hooks.entrySet().iterator(); it.hasNext();) {
            FishHook hook = it.next().getValue();
            if (!hook.isValid() || !session.arena.contains(hook.getLocation())) { hook.remove(); it.remove(); }
        }
        if (session.survivors().size() <= 1) { timeout(session); return; }
        if (elapsed % 20 == 0) for (Player player : session.survivors()) {
            rod(session, player);
            player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, 60, 0, true, false));
            player.sendActionBar(session.config.message("fishing-status", "coins", coins.getOrDefault(player.getUniqueId(), 0),
                    "seconds", Math.max(0, session.config.integer("max-seconds", 10, 7200) - elapsed / 20)));
        }
        if (elapsed % (session.config.integer("rank-interval-seconds", 1, 120) * 20) == 0) ranks(session, false);
    }
    private List<Rankings.Row<UUID>> ranks(PartySession session, boolean award) {
        Map<UUID, Integer> eligible = new LinkedHashMap<>();
        for (Player player : session.players()) eligible.put(player.getUniqueId(), coins.getOrDefault(player.getUniqueId(), 0));
        List<Rankings.Row<UUID>> ranks = Rankings.descending(eligible);
        for (Rankings.Row<UUID> row : ranks) {
            Player player = Bukkit.getPlayer(row.id());
            if (player == null) continue;
            if (row.rank() <= 3) session.broadcast("fishing-rank", "rank", row.rank(), "player", player.getName(), "coins", row.score());
            if (award) session.points(player, row.rank() <= 3 ? session.config.integer("rank-points." + (row.rank() == 1 ? "first" : row.rank() == 2 ? "second" : "third"), 0, 100) : 0);
        }
        return ranks;
    }
    @Override public void timeout(PartySession session) {
        Set<UUID> winners = new HashSet<>();
        for (Rankings.Row<UUID> row : ranks(session, true)) if (row.rank() == 1) winners.add(row.id());
        session.finish(session.players().stream().filter(p -> winners.contains(p.getUniqueId())).toList());
    }
    @Override public void eliminated(PartySession session, Player player) {
        FishHook hook = hooks.remove(player.getUniqueId());
        if (hook != null) hook.remove();
    }
    @Override public void hazard(PartySession session, Player player) {
        if (player.isDead()) { session.eliminate(player); return; }
        player.setFallDistance(0); player.setFireTicks(0); player.setVelocity(new Vector());
        if (!player.teleport(starts.get(player.getUniqueId()))) throw new IllegalStateException(session.config.text("teleport-failed"));
    }
}
