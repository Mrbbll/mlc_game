package com.mlc.mlcgames.party.combat.duel;

import com.mlc.mlcgames.party.combat.common.TeamCombat;
import com.mlc.mlcgames.party.core.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.*;
import java.util.*;

/** Battle Bridge: five goals, resettable bridge and five-second cages. */
public final class DuelGame extends TeamCombat {
    private final int[] score=new int[2];
    private List<Cuboid> forbidden;
    private int releaseAt;
    @Override public void validate(PartyConfig c,PartyConfig.Arena a){
        validateTeams(c,a);a.region("build");a.region("red-goal");a.region("blue-goal");
        a.region("red-cage");a.region("blue-cage");
        forbidden=a.regions().entrySet().stream().filter(e->e.getKey().startsWith("no-build-")).map(Map.Entry::getValue).toList();
        c.integer("goals-required",1,100);c.integer("goal-points",0,100);c.integer("cage-seconds",1,30);
        if(a.region("build").volume()>c.integer("limits.max-snapshot-blocks",1,100000))throw new IllegalArgumentException("建造区过大");
        for(String key:List.of("red-cage","blue-cage"))if(!a.region("build").contains(a.region(key)))throw new IllegalArgumentException("笼子必须位于build内");
    }
    @Override public List<Cuboid> writableRegions(PartyConfig.Arena a){return List.of(a.region("build"));}
    @Override public void start(PartySession s){assign(s);newRound(s);}
    private void equipment(PartySession s,Player p){
        p.getInventory().setItem(0,item(s,Material.IRON_SWORD));p.getInventory().setItem(1,item(s,Material.DIAMOND_PICKAXE));
        for(int slot=2;slot<=5;slot++){ItemStack b=item(s,team(p)==0?Material.YELLOW_TERRACOTTA:Material.BLUE_TERRACOTTA);b.setAmount(64);p.getInventory().setItem(slot,b);}
        p.getInventory().setItem(6,item(s,Material.BOW));p.getInventory().setItem(7,new ItemStack(Material.ARROW,4));p.getInventory().setItem(8,new ItemStack(Material.GOLDEN_APPLE,3));
        p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE,40,25,true,false));
    }
    private void newRound(PartySession s){
        for(Player p:s.survivors()){spawn(s,p);equipment(s,p);}
        for(String key:List.of("red-cage","blue-cage"))fill(s,s.arena.region(key),key.startsWith("red")?Material.YELLOW_STAINED_GLASS:Material.BLUE_STAINED_GLASS);
        // Cage regions are door panels, supplied by the new map rather than a solid spawn box.
        releaseAt=s.elapsed()+s.config.integer("cage-seconds",1,30)*20;
        s.broadcast("score","red",score[0],"blue",score[1]);
    }
    @Override public void tick(PartySession s,int elapsed){
        if(teamPlayers(s,0).isEmpty()||teamPlayers(s,1).isEmpty()){s.finish(List.of());return;}
        if(elapsed==releaseAt){fill(s,s.arena.region("red-cage"),Material.AIR);fill(s,s.arena.region("blue-cage"),Material.AIR);s.broadcast("released");}
    }
    @Override public void moved(PartySession s,PlayerMoveEvent e){
        Player p=e.getPlayer();if(e.getTo()==null)return;
        if(s.elapsed()<releaseAt){if(e.hasChangedPosition())e.setTo(e.getFrom());return;}
        Location l=e.getTo();int t=team(p);
        for(int own=0;own<2;own++)if(s.arena.region(own==0?"red-goal":"blue-goal").contains(l.getBlockX(),l.getBlockY(),l.getBlockZ())){
            if(own==t){hazard(s,p);return;} score[t]++;
            if(score[t]>=s.config.integer("goals-required",1,100)){finishTeam(s,t,s.config.integer("goal-points",0,100));return;}
            if(s.config.rules.getBoolean("clear-bridge-on-goal"))s.resetRound(()->{s.resetPlayers();newRound(s);});else newRound(s);
            return;
        }
    }
    @Override public void hazard(PartySession s,Player p){spawn(s,p);equipment(s,p);}
    @Override public void hit(PartySession s,Player a,Player v,EntityDamageByEntityEvent e){
        if(s.elapsed()<releaseAt||team(a)==team(v))return;
        e.setCancelled(false);if(e.getFinalDamage()>=v.getHealth()){e.setCancelled(true);hazard(s,v);}
    }
    @Override public void environmentalDamage(PartySession s,Player p,EntityDamageEvent e){
        if(e.getCause()==EntityDamageEvent.DamageCause.FALL)return;
        e.setCancelled(false);if(e.getFinalDamage()>=p.getHealth()){e.setCancelled(true);hazard(s,p);}
    }
    private boolean legal(Block b){return forbidden.stream().noneMatch(c->c.contains(b.getX(),b.getY(),b.getZ()));}
    @Override public boolean breakBlock(PartySession s,Player p,Block b){return legal(b)&&b.getType().name().endsWith("TERRACOTTA");}
    @Override public boolean placeBlock(PartySession s,Player p,Block b){return s.elapsed()>=releaseAt&&legal(b)&&b.getType()==(team(p)==0?Material.YELLOW_TERRACOTTA:Material.BLUE_TERRACOTTA);}
    @Override public void timeout(PartySession s){finishTeam(s,score[0]==score[1]?-1:score[0]>score[1]?0:1,s.config.integer("goal-points",0,100));}
    @Override public boolean allowSwap(PartySession s,Player p){return true;}
    @Override public boolean allowInventory(PartySession s,Player p){return true;}
}
