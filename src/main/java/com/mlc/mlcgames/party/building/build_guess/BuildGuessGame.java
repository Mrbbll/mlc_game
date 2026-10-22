package com.mlc.mlcgames.party.building.build_guess;

import com.mlc.mlcgames.party.building.common.BuildingRounds;
import com.mlc.mlcgames.party.core.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import java.util.*;

public final class BuildGuessGame extends BuildingRounds {
    private String word,tip;
    private int guessed;
    private boolean ending;
    @Override public void validate(PartyConfig c,PartyConfig.Arena a){super.validate(c,a);int duration=c.integer("guess-seconds",0,240);if(duration>0&&duration<60)throw new IllegalArgumentException("guess-seconds为0自动，或60..240");if(c.rules.getStringList("words").isEmpty())throw new IllegalArgumentException("猜词词库不能为空");}
    @Override public void start(PartySession s){initialize(s);}
    @Override protected void begin(PartySession s){
        ending=false;guessed=0;
        List<String> words=s.config.rules.getStringList("words");word=words.get(new Random().nextInt(words.size()));tip="_ ".repeat(word.length()).strip();
        int duration=s.config.integer("guess-seconds",0,240);if(duration==0)duration=s.players().size()>=6?120:180;deadline=s.elapsed()+duration*20;
        givePalette(s);announceBuilder(s);
        var helmet=s.item(Material.LEATHER_HELMET,"items.helmet");var meta=helmet.getItemMeta();
        meta.addAttributeModifier(Attribute.BLOCK_INTERACTION_RANGE,new AttributeModifier(new NamespacedKey("mlcgames_party","guess_reach"),3,AttributeModifier.Operation.ADD_NUMBER,EquipmentSlotGroup.HEAD));
        meta.addAttributeModifier(Attribute.MINING_EFFICIENCY,new AttributeModifier(new NamespacedKey("mlcgames_party","guess_mining"),10000,AttributeModifier.Operation.ADD_NUMBER,EquipmentSlotGroup.HEAD));helmet.setItemMeta(meta);helmet.addUnsafeEnchantment(Enchantment.BINDING_CURSE,1);builder.getInventory().setHelmet(helmet);
        builder.sendMessage(s.config.message("secret-word","word",word));
        for(Player p:s.players())if(!isBuilder(p)){
            playing.add(p.getUniqueId());p.setGameMode(GameMode.ADVENTURE);p.teleport(s.arena.group("runners").getFirst());
            p.getInventory().setItem(0,s.item(Material.WRITABLE_BOOK,"items.book"));p.sendMessage(s.config.message("word-length","length",word.length()));}
    }
    @Override public void tick(PartySession s,int elapsed){
        if(!readyToPlay(s,elapsed))return;
        if(ending||builder==null)return;
        if(!s.member(builder)||playing.stream().noneMatch(id->{Player p=Bukkit.getPlayer(id);return p!=null&&s.member(p);})){roundOver(s);return;}
        int seconds=(deadline-elapsed+19)/20;
        if(elapsed>=deadline){roundOver(s);return;}
        if(elapsed%20!=0)return;refill();builder.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST,40,3,true,false));builder.addPotionEffect(new PotionEffect(PotionEffectType.HASTE,40,255,true,false));
        if((seconds==90||seconds==60)&&word.length()>1){int at=new Random().nextInt(word.length());StringBuilder t=new StringBuilder();for(int i=0;i<word.length();i++)t.append(i==at?word.charAt(i):'_').append(' ');tip=t.toString().strip();}
        for(Player p:s.players()){
            p.sendActionBar(s.config.message(isBuilder(p)?"builder-status":"guesser-status","seconds",seconds,"word",word,"tip",tip));
            if(playing.contains(p.getUniqueId())&&!p.getInventory().contains(Material.WRITABLE_BOOK))p.getInventory().setItem(0,s.item(Material.WRITABLE_BOOK,"items.book"));}
    }
    @Override public void editBook(PartySession s,PlayerEditBookEvent e){
        e.setCancelled(true);Player p=e.getPlayer();
        if(ending||!playing.contains(p.getUniqueId())||e.getNewBookMeta().getPageCount()==0)return;
        String answer=e.getNewBookMeta().getPage(1);
        if(answer.equals(word)){
            add(s,p,guessed==0?3:guessed==1?2:1);guessed++;
            playing.remove(p.getUniqueId());p.getInventory().clear();p.setGameMode(GameMode.SPECTATOR);p.teleport(s.arena.spectator());
            s.broadcast("guessed","player",p.getName());
            if(guessed>=4)deadline=Math.min(deadline,s.elapsed()+600);
            if(playing.isEmpty())roundOver(s);
        }else{s.broadcast("guess-wrong","player",p.getName(),"guess",answer.substring(0,Math.min(10,answer.length())));p.getInventory().setItem(0,s.item(Material.WRITABLE_BOOK,"items.book"));}
    }
    private void roundOver(PartySession s){if(ending)return;ending=true;s.broadcast("answer","word",word);if(guessed>0&&s.member(builder))add(s,builder,3);next(s);}
    @Override public boolean breakBlock(PartySession s,Player p,Block b){return !ending&&isBuilder(p)&&inside(b)&&palette.contains(b.getType());}
    @Override public boolean placeBlock(PartySession s,Player p,Block b){return !ending&&isBuilder(p)&&inside(b)&&palette.contains(b.getType());}
}
