package com.mlc.mlcgames.combat.entity;

import com.mlc.mlcgames.combat.ArmorType;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Optional;

/** Owns PDC access for per-entity Blue Archive armour types. */
public final class ArmorTypeService {
    private final NamespacedKey armorTypeKey;
    private final Optional<ArmorType> defaultArmorType;

    public ArmorTypeService(JavaPlugin plugin, Optional<ArmorType> defaultArmorType) {
        armorTypeKey = new NamespacedKey(plugin, "armor_type");
        this.defaultArmorType = defaultArmorType;
    }

    public Optional<ArmorType> getArmorType(Entity entity) {
        return ArmorType.parse(entity.getPersistentDataContainer().get(armorTypeKey, PersistentDataType.STRING)).or(() -> defaultArmorType);
    }
    public boolean hasArmorType(Entity entity) { return ArmorType.parse(entity.getPersistentDataContainer().get(armorTypeKey, PersistentDataType.STRING)).isPresent(); }
    public void setArmorType(Entity entity, ArmorType armorType) { entity.getPersistentDataContainer().set(armorTypeKey, PersistentDataType.STRING, armorType.name()); }
    public void clearArmorType(Entity entity) { entity.getPersistentDataContainer().remove(armorTypeKey); }
}
