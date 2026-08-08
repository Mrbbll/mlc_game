package com.mlc.mlcgames.sandgame.gamephase;

import com.mlc.mlcgames.Gamesidebar;
import com.mlc.mlcgames.Teammanager;
import com.mlc.mlcgames.sandgame.Sandgame;
import com.mlc.mlcgames.sandgame.managers.Bossbarmanager;
import com.mlc.mlcgames.sandgame.managers.DeviceManager;
import net.kyori.adventure.text.Component;
import org.bukkit.GameMode;
import org.bukkit.GameRules;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import java.util.Objects;

import static com.mlc.mlcgames.Mlcgames.instance;
import static com.mlc.mlcgames.Mlcgames.miniMessage;

public class end {
    private static Component end_msg = miniMessage.deserialize("<gray>游戏结束");

    public static void endgame_with_winner(int winner){
        if(winner==1){
            end_msg = Sandgame.end_msg_1;
        }else if(winner==2){
            end_msg = Sandgame.end_msg_2;
        }else{
            end_msg = Sandgame.end_msg_3;
        }
        endgame();
    }

    public static void endgame(){
        for(Player player: Teammanager.getteamplayer(Teammanager.sandgame_team_1)){
            endplayergame(player);
        }
        for(Player player: Teammanager.getteamplayer(Teammanager.sandgame_team_2)){
            endplayergame(player);
        }
        Player top = Sandgame.getTopKiller();
        if (top != null) {
           instance.getServer().broadcast(miniMessage.deserialize("<gold>MVP：" + top.getName()
                    + "（击杀 " + Sandgame.player_kill_count.get(top) + " 人）"));
        }
        Sandgame.isstart=false;
        Sandgame.countdown=30;
        // 清理场上设备（生成器/塔展示实体）
        DeviceManager.cleanup();
        //清理掉落物和箭
        for(Entity entity: Objects.requireNonNull(instance.getServer().getWorld("world")).getEntities()){
            if(entity instanceof org.bukkit.entity.Arrow){
                entity.remove();
            }
            else if(entity instanceof org.bukkit.entity.Item){
                entity.remove();
            }
        }

        for(Block block: Sandgame.player_placed_blocks){
            block.setType(Material.AIR);
        }
        Sandgame.player_placed_blocks.clear();

        // 未开赛就 /sandgame end 时 timer 为 null
        if (Sandgame.timer != null) {
            Sandgame.timer.cancel();
        }
        Gamesidebar.readconfigline();
        HandlerList.unregisterAll(Sandgame.sandGameListener);
        Objects.requireNonNull(instance.getServer().getWorld("world")).setGameRule(GameRules.FALL_DAMAGE,true);
    }

    private static void endplayergame(Player player){
        player.setRespawnLocation(Sandgame.ready_loc,true);
        player.sendMessage(end_msg);
        player.getInventory().clear();
        player.playSound(player, Sound.ENTITY_CAMEL_DEATH, 1.0f, 1.0f);
        // 恢复生存模式（start 时设为了冒险模式）
        player.setGameMode(GameMode.ADVENTURE);
        player.teleportAsync(Sandgame.ready_loc);
        player.hideBossBar(Bossbarmanager.bossBar);
    }
}
