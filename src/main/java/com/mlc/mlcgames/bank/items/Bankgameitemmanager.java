package com.mlc.mlcgames.bank.items;

import com.mlc.mlcgames.bank.utils.Jobs;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

import static com.mlc.mlcgames.Mlcgames.bankgame;
import static com.mlc.mlcgames.Mlcgames.miniMessage;

public class Bankgameitemmanager {
    //菜单物品
    public static ItemStack bankgamemenu;
    //金库内物品
    public static ItemStack golditem;

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
    public static ItemStack shield;
    public static ItemStack arrow;
    public static ItemStack spectral_arrow;
    public static ItemStack slow_arrow;
    public static ItemStack stonesword;
    public static ItemStack crossbow;
    public static ItemStack quickcrossbow;
    public static ItemStack woodenaxe;
    public static ItemStack stoneaxe;
    public static ItemStack wolfspawnegg;
    public static ItemStack bow;
    public static ItemStack normal_bow;
    public static ItemStack normal_crossbow;
    public static ItemStack woodensword;
    public static ItemStack iron_sword;
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
    public static ItemStack weakness_potion;
    public static ItemStack bone;
    public static ItemStack wolf_armor;
    public static ItemStack beef;
    public static ItemStack golden_apple;



    public static void inititem(){
        ////菜单物品
        bankgamemenu = new ItemStack(Material.ECHO_SHARD);
        ItemMeta itemMeta = bankgamemenu.getItemMeta();
        itemMeta.setItemModel(NamespacedKey.fromString("mlcgames:mlcmenu"));
        bankgamemenu.setItemMeta(itemMeta);

        //金库内物品
        golditem = new ItemStack(Material.GOLD_INGOT);
        itemMeta = golditem.getItemMeta();
        itemMeta.displayName(miniMessage.deserialize("<!i><#ffc403>金条金条金条金条金条金条金条金条金条金条金条金条金条金条金条金条金条金条金条金条金条金条金条金条"));
        List<Component> lore = List.of(miniMessage.deserialize("<!i><#ffc403>金条金条金条金条金条金条金条金条金条金条金条金条金条金条金条金条金条金条金条金条金条金条金条金条"));
        itemMeta.lore(lore);
        golditem.setItemMeta(itemMeta);

        //队伍选择物品
        whiteconcrete = new ItemStack(Material.WHITE_CONCRETE);
        itemMeta = whiteconcrete.getItemMeta();
        itemMeta.displayName(miniMessage.deserialize("<!i>旁观"));
        whiteconcrete.setItemMeta(itemMeta);

        lightblueconcrete = new ItemStack(Material.LIGHT_BLUE_CONCRETE);
        itemMeta = lightblueconcrete.getItemMeta();
        itemMeta.displayName(miniMessage.deserialize("<!i><#0de3ff>蓝队"));
        lore = List.of(miniMessage.deserialize("<!i><#0de3ff>如果有警察队，则这队是警察"));
        itemMeta.lore(lore);
        lightblueconcrete.setItemMeta(itemMeta);

        redconcrete = new ItemStack(Material.RED_CONCRETE);
        itemMeta = redconcrete.getItemMeta();
        itemMeta.displayName(miniMessage.deserialize("<!i><#ff5340>红队"));
        lore = List.of(miniMessage.deserialize("<!i><#ff5340>如果有警察队，则这队是小偷"));
        itemMeta.lore(lore);
        redconcrete.setItemMeta(itemMeta);

        netheritesword = new ItemStack(Material.NETHERITE_SWORD);
        itemMeta = netheritesword.getItemMeta();
        itemMeta.displayName(miniMessage.deserialize("<!i><bold>开始"));
        lore = List.of(miniMessage.deserialize("<!i><bold><#fffb00>需要管理员权限"));
        itemMeta.lore(lore);

        netheritesword.setItemMeta(itemMeta);


        //职业菜单显示物品
        police_stonesword = new ItemStack(Material.STONE_SWORD);
        itemMeta = police_stonesword.getItemMeta();
        itemMeta.displayName(miniMessage.deserialize("<!i><bold><#2efff8>全副武装的警察"));
        police_stonesword.setItemMeta(itemMeta);

        police_crossbow = new ItemStack(Material.CROSSBOW);
        itemMeta = police_crossbow.getItemMeta();
        itemMeta.displayName(miniMessage.deserialize("<!i><bold><#ffd342>带远程攻击上debuff的警察"));
        police_crossbow.setItemMeta(itemMeta);

        police_woodenaxe = new ItemStack(Material.WOODEN_AXE);
        itemMeta = police_woodenaxe.getItemMeta();
        itemMeta.displayName(miniMessage.deserialize("<!i><bold><#d16eff>爱上debuff的警察"));
        police_woodenaxe.setItemMeta(itemMeta);

        police_wolfspawnegg = new ItemStack(Material.WOLF_SPAWN_EGG);
        itemMeta = police_wolfspawnegg.getItemMeta();
        itemMeta.displayName(miniMessage.deserialize("<!i><bold><#ff3d6e>喜欢养狗的警察"));
        police_wolfspawnegg.setItemMeta(itemMeta);

        police_bow = new ItemStack(Material.BOW);
        itemMeta = police_bow.getItemMeta();
        itemMeta.displayName(miniMessage.deserialize("<!i><bold><#99ff1c>远程攻击的警察"));
        police_bow.setItemMeta(itemMeta);

        thief_stonesword = new ItemStack(Material.STONE_SWORD);
        itemMeta = thief_stonesword.getItemMeta();
        itemMeta.displayName(miniMessage.deserialize("<!i><bold><#2efff8>喜欢打架的贼"));
        thief_stonesword.setItemMeta(itemMeta);

        thief_crossbow = new ItemStack(Material.CROSSBOW);
        itemMeta = thief_crossbow.getItemMeta();
        itemMeta.displayName(miniMessage.deserialize("<!i><bold><#ffd342>喜欢远程加debuff的贼"));
        thief_crossbow.setItemMeta(itemMeta);

        thief_woodenaxe = new ItemStack(Material.WOODEN_AXE);
        itemMeta = thief_woodenaxe.getItemMeta();
        itemMeta.displayName(miniMessage.deserialize("<!i><bold><#d16eff>喜欢喷水的贼"));
        thief_woodenaxe.setItemMeta(itemMeta);

        thief_wolfspawnegg = new ItemStack(Material.WOLF_SPAWN_EGG);
        itemMeta = thief_wolfspawnegg.getItemMeta();
        itemMeta.displayName(miniMessage.deserialize("<!i><bold><#ff3d6e>喜欢养狗的贼"));
        thief_wolfspawnegg.setItemMeta(itemMeta);

        thief_bow = new ItemStack(Material.BOW);
        itemMeta = thief_bow.getItemMeta();
        itemMeta.displayName(miniMessage.deserialize("<!i><bold><#99ff1c>喜欢射的贼"));
        thief_bow.setItemMeta(itemMeta);



        ////武器物品
        shield = new ItemStack(Material.SHIELD);
        itemMeta = shield.getItemMeta();
        itemMeta.displayName(miniMessage.deserialize("<!i>防爆盾"));
        shield.setItemMeta(itemMeta);


        stonesword = new ItemStack(Material.STONE_SWORD);
        itemMeta = stonesword.getItemMeta();
        stonesword.setItemMeta(itemMeta);

        iron_sword = new ItemStack(Material.IRON_SWORD);
        itemMeta = iron_sword.getItemMeta();
        iron_sword.setItemMeta(itemMeta);

        crossbow = new ItemStack(Material.CROSSBOW);
        itemMeta = crossbow.getItemMeta();
        crossbow.setItemMeta(itemMeta);

        quickcrossbow = new ItemStack(Material.CROSSBOW);
        itemMeta = quickcrossbow.getItemMeta();
        itemMeta.addEnchant(Enchantment.QUICK_CHARGE,2,true);
        quickcrossbow.setItemMeta(itemMeta);

        woodenaxe = new ItemStack(Material.WOODEN_AXE);
        itemMeta = woodenaxe.getItemMeta();
        woodenaxe.setItemMeta(itemMeta);

        stoneaxe = new ItemStack(Material.STONE_AXE);
        itemMeta = stoneaxe.getItemMeta();
        stoneaxe.setItemMeta(itemMeta);




        wolfspawnegg = new ItemStack(Material.WOLF_SPAWN_EGG);
        itemMeta = wolfspawnegg.getItemMeta();
        wolfspawnegg.setItemMeta(itemMeta);

        arrow = new ItemStack(Material.ARROW);
        arrow.setAmount(6);
        itemMeta = arrow.getItemMeta();
        arrow.setItemMeta(itemMeta);

        slow_arrow = new ItemStack(Material.TIPPED_ARROW);
        slow_arrow.setAmount(16);
        itemMeta = slow_arrow.getItemMeta();
        itemMeta.displayName(miniMessage.deserialize("<!i>迟缓箭"));
        PotionMeta slowPotionMeta = (PotionMeta) itemMeta;
        PotionEffect slowPotionEffect = new PotionEffect(PotionEffectType.SLOWNESS, 15*20, 0);
        slowPotionMeta.addCustomEffect(slowPotionEffect, true);
        slow_arrow.setItemMeta(itemMeta);

        spectral_arrow = new ItemStack(Material.SPECTRAL_ARROW);
        spectral_arrow.setAmount(16);
        itemMeta = spectral_arrow.getItemMeta();
        spectral_arrow.setItemMeta(itemMeta);

        bow = new ItemStack(Material.BOW);
        itemMeta = bow.getItemMeta();
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
        itemMeta.displayName(miniMessage.deserialize("<!i>治疗瓶"));
        PotionMeta potionMeta = (PotionMeta) itemMeta;
        PotionEffect healingEffect = new PotionEffect(PotionEffectType.REGENERATION, 20 * 10, 1);
        potionMeta.addCustomEffect(healingEffect, true);
        heal_potion.setItemMeta(itemMeta);

        damage_potion = new ItemStack(Material.SPLASH_POTION);
        itemMeta = damage_potion.getItemMeta();
        itemMeta.displayName(miniMessage.deserialize("<!i>伤害瓶"));
        PotionMeta damagePotionMeta = (PotionMeta) itemMeta;
        PotionEffect damageEffect = new PotionEffect(PotionEffectType.INSTANT_DAMAGE, 20, 0);
        damagePotionMeta.addCustomEffect(damageEffect, true);
        damage_potion.setItemMeta(itemMeta);

        weakness_potion = new ItemStack(Material.SPLASH_POTION);
        itemMeta = weakness_potion.getItemMeta();
        itemMeta.displayName(miniMessage.deserialize("<!i>虚弱瓶"));
        PotionMeta weaknessPotionMeta = (PotionMeta) itemMeta;
        PotionEffect weaknessEffect = new PotionEffect(PotionEffectType.WEAKNESS, 20 * 10, 1);
        weaknessPotionMeta.addCustomEffect(weaknessEffect, true);
        weakness_potion.setItemMeta(itemMeta);

        bone = new ItemStack(Material.BONE);
        itemMeta = bone.getItemMeta();
        bone.setAmount(64);
        bone.setItemMeta(itemMeta);


        wolf_armor = new ItemStack(Material.WOLF_ARMOR);
        itemMeta = wolf_armor.getItemMeta();
        wolf_armor.setItemMeta(itemMeta);

        beef = new ItemStack(Material.COOKED_BEEF);
        itemMeta = beef.getItemMeta();
        beef.setAmount(8);
        beef.setItemMeta(itemMeta);

        golden_apple = new ItemStack(Material.GOLDEN_APPLE);
        itemMeta = golden_apple.getItemMeta();
        golden_apple.setItemMeta(itemMeta);
        golden_apple.setAmount(1);
    }

    public static void givepoliceitem(Player player) {
        switch (bankgame.playerJobs.get(player)) {
            case Jobs.sword:
                player.getInventory().addItem(iron_sword);
                player.getInventory().setItemInOffHand(shield);
                player.getInventory().addItem(normal_bow);
                player.getInventory().addItem(arrow);
                player.getInventory().addItem(arrow);
                player.getInventory().addItem(golden_apple);
                player.getInventory().addItem(golden_apple);
                player.getInventory().addItem(golden_apple);
                player.getInventory().setHelmet(diamond_helmet);
                player.getInventory().setChestplate(diamond_chestplate);
                player.getInventory().setLeggings(diamond_leggings);
                player.getInventory().setBoots(diamond_boots);
                player.getInventory().addItem(beef);
                player.getInventory().addItem(beef);
                player.getInventory().addItem(beef);
                break;
            case Jobs.crossbow:
                player.getInventory().addItem(stonesword);
                player.getInventory().addItem(quickcrossbow);
                player.getInventory().addItem(golden_apple);
                player.getInventory().addItem(spectral_arrow);
                player.getInventory().addItem(spectral_arrow);
                player.getInventory().addItem(spectral_arrow);
                player.getInventory().addItem(slow_arrow);
                player.getInventory().setHelmet(diamond_helmet);
                player.getInventory().setChestplate(diamond_chestplate);
                player.getInventory().setLeggings(diamond_leggings);
                player.getInventory().setBoots(diamond_boots);
                player.getInventory().addItem(beef);
                player.getInventory().addItem(beef);
                break;
            case Jobs.woodenaxe:
                player.getInventory().addItem(stoneaxe);
                player.getInventory().addItem(golden_apple);
                player.getInventory().addItem(golden_apple);
                player.getInventory().setHelmet(diamond_helmet);
                player.getInventory().setChestplate(diamond_chestplate);
                player.getInventory().setLeggings(diamond_leggings);
                player.getInventory().setBoots(diamond_boots);
                player.getInventory().addItem(heal_potion);
                player.getInventory().addItem(heal_potion);
                player.getInventory().addItem(weakness_potion);
                player.getInventory().addItem(weakness_potion);
                player.getInventory().addItem(damage_potion);
                player.getInventory().addItem(damage_potion);
                player.getInventory().addItem(beef);
                player.getInventory().addItem(beef);

                break;
            case Jobs.wolfspawnegg:
                player.getInventory().addItem(stonesword);
                player.getInventory().addItem(wolfspawnegg);
                player.getInventory().addItem(wolfspawnegg);
                player.getInventory().addItem(wolfspawnegg);
                player.getInventory().addItem(wolfspawnegg);
                player.getInventory().addItem(wolfspawnegg);
                player.getInventory().addItem(wolf_armor);
                player.getInventory().addItem(wolf_armor);
                player.getInventory().addItem(wolf_armor);
                player.getInventory().addItem(wolf_armor);
                player.getInventory().addItem(wolf_armor);
                player.getInventory().addItem(golden_apple);
                player.getInventory().setHelmet(diamond_helmet);
                player.getInventory().setChestplate(diamond_chestplate);
                player.getInventory().setLeggings(diamond_leggings);
                player.getInventory().setBoots(diamond_boots);
                player.getInventory().addItem(beef);
                player.getInventory().addItem(beef);
                break;
            case Jobs.bow:
                player.getInventory().addItem(bow);
                player.getInventory().addItem(stonesword);
                player.getInventory().addItem(golden_apple);
                player.getInventory().addItem(arrow);
                player.getInventory().addItem(arrow);
                player.getInventory().addItem(arrow);
                player.getInventory().addItem(arrow);
                player.getInventory().addItem(arrow);
                player.getInventory().addItem(arrow);
                player.getInventory().addItem(arrow);
                player.getInventory().addItem(arrow);
                player.getInventory().addItem(arrow);
                player.getInventory().addItem(arrow);
                player.getInventory().setHelmet(diamond_helmet);
                player.getInventory().setChestplate(diamond_chestplate);
                player.getInventory().setLeggings(diamond_leggings);
                player.getInventory().setBoots(diamond_boots);
                player.getInventory().addItem(bone);
                player.getInventory().addItem(beef);
                player.getInventory().addItem(beef);
                break;
        }
    }

    public static void givethiefitem(Player player) {
        switch (bankgame.playerJobs.get(player)) {
            case Jobs.sword:
                player.getInventory().addItem(stonesword);
                player.getInventory().setBoots(lether_boots);
                player.getInventory().setHelmet(iron_helmet);
                player.getInventory().setChestplate(iron_chestplate);
                player.getInventory().setLeggings(lether_leggings);
                player.getInventory().addItem(normal_bow);
                player.getInventory().addItem(arrow);
                player.getInventory().addItem(beef);
                break;
            case Jobs.crossbow:
                player.getInventory().addItem(crossbow);
                player.getInventory().addItem(slow_arrow);
                player.getInventory().addItem(spectral_arrow);
                player.getInventory().addItem(diamond_helmet);
                player.getInventory().addItem(diamond_boots);
                player.getInventory().addItem(beef);
                break;
            case Jobs.woodenaxe:
                player.getInventory().addItem(woodenaxe);
                player.getInventory().addItem(heal_potion);
                player.getInventory().addItem(heal_potion);
                player.getInventory().addItem(damage_potion);
                player.getInventory().setChestplate(iron_chestplate);
                player.getInventory().setBoots(lether_boots);
                player.getInventory().setHelmet(lether_helmet);
                player.getInventory().addItem(beef);
                break;
            case Jobs.wolfspawnegg:
                player.getInventory().addItem(wolfspawnegg);
                player.getInventory().addItem(wolfspawnegg);
                player.getInventory().addItem(wolf_armor);
                player.getInventory().addItem(wolf_armor);
                player.getInventory().addItem(bone);
                player.getInventory().setHelmet(lether_helmet);
                player.getInventory().addItem(beef);
                break;
            case Jobs.bow:
                player.getInventory().addItem(bow);
                player.getInventory().addItem(woodensword);
                player.getInventory().addItem(arrow);
                player.getInventory().addItem(arrow);
                player.getInventory().addItem(arrow);
                player.getInventory().setHelmet(iron_helmet);
                player.getInventory().setBoots(lether_boots);

                break;
        }
    }
}
