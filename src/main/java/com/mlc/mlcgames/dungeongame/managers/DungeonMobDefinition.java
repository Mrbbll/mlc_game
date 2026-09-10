package com.mlc.mlcgames.dungeongame.managers;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;

import java.util.List;
import java.util.Map;
import java.util.Random;

/** 从一个环境怪物包解析出的不可变怪物定义。 */
record DungeonMobDefinition(
        String id,
        DungeonSession.MarkerKind category,
        EntityType entityType,
        int weight,
        String displayName,
        boolean nameVisible,
        boolean scaleWithDifficulty,
        Map<Attribute, Double> attributes,
        Equipment equipment
) {
    /** 六个实体装备槽；空字符串表示显式清空对应槽位。 */
    record Equipment(
            String helmet,
            String chestplate,
            String leggings,
            String boots,
            String mainHand,
            String offHand,
            boolean autoPixelArmor
    ) { }
}

/**
 * 对应一个 {@code <关卡>_<环境>.yml}。池在加载时已经完成校验，生成期不再解析字符串。
 */
record DungeonMobPack(
        int dungeonLevel,
        int environmentSet,
        Map<DungeonSession.MarkerKind, List<DungeonMobDefinition>> pools
) {
    DungeonMobDefinition pick(DungeonSession.MarkerKind category, Random random) {
        List<DungeonMobDefinition> choices = pools.get(category);
        if (choices == null || choices.isEmpty()) {
            throw new IllegalStateException("Mob pack " + dungeonLevel + "_" + environmentSet
                    + " has no " + category.name().toLowerCase() + " mobs");
        }

        long totalWeight = 0;
        for (DungeonMobDefinition choice : choices) totalWeight += choice.weight();
        long selected = random.nextLong(totalWeight);
        for (DungeonMobDefinition choice : choices) {
            selected -= choice.weight();
            if (selected < 0) return choice;
        }
        return choices.getLast();
    }
}
