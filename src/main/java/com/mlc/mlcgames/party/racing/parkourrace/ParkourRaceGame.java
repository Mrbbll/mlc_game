package com.mlc.mlcgames.party.racing.parkourrace;

import com.mlc.mlcgames.party.core.*;
import com.mlc.mlcgames.party.racing.common.RaceGame;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import java.util.*;

/** Source parkourrace: gold checkpoints and six material-triggered effects. */
public final class ParkourRaceGame extends RaceGame {
    private record Pad(String prefix, Material material, PotionEffectType effect, int duration, int amplifier) {}
    private final List<Pad> pads = new ArrayList<>();
    private Set<Material> checkpointMaterials;
    @Override public void validate(PartyConfig config, PartyConfig.Arena arena) {
        super.validate(config, arena);
        checkpointMaterials = config.materials("checkpoint-materials");
        arena.group("checkpoints");
        for (Map<?, ?> row : config.rules.getMapList("pad-effects")) {
            String prefix = Objects.toString(row.get("region-prefix"), "");
            Material material = Material.matchMaterial(Objects.toString(row.get("material"), ""));
            String name = Objects.toString(row.get("effect"), "");
            PotionEffectType effect = name.equals("CLEAR") ? null : Registry.EFFECT.get(NamespacedKey.minecraft(name.toLowerCase(Locale.ROOT)));
            if (prefix.isBlank() || material == null || (!name.equals("CLEAR") && effect == null) || !(row.get("duration-ticks") instanceof Number duration) || !(row.get("amplifier") instanceof Number amplifier)) throw new IllegalArgumentException("pad-effects 必须填写合法的区域、材料、效果和整数时长/等级");
            if (duration.doubleValue() != duration.intValue() || duration.intValue() < 1 || duration.intValue() > 200 || amplifier.doubleValue() != amplifier.intValue() || amplifier.intValue() < 0 || amplifier.intValue() > 30) throw new IllegalArgumentException("pad-effects 时长应为 1..200 tick，等级为 0..30");
            if (arena.regions().keySet().stream().noneMatch(key -> key.equals(prefix) || key.startsWith(prefix + "-"))) throw new IllegalArgumentException("缺少效果垫区域：" + prefix);
            pads.add(new Pad(prefix, material, effect, duration.intValue(), amplifier.intValue()));
        }
        if (pads.isEmpty()) throw new IllegalArgumentException("pad-effects 不能为空");
    }
    @Override public void tick(PartySession session, int elapsed) {
        for (Player player : session.survivors()) {
            Location feet = player.getLocation().subtract(0, 1, 0);
            Material block = feet.getBlock().getType();
            Optional<Location> declared = session.arena.group("checkpoints").stream().filter(p -> p.getBlockX() == feet.getBlockX() && p.getBlockZ() == feet.getBlockZ() && p.getBlockY() == feet.getBlockY() + 1).findFirst();
            if (checkpointMaterials.contains(block) && declared.isPresent()) {
                Location saved = checkpoints.get(player.getUniqueId());
                if (saved == null || saved.getBlockX() != player.getLocation().getBlockX() || saved.getBlockY() != player.getLocation().getBlockY() || saved.getBlockZ() != player.getLocation().getBlockZ()) {
                    checkpoints.put(player.getUniqueId(), declared.get().clone());
                    player.sendMessage(session.config.message("checkpoint-saved"));
                }
            }
            for (Pad pad : pads) {
                if (block != pad.material) continue;
                boolean inside = session.arena.regions().entrySet().stream().anyMatch(e -> (e.getKey().equals(pad.prefix) || e.getKey().startsWith(pad.prefix + "-")) && e.getValue().contains(feet.getBlockX(), feet.getBlockY(), feet.getBlockZ()));
                if (!inside) continue;
                if (pad.effect == null) player.clearActivePotionEffects();
                else player.addPotionEffect(new PotionEffect(pad.effect, pad.duration, pad.amplifier, true, false));
            }
        }
        super.tick(session, elapsed);
    }
    @Override public void hazard(PartySession session, Player player) {
        player.clearActivePotionEffects();
        super.hazard(session, player);
    }
}
