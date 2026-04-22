package com.mlc.mlcgames.bank.listener.clickprocess;

import com.mlc.mlcgames.Teammanager;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import static com.mlc.mlcgames.Mlcgames.bankgame;
import static com.mlc.mlcgames.Mlcgames.instance;

public class Teamselect {
        public static void selectteam(ItemStack itemStack, Player player){
            //如果玩家点击的物品不是混凝土，返回
            if(!(itemStack.getType() == Material.RED_CONCRETE ||
                    itemStack.getType() == Material.LIGHT_BLUE_CONCRETE||
                    itemStack.getType() == Material.WHITE_CONCRETE||
                    itemStack.getType() == Material.NETHERITE_SWORD)){
                return;
            }
            else if(itemStack.getType() == Material.RED_CONCRETE){
                player.sendMessage("你选择了红队");
                Teammanager.addPlayerToTeam(Teammanager.bankgame_thiefteam, player);
            }else if(itemStack.getType() == Material.LIGHT_BLUE_CONCRETE){
                player.sendMessage("你选择了蓝队");
                Teammanager.addPlayerToTeam(Teammanager.bankgame_policeteam, player);
            } else if (itemStack.getType() == Material.WHITE_CONCRETE) {
                player.sendMessage("你选择了旁观队伍");
                Teammanager.addPlayerToTeam(Teammanager.bankgame_spectateteam, player);
            } else if(itemStack.getType() == Material.NETHERITE_SWORD){
                player.sendMessage("检查准备");
                checkgameprepared();
            }
            player.closeInventory();
        }

    private static void checkgameprepared() {
            if(bankgame.players.size() >= 2 && !Teammanager.bankgame_thiefteam.getEntries().isEmpty() && !Teammanager.bankgame_policeteam.getEntries().isEmpty()){
                bankgame.gameprepared = true;
                instance.getServer().broadcast(Component.text("玩家数量足够，游戏准备就绪"));
                instance.getServer().broadcast(Component.text("再次右键菜单打开职业选择菜单"));
            }else {
                instance.getServer().broadcast(Component.text("玩家数量不足，无法开始游戏"));
            }
    }
}
