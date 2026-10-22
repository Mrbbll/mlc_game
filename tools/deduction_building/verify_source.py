from pathlib import Path
import yaml,re,json
ROOT=Path(__file__).resolve().parents[2]
MAP=Path('E:/map/Map-MiniGames/datapacks')
RES=ROOT/'src/main/resources/party'
GAMES={'deduction':['hideseek','killerever','endless_wolf'],'building':['build_guess','build_parkour'],'cooperation':['bomb','foodparty'],'survival':['pacman','live']}
count=0
for category,ids in GAMES.items():
    for game in ids:
        folder=RES/category/game
        configs={name:yaml.safe_load((folder/(name+'.yml')).read_text(encoding='utf-8')) for name in ['game','language','locations']}
        assert all(config['schema-version']==1 for config in configs.values())
        arena=configs['locations']['maps']['arena_1']
        assert arena['configured'] is False and arena['world']=='' and arena['points']['spawns']==[]
        for region in [arena['bounds'],*arena['regions'].values()]:
            assert set(region)=={'corner-1','corner-2'}
            assert all(v is None for p in region.values() for v in p.values())
        assert all(points==[] for points in arena['points']['groups'].values())
        code='\n'.join(p.read_text(encoding='utf-8') for p in (ROOT/'src/main/java/com/mlc/mlcgames/party'/category/game).glob('*.java'))
        if category=='survival':code+='\n'+(ROOT/'src/main/java/com/mlc/mlcgames/party/survival/chase/ChaseRounds.java').read_text(encoding='utf-8')
        if category=='building':code+='\n'+(ROOT/'src/main/java/com/mlc/mlcgames/party/building/common/BuildingRounds.java').read_text(encoding='utf-8')
        def get(data,path):
            for key in path.split('.'):
                assert key in data,(game,path)
                data=data[key]
            return data
        for key in re.findall(r'\.(?:text|message|broadcast)\("([\w.-]+)"',code):
            if not key.endswith('.'):get(configs['language'],key)
        for key in re.findall(r'\.(?:integer|number|materials|thresholds|itemMaterial)\("([\w.-]+)"\s*[,)]',code):get(configs['game'],key)
        count+=3
source=(MAP/'map_game_3rd/data/minecraft/function/build_guess/during/words.mcfunction').read_text(encoding='utf-8')
words=re.findall(r'^"([^"\r\n]+)"',source,re.M)
assert words==yaml.safe_load((RES/'building/build_guess/game.yml').read_text(encoding='utf-8'))['words']
assert len(words)==592
assert len(yaml.safe_load((RES/'building/build_guess/game.yml').read_text(encoding='utf-8'))['building-materials'])==31
assert len(yaml.safe_load((RES/'building/build_parkour/game.yml').read_text(encoding='utf-8'))['building-materials'])==24
food=yaml.safe_load((RES/'cooperation/foodparty/game.yml').read_text(encoding='utf-8'))
food_language=yaml.safe_load((RES/'cooperation/foodparty/language.yml').read_text(encoding='utf-8'))
food_locations=yaml.safe_load((RES/'cooperation/foodparty/locations.yml').read_text(encoding='utf-8'))['maps']['arena_1']
assert food['max-seconds']==180 and food['order-seconds']==60 and food['cook-seconds']==5 and food['ingredient-cooldown-seconds']==3
assert len(food['recipes'])==9 and {r['tier'] for r in food['recipes'].values()}=={1,2,3}
materials={m for recipe in food['recipes'].values() for m in recipe['ingredients']}
assert set(food_language['ingredient-names'])==materials
assert set(food_locations['points']['groups'])=={'kitchens','customers'}|{'ingredient-'+m.lower() for m in materials}
assert set(food_language['recipes'])==set(food['recipes'])
assert 'normal-kill-suicide' not in yaml.safe_load((RES/'deduction/endless_wolf/game.yml').read_text(encoding='utf-8'))
print(f'Validated {count} YAML configurations, exact 592-word source list, 31/24 building palettes and approved Food Party recipes/timings.')
