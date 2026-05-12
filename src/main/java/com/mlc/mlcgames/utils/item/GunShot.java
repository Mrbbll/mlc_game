package com.mlc.mlcgames.utils.item;

import com.mlc.mlcgames.utils.particle.Gunparticle;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static com.mlc.mlcgames.Mlcgames.instance;
import static com.mlc.mlcgames.Mlcgames.server;

public class GunShot {
    static NamespacedKey cooldownkey = new NamespacedKey(instance,"cooldown");

    //获取是否在冷却
    public static boolean isincooldown(ItemStack item){
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        return pdc.getOrDefault(cooldownkey,PersistentDataType.BOOLEAN,false);
    }


    //冷却
    public static void setcooldown(ItemStack item, int cooldown){
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        pdc.set(cooldownkey, PersistentDataType.BOOLEAN,true);
        BukkitTask task = new BukkitRunnable(){
            @Override
            public void run() {
                pdc.set(cooldownkey, PersistentDataType.BOOLEAN,false);
            }
        }.runTaskLater(instance,cooldown);
    }

    //枪射击事件单发直线型
    public static void lineGunshot(Player player, int damage) {
        Location eye  = player.getEyeLocation();
        Vector direction = eye.getDirection();
        var result = player.getWorld().rayTraceEntities(
                eye,
                direction,
                30,
                0.0, // 实体边界扩展，0 表示精确碰撞箱
                entity -> !entity.equals(player) && entity instanceof LivingEntity // 只关心活体
        );
        Gunparticle.lineGunshotparticle(eye,result);
        player.playSound(player, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 3,0.5f);
        if (result != null && result.getHitEntity() != null) {
            server.broadcast(Component.text("hit"));
            hurtevet(result.getHitEntity(),damage);

        }
    }


    // 霰弹枪射击（散射式）
    public static  void areaGunshot(Player player, int damagePerPellet, int pelletCount, double spreadAngleDegrees) {
        Location eye = player.getEyeLocation();
        Vector baseDirection = eye.getDirection().normalize();
        Random random = new Random();

        // 用 Map 统计每个实体被命中的次数
        Map<LivingEntity, Integer> hitCounts = new HashMap<>();
        double maxRange = 20; // 霰弹枪射程

        for (int i = 0; i < pelletCount; i++) {
            // 生成随机散射方向
            Vector pelletDir = getScatteredDirectionHorizontal(baseDirection, spreadAngleDegrees, random);

            var result = player.getWorld().rayTraceEntities(
                    eye,
                    pelletDir,
                    maxRange,
                    0.0, // 精确碰撞箱
                    entity -> !entity.equals(player) && entity instanceof LivingEntity
            );

            if (result != null && result.getHitEntity() instanceof LivingEntity hitEntity) {
                hitCounts.merge(hitEntity, 1, Integer::sum);
            }
        }

        // 对每个命中的实体造成总伤害 = 单颗弹丸伤害 × 命中次数
        for (Map.Entry<LivingEntity, Integer> entry : hitCounts.entrySet()) {
            LivingEntity target = entry.getKey();
            int timesHit = entry.getValue();
            int totalDamage = damagePerPellet * timesHit;

            DamageSource source = DamageSource.builder(DamageType.ARROW) // 伤害类型
                    .withDirectEntity(player)                                 // 直接来源
                    .withDamageLocation(target.getLocation())                 // 伤害位置
                    .build();


            target.damage(totalDamage, source);
            server.broadcast(Component.text("霰弹命中 " + target.getName() + " ×" + timesHit));
        }
    }
    //这是水平的
    public static Vector getScatteredDirectionHorizontal(Vector base, double spreadAngleDegrees, Random random) {
        if (spreadAngleDegrees <= 0) {
            return base.clone();
        }
        // 随机水平偏移角度（-spread 到 +spread 度）
        double yawOffset = (random.nextDouble() * 2 - 1) * spreadAngleDegrees;
        // 绕世界 Y 轴旋转，保持原有俯仰角不变
        Vector scattered = base.clone().rotateAroundY(Math.toRadians(yawOffset));
        return scattered.normalize();
    }


    //这个是圆锥的
    public static Vector getScatteredDirection(Vector base, double spreadAngleDegrees, Random random) {
        if (spreadAngleDegrees <= 0) {
            return base.clone();
        }

        // 将角度转换为弧度
        double spreadRadians = Math.toRadians(spreadAngleDegrees);

        // 生成两个随机角度：绕Y轴（水平散射）和绕垂直平面（垂直散射）
        // 为了让散射均匀分布在圆锥内，这里采用球坐标采样
        double azimuth = random.nextDouble() * 2 * Math.PI;       // 0 到 2pi
        double polar = random.nextDouble() * spreadRadians;       // 0 到最大散射角

        // 基准方向向量
        Vector dir = base.clone();
        // 构造一个垂直于 base 的向量
        Vector perpendicular;
        if (Math.abs(dir.getX()) < 0.1 && Math.abs(dir.getZ()) < 0.1) {
            // 如果方向几乎垂直（例如正上方或正下方），则使用 Y 轴构建垂直平面
            perpendicular = new Vector(1, 0, 0).crossProduct(dir).normalize();
        } else {
            perpendicular = new Vector(0, 1, 0).crossProduct(dir).normalize();
        }
        Vector axis = dir.clone().crossProduct(perpendicular).normalize();

        // 使用罗德里格斯旋转公式生成锥形方向
        Vector scattered = dir.clone()
                .rotateAroundAxis(perpendicular, polar)
                .rotateAroundAxis(axis, azimuth);
        return scattered.normalize();
    }


    //枪射击伤害事件
    public static void hurtevet(@Nullable Entity hitEntity , int dammage) {
        Damageable damageable = (Damageable) hitEntity;
        if (damageable != null) {
            server.broadcast(Component.text("hurt"));
            damageable.damage(dammage);
        }
    }
}
