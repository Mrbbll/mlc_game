package com.mlc.mlcgames.utils.particle;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public class Gunparticle {
    public static void lineGunshotparticle(Location eye, RayTraceResult hurtentity){
        //这个是连线版的
//        Vector hitPos = hurtentity.getHitPosition();
//        Location start = eye.clone();
//        Location end = start.clone().add(eye.getDirection().clone().multiply(start.distance(hitPos.toLocation(start.getWorld()))));
//
//        World world = start.getWorld();
//        Vector dir = eye.getDirection().clone().normalize();
//        double distance = start.distance(end);
//
//        for (double d = 0; d <= distance; d += 1.0) {
//            Location point = start.clone().add(dir.clone().multiply(d));
//            world.spawnParticle(Particle.ELECTRIC_SPARK, point, 1, 0, 0, 0, 0);
//        }
        //这个就是两端
        World world = eye.getWorld();
        world.spawnParticle(Particle.FLAME,eye,3,0.2,0.2,0.2,0);
        if(hurtentity!=null){
            Location hitLoc = hurtentity.getHitPosition().toLocation(world);
            world.spawnParticle(
                    Particle.BLOCK_CRUMBLE,
                    hitLoc,
                    30,                     // 粒子数量
                    0.3, 0.3, 0.3,         // 偏移范围（XYZ）
                    0,                      // 额外数据（速度，用于某些粒子）
                    Material.RED_GLAZED_TERRACOTTA.createBlockData()   // 方块数据（红色）
            );
        }
    }
}
