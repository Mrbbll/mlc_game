package com.mlc.mlcgames.zombieday.gamephase;

import com.mlc.mlcgames.zombieday.Item;
import com.mlc.mlcgames.zombieday.Zombiedaygame;
import com.mlc.mlcgames.zombieday.inv.*;
import org.bukkit.Location;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static com.mlc.mlcgames.Mlcgames.instance;
import static com.mlc.mlcgames.Mlcgames.zombiedayConfiguration;
import static com.mlc.mlcgames.zombieday.Zombiedaygame.*;

public class Init {
    public static void init() throws IOException {
        //初始化物品
        Item.init();
        Zombiedaygame.turn = 0;
        Zombiedaygame.isstart = false;
        Zombiedaygame.respawnloc = zombiedayConfiguration.getLocation("location.respawnloc");
        if(Zombiedaygame.respawnloc == null){
            Zombiedaygame.respawnloc = new Location(instance.getServer().getWorld("world"),0,0,0 );
            zombiedayConfiguration.set("location.respawnloc", Zombiedaygame.respawnloc);
        }
        //初始化药水商店
        PotionInv.init();
        BulletInv.init();
        FoodInv.init();
        ArmorInv.init();
        EffectInv.init();

        prepareloc = zombiedayConfiguration.getLocation("location.prepareloc");
        if(prepareloc == null){
            prepareloc = new Location(instance.getServer().getWorld("world"),0,1,0 );
            zombiedayConfiguration.set("location.prepareloc", prepareloc);
        }

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

        arealoc1 = zombiedayConfiguration.getLocation("location.arealoc1");
        if(arealoc1 == null){
            arealoc1 = new Location(instance.getServer().getWorld("world"),0,0,0 );
            zombiedayConfiguration.set("location.arealoc1", arealoc1);
        }
        arealoc2 = zombiedayConfiguration.getLocation("location.arealoc2");
        if(arealoc2 == null){
            arealoc2 = new Location(instance.getServer().getWorld("world"),0,0,0 );
            zombiedayConfiguration.set("location.arealoc2", arealoc2);
        }
        arealoc3 = zombiedayConfiguration.getLocation("location.arealoc3");
        if(arealoc3 == null){
            arealoc3 = new Location(instance.getServer().getWorld("world"),0,0,0 );
            zombiedayConfiguration.set("location.arealoc3", arealoc3);
        }
        arealoc4 = zombiedayConfiguration.getLocation("location.arealoc4");
        if(arealoc4 == null){
            arealoc4 = new Location(instance.getServer().getWorld("world"),0,0,0 );
            zombiedayConfiguration.set("location.arealoc4", arealoc4);
        }

//        while(location != null){
//            Zombiedaygame.fixlocs.add(location);
//            n++;
//            location = zombiedayConfiguration.getLocation("location.fixloc_"+n);
//        }

        zombiedayConfiguration.save(new File(instance.getDataFolder(), "zombieday.yml"));
    }
}
