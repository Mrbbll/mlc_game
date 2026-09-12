/**
 * 文件说明：CraftEngine 实验性数值属性 API 的唯一访问边界。
 * 目前用于读取和调试攻击/防御等数值；未来伤害公式接入也应只经由此类，避免 CE API 散落在核心逻辑中。
 */
package com.mlc.mlcgames.combat.integration.craftengine;

import com.mlc.mlcgames.combat.CombatAttribute;
import net.momirealms.craftengine.bukkit.api.BukkitAdaptor;
import net.momirealms.craftengine.bukkit.attribute.BukkitAttributeManager;
import net.momirealms.craftengine.core.attribute.Attribute;
import net.momirealms.craftengine.core.attribute.modifier.AttributeModifierScope;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.plugin.context.ContextHolder;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.plugin.context.SimpleContext;
import net.momirealms.craftengine.core.plugin.context.parameter.DirectContextParameters;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.OptionalDouble;

/**
 * Single boundary for CraftEngine's experimental numeric attribute API.
 * The current affinity pipeline does not read numeric values yet, preventing a second damage formula.
 */
public final class CraftEngineAttributeBridge {
    private final JavaPlugin plugin;
    private final CraftEngineHook hook;
    private boolean failureReported;

    public CraftEngineAttributeBridge(JavaPlugin plugin, CraftEngineHook hook) {
        this.plugin = plugin;
        this.hook = hook;
    }

    public OptionalDouble getEntityValue(LivingEntity entity, CombatAttribute attribute) {
        return getEntityValue(entity, attribute.id());
    }

    public OptionalDouble getEntityValue(LivingEntity entity, String attributeId) {
        if (!hook.isAvailable()) {
            return OptionalDouble.empty();
        }
        try {
            var manager = BukkitAttributeManager.instance();
            var ceAttribute = manager.getAttribute(Key.of(attributeId));
            if (ceAttribute.isEmpty()) {
                return OptionalDouble.empty();
            }
            var ceEntity = (net.momirealms.craftengine.core.entity.LivingEntity) BukkitAdaptor.adapt(entity);
            return OptionalDouble.of(manager.getAttributeValue(ceEntity, ceAttribute.get()));
        } catch (LinkageError | RuntimeException exception) {
            reportFailure(exception);
            return OptionalDouble.empty();
        }
    }

    /**
     * 返回玩家在“使用当前主手物品攻击”时的属性值。
     *
     * <p>CraftEngine 有意把 {@code scope: entity} 与 {@code scope: weapon} 分开：
     * {@code getAttributeValue} 只返回玩家常驻值，武器值需要通过
     * {@code getWeaponAttributeValue} 单独计算后相加。这里构造与 CE 伤害事件一致的
     * ITEM/ENTITY/PLAYER 上下文，确保随机物品参数和条件修饰符也能正确求值。</p>
     */
    public OptionalDouble getCombatValue(Player player, String attributeId) {
        if (!hook.isAvailable()) {
            return OptionalDouble.empty();
        }
        try {
            var manager = BukkitAttributeManager.instance();
            var ceAttribute = manager.getAttribute(Key.of(attributeId));
            if (ceAttribute.isEmpty()) {
                return OptionalDouble.empty();
            }

            var cePlayer = BukkitAdaptor.adapt(player);
            var mainHand = BukkitAdaptor.adapt(player.getInventory().getItemInMainHand());
            var context = SimpleContext.of(ContextHolder.builder()
                    .withOptionalParameter(DirectContextParameters.ITEM, mainHand)
                    .withParameter(DirectContextParameters.ENTITY, cePlayer)
                    .withParameter(DirectContextParameters.PLAYER, cePlayer)
                    .withParameter(DirectContextParameters.THIS_ENTITY, cePlayer)
                    .withParameter(DirectContextParameters.POSITION, cePlayer.position())
                    .build());

            // 武器增量可能为负数，例如“目标命中 500”会写成 500 - 基础 1000 = -500。
            // CE 26.8.2 的公开便捷方法会过早按属性最小值裁剪该增量，因此这里复现其
            // operation 顺序但延后裁剪，确保低于基础属性的武器也能正确显示。
            double entityValue = manager.getAttributeValue(cePlayer, ceAttribute.get());
            double weaponValue = getUnclampedWeaponValue(manager, mainHand, ceAttribute.get(), context);
            return OptionalDouble.of(ceAttribute.get().limit(entityValue + weaponValue));
        } catch (LinkageError | RuntimeException exception) {
            reportFailure(exception);
            return OptionalDouble.empty();
        }
    }

    /**
     * 等价于 CE AttributeModifiers.weaponValue 的运算过程，但不在“增量”阶段调用
     * Attribute.limit。约束必须应用于实体值与武器增量相加后的最终结果。
     */
    private static double getUnclampedWeaponValue(BukkitAttributeManager manager, Item item,
                                                  Attribute attribute, Context context) {
        if (item == null || item.isEmpty()) return 0.0;

        double value = 0.0;
        var modifiers = manager.getItemAttributeModifiers(item);
        for (var operation : attribute.operations()) {
            double valueBeforeOperation = value;
            for (var modifier : modifiers) {
                if (modifier.scope != AttributeModifierScope.WEAPON
                        || !modifier.attribute.equals(attribute.id())
                        || !modifier.operation.equals(operation.id())
                        || !modifier.condition.test(context)) {
                    continue;
                }
                value = operation.apply(valueBeforeOperation, value, modifier.amount.getDouble(context));
            }
        }
        return value;
    }

    private void reportFailure(Throwable exception) {
        if (failureReported) return;
        failureReported = true;
        plugin.getLogger().warning("CraftEngine attribute integration is unavailable; numeric combat attributes are ignored. "
                + exception.getClass().getSimpleName());
    }
}
