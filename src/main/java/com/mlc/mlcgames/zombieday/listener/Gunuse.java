package com.mlc.mlcgames.zombieday.listener;

import com.mlc.mlcgames.Teammanager;
import com.mlc.mlcgames.zombieday.Item;
import com.mlc.mlcgames.zombieday.Zombiedaygame;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

public class Gunuse implements Listener {
    @EventHandler
    public void onGunuse(PlayerInteractEvent event){
        if(!Zombiedaygame.isstart){
            return;
        }
        Player player = event.getPlayer();
        if(!Teammanager.isPlayerInTeam(player,Teammanager.zombieday_team)){
            return;
        }
        if (event.getItem() != null && !event.getItem().getType().equals(Material.ECHO_SHARD)) {
            return;
        }
        Action action = event.getAction();
        if(action.equals(Action.RIGHT_CLICK_AIR)||action.equals(Action.RIGHT_CLICK_BLOCK)){
            Gunshotevent(player,event.getItem());
        }
        if(action.equals(Action.LEFT_CLICK_AIR)||action.equals(Action.LEFT_CLICK_BLOCK)){
            Gunrefillevent();
        }
    }

    private void Gunrefillevent() {

    }

    private void Gunshotevent(Player player, ItemStack gun) {
        Location eye  = player.getEyeLocation();
        Vector direction = eye.getDirection();
        var result = player.getWorld().rayTraceEntities(
                eye,
                direction,
                20,
                0.0, // 实体边界扩展，0 表示精确碰撞箱
                entity -> !entity.equals(player) && entity instanceof LivingEntity // 只关心活体
        );

        if (result != null && result.getHitEntity() != null) {
            hurtevet(result.getHitEntity(),gun);
        }

    }

    private void hurtevet(@Nullable Entity hitEntity,ItemStack gun) {
        if (gun.equals(Item.handgun)) {
            Damageable damageable = (Damageable) hitEntity;
            if (damageable != null) {
                damageable.damage(10);

            }
        } else {
            throw new IllegalStateException("Unexpected value: " + gun.getType());
        }
    }
}
