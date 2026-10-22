package com.mlc.mlcgames.party.combat.common;

import com.mlc.mlcgames.party.core.PartySession;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.util.Vector;
import java.util.*;

public final class Explosives {
    private Explosives(){}
    public static void launch(PartySession s,Player p,int kind){
        Vector d=p.getEyeLocation().getDirection();
        if(kind==1)for(int distance:List.of(2,4,6))spawn(s,p,p.getEyeLocation().add(d.clone().multiply(distance)),d.clone().multiply(2),1,false,5);
        else spawn(s,p,p.getEyeLocation().add(d),d.clone().multiply(kind==2?1.2:kind==3?3:kind==4?.25:2),kind,false,s.config.id.equals("cloud")?2:5);
    }
    public static void spawn(PartySession s,Player owner,Location l,Vector velocity,int kind,boolean rain,float power){
        if(!s.arena.contains(l))return;Entity entity;
        if(kind==1){org.bukkit.entity.minecart.ExplosiveMinecart cart=s.arena.world().spawn(l,org.bukkit.entity.minecart.ExplosiveMinecart.class);cart.setFuseTicks(80);entity=cart;}
        else if(kind==3){Creeper creeper=s.arena.world().spawn(l,Creeper.class);creeper.setMaxFuseTicks(rain?30:60);creeper.ignite();creeper.setAI(false);creeper.setInvulnerable(rain);entity=creeper;}
        else if(kind==4){LargeFireball ball=s.arena.world().spawn(l,LargeFireball.class);ball.setYield(power);ball.setIsIncendiary(false);if(owner!=null)ball.setShooter(owner);ball.setDirection(velocity.clone().normalize().multiply(.4));entity=ball;}
        else {TNTPrimed tnt=s.arena.world().spawn(l,TNTPrimed.class);tnt.setFuseTicks(50);if(owner!=null)tnt.setSource(owner);entity=tnt;}
        entity.setVelocity(velocity);s.track(entity);
    }
    public static void explode(PartySession s,EntityExplodeEvent e){
        if(!s.owns(e.getEntity()))return;
        for(org.bukkit.block.Block b:List.copyOf(e.blockList()))if(s.writable(b))s.set(b,Material.AIR);
        e.blockList().clear();
    }
}
