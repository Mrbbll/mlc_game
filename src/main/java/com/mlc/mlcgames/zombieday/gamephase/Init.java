package com.mlc.mlcgames.zombieday.gamephase;

import com.mlc.mlcgames.zombieday.Item;
import com.mlc.mlcgames.zombieday.Zombiedaygame;
import com.mlc.mlcgames.zombieday.inv.Potioninv;
import org.bukkit.Location;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import static com.mlc.mlcgames.Mlcgames.instance;
import static com.mlc.mlcgames.Mlcgames.zombiedayConfiguration;
import static com.mlc.mlcgames.zombieday.Zombiedaygame.*;

public class Init {
    public static void init() throws IOException {
        //初始化物品
        Item.init();
        Zombiedaygame.turn = 0;
        Zombiedaygame.isstart = false;
        Zombiedaygame.respawnloc = zombiedayConfiguration.getLocation("respawnloc");
        if(Zombiedaygame.respawnloc == null){
            Zombiedaygame.respawnloc = new Location(instance.getServer().getWorld("world"),0,0,0 );
            zombiedayConfiguration.set("respawnloc", Zombiedaygame.respawnloc);
        }
        //初始化药水商店
        Potioninv.init();

        //僵尸点位
        zombieskylocs = new ArrayList<>();
        int n = 1;
        Location location = zombiedayConfiguration.getLocation("location.zombieloc_"+n);
        while(location != null){
            zombieskylocs.add(location);
            n++;
            location = zombiedayConfiguration.getLocation("location.zombieloc_"+n);
        }

        zombieloc1 = zombiedayConfiguration.getLocation("location.zombieloc1");
        if(zombieloc1 == null){
            zombieloc1 = new Location(instance.getServer().getWorld("world"),0,0,0 );
            zombiedayConfiguration.set("location.zombieloc1", zombieloc1);
        }
        zombieloc2 = zombiedayConfiguration.getLocation("location.zombieloc2");
        if(zombieloc2 == null){
            zombieloc2 = new Location(instance.getServer().getWorld("world"),0,0,0 );
            zombiedayConfiguration.set("location.zombieloc2", zombieloc2);
        }

//        Zombiedaygame.fixlocs = new HashSet<>();
//        n = 1;
//        location = zombiedayConfiguration.getLocation("location.fixloc_"+n);
//        while(location != null){
//            Zombiedaygame.fixlocs.add(location);
//            n++;
//            location = zombiedayConfiguration.getLocation("location.fixloc_"+n);
//        }

        zombiedayConfiguration.save(new File(instance.getDataFolder(), "zombieday.yml"));
    }
}
