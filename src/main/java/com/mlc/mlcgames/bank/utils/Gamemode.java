package com.mlc.mlcgames.bank.utils;

public enum Gamemode {

    thiefvsthief("小偷vs小偷"),
    thiefvspolice("小偷vs警察");


    private final String chineseName;

    private Gamemode(String chineseName) {
        this.chineseName = chineseName;
    }


    public String getChineseName() {
        return chineseName;
    }
}
