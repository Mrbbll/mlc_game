package com.mlc.mlcgames.party.combat.common;

import com.mlc.mlcgames.party.core.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Switch;
import org.bukkit.entity.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.*;
import org.bukkit.potion.*;
import java.util.*;

/** Shared engine for the source's three distinct Blaze match modes. */
public abstract class BlazeBase extends TeamCombat {
    private final int mode;
    private final Map<UUID,Integer> coins=new HashMap<>();
    private final int[] scores=new int[2];
    private int phase,until,policeTeam,halves,refillAt;
    private boolean hunger;
    private NativeLoot loot;
    protected BlazeBase(int mode){this.mode=mode;}
    @Override public void validate(PartyConfig c,PartyConfig.Arena a){
        validateTeams(c,a);a.region("build");a.region("red-shop");a.region("blue-shop");a.group("targets");
        if(mode==3)a.group("police-spawn");c.integer("round-seconds",10,3600);c.integer("wins-required",1,20);
        hunger=c.rules.getBoolean("hunger-equipment");if(hunger){loot=new NativeLoot(c);a.group("chests");}
        for(String role:Objects.requireNonNull(c.rules.getConfigurationSection("roles")).getKeys(false))for(Map<?,?> row:c.rules.getMapList("roles."+role+".items"))NativeEquipment.create(String.valueOf(row.get("input")),1);
        for(String key:List.of("door-a","door-b"))if(a.regions().containsKey(key)&&!a.region("build").contains(a.region(key)))throw new IllegalArgumentException("门必须在build内");
    }
    @Override public List<Cuboid> writableRegions(PartyConfig.Arena a){return List.of(a.region("build"));}
    @Override public void start(PartySession s){assign(s);begin(s,true);}
    private void begin(PartySession s,boolean first){
        s.resetPlayers();phase=0;until=s.elapsed()+(first?200:100);refillAt=s.elapsed()+6000;
        for(Player p:s.survivors()){spawnAt(s,p);coins.put(p.getUniqueId(),50);p.sendMessage(s.config.message("commands"));}
        for(int i=0;i<s.arena.group("targets").size();i++)s.set(s.arena.group("targets").get(i).getBlock(),mode==2||mode==3&&i!=s.config.rules.getInt("police-target",0)?Material.GRAY_WOOL:Material.GLASS);
        if(hunger)refill(s);
    }
    private void spawnAt(PartySession s,Player p){
        spawn(s,p);p.setGameMode(GameMode.SURVIVAL);
        if(mode==3&&team(p)==policeTeam)p.teleport(s.arena.group("police-spawn").getFirst());
    }
    private void glass(PartySession s,Player p){
        if(mode==2)return;
        p.getInventory().addItem(s.item(Material.WOODEN_PICKAXE,"items.pickaxe"));
        Material material=mode==3&&team(p)==policeTeam?Material.GLASS:team(p)==0?Material.BLUE_STAINED_GLASS:Material.RED_STAINED_GLASS;
        p.getInventory().addItem(new ItemStack(material,64));
    }
    private boolean base(PartySession s,Player p){
        if(phase==0)return true;Location l=p.getLocation();Cuboid shop=s.arena.region(team(p)==0?"red-shop":"blue-shop");
        return shop.contains(l.getBlockX(),l.getBlockY(),l.getBlockZ())||mode==3&&team(p)==policeTeam&&l.distanceSquared(s.arena.group("police-spawn").getFirst())<=64;
    }
    public boolean action(PartySession s,Player p,String[] args){
        if(args.length<2)return false;
        if(args[0].equalsIgnoreCase("role")){
            if(hunger||!base(s,p)){p.sendMessage(s.config.message("base-only"));return true;}
            String path="roles."+args[1];if(!s.config.rules.isConfigurationSection(path))return false;
            int cost=s.config.rules.getInt(path+".cost",50);if(!pay(s,p,cost))return true;
            for(int slot=0;slot<41;slot++){ItemStack i=p.getInventory().getItem(slot);if(NativeEquipment.flag(i,"blaze").equals("job"))p.getInventory().setItem(slot,null);}
            NativeEquipment.give(s,p,path+".items");p.sendMessage(s.config.message("bought","coins",coins.get(p.getUniqueId())));return true;
        }
        if(args[0].equalsIgnoreCase("shop")){
            String path="shop."+args[1];if(!s.config.rules.isConfigurationSection(path))return false;
            if(args[1].equals("1")&&mode==2)return true;
            if(!pay(s,p,s.config.rules.getInt(path+".cost")))return true;
            if(args[1].equals("1"))glass(s,p);else NativeEquipment.give(s,p,path+".items");
            p.sendMessage(s.config.message("bought","coins",coins.get(p.getUniqueId())));return true;
        }
        return false;
    }
    private boolean pay(PartySession s,Player p,int amount){int balance=coins.getOrDefault(p.getUniqueId(),0);if(balance<amount){p.sendMessage(s.config.message("poor"));return false;}coins.put(p.getUniqueId(),balance-amount);return true;}
    @Override public void tick(PartySession s,int elapsed){
        for(int t=0;t<2;t++)if(teamPlayers(s,t).isEmpty()){finishTeam(s,1-t,3);return;}
        if(phase==0){if(elapsed>=until){phase=1;until=elapsed+s.config.integer("round-seconds",10,3600)*20;for(Player p:s.survivors())glass(s,p);s.broadcast("fight");}return;}
        if(elapsed%20==0){
            for(Location point:s.arena.group("targets")){
                Material m=point.getBlock().getType();int t=m==Material.BLUE_STAINED_GLASS?0:m==Material.RED_STAINED_GLASS?1:-1;
                if(t>=0){scores[t]++;for(Player p:s.survivors())if(team(p)==t)coins.merge(p.getUniqueId(),1,Integer::sum);}
            }
            for(Player p:s.survivors()){
                coins.merge(p.getUniqueId(),1,Integer::sum);p.sendActionBar(s.config.message("status","red",scores[0],"blue",scores[1],"coins",coins.get(p.getUniqueId()),"seconds",Math.max(0,(until-elapsed)/20)));
                Block ahead=p.getLocation().add(p.getLocation().getDirection().setY(0).normalize()).getBlock();
                if(Tag.LEAVES.isTagged(ahead.getRelative(0,1,0).getType()))p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST,20,1,true,false));
            }
            if(hunger&&mode==1&&elapsed>=refillAt){refill(s);refillAt=elapsed+6000;}
        }
        doors(s);
        if(mode>=2){
            int a=(int)s.survivors().stream().filter(p->team(p)==0).count(),b=(int)s.survivors().stream().filter(p->team(p)==1).count();
            if(a==0||b==0){endRound(s,a==b?-1:a>b?0:1);return;}
        }
        if(elapsed>=until){
            if(mode==1){finishTeam(s,scores[0]==scores[1]?-1:scores[0]>scores[1]?0:1,3);return;}
            if(mode==2){long a=s.survivors().stream().filter(p->team(p)==0).count(),b=s.survivors().stream().filter(p->team(p)==1).count();endRound(s,a==b?-1:a>b?0:1);}
            else endRound(s,-1);
        }
    }
    private void endRound(PartySession s,int winner){
        if(mode==2){if(winner>=0)scores[winner]++;if(winner>=0&&scores[winner]>=s.config.integer("wins-required",1,20)){finishTeam(s,winner,3);return;}}
        else {if(winner>=0&&winner!=policeTeam)scores[winner]+=Math.max(0,(until-s.elapsed())/20);policeTeam=1-policeTeam;halves++;if(halves>=4){finishTeam(s,scores[0]==scores[1]?-1:scores[0]>scores[1]?0:1,3);return;}}
        s.resetRound(()->begin(s,false));
    }
    private void refill(PartySession s){
        for(Location l:s.arena.group("chests")){
            if(!(l.getBlock().getState() instanceof org.bukkit.block.Container c))throw new IllegalArgumentException("chests点必须为已有木桶");
            c.getInventory().clear();for(ItemStack item:loot.roll("blaze/barrel"))c.getInventory().addItem(item);
        }
    }
    private void doors(PartySession s){
        if(!s.arena.groups().containsKey("levers"))return;
        List<Location> levers=s.arena.group("levers");
        for(int i=0;i<Math.min(2,levers.size());i++){
            String key=i==0?"door-a":"door-b";if(!s.arena.regions().containsKey(key)||!(levers.get(i).getBlock().getBlockData() instanceof Switch lever))continue;
            Cuboid door=s.arena.region(key);Material mat=lever.isPowered()?Material.WAXED_OXIDIZED_COPPER_BARS:Material.AIR;
            for(int x=door.minX();x<=door.maxX();x++)for(int y=door.minY();y<=door.maxY();y++)for(int z=door.minZ();z<=door.maxZ();z++){Block b=s.arena.world().getBlockAt(x,y,z);if(b.getType()!=mat)s.set(b,mat);}
        }
    }
    @Override public void hit(PartySession s,Player a,Player v,EntityDamageByEntityEvent e){
        if(phase!=1||team(a)==team(v))return;e.setCancelled(false);
        if(e.getFinalDamage()>=v.getHealth()){coins.merge(a.getUniqueId(),100,Integer::sum);if(mode==1){e.setCancelled(true);hazard(s,v);}}
    }
    @Override public void environmentalDamage(PartySession s,Player p,EntityDamageEvent e){e.setCancelled(false);if(e.getFinalDamage()>=p.getHealth()&&mode==1){e.setCancelled(true);hazard(s,p);}}
    @Override public void hazard(PartySession s,Player p){
        coins.compute(p.getUniqueId(),(id,value)->Math.max(0,(value==null?0:value)-100));
        if(mode==1){spawnAt(s,p);glass(s,p);}else s.eliminate(p);
    }
    @Override public boolean breakBlock(PartySession s,Player p,Block b){
        return phase==1&&(b.getType()==Material.GLASS||b.getType()==Material.BLUE_STAINED_GLASS||b.getType()==Material.RED_STAINED_GLASS||b.getType()==Material.WAXED_OXIDIZED_COPPER_BARS&&p.getInventory().getItemInMainHand().getType()==Material.DIAMOND_PICKAXE);
    }
    @Override public boolean placeBlock(PartySession s,Player p,Block b){
        return phase==1&&s.arena.group("targets").stream().anyMatch(l->l.getBlock().equals(b))&&b.getType()==(mode==3&&team(p)==policeTeam?Material.GLASS:team(p)==0?Material.BLUE_STAINED_GLASS:Material.RED_STAINED_GLASS);
    }
    @Override public void consume(PartySession s,PlayerItemConsumeEvent e){
        Player p=e.getPlayer();String skill=NativeEquipment.flag(e.getItem(),"blaze_skill"),once=NativeEquipment.flag(e.getItem(),"blaze");
        if(!skill.isEmpty())e.setReplacement(e.getItem().clone());
        if(once.equals("back_base")){Location spawn=s.arena.teamSpawns().get(teamKeys[team(p)]).getFirst();p.teleport(spawn);}
        if(once.equals("fly")||skill.equals("fly_bow")){
            Projectile projectile=skill.equals("fly_bow")?p.launchProjectile(Arrow.class):p.launchProjectile(Snowball.class);projectile.setVelocity(p.getEyeLocation().getDirection().multiply(skill.isEmpty()?2:2.5));s.track(projectile);p.leaveVehicle();projectile.addPassenger(p);
        }
        if(skill.equals("blind_skill")||once.equals("blind"))for(Player enemy:s.survivors())if(team(enemy)!=team(p)&&enemy.getLocation().distanceSquared(p.getLocation())<=100){enemy.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS,160,1,true,false));enemy.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS,160,1,true,false));}
        if(skill.equals("freeze_skill")||once.equals("freeze"))for(Player enemy:s.survivors())if(team(enemy)!=team(p)&&enemy.getLocation().distanceSquared(p.getLocation())<=100){enemy.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS,200,5,true,false));enemy.damage(5,p);}
        if(skill.equals("singing_skill"))for(Player ally:s.survivors())if(team(ally)==team(p)&&ally.getLocation().distanceSquared(p.getLocation())<=36){ally.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH,100,0,true,false));ally.addPotionEffect(new PotionEffect(PotionEffectType.SPEED,160,1,true,false));ally.addPotionEffect(new PotionEffect(PotionEffectType.HASTE,100,1,true,false));}
    }
    @Override public void interact(PartySession s,PlayerInteractEvent e){
        if(e.getClickedBlock()==null)return;
        if(e.getClickedBlock().getBlockData() instanceof Switch)e.setUseInteractedBlock(org.bukkit.event.Event.Result.ALLOW);
        if(hunger&&e.getClickedBlock().getState() instanceof org.bukkit.block.Container)e.setUseInteractedBlock(org.bukkit.event.Event.Result.ALLOW);
    }
    public boolean allowPickup(PartySession s,Player p,Item item){return s.owns(item)&&!NativeEquipment.flag(item.getItemStack(),"blaze").equals("job");}
    public boolean allowDrop(PartySession s,Player p,Item item){if(!NativeEquipment.flag(item.getItemStack(),"blaze").isEmpty())return false;s.track(item);return true;}
    public boolean allowSwap(PartySession s,Player p){return true;}
    public boolean allowInventory(PartySession s,org.bukkit.event.inventory.InventoryClickEvent e){return e.getView().getTopInventory().getType()==org.bukkit.event.inventory.InventoryType.CRAFTING||hunger&&e.getView().getTopInventory().getLocation()!=null&&s.arena.contains(e.getView().getTopInventory().getLocation());}
    @Override public boolean allowInventory(PartySession s,Player p){return true;}
    @Override public boolean allowBlockInteraction(PartySession s,Player p,Block b){return b.getBlockData() instanceof Switch||hunger&&s.writable(b)&&b.getState() instanceof org.bukkit.block.Container;}
    @Override public void timeout(PartySession s){finishTeam(s,scores[0]==scores[1]?-1:scores[0]>scores[1]?0:1,3);}
}
