package com.mlc.mlcgames.party.reaction.tsays;

import com.google.gson.*;
import com.mlc.mlcgames.party.core.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Seven equally weighted task groups, all 70 source rows, including the reversed T prefix. */
public final class TSaysGame implements PartyGame {
    private final Random random=new Random();
    private final Map<String,List<Task>> groups=new LinkedHashMap<>();
    private final Map<UUID,Integer> coins=new LinkedHashMap<>();
    private final Set<UUID> judged=new HashSet<>();
    private final Map<UUID,Integer> lastAttack=new HashMap<>();
    private final List<Entity> sceneEntities=new ArrayList<>();
    private JsonObject scenes;
    private Location origin;
    private Task task;
    private int round,phase,deadline,finished;
    private boolean reverse;
    private static final Map<Integer,Material> TARGETS=new HashMap<>();
    static {String[] craft={"DIAMOND_SWORD","GOLDEN_SWORD","IRON_SWORD","WOODEN_SWORD","STONE_SWORD","OAK_DOOR","IRON_DOOR","IRON_TRAPDOOR","OAK_TRAPDOOR","CHEST"};for(int i=0;i<10;i++)TARGETS.put(10+i,Material.valueOf(craft[i]));String[] extras={"SHIELD","FURNACE","BREAD","BUCKET","SHEARS","WOODEN_PICKAXE"};for(int i=0;i<6;i++)TARGETS.put(30+i,Material.valueOf(extras[i]));String[] wear={"GOLDEN","IRON","LEATHER","CHAINMAIL","COPPER","DIAMOND","NETHERITE"};for(int i=0;i<7;i++)TARGETS.put(-10+i,Material.valueOf(wear[i]+"_CHESTPLATE"));String[] helmets={"GOLDEN","IRON","DIAMOND","NETHERITE"};for(int i=0;i<4;i++)TARGETS.put(40+i,Material.valueOf(helmets[i]+"_HELMET"));TARGETS.put(50,Material.WOODEN_SWORD);TARGETS.put(51,Material.DIAMOND_SWORD);TARGETS.put(52,Material.SHIELD);String[] concrete={"YELLOW","RED","WHITE","LIGHT_BLUE","BLUE","LIME","GREEN","BLACK","PINK","BROWN"};for(int i=0;i<10;i++)TARGETS.put(-20+i,Material.valueOf(concrete[i]+"_CONCRETE"));Material[] blocks={Material.ICE,Material.BLUE_ICE,Material.IRON_BLOCK,Material.GOLD_BLOCK,Material.HAY_BLOCK,Material.EMERALD_BLOCK,Material.MAGMA_BLOCK,Material.ANVIL};for(int i=0;i<8;i++)TARGETS.put(70+i,blocks[i]);}
    @Override public boolean lastPlayerWins(){return false;}
    @Override public void validate(PartyConfig c,PartyConfig.Arena a){origin=a.group("scene-origin").get(0);a.group("parkour-start");a.group("husk-spawns");Cuboid region=a.region("scene");if(!region.contains(origin.getBlockX()-20,origin.getBlockY()-2,origin.getBlockZ()-20)||!region.contains(origin.getBlockX()+20,origin.getBlockY()+18,origin.getBlockZ()+20))throw new IllegalArgumentException("scene须覆盖scene-origin横向±20、纵向-2..18的原场景体积");c.integer("rounds",1,100);try(InputStream in=TSaysGame.class.getResourceAsStream("/party/reaction/tsays/task-data.json")){if(in==null)throw new IllegalArgumentException("缺少内置任务表");JsonObject data=JsonParser.parseReader(new InputStreamReader(in,StandardCharsets.UTF_8)).getAsJsonObject();scenes=data.getAsJsonObject("scenes");for(JsonElement row:data.getAsJsonArray("tasks")){JsonObject o=row.getAsJsonObject();Task t=new Task(o.get("id").getAsInt(),o.get("seconds").getAsInt(),o.get("message").getAsString(),o.get("negative").getAsBoolean(),o.get("group").getAsString());groups.computeIfAbsent(t.group,k->new ArrayList<>()).add(t);}}catch(IOException e){throw new IllegalArgumentException(e);}}
    @Override public List<Cuboid>writableRegions(PartyConfig.Arena a){return List.of(a.region("scene"));}
    @Override public void start(PartySession s){for(Player p:s.players())coins.put(p.getUniqueId(),1);next(s);}
    private void next(PartySession s){if(++round>s.config.integer("rounds",1,100)){timeout(s);return;}judged.clear();finished=0;List<String>keys=new ArrayList<>(groups.keySet());List<Task>list=groups.get(keys.get(random.nextInt(keys.size())));Task selected=list.get(random.nextInt(list.size()));task=new Task(selected.id,selected.seconds,s.config.text("tasks."+selected.id),selected.negative,selected.group);reverse=random.nextBoolean();phase=0;deadline=s.elapsed()+40;s.broadcast("round","round",round,"total",s.config.integer("rounds",1,100));}
    private void scene(PartySession s,String name){for(JsonElement element:scenes.getAsJsonArray(name)){JsonObject row=element.getAsJsonObject();JsonArray first=row.getAsJsonArray("first"),second=row.getAsJsonArray("second");int x=first.get(0).getAsInt(),y=first.get(1).getAsInt(),z=first.get(2).getAsInt(),xx=second.get(0).getAsInt(),yy=second.get(1).getAsInt(),zz=second.get(2).getAsInt();Material material=Material.valueOf(row.get("material").getAsString());boolean airOnly=row.get("air-only").getAsBoolean();for(int dx=Math.min(x,xx);dx<=Math.max(x,xx);dx++)for(int dy=Math.min(y,yy);dy<=Math.max(y,yy);dy++)for(int dz=Math.min(z,zz);dz<=Math.max(z,zz);dz++){Block b=origin.clone().add(dx,dy,dz).getBlock();if(!airOnly||b.getType().isAir())s.set(b,material);}}}
    private void setup(PartySession s){for(Player p:s.survivors()){p.getInventory().clear();p.clearActivePotionEffects();if(task.group.equals("craft")){give(p,Material.OAK_LOG,4);give(p,Material.DIAMOND_BLOCK,1);give(p,Material.IRON_BLOCK,1);give(p,Material.GOLD_BLOCK,1);give(p,Material.COBBLESTONE,9);give(p,Material.WHEAT,4);}if(task.group.equals("wear")){for(String type:List.of("LEATHER","GOLDEN","IRON","DIAMOND","CHAINMAIL","COPPER","NETHERITE")){give(p,Material.valueOf(type+"_CHESTPLATE"),1);give(p,Material.valueOf(type+"_HELMET"),1);}}if(task.group.equals("hand")){give(p,TARGETS.get(task.id),1);}
                if(task.group.equals("use")){give(p,Material.EGG,3);give(p,Material.SNOWBALL,3);give(p,Material.BOW,1);give(p,Material.ARROW,8);give(p,Material.FISHING_ROD,1);ItemStack bread=new ItemStack(Material.BREAD,3);var meta=bread.getItemMeta();var food=meta.getFood();food.setCanAlwaysEat(true);meta.setFood(food);bread.setItemMeta(meta);p.getInventory().addItem(bread);give(p,Material.POTION,1);if(task.id==66){give(p,Material.WHITE_WOOL,3);p.setGameMode(GameMode.SURVIVAL);}else p.setGameMode(GameMode.ADVENTURE);}if(task.id==9){p.teleport(s.arena.group("parkour-start").get(0));p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY,650,25,true,false));}}
        if(task.group.equals("craft"))s.set(origin.getBlock(),Material.CRAFTING_TABLE);
        if(task.id==9)scene(s,"parkour_to_the_end");
        if(task.id>=-20&&task.id<=-11)scene(s,"stand_on_block");
        if(task.id>=70&&task.id<=77){for(int x=-3;x<=3;x+=2)for(int z=-3;z<=3;z+=2)s.set(origin.clone().add(x,0,z).getBlock(),Math.abs(x)==3&&Math.abs(z)==3?TARGETS.get(task.id):Material.TERRACOTTA);}
        if(task.id==81){scene(s,"fight_zombie");for(Player ignored:s.survivors())husk(s);}
    }
    private void give(Player p,Material material,int amount){p.getInventory().addItem(new ItemStack(material,amount));}
    private void husk(PartySession s){List<Location>spots=s.arena.group("husk-spawns");Husk target=s.arena.world().spawn(spots.get(random.nextInt(spots.size())),Husk.class,e->{e.setAI(false);e.setSilent(true);e.setPersistent(false);e.setHealth(5);});s.track(target);sceneEntities.add(target);}
    private void judge(PartySession s,Player p,boolean condition,boolean equalPoints){if(phase!=2||!judged.add(p.getUniqueId()))return;boolean won=TSaysOutcome.success(condition,reverse);if(won){int reward=TSaysOutcome.reward(finished,equalPoints);coins.merge(p.getUniqueId(),reward,Integer::sum);finished++;p.sendMessage(s.config.message("success","coins",reward));}else p.sendMessage(s.config.message("failure"));if(task.id==66)p.setGameMode(GameMode.ADVENTURE);p.closeInventory();p.setItemOnCursor(null);p.getInventory().clear();}
    @Override public void tick(PartySession s,int elapsed){if(phase==0&&elapsed>=deadline){phase=1;deadline=elapsed+100;if(reverse)s.broadcast("prefix");return;}if(phase==1&&elapsed==deadline-20)s.broadcast("task","prefix",reverse?s.config.text("prefix"):"","task",task.message);if(phase==1&&elapsed>=deadline){phase=2;deadline=elapsed+task.seconds*20;setup(s);return;}if(phase!=2)return;
        for(Player p:s.survivors()){if(judged.contains(p.getUniqueId()))continue;int id=task.id;Material target=TARGETS.get(id);Material floor=p.getLocation().clone().subtract(0,1,0).getBlock().getType();
            if(task.group.equals("craft")&&p.getInventory().contains(target))judge(s,p,true,false);
            else if(task.group.equals("wear")){ItemStack item=id<0?p.getInventory().getChestplate():p.getInventory().getHelmet();if(item!=null&&!item.getType().isAir())judge(s,p,item.getType()==target,false);}
            else if(task.group.equals("hand")&&(id==52?p.getInventory().getItemInOffHand():p.getInventory().getItemInMainHand()).getType()==target)judge(s,p,true,false);
            else if(task.group.equals("block")){if(floor==target)judge(s,p,true,false);else if(id<0&&floor.name().endsWith("_CONCRETE"))judge(s,p,false,false);}
            else if(id==1&&floor==Material.BLACK_TERRACOTTA)judge(s,p,true,false);
            else if(id==8&&s.survivors().stream().anyMatch(other->other!=p&&other.getLocation().distanceSquared(p.getLocation())>=.01&&other.getLocation().distanceSquared(p.getLocation())<=1))judge(s,p,true,false);
            else if(id==82&&s.survivors().size()>=2&&s.survivors().stream().noneMatch(other->other!=p&&other.getLocation().distanceSquared(p.getLocation())<=16))judge(s,p,true,false);
            else if(id==9){if(p.getLocation().getBlock().getType()==Material.LAVA)judge(s,p,false,false);else if(floor==Material.DIAMOND_BLOCK)judge(s,p,true,false);}
            if((id==6||id==7)&&p.isSneaking())judge(s,p,id==6,false);
            if(id==20||id==80){rain(s,p,id==80);}
            if(floor==Material.BLACK_TERRACOTTA){p.teleport(s.arena.spawns().get(0));p.setFallDistance(0);}
        }
        if(task.id==81){sceneEntities.removeIf(e->!e.isValid());long need=s.survivors().stream().filter(p->!judged.contains(p.getUniqueId())).count();if(sceneEntities.stream().filter(e->e instanceof Husk).count()<need)husk(s);}
        if(elapsed>=deadline||judged.size()>=s.survivors().size()){for(Player p:s.survivors())if(!judged.contains(p.getUniqueId()))judge(s,p,task.negative,true);for(Entity entity:sceneEntities)entity.remove();sceneEntities.clear();phase=3;s.resetRound(()->{s.resetPlayers();next(s);});}
    }
    private void rain(PartySession s,Player p,boolean hit){for(int i=0;i<2;i++){Location l=p.getLocation().add(i==0&&hit?0:random.nextDouble()*10-5,hit?12:15,i==0&&hit?0:random.nextDouble()*10-5);if(s.arena.contains(l)){Arrow arrow=s.arena.world().spawn(l,Arrow.class,e->{e.setShooter(p);e.setDamage(0);e.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);});s.track(arrow);sceneEntities.add(arrow);}}}
    @Override public void moved(PartySession s,PlayerMoveEvent e){if(phase!=2||!e.hasChangedPosition())return;Player p=e.getPlayer();boolean jump=e.getTo().getY()>e.getFrom().getY()&&p.getVelocity().getY()>0;if(task.id==2&&jump)judge(s,p,true,false);if(task.id==3&&jump)judge(s,p,false,false);if(task.id==4)judge(s,p,true,false);if(task.id==5)judge(s,p,false,false);if(task.id==23&&p.isSprinting())judge(s,p,true,false);if(task.id==6&&p.isSneaking())judge(s,p,true,false);if(task.id==7&&p.isSneaking())judge(s,p,false,false);}
    @Override public void consume(PartySession s,PlayerItemConsumeEvent e){if(task!=null&&task.group.equals("use"))used(s,e.getPlayer(),e.getItem().getType()==Material.BREAD?64:65);}
    @Override public void fish(PartySession s,PlayerFishEvent e){if(e.getState()==PlayerFishEvent.State.FISHING)used(s,e.getPlayer(),63);e.setCancelled(false);}
    private void used(PartySession s,Player p,int id){if(task!=null&&task.group.equals("use"))judge(s,p,task.id==id,false);}
    @Override public boolean launch(PartySession s,Player p){Material held=p.getInventory().getItemInMainHand().getType();if(held==Material.EGG)used(s,p,60);else if(held==Material.SNOWBALL)used(s,p,61);else if(held==Material.BOW)used(s,p,62);return true;}
    @Override public boolean placeBlock(PartySession s,Player p,Block b){if(phase==2&&task.id==66&&!judged.contains(p.getUniqueId())&&b.getType()==Material.WHITE_WOOL&&s.writable(b)){used(s,p,66);return true;}return false;}
    @Override public void projectile(PartySession s,ProjectileHitEvent e){if(task!=null&&(task.id==20||task.id==80)&&e.getHitEntity() instanceof Player p&&s.active(p)&&sceneEntities.contains(e.getEntity()))judge(s,p,task.id==80,false);}
    @Override public void attackEntity(PartySession s,Player p,Entity target){if(phase==2&&task.id==81&&!judged.contains(p.getUniqueId())&&target instanceof Husk husk&&sceneEntities.contains(husk)&&p.getEyeLocation().distanceSquared(target.getLocation())<=20.25&&p.hasLineOfSight(target)&&s.elapsed()-lastAttack.getOrDefault(p.getUniqueId(),-20)>=10){lastAttack.put(p.getUniqueId(),s.elapsed());double damage=!p.isOnGround()&&p.getFallDistance()>0?1.5:1;if(husk.getHealth()<=damage){husk.remove();judge(s,p,true,false);}else husk.setHealth(husk.getHealth()-damage);}}
    @Override public void hazard(PartySession s,Player p){if(phase==2)judge(s,p,false,false);if(!p.isDead())p.teleport(s.arena.spawns().get(0));}
    @Override public boolean allowInventory(PartySession s,Player p){return phase==2&&!judged.contains(p.getUniqueId());}
    @Override public boolean allowBlockInteraction(PartySession s,Player p,Block block){return phase==2&&task.group.equals("craft")&&block.getType()==Material.CRAFTING_TABLE&&s.writable(block);}
    @Override public void timeout(PartySession s){List<Rankings.Row<UUID>>rank=Rankings.descending(coins);List<Player>w=new ArrayList<>();for(var row:rank){Player p=Bukkit.getPlayer(row.id());if(p==null||!s.member(p))continue;s.points(p,row.rank()<=3?4-row.rank():0);if(row.rank()==1)w.add(p);}s.finish(w);}
    private record Task(int id,int seconds,String message,boolean negative,String group){}
}
