package com.mlc.mlcgames.dungeongame.managers;

import com.mlc.mlcgames.Mlcgames;
import com.mlc.mlcgames.dungeongame.Dungeongame;
import com.mlc.mlcgames.dungeongame.mobs.CraftEngineMobEquipment;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.HashSet;
import java.util.Set;

/** 根据已校验的环境怪物定义创建单只实体，并应用名称、装备、属性和追踪标签。 */
final class DungeonMobService {
    private final JavaPlugin plugin;
    private final Random random;
    private final DungeonSession session;
    private final CraftEngineMobEquipment automaticPixelArmor;
    private final DungeonItemResolver itemResolver;
    private final NamespacedKey sessionIdKey;
    private final NamespacedKey encounterIdKey;
    private final NamespacedKey definitionIdKey;
    private final Set<String> warnedDefinitions = new HashSet<>();

    DungeonMobService(JavaPlugin plugin, Random random, DungeonSession session,
                      CraftEngineMobEquipment automaticPixelArmor) {
        this.plugin = plugin;
        this.random = random;
        this.session = session;
        this.automaticPixelArmor = automaticPixelArmor;
        itemResolver = new DungeonItemResolver(plugin);
        sessionIdKey = new NamespacedKey(plugin, "dungeon_session_id");
        encounterIdKey = new NamespacedKey(plugin, "dungeon_encounter_id");
        definitionIdKey = new NamespacedKey(plugin, "dungeon_mob_id");
    }

    /**
     * 每个标记每波生成一只怪。标记类型决定 normal/elite/boss 池，池内再按 weight 加权选择。
     */
    LivingEntity spawn(DungeonSession.Encounter encounter, DungeonSession.SpawnMarker marker) {
        DungeonMobDefinition definition = encounter.mobPack.pick(marker.kind(), random);
        Entity spawned = null;
        try {
            spawned = marker.location().getWorld().spawnEntity(marker.location(), definition.entityType());
            if (!(spawned instanceof LivingEntity living)) {
                spawned.remove();
                return null;
            }

            living.setPersistent(true);
            living.setRemoveWhenFarAway(false);
            applyIdentity(living, encounter, definition);
            applyConfiguredAttributes(living, definition.attributes());
            applyEquipment(living, definition.equipment());
            applyDifficultyScaling(living, definition.scaleWithDifficulty(), encounter.dungeonLevel);
            return living;
        } catch (RuntimeException | LinkageError exception) {
            if (spawned != null && spawned.isValid()) spawned.remove();
            if (warnedDefinitions.add(definition.id())) {
                plugin.getLogger().warning("Unable to spawn dungeon mob '" + definition.id()
                        + "' from " + encounter.dungeonLevel + "_" + encounter.environmentSet + ".yml: "
                        + exception.getClass().getSimpleName() + ": " + exception.getMessage());
            }
            return null;
        }
    }

    private void applyIdentity(LivingEntity entity, DungeonSession.Encounter encounter,
                               DungeonMobDefinition definition) {
        if (!definition.displayName().isBlank()) {
            entity.customName(MiniMessage.miniMessage().deserialize(definition.displayName()));
            entity.setCustomNameVisible(definition.nameVisible());
        }
        entity.getPersistentDataContainer().set(
                sessionIdKey, PersistentDataType.STRING, session.id.toString());
        entity.getPersistentDataContainer().set(
                encounterIdKey, PersistentDataType.STRING, encounter.id.toString());
        entity.getPersistentDataContainer().set(
                definitionIdKey, PersistentDataType.STRING, definition.id());
    }

    /** 配置值是基础属性；未配置的属性继续使用对应原版实体的默认值。 */
    private static void applyConfiguredAttributes(LivingEntity entity, Map<Attribute, Double> configured) {
        for (Map.Entry<Attribute, Double> entry : configured.entrySet()) {
            var instance = entity.getAttribute(entry.getKey());
            if (instance != null) instance.setBaseValue(entry.getValue());
        }
    }

    private void applyEquipment(LivingEntity entity, DungeonMobDefinition.Equipment configured) {
        EntityEquipment equipment = entity.getEquipment();
        if (equipment == null) return;

        if (configured.autoPixelArmor()) {
            automaticPixelArmor.equip(entity, Dungeongame.difficulty);
            // auto 模式仍允许单独覆盖主手、副手或某一件护甲。
            setIfConfigured(configured.helmet(), equipment::setHelmet);
            setIfConfigured(configured.chestplate(), equipment::setChestplate);
            setIfConfigured(configured.leggings(), equipment::setLeggings);
            setIfConfigured(configured.boots(), equipment::setBoots);
            setIfConfigured(configured.mainHand(), equipment::setItemInMainHand);
            setIfConfigured(configured.offHand(), equipment::setItemInOffHand);
        } else {
            // 显式 equipment 段代表完整装备方案，空值会清除实体原生携带的装备。
            equipment.setHelmet(itemResolver.resolve(configured.helmet()));
            equipment.setChestplate(itemResolver.resolve(configured.chestplate()));
            equipment.setLeggings(itemResolver.resolve(configured.leggings()));
            equipment.setBoots(itemResolver.resolve(configured.boots()));
            equipment.setItemInMainHand(itemResolver.resolve(configured.mainHand()));
            equipment.setItemInOffHand(itemResolver.resolve(configured.offHand()));
        }

        float dropChance = configured.dropChance();
        equipment.setHelmetDropChance(dropChance);
        equipment.setChestplateDropChance(dropChance);
        equipment.setLeggingsDropChance(dropChance);
        equipment.setBootsDropChance(dropChance);
        equipment.setItemInMainHandDropChance(dropChance);
        equipment.setItemInOffHandDropChance(dropChance);
    }

    private void setIfConfigured(String id, java.util.function.Consumer<ItemStack> setter) {
        if (id != null && !id.isBlank()) setter.accept(itemResolver.resolve(id));
    }

    /**
     * 开启 scale-with-difficulty 时，生命和攻击按菜单难度及地牢关卡累计倍率缩放；
     * 护甲、速度和击退抗性保持文件中的精确值。
     */
    private static void applyDifficultyScaling(LivingEntity entity, boolean enabled, int dungeonLevel) {
        double healthMultiplier = 1.0;
        double damageMultiplier = 1.0;
        if (enabled) {
            String difficulty = Dungeongame.difficulty.name().toLowerCase(Locale.ROOT);
            healthMultiplier = Mlcgames.dungeonConfiguration.getDouble(
                    "difficulty." + difficulty + ".health-multiplier",
                    Dungeongame.difficulty.healthMultiplier());
            damageMultiplier = Mlcgames.dungeonConfiguration.getDouble(
                    "difficulty." + difficulty + ".damage-multiplier",
                    Dungeongame.difficulty.damageMultiplier());
            double levelStep = Mlcgames.dungeonConfiguration.getDouble(
                    "progression.level-stat-multiplier", 1.5);
            if (levelStep <= 0.0) levelStep = 1.5;
            double levelMultiplier = Math.pow(levelStep, Math.max(0, dungeonLevel - 1));
            healthMultiplier *= levelMultiplier;
            damageMultiplier *= levelMultiplier;
        }

        var maxHealth = entity.getAttribute(Attribute.MAX_HEALTH);
        if (maxHealth != null) {
            maxHealth.setBaseValue(Math.max(1.0, maxHealth.getBaseValue() * healthMultiplier));
            entity.setHealth(maxHealth.getValue());
        }
        var attack = entity.getAttribute(Attribute.ATTACK_DAMAGE);
        if (attack != null) {
            attack.setBaseValue(Math.max(0.0, attack.getBaseValue() * damageMultiplier));
        }
    }
}
