package com.mlc.mlcgames.party.survival.sneak;

import com.mlc.mlcgames.party.core.Cuboid;
import com.mlc.mlcgames.party.core.PartyConfig;
import com.mlc.mlcgames.party.core.PartyGame;
import com.mlc.mlcgames.party.core.PartySession;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/** Pig-mounted tail survival. Markers mature after eight ticks and only replace the snow floor. */
public final class SneakGame implements PartyGame {
    private static final List<Material> COLORS = List.of(Material.WHITE_WOOL, Material.ORANGE_WOOL, Material.MAGENTA_WOOL,
            Material.LIGHT_BLUE_WOOL, Material.YELLOW_WOOL, Material.LIME_WOOL, Material.PINK_WOOL, Material.GRAY_WOOL,
            Material.LIGHT_GRAY_WOOL, Material.CYAN_WOOL, Material.PURPLE_WOOL, Material.BLUE_WOOL, Material.BROWN_WOOL,
            Material.GREEN_WOOL, Material.RED_WOOL, Material.BLACK_WOOL);
    private record Cell(int x, int y, int z) {}
    private static final class Trail { final Material color; int ticks; Trail(Material color, int ticks) { this.color=color; this.ticks=ticks; } }
    private final Random random = new Random();
    private final Map<UUID, Pig> mounts = new HashMap<>();
    private final Map<UUID, Material> colors = new HashMap<>();
    private final Map<Cell, Trail> trails = new LinkedHashMap<>();
    private final List<UUID> eliminated = new ArrayList<>();
    private Cuboid floor;
    private int prepared;
    private int warmup;
    private int playersAtStart;

    @Override public List<Cuboid> writableRegions(PartyConfig.Arena arena) { return List.of(arena.region("floor")); }

    @Override public void validate(PartyConfig config, PartyConfig.Arena arena) {
        floor = arena.region("floor");
        if (floor.minY() != floor.maxY()) throw new IllegalArgumentException("贪吃蛇 floor 必须是单层地面区域");
        if (floor.volume() > config.integer("limits.max-snapshot-blocks", 1, 1000000)) throw new IllegalArgumentException("贪吃蛇地面超过场地快照上限");
    }

    @Override public boolean prepare(PartySession session, int budget) {
        int sizeX=floor.maxX()-floor.minX()+1, sizeZ=floor.maxZ()-floor.minZ()+1;
        while (prepared<floor.volume()) {
            int x=floor.minX()+prepared%sizeX, z=floor.minZ()+prepared/sizeX%sizeZ;
            session.set(session.arena.world().getBlockAt(x,floor.minY(),z),Material.SNOW_BLOCK);
            prepared++;
            if (prepared%budget==0) break;
        }
        return prepared>=floor.volume();
    }

    @Override public void start(PartySession session) {
        playersAtStart=session.survivors().size();
        warmup=session.config.integer("warmup-seconds",0,60)*20;
        List<Location> starts=new ArrayList<>(session.arena.spawns());
        Collections.shuffle(starts,random);
        int i=0;
        for(Player player:session.survivors()) {
            Material wool=COLORS.get(random.nextInt(COLORS.size()));
            colors.put(player.getUniqueId(),wool);
            player.getInventory().setHelmet(new ItemStack(wool));
            player.getInventory().setItem(8,new ItemStack(wool));
            player.getInventory().setItemInOffHand(new ItemStack(Material.CARROT_ON_A_STICK));
            Pig pig=session.arena.world().spawn(starts.get(i++),Pig.class);
            pig.setAI(false); pig.setInvulnerable(true); pig.setSaddle(true); pig.setRemoveWhenFarAway(false);
            pig.addScoreboardTag("mlcgames_party_sneak_mount");
            session.track(pig); mounts.put(player.getUniqueId(),pig); pig.addPassenger(player);
        }
        session.broadcast("warmup", "seconds", warmup/20);
    }

    @Override public void tick(PartySession session,int elapsed) {
        if(warmup>0) { warmup--; if(warmup==0) session.broadcast("trail-start"); return; }
        for(Player player:session.survivors()) {
            Pig pig=mounts.get(player.getUniqueId());
            if(pig==null||!pig.isValid()||!player.isInsideVehicle()) {
                Pig free=session.arena.world().getEntitiesByClass(Pig.class).stream().filter(session::owns)
                        .filter(candidate->candidate.getPassengers().isEmpty()).min(Comparator.comparingDouble(p->p.getLocation().distanceSquared(player.getLocation()))).orElse(null);
                if(free==null){session.eliminate(player);continue;}
                mounts.put(player.getUniqueId(),free); free.addPassenger(player); pig=free;
            }
            Block below=player.getLocation().getBlock().getRelative(BlockFace.DOWN);
            if(Tag.WOOL.isTagged(below.getType())) { session.eliminate(player); continue; }
            int x=pig.getLocation().getBlockX(),z=pig.getLocation().getBlockZ(),y=floor.minY();
            if(x<floor.minX()||x>floor.maxX()||z<floor.minZ()||z>floor.maxZ()) { session.eliminate(player); continue; }
            boolean marked=trails.keySet().stream().anyMatch(c->Math.abs(c.x-x)<=1&&Math.abs(c.z-z)<=1);
            if(!marked) trails.put(new Cell(x,y,z),new Trail(colors.getOrDefault(player.getUniqueId(),Material.WHITE_WOOL),session.config.integer("trail-delay-ticks",1,40)));
        }
        Iterator<Map.Entry<Cell,Trail>> it=trails.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry<Cell,Trail> entry=it.next(); Trail trail=entry.getValue();
            if(--trail.ticks>0) continue;
            Cell c=entry.getKey(); Block block=session.arena.world().getBlockAt(c.x,c.y,c.z);
            if(block.getType()==Material.SNOW_BLOCK) session.set(block,trail.color);
            it.remove();
        }
    }

    @Override public void eliminated(PartySession session,Player player) {
        mounts.remove(player.getUniqueId()); colors.remove(player.getUniqueId());
        eliminated.add(player.getUniqueId());
        if(session.survivors().size()==1) {
            Player winner=session.survivors().get(0); session.points(winner,3);
            if(!eliminated.isEmpty()) score(session,eliminated.get(eliminated.size()-1),2);
            if(eliminated.size()>1) score(session,eliminated.get(eliminated.size()-2),1);
            session.finish(List.of(winner));
        } else if(session.survivors().isEmpty()) session.finish(List.of());
    }

    private void score(PartySession session,UUID id,int points) {
        session.players().stream().filter(p->p.getUniqueId().equals(id)).findFirst().ifPresent(p->session.points(p,points));
    }

    @Override public void timeout(PartySession session) {
        List<Player> winners=session.survivors();
        for(Player p:winners) session.points(p,3);
        session.finish(winners);
    }

    @Override public void stopped(PartySession session) { trails.clear(); mounts.clear(); colors.clear(); eliminated.clear(); }
}
