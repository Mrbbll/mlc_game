package com.mlc.mlcgames.zombieday.zombie;

import com.destroystokyo.paper.entity.Pathfinder;
import com.mlc.mlcgames.zombieday.Item;
import com.mlc.mlcgames.zombieday.Zombiedaygame;
import io.papermc.paper.threadedregions.scheduler.EntityScheduler;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

import static com.mlc.mlcgames.Mlcgames.*;
import static com.mlc.mlcgames.zombieday.Zombiedaygame.*;

public class Spwaner {
    public static NamespacedKey zombie_type = new NamespacedKey(instance,"zombie_type");


    public static void spawnzombie(Location location, int count,double damage,double health,double addspeed,Zombietype type){
        World world = location.getWorld();
        for(int i = 0;i<count;i++){
            Zombie zombie = (Zombie) world.spawnEntity(location, EntityType.ZOMBIE);
//            server.broadcast(miniMessage.deserialize("spawn zombie"));
            Objects.requireNonNull(zombie.getAttribute(Attribute.ATTACK_DAMAGE)).setBaseValue(damage);
            Objects.requireNonNull(zombie.getAttribute(Attribute.MAX_HEALTH)).setBaseValue(health);
            Objects.requireNonNull(zombie.getAttribute(Attribute.MOVEMENT_SPEED)).addModifier(new AttributeModifier(new NamespacedKey(instance,"zombie_speed"),addspeed,AttributeModifier.Operation.ADD_NUMBER));
            setZombieType(zombie,type);
            zombies.add(zombie);
            zombiecount = zombies.size();
            EntityScheduler entityScheduler = zombie.getScheduler();

            //僵尸寻路
            entityScheduler.runAtFixedRate(instance, scheduledTask->{
                if(zombie.getPathfinder().hasPath()){
//                    server.broadcast(miniMessage.deserialize("has way"));
                    return;
                }
                if(!isstart){
                    zombie.remove();
                }
                for(Player player : zombie.getLocation().getNearbyPlayers(60, 60, 60)){
                    if(player != null&&player.getGameMode()!=GameMode.CREATIVE && player.getGameMode()!= GameMode.SPECTATOR){
                            zombie.getPathfinder().moveTo(player);
                        }
                    }
            },null,1,20);
//            server.getMobGoals().addGoal(zombie,0,new ZombieGoal(zombie,speed));
        }
    }

    public static void setZombieType(Zombie zombie,Zombietype type){
        zombie.getPersistentDataContainer().set(zombie_type, PersistentDataType.STRING,type.getType());
        switch (type){
            case NORMAL:
                zombie.getEquipment().setHelmet(Item.lether_helmet);
                break;
            case FAST:
                zombie.getEquipment().setHelmet(Item.lether_helmet);
                zombie.getEquipment().setBoots(Item.lether_boots);
                break;
            case HIGHJUMP:
                zombie.getEquipment().setHelmet(Item.lether_helmet);
                zombie.getEquipment().setBoots(Item.iron_boots);
                zombie.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST,999999,2,true,false));
                break;
            case POLICE:
                zombie.getEquipment().setHelmet(Item.neitherite_helmet);
                break;
            case RICH:
                zombie.getEquipment().setHelmet(Item.iron_helmet);
                break;
            case BOSS:

                zombie.getEquipment().setHelmet(Item.neitherite_helmet);
                zombie.getEquipment().setBoots(Item.neitherite_boots);
                zombie.getEquipment().setChestplate(Item.neitherite_chestplate);
                zombie.getEquipment().setLeggings(Item.neitherite_leggings);
                zombie.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING,999999,1,true,false));
                break;
            default:
                break;
        }
    }


    private static Location getnearestpathpoint(Zombie zombie) {
        for(Block obstacle : obstacles){
            if(obstacle.getType()== Material.AIR){
                server.broadcast(miniMessage.deserialize("empty obstacle"));
                continue;
            }
            Pathfinder.PathResult result = zombie.getPathfinder().findPath(obstacle.getLocation().add(1,0,0));
            if (result != null && result.canReachFinalPoint()) {
                return result.getFinalPoint();
            }
        }
        server.broadcast(miniMessage.deserialize("no way to obstacle find"));
        return null;
    }

    private static Location getnearestobstacle(@NotNull Location location) {
        double minDistance = Double.MAX_VALUE;
        Location nearestObstacle = null;
        for(Block obstacle : obstacles){
            double distance = obstacle.getLocation().distance(location);
            if(distance < minDistance){
                minDistance = distance;
                nearestObstacle = obstacle.getLocation();
            }
        }
        return nearestObstacle;
    }

    public static void spawnrandomzombie(int num1, Location zombieloc1,double damage,double health,double addspeed) {
        BukkitTask task = new BukkitRunnable(){
            int count = num1;
            @Override
            public void run() {
                if(!isstart){
                    this.cancel();
                }

                if(count -- > 0){
                    spawnzombie(zombieloc1,1,damage,health,addspeed,Zombietype.getRandomType(turn));
                } else {
                    this.cancel();
                }
            }
        }.runTaskTimer(instance,0,10);
    };

    public static void spawnboss(int i, Location zombieloc1, double v, double v1, double v2) {
        spawnzombie(zombieloc1,i,v,v1,v2,Zombietype.BOSS);
    }

    public static void spawnrandomzombieinair(int numair, double damage, double health, double speed) {

        int n = zombieskylocs.size();
        if (n == 0 || numair <= 0) return;

        // 将 numair 随机分成 n 份
        int[] counts = splitRandomIntAllowZero(numair, n);

        int turn = Zombiedaygame.turn; // 获取当前轮次（按你的实际类名调整）

        for (int i = 0; i < n; i++) {
            spawnzombie(zombieskylocs.get(i), counts[i], damage, health, speed, Zombietype.getRandomType(turn));
            }
    }



    public static int[] splitRandomIntAllowZero(int total, int n) {
        int[] parts = new int[n];
        for (int i = 0; i < total; i++) {
            int idx = ThreadLocalRandom.current().nextInt(n);
            parts[idx]++;
        }
        return parts;
    }

}

