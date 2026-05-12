package com.mlc.mlcgames.zombieday;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class Item {
    public static ItemStack handgun;
    public static ItemStack rifle;
    public static ItemStack submachine_gun;
    public static ItemStack bow;
    public static ItemStack crossbow;


    public static void init(){
        handgun = new ItemStack(Material.ECHO_SHARD);
        rifle = new ItemStack(Material.ECHO_SHARD);
        submachine_gun = new ItemStack(Material.ECHO_SHARD);
        bow = new ItemStack(Material.BOW);
        crossbow = new ItemStack(Material.CROSSBOW);
    }
}
