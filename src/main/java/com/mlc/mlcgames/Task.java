package com.mlc.mlcgames;

import com.mlc.mlcgames.Listener.gamelistener;
import com.mlc.mlcgames.bank.commmand.bankgameend;
import com.mlc.mlcgames.bank.commmand.bankgameprepare;
import com.mlc.mlcgames.bank.commmand.bankgamereload;
import com.mlc.mlcgames.bank.items.Bankgameloottable;
import com.mlc.mlcgames.bank.listener.Bankgamelistener;
import com.mlc.mlcgames.bank.utils.Bankgamebossbar;
import com.mlc.mlcgames.bank.utils.Bankgameinit;
import com.mlc.mlcgames.bank.items.Bankgameitemmanager;
import com.mlc.mlcgames.bank.menus.bankmenus;
import com.mlc.mlcgames.commands.reload;
import com.mlc.mlcgames.combat.command.MlcCommand;
import com.mlc.mlcgames.combat.CombatService;
import com.mlc.mlcgames.combat.TypeEffectivenessService;
import com.mlc.mlcgames.combat.config.CombatConfig;
import com.mlc.mlcgames.combat.damageindicator.DamageIndicatorConfig;
import com.mlc.mlcgames.combat.damageindicator.DamageIndicatorService;
import com.mlc.mlcgames.dungeongame.commands.DungeonGame;
import com.mlc.mlcgames.dungeongame.gamephase.dungeongameinit;
import com.mlc.mlcgames.dungeongame.listener.DungeonRoomListener;
import com.mlc.mlcgames.dungeongame.managers.DungeonGameManager;
import com.mlc.mlcgames.dungeongame.menus.SettingMenu;
import com.mlc.mlcgames.sandgame.commands.SandGame;
import com.mlc.mlcgames.sandgame.gamephase.sandgameinit;
import com.mlc.mlcgames.sandgame.listener.Sand_Game_Teamselect_Listener;
import com.mlc.mlcgames.utils.item.GunHitListener;
import com.mlc.mlcgames.zombieday.commands.Zombieday;
import com.mlc.mlcgames.zombieday.gamephase.Init;
import com.mlc.mlcgames.zombieday.listener.EntityListener;
import com.mlc.mlcgames.zombieday.listener.Gunuse;
import com.mlc.mlcgames.zombieday.listener.throwitem;
import com.mlc.mlcgames.combat.entity.ArmorTypeService;
import com.mlc.mlcgames.combat.listener.CombatListener;
import com.mlc.mlcgames.combat.listener.ProjectileCombatListener;
import com.mlc.mlcgames.combat.weapon.AttackTypeService;
import org.bukkit.Bukkit;

import java.io.IOException;
import java.util.Objects;

import static com.mlc.mlcgames.Mlcgames.instance;

public class Task {
    public static void runtask() throws IOException {

        CombatConfig combatConfig = new CombatConfig(instance);
        DamageIndicatorConfig damageIndicatorConfig = new DamageIndicatorConfig(instance);
        ArmorTypeService armorTypeService = new ArmorTypeService(instance);
        AttackTypeService attackTypeService = new AttackTypeService(instance, combatConfig.defaultAttackType());
        CombatService combatService = new CombatService(combatConfig, armorTypeService, attackTypeService,
                new TypeEffectivenessService(combatConfig), new DamageIndicatorService(instance, damageIndicatorConfig));

        //事件注册
        Bukkit.getPluginManager().registerEvents(new Bankgamelistener() ,instance);
        Bukkit.getPluginManager().registerEvents(new gamelistener(),instance);
        Bukkit.getPluginManager().registerEvents(new Gunuse(),instance);
        Bukkit.getPluginManager().registerEvents(new throwitem(),instance);
        Bukkit.getPluginManager().registerEvents(new EntityListener(),instance);
        Bukkit.getPluginManager().registerEvents(new GunHitListener(),instance);
        Bukkit.getPluginManager().registerEvents(new Sand_Game_Teamselect_Listener(),instance);
        Bukkit.getPluginManager().registerEvents(new CombatListener(combatService), instance);
        Bukkit.getPluginManager().registerEvents(new ProjectileCombatListener(combatService), instance);
        DungeonGameManager dungeonGameManager = DungeonGameManager.initialize(instance);
        Bukkit.getPluginManager().registerEvents(new DungeonRoomListener(dungeonGameManager), instance);
        Bukkit.getPluginManager().registerEvents(new SettingMenu(), instance);
        //命令注册
        Objects.requireNonNull(instance.getCommand("reload")).setExecutor(new reload());
        Objects.requireNonNull(instance.getCommand("bankgameprepare")).setExecutor(new bankgameprepare());
        Objects.requireNonNull(instance.getCommand("bankgamereload")).setExecutor(new bankgamereload());
        Objects.requireNonNull(instance.getCommand("bankgameend")).setExecutor(new bankgameend());
        Objects.requireNonNull(instance.getCommand("zombieday")).setExecutor(new Zombieday());
        Objects.requireNonNull(instance.getCommand("sandgame")).setExecutor(new SandGame());
        DungeonGame dungeonGameCommand = new DungeonGame();
        Objects.requireNonNull(instance.getCommand("dungeongame")).setExecutor(dungeonGameCommand);
        MlcCommand mlcCommand = new MlcCommand(armorTypeService, attackTypeService, combatService);
        Objects.requireNonNull(instance.getCommand("mlc")).setExecutor(mlcCommand);
        Objects.requireNonNull(instance.getCommand("mlc")).setTabCompleter(mlcCommand);

        Objects.requireNonNull(instance.getCommand("bankgameprepare")).setTabCompleter(new bankgameprepare());
        Objects.requireNonNull(instance.getCommand("zombieday")).setTabCompleter(new Zombieday());
        Objects.requireNonNull(instance.getCommand("sandgame")).setTabCompleter(new SandGame());
        Objects.requireNonNull(instance.getCommand("dungeongame")).setTabCompleter(dungeonGameCommand);
        //物品初始化
        Bankgameitemmanager.inititem();
        Bankgameloottable.init();
        //bossbar初始化
        Bankgamebossbar.initBankgamebossbar();

        //菜单初始化
        bankmenus.init();

        //游戏初始化
        Bankgameinit.init();
        Init.init();
        sandgameinit.init();
        dungeongameinit.init();
    }
}
