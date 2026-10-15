package com.mlc.mlcgames.party.core;

import java.util.*;

/** Piecewise-constant rules, used for the source's player-count thresholds. */
public final class ThresholdTable {
    public record Entry(int minPlayers, int value) {}
    private final List<Entry> entries;
    public ThresholdTable(List<Entry> source) {
        if (source.isEmpty()) throw new IllegalArgumentException("人数阈值表不能为空");
        entries = source.stream().sorted(Comparator.comparingInt(Entry::minPlayers)).toList();
        Set<Integer> seen = new HashSet<>();
        for (Entry entry : entries) {
            if (entry.minPlayers < 2 || entry.minPlayers > 64 || entry.value < 1 || !seen.add(entry.minPlayers)) throw new IllegalArgumentException("人数阈值必须唯一，范围 2..64，数值须大于 0");
        }
        if (entries.getFirst().minPlayers != 2) throw new IllegalArgumentException("人数阈值表必须包含 2 人的基础规则");
    }
    public int value(int players) {
        if (players < 2 || players > 64) throw new IllegalArgumentException("人数必须为 2..64");
        int value = entries.getFirst().value;
        for (Entry entry : entries) { if (players < entry.minPlayers) break; value = entry.value; }
        return value;
    }
}
