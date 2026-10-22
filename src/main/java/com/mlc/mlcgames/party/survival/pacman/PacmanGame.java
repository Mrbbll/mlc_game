package com.mlc.mlcgames.party.survival.pacman;

import com.mlc.mlcgames.party.survival.chase.ChaseRounds;
import com.mlc.mlcgames.party.core.*;
import org.bukkit.*;
import org.bukkit.attribute.*;
import org.bukkit.inventory.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import java.util.*;

public final class PacmanGame extends ChaseRounds {
    private Cuboid floor;
    private boolean warming;
    @Override public void validate(PartyConfig c,PartyConfig.Arena a){super.validate(c,a);floor=a.region("floor");if(floor.minY()!=floor.maxY())throw new IllegalArgumentException("吃豆人floor必须为单层地板");}
    @Override public List<Cuboid> writableRegions(PartyConfig.Arena a){return List.of(a.region("floor"));}
    @Override protected void prepareRound(PartySession s){
        warming=false;
        for(int x=floor.minX();x<=floor.maxX();x++)for(int z=floor.minZ();z<=floor.maxZ();z++){var block=s.arena.world().getBlockAt(x,floor.minY(),z);if(block.getType()==Material.GOLD_BLOCK)s.set(block,Material.WHITE_CONCRETE);}
        for(Player p:s.players())helmet(s,p,false);
    }
    @Override protected void beginRound(PartySession s){
        if(!warming){for(int x=floor.minX();x<=floor.maxX();x++)for(int z=floor.minZ();z<=floor.maxZ();z++){var block=s.arena.world().getBlockAt(x,floor.minY(),z);if(block.getType()==Material.WHITE_CONCRETE)s.set(block,Material.GOLD_BLOCK);}warming=true;readyAt=s.elapsed()+(int)(floor.volume()/s.config.integer("limits.blocks-per-tick",1,2000))+2;return;}
        super.beginRound(s);
    }
    @Override protected void equipment(PartySession s,Player p){p.getInventory().clear();helmet(s,p,hunter(p));}
    private void helmet(PartySession s,Player p,boolean hunter){
        ItemStack item=s.item(hunter?Material.RED_CONCRETE:Material.YELLOW_CONCRETE,hunter?"items.red-helmet":"items.yellow-helmet");var meta=item.getItemMeta();
        Attribute[] attributes={Attribute.MOVEMENT_SPEED,Attribute.JUMP_STRENGTH,Attribute.KNOCKBACK_RESISTANCE,Attribute.ENTITY_INTERACTION_RANGE};double[] values={hunter?.04:0,-5,10,-1.5};
        for(int i=0;i<attributes.length;i++)meta.addAttributeModifier(attributes[i],new AttributeModifier(new NamespacedKey("mlcgames_party","pacman_"+i),values[i],AttributeModifier.Operation.ADD_NUMBER,EquipmentSlotGroup.HEAD));item.setItemMeta(meta);item.addUnsafeEnchantment(Enchantment.BINDING_CURSE,1);p.getInventory().setHelmet(item);
    }
    @Override public void tick(PartySession s,int elapsed){if(!advance(s,elapsed))return;for(UUID id:List.copyOf(runners)){Player p=Bukkit.getPlayer(id);if(p==null)continue;var block=p.getLocation().subtract(0,1,0).getBlock();if(s.writable(block)&&block.getType()==Material.GOLD_BLOCK){s.set(block,Material.WHITE_CONCRETE);add(s,p,1);p.playSound(p.getLocation(),Sound.ENTITY_EXPERIENCE_ORB_PICKUP,1,1);}}}
    @Override public void hit(PartySession s,Player attacker,Player victim,EntityDamageByEntityEvent event){event.setCancelled(true);if(!running||attacker.getLocation().distanceSquared(victim.getLocation())>2.25)return;if(hunter(attacker)&&!hunter(victim)&&runners.contains(victim.getUniqueId())){add(s,attacker,5);add(s,victim,-5);caught(s,victim);}else if(!hunter(attacker)&&hunter(victim))victim.teleport(s.arena.group("hunters").getFirst());}
}
