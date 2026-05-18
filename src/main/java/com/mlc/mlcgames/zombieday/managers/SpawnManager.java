package com.mlc.mlcgames.zombieday.managers;

import com.mlc.mlcgames.zombieday.Zombiedaygame;
import com.mlc.mlcgames.zombieday.zombie.Spwaner;
import com.mlc.mlcgames.zombieday.zombie.Zombietype;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.Zombie;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.Random;
import java.util.Set;

import static com.mlc.mlcgames.Mlcgames.instance;
import static com.mlc.mlcgames.Mlcgames.server;
import static com.mlc.mlcgames.zombieday.Zombiedaygame.*;

public class SpawnManager {

    public static void updatezombiecount(){
        BukkitTask task = new BukkitRunnable(){

            @Override
            public void run() {
                if(!Zombiedaygame.isstart){
                    this.cancel();
                }
                zombies.removeIf(zombie -> !zombie.isValid());
                Zombiedaygame.zombiecount = Zombiedaygame.zombies.size();
            }
        }.runTaskTimerAsynchronously(instance,20,10);
    }

    public static void Spawnzombie(int turn){
        Random random = new Random();
        int playernum = players.size();
        int num;

        int num1;
        int num2;
        int numair;
        double damage;
        double health;
        double speed;

        switch (difficulty){
            case NORMAL:
                num = random.nextInt(turn*5/2,turn * (3 + playernum));

                damage = 2+ turn*0.1;
                health = 10+ turn*0.2;
                speed = 0;

                num1 = (int) (num* random.nextDouble(0.2,0.8));
                num2 = num-num1;
                server.broadcast(Component.text("Spawned "+num1+" zombies in 1 and "+num2+" zombies in 2"));
                Spwaner.spawnrandomzombie(num1,zombieloc1,damage,health,speed);
                Spwaner.spawnrandomzombie(num2,zombieloc2,damage,health,speed);
                if(turn>=4&&turn%2==0){
                    Spwaner.spawnboss(1,zombieloc1,damage*2,health*5,speed*1.3);
                }
                break;
            case HARD:
                num = random.nextInt(turn*10/2,turn*(6 + playernum));

                damage = 3 + turn*0.2;
                health = 10 + turn*0.4;
                speed = 0;

                numair = (int) (num*random.nextDouble(0,0.05));
                num -= numair;
                num1 = (int) (num* random.nextDouble(0.2,0.8));
                num2 = num-num1;
                Spwaner.spawnrandomzombieinair(numair,damage,health,speed);
                Spwaner.spawnrandomzombie(num1,zombieloc1,damage,health,speed);
                Spwaner.spawnrandomzombie(num2,zombieloc2,damage,health,speed);
                if(turn>=4&&turn%2==0){
                    Spwaner.spawnboss(1,zombieloc1,damage*2,health*5,speed*1.3);
                }
                break;
            case INSANE:
                num = random.nextInt(turn*15/2,turn*(8 + playernum));

                damage = 4+ turn*0.3;
                health = 10+ turn*0.6;
                speed = 0.1;

                numair = (int) (num*random.nextDouble(0,0.05));
                num -= numair;
                num1 = (int) (num* random.nextDouble(0.2,0.8));
                num2 = num-num1;
                Spwaner.spawnrandomzombieinair(numair,damage,health,speed);
                Spwaner.spawnrandomzombie(num1,zombieloc1,damage,health,speed);
                Spwaner.spawnrandomzombie(num2,zombieloc2,damage,health,speed);
                if(turn>=4&&turn%2==0){
                    Spwaner.spawnboss(1,zombieloc1,damage*2,health*5,speed*1.3);
                }
                break;

            case TORMENT:
                num = random.nextInt(turn*20/2,turn*(11 + playernum));

                damage = 4 + turn*0.4;
                health = 14+ turn*0.6;
                speed = 0.1;

                numair = (int) (num*random.nextDouble(0,0.05));
                num -= numair;
                num1 = (int) (num* random.nextDouble(0.2,0.8));
                num2 = num-num1;
                Spwaner.spawnrandomzombieinair(numair,damage,health,speed);
                Spwaner.spawnrandomzombie(num1,zombieloc1,damage,health,speed);
                Spwaner.spawnrandomzombie(num2,zombieloc2,damage,health,speed);
                if(turn>=4&&turn%2==0){
                    Spwaner.spawnboss(1,zombieloc1,damage*2,health*5,speed*1.3);
                }
                break;
        }

    }
}
