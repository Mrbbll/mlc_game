# 已接入游戏配置与操作

当前注册了 52 个小游戏入口：保留 19 个既有小游戏实现，新增 33 个独立玩法；全部入口均有自己的逻辑目录和 `language.yml`、`locations.yml`、`game.yml`。新地图由服主提供，所有地图角点和特殊点位默认留空，填写前不能开局。桌游、棋类、PVE 地牢和已确认废案不在入口中。

## 安装与配置

构建产物为 `build/libs/mlcgames-1.0-SNAPSHOT-all.jar`。仍需要原插件已有的依赖（包括 FastAsyncWorldEdit）；本次没有另建插件。

插件首次加载会在其数据目录生成 `party/<分类>/<游戏 ID>/`。每个游戏都有三份配置：

- `language.yml`：玩法介绍、消息、道具名称。用 `&` 颜色代码；保留 `{player}`、`{seconds}` 等原有占位符。
- `locations.yml`：地图世界、地图角点和游戏特殊点位。
- `game.yml`：人数、时间、模式、地板材料和其他规则。

默认 `configured: false`，坐标是 `null`。需要填写真实新地图坐标，再设为 true；仅改开关不会绕过校验。

## 地图角点

`maps.arena_1.bounds.corner-1` 与 `corner-2` 是比赛区域三维包围盒的两个对角方块坐标。可反向选择角点，程序会归一化。区域应覆盖玩家所有正常跳跃、摔落和游戏活动空间；越界会淘汰。不要仅将 bounds 填成地板的一层。

每个 `regions.<名称>` 也使用两个角点，且必须位于 bounds 内。floor 表示本游戏可破坏或消失的地板区域；多层 SNOW 场地可以用一个立方区域包住各层地板及中间空气，程序仅破坏 game.yml 中列出的地板材质。

`points.waiting` 和 `points.exit` 必须位于 bounds 外，避免准备与恢复时把玩家困在场地里。`points.spectator` 设置为安全的观战位置。它们与比赛区域使用同一个 world。

`points.spawns` 是玩家出生点列表，至少提供 `game.yml` 中 players.max 个独立方块位置，可使用小数坐标居中：

```yaml
spawns:
  - {x: 12.5, y: 65, z: 20.5, yaw: 90, pitch: 0}
  - {x: 16.5, y: 65, z: 20.5, yaw: -90, pitch: 0}
```

以上坐标只是格式示例，不是原地图或你的新地图坐标。出生点应位于 bounds 内，留有足够站立空间，地板位于脚下一格。

击退大赛至少配置 `regions.launch-pad`；可增加 `launch-pad-2` 等区域。角点填写弹射垫方块本身的位置，玩家踩上这些区域获得漂浮弹射。

Wool Wars 还必须填写 `regions.build`（允许放置区）、其内部的单层 `regions.target`（默认玩法为 3×3），以及 `points.team-spawns.red`、`blue` 两个列表。每队列表数量至少为 players.max 的一半向上取整。普通 spawns 列表同样保留，用作公共会话出生流程。

## 命令

第二批特殊点位补充：

- `finder`：`points.groups.eggs` 是彩蛋底部生成点列表，按人数随机选其中一部分。
- `elytra`：`regions.gate` 是开局封闭、开始后移除的起跑门；`regions.finish` 填写绿宝石终点地板。
- `mutouren`：`regions.safe` 要包含全部出生点，位于此区内不作冻结违规判定；`points.groups.judge` 恰好一个裁判点。还需填写 gate 和 finish。
- `parkourrace`：`points.groups.checkpoints` 是站在金块上时的返回位置，Y 为金块上方一格。效果垫分别配置 speed-pad、jump-pad、lift-pad、slow-pad、blind-pad、clear-pad；填垫子方块的角点。可添加同前缀加 `-2` 的区域；若新地图有意取消某类垫子，也要同时从 game.yml 的 pad-effects 删除该项。
- `chair`：`points.groups.chairs` 是矿车候选生成点，必须在 cart-travel 内，数量至少达到最大人数所需椅子数；默认 16 人需要 10 个。cart-travel 填矿车回绕边界，bounds 在其外再留安全余量及乘客高度，场地须有可行驶地面。

上述 points.groups 下的值都是坐标列表，格式与 spawns 相同；保持空列表时会拒绝开局。

`bedwars`（起床战争，combat）：

- `game.yml` 的 `mode` 每次选择一种规则：`normal`、`no-beds`、`unlocked`、`xp`、`double-resources`、`class`、`zombie-wave`、`permanent-bed`；`team-count` 只能为 2 或 4。源地图布局用 `/party join bedwars <arena>` 选择，七个 arena 名称见 `source-maps`，规则模式由配置指定。
- 在 `locations.yml` 中把要用的源地图模板复制成单独配置，再填写 `world`、`bounds`、`regions.build`、各队出生点和床、铁/金/钻石/绿宝石生成点及商店点。所有坐标目前为空；床必须按对应队伍配置，`zombie-wave` 还需要四队床点及安全生成区域，凋灵事件使用 `points.groups.wither-spawn`。
- 经验模式统一使用经验积分：资源收益、商店价格和扣款均按 1/20/300 积分档配置，不使用 Minecraft 经验等级作为另一套货币。升级、陷阱、特殊物品和事件时间均可在 `game.yml` 调整。

`golf`（高尔夫，sports）：

- `points.groups.course-starts`、`ball-starts` 各填写恰好 3 个点，按球场 1、2、3 顺序排列，分别为玩家击球起点和球实体脚底生成点。
- `regions.course-1` 到 `course-3` 填对应球场的三维包围盒，包含球的正常腾空空间；`course-1-goal` 到 `course-3-goal` 填各球场铜块目标地板的两个角点。起点、球位和目标区域均须在对应球场内。球越过对应球场边界则重新放球。
- 整体 bounds 要容纳三个球场和旁观点，**高尔夫 spectator 必须在 bounds 内**。等待区、返回点仍在 bounds 外。只填球场地板一层会让正常击球被判越界。
- `play-all-courses: true` 轮流打完三个球场；设为 false 时只打 `selected-course`（1–3）。即使只选一个，三球场的点位配置仍须完整填写。
- 每位玩家每个球场最多 421 秒；每次进洞为 `5000 - 已用能量`，总分最高者获得 1 点本局派对积分，允许并列。按源码，已用能量跨球场保留，只有开局和开启 `reset-energy-on-retry` 后的重来会清零。六档球杆消耗分别为 5/10/15/20/25/30。
- 六个球杆占热键栏 1–6；7 为返回球场起点，8 为重新放球并返回起点，9 为放弃本轮，按住右键喝完一秒药水后生效。总时限默认 21600 秒，以容纳 16 人三球场轮流击球；这是新会话的可配置安全时限。

`fishing`（钓鱼达人，arcade）：填写 `regions.fishing-water` 的两个角点，包住允许垂钓的水体和鱼钩浮在水面的空间，至少覆盖水面上方一格。此区必须在 bounds 内，出生点应设置在岸边；仅在区内成功上钩才会兑换金币。不会自动生成池塘或改写生物群系。竹子只有鱼钩所在位置属于原版 jungle、sparse_jungle、bamboo_jungle 时才进入垃圾抽取池；其他生物群系仍可正常钓鱼。

默认 6 秒准备，计时器从 91 开始（首秒后显示 90），每 10 秒播报排名。原生饵钓 III 钓竿可重复使用，成功上钩后立即兑换金币；基础权重垃圾 10、宝藏 5、鱼类 85，宝藏先作额外 80% 判定，判定失败则本次仅在鱼类和垃圾中抽取。具体鱼获权重、22 种兑换价格和前三名 3/2/1 分均在 game.yml。无须安装原地图数据包；本模块只处理参赛者自己的鱼钩和鱼获，不能用钓竿拉动其他玩家或实体。

`miner`（一掘到底，arcade）：

- 填写 `points.groups.pillars`，每个点是矿柱中央**底部基岩**的整数方块坐标，不是玩家出生位置。至少提供 players.max 个点，使用前 players.max 个候选点，每局按实际人数取前 N 个，随机分配玩家。
- 默认随机层数 96。程序会生成每根 3×3、总高 102 格的屏障外壳：底部为所填基岩层，其上 96 层随机矿柱，再上 4 格空气和 1 格屏障屋顶。玩家出生在底部 Y+97，朝下看。整个外壳必须位于 bounds 内且彼此不重叠，中心横向至少相隔 3 格。建议留更宽间距方便观战。
- `regions.finish` 的两个角点要包住所有矿柱的基岩底部；默认只检测 BEDROCK，玩家踩到底部完赛。bounds 必须覆盖完整生成外壳，不能只填矿柱中心或一层地板。
- 所有候选矿井外壳都将纳入开局快照，因此这些空间不要放置箱子、告示牌等已有方块实体；预留空气空间最便于建图。生成的炉子等随机方块会随本局恢复移除，不能打开取物。
- 默认 600 秒，完赛积分 4/3/2/1；第 4 位完赛后剩余时间最多 30 秒。仅能破坏自己矿井中央随机层，屏障和基岩不可破坏，工具不损坏且每秒补齐。
- `block-palette` 中 236 个有效条目保留了源码重复材质，因此不要自动去重；编号 62 已按确认排除并重新抽取。每局只抽取一份序列，所有玩家使用同一份。

`car_race`（赛猪，racing）：

- `points.spawns` 是各玩家等候召唤坐骑的位置，仍至少配置 players.max 个独立点。开局记录起跑门后，玩家会先传到这些点，手持矿车按交换副手键召唤带鞍的猪；默认键为 F。全员上车后才开始 6 秒倒计时。上车准备默认限 120 秒，超时取消并恢复。
- 填写 `regions.gate` 的角点，程序先放置金合欢栅栏、开赛时移除，结束恢复。门区体积不得大于 blocks-per-tick。出生点和圈末返回点不能与门区相交。
- `regions.finish` 填红色羊毛终点地板角点，`points.groups.lap-return` 恰好一个坐骑返回点（填写猪脚底坐标，可设朝向）。每次猪踩到终点就计一圈并返回此点；返回点脚下不能落在 finish 内。第三圈完赛。此玩法沿用原图的终点触发与返回机制，没有额外增加检查点，赛道应通过地形阻止抄近路。
- `regions.launch-pad` 填黄绿色带釉陶瓦弹射地面角点；`regions.slow-pad` 填荧石减速地面角点。猪获得 1 秒漂浮 V，荧石在速度至少为 10 时扣 5。地图总 bounds 要包含全部赛道和弹射腾空空间。
- 前进每 tick 加 6，后退先扣 5，跳跃急停；应用当前速度后每 tick 扣 3 阻力。100 是加速判断阈值，保留原代码一次加速可超过它的规则。视角和原生骑乘控制方向，胡萝卜钓竿放在副手。
- 下车后自动寻找 5 格内的空闲本局坐骑，找不到转旁观。修正后的完赛积分为 3/2/2/1/1；第五人完赛或剩余参赛者不超过一人时结算，未完赛者不补发完赛分。默认 600 秒比赛上限用于防止无法结束，原图没有固定比赛时限。

第三批特殊区域补充：

- `poolwar`（冬泳怪鸽，sports）：配置 `regions.pool` 的两个角点，包住所有可被冰霜行者冻结的泳池水面与水体；该区域会在开局记录、结束恢复，不能放置箱子等方块实体。bounds 还需包含岸边休息平台与跳跃空间。出生点设置在合适的岸边或水面平台。鞋子结冰只允许发生在 pool 内，区外水面不会因参赛者结冰。
- `dropper`（星跳水立方，racing）：`points.groups.level-starts` 必须按第 1 到第 10 关顺序填写恰好 10 个起点；`regions.level-1-portal` 到 `level-10-portal` 分别是各关通关传送门的包围盒。玩家身体进入区域且所在方块匹配 portal-materials 才过关，不能用终点地板角点代替。每个起点不得位于本关传送门内。bounds 需要同时包含十关与全部下落空间。此玩法不发派对竞速积分。

```text
/party list
/party join <游戏 ID> [地图名称]
/party leave
/party check <游戏 ID> [地图名称]
/party start
/party stop
/party reload
```

普通玩家拥有 `mlcgames.party.play`。check、start、stop、reload 需要 `mlcgames.party.admin`，默认 OP。start 为当前等待队列开局，stop 强制清理；目前不是自动轮换派对。

未填写地图名称时使用 `arena_1`。目前一次只运行一个新游戏会话；在同一队列加入时使用不带地图名称的 join 命令。地图可在 maps 下配置多份供开局选择。

游戏进行时使用本局配置快照。先 stop，等待恢复结束后再 reload，避免一局途中规则或点位变动。

## 第一次实测

每个小游戏按自己的 `game.yml` 最低人数准备玩家。先使用 check 校验点位，再 join，由管理员 start。分别检查正常获胜、淘汰、leave、掉线和 stop；检查原物品恢复、返回点、场地复原和控制台错误。Wool Wars 还需要验证完整多回合。

请以 IMPLEMENTATION.md 的验证边界为准。Java 构建和静态配置检查不替代新地图联机验收。
