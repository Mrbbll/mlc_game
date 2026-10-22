"""Extract declarative task text and translated scene geometry, not executable functions."""
from pathlib import Path
import re,json
root=Path(__file__).resolve().parents[2]
source=Path(r'E:\map\Map-MiniGames\datapacks\map_game_3rd\data\minecraft\function\t_says')
tasks=[]
for line in (source/'tasks/setup.mcfunction').read_text(encoding='utf8').splitlines():
    m=re.search(r'tasks\.(\w+) append value \{id:(-?\d+),msg:\["([^"]+)"\],time:(\d+)',line)
    if m:tasks.append({'group':m[1],'id':int(m[2]),'message':m[3],'seconds':int(m[4]),'negative':'timeout:"all"' in line})
scenes={}
for name in ('parkour_to_the_end','stand_on_block','fight_zombie'):
    rows=[]
    for line in (source/f'scene/start/{name}.mcfunction').read_text(encoding='utf8').splitlines():
        m=re.fullmatch(r'(fill|setblock) (-?\d+) (-?\d+) (-?\d+) (?:(-?\d+) (-?\d+) (-?\d+) )?(?:minecraft:)?([a-z_]+)(?: (.*))?',line)
        if not m:continue
        x,y,z=map(int,m.group(2,3,4));xx,yy,zz=map(int,m.group(5,6,7)) if m[1]=='fill' else (x,y,z)
        rows.append({'first':[x-183,y-22,z-373],'second':[xx-183,yy-22,zz-373],'material':m[8].upper(),'air-only':m[9]=='replace air'})
    scenes[name]=rows
target=root/'src/main/resources/party/reaction/tsays/task-data.json';target.parent.mkdir(parents=True,exist_ok=True)
target.write_text(json.dumps({'schema-version':1,'tasks':tasks,'scenes':scenes},ensure_ascii=False,separators=(',',':')),encoding='utf8')
print('Extracted',len(tasks),'tasks',sum(map(len,scenes.values())),'scene rectangles')
