package com.mlc.mlcgames.sandgame.menus;

import com.mlc.mlcgames.sandgame.items.shopmenuitem;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

import static com.mlc.mlcgames.Mlcgames.instance;
import static com.mlc.mlcgames.Mlcgames.miniMessage;

public class ShopMenu {
    public static Inventory shop_menu;
    public static void init(){
        shop_menu = instance.getServer().createInventory(null, 9*6, miniMessage.deserialize("shop_menu"));
        setShopItem(0, shopmenuitem.wool);
        setShopItem(1, shopmenuitem.sand);
        setShopItem(2, shopmenuitem.stonesword);
        setShopItem(3, shopmenuitem.beef);
        setShopItem(4, shopmenuitem.cobweb);
        setShopItem(5, shopmenuitem.bow);
        setShopItem(6, shopmenuitem.arrow);
        setShopItem(7, shopmenuitem.goldenapple);
        setShopItem(8, shopmenuitem.invispotion);
        setShopItem(9, shopmenuitem.coingen);
        setShopItem(10, shopmenuitem.coingenfast);
        setShopItem(11, shopmenuitem.bomb);
        setShopItem(12, shopmenuitem.tower);
        setShopItem(13, shopmenuitem.towerdmg);
        setShopItem(14, shopmenuitem.towerarmor);
        setShopItem(15, shopmenuitem.sandgen);
    }

    // 展示用物品：在基础 lore 后追加价格与点击购买提示（实际售出仍用 shopmenuitem 的干净基础物品）
    private static void setShopItem(int slot, ItemStack base){
        ItemStack display = base.clone();
        display.editMeta(meta -> {
            List<Component> lore = new ArrayList<>();
            if (meta.hasLore()) {
                lore.addAll(meta.lore());
            }
            lore.add(miniMessage.deserialize("<gray>价格：<yellow><b>" + shopmenuitem.prices.get(base.getType())));
            lore.add(miniMessage.deserialize("<green><b>点击购买"));
            meta.lore(lore);
        });
        shop_menu.setItem(slot, display);
    }
}
