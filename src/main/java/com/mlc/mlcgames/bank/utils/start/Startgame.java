package com.mlc.mlcgames.bank.utils.start;

import com.mlc.mlcgames.Teammanager;
import com.mlc.mlcgames.bank.items.Bankgameitemmanager;
import com.mlc.mlcgames.bank.utils.Bankgame;
import com.mlc.mlcgames.bank.utils.Bankgamebossbar;
import com.mlc.mlcgames.bank.utils.Fillutils;
import com.mlc.mlcgames.bank.utils.Gamemode;
import com.mlc.mlcgames.bank.utils.end.Endgame;
import net.kyori.adventure.title.TitlePart;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;

import static com.mlc.mlcgames.Mlcgames.*;
import static com.mlc.mlcgames.bank.utils.Bankgame.gameendcountdown;
import static com.mlc.mlcgames.bank.utils.Bankgamebossbar.bankgamebossbar;

public class Startgame {

        public static void startgame(){
            bankgame.isStart = true;

            switch (bankgame.gamemode){
                case thiefvsthief -> {bankgame.remainTime = fileConfiguration.getInt("bankgame.tvtgametime");}
                case thiefvspolice -> {bankgame.remainTime = fileConfiguration.getInt("bankgame.tvpgametime");}
                default -> {bankgame.remainTime = 60;}
            }
            //如果游戏时间小于等于0,则默认时间为60秒
            if(bankgame.remainTime<=0){
                bankgame.remainTime = 60;
            }
            Bankgamebossbar.bossbarfulltime = bankgame.remainTime;
            //分别传送
            for(Player player : bankgame.players){
                player.getInventory().clear();
                player.updateInventory();
                if(Teammanager.getPlayerTeam(player).equals(Teammanager.bankgame_thiefteam)){
                    player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 999999, 1));
                    if(bankgame.gamemode.equals(Gamemode.thiefvsthief)){
                        player.setRespawnLocation(bankgame.thiefteamLocation1);
                        player.teleport(bankgame.thiefteamLocation1);
                        break;
                    }

                    player.setRespawnLocation(bankgame.thiefteamLocation,true);
                    player.teleport(bankgame.thiefteamLocation);
                }else if(Teammanager.getPlayerTeam(player).equals(Teammanager.bankgame_policeteam)){
                    if(bankgame.gamemode.equals(Gamemode.thiefvsthief)){
                        player.setRespawnLocation(bankgame.thiefteamLocation2,true);
                        player.teleport(bankgame.thiefteamLocation2);
                        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 999999, 1));
                        break;
                    }
                    player.setRespawnLocation(bankgame.policeteamLocation,true);
                    player.teleport(bankgame.policeteamLocation);
                }else if(Teammanager.getPlayerTeam(player).equals(Teammanager.bankgame_spectateteam)) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 999999, 1));
                    player.setRespawnLocation(bankgame.spectatelocation,true);
                    player.teleport(bankgame.spectatelocation);
                    player.setGameMode(GameMode.SPECTATOR);
                }

                //显示bossbar
                bankgamebossbar.addViewer(player);
            }
            //给游戏物品,分游戏模式
            for(Player player : bankgame.players){
                switch (bankgame.gamemode){
                    case thiefvsthief ->             {
                        if(Teammanager.getPlayerTeam(player).equals(Teammanager.bankgame_thiefteam)){
                            Bankgameitemmanager.givethiefitem(player);
                        }else if(Teammanager.getPlayerTeam(player).equals(Teammanager.bankgame_policeteam)){
                            Bankgameitemmanager.givethiefitem(player);
                        }
                    }

                    case thiefvspolice -> {
                        if(Teammanager.getPlayerTeam(player).equals(Teammanager.bankgame_thiefteam)){
                            Bankgameitemmanager.givethiefitem(player);
                        }else if(Teammanager.getPlayerTeam(player).equals(Teammanager.bankgame_policeteam)){
                            Bankgameitemmanager.givepoliceitem(player);
                        }


                    }
                }
            }

            //清除残留射出的箭
            for(Entity entity : bankgame.bankgameLocation.getWorld().getEntities()){
                if(entity instanceof org.bukkit.entity.Arrow){
                    entity.remove();
                }
            }
            //清除展示实体,潜匿贝,狗,箭头
            bankgame.bankgameLocation.getWorld().getEntities().forEach(entity -> {
                if(entity instanceof ItemDisplay){
                    Block block = entity.getLocation().getBlock();
                    if(block.getType() == Material.BARREL){
                    entity.remove();
                    }
                }else if(entity instanceof Item ||entity instanceof Arrow ||entity instanceof Wolf||entity instanceof Shulker){
                    entity.remove();
                }
            });
            //如果是tvp，生成潜匿贝,监听事件
            //生成潜匿贝
            if(bankgame.gamemode.equals(Gamemode.thiefvspolice)) {

                //监听事件
                //金库打开事件
                bankgame.Openlocklistener();

                //灯光破坏事件
                bankgame.Lightningbreaklistener();

                //灯光修复事件
                bankgame.Lightningfixlistener();

                //生成潜匿贝
                bankgame.pow1locentity = (Shulker) bankgame.powerLocation1.getWorld().spawnEntity(bankgame.powerLocation1, EntityType.SHULKER);

                bankgame.pow2locentity = (Shulker) bankgame.powerLocation2.getWorld().spawnEntity(bankgame.powerLocation2, EntityType.SHULKER);

                bankgame.outlocentity1 = (Shulker) bankgame.leaveLocation1.getWorld().spawnEntity(bankgame.leaveLocation1, EntityType.SHULKER);

                bankgame.outlocentity2 = (Shulker) bankgame.leaveLocation2.getWorld().spawnEntity(bankgame.leaveLocation2, EntityType.SHULKER);

                bankgame.goldlocentity = (Shulker) bankgame.spectatelocation.getWorld().spawnEntity(bankgame.spectatelocation, EntityType.SHULKER);



                Teammanager.addentityToTeam(Teammanager.bankgame_powerlocteam, bankgame.pow1locentity);
                Teammanager.addentityToTeam(Teammanager.bankgame_powerlocteam, bankgame.pow2locentity);
                Teammanager.addentityToTeam(Teammanager.bankgame_outlocteam, bankgame.outlocentity1);
                Teammanager.addentityToTeam(Teammanager.bankgame_outlocteam, bankgame.outlocentity2);
                Teammanager.addentityToTeam(Teammanager.bankgame_goldlocteam, bankgame.goldlocentity);

                inilocentiy(bankgame.pow1locentity);
                inilocentiy(bankgame.pow2locentity);
                inilocentiy(bankgame.outlocentity1);
                inilocentiy(bankgame.outlocentity2);
                inilocentiy(bankgame.goldlocentity);
                //给潜匿贝发光
                bankgame.outlocentity1.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING,99999*20,1,true));
                bankgame.outlocentity2.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING,99999*20,1,true));
                bankgame.pow1locentity.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING,99999*20,1,true));
                bankgame.pow2locentity.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING,99999*20,1,true));
            }
            //恢复照明

            Fillutils.replaceblock(Material.LIGHT);
            //开始倒计时
            gameendcountdown = new BukkitRunnable() {
                @Override
                public void run() {
                    if(bankgame.remainTime==30){
                        instance.getServer().broadcast(miniMessage.deserialize("<b><#ff2a26>>>> 还剩最后30秒"));
                        for(Player player: bankgame.players){
                            player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP,1,2);
                            player.sendTitlePart(TitlePart.TITLE,miniMessage.deserialize("<b><#ff2a26>⌚!!!"));
                        }

                    };

                    if(bankgame.remainTime > 0){
                        bankgame.remainTime--;
                        //更新bossbar
                        Bankgamebossbar.updateBossbar();
                    }else {
                        //倒计时结束，结束游戏
                        Endgame.endgame();
                        cancel();
                    }
                }
            }.runTaskTimer(instance, 0, 20);

            //道具事件

        }
    private static void inilocentiy(Shulker shulker){
        shulker.setAI(false);
        shulker.setPeek(0);
        shulker.setSilent(true);
        shulker.setInvulnerable(true);
        shulker.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 99999 * 20, 10));
        shulker.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 99999 * 20, 10));
        shulker.teleport(shulker.getLocation().add(0, -1, 0));
    }

}
