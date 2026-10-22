"""Cross-check pig-race motion and lap defaults against the supplied source, including approved points."""
from pathlib import Path
import argparse
import yaml

parser = argparse.ArgumentParser()
parser.add_argument('source_map', type=Path)
args = parser.parse_args()
source = args.source_map / 'datapacks/map_game_2nd/data/minecraft/function/car_race'
rules = yaml.safe_load((Path(__file__).resolve().parents[1] / 'src/main/resources/party/racing/car_race/game.yml').read_text(encoding='utf-8'))
def read(name):
    return (source / (name + '.mcfunction')).read_text(encoding='utf-8-sig')

motion = rules['speed']
assert f'matches {motion["cap"]}.. run scoreboard players add @s car.speed {motion["acceleration"]}' in read('move/forward')
assert f'remove @s car.speed {motion["brake"]}' in read('move/backward')
assert f'remove @s car.speed {motion["friction"]}' in read('tick')
assert f'matches {motion["slow-threshold"]}.. run scoreboard players remove @s car.speed {motion["slow-penalty"]}' in read('tick')
assert f'double {motion["attribute-factor"]}' in read('move/car_getmotion')
assert f'car.rounds matches {rules["laps"]}..' in read('around')
assert rules['finish-materials'] == ['RED_WOOL'] and 'red_wool' in read('tick')
assert rules['launch-materials'] == ['LIME_GLAZED_TERRACOTTA'] and 'levitation 1 4' in read('tick')
assert rules['slow-materials'] == ['GLOWSTONE'] and 'glowstone' in read('tick')
assert [rules['finish-points'][f'rank-{rank}'] for rank in range(1,6)] == [3,2,2,1,1]
assert 'gamemode spectator @s' in read('win') and 'sw.people tick matches 0' in read('win')
print('Validated source acceleration/brake/friction/soft cap, three laps, special surfaces and approved 3/2/2/1/1 points.')
