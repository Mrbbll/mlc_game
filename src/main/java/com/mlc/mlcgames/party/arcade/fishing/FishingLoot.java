package com.mlc.mlcgames.party.arcade.fishing;

import java.util.*;
import java.util.random.RandomGenerator;

/** Conditions are evaluated before weighted selection, as approved for the old source loot fields. */
public final class FishingLoot {
    public record Catch(String material, int coins) {
        public Catch { if (material == null || coins < 0 || coins > 1000) throw new IllegalArgumentException("fish reward"); }
    }
    public record Entry(Catch reward, int weight, boolean jungleOnly) {
        public Entry { Objects.requireNonNull(reward); if (weight < 1 || weight > 10000) throw new IllegalArgumentException("loot weight"); }
    }
    public record Pool(int weight, List<Entry> entries) {
        public Pool {
            if (weight < 1 || weight > 10000) throw new IllegalArgumentException("pool weight");
            entries = List.copyOf(entries);
            if (entries.isEmpty() || entries.size() > 100 || entries.stream().allMatch(Entry::jungleOnly)) throw new IllegalArgumentException("loot pool requires an unconditional entry");
        }
    }
    private final Pool fish;
    private final Pool junk;
    private final Pool treasure;
    private final double treasureChance;
    public FishingLoot(Pool fish, Pool junk, Pool treasure, double treasureChance) {
        this.fish = Objects.requireNonNull(fish);
        this.junk = Objects.requireNonNull(junk);
        this.treasure = Objects.requireNonNull(treasure);
        if (!Double.isFinite(treasureChance) || treasureChance < 0 || treasureChance > 1) throw new IllegalArgumentException("treasure chance");
        this.treasureChance = treasureChance;
    }
    public Catch draw(RandomGenerator random, boolean jungle) {
        boolean eligibleTreasure = random.nextDouble() < treasureChance;
        int group = random.nextInt(fish.weight + junk.weight + (eligibleTreasure ? treasure.weight : 0));
        Pool pool = group < junk.weight ? junk : group < junk.weight + (eligibleTreasure ? treasure.weight : 0) ? treasure : fish;
        int total = pool.entries.stream().filter(e -> jungle || !e.jungleOnly).mapToInt(Entry::weight).sum();
        int ticket = random.nextInt(total);
        for (Entry entry : pool.entries) {
            if (entry.jungleOnly && !jungle) continue;
            ticket -= entry.weight;
            if (ticket < 0) return entry.reward;
        }
        throw new IllegalStateException("loot selection");
    }
}
