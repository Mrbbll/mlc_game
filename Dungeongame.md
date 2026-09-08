# Dungeongame 开发与对接说明

最后维护日期：2026-09-08

## 当前完整流程

1. 玩家执行 `/dungeongame` 打开设置菜单。
2. 菜单可调整难度、地牢大小（普通房间目标数量）和特殊房间比例。
3. 点击“开启游戏”后，旧的 `Dungeongame` 世界会先被卸载并删除，然后创建新的虚空世界。
4. 系统在同一个世界中依次生成 3 关（文件名的 `floor=1-3`），每关 5 层，共 15 个独立布局。每关会从该 floor 可用的 `set` 中随机选择一个环境预设，并让本关 5 层共用该预设。每次只生成一层，避免把全部工作压在同一个服务器 tick。
5. `RoomSpawner` 按网页原型规划房间和桥，`Worldmanager` 粘贴 FAWE 原理图并在连接墙面开 8x8 的孔；所有房间的绝对边界和门洞会立即保存，不依赖之后被覆盖的静态布局表。
6. 所有房间粘贴结束后，扫描房间中的刷怪标记并把标记替换为普通方块。
7. 玩家第一次进入含有标记的房间时，房间所有连接口被封闭，并在标记上方生成对应怪物。
8. 该房间的所有受控怪物死亡后，连接口重新打开，并在房间内生成一个空战利品箱。
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
- `/dungeongame reload`：重新读取 `dungeongame.yml` 和 FAWE 模板。
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

正式游戏会为每一关随机选择一个可用 set，该关内部 5 层复用同一个模板池。因此不需要也不会读取 `4_*.schem` 或 `5_*.schem`。每个可被选中的 `floor + set` 必须同时具有 Start、Normal、特殊房、End、Boss、X 桥和 Z 桥模板：内部第 1-4 层使用 End，第 5 层使用 Boss。Boss 原理图必须至少包含一个 `CREAKING_HEART`。

尺寸必须遵循网页原型：

- 普通、特殊、开始、结束房间：2x2 区块，即 32x32 方块。
- Boss 房间：3x3 区块，即 48x48 方块。
- X/Z 桥：1x1 区块，即 16x16 方块。
- 高度不限制。粘贴时会自动查找每个原理图最低的非空气方块，并把所有模板的最低层对齐到 `layout.origin.y`。

## 15 个地牢的空间与推进顺序

所有布局都位于同一个 `Dungeongame` 世界。每关的 5 层沿 X 轴排列，3 关沿 Z 轴排列。间隔由 `layout.stage-spacing.x/z` 控制，最小强制为 1600 方块，默认 2048，避免随机布局互相覆盖。

固定推进链为：

`1-1 → 1-2 → 1-3 → 1-4 → 1-5 Boss → 2-1 → … → 2-5 Boss → 3-1 → … → 3-5 Boss → 胜利`

传送点使用 `progression.portal-block`，默认 `RESPAWN_ANCHOR`。这只是插件控制的安全标记，不使用会被原版逻辑抢先处理的 `END_PORTAL`。玩家进入传送点 2.25 格范围后触发；普通 End 房没有怪物标记时在全部布局生成完成后直接出现，如果 End 房含怪物标记则必须清房后出现，Boss 房永远必须清 Boss 后出现。

胜利时 `Dungeongame.lastGameWon=true`，`Dungeongame.lastWinner` 保存触发最终传送点的玩家 UUID，随后执行正常结束清理。

每提升一关，所有怪物的生命和攻击伤害会累计乘以 `progression.level-stat-multiplier`，默认值为 1.5。因此第 1 关为 ×1.0、第 2 关为 ×1.5、第 3 关为 ×2.25。该倍率会与菜单难度倍率、普通/精英/Boss 类型倍率继续相乘。

## 刷怪标记与房间战斗

直接在房间原理图中放置以下原版方块即可，无需为每个房间手写坐标：

| 原理图方块 | 作用 |
|---|---|
| `SPAWNER`（刷怪笼） | 在上方随机生成一只普通怪物 |
| `TRIAL_SPAWNER`（试炼刷怪笼） | 在上方随机生成一只精英怪物 |
| `CREAKING_HEART`（嘎吱核心） | 在上方生成随机 Boss |

一个房间可以放多个标记，每个标记生成一只怪物。扫描后标记会替换为 `markers.replacement`。怪物列表依次查找 `mobs.level-L.floor-N.<normal|elite|boss>`、`mobs.floor-N.*`、`mobs.default.*`，最后才使用代码内置列表。

房门封闭使用 `encounters.closed-door-block`。封闭范围与生成时开的孔完全一致，即区块中心对称的 8 格宽、8 格高开口；具体宽高仍由 `passage` 配置控制。

核心状态管理类：`dungeongame/managers/DungeonGameManager.java`。

## 难度与 CraftEngine 装备

难度同时影响：

- 怪物最大生命和当前生命。
- 怪物基础攻击伤害。
- 怪物头盔、胸甲所使用的 CraftEngine 自定义物品 ID。

倍率与物品 ID 位于 `dungeongame.yml` 的 `difficulty.easy|normal|hard` 下。必须把 `armor.helmet` 和 `armor.chestplate` 换成服务器中真实存在的 CraftEngine namespaced ID，例如 `namespace:item_id`。物品属性本身由 CraftEngine 物品定义负责；让三个难度引用三组不同定义即可改变护甲属性。当前仓库不知道服务器现有物品 ID，因此配置默认留空，不会虚构无效 ID。

生成出的装备掉落概率被设为 0。CraftEngine 未安装、ID 不存在或 API 构造失败时，只记录一次警告，避免破坏整场房间战斗。

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

推荐后续战利品随机器接受 `DungeonLootContext(dungeonLevel, floor, roomType, qualityTier)`，再按 `level -> floor -> roomType -> weighted entries` 查池。上下文定义在 `dungeongame/loot/DungeonLootContext.java`，只需替换 `DungeonGameManager.spawnLootChest` 中清空箱子后的填充部分，不要改动 PDC 契约。

## 主要文件

- `Dungeongame.java`：当前局全局设置和参与者。
- `DungeonDifficulty.java`：难度枚举和默认倍率。
- `SettingMenu.java`：设置、开始、结束菜单。
- `DungeonGameManager.java`：15 层分批生成、绝对坐标快照、推进传送、胜利、房间遭遇、怪物归属、清房和箱子生成。
- `DungeonRoomListener.java`：进入房间与怪物死亡事件。
- `CraftEngineMobEquipment.java`：按难度构造并装备 CE 物品。
- `DungeonLootContext.java`：未来战利品随机器的输入协议。
- `RoomSpawner.java`：网页原型布局算法。
- `Worldmanager.java`：世界重建、模板粘贴、开孔、门洞绝对坐标快照和封门。
- `Roommanager.java`：FAWE 文件发现、命名解析和模板分类。
- `dungeongame.yml`：运行参数、怪物表和装备 ID。

## 后续待接内容

- 实际 CraftEngine 头盔/胸甲 ID 和三档属性定义。
- 根据 `DungeonLootContext` 实现分层、分房型的加权战利品池。
- 如需多人组队入口，可在进入逻辑外增加队伍确认；当前其他玩家可用 `/dungeongame enter` 加入。
