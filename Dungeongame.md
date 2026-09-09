# Dungeongame 开发与对接说明

最后维护日期：2026-09-09

维护约定：凡是修改地牢流程、配置契约、原理图规范或 managers 内部架构，都必须同步更新本文对应章节，并在文末“维护记录”说明日期、改动内容和兼容性影响。

## 当前完整流程

1. 玩家执行 `/dungeongame` 打开设置菜单。
2. 菜单可调整难度、地牢大小（普通房间目标数量）和特殊房间比例。
3. 点击“开启游戏”后，旧的 `Dungeongame` 世界会先被卸载并删除，然后创建新的虚空世界。
4. 系统在同一个世界中依次生成 3 关（文件名的 `floor=1-3`），每关 5 层，共 15 个独立布局。每关会从原理图完整且存在同名怪物包的 `set` 中随机选择一个环境预设，并让本关 5 层共用该预设。每次只生成一层，避免把全部工作压在同一个服务器 tick。
5. `RoomSpawner` 按网页原型规划房间和桥，`Worldmanager` 粘贴 FAWE 原理图并在连接墙面开 8x8 的孔；所有房间的绝对边界和门洞会立即保存，不依赖之后被覆盖的静态布局表。
6. 所有房间粘贴结束后，扫描房间中的刷怪标记并把标记替换为普通方块。
7. 玩家第一次进入含有标记的房间时，房间所有连接口被封闭，并在每个标记上方生成第一波怪物；简单、普通、困难分别有 2、3、4 波。
8. 当前波全部消灭后等待 `encounters.next-wave-delay-ticks` 再生成下一波。全部波次结束后才重新开门、解锁终点传送门并生成空战利品箱。
9. 每关第 1-4 层的 End 房生成传送点并通往本关下一层；第 5 层 Boss 清除后生成传送点并通往下一关第 1 层。
10. 第 3 关第 5 层 Boss 清除后生成最终传送点，玩家靠近后设置胜利状态并结束游戏。
11. 点击“结束游戏”或执行 `/dungeongame end` 会取消仍在进行的生成任务、移除受控怪物、打开封闭房门并把参与者送回其他世界。

Purpur 新版可能把该世界存储为主世界下的 `dimensions/minecraft/dungeongame`，Linux 上目录名也会转为小写。删除校验已同时兼容传统 `Dungeongame` 目录和 Bukkit 返回的新式维度目录，但仍只允许删除末级名称为 `dungeongame` 的精确目标，避免误删其他世界。

## 菜单与命令

设置菜单类：`dungeongame/menus/SettingMenu.java`

- 难度：简单、普通、困难；左键向后切换，右键向前切换。
- 地牢大小：显示名为“地牢大小”，实际值是普通房间目标数量，范围 4-30。
- 特殊房间比例：0%-100%，步长 10%。网页算法仍保证至少尝试放置两个特殊房间，所以 0% 不代表绝对没有特殊房间。
- 游戏运行期间不允许更改上述设置。
- 开启游戏：重建世界，分 15 次生成全部 3×5 个布局，完成后进入第 1 关第 1 层。
- 结束游戏：清理当前一局。

命令：

- `/dungeongame` 或 `/dungeongame menu`：打开设置菜单。
- `/dungeongame start`：按当前菜单设置开始。
- `/dungeongame end`：结束当前游戏。
- `/dungeongame enter`：加入当前游戏并传送到起始房间。
- `/dungeongame reload`：重新读取 `dungeongame.yml`、FAWE 模板和 `dungeongame/mobs/*.yml` 环境怪物包。已经生成的房间继续使用开局快照，新配置从下一局生效。
- `/dungeongame info`：显示当前布局统计。
- `/dungeongame generate [关卡1-3] [层数1-5] [普通房数量] [seed]`：仅供单布局调试；使用 `generation.set` 指定环境，正式游玩应使用 `start`。

## 原理图规范

读取目录：FAWE 插件的 `schematics` 文件夹。

命名格式：`floor_set_type_variant.schem`

示例：

- `1_1_start_1.schem`
- `1_1_normal_1.schem`
- `1_1_shop_1.schem`
- `1_1_end_1.schem`
- `1_1_boss_1.schem`：第 1 关、环境 1 的 Boss
- `1_2_start_1.schem`：第 1 关、环境 2 的开始房
- `3_1_boss_1.schem`：第 3 关、环境 1 的最终 Boss
- `1_1_bridge_x_1.schem`：东西向桥
- `1_1_bridge_z_1.schem`：南北向桥

合法类型：`start`、`normal`、`end`、`boss`、`elit`、`shop`、`add`。

字段含义必须严格按下面理解：

- `floor`：地牢关数，只使用 1、2、3。
- `set`：当前关卡的环境预设编号，可有任意多个，例如第 1 关可以同时提供 set 1、2、3。
- `type`：房间类型。
- `variant`：同一 `floor + set + type` 下的随机房间变种。

正式游戏会为每一关随机选择一个可用 set，该关内部 5 层复用同一个模板池。因此不需要也不会读取 `4_*.schem` 或 `5_*.schem`。每个可被选中的 `floor + set` 必须同时具有 Start、Normal、特殊房、End、Boss、X 桥、Z 桥模板以及同名怪物包 `<floor>_<set>.yml`：内部第 1-4 层使用 End，第 5 层使用 Boss。Boss 原理图必须至少包含一个 `CREAKING_HEART`。

尺寸必须遵循网页原型：

- 普通、特殊、开始、结束房间：2x2 区块，即 32x32 方块。
- Boss 房间：3x3 区块，即 48x48 方块。
- X/Z 桥：1x1 区块，即 16x16 方块。
- 高度不限制。粘贴时会自动查找每个原理图最低的非空气方块，并把所有模板的最低层对齐到 `layout.origin.y`。

## 15 个地牢的空间与推进顺序

所有布局都位于同一个 `Dungeongame` 世界。每关的 5 层沿 X 轴排列，3 关沿 Z 轴排列。间隔由 `layout.stage-spacing.x/z` 控制，最小强制为 1600 方块，默认 2048，避免随机布局互相覆盖。

固定推进链为：

`1-1 → 1-2 → 1-3 → 1-4 → 1-5 Boss → 2-1 → … → 2-5 Boss → 3-1 → … → 3-5 Boss → 胜利`

每个 End 和 Boss 原理图必须放置且只能放置一个 `RESPAWN_ANCHOR`，作为传送点的精确坐标。布局扫描时它会被替换为 `progression.inactive-block`（默认空气），解锁后在原坐标恢复为 `progression.portal-block`（默认重生锚）。因此传送点不再随机寻找空间，也不会用中心回退覆盖房间建筑。标记缺失或重复时会直接报告对应原理图文件名和扫描数量。

玩家进入传送点 2.25 格范围后触发；普通 End 房没有怪物标记时在全部布局生成完成后直接出现，如果 End 房含怪物标记则必须清房后出现，Boss 房永远必须清 Boss 后出现。传送方块只是插件控制的安全标记，不建议改成会被原版逻辑抢先处理的 `END_PORTAL`。

胜利时 `Dungeongame.lastGameWon=true`，`Dungeongame.lastWinner` 保存触发最终传送点的玩家 UUID，随后执行正常结束清理。

每提升一关，开启 `scale-with-difficulty` 的怪物生命和攻击伤害会累计乘以 `progression.level-stat-multiplier`，默认值为 1.5。因此第 1 关为 ×1.0、第 2 关为 ×1.5、第 3 关为 ×2.25；该值继续与菜单难度的生命/攻击倍率相乘。normal/elite/boss 不再附加代码内置倍率，三类怪物的基础强度完全由环境怪物文件定义。

## 刷怪标记与房间战斗

直接在房间原理图中放置以下原版方块即可，无需为每个房间手写坐标：

| 原理图方块 | 作用 |
|---|---|
| `SPAWNER`（刷怪笼） | 每波从当前环境的 normal 池加权生成一只怪物 |
| `TRIAL_SPAWNER`（试炼刷怪笼） | 每波从当前环境的 elite 池加权生成一只精英怪物 |
| `CREAKING_HEART`（嘎吱核心） | 每波从当前环境的 boss 池加权生成一个 Boss |

一个房间可以放多个标记；每个标记在每一波复用一次，因此 3 个标记、普通难度会依次生成 3 波、总计 9 只怪物。扫描后标记会替换为 `markers.replacement`。当前波全部受控怪物死亡前不会生成下一波，全部波次结束前不会开门或结算奖励。下一波等待时间由主配置 `encounters.next-wave-delay-ticks` 控制，默认 40 tick。

波次数量由难度枚举固定推导：简单 2 波、普通 3 波、困难 4 波，即难度每提高一级增加一波。Boss 房同样遵循该规则，因此困难 Boss 房会连续进行 4 波 Boss 战。

房门封闭使用 `encounters.closed-door-block`。封闭范围与生成时开的孔完全一致，即区块中心对称的 8 格宽、8 格高开口；具体宽高仍由 `passage` 配置控制。

核心状态管理类：`dungeongame/managers/DungeonGameManager.java`。

## 环境怪物文件

运行目录为 `plugins/mlcgames/dungeongame/mobs/`，命名格式严格为 `<关卡>_<环境>.yml`，例如：

- `1_1.yml`：第 1 关、环境 set 1。
- `1_2.yml`：第 1 关、环境 set 2。
- `3_4.yml`：第 3 关、环境 set 4。

插件首次运行会创建 `1_1.yml`、`2_1.yml`、`3_1.yml` 示例。每个可选原理图环境必须存在对应文件，并且至少定义一个 normal、elite、boss 怪物，否则该环境不会被选中；如果该关没有任何“模板完整且怪物包存在”的环境，游戏会中止生成并报告缺失文件。

单个怪物格式：

```yaml
mobs:
  crypt_guard:
    category: normal
    entity-type: ZOMBIE
    weight: 60
    name: "<green>地穴守卫"
    name-visible: true
    scale-with-difficulty: true
    attributes:
      max-health: 24.0
      armor: 3.0
      armor-toughness: 1.0
      attack-damage: 4.0
      movement-speed: 0.23
      knockback-resistance: 0.10
      follow-range: 32.0
    equipment:
      helmet: IRON_HELMET
      chestplate: "mlcgame:pixel_iron_chestplate"
      leggings: ""
      boots: LEATHER_BOOTS
      main-hand: IRON_SWORD
      off-hand: SHIELD
      auto-pixel-armor: false
      drop-chance: 0.0
```

字段约定：

- `category`：必须是 `normal`、`elite`、`boss`，决定使用哪种原理图标记。
- `entity-type`：Bukkit 生物类型，例如 `ZOMBIE`、`SKELETON`、`PIGLIN_BRUTE`；必须是可存活实体。
- `weight`：同 category 内的相对随机权重，必须至少为 1。
- `name`：MiniMessage 格式显示名；留空则使用原版名称。
- `name-visible`：是否始终显示自定义名称，默认在 name 非空时开启。
- `scale-with-difficulty`：是否让基础生命和攻击继续乘菜单难度及关卡倍率，默认 `true`。
- `attributes`：可配置 `max-health`、`armor`、`armor-toughness`、`attack-damage`、`movement-speed`、`knockback-resistance`、`follow-range`；没有写的属性使用该实体的原版值。击退抗性范围为 0-1。
- `equipment`：支持六个装备槽。物品可写原版 `Material` 或 CraftEngine namespaced ID；空字符串会清空该槽。`drop-chance` 同时控制六个槽位，范围 0-1。
- `auto-pixel-armor`：为 `true` 时先使用既有像素护甲自动发现逻辑，再用本段非空槽位覆盖；整个 `equipment` 段缺失时默认为 `true`，显式写出该段时默认为 `false`。

配置在启动及 `/dungeongame reload` 时一次性校验。文件名错误、YAML 无效、缺少三类池、未知实体/属性、非法数值都会拒绝本次 reload；只有所有文件均有效时才整体替换注册表。当前局保存的是不可变环境快照，因此 reload 不会改变已经开始的波次。

生成实体会写入 `mlcgames:dungeon_session_id`、`mlcgames:dungeon_encounter_id`、`mlcgames:dungeon_mob_id` 三个 PDC 标签，方便后续诊断、掉落扩展和异常实体追踪。

## 难度与 CraftEngine 装备

难度同时影响：

- 怪物最大生命和当前生命。
- 怪物基础攻击伤害。
- 每件护甲额外增加的原版护甲值和护甲韧性。

服务器资源中的 `mlcgame:import/pixel_armor` 是护甲模板，实际生成出来的物品 ID 格式为 `mlcgame:pixel_<套装>_<部位>`。插件不读取测试服绝对路径，而是在 CraftEngine 完成加载后查询它的物品注册表，并按以下四个后缀自动组成套装：

- `_helmet`
- `_chestplate`
- `_leggings`
- `_boots`

只有四件齐全的套装会进入随机池；一只怪物的四件装备必定来自同一套。以后往 CraftEngine 添加符合命名格式的新套装，无需再手工把每个 ID 写入 `dungeongame.yml`。`dungeon-equipment.pixel-armor.namespace` 和 `item-prefix` 可以修改识别范围。

自动像素护甲的难度额外属性位于 `difficulty.easy|normal|hard.armor`：`attribute-bonus-per-piece` 是每件增加的护甲值，`toughness-bonus-per-piece` 是每件增加的护甲韧性。CraftEngine 物品本身已有的属性仍然保留。环境文件显式装备的掉落率由各怪物的 `equipment.drop-chance` 决定。

当前像素护甲模板已经把战斗护甲类型写入头盔的 `mlcgames:armor_type`，插件会直接保留该值。只有旧物品没有这个 PDC 时，才读取 `dungeon-equipment.pixel-armor.armor-type-by-set.<套装名>`；仍未映射则从 `dungeon-equipment.armor-types` 随机补一个。默认怪物池仅使用能显示四件人形护甲的实体。CraftEngine 未安装、找不到完整套装或构造失败时只记录一次警告，不中断地牢战斗。

适配类：`dungeongame/mobs/CraftEngineMobEquipment.java`。

## 战利品扩展契约

目前清房箱子是空的，但箱子的 `PersistentDataContainer` 已保存稳定上下文，后续随机器不需要重新判断房间坐标：

| PDC key | 类型 | 内容 |
|---|---|---|
| `mlcgames:dungeon_loot_level` | INTEGER | 地牢关卡，即文件名中的 floor（1-3） |
| `mlcgames:dungeon_loot_floor` | INTEGER | 当前层数 |
| `mlcgames:dungeon_loot_room_type` | STRING | `RoomType.name()` |
| `mlcgames:dungeon_loot_tier` | INTEGER | 综合品质层级 |

品质层级公式为 `(dungeonLevel - 1) * 50 + floor * 10 + roomBonus`。当前房间加成为：普通 1、Add 2、Elit 3、Boss 5、Shop 2，其他 0。因此不同等级、不同层和不同房间类型天然落入不同品质区间。

推荐后续战利品随机器接受 `DungeonLootContext(dungeonLevel, floor, roomType, qualityTier)`，再按 `level -> floor -> roomType -> weighted entries` 查池。上下文定义在 `dungeongame/loot/DungeonLootContext.java`，只需扩展 `DungeonLootChestService.spawn` 中清空箱子后的填充部分，不要改动 PDC 契约。

## Managers 架构与职责边界

对外仍只暴露 `DungeonGameManager`。命令、菜单和监听器不直接依赖内部服务，避免内部拆分扩散到整个插件。内部组件均为包级可见，通过构造器注入同一局的 `DungeonSession` 和所需协作者：

| 组件 | 唯一职责 | 不应承担的职责 |
|---|---|---|
| `DungeonGameManager` | 对外 API、开始/结束/进入、生命周期编排 | 模板扫描、刷怪细节、奖励箱坐标搜索 |
| `DungeonSession` | 保存关卡、遭遇、传送门、怪物归属等运行时快照 | 调度任务或修改世界 |
| `DungeonGenerationService` | 分 tick 生成 15 层，选择完整环境，扫描并替换模板标记 | 开始游戏状态、传送玩家、生成怪物 |
| `DungeonProgressionService` | 串联 15 层、解锁精确传送点、切层与胜利回调 | 判断清房条件 |
| `DungeonEncounterService` | 房间状态机、封门/开门、怪物归属与清房结算 | 解析怪物配置或构造装备 |
| `DungeonWaveService` | 按难度启动波次、等待当前波清空、调度下一波 | 解析怪物文件或开门结算 |
| `DungeonMobRegistry` | 事务式加载和校验 `<关卡>_<环境>.yml` | 创建 Bukkit 实体 |
| `DungeonMobService` | 加权选怪、实体创建、属性/装备/PDC 应用 | 保存房间状态或调度波次 |
| `DungeonItemResolver` | 将原版/CraftEngine 物品 ID 转为 ItemStack | 选择怪物或配置掉落 |
| `DungeonLootChestService` | 寻找安全箱子坐标、写入战利品 PDC 上下文 | 决定房间是否已经清理 |
| `DungeonConfiguration` | 集中处理 Material 配置与默认值 | 保存可变游戏状态 |
| `Worldmanager` | 保留旧静态 API 的世界操作兼容门面 | 直接实现删除、粘贴或几何算法 |
| `DungeonWorldLifecycle` | 虚空世界创建、卸载及受保护删除 | 房间布局或 FAWE 粘贴 |
| `DungeonTemplatePlacement` | 房间/桥模板尺寸校验与 FAWE 粘贴 | 世界删除或门洞计算 |
| `DungeonRoomGeometry` | 布局格到绝对坐标的换算、门洞和边界 | 模板读取或游戏流程 |

关键依赖方向为：`DungeonGameManager → 各服务 → DungeonSession`。遭遇服务只通过 `DungeonProgressionService.activate` 解锁出口，推进服务通过回调通知 Manager 胜利，不反向持有 Manager，因此没有循环依赖。`RoomSpawner` 的布局表仍是生成期临时数据，每层生成后必须立即转换为 `DungeonSession` 中的绝对坐标快照。

## 主要文件

- `Dungeongame.java`：当前局全局设置和参与者。
- `DungeonDifficulty.java`：难度枚举和默认倍率。
- `SettingMenu.java`：设置、开始、结束菜单。
- `DungeonGameManager.java`：稳定的对外门面与一局游戏生命周期编排。
- `DungeonSession.java`：关卡、遭遇、传送门及怪物归属的运行时数据模型。
- `DungeonGenerationService.java`：15 层分批生成、环境选择、绝对坐标快照和原理图标记扫描。
- `DungeonProgressionService.java`：层级推进、精确传送点激活和胜利回调。
- `DungeonEncounterService.java`：房间战斗状态机、封门、清房和服务协作。
- `DungeonWaveService.java`：难度波次数量、下一波延时、生成任务取消。
- `DungeonMobRegistry.java` / `DungeonMobDefinition.java`：环境怪物文件解析、严格校验和不可变定义。
- `DungeonMobService.java`：加权选怪、实体生成、属性、装备、名称及 PDC 标签。
- `DungeonItemResolver.java`：原版材质与 CraftEngine 物品 ID 解析。
- `DungeonLootChestService.java`：安全位置搜索和战利品箱 PDC 写入。
- `DungeonConfiguration.java`：配置材质的统一容错读取。
- `DungeonRoomListener.java`：进入房间与怪物死亡事件。
- `CraftEngineMobEquipment.java`：发现完整像素护甲套装，随机装备四件套并附加难度属性。
- `DungeonLootContext.java`：未来战利品随机器的输入协议。
- `RoomSpawner.java`：网页原型布局算法。
- `Worldmanager.java`：兼容既有调用方的世界操作静态门面。
- `DungeonWorldLifecycle.java`：世界重建、安全卸载与目录删除校验。
- `DungeonTemplatePlacement.java`：模板尺寸校验、普通房/桥的 FAWE 粘贴。
- `DungeonRoomGeometry.java`：开孔、门洞绝对坐标、房间边界和出生点计算。
- `Roommanager.java`：FAWE 文件发现、命名解析和模板分类。
- `dungeongame.yml`：运行参数、波次间隔、像素护甲发现规则和难度倍率。
- `dungeongame/mobs/<关卡>_<环境>.yml`：对应原理图环境的小怪、精英、Boss、权重、属性和装备。

## 后续待接内容

- 根据 `DungeonLootContext` 实现分层、分房型的加权战利品池。
- 如需多人组队入口，可在进入逻辑外增加队伍确认；当前其他玩家可用 `/dungeongame enter` 加入。

## 维护记录

### 2026-09-09：Managers 解耦

- 将原先集中在 `DungeonGameManager` 的生成、遭遇、怪物、推进、战利品和运行时模型拆为七个包内组件；Manager 只保留稳定门面和生命周期编排。
- 将 `Worldmanager` 中的世界生命周期、模板粘贴和房间几何拆为三个包内组件；原静态字段、方法及 `RoomBounds`/`DoorBounds` 类型继续兼容旧调用方。
- 保留 `initialize/get/isRunningOrGenerating/status/startGame/endGame/enter/handlePlayerPosition/handleMonsterDeath` 对外 API，命令、菜单与监听器无需迁移。
- 保留本次工作区已有的精确 `RESPAWN_ANCHOR` 传送标记、无安全坐标时不强放箱子、CraftEngine 像素护甲自动发现和新默认怪物池行为。
- `Actionbarmanager`、`Bossbarmanager`、`Sidebarmanager` 在仓库内从未被引用，现已标记为废弃兼容类型；暂不删除，避免破坏可能存在的外部类型引用。
- 注释补充了每个组件的职责、状态流转、幂等清理、坐标快照和配置回退约束。

### 2026-09-09：难度波次与环境怪物包

- 房间战斗改为顺序波次：简单 2 波、普通 3 波、困难 4 波；一波全部消灭后等待配置延时再生成下一波，全部波次结束后才开门和结算。
- 新增 `plugins/mlcgames/dungeongame/mobs/<关卡>_<环境>.yml`，环境选择现在要求原理图模板和同名怪物包同时完整。
- 怪物包支持 normal/elite/boss 加权池、Bukkit 实体类型、MiniMessage 名称、生命、护甲、韧性、攻击、移速、击退抗性、跟随距离和难度缩放开关。
- 六个装备槽支持原版 Material 与 CraftEngine ID，可配置统一掉落率或继续使用自动像素护甲。
- `/dungeongame reload` 同时事务式重载所有环境怪物文件；当前局使用生成时快照，不受中途 reload 影响。
- 怪物实体新增 session、encounter、definition 三个 PDC 标识；结束游戏会取消所有待生成波次，防止旧任务越局执行。
- 插件关闭时会主动结束当前地牢、取消待生成波次并移除受控怪物，避免持久化实体在 reload 后失去内存归属。
