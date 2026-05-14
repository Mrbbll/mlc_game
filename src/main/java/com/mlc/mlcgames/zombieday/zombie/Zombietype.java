package com.mlc.mlcgames.zombieday.zombie;

public enum Zombietype {
    NORMAL,
    FAST,
    HIGHJUMP,
    POLICE,
    RICH;
    public String getType(){
        return name().toLowerCase();
    }
}
