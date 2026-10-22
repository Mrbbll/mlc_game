package com.mlc.mlcgames.party.combat.common;

import com.mlc.mlcgames.party.core.PartySession;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.bukkit.persistence.PersistentDataType;
import java.util.*;
import java.util.regex.*;

/** Source item component strings are parsed as items by Bukkit, never dispatched as commands. */
public final class NativeEquipment {
    private NativeEquipment(){}
    public static ItemStack create(String input,int count){
        String itemInput=input.trim();String id=itemInput.split("\\[",2)[0].trim();if(!id.contains(":"))itemInput="minecraft:"+itemInput;
        ItemStack item=Bukkit.getItemFactory().createItemStack(itemInput);if(item.getType().isAir())return item;item.setAmount(Math.max(1,Math.min(99,count)));
        var meta=item.getItemMeta();
        for(String key:List.of("blaze_skill","blaze","job","job_pvp","bw_return_scroll","bw_platform","bw_class_pick","bw_invul","bw_levi","bw_milk","bw_bug","bw_class_tool")){
            Matcher m=Pattern.compile("(?:[\\\"']?"+key+"[\\\"']?)\\s*:\\s*(?:[\\\"']([^\\\"']+)[\\\"']|([A-Za-z0-9_]+))").matcher(input);
            if(m.find())meta.getPersistentDataContainer().set(new NamespacedKey("mlcgames","source_"+key),PersistentDataType.STRING,m.group(1)!=null?m.group(1):m.group(2));
        }
        if(input.contains("bw.tower.spawn"))meta.getPersistentDataContainer().set(new NamespacedKey("mlcgames","source_bw_tower"),PersistentDataType.STRING,"true");
        if(input.contains("bw.golem.spawn"))meta.getPersistentDataContainer().set(new NamespacedKey("mlcgames","source_bw_golem"),PersistentDataType.STRING,"true");
        item.setItemMeta(meta);return item;
    }
    public static String flag(ItemStack item,String key){return item==null||!item.hasItemMeta()?"":item.getItemMeta().getPersistentDataContainer().getOrDefault(new NamespacedKey("mlcgames","source_"+key),PersistentDataType.STRING,"");}
    public static void give(PartySession s,Player p,String path){
        for(Map<?,?> row:s.config.rules.getMapList(path)){
            ItemStack item=create(String.valueOf(row.get("input")),row.get("amount") instanceof Number n?n.intValue():1);
            switch(String.valueOf(row.get("slot"))){
                case "head"->p.getInventory().setHelmet(item);case "chest"->p.getInventory().setChestplate(item);case "legs"->p.getInventory().setLeggings(item);case "feet"->p.getInventory().setBoots(item);
                case "mainhand"->p.getInventory().setItemInMainHand(item);case "offhand"->p.getInventory().setItemInOffHand(item);default->{String slot=String.valueOf(row.get("slot"));if(slot.matches("\\d+"))p.getInventory().setItem(Integer.parseInt(slot),item);else p.getInventory().addItem(item);}
            }
        }
    }
}
