package com.mlc.mlcgames.dungeongame.menus;

import com.mlc.mlcgames.dungeongame.Dungeongame;
import com.mlc.mlcgames.dungeongame.managers.DungeonGameManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

/** In-game configuration screen for the next dungeon run. */
public final class SettingMenu implements Listener {
    private static final int DIFFICULTY_SLOT = 10;
    private static final int SIZE_SLOT = 12;
    private static final int SPECIAL_CHANCE_SLOT = 14;
    private static final int START_SLOT = 16;
    private static final int END_SLOT = 22;
    private static final int MIN_SIZE = 4;
    private static final int MAX_SIZE = 30;

    public void open(Player player) {
        if (!DungeonGameManager.get().canOpenMenu(player)) {
            player.sendMessage("§c只有地牢准备队或地牢正式队的玩家才能打开此菜单。");
            return;
        }
        SettingsHolder holder = new SettingsHolder();
        holder.inventory = Bukkit.createInventory(holder, 27, Component.text("地牢游戏设置"));
        render(holder.inventory);
        player.openInventory(holder.inventory);
    }

    private void render(Inventory inventory) {
        boolean running = DungeonGameManager.get().isRunningOrGenerating();
        ItemStack border = item(Material.GRAY_STAINED_GLASS_PANE, " ", List.of());
        for (int slot = 0; slot < inventory.getSize(); slot++) inventory.setItem(slot, border);

        inventory.setItem(DIFFICULTY_SLOT, item(Material.IRON_SWORD,
                "§e难度：§f" + Dungeongame.difficulty.displayName(),
                List.of("§7影响怪物生命、攻击伤害和 CraftEngine 护甲", "§a左键：下一个  §c右键：上一个")));
        inventory.setItem(SIZE_SLOT, item(Material.FILLED_MAP,
                "§e地牢大小：§f" + Dungeongame.mapsize,
                List.of("§7表示生成的普通房间目标数量", "§7范围：" + MIN_SIZE + " - " + MAX_SIZE,
                        "§a左键：+1  §c右键：-1")));
        inventory.setItem(SPECIAL_CHANCE_SLOT, item(Material.AMETHYST_SHARD,
                "§e特殊房间比例：§f" + Dungeongame.specialRoomChance + "%",
                List.of("§7每个可用分支生成特殊房间的概率", "§a左键：+10%  §c右键：-10%")));
        inventory.setItem(START_SLOT, item(running ? Material.REDSTONE_BLOCK : Material.EMERALD_BLOCK,
                running ? "§c游戏已经开始或正在生成" : "§a开启游戏",
                List.of("§7清空旧世界并生成 3 个等级 × 5 层地牢")));
        inventory.setItem(END_SLOT, item(Material.BARRIER, "§c结束游戏",
                List.of(running ? "§7结束当前地牢并传送所有参与者" : "§7当前没有正在运行的地牢")));
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof SettingsHolder)) return;
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player) || event.getClickedInventory() != event.getInventory()) return;
        // 玩家打开菜单后可能被移出队伍，因此点击时再次校验，不能只依赖 open()。
        if (!DungeonGameManager.get().canOpenMenu(player)) {
            player.closeInventory();
            player.sendMessage("§c你已不在地牢准备队或正式队中，菜单操作已取消。");
            return;
        }

        int slot = event.getRawSlot();
        if (slot == START_SLOT) {
            player.closeInventory();
            DungeonGameManager.get().startGame(player);
            return;
        }
        if (slot == END_SLOT) {
            player.closeInventory();
            if (!DungeonGameManager.get().isRunningOrGenerating()) {
                player.sendMessage("§c当前没有正在运行的地牢游戏。");
                return;
            }
            DungeonGameManager.get().endGame();
            Bukkit.broadcast(Component.text("地牢游戏已结束。"));
            return;
        }
        if (DungeonGameManager.get().isRunningOrGenerating()) {
            player.sendMessage("§c游戏运行期间不能修改设置。");
            return;
        }

        boolean backwards = event.isRightClick();
        if (slot == DIFFICULTY_SLOT) {
            Dungeongame.difficulty = backwards ? Dungeongame.difficulty.previous() : Dungeongame.difficulty.next();
        } else if (slot == SIZE_SLOT) {
            Dungeongame.mapsize = clamp(Dungeongame.mapsize + (backwards ? -1 : 1), MIN_SIZE, MAX_SIZE);
        } else if (slot == SPECIAL_CHANCE_SLOT) {
            Dungeongame.specialRoomChance = clamp(Dungeongame.specialRoomChance + (backwards ? -10 : 10), 0, 100);
        } else {
            return;
        }
        render(event.getInventory());
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof SettingsHolder) event.setCancelled(true);
    }

    private static ItemStack item(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static final class SettingsHolder implements InventoryHolder {
        private Inventory inventory;
        @Override public Inventory getInventory() { return inventory; }
    }
}
