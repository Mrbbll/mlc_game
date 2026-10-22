package com.mlc.mlcgames.party.deduction.hideseek;

import com.mlc.mlcgames.party.core.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.*;
import org.bukkit.util.Vector;
import java.util.*;

/** The current source uses block displays, despite its legacy animal function names. */
public final class HideSeekGame implements PartyGame {
    private record Mask(BlockDisplay display,Interaction target){}
    private final Set<UUID> hunters=new HashSet<>();
    private final Map<UUID,Mask> masks=new HashMap<>();
    private final List<BlockDisplay> scenery=new ArrayList<>();
    private final Map<UUID,Integer> hitAt=new HashMap<>();
    private final Map<String,Integer> cooldowns=new HashMap<>();
    private Set<Material> allowed;
    private int release,deadline;
    private int fakeCount;
    @Override public void validate(PartyConfig c,PartyConfig.Arena a){allowed=c.materials("disguise-blocks");a.group("hunter-hold");a.group("hunter-spawns");a.group("decoys");c.integer("seconds-per-player",0,90);c.integer("glow-interval-seconds",10,120);c.integer("hide-seconds",1,60);c.integer("decoy-count",0,100);}
    @Override public boolean lastPlayerWins(){return false;}
    @Override public void start(PartySession s){
        List<Player> list=new ArrayList<>(s.survivors());Collections.shuffle(list);
        int count=list.size()>=15?5:list.size()>=12?4:list.size()>=9?3:list.size()>=6?2:1;
        list.stream().limit(count).forEach(p->hunters.add(p.getUniqueId()));
        release=s.config.integer("hide-seconds",1,60)*20;
        deadline=Math.min(600,Math.max(120,90+list.size()*s.config.integer("seconds-per-player",0,90)))*20;
        for(Player p:list){p.getInventory().clear();p.setLevel(0);if(isHunter(p)){p.teleport(s.arena.group("hunter-hold").getFirst());p.sendMessage(s.config.message("hunter-role"));}else{mask(s,p,Material.DIRT);p.sendMessage(s.config.message("hider-role"));}equipment(s,p);}
    }
    private boolean isHunter(Player p){return hunters.contains(p.getUniqueId());}
    private Location random(List<Location> list){return list.get(new Random().nextInt(list.size())).clone();}
    private BlockDisplay display(PartySession s,Location at,Material material){BlockDisplay d=at.getWorld().spawn(at,BlockDisplay.class,e->{e.setBlock(material.createBlockData());e.setInvulnerable(true);e.setPersistent(false);e.setTeleportDuration(1);});s.track(d);return d;}
    private void mask(PartySession s,Player p,Material material){
        removeMask(p);Location at=p.getLocation().clone().subtract(.5,0,.5);
        BlockDisplay display=display(s,at,material);
        Interaction target=at.getWorld().spawn(at.clone().add(.5,0,.5),Interaction.class,e->{e.setInteractionWidth(1);e.setInteractionHeight(1);e.setResponsive(true);e.setPersistent(false);});s.track(target);
        masks.put(p.getUniqueId(),new Mask(display,target));
    }
    private void removeMask(Player p){Mask m=masks.remove(p.getUniqueId());if(m!=null){m.display.remove();m.target.remove();}}
    private void equipment(PartySession s,Player p){
        if(isHunter(p)){
            p.getInventory().setItem(0,s.item(Material.DIAMOND_SWORD,"items.sword"));p.getInventory().setItem(1,s.item(Material.BOW,"items.magic"));
            p.getInventory().setArmorContents(new ItemStack[]{new ItemStack(Material.LEATHER_BOOTS),new ItemStack(Material.LEATHER_LEGGINGS),new ItemStack(Material.LEATHER_CHESTPLATE),new ItemStack(Material.LEATHER_HELMET)});
            ItemStack boots=p.getInventory().getBoots();var meta=boots.getItemMeta();
            Attribute[] attributes={Attribute.MOVEMENT_SPEED,Attribute.JUMP_STRENGTH,Attribute.SAFE_FALL_DISTANCE,Attribute.WATER_MOVEMENT_EFFICIENCY};double[] amounts={.03,.4,5,.5};
            for(int i=0;i<attributes.length;i++)meta.addAttributeModifier(attributes[i],new AttributeModifier(new NamespacedKey("mlcgames_party","hide_boots_"+i),amounts[i],AttributeModifier.Operation.ADD_NUMBER,EquipmentSlotGroup.FEET));boots.setItemMeta(meta);p.getInventory().setBoots(boots);
        }else{
            ItemStack stick=s.item(Material.STICK,"items.stick");stick.addUnsafeEnchantment(Enchantment.KNOCKBACK,3);p.getInventory().setItem(0,stick);
            p.getInventory().setItem(1,s.item(Material.CARROT_ON_A_STICK,"items.disguise"));p.getInventory().setItem(2,s.item(Material.OAK_LEAVES,"items.decoy"));
        }
        p.getInventory().setItem(8,s.item(Material.SNOWBALL,"items.dash"));
    }
    @Override public void tick(PartySession s,int elapsed){
        List<Player> hiders=s.survivors().stream().filter(p->!isHunter(p)).toList();
        if(hiders.isEmpty()){win(s,true);return;}
        if(s.survivors().stream().noneMatch(this::isHunter)||elapsed>=deadline){win(s,false);return;}
        if(elapsed==release){for(Player p:s.survivors())if(isHunter(p))p.teleport(random(s.arena.group("hunter-spawns")));s.broadcast("released");}
        int interval=s.config.integer("glow-interval-seconds",10,120),remaining=(deadline-elapsed+19)/20;
        boolean glow=remaining%interval==0||remaining%interval==interval-1;
        if(elapsed%20==0&&masks.size()+scenery.size()+fakeCount<s.config.integer("decoy-count",0,100))scenery.add(display(s,random(s.arena.group("decoys")),Material.OAK_LEAVES));
        for(Player p:s.survivors()){
            if(isHunter(p)&&elapsed<release&&p.getLocation().distanceSquared(s.arena.group("hunter-hold").getFirst())>1)p.teleport(s.arena.group("hunter-hold").getFirst());
            Mask m=masks.get(p.getUniqueId());if(m!=null){Location at=p.getLocation().clone();if(p.isSneaking())at=new Location(at.getWorld(),at.getBlockX(),at.getBlockY(),at.getBlockZ());else at.subtract(.5,0,.5);m.display.teleport(at);m.target.teleport(at.clone().add(.5,0,.5));m.display.setGlowing(glow||deadline-elapsed<=600);}
            p.setGlowing(glow||!isHunter(p)&&deadline-elapsed<=600);
            if(!isHunter(p)){p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY,40,25,true,false));p.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING,40,25,true,false));}
            if(p.getLocation().subtract(0,.5,0).getBlock().getType()==Material.SLIME_BLOCK){p.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION,40,30,true,false));p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING,200,0,true,false));}
            if(elapsed%20==0){
                if(elapsed%80==0)p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION,40,1,true,false));
                boolean near=s.survivors().stream().anyMatch(other->isHunter(other)!=isHunter(p)&&other.getLocation().distanceSquared(p.getLocation())<=100);
                p.sendActionBar(s.config.message(elapsed<release?"hide-status":near?"nearby-status":"status","seconds",elapsed<release?(release-elapsed+19)/20:(deadline-elapsed+19)/20,"hiders",hiders.size()));}
        }
        if(elapsed%20==0&&remaining%interval==0)for(BlockDisplay d:scenery)if(d.isValid())d.teleport(random(s.arena.group("decoys")));
    }
    private boolean cooldown(PartySession s,Player p,String action,int seconds){String key=p.getUniqueId()+action;if(cooldowns.getOrDefault(key,0)>s.elapsed()){p.sendActionBar(s.config.message("cooldown"));return false;}cooldowns.put(key,s.elapsed()+seconds*20);return true;}
    @Override public void interact(PartySession s,PlayerInteractEvent e){
        if(!e.getAction().isRightClick()||e.getHand()!=org.bukkit.inventory.EquipmentSlot.HAND||e.getItem()==null)return;Player p=e.getPlayer();Material held=e.getItem().getType();e.setCancelled(true);
        if(held==Material.SNOWBALL&&cooldown(s,p,"shoot",20)){
            Snowball snowball=p.launchProjectile(Snowball.class,p.getEyeLocation().getDirection().multiply(1.5));s.track(snowball);snowball.addPassenger(p);
        }else if(!isHunter(p)&&held==Material.CARROT_ON_A_STICK&&cooldown(s,p,"disguise",5)){
            Material floor=p.getLocation().subtract(0,.3,0).getBlock().getType();if(allowed.contains(floor)){mask(s,p,floor);p.sendMessage(s.config.message("disguised","material",floor.name()));}else p.sendMessage(s.config.message("unsupported"));
        }else if(!isHunter(p)&&held==Material.OAK_LEAVES&&cooldown(s,p,"shoot",20)){
            Mask m=masks.get(p.getUniqueId());if(m!=null){Location at=p.getLocation();display(s,new Location(at.getWorld(),at.getBlockX(),at.getBlockY(),at.getBlockZ()),m.display.getBlock().getMaterial());fakeCount++;}
        }else if(isHunter(p)&&held==Material.BOW&&s.elapsed()>=release&&cooldown(s,p,"shoot",20)){
            Location eye=p.getEyeLocation();Vector direction=eye.getDirection();Player target=null;double nearest=Double.MAX_VALUE;
            for(Player other:s.survivors())if(!isHunter(other)){Vector delta=other.getLocation().add(0,.8,0).toVector().subtract(eye.toVector());double along=delta.dot(direction);if(along>=0&&along<=6&&delta.clone().subtract(direction.clone().multiply(along)).lengthSquared()<=1&&along<nearest){target=other;nearest=along;}}
            if(target!=null)damage(s,target,7);eye.getWorld().spawnParticle(Particle.CRIT,eye.clone().add(direction.multiply(3)),15,1,1,1,0);
        }
    }
    private void damage(PartySession s,Player p,double amount){
        if(hitAt.getOrDefault(p.getUniqueId(),-100)+10>s.elapsed())return;hitAt.put(p.getUniqueId(),s.elapsed());
        if(p.getHealth()<=amount){convert(s,p);}else{p.setHealth(p.getHealth()-amount);p.playHurtAnimation(0);p.setNoDamageTicks(10);}
    }
    private void convert(PartySession s,Player p){boolean hider=!isHunter(p);if(hider&&s.players().size()>=10&&s.survivors().stream().filter(other->!isHunter(other)).count()==1)s.points(p,1);removeMask(p);hunters.add(p.getUniqueId());p.getInventory().clear();p.clearActivePotionEffects();p.setHealth(20);p.setFireTicks(0);p.setFallDistance(0);p.leaveVehicle();p.teleport(random(s.arena.group(s.elapsed()<release?"hunter-hold":"hunter-spawns")));equipment(s,p);if(hider)s.broadcast("converted","player",p.getName());}
    @Override public void hit(PartySession s,Player attacker,Player victim,EntityDamageByEntityEvent e){
        e.setCancelled(true);if(s.elapsed()<release||isHunter(attacker)==isHunter(victim))return;
        e.setDamage(isHunter(attacker)&&attacker.getInventory().getItemInMainHand().getType()==Material.DIAMOND_SWORD?8:1);damage(s,victim,e.getFinalDamage());
        if(!isHunter(attacker)&&attacker.getInventory().getItemInMainHand().getType()==Material.STICK)victim.setVelocity(attacker.getLocation().getDirection().setY(.3).multiply(1.5));
    }
    @Override public void attackEntity(PartySession s,Player p,Entity target){if(!isHunter(p)||s.elapsed()<release)return;for(var entry:masks.entrySet())if(entry.getValue().target.equals(target)){Player victim=Bukkit.getPlayer(entry.getKey());if(victim!=null&&victim.getLocation().distanceSquared(p.getLocation())<=16)damage(s,victim,p.getInventory().getItemInMainHand().getType()==Material.DIAMOND_SWORD?8:1);break;}}
    @Override public void interactEntity(PartySession s,Player p,Entity target){}
    @Override public void environmentalDamage(PartySession s,Player p,EntityDamageEvent e){if(e.getCause()!=EntityDamageEvent.DamageCause.FALL)damage(s,p,e.getFinalDamage());}
    @Override public void eliminated(PartySession s,Player p){removeMask(p);}
    @Override public void hazard(PartySession s,Player p){convert(s,p);}
    private void win(PartySession s,boolean hunterWin){List<Player> winners=s.survivors().stream().filter(p->isHunter(p)==hunterWin).toList();for(Player p:winners)s.points(p,s.scores().getOrDefault(p.getUniqueId(),0)+1);s.finish(winners);}
    @Override public void timeout(PartySession s){win(s,false);}
    @Override public boolean launch(PartySession s,Player p){return true;}
    @Override public boolean allowInventoryEdit(PartySession s,Player p){return true;}
    @Override public boolean allowSwap(PartySession s,Player p){return true;}
}
