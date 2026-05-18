package com.mlc.mlcgames.zombieday.inv;

import com.mlc.mlcgames.zombieday.Item;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;

import java.util.List;

public class ArmorInv {

    public static Merchant merchant = Bukkit.createMerchant();
    public static void  init(){
        ItemStack emeraldStack = Item.emerald.clone();
        emeraldStack.setAmount(8);

        MerchantRecipe merchantRecipe1 = new MerchantRecipe(Item.lether_helmet,0, 999,false);
        merchantRecipe1.setIngredients(List.of(emeraldStack));

        MerchantRecipe merchantRecipe2 = new MerchantRecipe(Item.lether_chestplate,0, 999,false);
        merchantRecipe2.setIngredients(List.of(emeraldStack));

        MerchantRecipe merchantRecipe3 = new MerchantRecipe(Item.lether_leggings,0, 999,false);
        merchantRecipe3.setIngredients(List.of(emeraldStack));

        MerchantRecipe merchantRecipe4 = new MerchantRecipe(Item.lether_boots,0, 999,false);
        merchantRecipe4.setIngredients(List.of(emeraldStack));

        emeraldStack.setAmount(24);
        MerchantRecipe merchantRecipe5 = new MerchantRecipe(Item.iron_helmet,0, 999,false);
        merchantRecipe5.setIngredients(List.of(emeraldStack));

        MerchantRecipe merchantRecipe6 = new MerchantRecipe(Item.iron_chestplate,0, 999,false);
        merchantRecipe6.setIngredients(List.of(emeraldStack));

        MerchantRecipe merchantRecipe7 = new MerchantRecipe(Item.iron_leggings,0, 999,false);
        merchantRecipe7.setIngredients(List.of(emeraldStack));

        MerchantRecipe merchantRecipe8 = new MerchantRecipe(Item.iron_boots,0, 999,false);
        merchantRecipe8.setIngredients(List.of(emeraldStack));

        emeraldStack.setAmount(64);
        MerchantRecipe merchantRecipe9 = new MerchantRecipe(Item.neitherite_helmet,0, 999,false);
        merchantRecipe9.setIngredients(List.of(emeraldStack));

        MerchantRecipe merchantRecipe10 = new MerchantRecipe(Item.neitherite_chestplate,0, 999,false);
        merchantRecipe10.setIngredients(List.of(emeraldStack));

        MerchantRecipe merchantRecipe11 = new MerchantRecipe(Item.neitherite_leggings,0, 999,false);
        merchantRecipe11.setIngredients(List.of(emeraldStack));

        MerchantRecipe merchantRecipe12 = new MerchantRecipe(Item.neitherite_boots,0, 999,false);
        merchantRecipe12.setIngredients(List.of(emeraldStack));

        merchant.setRecipes(List.of(merchantRecipe1,merchantRecipe2,merchantRecipe3,merchantRecipe4,merchantRecipe5,merchantRecipe6,merchantRecipe7,merchantRecipe8,merchantRecipe9,merchantRecipe10,merchantRecipe11,merchantRecipe12));


    }
    public static void open(Player player) {
        InventoryView inv = MenuType.MERCHANT.builder().merchant(merchant).title(Component.text("Armor")).checkReachable(false).build(player);
        player.openInventory(inv);
    }
}
