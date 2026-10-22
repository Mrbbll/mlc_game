package com.mlc.mlcgames.party.racing.randomparkour;

import com.mlc.mlcgames.party.core.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Ladder;
import org.bukkit.entity.*;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.*;
import org.bukkit.util.Vector;
import java.util.*;

/** The original bounded, direction-reversing walk, with checkpoint platforms and incremental generation. */
public final class RandomParkourGame implements PartyGame {
    private final Random r=new Random();
    private final Map<UUID,Location> checkpoints=new HashMap<>();
    private final List<UUID> finishers=new ArrayList<>();
    private final List<EnderPearl> pearls=new ArrayList<>();
    private Cuboid region;
    private int clear,x,y,z,steps,checkpointStep,deadline;
    private boolean backX,backZ;
    private static final Material[] WOOL=Arrays.stream(DyeColor.values()).map(c->Material.valueOf(c.name()+"_WOOL")).toArray(Material[]::new);
    @Override public boolean lastPlayerWins(){return false;}
    @Override public boolean allowInventory(PartySession s,Player p){return true;}
    @Override public void launched(PartySession s,Player p,Projectile projectile){if(projectile instanceof EnderPearl pearl)pearls.add(pearl);}
    @Override public void validate(PartyConfig c,PartyConfig.Arena a){region=a.region("generator");a.group("generator-start");a.group("retry-start");if(region.maxX()-region.minX()<20||region.maxZ()-region.minZ()<12||region.maxY()-region.minY()<20)throw new IllegalArgumentException("生成空间至少21×21×13");if(!region.contains(a.group("generator-start").get(0).getBlockX(),a.group("generator-start").get(0).getBlockY(),a.group("generator-start").get(0).getBlockZ()))throw new IllegalArgumentException("生成起点必须位于generator内");c.integer("generation.minimum-steps",10,1000);c.integer("generation.maximum-steps",c.integer("generation.minimum-steps",10,1000),1000);}
    @Override public List<Cuboid>writableRegions(PartyConfig.Arena a){return List.of(a.region("generator"));}
    @Override public boolean prepare(PartySession s,int budget){for(int i=0;i<budget&&clear<region.volume();i++,clear++){int w=region.maxX()-region.minX()+1,d=region.maxZ()-region.minZ()+1;s.set(s.arena.world().getBlockAt(region.minX()+clear%w,region.minY()+clear/(w*d),region.minZ()+clear/w%d),Material.AIR);}return clear>=region.volume();}
    @Override public void start(PartySession s){Location start=s.arena.group("generator-start").get(0);x=start.getBlockX();y=start.getBlockY();z=start.getBlockZ();int min=s.config.integer("generation.minimum-steps",10,1000),max=s.config.integer("generation.maximum-steps",min,1000);steps=min+r.nextInt(max-min+1);checkpointStep=steps-(40+r.nextInt(21));deadline=s.elapsed()+20*(s.players().size()>=30?1200:s.players().size()>=20?900:600);platform(s,start,Material.EMERALD_BLOCK,true);for(Player p:s.survivors())boots(s,p);}
    private int random(int min,int max){return min+r.nextInt(max-min+1);}
    private void generate(PartySession s){
        if(steps<=0)return;
        steps--;
        int dx=0,dy=0,dz=0;
        int way=0;if(x>=region.maxX()-4)way=1;if(x>=region.maxX()-5)backX=true;if(x<=region.minX()+6)way=2;if(x<=region.minX()+7)backX=false;
        dx=way==1?random(-2,0):way==2?random(0,2):backX?random(-2,0):random(0,2);
        way=0;if(z>=region.maxZ()-5){way=1;backZ=true;}if(z<=region.minZ()+5){way=2;backZ=false;}
        if(!backZ)dz=way==1?random(-3,dx==0?-1:0):random(dx==0?1:0,dx==0?4:3);
        else dz=way==2?random(dx==0?1:0,2):random(-4,dx==0?-1:0);
        if(y<region.maxY()-19){dy=Math.max(0,random(y<=region.minY()+1?-10:-12,2));if(Math.abs(dx)>=4||Math.abs(dz)>=4||dx==0&&dz==0)dy=0;if(x>=region.maxX()-4&&!backX||x<=region.minX()+9&&backX)dy=random(0,2);}
        x=Math.max(region.minX()+2,Math.min(region.maxX()-2,x+dx));z=Math.max(region.minZ()+2,Math.min(region.maxZ()-2,z+dz));y=Math.min(region.maxY()-2,y+dy);
        Location l=new Location(s.arena.world(),x,y,z);
        if(steps<=0){platform(s,l,Material.LAPIS_BLOCK,false);return;}
        if(steps<=checkpointStep&&x>=region.minX()+3&&x<=region.maxX()-4&&z>=region.minZ()+3&&z<=region.maxZ()-3){platform(s,l,Material.LIME_WOOL,true);checkpointStep=steps-random(40,60);}
        else{s.set(l.getBlock(),WOOL[r.nextInt(WOOL.length)]);ladders(s,l,1,0);}
    }
    private void platform(PartySession s,Location l,Material material,boolean checkpoint){for(int dx=-1;dx<=1;dx++)for(int dz=-1;dz<=1;dz++){Block b=l.clone().add(dx,0,dz).getBlock();if(s.writable(b))s.set(b,material);}if(checkpoint)s.set(l.getBlock(),Material.GOLD_BLOCK);ladders(s,l,2,1);}
    private void ladders(PartySession s,Location l,int distance,int radius){for(int v=-radius;v<=radius;v++){ladder(s,l.clone().add(-distance,0,v).getBlock(),BlockFace.WEST);ladder(s,l.clone().add(distance,0,v).getBlock(),BlockFace.EAST);ladder(s,l.clone().add(v,0,-distance).getBlock(),BlockFace.NORTH);ladder(s,l.clone().add(v,0,distance).getBlock(),BlockFace.SOUTH);}}
    private void ladder(PartySession s,Block b,BlockFace facing){if(s.writable(b)&&b.getType().isAir()){Ladder data=(Ladder)Material.LADDER.createBlockData();data.setFacing(facing);s.setData(b,data);}}
    @Override public void tick(PartySession s,int elapsed){if(elapsed%5==0)generate(s);for(Iterator<EnderPearl>it=pearls.iterator();it.hasNext();){EnderPearl pearl=it.next();if(!pearl.isValid()){it.remove();continue;}if(pearl.getLocation().getBlock().getType()==Material.WATER){pearl.remove();it.remove();}}
        for(Player p:List.copyOf(s.survivors())){Location l=p.getLocation();Material below=l.clone().subtract(0,.5,0).getBlock().getType();if(below==Material.LAPIS_BLOCK){finish(s,p);continue;}if(below==Material.MAGMA_BLOCK||l.getY()<region.minY()-1){hazard(s,p);continue;}if(l.clone().subtract(0,1,0).getBlock().getType()==Material.GOLD_BLOCK){Location checkpoint=l.getBlock().getLocation().add(.5,0,.5);if(!checkpoints.containsKey(p.getUniqueId())||checkpoints.get(p.getUniqueId()).distanceSquared(checkpoint)>.1){checkpoints.put(p.getUniqueId(),checkpoint);p.sendMessage(s.config.message("checkpoint"));}}if(elapsed%20==0){p.sendActionBar(s.config.message("status","seconds",Math.max(0,(deadline-elapsed+19)/20),"steps",steps));if((deadline-elapsed)/20%10==0)loot(s,p);}}
        if(elapsed>=deadline||s.survivors().isEmpty())timeout(s);
    }
    private void finish(PartySession s,Player p){finishers.add(p.getUniqueId());int place=finishers.size();s.points(p,place==1?4:place==2?3:place==3?2:1);s.broadcast("finisher","player",p.getName(),"place",place);s.retire(p);if(place>=4)deadline=Math.min(deadline,s.elapsed()+600);}
    private void boots(PartySession s,Player p){p.getInventory().setBoots(s.item(Material.LEATHER_BOOTS,"items.boots"));}
    @Override public void hazard(PartySession s,Player p){p.teleport(checkpoints.getOrDefault(p.getUniqueId(),s.arena.group("retry-start").get(0)));p.setFallDistance(0);p.setVelocity(new Vector());p.setFireTicks(0);p.getInventory().clear();boots(s,p);p.sendMessage(s.config.message("retry"));}
    private void loot(PartySession s,Player p){int roll=r.nextInt(49);Material material=roll<1?Material.ENDER_PEARL:roll<6?Material.FISHING_ROD:roll<26?Material.SNOWBALL:roll<41?Material.POTION:Material.SPLASH_POTION;ItemStack item=s.item(material,"items.loot");if(material==Material.POTION||material==Material.SPLASH_POTION){PotionMeta meta=(PotionMeta)item.getItemMeta();PotionEffectType effect=roll<32?PotionEffectType.JUMP_BOOST:roll<38?PotionEffectType.SPEED:roll<41?PotionEffectType.LEVITATION:PotionEffectType.SLOWNESS;meta.addCustomEffect(new PotionEffect(effect,material==Material.POTION?200:100,effect==PotionEffectType.LEVITATION?0:1),true);item.setItemMeta(meta);}else {var meta=item.getItemMeta();var cooldown=meta.getUseCooldown();cooldown.setCooldownGroup(new NamespacedKey("mlcgames","random-parkour"));cooldown.setCooldownSeconds(material==Material.FISHING_ROD?.5f:5);meta.setUseCooldown(cooldown);item.setItemMeta(meta);}p.getInventory().addItem(item);}
    @Override public boolean launch(PartySession s,Player p){return true;}
    @Override public void projectile(PartySession s,ProjectileHitEvent e){if(e.getEntity() instanceof EnderPearl pearl){pearls.add(pearl);if(pearl.getLocation().getBlock().getType()==Material.WATER)e.setCancelled(true);}if(e.getEntity() instanceof Snowball){Location hit=e.getEntity().getLocation();for(Player p:s.survivors())if(p.getLocation().distanceSquared(hit)<16){Vector v=p.getLocation().toVector().subtract(hit.toVector());if(v.lengthSquared()<.01)v.setX(.1);p.setVelocity(v.normalize().multiply(1.2).setY(.5));}}}
    @Override public void fish(PartySession s,PlayerFishEvent e){if(e.getState()==PlayerFishEvent.State.CAUGHT_ENTITY&&e.getCaught() instanceof Player target&&s.active(target)){target.setVelocity(e.getPlayer().getLocation().toVector().subtract(target.getLocation().toVector()).normalize().multiply(.8));e.getPlayer().getInventory().setItemInMainHand(null);}e.setCancelled(false);}
    @Override public boolean retainFishingHook(){return true;}
    @Override public void timeout(PartySession s){List<Player>winners=finishers.isEmpty()?List.of():s.players().stream().filter(p->p.getUniqueId().equals(finishers.get(0))).toList();s.finish(winners);}
}
