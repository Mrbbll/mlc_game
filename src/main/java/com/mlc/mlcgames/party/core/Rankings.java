package com.mlc.mlcgames.party.core;

import java.util.*;

/** Dense ranking: tied scores share a rank, the next distinct score gets the next rank. */
public final class Rankings {
    public record Row<T>(T id, int score, int rank) {}
    public static <T> List<Row<T>> descending(Map<T, Integer> scores) {
        List<Map.Entry<T, Integer>> sorted = new ArrayList<>(scores.entrySet());
        sorted.sort(Map.Entry.<T, Integer>comparingByValue().reversed());
        List<Row<T>> result = new ArrayList<>();
        int rank = 0;
        Integer previous = null;
        for (Map.Entry<T, Integer> entry : sorted) {
            if (!Objects.equals(previous, entry.getValue())) rank++;
            result.add(new Row<>(entry.getKey(), entry.getValue(), rank));
            previous = entry.getValue();
        }
        return List.copyOf(result);
    }
}
