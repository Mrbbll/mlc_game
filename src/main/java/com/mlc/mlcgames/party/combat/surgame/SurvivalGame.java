package com.mlc.mlcgames.party.combat.surgame;

import com.mlc.mlcgames.party.combat.common.ChestSurvival;
import com.mlc.mlcgames.party.core.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.*;
import java.util.*;

/** Terrain survival, with either delayed free-for-all PvP or player infection. */
public final class SurvivalGame extends ChestSurvival {
    private final Set<UUID> infected=new HashSet<>();
    private boolean infection;
    private int rescue;
    @Override public boolean lastPlayerWins(){return false;}
    @Override public void validate(PartyConfig c,PartyConfig.Arena a){
        a.region("build");a.group("infected-spawn");infection=c.rules.getBoolean("infection-mode");c.integer("grace-seconds",1,600);
    }
    @Override public void start(PartySession s){
        rescue=Math.min(2400,600+s.players().size()*90)*20;
        for(Player p:s.survivors()){p.setGameMode(GameMode.SURVIVAL);p.getInventory().setItem(8,s.item(Material.COMPASS,"items.compass"));}
    }
    @Override protected boolean pvp(PartySession s){return s.elapsed()>=s.config.integer("grace-seconds",1,600)*20;}
    private List<Player> humans(PartySession s){return s.survivors().stream().filter(p->!infected.contains(p.getUniqueId())).toList();}
    private List<Player> zombies(PartySession s){return s.survivors().stream().filter(p->infected.contains(p.getUniqueId())).toList();}
    private void finish(PartySession s,List<Player> winners){for(Player p:winners)s.points(p,2);s.finish(winners);}
    private void infect(PartySession s,Player p){
        infected.add(p.getUniqueId());p.getInventory().clear();p.setGameMode(GameMode.ADVENTURE);p.clearActivePotionEffects();p.setHealth(20);p.setFireTicks(0);p.setFallDistance(0);p.setVelocity(new org.bukkit.util.Vector());
        p.teleport(s.arena.group("infected-spawn").getFirst());s.broadcast("infected","player",p.getName());
    }
    @Override public void tick(PartySession s,int elapsed){
        if(infection){
            if(elapsed==s.config.integer("grace-seconds",1,600)*20&&!humans(s).isEmpty()){List<Player> h=humans(s);infect(s,h.get(new Random().nextInt(h.size())));}
            if(humans(s).isEmpty()){finish(s,zombies(s));return;}
            if(elapsed>=rescue){finish(s,humans(s));return;}
            if(s.survivors().size()<=1){finish(s,humans(s));return;}
        }else if(s.survivors().size()<=1){finish(s,s.survivors());return;}
        if(elapsed%20!=0)return;
        for(Player p:s.survivors()){
            boolean z=infected.contains(p.getUniqueId());
            p.addPotionEffect(new PotionEffect(PotionEffectType.HASTE,60,z?0:1,true,false));
            if(z){p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED,60,0,true,false));p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST,60,1,true,false));p.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH,60,2,true,false));}
            if(infection&&!z&&rescue-elapsed<=2400)p.setGlowing(true);
            List<Player> enemies=infection?(z?humans(s):zombies(s)):s.survivors().stream().filter(e->!e.equals(p)).toList();
            enemies.stream().min(Comparator.comparingDouble(e->e.getLocation().distanceSquared(p.getLocation()))).ifPresent(e->p.setCompassTarget(e.getLocation()));
            p.sendActionBar(s.config.message("status","seconds",Math.max(0,(infection?rescue-elapsed:s.config.integer("grace-seconds",1,600)*20-elapsed)/20)));
        }
    }
    @Override public void hit(PartySession s,Player a,Player v,EntityDamageByEntityEvent e){
        if(!pvp(s)||infection&&infected.contains(a.getUniqueId())==infected.contains(v.getUniqueId()))return;
        e.setCancelled(false);
        if(e.getFinalDamage()>=v.getHealth()){
            dropInventory(s,v);
            if(infection&&infected.contains(a.getUniqueId())){e.setCancelled(true);infect(s,v);}
        }
    }
    @Override public void environmentalDamage(PartySession s,Player p,EntityDamageEvent e){
        e.setCancelled(false);if(e.getFinalDamage()>=p.getHealth()){
            dropInventory(s,p);if(infected.contains(p.getUniqueId())){e.setCancelled(true);infect(s,p);}
        }
    }
    @Override public void hazard(PartySession s,Player p){if(infected.contains(p.getUniqueId()))infect(s,p);else super.hazard(s,p);}
    @Override public boolean breakBlock(PartySession s,Player p,Block b){
        if(infected.contains(p.getUniqueId()))return false;
        for(ItemStack stack:b.getDrops(p.getInventory().getItemInMainHand(),p)){var item=b.getWorld().dropItemNaturally(b.getLocation(),stack);s.track(item);}return true;
    }
    @Override public boolean placeBlock(PartySession s,Player p,Block b){return !infected.contains(p.getUniqueId());}
    @Override public void timeout(PartySession s){if(infection)finish(s,humans(s));else s.finish(List.of());}
}
