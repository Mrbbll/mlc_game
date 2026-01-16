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

        //保存配置文件
        instance.saveConfig();
    }
}
