package com.mlc.mlcgames.bank.utils;

import com.mlc.mlcgames.Teammanager;
import com.mlc.mlcgames.bank.items.Bankgameloottable;
import com.mlc.mlcgames.bank.utils.end.Endgame;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.title.TitlePart;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Shulker;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Team;

import java.util.*;

import static com.mlc.mlcgames.Mlcgames.*;
import static com.mlc.mlcgames.bank.items.Bankgameitemmanager.golditem;

public class Bankgame {
    public Shulker pow1locentity;
    public Shulker pow2locentity;
    public Shulker outlocentity1;
    public Shulker outlocentity2;
    public Shulker goldlocentity;

    public int remainTime;
    public Gamemode gamemode;
    public List<Player> players;
    public boolean isStart;
    public boolean gameprepared;
    public int policescore;
    public int thiefscore;
    public String winnerteam;

    //等待或者中场大厅
    public Location bankgameLocation;
    public Location policeteamLocation;
    public Location thiefteamLocation;
    public Location spectatelocation;
    //大厅
    public Location lobbyLocation;

    //两个电站
    public Location powerLocation1;
    public Location powerLocation2;


    //游戏内实际逃离点，金库破坏后开
    public Location leaveLocation;

    //两个配置逃离点
    public Location leaveLocation1;
    public Location leaveLocation2;
    //用于填充替换光源
    public Location cornerLocation1;
    public Location cornerLocation2;

    //tvt模式下队伍
    public Location thiefteamLocation1;
    public Location thiefteamLocation2;

    public Map<Player, Jobs> playerJobs = new HashMap<>();
    public static BukkitTask gameendcountdown;
    public BukkitTask breaklockevent;
    public BukkitTask lightbreakevent;
    public BukkitTask lightfixevent;
    public BukkitTask effectgiveevent;
    public BukkitTask bringgoldoutevent;
    public int breaklocktime = 10;
    public int lightbreaktime1 = 10;
    public int lightbreaktime2 = 10;
    public int lightfixtime1 = 20;
    public int lightfixtime2 = 20;
    public int requirescore = 2000;

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
                if(!ispowerbreak){
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
                        if (Teammanager.getPlayerTeam(player).equals(Teammanager.bankgame_thiefteam) &&!player.hasPotionEffect(PotionEffectType.LUCK)) {

                            nearhavethief = true;
                            breaklocktime-= 1;
                            player.playSound(player,Sound.ENTITY_EXPERIENCE_ORB_PICKUP,2,2);
                            player.sendActionBar(Component.text("剩余时间：" + breaklocktime).color(TextColor.color(0xF4FF26)));
                            if (breaklocktime <= 0) {
                                //金库打开
                                Openlock(player);
                                islockbreak = true;
                                return;
                            }
                            break;
                        }
                    }
                }
                //附近没贼，重置倒计时
                if (!nearhavethief) {
                    breaklocktime = 10;
                }
            }
        }.runTaskTimer(instance, 0, 20);
    }


    private void Openlock( Player player) {
        //金库打开
        instance.getServer().broadcast(Component.text(">>> 金库被" + player.getName() + "打开").color(TextColor.color(0xFF0816)));
        player.getInventory().addItem(golditem);

        //效果给予
        bankgame.Effectgive();

        //随机出口
        Random random = new Random();
        int locationindex = random.nextInt(2);
        if(locationindex == 1){
            leaveLocation = leaveLocation2;
            outlocentity1.removePotionEffect(PotionEffectType.GLOWING);
            player.sendMessage(Component.text(">>> 你将从出口2离开").color(TextColor.color(0xF4FF26)));
        }else{
            leaveLocation = leaveLocation1;
            outlocentity2.removePotionEffect(PotionEffectType.GLOWING);
            player.sendMessage(Component.text(">>> 你将从出口1离开").color(TextColor.color(0xF4FF26)));
        }
        player.playSound(player,Sound.ENTITY_CHICKEN_HURT,1,1);
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
                            if(Teammanager.getPlayerTeam(player).equals(Teammanager.bankgame_thiefteam)&&!player.hasPotionEffect(PotionEffectType.LUCK)){
                                nearhavethief1 = true;
                                lightbreaktime1-= 1;
                                player.sendActionBar(Component.text("破坏剩余时间：" + lightbreaktime1).color(TextColor.color(0xF4FF26)));
                                if (lightbreaktime1 <= 0) {
                                    //破坏成功
                                    islightbreak1 = true;
                                    breaklightevent();
                                    bankgame.pow1locentity.removePotionEffect(PotionEffectType.GLOWING);
                                    player.sendMessage(Component.text("破坏成功").color(TextColor.color(0xFF00)));
                                    player.playSound(player,Sound.ENTITY_PLAYER_LEVELUP,1,1);
                                    lightbreaktime1 = 20;
                                    return;
                                }
                                player.playSound(player,Sound.ENTITY_EXPERIENCE_ORB_PICKUP,2,2);
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
                            if(Teammanager.getPlayerTeam(player).equals(Teammanager.bankgame_thiefteam) && !player.hasPotionEffect(PotionEffectType.LUCK)){
                                nearhavethief2 = true;
                                lightbreaktime2-= 1;
                                player.sendActionBar(Component.text("破坏剩余时间：" + lightbreaktime2).color(TextColor.color(0xF4FF26)));
                                if (lightbreaktime2 <= 0) {
                                    //破坏成功
                                    islightbreak2 = true;
                                    breaklightevent();
                                    bankgame.pow2locentity.removePotionEffect(PotionEffectType.GLOWING);
                                    player.sendMessage(Component.text("破坏成功").color(TextColor.color(0xFF00)));
                                    player.playSound(player,Sound.ENTITY_PLAYER_LEVELUP,1,1);
                                    lightbreaktime2 = 20;
                                    return;
                                }
                                player.playSound(player,Sound.ENTITY_EXPERIENCE_ORB_PICKUP,2,2);
                                break;
                            }
                        }
                    }
                    if(!nearhavethief2){
                        lightbreaktime2 = 20;
                    }
                }

            }
        }.runTaskTimer(instance, 0, 20);
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
                    Fillutils.replaceblock(Material.CAVE_AIR);

                    player1.sendTitlePart(TitlePart.TITLE,miniMessage.deserialize("<b><red><!>"));
                    player1.sendMessage(miniMessage.deserialize("<bold><red>>>> 某个电机被破坏了。。。"));
                    player1.playSound(player1.getLocation(), Sound.ENTITY_GOAT_SCREAMING_PREPARE_RAM,1,1);
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
                            if(Teammanager.getPlayerTeam(player).equals(Teammanager.bankgame_policeteam) && !player.hasPotionEffect(PotionEffectType.LUCK)){
                                nearhavepolice1 = true;
                                lightfixtime1-= 1;

                                player.sendActionBar(Component.text("修复剩余时间：" + lightfixtime1).color(TextColor.color(0xF4FF26)));
                                if (lightfixtime1 <= 0) {
                                    //修复成功
                                    islightbreak1 = false;
                                    fixlightevent();
                                    bankgame.pow1locentity.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING,99999*20,1,true));
                                    player.sendMessage(Component.text("修复成功").color(TextColor.color(0xFF00)));
                                    player.playSound(player,Sound.ENTITY_ALLAY_AMBIENT_WITH_ITEM,2,1);
                                    lightfixtime1 = 20;
                                    return;
                                }
                                player.playSound(player,Sound.ENTITY_EXPERIENCE_ORB_PICKUP,2,2);
                                break;
                            }
                        }
                    }
                    if(!nearhavepolice1){
                        lightfixtime1 = 20;
                    }
                }
                if(islightbreak2){
                    Collection<Entity> entity = location2.getNearbyEntities(1, 0.5, 1);
                    for(Entity ent : entity){
                        if(ent instanceof Player){
                            Player player = (Player) ent;
                            if(Teammanager.getPlayerTeam(player).equals(Teammanager.bankgame_policeteam) && !player.hasPotionEffect(PotionEffectType.LUCK)){
                                nearhavepolice2 = true;
                                lightfixtime2-= 1;
                                player.sendActionBar(Component.text("修复剩余时间：" + lightfixtime2).color(TextColor.color(0xF4FF26)));

                                if (lightfixtime2 <= 0) {
                                    //修复成功
                                    islightbreak2 = false;
                                    fixlightevent();
                                    bankgame.pow2locentity.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING,99999*20,1,true));
                                    player.sendMessage(Component.text("修复成功").color(TextColor.color(0xFF00)));
                                    player.playSound(player,Sound.ENTITY_ALLAY_AMBIENT_WITH_ITEM,2,1);
                                    lightfixtime2 = 20;
                                    return;
                                }
                                player.playSound(player,Sound.ENTITY_EXPERIENCE_ORB_PICKUP,2,2);
                                break;
                            }
                        }
                    }
                    if(!nearhavepolice2){
                        lightfixtime2 = 20;
                    }
                };
            }
        }.runTaskTimer(instance, 0, 20);
    }

    private void fixlightevent() {
        ispowerbreak = false;
        //两个都修复了
        if(!islightbreak1&&!islightbreak2){
            //给警队正常效果，开灯
            Teammanager.bankgame_policeteam.getEntries().forEach(player -> {
                Player player1 = Bukkit.getPlayer(player);
                if (player1 != null) {
                    player1.sendMessage(miniMessage.deserialize("<bold><green>>>> 两个电机都被修复了"));
                    Fillutils.replaceblock(Material.LIGHT);
                    player1.removePotionEffect(PotionEffectType.DARKNESS);
                }

            });
        }else{
            bankgame.players.forEach(player -> {
                if (player != null) {
                    player.sendMessage(miniMessage.deserialize("<bold><green>>>> 某个电机被修复了"));
                }
            });
        };

    }

    public void Effectgive() {
        for(Player player : Teammanager.getteamplayer(Teammanager.bankgame_policeteam)){
            player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE,999*20,1,true,true));
            player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION,999*20,1,true,true));
            player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH,999*20,0,true,true));
            player.sendTitlePart(TitlePart.TITLE,miniMessage.deserialize("<b><red>💲!!!"));
        }

        effectgiveevent = new BukkitRunnable() {
            @Override
            public void run() {
                if(!isStart){
                    cancel();
                };
                for(String player : Teammanager.bankgame_thiefteam.getEntries()){
                    Player player1 = Bukkit.getPlayer(player);
                    if(player1 != null){
                        player1.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 20, 0));

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
                        Team team = Teammanager.getPlayerTeam(player);
                        if(team.equals(Teammanager.bankgame_thiefteam)&&player.getInventory().contains(golditem)){
                            //结束游戏
                            bankgame.thiefscore += 9000;
                            Endgame.endgame();
                            this.cancel();
                        }else if(team.equals(Teammanager.bankgame_thiefteam)){
                            for ( ItemStack item : player.getInventory().getContents()){
                                if(item == null){
                                    continue;
                                }
                                if (item.getType().equals(Material.DIAMOND)) {
                                    int count = item.getAmount();
                                    bankgame.thiefscore += count*50;
                                    player.getInventory().remove(item);
                                    instance.getServer().broadcast(miniMessage.deserialize("<bold><gold>>>> <head:"+player.getName()+">带回了"+count+"个钻石"));
                                } else if (item.getType().equals(Material.EMERALD)) {
                                    int count = item.getAmount();
                                    bankgame.thiefscore += count*100;
                                    player.getInventory().remove(item);
                                    instance.getServer().broadcast(miniMessage.deserialize("<bold><gold>>>> <head:"+player.getName()+">带回了"+count+"个绿宝石"));

                                }
                            }
                        }
                    }
                    if(bankgame.thiefscore>=2000){
                        Endgame.endgame();
                    }

                }
            }
        }.runTaskTimer(instance, 0, 10);
    }
}


