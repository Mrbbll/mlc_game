package com.mlc.mlcgames.party;

import com.mlc.mlcgames.Mlcgames;
import com.mlc.mlcgames.dungeongame.Dungeongame;
import com.mlc.mlcgames.sandgame.Sandgame;
import com.mlc.mlcgames.zombieday.Zombiedaygame;
import com.mlc.mlcgames.party.core.*;
import com.mlc.mlcgames.party.arcade.mining.MiningGame;
import com.mlc.mlcgames.party.building.build_guess.BuildGuessGame;
import com.mlc.mlcgames.party.building.build_parkour.BuildParkourGame;
import com.mlc.mlcgames.party.combat.battle.BattleGame;
import com.mlc.mlcgames.party.combat.bedwars.BedWarsGame;
import com.mlc.mlcgames.party.combat.blaze_capture.BlazeCaptureGame;
import com.mlc.mlcgames.party.combat.blaze_police.BlazePoliceGame;
import com.mlc.mlcgames.party.combat.blaze_team.BlazeTeamGame;
import com.mlc.mlcgames.party.combat.cloud.CloudGame;
import com.mlc.mlcgames.party.combat.duel.DuelGame;
import com.mlc.mlcgames.party.combat.hunger.HungerGame;
import com.mlc.mlcgames.party.combat.jobpvp.JobPvpGame;
import com.mlc.mlcgames.party.combat.onearrow.OneArrowGame;
import com.mlc.mlcgames.party.combat.pillar.PillarGame;
import com.mlc.mlcgames.party.combat.sheepwars.SheepWarsGame;
import com.mlc.mlcgames.party.combat.surgame.SurvivalGame;
import com.mlc.mlcgames.party.combat.tntwars.TntWarsGame;
import com.mlc.mlcgames.party.cooperation.bomb.BombGame;
import com.mlc.mlcgames.party.cooperation.foodparty.FoodPartyGame;
import com.mlc.mlcgames.party.deduction.endless_wolf.EndlessWolfGame;
import com.mlc.mlcgames.party.deduction.hideseek.HideSeekGame;
import com.mlc.mlcgames.party.deduction.killerever.KillerEverGame;
import com.mlc.mlcgames.party.survival.believer.BelieverGame;
import com.mlc.mlcgames.party.survival.repel.RepelGame;
import com.mlc.mlcgames.party.survival.hotpotever.HotPotatoGame;
import com.mlc.mlcgames.party.survival.tntrun.TntRunGame;
import com.mlc.mlcgames.party.survival.spleef.SpleefGame;
import com.mlc.mlcgames.party.survival.bowspleef.BowSpleefGame;
import com.mlc.mlcgames.party.survival.snowmix.SnowMixGame;
import com.mlc.mlcgames.party.combat.woolwars.WoolWarsGame;
import com.mlc.mlcgames.party.exploration.finder.FinderGame;
import com.mlc.mlcgames.party.racing.elytra.ElytraGame;
import com.mlc.mlcgames.party.racing.mutouren.MutourenGame;
import com.mlc.mlcgames.party.racing.parkourrace.ParkourRaceGame;
import com.mlc.mlcgames.party.racing.blockrace.BlockraceGame;
import com.mlc.mlcgames.party.racing.randomparkour.RandomParkourGame;
import com.mlc.mlcgames.party.reaction.chair.ChairGame;
import com.mlc.mlcgames.party.reaction.color.ColorGame;
import com.mlc.mlcgames.party.reaction.tsays.TSaysGame;
import com.mlc.mlcgames.party.sports.poolwar.PoolWarGame;
import com.mlc.mlcgames.party.sports.dodgeball.DodgeballGame;
import com.mlc.mlcgames.party.sports.football.FootballGame;
import com.mlc.mlcgames.party.racing.dropper.DropperGame;
import com.mlc.mlcgames.party.sports.golf.GolfGame;
import com.mlc.mlcgames.party.arcade.fishing.FishingGame;
import com.mlc.mlcgames.party.arcade.miner.MinerGame;
import com.mlc.mlcgames.party.racing.car_race.PigRaceGame;
import com.mlc.mlcgames.party.survival.disastersnow.DisasterSnowGame;
import com.mlc.mlcgames.party.survival.firstoutwins.FirstOutWinsGame;
import com.mlc.mlcgames.party.survival.live.LiveGame;
import com.mlc.mlcgames.party.survival.pacman.PacmanGame;
import com.mlc.mlcgames.party.survival.sneak.SneakGame;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.event.world.EntitiesLoadEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import io.papermc.paper.event.player.PrePlayerAttackEntityEvent;
import com.destroystokyo.paper.event.player.PlayerElytraBoostEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.util.*;
import java.util.function.Supplier;

/** Additive entry point. Existing games and their listeners are not rewritten. */
public final class PartyModule implements Listener, TabExecutor {
    public static final String ENTITY_TAG = "mlcgames_party_owned";
    private record Definition(String path, Supplier<PartyGame> factory) {}
    private final JavaPlugin plugin;
    private final Map<String, Definition> definitions = new LinkedHashMap<>();
    private final PlayerRecovery recovery;
    private final BukkitTask ticker;
    private YamlConfiguration language;
    private PartySession session;
    private BlockJournal crashRecovery;
    private boolean recoveryBlocked;

    public PartyModule(JavaPlugin plugin) {
        this.plugin = plugin;
        recovery = new PlayerRecovery(plugin);
        register("believer", "survival", BelieverGame::new);
        register("repel", "survival", RepelGame::new);
        register("hotpotever", "survival", HotPotatoGame::new);
        register("tntrun", "survival", TntRunGame::new);
        register("spleef", "survival", SpleefGame::new);
        register("bowspleef", "survival", BowSpleefGame::new);
        register("snowmix", "survival", SnowMixGame::new);
        register("woolwars", "combat", WoolWarsGame::new);
        register("finder", "exploration", FinderGame::new);
        register("elytra", "racing", ElytraGame::new);
        register("mutouren", "racing", MutourenGame::new);
        register("parkourrace", "racing", ParkourRaceGame::new);
        register("chair", "reaction", ChairGame::new);
        register("poolwar", "sports", PoolWarGame::new);
        register("dropper", "racing", DropperGame::new);
        register("golf", "sports", GolfGame::new);
        register("fishing", "arcade", FishingGame::new);
        register("miner", "arcade", MinerGame::new);
        register("car_race", "racing", PigRaceGame::new);
        register("build_guess", "building", BuildGuessGame::new);
        register("build_parkour", "building", BuildParkourGame::new);
        register("battle", "combat", BattleGame::new);
        register("bedwars", "combat", BedWarsGame::new);
        register("blaze_capture", "combat", BlazeCaptureGame::new);
        register("blaze_police", "combat", BlazePoliceGame::new);
        register("blaze_team", "combat", BlazeTeamGame::new);
        register("cloud", "combat", CloudGame::new);
        register("duel", "combat", DuelGame::new);
        register("hunger", "combat", HungerGame::new);
        register("job_pvp", "combat", JobPvpGame::new);
        register("one_arrow", "combat", OneArrowGame::new);
        register("pillar", "combat", PillarGame::new);
        register("sheepwars", "combat", SheepWarsGame::new);
        register("surgame", "combat", SurvivalGame::new);
        register("tntwars", "combat", TntWarsGame::new);
        register("bomb", "cooperation", BombGame::new);
        register("foodparty", "cooperation", FoodPartyGame::new);
        register("endless_wolf", "deduction", EndlessWolfGame::new);
        register("hideseek", "deduction", HideSeekGame::new);
        register("killerever", "deduction", KillerEverGame::new);
        register("mining", "arcade", MiningGame::new);
        register("blockrace", "racing", BlockraceGame::new);
        register("random_parkour", "racing", RandomParkourGame::new);
        register("color", "reaction", ColorGame::new);
        register("tsays", "reaction", TSaysGame::new);
        register("dodgeball", "sports", DodgeballGame::new);
        register("football", "sports", FootballGame::new);
        register("disastersnow", "survival", DisasterSnowGame::new);
        register("firstoutwins", "survival", FirstOutWinsGame::new);
        register("live", "survival", LiveGame::new);
        register("pacman", "survival", PacmanGame::new);
        register("sneak", "survival", SneakGame::new);
        language = PartyConfig.load(plugin, "party/language.yml");
        File journal = new File(plugin.getDataFolder(), "party/recovery/journal.yml");
        if (journal.exists()) {
            try { crashRecovery = BlockJournal.recover(journal); }
            catch (Exception e) { recoveryBlocked = true; plugin.getLogger().severe(e.getMessage()); }
        }
        Bukkit.getPluginManager().registerEvents(this, plugin);
        for (World world : Bukkit.getWorlds()) for (Entity entity : world.getEntities()) if (entity.getScoreboardTags().contains(ENTITY_TAG)) entity.remove();
        PluginCommand command = Objects.requireNonNull(plugin.getCommand("party"));
        command.setExecutor(this);
        command.setTabCompleter(this);
        ticker = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 1, 1);
        for (Player player : Bukkit.getOnlinePlayers()) recoverLater(player);
    }

    private void register(String id, String category, Supplier<PartyGame> factory) {
        definitions.put(id, new Definition("party/" + category + "/" + id, factory));
        // Install defaults, but incomplete arena coordinates never prevent plugin startup.
        try { configuration(id); }
        catch (RuntimeException e) { plugin.getLogger().warning(id + ": " + e.getMessage()); }
    }

    public JavaPlugin plugin() { return plugin; }
    public PlayerRecovery recovery() { return recovery; }
    public void finished(PartySession ending) {
        if (session != ending) return;
        if (ending.settled()) {
            try {
                YamlConfiguration result = new YamlConfiguration();
                result.set("game", ending.config.id);
                result.set("finished-at", java.time.Instant.now().toString());
                result.set("winners", ending.winners().stream().map(UUID::toString).toList());
                for (Map.Entry<UUID, Integer> score : ending.scores().entrySet()) result.set("points." + score.getKey(), score.getValue());
                File file = new File(plugin.getDataFolder(), "party/results/latest.yml");
                java.nio.file.Files.createDirectories(file.getParentFile().toPath());
                result.save(file);
            } catch (Exception e) { plugin.getLogger().warning("派对结果记录失败：" + e.getMessage()); }
        }
        session = null;
    }

    private PartyConfig configuration(String id) {
        Definition definition = definitions.get(id);
        if (definition == null) throw new IllegalArgumentException(text("unknown-game", "game", id));
        return new PartyConfig(plugin, id, definition.path);
    }

    private String text(String key, Object... values) {
        String result = language.getString(key, key);
        for (int i = 0; i < values.length; i += 2) result = result.replace("{" + values[i] + "}", String.valueOf(values[i + 1]));
        return result;
    }

    private void message(CommandSender sender, String key, Object... values) {
        sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(text(key, values)));
    }

    private boolean legacyBusy() {
        return Mlcgames.bankgame.isStart || Zombiedaygame.isstart || Sandgame.isstart || Dungeongame.isstart;
    }

    private void tick() {
        try {
            if (crashRecovery != null) {
                if (crashRecovery.restore(1000)) { crashRecovery.delete(); crashRecovery = null; }
                return;
            }
            if (session != null) {
                if (legacyBusy() && session.state() != PartySession.State.ENDING) { session.broadcast("legacy-busy"); session.stop(); }
                session.tick();
            }
        } catch (Exception e) {
            plugin.getLogger().log(java.util.logging.Level.SEVERE, "派对处理失败，保留恢复记录", e);
            if (crashRecovery != null) { crashRecovery = null; recoveryBlocked = true; }
            if (session != null && session.state() != PartySession.State.ENDING) session.stop();
            else if (session != null) { recoveryBlocked = true; session = null; }
        }
    }

    public void close() {
        ticker.cancel();
        if (session != null) session.shutdown();
        if (crashRecovery != null) try { crashRecovery.restore(Integer.MAX_VALUE); crashRecovery.delete(); }
        catch (Exception e) { plugin.getLogger().warning("场地恢复记录已保留：" + e.getMessage()); }
        HandlerList.unregisterAll(this);
    }

    @Override public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try {
            if (!sender.hasPermission("mlcgames.party.play")) { message(sender, "no-permission"); return true; }
            String action = args.length == 0 ? "help" : args[0].toLowerCase(Locale.ROOT);
            switch (action) {
                case "list" -> {
                    for (String id : definitions.keySet()) message(sender, "game-row", "game", id, "name", configuration(id).text("name"));
                }
                case "join" -> {
                    if (!(sender instanceof Player player)) throw new IllegalArgumentException(text("players-only"));
                    if (args.length < 2 || args.length > 3) { message(sender, "help"); break; }
                    if (legacyBusy()) throw new IllegalStateException(text("legacy-busy"));
                    if (recoveryBlocked || crashRecovery != null) throw new IllegalStateException(text("recovering"));
                    if (recovery.pending(player)) throw new IllegalStateException(text("player-recovery"));
                    if (player.isDead() || player.isInsideVehicle() || !player.getItemOnCursor().getType().isAir()) throw new IllegalStateException(text("player-not-ready"));
                    org.bukkit.scoreboard.Team team = Bukkit.getScoreboardManager().getMainScoreboard().getEntryTeam(player.getName());
                    if (team != null && (team.getName().contains("bankgame") || team.getName().contains("sandgame") || team.getName().contains("zombieday") || team.getName().contains("dungeongame"))) throw new IllegalStateException(text("legacy-member"));
                    if (session != null && session.member(player)) throw new IllegalStateException(text("already-joined"));
                    if (session == null) {
                        PartyConfig config = configuration(args[1]);
                        session = new PartySession(this, config, config.arena(args.length == 3 ? args[2] : "arena_1"), definitions.get(args[1]).factory.get());
                    } else if (!session.config.id.equals(args[1]) || args.length == 3) throw new IllegalStateException(text("session-busy"));
                    session.join(player);
                }
                case "leave" -> {
                    if (!(sender instanceof Player player)) throw new IllegalArgumentException(text("players-only"));
                    if (session != null && session.member(player)) session.leave(player);
                    else if (recovery.pending(player)) recovery.restore(player);
                    else message(sender, "not-joined");
                }
                case "action" -> {
                    if (!(sender instanceof Player player)) throw new IllegalArgumentException(text("players-only"));
                    if (session == null || !session.member(player) || !session.active(player)) throw new IllegalStateException(text("not-joined"));
                    if (!session.game.action(session, player, Arrays.copyOfRange(args, 1, args.length))) message(sender, "help");
                }
                case "start", "stop", "check", "reload" -> {
                    if (!sender.hasPermission("mlcgames.party.admin")) { message(sender, "no-permission"); break; }
                    if (action.equals("reload")) {
                        if (session != null) throw new IllegalStateException(text("session-busy"));
                        language = PartyConfig.load(plugin, "party/language.yml");
                        for (String id : definitions.keySet()) configuration(id);
                        message(sender, "reloaded");
                    } else if (action.equals("check")) {
                        if (args.length < 2 || args.length > 3) { message(sender, "help"); break; }
                        PartyConfig config = configuration(args[1]);
                        PartyConfig.Arena arena = config.arena(args.length == 3 ? args[2] : "arena_1");
                        new PartySession(this, config, arena, definitions.get(args[1]).factory.get());
                        message(sender, "valid");
                    } else {
                        if (session == null) throw new IllegalStateException(text("no-session"));
                        if (action.equals("start")) session.start(); else session.stop();
                    }
                }
                default -> message(sender, "help");
            }
        } catch (Exception e) {
            if (session != null && session.state() == PartySession.State.WAITING && session.players().isEmpty()) session = null;
            message(sender, "error", "error", e.getMessage());
        }
        return true;
    }

    @Override public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> values = args.length == 1 ? new ArrayList<>(List.of("list", "join", "leave")) : new ArrayList<>();
        if (args.length == 1 && sender.hasPermission("mlcgames.party.admin")) values.addAll(List.of("start", "stop", "check", "reload"));
        if (args.length == 2 && List.of("join", "check").contains(args[0])) values.addAll(definitions.keySet());
        String prefix = args.length == 0 ? "" : args[args.length - 1].toLowerCase(Locale.ROOT);
        return values.stream().filter(v -> v.startsWith(prefix)).toList();
    }

    private boolean member(Player p) { return session != null && session.member(p) && (session.state() != PartySession.State.ENDING || recovery.pending(p)); }
    private boolean protectedBlock(Block b) { return session != null && session.arena.contains(b.getLocation()); }
    private static Player attacker(Entity entity) {
        if (entity instanceof Player player) return player;
        if (entity instanceof Projectile projectile && projectile.getShooter() instanceof Player player) return player;
        return null;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void damage(EntityDamageEvent event) {
        if (session != null && session.owns(event.getEntity()) && (session.state() == PartySession.State.RUNNING || session.state() == PartySession.State.READY)) session.game.entityDamage(session, event);
        if (!(event.getEntity() instanceof Player victim)) return;
        Player attacker = event instanceof EntityDamageByEntityEvent hit ? attacker(hit.getDamager()) : null;
        if (!member(victim)) { if (attacker != null && member(attacker)) event.setCancelled(true); return; }
        event.setCancelled(true);
        if (!session.active(victim)) return;
        if (session.active(victim) && attacker != null && session.active(attacker) && !victim.equals(attacker) && event instanceof EntityDamageByEntityEvent hit) {
            session.game.hit(session, attacker, victim, hit);
            event = hit;
        } else if (attacker == null) session.game.environmentalDamage(session, victim, event);
        if (!event.isCancelled() && event.getFinalDamage() >= victim.getHealth()) {
            event.setCancelled(true);
            if (!session.game.lethalDamage(session, victim, event)) session.eliminate(victim);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void entityDeath(EntityDeathEvent event) {
        if (session != null && session.owns(event.getEntity())
                && (session.state() == PartySession.State.RUNNING || session.state() == PartySession.State.READY)) session.game.entityDeath(session, event);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void breakBlock(BlockBreakEvent event) {
        if (!protectedBlock(event.getBlock()) && !member(event.getPlayer())) return;
        boolean allowed = member(event.getPlayer()) && session.active(event.getPlayer()) && session.writable(event.getBlock());
        if (allowed && session.game.nativeBreak(session, event.getPlayer(), event.getBlock())) return;
        event.setCancelled(true);
        if (allowed && session.game.breakBlock(session, event.getPlayer(), event.getBlock())) session.set(event.getBlock(), Material.AIR);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void placeBlock(BlockPlaceEvent event) {
        if (event instanceof BlockMultiPlaceEvent) return;
        if (!protectedBlock(event.getBlock()) && !member(event.getPlayer())) return;
        boolean allow = member(event.getPlayer()) && session.active(event.getPlayer()) && session.writable(event.getBlock()) && session.game.placeBlock(session, event.getPlayer(), event.getBlock());
        event.setCancelled(!allow);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void multiPlace(BlockMultiPlaceEvent event) {
        boolean relevant = member(event.getPlayer()) || protectedBlock(event.getBlock());
        if (!relevant) return;
        boolean allow = member(event.getPlayer()) && session.active(event.getPlayer())
                && event.getReplacedBlockStates().stream().allMatch(state -> session.writable(state.getBlock()))
                && session.game.placeBlock(session, event.getPlayer(), event.getBlock());
        event.setCancelled(!allow);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void blockDamage(BlockDamageEvent event) {
        if (member(event.getPlayer()) && session.active(event.getPlayer()) && session.game.instantBreak(session, event.getPlayer(), event.getBlock())) event.setInstaBreak(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void projectileLaunch(ProjectileLaunchEvent event) {
        if (event.getEntity().getShooter() instanceof Player player && member(player)) {
            if (!session.active(player) || !session.game.launch(session, player)) event.setCancelled(true);
            else { session.track(event.getEntity()); session.game.launched(session, player, event.getEntity()); }
        }
    }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void bowShoot(EntityShootBowEvent event) {
        if (event.getEntity() instanceof Player player && member(player)) {
            if (!session.active(player)) event.setCancelled(true); else session.game.shoot(session, event);
        }
    }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void elytraBoost(PlayerElytraBoostEvent event) {
        if (!member(event.getPlayer())) return;
        if (!session.active(event.getPlayer())) event.setCancelled(true);
        else session.track(event.getFirework());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void projectileHit(ProjectileHitEvent event) {
        if (event.getEntity().getShooter() instanceof Player player && member(player)) {
            if (event.getEntity() instanceof FishHook && session.game.retainFishingHook()) return;
            if (session.active(player)) session.game.projectile(session, event);
            Bukkit.getScheduler().runTask(plugin, () -> event.getEntity().remove());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void move(PlayerMoveEvent event) {
        if (session == null || event.getTo() == null) return;
        if (!member(event.getPlayer())) {
            if (session.arena.contains(event.getTo())) event.setCancelled(true);
        } else if (session.state() != PartySession.State.RUNNING && session.state() != PartySession.State.ENDING && event.hasChangedPosition()) event.setCancelled(true);
        else if (session.active(event.getPlayer()) && event.hasChangedPosition()) session.game.moved(session, event);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void teleport(PlayerTeleportEvent event) {
        if (session == null || event.getTo() == null) return;
        if (session.active(event.getPlayer()) && !session.arena.contains(event.getTo())) event.setCancelled(true);
        if (!session.member(event.getPlayer()) && session.arena.contains(event.getTo())) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void interact(PlayerInteractEvent event) {
        if (!member(event.getPlayer())) return;
        event.setUseInteractedBlock(Event.Result.DENY);
        if (!session.active(event.getPlayer())) event.setUseItemInHand(Event.Result.DENY);
        else {
            Block clicked = event.getClickedBlock();
            if (clicked != null && session.writable(clicked) && session.game.allowBlockInteraction(session, event.getPlayer(), clicked))
                event.setUseInteractedBlock(Event.Result.DEFAULT);
            session.game.interact(session, event);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void consume(PlayerItemConsumeEvent event) {
        if (!member(event.getPlayer())) return;
        if (!session.active(event.getPlayer())) event.setCancelled(true);
        else session.game.consume(session, event);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void fish(PlayerFishEvent event) {
        if (!member(event.getPlayer())) {
            if (session != null && event.getCaught() != null && (session.owns(event.getCaught())
                    || event.getCaught() instanceof Player player && member(player))) event.setCancelled(true);
            return;
        }
        if (!session.active(event.getPlayer())) { event.setCancelled(true); event.getHook().remove(); }
        else session.game.fish(session, event);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void interactEntity(PlayerInteractEntityEvent event) {
        if (session != null && session.owns(event.getRightClicked()) && !session.active(event.getPlayer())) { event.setCancelled(true); return; }
        if (member(event.getPlayer())) {
            event.setCancelled(true);
            if (session.active(event.getPlayer())) session.game.interactEntity(session, event.getPlayer(), event.getRightClicked());
        }
    }
    @EventHandler(priority = EventPriority.HIGHEST)
    public void attackEntity(PrePlayerAttackEntityEvent event) {
        if (session == null || !session.owns(event.getAttacked())) return;
        // Paper initially cancels targets vanilla cannot attack (e.g. invulnerable game entities).
        // Keep another listener's cancellation of a normal attack, while supporting our custom targets.
        if (event.isCancelled() && event.willAttack()) return;
        event.setCancelled(true);
        if (session.active(event.getPlayer())) session.game.attackEntity(session, event.getPlayer(), event.getAttacked());
    }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void vehicleEnter(VehicleEnterEvent event) {
        if (session != null && session.owns(event.getVehicle()) && (!(event.getEntered() instanceof Player player) || !session.active(player))) event.setCancelled(true);
    }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void inventory(InventoryClickEvent event) { if (event.getWhoClicked() instanceof Player p && member(p)) event.setCancelled(!session.active(p) || !session.game.allowInventory(session, event)); }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void drag(InventoryDragEvent event) { if (event.getWhoClicked() instanceof Player p && member(p)) event.setCancelled(!session.active(p) || !session.game.allowDrag(session, event)); }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void drop(PlayerDropItemEvent event) {
        if (!member(event.getPlayer())) return;
        boolean allow = session.active(event.getPlayer()) && session.game.allowDrop(session, event.getPlayer(), event.getItemDrop());
        event.setCancelled(!allow);
        if (allow) session.track(event.getItemDrop());
    }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void pickup(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player p && member(p)) event.setCancelled(!session.active(p) || !session.game.allowPickup(session, p, event.getItem()));
        else if (session != null && session.owns(event.getItem()) && (!(event.getEntity() instanceof Player p) || !member(p) || !session.active(p))) event.setCancelled(true);
    }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void swap(PlayerSwapHandItemsEvent event) {
        if (member(event.getPlayer())) {
            event.setCancelled(!session.active(event.getPlayer()) || !session.game.allowSwap(session, event.getPlayer()));
            if (session.active(event.getPlayer())) session.game.swap(session, event);
        }
    }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void hunger(FoodLevelChangeEvent event) { if (event.getEntity() instanceof Player p && member(p) && session.game.maintainFood(session)) event.setCancelled(true); }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void physics(BlockPhysicsEvent event) {
        if (protectedBlock(event.getBlock()) || session != null && session.writable(event.getSourceBlock())) {
            boolean natural = session.state() == PartySession.State.RUNNING && session.game.allowNaturalChanges(session);
            boolean allowed = session.writable(event.getBlock()) && session.writable(event.getSourceBlock())
                    && (natural || session.game.allowPhysics(session, event.getBlock()));
            event.setCancelled(!allowed);
        }
    }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void spread(BlockSpreadEvent event) {
        if (protectedBlock(event.getBlock()) || protectedBlock(event.getSource())) {
            boolean natural = session.state() == PartySession.State.RUNNING && session.game.allowNaturalChanges(session)
                    && session.writable(event.getSource()) && session.writable(event.getBlock());
            boolean allowed = session.writable(event.getBlock()) && (natural || session.game.allowPhysics(session, event.getBlock()));
            if (allowed) session.snapshot(event.getBlock());
            event.setCancelled(!allowed);
        }
    }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void itemSpawn(ItemSpawnEvent event) {
        if (session != null && session.arena.contains(event.getLocation())) {
            if (session.game.suppressArenaDrops()) event.setCancelled(true);
            else session.track(event.getEntity());
        }
    }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void spawnerSpawn(SpawnerSpawnEvent event) {
        if (session != null && session.writable(event.getSpawner().getBlock()) && session.game.suppressArenaDrops()) {
            if (session.game.allowPhysics(session, event.getSpawner().getBlock())) session.track(event.getEntity());
            else event.setCancelled(true);
        }
    }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void flow(BlockFromToEvent event) {
        if (!protectedBlock(event.getBlock()) && !protectedBlock(event.getToBlock())) return;
        boolean allow = session.state() == PartySession.State.RUNNING && session.game.allowNaturalChanges(session)
                && session.writable(event.getBlock()) && session.writable(event.getToBlock());
        if (!allow) allow = session.writable(event.getBlock()) && session.writable(event.getToBlock())
                && session.game.allowPhysics(session, event.getToBlock());
        if (allow) session.snapshot(event.getToBlock());
        event.setCancelled(!allow);
    }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void burn(BlockBurnEvent event) {
        if (!protectedBlock(event.getBlock())) return;
        boolean allow = session.state() == PartySession.State.RUNNING && session.writable(event.getBlock())
                && (session.game.allowNaturalChanges(session) || session.game.allowPhysics(session, event.getBlock()));
        if (allow) session.snapshot(event.getBlock());
        event.setCancelled(!allow);
    }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void ignite(BlockIgniteEvent event) {
        if (!protectedBlock(event.getBlock())) return;
        boolean allow = session.state() == PartySession.State.RUNNING && session.writable(event.getBlock())
                && (session.game.allowNaturalChanges(session) || session.game.allowPhysics(session, event.getBlock()));
        if (allow) session.snapshot(event.getBlock());
        event.setCancelled(!allow);
    }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void change(EntityChangeBlockEvent event) {
        if (session == null || !protectedBlock(event.getBlock()) && !session.owns(event.getEntity())) return;
        boolean allow = session.writable(event.getBlock()) && session.game.fallingBlockChange(session, event);
        if (!allow && event.getEntity() instanceof FallingBlock && session.owns(event.getEntity())
                && session.state() == PartySession.State.RUNNING && session.game.allowNaturalChanges(session)) allow = true;
        if (allow) session.snapshot(event.getBlock());
        event.setCancelled(!allow);
        if (!allow && event.getEntity() instanceof FallingBlock && session.owns(event.getEntity())) event.getEntity().remove();
    }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void form(BlockFormEvent event) {
        Player player = event instanceof EntityBlockFormEvent entityForm && entityForm.getEntity() instanceof Player p ? p : null;
        if (player == null && session != null && session.state() == PartySession.State.RUNNING && session.game.allowNaturalChanges(session)
                && session.writable(event.getBlock())) { session.snapshot(event.getBlock()); return; }
        if (!protectedBlock(event.getBlock()) && (player == null || !member(player))) return;
        boolean allow = player != null && member(player) && session.active(player) && session.writable(event.getBlock())
                && session.game.formBlock(session, player, event.getBlock(), event.getNewState().getType());
        event.setCancelled(!allow);
    }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void fade(BlockFadeEvent event) {
        if (protectedBlock(event.getBlock())) event.setCancelled((session.state() != PartySession.State.RUNNING && !session.game.allowPhysics(session, event.getBlock()))
                || !session.writable(event.getBlock()) || !session.game.fadeBlock(session, event.getBlock(), event.getNewState().getType()));
    }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void explode(EntityExplodeEvent event) {
        if (session != null && session.owns(event.getEntity()) && session.state() == PartySession.State.RUNNING)
            session.game.explode(session, event);
        Iterator<Block> blocks = event.blockList().iterator();
        while (blocks.hasNext()) {
            Block block = blocks.next();
            if (!protectedBlock(block)) continue;
            boolean allow = session.state() == PartySession.State.RUNNING && session.writable(block)
                    && session.game.allowNaturalChanges(session);
            if (allow) session.snapshot(block); else blocks.remove();
        }
    }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void blockExplode(BlockExplodeEvent event) {
        Iterator<Block> blocks = event.blockList().iterator();
        while (blocks.hasNext()) {
            Block block = blocks.next();
            if (!protectedBlock(block)) continue;
            boolean allow = session.state() == PartySession.State.RUNNING && session.writable(block)
                    && session.game.allowNaturalChanges(session);
            if (allow) session.snapshot(block); else blocks.remove();
        }
    }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void pistonExtend(BlockPistonExtendEvent event) {
        if (protectedBlock(event.getBlock()) || event.getBlocks().stream().anyMatch(b -> protectedBlock(b) || protectedBlock(b.getRelative(event.getDirection())))) event.setCancelled(true);
    }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void pistonRetract(BlockPistonRetractEvent event) {
        if (protectedBlock(event.getBlock()) || event.getBlocks().stream().anyMatch(b -> protectedBlock(b) || protectedBlock(b.getRelative(event.getDirection())))) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void death(PlayerDeathEvent event) {
        if (!member(event.getPlayer())) return;
        event.getDrops().clear();
        event.setDroppedExp(0);
        event.setKeepInventory(true);
        event.setKeepLevel(true);
        session.eliminate(event.getPlayer());
        Bukkit.getScheduler().runTask(plugin, () -> { if (event.getPlayer().isOnline() && event.getPlayer().isDead()) event.getPlayer().spigot().respawn(); });
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void bookEdit(PlayerEditBookEvent event) {
        if (member(event.getPlayer())) session.game.editBook(session, event);
    }

    @EventHandler public void respawn(PlayerRespawnEvent event) {
        if (member(event.getPlayer())) event.setRespawnLocation(session.arena.spectator());
        recoverLater(event.getPlayer());
    }
    @EventHandler public void join(PlayerJoinEvent event) { recoverLater(event.getPlayer()); }
    @EventHandler public void quit(PlayerQuitEvent event) { if (member(event.getPlayer())) session.leave(event.getPlayer()); }
    @EventHandler public void entityLoad(EntitiesLoadEvent event) {
        for (Entity entity : event.getEntities()) if (entity.getScoreboardTags().contains(ENTITY_TAG) && (session == null || !session.owns(entity))) entity.remove();
    }

    private void recoverLater(Player player) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (!player.isOnline()) return;
            if ((!member(player) || session.state() == PartySession.State.ENDING) && recovery.pending(player)) {
                try { recovery.restore(player); }
                catch (Exception e) { message(player, "error", "error", e.getMessage()); }
            } else if (member(player) && !session.active(player) && session.state() == PartySession.State.RUNNING) player.setGameMode(GameMode.SPECTATOR);
        });
    }
}
