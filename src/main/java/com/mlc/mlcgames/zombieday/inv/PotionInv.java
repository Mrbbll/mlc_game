package com.mlc.mlcgames.zombieday.inv;

import com.mlc.mlcgames.zombieday.Item;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;

import java.util.List;

public class PotionInv {
    public static Merchant merchant = Bukkit.createMerchant();
    public static void init(){
        MerchantRecipe merchantRecipe1 = new MerchantRecipe(Item.regeneration_potion,0,10,false);
        ItemStack item1 = Item.emerald.clone();
        item1.setAmount(10);
        merchantRecipe1.setIngredients(List.of(item1));
        merchantRecipe1.setIgnoreDiscounts(true);

        MerchantRecipe merchantRecipe2 = new MerchantRecipe(Item.instant_health_potion,0,10,false);
        ItemStack item2 = Item.emerald.clone();
        item2.setAmount(10);
        merchantRecipe2.setIngredients(List.of(item2));
        merchantRecipe2.setIgnoreDiscounts(true);

        MerchantRecipe merchantRecipe3 = new MerchantRecipe(Item.strength_potion,0,10,false);
        ItemStack item3 = Item.emerald.clone();
        item3.setAmount(10);
        merchantRecipe3.setIngredients(List.of(item3));
        merchantRecipe3.setIgnoreDiscounts(true);

        MerchantRecipe merchantRecipe4 = new MerchantRecipe(Item.speed_potion,0,10,false);
        ItemStack item4 = Item.emerald.clone();
        item4.setAmount(10);
        merchantRecipe4.setIngredients(List.of(item4));
        merchantRecipe4.setIgnoreDiscounts(true);

        MerchantRecipe merchantRecipe5 = new MerchantRecipe(Item.golden_apple,0,10,false);
        ItemStack item5 = Item.emerald.clone();
        item5.setAmount(20);
        merchantRecipe5.setIngredients(List.of(item5));
        merchantRecipe5.setIgnoreDiscounts(true);

        MerchantRecipe merchantRecipe6 = new MerchantRecipe(Item.regeneration_potion_splash,0,10,false);
        ItemStack item6 = Item.emerald.clone();
        item6.setAmount(15);
        merchantRecipe6.setIngredients(List.of(item6));
        merchantRecipe6.setIgnoreDiscounts(true);

        MerchantRecipe merchantRecipe7 = new MerchantRecipe(Item.instant_health_potion_splash,0,10,false);
        ItemStack item7 = Item.emerald.clone();
        item7.setAmount(15);
        merchantRecipe7.setIngredients(List.of(item7));
        merchantRecipe7.setIgnoreDiscounts(true);

        MerchantRecipe merchantRecipe8 = new MerchantRecipe(Item.strength_potion_splash,0,10,false);
        ItemStack item8 = Item.emerald.clone();
        item8.setAmount(15);
        merchantRecipe8.setIngredients(List.of(item8));
        merchantRecipe8.setIgnoreDiscounts(true);

        MerchantRecipe merchantRecipe9 = new MerchantRecipe(Item.speed_potion_splash,0,10,false);
        ItemStack item9 = Item.emerald.clone();
        item9.setAmount(15);
        merchantRecipe9.setIngredients(List.of(item9));
        merchantRecipe9.setIgnoreDiscounts(true);

        merchant.setRecipes(List.of(merchantRecipe1,
                merchantRecipe2,merchantRecipe3,
                merchantRecipe4,merchantRecipe5,
                merchantRecipe6,merchantRecipe7,
                merchantRecipe8,merchantRecipe9));
    }
    public static void open(Player player) {
        InventoryView inv = MenuType.MERCHANT.builder().merchant(merchant).title(Component.text("Potion")).checkReachable(false).build(player);
        player.openInventory(inv);
    }

}
