package com.mlc.mlcgames.bank.listener.clickprocess;

import com.mlc.mlcgames.Teammanager;
import com.mlc.mlcgames.bank.utils.Jobs;
import com.mlc.mlcgames.bank.utils.start.Startgame;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import static com.mlc.mlcgames.Mlcgames.*;

//🏹 🪓 🗡 🛡 ☀
public class Jobselect {
    public static void selectplicejob(ItemStack itemStack, Player player) {
        if(itemStack.getType()== Material.STONE_SWORD){
                player.sendMessage(">>> 你选择了 \uD83D\uDDE1" + Jobs.sword);
                bankgame.playerJobs.put(player, Jobs.sword);
        }else if(itemStack.getType()== Material.CROSSBOW){
                player.sendMessage(">>> 你选择了 ☀" + Jobs.crossbow);

                bankgame.playerJobs.put(player, Jobs.crossbow);
        }else if(itemStack.getType()== Material.WOODEN_AXE){
                player.sendMessage(">>> 你选择了 \uD83E\uDE93" + Jobs.woodenaxe);

                bankgame.playerJobs.put(player, Jobs.woodenaxe);
        }else if(itemStack.getType()== Material.WOLF_SPAWN_EGG){
                player.sendMessage(">>> 你选择了 \uD83D\uDEE1" + Jobs.wolfspawnegg);
                bankgame.playerJobs.put(player, Jobs.wolfspawnegg);
        }else if(itemStack.getType()== Material.BOW){
                player.sendMessage(">>> 你选择了 \uD83C\uDFF9" + Jobs.bow);
                bankgame.playerJobs.put(player, Jobs.bow);
        }
        checkgamestart();
        player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_NETHERITE, 10f, 0.1f);
        player.closeInventory();
    }

    public static void selectthiefjob(ItemStack itemStack, Player player) {
        if(itemStack.getType()== Material.STONE_SWORD){
                player.sendMessage(">>> 你选择了 \uD83D\uDDE1" + Jobs.sword);
                bankgame.playerJobs.put(player, Jobs.sword);
        }else if(itemStack.getType()== Material.CROSSBOW){
                player.sendMessage(">>> 你选择了 ☀" + Jobs.crossbow);
                bankgame.playerJobs.put(player, Jobs.crossbow);
        }else if(itemStack.getType()== Material.WOODEN_AXE){
                player.sendMessage(">>> 你选择了 \uD83E\uDE93" + Jobs.woodenaxe);
                bankgame.playerJobs.put(player, Jobs.woodenaxe);
        }else if(itemStack.getType()== Material.WOLF_SPAWN_EGG){
                player.sendMessage(">>> 你选择了 \uD83D\uDEE1" + Jobs.wolfspawnegg);
                bankgame.playerJobs.put(player, Jobs.wolfspawnegg);
        }else if(itemStack.getType()== Material.BOW){
                player.sendMessage(">>> 你选择了 \uD83C\uDFF9" + Jobs.bow);
                bankgame.playerJobs.put(player, Jobs.bow);
        }
        checkgamestart();
        player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_NETHERITE, 10f, 0.1f);
        player.closeInventory();
    }

    private static void checkgamestart() {
        for(Player player : bankgame.players){
            if(Teammanager.bankgame_spectateteam.hasPlayer(player)){
                continue;
            }
            if(!bankgame.playerJobs.containsKey(player)){
                return;
            }
        }
        Startgame.startgame();
        instance.getServer().broadcast(miniMessage.deserialize("<b><red><!>检测到所有人均选好职业，游戏开始"));
        for(Player player : bankgame.players){
            player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 2f, 0.6f);
        }
    }
}
