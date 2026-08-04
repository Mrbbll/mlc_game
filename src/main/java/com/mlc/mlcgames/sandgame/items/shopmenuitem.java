package com.mlc.mlcgames.sandgame.items;

import io.papermc.paper.datacomponent.DataComponentTypes;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;

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

    // 按物品类型定价（价格为一份商品的价格）
    public static Map<Material, Integer> prices = new HashMap<>();

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

        prices.put(Material.WHITE_WOOL, 30);
        prices.put(Material.SAND, 10);
        prices.put(Material.STONE_SWORD, 40);
        prices.put(Material.BEEF, 15);
        prices.put(Material.COBWEB, 20);
        prices.put(Material.BOW, 50);
        prices.put(Material.ARROW, 5);
        prices.put(Material.GOLDEN_APPLE, 80);
    }

}
