package com.mlc.mlcgames.zombieday.zombie;

import com.destroystokyo.paper.entity.Pathfinder;
import com.mlc.mlcgames.zombieday.Item;
import io.papermc.paper.threadedregions.scheduler.EntityScheduler;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static com.mlc.mlcgames.Mlcgames.*;
import static com.mlc.mlcgames.zombieday.Zombiedaygame.obstacles;

public class Spwaner {
    public static NamespacedKey zombie_type = new NamespacedKey(instance,"zombie_type");


    public static void spawnzombie(Location location, int count,int damage,int health,double speed,Zombietype type){
        World world = location.getWorld();
        for(int i = 0;i<count;i++){


            Zombie zombie = (Zombie) world.spawnEntity(location, EntityType.ZOMBIE);
            Objects.requireNonNull(zombie.getAttribute(Attribute.ATTACK_DAMAGE)).setBaseValue(damage);
            Objects.requireNonNull(zombie.getAttribute(Attribute.MAX_HEALTH)).setBaseValue(health);
            Objects.requireNonNull(zombie.getAttribute(Attribute.MOVEMENT_SPEED)).setBaseValue(speed);
            zombie.setMaximumNoDamageTicks(2);
            setZombieType(zombie,type);

            EntityScheduler entityScheduler = zombie.getScheduler();

            //僵尸寻路
            entityScheduler.runAtFixedRate(instance, scheduledTask->{
                if(zombie.getPathfinder().hasPath()){
                    server.broadcast(miniMessage.deserialize("has way"));
                    return;
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
}
