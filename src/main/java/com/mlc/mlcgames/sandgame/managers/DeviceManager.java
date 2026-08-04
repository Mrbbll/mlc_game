package com.mlc.mlcgames.sandgame.managers;

import com.mlc.mlcgames.Teammanager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

import static com.mlc.mlcgames.Mlcgames.miniMessage;

public class DeviceManager {

    public enum DeviceType {
        COIN_GENERATOR,
        SAND_GENERATOR,
        TOWER
    }

    public enum UpgradeType {
        SPEED,
        ATTACK,
        ARMOR
    }

    public static class Device {
        public DeviceType type;
        public Team team;
        public BlockDisplay blockDisplay;
        public TextDisplay textDisplay;
        public Location location;
        public boolean fast = false;
        public boolean attackUpgraded = false;
        public boolean armored = false;
        public int spawnTimer;
        public int attackCooldown = 0;
        public float rotation = 0;
        public List<BlockDisplay> upgrades = new ArrayList<>();

        public Device(DeviceType type, Team team, BlockDisplay blockDisplay, TextDisplay textDisplay, Location location) {
            this.type = type;
            this.team = team;
            this.blockDisplay = blockDisplay;
            this.textDisplay = textDisplay;
            this.location = location;
            this.spawnTimer = (type == DeviceType.SAND_GENERATOR) ? 30 : 3;
        }

        public void despawn() {
            blockDisplay.remove();
            textDisplay.remove();
            for (BlockDisplay u : upgrades) {
                u.remove();
            }
        }
    }

    public static final List<Device> devices = new ArrayList<>();

    // 放置设备；空间不足返回 null
    public static Device placeDevice(DeviceType type, Player owner, Location loc) {
        if (findDeviceNear(loc, 1.2) != null || !loc.getBlock().isPassable()) {
            return null;
        }
        World world = loc.getWorld();
        // 主展示方块：离地面 2 格高处，绕 Y 轴水平旋转（插值动画）
        BlockDisplay blockDisplay = world.spawn(loc.clone().add(0, 2, 0), BlockDisplay.class, e -> {
            e.setBlock(blockDataOf(type));
            applyRotation(e, 0, 0.9f);
            e.setInterpolationDelay(0);
            e.setInterpolationDuration(19);
        });
        TextDisplay textDisplay = world.spawn(loc.clone().add(0, 3.2, 0), TextDisplay.class, e -> {
            e.text(nameOf(type, Teammanager.getPlayerTeam(owner)));
            e.setBillboard(Display.Billboard.CENTER);
            e.setAlignment(TextDisplay.TextAlignment.CENTER);
            e.setBackgroundColor(org.bukkit.Color.fromARGB(0, 0, 0, 0));
        });
        Device device = new Device(type, Teammanager.getPlayerTeam(owner), blockDisplay, textDisplay, loc);
        devices.add(device);
        return device;
    }

    // 每秒调用一次（游戏 Timer 内）
    public static void tick() {
        for (Device d : new ArrayList<>(devices)) {
            if (!d.blockDisplay.isValid() || !d.textDisplay.isValid()) {
                d.despawn();
                devices.remove(d);
                continue;
            }
            // 主展示方块 + 升级标记：绕 Y 轴水平旋转（插值动画）
            d.rotation += 30;
            applyRotation(d.blockDisplay, d.rotation, 0.9f);
            d.upgrades.removeIf(u -> !u.isValid());
            for (BlockDisplay u : d.upgrades) {
                applyRotation(u, d.rotation, 0.4f);
            }
            switch (d.type) {
                case COIN_GENERATOR -> {
                    d.spawnTimer--;
                    if (d.spawnTimer <= 0) {
                        d.location.getWorld().dropItem(d.location, new ItemStack(Material.EMERALD));
                        d.spawnTimer = d.fast ? 1 : 3;
                    }
                }
                case SAND_GENERATOR -> {
                    d.spawnTimer--;
                    if (d.spawnTimer <= 0) {
                        d.location.getWorld().dropItem(d.location, new ItemStack(Material.SAND));
                        d.spawnTimer = 30;
                    }
                }
                case TOWER -> {
                    // 每 2 秒攻击一次
                    d.attackCooldown--;
                    if (d.attackCooldown <= 0) {
                        Player target = nearestEnemy(d);
                        if (target != null) {
                            target.damage(d.attackUpgraded ? 14 : 7);
                            drawLine(d.location, target.getLocation());
                        }
                        d.attackCooldown = 2;
                    }
                }
            }
        }
    }

    private static Player nearestEnemy(Device d) {
        Player nearest = null;
        double best = 5.0;
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getGameMode() == GameMode.SPECTATOR) {
                continue;
            }
            if (p.getLocation().getWorld() != d.location.getWorld()) {
                continue;
            }
            if (!isEnemy(d, p)) {
                continue;
            }
            double dist = p.getLocation().distance(d.location);
            if (dist <= 5 && dist < best) {
                best = dist;
                nearest = p;
            }
        }
        return nearest;
    }

    private static boolean isEnemy(Device d, Player p) {
        if (d.team == null) {
            return false;
        }
        if (d.team.equals(Teammanager.sandgame_team_1)) {
            return Teammanager.isPlayerInTeam(p, Teammanager.sandgame_team_2);
        }
        return Teammanager.isPlayerInTeam(p, Teammanager.sandgame_team_1);
    }

    // 在塔与目标之间画一条粒子线
    private static void drawLine(Location from, Location to) {
        World world = from.getWorld();
        if (world == null) {
            return;
        }
        Location a = from.clone().add(0, 0.5, 0);
        Location b = to.clone().add(0, 1, 0);
        Vector diff = b.clone().subtract(a).toVector();
        double distance = diff.length();
        if (distance <= 0) {
            return;
        }
        double step = 0.5;
        for (double t = 0; t <= distance; t += step) {
            Location point = a.clone().add(diff.clone().normalize().multiply(t));
            world.spawnParticle(Particle.FLAME, point, 1, 0, 0, 0, 0);
        }
    }

    // 设置展示方块绕 Y 轴水平旋转（配合插值动画平滑过渡）
    private static void applyRotation(BlockDisplay display, float yaw, float scale) {
        display.setTransformation(new Transformation(
                new Vector3f(0, 0, 0),
                new Quaternionf().rotationY((float) Math.toRadians(yaw)),
                new Vector3f(scale, scale, scale),
                new Quaternionf()));
    }

    // 升级特效：在主展示方块旁生成一个小的升级标记方块
    private static BlockDisplay spawnUpgradeMarker(Device d, Material material) {
        int idx = d.upgrades.size();
        double offsetX = (idx % 2 == 0) ? 0.6 : -0.6;
        double offsetZ = (idx / 2) * 0.6;
        Location markerLoc = d.location.clone().add(offsetX, 2, offsetZ);
        BlockDisplay marker = d.location.getWorld().spawn(markerLoc, BlockDisplay.class, e -> {
            e.setBlock(material.createBlockData());
            applyRotation(e, d.rotation, 0.4f);
            e.setInterpolationDelay(0);
            e.setInterpolationDuration(19);
        });
        return marker;
    }

    public static Device findDeviceNear(Location loc, double radius) {
        Device nearest = null;
        double best = radius;
        for (Device d : devices) {
            if (d.location.getWorld() != loc.getWorld()) {
                continue;
            }
            double dist = d.location.distance(loc);
            if (dist < best) {
                best = dist;
                nearest = d;
            }
        }
        return nearest;
    }

    // 升级已放置的设备；成功返回 true（消息已发送给玩家）
    public static boolean upgradeDevice(Player player, UpgradeType upgrade) {
        Block targetBlock = player.getTargetBlockExact(6);
        Location target = (targetBlock != null) ? targetBlock.getLocation().add(0.5, 0.5, 0.5) : player.getLocation();
        Device d = findDeviceNear(target, 2.5);
        if (d == null) {
            player.sendMessage(miniMessage.deserialize("<red>附近没有可升级的设备"));
            return false;
        }
        switch (upgrade) {
            case SPEED -> {
                if (d.type != DeviceType.COIN_GENERATOR) {
                    player.sendMessage(miniMessage.deserialize("<red>只能升级金币生成器"));
                    return false;
                }
                if (d.fast) {
                    player.sendMessage(miniMessage.deserialize("<red>该金币生成器已加速"));
                    return false;
                }
                d.fast = true;
                d.upgrades.add(spawnUpgradeMarker(d, Material.GOLD_BLOCK));
                player.sendMessage(miniMessage.deserialize("<green>金币生成器已加速（每1秒生成）"));
                return true;
            }
            case ATTACK -> {
                if (d.type != DeviceType.TOWER) {
                    player.sendMessage(miniMessage.deserialize("<red>只能升级防御塔"));
                    return false;
                }
                if (d.attackUpgraded) {
                    player.sendMessage(miniMessage.deserialize("<red>该防御塔已加强攻击"));
                    return false;
                }
                d.attackUpgraded = true;
                d.upgrades.add(spawnUpgradeMarker(d, Material.MAGMA_BLOCK));
                player.sendMessage(miniMessage.deserialize("<green>防御塔攻击加强（伤害14）"));
                return true;
            }
            case ARMOR -> {
                if (d.armored) {
                    player.sendMessage(miniMessage.deserialize("<red>该设备已有护甲"));
                    return false;
                }
                d.armored = true;
                d.upgrades.add(spawnUpgradeMarker(d, Material.IRON_BLOCK));
                player.sendMessage(miniMessage.deserialize("<green>设备获得护甲（抵挡一次炸弹）"));
                return true;
            }
        }
        return false;
    }

    // 炸弹：以玩家右键点击的方块为中心，拆除 3 格内设备；护甲设备抵挡一次
    public static boolean detonateBomb(Player player, Block clickedBlock) {
        Location center;
        if (clickedBlock != null) {
            center = clickedBlock.getLocation().add(0.5, 0.5, 0.5);
        } else {
            Block target = player.getTargetBlockExact(6);
            if (target == null) {
                return false; // 没有可引爆的方块目标，不消耗炸弹
            }
            center = target.getLocation().add(0.5, 0.5, 0.5);
        }
        center.getWorld().playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 1f, 1f);
        center.getWorld().spawnParticle(Particle.EXPLOSION, center, 1);
        for (Device d : new ArrayList<>(devices)) {
            if (d.location.getWorld() != center.getWorld()) {
                continue;
            }
            if (d.location.distance(center) <= 3) {
                if (d.armored) {
                    d.armored = false;
                    d.location.getWorld().playSound(d.location, Sound.BLOCK_ANVIL_BREAK, 1f, 1f);
                } else {
                    d.despawn();
                    devices.remove(d);
                }
            }
        }
        return true;
    }

    // 游戏结束清理所有设备
    public static void cleanup() {
        for (Device d : devices) {
            d.despawn();
        }
        devices.clear();
    }

    private static BlockData blockDataOf(DeviceType type) {
        return switch (type) {
            case COIN_GENERATOR -> Material.EMERALD_BLOCK.createBlockData();
            case SAND_GENERATOR -> Material.SANDSTONE.createBlockData();
            case TOWER -> Material.OBSIDIAN.createBlockData();
        };
    }

    private static Component nameOf(DeviceType type, Team team) {
        boolean redTeam = (team != null && team.equals(Teammanager.sandgame_team_1));
        String teamTag = redTeam ? "<red><b>红队·" : "<blue><b>蓝队·";
        String name = switch (type) {
            case COIN_GENERATOR -> "金币生成器";
            case SAND_GENERATOR -> "沙子生成器";
            case TOWER -> "防御塔";
        };
        return miniMessage.deserialize(teamTag + name);
    }
}
