package com.mlc.mlcgames.utils.item;

import com.mlc.mlcgames.zombieday.Item;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import static com.mlc.mlcgames.Mlcgames.instance;

public class Gun {
    public static NamespacedKey refillcooldown = new NamespacedKey(instance,"refillcooldown");

    public static void setRefillcooldown(ItemStack item,boolean bool){
        ItemMeta itemMeta = item.getItemMeta();
        PersistentDataContainer pdc = itemMeta.getPersistentDataContainer();
        pdc.set(refillcooldown,PersistentDataType.BOOLEAN,bool);
        item.setItemMeta(itemMeta);
    }

    public static boolean isinRefillcooldown(ItemStack item){
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        return pdc.getOrDefault(refillcooldown,PersistentDataType.BOOLEAN,false);
    }


    public static int getbulletcount(ItemStack gun){
        return gun.getItemMeta().getPersistentDataContainer().getOrDefault(Item.bulletcountkey, PersistentDataType.INTEGER, 0);
    }
    public static void setbulletcount(ItemStack gun, int bulletcount){
        ItemMeta itemMeta = gun.getItemMeta();
        itemMeta.getPersistentDataContainer().set(Item.bulletcountkey, PersistentDataType.INTEGER, bulletcount);
        gun.setItemMeta(itemMeta);
    }
    public static void setmaxbulletcount(ItemStack gun, int maxbulletcount){
        ItemMeta itemMeta = gun.getItemMeta();
        itemMeta.getPersistentDataContainer().set(Item.maxbulletcountkey, PersistentDataType.INTEGER, maxbulletcount);
        gun.setItemMeta(itemMeta);
    }
    public static void settypedata(ItemStack gun, String type){
        ItemMeta itemMeta = gun.getItemMeta();
        itemMeta.getPersistentDataContainer().set(Item.itemtype, PersistentDataType.STRING, type);
        gun.setItemMeta(itemMeta);
        gun.setItemMeta(itemMeta);
    }

    public static int getmaxbulletcount(ItemStack gun) {
        return gun.getItemMeta().getPersistentDataContainer().getOrDefault(Item.maxbulletcountkey, PersistentDataType.INTEGER, 0);
    }

    public static int getinvbulletcount(Player player) {
        int invbulletcount = 0;
        for (ItemStack itemStack : player.getInventory().getContents()) {
            if (itemStack != null && itemStack.getType() == Material.STONE_BUTTON) {
                invbulletcount += itemStack.getAmount();
            }
        }
        return invbulletcount;
    }

    public static void removeinvbullet(Player player,int bulletcount){
        for (ItemStack itemStack : player.getInventory().getContents()) {
            if (itemStack != null && itemStack.getType() == Material.STONE_BUTTON) {
                if(itemStack.getAmount()>=bulletcount){
                    itemStack.setAmount(itemStack.getAmount() - bulletcount);
                    return;
                }else {
                    itemStack.setAmount(0);
                    bulletcount-=itemStack.getAmount();
                }

            }
        }
    }

}
