package com.mlc.mlcgames.zombieday.managers;

import com.mlc.mlcgames.zombieday.Item;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static com.mlc.mlcgames.Mlcgames.miniMessage;

public class Lottery {
    public static Map<ItemStack,Integer> lotterymap = new HashMap<>();
    public static int total = 0;
    static {
        lotterymap.put(Item.handgun.clone(),4);
        lotterymap.put(Item.shotgun.clone(),2);
        lotterymap.put(Item.submachine_gun.clone(),2);
        lotterymap.put(Item.rifle.clone(),2);
        lotterymap.put(Item.grenade.clone(),2);
        ItemStack bullet = Item.bullet.clone();
        bullet.setAmount(64);
        lotterymap.put(bullet,20);
        lotterymap.put(ItemStack.of(Material.GOLDEN_APPLE,5),1);
        lotterymap.put(ItemStack.of(Material.IRON_GOLEM_SPAWN_EGG),5);
        lotterymap.put(ItemStack.of(Material.IRON_GOLEM_SPAWN_EGG,2),1);
        lotterymap.put(ItemStack.of(Material.WOLF_SPAWN_EGG,3),10);
        lotterymap.put(ItemStack.of(Material.WOLF_SPAWN_EGG,5),5);
        lotterymap.put(ItemStack.of(Material.GOLDEN_CARROT,32),10);
        ItemStack opcrossbow = ItemStack.of(Material.CROSSBOW);

        ItemMeta opcrossbowMeta = opcrossbow.getItemMeta();
        opcrossbowMeta.setUnbreakable(true);
        opcrossbowMeta.customName(miniMessage.deserialize("<!i>神弩"));
        opcrossbowMeta.addEnchant(Enchantment.QUICK_CHARGE,4,true);
        opcrossbowMeta.addEnchant(Enchantment.PIERCING,3,true);
        opcrossbowMeta.addEnchant(Enchantment.MULTISHOT,2,true);
        opcrossbow.setItemMeta(opcrossbowMeta);
        lotterymap.put(opcrossbow,1);

        ItemStack opcrossbow1 = ItemStack.of(Material.CROSSBOW);
        ItemMeta opcrossbowMeta1 = opcrossbow1.getItemMeta();
        opcrossbowMeta1.setUnbreakable(true);
        opcrossbowMeta1.customName(miniMessage.deserialize("<!i>半神弩"));
        opcrossbowMeta1.addEnchant(Enchantment.QUICK_CHARGE,3,true);
        opcrossbowMeta1.addEnchant(Enchantment.PIERCING,3,true);
        opcrossbow1.setItemMeta(opcrossbowMeta1);
        lotterymap.put(opcrossbow1,2);

        lotterymap.put(Item.bazooka.clone(),2);
        ItemStack howitzer = Item.howitzer.clone();
        howitzer.setAmount(8);
        lotterymap.put(howitzer,4);

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
