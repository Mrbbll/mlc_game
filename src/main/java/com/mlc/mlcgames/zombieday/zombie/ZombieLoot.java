package com.mlc.mlcgames.zombieday.zombie;

import com.mlc.mlcgames.zombieday.Item;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class ZombieLoot {
    private static final Map<String, List<LootEntry>> lootTable = new HashMap<>();

    static {

        addLoot(Zombietype.NORMAL.getType(),
            new LootEntry(Item.emerald, 0.8),
            new LootEntry(Item.emerald, 0.8),
            new LootEntry(new ItemStack(Material.FEATHER, 1), 0.3)
        );

        addLoot(Zombietype.FAST.getType(),
            new LootEntry(Item.emerald, 1),
            new LootEntry(new ItemStack(Material.FEATHER, 5), 0.3)
        );
        addLoot(Zombietype.HIGHJUMP.getType(),
            new LootEntry(Item.emerald, 1),
            new LootEntry(new ItemStack(Material.FEATHER, 5), 0.3)
        );
        addLoot(Zombietype.POLICE.getType(),
            new LootEntry(Item.emerald, 1),
                new LootEntry(Item.handgun,0.1),
                new LootEntry(Item.rifle,0.02),
                new LootEntry(Item.shotgun,0.02),
                new LootEntry(Item.submachine_gun,0.02),
                new LootEntry(Item.grenade,0.5)
        );
        addLoot(Zombietype.RICH.getType(),
            new LootEntry(Item.emerald, 1),
            new LootEntry(Item.emerald, 1),
            new LootEntry(Item.emerald, 1)
        );
    }

    private static void addLoot(String type, LootEntry... entries) {
        lootTable.put(type, Arrays.asList(entries));
    }

    public static List<ItemStack> generateLoot(String type) {
        List<LootEntry> entries = lootTable.get(type);
        if (entries == null) return Collections.emptyList();

        List<ItemStack> drops = new ArrayList<>();
        for (LootEntry entry : entries) {
            if (Math.random() < entry.chance) {
                drops.add(entry.item.clone());
            }
        }
        return drops;
    }


    private static class LootEntry {
        final ItemStack item;
        final double chance;

        LootEntry(ItemStack item, double chance) {
            this.item = item;
            this.chance = chance;
        }
    }
}