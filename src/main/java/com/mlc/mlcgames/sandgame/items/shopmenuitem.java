package com.mlc.mlcgames.sandgame.items;

import io.papermc.paper.datacomponent.DataComponentTypes;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.mlc.mlcgames.Mlcgames.instance;
import static com.mlc.mlcgames.Mlcgames.miniMessage;

public class shopmenuitem {
    public static ItemStack wool;
    public static ItemStack sand;
    public static ItemStack stonesword;
    public static ItemStack beef;
    public static ItemStack cobweb;
    public static ItemStack bow;
    public static ItemStack arrow;
    public static ItemStack goldenapple;

    // 新增：设备与升级
    public static ItemStack invispotion;
    public static ItemStack coingen;
    public static ItemStack coingenfast;
    public static ItemStack bomb;
    public static ItemStack tower;
    public static ItemStack towerdmg;
    public static ItemStack towerarmor;
    public static ItemStack sandgen;

    // 按物品类型定价（价格为一份商品的价格）
    public static Map<Material, Integer> prices = new HashMap<>();

    // 实际售出的干净物品（不带价格/点击提示 lore），按类型查找
    public static Map<Material, ItemStack> sellables = new HashMap<>();

    public static void init(){
        wool = new ItemStack(Material.WHITE_WOOL, 16);
        wool.editMeta(meta -> {
            meta.displayName(miniMessage.deserialize("<b><white>白色羊毛"));
            meta.lore(List.of(miniMessage.deserialize("<gray>通用建筑材料")));
        });
        wool.setData(DataComponentTypes.CAN_PLACE_ON, itemmanager.buildAdventurePredicate(List.of(Material.SAND)));

        sand = new ItemStack(Material.SAND);
        sand.editMeta(meta -> {
            meta.displayName(miniMessage.deserialize("<b><#e6c35c>沙子"));
            meta.lore(List.of(miniMessage.deserialize("<gray>掩体与建筑材料")));
        });

        stonesword = new ItemStack(Material.STONE_SWORD);
        stonesword.editMeta(meta -> {
            meta.displayName(miniMessage.deserialize("<b><gold>石剑"));
            meta.lore(List.of(miniMessage.deserialize("<gold>更锋利的武器")));
            meta.setUnbreakable(true);
            meta.addAttributeModifier(Attribute.ATTACK_DAMAGE,
                    new AttributeModifier(new NamespacedKey(instance, "stone_sword_damage"), 5.0,
                            AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.HAND));
        });
        stonesword.setData(DataComponentTypes.CAN_BREAK, itemmanager.buildAdventurePredicate(itemmanager.breakableMaterials()));

        beef = new ItemStack(Material.BEEF, 3);
        beef.editMeta(meta -> {
            meta.displayName(miniMessage.deserialize("<b><white>生牛肉"));
            meta.lore(List.of(miniMessage.deserialize("<gray>补充饥饿值")));
        });

        cobweb = new ItemStack(Material.COBWEB, 3);
        cobweb.editMeta(meta -> {
            meta.displayName(miniMessage.deserialize("<b><white>蜘蛛网"));
            meta.lore(List.of(miniMessage.deserialize("<gray>减缓敌人移动")));
        });

        bow = new ItemStack(Material.BOW);
        bow.editMeta(meta -> {
            meta.displayName(miniMessage.deserialize("<b><gold>强化弓"));
            meta.lore(List.of(miniMessage.deserialize("<gold>远程压制")));
            meta.setUnbreakable(true);
            meta.addEnchant(Enchantment.POWER,2,true);
        });

        arrow = new ItemStack(Material.ARROW);
        arrow.editMeta(meta -> {
            meta.displayName(miniMessage.deserialize("<b><white>箭矢"));
            meta.lore(List.of(miniMessage.deserialize("<gray>弓的弹药")));
        });

        goldenapple = new ItemStack(Material.GOLDEN_APPLE);
        goldenapple.editMeta(meta -> {
            meta.displayName(miniMessage.deserialize("<b><gold>金苹果"));
            meta.lore(List.of(miniMessage.deserialize("<gold>恢复生命并附加增益")));
        });

        invispotion = new ItemStack(Material.POTION);
        invispotion.editMeta(PotionMeta.class, meta -> {
            meta.displayName(miniMessage.deserialize("<b><#ff55ff>隐身药水"));
            meta.lore(List.of(miniMessage.deserialize("<gray>隐身30秒")));
            meta.setColor(Color.fromRGB(255, 85, 255));
            meta.addCustomEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 20*30, 0, true, true, true), true);
        });

        coingen = new ItemStack(Material.EMERALD_BLOCK);
        coingen.editMeta(meta -> {
            meta.displayName(miniMessage.deserialize("<b><green>金币生成器"));
            meta.lore(List.of(miniMessage.deserialize("<gray>右键放置，每3秒生成1个绿宝石")));
        });

        coingenfast = new ItemStack(Material.SUGAR);
        coingenfast.editMeta(meta -> {
            meta.displayName(miniMessage.deserialize("<b><yellow>金币生成器速度加强版"));
            meta.lore(List.of(miniMessage.deserialize("<gray>右键金币生成器，加速为每1秒1个")));
        });

        bomb = new ItemStack(Material.TNT);
        bomb.editMeta(meta -> {
            meta.displayName(miniMessage.deserialize("<b><red>炸弹"));
            meta.lore(List.of(miniMessage.deserialize("<gray>右键引爆，拆除3格内设备")));
        });

        tower = new ItemStack(Material.OBSIDIAN);
        tower.editMeta(meta -> {
            meta.displayName(miniMessage.deserialize("<b><dark_purple>防御塔"));
            meta.lore(List.of(miniMessage.deserialize("<gray>右键放置，攻击5格内敌人")));
        });

        towerdmg = new ItemStack(Material.BLAZE_POWDER);
        towerdmg.editMeta(meta -> {
            meta.displayName(miniMessage.deserialize("<b><gold>防御塔攻击加强版"));
            meta.lore(List.of(miniMessage.deserialize("<gray>右键防御塔，伤害翻倍为14")));
        });

        towerarmor = new ItemStack(Material.SHIELD);
        towerarmor.editMeta(meta -> {
            meta.displayName(miniMessage.deserialize("<b><aqua>防御塔护甲加强版"));
            meta.lore(List.of(miniMessage.deserialize("<gray>右键设备，可抵挡1次炸弹")));
        });

        sandgen = new ItemStack(Material.SANDSTONE);
        sandgen.editMeta(meta -> {
            meta.displayName(miniMessage.deserialize("<b><yellow>沙子生成器"));
            meta.lore(List.of(miniMessage.deserialize("<gray>右键放置，每30秒生成1个沙子")));
        });

        prices.put(Material.WHITE_WOOL, 30);
        prices.put(Material.SAND, 10);
        prices.put(Material.STONE_SWORD, 40);
        prices.put(Material.BEEF, 15);
        prices.put(Material.COBWEB, 20);
        prices.put(Material.BOW, 50);
        prices.put(Material.ARROW, 5);
        prices.put(Material.GOLDEN_APPLE, 80);
        prices.put(Material.POTION, 200);
        prices.put(Material.EMERALD_BLOCK, 500);
        prices.put(Material.SUGAR, 100);
        prices.put(Material.TNT, 100);
        prices.put(Material.OBSIDIAN, 200);
        prices.put(Material.BLAZE_POWDER, 200);
        prices.put(Material.SHIELD, 200);
        prices.put(Material.SANDSTONE, 1000);

        sellables.put(Material.WHITE_WOOL, wool);
        sellables.put(Material.SAND, sand);
        sellables.put(Material.STONE_SWORD, stonesword);
        sellables.put(Material.BEEF, beef);
        sellables.put(Material.COBWEB, cobweb);
        sellables.put(Material.BOW, bow);
        sellables.put(Material.ARROW, arrow);
        sellables.put(Material.GOLDEN_APPLE, goldenapple);
        sellables.put(Material.POTION, invispotion);
        sellables.put(Material.EMERALD_BLOCK, coingen);
        sellables.put(Material.SUGAR, coingenfast);
        sellables.put(Material.TNT, bomb);
        sellables.put(Material.OBSIDIAN, tower);
        sellables.put(Material.BLAZE_POWDER, towerdmg);
        sellables.put(Material.SHIELD, towerarmor);
        sellables.put(Material.SANDSTONE, sandgen);
    }

}
