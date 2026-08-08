package com.mlc.mlcgames.sandgame.menus;

import com.mlc.mlcgames.sandgame.items.shopmenuitem;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.mlc.mlcgames.Mlcgames.instance;
import static com.mlc.mlcgames.Mlcgames.miniMessage;

public class ShopMenu {
    public static Inventory shop_menu;
    public static void init(){
        shop_menu = instance.getServer().createInventory(null, 9*6, miniMessage.deserialize("shop_menu"));

        setShopItem(0, shopmenuitem.sand);
        setShopItem(1, shopmenuitem.stonesword);
        setShopItem(2, shopmenuitem.beef);
        setShopItem(3, shopmenuitem.cobweb);
        setShopItem(4, shopmenuitem.bow);
        setShopItem(5, shopmenuitem.arrow);
        setShopItem(6, shopmenuitem.goldenapple);
        setShopItem(7, shopmenuitem.invispotion);
        setShopItem(8, shopmenuitem.jumppotion);
        setShopItem(9, shopmenuitem.speedpotion);
        setShopItem(10, shopmenuitem.regenpotion);
        setShopItem(11, shopmenuitem.endpeal);
        setShopItem(12, shopmenuitem.iron_chestplate);
        setShopItem(18, shopmenuitem.coingen);
        setShopItem(19, shopmenuitem.coingenfast);
        setShopItem(20, shopmenuitem.bomb);
        setShopItem(21, shopmenuitem.tower);
        setShopItem(22, shopmenuitem.towerdmg);
        setShopItem(23, shopmenuitem.towerarmor);
        setShopItem(24, shopmenuitem.sandgen);
    }

    // 展示用物品：在基础 lore 后追加价格与点击购买提示（实际售出仍用 shopmenuitem 的干净基础物品）
    private static void setShopItem(int slot, ItemStack base){
        Integer price = shopmenuitem.prices.get(base);
        if (price == null) {
            return;
        }
        ItemStack display = base.clone();
        display.editMeta(meta -> {
            List<Component> lore = new ArrayList<>();
            if (meta.hasLore()) {
                lore.addAll(Objects.requireNonNull(meta.lore()));
            }
            lore.add(miniMessage.deserialize("<gray>价格：<yellow><b>" + price));
            lore.add(miniMessage.deserialize("<green><b>点击购买"));
            meta.lore(lore);
        });
        // 点击时拿到的是展示物品（带价格 lore），故以展示物品为键登记价格与干净基底
        shopmenuitem.prices.put(display, price);
        shopmenuitem.sellables.put(display, base);
        shop_menu.setItem(slot, display);
    }
}
