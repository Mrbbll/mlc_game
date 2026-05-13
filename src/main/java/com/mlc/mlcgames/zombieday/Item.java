package com.mlc.mlcgames.zombieday;

import com.mlc.mlcgames.utils.item.Gun;
import io.papermc.paper.block.BlockPredicate;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemAdventurePredicate;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.set.RegistryKeySet;
import io.papermc.paper.registry.set.RegistrySet;
import net.kyori.adventure.key.Key;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.BlockType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.mlc.mlcgames.Mlcgames.instance;
import static com.mlc.mlcgames.Mlcgames.miniMessage;

public class Item {
    public static final NamespacedKey bulletcountkey = new NamespacedKey(instance,"bulletcount");
    public static final NamespacedKey maxbulletcountkey = new NamespacedKey(instance,"maxbulletcount");
    public static final NamespacedKey itemtype = new NamespacedKey(instance,"itemtype");

    public static ItemStack handgun;
    public static ItemStack shotgun;
    public static ItemStack rifle;
    public static ItemStack submachine_gun;
    public static ItemStack bow;
    public static ItemStack crossbow;
    public static ItemStack grenade;
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
    public static List<ItemStack> itemlist;


    public static void init(){
        handgun = new ItemStack(Material.ECHO_SHARD);
        Gun.settypedata(handgun, "handgun");
        Gun.setbulletcount(handgun, 8);
        Gun.setmaxbulletcount(handgun, 8);
        ItemMeta itemMeta = handgun.getItemMeta();
        itemMeta.setMaxStackSize(1);
        itemMeta.customName(miniMessage.deserialize("<!i>手枪"));
        handgun.setItemMeta(itemMeta);

        rifle = new ItemStack(Material.ECHO_SHARD);
        Gun.settypedata(rifle, "rifle");
        Gun.setbulletcount(rifle, 30);
        Gun.setmaxbulletcount(rifle, 30);
        ItemMeta itemMeta1 = rifle.getItemMeta();
        itemMeta1.setMaxStackSize(1);
        itemMeta1.customName(miniMessage.deserialize("<!i>步枪"));
        rifle.setItemMeta(itemMeta1);


        shotgun = new ItemStack(Material.ECHO_SHARD);
        Gun.settypedata(shotgun, "shotgun");
        Gun.setbulletcount(shotgun, 4);
        Gun.setmaxbulletcount(shotgun, 4);
        ItemMeta itemMeta2 = shotgun.getItemMeta();
        itemMeta2.setMaxStackSize(1);
        itemMeta2.customName(miniMessage.deserialize("<!i>霰弹枪"));
        shotgun.setItemMeta(itemMeta2);


        submachine_gun = new ItemStack(Material.ECHO_SHARD);
        Gun.settypedata(submachine_gun, "submachine_gun");
        Gun.setbulletcount(submachine_gun, 40);
        Gun.setmaxbulletcount(submachine_gun, 40);
        ItemMeta itemMeta3 = submachine_gun.getItemMeta();
        itemMeta3.setMaxStackSize(1);
        itemMeta3.customName(miniMessage.deserialize("<!i>冲锋枪"));
        submachine_gun.setItemMeta(itemMeta3);

        bow = new ItemStack(Material.BOW);
        ItemMeta itemMeta4 = bow.getItemMeta();
        itemMeta4.setUnbreakable(true);
        bow.setItemMeta(itemMeta4);

        crossbow = new ItemStack(Material.CROSSBOW);
        ItemMeta itemMeta5 = crossbow.getItemMeta();
        itemMeta5.setUnbreakable(true);
        crossbow.setItemMeta(itemMeta5);


        grenade = new ItemStack(Material.EGG);
        Gun.settypedata(grenade, "grenade");
        ItemMeta itemMeta6 = grenade.getItemMeta();
        itemMeta6.setMaxStackSize(1);
        itemMeta6.customName(miniMessage.deserialize("<!i>手榴弹"));
        grenade.setItemMeta(itemMeta6);

        bullet = new ItemStack(Material.STONE_BUTTON);
        ItemMeta itemMeta7 = bullet.getItemMeta();
        itemMeta7.setMaxStackSize(16);
        itemMeta7.customName(miniMessage.deserialize("<!i>通用子弹"));
        bullet.setItemMeta(itemMeta7);

        arrow = new ItemStack(Material.ARROW);
        ItemMeta itemMeta8 = arrow.getItemMeta();

        arrow.setItemMeta(itemMeta8);
        arrow.setAmount(16);


        feather = new ItemStack(Material.FEATHER);
        iron_sword = new ItemStack(Material.IRON_SWORD);
        ItemMeta itemMeta9 = iron_sword.getItemMeta();
        itemMeta9.setUnbreakable(true);
        itemMeta9.addAttributeModifier(Attribute.ATTACK_SPEED, AttributeModifier.deserialize(
                Map.of("amount", -0.5,
                        "operation", "0",
                        "key","mlcgame:attackspeed"
                        )));
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
        itemMeta19.setUnbreakable(true);
        iron_axe.setItemMeta(itemMeta19);
        iron_axe.setData(DataComponentTypes.CAN_BREAK, predicate);

        iron_shovel = new ItemStack(Material.IRON_SHOVEL);
        ItemMeta itemMeta20 = iron_shovel.getItemMeta();
        itemMeta20.setUnbreakable(true);
        iron_shovel.setItemMeta(itemMeta20);
        iron_shovel.setData(DataComponentTypes.CAN_BREAK, predicate);

        itemlist = List.of(handgun,
                rifle,
                shotgun,
                submachine_gun,
                bow,
                crossbow,
                grenade,
                bullet,
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
                ,iron_shovel);
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
    }
}
