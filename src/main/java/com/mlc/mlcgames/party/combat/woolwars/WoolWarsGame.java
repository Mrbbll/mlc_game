package com.mlc.mlcgames.party.combat.woolwars;

import com.mlc.mlcgames.party.core.*;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.*;

/** Reconstructed rules approved by user; source Wool Wars has no playable start/tick. */
public final class WoolWarsGame implements PartyGame {
    private final Map<UUID, Integer> teams = new HashMap<>();
    private final Set<Block> placed = new HashSet<>();
    private final int[] wins = new int[2];
    private Cuboid target;
    private Cuboid build;
    private int roundAt;
    private Scoreboard scoreboard;

    @Override public boolean lastPlayerWins() { return false; }
    @Override public void validate(PartyConfig config, PartyConfig.Arena arena) {
        target = arena.region("target");
        build = arena.region("build");
        if (target.minY() != target.maxY() || !build.contains(target)) throw new IllegalArgumentException("目标区必须为建造区内的单层区域");
        if (target.volume() > 256) throw new IllegalArgumentException("目标区最多 256 个方块");
        int required = (config.integer("players.max", 2, 64) + 1) / 2;
        for (String team : List.of("red", "blue")) if (arena.teamSpawns().getOrDefault(team, List.of()).size() < required) throw new IllegalArgumentException("points.team-spawns." + team + " 至少需要 " + required + " 个点位");
        config.integer("round-seconds", 10, 600);
        config.integer("wins-required", 1, 10);
        config.itemMaterial("weapons.sword");
        config.itemMaterial("weapons.shears");
        if (target.volume() > config.integer("limits.blocks-per-tick", 1, 2000)) throw new IllegalArgumentException("目标区方块数必须不超过 limits.blocks-per-tick");
    }
    @Override public List<Cuboid> writableRegions(PartyConfig.Arena arena) { return List.of(arena.region("build")); }
    @Override public void start(PartySession session) {
        List<Player> players = new ArrayList<>(session.players());
        Collections.shuffle(players);
        scoreboard = Objects.requireNonNull(Bukkit.getScoreboardManager()).getNewScoreboard();
        Team red = scoreboard.registerNewTeam("party_red");
        red.color(NamedTextColor.RED);
        red.setAllowFriendlyFire(false);
        Team blue = scoreboard.registerNewTeam("party_blue");
        blue.color(NamedTextColor.BLUE);
        blue.setAllowFriendlyFire(false);
        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            teams.put(player.getUniqueId(), i % 2);
            (i % 2 == 0 ? red : blue).addEntry(player.getName());
            player.setScoreboard(scoreboard);
        }
        startRound(session);
    }
    private void startRound(PartySession session) {
        placed.clear();
        session.resetPlayers();
        int[] count = new int[2];
        for (Player player : session.survivors()) {
            int team = teams.get(player.getUniqueId());
            player.setGameMode(GameMode.SURVIVAL);
            if (!player.teleport(session.arena.teamSpawns().get(team == 0 ? "red" : "blue").get(count[team]++))) throw new IllegalStateException("队伍出生点传送失败");
            player.getInventory().setItem(0, session.item(session.config.itemMaterial("weapons.sword"), "items.sword"));
            player.getInventory().setItem(1, session.item(session.config.itemMaterial("weapons.shears"), "items.shears"));
            ItemStack wool = session.item(team == 0 ? Material.RED_WOOL : Material.BLUE_WOOL, "items.wool");
            wool.setAmount(64);
            player.getInventory().setItem(2, wool);
        }
        for (int x = target.minX(); x <= target.maxX(); x++) for (int z = target.minZ(); z <= target.maxZ(); z++) session.set(session.arena.world().getBlockAt(x, target.minY(), z), Material.WHITE_WOOL);
        roundAt = session.elapsed();
        session.broadcast("round-start", "red", wins[0], "blue", wins[1]);
    }
    @Override public void tick(PartySession session, int elapsed) {
        List<Player> red = session.survivors().stream().filter(p -> teams.get(p.getUniqueId()) == 0).toList();
        List<Player> blue = session.survivors().stream().filter(p -> teams.get(p.getUniqueId()) == 1).toList();
        // A team leaving the whole game is a forfeit, not an endless series of rounds.
        if (session.players().stream().noneMatch(p -> teams.get(p.getUniqueId()) == 0)) { session.finish(session.players().stream().filter(p -> teams.get(p.getUniqueId()) == 1).toList()); return; }
        if (session.players().stream().noneMatch(p -> teams.get(p.getUniqueId()) == 1)) { session.finish(session.players().stream().filter(p -> teams.get(p.getUniqueId()) == 0).toList()); return; }
        int[] control = new int[2];
        for (int x = target.minX(); x <= target.maxX(); x++) for (int z = target.minZ(); z <= target.maxZ(); z++) {
            Material material = session.arena.world().getBlockAt(x, target.minY(), z).getType();
            if (material == Material.RED_WOOL) control[0]++;
            if (material == Material.BLUE_WOOL) control[1]++;
        }
        int winner = -2;
        if (red.isEmpty() && blue.isEmpty()) winner = -1;
        else if (red.isEmpty() || control[1] == target.volume()) winner = 1;
        else if (blue.isEmpty() || control[0] == target.volume()) winner = 0;
        else if (elapsed - roundAt >= session.config.integer("round-seconds", 10, 600) * 20) winner = control[0] == control[1] ? -1 : control[0] > control[1] ? 0 : 1;
        if (winner != -2) {
            if (winner >= 0) wins[winner]++;
            session.broadcast("round-end", "team", winner < 0 ? session.config.text("draw") : session.config.text(winner == 0 ? "teams.red" : "teams.blue"));
            if (winner >= 0 && wins[winner] >= session.config.integer("wins-required", 1, 10)) {
                final int winningTeam = winner;
                session.finish(session.players().stream().filter(p -> teams.get(p.getUniqueId()) == winningTeam).toList());
            } else session.resetRound(() -> startRound(session));
            return;
        }
        if (elapsed % 20 == 0) for (Player player : session.survivors()) {
            ItemStack wool = session.item(teams.get(player.getUniqueId()) == 0 ? Material.RED_WOOL : Material.BLUE_WOOL, "items.wool");
            wool.setAmount(64);
            player.getInventory().setItem(2, wool);
        }
    }
    @Override public boolean breakBlock(PartySession session, Player player, Block block) {
        if (target.contains(block.getX(), block.getY(), block.getZ())) return block.getType().name().endsWith("_WOOL");
        return placed.remove(block);
    }
    @Override public boolean placeBlock(PartySession session, Player player, Block block) {
        Material correct = teams.get(player.getUniqueId()) == 0 ? Material.RED_WOOL : Material.BLUE_WOOL;
        if (block.getType() != correct) return false;
        placed.add(block);
        return true;
    }
    @Override public void hit(PartySession session, Player attacker, Player victim, EntityDamageByEntityEvent event) {
        if (!teams.get(attacker.getUniqueId()).equals(teams.get(victim.getUniqueId()))) event.setCancelled(false);
    }
}
