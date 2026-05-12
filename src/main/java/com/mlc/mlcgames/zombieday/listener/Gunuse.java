package com.mlc.mlcgames.zombieday.listener;

import com.mlc.mlcgames.Teammanager;
import com.mlc.mlcgames.utils.item.Gun;
import com.mlc.mlcgames.utils.item.GunShot;
import com.mlc.mlcgames.zombieday.Item;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import static com.mlc.mlcgames.Mlcgames.instance;
import static com.mlc.mlcgames.Mlcgames.server;

public class Gunuse implements Listener {

    //枪使用事件
    @EventHandler
    public void onGunuse(PlayerInteractEvent event){
        Player player = event.getPlayer();
        if(!Teammanager.isPlayerInTeam(player,Teammanager.zombieday_team)){
            server.broadcast(Component.text("no in team"));
            return;
        }
        ItemStack item = event.getItem();
        if (item == null || !item.getType().equals(Material.ECHO_SHARD)) {
            server.broadcast(Component.text("no gun"));
            return;
        }

        Action action = event.getAction();

        if(action.equals(Action.RIGHT_CLICK_AIR)||action.equals(Action.RIGHT_CLICK_BLOCK)){
            server.broadcast(Component.text("right click"));
            if(GunShot.isincooldown(item)){
                server.broadcast(Component.text("in cooldown"));
                event.setCancelled(true);
                return;
            }

            int bulletcount = Gun.getbulletcount(item);
            if(bulletcount<=0){
                player.sendActionBar(Component.text("子弹不足"));
                return;
            }
            Gun.setbulletcount(item,bulletcount-1);
            Gunshotevent(player,item);

            event.setCancelled(true);
            return;
        }
        if(action.equals(Action.LEFT_CLICK_AIR)||action.equals(Action.LEFT_CLICK_BLOCK)){
            server.broadcast(Component.text("left click"));
            Gunrefillevent(item,player);
            event.setCancelled(true);
        }
    }

    //枪射击判断种类并处理
    private void Gunshotevent(Player player, @NotNull ItemStack item) {
        switch (item.getItemMeta().getPersistentDataContainer().getOrDefault(Item.itemtype,PersistentDataType.STRING,"null")){
            case "handgun":
                GunShot.lineGunshot(player,10);
                GunShot.setcooldown(item,10);
                break;
            case "rifle":
                GunShot.lineGunshot(player,6);
                break;
            case "submachine_gun":
                GunShot.lineGunshot(player,7);
                break;
            case "shotgun":
                GunShot.areaGunshot(player,5,10,15);
                break;
            case "null":
                break;
        }

    }

    //枪切换事件
    @EventHandler
    public void onGunSwitch(PlayerItemHeldEvent event){
        Player player = event.getPlayer();
        if(!Teammanager.isPlayerInTeam(player,Teammanager.zombieday_team)){
            return;
        }

        server.broadcast(Component.text("gun switch"));
        int slot = event.getNewSlot();
        ItemStack itemStack = player.getInventory().getItem(slot);
        if(itemStack == null){
            player.setLevel(0);
            return;
        }
        if (!itemStack.getType().equals(Material.ECHO_SHARD)) {
            player.setLevel(0);
            return;
        }
        int bulletcount = Gun.getbulletcount(itemStack);
        player.setLevel(bulletcount);


    }

    //枪填弹事件
    private void Gunrefillevent(ItemStack gun,Player player) {
        if(player.hasPotionEffect(PotionEffectType.SLOWNESS)){
            player.sendActionBar(Component.text("正在填弹"));
            return;
        }
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS,100,0,true,false));
        int bulletcount = Gun.getbulletcount(gun);
        int maxbulletcount = Gun.getmaxbulletcount(gun);

        if(bulletcount>=maxbulletcount){
            return;
        }

        int invbulletcount = Gun.getinvbulletcount(player);
        if(invbulletcount<=0){
            return;
        }
        int needbulletcount = maxbulletcount-bulletcount;
        int consumebulletcount = Math.min(invbulletcount, needbulletcount);
        int newbulletcount = bulletcount+consumebulletcount;
        Gun.removeinvbullet(player,consumebulletcount);
        BukkitTask task = new BukkitRunnable(){
            @Override
            public void run() {
                player.removePotionEffect(PotionEffectType.SLOWNESS);
                Gun.setbulletcount(gun,newbulletcount);
            }
        }.runTaskLater(instance,100);
    }

}
