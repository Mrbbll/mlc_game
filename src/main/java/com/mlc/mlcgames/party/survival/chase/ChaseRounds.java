package com.mlc.mlcgames.party.survival.chase;

import com.mlc.mlcgames.party.core.*;
import org.bukkit.*;
import org.bukkit.entity.Player;
import java.util.*;

/** All participants return each round; the eligible hunter pool is consumed once. */
public abstract class ChaseRounds implements PartyGame {
    protected final Map<UUID,Integer> coins=new LinkedHashMap<>();
    protected final Set<UUID> unused=new LinkedHashSet<>(),hunters=new HashSet<>(),runners=new LinkedHashSet<>();
    protected boolean running;
    protected int readyAt,endsAt;
    @Override public void validate(PartyConfig c,PartyConfig.Arena a){a.group("hunters");c.integer("round-seconds",10,300);c.integer("between-round-seconds",1,30);}
    @Override public boolean lastPlayerWins(){return false;}
    @Override public void start(PartySession s){for(Player p:s.players()){unused.add(p.getUniqueId());coins.put(p.getUniqueId(),initialCoins());}next(s);}
    protected int initialCoins(){return 0;}
    protected void next(PartySession s){
        running=false;hunters.clear();runners.clear();unused.removeIf(id->{Player p=Bukkit.getPlayer(id);return p==null||!s.member(p);});
        if(unused.isEmpty()||s.players().size()<2){settle(s);return;}
        s.resetRound(()->{s.resetPlayers();prepareRound(s);readyAt=s.elapsed()+s.config.integer("between-round-seconds",1,30)*20;s.broadcast("round-pause","seconds",s.config.integer("between-round-seconds",1,30));});
    }
    protected void prepareRound(PartySession s){}
    protected void beginRound(PartySession s){
        List<UUID> pool=new ArrayList<>(unused);Collections.shuffle(pool);
        int count=1;for(int threshold=6;threshold<=60;threshold+=3)if(s.players().size()+2>=threshold)count++;
        for(UUID id:pool.stream().limit(count).toList()){hunters.add(id);unused.remove(id);}
        for(Player p:s.players())if(!hunters.contains(p.getUniqueId()))runners.add(p.getUniqueId());
        for(Player p:s.survivors()){
            if(hunter(p))p.teleport(s.arena.group("hunters").get(new Random().nextInt(s.arena.group("hunters").size())));
            equipment(s,p);p.sendMessage(s.config.message(hunter(p)?"hunter-role":"runner-role"));
            p.setLevel(coins.getOrDefault(p.getUniqueId(),0));
        }
        running=true;endsAt=s.elapsed()+s.config.integer("round-seconds",10,300)*20;
    }
    protected abstract void equipment(PartySession s,Player p);
    protected boolean hunter(Player p){return hunters.contains(p.getUniqueId());}
    protected void add(PartySession s,Player p,int amount){coins.compute(p.getUniqueId(),(id,old)->Math.max(0,(old==null?0:old)+amount));p.setLevel(coins.get(p.getUniqueId()));p.sendActionBar(s.config.message("coin-change","amount",amount,"coins",coins.get(p.getUniqueId())));}
    protected void caught(PartySession s,Player p){if(!runners.remove(p.getUniqueId()))return;onCaught(s,p);s.retire(p);s.broadcast("caught","player",p.getName());}
    protected void onCaught(PartySession s,Player p){}
    protected void roundOver(PartySession s){next(s);}
    protected boolean advance(PartySession s,int elapsed){
        if(!running){if(elapsed>=readyAt)beginRound(s);return false;}
        runners.removeIf(id->{Player p=Bukkit.getPlayer(id);return p==null||!s.member(p);});
        if(elapsed>=endsAt||runners.isEmpty()){roundOver(s);return false;}
        if(elapsed%20==0)for(Player p:s.players())p.sendActionBar(s.config.message("status","seconds",(endsAt-elapsed+19)/20,"coins",coins.getOrDefault(p.getUniqueId(),0)));
        return true;
    }
    @Override public void hazard(PartySession s,Player p){if(!running||hunter(p)){p.setHealth(20);p.setFireTicks(0);p.setFallDistance(0);p.teleport(hunter(p)?s.arena.group("hunters").getFirst():s.arena.spawns().getFirst());if(hunter(p))equipment(s,p);}else caught(s,p);}
    protected void settle(PartySession s){Map<UUID,Integer> present=new LinkedHashMap<>();for(Player p:s.players())present.put(p.getUniqueId(),coins.getOrDefault(p.getUniqueId(),0));List<Player> winners=new ArrayList<>();for(var row:Rankings.descending(present)){Player p=Bukkit.getPlayer(row.id());if(p==null)continue;s.points(p,Math.max(0,4-row.rank()));s.broadcast("ranking","player",p.getName(),"coins",row.score(),"rank",row.rank());if(row.rank()==1)winners.add(p);}s.finish(winners);}
    @Override public void timeout(PartySession s){settle(s);}
}
