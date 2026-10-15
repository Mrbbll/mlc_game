package com.mlc.mlcgames.party.core;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import java.util.List;

/** All callbacks run on the server thread. No handler may schedule unowned tasks. */
public interface PartyGame {
    default void validate(PartyConfig config, PartyConfig.Arena arena) {}
    default List<Cuboid> writableRegions(PartyConfig.Arena arena) { return List.of(); }
    default boolean prepare(PartySession session, int blockBudget) { return true; }
    void start(PartySession session);
    void tick(PartySession session, int elapsed);
    default boolean breakBlock(PartySession session, Player player, Block block) { return false; }
    default boolean placeBlock(PartySession session, Player player, Block block) { return false; }
    default boolean instantBreak(PartySession session, Player player, Block block) { return false; }
    default boolean launch(PartySession session, Player player) { return true; }
    default boolean formBlock(PartySession session, Player player, Block block, org.bukkit.Material material) { return false; }
    default boolean fadeBlock(PartySession session, Block block, org.bukkit.Material material) { return false; }
    default void hit(PartySession session, Player attacker, Player victim, EntityDamageByEntityEvent event) {}
    default void environmentalDamage(PartySession session, Player player, org.bukkit.event.entity.EntityDamageEvent event) {}
    default void projectile(PartySession session, ProjectileHitEvent event) {}
    default void eliminated(PartySession session, Player player) {}
    default void hazard(PartySession session, Player player) { session.eliminate(player); }
    default void timeout(PartySession session) { session.finish(List.of()); }
    default void moved(PartySession session, PlayerMoveEvent event) {}
    default void interact(PartySession session, PlayerInteractEvent event) {}
    default void consume(PartySession session, org.bukkit.event.player.PlayerItemConsumeEvent event) {}
    default void fish(PartySession session, org.bukkit.event.player.PlayerFishEvent event) { event.setCancelled(true); }
    default boolean retainFishingHook() { return false; }
    default void interactEntity(PartySession session, Player player, Entity target) {}
    default void attackEntity(PartySession session, Player player, Entity target) { interactEntity(session, player, target); }
    default boolean lastPlayerWins() { return true; }
    default int maximumDurationSeconds() { return 7200; }
    default boolean physicsOnWrite(PartySession session) { return false; }
    default boolean allowPhysics(PartySession session, Block block) { return false; }
    default boolean fallingBlockChange(PartySession session, org.bukkit.event.entity.EntityChangeBlockEvent event) { return false; }
    default boolean suppressArenaDrops() { return false; }
}
