package com.mlc.mlcgames.zombieday.listener;

import com.mlc.mlcgames.Teammanager;
import com.mlc.mlcgames.zombieday.Zombiedaygame;
import org.bukkit.Location;
import org.bukkit.Material;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import static com.mlc.mlcgames.Mlcgames.instance;
import static com.mlc.mlcgames.zombieday.Zombiedaygame.obstacles;
import static com.mlc.mlcgames.zombieday.Zombiedaygame.players;

public class Obstacle {

    public static void ObstaclebreakeventListener(Location location){
        BukkitTask breaktask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!Zombiedaygame.isstart) {
                    this.cancel();
                }
                for (Entity entity : location.getWorld().getEntitiesByClasses(Zombie.class)) {

                    Location loc = entity.getLocation();
                    Location toploc = loc.clone().add(0,1,0);

                    Vector facing = loc.getDirection().normalize();
                    Block frontBlock = loc.clone().add(facing).getBlock();
                    Block topBlock = toploc.add(facing).getBlock();

                    if (frontBlock.getType() == Material.OAK_FENCE) {
                        // 让方块消失（变成空气）

                        frontBlock.setType(Material.AIR);
                        loc.getWorld().playSound(entity, Sound.BLOCK_CHERRY_WOOD_BREAK,3,1.5f);
                        loc.getWorld().spawnParticle(Particle.BLOCK_CRUMBLE,frontBlock.getLocation(),
                                20,0.5,0.5,0.5,
                                Material.OAK_FENCE.createBlockData());
                        float breaktime = Zombiedaygame.obstaclebreaktime.getOrDefault(frontBlock,0f)+0.4f;
                        if(breaktime>=1f){
                            obstacles.remove(frontBlock);
                            frontBlock.setType(Material.AIR);
                            Zombiedaygame.obstaclebreaktime.remove(frontBlock);
                        }
                        else {
                            for(Player player:players){
                                player.sendBlockDamage(frontBlock.getLocation(), breaktime);
                            }
                            Zombiedaygame.obstaclebreaktime.put(frontBlock,breaktime);
                        }

                    }
                    if (topBlock.getType() == Material.OAK_FENCE) {
                        // 让方块消失（变成空气）

                        loc.getWorld().playSound(entity, Sound.BLOCK_CHERRY_WOOD_BREAK,3,1.5f);
                        loc.getWorld().spawnParticle(Particle.BLOCK_CRUMBLE,topBlock.getLocation(),
                                20,0.5,0.5,0.5,
                                Material.OAK_FENCE.createBlockData());
                        float breaktime = Zombiedaygame.obstaclebreaktime.getOrDefault(topBlock,0f)+0.4f;
                        if(breaktime>=1f){
                            obstacles.remove(topBlock);
                            topBlock.setType(Material.AIR);
                            Zombiedaygame.obstaclebreaktime.remove(topBlock);
                        }
                        else {
                            for(Player player:players){
                                player.sendBlockDamage(topBlock.getLocation(), breaktime);
                            }
                            Zombiedaygame.obstaclebreaktime.put(topBlock,breaktime);
                        }
                    }
                }
            }
        }.runTaskTimer(instance,0,20);
    }

    public static void ObstaclefixeventListener() {
        for(Player player: Teammanager.getteamplayer(Teammanager.zombieday_team)){

            BukkitTask fixtask = new BukkitRunnable(){
                int countdown = 0;
                @Override
                public void run() {
                    if(!Zombiedaygame.isstart){
                        this.cancel();
                    }
                    if(!player.isOnline()){
                        this.cancel();
                    }
                    if(!player.isSneaking()){
                        countdown=0;
                        return;
                    }
                    ItemStack itemStack = player.getInventory().getItemInMainHand();
                    if(!itemStack.getType().equals(Material.OAK_FENCE)){
                        countdown=0;
                        return;
                    }
                    Location location = player.getLocation();
                    Block footblock = location.getBlock();
                    if(footblock.getType()!=Material.AIR){
                        countdown=0;
                        return;
                    }

                    Block block = location.clone().add(0,-1,0).getBlock();
                    if(block.isSolid()&&!obstacles.contains(block)&&block.getType()!=Material.OAK_FENCE&&block.getType()!=Material.AIR){
                        countdown++;
                        if(countdown>=20){
                            Block block1 = location.getBlock();
                            block1.setType(Material.OAK_FENCE);
                            obstacles.add(block1);
                            Zombiedaygame.obstaclebreaktime.put(block1,0f);
                            itemStack.setAmount(itemStack.getAmount()-1);
                            player.playSound(location, Sound.BLOCK_STONE_PLACE,1,1);
                            countdown=0;
                        }
                    }
                }

            }.runTaskTimer(instance,0,1);
        }
    }

}
