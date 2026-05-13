package com.mlc.mlcgames.zombieday.zombie;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootTable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Random;

import static com.mlc.mlcgames.Mlcgames.instance;

public class ZombieLootTable implements LootTable {
    @Override
    public @NotNull Collection<ItemStack> populateLoot(@Nullable Random random, @NotNull LootContext context) {
        return List.of();
    }

    @Override
    public void fillInventory(@NotNull Inventory inventory, @Nullable Random random, @NotNull LootContext context) {

    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return new NamespacedKey(instance,"zombieloottable");
    }
}
