package com.mlc.mlcgames.sandgame.items;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class shopmenuitem {
    public static ItemStack wool;
    public static ItemStack sand;
    public static ItemStack stonesword;
    public static ItemStack beef;
    public static ItemStack cobweb;
    public static ItemStack bow;
    public static ItemStack arrow;
    public static ItemStack goldenapple;

    public static void init(){
        wool = ItemStack.of(Material.WHITE_WOOL,16);
        sand = ItemStack.of(Material.SAND,1);
        stonesword = ItemStack.of(Material.STONE_SWORD,1);
        beef = ItemStack.of(Material.BEEF,3);
        cobweb = ItemStack.of(Material.COBWEB,3);
        bow = ItemStack.of(Material.BOW,1);
        arrow = ItemStack.of(Material.ARROW,1);
        goldenapple = ItemStack.of(Material.GOLDEN_APPLE,1);


    }

}
