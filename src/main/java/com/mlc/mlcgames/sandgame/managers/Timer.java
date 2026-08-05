package com.mlc.mlcgames.sandgame.managers;

import com.mlc.mlcgames.Teammanager;
import com.mlc.mlcgames.sandgame.Sandgame;
import com.mlc.mlcgames.sandgame.gamephase.end;
import com.mlc.mlcgames.sandgame.items.itemmanager;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import static com.mlc.mlcgames.Mlcgames.instance;
import static com.mlc.mlcgames.Mlcgames.miniMessage;

public class Timer extends BukkitRunnable {
    @Override
    public void run() {
        if(Sandgame.isstart){
            Sandgame.countdown--;
            check_if_player_bring_sand_back();
            Bossbarmanager.updateBossBar();
            //写回 Map，必须 replaceAll
            Sandgame.player_money.replaceAll((player, money) -> money + 5);
            for (Player player : Sandgame.getSandgamePlayer()) {
                player.sendActionBar(miniMessage.deserialize("<green>金钱：<yellow><b>" + Sandgame.player_money.getOrDefault(player, 0)));

            }
            sidebarmanager.updatesidebar();
            // 设备 tick（生成器产出、防御塔攻击）
            DeviceManager.tick();
            if(Sandgame.countdown<=0){
                Sandgame.countdown = 30;
                itemmanager.spawnsand(Sandgame.sand_spawn_loc);
                itemmanager.spawnitem(Sandgame.item_spawn_loc_1);
                itemmanager.spawnitem(Sandgame.item_spawn_loc_2);
                Sandgame.team_1_sand_count--;
                Sandgame.team_2_sand_count--;
                if(Sandgame.team_1_sand_count<=0&&Sandgame.team_2_sand_count<=0){
                    end.endgame_with_winner(3);
                }else if(Sandgame.team_1_sand_count<=0){
                    end.endgame_with_winner(2);
                }else if(Sandgame.team_2_sand_count<=0){
                    end.endgame_with_winner(1);
                }

            }
        }else {
            this.cancel();
        }
    }

    private void check_if_player_bring_sand_back() {
        for(Player player: Sandgame.team_1_sand_loc.getNearbyPlayers(1.5)){
            if(Teammanager.isPlayerInTeam(player,Teammanager.sandgame_team_1)){
                Inventory inventory = player.getInventory();
                for (ItemStack itemstack: inventory.getContents()){
                    if(itemstack!=null){
                        if(itemstack.getType().equals(org.bukkit.Material.SAND)){
                            Sandgame.team_1_sand_count+=itemstack.getAmount();
                            player.playSound(player, Sound.ENTITY_PLAYER_TELEPORT, 1.0f, 1.0f);
                            instance.getServer().broadcast(Sandgame.team_1_sand_bring_msg);
                            itemstack.setAmount(0);
                        }
                    }
                }
            }
        }
        for(Player player: Sandgame.team_2_sand_loc.getNearbyPlayers(1.5)){
            if(Teammanager.isPlayerInTeam(player,Teammanager.sandgame_team_2)){
                Inventory inventory = player.getInventory();
                for (ItemStack itemstack: inventory.getContents()){
                    if(itemstack!=null){
                        if(itemstack.getType().equals(org.bukkit.Material.SAND)){
                            Sandgame.team_2_sand_count+=itemstack.getAmount();
                            player.playSound(player, Sound.ENTITY_PLAYER_TELEPORT, 1.0f, 1.0f);
                            instance.getServer().broadcast(Sandgame.team_2_sand_bring_msg);
                            itemstack.setAmount(0);
                        }
                    }
                }
            }
        }
    }
}
