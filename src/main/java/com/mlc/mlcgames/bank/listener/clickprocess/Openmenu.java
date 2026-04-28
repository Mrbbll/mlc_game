package com.mlc.mlcgames.bank.listener.clickprocess;

import com.mlc.mlcgames.Teammanager;
import com.mlc.mlcgames.bank.menus.bankmenus;
import com.mlc.mlcgames.bank.utils.Gamemode;
import org.bukkit.entity.Player;

import static com.mlc.mlcgames.Mlcgames.bankgame;

public class Openmenu {
        public static void Openbankmenu(Player player, Gamemode gamemode){
            //如果游戏开始，返回
            if(bankgame.isStart){
                return;
            }
            //根据玩家所处游戏阶段和队伍打开对应的菜单
            switch (gamemode){
                case thiefvsthief:
                    if( Teammanager.isPlayerInTeam(player, Teammanager.bankgame_policeteam)&&bankgame.gameprepared){
                        player.openInventory(bankmenus.thiefmenu);
                    } else if( Teammanager.isPlayerInTeam(player, Teammanager.bankgame_thiefteam)&&bankgame.gameprepared){
                        player.openInventory(bankmenus.thiefmenu);
                    } else if( Teammanager.isPlayerInTeam(player, Teammanager.bankgame_spectateteam)&&!bankgame.gameprepared){
                        player.openInventory(bankmenus.bankmenu);
                        return;
                    } else if(!bankgame.gameprepared){
                        //如果玩家不在队伍中返回
                        player.openInventory(bankmenus.bankmenu);
                        return;
                    }
                    break;
                case thiefvspolice:
                    if( Teammanager.isPlayerInTeam(player, Teammanager.bankgame_policeteam)&&bankgame.gameprepared){
                        player.openInventory(bankmenus.policemenu);
                    } else if( Teammanager.isPlayerInTeam(player, Teammanager.bankgame_thiefteam)&&bankgame.gameprepared){
                        player.openInventory(bankmenus.thiefmenu);
                    } else if( Teammanager.isPlayerInTeam(player, Teammanager.bankgame_spectateteam)&&!bankgame.gameprepared){
                        player.openInventory(bankmenus.bankmenu);
                        return;
                    } else if(!bankgame.gameprepared){
                        //如果玩家不在队伍中返回
                        player.openInventory(bankmenus.bankmenu);
                        return;
                    }
                    break;
            }
        }
}
