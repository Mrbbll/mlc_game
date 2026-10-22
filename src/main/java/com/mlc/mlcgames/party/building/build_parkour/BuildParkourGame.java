package com.mlc.mlcgames.party.building.build_parkour;

import com.mlc.mlcgames.party.building.common.BuildingRounds;
import com.mlc.mlcgames.party.core.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.*;
import java.util.*;

public final class BuildParkourGame extends BuildingRounds {
    private enum Phase { BUILD, OVERTIME, WAIT_PROVE, PROVE, RUN }
    private Phase phase;
    private int failures;
    private boolean ending;
    private int proveAt;
    @Override public void validate(PartyConfig c,PartyConfig.Arena a){super.validate(c,a);int duration=c.integer("build-seconds",0,240);if(duration>0&&duration<60)throw new IllegalArgumentException("build-seconds为0自动，或60..240");c.integer("prove-seconds",15,300);c.integer("run-seconds",15,300);c.integer("overtime-seconds",1,60);}
    @Override public void start(PartySession s){initialize(s);}
    @Override protected void begin(PartySession s){
        ending=false;failures=0;phase=Phase.BUILD;int duration=s.config.integer("build-seconds",0,240);if(duration==0){int n=s.players().size();duration=n>=18?90:n>=12?120:n>=6?180:240;}deadline=s.elapsed()+duration*20;
        givePalette(s);builder.getInventory().setItem(33,s.item(Material.BAT_SPAWN_EGG,"items.trident-pad"));builder.getInventory().setItem(34,s.item(Material.CHICKEN_SPAWN_EGG,"items.finish"));
        announceBuilder(s);builder.sendMessage(s.config.message("builder-rules"));
    }
    @Override public void tick(PartySession s,int elapsed){
        if(!readyToPlay(s,elapsed))return;
        if(ending||builder==null)return;
        if(!s.member(builder)){builderFailed(s);return;}
        if(phase==Phase.WAIT_PROVE){builder.teleport(s.arena.group("builder").getFirst());if(elapsed>=proveAt){phase=Phase.PROVE;deadline=elapsed+s.config.integer("prove-seconds",15,300)*20;s.broadcast("prove");}else return;}
        if(elapsed>=deadline){
            if(phase==Phase.BUILD){phase=Phase.OVERTIME;deadline=elapsed+s.config.integer("overtime-seconds",1,60)*20;builder.getInventory().clear();builder.getInventory().setItem(0,s.item(Material.CHICKEN_SPAWN_EGG,"items.finish"));s.broadcast("overtime");}
            else if(phase==Phase.OVERTIME||phase==Phase.PROVE)builderFailed(s);
            else{failures+=playing.size();playing.clear();roundOver(s);}
            return;
        }
        if(phase==Phase.PROVE)effects(s,builder);
        if(phase==Phase.RUN){for(Player p:s.players())p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY,40,25,true,false));for(UUID id:List.copyOf(playing)){Player p=Bukkit.getPlayer(id);if(p==null||!s.member(p)){playing.remove(id);failures++;}else effects(s,p);}}
        if(phase==Phase.RUN&&playing.isEmpty()){roundOver(s);return;}
        if(elapsed%20==0){
            if(phase==Phase.BUILD)refill();
            for(Player p:s.players())p.sendActionBar(s.config.message("status","seconds",(deadline-elapsed+19)/20,"phase",s.config.text("phases."+phase.name().toLowerCase(Locale.ROOT))));
        }
    }
    @Override public void interact(PartySession s,PlayerInteractEvent e){
        if(!isBuilder(e.getPlayer())||!(phase==Phase.BUILD||phase==Phase.OVERTIME)||e.getItem()==null||!e.getAction().isRightClick())return;
        Material item=e.getItem().getType();if(item!=Material.CHICKEN_SPAWN_EGG&&item!=Material.BAT_SPAWN_EGG)return;e.setCancelled(true);
        if(e.getClickedBlock()==null)return;Block target=e.getClickedBlock().getRelative(e.getBlockFace());if(!inside(target)||!target.isEmpty())return;
        if(item==Material.CHICKEN_SPAWN_EGG){s.set(target,Material.PINK_WOOL);prove(s);}
        else if(phase==Phase.BUILD){Waterlogged slab=(Waterlogged)Material.PRISMARINE_SLAB.createBlockData();slab.setWaterlogged(true);s.setData(target,slab);
            for(var face:List.of(org.bukkit.block.BlockFace.NORTH,org.bukkit.block.BlockFace.SOUTH,org.bukkit.block.BlockFace.EAST,org.bukkit.block.BlockFace.WEST)){Block side=target.getRelative(face);if(inside(side)&&side.isEmpty())s.set(side,Material.STRUCTURE_VOID);}}
    }
    private void prove(PartySession s){
        phase=Phase.WAIT_PROVE;deadline=Integer.MAX_VALUE;int removed=0;
        for(int x=build.minX();x<=build.maxX();x++)for(int y=build.minY();y<=build.maxY();y++)for(int z=build.minZ();z<=build.maxZ();z++){Block b=s.arena.world().getBlockAt(x,y,z);if(b.getType()==Material.SCAFFOLDING){s.set(b,Material.AIR);removed++;}}
        proveAt=s.elapsed()+(removed/s.config.integer("limits.blocks-per-tick",1,2000))+2;
        builder.getInventory().clear();builder.setGameMode(GameMode.ADVENTURE);builder.teleport(s.arena.group("builder").getFirst());
    }
    private void runners(PartySession s){
        phase=Phase.RUN;deadline=s.elapsed()+s.config.integer("run-seconds",15,300)*20;
        builder.setGameMode(GameMode.SPECTATOR);builder.teleport(s.arena.spectator());
        for(Player p:s.players())if(!isBuilder(p)){playing.add(p.getUniqueId());p.getInventory().clear();p.setGameMode(GameMode.ADVENTURE);p.teleport(s.arena.group("runners").getFirst());p.sendMessage(s.config.message("runner-rules"));}
        s.broadcast("run");
    }
    private void effects(PartySession s,Player p){
        Block below=p.getLocation().subtract(0,.1,0).getBlock();Material type=below.getType();
        if(type==Material.RED_CONCRETE||type==Material.BLACK_CONCRETE){fail(s,p);return;}
        if(p.getLocation().subtract(0,.5,0).getBlock().getType()==Material.PINK_WOOL){
            if(phase==Phase.PROVE){runners(s);return;}if(playing.remove(p.getUniqueId())){add(s,p,1);p.getInventory().clear();p.setGameMode(GameMode.SPECTATOR);p.teleport(s.arena.spectator());s.broadcast("completed","player",p.getName());}return;}
        Material floor=p.getLocation().subtract(0,1,0).getBlock().getType();
        switch(floor){case LIME_CONCRETE->effect(p,PotionEffectType.JUMP_BOOST,20,3);case LIGHT_BLUE_CONCRETE->effect(p,PotionEffectType.SPEED,20,1);case PINK_CONCRETE->effect(p,PotionEffectType.LEVITATION,40,1);case YELLOW_CONCRETE->effect(p,PotionEffectType.BLINDNESS,40,2);case BROWN_CONCRETE->effect(p,PotionEffectType.SLOWNESS,20,0);default->{}}
        Block feet=p.getLocation().getBlock();
        if(feet.getType()==Material.PRISMARINE_SLAB&&feet.getBlockData() instanceof Waterlogged w&&w.isWaterlogged()){
            if(!p.getInventory().contains(Material.TRIDENT)){ItemStack trident=s.item(Material.TRIDENT,"items.trident");trident.addUnsafeEnchantment(Enchantment.RIPTIDE,1);p.getInventory().setItem(8,trident);}
        }else p.getInventory().remove(Material.TRIDENT);
    }
    private void effect(Player p,PotionEffectType effect,int ticks,int amp){p.addPotionEffect(new PotionEffect(effect,ticks,amp,true,false));}
    private void fail(PartySession s,Player p){
        if(phase==Phase.PROVE&&isBuilder(p)){builderFailed(s);return;}
        if(phase==Phase.RUN&&playing.remove(p.getUniqueId())){failures++;p.getInventory().clear();p.setGameMode(GameMode.SPECTATOR);p.teleport(s.arena.spectator());s.broadcast("failed","player",p.getName());}
    }
    private void builderFailed(PartySession s){if(ending)return;for(Player p:s.players())if(!isBuilder(p))add(s,p,1);failures=0;s.broadcast("builder-failed");roundOver(s);}
    private void roundOver(PartySession s){if(ending)return;ending=true;if(failures>0&&s.member(builder))add(s,builder,1);next(s);}
    @Override public void hazard(PartySession s,Player p){if(phase==Phase.BUILD||phase==Phase.OVERTIME)super.hazard(s,p);else fail(s,p);}
    @Override public boolean breakBlock(PartySession s,Player p,Block b){return phase==Phase.BUILD&&isBuilder(p)&&inside(b)&&(palette.contains(b.getType())||b.getType()==Material.PRISMARINE_SLAB||b.getType()==Material.STRUCTURE_VOID);}
    @Override public boolean placeBlock(PartySession s,Player p,Block b){return phase==Phase.BUILD&&isBuilder(p)&&inside(b)&&palette.contains(b.getType());}
    @Override public boolean allowPhysics(PartySession s,Block b){return inside(b);}
}
