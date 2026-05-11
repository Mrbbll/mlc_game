package com.mlc.mlcgames.zombieday.gamephase;

import com.mlc.mlcgames.zombieday.Zombiedaygame;
import org.bukkit.Location;

import java.io.IOException;
import java.util.HashSet;
import java.util.Objects;

import static com.mlc.mlcgames.Mlcgames.instance;
import static com.mlc.mlcgames.Mlcgames.zombiedayConfiguration;
import static com.mlc.mlcgames.zombieday.Zombiedaygame.zombielocs;

public class Init {
    public static void init() throws IOException {
        Zombiedaygame.respawnloc = zombiedayConfiguration.getLocation("respawnloc");
        if(Zombiedaygame.respawnloc == null){
            Zombiedaygame.respawnloc = new Location(instance.getServer().getWorld("world"),0,0,0 );
            zombiedayConfiguration.set("respawnloc", Zombiedaygame.respawnloc);
        }


        zombielocs = new HashSet<>();
        int n = 1;
        Location location = zombiedayConfiguration.getLocation("location.zombieloc_"+n);
        while(location != null){
            zombielocs.add(location);
            n++;
            location = zombiedayConfiguration.getLocation("location.zombieloc_"+n);
        }

        Zombiedaygame.fixlocs = new HashSet<>();
        n = 1;
        location = zombiedayConfiguration.getLocation("location.fixloc_"+n);
        while(location != null){
            Zombiedaygame.fixlocs.add(location);
            n++;
            location = zombiedayConfiguration.getLocation("location.fixloc_"+n);
        }

        zombiedayConfiguration.save(instance.getDataFolder().getCanonicalPath());
    }
}
