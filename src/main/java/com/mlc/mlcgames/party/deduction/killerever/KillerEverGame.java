package com.mlc.mlcgames.party.deduction.killerever;

import com.mlc.mlcgames.party.core.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.*;
import java.util.*;

public final class KillerEverGame implements PartyGame {
    private final Set<UUID> killers=new HashSet<>(),detectives=new HashSet<>();
    private final Map<UUID,Integer> shots=new HashMap<>();
    private final Map<Interaction,ItemDisplay> bows=new HashMap<>();
    private final Map<Block,Integer> cauldrons=new HashMap<>();
    private final Map<UUID,Location> deaths=new HashMap<>();
    private int weaponsAt,finalAt,endAt;
    private boolean armed,finale;
    @Override public void validate(PartyConfig c,PartyConfig.Arena a){a.group("gold-spawns");a.group("potion-shops");a.group("wind-shops");Cuboid shops=a.region("shops");for(Location at:a.group("potion-shops"))if(!shops.contains(at.getBlockX(),at.getBlockY(),at.getBlockZ()))throw new IllegalArgumentException("炼药点须在shops恢复区域内");c.integer("weapon-delay-seconds",1,60);int duration=c.integer("normal-seconds",0,600);if(duration>0&&duration<10)throw new IllegalArgumentException("normal-seconds为0自动，或10..600");c.integer("final-seconds",10,120);}
    @Override public List<Cuboid> writableRegions(PartyConfig.Arena a){return List.of(a.region("shops"));}
    @Override public boolean lastPlayerWins(){return false;}
    @Override public void start(PartySession s){
        List<Player> list=new ArrayList<>(s.survivors());Collections.shuffle(list);int count=list.size()>=40?4:list.size()>=28?3:list.size()>=16?2:1;
        for(int i=0;i<count;i++){detectives.add(list.get(i*2).getUniqueId());killers.add(list.get(i*2+1).getUniqueId());}
        int duration=s.config.integer("normal-seconds",0,600);if(duration==0)duration=list.size()>=21?360:300;weaponsAt=s.config.integer("weapon-delay-seconds",1,60)*20;finalAt=weaponsAt+duration*20;endAt=finalAt+s.config.integer("final-seconds",10,120)*20;
        for(Player p:list)p.sendMessage(s.config.message(killers.contains(p.getUniqueId())?"killer-role":detectives.contains(p.getUniqueId())?"detective-role":"normal-role"));
        String names=String.join(", ",list.stream().filter(p->killers.contains(p.getUniqueId())).map(Player::getName).toList());for(Player p:list)if(killers.contains(p.getUniqueId()))p.sendMessage(s.config.message("killer-allies","players",names));
    }
    @Override public void tick(PartySession s,int elapsed){
        List<Player> live=s.survivors();
        if(live.stream().noneMatch(p->killers.contains(p.getUniqueId()))){finish(s,false);return;}
        if(live.stream().noneMatch(p->!killers.contains(p.getUniqueId()))){finish(s,true);return;}
        if(elapsed>=endAt){s.finish(List.of());return;}
        if(!armed&&elapsed>=weaponsAt){armed=true;s.broadcast("armed");for(Player p:live)if(killers.contains(p.getUniqueId()))p.getInventory().setItem(0,s.item(Material.CARROT_ON_A_STICK,"items.knife"));else if(detectives.contains(p.getUniqueId()))detectiveItems(s,p);}
        if(!finale&&elapsed>=finalAt){finale=true;s.broadcast("finale");}
        if(finale)for(Player p:live)p.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING,40,0,true,false));
        if(elapsed%40==0){List<Location> spawns=s.arena.group("gold-spawns");Location at=spawns.get(new Random().nextInt(spawns.size()));Item drop=s.arena.world().dropItem(at,new ItemStack(Material.GOLD_INGOT));drop.setGlowing(true);drop.setTicksLived(4800);s.track(drop);}
        if(elapsed%20!=0)return;
        for(Player p:live){
            if(elapsed%80==0)p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION,40,1,true,false));
            if(armed&&detectives.contains(p.getUniqueId())&&!p.getInventory().contains(Material.ARROW))p.getInventory().addItem(new ItemStack(Material.ARROW));
            if(!detectives.contains(p.getUniqueId())&&currency(p)>=10){spend(p,10);if(!p.getInventory().contains(Material.BOW)){ItemStack bow=s.item(Material.BOW,"items.bow");bow.addUnsafeEnchantment(Enchantment.POWER,10);p.getInventory().addItem(bow);}p.getInventory().addItem(new ItemStack(Material.ARROW));}
            if(p.getLocation().subtract(0,2,0).getBlock().getType()==Material.LOOM)p.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION,20,15,true,false));
            p.sendActionBar(s.config.message(elapsed<weaponsAt?"wait-status":"status","seconds",elapsed<weaponsAt?(weaponsAt-elapsed+19)/20:(endAt-elapsed+19)/20,"remaining",live.size()));
        }
        for(var entry:List.copyOf(cauldrons.entrySet())){
            int stage=(elapsed-entry.getValue())/20;Block b=entry.getKey();
            if(stage>=6){s.set(b,Material.CAULDRON);cauldrons.remove(b);}else{s.setData(b,Bukkit.createBlockData((stage<3?"minecraft:water_cauldron[level="+(stage+1):"minecraft:powder_snow_cauldron[level="+(stage-2))+"]"));}
        }
    }
    private int currency(Player p){int n=0;for(ItemStack i:p.getInventory().getContents())if(i!=null&&i.getType()==Material.GOLD_INGOT)n+=i.getAmount();return n;}
    private void spend(Player p,int amount){for(int slot=0;slot<p.getInventory().getSize()&&amount>0;slot++){ItemStack item=p.getInventory().getItem(slot);if(item==null||item.getType()!=Material.GOLD_INGOT)continue;int take=Math.min(amount,item.getAmount());item.setAmount(item.getAmount()-take);p.getInventory().setItem(slot,item.getAmount()==0?null:item);amount-=take;}if(amount!=0)throw new IllegalStateException("金币扣除与余额不一致");}
    private void detectiveItems(PartySession s,Player p){ItemStack crossbow=s.item(Material.CROSSBOW,"items.crossbow");crossbow.addUnsafeEnchantment(Enchantment.QUICK_CHARGE,5);p.getInventory().addItem(crossbow,new ItemStack(Material.ARROW));}
    @Override public boolean pickup(PartySession s,Player p,Item item){return s.owns(item)&&item.getItemStack().getType()==Material.GOLD_INGOT;}
    @Override public boolean allowInventoryEdit(PartySession s,Player p){return true;}
    @Override public boolean allowSwap(PartySession s,Player p){return true;}
    @Override public void interact(PartySession s,PlayerInteractEvent e){
        if(!e.getAction().isRightClick()||e.getHand()!=org.bukkit.inventory.EquipmentSlot.HAND)return;
        Player p=e.getPlayer();
        if(armed&&killers.contains(p.getUniqueId())&&p.getInventory().getItemInMainHand().getType()==Material.CARROT_ON_A_STICK){
            e.setCancelled(true);if(shots.getOrDefault(p.getUniqueId(),0)>s.elapsed())return;shots.put(p.getUniqueId(),s.elapsed()+200);
            Arrow arrow=p.launchProjectile(Arrow.class,p.getEyeLocation().getDirection());arrow.setGravity(false);arrow.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);s.track(arrow);return;
        }
        if(e.getClickedBlock()==null)return;Block clicked=e.getClickedBlock();
        boolean shop=s.arena.group("potion-shops").stream().anyMatch(at->at.getBlock().equals(clicked));
        boolean wind=s.arena.group("wind-shops").stream().anyMatch(at->at.getBlock().equals(clicked));
        if(!shop&&!wind)return;e.setCancelled(true);if(!s.writable(clicked))return;
        int cost=shop?2:1;if(currency(p)<cost){p.sendMessage(s.config.message("not-enough-gold"));return;}
        if(shop&&cauldrons.containsKey(clicked)){p.sendMessage(s.config.message("shop-cooldown"));return;}spend(p,cost);
        if(wind){p.getInventory().addItem(new ItemStack(Material.WIND_CHARGE));return;}
        cauldrons.put(clicked,s.elapsed());s.setData(clicked,Bukkit.createBlockData("minecraft:water_cauldron[level=1]"));
        int roll=new Random().nextInt(14);PotionEffectType[] effects={PotionEffectType.SPEED,PotionEffectType.MINING_FATIGUE,PotionEffectType.NIGHT_VISION,PotionEffectType.HASTE,PotionEffectType.INVISIBILITY,PotionEffectType.RESISTANCE,PotionEffectType.JUMP_BOOST};
        ItemStack potion=s.item(Material.POTION,roll%2==0?"items.mystery":"items.potion-"+(roll/2));PotionMeta meta=(PotionMeta)potion.getItemMeta();meta.addCustomEffect(new PotionEffect(effects[roll/2],roll/2==5?400:600,roll/2==5?25:roll/2==6?2:1),true);if(roll%2==0)meta.setColor(Color.BLACK);potion.setItemMeta(meta);p.getInventory().addItem(potion);
    }
    @Override public void hit(PartySession s,Player attacker,Player victim,EntityDamageByEntityEvent e){
        e.setCancelled(true);if(!armed)return;
        if(e.getDamager() instanceof Projectile){if(victim.hasPotionEffect(PotionEffectType.RESISTANCE))return;kill(s,attacker,victim);return;}
        if(killers.contains(attacker.getUniqueId())&&attacker.getInventory().getItemInMainHand().getType()==Material.CARROT_ON_A_STICK){kill(s,attacker,victim);return;}
        e.setCancelled(false);
    }
    private void kill(PartySession s,Player attacker,Player victim){deaths.put(victim.getUniqueId(),victim.getLocation());s.eliminate(victim);if(!killers.contains(attacker.getUniqueId())&&!killers.contains(victim.getUniqueId())){attacker.sendMessage(s.config.message("wrong-kill"));deaths.put(attacker.getUniqueId(),attacker.getLocation());s.eliminate(attacker);}}
    @Override public void eliminated(PartySession s,Player p){if(!detectives.contains(p.getUniqueId()))return;Location at=deaths.getOrDefault(p.getUniqueId(),p.getLocation());
        Interaction target=at.getWorld().spawn(at,Interaction.class,e->{e.setInteractionWidth(1);e.setInteractionHeight(1.5f);e.setResponsive(true);e.setPersistent(false);});s.track(target);
        ItemDisplay display=at.getWorld().spawn(at.clone().add(0,1,0),ItemDisplay.class,e->{e.setItemStack(new ItemStack(Material.CROSSBOW));e.setGlowing(true);e.setPersistent(false);});s.track(display);bows.put(target,display);s.broadcast("bow-dropped");
    }
    @Override public void interactEntity(PartySession s,Player p,Entity target){
        if(!(target instanceof Interaction hit)||!bows.containsKey(hit)||killers.contains(p.getUniqueId())||detectives.contains(p.getUniqueId())||p.getLocation().distanceSquared(hit.getLocation())>16)return;
        bows.remove(hit).remove();hit.remove();detectives.add(p.getUniqueId());detectiveItems(s,p);p.sendMessage(s.config.message("became-detective"));
    }
    @Override public void attackEntity(PartySession s,Player p,Entity target){}
    private void finish(PartySession s,boolean killerWin){List<Player> winners=s.players().stream().filter(p->killers.contains(p.getUniqueId())==killerWin).toList();for(Player p:winners)s.points(p,killerWin?2:s.active(p)?2:1);s.finish(winners);}
    @Override public void environmentalDamage(PartySession s,Player p,EntityDamageEvent e){e.setCancelled(e.getCause()==EntityDamageEvent.DamageCause.FALL);}
    @Override public boolean lethalDamage(PartySession s,Player p,EntityDamageEvent event){deaths.put(p.getUniqueId(),p.getLocation());s.eliminate(p);if(event instanceof EntityDamageByEntityEvent hit){Player source=hit.getDamager() instanceof Player player?player:hit.getDamager() instanceof Projectile projectile&&projectile.getShooter() instanceof Player player?player:null;if(source!=null&&!killers.contains(source.getUniqueId())&&!killers.contains(p.getUniqueId())){deaths.put(source.getUniqueId(),source.getLocation());s.eliminate(source);}}return true;}
    @Override public void timeout(PartySession s){s.finish(List.of());}
}
