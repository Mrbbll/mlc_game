package com.mlc.mlcgames.sandgame.items;

import com.mlc.mlcgames.Teammanager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Team;

public class itemmanager {
    public static ItemStack sand;
    public static ItemStack baseitem_sword;
    public static ItemStack team_1_wool;
    public static ItemStack team_2_wool;
    public static ItemStack baseitem_helmet_1;
    public static ItemStack baseitem_helmet_2;
    public static ItemStack baseitem_shears;

    public static ItemStack arrow;
    public static ItemStack bow;

    public static ItemStack potion_speed;
    public static ItemStack potion_jump;
    public static ItemStack potion_regeneration;
    public static ItemStack beef;
    public static ItemStack wool_1;
    public static ItemStack wool_2;


    public static void init(){
        sand = new ItemStack(Material.SAND);
        baseitem_sword = new ItemStack(Material.WOODEN_SWORD);
        team_1_wool = new ItemStack(Material.RED_WOOL);
        team_2_wool = new ItemStack(Material.BLUE_WOOL);
        baseitem_helmet_1 = new ItemStack(Material.LEATHER_HELMET);
        baseitem_helmet_2 = new ItemStack(Material.LEATHER_HELMET);
        baseitem_shears = new ItemStack(Material.SHEARS);
        arrow = new ItemStack(Material.ARROW);
        bow = new ItemStack(Material.BOW);
        potion_speed = new ItemStack(Material.POTION);
        potion_jump = new ItemStack(Material.POTION);
        potion_regeneration = new ItemStack(Material.POTION);
        beef = new ItemStack(Material.BEEF);
        wool_1 = new ItemStack(Material.RED_WOOL);
        wool_1.setAmount(64);
        wool_2 = new ItemStack(Material.BLUE_WOOL);
        wool_2.setAmount(64);


    }

    public static void giveBaseItems(Player player){
        Team team = Teammanager.getPlayerTeam(player);
        if(team.equals(Teammanager.sandgame_team_1)){
            player.getInventory().addItem(wool_1);
            player.getInventory().addItem(baseitem_helmet_1);
        }else if(team.equals(Teammanager.sandgame_team_2)){
            player.getInventory().addItem(wool_2);
            player.getInventory().addItem(baseitem_helmet_2);
        }
        player.getInventory().addItem(baseitem_sword);
        player.getInventory().addItem(baseitem_shears);
        player.getInventory().addItem(arrow);
        player.getInventory().addItem(bow);
        player.getInventory().addItem(beef);
    }

    public static void spawnsand(Location loc){
        loc.getWorld().dropItem(loc, sand);
    }

    public static void spawnitem(Location loc){
        loc.getWorld().dropItem(loc, baseitem_sword);
    }
}
