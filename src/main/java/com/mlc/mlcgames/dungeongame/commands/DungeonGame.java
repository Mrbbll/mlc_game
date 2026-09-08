package com.mlc.mlcgames.dungeongame.commands;

import com.mlc.mlcgames.Mlcgames;
import com.mlc.mlcgames.dungeongame.gamephase.dungeongameinit;
import com.mlc.mlcgames.dungeongame.managers.RoomSpawner;
import com.mlc.mlcgames.dungeongame.managers.Worldmanager;
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
            sender.sendMessage("Usage: /dungeongame generate [floor] [normal rooms] [seed] | enter | reload | info");
            return true;
        }
        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "generate" -> generate(sender, args);
            case "enter" -> enter(sender);
            case "reload" -> reload(sender);
            case "info" -> sender.sendMessage("Dungeon layout: " + RoomSpawner.roomList.size()
                    + " rooms, " + RoomSpawner.specialroomcount + " special branches, "
                    + RoomSpawner.connections.size() + " connections.");
            default -> sender.sendMessage("Usage: /dungeongame generate [floor] [normal rooms] [seed] | enter | reload | info");
        }
        return true;
    }

    private void generate(CommandSender sender, String[] args) {
        try {
            int floor = args.length >= 2 ? Integer.parseInt(args[1]) : 1;
            int normalRooms = args.length >= 3 ? Integer.parseInt(args[2])
                    : Mlcgames.dungeonConfiguration.getInt("generation.normal-rooms", 7);
            int specialChance = Mlcgames.dungeonConfiguration.getInt("generation.special-chance", 65);
            Random random = args.length >= 4 ? new Random(Long.parseLong(args[3])) : new Random();
            if (floor < 1 || floor > 5) throw new IllegalArgumentException("floor must be from 1 to 5");
            RoomSpawner.generateRooms(normalRooms, floor, specialChance, random);
            sender.sendMessage("Generated floor " + floor + ": " + RoomSpawner.roomList.size() + " rooms, "
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
        RoomSpawner.getGeneratedStartRoom().ifPresentOrElse(start -> {
            player.teleport(Worldmanager.getRoomSpawn(start));
            player.sendMessage("Entered the dungeon.");
        }, () -> sender.sendMessage("Generate a dungeon first: /dungeongame generate"));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length == 1) return List.of("generate", "enter", "reload", "info");
        if (args.length == 2 && args[0].equalsIgnoreCase("generate")) return List.of("1", "2", "3", "4", "5");
        return List.of();
    }
}
