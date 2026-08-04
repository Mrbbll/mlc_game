package com.mlc.mlcgames.sandgame.gamephase;

import com.mlc.mlcgames.Mlcgames;
import com.mlc.mlcgames.sandgame.Sandgame;
import com.mlc.mlcgames.sandgame.items.itemmanager;
import com.mlc.mlcgames.sandgame.items.shopmenuitem;
import com.mlc.mlcgames.sandgame.menus.SandGameMenu;
import com.mlc.mlcgames.sandgame.menus.ShopMenu;
import org.bukkit.Location;


import java.io.File;
import java.io.IOException;
import java.util.Objects;

import static com.mlc.mlcgames.Mlcgames.instance;
import static com.mlc.mlcgames.Mlcgames.miniMessage;

public class sandgameinit {
    public static void init() throws IOException {
        Sandgame.isstart = false;
        // 物品与菜单初始化：itemmanager/shopmenuitem 的静态物品字段必须先 init，
        // SandGameMenu/ShopMenu 才能引用到非 null 物品
        itemmanager.init();
        shopmenuitem.init();
        SandGameMenu.init();
        ShopMenu.init();
        Sandgame.player_money.clear();
        Sandgame.player_kill_count.clear();
        Sandgame.end_msg_1 = miniMessage.deserialize("1");
        Sandgame.end_msg_2 = miniMessage.deserialize("2");
        Sandgame.end_msg_3 = miniMessage.deserialize("3");
        Sandgame.shop_success_msg = miniMessage.deserialize("4");
        Sandgame.shop_fail_msg = miniMessage.deserialize("5");
        Sandgame.sand_spawn_msg = miniMessage.deserialize("6");
        Sandgame.team_join_msg_1 = miniMessage.deserialize("7");
        Sandgame.team_join_msg_2 = miniMessage.deserialize("8");


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

        Mlcgames.sandgameConfiguration.save(new File(instance.getDataFolder(),"sandgame.yml"));
    }

}
