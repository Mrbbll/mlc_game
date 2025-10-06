package com.mlc.mlcgames.bank;

import org.bukkit.inventory.Inventory;

import static com.mlc.mlcgames.Mlcgames.itemmanger;

public class Initinv {
    public Initinv(Inventory inv, int i) {
        if(i == 1){

//            ItemStack itemStack1 = ItemStack.of(Material.SHIELD);
//            ItemStack itemStack2 = ItemStack.of(Material.STONE_SWORD);
//            ItemStack itemStack3 = ItemStack.of(Material.CROSSBOW);
//            ItemStack itemStack4 = ItemStack.of(Material.WOLF_SPAWN_EGG);
//            ItemStack itemStack5 = ItemStack.of(Material.POTION);
//            ItemMeta itemMeta1 = itemStack1.getItemMeta();
//            ItemMeta itemMeta2 = itemStack1.getItemMeta();
//            ItemMeta itemMeta3 = itemStack1.getItemMeta();
//            ItemMeta itemMeta4 = itemStack1.getItemMeta();
//            ItemMeta itemMeta5 = itemStack1.getItemMeta();
//            PotionMeta potionMeta= (PotionMeta) itemMeta5;
//            potionMeta.setBasePotionType(PotionType.HEALING);
//
//            itemMeta1.itemName(miniMessage.deserialize(""));
//            itemMeta2.itemName(miniMessage.deserialize(""));
//            itemMeta3.itemName(miniMessage.deserialize(""));
//            itemMeta4.itemName(miniMessage.deserialize(""));
//            itemMeta5.itemName(miniMessage.deserialize(""));
//
//            List<Component> lore1 = new ArrayList<>();
//            List<Component> lore2 = new ArrayList<>();
//            List<Component> lore3 = new ArrayList<>();
//            List<Component> lore4 = new ArrayList<>();
//            List<Component> lore5 = new ArrayList<>();
//
//            lore1.add(miniMessage.deserialize("<!i>a"));
//            lore1.add(miniMessage.deserialize("<!i>b"));
//
//            lore2.add(miniMessage.deserialize("<!i>a"));
//            lore2.add(miniMessage.deserialize("<!i>b"));
//
//            lore3.add(miniMessage.deserialize("<!i>a"));
//            lore3.add(miniMessage.deserialize("<!i>b"));
//
//            lore4.add(miniMessage.deserialize("<!i>a"));
//            lore4.add(miniMessage.deserialize("<!i>b"));
//
//            lore5.add(miniMessage.deserialize("<!i>a"));
//            lore5.add(miniMessage.deserialize("<!i>b"));
//
//            itemMeta1.lore(lore1);
//            itemMeta2.lore(lore2);
//            itemMeta3.lore(lore3);
//            itemMeta4.lore(lore4);
//            itemMeta5.lore(lore5);


            inv.setItem(2, itemmanger.a_itemStack1);
            inv.setItem(11, itemmanger.a_itemStack2);
            inv.setItem(20, itemmanger.a_itemStack3);
            inv.setItem(29, itemmanger.a_itemStack4);
            inv.setItem(38, itemmanger.a_itemStack5);

        } else if (i==2) {
//            ItemStack itemStack1 = ItemStack.of(Material.SHIELD);
//            ItemStack itemStack2 = ItemStack.of(Material.STONE_SWORD);
//            ItemStack itemStack3 = ItemStack.of(Material.CROSSBOW);
//            ItemStack itemStack4 = ItemStack.of(Material.WOLF_SPAWN_EGG);
//            ItemStack itemStack5 = ItemStack.of(Material.POTION);
//            ItemMeta itemMeta1 = itemStack1.getItemMeta();
//            ItemMeta itemMeta2 = itemStack1.getItemMeta();
//            ItemMeta itemMeta3 = itemStack1.getItemMeta();
//            ItemMeta itemMeta4 = itemStack1.getItemMeta();
//            ItemMeta itemMeta5 = itemStack1.getItemMeta();
//            PotionMeta potionMeta= (PotionMeta) itemMeta5;
//            potionMeta.setBasePotionType(PotionType.HEALING);
//
//            itemMeta1.itemName(miniMessage.deserialize(""));
//            itemMeta2.itemName(miniMessage.deserialize(""));
//            itemMeta3.itemName(miniMessage.deserialize(""));
//            itemMeta4.itemName(miniMessage.deserialize(""));
//            itemMeta5.itemName(miniMessage.deserialize(""));
//
//            List<Component> lore1 = new ArrayList<>();
//            List<Component> lore2 = new ArrayList<>();
//            List<Component> lore3 = new ArrayList<>();
//            List<Component> lore4 = new ArrayList<>();
//            List<Component> lore5 = new ArrayList<>();
//
//            lore1.add(miniMessage.deserialize("<!i>a"));
//            lore1.add(miniMessage.deserialize("<!i>b"));
//
//            lore2.add(miniMessage.deserialize("<!i>a"));
//            lore2.add(miniMessage.deserialize("<!i>b"));
//
//            lore3.add(miniMessage.deserialize("<!i>a"));
//            lore3.add(miniMessage.deserialize("<!i>b"));
//
//            lore4.add(miniMessage.deserialize("<!i>a"));
//            lore4.add(miniMessage.deserialize("<!i>b"));
//
//            lore5.add(miniMessage.deserialize("<!i>a"));
//            lore5.add(miniMessage.deserialize("<!i>b"));
//
//            itemMeta1.lore(lore1);
//            itemMeta2.lore(lore2);
//            itemMeta3.lore(lore3);
//            itemMeta4.lore(lore4);
//            itemMeta5.lore(lore5);

            inv.setItem(2, itemmanger.b_itemStack1);
            inv.setItem(11, itemmanger.b_itemStack2);
            inv.setItem(20, itemmanger.b_itemStack3);
            inv.setItem(29, itemmanger.b_itemStack4);
            inv.setItem(38, itemmanger.b_itemStack5);
        }
    }
}
