package com.mlc.mlcgames.sandgame.gamephase;

import com.mlc.mlcgames.Mlcgames;
import com.mlc.mlcgames.sandgame.Sandgame;
import com.mlc.mlcgames.sandgame.menus.SandGameMenu;
import org.bukkit.Location;


import java.io.File;
import java.io.IOException;
import java.util.Objects;

import static com.mlc.mlcgames.Mlcgames.instance;

public class sandgameinit {
    public static void init() throws IOException {
        Sandgame.isstart = false;
        SandGameMenu.init();
        Sandgame.player_money.clear();
        Sandgame.player_kill_count.clear();

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
