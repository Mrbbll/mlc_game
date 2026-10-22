"""Writes only the seven games owned by this migration batch."""
from pathlib import Path
import yaml,json,copy
root=Path(__file__).resolve().parents[2]/'src/main/resources/party'
base_lang=yaml.safe_load((root/'sports/poolwar/language.yml').read_text(encoding='utf8'))
for key in ('name','instructions','items','swimming','resting','healthy'):base_lang.pop(key,None)
base_rules={'schema-version':1,'enabled':True,'players':{'min':2,'max':16},'countdown-seconds':5,'max-seconds':1800,'limits':{'max-arena-blocks':1000000,'max-snapshot-blocks':100000,'blocks-per-tick':1000},'elimination-blocks':[],'elimination-depth':1}
point=lambda:dict(x=None,y=None,z=None)
region=lambda:{'corner-1':point(),'corner-2':point()}
def write(category,id,name,instructions,extra,lang,regions,groups=(),teams=False):
    folder=root/category/id;folder.mkdir(parents=True,exist_ok=True)
    rules=copy.deepcopy(base_rules);rules.update(extra)
    language=copy.deepcopy(base_lang);language.update({'name':name,'instructions':instructions});language.update(lang)
    loc={'schema-version':1,'maps':{'arena_1':{'configured':False,'world':'','bounds':region(),'points':{'waiting':point(),'spectator':point(),'exit':point(),'spawns':[]},'regions':{r:region() for r in regions}}}}
    points=loc['maps']['arena_1']['points']
    if groups:points['groups']={key:[point()] for key in groups}
    if teams:points['team-spawns']={'a':[],'b':[]}
    for file,data in [('game',rules),('language',language),('locations',loc)]:
        (folder/f'{file}.yml').write_text('# 新地图由服主提供；所有角点和特殊点位必须配置后才能启用。\n'+yaml.safe_dump(data,allow_unicode=True,sort_keys=False,width=120),encoding='utf8')

ball_lang={'items':{'boots':'球鞋','force':'力度球鞋','rush':'冲刺 - 右键','jump':'高跳 - 右键','jersey':'队服'},'force':'&a力度: {value}','serve':'&e本次由 {player} 发球，请选择力度后左键击球。','left-serve':'&c发球请使用左键。','score':'&bA {a} : {b} B | 第 {round} 轮 | 剩余 {seconds}秒','round-result':'&e本轮结束，A {a} : {b} B'}
for id,name,regions in [('football','足球',['court','goal-a','goal-b']),('dodgeball','躲避球',['court'])]:
    write('sports',id,name,'&b左键击球，右键停球。冲刺及高跳有4–6秒共用冷却。'+('球进对方球门得分；出界由上次触球队的对手发球。' if id=='football' else '躲避五个移动球，红羊毛出界淘汰。球越界会穿越到对边。'),{'target-score':3,'round-seconds':120,'intermission-seconds':5,'serve-seconds':30},ball_lang,regions,['ball-centre'],True)

write('survival','disastersnow','灾难 · 雪灾','&f躲避天降灾害。69秒速度达到上限，99秒启用PVP，129秒起每5秒缩圈。',{'max-seconds':240,'initial-layers':3},{'status':'&b速度 {speed}/10 | PVP {pvp} | 缩圈 {shrink}','on':'已开启','off':'未开启','items':{'tool':'雪灾道具'}},['snow-volume'])
palette=[f'{c}_TERRACOTTA' for c in ['WHITE','ORANGE','MAGENTA','LIGHT_BLUE','YELLOW','LIME','PINK','GRAY','LIGHT_GRAY','CYAN','PURPLE','BLUE','BROWN','GREEN','RED','BLACK']]
write('reaction','color','色盲派对','&b观察提示方块，及时站到同色地板；其他颜色会消失。17种原图案及第10/15轮进阶保留。',{'decision-seconds':6,'decrease-every-rounds':1,'maximum-decreases':4,'palette':palette},{'target':'&b第{round}轮 | 目标 {target}','warning':'&e第{round}轮 | {seconds}秒后移除非{target}方块'},['floor'])
write('racing','random_parkour','随机跑酷','&b随机生成150–300段路线。金块记录检查点，青金石平台完赛；掉落回最近检查点并清空道具。',{'max-seconds':1201,'limits':{'max-arena-blocks':1000000,'max-snapshot-blocks':300000,'blocks-per-tick':1000},'generation':{'minimum-steps':150,'maximum-steps':300}},{'checkpoint':'&a已保存检查点。','retry':'&e已返回最近检查点或起点。','status':'&b剩余 {seconds}秒 | 路线生成剩余 {steps} 段','finisher':'&a{player} 第{place}名完成跑酷。','items':{'boots':'跑酷鞋','loot':'跑酷道具'}},['generator'],['generator-start','retry-start'])
data=json.loads((root/'reaction/tsays/task-data.json').read_text(encoding='utf8'))
write('reaction','tsays','不要相信T氏的话','&d有“T氏说”要反着做，没有则照着做。7组70任务均等分组抽取；每轮按完成顺序赚3/2/1金币，最终按金币排名。',{'rounds':20,'max-seconds':1800},{'round':'&b第 {round}/{total} 轮','task':'&e{prefix}{task}','prefix':'T氏说：','success':'&a完成！获得 {coins} 金币。','failure':'&c本轮失败。','tasks':{str(t['id']):t['message'] for t in data['tasks']}},['scene'],['scene-origin','parkour-start','husk-spawns'])
write('combat','sheepwars','绵羊突击队','&b木剑与弓近战，搭建或破坏场地。右键发射有重力绵羊；按F发射直线绵羊。爆炸、高爆、骑乘、治疗、地震和黑洞六种供给；全灭敌队获胜。',{'max-seconds':1800,'limits':{'max-arena-blocks':1000000,'max-snapshot-blocks':100000,'blocks-per-tick':1000,'maximum-sheep':256}},{'status':'&b比赛进行 {seconds}秒','items':{'sword':'木剑','bow':'弓','chest':'防爆队服','boots':'防摔鞋','small':'爆炸绵羊','big':'高爆绵羊','rider':'云霄绵羊','health':'治疗绵羊','earthquake':'地震绵羊','blackhole':'黑洞绵羊'}},['terrain'],teams=True)
print('Wrote 21 configuration files for 7 games')
