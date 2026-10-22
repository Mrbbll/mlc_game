package com.mlc.mlcgames.party.survival.disastersnow;

import com.mlc.mlcgames.party.core.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.*;
import java.util.*;

/** Falling hazards use local trajectories so every block mutation goes through the recovery journal. */
public final class DisasterSnowGame implements PartyGame {
    private final Random random=new Random();
    private final List<Drop> drops=new ArrayList<>();
    private Cuboid field;
    private int prepared,shrink;
    private final SnowTimeline timeline=new SnowTimeline(15,6,10,30,30,5,20,5);
    @Override public void validate(PartyConfig c,PartyConfig.Arena a){field=a.region("snow-volume");if(field.maxY()-field.minY()<8||field.maxX()-field.minX()!=38||field.maxZ()-field.minZ()!=38)throw new IllegalArgumentException("snow-volume 横向须39×39且高度至少9格，以保留20步缩圈");if(field.volume()>c.integer("limits.max-snapshot-blocks",1,100000))throw new IllegalArgumentException("雪场超过恢复快照限制");c.integer("initial-layers",1,6);}
    @Override public List<Cuboid>writableRegions(PartyConfig.Arena a){return List.of(a.region("snow-volume"));}
    @Override public boolean prepare(PartySession s,int budget){long volume=field.volume();for(int i=0;i<budget&&prepared<volume;i++,prepared++){int w=field.maxX()-field.minX()+1,d=field.maxZ()-field.minZ()+1;int x=field.minX()+prepared%w,z=field.minZ()+prepared/w%d,y=field.minY()+prepared/(w*d);s.set(s.arena.world().getBlockAt(x,y,z),y<field.minY()+s.config.integer("initial-layers",1,6)?Material.SNOW_BLOCK:Material.AIR);}return prepared>=volume;}
    @Override public void start(PartySession s){for(Player p:s.survivors()){p.setGameMode(GameMode.SURVIVAL);give(s,p);}}
    @Override public boolean allowInventory(PartySession s,Player p){return true;}
    @Override public void tick(PartySession s,int elapsed){
        int second=elapsed/20,speed=timeline.speed(second);
        if(second<timeline.stopSnowAt()&&elapsed%5==0)for(int i=0;i<speed;i++){spawn(s,false);spawn(s,true);}
        if(elapsed%20==0){
            if(second>=21&&second<=69&&(second-21)%6==0||second==99||second>=159&&(second-159)%30==0)for(Player p:s.survivors())give(s,p);
            for(Player p:s.survivors()){p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION,40,0,true,false));p.sendActionBar(s.config.message("status","speed",speed,"pvp",second>=99?s.config.text("on"):s.config.text("off"),"shrink",shrink));
                if(p.getInventory().getItemInMainHand().getType()==Material.BOW)p.getInventory().setItemInOffHand(new ItemStack(Material.ARROW));else if(p.getInventory().getItemInOffHand().getType()==Material.ARROW)p.getInventory().setItemInOffHand(null);
            }
            if(timeline.shrinkAt(second))shrink(s);
        }
        for(Iterator<Drop>it=drops.iterator();it.hasNext();){Drop d=it.next();if(!d.visual.isValid()){it.remove();continue;}d.velocity-=.04;d.y+=d.velocity;Location l=d.visual.getLocation();l.setY(d.y);d.visual.teleport(l);Block floor=l.clone().subtract(0,.5,0).getBlock();if(d.y<field.minY()-5||!s.arena.contains(l)){d.visual.remove();it.remove();continue;}
            if(floor.getType()==Material.SNOW_BLOCK){impact(s,l,d.kind);if(d.kind!=4&&d.kind!=5){d.visual.remove();it.remove();}}
        }
        if(second>=timeline.finishAt()){for(Player p:s.survivors())s.points(p,3);s.finish(s.survivors());}
    }
    private void spawn(PartySession s,boolean hazard){int cx=(field.minX()+field.maxX())/2,cz=(field.minZ()+field.maxZ())/2;int radius=Math.max(1,Math.min(field.maxX()-field.minX(),field.maxZ()-field.minZ())/2-1-shrink);int x=cx+random.nextInt(radius*2+1)-radius,z=cz+random.nextInt(radius*2+1)-radius;Location l=new Location(s.arena.world(),x+.5,field.maxY(),z+.5);int roll=random.nextInt(200)+1;int kind=!hazard?0:roll<=160?1:roll<=175?2:roll==176?3:roll<=180?4:roll==181?5:roll<=194?6:7;
        BlockDisplay v=s.arena.world().spawn(l,BlockDisplay.class,e->{e.setBlock((kind==0||kind>=6?Material.SNOW_BLOCK:kind==1?Material.REDSTONE_BLOCK:kind==4?Material.PISTON:kind==5?Material.TRIDENT.isBlock()?Material.TRIDENT:Material.NETHERITE_BLOCK:Material.TNT).createBlockData());e.setDisplayWidth(1);e.setDisplayHeight(1);e.setPersistent(false);});s.track(v);drops.add(new Drop(v,l.getY(),kind));
        // Original roll 176 creates both a large blast and a piercing hazard.
        if(hazard&&roll==176){BlockDisplay extra=s.arena.world().spawn(l,BlockDisplay.class,e->{e.setBlock(Material.PISTON.createBlockData());e.setPersistent(false);});s.track(extra);drops.add(new Drop(extra,l.getY(),4));}
    }
    private void impact(PartySession s,Location l,int kind){int r=kind==2||kind==3||kind==5||kind==6?1:0;int lo=kind==3?-2:kind==2||kind==5?-1:kind==0?0:kind==6||kind==7?0:-1,hi=kind==7?3:kind==2||kind==3||kind==5?1:kind==6||kind==0?0:-1;
        if(kind==0){Block b=l.getBlock();if(s.writable(b)&&b.getType().isAir())s.set(b,Material.SNOW_BLOCK);for(Player p:s.survivors())if(p.getLocation().distanceSquared(l)<1.5)damage(s,p,2);return;}
        for(int x=-r;x<=r;x++)for(int z=-r;z<=r;z++)for(int y=lo;y<=hi;y++){Block b=l.clone().add(x,y,z).getBlock();if(!s.writable(b))continue;if(kind>=6){if(b.getType().isAir())s.set(b,Material.SNOW_BLOCK);}else if(b.getType()==Material.SNOW_BLOCK)s.set(b,Material.AIR);}
        s.arena.world().spawnParticle(Particle.SNOWFLAKE,l,15,.5,.5,.5,.02);
    }
    private void damage(PartySession s,Player p,double n){if(p.getHealth()<=n)s.eliminate(p);else p.setHealth(p.getHealth()-n);}
    private void shrink(PartySession s){int step=shrink++;int minX=field.minX()+step,maxX=field.maxX()-step,minZ=field.minZ()+step,maxZ=field.maxZ()-step;if(minX>maxX||minZ>maxZ)return;for(int y=field.minY();y<=field.maxY();y++){for(int x=minX;x<=maxX;x++){s.set(s.arena.world().getBlockAt(x,y,minZ),Material.AIR);s.set(s.arena.world().getBlockAt(x,y,maxZ),Material.AIR);}for(int z=minZ+1;z<maxZ;z++){s.set(s.arena.world().getBlockAt(minX,y,z),Material.AIR);s.set(s.arena.world().getBlockAt(maxX,y,z),Material.AIR);}}}
    private void give(PartySession s,Player p){int roll=random.nextInt(21)+1;Material type=roll<=6?Material.WOODEN_SHOVEL:roll<=12?Material.SNOW_BLOCK:roll<=14?Material.STONE_SHOVEL:roll==15?Material.BOW:roll<=17?Material.SNOWBALL:roll==18?Material.FISHING_ROD:roll==19?Material.STICK:roll==20?Material.ENDER_PEARL:Material.GOLDEN_SHOVEL;ItemStack item=s.item(type,"items.tool");if(type==Material.SNOWBALL)item.setAmount(2);if(type==Material.STICK)item.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.KNOCKBACK,2);p.getInventory().addItem(item);}
    @Override public boolean breakBlock(PartySession s,Player p,Block b){ItemStack item=p.getInventory().getItemInMainHand();Material held=item.getType();if(b.getType()!=Material.SNOW_BLOCK||!s.writable(b)||!Set.of(Material.WOODEN_SHOVEL,Material.STONE_SHOVEL,Material.GOLDEN_SHOVEL).contains(held))return false;org.bukkit.inventory.meta.Damageable meta=(org.bukkit.inventory.meta.Damageable)item.getItemMeta();int uses=meta.getDamage()+1,max=held==Material.WOODEN_SHOVEL?1:held==Material.STONE_SHOVEL?3:6;if(uses>=max)p.getInventory().setItemInMainHand(null);else {meta.setDamage(uses);item.setItemMeta(meta);}return true;}
    @Override public boolean placeBlock(PartySession s,Player p,Block b){return b.getType()==Material.SNOW_BLOCK&&s.writable(b)&&b.getRelative(0,-1,0).getType()==Material.SNOW_BLOCK;}
    @Override public boolean instantBreak(PartySession s,Player p,Block b){return false;}
    @Override public void hit(PartySession s,Player a,Player v,EntityDamageByEntityEvent e){if(s.elapsed()>=99*20){e.setCancelled(false);if(a.getInventory().getItemInMainHand().getType()==Material.STICK)a.getInventory().setItemInMainHand(null);}}
    @Override public boolean launch(PartySession s,Player p){if(p.getInventory().getItemInMainHand().getType()==Material.BOW)p.getInventory().setItemInMainHand(null);return true;}
    @Override public void fish(PartySession s,org.bukkit.event.player.PlayerFishEvent e){if(e.getState()==org.bukkit.event.player.PlayerFishEvent.State.CAUGHT_ENTITY&&e.getCaught() instanceof Player p&&s.active(p)){org.bukkit.util.Vector v=e.getPlayer().getLocation().toVector().subtract(p.getLocation().toVector());if(v.lengthSquared()>.001)p.setVelocity(v.normalize().multiply(.8));e.getPlayer().getInventory().setItemInMainHand(null);}e.setCancelled(false);}
    @Override public boolean retainFishingHook(){return true;}
    @Override public void environmentalDamage(PartySession s,Player p,EntityDamageEvent e){e.setCancelled(false);}
    @Override public void projectile(PartySession s,ProjectileHitEvent e){Location l=e.getEntity().getLocation();if(e.getEntity() instanceof Arrow||e.getEntity() instanceof Snowball)for(int x=-1;x<=1;x++)for(int y=-1;y<=1;y++)for(int z=-1;z<=1;z++){Block b=l.clone().add(x,y,z).getBlock();if(s.writable(b)&&b.getType()==Material.SNOW_BLOCK)s.set(b,Material.AIR);}}
    @Override public void eliminated(PartySession s,Player p){int left=s.survivors().size();if(left<=2)s.points(p,left==0?3:left==1?2:1);if(left==1)s.points(s.survivors().get(0),3);}
    @Override public void timeout(PartySession s){for(Player p:s.survivors())s.points(p,3);s.finish(s.survivors());}
    private static class Drop{final BlockDisplay visual;final int kind;double y,velocity;Drop(BlockDisplay v,double y,int k){visual=v;this.y=y;kind=k;}}
}
