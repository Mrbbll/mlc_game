package com.mlc.mlcgames.party.combat.battle;

import com.mlc.mlcgames.party.combat.common.TeamCombat;
import com.mlc.mlcgames.party.core.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.potion.*;
import java.util.*;

/** Original Battle Box: limited shared role choices, wool control, best of three. */
public final class BattleGame extends TeamCombat {
    private final Map<UUID,Integer> choices=new HashMap<>();
    private final int[] wins=new int[2];
    private final int[][] available=new int[2][4];
    private Cuboid target;
    private int phase,phaseUntil;
    @Override public void validate(PartyConfig c,PartyConfig.Arena a){
        validateTeams(c,a);target=a.region("target");
        if(target.volume()!=9||target.minY()!=target.maxY())throw new IllegalArgumentException("Battle Box中心目标必须为9格单层");
        for(String region:List.of("red-gate","blue-gate"))a.region(region);
        a.group("potions");c.integer("selection-seconds",1,120);c.integer("round-seconds",10,600);c.integer("wins-required",1,10);
    }
    @Override public List<Cuboid> writableRegions(PartyConfig.Arena a){return List.of(a.region("target"),a.region("red-gate"),a.region("blue-gate"));}
    @Override public void start(PartySession s){
        assign(s);
        // Source benches one randomly selected player for odd participant counts.
        if(s.players().size()%2!=0){Player p=teamPlayers(s,0).getLast();teams.remove(p.getUniqueId());s.retire(p);p.sendMessage(s.config.message("benched"));}
        beginRound(s);
    }
    private void beginRound(PartySession s){
        choices.clear();s.resetPlayers();
        int quota=(s.players().size()+7)/8;
        for(int[] values:available)Arrays.fill(values,quota);
        for(Player p:s.survivors()){
            if(team(p)<0){s.retire(p);continue;}spawn(s,p);gear(s,p);
            // Select a limited team role with hotbar slots 5..8 and right click.
            Material[] icons={Material.WOODEN_AXE,Material.CROSSBOW,Material.CROSSBOW,Material.STONE_SWORD};
            for(int i=0;i<4;i++)p.getInventory().setItem(i+5,s.item(icons[i],"roles."+i));
        }
        fill(s,target,Material.WHITE_WOOL);fill(s,s.arena.region("red-gate"),Material.RED_STAINED_GLASS);fill(s,s.arena.region("blue-gate"),Material.LIGHT_BLUE_STAINED_GLASS);
        phase=0;phaseUntil=s.elapsed()+s.config.integer("selection-seconds",1,120)*20;
        s.broadcast("select");
    }
    private void gear(PartySession s,Player p){
        p.getInventory().setItem(0,item(s,Material.WOODEN_SWORD));p.getInventory().setItem(1,item(s,Material.SHEARS));
        ItemStack wool=item(s,team(p)==0?Material.RED_WOOL:Material.BLUE_WOOL);wool.setAmount(64);p.getInventory().setItem(2,wool);
        p.getInventory().setItem(3,item(s,Material.BOW));p.getInventory().setItem(4,new ItemStack(Material.ARROW,4));
        ItemStack chest=item(s,Material.LEATHER_CHESTPLATE);LeatherArmorMeta meta=(LeatherArmorMeta)chest.getItemMeta();meta.setColor(team(p)==0?Color.RED:Color.BLUE);chest.setItemMeta(meta);p.getInventory().setChestplate(chest);
    }
    private void choose(PartySession s,Player p,int role){
        if(phase!=0||choices.containsKey(p.getUniqueId())||available[team(p)][role]<=0)return;
        available[team(p)][role]--;choices.put(p.getUniqueId(),role);
        for(int i=5;i<9;i++)p.getInventory().setItem(i,null);
        ItemStack special;
        if(role==0){special=item(s,Material.WOODEN_AXE);special.addUnsafeEnchantment(Enchantment.KNOCKBACK,2);p.getInventory().setItem(5,special);}
        else if(role==1||role==2){special=item(s,Material.CROSSBOW);special.addUnsafeEnchantment(role==1?Enchantment.MULTISHOT:Enchantment.QUICK_CHARGE,1);p.getInventory().setItem(3,special);}
        else p.getInventory().setItem(0,item(s,Material.STONE_SWORD));
        p.sendMessage(s.config.message("selected","role",s.config.text("roles."+role)));
    }
    @Override public void interact(PartySession s,PlayerInteractEvent e){
        if(!e.getAction().isRightClick()||e.getHand()!=EquipmentSlot.HAND)return;
        int slot=e.getPlayer().getInventory().getHeldItemSlot();if(slot>=5&&slot<=8){e.setCancelled(true);choose(s,e.getPlayer(),slot-5);}
    }
    @Override public void moved(PartySession s,PlayerMoveEvent e){if(phase!=2&&e.hasChangedPosition())e.setTo(e.getFrom());}
    @Override public void tick(PartySession s,int elapsed){
        for(int t=0;t<2;t++)if(teamPlayers(s,t).isEmpty()){finishTeam(s,1-t,2);return;}
        if(phase==0){
            if(elapsed>=phaseUntil){for(Player p:s.survivors())if(!choices.containsKey(p.getUniqueId()))for(int r=0;r<4;r++)if(available[team(p)][r]>0){choose(s,p,r);break;}}
            if(s.survivors().stream().allMatch(p->choices.containsKey(p.getUniqueId()))){phase=1;phaseUntil=elapsed+100;s.broadcast("ready");}
        }else if(phase==1&&elapsed>=phaseUntil){
            phase=2;phaseUntil=elapsed+s.config.integer("round-seconds",10,600)*20;fill(s,s.arena.region("red-gate"),Material.AIR);fill(s,s.arena.region("blue-gate"),Material.AIR);spawnPotions(s);s.broadcast("fight");
        }else if(phase==2){
            int[] control=new int[2];for(int x=target.minX();x<=target.maxX();x++)for(int z=target.minZ();z<=target.maxZ();z++){
                Material m=s.arena.world().getBlockAt(x,target.minY(),z).getType();if(m==Material.RED_WOOL)control[0]++;if(m==Material.BLUE_WOOL)control[1]++;
            }
            if(control[0]==9||control[1]==9||elapsed>=phaseUntil){
                int winner=control[0]==control[1]?-1:control[0]>control[1]?0:1;if(winner>=0)wins[winner]++;
                s.broadcast("round-end","red",wins[0],"blue",wins[1]);
                if(winner>=0&&wins[winner]>=s.config.integer("wins-required",1,10)){finishTeam(s,winner,2);return;}
                phase=3;phaseUntil=elapsed+100;for(Player p:s.survivors())p.setGameMode(GameMode.SPECTATOR);
            }
        }else if(phase==3&&elapsed>=phaseUntil)s.resetRound(()->beginRound(s));
    }
    private void spawnPotions(PartySession s){
        PotionEffectType[] effects={PotionEffectType.SPEED,PotionEffectType.SLOWNESS,PotionEffectType.HASTE,PotionEffectType.MINING_FATIGUE,PotionEffectType.STRENGTH,PotionEffectType.INSTANT_HEALTH,PotionEffectType.INSTANT_DAMAGE,PotionEffectType.JUMP_BOOST};
        for(Location l:s.arena.group("potions")){
            int choice=java.util.concurrent.ThreadLocalRandom.current().nextInt(8);ItemStack potion=item(s,Material.SPLASH_POTION);PotionMeta meta=(PotionMeta)potion.getItemMeta();meta.addCustomEffect(new PotionEffect(effects[choice],choice==5||choice==6?1:200,choice==3?1:0),true);potion.setItemMeta(meta);
            org.bukkit.entity.Item drop=s.arena.world().dropItem(l,potion);s.track(drop);drop.setPickupDelay(0);
        }
    }
    @Override public boolean breakBlock(PartySession s,Player p,Block b){return phase==2&&target.contains(b.getX(),b.getY(),b.getZ())&&b.getType().name().endsWith("_WOOL");}
    @Override public boolean placeBlock(PartySession s,Player p,Block b){return phase==2&&target.contains(b.getX(),b.getY(),b.getZ())&&b.getType()==(team(p)==0?Material.RED_WOOL:Material.BLUE_WOOL);}
    @Override public void hit(PartySession s,Player a,Player v,EntityDamageByEntityEvent e){if(phase==2&&team(a)!=team(v))e.setCancelled(false);}
    @Override public void timeout(PartySession s){finishTeam(s,wins[0]==wins[1]?-1:wins[0]>wins[1]?0:1,2);}
    @Override public boolean allowPickup(PartySession s,Player p,org.bukkit.entity.Item item){return phase==2&&s.owns(item)&&item.getItemStack().getType()==Material.SPLASH_POTION;}
    @Override public boolean allowSwap(PartySession s,Player p){return phase==2;}
}
