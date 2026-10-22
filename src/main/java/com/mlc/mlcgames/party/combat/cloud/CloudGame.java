package com.mlc.mlcgames.party.combat.cloud;

import com.mlc.mlcgames.party.combat.common.*;
import com.mlc.mlcgames.party.core.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import java.util.*;

/** All four source supply modes and four timed refills are preserved. */
public final class CloudGame extends ChestSurvival {
    private int next,refills;
    private String mode;
    @Override public boolean lastPlayerWins(){return false;}
    @Override public void validate(PartyConfig c,PartyConfig.Arena a){
        validateLoot(c,a);mode=c.rules.getString("supply-mode","normal");if(!List.of("normal","tnt","hunger","potion").contains(mode))throw new IllegalArgumentException("supply-mode应为normal/tnt/hunger/potion");
        for(String key:List.of("side","sidemiddle","middle"))a.group(key);c.integer("first-refill-seconds",1,600);c.integer("refill-seconds",1,600);c.integer("final-seconds",1,600);
    }
    private void refillAll(PartySession s){
        for(String group:List.of("side","sidemiddle","middle")){
            String name=group.equals("sidemiddle")?"sidemi":group;
            refill(s,group,"cloud/"+mode+"/"+(mode.equals("tnt")?"tnt_":"")+name,Material.CHEST);
        }
    }
    @Override public void start(PartySession s){
        for(Player p:s.survivors()){p.setGameMode(GameMode.SURVIVAL);if(mode.equals("tnt"))p.getInventory().setHelmet(s.item(Material.LEATHER_HELMET,"items.equipment"));}
        refillAll(s);next=s.elapsed()+s.config.integer("first-refill-seconds",1,600)*20;
    }
    @Override public void tick(PartySession s,int elapsed){
        if(s.survivors().size()<=1){awardSurvivors(s,3);return;}
        if(elapsed>=next){
            if(refills>=4){s.finish(List.of());return;}
            refillAll(s);refills++;next=elapsed+(refills==4?s.config.integer("final-seconds",1,600):s.config.integer("refill-seconds",1,600))*20;
        }
        if(elapsed%20==0)for(Player p:s.survivors())p.sendActionBar(s.config.message("status","seconds",Math.max(0,(next-elapsed)/20),"refills",refills));
    }
    @Override public boolean breakBlock(PartySession s,Player p,Block b){
        if(!s.writable(b))return false;
        for(var drop:b.getDrops(p.getInventory().getItemInMainHand(),p)){var entity=b.getWorld().dropItemNaturally(b.getLocation(),drop);s.track(entity);}
        return true;
    }
    @Override public boolean placeBlock(PartySession s,Player p,Block b){return s.writable(b);}
    @Override public void interact(PartySession s,org.bukkit.event.player.PlayerInteractEvent e){
        super.interact(s,e);
        if(e.getHand()!=org.bukkit.inventory.EquipmentSlot.HAND||!e.getAction().isRightClick()||e.getItem()==null)return;
        int kind=e.getItem().getItemMeta().getPersistentDataContainer().getOrDefault(new NamespacedKey("mlcgames","source_tnt"),org.bukkit.persistence.PersistentDataType.INTEGER,0);
        if(kind>=1&&kind<=4||kind==8){e.setCancelled(true);Explosives.launch(s,e.getPlayer(),kind);e.getPlayer().getInventory().setItemInMainHand(null);}
    }
    public void explode(PartySession s,org.bukkit.event.entity.EntityExplodeEvent e){Explosives.explode(s,e);}
}
