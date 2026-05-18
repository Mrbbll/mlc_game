package com.mlc.mlcgames.zombieday.zombie;

import com.mlc.mlcgames.zombieday.Item;
import com.mlc.mlcgames.zombieday.Zombiedaygame;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class ZombieLoot {
    private static final Map<String, List<LootEntry>> lootTable = new HashMap<>();

    public static void init(){
        ItemStack emerald = Item.emerald.clone();
        emerald.setAmount(Zombiedaygame.players.size()*2);

        addLoot(Zombietype.NORMAL.getType(),
            new LootEntry(emerald, 1),
            new LootEntry(new ItemStack(Material.FEATHER, Zombiedaygame.players.size()*2), 0.3)
        );

        addLoot(Zombietype.FAST.getType(),
            new LootEntry(emerald, 1),
            new LootEntry(new ItemStack(Material.FEATHER, Zombiedaygame.players.size()*6), 0.3)
        );
        addLoot(Zombietype.HIGHJUMP.getType(),
            new LootEntry(emerald, 1),
            new LootEntry(new ItemStack(Material.FEATHER, Zombiedaygame.players.size()*6), 0.3)
        );
        addLoot(Zombietype.POLICE.getType(),
            new LootEntry(emerald, 1),
                new LootEntry(Item.handgun,0.1),
                new LootEntry(Item.rifle,0.02),
                new LootEntry(Item.shotgun,0.02),
                new LootEntry(Item.submachine_gun,0.02),
                new LootEntry(Item.grenade,0.3)
        );
        addLoot(Zombietype.RICH.getType(),
            new LootEntry(emerald, 1),
            new LootEntry(emerald, 1),
            new LootEntry(emerald, 1)
        );
        addLoot(Zombietype.BOSS.getType(),
                new LootEntry(Item.key,1),
            new LootEntry(emerald, 1),
                new LootEntry(emerald, 1),
                new LootEntry(emerald, 1)
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