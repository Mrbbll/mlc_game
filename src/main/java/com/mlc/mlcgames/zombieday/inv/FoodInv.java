package com.mlc.mlcgames.zombieday.inv;

import com.mlc.mlcgames.zombieday.Item;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;

import java.util.List;

public class FoodInv {
    public static Merchant merchant = Bukkit.createMerchant();

    public static void init() {
        ItemStack emeraldStack = Item.emerald.clone();
        emeraldStack.setAmount(8);

        MerchantRecipe merchantRecipe1 = new MerchantRecipe(ItemStack.of(Material.BREAD,16), 0,999,false);
        merchantRecipe1.addIngredient(emeraldStack);

        MerchantRecipe merchantRecipe3 = new MerchantRecipe(ItemStack.of(Material.COOKED_CHICKEN,16), 0,999,false);
        merchantRecipe3.addIngredient(emeraldStack);
        emeraldStack.setAmount(16);
        MerchantRecipe merchantRecipe2 = new MerchantRecipe(ItemStack.of(Material.COOKED_PORKCHOP,16), 0,999,false);
        merchantRecipe2.addIngredient(emeraldStack);
        MerchantRecipe merchantRecipe4 = new MerchantRecipe(ItemStack.of(Material.COOKED_BEEF,16), 0,999,false);
        merchantRecipe4.addIngredient(emeraldStack);
        emeraldStack.setAmount(20);
        MerchantRecipe merchantRecipe5 = new MerchantRecipe(ItemStack.of(Material.GOLDEN_APPLE,1), 0,999,false);
        merchantRecipe5.addIngredient(emeraldStack);
        MerchantRecipe merchantRecipe6 = new MerchantRecipe(ItemStack.of(Material.GOLDEN_CARROT,6), 0,999,false);
        merchantRecipe6.addIngredient(emeraldStack);

        List<MerchantRecipe> recipes = List.of(merchantRecipe1,merchantRecipe2,merchantRecipe3,merchantRecipe4,merchantRecipe5,merchantRecipe6);
        merchant.setRecipes(recipes);
    }

    public static void open(Player player) {
        InventoryView inv = MenuType.MERCHANT.builder().merchant(merchant).title(Component.text("Food")).checkReachable(false).build(player);
        player.openInventory(inv);
    }
}
