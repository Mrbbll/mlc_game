"""Read-only source audit. Does not modify the supplied Minecraft map."""
import argparse
import json
import re
from pathlib import Path


CATEGORIES = {
    'reaction': {5, 34, 45, 46},
    'survival': {2, 3, 4, 6, 9, 14, 29, 30, 48, 50},
    'arcade': {23, 31, 32, 33, 37},
    'racing': {25, 35, 42, 49},
    'combat': {8, 11, 15, 16, 17, 18, 19, 22, 26, 27, 28, 41, 44, 47},
    'deduction': {1, 21},
    'building': {38, 39},
    'sports': {10, 12, 36, 43},
    'exploration': {40},
    'cooperation': {13},
}


def write_inventory(result, target):
    lines = ['# 移植清单', '',
             '> 本表自动生成；已实现条目由 progress.json 显式记录。待审查条目不能视为已完成。', '',
             '轮换目录 49 个条目，排除道具躲猫猫和船战后保留 47 个候选玩法。桌游、棋类和 PVE 另行排除。分类是插件目录建议，不改变原玩法。', '',
             '| 原 ID | 玩法 | 建议分类 | 源函数前缀 | 状态 |',
             '| --- | --- | --- | --- | --- |']
    for game in sorted(result['games'], key=lambda game: game['id']):
        category = next((name for name, ids in CATEGORIES.items() if game['id'] in ids), 'excluded' if game['excluded'] else 'review')
        status = '排除：废案' if game['excluded'] else '待源码审查与实现'
        if game['id'] == 35:
            status += '；结束清理缺失，已同意补全'
        if game.get('implementation'):
            status = game['implementation']['status'] + '；ID: ' + game['implementation']['plugin-id']
        lines.append(f"| {game['id']} | {game['name']} | {category} | `{game['source_prefix']}` | {status} |")
    lines += ['', '## 轮换目录之外的入口', '',
              '入口存在不等于已具备完整游戏逻辑。以下列表不计入上述 47 个条目。', '',
              '| 加入入口 | 处置 |', '| --- | --- |']
    excluded = {
        'minecraft:pvelobby/join': '排除：PVE 大厅',
        'minecraft:desk/join': '排除：全部桌游，用户已确认（包括掷骰子、俄罗斯轮盘、商贾传奇）',
        'minecraft:small_games/chess/join': '排除：棋类',
        'minecraft:small_games/total/join': '派对调度系统，单独实现；不算小游戏',
        'woolwar:join': '已按讨论方案独立实现；待地图实测与派对计分接入，ID: woolwars',
        'minecraft:lpark/join': '关卡跑酷：按当前多人派对范围暂不加入',
        'minecraft:npark/join': '自由跑酷：按当前多人派对范围暂不加入',
        'minecraft:maze/join': '迷宫：按当前多人派对范围暂不加入',
        'minecraft:bridge_practice/join': '搭路练习：按当前多人派对范围暂不加入',
    }
    for candidate in result['additional_join_candidates']:
        key = candidate['function']
        implementation = result.get('independent-implementations', {}).get(key)
        status = implementation['status'] + '；ID: ' + implementation['plugin-id'] if implementation else excluded.get(key, '独立多人玩法：纳入待审查清单')
        lines.append(f"| `{key}` | {status} |")
    lines += ['', '## 不修改的既有模块', '',
              '银行、僵尸末日、沙子游戏、地牢及公共战斗系统保留。与它们名称或玩法相近的新条目须先核对，避免重复移植或修改既有实现。', '',
              '## 进度记录原则', '',
              '完成一个玩法后，由实现者填写具体源码目录、配置目录、支持模式、编译与实测结果；不能只凭创建文件夹或三份 YAML 将状态改为完成。', '']
    target.write_text('\n'.join(lines), encoding='utf-8')


def audit(root):
    packs = root / 'datapacks'
    functions = {}
    for path in sorted(packs.rglob('*.mcfunction')):
        parts = path.relative_to(packs).parts
        if len(parts) < 5 or parts[1] != 'data' or parts[3] not in ('function', 'functions'):
            continue
        key = parts[2] + ':' + '/'.join(parts[4:])[:-11]
        functions.setdefault(key, []).append(path)
    catalog = packs / 'small_games/data/minecraft/function/small_games/total/games/all_total_games.mcfunction'
    games = []
    for line in catalog.read_text(encoding='utf-8-sig').splitlines():
        if line.lstrip().startswith('#') or 'random_games_all.games append value' not in line:
            continue
        def field(key):
            value = re.search(r'\b' + key + r':("(?:\\.|[^"\\])*")', line)
            return json.loads(value[1]) if value else ''
        game_id = int(re.search(r'\bid:(\d+)', line)[1])
        calls = re.findall(r'function ([a-z0-9_:/.-]+)', line)
        first = calls[0] if calls else ''
        if ':' not in first:
            first = 'minecraft:' + first
        ns, game_path = first.split(':', 1)
        prefix = ns + ':' + game_path.split('/')[0] + '/' if '/' in game_path else ns + ':'
        if prefix == 'minecraft:disaster/':
            prefix = 'minecraft:disaster/snow/'
        sources = [(key, path) for key, paths in functions.items() if key.startswith(prefix) for path in paths]
        refs = set()
        features = set()
        coordinates = []
        for key, path in sources:
            for number, command in enumerate(path.read_text(encoding='utf-8-sig').splitlines(), 1):
                if not command.strip() or command.lstrip().startswith('#'):
                    continue
                for ref in re.findall(r'\bfunction ([a-z0-9_:/.-]+)', command):
                    refs.add(ref if ':' in ref else 'minecraft:' + ref)
                for feature in ('clone', 'fill', 'place template', 'place structure', 'summon', 'loot', 'item_model', 'dialog', 'random', 'execute in', '$'):
                    if feature in command:
                        features.add(feature)
                if re.search(r'\b(?:tp|teleport|clone|fill|setblock|positioned|summon|forceload)\b', command):
                    coordinates.append({'source': str(path.relative_to(root)).replace('\\', '/'), 'line': number, 'command': command})
        prefix_text = field('prefix')
        games.append({
            'id': game_id, 'name': re.sub(r'§.', '', field('name')), 'category': prefix_text,
            'description': field('desc'), 'source_prefix': prefix,
            'excluded': '废案' in prefix_text, 'status': 'excluded' if '废案' in prefix_text else 'pending-source-review',
            'function_count': len(sources), 'features': sorted(features),
            'missing_direct_function_references': sorted(refs - functions.keys()),
            'external_function_references': sorted(ref for ref in refs if not ref.startswith(prefix)),
            'coordinate_command_count': len(coordinates),
            'coordinate_samples': coordinates[:16],
        })
    prefixes = {game['source_prefix'] for game in games}
    candidates = []
    for key, paths in functions.items():
        if (key.endswith('/join') or key.endswith(':join')) and not any(key.startswith(prefix) for prefix in prefixes):
            if any(path.relative_to(packs).parts[0] in ('map_all', 'map_game_2nd', 'map_game_3rd', 'small_games', 'foodparty', 'map_woolwars') for path in paths):
                candidates.append({'function': key, 'sources': [str(path.relative_to(root)).replace('\\', '/') for path in paths]})
    return {'source_root': str(root), 'catalog_entries': len(games), 'included_entries': sum(not game['excluded'] for game in games), 'games': games, 'additional_join_candidates': candidates,
            'duplicate_function_ids': {key: [str(path.relative_to(root)).replace('\\', '/') for path in paths] for key, paths in functions.items() if len(paths) > 1}}


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('map_root', type=Path)
    parser.add_argument('--output', type=Path, required=True)
    args = parser.parse_args()
    result = audit(args.map_root)
    args.output.parent.mkdir(parents=True, exist_ok=True)
    progress_path = args.output.with_name('progress.json')
    if progress_path.exists():
        progress = json.loads(progress_path.read_text(encoding='utf-8'))
        result['independent-implementations'] = progress.get('independent-games', {})
        for game in result['games']:
            if str(game['id']) in progress.get('source-games', {}):
                game['implementation'] = progress['source-games'][str(game['id'])]
    args.output.write_text(json.dumps(result, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    write_inventory(result, args.output.with_name('MIGRATION.md'))
    print(json.dumps({key: value for key, value in result.items() if key not in ('games', 'additional_join_candidates')}, ensure_ascii=False))
    print('Additional join candidates:', len(result['additional_join_candidates']))
    for game in result['games']:
        if not game['excluded'] and game['missing_direct_function_references']:
            print(game['name'], ':', ', '.join(game['missing_direct_function_references'][:12]))
