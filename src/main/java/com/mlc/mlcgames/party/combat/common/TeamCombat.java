package com.mlc.mlcgames.party.combat.common;

import com.mlc.mlcgames.party.core.*;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.*;
import net.kyori.adventure.text.format.NamedTextColor;
import java.util.*;

/** Per-session team ownership, never the legacy plugin's shared scoreboard. */
public abstract class TeamCombat implements PartyGame {
    protected final Map<UUID,Integer> teams = new HashMap<>();
    protected final String[] teamKeys = {"red", "blue"};
    protected void validateTeams(PartyConfig c, PartyConfig.Arena a) {
        int required = (c.integer("players.max",2,64)+1)/2;
        for (String key : teamKeys) if (a.teamSpawns().getOrDefault(key,List.of()).size()<required)
            throw new IllegalArgumentException("points.team-spawns."+key+" 至少需要 "+required+" 个点位");
    }
    protected void assign(PartySession s) {
        List<Player> list = new ArrayList<>(s.players()); Collections.shuffle(list);
        Scoreboard board = Objects.requireNonNull(Bukkit.getScoreboardManager()).getNewScoreboard();
        Team[] ts = {board.registerNewTeam("party_red"),board.registerNewTeam("party_blue")};
        ts[0].color(NamedTextColor.RED);ts[1].color(NamedTextColor.BLUE);
        for(Team t:ts)t.setAllowFriendlyFire(false);
        for(int i=0;i<list.size();i++){Player p=list.get(i);teams.put(p.getUniqueId(),i%2);ts[i%2].addEntry(p.getName());p.setScoreboard(board);}
    }
    protected int team(Player p){return teams.getOrDefault(p.getUniqueId(),-1);}
    protected List<Player> teamPlayers(PartySession s,int team){return s.players().stream().filter(p->team(p)==team).toList();}
    protected void spawn(PartySession s,Player p) {
        List<Player> members=teamPlayers(s,team(p));
        List<Location> points=s.arena.teamSpawns().get(teamKeys[team(p)]);
        p.leaveVehicle();p.setGameMode(GameMode.SURVIVAL);p.getInventory().clear();p.clearActivePotionEffects();
        p.setHealth(Objects.requireNonNull(p.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH)).getValue());
        p.setFireTicks(0);p.setFallDistance(0);p.setVelocity(new org.bukkit.util.Vector());
        if(!p.teleport(points.get(Math.max(0,members.indexOf(p))%points.size())))throw new IllegalStateException("队伍传送失败");
    }
    protected void finishTeam(PartySession s,int winner,int points){
        List<Player> winners=winner<0?List.of():teamPlayers(s,winner);
        for(Player p:winners)s.points(p,points);
        s.finish(winners);
    }
    protected ItemStack item(PartySession s,Material m){return s.item(m,"items.equipment");}
    protected void fill(PartySession s,Cuboid c,Material m){
        for(int x=c.minX();x<=c.maxX();x++)for(int y=c.minY();y<=c.maxY();y++)for(int z=c.minZ();z<=c.maxZ();z++)s.set(s.arena.world().getBlockAt(x,y,z),m);
    }
    @Override public boolean lastPlayerWins(){return false;}
}
