# 移植清单

> 本表自动生成；已实现条目由 progress.json 显式记录。待审查条目不能视为已完成。

轮换目录 49 个条目，排除道具躲猫猫和船战后保留 47 个候选玩法。桌游、棋类和 PVE 另行排除。分类是插件目录建议，不改变原玩法。

| 原 ID | 玩法 | 建议分类 | 源函数前缀 | 状态 |
| --- | --- | --- | --- | --- |
| 1 | 方块躲猫猫 | deduction | `minecraft:hideseek/` | 逻辑与三份配置已迁移；新地图点位待填写及实测；ID: hideseek |
| 2 | TNTRUN | survival | `minecraft:snow/` | 已迁移，待地图实测与轮换积分接入；ID: tntrun |
| 3 | 掘一死战 | survival | `minecraft:snow/` | 已迁移，待地图实测与轮换积分接入；ID: spleef |
| 4 | 击退大赛 | survival | `minecraft:repel/` | 已迁移击退玩法，待地图实测校准；ID: repel |
| 5 | 色盲派对 | reaction | `minecraft:color/` | 逻辑与三份配置已迁移；新地图点位待填写及实测；ID: color |
| 6 | 信仰方块 | survival | `minecraft:believer/` | 已迁移，待地图实测与轮换积分接入；ID: believer |
| 7 | 道具躲猫猫 | excluded | `minecraft:hide2/` | 排除：废案 |
| 8 | 战桥 | combat | `minecraft:duel/` | 战桥逻辑与三份配置已迁移；新地图点位待填写及实测；ID: duel |
| 9 | 烫手的山芋 | survival | `minecraft:hotpotever/` | 已迁移，待地图实测与轮换积分接入；ID: hotpotever |
| 10 | 高尔夫游戏 | sports | `minecraft:golf/` | 已迁移轮流击球、三球场与能量计分；待击球手感实测；ID: golf |
| 11 | TNTWARS | combat | `minecraft:tntwars/` | 逻辑与三份配置已迁移；新地图点位待填写及实测；ID: tntwars |
| 12 | 冬泳怪鸽 | sports | `minecraft:poolwar/` | 已迁移体力、冰霜行者与积分；待实机；ID: poolwar |
| 13 | 拆弹专家 | cooperation | `minecraft:bomb/` | 逻辑与三份配置已迁移；新地图点位待填写及实测；ID: bomb |
| 14 | 吸血鬼之夜 | survival | `minecraft:endless_wolf/` | 按有效源码迁移；不添加普通玩家击杀后的自杀惩罚；ID: endless_wolf |
| 15 | 起床战争 | combat | `minecraft:bedwars/` | 八种规则模式、2/4 队、七张地图模板、商店/升级/陷阱/特殊事件及经验积分模式已迁移；地图点位待新地图填写及实测；ID: bedwars |
| 16 | BATTLE BOX | combat | `minecraft:battle/` | Battle Box 逻辑与三份配置已迁移；新地图点位待填写及实测；ID: battle |
| 17 | 云端争霸 | combat | `minecraft:cloud/` | 旧战利品字段已按意图修复并迁移，待地图实测；ID: cloud |
| 18 | 饥饿游戏 | combat | `minecraft:hunger/` | 逻辑与三份配置已迁移；新地图点位待填写及实测；ID: hunger |
| 19 | 生存游戏 | combat | `surgame:` | 逻辑与三份配置已迁移；新地图点位待填写及实测；ID: surgame |
| 21 | 密室杀手 | deduction | `minecraft:killerever/` | 逻辑与三份配置已迁移；新地图点位待填写及实测；ID: killerever |
| 22 | 幸运之柱 | combat | `minecraft:pillar/` | 已补全原空缺的定时随机发物品逻辑；待地图实测；ID: pillar |
| 23 | 贪吃蛇 | arcade | `minecraft:sneak/` | 贪吃蛇逻辑与三份配置已迁移；新地图点位待填写及实测；ID: sneak |
| 24 | 船战 | excluded | `minecraft:boatwars/` | 排除：废案 |
| 25 | 赛猪 | racing | `minecraft:car_race/` | 三圈赛猪已迁移；获胜积分修复为 3/2/2/1/1；ID: car_race |
| 26 | 烽火燎原丶占点模式 | combat | `minecraft:blaze/` | 占点模式逻辑与三份配置已迁移；待地图实测；ID: blaze_capture |
| 27 | 烽火燎原丶团战模式 | combat | `minecraft:blaze/` | 团战模式逻辑与三份配置已迁移；待地图实测；ID: blaze_team |
| 28 | 烽火燎原丶警匪模式 | combat | `minecraft:blaze/` | 警匪模式逻辑与三份配置已迁移；待地图实测；ID: blaze_police |
| 29 | 雪崩大战(混合模式) | survival | `minecraft:snow/` | 已迁移，待地图实测与轮换积分接入；ID: snowmix |
| 30 | 掘一死战(爆炸弓版) | survival | `minecraft:snow/` | 已迁移，待地图实测与轮换积分接入；ID: bowspleef |
| 31 | 吃豆人 | arcade | `minecraft:pacman/` | 多轮追捕、金币与复活机制已迁移；待地图实测；ID: pacman |
| 32 | 一掘到底 | arcade | `minecraft:miner/` | 原 96 层随机矿柱与工具已迁移；随机编号 62 按确认重抽；ID: miner |
| 33 | 挖矿达人 | arcade | `minecraft:mining/` | 共享随机矿层、五种精准采集工具及金币榜已迁移；待地图实测；ID: mining |
| 34 | 抢椅子 | reaction | `minecraft:chair/` | 移动椅子、淘汰与积分已迁移；待实机；ID: chair |
| 35 | 随机跑酷 | racing | `minecraft:random_parkour/` | 已补全会场复原、实体与定时任务清理；待地图实测；ID: random_parkour |
| 36 | 足球游戏 | sports | `minecraft:ballgame/` | 足球玩法逻辑与三份配置已迁移；待地图实测；ID: football |
| 37 | 钓鱼达人 | arcade | `minecraft:fishing/` | 原生钓鱼、旧格式战利品字段与计分已迁移；ID: fishing |
| 38 | 你建我跑 | building | `minecraft:build_parkour/` | 建筑跑酷玩法逻辑与三份配置已迁移；待地图实测；ID: build_parkour |
| 39 | 建筑猜猜乐 | building | `minecraft:build_guess/` | 建筑猜词玩法逻辑与三份配置已迁移；待地图实测；ID: build_guess |
| 40 | 彩蛋猎人 | exploration | `minecraft:finder/` | 彩蛋收集与并列计分已迁移；待地图实测；ID: finder |
| 41 | 一箭毙命 | combat | `minecraft:one_arrow/` | 一击箭、道具箱、潜行隐身与随机事件已迁移；待地图实测；ID: one_arrow |
| 42 | 飞行大赛 | racing | `minecraft:elytra/` | 飞行竞速与完赛积分已迁移；待地图实测；ID: elytra |
| 43 | 躲避球游戏 | sports | `minecraft:ballgame/` | 躲避球玩法逻辑与三份配置已迁移；待地图实测；ID: dodgeball |
| 44 | 追杀游戏 | combat | `minecraft:live/` | 轮次追杀、职业装备及压板补给逻辑已迁移；待地图实测；ID: live |
| 45 | 不要相信T氏的话 | reaction | `minecraft:t_says/` | 任务玩法已迁移；任务 66 仅允许区域内放置指定白色羊毛；ID: tsays |
| 46 | 1、2、3，木头人 | reaction | `minecraft:mutouren/` | 冻结、重试与完赛积分已迁移；待地图实测；ID: mutouren |
| 47 | 绵羊突击队 | combat | `minecraft:sheepwars/` | 绵羊突击队玩法逻辑与三份配置已迁移；待地图实测；ID: sheepwars |
| 48 | 灾难 · 雪灾 | survival | `minecraft:disaster/snow/` | 雪灾玩法逻辑与三份配置已迁移；待地图实测；ID: disastersnow |
| 49 | 方块竞速 | racing | `minecraft:blockrace/` | 生存建造、TEST_BLOCK 终点与重置区逻辑已迁移；待地图实测；ID: blockrace |
| 50 | 谢幕者胜 | survival | `minecraft:firstoutwins/` | 生存建造、目标区、名次积分及单人开局已迁移；待地图实测；ID: firstoutwins |

## 轮换目录之外的入口

入口存在不等于已具备完整游戏逻辑。以下列表不计入上述 47 个条目。

| 加入入口 | 处置 |
| --- | --- |
| `foodparty:join` | 按确认方案补齐订单、采集、制作、交单与排名积分；待地图实测；ID: foodparty |
| `minecraft:dropper/join` | 既有独立实现：十关推进、失败重试和次数；待新地图实测；ID: dropper |
| `minecraft:job_pvp/join` | 21 套职业装备及源码明确被动已迁移；缺失的主动技能触发逻辑未推测补写；ID: job_pvp |
| `minecraft:lpark/join` | 关卡跑酷：按当前多人派对范围暂不加入 |
| `minecraft:maze/join` | 迷宫：按当前多人派对范围暂不加入 |
| `minecraft:npark/join` | 自由跑酷：按当前多人派对范围暂不加入 |
| `minecraft:parkourrace/join` | 既有独立实现；按确认修复为每位完赛者 2 分；ID: parkourrace |
| `minecraft:pvelobby/join` | 排除：PVE 大厅 |
| `minecraft:twolf/join` | 排除：狼人杀为桌游，用户已确认排除全部桌游 |
| `minecraft:bridge_practice/join` | 搭路练习：按当前多人派对范围暂不加入 |
| `woolwar:join` | 已按确认方案实现四队占点/淘汰回合；待地图实测；ID: woolwars |
| `minecraft:desk/join` | 排除：全部桌游，用户已确认（包括掷骰子、俄罗斯轮盘、商贾传奇） |
| `minecraft:small_games/chess/join` | 排除：棋类 |
| `minecraft:small_games/total/join` | 派对调度系统，单独实现；不算小游戏 |

## 不修改的既有模块

银行、僵尸末日、沙子游戏、地牢及公共战斗系统保留。与它们名称或玩法相近的新条目须先核对，避免重复移植或修改既有实现。

## 进度记录原则

完成一个玩法后，由实现者填写具体源码目录、配置目录、支持模式、编译与实测结果；不能只凭创建文件夹或三份 YAML 将状态改为完成。
