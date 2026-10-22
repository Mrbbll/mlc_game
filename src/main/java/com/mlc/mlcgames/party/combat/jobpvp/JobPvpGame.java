package com.mlc.mlcgames.party.combat.jobpvp;

import com.mlc.mlcgames.party.combat.common.NativeEquipment;
import com.mlc.mlcgames.party.core.PartyConfig;
import com.mlc.mlcgames.party.core.PartyGame;
import com.mlc.mlcgames.party.core.PartySession;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Source-backed job_pvp kits and passives; active skill functions have no source dispatcher. */
public final class JobPvpGame implements PartyGame {
    private static final int INFINITE_EFFECT_TICKS = Integer.MAX_VALUE;
    private final Map<UUID, Integer> kills = new HashMap<>();
    private Set<String> jobs = Set.of();

    @Override public boolean lastPlayerWins() { return false; }

    @Override public void validate(PartyConfig config, PartyConfig.Arena arena) {
        var section = config.rules.getConfigurationSection("kits");
        if (section == null || section.getKeys(false).isEmpty()) throw new IllegalArgumentException("kits 至少需要配置一个 source job");
        jobs = Set.copyOf(section.getKeys(false));
        for (String job : jobs) {
            if (config.rules.getMapList("kits." + job).isEmpty()) throw new IllegalArgumentException("kits." + job + " 不能为空");
        }
    }

    @Override public void start(PartySession session) {
        kills.clear();
        for (Player player : session.survivors()) {
            player.setGameMode(GameMode.SURVIVAL);
            player.setLevel(0);
            player.setExp(0);
            player.setTotalExperience(0);
            nightVision(player);
        }
    }

    @Override public void tick(PartySession session, int elapsed) {
        if (elapsed % 20 != 0) return;
        for (Player player : session.survivors()) {
            nightVision(player);
            if (player.getLevel() > 0) player.setLevel(player.getLevel() - 1);
        }
    }

    @Override public void hit(PartySession session, Player attacker, Player victim, EntityDamageByEntityEvent event) {
        event.setCancelled(false);
        if (event.getFinalDamage() >= victim.getHealth()) {
            kills.merge(attacker.getUniqueId(), 1, Integer::sum);
        }
    }

    @Override public boolean allowInventory(PartySession session, Player player) { return true; }

    @Override public boolean allowDrop(PartySession session, Player player, ItemStack item) {
        // The source tick retains only job_pvp:1 drops and removes job_pvp:2 drops.
        return NativeEquipment.flag(item, "job_pvp").equals("1");
    }

    @Override public boolean allowPickup(PartySession session, Player player, org.bukkit.entity.Item item) {
        return NativeEquipment.flag(item.getItemStack(), "job_pvp").equals("1");
    }

    @Override public boolean action(PartySession session, Player player, String[] args) {
        if (args.length != 2 || !args[0].equalsIgnoreCase("job")) return false;
        String job = args[1].toLowerCase(java.util.Locale.ROOT);
        if (!jobs.contains(job)) {
            player.sendMessage(session.config.message("unknown-job", "job", job));
            return true;
        }
        Object configured = session.config.rules.get("job-requirements." + job);
        if (!(configured instanceof Number requirement) || requirement.doubleValue() != requirement.intValue()
                || requirement.intValue() < 0 || requirement.intValue() > 10000) {
            player.sendMessage(session.config.message("threshold-unconfigured", "job", job));
            return true;
        }
        int count = kills.getOrDefault(player.getUniqueId(), 0);
        if (count < requirement.intValue()) {
            player.sendMessage(session.config.message("threshold-not-met", "job", job,
                    "required", requirement.intValue(), "kills", count));
            return true;
        }

        player.getInventory().clear();
        player.clearActivePotionEffects();
        NativeEquipment.give(session, player, "kits." + job);
        applyPassive(player, job);
        player.sendMessage(session.config.message("job-selected", "job", job));
        return true;
    }

    private static void nightVision(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 400, 25, true, false));
    }

    private static void applyPassive(Player player, String job) {
        switch (job) {
            case "fire" -> effect(player, PotionEffectType.FIRE_RESISTANCE, 25);
            case "old" -> effect(player, PotionEffectType.SLOWNESS, 0);
            case "uma" -> {
                effect(player, PotionEffectType.SLOW_FALLING, 0);
                effect(player, PotionEffectType.JUMP_BOOST, 1);
            }
        }
    }

    private static void effect(Player player, PotionEffectType type, int amplifier) {
        player.addPotionEffect(new PotionEffect(type, INFINITE_EFFECT_TICKS, amplifier, true, false));
    }
}
