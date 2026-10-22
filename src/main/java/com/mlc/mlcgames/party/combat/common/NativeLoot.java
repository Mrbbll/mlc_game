package com.mlc.mlcgames.party.combat.common;

import com.google.gson.*;
import com.mlc.mlcgames.party.core.PartyConfig;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import java.util.*;

/** Java implementation of the source map's declarative pools, with no command execution. */
public final class NativeLoot {
    private final JsonObject tables;
    private final JsonObject tags;
    private final Random random=new Random();
    public NativeLoot(PartyConfig c){
        JsonObject data=JsonParser.parseString(c.rules.getString("loot-data","{}")).getAsJsonObject();
        tables=data.getAsJsonObject("tables");tags=data.has("tags")?data.getAsJsonObject("tags"):new JsonObject();
        if(tables==null||tables.size()==0)throw new IllegalArgumentException("loot-data.tables 缺失");
    }
    public List<ItemStack> roll(String name){List<ItemStack> out=new ArrayList<>();table(name,out,0);return out;}
    private String id(String name){return name.contains(":")?name.substring(name.indexOf(':')+1):name;}
    private void table(String name,List<ItemStack> out,int depth){
        if(depth>24)throw new IllegalArgumentException("战利品循环引用");
        JsonObject table=tables.getAsJsonObject(id(name));
        if(table==null){
            org.bukkit.loot.LootTable builtin=Bukkit.getLootTable(NamespacedKey.minecraft(id(name)));
            if(builtin==null)throw new IllegalArgumentException("缺少战利品表 "+name);
            out.addAll(builtin.populateLoot(random,new org.bukkit.loot.LootContext.Builder(Bukkit.getWorlds().getFirst().getSpawnLocation()).build()));return;
        }
        for(JsonElement pe:table.getAsJsonArray("pools")){
            JsonObject p=pe.getAsJsonObject();int rolls=number(p.get("rolls"));
            List<JsonObject> entries=new ArrayList<>();
            for(JsonElement ee:p.getAsJsonArray("entries")){
                JsonObject e=ee.getAsJsonObject();if(!condition(e.get("condition")))continue;
                if(id(string(e,"type","")).equals("tag")){
                    String tag=string(e,"items",string(e,"name","")).replace("#","");JsonArray list=tags.getAsJsonArray(id(tag));
                    if(list==null)throw new IllegalArgumentException("缺少物品标签 "+tag);
                    for(JsonElement item:list){JsonObject copy=e.deepCopy();copy.addProperty("type","item");copy.addProperty("name",item.getAsString());entries.add(copy);}
                }else entries.add(e);
            }
            long weight=entries.stream().mapToLong(e->e.has("weight")?e.get("weight").getAsInt():1).sum();
            if(weight<=0)continue;
            for(int r=0;r<rolls;r++){
                long choice=random.nextLong(weight);JsonObject selected=null;
                for(JsonObject e:entries){choice-=e.has("weight")?e.get("weight").getAsInt():1;if(choice<0){selected=e;break;}}
                if(selected==null)continue;
                String type=id(string(selected,"type",""));
                if(type.equals("loot_table")){table(string(selected,"value",string(selected,"name","")),out,depth+1);continue;}
                if(type.equals("empty"))continue;
                if(!type.equals("item"))throw new IllegalArgumentException("不支持的战利品条目 "+type);
                Spec spec=new Spec(string(selected,"name",""));
                modify(spec,selected.get("modifier"));
                if(selected.has("functions"))for(JsonElement f:selected.getAsJsonArray("functions"))modify(spec,f);
                ItemStack stack=spec.build();if(stack!=null)out.add(stack);
            }
        }
    }
    private boolean condition(JsonElement element){
        if(element==null||element.isJsonNull())return true;
        JsonObject c=element.getAsJsonObject();String type=id(string(c,"type",string(c,"condition","")));
        if(type.equals("random_chance"))return random.nextDouble()<c.get("chance").getAsDouble();
        throw new IllegalArgumentException("不支持的战利品条件 "+type);
    }
    private int number(JsonElement e){
        if(e==null)return 1;if(e.isJsonPrimitive())return e.getAsInt();JsonObject n=e.getAsJsonObject();String type=id(string(n,"type","uniform"));
        if(type.equals("uniform")){int lo=number(n.get("min")),hi=number(n.get("max"));return random.nextInt(lo,hi+1);}
        if(type.equals("binomial")){int hits=0;for(int i=0;i<n.get("n").getAsInt();i++)if(random.nextDouble()<n.get("p").getAsDouble())hits++;return hits;}
        throw new IllegalArgumentException("不支持的数量分布 "+type);
    }
    private void modify(Spec s,JsonElement value){
        if(value==null||value.isJsonNull())return;JsonObject f=value.getAsJsonObject();if(!condition(f.get("condition")))return;
        String type=id(string(f,"type",string(f,"function","")));
        switch(type){
            case "sequence" -> {for(JsonElement child:f.getAsJsonArray("functions"))modify(s,child);}
            case "set_count" -> s.count=number(f.get("count"));
            case "set_components" -> {for(var e:f.getAsJsonObject("components").entrySet())s.components.add(e.getKey().contains(":")?e.getKey():"minecraft:"+e.getKey(),e.getValue().deepCopy());}
            case "set_potion" -> {JsonObject p=new JsonObject();p.addProperty("potion",string(f,"id","").contains(":")?string(f,"id",""):"minecraft:"+string(f,"id",""));s.components.add("minecraft:potion_contents",p);}
            case "set_enchantments" -> {for(var e:f.getAsJsonObject("enchantments").entrySet())s.enchants.put(e.getKey(),number(e.getValue()));}
            case "enchant_with_levels" -> s.levels=number(f.get("levels"));
            case "enchant_randomly" -> {
                List<Enchantment> options=new ArrayList<>();
                if(f.has("options"))for(JsonElement e:f.getAsJsonArray("options")){Enchantment en=Registry.ENCHANTMENT.get(NamespacedKey.fromString(e.getAsString()));if(en!=null)options.add(en);}
                else for(Enchantment en:Registry.ENCHANTMENT)if(en.canEnchantItem(new ItemStack(Objects.requireNonNull(Material.matchMaterial(s.material)))))options.add(en);
                if(!options.isEmpty()){Enchantment en=options.get(random.nextInt(options.size()));s.enchants.put(en.getKey().toString(),random.nextInt(en.getStartLevel(),en.getMaxLevel()+1));}
            }
            default -> throw new IllegalArgumentException("不支持的战利品修饰 "+type);
        }
    }
    private static String string(JsonObject o,String key,String fallback){return o.has(key)&&o.get(key).isJsonPrimitive()?o.get(key).getAsString():fallback;}
    private final class Spec{
        final String material;int count=1,levels=-1;final JsonObject components=new JsonObject();final Map<String,Integer> enchants=new LinkedHashMap<>();
        Spec(String material){this.material=material;}
        ItemStack build(){
            if(count<=0)return null;StringBuilder input=new StringBuilder(material);
            if(!components.isEmpty()){input.append('[');boolean first=true;for(var e:components.entrySet()){if(!first)input.append(',');first=false;input.append(e.getKey()).append('=').append(e.getValue());}input.append(']');}
            ItemStack stack=Bukkit.getItemFactory().createItemStack(input.toString());
            if(levels>=0)stack=Bukkit.getItemFactory().enchantItem(stack,levels,false);
            for(var e:enchants.entrySet()){
                Enchantment en=Registry.ENCHANTMENT.get(NamespacedKey.fromString(e.getKey()));if(en==null)throw new IllegalArgumentException("未知附魔 "+e.getKey());
                if(stack.getItemMeta() instanceof EnchantmentStorageMeta meta){meta.addStoredEnchant(en,e.getValue(),true);stack.setItemMeta(meta);}else stack.addUnsafeEnchantment(en,e.getValue());
            }
            JsonObject custom=components.has("minecraft:custom_data")?components.getAsJsonObject("minecraft:custom_data"):null;
            if(custom!=null&&custom.has("tnt")){
                var meta=stack.getItemMeta();meta.getPersistentDataContainer().set(new NamespacedKey("mlcgames","source_tnt"),org.bukkit.persistence.PersistentDataType.INTEGER,custom.get("tnt").getAsInt());stack.setItemMeta(meta);
            }
            stack.setAmount(Math.min(count,99));return stack;
        }
    }
}
