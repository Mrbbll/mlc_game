package com.mlc.mlcgames.zombieday.inv;

import com.mlc.mlcgames.zombieday.Item;
import com.mlc.mlcgames.zombieday.Zombiedaygame;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Objects;

import static com.mlc.mlcgames.Mlcgames.miniMessage;

public class EffectInv {
    public static Inventory inv = Bukkit.createInventory(null,9);
    private static final ItemStack itemStack1 = ItemStack.of(Material.REDSTONE);
    private static final ItemStack itemStack2 = ItemStack.of(Material.RABBIT_FOOT);
    private static final ItemStack itemStack3 = ItemStack.of(Material.DIAMOND_PICKAXE);
    private static final ItemStack itemStack4 = ItemStack.of(Material.DIAMOND_CHESTPLATE);
    private static final ItemStack itemStack5 = ItemStack.of(Material.DIAMOND_SWORD);

    static{
        ItemMeta itemMeta = itemStack1.getItemMeta();
        itemMeta.displayName(miniMessage.deserialize("<!i>生命上限提升"));
        itemStack1.setItemMeta(itemMeta);

        ItemMeta itemMeta1 = itemStack2.getItemMeta();
        itemMeta1.displayName(miniMessage.deserialize("<!i>速度提升"));
        itemStack2.setItemMeta(itemMeta1);

        ItemMeta itemMeta2 = itemStack3.getItemMeta();
        itemMeta2.displayName(miniMessage.deserialize("<!i>挖掘速度提升"));
        itemStack3.setItemMeta(itemMeta2);

        ItemMeta itemMeta3 = itemStack4.getItemMeta();
        itemMeta3.displayName(miniMessage.deserialize("<!i>护甲提升"));
        itemStack4.setItemMeta(itemMeta3);

        ItemMeta itemMeta4 = itemStack5.getItemMeta();
        itemMeta4.displayName(miniMessage.deserialize("<!i>击退提升"));
        itemStack5.setItemMeta(itemMeta4);

        inv.setItem(0,itemStack1);
        inv.setItem(2,itemStack2);
        inv.setItem(4,itemStack3);
        inv.setItem(6,itemStack4);
        inv.setItem(8,itemStack5);
    }

    public static void init(){
        inv.setItem(0,itemStack1);
        inv.setItem(2,itemStack2);
        inv.setItem(4,itemStack3);
        inv.setItem(6,itemStack4);
        inv.setItem(8,itemStack5);
    }

    public static void open(Player player){
        player.openInventory(inv);
    }

    public static void selecthander(ItemStack item,Player player){
        ItemStack itemStack = player.getInventory().getItemInMainHand();
        if(!itemStack.getType().equals(Material.EMERALD)){
            player.sendMessage(miniMessage.deserialize("<!i>请手持货币"));
            return;
        }
        int num = itemStack.getAmount();
        if(num<64) {
            player.sendMessage(miniMessage.deserialize("<!i>你没有足够的货币"));
            return;
        };

        if(item.equals(itemStack1)){
            for(Player player1: Zombiedaygame.players){
                AttributeInstance attributeInstance = player1.getAttribute(Attribute.MAX_HEALTH);
                if( attributeInstance!=null){
                    attributeInstance.setBaseValue(attributeInstance.getBaseValue() + 10);
                }
                itemStack.setAmount(0);
                player1.sendMessage(miniMessage.deserialize(player.getName()+"<!i>开启了生命上限提升"));
            }
            ItemStack itemstack1_1 = itemStack1.clone();
            List<Component> lore = List.of(miniMessage.deserialize("<!i>生命上限已提升"));
            ItemMeta itemMeta = itemstack1_1.getItemMeta();
            itemMeta.lore(lore);
            itemstack1_1.setItemMeta(itemMeta);
            inv.setItem(0,itemstack1_1);

        }else if(item.equals(itemStack2)){
            for(Player player1: Zombiedaygame.players){
                AttributeInstance attributeInstance = player1.getAttribute(Attribute.MOVEMENT_SPEED);
                if( attributeInstance!=null){
                    attributeInstance.setBaseValue(attributeInstance.getBaseValue() + 0.02);
                    itemStack.setAmount(0);
                }
                player1.sendMessage(miniMessage.deserialize(player.getName()+"<!i>开启了速度提升"));
            }
            ItemStack itemstack2_1 = itemStack2.clone();
            List<Component> lore = List.of(miniMessage.deserialize(player.getName()+"<!i>速度已提升"));
            ItemMeta itemMeta = itemstack2_1.getItemMeta();
            itemMeta.lore(lore);
            itemstack2_1.setItemMeta(itemMeta);
            inv.setItem(2,itemstack2_1);

        }else if(item.equals(itemStack3)){
            for(Player player1: Zombiedaygame.players){
                AttributeInstance attributeInstance = player1.getAttribute(Attribute.BLOCK_BREAK_SPEED);
                if( attributeInstance!=null){
                    attributeInstance.setBaseValue(attributeInstance.getBaseValue() + 0.2);
                    itemStack.setAmount(0);
                }
                player1.sendMessage(miniMessage.deserialize(player.getName()+"<!i>开启了挖掘速度提升"));
            }
            ItemStack itemstack3_1 = itemStack3.clone();
            List<Component> lore = List.of(miniMessage.deserialize(player.getName()+"<!i>挖掘速度已提升"));
            ItemMeta itemMeta = itemstack3_1.getItemMeta();
            itemMeta.lore(lore);
            itemstack3_1.setItemMeta(itemMeta);
            inv.setItem(4,itemstack3_1);
        }else if(item.equals(itemStack4)){
            for(Player player1: Zombiedaygame.players){
                AttributeInstance attributeInstance = player1.getAttribute(Attribute.ARMOR);
                if( attributeInstance!=null){
                    attributeInstance.setBaseValue(attributeInstance.getBaseValue() + 4);
                    itemStack.setAmount(0);
                }
                player1.sendMessage(miniMessage.deserialize(player.getName()+"<!i>开启了护甲提升"));
            }
            ItemStack itemstack4_1 = itemStack4.clone();
            List<Component> lore = List.of(miniMessage.deserialize(player.getName()+"<!i>护甲已提升"));
            ItemMeta itemMeta = itemstack4_1.getItemMeta();
            itemMeta.lore(lore);
            itemstack4_1.setItemMeta(itemMeta);
            inv.setItem(6,itemstack4_1);
        }else if(item.equals(itemStack5)){
            for(Player player1: Zombiedaygame.players){
                AttributeInstance attributeInstance = player1.getAttribute(Attribute.ATTACK_KNOCKBACK);
                if( attributeInstance!=null){
                    attributeInstance.setBaseValue(attributeInstance.getBaseValue() + 0.2);
                    itemStack.setAmount(0);
                }
                player1.sendMessage(miniMessage.deserialize(player.getName()+"<!i>开启了击退提升"));
            }
            ItemStack itemstack5_1 = itemStack5.clone();
            List<Component> lore = List.of(miniMessage.deserialize(player.getName()+"<!i>击退已提升"));
            ItemMeta itemMeta = itemstack5_1.getItemMeta();
            itemMeta.lore(lore);
            itemstack5_1.setItemMeta(itemMeta);
            inv.setItem(8,itemstack5_1);
        }
    }
}
