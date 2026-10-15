package com.mlc.mlcgames.party.core;

import java.util.*;

/** Records each finisher once; later disconnects cannot renumber earlier finishers. */
public final class FinishOrder<T> {
    private final LinkedHashMap<T, Integer> places = new LinkedHashMap<>();
    public int finish(T player) {
        if (places.containsKey(player)) return 0;
        int rank = places.size() + 1;
        places.put(player, rank);
        return rank;
    }
    public int count() { return places.size(); }
    public Map<T, Integer> places() { return Collections.unmodifiableMap(new LinkedHashMap<>(places)); }
}
