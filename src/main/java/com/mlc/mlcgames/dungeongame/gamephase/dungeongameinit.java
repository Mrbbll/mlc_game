package com.mlc.mlcgames.dungeongame.gamephase;

import com.mlc.mlcgames.Mlcgames;
import com.mlc.mlcgames.dungeongame.DungeonDifficulty;
import com.mlc.mlcgames.dungeongame.Dungeongame;
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
        Worldmanager.passageWidth = Mlcgames.dungeonConfiguration.getInt("passage.width", 8);
        Worldmanager.passageHeight = Mlcgames.dungeonConfiguration.getInt("passage.height", 8);
        Worldmanager.passageBottomOffset = Mlcgames.dungeonConfiguration.getInt("passage.bottom-offset", 1);
        if (!Dungeongame.isstart) {
            Dungeongame.mapsize = Math.max(4, Mlcgames.dungeonConfiguration.getInt("generation.normal-rooms", 7));
            Dungeongame.specialRoomChance = Math.max(0, Math.min(100,
                    Mlcgames.dungeonConfiguration.getInt("generation.special-chance", 70)));
            Dungeongame.currentLevel = 1;
            Dungeongame.currentFloor = 1;
            Dungeongame.difficulty = DungeonDifficulty.parse(
                    Mlcgames.dungeonConfiguration.getString("generation.difficulty", "normal"));
        }
        if (templateCount == 0) {
            Mlcgames.instance.getLogger().warning("No matching FAWE room schematics found for dungeon set " + dungeonSet);
        }
        return templateCount;

    }
}
