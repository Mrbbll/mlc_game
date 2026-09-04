/**
 * 文件说明：相性结算后的伤害浮字服务。
 * 它在事件完全处理后的下一 tick 读取最终伤害；命中显示相性与数值，命中检定失败则显示 MISS。
 */
package com.mlc.mlcgames.combat.damageindicator;

import com.mlc.mlcgames.combat.DamageAffinity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

/** Displays one MLCGame-owned floating result after a resolved combat hit. */
public final class DamageIndicatorService {
    private static final DecimalFormat DAMAGE_FORMAT = new DecimalFormat("0.##", DecimalFormatSymbols.getInstance(Locale.ROOT));

    private final JavaPlugin plugin;
    private final DamageIndicatorConfig config;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public DamageIndicatorService(JavaPlugin plugin, DamageIndicatorConfig config) {
        this.plugin = plugin;
        this.config = config;
    }

    /** Schedules display after every listener has had a chance to adjust or cancel the damage event. */
    public void displayAfterDamage(EntityDamageByEntityEvent event, LivingEntity victim, DamageAffinity affinity) {
        if (!config.enabled()) return;
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            if (event.isCancelled() || !victim.isValid()) return;
            double finalDamage = event.getFinalDamage();
            if (finalDamage <= 0.0d) {
                showMiss(victim);
                return;
            }
            show(victim, affinity, finalDamage);
        });
    }

    private void show(LivingEntity victim, DamageAffinity affinity, double finalDamage) {
        Location location = createLocation(victim);
        List<Player> viewers = nearbyPlayers(location);
        if (viewers.isEmpty()) return;

        String plainFormat = config.format(affinity).replace("<damage>", DAMAGE_FORMAT.format(finalDamage));
        Component text = miniMessage.deserialize(plainFormat);
        new DamageHologram(plugin, config, location, text, viewers).start();
    }

    private void showMiss(LivingEntity victim) {
        Location location = createLocation(victim);
        List<Player> viewers = nearbyPlayers(location);
        if (viewers.isEmpty()) return;

        Component text = miniMessage.deserialize(config.missFormat());
        new DamageHologram(plugin, config, location, text, viewers).start();
    }

    private Location createLocation(LivingEntity victim) {
        double spread = config.horizontalSpread();
        double x = ThreadLocalRandom.current().nextDouble(-spread, spread + Double.MIN_VALUE);
        double z = ThreadLocalRandom.current().nextDouble(-spread, spread + Double.MIN_VALUE);
        return victim.getLocation().add(x, victim.getHeight() + config.heightOffset(), z);
    }

    private List<Player> nearbyPlayers(Location location) {
        double viewDistanceSquared = config.viewDistance() * config.viewDistance();
        List<Player> viewers = new ArrayList<>();
        for (Player player : location.getWorld().getPlayers()) {
            if (player.getLocation().distanceSquared(location) <= viewDistanceSquared) viewers.add(player);
        }
        return viewers;
    }
}
