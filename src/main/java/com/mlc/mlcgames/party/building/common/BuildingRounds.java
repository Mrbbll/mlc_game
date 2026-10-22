package com.mlc.mlcgames.party.building.common;

import com.mlc.mlcgames.party.core.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import java.util.*;

/** Shared round bookkeeping; each game defines its own building and scoring rules. */
public abstract class BuildingRounds implements PartyGame {
    protected final Map<UUID,Integer> coins = new LinkedHashMap<>();
    protected final Deque<UUID> builders = new ArrayDeque<>();
    protected final Set<UUID> playing = new HashSet<>();
    protected Player builder;
    protected Cuboid build;
    protected Set<Material> palette;
    protected int deadline, transition;
    private boolean awaitingBuild;
    @Override public void validate(PartyConfig c, PartyConfig.Arena a) {
        build = a.region("build"); a.group("builder"); a.group("runners");
        palette = c.materials("building-materials");
        c.integer("builder-limit",0,64); c.integer("round-break-seconds",1,30);
        if(build.volume()>c.integer("limits.max-snapshot-blocks",1,100000)) throw new IllegalArgumentException("建造区超出快照限制");
        if(palette.size()>32) throw new IllegalArgumentException("建材最多32种，预留工具与操作物品栏");
    }
    @Override public List<Cuboid> writableRegions(PartyConfig.Arena a){return List.of(a.region("build"));}
    @Override public boolean lastPlayerWins(){return false;}
    @Override public int maximumDurationSeconds(){return 86400;}
    protected void initialize(PartySession s){
        List<Player> list=new ArrayList<>(s.players()); Collections.shuffle(list);
        for(Player p:list) coins.put(p.getUniqueId(),1);
        int limit=s.config.integer("builder-limit",0,64);
        list.stream().limit(limit==0?64:limit).forEach(p->builders.add(p.getUniqueId()));
        next(s);
    }
    protected void next(PartySession s){
        playing.clear(); builder=null;
        while(!builders.isEmpty() && builder==null){Player p=Bukkit.getPlayer(builders.remove()); if(p!=null&&s.member(p))builder=p;}
        if(builder==null||s.players().size()<2){settle(s);return;}
        transition=s.elapsed()+s.config.integer("round-break-seconds",1,30)*20;
        s.resetRound(()->{
            s.resetPlayers();
            for(Player p:s.players()){p.getInventory().clear();p.setGameMode(GameMode.SPECTATOR);p.teleport(s.arena.spectator());}
            // Source clears the entire build volume at the start of each turn.
            for(int x=build.minX();x<=build.maxX();x++)for(int y=build.minY();y<=build.maxY();y++)for(int z=build.minZ();z<=build.maxZ();z++)
                s.set(s.arena.world().getBlockAt(x,y,z),Material.AIR);
            // The session queues writes over its per-tick budget. Do not expose the
            // builder until the last queued clear has drained, or it can erase a new block.
            int budget=s.config.integer("limits.blocks-per-tick",1,2000);
            transition=s.elapsed()+Math.max(s.config.integer("round-break-seconds",1,30)*20,(int)((build.volume()+budget-1)/budget)+2);
            awaitingBuild=true;
        });
    }
    protected boolean readyToPlay(PartySession s,int elapsed){
        if(awaitingBuild){if(elapsed<transition)return false;awaitingBuild=false;begin(s);}
        return true;
    }
    protected abstract void begin(PartySession s);
    protected void announceBuilder(PartySession s){s.broadcast("builder-turn","player",builder.getName());}
    protected void givePalette(PartySession s){
        builder.getInventory().clear(); int slot=0;
        for(Material m:palette)builder.getInventory().setItem(slot++,new ItemStack(m,64));
        builder.getInventory().setItem(35,s.item(Material.DIAMOND_PICKAXE,"items.tool"));
        builder.setGameMode(GameMode.SURVIVAL); builder.teleport(s.arena.group("builder").getFirst());
    }
    protected void refill(){for(Material m:palette)if(!builder.getInventory().contains(m))builder.getInventory().addItem(new ItemStack(m,64));}
    protected void add(PartySession s,Player p,int n){coins.merge(p.getUniqueId(),n,Integer::sum);p.sendMessage(s.config.message("coins","amount",n,"total",coins.get(p.getUniqueId())));}
    protected boolean inside(Block b){return build.contains(b.getX(),b.getY(),b.getZ());}
    protected boolean isBuilder(Player p){return builder!=null&&builder.equals(p);}
    @Override public boolean allowInventoryEdit(PartySession s,Player p){return isBuilder(p);}
    @Override public boolean allowSwap(PartySession s,Player p){return isBuilder(p);}
    @Override public boolean allowBlockInteraction(PartySession s,Player p,Block b){return inside(b)&&palette.contains(b.getType());}
    @Override public boolean instantBreak(PartySession s,Player p,Block b){return breakBlock(s,p,b);}
    @Override public void hazard(PartySession s,Player p){
        p.setFallDistance(0);p.setFireTicks(0);
        p.teleport(isBuilder(p)?s.arena.group("builder").getFirst():s.arena.group("runners").getFirst());
    }
    protected void settle(PartySession s){
        Map<UUID,Integer> present=new LinkedHashMap<>();for(Player p:s.players())present.put(p.getUniqueId(),coins.getOrDefault(p.getUniqueId(),1));
        List<Player> winners=new ArrayList<>();
        for(var row:Rankings.descending(present)){Player p=Bukkit.getPlayer(row.id());if(p==null)continue;
            if(row.rank()==1)winners.add(p);s.points(p,Math.max(0,4-row.rank()));
            s.broadcast("ranking","rank",row.rank(),"player",p.getName(),"coins",row.score());}
        s.finish(winners);
    }
    @Override public void timeout(PartySession s){settle(s);}
}
