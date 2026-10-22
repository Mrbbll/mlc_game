package com.mlc.mlcgames.party.deduction.endless_wolf;

import com.mlc.mlcgames.party.core.*;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Equippable;
import org.bukkit.*;
import org.bukkit.attribute.*;
import org.bukkit.entity.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.*;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.*;
import java.util.*;

public final class EndlessWolfGame implements PartyGame {
    private static final NamespacedKey VAMPIRE=new NamespacedKey("mlcgames_party","vampire"),HOLY=new NamespacedKey("mlcgames_party","holy");
    private final Set<UUID> vampires=new HashSet<>();
    private int deadline,days;
    private boolean night,combat;
    @Override public void validate(PartyConfig c,PartyConfig.Arena a){a.group("loot-spawns");c.integer("day-seconds",30,180);c.integer("night-seconds",30,180);c.integer("days",1,5);c.integer("loot-interval-seconds",1,30);}
    @Override public boolean lastPlayerWins(){return false;}
    @Override public void start(PartySession s){
        List<Player> list=new ArrayList<>(s.survivors());Collections.shuffle(list);int count=Math.min(7,1+list.size()/6);list.stream().limit(count).forEach(p->vampires.add(p.getUniqueId()));
        days=s.config.integer("days",1,5);for(Player p:list)p.sendMessage(s.config.message(vampire(p)?"vampire-role":"survivor-role"));day(s);
    }
    private boolean vampire(Player p){return vampires.contains(p.getUniqueId());}
    private boolean tagged(ItemStack item,NamespacedKey key){return item!=null&&item.hasItemMeta()&&item.getItemMeta().getPersistentDataContainer().has(key,PersistentDataType.BYTE);}
    private ItemStack tag(ItemStack item,NamespacedKey key){ItemMeta meta=item.getItemMeta();meta.getPersistentDataContainer().set(key,PersistentDataType.BYTE,(byte)1);item.setItemMeta(meta);return item;}
    private void attribute(ItemStack item,Attribute attribute,double value,EquipmentSlotGroup slot){ItemMeta meta=item.getItemMeta();meta.addAttributeModifier(attribute,new AttributeModifier(new NamespacedKey("mlcgames_party",attribute.getKey().getKey().replace('.','_')),value,AttributeModifier.Operation.ADD_NUMBER,slot));item.setItemMeta(meta);}
    private void removeVampireItems(Player p){ItemStack[] contents=p.getInventory().getContents();for(int i=0;i<contents.length;i++)if(tagged(contents[i],VAMPIRE))p.getInventory().setItem(i,null);}
    private void equip(PartySession s,Player p){
        removeVampireItems(p);if(!vampire(p))return;
        ItemStack rod=tag(s.item(Material.END_ROD,"items.symbol"),VAMPIRE);rod.setData(DataComponentTypes.EQUIPPABLE,Equippable.equippable(EquipmentSlot.HEAD).swappable(true).equipOnInteract(true).build());
        if(night){attribute(rod,Attribute.GRAVITY,-.05,EquipmentSlotGroup.HEAD);attribute(rod,Attribute.JUMP_STRENGTH,.1,EquipmentSlotGroup.HEAD);attribute(rod,Attribute.SAFE_FALL_DISTANCE,10,EquipmentSlotGroup.HEAD);attribute(rod,Attribute.MOVEMENT_SPEED,.1,EquipmentSlotGroup.HEAD);}
        p.getInventory().setItem(7,rod);
        if(night){ItemStack sword=tag(s.item(Material.DIAMOND_SWORD,"items.sword"),VAMPIRE);ItemMeta meta=sword.getItemMeta();meta.setUnbreakable(false);sword.setItemMeta(meta);sword.addUnsafeEnchantment(Enchantment.SHARPNESS,1);sword.addUnsafeEnchantment(Enchantment.FIRE_ASPECT,1);attribute(sword,Attribute.ATTACK_DAMAGE,10,EquipmentSlotGroup.MAINHAND);p.getInventory().setItem(0,sword);}
    }
    private void day(PartySession s){
        if(days--<=0){finish(s,false);return;}night=false;deadline=s.elapsed()+s.config.integer("day-seconds",30,180)*20;
        for(Player p:s.players()){p.setPlayerTime(1000,false);equip(s,p);}s.broadcast("day","days",days+1);
    }
    private void night(PartySession s){night=true;combat=true;deadline=s.elapsed()+s.config.integer("night-seconds",30,180)*20;for(Player p:s.players()){p.setPlayerTime(18000,false);equip(s,p);}s.broadcast("night");}
    @Override public void tick(PartySession s,int elapsed){
        List<Player> live=s.survivors();boolean anyVampire=live.stream().anyMatch(this::vampire),anyNormal=live.stream().anyMatch(p->!vampire(p));
        if(!anyVampire||!anyNormal){finish(s,anyVampire);return;}
        if(elapsed>=deadline){if(night)day(s);else night(s);return;}
        if(elapsed%(s.config.integer("loot-interval-seconds",1,30)*20)==0){
            List<Location> eligible=s.arena.group("loot-spawns").stream().filter(at->at.getWorld().getNearbyEntities(at,3,3,3,e->e instanceof Item).isEmpty()).toList();
            if(!eligible.isEmpty()){Location at=eligible.get(new Random().nextInt(eligible.size()));Item item=at.getWorld().dropItem(at,loot(s,new Random().nextInt(41)));item.setGlowing(true);s.track(item);}
        }
        if(elapsed%20!=0)return;
        for(Player p:List.copyOf(live)){
            if(vampire(p)){
                if(!night){p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS,40,0,true,false));if(p.getInventory().getHelmet()==null&&p.getEyeLocation().getBlock().getLightFromSky()==15){hurt(s,p,2);p.sendActionBar(s.config.message("sunburn"));}}
                boolean holy=false;for(ItemStack item:p.getInventory().getContents())holy|=tagged(item,HOLY);
                if(holy){hurt(s,p,3);p.sendActionBar(s.config.message("holy-burn"));}
                // Source separately checks container.* and weapon.*, so a held holy
                // item triggers its second damage command too.
                if(tagged(p.getInventory().getItemInMainHand(),HOLY)||tagged(p.getInventory().getItemInOffHand(),HOLY))hurt(s,p,3);
                Player nearest=live.stream().filter(other->!vampire(other)).min(Comparator.comparingDouble(other->other.getLocation().distanceSquared(p.getLocation()))).orElse(null);
                if(nearest!=null){ItemStack compass=tag(s.item(Material.COMPASS,"items.compass"),VAMPIRE);CompassMeta meta=(CompassMeta)compass.getItemMeta();meta.setLodestone(nearest.getLocation());meta.setLodestoneTracked(false);compass.setItemMeta(meta);p.getInventory().setItem(8,compass);}
                boolean hasRod=false;for(ItemStack item:p.getInventory().getContents())hasRod|=item!=null&&item.getType()==Material.END_ROD&&tagged(item,VAMPIRE);if(!hasRod)equip(s,p);
            }else removeVampireItems(p);
            if(elapsed%80==0)p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION,40,1,true,false));
            p.sendActionBar(s.config.message(night?"night-status":"day-status","seconds",(deadline-elapsed+19)/20));
        }
    }
    private ItemStack loot(PartySession s,int roll){
        Material type=roll==0?Material.TOTEM_OF_UNDYING:roll<=2?Material.STONE_SWORD:roll==3?Material.IRON_SWORD:roll<=10?Material.LEATHER_HELMET:roll<=15?Material.LEATHER_CHESTPLATE:roll<=20?Material.LEATHER_LEGGINGS:roll<=25?Material.LEATHER_BOOTS:roll<=30?Material.WIND_CHARGE:roll<=32?Material.SHIELD:Material.SPLASH_POTION;
        ItemStack item=new ItemStack(type,roll>=26&&roll<=30?8:1);
        if(type==Material.STONE_SWORD||type==Material.IRON_SWORD||type==Material.SHIELD){item=s.item(type,type==Material.STONE_SWORD?"items.holy-sword":type==Material.IRON_SWORD?"items.cross":"items.holy-shield");tag(item,HOLY);ItemMeta meta=item.getItemMeta();meta.setUnbreakable(false);item.setItemMeta(meta);}
        if(type==Material.IRON_SWORD){item.addUnsafeEnchantment(Enchantment.SHARPNESS,2);org.bukkit.inventory.meta.Damageable meta=(org.bukkit.inventory.meta.Damageable)item.getItemMeta();meta.setUnbreakable(false);meta.setMaxDamage(1);item.setItemMeta(meta);attribute(item,Attribute.ATTACK_DAMAGE,18,EquipmentSlotGroup.MAINHAND);attribute(item,Attribute.ATTACK_SPEED,3,EquipmentSlotGroup.MAINHAND);}
        if(type==Material.SPLASH_POTION){PotionMeta meta=(PotionMeta)item.getItemMeta();meta.setBasePotionType(switch(roll){case 33->PotionType.SWIFTNESS;case 34->PotionType.SLOWNESS;case 35->PotionType.STRONG_LEAPING;case 36->PotionType.STRENGTH;default->PotionType.INVISIBILITY;});if(roll==40){meta.setBasePotionType(PotionType.WATER);meta.addCustomEffect(new PotionEffect(PotionEffectType.RESISTANCE,100,5),true);}item.setItemMeta(meta);}
        return item;
    }
    private void hurt(PartySession s,Player p,double damage){if(!s.active(p))return;if(p.getHealth()<=damage){
        // Preserve the ordinary vanilla totem escape without entering a real death screen.
        if(p.getInventory().getItemInOffHand().getType()==Material.TOTEM_OF_UNDYING||p.getInventory().getItemInMainHand().getType()==Material.TOTEM_OF_UNDYING){if(p.getInventory().getItemInOffHand().getType()==Material.TOTEM_OF_UNDYING)p.getInventory().setItemInOffHand(null);else p.getInventory().setItemInMainHand(null);p.setHealth(1);p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION,900,1));p.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION,100,1));p.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE,800,0));return;}s.eliminate(p);
    }else{p.setHealth(p.getHealth()-damage);p.playHurtAnimation(0);}}
    @Override public void hit(PartySession s,Player attacker,Player victim,EntityDamageByEntityEvent e){
        e.setCancelled(true);if(!combat)return;
        boolean lethal=e.getFinalDamage()>=victim.getHealth();e.setCancelled(false);
        // Ordinary lethal hits eliminate only their victim. The source rules and
        // user-approved behavior do not punish a normal attacker for a kill.
        if(lethal){e.setCancelled(true);hurt(s,victim,e.getFinalDamage());}
    }
    @Override public void environmentalDamage(PartySession s,Player p,EntityDamageEvent e){e.setCancelled(true);hurt(s,p,e.getFinalDamage());}
    @Override public boolean allowInventoryEdit(PartySession s,Player p){return true;}
    @Override public boolean allowSwap(PartySession s,Player p){return true;}
    @Override public boolean pickup(PartySession s,Player p,Item item){return s.owns(item);}
    @Override public boolean allowDrop(PartySession s,Player p,ItemStack item){return !tagged(item,VAMPIRE);}
    @Override public void interact(PartySession s,PlayerInteractEvent e){
        if(e.getHand()!=EquipmentSlot.HAND||!e.getAction().isRightClick()||e.getItem()==null||e.getItem().getType()!=Material.END_ROD||!tagged(e.getItem(),VAMPIRE))return;
        e.setCancelled(true);Player p=e.getPlayer();ItemStack before=p.getInventory().getHelmet();p.getInventory().setHelmet(e.getItem().clone());p.getInventory().setItemInMainHand(before);
    }
    private void finish(PartySession s,boolean vampireWin){List<Player> winners=s.players().stream().filter(p->vampire(p)==vampireWin&&(vampireWin||s.active(p))).toList();for(Player p:winners)s.points(p,2);s.finish(winners);}
    @Override public void stopped(PartySession s){for(Player p:s.players())p.resetPlayerTime();}
    @Override public void eliminated(PartySession s,Player p){p.resetPlayerTime();}
    @Override public void timeout(PartySession s){finish(s,false);}
}
