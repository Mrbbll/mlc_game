package com.mlc.mlcgames.dungeongame.managers;

import com.mlc.mlcgames.Mlcgames;
import org.bukkit.Material;

/** 地牢配置的容错读取入口，避免每个服务各自重复 Material 回退逻辑。 */
final class DungeonConfiguration {
    private DungeonConfiguration() { }

    static Material material(String path, Material fallback) {
        String configured = Mlcgames.dungeonConfiguration.getString(path, fallback.name());
        Material material = Material.matchMaterial(configured);
        return material == null ? fallback : material;
    }
}
