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

    public static ItemStack sand;
    public static ItemStack stonesword;
    public static ItemStack beef;
    public static ItemStack cobweb;
    public static ItemStack bow;
    public static ItemStack arrow;
    public static ItemStack goldenapple;
    public static ItemStack iron_chestplate;

    // 新增：设备与升级
    public static ItemStack invispotion;
    public static ItemStack speedpotion;
    public static ItemStack jumppotion;
    public static ItemStack regenpotion;
    public static ItemStack endpeal;

    public static ItemStack coingen;
    public static ItemStack coingenfast;
    public static ItemStack bomb;
    public static ItemStack tower;
    public static ItemStack towerdmg;
    public static ItemStack towerarmor;
    public static ItemStack sandgen;

    // 以干净物品为键定价；同材质不同物品（如 4 瓶药水）因 meta 不同而键不同
    public static Map<ItemStack, Integer> prices = new HashMap<>();

    // 售出的干净物品：以商店格子中实际摆放的展示物品（带价格 lore）为键，点击时反查
    public static Map<ItemStack, ItemStack> sellables = new HashMap<>();

    public static void init(){

        sand = new ItemStack(Material.SAND);
        sand.editMeta(meta -> {
            meta.displayName(miniMessage.deserialize("<!i><b><#e6c35c>沙子"));
            meta.lore(List.of(miniMessage.deserialize("<!i><gray>还能直接买？？？！")));
        });

        stonesword = new ItemStack(Material.STONE_SWORD);
        stonesword.editMeta(meta -> {
            meta.displayName(miniMessage.deserialize("<!i><b><gold>石剑"));
            meta.lore(List.of(miniMessage.deserialize("<!i><gold>更锋利的武器")));
            meta.setUnbreakable(true);
            meta.addAttributeModifier(Attribute.ATTACK_DAMAGE,
                    new AttributeModifier(new NamespacedKey(instance, "stone_sword_damage"), 5.0,
                            AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.HAND));
        });
        stonesword.setData(DataComponentTypes.CAN_BREAK, itemmanager.buildAdventurePredicate(itemmanager.breakableMaterials()));

        beef = new ItemStack(Material.COOKED_BEEF, 3);
        beef.editMeta(meta -> {
            meta.displayName(miniMessage.deserialize("<!i><b><white>牛肉"));
            meta.lore(List.of(miniMessage.deserialize("<!i><gray>补充饥饿值")));
        });

        cobweb = new ItemStack(Material.COBWEB, 3);
        cobweb.editMeta(meta -> {
            meta.displayName(miniMessage.deserialize("<!i><b><white>蜘蛛网"));
            meta.lore(List.of(miniMessage.deserialize("<!i><gray>减缓敌人移动")));
        });
        cobweb.setData(DataComponentTypes.CAN_PLACE_ON, itemmanager.buildAdventurePredicate(
                List.of(Material.SMOOTH_SANDSTONE,
                        Material.MUD_BRICKS,
                        Material.BLUE_ICE,
                        Material.BAMBOO_MOSAIC)));

        bow = new ItemStack(Material.BOW);
        bow.editMeta(meta -> {
            meta.displayName(miniMessage.deserialize("<!i><b><gold>强化弓"));
            meta.lore(List.of(miniMessage.deserialize("<!i><gold>远程压制")));
            meta.setUnbreakable(true);
            meta.addEnchant(Enchantment.POWER,2,true);
        });

        arrow = new ItemStack(Material.ARROW);
        arrow.editMeta(meta -> {
            meta.displayName(miniMessage.deserialize("<!i><b><white>箭矢"));
            meta.lore(List.of(miniMessage.deserialize("<!i><gray>弓的弹药")));
        });

        goldenapple = new ItemStack(Material.GOLDEN_APPLE);
        goldenapple.editMeta(meta -> {
            meta.displayName(miniMessage.deserialize("<!i><b><gold>金苹果"));
            meta.lore(List.of(miniMessage.deserialize("<!i><gold>恢复生命并附加增益")));
        });

        iron_chestplate = new ItemStack(Material.IRON_CHESTPLATE);
        iron_chestplate.editMeta(meta -> {
            meta.displayName(miniMessage.deserialize("<!i><b><gold>铁胸甲"));
            meta.lore(List.of(miniMessage.deserialize("<!i><gold>增加防御")));
            meta.setUnbreakable(true);
        });


        invispotion = new ItemStack(Material.POTION);
        invispotion.editMeta(PotionMeta.class, meta -> {
            meta.displayName(miniMessage.deserialize("<!i><b><#ff55ff>隐身药水"));
            meta.lore(List.of(miniMessage.deserialize("<!i><gray>隐身30秒")));
            meta.setColor(Color.fromRGB(255, 85, 255));
            meta.addCustomEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 20*30, 0, true, true, true), true);
        });

        speedpotion = new ItemStack(Material.POTION);
        speedpotion.editMeta(PotionMeta.class, meta -> {
            meta.displayName(miniMessage.deserialize("<!i><b><#55ff55>速度药水"));
            meta.lore(List.of(miniMessage.deserialize("<!i><gray>速度30秒")));
            meta.setColor(Color.fromRGB(85, 255, 85));
            meta.addCustomEffect(new PotionEffect(PotionEffectType.SPEED, 20*30, 0, true, true, true), true);
        });

        jumppotion = new ItemStack(Material.POTION);
        jumppotion.editMeta(PotionMeta.class, meta -> {
            meta.displayName(miniMessage.deserialize("<!i><b><#55ff55>跳跃药水"));
            meta.lore(List.of(miniMessage.deserialize("<!i><gray>跳跃30秒")));
            meta.setColor(Color.fromRGB(85, 255, 85));
            meta.addCustomEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 20*30, 0, true, true, true), true);
        });

        regenpotion = new ItemStack(Material.POTION);
        regenpotion.editMeta(PotionMeta.class, meta -> {
            meta.displayName(miniMessage.deserialize("<!i><b><#55ff55>恢复生命药水"));
            meta.lore(List.of(miniMessage.deserialize("<!i><gray>恢复生命30秒")));
            meta.setColor(Color.fromRGB(85, 255, 85));
            meta.addCustomEffect(new PotionEffect(PotionEffectType.REGENERATION, 20*30, 0, true, true, true), true);
        });

        endpeal = new ItemStack(Material.ENDER_PEARL);
        endpeal.editMeta(meta -> {
            meta.displayName(miniMessage.deserialize("<!i><b><white>末影珍珠"));
            meta.lore(List.of(miniMessage.deserialize("<!i><gray>传送")));
        });

        coingen = new ItemStack(Material.EMERALD_BLOCK);
        coingen.editMeta(meta -> {
            meta.displayName(miniMessage.deserialize("<!i><b><green>金币生成器"));
            meta.lore(List.of(miniMessage.deserialize("<!i><gray>右键放置，每3秒生成1个绿宝石")));
        });

        coingenfast = new ItemStack(Material.SUGAR);
        coingenfast.editMeta(meta -> {
            meta.displayName(miniMessage.deserialize("<!i><b><yellow>金币生成器速度加强插件"));
            meta.lore(List.of(miniMessage.deserialize("<!i><gray>右键金币生成器，加速为每1秒1个")));
        });

        bomb = new ItemStack(Material.TNT);
        bomb.editMeta(meta -> {
            meta.displayName(miniMessage.deserialize("<!i><b><red>炸弹"));
            meta.lore(List.of(miniMessage.deserialize("<!i><gray>右键引爆，拆除3格内设备")));
        });

        tower = new ItemStack(Material.OBSIDIAN);
        tower.editMeta(meta -> {
            meta.displayName(miniMessage.deserialize("<!i><b><dark_purple>防御塔"));
            meta.lore(List.of(miniMessage.deserialize("<!i><gray>右键放置，攻击5格内敌人")));
        });

        towerdmg = new ItemStack(Material.BLAZE_POWDER);
        towerdmg.editMeta(meta -> {
            meta.displayName(miniMessage.deserialize("<!i><b><gold>防御塔攻击加强插件"));
            meta.lore(List.of(miniMessage.deserialize("<!i><gray>右键防御塔，伤害翻倍为14")));
        });

        towerarmor = new ItemStack(Material.SHIELD);
        towerarmor.editMeta(meta -> {
            meta.displayName(miniMessage.deserialize("<!i><b><aqua>防御塔护甲加强插件"));
            meta.lore(List.of(miniMessage.deserialize("<!i><gray>右键设备，可抵挡1次炸弹")));
        });

        sandgen = new ItemStack(Material.SANDSTONE);
        sandgen.editMeta(meta -> {
            meta.displayName(miniMessage.deserialize("<!i><b><yellow>沙子生成器"));
            meta.lore(List.of(miniMessage.deserialize("<!i><gray>右键放置，每30秒生成1个沙子")));
        });


        prices.put(sand, 1000);
        prices.put(stonesword, 300);
        prices.put(beef, 50);
        prices.put(cobweb, 150);
        prices.put(bow, 300);
        prices.put(arrow, 10);
        prices.put(goldenapple, 150);
        prices.put(invispotion, 300);
        prices.put(speedpotion, 300);
        prices.put(jumppotion, 300);
        prices.put(regenpotion, 300);
        prices.put(coingen, 300);
        prices.put(coingenfast, 300);
        prices.put(bomb, 300);
        prices.put(tower, 300);
        prices.put(towerdmg, 300);
        prices.put(towerarmor, 100);
        prices.put(sandgen, 2000);
        prices.put(iron_chestplate, 200);
        prices.put(endpeal, 1000);
        // sellables 以展示物品为键，由 ShopMenu.setShopItem 登记
    }

}
