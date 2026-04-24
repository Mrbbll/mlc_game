package com.mlc.mlcgames.bank.utils;

import com.mlc.mlcgames.Teammanager;
import com.mlc.mlcgames.bank.utils.end.Endgame;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

import static com.mlc.mlcgames.Mlcgames.bankgame;
import static com.mlc.mlcgames.Mlcgames.instance;
import static com.mlc.mlcgames.bank.items.Bankgameitemmanager.golditem;

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
    public Location powerLocation1;
    public Location powerLocation2;
    public Location leaveLocation;
    public Location leaveLocation1;
    public Location leaveLocation2;

    public Map<Player, Jobs> playerJobs = new HashMap<>();
    public static BukkitTask gameendcountdown;
    public BukkitTask breaklockevent;
    public BukkitTask lightbreakevent;
    public BukkitTask lightfixevent;
    public BukkitTask effectgiveevent;
    public BukkitTask bringgoldoutevent;
    public int breaklocktime = 40;
    public int lightbreaktime1 = 20;
    public int lightbreaktime2 = 20;
    public int lightfixtime1 = 20;
    public int lightfixtime2 = 20;

    public boolean islockbreak = false;
    public boolean islightbreak1 = false;
    public boolean islightbreak2 = false;
    public boolean ispowerbreak = false;





    public void Openlocklistener(){
        //金库破坏事件
        breaklockevent = new BukkitRunnable() {
            @Override
            public void run() {
                if(islockbreak){
                    return;
                }
                if(!isStart){
                    this.cancel();
                }
                boolean nearhavethief = false;
                Location location = bankgame.spectatelocation;
                Collection<Entity> entity = location.getNearbyEntities(1, 0.5, 1);
                for (Entity ent : entity) {
                    if (ent instanceof Player) {
                        Player player = (Player) ent;
                        //如果是贼，在20秒后金库打开
                        if (Teammanager.getPlayerTeam(player).equals(Teammanager.bankgame_thiefteam)) {

                            nearhavethief = true;
                            //没电打开速度翻倍
                            if(ispowerbreak){
                                breaklocktime-= 2;
                            }else{
                                breaklocktime-= 1;
                            }

                            player.sendMessage(Component.text("剩余时间：" + breaklocktime).color(TextColor.color(0xF4FF26)));
                            if (breaklocktime <= 0) {
                                //金库打开
                                Openlock(player);
                                islockbreak = true;
                            }
                            break;
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


    private void Openlock( Player player) {
        //金库打开
        instance.getServer().broadcast(Component.text("金库被" + player.getName() + "打开").color(TextColor.color(0xFF0816)));
        player.getInventory().addItem(golditem);

        //效果给予
        bankgame.Effectgive();

        //随机出口
        Random random = new Random();
        int locationindex = random.nextInt(2);
        if(locationindex == 1){
            leaveLocation = leaveLocation2;
            player.sendMessage(Component.text("你将从出口2离开").color(TextColor.color(0xF4FF26)));
        }else{
            leaveLocation = leaveLocation1;
            player.sendMessage(Component.text("你将从出口1离开").color(TextColor.color(0xF4FF26)));
        }
        //带金条离开事件
        bankgame.Bringgoldoutevent();


    }

    public void Lightningbreaklistener() {
        lightbreakevent = new BukkitRunnable() {
            @Override
            public void run() {
                if(!isStart){
                    this.cancel();
                }
                boolean nearhavethief1 = false;
                boolean nearhavethief2 = false;

                Location location1 = powerLocation1;
                Location location2 = powerLocation2;
                if(!islightbreak1){
                    Collection<Entity> entity = location1.getNearbyEntities(1, 0.5, 1);
                    for(Entity ent : entity){
                        if(ent instanceof Player){
                            Player player = (Player) ent;
                            if(Teammanager.getPlayerTeam(player).equals(Teammanager.bankgame_thiefteam)){
                                nearhavethief1 = true;
                                lightbreaktime1-= 1;
                                player.sendMessage(Component.text("破坏剩余时间：" + lightbreaktime1).color(TextColor.color(0xF4FF26)));
                                if (lightbreaktime1 <= 0) {
                                    //破坏成功
                                    islightbreak1 = true;
                                    breaklightevent();
                                    player.sendMessage(Component.text("破坏成功").color(TextColor.color(0xFF00)));
                                }
                                break;
                            }
                        }
                    }
                    if(!nearhavethief1){
                        lightbreaktime1 = 20;
                    }
                }
                if(!islightbreak2){
                    Collection<Entity> entity = location2.getNearbyEntities(1, 0.5, 1);
                    for(Entity ent : entity){
                        if(ent instanceof Player){
                            Player player = (Player) ent;
                            if(Teammanager.getPlayerTeam(player).equals(Teammanager.bankgame_thiefteam)){
                                nearhavethief2 = true;
                                lightbreaktime2-= 1;
                                player.sendMessage(Component.text("破坏剩余时间：" + lightbreaktime2).color(TextColor.color(0xF4FF26)));
                                if (lightbreaktime2 <= 0) {
                                    //破坏成功
                                    islightbreak2 = true;
                                    breaklightevent();
                                    player.sendMessage(Component.text("破坏成功").color(TextColor.color(0xFF00)));
                                }
                                break;
                            }
                        }
                    }
                    if(!nearhavethief2){
                        lightbreaktime2 = 20;
                    }
                }

            }
        }.runTaskTimer(instance, 0, 10);
    }

    private void breaklightevent() {

        //两个都破坏成功情况
        if(islightbreak1&&islightbreak2){
            //电停
            ispowerbreak = true;

        }
        //有一个破坏成功
        else if (islightbreak1||islightbreak2){
            //给警队黑暗效果，关灯
            Teammanager.bankgame_policeteam.getEntries().forEach(player -> {
                Player player1 = Bukkit.getPlayer(player);
                if (player1 != null) {
                    player1.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 999999, 1));
                    player1.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 999999, 1));
                }

            });
        }
    }

    public void Lightningfixlistener() {
        lightfixevent = new BukkitRunnable() {
            @Override
            public void run() {
                if(!isStart){
                    this.cancel();
                }
                boolean nearhavepolice1 = false;
                boolean nearhavepolice2 = false;

                Location location1 = powerLocation1;
                Location location2 = powerLocation2;
                if(islightbreak1){
                    Collection<Entity> entity = location1.getNearbyEntities(1, 0.5, 1);
                    for(Entity ent : entity){
                        if(ent instanceof Player){
                            Player player = (Player) ent;
                            if(Teammanager.getPlayerTeam(player).equals(Teammanager.bankgame_policeteam)){
                                nearhavepolice1 = true;
                                lightfixtime1-= 2;
                                player.sendMessage(Component.text("修复剩余时间：" + lightfixtime1).color(TextColor.color(0xF4FF26)));
                                if (lightfixtime1 <= 0) {
                                    //修复成功
                                    islightbreak1 = false;
                                    fixlightevent();
                                    player.sendMessage(Component.text("修复成功").color(TextColor.color(0xFF00)));
                                }
                                break;
                            }
                        }
                    }
                    if(!nearhavepolice1){
                        lightfixtime1 = 20;
                    }
                };

                if(islightbreak2){
                    Collection<Entity> entity = location2.getNearbyEntities(1, 0.5, 1);
                    for(Entity ent : entity){
                        if(ent instanceof Player){
                            Player player = (Player) ent;
                            if(Teammanager.getPlayerTeam(player).equals(Teammanager.bankgame_policeteam)){
                                nearhavepolice2 = true;
                                lightfixtime2-= 2;
                                player.sendMessage(Component.text("修复剩余时间：" + lightfixtime2).color(TextColor.color(0xF4FF26)));
                                if (lightfixtime2 <= 0) {
                                    //修复成功
                                    islightbreak2 = false;
                                    fixlightevent();
                                    player.sendMessage(Component.text("修复成功").color(TextColor.color(0xFF00)));
                                }
                                break;
                            }
                        }
                    }
                    if(!nearhavepolice2){
                        lightfixtime2 = 20;
                    }
                };
            }
        }.runTaskTimer(instance, 0, 10);
    }

    private void fixlightevent() {
        ispowerbreak = false;
        //两个都修复了
        if(!islightbreak1&&!islightbreak2){
            //给警队正常效果，开灯
            Teammanager.bankgame_policeteam.getEntries().forEach(player -> {
                Player player1 = Bukkit.getPlayer(player);
                if (player1 != null) {
                    player1.removePotionEffect(PotionEffectType.BLINDNESS);
                    player1.removePotionEffect(PotionEffectType.DARKNESS);
                }

            });
        }
    }

    public void Effectgive() {
        effectgiveevent = new BukkitRunnable() {
            @Override
            public void run() {
                if(!isStart){
                    cancel();
                };
                for(String player : Teammanager.bankgame_thiefteam.getEntries()){
                    Player player1 = Bukkit.getPlayer(player);
                    if(player1 != null){
                        player1.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 1, 0));
                    }
                }
            }
        }.runTaskTimer(instance, 0, 10);
    }

    public void Bringgoldoutevent() {
        bringgoldoutevent = new BukkitRunnable() {
            @Override
            public void run() {
                if(!isStart){
                    this.cancel();
                };
                Location location = leaveLocation;

                Collection<Entity> entity = location.getNearbyEntities(1, 0.5, 1);
                for(Entity ent : entity){
                    if(ent instanceof Player){
                        Player player = (Player) ent;
                        if(Teammanager.getPlayerTeam(player).equals(Teammanager.bankgame_thiefteam)&&player.getInventory().contains(golditem)){
                            //结束游戏
                            bankgame.thiefscore += 9000;
                            Endgame.endgame();
                            this.cancel();
                        }
                    }
                }
            }
        }.runTaskTimer(instance, 0, 10);
    }
}


