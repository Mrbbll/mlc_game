package com.mlc.mlcgames.sandgame.gamephase;

import com.mlc.mlcgames.Mlcgames;
import com.mlc.mlcgames.sandgame.Sandgame;
import com.mlc.mlcgames.sandgame.items.itemmanager;
import com.mlc.mlcgames.sandgame.items.shopmenuitem;
import com.mlc.mlcgames.sandgame.managers.Bossbarmanager;
import com.mlc.mlcgames.sandgame.menus.SandGameMenu;
import com.mlc.mlcgames.sandgame.menus.ShopMenu;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;


import java.io.File;
import java.io.IOException;
import java.util.Objects;

import static com.mlc.mlcgames.Mlcgames.*;

public class sandgameinit {
    public static void init() throws IOException {
        Sandgame.isstart = false;
        // 物品与菜单初始化：itemmanager/shopmenuitem 的静态物品字段必须先 init，
        // SandGameMenu/ShopMenu 才能引用到非 null 物品
        itemmanager.init();
        shopmenuitem.init();
        SandGameMenu.init();
        ShopMenu.init();
        Bossbarmanager.init();
        Sandgame.player_money.clear();
        Sandgame.player_kill_count.clear();
        Sandgame.end_msg_1 = miniMessage.deserialize("team 1 win");
        Sandgame.end_msg_2 = miniMessage.deserialize("team 2 win");
        Sandgame.end_msg_3 = miniMessage.deserialize("no winner");
        Sandgame.shop_success_msg = miniMessage.deserialize("shop success");
        Sandgame.shop_fail_msg = miniMessage.deserialize("shop fail");
        Sandgame.sand_spawn_msg = miniMessage.deserialize("sand spawn");
        Sandgame.team_join_msg_1 = miniMessage.deserialize("team 1 join");
        Sandgame.team_join_msg_2 = miniMessage.deserialize("team 2 join");
        Sandgame.team_join_msg_2 = miniMessage.deserialize("team 2 join");
        Sandgame.team_1_sand_bring_msg = miniMessage.deserialize("team 1 bring sand");
        Sandgame.team_2_sand_bring_msg = miniMessage.deserialize("team 2 bring sand");
        Sandgame.team_1_sand_steal_msg = miniMessage.deserialize("team 1 steal sand");
        Sandgame.team_2_sand_steal_msg = miniMessage.deserialize("team 2 steal sand");
        Sandgame.start_msg = miniMessage.deserialize("game start");


        Mlcgames.sandgameConfiguration = configManager.loadConfig("sandgame.yml");
        Location def_loc = Objects.requireNonNull(instance.getServer().getWorld("world")).getSpawnLocation();
        Sandgame.sand_spawn_loc = Mlcgames.sandgameConfiguration.getLocation("spawn_loc",def_loc);
        Mlcgames.sandgameConfiguration.set("spawn_loc",Sandgame.sand_spawn_loc);
        Sandgame.team_1_loc = Mlcgames.sandgameConfiguration.getLocation("team_1_loc",def_loc);
        Mlcgames.sandgameConfiguration.set("team_1_loc",Sandgame.team_1_loc);
        Sandgame.team_2_loc = Mlcgames.sandgameConfiguration.getLocation("team_2_loc",def_loc);
        Mlcgames.sandgameConfiguration.set("team_2_loc",Sandgame.team_2_loc);
        Sandgame.ready_loc = Mlcgames.sandgameConfiguration.getLocation("ready_loc",def_loc);
        Mlcgames.sandgameConfiguration.set("ready_loc",Sandgame.ready_loc);
        Sandgame.item_spawn_loc_1 = Mlcgames.sandgameConfiguration.getLocation("item_spawn_loc_1",def_loc);
        Mlcgames.sandgameConfiguration.set("item_spawn_loc_1",Sandgame.item_spawn_loc_1);
        Sandgame.item_spawn_loc_2 = Mlcgames.sandgameConfiguration.getLocation("item_spawn_loc_2",def_loc);
        Mlcgames.sandgameConfiguration.set("item_spawn_loc_2",Sandgame.item_spawn_loc_2);
        Sandgame.countdown = Mlcgames.sandgameConfiguration.getInt("countdown",0);
        Mlcgames.sandgameConfiguration.set("countdown",Sandgame.countdown);
        Sandgame.team_1_sand_loc = Mlcgames.sandgameConfiguration.getLocation("team_1_sand_loc",def_loc);
        Mlcgames.sandgameConfiguration.set("team_1_sand_loc",Sandgame.team_1_sand_loc);
        Sandgame.team_2_sand_loc = Mlcgames.sandgameConfiguration.getLocation("team_2_sand_loc",def_loc);
        Mlcgames.sandgameConfiguration.set("team_2_sand_loc",Sandgame.team_2_sand_loc);
        Sandgame.money_spawn_loc_1 = sandgameConfiguration.getLocation("money_spawn_loc_1",def_loc);
        Mlcgames.sandgameConfiguration.set("money_spawn_loc_1",Sandgame.money_spawn_loc_1);
        Sandgame.money_spawn_loc_2 = Mlcgames.sandgameConfiguration.getLocation("money_spawn_loc_2",def_loc);
        Mlcgames.sandgameConfiguration.set("money_spawn_loc_2",Sandgame.money_spawn_loc_2);
        Sandgame.money_spawn_loc_3 = Mlcgames.sandgameConfiguration.getLocation("money_spawn_loc_3",def_loc);
        Mlcgames.sandgameConfiguration.set("money_spawn_loc_3",Sandgame.money_spawn_loc_3);
        Sandgame.money_spawn_loc_4 = Mlcgames.sandgameConfiguration.getLocation("money_spawn_loc_4",def_loc);
        Mlcgames.sandgameConfiguration.set("money_spawn_loc_4",Sandgame.money_spawn_loc_4);


        Mlcgames.sandgameConfiguration.save(new File(instance.getDataFolder(),"sandgame.yml"));
    }

}
