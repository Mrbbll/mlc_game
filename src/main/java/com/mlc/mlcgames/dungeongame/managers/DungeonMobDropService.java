package com.mlc.mlcgames.dungeongame.managers;

import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ThreadLocalRandom;

/** 地牢受控怪物的统一死亡掉落规则：清除原有掉落，只随机掉落 0-3 个 CE 金币。 */
final class DungeonMobDropService {
    private static final String COIN_ITEM_ID = "mlcgame:coin";
    private static final int MAX_COIN_AMOUNT = 3;
    private final DungeonItemResolver itemResolver;

    DungeonMobDropService(DungeonItemResolver itemResolver) {
        this.itemResolver = itemResolver;
    }

    void replaceDrops(EntityDeathEvent event) {
        event.getDrops().clear();
        event.setDroppedExp(0);

        int amount = ThreadLocalRandom.current().nextInt(MAX_COIN_AMOUNT + 1);
        if (amount == 0) return;
        ItemStack coin = itemResolver.resolve(COIN_ITEM_ID);
        if (coin == null) return;
        coin.setAmount(amount);
        event.getDrops().add(coin);
    }
}
