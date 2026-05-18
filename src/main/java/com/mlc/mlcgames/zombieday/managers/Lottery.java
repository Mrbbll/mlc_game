package com.mlc.mlcgames.zombieday.managers;

import com.mlc.mlcgames.zombieday.Item;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Lottery {
    public static Map<ItemStack,Integer> lotterymap = new HashMap<>();
    public static int total = 0;
    static {
        lotterymap.put(Item.handgun.clone(),2);
        lotterymap.put(Item.shotgun.clone(),1);
        lotterymap.put(Item.submachine_gun.clone(),1);
        lotterymap.put(Item.rifle.clone(),1);
        lotterymap.put(Item.grenade.clone(),1);
        ItemStack bullet = Item.bullet.clone();
        bullet.setAmount(64);
        lotterymap.put(bullet,20);
        lotterymap.put(ItemStack.of(Material.GOLDEN_APPLE,5),1);
        lotterymap.put(ItemStack.of(Material.IRON_GOLEM_SPAWN_EGG),5);
        lotterymap.put(ItemStack.of(Material.IRON_GOLEM_SPAWN_EGG,2),1);
        lotterymap.put(ItemStack.of(Material.WOLF_SPAWN_EGG,3),10);
        lotterymap.put(ItemStack.of(Material.WOLF_SPAWN_EGG,5),5);
        lotterymap.put(ItemStack.of(Material.GOLDEN_CARROT,32),10);
        for (Integer value : lotterymap.values()) {
            total += value;
        }
    }

    public static ItemStack getRandomItem() {
        Random random = new Random();
        int randomInt = random.nextInt(total);
        int current = 0;
        for (ItemStack itemStack : lotterymap.keySet()) {
            current += lotterymap.get(itemStack);
            if (randomInt < current) {
                return itemStack.clone();
            }
        }
        return  Item.arrow.clone();
    }
}
