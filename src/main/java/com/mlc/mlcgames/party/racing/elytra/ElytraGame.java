package com.mlc.mlcgames.party.racing.elytra;

import com.mlc.mlcgames.party.core.*;
import com.mlc.mlcgames.party.racing.common.RaceGame;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/** Source elytra: four rockets, lava retries, emerald finish, fourth-finisher shortening. */
public final class ElytraGame extends RaceGame {
    private Cuboid gate;
    private boolean prepared;
    @Override public void validate(PartyConfig config, PartyConfig.Arena arena) {
        super.validate(config, arena);
        gate = arena.region("gate");
        if (gate.volume() > config.integer("limits.blocks-per-tick", 1, 2000)) throw new IllegalArgumentException("飞行起跑门体积超过每 tick 修改上限");
        config.integer("rockets-per-attempt", 1, 64);
    }
    @Override public List<Cuboid> writableRegions(PartyConfig.Arena arena) { return List.of(arena.region("gate")); }
    @Override public boolean prepare(PartySession session, int budget) {
        if (!prepared) { fillGate(session, Material.BARRIER); prepared = true; }
        return true;
    }
    private void fillGate(PartySession session, Material material) {
        for (int x = gate.minX(); x <= gate.maxX(); x++) for (int y = gate.minY(); y <= gate.maxY(); y++) for (int z = gate.minZ(); z <= gate.maxZ(); z++) session.set(session.arena.world().getBlockAt(x, y, z), material);
    }
    @Override public void start(PartySession session) {
        super.start(session);
        fillGate(session, Material.AIR);
        for (Player player : session.survivors()) equip(session, player);
    }
    private void equip(PartySession session, Player player) {
        player.getInventory().clear();
        ItemStack elytra = session.item(Material.ELYTRA, "items.elytra");
        elytra.addUnsafeEnchantment(Enchantment.BINDING_CURSE, 1);
        player.getInventory().setChestplate(elytra);
        ItemStack rockets = session.item(Material.FIREWORK_ROCKET, "items.rocket");
        rockets.setAmount(session.config.integer("rockets-per-attempt", 1, 64));
        player.getInventory().setItem(0, rockets);
    }
    @Override public void hazard(PartySession session, Player player) {
        player.setGliding(false);
        super.hazard(session, player);
        if (session.active(player)) equip(session, player);
    }
}
