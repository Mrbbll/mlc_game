package com.mlc.mlcgames.party.cooperation.foodparty;

import com.mlc.mlcgames.party.core.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.*;
import org.bukkit.persistence.PersistentDataType;
import java.util.*;

/** User-approved reconstruction: the source's start/score/recipe functions are empty. */
public final class FoodPartyGame implements PartyGame {
    private static final NamespacedKey DISH=new NamespacedKey("mlcgames_party","food_dish");
    private record Recipe(String id,Material result,int tier,Map<Material,Integer> ingredients){}
    private record Order(Recipe recipe,int expires){}
    private record Cooking(Recipe recipe,int doneAt){}
    private final List<Recipe> recipes=new ArrayList<>();
    private final Map<UUID,Order[]> orders=new HashMap<>();
    private final Map<UUID,Integer> scores=new LinkedHashMap<>();
    private final Map<UUID,Cooking> cooking=new HashMap<>();
    private final Map<String,Integer> harvested=new HashMap<>();
    private final Map<UUID,Inventory> menus=new HashMap<>();
    private final Map<UUID,Integer> customers=new HashMap<>();
    @Override public void validate(PartyConfig c,PartyConfig.Arena a){
        a.group("kitchens");if(a.group("customers").size()!=3)throw new IllegalArgumentException("美食派对需3个顾客点位");
        c.integer("order-seconds",10,300);c.integer("cook-seconds",1,30);c.integer("ingredient-cooldown-seconds",1,30);
        ConfigurationSection source=c.rules.getConfigurationSection("recipes");if(source==null||source.getKeys(false).size()!=9)throw new IllegalArgumentException("必须配置9个配方");
        for(String id:source.getKeys(false)){
            Material result=Material.matchMaterial(source.getString(id+".result",""));if(result==null||!result.isItem())throw new IllegalArgumentException("无效成品 "+id);
            int tier=source.getInt(id+".tier");if(tier<1||tier>3)throw new IllegalArgumentException("配方等级必须1..3");
            ConfigurationSection ingredients=source.getConfigurationSection(id+".ingredients");if(ingredients==null||ingredients.getKeys(false).isEmpty())throw new IllegalArgumentException("配方材料不能为空");
            Map<Material,Integer> requirements=new LinkedHashMap<>();for(String key:ingredients.getKeys(false)){
                Material material=Material.matchMaterial(key);int amount=ingredients.getInt(key);if(material==null||!material.isItem()||amount<1||amount>64)throw new IllegalArgumentException("无效配方材料 "+key);
                a.group("ingredient-"+material.name().toLowerCase(Locale.ROOT));requirements.put(material,amount);
                c.text("ingredient-names."+material.name());
            }
            c.text("recipes."+id);recipes.add(new Recipe(id,result,tier,Map.copyOf(requirements)));
        }
    }
    @Override public boolean lastPlayerWins(){return false;}
    @Override public void start(PartySession s){
        for(Player p:s.players()){scores.put(p.getUniqueId(),0);Order[] list=new Order[3];for(int i=0;i<3;i++)list[i]=newOrder(s);orders.put(p.getUniqueId(),list);showOrders(s,p);}
        int index=0;for(Location at:s.arena.group("customers")){
            Villager customer=at.getWorld().spawn(at,Villager.class,e->{e.setAI(false);e.setInvulnerable(true);e.setSilent(true);e.setCollidable(false);e.setPersistent(false);e.customName(s.config.message("customer-name","number",customers.size()+1));e.setCustomNameVisible(true);});s.track(customer);customers.put(customer.getUniqueId(),index++);
        }
    }
    private Order newOrder(PartySession s){return new Order(recipes.get(new Random().nextInt(recipes.size())),s.elapsed()+s.config.integer("order-seconds",10,300)*20);}
    @Override public void tick(PartySession s,int elapsed){
        for(Player p:s.survivors()){
            Order[] list=orders.get(p.getUniqueId());boolean refreshed=false;
            for(int i=0;i<3;i++)if(elapsed>=list[i].expires){list[i]=newOrder(s);refreshed=true;p.sendMessage(s.config.message("order-expired","number",i+1));}
            if(refreshed)showOrders(s,p);
            Cooking pending=cooking.get(p.getUniqueId());if(pending!=null&&elapsed>=pending.doneAt){
                ItemStack dish=s.item(pending.recipe.result,"recipes."+pending.recipe.id);var meta=dish.getItemMeta();meta.getPersistentDataContainer().set(DISH,PersistentDataType.STRING,pending.recipe.id);dish.setItemMeta(meta);
                // Do not discard a completed dish when a player filled their inventory meanwhile.
                if(p.getInventory().addItem(dish).isEmpty()){cooking.remove(p.getUniqueId());p.sendMessage(s.config.message("cooked","dish",s.config.text("recipes."+pending.recipe.id)));}
            }
            if(elapsed%20==0)p.sendActionBar(s.config.message("status","score",scores.get(p.getUniqueId()),"seconds",Math.max(0,s.config.integer("max-seconds",10,7200)-elapsed/20),"cooking",pending==null?s.config.text("idle"):Math.max(0,(pending.doneAt-elapsed+19)/20)));
        }
    }
    @Override public void interact(PartySession s,PlayerInteractEvent e){
        if(e.getHand()!=EquipmentSlot.HAND||!e.getAction().isRightClick()||e.getClickedBlock()==null)return;var clicked=e.getClickedBlock();Player p=e.getPlayer();
        if(s.arena.group("kitchens").stream().anyMatch(at->at.getBlock().equals(clicked))){e.setCancelled(true);open(s,p);return;}
        for(Material material:recipes.stream().flatMap(r->r.ingredients.keySet().stream()).distinct().toList())if(s.arena.group("ingredient-"+material.name().toLowerCase(Locale.ROOT)).stream().anyMatch(at->at.getBlock().equals(clicked))){
            e.setCancelled(true);String key=p.getUniqueId()+":"+material.name();if(harvested.getOrDefault(key,0)>s.elapsed()){p.sendActionBar(s.config.message("ingredient-cooldown"));return;}
            if(p.getInventory().addItem(new ItemStack(material)).isEmpty())harvested.put(key,s.elapsed()+s.config.integer("ingredient-cooldown-seconds",1,30)*20);else p.sendMessage(s.config.message("inventory-full"));return;
        }
    }
    private void open(PartySession s,Player p){Inventory menu=Bukkit.createInventory(null,9,s.config.message("menu-title"));for(int i=0;i<recipes.size();i++){Recipe recipe=recipes.get(i);ItemStack icon=s.item(recipe.result,"recipes."+recipe.id);var meta=icon.getItemMeta();List<net.kyori.adventure.text.Component> lore=new ArrayList<>();lore.add(s.config.message("recipe-value","points",recipe.tier*5));for(var need:recipe.ingredients.entrySet())lore.add(s.config.message("recipe-ingredient","material",s.config.text("ingredient-names."+need.getKey().name()),"count",need.getValue()));meta.lore(lore);icon.setItemMeta(meta);menu.setItem(i,icon);}menus.put(p.getUniqueId(),menu);p.openInventory(menu);}
    @Override public boolean allowInventory(PartySession s,InventoryClickEvent e){
        if(!(e.getWhoClicked() instanceof Player p))return false;Inventory menu=menus.get(p.getUniqueId());if(menu==null||e.getView().getTopInventory()!=menu)return e.getClickedInventory() instanceof PlayerInventory;
        e.setCancelled(true);if(e.getRawSlot()>=0&&e.getRawSlot()<recipes.size())cook(s,p,recipes.get(e.getRawSlot()));return false;
    }
    @Override public boolean allowDrag(PartySession s,org.bukkit.event.inventory.InventoryDragEvent e){return menus.get(e.getWhoClicked().getUniqueId())!=e.getView().getTopInventory()&&e.getRawSlots().stream().allMatch(slot->slot>=e.getView().getTopInventory().getSize());}
    private void cook(PartySession s,Player p,Recipe recipe){
        if(cooking.containsKey(p.getUniqueId())){p.sendMessage(s.config.message("already-cooking"));return;}
        for(var need:recipe.ingredients.entrySet())if(!p.getInventory().containsAtLeast(new ItemStack(need.getKey()),need.getValue())){p.sendMessage(s.config.message("missing-ingredients"));return;}
        for(var need:recipe.ingredients.entrySet())p.getInventory().removeItem(new ItemStack(need.getKey(),need.getValue()));
        cooking.put(p.getUniqueId(),new Cooking(recipe,s.elapsed()+s.config.integer("cook-seconds",1,30)*20));p.closeInventory();p.sendMessage(s.config.message("cooking-started","dish",s.config.text("recipes."+recipe.id)));
    }
    @Override public void interactEntity(PartySession s,Player p,Entity target){Integer number=customers.get(target.getUniqueId());if(number==null||p.getLocation().distanceSquared(target.getLocation())>20.25)return;Order[] list=orders.get(p.getUniqueId());Order order=list[number];ItemStack held=p.getInventory().getItemInMainHand();String dish=held.hasItemMeta()?held.getItemMeta().getPersistentDataContainer().get(DISH,PersistentDataType.STRING):null;
        if(!order.recipe.id.equals(dish)){p.sendMessage(s.config.message("wrong-dish","number",number+1,"dish",s.config.text("recipes."+order.recipe.id),"seconds",Math.max(0,(order.expires-s.elapsed()+19)/20)));return;}
        if(s.elapsed()>=order.expires){list[number]=newOrder(s);showOrders(s,p);return;}
        int remaining=order.expires-s.elapsed();int bonus=(int)Math.ceil(5.0*remaining/(s.config.integer("order-seconds",10,300)*20));int value=order.recipe.tier*5+Math.min(5,bonus);held.setAmount(held.getAmount()-1);scores.merge(p.getUniqueId(),value,Integer::sum);p.sendMessage(s.config.message("served","number",number+1,"points",value,"score",scores.get(p.getUniqueId())));list[number]=newOrder(s);showOrders(s,p);
    }
    private void showOrders(PartySession s,Player p){Order[] list=orders.get(p.getUniqueId());for(int i=0;i<list.length;i++)p.sendMessage(s.config.message("order","number",i+1,"dish",s.config.text("recipes."+list[i].recipe.id),"seconds",Math.max(0,(list[i].expires-s.elapsed()+19)/20)));}
    @Override public void attackEntity(PartySession s,Player p,Entity target){}
    @Override public boolean allowInventoryEdit(PartySession s,Player p){return true;}
    @Override public boolean allowSwap(PartySession s,Player p){return true;}
    @Override public void consume(PartySession s,org.bukkit.event.player.PlayerItemConsumeEvent event){event.setCancelled(true);}
    @Override public void hazard(PartySession s,Player p){p.setFallDistance(0);p.setFireTicks(0);p.teleport(s.arena.spawns().getFirst());}
    @Override public void timeout(PartySession s){List<Player> winners=new ArrayList<>();Map<UUID,Integer> present=new LinkedHashMap<>();for(Player p:s.players())present.put(p.getUniqueId(),scores.getOrDefault(p.getUniqueId(),0));for(var row:Rankings.descending(present)){Player p=Bukkit.getPlayer(row.id());if(p==null)continue;s.points(p,Math.max(0,4-row.rank()));s.broadcast("ranking","rank",row.rank(),"player",p.getName(),"score",row.score());if(row.rank()==1)winners.add(p);}s.finish(winners);}
}
