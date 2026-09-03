package com.mlc.mlcgames.combat.command;

import com.mlc.mlcgames.combat.ArmorType;
import com.mlc.mlcgames.combat.AttackType;
import com.mlc.mlcgames.combat.CombatService;
import com.mlc.mlcgames.combat.entity.ArmorTypeService;
import com.mlc.mlcgames.combat.weapon.AttackTypeService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;

/** Development commands for assigning and checking combat attributes. */
public final class MlcCommand implements CommandExecutor, TabCompleter {
    private static final String PERMISSION = "mlcgames.combat.admin";
    private final ArmorTypeService armorTypeService;
    private final AttackTypeService attackTypeService;
    private final CombatService combatService;

    public MlcCommand(ArmorTypeService armorTypeService, AttackTypeService attackTypeService, CombatService combatService) {
        this.armorTypeService = armorTypeService;
        this.attackTypeService = attackTypeService;
        this.combatService = combatService;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission(PERMISSION)) { sender.sendMessage("You do not have permission to manage combat attributes."); return true; }
        if (!(sender instanceof Player player)) { sender.sendMessage("This command must be used by a player."); return true; }
        if (args.length == 0) { sender.sendMessage("Usage: /mlc armor <get|set|clear> | attacktype <get|set|clear> | combat debug"); return true; }
        return switch (args[0].toLowerCase(Locale.ROOT)) {
            case "armor" -> handleArmor(player, args);
            case "attacktype" -> handleAttackType(player, args);
            case "combat" -> handleCombat(player, args);
            default -> { sender.sendMessage("Unknown subcommand."); yield true; }
        };
    }

    private boolean handleArmor(Player player, String[] args) {
        LivingEntity target = target(player);
        if (target == null) { player.sendMessage("Look at a living entity within 10 blocks first."); return true; }
        if (args.length < 2) { player.sendMessage("Usage: /mlc armor <get|set|clear>"); return true; }
        switch (args[1].toLowerCase(Locale.ROOT)) {
            case "get" -> player.sendMessage("Armor type: " + armorTypeService.getArmorType(target).map(Enum::name).orElse("none"));
            case "set" -> {
                if (args.length < 3) { player.sendMessage("Usage: /mlc armor set <light|heavy|special|elastic>"); return true; }
                ArmorType.parse(args[2]).ifPresentOrElse(type -> { armorTypeService.setArmorType(target, type); player.sendMessage("Set " + target.getName() + " armor type to " + type + "."); }, () -> player.sendMessage("Unknown armor type."));
            }
            case "clear" -> { armorTypeService.clearArmorType(target); player.sendMessage("Cleared " + target.getName() + " armor type."); }
            default -> player.sendMessage("Usage: /mlc armor <get|set|clear>");
        }
        return true;
    }

    private boolean handleAttackType(Player player, String[] args) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType().isAir()) { player.sendMessage("Hold an item first."); return true; }
        if (args.length < 2) { player.sendMessage("Usage: /mlc attacktype <get|set|clear>"); return true; }
        switch (args[1].toLowerCase(Locale.ROOT)) {
            case "get" -> player.sendMessage("Attack type: " + attackTypeService.getAttackType(item).map(Enum::name).orElse("none"));
            case "set" -> {
                if (args.length < 3) { player.sendMessage("Usage: /mlc attacktype set <explosive|piercing|mystic|sonic>"); return true; }
                AttackType.parse(args[2]).ifPresentOrElse(type -> { attackTypeService.setAttackType(item, type); player.sendMessage("Set held item attack type to " + type + "."); }, () -> player.sendMessage("Unknown attack type."));
            }
            case "clear" -> { attackTypeService.clearAttackType(item); player.sendMessage("Cleared held item attack type."); }
            default -> player.sendMessage("Usage: /mlc attacktype <get|set|clear>");
        }
        return true;
    }

    private boolean handleCombat(Player player, String[] args) {
        if (args.length == 2 && args[1].equalsIgnoreCase("debug")) player.sendMessage("Combat debug " + (combatService.toggleDebug(player.getUniqueId()) ? "enabled." : "disabled."));
        else player.sendMessage("Usage: /mlc combat debug");
        return true;
    }

    private LivingEntity target(Player player) {
        Entity entity = player.getTargetEntity(10);
        return entity instanceof LivingEntity livingEntity ? livingEntity : null;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) return List.of("armor", "attacktype", "combat");
        if (args.length == 2 && (args[0].equalsIgnoreCase("armor") || args[0].equalsIgnoreCase("attacktype"))) return List.of("get", "set", "clear");
        if (args.length == 2 && args[0].equalsIgnoreCase("combat")) return List.of("debug");
        if (args.length == 3 && args[0].equalsIgnoreCase("armor") && args[1].equalsIgnoreCase("set")) return List.of("light", "heavy", "special", "elastic");
        if (args.length == 3 && args[0].equalsIgnoreCase("attacktype") && args[1].equalsIgnoreCase("set")) return List.of("explosive", "piercing", "mystic", "sonic");
        return List.of();
    }
}
