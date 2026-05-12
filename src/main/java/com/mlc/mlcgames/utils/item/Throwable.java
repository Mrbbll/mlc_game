package com.mlc.mlcgames.utils.item;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import static com.mlc.mlcgames.Mlcgames.instance;
import static com.mlc.mlcgames.zombieday.Item.grenade;

public class Throwable {
    public static void throwgrenade(Player player){
        // 在玩家眼睛位置稍前生成一个物品实体作为手榴弹
        Location spawnLoc = player.getEyeLocation().add(player.getLocation().getDirection().multiply(0.5));
        Item grenadeEntity = player.getWorld().dropItem(spawnLoc, grenade);
        grenadeEntity.setPickupDelay(Integer.MAX_VALUE);  // 防止被捡起
        grenadeEntity.setVelocity(player.getLocation().getDirection().multiply(0.8)); // 投掷速度

        // 启动爆炸计时器
        new GrenadeFuseTask(grenadeEntity, player).runTaskTimer(instance, 0, 20);
    }

    public static class GrenadeFuseTask extends BukkitRunnable {

        private final Item grenade;
        private final Player thrower;
        private int ticksLeft = 3; // 3秒

        public GrenadeFuseTask(Item grenadeEntity, Player thrower) {
            this.grenade = grenadeEntity;
            this.thrower = thrower;
        }

        @Override
        public void run() {
            // 手榴弹可能已消失（如服务器重启或未知原因）
            if (grenade == null || !grenade.isValid()) {
                cancel();
                return;
            }

            if (ticksLeft <= 0) {
                explode();
                cancel();
                return;
            }

            //倒计时提示音效粒子
            grenade.getWorld().playSound(grenade.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 0.5f, 2.0f);
            ticksLeft--;
        }

        private void explode() {
            Location loc = grenade.getLocation();
            grenade.remove(); // 移除手榴弹实体

            // 播放爆炸特效
            loc.getWorld().spawnParticle(Particle.EXPLOSION, loc, 1);
            loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);

            double explosionRadius = 8.0;  // 爆炸半径
            double maxDamage = 60.0;       // 中心最大伤害

            // 获取范围内的所有生物（包括玩家）
            for (LivingEntity entity : loc.getNearbyLivingEntities(explosionRadius)) {

                // 计算距离因子（线性衰减：边缘伤害为0）
                double distance = entity.getLocation().distance(loc);
                double damageFactor = 1.0 - (distance / explosionRadius); // 0.0 到 1.0
                damageFactor = Math.max(0.0, damageFactor);
                double finalDamage = maxDamage * damageFactor;

                if (finalDamage <= 0) continue;

                // 使用 DamageSource 包装伤害
                DamageSource source = DamageSource.builder(DamageType.EXPLOSION)
                        .withDirectEntity(thrower)         // 来源是投掷者
                        .withDamageLocation(loc)
                        .build();

                entity.damage(finalDamage, source);
            }
        }
    }
}
