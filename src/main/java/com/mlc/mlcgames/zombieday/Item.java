package com.mlc.mlcgames.zombieday;

import com.mlc.mlcgames.utils.item.Gun;
import io.papermc.paper.block.BlockPredicate;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemAdventurePredicate;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.set.RegistryKeySet;
import io.papermc.paper.registry.set.RegistrySet;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.BlockType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.mlc.mlcgames.Mlcgames.instance;
import static com.mlc.mlcgames.Mlcgames.miniMessage;

public class Item {
    public static final NamespacedKey bulletcountkey = new NamespacedKey(instance,"bulletcount");
    public static final NamespacedKey maxbulletcountkey = new NamespacedKey(instance,"maxbulletcount");
    public static final NamespacedKey itemtype = new NamespacedKey(instance,"itemtype");
    public static final NamespacedKey needbullettypekey = new NamespacedKey(instance,"needbullettype");
    public static final NamespacedKey bullettypekey = new NamespacedKey(instance,"bullettype");

    public static ItemStack handgun;
    public static ItemStack shotgun;
    public static ItemStack rifle;
    public static ItemStack submachine_gun;
    public static ItemStack bow;
    public static ItemStack crossbow;
    public static ItemStack bazooka;
    public static ItemStack howitzer;
    public static ItemStack grenade;
    public static ItemStack key;
    public static ItemStack bullet;
    public static ItemStack arrow;
    public static ItemStack feather;
    public static ItemStack iron_axe;
    public static ItemStack iron_shovel;
    public static ItemStack iron_sword;
    public static ItemStack iron_chestplate;
    public static ItemStack iron_leggings;
    public static ItemStack iron_boots;
    public static ItemStack iron_helmet;
    public static ItemStack lether_chestplate;
    public static ItemStack lether_leggings;
    public static ItemStack lether_boots;
    public static ItemStack lether_helmet;
    public static ItemStack neitherite_helmet;
    public static ItemStack neitherite_chestplate;
    public static ItemStack neitherite_leggings;
    public static ItemStack neitherite_boots;
    public static ItemStack speed_potion;
    public static ItemStack strength_potion;
    public static ItemStack regeneration_potion;
    public static ItemStack instant_health_potion;
    public static ItemStack golden_apple;
    public static ItemStack speed_potion_splash;
    public static ItemStack strength_potion_splash;
    public static ItemStack regeneration_potion_splash;
    public static ItemStack instant_health_potion_splash;
    public static ItemStack emerald;
    public static List<ItemStack> itemlist;
    public static ItemStack slow_potion_splash;


    public static void init(){
        handgun = new ItemStack(Material.ECHO_SHARD);
        Gun.settypedata(handgun, "handgun");
        Gun.setneedbullettype(handgun, "normal");
        Gun.setbulletcount(handgun, 8);
        Gun.setmaxbulletcount(handgun, 8);
        Gun.setRefilltime(handgun,4000L);
        ItemMeta itemMeta = handgun.getItemMeta();
        itemMeta.setMaxStackSize(1);
        itemMeta.customName(miniMessage.deserialize("<!i>手枪"));
        itemMeta.setItemModel(NamespacedKey.fromString("mlcgames:gun/handgun"));
        handgun.setItemMeta(itemMeta);

        rifle = new ItemStack(Material.ECHO_SHARD);
        Gun.settypedata(rifle, "rifle");
        Gun.setneedbullettype(rifle, "normal");
        Gun.setbulletcount(rifle, 30);
        Gun.setmaxbulletcount(rifle, 30);
        Gun.setRefilltime(rifle,4000L);
        ItemMeta itemMeta1 = rifle.getItemMeta();
        itemMeta1.setMaxStackSize(1);
        itemMeta1.customName(miniMessage.deserialize("<!i>步枪"));
        itemMeta1.setItemModel(NamespacedKey.fromString("mlcgames:gun/rifle"));
        rifle.setItemMeta(itemMeta1);


        shotgun = new ItemStack(Material.ECHO_SHARD);
        Gun.settypedata(shotgun, "shotgun");
        Gun.setneedbullettype(shotgun, "normal");
        Gun.setbulletcount(shotgun, 4);
        Gun.setmaxbulletcount(shotgun, 4);
        Gun.setRefilltime(shotgun,3000L);
        ItemMeta itemMeta2 = shotgun.getItemMeta();
        itemMeta2.setMaxStackSize(1);
        itemMeta2.customName(miniMessage.deserialize("<!i>霰弹枪"));
        itemMeta2.setItemModel(NamespacedKey.fromString("mlcgames:gun/shotgun"));
        shotgun.setItemMeta(itemMeta2);


        submachine_gun = new ItemStack(Material.ECHO_SHARD);
        Gun.settypedata(submachine_gun, "submachine_gun");
        Gun.setneedbullettype(submachine_gun, "normal");
        Gun.setbulletcount(submachine_gun, 40);
        Gun.setmaxbulletcount(submachine_gun, 40);
        Gun.setRefilltime(submachine_gun,4000L);
        ItemMeta itemMeta3 = submachine_gun.getItemMeta();
        itemMeta3.setMaxStackSize(1);
        itemMeta3.customName(miniMessage.deserialize("<!i>冲锋枪"));
        itemMeta3.setItemModel(NamespacedKey.fromString("mlcgames:gun/submachine_gun"));
        submachine_gun.setItemMeta(itemMeta3);

        bow = new ItemStack(Material.BOW);
        ItemMeta itemMeta4 = bow.getItemMeta();
        itemMeta4.setUnbreakable(true);
        bow.setItemMeta(itemMeta4);

        crossbow = new ItemStack(Material.CROSSBOW);
        ItemMeta itemMeta5 = crossbow.getItemMeta();
        itemMeta5.setUnbreakable(true);
        crossbow.setItemMeta(itemMeta5);


        bazooka = ItemStack.of(Material.ECHO_SHARD);
        Gun.settypedata(bazooka, "bazooka");
        Gun.setneedbullettype(bazooka, "explode");
        Gun.setmaxbulletcount(bazooka, 1);
        Gun.setbulletcount(bazooka, 0);
        Gun.setRefilltime(bazooka,3000L);
        ItemMeta itemMeta37 = bazooka.getItemMeta();
        itemMeta37.setMaxStackSize(1);
        itemMeta37.customName(miniMessage.deserialize("<!i>火箭筒"));
        itemMeta37.setItemModel(NamespacedKey.fromString("mlcgames:gun/bazooka"));
        bazooka.setItemMeta(itemMeta37);

        howitzer = ItemStack.of(Material.STONE_BUTTON);
        Gun.setbullettype(howitzer, "explode");
        ItemMeta itemMeta38 = howitzer.getItemMeta();
        itemMeta38.setMaxStackSize(4);
        itemMeta38.customName(miniMessage.deserialize("<!i>火箭筒弹药"));
        itemMeta38.setItemModel(NamespacedKey.fromString("mlcgames:gun/howitzer"));
        howitzer.setItemMeta(itemMeta38);



        grenade = new ItemStack(Material.EGG);
        Gun.settypedata(grenade, "grenade");
        ItemMeta itemMeta6 = grenade.getItemMeta();
        itemMeta6.setMaxStackSize(1);
        itemMeta6.customName(miniMessage.deserialize("<!i>手榴弹"));
        grenade.setItemMeta(itemMeta6);

        bullet = new ItemStack(Material.STONE_BUTTON);
        Gun.setbullettype(bullet, "normal");
        ItemMeta itemMeta7 = bullet.getItemMeta();
        itemMeta7.customName(miniMessage.deserialize("<!i>通用子弹"));
        bullet.setItemMeta(itemMeta7);

        arrow = new ItemStack(Material.ARROW);
        ItemMeta itemMeta8 = arrow.getItemMeta();

        arrow.setItemMeta(itemMeta8);
        arrow.setAmount(16);

        key = new ItemStack(Material.TRIAL_KEY);
        ItemMeta itemMeta30 = key.getItemMeta();
        itemMeta30.customName(miniMessage.deserialize("<!i>区域钥匙"));
        List<Component> lore = new ArrayList<>();
        lore.add(miniMessage.deserialize("<!i>用于解锁任一区域"));
        itemMeta30.lore(lore);
        key.setItemMeta(itemMeta30);


        feather = new ItemStack(Material.FEATHER);

        iron_sword = ItemStack.of(Material.IRON_SWORD);
        ItemMeta itemMeta9 = iron_sword.getItemMeta();
        itemMeta9.setUnbreakable(true);
        AttributeModifier attributeModifier1 =
                new AttributeModifier(Objects.requireNonNull(NamespacedKey.fromString("mlcgames:swordattackspeed")),
                        -3,
                        AttributeModifier.Operation.ADD_NUMBER,
                        EquipmentSlotGroup.MAINHAND);
        itemMeta9.addAttributeModifier(Attribute.ATTACK_SPEED,attributeModifier1);
        AttributeModifier attributeModifier2 =
                new AttributeModifier(Objects.requireNonNull(NamespacedKey.fromString("mlcgames:swordattackdamage")),
                        7,
                        AttributeModifier.Operation.ADD_NUMBER,
                        EquipmentSlotGroup.MAINHAND);

        itemMeta9.addAttributeModifier(Attribute.ATTACK_DAMAGE,attributeModifier2);
        iron_sword.setItemMeta(itemMeta9);

        iron_chestplate = new ItemStack(Material.IRON_CHESTPLATE);
        ItemMeta itemMeta10 = iron_chestplate.getItemMeta();
        itemMeta10.setUnbreakable(true);
        iron_chestplate.setItemMeta(itemMeta10);

        iron_leggings = new ItemStack(Material.IRON_LEGGINGS);
        ItemMeta itemMeta11 = iron_leggings.getItemMeta();
        itemMeta11.setUnbreakable(true);
        iron_leggings.setItemMeta(itemMeta11);

        iron_boots = new ItemStack(Material.IRON_BOOTS);
        ItemMeta itemMeta12 = iron_boots.getItemMeta();
        itemMeta12.setUnbreakable(true);
        iron_boots.setItemMeta(itemMeta12);

        iron_helmet = new ItemStack(Material.IRON_HELMET);
        ItemMeta itemMeta13 = iron_helmet.getItemMeta();
        itemMeta13.setUnbreakable(true);
        iron_helmet.setItemMeta(itemMeta13);

        lether_chestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
        ItemMeta itemMeta14 = lether_chestplate.getItemMeta();
        itemMeta14.setUnbreakable(true);
        lether_chestplate.setItemMeta(itemMeta14);

        lether_leggings = new ItemStack(Material.LEATHER_LEGGINGS);
        ItemMeta itemMeta15 = lether_leggings.getItemMeta();
        itemMeta15.setUnbreakable(true);
        lether_leggings.setItemMeta(itemMeta15);

        lether_boots = new ItemStack(Material.LEATHER_BOOTS);
        ItemMeta itemMeta16 = lether_boots.getItemMeta();
        itemMeta16.setUnbreakable(true);
        lether_boots.setItemMeta(itemMeta16);

        lether_helmet = new ItemStack(Material.LEATHER_HELMET);
        ItemMeta itemMeta17 = lether_helmet.getItemMeta();
        itemMeta17.setUnbreakable(true);
        lether_helmet.setItemMeta(itemMeta17);



        RegistryKeySet<@NotNull BlockType> blocks = RegistrySet.keySetFromValues(
                RegistryKey.BLOCK,
                Set.of(BlockType.OAK_FENCE, BlockType.OAK_LOG, BlockType.GRAVEL));
        List<BlockPredicate> breakable = List.of(
                BlockPredicate.predicate().blocks(blocks).build()
        );
        ItemAdventurePredicate predicate = ItemAdventurePredicate.itemAdventurePredicate(breakable);


        iron_axe = new ItemStack(Material.IRON_AXE);
        ItemMeta itemMeta19 = iron_axe.getItemMeta();
        AttributeModifier attributeModifier3 =
                new AttributeModifier(Objects.requireNonNull(NamespacedKey.fromString("mlcgames:swordattackspeed")),
                        -3.9,
                        AttributeModifier.Operation.ADD_NUMBER,
                        EquipmentSlotGroup.MAINHAND);
        itemMeta19.setUnbreakable(true);
        itemMeta19.addAttributeModifier(Attribute.ATTACK_SPEED,attributeModifier3);
        iron_axe.setItemMeta(itemMeta19);
        iron_axe.setData(DataComponentTypes.CAN_BREAK, predicate);

        iron_shovel = new ItemStack(Material.IRON_SHOVEL);
        ItemMeta itemMeta20 = iron_shovel.getItemMeta();
        AttributeModifier attributeModifier4 =
                new AttributeModifier(Objects.requireNonNull(NamespacedKey.fromString("mlcgames:swordattackspeed")),
                        -3.9,
                        AttributeModifier.Operation.ADD_NUMBER,
                        EquipmentSlotGroup.MAINHAND);
        itemMeta20.addAttributeModifier(Attribute.ATTACK_SPEED,attributeModifier4);
        itemMeta20.setUnbreakable(true);
        iron_shovel.setItemMeta(itemMeta20);
        iron_shovel.setData(DataComponentTypes.CAN_BREAK, predicate);

        neitherite_helmet = new ItemStack(Material.NETHERITE_HELMET);
        ItemMeta itemMeta22 = neitherite_helmet.getItemMeta();
        itemMeta22.setUnbreakable(true);
        neitherite_helmet.setItemMeta(itemMeta22);

        neitherite_chestplate = new ItemStack(Material.NETHERITE_CHESTPLATE);
        ItemMeta itemMeta23 = neitherite_chestplate.getItemMeta();
        itemMeta23.setUnbreakable(true);
        neitherite_chestplate.setItemMeta(itemMeta23);

        neitherite_leggings = new ItemStack(Material.NETHERITE_LEGGINGS);
        ItemMeta itemMeta24 = neitherite_leggings.getItemMeta();
        itemMeta24.setUnbreakable(true);
        neitherite_leggings.setItemMeta(itemMeta24);

        neitherite_boots = new ItemStack(Material.NETHERITE_BOOTS);
        ItemMeta itemMeta25 = neitherite_boots.getItemMeta();
        itemMeta25.setUnbreakable(true);
        neitherite_boots.setItemMeta(itemMeta25);

        emerald = new ItemStack(Material.EMERALD);
        ItemMeta itemMeta21 = emerald.getItemMeta();
        itemMeta21.customName(miniMessage.deserialize("<!i>货币"));
        List<Component> lore1 = List.of(miniMessage.deserialize("<!i>或许可以在贩卖机使用"));
        itemMeta21.lore(lore1);
        emerald.setItemMeta(itemMeta21);

        speed_potion = new ItemStack(Material.POTION);
        ItemMeta itemMeta26 = speed_potion.getItemMeta();
        itemMeta26.customName(miniMessage.deserialize("<!i>速度药水"));
        PotionMeta potionMeta = (PotionMeta) itemMeta26;
        potionMeta.addCustomEffect(new PotionEffect(PotionEffectType.SPEED, 20*120, 0, true, false), true);
        speed_potion.setItemMeta(itemMeta26);

        strength_potion = new ItemStack(Material.POTION);
        ItemMeta itemMeta27 = strength_potion.getItemMeta();
        itemMeta27.customName(miniMessage.deserialize("<!i>力量药水"));
        PotionMeta potionMeta1 = (PotionMeta) itemMeta27;
        potionMeta1.addCustomEffect(new PotionEffect(PotionEffectType.STRENGTH, 20*120, 0, true, false), true);
        strength_potion.setItemMeta(itemMeta27);

        regeneration_potion = new ItemStack(Material.POTION);
        ItemMeta itemMeta28 = regeneration_potion.getItemMeta();
        itemMeta28.customName(miniMessage.deserialize("<!i>再生药水"));
        PotionMeta potionMeta2 = (PotionMeta) itemMeta28;
        potionMeta2.addCustomEffect(new PotionEffect(PotionEffectType.REGENERATION, 20*120, 0, true, false), true);
        regeneration_potion.setItemMeta(itemMeta28);

        instant_health_potion = new ItemStack(Material.POTION);
        ItemMeta itemMeta29 = instant_health_potion.getItemMeta();
        itemMeta29.customName(miniMessage.deserialize("<!i>治疗药水"));
        PotionMeta potionMeta3 = (PotionMeta) itemMeta29;
        potionMeta3.addCustomEffect(new PotionEffect(PotionEffectType.INSTANT_HEALTH, 20, 0, true, false), true);
        instant_health_potion.setItemMeta(itemMeta29);

        golden_apple = new ItemStack(Material.GOLDEN_APPLE);
        ItemMeta itemMeta36 = golden_apple.getItemMeta();
        itemMeta36.customName(miniMessage.deserialize("<!i>金苹果"));
        golden_apple.setItemMeta(itemMeta36);

        speed_potion_splash = new ItemStack(Material.SPLASH_POTION);
        ItemMeta itemMeta31 = speed_potion_splash.getItemMeta();
        itemMeta31.customName(miniMessage.deserialize("<!i>速度药水"));
        PotionMeta potionMeta4 = (PotionMeta) itemMeta31;
        potionMeta4.addCustomEffect(new PotionEffect(PotionEffectType.SPEED, 20*120, 0, true, false), true);
        speed_potion_splash.setItemMeta(itemMeta31);

        strength_potion_splash = new ItemStack(Material.SPLASH_POTION);
        ItemMeta itemMeta32 = strength_potion_splash.getItemMeta();
        itemMeta32.customName(miniMessage.deserialize("<!i>力量药水"));
        PotionMeta potionMeta5 = (PotionMeta) itemMeta32;
        potionMeta5.addCustomEffect(new PotionEffect(PotionEffectType.STRENGTH, 20*120, 0, true, false), true);
        strength_potion_splash.setItemMeta(itemMeta32);

        regeneration_potion_splash = new ItemStack(Material.SPLASH_POTION);
        ItemMeta itemMeta33 = regeneration_potion_splash.getItemMeta();
        itemMeta33.customName(miniMessage.deserialize("<!i>再生药水"));
        PotionMeta potionMeta6 = (PotionMeta) itemMeta33;
        potionMeta6.addCustomEffect(new PotionEffect(PotionEffectType.REGENERATION, 20*120, 0, true, false), true);
        regeneration_potion_splash.setItemMeta(itemMeta33);

        instant_health_potion_splash = new ItemStack(Material.SPLASH_POTION);
        ItemMeta itemMeta34 = instant_health_potion_splash.getItemMeta();
        itemMeta34.customName(miniMessage.deserialize("<!i>治疗药水"));
        PotionMeta potionMeta7 = (PotionMeta) itemMeta34;
        potionMeta7.addCustomEffect(new PotionEffect(PotionEffectType.INSTANT_HEALTH, 20, 0, true, false), true);
        instant_health_potion_splash.setItemMeta(itemMeta34);

        slow_potion_splash = new ItemStack(Material.SPLASH_POTION);
        ItemMeta itemMeta35 = slow_potion_splash.getItemMeta();
        itemMeta35.customName(miniMessage.deserialize("<!i>减速药水"));
        PotionMeta potionMeta8 = (PotionMeta) itemMeta35;
        potionMeta8.addCustomEffect(new PotionEffect(PotionEffectType.SLOWNESS, 20*120, 0, true, false), true);
        slow_potion_splash.setItemMeta(itemMeta35);

        itemlist = List.of(handgun,
                rifle,
                shotgun,
                submachine_gun,
                bow,
                crossbow,
                grenade,
                bullet,
                bazooka,
                howitzer,
                arrow,
                iron_sword,
                iron_chestplate,
                iron_leggings,
                iron_boots
                ,iron_helmet
                ,lether_chestplate
                ,lether_leggings
                ,lether_boots
                ,lether_helmet
                ,iron_axe
                ,iron_shovel
                ,neitherite_helmet
                ,neitherite_chestplate
                ,neitherite_leggings
                ,neitherite_boots,
                speed_potion,
                strength_potion,
                regeneration_potion,
                instant_health_potion,
                speed_potion_splash,
                strength_potion_splash,
                regeneration_potion_splash,
                instant_health_potion_splash,
                golden_apple,
                emerald);
    }

    public static void giveitem(Player player) {
        player.getInventory().addItem(iron_sword);
        player.getInventory().setHelmet(lether_helmet);
        player.getInventory().addItem(iron_axe);
        player.getInventory().addItem(iron_shovel);
        player.getInventory().addItem(bow);
        player.getInventory().addItem(arrow);
        player.getInventory().addItem(arrow);
        player.getInventory().addItem(arrow);
        player.getInventory().addItem(ItemStack.of(Material.COOKED_BEEF,16));
    }
}
