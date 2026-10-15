package com.mlc.mlcgames.party.core;

import com.mlc.mlcgames.party.PartyModule;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.util.*;

public final class PartySession {
    public enum State { WAITING, PREPARING, COUNTDOWN, RUNNING, RESETTING, ENDING }
    public final PartyConfig config;
    public final PartyConfig.Arena arena;
    public final PartyGame game;
    private final PartyModule module;
    private final Set<UUID> participants = new LinkedHashSet<>();
    private final Set<UUID> alive = new LinkedHashSet<>();
    private final Set<UUID> entities = new HashSet<>();
    private final List<Cuboid> writable;
    private BlockJournal journal;
    private boolean baselineReady;
    private State state = State.WAITING;
    private Runnable afterReset;
    private int ticks;
    private int elapsed;
    private int mutationTick = -1;
    private int applied;
    private final Map<Block, Material> changes = new LinkedHashMap<>();
    private final Map<UUID, Integer> scores = new LinkedHashMap<>();
    private Set<UUID> winners = Set.of();
    private boolean settled;

    public PartySession(PartyModule module, PartyConfig config, PartyConfig.Arena arena, PartyGame game) {
        this.module = module;
        this.config = config;
        this.arena = arena;
        this.game = game;
        game.validate(config, arena);
        writable = List.copyOf(game.writableRegions(arena));
        config.integer("players.min", 2, config.integer("players.max", 2, 64));
        config.integer("countdown-seconds", 1, 60);
        config.integer("max-seconds", 10, game.maximumDurationSeconds());
        config.integer("limits.blocks-per-tick", 1, 2000);
        config.integer("limits.max-snapshot-blocks", 1, 100000);
        config.integer("elimination-depth", 1, 2);
    }

    public State state() { return state; }
    public int elapsed() { return elapsed; }
    public boolean active(Player player) { return alive.contains(player.getUniqueId()) && state == State.RUNNING; }
    public boolean member(Player player) { return participants.contains(player.getUniqueId()); }
    public List<Player> players() { return participants.stream().map(Bukkit::getPlayer).filter(Objects::nonNull).toList(); }
    public List<Player> survivors() { return alive.stream().map(Bukkit::getPlayer).filter(Objects::nonNull).toList(); }
    public boolean writable(Block block) {
        return arena.world().equals(block.getWorld()) && writable.stream().anyMatch(r -> r.contains(block.getX(), block.getY(), block.getZ()));
    }
    public void set(Block block, Material material) {
        if (journal == null || !writable(block)) throw new IllegalArgumentException("尝试修改未声明区域 " + block.getLocation());
        if (mutationTick != Bukkit.getCurrentTick()) { mutationTick = Bukkit.getCurrentTick(); applied = 0; }
        if (applied < config.integer("limits.blocks-per-tick", 1, 2000)) {
            changes.remove(block);
            block.setType(material, game.physicsOnWrite(this));
            applied++;
        } else changes.put(block, material);
    }
    public void track(Entity entity) { entities.add(entity.getUniqueId()); entity.addScoreboardTag(PartyModule.ENTITY_TAG); }
    public boolean owns(Entity entity) { return entities.contains(entity.getUniqueId()); }
    public Map<UUID, Integer> scores() { return Map.copyOf(scores); }
    public boolean settled() { return settled; }
    public Set<UUID> winners() { return winners; }
    public void points(Player player, int value) {
        if (value < 0 || value > 10000 || !member(player)) throw new IllegalArgumentException("非法比赛积分");
        scores.put(player.getUniqueId(), value);
    }
    public void retire(Player player) {
        if (!alive.remove(player.getUniqueId())) return;
        player.leaveVehicle();
        player.getInventory().clear();
        player.setGlowing(false);
        if (!player.isDead()) { player.setGameMode(GameMode.SPECTATOR); player.teleport(arena.spectator()); }
    }

    public void join(Player player) {
        if (state != State.WAITING) throw new IllegalStateException(config.text("already-running"));
        if (participants.size() >= config.integer("players.max", 2, 64)) throw new IllegalStateException(config.text("full"));
        player.closeInventory();
        module.recovery().capture(player, arena.exit());
        participants.add(player.getUniqueId());
        player.getInventory().clear();
        player.clearActivePotionEffects();
        player.setGlowing(false);
        player.setFireTicks(0);
        player.setFallDistance(0);
        player.setGameMode(GameMode.ADVENTURE);
        player.setAllowFlight(false);
        if (!player.teleport(arena.waiting())) {
            participants.remove(player.getUniqueId());
            module.recovery().restore(player);
            throw new IllegalStateException(config.text("teleport-failed"));
        }
        broadcast("joined", "player", player.getName(), "count", participants.size());
    }

    public void start() {
        if (state != State.WAITING) throw new IllegalStateException(config.text("already-running"));
        if (participants.size() < config.integer("players.min", 2, 64)) throw new IllegalStateException(config.text("not-enough"));
        journal = new BlockJournal(new File(module.plugin().getDataFolder(), "party/recovery/journal.yml"), arena.world(), writable,
                config.integer("limits.max-snapshot-blocks", 1, 100000));
        state = State.PREPARING;
        broadcast("preparing");
    }

    public void tick() throws Exception {
        int budget = config.integer("limits.blocks-per-tick", 1, 2000);
        if (state != State.ENDING && state != State.RESETTING) {
            // Defer excess writes instead of allowing a volley of arrows to spike one tick.
            List<Map.Entry<Block, Material>> queued = new ArrayList<>(changes.entrySet());
            for (Map.Entry<Block, Material> entry : queued.subList(0, Math.min(queued.size(), budget))) set(entry.getKey(), entry.getValue());
        }
        if (state == State.PREPARING) {
            if (!baselineReady) baselineReady = journal.capture(budget);
            if (baselineReady && game.prepare(this, budget) && changes.isEmpty()) { state = State.COUNTDOWN; ticks = config.integer("countdown-seconds", 1, 60) * 20; }
            return;
        }
        if (state == State.RESETTING || state == State.ENDING) {
            if (journal == null || journal.restore(budget)) {
                if (state == State.RESETTING) { state = State.RUNNING; afterReset.run(); afterReset = null; }
                else {
                    if (journal != null) journal.delete();
                    module.finished(this);
                }
            }
            return;
        }
        if (state == State.COUNTDOWN) {
            if (ticks % 20 == 0) broadcast("countdown", "seconds", ticks / 20);
            if (--ticks <= 0) {
                state = State.RUNNING;
                alive.addAll(participants);
                resetPlayers();
                broadcast("started");
                broadcast("instructions");
                game.start(this);
            }
            return;
        }
        if (state != State.RUNNING) return;
        elapsed++;
        // Evaluate all players first so simultaneous elimination yields a draw.
        for (Player player : List.copyOf(survivors())) {
            boolean eliminated = !arena.contains(player.getLocation()) || player.isDead();
            for (int depth = 1; depth <= config.integer("elimination-depth", 1, 2); depth++) eliminated |= config.rules.getStringList("elimination-blocks").contains(player.getLocation().subtract(0, depth, 0).getBlock().getType().name());
            if (eliminated) game.hazard(this, player);
        }
        if (game.lastPlayerWins() && alive.size() <= 1) { finish(survivors()); return; }
        if (elapsed >= config.integer("max-seconds", 10, game.maximumDurationSeconds()) * 20) { game.timeout(this); return; }
        for (Player player : survivors()) {
            player.setFoodLevel(20);
            player.setSaturation(20);
            if (elapsed % 20 == 0) player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 400, 0, true, false));
        }
        game.tick(this, elapsed);
    }

    public void resetPlayers() {
        alive.clear();
        List<Location> spawns = new ArrayList<>(arena.spawns());
        Collections.shuffle(spawns);
        int index = 0;
        for (Player player : players()) {
            if (player.isDead()) throw new IllegalStateException("玩家仍在死亡重生流程中");
            player.getInventory().clear();
            player.clearActivePotionEffects();
            player.setGlowing(false);
            player.setGameMode(GameMode.ADVENTURE);
            player.setAllowFlight(false);
            player.setHealth(Objects.requireNonNull(player.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH)).getValue());
            player.setFireTicks(0);
            player.setFallDistance(0);
            if (!player.teleport(spawns.get(index++))) throw new IllegalStateException(config.text("teleport-failed"));
            alive.add(player.getUniqueId());
        }
    }

    public void eliminate(Player player) {
        if (!alive.remove(player.getUniqueId())) return;
        player.leaveVehicle();
        for (UUID id : List.copyOf(entities)) {
            Entity entity = Bukkit.getEntity(id);
            if (entity instanceof org.bukkit.entity.Projectile projectile && player.equals(projectile.getShooter())) { entity.remove(); entities.remove(id); }
        }
        player.getInventory().clear();
        player.setGlowing(false);
        if (!player.isDead()) {
            player.setGameMode(GameMode.SPECTATOR);
            player.teleport(arena.spectator());
        }
        broadcast("eliminated", "player", player.getName());
        game.eliminated(this, player);
    }

    public void leave(Player player) {
        eliminate(player);
        participants.remove(player.getUniqueId());
        module.recovery().restore(player);
        if (participants.isEmpty()) stop();
        else if ((state == State.COUNTDOWN || state == State.PREPARING) && participants.size() < config.integer("players.min", 2, 64)) stop();
    }

    public void finish(Collection<Player> winners) {
        if (state == State.ENDING) return;
        this.winners = Set.copyOf(winners.stream().map(Player::getUniqueId).toList());
        settled = true;
        String names = winners.isEmpty() ? config.text("draw") : String.join(", ", winners.stream().map(Player::getName).toList());
        broadcast("finished", "winners", names);
        stop();
    }

    public void stop() {
        if (state == State.ENDING) return;
        // A partially captured baseline has never been used to mutate the world.
        if (state == State.PREPARING && !baselineReady) journal = null;
        state = State.ENDING;
        changes.clear();
        alive.clear();
        cleanEntities();
        for (Player player : players()) {
            try { module.recovery().restore(player); }
            catch (RuntimeException e) { module.plugin().getLogger().warning("派对玩家恢复延后：" + e.getMessage()); }
        }
        if (journal != null) journal.rewind();
    }

    public void resetRound(Runnable next) {
        state = State.RESETTING;
        changes.clear();
        afterReset = next;
        cleanEntities();
        for (Player player : players()) {
            if (!player.isDead()) { player.setGameMode(GameMode.SPECTATOR); player.teleport(arena.spectator()); }
        }
        journal.rewind();
    }

    public void shutdown() {
        stop();
        // Disable has no future ticks. Finish bounded journal restoration now.
        if (journal != null) { journal.restore(Integer.MAX_VALUE); try { journal.delete(); } catch (Exception e) { module.plugin().getLogger().warning(e.getMessage()); } }
    }

    private void cleanEntities() {
        for (UUID id : entities) { Entity entity = Bukkit.getEntity(id); if (entity != null) entity.remove(); }
        entities.clear();
    }

    public void broadcast(String key, Object... values) {
        for (Player player : players()) player.sendMessage(config.message(key, values));
    }

    public ItemStack item(Material material, String key) {
        ItemStack item = new ItemStack(material);
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        meta.displayName(config.message(key));
        meta.setUnbreakable(true);
        item.setItemMeta(meta);
        return item;
    }
}
