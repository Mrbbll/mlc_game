package com.mlc.mlcgames.zombieday;

public enum Difficuty {
    NORMAL("<#7f8182>"),
    HARD("<#3bd4ff>"),
    INSANE("<#ff8317>"),
    TORMENT("<#ff0033>");

    private final String color;

    Difficuty(String color) {
        this.color = color;
    }

    public String withcolor() {
        return color + name();
    }
}
