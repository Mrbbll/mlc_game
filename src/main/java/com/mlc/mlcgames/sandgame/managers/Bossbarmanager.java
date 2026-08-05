package com.mlc.mlcgames.sandgame.managers;

import com.mlc.mlcgames.sandgame.Sandgame;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;


import static com.mlc.mlcgames.Mlcgames.miniMessage;

public class Bossbarmanager {
    public static BossBar bossBar;
    public static Component bossbarname;


    public static void init(){

        bossBar = BossBar.bossBar(miniMessage.deserialize("Sandgame"),1.0f, net.kyori.adventure.bossbar.BossBar.Color.PINK, net.kyori.adventure.bossbar.BossBar.Overlay.NOTCHED_20);

    }
    public static void setBossBar(){
        if(Sandgame.isstart){
            bossbarname = getbossbarname();
        }
        else{
            bossbarname = miniMessage.deserialize("<b><green>Sandgame ready phase");

        }
        bossBar.name(bossbarname);
        for(Player player : Sandgame.getSandgamePlayer()){
            bossBar.addViewer(player);
        }
    }

    private static Component getbossbarname() {
        return miniMessage.deserialize("<red>red team:<team1_sandcount>|<blue>blue team:<team2_sandcount>",
                Placeholder.parsed("team1_sandcount","" + Sandgame.team_1_sand_count),
                Placeholder.parsed("team2_sandcount","" + Sandgame.team_2_sand_count));
    }

    public static void updateBossBar(){
        setBossBar();
        bossBar.progress(Sandgame.countdown / 30.0f);
    }
}
