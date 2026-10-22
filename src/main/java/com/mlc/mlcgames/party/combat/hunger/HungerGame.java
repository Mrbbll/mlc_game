package com.mlc.mlcgames.party.combat.hunger;

import com.mlc.mlcgames.party.combat.common.ChestSurvival;
import com.mlc.mlcgames.party.core.*;
import org.bukkit.*;
import org.bukkit.entity.Player;
import java.util.*;

/** Sixty-second grace, two one-minute refills, then a five-second deathmatch countdown. */
public final class HungerGame extends ChestSurvival {
    private int phase,next;
    @Override public boolean lastPlayerWins(){return false;}
    @Override public void validate(PartyConfig c,PartyConfig.Arena a){validateLoot(c,a);a.group("chests");if(a.group("deathmatch").size()<c.integer("players.max",2,64))throw new IllegalArgumentException("deathmatch点位至少等于players.max");c.integer("grace-seconds",1,300);c.integer("refill-seconds",1,600);c.integer("deathmatch-seconds",10,600);}
    @Override public void start(PartySession s){for(Player p:s.survivors())p.setGameMode(GameMode.ADVENTURE);refill(s,"chests","hunger/chest",Material.BARREL);phase=0;next=s.elapsed()+s.config.integer("grace-seconds",1,300)*20;}
    @Override protected boolean pvp(PartySession s){return phase>=1&&phase!=4;}
    @Override public void tick(PartySession s,int elapsed){
        if(s.survivors().size()<=1){awardSurvivors(s,3);return;}
        if(elapsed>=next){
            switch(phase){
                case 0->{phase=1;next=elapsed+s.config.integer("refill-seconds",1,600)*20;s.broadcast("pvp");}
                case 1,2->{phase++;refill(s,"chests","hunger/chest",Material.BARREL);next=elapsed+s.config.integer("refill-seconds",1,600)*20;}
                case 3->{phase=4;next=elapsed+100;int i=0;for(Player p:s.survivors())p.teleport(s.arena.group("deathmatch").get(i++));s.broadcast("deathmatch-ready");}
                case 4->{phase=5;next=elapsed+s.config.integer("deathmatch-seconds",10,600)*20;s.broadcast("deathmatch");}
                default->awardSurvivors(s,3);
            }
        }
        if(elapsed%20==0)for(Player p:s.survivors())p.sendActionBar(s.config.message("status","seconds",Math.max(0,(next-elapsed)/20),"phase",s.config.text("phases."+phase)));
    }
    @Override public void timeout(PartySession s){awardSurvivors(s,3);}
}
