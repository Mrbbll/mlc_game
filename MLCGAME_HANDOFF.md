# MLCGame 接手说明：Blue Archive 风格战斗属性系统

> 目标：让新 Agent 能直接接手 `mlcgame` 项目，继续实现基于 CraftEngine
> 的 Blue Archive（蔚蓝档案）风格战斗系统。
>
> 当前重点不是完整复刻 Blue Archive
> 全部战斗公式，而是先建立一个稳定、可扩展的 **攻击类型 / 装甲类型 /
> 相性倍率** 基础框架。

## 1. 背景与需求

项目希望在 Minecraft / Paper 服务端中实现类似 Blue Archive
的攻击属性克制机制。

核心概念：

### 攻击类型

初期至少考虑：

-   `EXPLOSIVE`：爆发
-   `PIERCING`：贯通
-   `MYSTIC`：神秘
-   `SONIC`：振动

后续应允许继续扩展，不要把业务逻辑写死在事件监听器中。

### 装甲类型

初期至少考虑：

-   `LIGHT`：轻装甲
-   `HEAVY`：重装甲
-   `SPECIAL`：特殊装甲
-   `ELASTIC`：弹性装甲

要求装甲类型是**实体个体级别**的数据。

例如，同样都是 `minecraft:zombie`：

-   Zombie A 可以是 `LIGHT`
-   Zombie B 可以是 `HEAVY`
-   Boss Zombie 可以是 `SPECIAL`

因此不能只按照 Bukkit `EntityType` 判断装甲。

### 相性结果

攻击类型与装甲类型组合后产生类似 Blue Archive 的相性结果：

-   `WEAK`
-   `EFFECTIVE`
-   `NORMAL`
-   `RESIST`

当前可先按以下倍率设计：

-   Weak：`2.0`
-   Effective：`1.5`
-   Normal：`1.0`
-   Resist：`0.5`

注意：倍率表应集中配置/管理，不要把大量 `switch` 分散在代码中。

------------------------------------------------------------------------

## 2. CraftEngine 在系统中的定位

项目已经考虑使用 CraftEngine。

CraftEngine 当前有自定义 Attribute System，可以：

-   定义自定义数值属性；
-   给物品添加属性修饰符；
-   使用 `scope: entity` 修改持有者属性；
-   使用 `scope: weapon` 让属性只参与当前武器伤害结算；
-   在 Damage Rules 中读取攻击者/受击者属性；
-   使用表达式、composition 或 JavaScript 编写伤害公式；
-   通过 API 注册自定义 Damage Effect 等扩展内容。

但 CraftEngine 的 Attribute System 当前仍属于
**Experimental**，官方明确说明配置格式、行为和 API 可能发生不兼容变化。

因此不要把整个 BA 战斗核心强绑定到 CraftEngine 的实验性内部 API。

建议职责划分：

``` text
CraftEngine
├── 自定义武器/物品
├── 武器显示
├── 数值型 RPG 属性
│   ├── 攻击力
│   ├── 暴击率
│   ├── 暴击伤害
│   ├── 防御力
│   └── 穿透等
└── 必要时参与 Damage Rules

MLCGame 自己的战斗模块
├── AttackType
├── ArmorType
├── DamageAffinity / Effectiveness
├── 实体装甲类型存储
├── 武器攻击类型解析
├── 相性矩阵
├── 最终倍率计算
├── Bukkit/Paper 伤害事件接入
└── 后续 Weak/Resist UI、粒子、音效等
```

------------------------------------------------------------------------

## 3. 推荐的数据模型

建议先建立以下枚举。

### `AttackType`

示意：

``` java
public enum AttackType {
    EXPLOSIVE,
    PIERCING,
    MYSTIC,
    SONIC
}
```

### `ArmorType`

示意：

``` java
public enum ArmorType {
    LIGHT,
    HEAVY,
    SPECIAL,
    ELASTIC
}
```

### `DamageAffinity`

建议不要直接让业务层返回一个 `double`，而是先返回语义明确的结果：

``` java
public enum DamageAffinity {
    WEAK(2.0),
    EFFECTIVE(1.5),
    NORMAL(1.0),
    RESIST(0.5);

    private final double multiplier;

    DamageAffinity(double multiplier) {
        this.multiplier = multiplier;
    }

    public double multiplier() {
        return multiplier;
    }
}
```

这样以后显示 Weak / Resist 提示时不需要重新反推倍率。

------------------------------------------------------------------------

## 4. 实体装甲类型存储

### 推荐方案：PersistentDataContainer

装甲类型属于某一个实体实例，不应该只按照 EntityType 判断。

建议使用 Bukkit/Paper `PersistentDataContainer` 保存：

``` text
mlcgame:armor_type = "HEAVY"
```

示意代码：

``` java
NamespacedKey armorTypeKey =
        new NamespacedKey(plugin, "armor_type");

entity.getPersistentDataContainer().set(
        armorTypeKey,
        PersistentDataType.STRING,
        ArmorType.HEAVY.name()
);
```

读取时：

1.  获取 PDC 字符串；
2.  转换为 `ArmorType`；
3.  处理不存在/非法值；
4.  使用统一默认值或返回 `Optional<ArmorType>`。

建议封装成独立 Service，例如：

``` text
ArmorTypeService
├── getArmorType(Entity)
├── setArmorType(Entity, ArmorType)
├── clearArmorType(Entity)
└── hasArmorType(Entity)
```

不要让各个 Listener 自己直接操作 PDC。

------------------------------------------------------------------------

## 5. 武器攻击类型

需要决定 CraftEngine 武器如何声明攻击类型。

目标是最终可以配置类似：

``` text
某把枪
attack-type: EXPLOSIVE
```

然后伤害事件能够得到：

``` java
AttackType attackType = attackTypeService.getAttackType(item);
```

实现时优先调查以下方案：

1.  CraftEngine 是否有稳定公开 API 可以读取自定义物品 ID / 自定义数据；
2.  是否适合在物品 PDC / Data Component 中保存 `mlcgame:attack_type`；
3.  是否可以通过 MLCGame 配置建立 CraftEngine item ID → AttackType
    映射。

推荐优先保证 MLCGame 与 CE 解耦。

例如配置：

``` yaml
weapons:
  ba:shiroko_rifle:
    attack-type: EXPLOSIVE

  ba:hoshino_shotgun:
    attack-type: PIERCING
```

如果 CraftEngine 能稳定取得 item ID，就通过 item ID 查询攻击类型。

不要为了取得一个攻击类型而深度依赖 CE Experimental Attribute API。

------------------------------------------------------------------------

## 6. 相性矩阵

建立一个独立的 `DamageAffinityService` / `TypeEffectivenessService`。

调用方式应类似：

``` java
DamageAffinity affinity =
        affinityService.resolve(attackType, armorType);

double finalDamage =
        originalDamage * affinity.multiplier();
```

相性表应该集中维护。

可以先硬编码在一个独立类中，但最好最终支持 YAML 配置，例如：

``` yaml
affinity:
  EXPLOSIVE:
    LIGHT: WEAK
    HEAVY: NORMAL
    SPECIAL: RESIST
    ELASTIC: NORMAL

  PIERCING:
    LIGHT: RESIST
    HEAVY: WEAK
    SPECIAL: NORMAL
    ELASTIC: NORMAL
```

实际 Blue Archive
的完整矩阵请在实现前重新核对可靠资料，不要仅根据本文示意直接认定全部组合。

------------------------------------------------------------------------

## 7. 伤害事件接入

初期可以通过 Paper/Bukkit 的伤害事件实现。

重点处理：

-   玩家近战；
-   玩家使用 CraftEngine 武器；
-   Projectile；
-   Projectile 的 shooter；
-   非玩家攻击者；
-   二次伤害/技能伤害；
-   防止重复应用倍率。

基础流程：

``` text
发生伤害
  ↓
确定真正攻击者
  ↓
确定攻击来源/武器
  ↓
解析 AttackType
  ↓
读取 victim 的 ArmorType
  ↓
解析 DamageAffinity
  ↓
取得 multiplier
  ↓
修改伤害
  ↓
触发 Weak / Resist 等反馈
```

建议将 Listener 保持很薄：

``` java
@EventHandler
public void onDamage(EntityDamageByEntityEvent event) {
    combatService.handle(event);
}
```

真正逻辑放到 `CombatService`。

------------------------------------------------------------------------

## 8. Projectile 必须特别处理

枪械/弓箭/技能很可能通过 Projectile 造成伤害。

不能简单使用：

``` java
event.getDamager()
```

就认为这是玩家。

需要区分：

``` text
direct entity = projectile
causing entity = shooter/player
```

并考虑一个重要问题：

**Projectile 发射后玩家可能已经切换武器。**

因此如果攻击类型来自武器，最好在 Projectile 创建/发射时就把 AttackType
写入 Projectile 的 PDC，而不是命中时再读取玩家当前主手。

例如：

``` text
Arrow PDC:
mlcgame:attack_type = EXPLOSIVE
```

命中时直接读取 Projectile 保存的攻击类型。

------------------------------------------------------------------------

## 9. 管理命令

为了方便开发测试，建议实现命令。

至少：

``` text
/mlc armor get
/mlc armor set <type>
/mlc armor clear
```

目标实体可以先设计为玩家准星指向的 LivingEntity。

例如：

``` text
/mlc armor set heavy
```

将准星实体设置为 Heavy Armor。

还可以加入：

``` text
/mlc attacktype
/mlc combat debug
```

Debug 模式输出：

``` text
Attacker: xxx
Victim: xxx
AttackType: EXPLOSIVE
ArmorType: LIGHT
Affinity: WEAK
Multiplier: 2.0
OriginalDamage: 10
FinalDamage: 20
```

这对后续排查 CraftEngine 与 Paper 的伤害顺序非常重要。

------------------------------------------------------------------------

## 10. 配置设计

建议至少准备：

``` text
config.yml
combat.yml
weapons.yml
messages.yml
```

### `combat.yml`

负责：

-   相性矩阵；
-   Weak/Effective/Normal/Resist 倍率；
-   默认 AttackType；
-   默认 ArmorType；
-   是否启用相性系统。

### `weapons.yml`

负责：

``` text
CraftEngine Item ID -> AttackType
```

映射。

### `messages.yml`

后续用于：

-   Weak
-   Resist
-   Effective
-   Normal
-   Debug

等显示文本。

------------------------------------------------------------------------

## 11. CraftEngine Attribute 的使用原则

CraftEngine Attribute 更适合处理**连续数值属性**：

-   Attack
-   Defense
-   CriticalChance
-   CriticalDamage
-   ArmorPenetration
-   DamageBonus
-   HealingBonus

AttackType / ArmorType 本质是枚举/标签，而不是连续数值。

因此当前建议：

``` text
AttackType / ArmorType
→ MLCGame 自己管理

攻击力 / 防御力 / 暴击等
→ 可以使用 CraftEngine Attribute
```

CraftEngine Damage Rules 支持：

``` text
<attacker_attr:namespace:attribute>
<victim_attr:namespace:attribute>
```

因此后续可以把 CE 数值属性加入 MLCGame 的最终伤害公式。

------------------------------------------------------------------------

## 12. CraftEngine API 调查任务

新 Agent 接手后需要进一步确认当前项目使用的 CraftEngine 版本，并检查对应
API。

重点调查：

1.  当前 CraftEngine 具体版本；
2.  Maven/Gradle dependency 坐标；
3.  CraftEngine API 获取自定义 Item ID 的正式方法；
4.  CraftEngine Attribute API 是否有稳定的：
    -   查询 Attribute；
    -   查询实体 AttributeInstance；
    -   读取最终值；
    -   添加/移除 modifier；
5.  DamageEvent / DamageEffect API 的包名和实际签名；
6.  CE Damage Rules 与 Bukkit/Paper Damage Event 的执行顺序；
7.  自己修改 Bukkit event damage 后是否会被 CE 再次覆盖；
8.  Projectile 对 CE weapon-scope attribute 的处理方式。

不要根据网上旧版本代码直接写 import，必须以项目当前 CraftEngine jar/API
为准。

CraftEngine 官方当前明确标记 Attribute System 为
Experimental，因此这部分应通过适配层隔离。

建议：

``` text
integration/
└── craftengine/
    ├── CraftEngineHook
    ├── CraftEngineItemResolver
    └── CraftEngineAttributeBridge
```

业务层不要直接散落 CraftEngine API 调用。

------------------------------------------------------------------------

## 13. 推荐包结构

可以考虑：

``` text
<项目基础包>
├── MLCGamePlugin.java
│
├── combat/
│   ├── AttackType.java
│   ├── ArmorType.java
│   ├── DamageAffinity.java
│   ├── CombatService.java
│   └── TypeEffectivenessService.java
│
├── entity/
│   └── ArmorTypeService.java
│
├── weapon/
│   └── AttackTypeService.java
│
├── listener/
│   ├── CombatListener.java
│   └── ProjectileListener.java
│
├── command/
│   └── MlcCommand.java
│
├── integration/
│   └── craftengine/
│       ├── CraftEngineHook.java
│       ├── CraftEngineItemResolver.java
│       └── CraftEngineAttributeBridge.java
│
└── config/
    ├── CombatConfig.java
    └── WeaponConfig.java
```

根据现有项目结构调整，不要为了符合本文强行大规模重构。

------------------------------------------------------------------------

## 14. 第一阶段验收目标

第一阶段只要求跑通最小闭环：

1.  插件能够正常加载；
2.  能给任意 LivingEntity 设置 ArmorType；
3.  ArmorType 在实体 PDC 中持久保存；
4.  能识别一把测试武器的 AttackType；
5.  攻击时能够解析 AttackType × ArmorType；
6.  能得到 Weak/Normal/Resist 等结果；
7.  能修改最终伤害；
8.  Projectile 能正确继承发射时的 AttackType；
9.  有 Debug 输出可以确认计算过程；
10. 不影响没有设置 BA 属性的普通 Minecraft 战斗。

测试示例：

``` text
测试武器：EXPLOSIVE
目标 Zombie：LIGHT
基础伤害：10

期望：
Affinity = WEAK
Multiplier = 2.0
FinalDamage ≈ 20
```

再测试：

``` text
测试武器：EXPLOSIVE
目标 Zombie：SPECIAL

期望：
Affinity = RESIST
Multiplier = 0.5
FinalDamage ≈ 5
```

具体矩阵以最终核对后的配置为准。

------------------------------------------------------------------------

## 15. 第二阶段工作

第一阶段稳定后再加入：

-   CraftEngine 数值 Attribute；
-   Attack / Defense；
-   CriticalChance；
-   CriticalDamage；
-   ArmorPenetration；
-   最终伤害公式；
-   Weak / Resist 浮字；
-   ActionBar；
-   粒子；
-   音效；
-   技能伤害；
-   Mob 主动攻击玩家时的 AttackType；
-   玩家自身 ArmorType；
-   Boss 特殊抗性；
-   配置热重载；
-   PlaceholderAPI；
-   其他怪物插件集成。

------------------------------------------------------------------------

## 16. 重要注意事项

### 不要重复计算伤害

CraftEngine Damage Rules 和自己的 Bukkit Listener
如果同时修改同一次伤害，很容易发生：

``` text
原伤害 10
CE ×2
MLCGame 又 ×2
最终 40
```

必须明确最终伤害管线由谁负责。

### 不要把 ArmorType 当作原版 Armor

这里的：

``` text
LIGHT / HEAVY / SPECIAL / ELASTIC
```

是 Blue Archive 风格的**防御类型**。

它和 Minecraft：

``` text
minecraft:armor
minecraft:armor_toughness
```

不是同一个概念。

### 不要把类型系统硬编码进 Listener

Listener 只负责接事件。

类型解析、矩阵和计算全部放 Service。

### 保证默认行为安全

如果：

-   武器没有 AttackType；
-   实体没有 ArmorType；
-   CraftEngine 不存在；
-   CE API 调用失败；

应该退回正常 Minecraft 伤害，而不是取消攻击或报错刷屏。

------------------------------------------------------------------------

## 17. 新 Agent 接手后的第一步

请先检查 `D:\desktop\plugins\mlcgame` 当前项目，而不是直接创建全新工程。

依次确认：

1.  `build.gradle` / `build.gradle.kts` / `pom.xml`；
2.  Paper API 版本；
3.  Java 版本；
4.  CraftEngine 版本；
5.  当前 package；
6.  已有 Listener / Service / Command；
7.  是否已经存在伤害系统；
8.  是否已经存在 PDC 工具类；
9.  是否已有 CraftEngine integration；
10. 是否有现成配置加载框架。

然后再基于现有架构实现。

不要未经检查就覆盖现有代码。

------------------------------------------------------------------------

## 18. 当前决策摘要

目前已确定的方向：

``` text
目标：
实现 Blue Archive 风格攻击类型 × 装甲类型克制系统。

实体装甲：
使用实体个体级数据，优先 PDC。

攻击类型：
由武器/攻击来源决定。

相性：
AttackType × ArmorType -> DamageAffinity -> multiplier。

CraftEngine：
主要负责物品和数值型 Attribute；
通过 integration 层与 MLCGame 对接；
避免让核心类型系统依赖 CE Experimental Attribute API。

伤害：
先实现 Paper/Bukkit 最小闭环，再决定是否把最终公式迁入/结合 CE Damage Rules。
```

------------------------------------------------------------------------

## 19. 官方资料提示

实现 CraftEngine 对接时优先查看 CraftEngine 当前官方 Wiki：

-   Attribute System
-   Attribute Definition
-   Item Modifiers
-   Damage Rules / Damage Formula
-   CraftEngine API / Javadocs（如果当前版本提供）

截至整理本文时，CraftEngine 官方 Wiki 将 Attribute System 标记为
Experimental，API 和配置存在不兼容变更风险。

因此接手 Agent 必须以项目实际使用版本为准核对
API，不要假定本文中的示意代码就是可直接编译的 CraftEngine API 代码。
