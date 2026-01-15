package com.mlc.mlcgames.bank.utils;

import java.util.ArrayList;

import static com.mlc.mlcgames.Mlcgames.bankgame;

public class Bankgameinit {
    public static void init(){
        //初始化银行游戏
        //先传送玩家到银行游戏区域
        //设置玩家游戏模式为冒险
        //清除玩家状态效果
        bankgame.remainTime = 0;
        bankgame.players = new ArrayList<>();
        bankgame.isStart = false;
        bankgame.winnerteam = "";
        bankgame.playerJobs.clear();
    }
}
