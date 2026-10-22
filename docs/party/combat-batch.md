# 战斗玩法迁移记录

以下代码为新派对模块，未修改旧游戏。不要把“文件已创建”当作验证完成；所有玩法仍需补地图后联机测试。

| ID | category | class | 原源码 |
|---|---|---|---|
| duel | combat | com.mlc.mlcgames.party.combat.duel.DuelGame | map_all/duel + fix:duel/thing |
| battle | combat | com.mlc.mlcgames.party.combat.battle.BattleGame | map_all/battle |
| tntwars | combat | com.mlc.mlcgames.party.combat.tntwars.TntWarsGame | map_all/tntwars |
| cloud | combat | com.mlc.mlcgames.party.combat.cloud.CloudGame | map_game_2nd/cloud，四供给模式 |
| bedwars | combat | com.mlc.mlcgames.party.combat.bedwars.BedWarsGame | map_game_2nd/bedwars，八规则模式、七布局模板 |
| hunger | combat | com.mlc.mlcgames.party.combat.hunger.HungerGame | map_all/hunger |
| surgame | combat | com.mlc.mlcgames.party.combat.surgame.SurvivalGame | map_surgame/surgame，竞赛/感染 |
| pillar | combat | com.mlc.mlcgames.party.combat.pillar.PillarGame | map_game_2nd/pillar |

## 核心适配与源码缺失补全

- 原生容器/库存/掉落/副手操作使用默认拒绝、单游戏显式放行的回调；场地快照保存TileState的完整NBT，包含容器物品。
- 归属本局的爆炸会逐块记录恢复数据，再交给本局处理；未归属爆炸不能获得地图写权限。TNT弓通过 EntityShootBowEvent 让箭携带 80 tick TNT。
- 原生死亡可由游戏拦截并切换为本局淘汰或重生流程，避免插件全局监听器吞掉各游戏规则。
- 用户已同意补全空白的 `pillar/give_item`：每 2 秒按原 `lucky_pillar/demo` 战利品表给每位存活者随机物品。
- 用户已同意按字段意图解析旧格式 loot `condition` 和 `modifier`；NativeLoot 处理源权重、条件、数量、药水、附魔和组件。

## 已核对的规则

- 战桥：黄蓝两队，5球获胜，每球全员重置与5秒笼子，岩浆重生；原武器/四色4组陶瓦/4箭/3金苹果；胜队3分。新地图red-cage/blue-cage配置为门面板，禁止写实心包围出生点。no-build-*支持多片禁止建造区域。
- Battle Box：奇数随机轮空一名；每队每种职业名额ceil(总人数/8)；60秒选择（右键快捷栏5-8），5秒笼子；中间9格羊毛抢占/60秒比较，灭队本身不直接结束回合；先3胜，获胜2分。两随机药水点对应原8种效果，10秒持续。先恢复基线后下一轮。
- TNTWars：10秒发原tnttool2权重物资；Minecart连发3个、普通/远程TNT、苦力怕、火球、三种10秒雨、TNT箭；伤害抵抗25，靠虚空淘汰，胜队2分。
- 云端：保留normal/tnt/hunger/potion四种原权重物资；首次120秒后补给，随后3次240秒补给，最后60秒平局；存活到最后3分。源码comment与流程不一致时按实际流程。
- 饥饿：60秒保护；PVP启用后60秒、再60秒两次刷新；再60秒进入决战区，5秒准备，180秒决战；结束时全部仍存活者各3分。源码注释“5分钟”与实际60秒不同，保留实际命令值。
- 生存：3分钟后允许PVP或随机一人感染；感染模式救援min(2400,600+人数*90)秒，最后120秒人类发光；僵尸被击杀可再生，人类仅被感染者击杀才转感染；普通意外死亡淘汰；获胜方2分。原joinzom不给武器，保留原冒险模式空装备设定。
- 幸运柱：原哭泣黑曜石柱，随机1..13或指定1..14地板，11x11双层地板；人数时长300/600/900/1200秒；按剩余时间加快基岩墙缩圈；普通/超时存活者3分。
- 起床战争：普通、无床、解锁、经验积分、双倍资源、职业、僵尸波次、永久床八模式；2/4队配置。保留床复活、工具等级、商店与升级、陷阱、羽毛回城、末期凋灵及僵尸波次。经验模式的资源收益与价格统一为 1/20/300 积分。七个源布局对应七个独立 arena 模板，坐标留空供新地图填写；原数据包大厅投票选择器未移植，布局由 join arena 参数指定，规则模式由 game.yml 固定。

## 数据实现

NativeLoot原生解释源JSON池（非命令执行），包含原权重、标签逐物品展开、随机数量、概率条件、药水、附魔及完整components。原生强度附魔使用Bukkit ItemFactory。原版战利品引用使用Bukkit自带LootTable。幸运加成保持原无luck基值，bonus_rolls不凭空增加抽数。

NativeEquipment只解析原物品参数，用于保留职业装备属性、冷却、消耗行为，不执行源函数命令。
