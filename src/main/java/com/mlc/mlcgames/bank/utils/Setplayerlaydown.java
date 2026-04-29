package com.mlc.mlcgames.bank.utils;

import com.mlc.mlcgames.Teammanager;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Collection;

import static com.mlc.mlcgames.Mlcgames.*;

public class Setplayerlaydown {
    public Shulker shulker;
    public ItemDisplay itemDisplay;
    public BukkitTask tp;
    public BukkitTask savetask;
    public Location location;
    public boolean issaved;
    public int savetime = 10;

    public Setplayerlaydown(Player player){
        player.addPotionEffect(new PotionEffect((PotionEffectType.SLOWNESS),9999*20,255,true,false));
        player.addPotionEffect(new PotionEffect((PotionEffectType.WEAKNESS),9999*20,255,true,false));
        player.addPotionEffect(new PotionEffect((PotionEffectType.REGENERATION), 9999*20,255,true ,false));
        player.addPotionEffect(new PotionEffect((PotionEffectType.LUCK),9999*20,1,true,false ));
        location = player.getLocation().add(0,1,0);
        issaved =false;
        itemDisplay = (ItemDisplay) location.getWorld().spawnEntity(location, EntityType.ITEM_DISPLAY);
        shulker = (Shulker) location.getWorld().spawnEntity(location, EntityType.SHULKER);
        shulker.setAI(false);
        shulker.setSilent(false);
        shulker.setInvisible(true);
        shulker.setInvulnerable(true);
        shulker.setGravity(false);
        shulker.setPeek(0);
        AttributeInstance attributeInstance =  shulker.getAttribute(Attribute.SCALE);
        if (attributeInstance != null) {
            attributeInstance.setBaseValue(0.1);
        }
        itemDisplay.addPassenger(shulker);
        tp = new BukkitRunnable() {
            @Override
            public void run() {
                if(issaved|| !bankgame.isStart){
                    this.cancel();
                }
                player.sendActionBar(miniMessage.deserialize("<b><red>你已被击倒,等待队友救援"));
                itemDisplay.teleport(player.getLocation().add(0,1,0));
            }
        }.runTaskTimer(instance,0,1);

        savetask = new BukkitRunnable(){
            @Override
            public void run(){
                if(issaved|| !bankgame.isStart){
                    this.cancel();
                }
                boolean hasfriend =false;
                location = player.getLocation();
                Collection<Entity> ent = location.getNearbyEntities(1,1,1);
                for(Entity entity :ent ){
                    if(entity instanceof Player player1){
                        if(!player1.equals(player) && Teammanager.getPlayerTeam(player1).equals(Teammanager.getPlayerTeam(player)) && !player1.hasPotionEffect(PotionEffectType.LUCK)){
                            hasfriend = true;
                            player1.sendActionBar(miniMessage.deserialize("<#0dff0d><b>剩余救援时间 "+savetime));
                            savetime -= 1;
                            player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP,1,2);
                            if(savetime==0){
                                instance.getServer().broadcast(miniMessage.deserialize( "<head:"+ player1.getName() + "> " + " <b>SAVE<GREEN>✔ -><reset> " + " <head:"+ player.getName() + ">"));
                                saveplayer(player);
                                player.playSound(player, Sound.BLOCK_AMETHYST_BLOCK_BREAK,1,1);
                            }
                            break;
                        }
                    }
                }
                if(!hasfriend){
                    savetime = 10;
                }
            }
        }.runTaskTimer(instance,0,20);

    }

    private void saveplayer(Player player) {
        issaved = true;

        player.removePotionEffect(PotionEffectType.REGENERATION);
        player.removePotionEffect(PotionEffectType.WEAKNESS);
        player.removePotionEffect(PotionEffectType.SLOWNESS);
        player.removePotionEffect(PotionEffectType.LUCK);
        shulker.remove();
        itemDisplay.remove();
    }
}
