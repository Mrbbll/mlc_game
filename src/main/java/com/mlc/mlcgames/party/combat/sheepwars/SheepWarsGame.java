package com.mlc.mlcgames.party.combat.sheepwars;

import com.mlc.mlcgames.party.core.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.*;
import org.bukkit.util.Vector;
import java.util.*;

public final class SheepWarsGame implements PartyGame {
    private final Random random=new Random();
    private final Map<UUID,Integer> teams=new HashMap<>();
    private final Map<UUID,Integer> lastAttack=new HashMap<>();
    private final List<Shot> shots=new ArrayList<>();
    private static final NamespacedKey TYPE=new NamespacedKey("mlcgames","sheep-type");
    private enum Kind { SMALL,BIG,RIDER,HEALTH,EARTHQUAKE,BLACKHOLE }
    @Override public boolean lastPlayerWins(){return false;}
    @Override public boolean allowInventory(PartySession s,Player p){return true;}
    @Override public void validate(PartyConfig c,PartyConfig.Arena a){a.region("terrain");for(String team:List.of("a","b"))if(a.teamSpawns().getOrDefault(team,List.of()).size()<(c.integer("players.max",2,64)+1)/2)throw new IllegalArgumentException("队伍出生点不足" );c.integer("limits.maximum-sheep",1,512);}
    @Override public List<Cuboid>writableRegions(PartyConfig.Arena a){return List.of(a.region("terrain"));}
    @Override public void start(PartySession s){List<Player>p=new ArrayList<>(s.players());Collections.shuffle(p);int[] index={0,0};for(int i=0;i<p.size();i++){Player player=p.get(i);player.setGameMode(GameMode.SURVIVAL);int team=i%2;teams.put(player.getUniqueId(),team);player.teleport(s.arena.teamSpawns().get(team==0?"a":"b").get(index[team]++));player.getInventory().setItem(0,s.item(Material.WOODEN_SWORD,"items.sword"));player.getInventory().setItem(1,s.item(Material.BOW,"items.bow"));player.getInventory().setItem(2,new ItemStack(team==0?Material.BLUE_WOOL:Material.RED_WOOL,3));player.getInventory().setItem(35,new ItemStack(Material.ARROW));ItemStack chest=s.item(Material.LEATHER_CHESTPLATE,"items.chest");LeatherArmorMeta meta=(LeatherArmorMeta)chest.getItemMeta();meta.setColor(team==0?Color.BLUE:Color.RED);chest.setItemMeta(meta);chest.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.BLAST_PROTECTION,15);player.getInventory().setChestplate(chest);player.getInventory().setBoots(s.item(Material.LEATHER_BOOTS,"items.boots"));}}
    private ItemStack token(PartySession s,Kind kind){ItemStack item=s.item(Material.PAPER,"items."+kind.name().toLowerCase(Locale.ROOT));var meta=item.getItemMeta();meta.getPersistentDataContainer().set(TYPE,PersistentDataType.STRING,kind.name());item.setItemMeta(meta);return item;}
    private Kind kind(ItemStack item){if(item==null||!item.hasItemMeta())return null;String value=item.getItemMeta().getPersistentDataContainer().get(TYPE,PersistentDataType.STRING);return value==null?null:Kind.valueOf(value);}
    @Override public void tick(PartySession s,int elapsed){if(elapsed%20==0){int seconds=elapsed/20;for(Player p:s.survivors()){if(seconds%5==1){p.getInventory().addItem(new ItemStack(teams.get(p.getUniqueId())==0?Material.BLUE_WOOL:Material.RED_WOOL));p.getInventory().setItem(35,new ItemStack(Material.ARROW));}if(seconds%10==1){int roll=random.nextInt(23);Kind k=roll<=8?Kind.SMALL:roll<=12?Kind.BIG:roll==13?Kind.RIDER:roll<=17?Kind.HEALTH:roll<=20?Kind.EARTHQUAKE:Kind.BLACKHOLE;p.getInventory().addItem(token(s,k));}p.sendActionBar(s.config.message("status","seconds",seconds));}settleIfGone(s);}
        for(Iterator<Shot>it=shots.iterator();it.hasNext();){Shot shot=it.next();if(!shot.sheep.isValid()){it.remove();continue;}Location l=shot.sheep.getLocation();if(!s.arena.contains(l)){shot.sheep.remove();it.remove();continue;}
            if(!shot.triggered){if(shot.gravity){Vector velocity=shot.sheep.getVelocity();if(velocity.lengthSquared()<.03)trigger(s,shot);}else{for(int i=0;i<6&&!shot.triggered;i++){Location front=shot.sheep.getLocation().add(shot.direction);boolean collision=front.getBlock().getType().isSolid()||s.survivors().stream().anyMatch(p->!p.getUniqueId().equals(shot.owner)&&p.getLocation().distanceSquared(front)<1);if(collision)trigger(s,shot);else shot.sheep.teleport(shot.sheep.getLocation().add(shot.direction.clone().multiply(.25)));}}}
            else {int age=elapsed-shot.triggerTick;if(shot.kind==Kind.HEALTH&&age%40==0)for(Player p:s.survivors())if(p.getLocation().distanceSquared(l)<=25)p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION,40,3,true,false));
                if(shot.kind==Kind.EARTHQUAKE){shot.sheep.setRotation(shot.sheep.getYaw()+10,0);Vector direction=shot.sheep.getLocation().getDirection().setY(0).multiply(3);blast(s,l.clone().add(direction),1.5f);}
                if(shot.kind==Kind.BLACKHOLE)for(Player p:s.survivors())if(p.getLocation().distanceSquared(l)<=64){Location look=p.getLocation();Vector direction=l.clone().subtract(0,.5,0).toVector().subtract(look.toVector());look.setDirection(direction);p.teleport(look);p.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS,40,25,true,false));}
                int duration=shot.kind==Kind.BIG?60:shot.kind==Kind.HEALTH?400:200;if(age>=duration){if(shot.kind==Kind.BIG){float power=shot.gravity?8:6;blast(s,l,power);blast(s,l.clone().add(shot.direction),power);blast(s,l.clone().subtract(shot.direction),power);}else if(shot.kind==Kind.EARTHQUAKE)blast(s,l,4);else if(shot.kind==Kind.BLACKHOLE)blast(s,l,2);shot.sheep.remove();it.remove();}
            }
        }
    }
    private void trigger(PartySession s,Shot shot){shot.triggered=true;shot.triggerTick=s.elapsed();shot.sheep.setGravity(false);shot.sheep.setVelocity(new Vector());shot.sheep.setGlowing(true);if(shot.kind==Kind.SMALL){blast(s,shot.sheep.getLocation(),shot.gravity?4:3);shot.sheep.remove();}else if(shot.kind==Kind.RIDER){shot.sheep.eject();shot.sheep.remove();}}
    private void fire(PartySession s,Player p,ItemStack item,boolean gravity){Kind kind=kind(item);if(kind==null)return;if(shots.size()>=s.config.integer("limits.maximum-sheep",1,512))return;item.subtract();Location l=p.getEyeLocation();Vector direction=l.getDirection();Sheep sheep=s.arena.world().spawn(l,Sheep.class,e->{e.setAI(false);e.setSilent(true);e.setGravity(gravity);e.setPersistent(false);e.setColor(switch(kind){case SMALL->DyeColor.RED;case BIG->DyeColor.ORANGE;case HEALTH->DyeColor.PINK;case EARTHQUAKE->DyeColor.BROWN;case BLACKHOLE->DyeColor.BLACK;case RIDER->DyeColor.WHITE;});e.customName(s.config.message("items."+kind.name().toLowerCase(Locale.ROOT)));e.setCustomNameVisible(true);});s.track(sheep);shots.add(new Shot(sheep,p.getUniqueId(),kind,gravity,direction));if(gravity)sheep.setVelocity(direction.clone().multiply(5));if(kind==Kind.RIDER){p.leaveVehicle();sheep.addPassenger(p);}}
    @Override public void interact(PartySession s,PlayerInteractEvent e){if(e.getAction().isRightClick()&&kind(e.getItem())!=null){e.setCancelled(true);fire(s,e.getPlayer(),e.getItem(),true);}}
    @Override public void swap(PartySession s,PlayerSwapHandItemsEvent e){e.setCancelled(true);fire(s,e.getPlayer(),e.getMainHandItem(),false);}
    @Override public boolean breakBlock(PartySession s,Player p,Block b){return s.writable(b);}
    @Override public boolean placeBlock(PartySession s,Player p,Block b){return s.writable(b)&&(b.getType()==Material.BLUE_WOOL||b.getType()==Material.RED_WOOL);}
    @Override public void hit(PartySession s,Player a,Player v,EntityDamageByEntityEvent e){if(!teams.get(a.getUniqueId()).equals(teams.get(v.getUniqueId())))e.setCancelled(false);}
    @Override public void environmentalDamage(PartySession s,Player p,EntityDamageEvent e){if(e.getCause()!=EntityDamageEvent.DamageCause.FALL)e.setCancelled(false);}
    @Override public void attackEntity(PartySession s,Player p,Entity target){if(target instanceof Sheep sheep&&s.owns(sheep)&&p.getEyeLocation().distanceSquared(target.getLocation())<=20.25&&p.hasLineOfSight(target)&&s.elapsed()-lastAttack.getOrDefault(p.getUniqueId(),-20)>=10){lastAttack.put(p.getUniqueId(),s.elapsed());if(sheep.getHealth()<=4)sheep.remove();else sheep.setHealth(sheep.getHealth()-4);}}
    private void blast(PartySession s,Location l,float power){
        Set<Block> destroyed=new HashSet<>();
        // Vanilla's 16-cube surface ray sampling and resistance attenuation, journalled instead of native destruction.
        for(int ix=0;ix<16;ix++)for(int iy=0;iy<16;iy++)for(int iz=0;iz<16;iz++)if(ix==0||ix==15||iy==0||iy==15||iz==0||iz==15){Vector step=new Vector(ix/15d*2-1,iy/15d*2-1,iz/15d*2-1).normalize().multiply(.3);double energy=power*(.7+random.nextFloat()*.6);Location point=l.clone();for(int n=0;energy>0&&n<200;n++){Block b=point.getBlock();if(!b.getType().isAir())energy-=(b.getType().getBlastResistance()+.3)*.3;if(energy>0&&s.writable(b))destroyed.add(b);energy-=.225;point.add(step);}}
        for(Player p:List.copyOf(s.survivors())){double distance=p.getLocation().distance(l)/(power*2);if(distance>1)continue;double impact=(1-distance)*exposure(s,l,p);double damage=(impact*impact+impact)*.5*7*power*2+1;double armour=Math.max(.6,Math.min(20,3-damage/2));damage*=(1-armour/25)*.2; // Leather armour and vanilla's capped blast-protection reduction.
            if(p.getHealth()<=damage)s.eliminate(p);else p.setHealth(p.getHealth()-damage);Vector v=p.getLocation().toVector().subtract(l.toVector());if(v.lengthSquared()<.01)v.setY(1);p.setVelocity(v.normalize().multiply(impact*(teams.getOrDefault(p.getUniqueId(),0)==0?11:5.5)));}
        for(Block b:destroyed)s.set(b,Material.AIR);
        s.arena.world().spawnParticle(Particle.EXPLOSION,l,1);s.arena.world().playSound(l,Sound.ENTITY_GENERIC_EXPLODE,1,1);
    }
    private double exposure(PartySession s,Location source,Player p){org.bukkit.util.BoundingBox box=p.getBoundingBox();double sx=1/(box.getWidthX()*2+1),sy=1/(box.getHeight()*2+1),sz=1/(box.getWidthZ()*2+1);int clear=0,total=0;double ox=(1-Math.floor(1/sx)*sx)/2,oz=(1-Math.floor(1/sz)*sz)/2;for(double x=0;x<=1;x+=sx)for(double y=0;y<=1;y+=sy)for(double z=0;z<=1;z+=sz){Location sample=new Location(s.arena.world(),box.getMinX()+x*box.getWidthX()+ox,box.getMinY()+y*box.getHeight(),box.getMinZ()+z*box.getWidthZ()+oz);Vector vector=source.toVector().subtract(sample.toVector());double distance=vector.length();if(distance<.0001||s.arena.world().rayTraceBlocks(sample,vector.normalize(),distance,FluidCollisionMode.NEVER,true)==null)clear++;total++;}return total==0?0:(double)clear/total;}
    private void settleIfGone(PartySession s){long a=s.survivors().stream().filter(p->teams.get(p.getUniqueId())==0).count(),b=s.survivors().size()-a;if(a==0||b==0){int winner=a==b?-1:a>b?0:1;List<Player>w=s.players().stream().filter(p->teams.get(p.getUniqueId())==winner).toList();for(Player p:w)s.points(p,2);s.finish(w);}}
    @Override public void timeout(PartySession s){s.finish(List.of());}
    private static class Shot{final Sheep sheep;final UUID owner;final Kind kind;final boolean gravity;final Vector direction;boolean triggered;int triggerTick;Shot(Sheep s,UUID o,Kind k,boolean g,Vector d){sheep=s;owner=o;kind=k;gravity=g;direction=d.clone();}}
}
