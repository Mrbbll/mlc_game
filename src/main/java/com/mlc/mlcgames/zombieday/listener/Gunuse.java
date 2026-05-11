package com.mlc.mlcgames.zombieday.listener;

import com.mlc.mlcgames.zombieday.Zombiedaygame;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class Gunuse implements Listener {
    @EventHandler
    public void onGunuse(PlayerInteractEvent event){
        if(!Zombiedaygame.isstart){
            return;
        }
        Player player = event.getPlayer();
        if (event.getItem() != null && !event.getItem().getType().equals(Material.ECHO_SHARD)) {
            return;
        }
        Action action = event.getAction();
        if(action.equals(Action.RIGHT_CLICK_AIR)||action.equals(Action.RIGHT_CLICK_BLOCK)){
            Gunshotevent();
        }
        if(action.equals(Action.LEFT_CLICK_AIR)||action.equals(Action.LEFT_CLICK_BLOCK)){
            Gunrefillevent();
        }
    }

    private void Gunrefillevent() {
    }

    private void Gunshotevent() {
    }
}
