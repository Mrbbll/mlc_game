/**
 * 文件说明：CraftEngine 实验性数值属性 API 的唯一访问边界。
 * 目前用于读取和调试攻击/防御等数值；未来伤害公式接入也应只经由此类，避免 CE API 散落在核心逻辑中。
 */
package com.mlc.mlcgames.combat.integration.craftengine;

import com.mlc.mlcgames.combat.CombatAttribute;
import net.momirealms.craftengine.bukkit.api.BukkitAdaptor;
import net.momirealms.craftengine.bukkit.attribute.BukkitAttributeManager;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.entity.LivingEntity;
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
            if (!failureReported) {
                failureReported = true;
                plugin.getLogger().warning("CraftEngine attribute integration is unavailable; numeric combat attributes are ignored. "
                        + exception.getClass().getSimpleName());
            }
            return OptionalDouble.empty();
        }
    }
}
