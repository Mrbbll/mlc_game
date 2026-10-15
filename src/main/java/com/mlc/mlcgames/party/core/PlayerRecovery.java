package com.mlc.mlcgames.party.core;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

/** Written before joining; offline players recover on their next login, including after restart. */
public final class PlayerRecovery {
    private final File file;
    private final YamlConfiguration data = new YamlConfiguration();
    private final Map<UUID, Scoreboard> scoreboards = new HashMap<>();

    public PlayerRecovery(JavaPlugin plugin) {
        file = new File(plugin.getDataFolder(), "party/recovery/players.yml");
        if (file.exists()) try { data.load(file); }
        catch (Exception e) { throw new IllegalStateException("玩家恢复文件不可读，派对模块拒绝启动", e); }
    }

    public boolean pending(Player player) { return data.isConfigurationSection(player.getUniqueId().toString()); }

    public void capture(Player player, Location exit) {
        if (pending(player)) throw new IllegalStateException("玩家仍有未恢复数据");
        String key = player.getUniqueId().toString();
        saveLocation(key + ".location", player.getLocation());
        saveLocation(key + ".exit", exit);
        data.set(key + ".mode", player.getGameMode().name());
        data.set(key + ".contents", Arrays.asList(player.getInventory().getContents()));
        data.set(key + ".health", player.getHealth());
        data.set(key + ".food", player.getFoodLevel());
        data.set(key + ".saturation", player.getSaturation());
        data.set(key + ".exhaustion", player.getExhaustion());
        data.set(key + ".level", player.getLevel());
        data.set(key + ".exp", player.getExp());
        data.set(key + ".total-exp", player.getTotalExperience());
        data.set(key + ".allow-flight", player.getAllowFlight());
        data.set(key + ".flying", player.isFlying());
        data.set(key + ".fire", player.getFireTicks());
        data.set(key + ".fall", player.getFallDistance());
        data.set(key + ".effects", new ArrayList<>(player.getActivePotionEffects()));
        data.set(key + ".glowing", player.isGlowing());
        save();
        scoreboards.put(player.getUniqueId(), player.getScoreboard());
    }

    public boolean restore(Player player) {
        String key = player.getUniqueId().toString();
        ConfigurationSection saved = data.getConfigurationSection(key);
        if (saved == null || player.isDead()) return false;
        Location location = readLocation(saved, saved.contains("exit") ? "exit" : "location");
        if (location == null || location.getWorld() == null) throw new IllegalStateException("玩家原位置世界尚未加载，保留恢复数据");
        player.closeInventory();
        player.getInventory().clear();
        List<?> contents = saved.getList("contents", List.of());
        player.getInventory().setContents(contents.toArray(new ItemStack[0]));
        player.setGameMode(GameMode.valueOf(saved.getString("mode", "ADVENTURE")));
        player.setAllowFlight(saved.getBoolean("allow-flight"));
        player.setFlying(saved.getBoolean("flying") && player.getAllowFlight());
        player.clearActivePotionEffects();
        for (Object effect : saved.getList("effects", List.of())) if (effect instanceof PotionEffect potion) player.addPotionEffect(potion);
        player.setGlowing(saved.getBoolean("glowing"));
        player.setHealth(Math.max(0.5, Math.min(saved.getDouble("health", 20), Objects.requireNonNull(player.getAttribute(Attribute.MAX_HEALTH)).getValue())));
        player.setFoodLevel(saved.getInt("food", 20));
        player.setSaturation((float) saved.getDouble("saturation"));
        player.setExhaustion((float) saved.getDouble("exhaustion"));
        player.setTotalExperience(saved.getInt("total-exp"));
        player.setLevel(saved.getInt("level"));
        player.setExp((float) saved.getDouble("exp"));
        player.setFireTicks(saved.getInt("fire"));
        player.setFallDistance((float) saved.getDouble("fall"));
        Scoreboard scoreboard = scoreboards.remove(player.getUniqueId());
        if (scoreboard != null) player.setScoreboard(scoreboard);
        if (!player.teleport(location)) throw new IllegalStateException("恢复位置传送被取消，保留恢复数据");
        Map<String, Object> retained = new LinkedHashMap<>(saved.getValues(false));
        data.set(key, null);
        try { save(); }
        catch (RuntimeException e) { data.createSection(key, retained); throw e; }
        return true;
    }

    private void save() {
        try {
            Files.createDirectories(file.getParentFile().toPath());
            File temporary = new File(file.getParentFile(), "players.yml.tmp");
            data.save(temporary);
            Files.move(temporary.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) { throw new IllegalStateException("无法保存玩家恢复数据", e); }
    }

    private void saveLocation(String key, Location location) {
        data.set(key + ".world", Objects.requireNonNull(location.getWorld()).getUID().toString());
        data.set(key + ".x", location.getX());
        data.set(key + ".y", location.getY());
        data.set(key + ".z", location.getZ());
        data.set(key + ".yaw", location.getYaw());
        data.set(key + ".pitch", location.getPitch());
    }

    private Location readLocation(ConfigurationSection section, String key) {
        if (section.get(key) instanceof Location old) return old;
        ConfigurationSection loc = section.getConfigurationSection(key);
        if (loc == null) throw new IllegalStateException("恢复位置记录缺失");
        World world = Bukkit.getWorld(UUID.fromString(Objects.requireNonNull(loc.getString("world"))));
        if (world == null) throw new IllegalStateException("玩家返回世界尚未加载，保留恢复记录");
        return new Location(world, loc.getDouble("x"), loc.getDouble("y"), loc.getDouble("z"), (float) loc.getDouble("yaw"), (float) loc.getDouble("pitch"));
    }
}
