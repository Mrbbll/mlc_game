package com.mlc.mlcgames.party.combat.pillar;

import com.mlc.mlcgames.party.combat.common.*;
import com.mlc.mlcgames.party.core.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.util.*;

/** Generated pillars, original fourteen floor settings and accelerating bedrock contraction. */
public final class PillarGame extends ChestSurvival {
    private record Write(Block block,Material material){}
    private final ArrayDeque<Write> writes=new ArrayDeque<>();
    private long clearIndex;
    private boolean generated;
    private Cuboid build;
    private List<Location> starts;
    private int duration,nextShrink,ring;
    @Override public boolean lastPlayerWins(){return false;}
    @Override public void validate(PartyConfig c,PartyConfig.Arena a){
        validateLoot(c,a);build=a.region("build");starts=a.group("pillars");
        if(starts.size()<c.integer("players.max",2,64))throw new IllegalArgumentException("pillars点位至少等于players.max");
        int height=c.integer("pillar-height",2,128);int radius=c.integer("floor-radius",1,8);c.integer("floor-type",0,14);c.integer("item-seconds",1,60);
        for(Location p:starts){
            if(!build.contains(p.getBlockX()-radius,p.getBlockY()-height-2,p.getBlockZ()-radius)||!build.contains(p.getBlockX()+radius,p.getBlockY()-1,p.getBlockZ()+radius))throw new IllegalArgumentException("柱体和地板必须完全位于build内");
        }
    }
    @Override public boolean prepare(PartySession s,int budget){
        int width=build.maxX()-build.minX()+1,depth=build.maxZ()-build.minZ()+1;
        while(budget>0&&clearIndex<build.volume()){
            long n=clearIndex++;int x=build.minX()+(int)(n%width),z=build.minZ()+(int)((n/width)%depth),y=build.minY()+(int)(n/(width*(long)depth));s.set(s.arena.world().getBlockAt(x,y,z),Material.AIR);budget--;
        }
        if(clearIndex<build.volume())return false;
        if(!generated){
            generated=true;List<Location> all=new ArrayList<>(starts);Collections.shuffle(all);starts=List.copyOf(all.subList(0,s.players().size()));
            int type=s.config.integer("floor-type",0,14);if(type==0)type=new Random().nextInt(1,14);
            Material[] floors={Material.AIR,Material.AIR,Material.SNOW_BLOCK,Material.GRASS_BLOCK,Material.NETHERRACK,Material.SANDSTONE,Material.SLIME_BLOCK,Material.WATER,Material.LAVA,Material.POWDER_SNOW,Material.GLASS,Material.GLASS_PANE,Material.IRON_BARS,Material.IRON_CHAIN,Material.COBWEB};
            int height=s.config.integer("pillar-height",2,128),radius=s.config.integer("floor-radius",1,8);
            for(Location p:starts){
                for(int y=p.getBlockY()-height;y<p.getBlockY();y++)writes.add(new Write(s.arena.world().getBlockAt(p.getBlockX(),y,p.getBlockZ()),Material.CRYING_OBSIDIAN));
                if(type>1)for(int x=p.getBlockX()-radius;x<=p.getBlockX()+radius;x++)for(int z=p.getBlockZ()-radius;z<=p.getBlockZ()+radius;z++)for(int y=p.getBlockY()-height-2;y<=p.getBlockY()-height-1;y++)writes.add(new Write(s.arena.world().getBlockAt(x,y,z),floors[type]));
            }
        }
        while(budget-->0&&!writes.isEmpty()){Write w=writes.removeFirst();s.set(w.block,w.material);}return writes.isEmpty();
    }
    @Override public void start(PartySession s){
        int count=s.players().size();duration=(count>=15?1200:count>=10?900:count>=3?600:300)*20;
        int override=s.config.rules.getInt("duration-seconds",0);if(override>0)duration=override*20;
        nextShrink=s.elapsed()+interval(duration/20)*20;int i=0;
        for(Player p:s.survivors()){p.setGameMode(GameMode.SURVIVAL);p.teleport(starts.get(i++));}
    }
    private int interval(int remaining){return remaining<=30?2:remaining<=60?4:remaining<=120?6:remaining<=300?11:remaining<=600?31:61;}
    @Override public void tick(PartySession s,int elapsed){
        if(s.survivors().size()<=1){awardSurvivors(s,3);return;}
        if(elapsed>=duration){awardSurvivors(s,3);return;}
        if(elapsed%(s.config.integer("item-seconds",1,60)*20)==0)for(Player p:s.survivors())for(ItemStack item:loot.roll("lucky_pillar/demo")){
            for(ItemStack left:p.getInventory().addItem(item).values()){var drop=p.getWorld().dropItemNaturally(p.getLocation(),left);s.track(drop);}
        }
        int maxRing=(Math.min(build.maxX()-build.minX(),build.maxZ()-build.minZ())-2)/2;
        if(elapsed>=nextShrink&&ring<=maxRing){
            int left=build.minX()+ring,right=build.maxX()-ring,near=build.minZ()+ring,far=build.maxZ()-ring;
            for(int y=build.minY();y<=build.maxY();y++){
                for(int x=left;x<=right;x++){s.set(s.arena.world().getBlockAt(x,y,near),Material.BEDROCK);s.set(s.arena.world().getBlockAt(x,y,far),Material.BEDROCK);}
                for(int z=near;z<=far;z++){s.set(s.arena.world().getBlockAt(left,y,z),Material.BEDROCK);s.set(s.arena.world().getBlockAt(right,y,z),Material.BEDROCK);}
            }
            ring++;nextShrink=elapsed+interval((duration-elapsed)/20)*20;s.broadcast("shrink","ring",ring);
        }
        if(elapsed%20==0)for(Player p:s.survivors())p.sendActionBar(s.config.message("status","seconds",Math.max(0,(duration-elapsed)/20),"shrink",Math.max(0,(nextShrink-elapsed)/20)));
    }
    @Override public boolean breakBlock(PartySession s,Player p,Block b){
        if(b.getType()==Material.BEDROCK||b.getType()==Material.BARRIER)return false;
        for(ItemStack item:b.getDrops(p.getInventory().getItemInMainHand(),p)){var drop=b.getWorld().dropItemNaturally(b.getLocation(),item);s.track(drop);}return true;
    }
    @Override public boolean placeBlock(PartySession s,Player p,Block b){return b.getY()<build.maxY()-2;}
    public void explode(PartySession s,org.bukkit.event.entity.EntityExplodeEvent e){Explosives.explode(s,e);}
    @Override public void timeout(PartySession s){awardSurvivors(s,3);}
}
