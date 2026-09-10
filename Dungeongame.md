# Dungeongame 开发与对接说明

最后维护日期：2026-09-11

维护约定：凡是修改地牢流程、配置契约、原理图规范或 managers 内部架构，都必须同步更新本文对应章节，并在文末“维护记录”说明日期、改动内容和兼容性影响。

## 当前完整流程

1. `dungeongame_prepareteam` 或 `dungeongame_team` 的玩家执行 `/dungeongame` 打开设置菜单；其他玩家会被拒绝。
2. 菜单可调整难度、地牢大小（普通房间目标数量）和特殊房间比例。
3. 点击“开启游戏”后，`dungeongame_prepareteam` 的全部玩家条目（包括暂时离线的成员）会迁移到 `dungeongame_team`；旧的 `Dungeongame` 世界随后被卸载并删除，再创建新的虚空世界。
4. 系统在同一个世界中依次生成 3 关（文件名的 `floor=1-3`），每关 5 层，共 15 个独立布局。每关会从原理图完整且存在同名怪物包的 `set` 中随机选择一个环境预设，并让本关 5 层共用该预设。每次只生成一层，避免把全部工作压在同一个服务器 tick。
5. `RoomSpawner` 按网页原型规划房间和桥，`Worldmanager` 粘贴 FAWE 原理图并在连接墙面开 8x8 的孔；所有房间的绝对边界和门洞会立即保存，不依赖之后被覆盖的静态布局表。
6. 所有房间粘贴结束后，扫描房间中的刷怪标记并把标记替换为普通方块。
7. 生成完成后，`dungeongame_team` 中所有在线成员会一起进入第一层。任一队员第一次进入含有标记的房间时，系统先把其他在线队员强制集结到触发者附近，再封闭房间所有连接口并生成怪物。普通与特殊战斗房在每个标记上方生成第一波怪物；简单、普通、困难分别有 2、3、4 波。Boss 房不启用波次，只生成一次。
8. 普通与特殊房的当前波全部消灭后等待 `encounters.next-wave-delay-ticks` 再生成下一波；全部波次结束后才重新开门并生成空战利品箱。Boss 房唯一 Boss 被消灭后直接开门、解锁传送点并结算。
9. 每关第 1-4 层的 End 房生成传送点并通往本关下一层；第 5 层 Boss 清除后生成传送点并通往下一关第 1 层。
10. 地牢队员死亡后会自动重生为旁观者。仍有在线、存活且已经进入本局的队友时，阵亡者等待队友进入下一层并在新层复活；最后一名可战斗成员死亡时，5 秒后判定团灭并结束游戏。
11. 第 3 关第 5 层 Boss 清除后生成最终传送点，玩家靠近后设置胜利状态并结束游戏。
12. 点击“结束游戏”或执行 `/dungeongame end` 会取消仍在进行的生成任务、移除受控怪物、恢复旁观中的玩家、打开封闭房门并把参与者送回其他世界。

Purpur 新版可能把该世界存储为主世界下的 `dimensions/minecraft/dungeongame`，Linux 上目录名也会转为小写。删除校验已同时兼容传统 `Dungeongame` 目录和 Bukkit 返回的新式维度目录，但仍只允许删除末级名称为 `dungeongame` 的精确目标，避免误删其他世界。

## 菜单与命令

设置菜单类：`dungeongame/menus/SettingMenu.java`

- 难度：简单、普通、困难；左键向后切换，右键向前切换。
- 地牢大小：显示名为“地牢大小”，实际值是普通房间目标数量，范围 4-30。
- 特殊房间比例：0%-100%，步长 10%。网页算法仍保证至少尝试放置两个特殊房间，所以 0% 不代表绝对没有特殊房间。
- 菜单准入：仅 `dungeongame_prepareteam` 与 `dungeongame_team` 成员可以打开和操作；打开后离队也会在点击时被再次拦截。
- 游戏运行期间不允许更改上述设置。
- 开启游戏：把准备队全员迁移到正式队，重建世界并分 15 次生成全部 3×5 个布局，完成后把正式队所有在线成员送入第 1 关第 1 层。
- 结束游戏：清理当前一局。

命令：

- `/dungeongame` 或 `/dungeongame menu`：准备队或正式队成员打开设置菜单。
- `/dungeongame start`：仅准备队或正式队成员可按当前设置开始；开始时迁移全部准备队条目。
- `/dungeongame end`：结束当前游戏。
- `/dungeongame enter`：仅允许 `dungeongame_team` 成员补进当前游戏并传送到起始房间；不会把非队员自动加入队伍。
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

正式游戏会为每一关随机选择一个可用 set，该关内部 5 层复用同一个模板池。因此不需要也不会读取 `4_*.schem` 或 `5_*.schem`。每个可被选中的 `floor + set` 必须同时具有 Start、Normal、特殊房、End、Boss、X 桥、Z 桥模板以及同名怪物包 `<floor>_<set>.yml`：内部第 1-4 层使用 End，第 5 层使用 Boss。Boss 原理图必须恰好包含一个 `CREAKING_HEART`，重复或缺失都会中止生成并报告数量。

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
| `CREAKING_HEART`（嘎吱核心） | Boss 房激活时从当前环境的 boss 池加权生成唯一 Boss |

普通与特殊房可以放多个标记；每个标记在每一波复用一次，因此 3 个标记、普通难度会依次生成 3 波、总计 9 只怪物。扫描后标记会替换为 `markers.replacement`。当前波全部受控怪物死亡前不会生成下一波，全部波次结束前不会开门或结算奖励。下一波等待时间由主配置 `encounters.next-wave-delay-ticks` 控制，默认 40 tick。

波次数量由难度枚举固定推导：简单 2 波、普通 3 波、困难 4 波，即难度每提高一级增加一波。此规则只用于非 Boss 房。Boss 房固定执行一次生成且不显示 `1/1 波` 提示；原理图只能放一个 `CREAKING_HEART`，所以整场只会生成一个 Boss。Boss 房如果额外放置普通或精英标记，它们会作为同一次决战的随从生成，不会开启后续波次。

房门封闭使用 `encounters.closed-door-block`。封闭范围与生成时开的孔完全一致，即区块中心对称的 8 格宽、8 格高开口；具体宽高仍由 `passage` 配置控制。

核心状态管理类：`dungeongame/managers/DungeonGameManager.java`。

## 多人队伍与房间集结

地牢运行期的准入来源固定为主记分板队伍 `dungeongame_team`，等待开局的玩家放在 `dungeongame_prepareteam`。设置菜单和开始命令接受这两个队伍的成员；正式开始时 `DungeonPartyService.promotePreparedPlayers()` 使用条目快照把准备队全部迁移到正式队，包括当前离线但仍保存在记分板中的玩家名。非队员不会被开始命令自动收编，即使被其他方式传送进地牢世界，也不能触发房间、使用推进传送点或完成胜利流程。

生成完成时会查询一次全部在线队员并传送到第一层。此后每次 `WAITING` 状态的战斗房首次被任一队员触发时，会重新实时查询队伍，因此开局后才上线或才加入队伍的玩家也能在下一场新遭遇时被拉入。离线成员保留队伍资格但无法传送；可在上线后使用 `/dungeongame enter` 补进当前局。

集结顺序严格为：`房间状态切换为 ACTIVE → 传送队员 → 关门 → 生成第一波`。先切换状态用于阻止 `PlayerTeleportEvent` 再次激活同一房间；先传送再关门则避免队员被屏障留在门外。已在目标房间内的队员保持原位，其他在线队员优先分散到触发者附近的安全方块；没有可用偏移位置时回退到触发者坐标。所有实际进入过本局的玩家都会保留在 `Dungeongame.participants`，即使中途离队，结束游戏时仍会被安全送出即将卸载的地牢世界。

## 玩家死亡、旁观与跨层复活

`DungeonPlayerLifeService` 只接管同时满足以下条件的死亡：地牢已经运行、玩家位于地牢世界、属于 `dungeongame_team`，并且已经记录在本局 `Dungeongame.participants`。普通世界、生成期间以及其他游戏模块的玩家死亡不受影响。

死亡发生时立即把玩家加入等待复活集合，并在下一 tick 自动执行 Bukkit 重生。重生位置固定为死亡位置，重生事件结束后切换为自由 `SPECTATOR`，不会自动选择或锁定任何队友作为旁观目标。等待复活的旁观者可以自由飞行观察，但不能触发房间、层级传送点或胜利。“存活队友”只用于判断等待复活还是团灭，必须同时满足：在线、正式队成员、已进入本局、仍在地牢世界、未死亡、未等待复活且不是旁观模式。

当任一存活队员使用层级传送点进入下一层，推进回调会恢复全部在线等待者为 `SURVIVAL`，传送到新层起点，并在下一 tick 继续执行房间检测。若复活传送被其他插件取消，该玩家继续保持旁观等待状态。最后一名可战斗成员死亡时启动固定 100 tick（5 秒）的团灭任务；到期后恢复旁观者、把全体参与者送出地牢并结束本局。手动结束、生成失败或插件关闭都会取消尚未执行的团灭任务。

## 地牢监听器生命周期

`SettingMenu` 是唯一在插件启动时常驻注册的地牢监听器，用来处理菜单点击和拖动。移动检测、怪物死亡、玩家死亡与玩家重生统一由 `DungeonRoomListener` 处理；它只在合法玩家执行地牢开始后由 `DungeonGameManager` 动态注册，并在手动结束、团灭、胜利、生成失败或插件关闭时立即通过 `HandlerList.unregisterAll` 注销。撤离传送和怪物清理发生在注销之后，不会意外再次触发推进逻辑。

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
```

字段约定：

- `category`：必须是 `normal`、`elite`、`boss`，决定使用哪种原理图标记。
- `entity-type`：Bukkit 生物类型，例如 `ZOMBIE`、`SKELETON`、`PIGLIN_BRUTE`；必须是可存活实体。
- `weight`：同 category 内的相对随机权重，必须至少为 1。
- `name`：MiniMessage 格式显示名；留空则使用原版名称。
- `name-visible`：是否始终显示自定义名称，默认在 name 非空时开启。
- `scale-with-difficulty`：是否让基础生命和攻击继续乘菜单难度及关卡倍率，默认 `true`。
- `attributes`：可配置 `max-health`、`armor`、`armor-toughness`、`attack-damage`、`movement-speed`、`knockback-resistance`、`follow-range`；没有写的属性使用该实体的原版值。击退抗性范围为 0-1。
- `equipment`：支持六个装备槽。物品可写原版 `Material` 或 CraftEngine namespaced ID；空字符串会清空该槽。所有身上装备都被强制设为不可掉落，不提供单怪掉率字段。
- `auto-pixel-armor`：为 `true` 时先使用既有像素护甲自动发现逻辑，再用本段非空槽位覆盖；整个 `equipment` 段缺失时默认为 `true`，显式写出该段时默认为 `false`。

配置在启动及 `/dungeongame reload` 时一次性校验。文件名错误、YAML 无效、缺少三类池、未知实体/属性、非法数值都会拒绝本次 reload；只有所有文件均有效时才整体替换注册表。当前局保存的是不可变环境快照，因此 reload 不会改变已经开始的波次。

生成实体会写入 `mlcgames:dungeon_session_id`、`mlcgames:dungeon_encounter_id`、`mlcgames:dungeon_mob_id` 三个 PDC 标签，方便后续诊断、掉落扩展和异常实体追踪。

## 地牢怪物死亡掉落

只有登记在当前 `DungeonSession.monsterOwners` 中的受控怪物会使用地牢掉落规则。其 `EntityDeathEvent` 在 `HIGHEST` 优先级执行以下处理：

1. 清空原版实体掉落和事件中已有的全部装备掉落。
2. 把死亡经验设为 0。
3. 独立随机生成 0-3 的金币数量；结果大于 0 时通过 CraftEngine 构造对应数量的 `mlcgame:coin`。

金币数量在包含两端的 `[0, 3]` 范围内均匀随机，不受 Looting 或怪物类型影响；结果为 0 时不创建掉落物。如果 CraftEngine 不可用或 `mlcgame:coin` 未注册，本次死亡不会产生其他替代物，只会在控制台对该 ID 警告一次。非地牢怪物以及其他游戏模块的怪物保持原有掉落规则。

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

自动像素护甲的难度额外属性位于 `difficulty.easy|normal|hard.armor`：`attribute-bonus-per-piece` 是每件增加的护甲值，`toughness-bonus-per-piece` 是每件增加的护甲韧性。CraftEngine 物品本身已有的属性仍然保留。自动或显式配置的装备均不会掉落。

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
| `DungeonPartyService` | 准备队迁移、正式队准入、参与者同步与新房间整队集结 | 房间状态、刷怪或关卡生成 |
| `DungeonPlayerLifeService` | 玩家死亡、自动旁观、跨层复活和 5 秒团灭任务 | 怪物死亡、房间结算或队伍配置 |
| `DungeonSession` | 保存关卡、遭遇、传送门、怪物归属等运行时快照 | 调度任务或修改世界 |
| `DungeonGenerationService` | 分 tick 生成 15 层，选择完整环境，扫描并替换模板标记 | 开始游戏状态、传送玩家、生成怪物 |
| `DungeonProgressionService` | 串联 15 层、解锁精确传送点、切层与胜利回调 | 判断清房条件 |
| `DungeonEncounterService` | 房间状态机、封门/开门、怪物归属与清房结算 | 解析怪物配置或构造装备 |
| `DungeonWaveService` | 按难度启动波次、等待当前波清空、调度下一波 | 解析怪物文件或开门结算 |
| `DungeonMobRegistry` | 事务式加载和校验 `<关卡>_<环境>.yml` | 创建 Bukkit 实体 |
| `DungeonMobService` | 加权选怪、实体创建、属性/装备/PDC 应用 | 保存房间状态或调度波次 |
| `DungeonItemResolver` | 将原版/CraftEngine 物品 ID 转为 ItemStack | 选择怪物或配置掉落 |
| `DungeonMobDropService` | 清空地牢怪物原版掉落和经验，随机加入 0-3 个 `mlcgame:coin` | 推进波次或处理非地牢实体 |
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
- `DungeonPartyService.java`：地牢队伍准入、在线成员查询、参与者登记和房间集结传送。
- `DungeonPlayerLifeService.java`：玩家死亡等待、自动旁观、下一层复活和团灭延时结束。
- `DungeonSession.java`：关卡、遭遇、传送门及怪物归属的运行时数据模型。
- `DungeonGenerationService.java`：15 层分批生成、环境选择、绝对坐标快照和原理图标记扫描。
- `DungeonProgressionService.java`：层级推进、精确传送点激活和胜利回调。
- `DungeonEncounterService.java`：房间战斗状态机、封门、清房和服务协作。
- `DungeonWaveService.java`：难度波次数量、下一波延时、生成任务取消。
- `DungeonMobRegistry.java` / `DungeonMobDefinition.java`：环境怪物文件解析、严格校验和不可变定义。
- `DungeonMobService.java`：加权选怪、实体生成、属性、装备、名称及 PDC 标签。
- `DungeonItemResolver.java`：原版材质与 CraftEngine 物品 ID 解析。
- `DungeonMobDropService.java`：地牢怪物死亡掉落替换，只保留随机 0-3 个 CE 金币。
- `DungeonLootChestService.java`：安全位置搜索和战利品箱 PDC 写入。
- `DungeonConfiguration.java`：配置材质的统一容错读取。
- `DungeonRoomListener.java`：运行期动态注册的移动、怪物死亡、玩家死亡与重生事件入口。
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

### 2026-09-10：Boss 单次遭遇

- Boss 房不再使用难度波数，始终只执行一次怪物生成，也不显示 `1/1 波` ActionBar。
- Boss 原理图从“至少一个”收紧为“恰好一个” `CREAKING_HEART`，保证整场只生成一个 boss 池实体；其他普通/精英标记如存在，仅作为同场随从生成一次。

### 2026-09-10：统一怪物金币掉落

- 只有遭遇系统登记的地牢怪物会被接管掉落；普通世界和其他模块的实体不受影响。
- 地牢怪物死亡时清空原版物品、装备和经验掉落，仅生成 CraftEngine `mlcgame:coin`。
- 删除环境怪物文件中的 `equipment.drop-chance`，所有六个装备槽的掉落率由代码强制为 0。
- 死亡监听器改为传递完整 `EntityDeathEvent` 并在 `HIGHEST` 优先级替换掉落；UUID 入口仅为旧代码兼容保留。

### 2026-09-10：金币数量随机化

- `mlcgame:coin` 从固定 1 个改为每只受控怪物独立均匀随机 0-3 个；随机到 0 时不创建物品实体，Looting 不改变数量。

### 2026-09-10：多人队伍与新房间强制集结

- `dungeongame_team` 成为地牢运行期唯一准入来源；生成完成后传送全部在线队员，`/dungeongame enter` 仅供队员中途补进。
- 新增 `DungeonPartyService`，集中负责精确在线成员查询、参与者登记、起点传送和安全分散集结，避免记分板逻辑继续耦合进 Manager 与遭遇状态机。
- 任一队员首次进入尚未激活的战斗房时，其他在线队员会在关门和第一波刷怪之前被强制传送到触发者附近；已经在房间内的成员不会被重复移动。
- 房间先原子式切换为 `ACTIVE` 再执行集结，防止传送事件同步重入导致重复刷怪；非队员不能触发房间、推进层级或胜利。
- `Dungeongame.participants` 继续记录本局实际进入过的玩家，不因中途离队而删除，确保结束游戏时仍能统一撤离地牢世界。

### 2026-09-10：死亡复活、准备队迁移与动态监听

- 菜单与开始命令只接受 `dungeongame_prepareteam` 或 `dungeongame_team` 成员；菜单打开后还会在每次点击时重新校验权限。
- 开始游戏不再自动收编任意发起者，而是把 `dungeongame_prepareteam` 的全部在线/离线条目迁移到 `dungeongame_team`，随后整队参与本局。
- 新增 `DungeonPlayerLifeService`：有存活队友的阵亡者自动重生为旁观模式，并在任一队友进入下一层时恢复生存、传送到新层；最后一名可战斗成员死亡则在 5 秒后结束地牢。
- 等待复活的旁观者不会触发房间或层级推进；复活传送失败时继续保持旁观等待，不会以生存模式滞留旧层。
- 团灭延时会在正常结束、生成失败或插件关闭时取消；结束清理会恢复仍在等待的旁观者，避免把旁观状态带出地牢。
- `DungeonRoomListener` 从启动期常驻注册改为每局开始时动态注册，在手动结束、团灭、胜利、生成失败或插件关闭时注销；`SettingMenu` 保持常驻。

### 2026-09-11：阵亡者改为自由旁观

- 删除阵亡后自动选择队友并调用 `setSpectatorTarget(...)` 的镜头锁定逻辑，也不再把重生点改到队友身边。
- 阵亡者在死亡位置进入普通自由旁观模式；等待复活标记仍会阻止其触发怪物房、层级传送点和最终胜利，跨层复活及团灭规则不变。
