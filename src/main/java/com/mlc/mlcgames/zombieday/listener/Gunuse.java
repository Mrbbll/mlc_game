package com.mlc.mlcgames.zombieday.listener;

import com.mlc.mlcgames.Teammanager;
import com.mlc.mlcgames.utils.item.Gun;
import com.mlc.mlcgames.utils.item.GunShot;
import com.mlc.mlcgames.zombieday.Item;
import com.mlc.mlcgames.zombieday.Zombiedaygame;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import static com.mlc.mlcgames.Mlcgames.*;

public class Gunuse implements Listener {

    //枪使用事件
    @EventHandler
    public void onGunuse(PlayerInteractEvent event){
        Player player = event.getPlayer();
        if(!Teammanager.isPlayerInTeam(player,Teammanager.zombieday_team)){
//            server.broadcast(Component.text("no in team"));
            return;
        }
        ItemStack item = event.getItem();
        if (item == null || !item.getType().equals(Material.ECHO_SHARD)) {
//            server.broadcast(Component.text("no gun"));
            return;
        }

        Action action = event.getAction();

        if(action.equals(Action.RIGHT_CLICK_AIR)||action.equals(Action.RIGHT_CLICK_BLOCK)){
//            server.broadcast(Component.text("right click"));
            if(Gun.isinRefillcooldown(item)){
                player.sendActionBar(miniMessage.deserialize("<b><red>正在装弹中..."));
                return;
            }
            if(GunShot.isincooldown(item)){
                event.setCancelled(true);
                return;
            }

            int bulletcount = Gun.getbulletcount(item);
            if(bulletcount<=0){
                player.sendActionBar(miniMessage.deserialize("<b><red>子弹不足"));
                return;
            }
            Gun.setbulletcount(item,bulletcount-1);
            player.setLevel(bulletcount-1);
            Gunshotevent(player,item);

            event.setCancelled(true);
            return;
        }
        if(action.equals(Action.LEFT_CLICK_AIR)||action.equals(Action.LEFT_CLICK_BLOCK)){
//            server.broadcast(Component.text("left click"));
            int bulletcount = Gun.getbulletcount(item);
            int maxbulletcount = Gun.getmaxbulletcount(item);
            if(bulletcount==maxbulletcount){
                return;
            }

            if(Gun.isinRefillcooldown(item)){
                player.sendActionBar(miniMessage.deserialize("<b><red>正在装弹中..."));
                return;
            }
            Gunrefillevent(item,player);
            event.setCancelled(true);
        }
    }

    //枪射击判断种类并处理
    private void Gunshotevent(Player player, @NotNull ItemStack item) {
        switch (item.getItemMeta().getPersistentDataContainer().getOrDefault(Item.itemtype,PersistentDataType.STRING,"null")){
            case "handgun":
                GunShot.lineGunshot(player,14);
                GunShot.setcooldown(item, 500);
                break;
            case "rifle":
                GunShot.lineGunshot(player,10);
                GunShot.setcooldown(item,200);
                break;
            case "submachine_gun":
                GunShot.lineGunshot(player,10);
                GunShot.setcooldown(item,150);
                break;
            case "shotgun":
                GunShot.areaGunshot(player,7,10,15);
                GunShot.setcooldown(item,1000);
                break;
            case "bazooka":
                GunShot.lineexplodeshot(player,30.0,8.0);
                GunShot.setcooldown(item,1000);
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

//        server.broadcast(Component.text("gun switch"));
//        防止切换装弹中枪
//        int oldslot = event.getPreviousSlot();
//        ItemStack olditem = player.getInventory().getItem(oldslot);
//
//        if (olditem != null && Gun.isinRefillcooldown(olditem)) {
//            player.sendActionBar(miniMessage.deserialize("<b><red>正在装弹中..."));
//            event.setCancelled(true);
//            return;
//        }

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

//    @EventHandler
//    private  void  Gunthrowevent(PlayerDropItemEvent event) {
//        if (!Zombiedaygame.isstart) {
//            return;
//        }
//        if (!Teammanager.isPlayerInTeam(event.getPlayer(), Teammanager.zombieday_team)) {
//            return;
//        }
//        ItemStack item = event.getItemDrop().getItemStack();
//        if(!item.getType().equals(Material.ECHO_SHARD)){
//            return;
//        }
//        if(Gun.isinRefillcooldown(item)){
//            Gun.setRefillcooldown(item,false);
//        }
//    }
    //枪填弹事件
    private void Gunrefillevent(ItemStack gun,Player player) {
        int bulletcount = Gun.getbulletcount(gun);
        int maxbulletcount = Gun.getmaxbulletcount(gun);

        if(bulletcount>=maxbulletcount){
            return;
        }

        int invbulletcount = Gun.getinvbulletcount(player,gun);
        if(invbulletcount<=0){
            player.sendActionBar(miniMessage.deserialize("<b><red>背包弹药耗尽"));
            return;
        }
        int needbulletcount = maxbulletcount-bulletcount;
        int consumebulletcount = Math.min(invbulletcount, needbulletcount);
        int newbulletcount = bulletcount+consumebulletcount;
        Gun.removeinvbullet(player,consumebulletcount,gun);
        Gun.setRefillcooldown(gun);
        player.sendActionBar(miniMessage.deserialize("<b><red>开始装弹"));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS,100,0,true,false));

        BukkitTask task = new BukkitRunnable(){
            @Override
            public void run() {
                player.removePotionEffect(PotionEffectType.SLOWNESS);
                if(player.getInventory().getItemInMainHand().equals(gun)){
                    player.setLevel(newbulletcount);
                }
                player.sendActionBar(miniMessage.deserialize("<b><green>装弹完毕"));
                Gun.setbulletcount(gun,newbulletcount);
            }
        }.runTaskLater(instance,100);
    }

}
