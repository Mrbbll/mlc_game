"""Compare shipped fishing rules with the supplied, unmodified source functions and loot tables."""
import argparse
import json
import re
from pathlib import Path
import yaml

parser = argparse.ArgumentParser()
parser.add_argument('source_map', type=Path)
args = parser.parse_args()
source = args.source_map / 'datapacks/map_game_3rd/data/minecraft'
root = Path(__file__).resolve().parents[1]
rules = yaml.safe_load((root / 'src/main/resources/party/arcade/fishing/game.yml').read_text(encoding='utf-8'))
text = (source / 'function/fishing/charge_coin.mcfunction').read_text(encoding='utf-8-sig')
prices = {}
for material, commands in re.findall(r'clear @s (?:minecraft:)?(\w+)(.*?)(?=execute store result score fishing.coin temp run clear|\Z)', text, re.S):
    amount = re.search(r'scoreboard players set fishing.coin.mutiplied temp (\d+)', commands)
    assert amount, material
    prices[material.upper()] = int(amount[1])
assert prices == rules['coin-values'], 'Source coin prices differ'

def read_table(name):
    return json.loads((source / f'loot_table/fishing/{name}.json').read_text(encoding='utf-8-sig'))['pools'][0]['entries']

groups = read_table('fishing')
assert {row['value'].rsplit('/', 1)[1]: row['weight'] for row in groups} == rules['pool-weights']
treasure = next(row for row in groups if row['value'].endswith('/treasure'))
assert treasure['condition']['chance'] == rules['treasure-condition-chance']
for group in ('fish', 'junk', 'treasure'):
    expected = []
    for row in read_table('fishing/' + group):
        material = row['name'].split(':')[-1].upper()
        # The source enchants the book with 30 levels, changing the actual item to enchanted_book.
        if material == 'BOOK' and row.get('modifier', {}).get('type') == 'minecraft:enchant_with_levels':
            material = 'ENCHANTED_BOOK'
        result = {'material': material, 'weight': row.get('weight', 1)}
        if 'condition' in row:
            assert row['condition']['type'] == 'location_check'
            assert set(row['condition']['predicate']['biomes']) == {'minecraft:jungle', 'minecraft:sparse_jungle', 'minecraft:bamboo_jungle'}
            result['jungle-only'] = True
        expected.append(result)
    assert expected == rules['loot'][group], f'Source {group} weights differ'
print(f'Validated {len(prices)} source prices, all source loot weights, treasure condition and bamboo biomes.')
