package com.mlc.mlcgames.bank.utils;

import com.mlc.mlcgames.Teammanager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Shulker;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;

import static com.mlc.mlcgames.Mlcgames.*;
import static com.mlc.mlcgames.bank.utils.Fillutils.getreplaceblock;

public class Bankgameinit {
    public static void init(){
        //初始化银行游戏
        //先传送玩家到银行游戏区域
        //设置玩家游戏模式为冒险
        //清除玩家状态效果
        bankgame.remainTime = 0;
        bankgame.players = new ArrayList<>();
        bankgame.isStart = false;
        bankgame.gameprepared = false;
        bankgame.gamemode= Gamemode.thiefvsthief;
        bankgame.policescore = 0;
        bankgame.thiefscore = 0;
        bankgame.winnerteam = "";
        bankgame.playerJobs.clear();

        bankgame.ispowerbreak = false;
        bankgame.islockbreak = false;
        bankgame.islightbreak1 = false;
        bankgame.islightbreak2 = false;

        bankgame.bankgameLocation = fileConfiguration.getLocation("bankgame.bankgameLocation");
        if(bankgame.bankgameLocation == null){
            bankgame.bankgameLocation = new Location(instance.getServer().getWorld("world"),0,1,0);
        }
        fileConfiguration.set("bankgame.bankgameLocation",bankgame.bankgameLocation);

        bankgame.policeteamLocation = fileConfiguration.getLocation("bankgame.policeteamLocation");
        if(bankgame.policeteamLocation == null){
            bankgame.policeteamLocation = new Location(instance.getServer().getWorld("world"),0,1,0);
        }
        fileConfiguration.set("bankgame.policeteamLocation",bankgame.policeteamLocation);

        bankgame.thiefteamLocation = fileConfiguration.getLocation("bankgame.thiefteamLocation");
        if(bankgame.thiefteamLocation == null){
            bankgame.thiefteamLocation = new Location(instance.getServer().getWorld("world"),0,1,0);
        }
        fileConfiguration.set("bankgame.thiefteamLocation",bankgame.thiefteamLocation);

        bankgame.spectatelocation = fileConfiguration.getLocation("bankgame.spectatelocation");
        if(bankgame.spectatelocation == null){
            bankgame.spectatelocation = new Location(instance.getServer().getWorld("world"),0,1,0);
        }
        fileConfiguration.set("bankgame.spectatelocation",bankgame.spectatelocation);

        bankgame.lobbyLocation = fileConfiguration.getLocation("lobby");
        if(bankgame.lobbyLocation == null){
            bankgame.lobbyLocation = new Location(instance.getServer().getWorld("world"),0,1,0);
        }
        fileConfiguration.set("lobby",bankgame.lobbyLocation);


        bankgame.powerLocation1 = fileConfiguration.getLocation("bankgame.powerLocation1");
        if(bankgame.powerLocation1 == null){
            bankgame.powerLocation1 = new Location(instance.getServer().getWorld("world"),0,1,0);
        }
        fileConfiguration.set("bankgame.powerLocation1",bankgame.powerLocation1);

        bankgame.powerLocation2 = fileConfiguration.getLocation("bankgame.powerLocation2");
        if(bankgame.powerLocation2 == null){
            bankgame.powerLocation2 = new Location(instance.getServer().getWorld("world"),0,1,0);
        }
        fileConfiguration.set("bankgame.powerLocation2",bankgame.powerLocation2);

        bankgame.leaveLocation1 = fileConfiguration.getLocation("bankgame.leaveLocation1");
        if(bankgame.leaveLocation1 == null){
            bankgame.leaveLocation1 = new Location(instance.getServer().getWorld("world"),0,1,0);
        }
        fileConfiguration.set("bankgame.leaveLocation1", bankgame.leaveLocation1);

        bankgame.leaveLocation2 = fileConfiguration.getLocation("bankgame.leaveLocation2");
        if(bankgame.leaveLocation2 == null){
            bankgame.leaveLocation2 = new Location(instance.getServer().getWorld("world"),0,1,0);
        }
        fileConfiguration.set("bankgame.leaveLocation2", bankgame.leaveLocation2);

        bankgame.cornerLocation1 = fileConfiguration.getLocation("bankgame.cornerLocation1");
        if(bankgame.cornerLocation1 == null){
            bankgame.cornerLocation1 = new Location(instance.getServer().getWorld("world"),0,1,0);
        }
        fileConfiguration.set("bankgame.cornerLocation1", bankgame.cornerLocation1);

        bankgame.cornerLocation2 = fileConfiguration.getLocation("bankgame.cornerLocation2");
        if(bankgame.cornerLocation2 == null){
            bankgame.cornerLocation2 = new Location(instance.getServer().getWorld("world"),0,1,0);
        }
        fileConfiguration.set("bankgame.cornerLocation2", bankgame.cornerLocation2);


        bankgame.thiefteamLocation1 = fileConfiguration.getLocation("bankgame.thiefteamLocation1");
        if(bankgame.thiefteamLocation1 == null){
            bankgame.thiefteamLocation1 = new Location(instance.getServer().getWorld("world"),0,1,0);
        }
        fileConfiguration.set("bankgame.thiefteamLocation1", bankgame.thiefteamLocation1);

        bankgame.thiefteamLocation2 = fileConfiguration.getLocation("bankgame.thiefteamLocation2");
        if(bankgame.thiefteamLocation2 == null){
            bankgame.thiefteamLocation2 = new Location(instance.getServer().getWorld("world"),0,1,0);
        }
        fileConfiguration.set("bankgame.thiefteamLocation2", bankgame.thiefteamLocation2);

        //保存配置文件
        instance.saveConfig();

        //获取替换块
        Fillutils.getreplaceblock(bankgame.cornerLocation1, bankgame.cornerLocation2, Material.LIGHT, Material.CAVE_AIR);
    }


}
