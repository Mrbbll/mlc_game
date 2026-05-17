package com.mlc.mlcgames.zombieday.inv;

import com.mlc.mlcgames.zombieday.Item;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.*;

import java.util.List;

public class Potioninv {
    public static Merchant merchant = Bukkit.createMerchant();
    public static void init(){
        MerchantRecipe merchantRecipe = new MerchantRecipe(Item.arrow,0,10,false);
        merchantRecipe.setIngredients(List.of(Item.emerald));
        merchantRecipe.setIgnoreDiscounts(true);
        merchant.setRecipes(List.of(merchantRecipe));
    }
    public static void open(Player player) {
        InventoryView inv = MenuType.MERCHANT.builder().merchant(merchant).title(Component.text("Potion")).checkReachable(false).build(player);
        player.openInventory(inv);
    }

}
