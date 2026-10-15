"""Maintainer helper: creates bundled defaults; never writes live server configuration."""
from pathlib import Path
import json
import yaml

ROOT = Path(__file__).resolve().parents[1] / 'src/main/resources/party'


def write(path, data, comment=''):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(comment + yaml.safe_dump(data, allow_unicode=True, sort_keys=False), encoding='utf-8')


def point():
    return dict(x=None, y=None, z=None)


def region():
    return {'corner-1': point(), 'corner-2': point()}


names = {
    'believer': ('survival', '信仰方块', '白色→黄色→橙色→红色→消失，留在场上直到最后。'),
    'repel': ('survival', '击退大赛', '开局 10 秒后获得击退棒和弓箭，将对手击退到淘汰区域；末地传送门框是弹射点。'),
    'hotpotever': ('survival', '烫手的山芋', '持有者攻击其他玩家传递土豆；倒计时结束时持有者淘汰。'),
    'tntrun': ('survival', 'TNTRUN', '开局保护结束后，踩过的地板会延迟消失，存活到最后。'),
    'spleef': ('survival', '掘一死战', '挖掉对手脚下的方块，存活到最后；后期地板从边缘逐步消失。'),
    'snowmix': ('survival', '雪崩大战（混合模式）', '地板会被踩踏或挖掘破坏，存活到最后；后期地板逐步缩小。'),
    'bowspleef': ('survival', '掘一死战（爆炸弓版）', '箭落地会破坏周围地板，存活到最后；后期地板逐步缩小。'),
    'woolwars': ('combat', 'Wool Wars', '清除中立方块并用本队羊毛占满中心，或淘汰对方全队，赢下回合。'),
    'finder': ('exploration', '彩蛋猎人', '寻找地图中发光的彩蛋，左键或右键领取。收集数量最多的玩家获胜。'),
    'elytra': ('racing', '飞行大赛', '穿戴鞘翅飞向绿宝石终点，每次尝试有 4 个烟花；碰到岩浆返回起点。'),
    'mutouren': ('racing', '1、2、3，木头人', '行动阶段前进，冻结阶段保持不动。违规会返回起点；到达绿宝石终点完成比赛。'),
    'parkourrace': ('racing', '跑酷竞赛', '踩金块记录检查点，利用效果垫跑到绿宝石终点；踩到危险地面会返回检查点。'),
    'chair': ('reaction', '抢椅子', '准备信号结束后，右键抢占移动矿车。倒计时结束时没坐上椅子的人淘汰。'),
    'poolwar': ('sports', '冬泳怪鸽', '用击退棒将对手击入水中。泡水消耗体力并降低体力上限，离水休息恢复体力，活到最后。'),
    'dropper': ('racing', '星跳水立方', '从平台顶端安全到达底端，寻找传送门进入下一关；失败会返回当前关起点并记录次数。共十关。'),
    'golf': ('sports', '高尔夫游戏', '轮流用六档球杆把球击入铜块目标区。进洞得分为 5000 减已用能量，总分最高者获胜。'),
    'fishing': ('arcade', '钓鱼达人', '使用饵钓 III 钓竿钓鱼，鱼获自动兑换金币。金币最多者获胜，前三名奖励派对积分，允许并列。'),
    'miner': ('arcade', '一掘到底', '使用五种挖掘工具向下挖到底部基岩。所有玩家的随机矿柱相同，按完赛顺序奖励积分。'),
}

common_language = {
    'schema-version': 1,
    'disabled': '&c此小游戏未启用。',
    'map-unconfigured': '&c地图 {map} 尚未配置，请先填写 locations.yml 的角点和特殊点位。',
    'already-running': '&c当前游戏已开始或正在清理。',
    'full': '&c人数已满。',
    'teleport-failed': '&c传送被阻止，已取消加入或开局。',
    'joined': '&a{player} 加入了等待队列，当前 {count} 人。',
    'not-enough': '&c人数不足，无法开始。',
    'preparing': '&e正在记录场地，准备游戏。',
    'countdown': '&e距离开始还有 {seconds} 秒。',
    'started': '&a游戏开始！',
    'eliminated': '&c{player} 已淘汰。',
    'draw': '平局',
    'finished': '&6游戏结束，获胜者：{winners}',
    'legacy-busy': '&e已有游戏开始运行，派对已结束并恢复，避免影响已有游戏。',
}

for game_id, (category, name, instructions) in names.items():
    directory = ROOT / category / game_id
    language = dict(common_language, name=name, instructions='&b' + instructions)
    rules = {
        'schema-version': 1, 'enabled': True,
        'players': {'min': 2, 'max': 8 if game_id == 'woolwars' else 16},
        'countdown-seconds': 5, 'max-seconds': 1800 if game_id == 'woolwars' else 600,
        'limits': {'max-arena-blocks': 1000000, 'max-snapshot-blocks': 100000, 'blocks-per-tick': 1000},
        'elimination-blocks': ['ORANGE_STAINED_GLASS'] if game_id == 'repel' else ['MAGMA_BLOCK'],
        'elimination-depth': 2 if game_id == 'repel' else 1,
    }
    regions = {}
    points = {'waiting': dict(point(), yaw=0, pitch=0), 'spectator': dict(point(), yaw=0, pitch=0), 'exit': dict(point(), yaw=0, pitch=0), 'spawns': []}
    if game_id == 'believer':
        rules.update({'changes-per-step': 3, 'step-ticks': 20, 'warmup-seconds': 3})
        regions['floor'] = region()
    if game_id == 'repel':
        rules.update({'weapon-delay-seconds': 10, 'arrows': 15, 'hit-cooldown-ticks': 10,
                      'knockback': {'horizontal': 0.9, 'vertical': 0.4}})
        rules['launch-pad'] = {'duration-ticks': 20, 'amplifier': 15}
        regions['launch-pad'] = region()
        language['weapons-ready'] = '&e现在可以攻击对手了！'
        language['items'] = {'stick': '&a击退棒', 'bow': '&c击退弓'}
    if game_id == 'hotpotever':
        rules.update({'mode': 0, 'between-round-seconds': 5, 'pass-cooldown-ticks': 0, 'projectile-cooldown-ticks': 60})
        rules['holder-counts'] = [{'min-players': n, 'value': count} for n, count in [(2,1),(5,2),(8,3),(12,4),(16,6),(20,8)] + [(n, 9+(n-24)//4) for n in range(24,61,4)]]
        rules['explosion-seconds'] = [{'min-players': n, 'value': seconds} for n, seconds in [(2,30),(3,40),(4,45),(8,50),(16,60)]]
        language.update({'round-break': '&e下一轮将在 {seconds} 秒后开始。',
                         'holders': '&c土豆持有者：{players}',
                         'holder-status': '&c你持有土豆！快传给别人！剩余 {seconds} 秒',
                         'runner-status': '&b躲避土豆持有者！剩余 {seconds} 秒',
                         'passed': '&e{from} 把土豆传给了 {to}。',
                         'items': {'potato': '&c烫手的山芋', 'weapon': '&e传递武器'}})
    if game_id in ('tntrun', 'spleef', 'snowmix', 'bowspleef'):
        regions['floor'] = region()
        rules.update({'floor-materials': ['SNOW_BLOCK', 'SNOW', 'GLASS', 'GLASS_PANE', 'STONE', 'COBBLESTONE', 'GRASS_BLOCK', 'DIRT', 'OAK_SLAB', 'POWDER_SNOW', 'RED_SAND', 'SAND', 'GRAVEL', 'SOUL_SAND', 'SOUL_SOIL', 'DIRT_PATH', 'FARMLAND', 'LADDER', 'TORCH', 'BOOKSHELF', 'TNT', 'WHITE_TERRACOTTA', '#minecraft:logs', '#minecraft:leaves', '#minecraft:doors', '#minecraft:dirt', '#minecraft:planks', '#minecraft:rails', '#minecraft:stairs', '#minecraft:fences', '#minecraft:fence_gates', '#minecraft:trapdoors', '#minecraft:walls'] + [c + '_STAINED_GLASS' for c in ('WHITE','ORANGE','MAGENTA','LIGHT_BLUE','YELLOW','LIME','PINK','GRAY','LIGHT_GRAY','CYAN','PURPLE','BLUE','BROWN','GREEN','RED','BLACK')],
                      'fall-delay-ticks': 12, 'running-warmup-seconds': 6,
                      'shrink-start-seconds': 45, 'shrink-step-seconds': 5,
                      'arrow-radius': 1, 'arrows-per-second': 5})
        language['items'] = {'tool': '&f地板破坏工具', 'bow': '&c方块爆炸弓'}
    if game_id == 'woolwars':
        regions = {'build': region(), 'target': region()}
        points['team-spawns'] = {'red': [], 'blue': []}
        rules.update({'round-seconds': 120, 'wins-required': 3, 'weapons': {'sword': 'STONE_SWORD', 'shears': 'SHEARS'}})
        language.update({'teams': {'red': '红队', 'blue': '蓝队'},
                         'round-start': '&e新回合开始！红队 {red} : {blue} 蓝队',
                         'round-end': '&6本回合结果：{team}',
                         'items': {'sword': '&f基础武器', 'shears': '&f羊毛剪刀', 'wool': '&f本队羊毛'}})
    if category == 'racing' and game_id != 'dropper' or game_id == 'miner':
        regions['finish'] = region()
        rules.update({'max-seconds': 600 if game_id == 'parkourrace' else 240, 'finish-materials': ['EMERALD_BLOCK'],
                      'pvp-knockback': game_id == 'mutouren',
                      'shorten-after-finishers': 0 if game_id == 'parkourrace' else 4, 'shortened-seconds': 30,
                      'finish-points': {'first': 2 if game_id == 'parkourrace' else 4, 'second': 2 if game_id == 'parkourrace' else 3, 'third': 2, 'remaining': 2 if game_id == 'parkourrace' else 1}})
        language.update({'race-finished': '&a{player} 第 {rank} 个完赛，获得 {points} 分。',
                         'race-shortened': '&e完赛人数达到阈值，剩余时间缩短到 {seconds} 秒。',
                         'race-retry': '&e本次尝试失败，已返回起点或检查点。',
                         'race-status': '&e剩余 {seconds} 秒，已有 {finishers} 人完赛。'})
    if game_id == 'finder':
        rules.update({'max-seconds': 90, 'collection-reach': 4.5, 'all-found-seconds': 5,
                      'rank-interval-seconds': 15, 'rank-points': {'first': 3, 'second': 2, 'third': 1},
                      'egg-counts': [{'min-players': n, 'value': v} for n, v in [(2,30),(6,60),(11,80),(21,1000)]]})
        points['groups'] = {'eggs': []}
        language.update({'items': {'egg': '&d彩蛋'}, 'egg-found': '&6{player} 找到一个彩蛋，当前 {count} 个。',
                         'all-eggs-found': '&6所有彩蛋已找到，剩余时间缩短至 {seconds} 秒。',
                         'egg-rank': '&e第 {rank} 名：{player}（{count} 个彩蛋）'})
    if game_id == 'elytra':
        regions['gate'] = region()
        rules.update({'rockets-per-attempt': 4, 'elimination-blocks': ['LAVA']})
        language['items'] = {'elytra': '&b比赛鞘翅', 'rocket': '&e助推烟花'}
    if game_id == 'mutouren':
        regions.update({'gate': region(), 'safe': region()})
        points['groups'] = {'judge': []}
        rules.update({'first-moving': {'min-seconds': 6, 'max-seconds': 20}, 'moving': {'min-seconds': 8, 'max-seconds': 20},
                      'frozen': {'min-seconds': 3, 'max-seconds': 6}, 'movement-tolerance': 0.001,
                      'progress-interval-seconds': 5, 'progress-axis': 'X'})
        language.update({'judge-name': '&c鬼', 'freeze-start': '&c不许动！', 'freeze-end': '&a可以继续前进！',
                         'frozen-status': '&c冻结中，请保持不动！剩余 {seconds} 秒', 'moving-status': '&a可以前进，{seconds} 秒后冻结。',
                         'moved-while-frozen': '&c你动了，已返回起点！',
                         'signals': {'count': '&b1、2、3', 'name': '&e木头人', 'stop': '&c不许动！', 'warning': '&e注意停止移动'},
                         'progress-rank': '&e当前第 {rank} 名：{player}（前进 {distance} 米）'})
    if game_id == 'parkourrace':
        points['groups'] = {'checkpoints': []}
        rules['elimination-blocks'] = ['RED_CONCRETE', 'GRASS_BLOCK', 'BLACK_STAINED_GLASS', 'OAK_LEAVES']
        rules['checkpoint-materials'] = ['GOLD_BLOCK']
        rules['pad-effects'] = []
        for prefix, material, effect, duration, amplifier in [('speed-pad','LIGHT_BLUE_CONCRETE','SPEED',20,3),('jump-pad','LIME_CONCRETE','JUMP_BOOST',20,3),('lift-pad','BLACK_CONCRETE','LEVITATION',20,4),('slow-pad','BROWN_CONCRETE','SLOWNESS',40,2),('blind-pad','YELLOW_CONCRETE','BLINDNESS',40,20),('clear-pad','GLASS','CLEAR',20,0)]:
            regions[prefix] = region()
            rules['pad-effects'].append({'region-prefix': prefix, 'material': material, 'effect': effect, 'duration-ticks': duration, 'amplifier': amplifier})
        language['checkpoint-saved'] = '&a检查点已记录。'
    if game_id == 'chair':
        regions['cart-travel'] = region()
        points['groups'] = {'chairs': []}
        rules.update({'prepare': {'min-seconds': 1, 'max-seconds': 10}, 'claim-seconds': 10, 'break-seconds': 5,
                      'velocity': {'min': 0.1, 'max': 2.0}, 'cart-max-speed': 0.4, 'restart-speed': 0.15, 'claim-reach': 4.5,
                      'rank-points': {'first': 3, 'second': 2, 'third': 1},
                      'chair-counts': [{'min-players': n, 'value': value} for n, value in [(2,1),(3,2),(4,3),(7,5),(10,8),(15,10),(21,15)] + [(n,n//2) for n in range(31,65)]]})
        language.update({'chair-name': '&6板凳', 'chairs-preparing': '&e正在准备，本轮椅子数量：{count}',
                         'chairs-ready': '&a板凳已出现，请尽快右键抢夺！', 'chair-round-ended': '&6本轮结束，未入座玩家已淘汰。'})
    if game_id == 'poolwar':
        regions['pool'] = region()
        rules.update({'elimination-blocks': [], 'stamina': {'initial': 80, 'minimum-capacity': 30, 'capacity-loss-per-second': 5},
                      'hit-cooldown-ticks': 10, 'knockback': {'horizontal': 0.9, 'vertical': 0.4},
                      'rank-points': {'first': 3, 'second': 2, 'third': 1}})
        language.update({'items': {'boots': '&b冰霜行者鞋', 'stick': '&b击退棒'},
                         'swimming': '&c注意体力！{stamina} / {capacity}', 'resting': '&d正在休息：{stamina} / {capacity}',
                         'healthy': '&a当前体力充足：{stamina} / {capacity}'})
    if game_id == 'dropper':
        points['groups'] = {'level-starts': []}
        regions.update({f'level-{level}-portal': region() for level in range(1, 11)})
        rules.update({'max-seconds': 7200, 'elimination-blocks': [], 'attempt-health': 2, 'portal-materials': ['NETHER_PORTAL']})
        language.update({'dropper-level': '&d第 {level} 关：寻找传送门进入下一关。',
                         'dropper-failed': '&c本次尝试失败。当前失败次数：{failures}',
                         'dropper-passed': '&b{player} 完成了第 {level} 关。',
                         'dropper-complete': '&a{player} 完成了全部十关，总失败次数：{failures}。'})
    if game_id == 'golf':
        points['groups'] = {'course-starts': [], 'ball-starts': []}
        for course in range(1, 4):
            regions[f'course-{course}'] = region()
            regions[f'course-{course}-goal'] = region()
        rules.update({'max-seconds': 21600, 'elimination-blocks': [], 'play-all-courses': True,
                      'selected-course': 1, 'turn-seconds': 421, 'course-preview-seconds': 5,
                      'shot-cooldown-ticks': 10, 'control-drink-seconds': 1.0, 'hit-reach': 4.5, 'knockback': {'per-level': 0.5, 'vertical': 0.4},
                      'reset-energy-on-retry': True, 'hole-base-score': 5000, 'winner-points': 1,
                      'club-energy': {f'level-{level}': level * 5 for level in range(1, 7)},
                      'goal-materials': ['COPPER_BLOCK'], 'hazard-materials': ['MAGMA_BLOCK']})
        language.update({'ball-name': '&eBall', 'items': {**{f'club-{level}': f'&a击退 {level}（能量 {level * 5}）' for level in range(1, 7)},
                         'back': '&b返回球场起点 - 右键', 'retry': '&c重来 - 右键', 'skip': '&c放弃本轮击球 - 右键'},
                         'golf-course': '&6球场 {course}，{seconds} 秒后开始轮流击球。',
                         'golf-turn': '&6现在轮到 {player} 击球。', 'golf-retry': '&c{player} 击球失败，球已重置。',
                         'golf-turn-timeout': '&c{player} 本轮时间已用完。',
                         'golf-status': '&e{player} 的回合，剩余 {seconds} 秒，已使用能量 {energy}。',
                         'golf-hole': '&a{player} 成功进洞，本次得分 {score}，累计 {total}。',
                         'golf-shot': '&c消耗 {cost} 能量，已使用总能量 {energy}。',
                         'golf-skipped': '&c{player} 放弃了本轮击球。', 'golf-total': '&6{player} 最终高尔夫分数：{score}'})
    if game_id == 'fishing':
        regions['fishing-water'] = region()
        values = {'COD': 3, 'SALMON': 6, 'TROPICAL_FISH': 10, 'PUFFERFISH': 8, 'LILY_PAD': 1,
                  'LEATHER_BOOTS': 3, 'LEATHER': 3, 'BONE': 3, 'POTION': 3, 'STRING': 5, 'BOWL': 3,
                  'STICK': 5, 'INK_SAC': 5, 'TRIPWIRE_HOOK': 3, 'ROTTEN_FLESH': 3, 'BAMBOO': 6,
                  'NAME_TAG': 15, 'SADDLE': 15, 'BOW': 15, 'BOOK': 10, 'ENCHANTED_BOOK': 15, 'NAUTILUS_SHELL': 15}
        catches = dict(zip(values, ['鳕鱼', '鲑鱼', '热带鱼', '河豚', '睡莲', '皮革靴子', '皮革', '骨头', '水瓶', '线',
                                   '碗', '木棍', '墨囊', '绊线钩', '腐肉', '竹子', '命名牌', '鞍', '弓', '书', '附魔书', '鹦鹉螺壳']))
        rules.update({'countdown-seconds': 6, 'max-seconds': 91, 'elimination-blocks': [], 'lure-level': 3,
                      'rank-interval-seconds': 10, 'rank-points': {'first': 3, 'second': 2, 'third': 1},
                      'pool-weights': {'junk': 10, 'treasure': 5, 'fish': 85}, 'treasure-condition-chance': 0.8,
                      'coin-values': values,
                      'loot': {'fish': [{'material': material, 'weight': weight} for material, weight in [('COD',60), ('SALMON',25), ('TROPICAL_FISH',2), ('PUFFERFISH',13)]],
                               'junk': [{'material': material, 'weight': weight, **({'jungle-only': True} if material == 'BAMBOO' else {})} for material, weight in [('LILY_PAD',17), ('LEATHER_BOOTS',10), ('LEATHER',10), ('BONE',10), ('POTION',10), ('STRING',5), ('BOWL',10), ('STICK',5), ('INK_SAC',5), ('TRIPWIRE_HOOK',10), ('ROTTEN_FLESH',10), ('BAMBOO',10)]],
                               'treasure': [{'material': material, 'weight': 1} for material in ('NAME_TAG', 'SADDLE', 'BOW', 'ENCHANTED_BOOK', 'NAUTILUS_SHELL')]}})
        language.update({'items': {'rod': '&b钓鱼达人钓竿', 'coin': '&6金币'}, 'catches': catches,
                         'fishing-caught': '&6你钓到了 {catch}，兑换 {coins} 金币，当前共 {total} 金币。',
                         'outside-fishing-water': '&c请在配置的钓鱼水域内垂钓。',
                         'fishing-status': '&e金币 {coins}，距离结束还有 {seconds} 秒。',
                         'fishing-rank': '&6第 {rank} 名：{player}（{coins} 金币）'})
    if game_id == 'miner':
        points['groups'] = {'pillars': []}
        palette = json.loads((Path(__file__).parent / 'data/miner-palette.json').read_text(encoding='utf-8'))
        rules.update({'max-seconds': 600, 'countdown-seconds': 6, 'elimination-blocks': [],
                      'random-layers': 96, 'tools': {'efficiency': 3}, 'finish-materials': ['BEDROCK'],
                      'block-palette': [row['material'] for row in palette['entries']]})
        language['items'] = {f'tool-{i+1}': '&b' + name for i, name in enumerate(['挖掘镐', '挖掘斧', '挖掘锹', '挖掘锄', '剪刀'])}
    locations = {'schema-version': 1, 'maps': {'arena_1': {'configured': False, 'world': '', 'bounds': region(), 'points': points, 'regions': regions}}}
    write(directory / 'language.yml', language)
    write(directory / 'game.yml', rules, '# 原地图移植；参数可调整，坐标统一在 locations.yml。\n')
    write(directory / 'locations.yml', locations, '# 由你提供新地图；所有 null 坐标必须填写。角点均为整数，出生点可有小数。\n# spawns 至少提供 players.max 个独立点位。floor/build/target 均必须位于 bounds 内。\n')

write(ROOT / 'language.yml', {
    'schema-version': 1,
    'help': '&e/party list | join <游戏> [地图] | leave | start | stop | check <游戏> [地图] | reload',
    'unknown-game': '&c未知小游戏：{game}', 'no-permission': '&c没有权限。', 'players-only': '此命令仅玩家可用。',
    'game-row': '&e{game} &7- &f{name}', 'error': '&c{error}',
    'legacy-busy': '&c已有游戏正在运行，请先结束已有游戏。',
    'legacy-member': '&c请先退出已有游戏的等待队列。',
    'recovering': '&e场地正在恢复或恢复受阻；请查看控制台与 party/recovery。',
    'player-recovery': '&e你仍有待恢复状态，请先使用 /party leave。',
    'player-not-ready': '&c请先复活、离开载具并清空鼠标上的物品。',
    'already-joined': '&e你已加入当前游戏。', 'session-busy': '&e已有派对会话，请先结束。',
    'not-joined': '&e你没有加入派对小游戏。', 'reloaded': '&a派对配置已重新读取。',
    'valid': '&a地图点位及规则校验通过；开局还将检查快照区域的方块。', 'no-session': '&e当前没有派对会话。',
})
print('Created defaults for', len(names), 'games')
