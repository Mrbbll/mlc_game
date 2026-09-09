package com.mlc.mlcgames.dungeongame.mobs;

import com.mlc.mlcgames.Mlcgames;
import com.mlc.mlcgames.combat.ArmorType;
import com.mlc.mlcgames.combat.entity.ArmorTypeService;
import com.mlc.mlcgames.dungeongame.DungeonDifficulty;
import net.momirealms.craftengine.bukkit.item.BukkitItem;
import net.momirealms.craftengine.bukkit.item.BukkitItemManager;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

/** Discovers complete mlcgame pixel armour sets and equips one coherent random set per mob. */
public final class CraftEngineMobEquipment {
    private enum ArmorPart {
        HELMET("helmet", EquipmentSlotGroup.HEAD),
        CHESTPLATE("chestplate", EquipmentSlotGroup.CHEST),
        LEGGINGS("leggings", EquipmentSlotGroup.LEGS),
        BOOTS("boots", EquipmentSlotGroup.FEET);

        final String suffix;
        final EquipmentSlotGroup slot;
        ArmorPart(String suffix, EquipmentSlotGroup slot) {
            this.suffix = suffix;
            this.slot = slot;
        }
    }

    private record ArmorSet(String name, Map<ArmorPart, Key> items) { }

    private final JavaPlugin plugin;
    private final ArmorTypeService armorTypeService;
    private final Random random = new Random();
    private List<ArmorSet> cachedSets;
    private boolean warned;

    public CraftEngineMobEquipment(JavaPlugin plugin) {
        this.plugin = plugin;
        this.armorTypeService = new ArmorTypeService(plugin);
    }

    public void equip(LivingEntity entity, DungeonDifficulty difficulty) {
        EntityEquipment equipment = entity.getEquipment();
        if (equipment == null) return;
        if (Bukkit.getPluginManager().getPlugin("CraftEngine") == null) {
            warn("CraftEngine is unavailable; dungeon mob armour was not equipped");
            return;
        }

        List<ArmorSet> sets = armorSets();
        if (sets.isEmpty()) {
            warn("No complete CraftEngine pixel armour sets were discovered");
            return;
        }
        ArmorSet selected = sets.get(random.nextInt(sets.size()));
        EnumMap<ArmorPart, ItemStack> built = new EnumMap<>(ArmorPart.class);
        for (ArmorPart part : ArmorPart.values()) {
            ItemStack item = build(selected.items().get(part));
            if (item == null) {
                warn("Unable to build complete CraftEngine armour set '" + selected.name() + "'");
                return;
            }
            applyDifficultyAttributes(item, difficulty, part);
            built.put(part, item);
        }

        equipment.setHelmet(built.get(ArmorPart.HELMET));
        equipment.setChestplate(built.get(ArmorPart.CHESTPLATE));
        equipment.setLeggings(built.get(ArmorPart.LEGGINGS));
        equipment.setBoots(built.get(ArmorPart.BOOTS));
        equipment.setHelmetDropChance(0.0F);
        equipment.setChestplateDropChance(0.0F);
        equipment.setLeggingsDropChance(0.0F);
        equipment.setBootsDropChance(0.0F);
        if (armorTypeService.getArmorType(entity) == ArmorType.NONE) {
            armorTypeService.setArmorType(entity, fallbackArmorType(selected.name()));
        }
    }

    private List<ArmorSet> armorSets() {
        if (cachedSets != null && !cachedSets.isEmpty()) return cachedSets;
        String namespace = Mlcgames.dungeonConfiguration.getString(
                "dungeon-equipment.pixel-armor.namespace", "mlcgame");
        String prefix = Mlcgames.dungeonConfiguration.getString(
                "dungeon-equipment.pixel-armor.item-prefix", "pixel_");
        Map<String, EnumMap<ArmorPart, Key>> discovered = new TreeMap<>();
        try {
            for (Key id : BukkitItemManager.instance().allItemIds()) {
                if (!id.namespace().equals(namespace) || !id.value().startsWith(prefix)) continue;
                for (ArmorPart part : ArmorPart.values()) {
                    String suffix = "_" + part.suffix;
                    if (!id.value().endsWith(suffix)) continue;
                    String setName = id.value().substring(prefix.length(), id.value().length() - suffix.length());
                    if (!setName.isBlank()) {
                        discovered.computeIfAbsent(setName, ignored -> new EnumMap<>(ArmorPart.class)).put(part, id);
                    }
                }
            }
        } catch (RuntimeException | LinkageError exception) {
            warn("Unable to inspect CraftEngine pixel armour items: " + exception.getClass().getSimpleName());
            return List.of();
        }
        cachedSets = discovered.entrySet().stream()
                .filter(entry -> entry.getValue().size() == ArmorPart.values().length)
                .map(entry -> new ArmorSet(entry.getKey(), Map.copyOf(entry.getValue())))
                .toList();
        if (!cachedSets.isEmpty()) {
            plugin.getLogger().info("Discovered " + cachedSets.size() + " complete CraftEngine pixel armour sets");
        }
        return cachedSets;
    }

    private ItemStack build(Key id) {
        if (id == null) return null;
        try {
            BukkitItem item = BukkitItemManager.instance().createCustomWrappedItem(id, null);
            ItemStack result = item.getBukkitItem();
            return result == null || result.getType().isAir() ? null : result.clone();
        } catch (RuntimeException | LinkageError exception) {
            return null;
        }
    }

    private void applyDifficultyAttributes(ItemStack item, DungeonDifficulty difficulty, ArmorPart part) {
        String path = "difficulty." + difficulty.name().toLowerCase(Locale.ROOT) + ".armor.";
        double armorBonus = Mlcgames.dungeonConfiguration.getDouble(path + "attribute-bonus-per-piece",
                switch (difficulty) { case EASY -> 0.0; case NORMAL -> 0.5; case HARD -> 1.0; });
        double toughnessBonus = Mlcgames.dungeonConfiguration.getDouble(path + "toughness-bonus-per-piece",
                switch (difficulty) { case EASY -> 0.0; case NORMAL -> 0.25; case HARD -> 0.5; });
        item.editMeta(meta -> {
            if (armorBonus != 0.0) {
                meta.addAttributeModifier(Attribute.ARMOR, new AttributeModifier(
                        new NamespacedKey(plugin, "dungeon_" + difficulty.name().toLowerCase(Locale.ROOT)
                                + "_" + part.suffix + "_armor"),
                        armorBonus, AttributeModifier.Operation.ADD_NUMBER, part.slot));
            }
            if (toughnessBonus != 0.0) {
                meta.addAttributeModifier(Attribute.ARMOR_TOUGHNESS, new AttributeModifier(
                        new NamespacedKey(plugin, "dungeon_" + difficulty.name().toLowerCase(Locale.ROOT)
                                + "_" + part.suffix + "_toughness"),
                        toughnessBonus, AttributeModifier.Operation.ADD_NUMBER, part.slot));
            }
        });
    }

    private ArmorType fallbackArmorType(String setName) {
        Optional<ArmorType> mapped = ArmorType.parse(Mlcgames.dungeonConfiguration.getString(
                "dungeon-equipment.pixel-armor.armor-type-by-set." + setName));
        if (mapped.isPresent() && mapped.get() != ArmorType.NONE) return mapped.get();
        List<ArmorType> configured = Mlcgames.dungeonConfiguration
                .getStringList("dungeon-equipment.armor-types").stream()
                .map(ArmorType::parse).flatMap(Optional::stream)
                .filter(type -> type != ArmorType.NONE).toList();
        List<ArmorType> choices = configured.isEmpty()
                ? List.of(ArmorType.LIGHT, ArmorType.HEAVY, ArmorType.SPECIAL, ArmorType.ELASTIC)
                : configured;
        return choices.get(random.nextInt(choices.size()));
    }

    private void warn(String message) {
        if (warned) return;
        warned = true;
        plugin.getLogger().warning(message);
    }
}
