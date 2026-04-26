package com.mlc.mlcgames.bank.listener.clickprocess;

import com.mlc.mlcgames.Teammanager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import static com.mlc.mlcgames.Mlcgames.*;

public class Teamselect {
        public static void selectteam(ItemStack itemStack, Player player){
            if(bankgame.gameprepared){
                player.closeInventory();
                return;
            }
            //如果玩家点击的物品不是混凝土，返回
            if(!(itemStack.getType() == Material.RED_CONCRETE ||
                    itemStack.getType() == Material.LIGHT_BLUE_CONCRETE||
                    itemStack.getType() == Material.WHITE_CONCRETE||
                    itemStack.getType() == Material.NETHERITE_SWORD)){
                return;
            }
            else if(itemStack.getType() == Material.RED_CONCRETE){
                player.sendMessage(miniMessage.deserialize("你选择了<red>红队</red>"));
                Teammanager.addPlayerToTeam(Teammanager.bankgame_thiefteam, player);
            }else if(itemStack.getType() == Material.LIGHT_BLUE_CONCRETE){
                player.sendMessage(miniMessage.deserialize("你选择了<blue>蓝队</blue>"));
                Teammanager.addPlayerToTeam(Teammanager.bankgame_policeteam, player);
            } else if (itemStack.getType() == Material.WHITE_CONCRETE) {
                player.sendMessage(miniMessage.deserialize("你选择了<grey>旁观队</grey>"));
                Teammanager.addPlayerToTeam(Teammanager.bankgame_spectateteam, player);
            } else if(itemStack.getType() == Material.NETHERITE_SWORD){
                if(!player.isOp()){
                    return;
                }
                checkgameprepared();
            }
            player.closeInventory();
        }

    private static void checkgameprepared() {
            if(bankgame.players.size() >= 2 && !Teammanager.bankgame_thiefteam.getEntries().isEmpty() && !Teammanager.bankgame_policeteam.getEntries().isEmpty()){
                bankgame.gameprepared = true;
                instance.getServer().broadcast(miniMessage.deserialize("<green><bold>玩家数量足够，游戏准备就绪"));
                instance.getServer().broadcast(Component.text("再次右键菜单打开职业选择菜单").color(NamedTextColor.GREEN).style(Style.style(TextDecoration.BOLD)));

            }else {
                instance.getServer().broadcast(miniMessage.deserialize("<red><bold>玩家数量不足，无法开始游戏"));
            }
    }
}
