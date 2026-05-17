package com.mlc.mlcgames.zombieday.inv;

import com.mlc.mlcgames.zombieday.Item;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;

import java.util.List;

public class BulletInv {
    public static Merchant merchant = Bukkit.createMerchant();
    public static void init(){
        MerchantRecipe merchantRecipe1 = new MerchantRecipe(Item.handgun, 1);
        ItemStack emeraldStack = new ItemStack(Item.emerald);
        emeraldStack.setAmount(64);
        merchantRecipe1.setIngredients(List.of(emeraldStack,emeraldStack));

        MerchantRecipe merchantRecipe2 = new MerchantRecipe(Item.rifle, 2);
        merchantRecipe2.setIngredients(List.of(emeraldStack,emeraldStack));

        MerchantRecipe merchantRecipe3 = new MerchantRecipe(Item.shotgun, 2);
        merchantRecipe3.setIngredients(List.of(emeraldStack,emeraldStack));

        MerchantRecipe merchantRecipe4 = new MerchantRecipe(Item.submachine_gun, 2);
        merchantRecipe4.setIngredients(List.of(emeraldStack,emeraldStack));

        MerchantRecipe merchantRecipe5 = new MerchantRecipe(Item.grenade, 2);
        merchantRecipe5.setIngredients(List.of(emeraldStack,emeraldStack));

        emeraldStack.setAmount(32);
        MerchantRecipe merchantRecipe6 = new MerchantRecipe(ItemStack.of(Material.STONE_BUTTON,32), 99999);
        merchantRecipe6.setIngredients(List.of(emeraldStack));

        emeraldStack.setAmount(8);
        MerchantRecipe merchantRecipe7 = new MerchantRecipe(Item.arrow, 99999);
        merchantRecipe7.setIngredients(List.of(emeraldStack));

        merchant.setRecipes(List.of(merchantRecipe1,merchantRecipe2,merchantRecipe3,merchantRecipe4,merchantRecipe5,merchantRecipe6,merchantRecipe7));

    }
    public static void open(Player player) {
        InventoryView inv = MenuType.MERCHANT.builder().merchant(merchant).title(Component.text("Bullet")).checkReachable(false).build(player);
        player.openInventory(inv);
    }

}
