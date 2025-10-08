package com.mlc.mlcgames.bank;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

public class Bank_bossbar {
    public final BossBar bossBar;
    float progress = 1.0f;
    public Bank_bossbar(){
        bossBar = BossBar.bossBar(Component.text(""),progress, BossBar.Color.WHITE,BossBar.Overlay.NOTCHED_6);
    }

    public void init(){
        bossBar.progress(progress);
    }


}
