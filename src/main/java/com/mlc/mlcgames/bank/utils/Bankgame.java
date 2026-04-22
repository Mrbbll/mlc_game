package com.mlc.mlcgames.bank.utils;

import com.mlc.mlcgames.Teammanager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Location;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.mlc.mlcgames.Mlcgames.bankgame;
import static com.mlc.mlcgames.Mlcgames.instance;

public class Bankgame {
    public int remainTime;
    public Gamemode gamemode;
    public List<Player> players;
    public boolean isStart;
    public boolean gameprepared;
    public int policescore;
    public int thiefscore;
    public String winnerteam;

    public Location bankgameLocation;
    public Location policeteamLocation;
    public Location thiefteamLocation;
    public Location spectatelocation;
    public Location lobbyLocation;

    public Map<Player, Jobs> playerJobs = new HashMap<>();
    public static BukkitTask gameendcountdown;
    public static boolean isBreaklock;
    public static BukkitTask breaklockevent;
    private int breaklocktime = 40;





    public void Openlocklistener(){
        //金库破坏事件
        breaklockevent = new BukkitRunnable() {
            @Override
            public void run() {
                boolean nearhavethief = false;

                Location location = bankgame.spectatelocation;
                Collection<Entity> entity = location.getNearbyEntities(1, 0, 1);
                for (Entity ent : entity) {
                    if (ent instanceof Player) {
                        Player player = (Player) ent;
                        //如果是贼，在20秒后金库打开
                        if (Teammanager.getPlayerTeam(player).equals(Teammanager.bankgame_thiefteam)) {
                            nearhavethief = true;
                            breaklocktime-= 1;
                            if (breaklocktime <= 0) {
                                //金库打开
                                Openlock(bankgame.spectatelocation, player);
                            }
                        }
                    }
                }
                //附近没贼，重置倒计时
                if (!nearhavethief) {
                    breaklocktime = 40;
                }
            }
        }.runTaskTimer(instance, 0, 10);
    }


    private static void Openlock(Location spectatelocation, Player player) {
        //金库打开
        for(Player p : bankgame.players){
            p.sendMessage(Component.text("金库被" + player.getName() + "打开").color(TextColor.color(0xFF0816)));
        }
    }

    public void Lightningbreaklistener() {
    }
}


