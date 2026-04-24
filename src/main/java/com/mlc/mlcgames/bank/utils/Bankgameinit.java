package com.mlc.mlcgames.bank.utils;

import org.bukkit.Location;

import java.util.ArrayList;

import static com.mlc.mlcgames.Mlcgames.*;

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

        //保存配置文件
        instance.saveConfig();
    }
}
