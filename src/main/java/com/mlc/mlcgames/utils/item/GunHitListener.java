package com.mlc.mlcgames.utils.item;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class GunHitListener implements Listener {
    @EventHandler
    public void onexplodeProjectileHit(ProjectileHitEvent event){
        Entity entity = event.getEntity();
        PersistentDataContainer pdc = entity.getPersistentDataContainer();
        Double damage = 0.0D;
        Double range = 0.0D;
        if(!pdc.has(GunShot.damagekey, PersistentDataType.DOUBLE)){
            return;
        }else {
            damage = pdc.get(GunShot.damagekey, PersistentDataType.DOUBLE);
        }
        if(!pdc.has(GunShot.rangekey, PersistentDataType.DOUBLE)){
            return;
        }else {
            range = pdc.get(GunShot.rangekey, PersistentDataType.DOUBLE);
        }
        Throwable.explode(entity, (Entity) event.getEntity().getShooter(), range, damage);
    }
}
