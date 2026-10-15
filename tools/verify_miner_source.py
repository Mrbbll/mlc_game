"""Verify equal-weight source rolls, retaining duplicates and excluding the approved missing roll 62."""
import argparse
import json
import re
from pathlib import Path
import yaml

parser = argparse.ArgumentParser()
parser.add_argument('source_map', type=Path)
args = parser.parse_args()
source = args.source_map / 'datapacks/map_game_2nd/data/minecraft/function/miner'
root = Path(__file__).resolve().parents[1]
text = (source / 'map/set_random_block.mcfunction').read_text(encoding='utf-8-sig')
rows = re.findall(r'matches (\d+) run setblock ~ ~ ~ (?:minecraft:)?([a-z_]+)', text)
expected = [{'roll': int(number), 'material': material.upper()} for number, material in rows]
assert len(expected) == 236
assert set(range(237)) - {row['roll'] for row in expected} == {62}
snapshot = json.loads((root / 'tools/data/miner-palette.json').read_text(encoding='utf-8'))
assert expected == snapshot['entries']
rules = yaml.safe_load((root / 'src/main/resources/party/arcade/miner/game.yml').read_text(encoding='utf-8'))
assert rules['block-palette'] == [row['material'] for row in expected]
positions = re.findall(r'positioned -57 (\d+) 654 run function minecraft:miner/map/set_random_block', (source / 'map/summon_random_pillar.mcfunction').read_text(encoding='utf-8-sig'))
assert len(positions) == rules['random-layers'] == 96
assert list(map(int, positions)) == list(range(50, 146))
print('Validated 236 source palette rolls including duplicate materials, approved reroll of 62 and 96 random layers.')
