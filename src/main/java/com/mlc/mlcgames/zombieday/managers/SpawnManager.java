package com.mlc.mlcgames.zombieday.managers;

import com.mlc.mlcgames.zombieday.Zombiedaygame;
import com.mlc.mlcgames.zombieday.zombie.Spwaner;
import com.mlc.mlcgames.zombieday.zombie.Zombietype;
import org.bukkit.Location;

import java.util.List;
import java.util.Random;
import java.util.Set;

import static com.mlc.mlcgames.zombieday.Zombiedaygame.*;

public class SpawnManager {


    public static void Spawnzombie(int turn){
        Random random = new Random();
        int num;
        int num1;
        int num2;
        int numair;
        double damage;
        double health;
        double speed;

        switch (difficulty){
            case NORMAL:
                num = random.nextInt(turn*10);
                damage = 2+ turn*0.1;
                health = 20+ turn*0.2;
                speed = 0;

                num1 = (int) (num* random.nextDouble(0.2,0.8));
                num2 = num-num1;
                Spwaner.spawnrandomzombie(num1,zombieloc1,damage,health,speed);
                Spwaner.spawnrandomzombie(num2,zombieloc2,damage,health,speed);

                break;
            case HARD:
                num = random.nextInt(turn*15);

                damage = 3 + turn*0.2;
                health = 20 + turn*0.4;
                speed = 0.1;

                numair = (int) (num*random.nextDouble(0,0.05));
                num -= numair;
                num1 = (int) (num* random.nextDouble(0.2,0.8));
                num2 = num-num1;
                Spwaner.spawnrandomzombieinair(numair,damage,health,speed);
                Spwaner.spawnrandomzombie(num1,zombieloc1,damage,health,speed);
                Spwaner.spawnrandomzombie(num2,zombieloc2,damage,health,speed);
                break;
            case INSANE:
                num = random.nextInt(turn*20);

                damage = 3+ turn*0.5;
                health = 30+ turn*0.6;
                speed = 0.15;

                numair = (int) (num*random.nextDouble(0,0.05));
                num -= numair;
                num1 = (int) (num* random.nextDouble(0.2,0.8));
                num2 = num-num1;
                Spwaner.spawnrandomzombieinair(numair,damage,health,speed);
                Spwaner.spawnrandomzombie(num1,zombieloc1,damage,health,speed);
                Spwaner.spawnrandomzombie(num2,zombieloc2,damage,health,speed);
                break;
            case TORMENT:
                num = random.nextInt(turn*30);

                damage = 3+ turn*0.5;
                health = 30+ turn*0.6;
                speed = 0.2;

                numair = (int) (num*random.nextDouble(0,0.05));
                num -= numair;
                num1 = (int) (num* random.nextDouble(0.2,0.8));
                num2 = num-num1;
                Spwaner.spawnrandomzombieinair(numair,damage,health,speed);
                Spwaner.spawnrandomzombie(num1,zombieloc1,damage,health,speed);
                Spwaner.spawnrandomzombie(num2,zombieloc2,damage,health,speed);
                break;
        }

    }
}
