package com.mlc.mlcgames.bank.items;

import net.kyori.adventure.text.Component;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Barrel;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.components.UseCooldownComponent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Random;

import static com.mlc.mlcgames.Mlcgames.miniMessage;

public class Bankgameloottable {
    public static ItemStack air;
    public static ItemStack bone;
    public static ItemStack diamond;
    public static ItemStack gold;
    public static ItemStack arrow;
    public static ItemStack knockbackstick;
    public static ItemStack speedpotion;
    public static ItemStack jumppotion;
    public static ItemStack regenpotion;
    public static ItemStack strengthpotion;
    public static ItemStack invisibilitypotion;

    public static ItemStack book1;
    public static ItemStack book2;
    public static ItemStack book3;
    public static ItemStack book4;
    public static ItemStack book5;
    public static ItemStack book6;
    public static ItemStack book7;
    public static ItemStack book8;
    public static ItemStack book9;
    public static ItemStack book10;
    public static ItemStack book11;
    public static ItemStack book12;
    public static ItemStack book13;
    public static ItemStack book14;
    public static ItemStack book15;
    public static ItemStack book16;
    public static ItemStack book17;
    public static ItemStack book18;
    public static ItemStack book19;
    public static ItemStack book20;
    public static ItemStack book21;
    public static ItemStack book22;
    public static ItemStack book23;
    public static ItemStack book24;
    public static ItemStack book25;
    public static ItemStack book26;
    public static ItemStack book27;
    public static ItemStack book28;

    public static List<ItemStack> loottable;

    public static void init(){
        air = new ItemStack(Material.AIR);

        bone = new ItemStack(Material.BONE);

        diamond = new ItemStack(Material.DIAMOND);

        gold = new ItemStack(Material.GOLD_INGOT);
        ItemMeta goldmeta = gold.getItemMeta();
        goldmeta.customName(miniMessage.deserialize("<!i>以假乱真的金条"));
        gold.setItemMeta(goldmeta);

        arrow = new ItemStack(Material.ARROW);
        arrow.setAmount(4);

        knockbackstick = new ItemStack(Material.WOODEN_SWORD);
        ItemMeta meta = knockbackstick.getItemMeta();
        meta.customName(miniMessage.deserialize("<!i>击退剑"));
        meta.addEnchant(Enchantment.KNOCKBACK,5,true);

        Damageable damageable = (Damageable) meta;
        damageable.setUnbreakable(false);
        damageable.setMaxDamage(2);
        damageable.setDamage(0);
        AttributeModifier attributeModifier = new AttributeModifier(Objects.requireNonNull(NamespacedKey.fromString("mlcgame:knockback")),-0.5,AttributeModifier.Operation.ADD_NUMBER);
        meta.addAttributeModifier(Attribute.ATTACK_DAMAGE, attributeModifier);
        knockbackstick.setItemMeta(meta);

        speedpotion = new ItemStack(Material.POTION);
        ItemMeta speedpotionmeta = speedpotion.getItemMeta();
        speedpotionmeta.customName(miniMessage.deserialize("<!i>速度药水"));
        PotionMeta meta1= (PotionMeta) speedpotionmeta;
        meta1.addCustomEffect(new PotionEffect(PotionEffectType.SPEED, 30*20, 0),true);
        speedpotion.setItemMeta(speedpotionmeta);

        jumppotion = new ItemStack(Material.POTION);
        ItemMeta jumppotionmeta = jumppotion.getItemMeta();
        jumppotionmeta.customName(miniMessage.deserialize("<!i>跳跃药水"));
        PotionMeta meta2= (PotionMeta) jumppotionmeta;
        meta2.addCustomEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 60*20, 0),true);
        jumppotion.setItemMeta(jumppotionmeta);

        regenpotion = new ItemStack(Material.POTION);
        ItemMeta regenpotionmeta = regenpotion.getItemMeta();
        regenpotionmeta.customName(miniMessage.deserialize("<!i>再生药水"));
        PotionMeta meta3= (PotionMeta) regenpotionmeta;
        meta3.addCustomEffect(new PotionEffect(PotionEffectType.REGENERATION, 30*20, 0),true);
        regenpotion.setItemMeta(regenpotionmeta);

        strengthpotion = new ItemStack(Material.POTION);
        ItemMeta strengthpotionmeta = strengthpotion.getItemMeta();
        strengthpotionmeta.customName(miniMessage.deserialize("<!i>力量药水"));
        PotionMeta meta4= (PotionMeta) strengthpotionmeta;
        meta4.addCustomEffect(new PotionEffect(PotionEffectType.STRENGTH, 30*20, 0),true);
        strengthpotion.setItemMeta(strengthpotionmeta);

        invisibilitypotion = new ItemStack(Material.POTION);
        ItemMeta invisibilitypotionmeta = invisibilitypotion.getItemMeta();
        invisibilitypotionmeta.customName(miniMessage.deserialize("<!i>隐身药水"));
        PotionMeta meta5= (PotionMeta) invisibilitypotionmeta;
        meta5.addCustomEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 30*20, 1),true);
        invisibilitypotion.setItemMeta(invisibilitypotionmeta);


        book1 = new ItemStack(Material.BOOK);
        ItemMeta book1meta = book1.getItemMeta();
        book1meta.customName(miniMessage.deserialize("<!i>伏尼契手稿"));
        book1.setItemMeta(book1meta);

        book2 = new ItemStack(Material.BOOK);
        ItemMeta book2meta = book2.getItemMeta();
        book2meta.customName(miniMessage.deserialize("<!i>工务部宣传单"));
        book2.setItemMeta(book2meta);

        book3 = new ItemStack(Material.BOOK);
        ItemMeta book3meta = book3.getItemMeta();
        book3meta.customName(miniMessage.deserialize("<!i>货运列车路线图"));
        book3.setItemMeta(book3meta);


        book4 = new ItemStack(Material.BOOK);
        ItemMeta book4meta = book4.getItemMeta();
        book4meta.customName(miniMessage.deserialize("<!i>技术笔记（阿拜多斯）"));
        book4.setItemMeta(book4meta);

        book5 = new ItemStack(Material.BOOK);
        ItemMeta book5meta = book5.getItemMeta();
        book5meta.customName(miniMessage.deserialize("<!i>技术笔记（阿里乌斯）"));
        book5.setItemMeta(book5meta);

        book6 = new ItemStack(Material.BOOK);
        ItemMeta book6meta = book6.getItemMeta();
        book6meta.customName(miniMessage.deserialize("<!i>技术笔记（千禧年）"));
        book6.setItemMeta(book6meta);

        book7 = new ItemStack(Material.BOOK);
        ItemMeta book7meta = book7.getItemMeta();
        book7meta.customName(miniMessage.deserialize("<!i>技术笔记（圣三一）"));
        book7.setItemMeta(book7meta);

        book8 = new ItemStack(Material.BOOK);
        ItemMeta book8meta = book8.getItemMeta();
        book8meta.customName(miniMessage.deserialize("<!i>技术笔记（山海经）"));
        book8.setItemMeta(book8meta);

        book9 = new ItemStack(Material.BOOK);
        ItemMeta book9meta = book9.getItemMeta();
        book9meta.customName(miniMessage.deserialize("<!i>技术笔记（瓦尔基里）"));
        book9.setItemMeta(book9meta);

        book10 = new ItemStack(Material.BOOK);
        ItemMeta book10meta = book10.getItemMeta();
        book10meta.customName(miniMessage.deserialize("<!i>技术笔记（红冬）"));
        book10.setItemMeta(book10meta);

        book11 = new ItemStack(Material.BOOK);
        ItemMeta book11meta = book11.getItemMeta();
        book11meta.customName(miniMessage.deserialize("<!i>技术笔记（格黑娜）"));
        book11.setItemMeta(book11meta);

        book12 = new ItemStack(Material.BOOK);
        ItemMeta book12meta = book12.getItemMeta();
        book12meta.customName(miniMessage.deserialize("<!i>技术笔记（百鬼夜行）"));
        book12.setItemMeta(book12meta);

        book13 = new ItemStack(Material.BOOK);
        ItemMeta book13meta = book13.getItemMeta();
        book13meta.customName(miniMessage.deserialize("<!i>谜之杂志"));
        List<Component> lore = List.of(miniMessage.deserialize("<!i>刊载了大量来源不明的阴谋论的杂志。随便翻翻无妨，但切勿盲目轻信。"));
        book13meta.lore(lore);
        book13.setItemMeta(book13meta);

        book14 = new ItemStack(Material.BOOK);
        ItemMeta book14meta = book14.getItemMeta();
        book14meta.customName(miniMessage.deserialize("<!i>评论笔记本"));
        lore = List.of(miniMessage.deserialize("<!i>密密麻麻记录着某人评论的语句的笔记本。虽然用词合乎礼仪，内容却严厉尖锐。"));
        book14meta.lore(lore);
        book14.setItemMeta(book14meta);

        book15 = new ItemStack(Material.BOOK);
        ItemMeta book15meta = book15.getItemMeta();
        book15meta.customName(miniMessage.deserialize("<!i>情人节挑战书"));
        lore = List.of(miniMessage.deserialize("<!i>玲纱的挑战书形状的巧克力。其实只是在宽敞的巧克力上放着挑战书信封。里面有玲纱的来信。"));
        book15meta.lore(lore);
        book15.setItemMeta(book15meta);

        book16 = new ItemStack(Material.BOOK);
        ItemMeta book16meta = book16.getItemMeta();
        book16meta.customName(miniMessage.deserialize("<!i>若藻逮捕令"));
        lore = List.of(miniMessage.deserialize("<!i>为了逮补若藻，而从女武神警察学园取得的逮捕令。上面写着若藻一直以来犯下的可怕罪行。"));
        book16meta.lore(lore);
        book16.setItemMeta(book16meta);

        book17 = new ItemStack(Material.BOOK);
        ItemMeta book17meta = book17.getItemMeta();
        book17meta.customName(miniMessage.deserialize("<!i>书之友"));
        lore = List.of(miniMessage.deserialize("<!i>经过特殊处理而不易脏手的圆球状特制巧克力。在志美子等读书人之间很受欢迎。"));
        book17meta.lore(lore);
        book17.setItemMeta(book17meta);

        book18 = new ItemStack(Material.BOOK);
        ItemMeta book18meta = book18.getItemMeta();
        book18meta.customName(miniMessage.deserialize("<!i>完整的罗洪特抄本"));
        lore = List.of(miniMessage.deserialize("<!i>状态完整的罗洪特抄本。可用来进行各种制造及强化。"));
        book18meta.lore(lore);
        book18.setItemMeta(book18meta);

        book19 = new ItemStack(Material.BOOK);
        ItemMeta book19meta = book19.getItemMeta();
        book19meta.customName(miniMessage.deserialize("<!i>文化书籍的样刊"));
        lore = List.of(miniMessage.deserialize("<!i>象征着文化交流的书籍的样本。健全的文化交流是很重要的。在奇怪的误解扩散之前，早点去回收吧。"));
        book19meta.lore(lore);
        book19.setItemMeta(book19meta);

        book20 = new ItemStack(Material.BOOK);
        ItemMeta book20meta = book20.getItemMeta();
        book20meta.customName(miniMessage.deserialize("<!i>修学旅行指南计划表"));
        lore = List.of(miniMessage.deserialize("<!i>总结了修学旅行指南计划的文件。即便计划会被打乱，制定计划也仍至关重要。"));
        book20meta.lore(lore);
        book20.setItemMeta(book20meta);

        book21 = new ItemStack(Material.BOOK);
        ItemMeta book21meta = book21.getItemMeta();
        book21meta.customName(miniMessage.deserialize("<!i>学院交流会传单"));
        lore = List.of(miniMessage.deserialize("<!i>公告百鬼夜行和格黑娜学园间的交流会将要举办的传单。上面除了有主办人尼娅的问候，也有这次交流会的主要行程。"));
        book21meta.lore(lore);
        book21.setItemMeta(book21meta);

        book22 = new ItemStack(Material.BOOK);
        ItemMeta book22meta = book22.getItemMeta();
        book22meta.customName(miniMessage.deserialize("<!i>指南手册"));
        lore = List.of(miniMessage.deserialize("<!i>晄轮大祭实行委员本部发的指南手册。除了赛程之外，还详细记载着场地附近的美食地图。"));
        book22meta.lore(lore);
        book22.setItemMeta(book22meta);

        book23 = new ItemStack(Material.BOOK);
        ItemMeta book23meta = book23.getItemMeta();
        book23meta.customName(miniMessage.deserialize("<!i>初音未来的写真卡"));
        lore = List.of(miniMessage.deserialize("<!i>初音未来在<基沃托斯现场演唱会>上发送的特制写真小卡。因为演唱会盛况空前，小卡成了凡是初音粉丝都想入手的稀有周边商品。"),
                miniMessage.deserialize("<!i>（赠予任意学生时 ＋60 好感经验。）")
                );
        book23meta.lore(lore);
        book23.setItemMeta(book23meta);

        book24 = new ItemStack(Material.BOOK);
        ItemMeta book24meta = book24.getItemMeta();
        book24meta.customName(miniMessage.deserialize("<!i>禁忌之恋 ~不被允许的恋情才更加美好~"));
        lore = List.of(miniMessage.deserialize("<!i>最近人气恋爱小说作者「恋爱鲤鱼」的最新作！ 无法实现的禁忌之恋，令人魂牵梦萦的话题作品！"));
        book24meta.lore(lore);
        book24.setItemMeta(book24meta);

        book25 = new ItemStack(Material.BOOK);
        ItemMeta book25meta = book25.getItemMeta();
        book25meta.customName(miniMessage.deserialize("<!i>游戏杂志 『HIT GIRLS 』"));
        lore = List.of(miniMessage.deserialize("<!i>不放过任何琐碎的情报。 收录基沃托斯最新游戏发售消息、经典游戏攻略、游戏周边发售等消息的游戏杂志。"));
        book25meta.lore(lore);
        book25.setItemMeta(book25meta);

        book26 = new ItemStack(Material.BOOK);
        ItemMeta book26meta = book26.getItemMeta();
        book26meta.customName(miniMessage.deserialize("<!i>桌游「THE·人生」"));
        lore = List.of(miniMessage.deserialize("<!i>可以就业、中头奖、进行宇宙之旅的好玩桌游。每局游戏都有难以预料结果的轮盘和骰子，就如同我们的人生一样。"));
        book26meta.lore(lore);
        book26.setItemMeta(book26meta);

        book27 = new ItemStack(Material.BOOK);
        ItemMeta book27meta = book27.getItemMeta();
        book27meta.customName(miniMessage.deserialize("<!i>游戏杂志"));
        lore = List.of(miniMessage.deserialize("<!i>对游戏开发部所做游戏作出激烈批评的杂志。也许回收后烧掉比较好……？"));
        book27meta.lore(lore);
        book27.setItemMeta(book27meta);

        book28 = new ItemStack(Material.BOOK);
        ItemMeta book28meta = book28.getItemMeta();
        book28meta.customName(miniMessage.deserialize("<!i>奥秘之书"));
        lore = List.of(miniMessage.deserialize("<!i>记载着战术奥秘的技术笔记。"));
        book28meta.lore(lore);
        book28.setItemMeta(book28meta);


        loottable = List.of(
                air,
                air,
                air,
                air,
                air,
                air,
                air,
                air,
                air,
                air,
                air,
                air,
                air,
                air,
                air,
                air,
                air,
                air,
                air,
                air,
                air,
                air,
                air,

                bone,
                diamond,
                gold,
                knockbackstick,
                speedpotion,
                jumppotion,
                regenpotion,
                strengthpotion,
                invisibilitypotion,
                book1,
                book2,
                book3,
                book4,
                book5,
                book6,
                book7,
                book8,
                book9,
                book10,
                book11,
                book12,
                book13,
                book14,
                book15,
                book16,
                book17,
                book18,
                book19,
                book20,
                book21,
                book22,
                book23,
                book24,
                book25,
                book26,
                book27,
                book28);
    }





    public static void addrandomitem(@Nullable Block clickedBlock) {
        if(clickedBlock==null){
            return;
        }
        if(clickedBlock.getType()!= Material.BARREL){
            return;
        }
        Barrel barrel = (Barrel) clickedBlock.getState();
        Inventory inv = barrel.getInventory();
        inv.clear();
        Random random = new Random();
        int turn = random.nextInt(27);
        for(int i=0;i<turn;i++){
            inv.setItem(i,getrandomitem());
        }

    }
    private static ItemStack getrandomitem(){
        Random random = new Random();
        int item = random.nextInt(loottable.size());
        return loottable.get(item);
    }

}
