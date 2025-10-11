package com.mlc.mlcgames.bank;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import static com.mlc.mlcgames.Gameinit.bank_lobby;
import static com.mlc.mlcgames.Mlcgames.ingamepalyer;
import static com.mlc.mlcgames.Mlcgames.Bank_isstart;

public class Bank_gameend {
    public Bank_gameend(){
        Bank_isstart=false;
    }
    public void endgame(BossBar bossBar){
        for(Player player:ingamepalyer){
            player.teleport(bank_lobby);
            player.hideBossBar(bossBar);
        }
        Bukkit.broadcast(Component.text("game over"));
        ingamepalyer.clear();
    }
}
