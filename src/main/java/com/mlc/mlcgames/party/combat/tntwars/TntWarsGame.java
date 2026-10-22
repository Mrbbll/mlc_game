package com.mlc.mlcgames.party.combat.tntwars;

import com.mlc.mlcgames.party.combat.common.*;
import com.mlc.mlcgames.party.core.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.*;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.*;
import org.bukkit.util.Vector;
import java.util.*;

/** Explosive projectiles and three mutually exclusive ten-second rain events. */
public final class TntWarsGame extends TeamCombat {
    private NativeLoot loot;
    private final int[] rainUntil=new int[3];
    private final Map<UUID,Player> owners=new HashMap<>();
    @Override public void validate(PartyConfig c,PartyConfig.Arena a){validateTeams(c,a);a.region("build");a.group("rain");loot=new NativeLoot(c);c.integer("supply-seconds",1,60);}
    @Override public List<Cuboid> writableRegions(PartyConfig.Arena a){return List.of(a.region("build"));}
    @Override public void start(PartySession s){assign(s);for(Player p:s.survivors()){spawn(s,p);p.setGameMode(GameMode.ADVENTURE);}}
    @Override public void tick(PartySession s,int elapsed){
        if(elapsed%20!=0)return;
        for(int t=0;t<2;t++){final int check=t;if(s.survivors().stream().noneMatch(p->team(p)==check)){finishTeam(s,1-t,2);return;}}
        for(Player p:s.survivors()){
            p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE,60,25,true,false));p.setGlowing(true);
            if(elapsed%(s.config.integer("supply-seconds",1,60)*20)==0)for(ItemStack i:loot.roll("tntwars/tnttool2"))p.getInventory().addItem(i);
            boolean bow=Arrays.stream(p.getInventory().getContents()).filter(Objects::nonNull).anyMatch(i->kind(i)==9);
            if(bow)p.getInventory().setItem(35,new ItemStack(Material.ARROW));
        }
        List<Location> points=s.arena.group("rain");Random random=new Random();
        for(int i=0;i<3;i++)if(elapsed<rainUntil[i]){
            Location l=points.get(random.nextInt(points.size())).clone();spawnExplosive(s,null,l,new Vector(0,i==0?-.8:i==1?-.5:-.3,0),i==0?2:i==1?3:4,true);
        }
    }
    private int kind(ItemStack i){return i.getItemMeta().getPersistentDataContainer().getOrDefault(new NamespacedKey("mlcgames","source_tnt"),PersistentDataType.INTEGER,0);}
    @Override public void interact(PartySession s,PlayerInteractEvent e){
        if(e.getHand()!=EquipmentSlot.HAND||!e.getAction().isRightClick()||e.getItem()==null)return;
        int kind=kind(e.getItem());if(kind<1||kind>8)return;e.setCancelled(true);Player p=e.getPlayer();
        if(kind>=5&&kind<=7){int i=kind-5;if(s.elapsed()<rainUntil[i])return;rainUntil[i]=s.elapsed()+200;s.broadcast("rain","player",p.getName(),"type",s.config.text("rain-types."+i));}
        else {
            Vector d=p.getEyeLocation().getDirection();
            if(kind==1)for(int distance:List.of(2,4,6))spawnExplosive(s,p,p.getEyeLocation().add(d.clone().multiply(distance)),d.clone().multiply(2),1,false);
            else spawnExplosive(s,p,p.getEyeLocation().add(d),d.clone().multiply(kind==2?1.2:kind==3?3:kind==4?.25:2),kind,false);
        }
        p.getInventory().setItemInMainHand(null);p.playSound(p.getLocation(),Sound.ENTITY_ARROW_SHOOT,1,2);
    }
    private void spawnExplosive(PartySession s,Player owner,Location l,Vector velocity,int kind,boolean rain){
        if(!s.arena.contains(l))return;Entity entity;
        if(kind==1){org.bukkit.entity.minecart.ExplosiveMinecart cart=s.arena.world().spawn(l,org.bukkit.entity.minecart.ExplosiveMinecart.class);cart.setFuseTicks(80);entity=cart;}
        else if(kind==3){Creeper creeper=s.arena.world().spawn(l,Creeper.class);creeper.setMaxFuseTicks(rain?30:60);creeper.ignite();creeper.setAI(false);creeper.setInvulnerable(rain);entity=creeper;}
        else if(kind==4){LargeFireball ball=s.arena.world().spawn(l,LargeFireball.class);ball.setYield(rain?4:5);ball.setIsIncendiary(false);if(owner!=null)ball.setShooter(owner);ball.setDirection(velocity.clone().normalize().multiply(.4));entity=ball;}
        else {TNTPrimed tnt=s.arena.world().spawn(l,TNTPrimed.class);tnt.setFuseTicks(50);if(owner!=null)tnt.setSource(owner);entity=tnt;}
        entity.setVelocity(velocity);s.track(entity);if(owner!=null)owners.put(entity.getUniqueId(),owner);
    }
    @Override public boolean launch(PartySession s,Player p){
        // TNT bow launches one arrow with an eighty-tick fuse riding it.
        return true;
    }
    @Override public void shoot(PartySession s,EntityShootBowEvent e){
        if(!(e.getEntity() instanceof Player p)||e.getBow()==null||kind(e.getBow())!=9)return;
        if(e.getProjectile() instanceof AbstractArrow arrow){TNTPrimed tnt=s.arena.world().spawn(arrow.getLocation(),TNTPrimed.class);tnt.setFuseTicks(80);tnt.setSource(p);s.track(tnt);arrow.addPassenger(tnt);}
    }
    @Override public boolean allowSwap(PartySession s,Player p){return true;}
    @Override public boolean allowInventory(PartySession s,Player p){return true;}
    @Override public void projectile(PartySession s,ProjectileHitEvent e){
        if(e.getEntity() instanceof AbstractArrow arrow&&arrow.getShooter() instanceof Player p){
            for(Entity passenger:arrow.getPassengers())passenger.leaveVehicle();
        }
    }
    @Override public void hit(PartySession s,Player a,Player v,EntityDamageByEntityEvent e){e.setCancelled(false);e.setDamage(0);}
    @Override public void environmentalDamage(PartySession s,Player p,EntityDamageEvent e){e.setCancelled(false);e.setDamage(0);}
    /** Called by PartyModule for owned explosion entities; mutations remain journalled. */
    public void explode(PartySession s,EntityExplodeEvent e){
        if(!s.owns(e.getEntity()))return;
        for(org.bukkit.block.Block b:List.copyOf(e.blockList()))if(s.writable(b))s.set(b,Material.AIR);
        e.blockList().clear();
    }
    @Override public void timeout(PartySession s){s.finish(List.of());}
}
