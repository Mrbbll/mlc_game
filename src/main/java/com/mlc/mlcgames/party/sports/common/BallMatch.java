package com.mlc.mlcgames.party.sports.common;

import com.mlc.mlcgames.party.core.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.*;
import org.bukkit.util.Vector;
import java.util.*;

/** Independent match state; entities and players are owned by the enclosing session. */
public abstract class BallMatch implements PartyGame {
    private final boolean dodge;
    private final Random random = new Random();
    private final Map<UUID,Integer> teams = new HashMap<>(), cooldowns = new HashMap<>();
    private final List<Ball> balls = new ArrayList<>();
    private int a,b,round,deadline,serveTeam=-1,lastScorer=-1;
    private UUID server;
    private enum Phase { BREAK,SERVE,PLAY }
    private Phase phase;
    protected BallMatch(boolean dodge) { this.dodge=dodge; }
    @Override public boolean lastPlayerWins() { return false; }
    @Override public void validate(PartyConfig c, PartyConfig.Arena arena) {
        arena.region("court"); arena.group("ball-centre");
        if(!dodge) { arena.region("goal-a"); arena.region("goal-b"); }
        for(String team:List.of("a","b")) if(arena.teamSpawns().getOrDefault(team,List.of()).size()<(c.integer("players.max",2,64)+1)/2) throw new IllegalArgumentException("队伍出生点不足: "+team);
        c.integer("target-score",1,21); c.integer("round-seconds",10,1800); c.integer("intermission-seconds",1,30); c.integer("serve-seconds",1,60);
    }
    @Override public void start(PartySession s) {
        List<Player> shuffled=new ArrayList<>(s.players()); Collections.shuffle(shuffled);
        for(int i=0;i<shuffled.size();i++) teams.put(shuffled.get(i).getUniqueId(),i%2);
        next(s);
    }
    private void kit(PartySession s,Player p,boolean serving) {
        p.getInventory().clear(); p.getInventory().setBoots(s.item(Material.LEATHER_BOOTS,"items.boots"));
        for(int i=0;i<(dodge?4:serving?8:0);i++) { ItemStack item=s.item(dodge?Material.STICK:Material.LEATHER_BOOTS,"items.force"); var meta=item.getItemMeta(); meta.displayName(s.config.message("force","value",dodge?(i+1)*2:i+1)); item.setItemMeta(meta); p.getInventory().setItem(i,item); }
        p.getInventory().setItem(8,s.item(Material.CARROT_ON_A_STICK,"items.rush"));
        p.getInventory().setItem(7,s.item(Material.CARROT_ON_A_STICK,"items.jump"));
        var meta=s.item(Material.LEATHER_CHESTPLATE,"items.jersey"); var leather=(org.bukkit.inventory.meta.LeatherArmorMeta)meta.getItemMeta(); leather.setColor(teams.get(p.getUniqueId())==0?Color.BLUE:Color.RED); meta.setItemMeta(leather); p.getInventory().setChestplate(meta);
    }
    private void next(PartySession s) {
        clear();
        if(a>=s.config.integer("target-score",1,21)||b>=s.config.integer("target-score",1,21)||round>=2*s.config.integer("target-score",1,21)) { timeout(s);return; }
        round++; s.resetPlayers(); int[] indexes={0,0};
        for(Player p:s.survivors()) { int team=teams.get(p.getUniqueId()); p.teleport(s.arena.teamSpawns().get(team==0?"a":"b").get(indexes[team]++)); kit(s,p,false); }
        phase=Phase.BREAK; deadline=s.elapsed()+20*s.config.integer("intermission-seconds",1,30);
    }
    private void spawn(PartySession s) {
        for(int i=0;i<(dodge?5:1);i++) {
            Location l=s.arena.group("ball-centre").get(0).clone();
            ArmorStand stand=s.arena.world().spawn(l,ArmorStand.class,e->{e.setSmall(true);e.setInvisible(true);e.setInvulnerable(true);e.setBasePlate(false);e.setPersistent(false);e.getEquipment().setHelmet(new ItemStack(Material.SEA_LANTERN));}); s.track(stand);
            Interaction interaction=s.arena.world().spawn(l,Interaction.class,e->{e.setInteractionWidth(1);e.setInteractionHeight(1);e.setResponsive(true);e.setPersistent(false);});s.track(interaction);
            balls.add(new Ball(stand,interaction));
        }
        if(dodge) { phase=Phase.PLAY; deadline=s.elapsed()+s.config.integer("round-seconds",10,1800)*20; balls.forEach(this::randomMotion); }
        else { phase=Phase.SERVE; serveTeam=lastScorer<0?random.nextInt(2):1-lastScorer; server=s.survivors().stream().filter(p->teams.get(p.getUniqueId())==serveTeam).map(Player::getUniqueId).findFirst().orElse(null); deadline=s.elapsed()+20*s.config.integer("serve-seconds",1,60); Player p=server==null?null:Bukkit.getPlayer(server);if(p!=null){kit(s,p,true);s.broadcast("serve","player",p.getName());} }
    }
    private void randomMotion(Ball ball) { ball.stand.setVelocity(new Vector((random.nextInt(301)-150)*.01,.5+random.nextInt(2)*.01,(random.nextInt(301)-150)*.01)); }
    @Override public void tick(PartySession s,int elapsed) {
        long membersA=s.players().stream().filter(p->teams.getOrDefault(p.getUniqueId(),-1)==0).count(),membersB=s.players().size()-membersA;
        if(membersA==0||membersB==0){int winner=membersA==membersB?-1:membersA>membersB?0:1;clear();List<Player>w=s.players().stream().filter(p->teams.getOrDefault(p.getUniqueId(),-2)==winner).toList();for(Player p:w)s.points(p,2);s.finish(w);return;}
        if(elapsed%20==0) {for(Player p:s.survivors()){int cd=Math.max(0,cooldowns.getOrDefault(p.getUniqueId(),0)-1);cooldowns.put(p.getUniqueId(),cd);p.setLevel(cd);if(!dodge)p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED,40,1,true,false));p.sendActionBar(s.config.message("score","a",a,"b",b,"round",round,"seconds",Math.max(0,(deadline-elapsed+19)/20)));}}
        if(phase==Phase.BREAK) { if(elapsed>=deadline)spawn(s);return; }
        if(phase==Phase.SERVE&&elapsed>=deadline) play(s);
        Cuboid court=s.arena.region("court");
        for(Ball ball:List.copyOf(balls)) {
            if(!ball.stand.isValid())throw new IllegalStateException("球实体意外消失");
            Location l=ball.stand.getLocation(); ball.interaction.teleport(l);
            if(dodge) {
                // Source wraps the ball across the opposite edge, rather than reflecting it.
                boolean wrap=false;if(l.getX()<court.minX()+.1){l.setX(court.maxX()+.8);wrap=true;}else if(l.getX()>court.maxX()+.9){l.setX(court.minX()+.2);wrap=true;}
                if(l.getZ()<court.minZ()+.1){l.setZ(court.maxZ()+.8);wrap=true;}else if(l.getZ()>court.maxZ()+.9){l.setZ(court.minZ()+.2);wrap=true;}
                if(wrap){Vector v=ball.stand.getVelocity();ball.stand.teleport(l);ball.stand.setVelocity(v);}
                Vector v=ball.stand.getVelocity();if(Math.abs(v.getX())<=.1&&Math.abs(v.getY())<=.1&&Math.abs(v.getZ())<=.1){randomMotion(ball);continue;}
                for(Player p:List.copyOf(s.survivors())) {Location pl=p.getLocation();if(Math.abs(pl.getX()-l.getX())<=.4&&Math.abs(pl.getZ()-l.getZ())<=.4&&pl.getY()-l.getY()>=-1.8&&pl.getY()<=l.getY())s.eliminate(p);}
            } else {
                if(in(s.arena.region("goal-a"),l)){winRound(s,1);return;}if(in(s.arena.region("goal-b"),l)){winRound(s,0);return;}
                if(l.clone().subtract(0,.1,0).getBlock().getType()==Material.BLACK_WOOL||!in(court,l)) {
                    l.setX(Math.max(court.minX()+.5,Math.min(court.maxX()+.5,l.getX())));l.setZ(Math.max(court.minZ()+.5,Math.min(court.maxZ()+.5,l.getZ())));l.setY(s.arena.group("ball-centre").get(0).getY());ball.stand.teleport(l);ball.stand.setVelocity(new Vector());
                    phase=Phase.SERVE;serveTeam=ball.lastTouch<0?random.nextInt(2):1-ball.lastTouch;server=s.survivors().stream().filter(p->teams.get(p.getUniqueId())==serveTeam).map(Player::getUniqueId).findFirst().orElse(null);deadline=elapsed+20*s.config.integer("serve-seconds",1,60);for(Player p:s.survivors())kit(s,p,p.getUniqueId().equals(server));
                }
                if(l.getBlock().getType()==Material.COBWEB)ball.stand.setVelocity(ball.stand.getVelocity().setY(-2));
            }
        }
        if(dodge) {
            for(Player p:List.copyOf(s.survivors()))if(p.getLocation().clone().subtract(0,1,0).getBlock().getType()==Material.RED_WOOL)s.eliminate(p);
            long na=count(s,0),nb=count(s,1);if(na==0||nb==0||elapsed>=deadline){winRound(s,na==nb?-1:na>nb?0:1);return;}
        } else if(phase==Phase.PLAY&&elapsed>=deadline)winRound(s,-1);
    }
    private boolean in(Cuboid c,Location l){return c.contains(l.getBlockX(),l.getBlockY(),l.getBlockZ());}
    private long count(PartySession s,int t){return s.survivors().stream().filter(p->teams.get(p.getUniqueId())==t).count();}
    private void play(PartySession s){phase=Phase.PLAY;deadline=s.elapsed()+20*s.config.integer("round-seconds",10,1800);for(Player p:s.survivors())kit(s,p,false);}
    private void winRound(PartySession s,int team){if(team==0)a++;if(team==1)b++;lastScorer=team;s.broadcast("round-result","a",a,"b",b);next(s);}
    @Override public void attackEntity(PartySession s,Player p,Entity target){contact(s,p,target,true);}
    @Override public void interactEntity(PartySession s,Player p,Entity target){contact(s,p,target,false);}
    private void contact(PartySession s,Player p,Entity target,boolean kick){
        if(phase==Phase.BREAK||phase==Phase.SERVE&&!p.getUniqueId().equals(server))return;
        Ball ball=balls.stream().filter(x->x.interaction.equals(target)||x.stand.equals(target)).findFirst().orElse(null);
        if(ball==null||p.getEyeLocation().distanceSquared(target.getLocation())>100||!p.hasLineOfSight(target))return;
        if(!kick){if(phase!=Phase.SERVE)ball.stand.setVelocity(new Vector());else p.sendMessage(s.config.message("left-serve"));}
        else {int slot=p.getInventory().getHeldItemSlot();int force=dodge&&slot<4?(slot+1)*2:phase==Phase.SERVE&&slot<8?slot+1:3;Vector direction=p.getLocation().getDirection().setY(0);if(direction.lengthSquared()<.001)return;ball.stand.setVelocity(direction.normalize().multiply(force*.5).setY((force+1)*.1));if(phase==Phase.SERVE)play(s);}
        ball.lastTouch=teams.get(p.getUniqueId());
    }
    @Override public void interact(PartySession s,PlayerInteractEvent e){if(!e.getAction().isRightClick()||e.getItem()==null||e.getItem().getType()!=Material.CARROT_ON_A_STICK)return;e.setCancelled(true);Player p=e.getPlayer();if(cooldowns.getOrDefault(p.getUniqueId(),0)>0)return;int slot=p.getInventory().getHeldItemSlot();if(slot!=7&&slot!=8)return;p.addPotionEffect(new PotionEffect(slot==7?PotionEffectType.JUMP_BOOST:PotionEffectType.SPEED,40,slot==7?2:4,true,false));cooldowns.put(p.getUniqueId(),4+random.nextInt(3));}
    @Override public void hazard(PartySession s,Player p){if(dodge)s.eliminate(p);else p.teleport(s.arena.teamSpawns().get(teams.get(p.getUniqueId())==0?"a":"b").get(0));}
    @Override public void timeout(PartySession s){clear();int team=a==b?-1:a>b?0:1;List<Player>winners=s.players().stream().filter(p->teams.getOrDefault(p.getUniqueId(),-2)==team).toList();for(Player p:winners)s.points(p,2);s.finish(winners);}
    private void clear(){for(Ball b:balls){b.stand.remove();b.interaction.remove();}balls.clear();}
    private static class Ball {final ArmorStand stand;final Interaction interaction;int lastTouch=-1;Ball(ArmorStand s,Interaction i){stand=s;interaction=i;}}
}
