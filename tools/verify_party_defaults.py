"""Check the runtime registry against shipped YAML and player-facing language keys."""
from pathlib import Path
import re
import zipfile
import yaml

ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / 'src/main/java/com/mlc/mlcgames/party'
RESOURCES = ROOT / 'src/main/resources/party'


def get(data, path):
    for part in path.split('.'):
        assert isinstance(data, dict) and part in data, f'Missing {path}'
        data = data[part]
    return data


def assert_unset_coordinates(value, context):
    if isinstance(value, dict):
        if {'x', 'y', 'z'} <= value.keys():
            assert all(value[axis] is None for axis in ('x', 'y', 'z')), f'{context}: coordinates must remain unconfigured'
        for key, child in value.items():
            assert_unset_coordinates(child, f'{context}.{key}')
    elif isinstance(value, list):
        for index, child in enumerate(value):
            assert_unset_coordinates(child, f'{context}[{index}]')


def paths(data, prefix=''):
    if isinstance(data, dict):
        for key, child in data.items():
            current = f'{prefix}.{key}' if prefix else str(key)
            yield current
            yield from paths(child, current)


registry = (JAVA / 'PartyModule.java').read_text(encoding='utf-8')
games = re.findall(r'register\("([a-z0-9_]+)", "([a-z]+)",', registry)
assert len({game for game, category in games}) == len(games) > 0
registered = {f'{category}/{game}' for game, category in games}
available = {
    f'{category.name}/{game.name}'
    for category in RESOURCES.iterdir() if category.is_dir()
    for game in category.iterdir() if game.is_dir()
}
assert registered == available, f'Resource/registry mismatch: missing={registered - available}, extra={available - registered}'
common = (JAVA / 'core/PartySession.java').read_text(encoding='utf-8')


def related_sources(initial, category, game, source_game):
    """Include game-local helpers and recursively imported plugin source classes."""
    pending = list(initial)
    seen = set()
    allowed_roots = [JAVA / category / source_game, JAVA / 'core', JAVA / category / 'common']
    if game in ('tntrun', 'spleef', 'bowspleef', 'snowmix'):
        allowed_roots.append(JAVA / 'survival/snow')
    if game in ('live', 'pacman'):
        allowed_roots.append(JAVA / 'survival/chase')

    def allowed(source):
        return any(source == root or root in source.parents for root in allowed_roots)

    while pending:
        source = pending.pop()
        if source in seen:
            continue
        seen.add(source)
        content = source.read_text(encoding='utf-8')
        for imported in re.findall(r'^import\s+(com\.mlc\.mlcgames\.party\.[\w.*]+);', content, re.MULTILINE):
            relative = imported.split('.party.', 1)[1]
            if relative.endswith('.*'):
                package = JAVA / relative[:-2].replace('.', '/')
                for candidate in package.glob('*.java'):
                    if not allowed(candidate) or candidate in seen:
                        continue
                    candidate_text = candidate.read_text(encoding='utf-8')
                    declared_types = re.findall(r'\b(?:class|interface|enum|record)\s+(\w+)', candidate_text)
                    if any(re.search(r'\b' + re.escape(type_name) + r'\b', content) for type_name in declared_types):
                        pending.append(candidate)
            else:
                dependency = JAVA / (relative.replace('.', '/') + '.java')
                if dependency.is_file() and allowed(dependency) and dependency not in seen:
                    pending.append(dependency)
    return sorted(seen)


for game, category in games:
    directory = RESOURCES / category / game
    configs = {name: yaml.safe_load((directory / (name + '.yml')).read_text(encoding='utf-8')) for name in ('game', 'locations', 'language')}
    assert all(config['schema-version'] == 1 for config in configs.values())
    rules, language, locations = configs['game'], configs['language'], configs['locations']
    arena = get(locations, 'maps.arena_1')
    assert arena['configured'] is False and arena['world'] == ''
    assert_unset_coordinates(arena, f'{game}.locations')
    for corner in ('corner-1', 'corner-2'):
        assert all(value is None for value in arena['bounds'][corner].values())
    assert get(arena, 'points.spawns') == []
    minimum_players = 1 if game == 'firstoutwins' else 2
    assert minimum_players <= rules['players']['min'] <= rules['players']['max'] <= 64
    source_game = {'job_pvp': 'jobpvp', 'one_arrow': 'onearrow', 'random_parkour': 'randomparkour'}.get(game, game)
    sources = list((JAVA / category / source_game).glob('*.java'))
    assert sources, f'{game}: logic directory missing'
    if game in ('tntrun', 'spleef', 'bowspleef', 'snowmix'):
        sources += list((JAVA / 'survival/snow').glob('*.java'))
    sources = related_sources(sources, category, game, source_game)
    code = common + '\n' + '\n'.join(path.read_text(encoding='utf-8') for path in sources)
    for key in re.findall(r'\.(?:integer|number|materials|thresholds|itemMaterial)\("([\w.-]+)"\s*[,)]', code):
        try:
            get(rules, key)
        except AssertionError as error:
            raise AssertionError(f'{category}/{game}: {error}') from error
    for key in re.findall(r'\.(?:text|message|broadcast)\("([\w.-]+)"', code):
        if key.endswith('-'):
            assert any(path.startswith(key) for path in paths(language)), f'{game}: missing language prefix {key}'
        elif not key.endswith('.'):
            get(language, key)
    for key in re.findall(r'\.item\([^\n;]*?, "([\w.-]+)"\)', code):
        get(language, key)
    for receiver, accessor, key in re.findall(
            r'([\w.]+)\.(getString|getInt|getDouble|getLong|getBoolean|getStringList|getConfigurationSection|contains)\("([\w.-]+)"', code):
        if 'language' in receiver:
            get(language, key)
        elif 'rules' in receiver:
            get(rules, key)
    for region in re.findall(r'arena\.region\("([\w.-]+)"\)', code):
        if game == 'dodgeball' and region in ('goal-a', 'goal-b'):
            continue
        assert region in arena['regions'], f'{game}: missing region {region}'
    for group in re.findall(r'arena\.group\("([\w.-]+)"\)', code):
        if group == 'police-spawn' and game != 'blaze_police':
            continue
        assert group in get(arena, 'points.groups'), f'{game}: missing point group {group}'
    for row in rules.get('pad-effects', []):
        assert row['region-prefix'] in arena['regions']
        assert 1 <= row['duration-ticks'] <= 200 and 0 <= row['amplifier'] <= 30
    if game == 'parkourrace':
        assert set(rules['finish-points'].values()) == {2}, 'User approved exactly 2 points per finisher'
    if game == 'chair':
        for n in range(2, 65):
            expected = n//2 if n >= 31 else 15 if n >= 21 else 10 if n >= 15 else 8 if n >= 10 else 5 if n >= 7 else 3 if n >= 4 else 2 if n == 3 else 1
            actual = next(row['value'] for row in reversed(rules['chair-counts']) if row['min-players'] <= n)
            assert actual == expected < n
    if game == 'poolwar':
        assert rules['stamina'] == {'initial': 80, 'minimum-capacity': 30, 'capacity-loss-per-second': 5}
        assert rules['rank-points'] == {'first': 3, 'second': 2, 'third': 1}
    if game == 'golf':
        assert rules['turn-seconds'] == 421 and rules['winner-points'] == 1
        assert rules['hole-base-score'] == 5000
        assert rules['club-energy'] == {f'level-{level}': 5 * level for level in range(1, 7)}
        assert rules['max-seconds'] >= rules['players']['max'] * 3 * rules['turn-seconds'] + 3 * rules['course-preview-seconds']
        for level in range(1, 7):
            get(language, f'items.club-{level}')
        for course in range(1, 4):
            get(arena, f'regions.course-{course}')
            get(arena, f'regions.course-{course}-goal')
    if game == 'fishing':
        assert rules['pool-weights'] == {'junk': 10, 'treasure': 5, 'fish': 85}
        assert rules['treasure-condition-chance'] == 0.8
        assert rules['lure-level'] == 3 and rules['max-seconds'] == 91
        assert len(rules['coin-values']) == 22
        for pool in rules['loot'].values():
            for row in pool:
                assert row['weight'] > 0
                get(rules, 'coin-values.' + row['material'])
                get(language, 'catches.' + row['material'])
        bamboo = next(row for row in rules['loot']['junk'] if row['material'] == 'BAMBOO')
        assert bamboo['jungle-only'] is True
    if game == 'miner':
        import json
        palette = json.loads((ROOT / 'tools/data/miner-palette.json').read_text(encoding='utf-8'))
        assert rules['block-palette'] == [row['material'] for row in palette['entries']]
        assert len(rules['block-palette']) == 236
        assert {row['roll'] for row in palette['entries']} == set(range(237)) - {62}
        assert rules['random-layers'] == 96 and rules['tools']['efficiency'] == 3
        assert rules['finish-materials'] == ['BEDROCK'] and rules['max-seconds'] == 600
        assert list(rules['finish-points'].values()) == [4, 3, 2, 1]
        assert rules['shorten-after-finishers'] == 4 and rules['shortened-seconds'] == 30
        for slot in range(1, 6):
            get(language, f'items.tool-{slot}')
    if game == 'mining':
        assert rules['mine']['layers'] == 28
        assert len(rules['mine']['ore-rolls']) == 27
        assert rules['max-seconds'] == 61
        assert rules['coin-values']['STONE'] > 0
        get(arena, 'regions.mine-area')
    if game == 'car_race':
        assert list(rules['finish-points'].values()) == [3, 2, 2, 1, 1]
        assert rules['laps'] == 3 and rules['finishers-to-end'] == 5
        assert rules['speed'] == {'acceleration': 6, 'brake': 5, 'friction': 3, 'cap': 100, 'slow-threshold': 10, 'slow-penalty': 5, 'attribute-factor': 0.01}
    if game == 'dropper':
        assert get(arena, 'points.groups.level-starts') == []
        assert rules['attempt-health'] == 2
        assert 'finish-points' not in rules
        for level in range(1, 11):
            for corner in get(arena, f'regions.level-{level}-portal').values():
                assert all(value is None for value in corner.values())

jar = ROOT / 'build/libs/mlcgames-1.0-SNAPSHOT-all.jar'
inputs = [JAVA / 'PartyModule.java', *JAVA.rglob('*.java'), *RESOURCES.rglob('*.yml')]
bundle_checked = False
if jar.exists() and jar.stat().st_mtime >= max(path.stat().st_mtime for path in inputs):
    with zipfile.ZipFile(jar) as archive:
        names = set(archive.namelist())
        for game, category in games:
            for name in ('game', 'locations', 'language'):
                assert f'party/{category}/{game}/{name}.yml' in names
        assert 'party/SOURCE-LICENSE.txt' in names and 'party/NOTICE.txt' in names
    bundle_checked = True
print(f'Validated {len(games)} registered games, {len(games)*3} YAML files, required rule/language keys, unset coordinates; bundled resources {"verified" if bundle_checked else "not checked (no current jar)"}.')
