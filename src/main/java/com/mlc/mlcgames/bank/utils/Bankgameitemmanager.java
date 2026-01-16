package com.mlc.mlcgames.bank.utils;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import static com.mlc.mlcgames.Mlcgames.bankgame;

public class Bankgameitemmanager {
    //菜单物品
    public static ItemStack bankgamemenu;

    //队伍选择物品
    public static ItemStack whiteconcrete;
    public static ItemStack lightblueconcrete;
    public static ItemStack redconcrete;
    public static ItemStack netheritesword;

    //职业菜单显示物品
    public static ItemStack police_stonesword;
    public static ItemStack police_crossbow;
    public static ItemStack police_woodenaxe;
    public static ItemStack police_wolfspawnegg;
    public static ItemStack police_bow;

    public static ItemStack thief_stonesword;
    public static ItemStack thief_crossbow;
    public static ItemStack thief_woodenaxe;
    public static ItemStack thief_wolfspawnegg;
    public static ItemStack thief_bow;

    //武器物品
    public static ItemStack arrow;
    public static ItemStack stonesword;
    public static ItemStack crossbow;
    public static ItemStack woodenaxe;
    public static ItemStack wolfspawnegg;
    public static ItemStack bow;
    public static ItemStack normal_bow;
    public static ItemStack normal_crossbow;
    public static ItemStack woodensword;
    public static ItemStack iron_chestplate;
    public static ItemStack iron_leggings;
    public static ItemStack iron_boots;
    public static ItemStack iron_helmet;
    public static ItemStack lether_chestplate;
    public static ItemStack lether_leggings;
    public static ItemStack lether_boots;
    public static ItemStack lether_helmet;
    public static ItemStack diamond_chestplate;
    public static ItemStack diamond_leggings;
    public static ItemStack diamond_boots;
    public static ItemStack diamond_helmet;
    public static ItemStack heal_potion;
    public static ItemStack damage_potion;
    public static ItemStack bone;
    public static ItemStack wolf_armor;
    public static ItemStack beef;



    public static void inititem(){
        ////菜单物品
        bankgamemenu = new ItemStack(Material.ECHO_SHARD);
        ItemMeta itemMeta = bankgamemenu.getItemMeta();
        itemMeta.setItemModel(NamespacedKey.fromString("mlcgames:mlcmenu"));
        bankgamemenu.setItemMeta(itemMeta);

        //队伍选择物品
        whiteconcrete = new ItemStack(Material.WHITE_CONCRETE);
        itemMeta = whiteconcrete.getItemMeta();
        whiteconcrete.setItemMeta(itemMeta);

        lightblueconcrete = new ItemStack(Material.LIGHT_BLUE_CONCRETE);
        itemMeta = lightblueconcrete.getItemMeta();
        lightblueconcrete.setItemMeta(itemMeta);

        redconcrete = new ItemStack(Material.RED_CONCRETE);
        itemMeta = redconcrete.getItemMeta();
        redconcrete.setItemMeta(itemMeta);

        netheritesword = new ItemStack(Material.NETHERITE_SWORD);
        itemMeta = netheritesword.getItemMeta();
        netheritesword.setItemMeta(itemMeta);


        //职业菜单显示物品
        police_stonesword = new ItemStack(Material.STONE_SWORD);
        itemMeta = police_stonesword.getItemMeta();
        police_stonesword.setItemMeta(itemMeta);

        police_crossbow = new ItemStack(Material.CROSSBOW);
        itemMeta = police_crossbow.getItemMeta();
        police_crossbow.setItemMeta(itemMeta);

        police_woodenaxe = new ItemStack(Material.WOODEN_AXE);
        itemMeta = police_woodenaxe.getItemMeta();
        police_woodenaxe.setItemMeta(itemMeta);

        police_wolfspawnegg = new ItemStack(Material.WOLF_SPAWN_EGG);
        itemMeta = police_wolfspawnegg.getItemMeta();
        police_wolfspawnegg.setItemMeta(itemMeta);

        police_bow = new ItemStack(Material.BOW);
        itemMeta = police_bow.getItemMeta();
        police_bow.setItemMeta(itemMeta);

        thief_stonesword = new ItemStack(Material.STONE_SWORD);
        itemMeta = thief_stonesword.getItemMeta();
        thief_stonesword.setItemMeta(itemMeta);

        thief_crossbow = new ItemStack(Material.CROSSBOW);
        itemMeta = thief_crossbow.getItemMeta();
        thief_crossbow.setItemMeta(itemMeta);

        thief_woodenaxe = new ItemStack(Material.WOODEN_AXE);
        itemMeta = thief_woodenaxe.getItemMeta();
        thief_woodenaxe.setItemMeta(itemMeta);

        thief_wolfspawnegg = new ItemStack(Material.WOLF_SPAWN_EGG);
        itemMeta = thief_wolfspawnegg.getItemMeta();
        thief_wolfspawnegg.setItemMeta(itemMeta);

        thief_bow = new ItemStack(Material.BOW);
        itemMeta = thief_bow.getItemMeta();
        thief_bow.setItemMeta(itemMeta);



        ////武器物品
        stonesword = new ItemStack(Material.STONE_SWORD);
        itemMeta = stonesword.getItemMeta();
        stonesword.setItemMeta(itemMeta);

        crossbow = new ItemStack(Material.CROSSBOW);
        itemMeta = crossbow.getItemMeta();
        itemMeta.addEnchant(Enchantment.QUICK_CHARGE,2,true);
        crossbow.setItemMeta(itemMeta);

        woodenaxe = new ItemStack(Material.WOODEN_AXE);
        itemMeta = woodenaxe.getItemMeta();
        woodenaxe.setItemMeta(itemMeta);

        wolfspawnegg = new ItemStack(Material.WOLF_SPAWN_EGG);
        itemMeta = wolfspawnegg.getItemMeta();
        wolfspawnegg.setItemMeta(itemMeta);

        arrow = new ItemStack(Material.ARROW);
        arrow.setAmount(6);
        itemMeta = arrow.getItemMeta();
        arrow.setItemMeta(itemMeta);

        bow = new ItemStack(Material.BOW);
        itemMeta = bow.getItemMeta();
        itemMeta.addEnchant(Enchantment.PUNCH,1,true);
        bow.setItemMeta(itemMeta);

        normal_bow = new ItemStack(Material.BOW);
        itemMeta = normal_bow.getItemMeta();
        normal_bow.setItemMeta(itemMeta);

        normal_crossbow = new ItemStack(Material.CROSSBOW);
        itemMeta = normal_crossbow.getItemMeta();
        normal_crossbow.setItemMeta(itemMeta);

        woodensword = new ItemStack(Material.WOODEN_SWORD);
        itemMeta = woodensword.getItemMeta();
        woodensword.setItemMeta(itemMeta);

        iron_chestplate = new ItemStack(Material.IRON_CHESTPLATE);
        itemMeta = iron_chestplate.getItemMeta();
        iron_chestplate.setItemMeta(itemMeta);

        iron_leggings = new ItemStack(Material.IRON_LEGGINGS);
        itemMeta = iron_leggings.getItemMeta();
        iron_leggings.setItemMeta(itemMeta);

        iron_boots = new ItemStack(Material.IRON_BOOTS);
        itemMeta = iron_boots.getItemMeta();
        iron_boots.setItemMeta(itemMeta);

        iron_helmet = new ItemStack(Material.IRON_HELMET);
        itemMeta = iron_helmet.getItemMeta();
        iron_helmet.setItemMeta(itemMeta);

        lether_chestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
        itemMeta = lether_chestplate.getItemMeta();
        lether_chestplate.setItemMeta(itemMeta);

        lether_leggings = new ItemStack(Material.LEATHER_LEGGINGS);
        itemMeta = lether_leggings.getItemMeta();
        lether_leggings.setItemMeta(itemMeta);

        lether_boots = new ItemStack(Material.LEATHER_BOOTS);
        itemMeta = lether_boots.getItemMeta();
        lether_boots.setItemMeta(itemMeta);

        lether_helmet = new ItemStack(Material.LEATHER_HELMET);
        itemMeta = lether_helmet.getItemMeta();
        lether_helmet.setItemMeta(itemMeta);

        diamond_chestplate = new ItemStack(Material.DIAMOND_CHESTPLATE);
        itemMeta = diamond_chestplate.getItemMeta();
        diamond_chestplate.setItemMeta(itemMeta);

        diamond_leggings = new ItemStack(Material.DIAMOND_LEGGINGS);
        itemMeta = diamond_leggings.getItemMeta();
        diamond_leggings.setItemMeta(itemMeta);

        diamond_boots = new ItemStack(Material.DIAMOND_BOOTS);
        itemMeta = diamond_boots.getItemMeta();
        diamond_boots.setItemMeta(itemMeta);

        diamond_helmet = new ItemStack(Material.DIAMOND_HELMET);
        itemMeta = diamond_helmet.getItemMeta();
        diamond_helmet.setItemMeta(itemMeta);

        heal_potion = new ItemStack(Material.SPLASH_POTION);
        itemMeta = heal_potion.getItemMeta();
        PotionMeta potionMeta = (PotionMeta) itemMeta;
        PotionEffect healingEffect = new PotionEffect(PotionEffectType.REGENERATION, 20 * 10, 1);
        potionMeta.addCustomEffect(healingEffect, true);
        heal_potion.setItemMeta(itemMeta);

        damage_potion = new ItemStack(Material.SPLASH_POTION);
        itemMeta = damage_potion.getItemMeta();
        PotionMeta damagePotionMeta = (PotionMeta) itemMeta;
        PotionEffect damageEffect = new PotionEffect(PotionEffectType.WEAKNESS, 20 * 10, 1);
        damagePotionMeta.addCustomEffect(damageEffect, true);
        damage_potion.setItemMeta(itemMeta);

        bone = new ItemStack(Material.BONE);
        itemMeta = bone.getItemMeta();
        bone.setAmount(64);
        bone.setItemMeta(itemMeta);


        wolf_armor = new ItemStack(Material.WOLF_ARMOR);
        itemMeta = wolf_armor.getItemMeta();
        wolf_armor.setItemMeta(itemMeta);

        beef = new ItemStack(Material.BEEF);
        itemMeta = beef.getItemMeta();
        beef.setAmount(8);
        beef.setItemMeta(itemMeta);
    }

    public static void givepoliceitem(Player player) {
        switch (bankgame.playerJobs.get(player)) {
            case Jobs.sword:
                player.getInventory().addItem(stonesword);
                break;
            case Jobs.crossbow:
                player.getInventory().addItem(crossbow);
                break;
            case Jobs.woodenaxe:
                player.getInventory().addItem(woodenaxe);
                break;
            case Jobs.wolfspawnegg:
                player.getInventory().addItem(wolfspawnegg);
                break;
            case Jobs.bow:
                player.getInventory().addItem(bow);
                break;
        }
    }

    public static void givethiefitem(Player player) {
        switch (bankgame.playerJobs.get(player)) {
            case Jobs.sword:
                player.getInventory().addItem(stonesword);
                player.getInventory().addItem(lether_boots);
                player.getInventory().addItem(iron_helmet);
                player.getInventory().addItem(iron_chestplate);
                player.getInventory().addItem(lether_leggings);
                player.getInventory().addItem(normal_bow);
                player.getInventory().addItem(arrow);
                player.getInventory().addItem(beef);
                break;
            case Jobs.crossbow:
                player.getInventory().addItem(crossbow);
                player.getInventory().addItem(arrow);
                player.getInventory().addItem(arrow);
                player.getInventory().addItem(arrow);
                player.getInventory().addItem(arrow);
                player.getInventory().addItem(arrow);
                player.getInventory().addItem(diamond_helmet);
                player.getInventory().addItem(diamond_boots);
                player.getInventory().addItem(beef);
                break;
            case Jobs.woodenaxe:
                player.getInventory().addItem(woodenaxe);
                player.getInventory().addItem(heal_potion);
                player.getInventory().addItem(heal_potion);
                player.getInventory().addItem(damage_potion);
                player.getInventory().addItem(damage_potion);
                player.getInventory().addItem(iron_chestplate);
                player.getInventory().addItem(lether_boots);
                player.getInventory().addItem(lether_helmet);
                player.getInventory().addItem(beef);
                break;
            case Jobs.wolfspawnegg:
                player.getInventory().addItem(wolfspawnegg);
                player.getInventory().addItem(wolfspawnegg);
                player.getInventory().addItem(wolfspawnegg);
                player.getInventory().addItem(wolf_armor);
                player.getInventory().addItem(wolf_armor);
                player.getInventory().addItem(wolf_armor);
                player.getInventory().addItem(bone);
                player.getInventory().addItem(lether_helmet);
                player.getInventory().addItem(beef);
                break;
            case Jobs.bow:
                player.getInventory().addItem(bow);
                player.getInventory().addItem(woodensword);
                player.getInventory().addItem(arrow);
                player.getInventory().addItem(arrow);
                player.getInventory().addItem(arrow);
                player.getInventory().addItem(iron_helmet);
                player.getInventory().addItem(lether_boots);
                player.getInventory().addItem(iron_helmet);

                break;
        }
    }
}
