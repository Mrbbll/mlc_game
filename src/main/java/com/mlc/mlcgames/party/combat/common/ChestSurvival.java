package com.mlc.mlcgames.party.combat.common;

import com.mlc.mlcgames.party.core.*;
import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.entity.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.*;
import java.util.*;

/** Native inventory survival with only session-owned drops and snapshotted containers. */
public abstract class ChestSurvival implements PartyGame {
    protected NativeLoot loot;
    protected void validateLoot(PartyConfig c,PartyConfig.Arena a){loot=new NativeLoot(c);a.region("build");}
    @Override public List<Cuboid> writableRegions(PartyConfig.Arena a){return List.of(a.region("build"));}
    protected void refill(PartySession s,String group,String table,Material container){
        for(Location l:s.arena.group(group)){
            Block b=l.getBlock();if(!s.writable(b))throw new IllegalArgumentException("补给箱不在build区域");
            s.set(b,container);
            if(!(b.getState() instanceof Container chest))throw new IllegalStateException("方块预算不足或非容器");
            chest.getInventory().clear();List<ItemStack> items=loot.roll(table);Collections.shuffle(items);
            List<Integer> slots=new ArrayList<>();for(int i=0;i<chest.getInventory().getSize();i++)slots.add(i);Collections.shuffle(slots);
            for(int i=0;i<items.size()&&i<slots.size();i++)chest.getInventory().setItem(slots.get(i),items.get(i));
        }
        s.broadcast("refilled");
    }
    protected void dropInventory(PartySession s,Player p){
        for(ItemStack item:p.getInventory().getContents())if(item!=null&&!item.getType().isAir()){
            Item drop=p.getWorld().dropItemNaturally(p.getLocation(),item.clone());s.track(drop);drop.setPickupDelay(0);
        }
    }
    @Override public void hit(PartySession s,Player a,Player v,EntityDamageByEntityEvent e){
        if(!pvp(s))return;e.setCancelled(false);if(e.getFinalDamage()>=v.getHealth())dropInventory(s,v);
    }
    protected boolean pvp(PartySession s){return true;}
    @Override public void environmentalDamage(PartySession s,Player p,EntityDamageEvent e){e.setCancelled(false);if(e.getFinalDamage()>=p.getHealth())dropInventory(s,p);}
    @Override public void hazard(PartySession s,Player p){if(!p.isDead())dropInventory(s,p);s.eliminate(p);}
    @Override public void interact(PartySession s,PlayerInteractEvent e){
        if(e.getClickedBlock()!=null&&s.writable(e.getClickedBlock())&&e.getClickedBlock().getState() instanceof Container){
            e.setUseInteractedBlock(org.bukkit.event.Event.Result.ALLOW);
        }
    }
    public boolean allowPickup(PartySession s,Player p,Item item){return s.owns(item);}
    public boolean allowInventory(PartySession s,org.bukkit.event.inventory.InventoryClickEvent e){
        Inventory top=e.getView().getTopInventory();
        return top.getType()==org.bukkit.event.inventory.InventoryType.CRAFTING||top.getLocation()!=null&&s.arena.contains(top.getLocation())&&s.writable(top.getLocation().getBlock());
    }
    public boolean allowDrop(PartySession s,Player p,Item item){s.track(item);return true;}
    public boolean allowSwap(PartySession s,Player p){return true;}
    @Override public boolean allowInventory(PartySession s,Player p){return true;}
    @Override public boolean allowBlockInteraction(PartySession s,Player p,Block b){return s.writable(b)&&(b.getState() instanceof Container||b.getType()==Material.CRAFTING_TABLE||b.getType()==Material.ANVIL||b.getType()==Material.ENCHANTING_TABLE);}
    @Override public boolean allowNaturalChanges(PartySession s){return true;}
    @Override public boolean maintainFood(PartySession s){return false;}
    protected void awardSurvivors(PartySession s,int points){for(Player p:s.survivors())s.points(p,points);s.finish(s.survivors());}
    @Override public void timeout(PartySession s){s.finish(List.of());}
}
