package com.mlc.mlcgames.zombieday.zombie;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public enum Zombietype {
    NORMAL(50) {
        @Override
        public int getWeight(int turn) {
            // 普通僵尸权重随轮次缓慢降低，以让位给特殊僵尸
            return Math.max(10, baseWeight - turn * 2);
        }
    },
    FAST(20) {
        @Override
        public int getWeight(int turn) {
            return baseWeight + turn; // 后期增多
        }
    },
    HIGHJUMP(5) {
        @Override
        public int getWeight(int turn) {
            return turn >= 3 ? baseWeight + (turn - 3) * 2 : 0; // 第3轮才开始出现
        }
    },
    POLICE(5) {
        @Override
        public int getWeight(int turn) {
            return turn >= 5 ? baseWeight + (turn - 5) * 3 : 0;
        }
    },
    RICH(10) {
        @Override
        public int getWeight(int turn) {
            return baseWeight + turn / 2; // 随轮次线性增加
        }
    },
    BOSS(0);


    protected final int baseWeight;

    Zombietype(int baseWeight) {
        this.baseWeight = baseWeight;
    }
    public String getType(){
        return name().toLowerCase();
    }

    public int getWeight(int turn) {
        return baseWeight;
    }


    public static Zombietype getRandomType(int turn) {
        Zombietype[] types = values();
        int totalWeight = 0;
        for (Zombietype t : types) {
            totalWeight += t.getWeight(turn);
        }

        int rand = ThreadLocalRandom.current().nextInt(totalWeight);
        int cumulative = 0;
        for (Zombietype t : types) {
            cumulative += t.getWeight(turn);
            if (rand < cumulative) {
                return t;
            }
        }
        return NORMAL;
    }
}
