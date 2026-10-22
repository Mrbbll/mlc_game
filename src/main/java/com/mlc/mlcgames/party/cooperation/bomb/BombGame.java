package com.mlc.mlcgames.party.cooperation.bomb;

import com.mlc.mlcgames.party.core.*;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Consumable;
import io.papermc.paper.datacomponent.item.consumable.ItemUseAnimation;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.SculkShrieker;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import java.util.*;

/** Sequential defusal attempts. Real sculk vibration and wool occlusion remain native. */
public final class BombGame implements PartyGame {
    private final Map<UUID,Integer> totals=new LinkedHashMap<>();
    private final Deque<UUID> queue=new ArrayDeque<>();
    private static final Set<Material> SCULK=EnumSet.of(Material.SCULK,Material.SCULK_CATALYST,Material.SCULK_SENSOR,Material.SCULK_SHRIEKER,Material.SCULK_VEIN);
    private int level,endLevel,deadline,resumeAt;
    private Player current;
    private Cuboid field;
    private boolean retry;
    @Override public void validate(PartyConfig c,PartyConfig.Arena a){
        long volume=0;
        for(int i=1;i<=6;i++){Cuboid region=a.region("level-"+i);volume+=region.volume();if(region.volume()>10000)throw new IllegalArgumentException("每个拆弹关卡最多10000方块");boolean shrieker=false,sensor=false;for(int x=region.minX();x<=region.maxX();x++)for(int z=region.minZ();z<=region.maxZ();z++){Material material=a.world().getBlockAt(x,region.minY(),z).getType();shrieker|=material==Material.SCULK_SHRIEKER;sensor|=material==Material.SCULK_SENSOR;}if(!shrieker||!sensor)throw new IllegalArgumentException("每关最低层至少放置一个幽匿尖啸体和潜声传感器");}
        if(volume>c.integer("limits.max-snapshot-blocks",1,100000))throw new IllegalArgumentException("六个关卡体积超过快照上限");
        if(a.group("level-starts").size()!=6)throw new IllegalArgumentException("拆弹需6个关卡起点，依次对应level-1..6");
        c.integer("selected-level",1,6);c.integer("turn-seconds",10,600);c.integer("reset-cooldown-seconds",1,10);
    }
    @Override public List<Cuboid> writableRegions(PartyConfig.Arena a){List<Cuboid> list=new ArrayList<>();for(int i=1;i<=6;i++)list.add(a.region("level-"+i));return list;}
    @Override public boolean lastPlayerWins(){return false;}
    @Override public int maximumDurationSeconds(){return 172800;}
    @Override public void start(PartySession s){
        for(Player p:s.players())totals.put(p.getUniqueId(),0);
        level=s.config.rules.getBoolean("all-levels",true)?1:s.config.integer("selected-level",1,6);endLevel=s.config.rules.getBoolean("all-levels",true)?6:level;
        prepareLevel(s);
    }
    private void prepareLevel(PartySession s){
        field=s.arena.region("level-"+level);queue.clear();List<Player> shuffled=new ArrayList<>(s.players());Collections.shuffle(shuffled);for(Player p:shuffled)queue.add(p.getUniqueId());next(s);
    }
    private void next(PartySession s){
        current=null;
        while(!queue.isEmpty()&&current==null){Player p=Bukkit.getPlayer(queue.remove());if(p!=null&&s.member(p))current=p;}
        if(current==null){if(++level>endLevel){settle(s);return;}prepareLevel(s);return;}
        deadline=s.elapsed()+s.config.integer("turn-seconds",10,600)*20;restore(s,false);
    }
    private void restore(PartySession s,boolean sameTurn){
        retry=sameTurn;
        s.resetRound(()->{
            s.resetPlayers();for(Player p:s.players()){p.getInventory().clear();p.setGameMode(GameMode.SPECTATOR);p.teleport(s.arena.spectator());}
            if(current==null||!s.member(current)){next(s);return;}
            current.setGameMode(GameMode.SURVIVAL);current.teleport(s.arena.group("level-starts").get(level-1));
            resumeAt=s.elapsed()+s.config.integer("reset-cooldown-seconds",1,10)*20;items(s);s.broadcast(sameTurn?"retry":"turn","player",current.getName(),"level",level);
        });
    }
    private void items(PartySession s){
        ItemStack hoe=s.item(Material.NETHERITE_HOE,"items.hoe");hoe.addUnsafeEnchantment(Enchantment.EFFICIENCY,5);current.getInventory().setItem(0,hoe);
        ItemStack wool=s.item(Material.LIME_WOOL,"items.wool");wool.setAmount(4);current.getInventory().setItem(1,wool);
        for(int slot=7;slot<=8;slot++){ItemStack control=s.item(Material.POTION,slot==7?"items.retry":"items.give-up");control.setData(DataComponentTypes.CONSUMABLE,Consumable.consumable().consumeSeconds(1).animation(ItemUseAnimation.DRINK).build());current.getInventory().setItem(slot,control);}
    }
    @Override public void tick(PartySession s,int elapsed){
        if(current==null||!s.member(current)){next(s);return;}
        if(elapsed>=deadline){s.broadcast("turn-timeout","player",current.getName());next(s);return;}
        if(elapsed<resumeAt)return;
        // Check every tick so a brief native shrieking state cannot be missed.
        for(int x=field.minX();x<=field.maxX();x++)for(int y=field.minY();y<=field.maxY();y++)for(int z=field.minZ();z<=field.maxZ();z++){
            Block b=s.arena.world().getBlockAt(x,y,z);
            if(b.getBlockData() instanceof SculkShrieker shrieker&&shrieker.isShrieking()){s.broadcast("detonated");restore(s,true);return;}
        }
        if(elapsed%20!=0)return;
        int sculk=0,wool=0;
        for(int x=field.minX();x<=field.maxX();x++)for(int z=field.minZ();z<=field.maxZ();z++){
            Material m=s.arena.world().getBlockAt(x,field.minY(),z).getType();if(SCULK.contains(m))sculk++;if(m==Material.LIME_WOOL)wool++;}
        if(sculk==0){int score=200-wool;totals.merge(current.getUniqueId(),score,Integer::sum);s.broadcast("defused","player",current.getName(),"score",score,"total",totals.get(current.getUniqueId()));next(s);return;}
        items(s);for(Player p:s.players())p.sendActionBar(s.config.message("status","player",current.getName(),"level",level,"seconds",(deadline-elapsed+19)/20));
    }
    private boolean turn(PartySession s,Player p){return current!=null&&current.equals(p)&&s.elapsed()>=resumeAt;}
    private boolean inside(Block b){return field!=null&&field.contains(b.getX(),b.getY(),b.getZ());}
    @Override public boolean breakBlock(PartySession s,Player p,Block b){boolean allowed=turn(s,p)&&inside(b)&&SCULK.contains(b.getType());if(allowed)b.getWorld().sendGameEvent(p,GameEvent.BLOCK_DESTROY,b.getLocation().toVector());return allowed;}
    @Override public boolean placeBlock(PartySession s,Player p,Block b){if(!turn(s,p)||!inside(b)||b.getType()!=Material.LIME_WOOL)return false;Material floor=b.getRelative(0,-1,0).getType();return floor==Material.BLACK_WOOL||SCULK.contains(floor);}
    @Override public boolean allowPhysics(PartySession s,Block b){return s.writable(b);}
    @Override public boolean suppressArenaDrops(){return true;}
    @Override public void consume(PartySession s,PlayerItemConsumeEvent e){e.setCancelled(true);if(!turn(s,e.getPlayer()))return;int slot=e.getPlayer().getInventory().getHeldItemSlot();if(slot==7)restore(s,true);else if(slot==8)next(s);}
    @Override public void hazard(PartySession s,Player p){if(current!=null&&current.equals(p)){s.broadcast("turn-timeout","player",p.getName());next(s);}}
    private void settle(PartySession s){
        Map<UUID,Integer> present=new LinkedHashMap<>();for(Player p:s.players())present.put(p.getUniqueId(),totals.getOrDefault(p.getUniqueId(),0));
        List<Player> winners=new ArrayList<>();for(var row:Rankings.descending(present)){Player p=Bukkit.getPlayer(row.id());if(p==null)continue;s.broadcast("ranking","player",p.getName(),"score",row.score());if(row.rank()==1){winners.add(p);s.points(p,1);}}
        s.finish(winners);
    }
    @Override public void timeout(PartySession s){settle(s);}
}
