package com.mlc.mlcgames.dungeongame.gamephase;

import com.mlc.mlcgames.Mlcgames;
import com.mlc.mlcgames.dungeongame.managers.Roommanager;
import com.mlc.mlcgames.dungeongame.managers.Worldmanager;

/** 初始化：从 dungeongame.yml 读取配置并组装房间列表。 */
public class dungeongameinit {
    public static void init(){
        reloadTemplates();
        Worldmanager.createDungeonWorld();
    }

    public static int reloadTemplates() {
        int dungeonSet = Mlcgames.dungeonConfiguration.getInt("generation.set", 1);
        int templateCount = Roommanager.loadSchematicTemplates(dungeonSet);
        Worldmanager.layoutOriginX = Mlcgames.dungeonConfiguration.getInt("layout.origin.x", 0);
        Worldmanager.layoutOriginY = Mlcgames.dungeonConfiguration.getInt("layout.origin.y", 80);
        Worldmanager.layoutOriginZ = Mlcgames.dungeonConfiguration.getInt("layout.origin.z", 0);
        Worldmanager.layoutCellSize = Math.max(16, Mlcgames.dungeonConfiguration.getInt("layout.cell-size", 64));
        if (templateCount == 0) {
            Mlcgames.instance.getLogger().warning("No matching FAWE room schematics found for dungeon set " + dungeonSet);
        }
        return templateCount;

    }
}
