package com.mlc.mlcgames.bank.utils;

import static com.mlc.mlcgames.Mlcgames.bankgame;

public enum Jobs {
    None(" "),
    sword("\uD83D\uDDE1"),
    bow("\uD83C\uDFF9"),
    woodenaxe("\uD83E\uDE93"),
    crossbow("☀"),
    wolfspawnegg("\uD83D\uDEE1");

    private final String emoji;

    private Jobs(String emoji) {
        this.emoji = emoji;
    }

    public String getEmoji() {
        return emoji;
    }
}
