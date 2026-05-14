package com.mlc.mlcgames.zombieday.zombie;

import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.Objects;

import static com.mlc.mlcgames.Mlcgames.miniMessage;
import static com.mlc.mlcgames.Mlcgames.server;

public class ZombieGoal implements Goal<@NotNull Zombie> {
    public static final GoalKey<@NotNull Zombie> KEY = GoalKey.of(
            Zombie.class,
            new NamespacedKey("mlcgames", "zombie_follow_player")
    );

    private Player player;
    private final Zombie zombie;
    private final double speed;

    public ZombieGoal(Zombie zombie,double speed) {
        this.zombie = zombie;
        this.speed = speed;
    }

    @Override
    public boolean shouldActivate() {
        return true;
    }

    @Override
    public boolean shouldStayActive() {
        return false;
    }

    @Override
    public void start() {
        for(Player player1: zombie.getLocation().getNearbyPlayers(60, 60, 60)){
            if(player1 != null){
                this.player = player1;
                zombie.getPathfinder().moveTo(player,speed);
                break;
            }
        }
    }

    @Override
    public void stop() {
        Goal.super.stop();
    }

    @Override
    public void tick() {
        zombie.getPathfinder().moveTo(player, speed);
        if(zombie.getPathfinder().getCurrentPath() == null){
            server.broadcast(miniMessage.deserialize("no path"));
            return;
        }
        if(zombie.getPathfinder().getCurrentPath().getNextPoint() == null){
            server.broadcast(miniMessage.deserialize("no next point"));
            return;
        }
        server.broadcast(miniMessage.deserialize("i want to go to"+ zombie.getPathfinder().getCurrentPath().getNextPoint().toString()));
        Goal.super.tick();
    }

    @Override
    public GoalKey<@NotNull Zombie> getKey() {
        return KEY;
    }

    @Override
    public EnumSet<GoalType> getTypes() {
        return  EnumSet.of(GoalType.MOVE);
    }
}
