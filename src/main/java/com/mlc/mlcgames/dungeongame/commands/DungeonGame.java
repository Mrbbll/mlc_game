package com.mlc.mlcgames.dungeongame.commands;

import com.mlc.mlcgames.Mlcgames;
import com.mlc.mlcgames.dungeongame.gamephase.dungeongameinit;
import com.mlc.mlcgames.dungeongame.managers.DungeonGameManager;
import com.mlc.mlcgames.dungeongame.managers.Roommanager;
import com.mlc.mlcgames.dungeongame.managers.RoomSpawner;
import com.mlc.mlcgames.dungeongame.managers.Worldmanager;
import com.mlc.mlcgames.dungeongame.menus.SettingMenu;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.Random;

public class DungeonGame implements TabExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length == 0) {
            openMenu(sender);
            return true;
        }
        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "menu" -> openMenu(sender);
            case "start" -> start(sender);
            case "end" -> end(sender);
            case "generate" -> generate(sender, args);
            case "enter" -> enter(sender);
            case "reload" -> reload(sender);
            case "info" -> sender.sendMessage("Dungeon: " + DungeonGameManager.get().status());
            default -> usage(sender);
        }
        return true;
    }

    private void generate(CommandSender sender, String[] args) {
        if (DungeonGameManager.get().isRunningOrGenerating()) {
            sender.sendMessage("Stop the active dungeon before using the layout debug command.");
            return;
        }
        try {
            int dungeonLevel = args.length >= 2 ? Integer.parseInt(args[1]) : 1;
            int layer = args.length >= 3 ? Integer.parseInt(args[2]) : 1;
            int normalRooms = args.length >= 4 ? Integer.parseInt(args[3])
                    : Mlcgames.dungeonConfiguration.getInt("generation.normal-rooms", 7);
            int specialChance = Mlcgames.dungeonConfiguration.getInt("generation.special-chance", 65);
            int dungeonSet = Mlcgames.dungeonConfiguration.getInt("generation.set", 1);
            Random random = args.length >= 5 ? new Random(Long.parseLong(args[4])) : new Random();
            if (dungeonLevel < 1 || dungeonLevel > 3) throw new IllegalArgumentException("level must be from 1 to 3");
            if (layer < 1 || layer > 5) throw new IllegalArgumentException("layer must be from 1 to 5");
            Roommanager.loadSchematicTemplates(dungeonLevel, dungeonSet);
            Worldmanager.layoutOriginX = Mlcgames.dungeonConfiguration.getInt("layout.origin.x", 0);
            Worldmanager.layoutOriginY = Mlcgames.dungeonConfiguration.getInt("layout.origin.y", 80);
            Worldmanager.layoutOriginZ = Mlcgames.dungeonConfiguration.getInt("layout.origin.z", 0);
            Worldmanager.createDungeonWorld();
            RoomSpawner.generateRooms(normalRooms, dungeonLevel, layer, specialChance, random);
            sender.sendMessage("Generated level " + dungeonLevel + " layer " + layer + ": "
                    + RoomSpawner.roomList.size() + " rooms, "
                    + RoomSpawner.specialroomcount + " special branches.");
        } catch (IllegalArgumentException | IllegalStateException exception) {
            sender.sendMessage("Dungeon generation failed: " + exception.getMessage());
        }
    }

    private void reload(CommandSender sender) {
        Mlcgames.configManager.reloadConfig("dungeongame.yml");
        Mlcgames.dungeonConfiguration = Mlcgames.configManager.getConfig("dungeongame.yml");
        try {
            sender.sendMessage("Loaded " + dungeongameinit.reloadTemplates() + " dungeon room templates.");
        } catch (IllegalArgumentException exception) {
            sender.sendMessage("Dungeon configuration error: " + exception.getMessage());
        }
    }

    private void enter(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only a player can enter the dungeon.");
            return;
        }
        DungeonGameManager.get().enter(player);
    }

    private void openMenu(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            usage(sender);
            return;
        }
        new SettingMenu().open(player);
    }

    private void start(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only a player can start the dungeon.");
            return;
        }
        DungeonGameManager.get().startGame(player);
    }

    private void end(CommandSender sender) {
        if (!DungeonGameManager.get().isRunningOrGenerating()) {
            sender.sendMessage("There is no active dungeon game.");
            return;
        }
        DungeonGameManager.get().endGame();
        sender.sendMessage("Dungeon game ended.");
    }

    private void usage(CommandSender sender) {
        sender.sendMessage("Usage: /dungeongame [menu|start|end|enter|generate|reload|info]");
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length == 1) return List.of("menu", "start", "end", "enter", "generate", "reload", "info");
        if (args.length == 2 && args[0].equalsIgnoreCase("generate")) return List.of("1", "2", "3");
        if (args.length == 3 && args[0].equalsIgnoreCase("generate")) return List.of("1", "2", "3", "4", "5");
        return List.of();
    }
}
