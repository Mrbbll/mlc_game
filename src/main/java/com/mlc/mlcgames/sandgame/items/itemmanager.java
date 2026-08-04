package com.mlc.mlcgames.sandgame.items;

import com.mlc.mlcgames.Teammanager;
import com.mlc.mlcgames.sandgame.Sandgame;
import io.papermc.paper.block.BlockPredicate;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemAdventurePredicate;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.set.RegistrySet;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.BlockType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.mlc.mlcgames.Mlcgames.instance;
import static com.mlc.mlcgames.Mlcgames.miniMessage;

public class itemmanager {
    public static ItemStack sand;
    public static ItemStack baseitem_sword;
    public static ItemStack baseitem_shovel;
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

    // 剑/剪刀在冒险模式下可破坏的方块：所有羊毛 + 蜘蛛网 + 沙子
    static List<Material> breakableMaterials(){
        List<Material> list = new ArrayList<>();
        for (Material m : Material.values()) {
            if (m.name().endsWith("_WOOL")) {
                list.add(m);
            }
        }
        list.add(Material.COBWEB);
        list.add(Material.SAND);
        return list;
    }

    // 把 Material 列表转成冒险模式破坏/放置谓词（Data Component API，替代弃用的 setCanDestroy/setCanPlaceOn）
    static ItemAdventurePredicate buildAdventurePredicate(List<Material> materials) {
        List<BlockType> blocks = materials.stream()
                .map(m -> Objects.requireNonNull(m.asBlockType()))
                .toList();
        BlockPredicate predicate = BlockPredicate.predicate()
                .blocks(RegistrySet.keySetFromValues(RegistryKey.BLOCK, blocks))
                .build();
        return ItemAdventurePredicate.itemAdventurePredicate(List.of(predicate));
    }

    public static void init(){
        sand = new ItemStack(Material.SAND);
        sand.editMeta(meta -> {
            meta.displayName(miniMessage.deserialize("<b><#e6c35c>沙子"));
            meta.lore(List.of(miniMessage.deserialize("<gray>建筑与掩体材料")));
        });

        baseitem_sword = new ItemStack(Material.WOODEN_SWORD);
        baseitem_sword.editMeta(meta -> {
            meta.displayName(miniMessage.deserialize("<b><white>木剑"));
            meta.lore(List.of(miniMessage.deserialize("<gray>最基本的武器")));
            meta.setUnbreakable(true);
            meta.addAttributeModifier(Attribute.ATTACK_DAMAGE,
                    new AttributeModifier(new NamespacedKey(instance, "base_sword_damage"), 3.0,
                            AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.HAND));
        });
        baseitem_sword.setData(DataComponentTypes.CAN_BREAK, buildAdventurePredicate(breakableMaterials()));

        baseitem_shovel = new ItemStack(Material.WOODEN_SHOVEL);
        baseitem_shovel.editMeta(meta -> {
            meta.displayName(miniMessage.deserialize("<b><#e6c35c>木锹"));
            meta.lore(List.of(miniMessage.deserialize("<gray>只能破坏沙子")));
            meta.setUnbreakable(true);
        });
        baseitem_shovel.setData(DataComponentTypes.CAN_BREAK, buildAdventurePredicate(List.of(Material.SAND)));

        team_1_wool = new ItemStack(Material.RED_WOOL);
        team_1_wool.editMeta(meta -> {
            meta.displayName(miniMessage.deserialize("<b><#ff5555>红队徽章"));
            meta.lore(List.of(miniMessage.deserialize("<gray>点击加入红队")));
        });

        team_2_wool = new ItemStack(Material.BLUE_WOOL);
        team_2_wool.editMeta(meta -> {
            meta.displayName(miniMessage.deserialize("<b><#55aaff>蓝队徽章"));
            meta.lore(List.of(miniMessage.deserialize("<gray>点击加入蓝队")));
        });

        baseitem_helmet_1 = new ItemStack(Material.LEATHER_HELMET);
        baseitem_helmet_1.editMeta(LeatherArmorMeta.class, meta -> {
            meta.displayName(miniMessage.deserialize("<b><#ff5555>红队皮甲头盔"));
            meta.lore(List.of(miniMessage.deserialize("<gray>红队基础护甲")));
            meta.setUnbreakable(true);
            meta.setColor(Color.fromRGB(200, 60, 60));
            meta.addAttributeModifier(Attribute.ARMOR,
                    new AttributeModifier(new NamespacedKey(instance, "helmet_1_armor"), 10.0,
                            AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.HEAD));
        });

        baseitem_helmet_2 = new ItemStack(Material.LEATHER_HELMET);
        baseitem_helmet_2.editMeta(LeatherArmorMeta.class, meta -> {
            meta.displayName(miniMessage.deserialize("<b><#55aaff>蓝队皮甲头盔"));
            meta.lore(List.of(miniMessage.deserialize("<gray>蓝队基础护甲")));
            meta.setUnbreakable(true);
            meta.setColor(Color.fromRGB(60, 100, 230));
            meta.addAttributeModifier(Attribute.ARMOR,
                    new AttributeModifier(new NamespacedKey(instance, "helmet_2_armor"), 10.0,
                            AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.HEAD));
        });

        baseitem_shears = new ItemStack(Material.SHEARS);
        baseitem_shears.editMeta(meta -> {
            meta.displayName(miniMessage.deserialize("<b><white>剪刀"));
            meta.lore(List.of(miniMessage.deserialize("<gray>只能破坏羊毛/蜘蛛网/沙子")));
            meta.setUnbreakable(true);
        });
        baseitem_shears.setData(DataComponentTypes.CAN_BREAK, buildAdventurePredicate(breakableMaterials()));

        arrow = new ItemStack(Material.ARROW);
        arrow.editMeta(meta -> {
            meta.displayName(miniMessage.deserialize("<b><white>箭矢"));
            meta.lore(List.of(miniMessage.deserialize("<gray>弓的弹药")));
        });

        bow = new ItemStack(Material.BOW);
        bow.editMeta(meta -> {
            meta.displayName(miniMessage.deserialize("<b><white>弓"));
            meta.lore(List.of(miniMessage.deserialize("<gray>远程射击")));
            meta.setUnbreakable(true);
        });

        potion_speed = new ItemStack(Material.POTION);
        potion_speed.editMeta(PotionMeta.class, meta -> {
            meta.displayName(miniMessage.deserialize("<b><#55ffff>迅捷药水"));
            meta.lore(List.of(miniMessage.deserialize("<gray>提升移动速度")));
            meta.setColor(Color.fromRGB(85, 255, 255));
            meta.addCustomEffect(new PotionEffect(PotionEffectType.SPEED, 20*30, 1, true, true, true), true);
        });

        potion_jump = new ItemStack(Material.POTION);
        potion_jump.editMeta(PotionMeta.class, meta -> {
            meta.displayName(miniMessage.deserialize("<b><#88ff88>跳跃药水"));
            meta.lore(List.of(miniMessage.deserialize("<gray>提升跳跃高度")));
            meta.setColor(Color.fromRGB(136, 255, 136));
            meta.addCustomEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 20*30, 1, true, true, true), true);
        });

        potion_regeneration = new ItemStack(Material.POTION);
        potion_regeneration.editMeta(PotionMeta.class, meta -> {
            meta.displayName(miniMessage.deserialize("<b><#ff55ff>再生药水"));
            meta.lore(List.of(miniMessage.deserialize("<gray>缓慢恢复生命")));
            meta.setColor(Color.fromRGB(255, 85, 255));
            meta.addCustomEffect(new PotionEffect(PotionEffectType.REGENERATION, 20*30, 0, true, true, true), true);
        });

        beef = new ItemStack(Material.BEEF);
        beef.editMeta(meta -> {
            meta.displayName(miniMessage.deserialize("<b><white>生牛肉"));
            meta.lore(List.of(miniMessage.deserialize("<gray>补充饥饿值")));
        });

        wool_1 = new ItemStack(Material.RED_WOOL);
        wool_1.editMeta(meta -> {
            meta.displayName(miniMessage.deserialize("<b><#ff5555>红色羊毛"));
            meta.lore(List.of(miniMessage.deserialize("<gray>建筑与掩体材料")));
        });
        wool_1.setData(DataComponentTypes.CAN_PLACE_ON, buildAdventurePredicate(List.of(Material.SAND)));
        wool_1.setAmount(64);

        wool_2 = new ItemStack(Material.BLUE_WOOL);
        wool_2.editMeta(meta -> {
            meta.displayName(miniMessage.deserialize("<b><#55aaff>蓝色羊毛"));
            meta.lore(List.of(miniMessage.deserialize("<gray>建筑与掩体材料")));
        });
        wool_2.setData(DataComponentTypes.CAN_PLACE_ON, buildAdventurePredicate(List.of(Material.SAND)));
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
        player.getInventory().addItem(baseitem_shovel);
        player.getInventory().addItem(baseitem_shears);
        player.getInventory().addItem(arrow);
        player.getInventory().addItem(bow);
        player.getInventory().addItem(beef);
    }

    public static void spawnsand(Location loc){
        loc.getWorld().dropItem(loc, sand);
        instance.getServer().broadcast(Sandgame.sand_spawn_msg);
    }

    public static void spawnitem(Location loc){
        loc.getWorld().dropItem(loc, baseitem_sword);
    }
}
