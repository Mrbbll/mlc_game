package com.mlc.mlcgames.party.survival.live;

import com.mlc.mlcgames.party.survival.chase.ChaseRounds;
import com.mlc.mlcgames.party.core.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.*;
import java.util.*;

public final class LiveGame extends ChaseRounds {
    private boolean flying;
    private final Set<UUID> onSupply=new HashSet<>();
    @Override public void validate(PartyConfig c,PartyConfig.Arena a){super.validate(c,a);a.region("play");a.group("supply");String mode=c.rules.getString("map-type","");if(!Set.of("rainbow","nether").contains(mode))throw new IllegalArgumentException("map-type须为rainbow或nether");flying=mode.equals("rainbow");}
    @Override public List<Cuboid> writableRegions(PartyConfig.Arena a){return List.of(a.region("play"));}
    @Override protected int initialCoins(){return 1;}
    @Override protected void prepareRound(PartySession s){onSupply.clear();for(int x=s.arena.region("play").minX();x<=s.arena.region("play").maxX();x++)for(int y=s.arena.region("play").minY();y<=s.arena.region("play").maxY();y++)for(int z=s.arena.region("play").minZ();z<=s.arena.region("play").maxZ();z++){Block block=s.arena.world().getBlockAt(x,y,z);if(block.getType()==Material.HEAVY_WEIGHTED_PRESSURE_PLATE)s.set(block,Material.AIR);}}
    private ItemStack enchanted(PartySession s,Material type,String name,Enchantment enchantment,int level){ItemStack item=s.item(type,name);item.addUnsafeEnchantment(enchantment,level);return item;}
    @Override protected void equipment(PartySession s,Player p){
        p.getInventory().clear();
        if(hunter(p)){
            if(flying){p.getInventory().setItem(0,enchanted(s,Material.TRIDENT,"items.riptide",Enchantment.RIPTIDE,1));p.getInventory().setItem(1,enchanted(s,Material.TRIDENT,"items.loyalty",Enchantment.LOYALTY,1));p.getInventory().setChestplate(s.item(Material.ELYTRA,"items.elytra"));}
            else{p.getInventory().setItem(0,enchanted(s,Material.DIAMOND_SWORD,"items.sword",Enchantment.SHARPNESS,1));p.getInventory().setItem(1,enchanted(s,Material.DIAMOND_AXE,"items.axe",Enchantment.SHARPNESS,1));p.getInventory().setChestplate(s.item(Material.DIAMOND_CHESTPLATE,"items.chestplate"));}
            supply(s,p);
        }else{p.getInventory().setItem(0,enchanted(s,Material.STICK,"items.stick",Enchantment.KNOCKBACK,flying?3:2));if(flying)p.getInventory().setChestplate(s.item(Material.ELYTRA,"items.elytra"));ItemStack plates=s.item(Material.HEAVY_WEIGHTED_PRESSURE_PLATE,"items.plate");plates.setAmount(8);p.getInventory().setItem(1,plates);}
    }
    private void supply(PartySession s,Player p){p.getInventory().setItem(6,new ItemStack(Material.WIND_CHARGE,8));for(int i=0;i<2;i++){ItemStack potion=s.item(Material.POTION,i==0?"items.speed":"items.jump");PotionMeta meta=(PotionMeta)potion.getItemMeta();meta.addCustomEffect(new PotionEffect(i==0?PotionEffectType.SPEED:PotionEffectType.JUMP_BOOST,200,i==0?0:2),true);potion.setItemMeta(meta);p.getInventory().setItem(7+i,potion);}}
    @Override public void tick(PartySession s,int elapsed){if(!advance(s,elapsed))return;for(Player p:s.survivors()){
        if(elapsed%20==0){if(hunter(p)){p.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS,40,0,true,false));if(!flying)p.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE,100,25,true,false));else{boolean riptide=false,loyalty=false;for(ItemStack item:p.getInventory().getContents())if(item!=null&&item.getType()==Material.TRIDENT){riptide|=item.containsEnchantment(Enchantment.RIPTIDE);loyalty|=item.containsEnchantment(Enchantment.LOYALTY);}if(!riptide)p.getInventory().addItem(enchanted(s,Material.TRIDENT,"items.riptide",Enchantment.RIPTIDE,1));if(!loyalty)p.getInventory().addItem(enchanted(s,Material.TRIDENT,"items.loyalty",Enchantment.LOYALTY,1));}}if(elapsed%80==0)p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION,40,1,true,false));}
        boolean supply=hunter(p)&&s.arena.group("supply").stream().anyMatch(at->at.getBlockX()==p.getLocation().getBlockX()&&at.getBlockZ()==p.getLocation().getBlockZ()&&Math.abs(at.getY()-p.getLocation().getY())<1.5);
        if(supply&&onSupply.add(p.getUniqueId()))supply(s,p);if(!supply)onSupply.remove(p.getUniqueId());
    }}
    @Override protected void onCaught(PartySession s,Player p){int left=runners.size();if(left<3)add(s,p,3-left);}
    @Override protected void roundOver(PartySession s){if(runners.isEmpty()){for(Player p:s.survivors())if(hunter(p))add(s,p,4);}else for(UUID id:runners){Player p=Bukkit.getPlayer(id);if(p!=null)add(s,p,4);}super.roundOver(s);}
    @Override public void hit(PartySession s,Player attacker,Player victim,EntityDamageByEntityEvent event){event.setCancelled(!running);}
    @Override public void environmentalDamage(PartySession s,Player p,EntityDamageEvent event){event.setCancelled(!running);}
    @Override public boolean lethalDamage(PartySession s,Player p,EntityDamageEvent event){hazard(s,p);return true;}
    @Override public boolean placeBlock(PartySession s,Player p,Block b){return running&&!hunter(p)&&b.getType()==Material.HEAVY_WEIGHTED_PRESSURE_PLATE&&b.getRelative(0,-1,0).getType()==Material.SLIME_BLOCK;}
    @Override public boolean allowPhysics(PartySession s,Block block){return s.writable(block);}
    @Override public boolean allowBlockInteraction(PartySession s,Player p,Block b){return b.getType()==Material.HEAVY_WEIGHTED_PRESSURE_PLATE;}
    @Override public boolean allowInventoryEdit(PartySession s,Player p){return true;}
    @Override public boolean allowSwap(PartySession s,Player p){return true;}
    @Override public void shoot(PartySession s,EntityShootBowEvent event){}
}
