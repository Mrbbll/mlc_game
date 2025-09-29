package com.mlc.mlcgames;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

import java.util.ArrayList;
import java.util.List;

import static com.mlc.mlcgames.Mlcgames.miniMessage;

public class Itemmanger {
    public final ItemStack mlcmenu;
    public final ItemStack a_itemStack1;
    public final ItemStack a_itemStack2;
    public final ItemStack a_itemStack3;
    public final ItemStack a_itemStack4;
    public final ItemStack a_itemStack5;
    public final ItemStack b_itemStack1;
    public final ItemStack b_itemStack2;
    public final ItemStack b_itemStack3;
    public final ItemStack b_itemStack4;
    public final ItemStack b_itemStack5;
    Itemmanger(){

        this.mlcmenu = ItemStack.of(Material.ECHO_SHARD);
        ItemMeta itemMeta = mlcmenu.getItemMeta();
        itemMeta.setItemModel(NamespacedKey.fromString("mlcgames:mlcmenu"));
        itemMeta.itemName(Component.text("菜单", TextColor.fromHexString("#eea468")));

        a_itemStack1 = ItemStack.of(Material.SHIELD);
        a_itemStack2 = ItemStack.of(Material.STONE_SWORD);
        a_itemStack3 = ItemStack.of(Material.CROSSBOW);
        a_itemStack4 = ItemStack.of(Material.WOLF_SPAWN_EGG);
        a_itemStack5 = ItemStack.of(Material.POTION);
        ItemMeta a_itemMeta1 = a_itemStack1.getItemMeta();
        ItemMeta a_itemMeta2 = a_itemStack2.getItemMeta();
        ItemMeta a_itemMeta3 = a_itemStack3.getItemMeta();
        ItemMeta a_itemMeta4 = a_itemStack4.getItemMeta();
        ItemMeta a_itemMeta5 = a_itemStack5.getItemMeta();
        PotionMeta a_potionMeta= (PotionMeta) a_itemMeta5;
        a_potionMeta.setBasePotionType(PotionType.HEALING);

        a_itemMeta1.itemName(miniMessage.deserialize(""));
        a_itemMeta2.itemName(miniMessage.deserialize(""));
        a_itemMeta3.itemName(miniMessage.deserialize(""));
        a_itemMeta4.itemName(miniMessage.deserialize(""));
        a_itemMeta5.itemName(miniMessage.deserialize(""));

        List<Component> a_lore1 = new ArrayList<>();
        List<Component> a_lore2 = new ArrayList<>();
        List<Component> a_lore3 = new ArrayList<>();
        List<Component> a_lore4 = new ArrayList<>();
        List<Component> a_lore5 = new ArrayList<>();

        a_lore1.add(miniMessage.deserialize("<!i>a"));
        a_lore1.add(miniMessage.deserialize("<!i>b"));

        a_lore2.add(miniMessage.deserialize("<!i>a"));
        a_lore2.add(miniMessage.deserialize("<!i>b"));

        a_lore3.add(miniMessage.deserialize("<!i>a"));
        a_lore3.add(miniMessage.deserialize("<!i>b"));

        a_lore4.add(miniMessage.deserialize("<!i>a"));
        a_lore4.add(miniMessage.deserialize("<!i>b"));

        a_lore5.add(miniMessage.deserialize("<!i>a"));
        a_lore5.add(miniMessage.deserialize("<!i>b"));

        a_itemMeta1.lore(a_lore1);
        a_itemMeta2.lore(a_lore2);
        a_itemMeta3.lore(a_lore3);
        a_itemMeta4.lore(a_lore4);
        a_itemMeta5.lore(a_lore5);

        a_itemStack1.setItemMeta(a_itemMeta1);
        a_itemStack2.setItemMeta(a_itemMeta2);
        a_itemStack3.setItemMeta(a_itemMeta3);
        a_itemStack4.setItemMeta(a_itemMeta4);
        a_itemStack5.setItemMeta(a_itemMeta5);

        b_itemStack1 = ItemStack.of(Material.SHIELD);
        b_itemStack2 = ItemStack.of(Material.STONE_SWORD);
        b_itemStack3 = ItemStack.of(Material.CROSSBOW);
        b_itemStack4 = ItemStack.of(Material.WOLF_SPAWN_EGG);
        b_itemStack5 = ItemStack.of(Material.POTION);
        ItemMeta b_itemMeta1 = b_itemStack1.getItemMeta();
        ItemMeta b_itemMeta2 = b_itemStack2.getItemMeta();
        ItemMeta b_itemMeta3 = b_itemStack3.getItemMeta();
        ItemMeta b_itemMeta4 = b_itemStack4.getItemMeta();
        ItemMeta b_itemMeta5 = b_itemStack5.getItemMeta();
        PotionMeta b_potionMeta= (PotionMeta) b_itemMeta5;
        b_potionMeta.setBasePotionType(PotionType.HEALING);

        b_itemMeta1.itemName(miniMessage.deserialize(""));
        b_itemMeta2.itemName(miniMessage.deserialize(""));
        b_itemMeta3.itemName(miniMessage.deserialize(""));
        b_itemMeta4.itemName(miniMessage.deserialize(""));
        b_itemMeta5.itemName(miniMessage.deserialize(""));

        List<Component> b_lore1 = new ArrayList<>();
        List<Component> b_lore2 = new ArrayList<>();
        List<Component> b_lore3 = new ArrayList<>();
        List<Component> b_lore4 = new ArrayList<>();
        List<Component> b_lore5 = new ArrayList<>();

        b_lore1.add(miniMessage.deserialize("<!i>a"));
        b_lore1.add(miniMessage.deserialize("<!i>b"));

        b_lore2.add(miniMessage.deserialize("<!i>a"));
        b_lore2.add(miniMessage.deserialize("<!i>b"));

        b_lore3.add(miniMessage.deserialize("<!i>a"));
        b_lore3.add(miniMessage.deserialize("<!i>b"));

        b_lore4.add(miniMessage.deserialize("<!i>a"));
        b_lore4.add(miniMessage.deserialize("<!i>b"));

        b_lore5.add(miniMessage.deserialize("<!i>a"));
        b_lore5.add(miniMessage.deserialize("<!i>b"));

        b_itemMeta1.lore(b_lore1);
        b_itemMeta2.lore(b_lore2);
        b_itemMeta3.lore(b_lore3);
        b_itemMeta4.lore(b_lore4);
        b_itemMeta5.lore(b_lore5);

        b_itemStack1.setItemMeta(b_itemMeta1);
        b_itemStack2.setItemMeta(b_itemMeta2);
        b_itemStack3.setItemMeta(b_itemMeta3);
        b_itemStack4.setItemMeta(b_itemMeta4);
        b_itemStack5.setItemMeta(b_itemMeta5);
    }



}
