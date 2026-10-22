package com.mlc.mlcgames.party.reaction.color;

import com.google.gson.*;
import com.mlc.mlcgames.party.core.*;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.potion.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** All 17 source pattern families. Geometry is data extracted offline, never interpreted commands. */
public final class ColorGame implements PartyGame {
    private static final int[] VARIANTS={0,4,4,1,1,17,3,2,3,3,3,6,3,4,3,2,2,3};
    private final Random random=new Random();
    private JsonObject data;
    private Cuboid floor;
    private List<Material> palette;
    private int round,type,variant,phase,deadline,decision;
    private Material target;
    private Material[] board;
    private int write;
    @Override public void validate(PartyConfig c,PartyConfig.Arena a){floor=a.region("floor");if(floor.maxY()!=floor.minY()||floor.maxX()-floor.minX()!=39||floor.maxZ()-floor.minZ()!=39)throw new IllegalArgumentException("保留原图所有图案需40×40单层floor区域");palette=new ArrayList<>(c.materials("palette"));c.integer("decision-seconds",2,30);c.integer("decrease-every-rounds",1,100);c.integer("maximum-decreases",0,28);try(InputStream in=ColorGame.class.getResourceAsStream("/party/reaction/color/pattern-data.json")){if(in==null)throw new IllegalArgumentException("缺少内置图案数据");data=JsonParser.parseReader(new InputStreamReader(in,StandardCharsets.UTF_8)).getAsJsonObject();}catch(IOException e){throw new IllegalArgumentException(e);}}
    @Override public List<Cuboid>writableRegions(PartyConfig.Arena a){return List.of(a.region("floor"));}
    @Override public boolean prepare(PartySession s,int budget){if(board==null){board=new Material[1600];Arrays.fill(board,Material.GLASS);}flush(s,budget);return write==1600;}
    private void flush(PartySession s,int budget){for(int i=0;i<budget&&write<1600;i++,write++)s.set(s.arena.world().getBlockAt(floor.minX()+write%40,floor.minY(),floor.minZ()+write/40),board[write]);}
    @Override public void start(PartySession s){phase=0;deadline=s.elapsed()+60;decision=s.config.integer("decision-seconds",2,30);}
    private void next(PartySession s){round++;type=random.nextInt(17)+1;variant=random.nextInt(VARIANTS[type])+1;phase=1;deadline=s.elapsed()+120;generate(s);}
    private Material pick(){return palette.get(random.nextInt(palette.size()));}
    private void generate(PartySession s){
        board=new Material[1600];Arrays.fill(board,Material.AIR);write=0;
        if(type>=6&&type!=9){if(round>=15)variant=1+random.nextInt(VARIANTS[type]);else if(round>=10){int[] family=switch(type){case 11->variant<=3?new int[]{1,2,3}:new int[]{4,5,6};case 13->variant<=2?new int[]{1,2}:new int[]{3,4};case 15,17->new int[]{variant};default->java.util.stream.IntStream.rangeClosed(1,VARIANTS[type]).toArray();};variant=family[random.nextInt(family.length)];}}
        if(type==5){JsonArray row=data.getAsJsonArray("gradients").get(random.nextInt(17)).getAsJsonArray();Material replacement=pick();for(int i=0;i<1600;i++){Material m=Material.matchMaterial(row.get(i).getAsString());board[i]=m==Material.DIAMOND_BLOCK?replacement:m;}for(int z=2;z<38;z++)for(int x=2;x<38;x++)if(board[z*40+x]==Material.AIR)board[z*40+x]=replacement;}
        else if(type==9){Material base=pick();for(int z=2;z<38;z++)for(int x=2;x<38;x++)board[z*40+x]=base;int size=round>=10?2+random.nextInt(3):variant+1;for(int i=0;i<16;i++){int x=5+random.nextInt(28),z=5+random.nextInt(28);Material m=pick();for(int dx=0;dx<size;dx++)for(int dz=0;dz<size;dz++)if(x-dx>=2&&z+dz<38)board[(z+dz)*40+x-dx]=m;}}
        else {String key=type<=2?"1_"+variant:type==3||type==4?type+"_1":type+"_"+(char)('a'+variant-1);JsonArray labels=data.getAsJsonObject("patterns").getAsJsonArray(key);Map<Integer,Material> colours=new HashMap<>();for(int z=0;z<36;z++)for(int x=0;x<36;x++){int label=labels.get(z*36+x).getAsInt();board[(z+2)*40+x+2]=colours.computeIfAbsent(label,k->pick());}}
        List<Material> present=Arrays.stream(board).filter(Objects::nonNull).filter(m->!m.isAir()).toList();target=present.get(random.nextInt(present.size()));
        for(Player p:s.survivors()){p.getInventory().clear();p.getInventory().setItem(4,new org.bukkit.inventory.ItemStack(target));p.sendActionBar(s.config.message("target","round",round,"target",target.name()));}
    }
    @Override public void tick(PartySession s,int elapsed){
        if(write<1600){flush(s,s.config.integer("limits.blocks-per-tick",1,2000));return;}
        for(Player p:List.copyOf(s.survivors())){Location l=p.getLocation();if(l.getY()<floor.minY()-3||l.getBlock().getType()==Material.WATER||l.clone().subtract(0,1,0).getBlock().getType()==Material.WATER)s.eliminate(p);if(decision<=4)p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED,40,1,true,false));if(decision<=2)p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST,40,1,true,false));}
        if(phase==0){if(elapsed>=deadline)next(s);return;}
        if(phase==1){if(elapsed>=deadline){phase=2;int decreases=Math.min(s.config.integer("maximum-decreases",0,28),round/s.config.integer("decrease-every-rounds",1,100));decision=Math.max(2,s.config.integer("decision-seconds",2,30)-decreases);deadline=elapsed+decision*20;}else if(elapsed%20==0)generate(s);}
        else if(phase==2){if(elapsed%20==0)for(Player p:s.survivors())p.sendActionBar(s.config.message("warning","round",round,"seconds",Math.max(0,(deadline-elapsed+19)/20),"target",target.name()));if(elapsed>=deadline){for(int i=0;i<board.length;i++)if(board[i]!=target)board[i]=Material.AIR;write=0;phase=0;deadline=elapsed+100;}}
    }
    @Override public void eliminated(PartySession s,Player p){int left=s.survivors().size();if(left<=2)s.points(p,left==0?3:left==1?2:1);if(left==1)s.points(s.survivors().get(0),3);}
    @Override public void timeout(PartySession s){for(Player p:s.survivors())s.points(p,3);s.finish(s.survivors());}
}
