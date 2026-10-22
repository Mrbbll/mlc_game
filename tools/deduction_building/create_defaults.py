from pathlib import Path
import re,json,yaml,copy
ROOT=Path(__file__).resolve().parents[2]
MAP=Path('E:/map/Map-MiniGames/datapacks')
RES=ROOT/'src/main/resources/party'
BASE=yaml.safe_load((RES/'exploration/finder/language.yml').read_text(encoding='utf-8'))
COMMON={k:v for k,v in BASE.items() if k in ['schema-version','disabled','map-unconfigured','already-running','full','teleport-failed','joined','not-enough','preparing','countdown','started','eliminated','draw','finished','legacy-busy']}
def point():return dict(x=None,y=None,z=None,yaw=0,pitch=0)
def region():return {'corner-1':dict(x=None,y=None,z=None),'corner-2':dict(x=None,y=None,z=None)}
def output(category,id,name,instructions,rules,lang,groups,regions,header='# 原地图逻辑移植；所有地图坐标由服主填写。'):
    root=RES/category/id;root.mkdir(parents=True,exist_ok=True)
    default={'schema-version':1,'enabled':True,'players':{'min':2,'max':16},'countdown-seconds':5,'max-seconds':7200,'limits':{'max-arena-blocks':1000000,'max-snapshot-blocks':100000,'blocks-per-tick':1000},'elimination-blocks':[],'elimination-depth':1}
    default.update(rules)
    language=copy.deepcopy(COMMON);language.update({'name':name,'instructions':instructions});language.update(lang)
    locations={'schema-version':1,'maps':{'arena_1':{'configured':False,'world':'','bounds':region(),'points':{'waiting':point(),'spectator':point(),'exit':point(),'spawns':[],'groups':{name:[] for name in groups}},'regions':{name:region() for name in regions}}}}
    for filename,data in [('game',default),('language',language),('locations',locations)]:
        (root/(filename+'.yml')).write_text(header+'\n'+yaml.safe_dump(data,allow_unicode=True,sort_keys=False),encoding='utf-8')
def palette(game):
    source=(MAP/'map_game_3rd/data/minecraft/function'/game/'during/player/items.mcfunction') if game=='build_parkour' else MAP/'map_game_3rd/data/minecraft/function/build_guess/during/player/items_builder.mcfunction'
    names=[]
    for line in source.read_text(encoding='utf-8').splitlines():
        if line.startswith('#'):continue
        match=re.search(r'run give @s (?:minecraft:)?([a-z_]+)',line)
        if match and match[1] not in ['diamond_pickaxe','bat_spawn_egg','chicken_spawn_egg'] and match[1].upper() not in names:names.append(match[1].upper())
    return names
buildlang={'builder-turn':'&b轮到 {player} 建造了。','coins':'&e金币 +{amount}，当前 {total}。','ranking':'&6第 {rank} 名：{player}（{coins} 金币）','items':{'tool':'&b建造工具'}}
guesslang=copy.deepcopy(buildlang)
guesslang['items'].update({'book':'&b书与笔：在第一页写下猜测并保存，无需署名','helmet':'&b建造辅助头盔'})
guesslang.update({'secret-word':'&e你的词汇：{word}。建造让其他玩家猜出，禁止直接泄露原文。','word-length':'&b本轮词汇长度：{length}。使用书与笔作答。','builder-status':'&b剩余 {seconds} 秒，词汇：{word}','guesser-status':'&b剩余 {seconds} 秒，提示：{tip}','guessed':'&a{player} 猜对了！','guess-wrong':'&b{player} 的猜测是 {guess}，未猜中。','answer':'&6本轮结束，答案：{word}。'})
wordfile=MAP/'map_game_3rd/data/minecraft/function/build_guess/during/words.mcfunction'
words=re.findall(r'^"([^"\r\n]+)"',wordfile.read_text(encoding='utf-8'),re.M)
output('building','build_guess','建筑猜猜乐','&b轮流建造指定词汇，其他人使用书与笔猜测；按金币排名。',{'builder-limit':0,'round-break-seconds':3,'max-seconds':86400,'guess-seconds':0,'building-materials':palette('build_guess'),'words':words},guesslang,['builder','runners'],['build'])
parklang=copy.deepcopy(buildlang)
parklang['items'].update({'trident-pad':'&b放置含水三叉戟方块','finish':'&d放置终点并开始自证','trident':'&b激流三叉戟'})
parklang.update({'builder-rules':'&a建造自己能通过的跑酷，放置终点后有120秒自证。红、黑混凝土会失败；含水海晶半砖获得激流三叉戟，脚手架自证时移除。','runner-rules':'&e只有一次机会：抵达粉色羊毛终点得1金币。','overtime':'&c建造超时，仅剩30秒放置终点！','prove':'&b建造已完成，建造者开始自证跑酷。','run':'&a建造者自证成功，其他玩家开始挑战。','completed':'&a{player} 完成了跑酷。','failed':'&c{player} 挑战失败。','builder-failed':'&c建造者未能完成自证，其他玩家各获得1金币。','status':'&b{phase}：剩余 {seconds} 秒。','phases':{'build':'建造','overtime':'放置终点','prove':'建造者自证','run':'玩家挑战'}})
output('building','build_parkour','你建我跑','&b轮流建造跑酷并自证，其他玩家一次挑战；有人失败则建造者得金币。',{'builder-limit':0,'round-break-seconds':3,'max-seconds':86400,'build-seconds':0,'prove-seconds':120,'run-seconds':120,'overtime-seconds':30,'building-materials':palette('build_parkour')},parklang,['builder','runners'],['build'])
output('cooperation','bomb','拆弹专家','&b逐人挑战六个关卡，利用羊毛阻挡振动，避免触发幽匿尖啸体。成功拆除最低层所有幽匿方块，获得200减当前羊毛数的成绩。',{'max-seconds':172800,'all-levels':True,'selected-level':1,'turn-seconds':300,'reset-cooldown-seconds':2},{'turn':'&6第 {level} 关，轮到 {player}，准备后开始拆弹。','retry':'&e第 {level} 关已重置，剩余时限继续计算。','turn-timeout':'&c{player} 本关回合结束。','detonated':'&c尖啸体已被触发，拆弹失败，重新尝试！','defused':'&a{player} 拆弹成功：{score} 分，总成绩 {total}。','ranking':'&e{player} 总成绩：{score}。','status':'&b第 {level} 关，{player}，剩余 {seconds} 秒。','items':{'hoe':'&b幽匿铲除器','wool':'&a幽匿阻碍器','retry':'&b重来：长按使用','give-up':'&c放弃本关：长按使用'}},['level-starts'],[f'level-{i}' for i in range(1,7)])
foodlang={'order-opened':'&e订单 {slot}：{recipe}，剩余 {seconds} 秒。','order-expired':'&c订单 {slot} 已超时，正在刷新。','collection-cooldown':'&e这个食材点还需 {seconds} 秒恢复。','collected':'&a收集到：{ingredient}。','inventory-full':'&c背包空间不足。','preparation-busy':'&c你已有一道菜正在制作或等待交付。','no-ready-recipe':'&c当前工作台没有可制作的订单，或食材不足。','preparing-recipe':'&e正在制作 {recipe}，需要 {seconds} 秒。','prepared':'&a{recipe} 已完成。手持成品前往交付柜台。','wrong-delivery':'&c当前订单不需要 {recipe}，成品和食材都已保留。','missing-delivery-materials':'&c食材不足，未交付；成品保持不变。','delivered':'&a交付 {recipe}：配方 {base} 分，时效 {bonus} 分，共 +{gained} 分（当前 {total}）。','status-order':'{slot}:{recipe} {seconds}s','status':'&e得分 {score} | {orders} | 剩余 {seconds}s','final-ranking':'&6第 {rank} 名：{player}（{score} 分，派对 +{points}）','ingredient':{'wheat':'小麦','carrot':'胡萝卜','potato':'土豆','beef':'牛肉','chicken':'鸡肉','egg':'鸡蛋','sugar':'糖','milk':'牛奶','cod':'鳕鱼','apple':'苹果'}}
foodlang['order-opened']='&e订单 {slot}：{recipe}；工作台：{station}；食材：{materials}；剩余 {seconds} 秒。'
foodlang['station']={'oven':'烤炉','grill':'烧烤架','prep':'备料台','mix':'调饮台'}
foodrecipes=[{'id':'bread','name':'面包','dish':'BREAD','tier':1,'station':'oven','ingredients':{'wheat':2}},{'id':'salad','name':'苹果胡萝卜沙拉','dish':'RABBIT_STEW','tier':1,'station':'prep','ingredients':{'carrot':1,'apple':1}},{'id':'grilled_chicken','name':'烤鸡','dish':'COOKED_CHICKEN','tier':1,'station':'grill','ingredients':{'chicken':1}},{'id':'baked_potato','name':'烤土豆','dish':'BAKED_POTATO','tier':1,'station':'oven','ingredients':{'potato':2}},{'id':'beef_burger','name':'牛肉汉堡','dish':'COOKED_BEEF','tier':2,'station':'grill','ingredients':{'beef':1,'wheat':1,'carrot':1}},{'id':'apple_pie','name':'苹果派','dish':'PUMPKIN_PIE','tier':2,'station':'oven','ingredients':{'wheat':2,'apple':1,'sugar':1,'egg':1}},{'id':'fish_platter','name':'烤鱼拼盘','dish':'COOKED_COD','tier':2,'station':'grill','ingredients':{'cod':1,'potato':1}},{'id':'milk_pudding','name':'牛奶布丁','dish':'HONEY_BOTTLE','tier':3,'station':'mix','ingredients':{'milk':1,'sugar':1,'egg':1}},{'id':'feast_platter','name':'丰盛拼盘','dish':'GOLDEN_CARROT','tier':3,'station':'prep','ingredients':{'beef':1,'chicken':1,'cod':1,'carrot':1,'apple':1}}]
foodrules={'max-seconds':180,'run-seconds':180,'order-count':3,'order-seconds':60,'collection-cooldown-seconds':3,'preparation-seconds':5,'collection-block':'HAY_BLOCK','delivery-block':'BARREL','tier-points':{1:5,2:10,3:15},'ingredients':{'wheat':'WHEAT','carrot':'CARROT','potato':'POTATO','beef':'BEEF','chicken':'CHICKEN','egg':'EGG','sugar':'SUGAR','milk':'MILK_BUCKET','cod':'COD','apple':'APPLE'},'stations':{'oven':'FURNACE','grill':'CAMPFIRE','prep':'CRAFTING_TABLE','mix':'BREWING_STAND'},'recipes':foodrecipes}
foodlang['ingredient-names']={'WHEAT':'小麦','POTATO':'马铃薯','BEETROOT':'甜菜根','BOWL':'碗','PUMPKIN':'南瓜','SUGAR':'糖','EGG':'鸡蛋','COCOA_BEANS':'可可豆','RED_MUSHROOM':'红蘑菇','BROWN_MUSHROOM':'棕蘑菇','MILK_BUCKET':'牛奶桶','BAKED_POTATO':'烤马铃薯','COOKED_RABBIT':'熟兔肉','CARROT':'胡萝卜','DANDELION':'蒲公英'}
output('cooperation','foodparty','美食派对','&b每人独立经营180秒：收集食材、按订单到工作台制作，再到柜台交付。错误交付保留成品与食材。',foodrules,foodlang,['collect-'+key for key in foodrules['ingredients']]+['station-'+key for key in foodrules['stations']]+['delivery'],[],header='# 插件补全玩法；原数据包没有采集、配方、顾客评估或计分规则。')
# Expand map-defined tags, retain vanilla tags for Bukkit resolution.
tagpaths={}
for file in MAP.rglob('tags/block/*.json'):tagpaths.setdefault(file.stem,file)
def expand(values,seen=()):
    out=[]
    for value in values:
        if isinstance(value,dict):value=value['id']
        if value.startswith('#'):
            name=value[1:].split(':')[-1]
            file=tagpaths.get(name)
            if file and name not in seen:out.extend(expand(json.loads(file.read_text(encoding='utf-8'))['values'],seen+(name,)))
            else:out.append(('#minecraft:concrete_powder' if name=='concrete_powders' else value) if ':' in value else '#minecraft:'+value[1:])
        else:out.append(value.split(':')[-1].upper())
    return list(dict.fromkeys(out))
hide=json.loads((MAP/'map_all/data/minecraft/tags/block/hideseek.json').read_text(encoding='utf-8'))['values']
output('deduction','hideseek','方块躲猫猫','&b躲藏者伪装脚下方块、制造分身；猎人等待30秒后搜寻。躲藏者阵亡转为猎人，猎人阵亡后重返战场。',{'max-seconds':600,'seconds-per-player':15,'glow-interval-seconds':60,'hide-seconds':30,'decoy-count':41,'disguise-blocks':expand(hide)},{'hunter-role':'&c你的身份：猎人。等待躲藏时间结束后出动。','hider-role':'&b你的身份：躲藏者。潜行令伪装对齐方块。','released':'&c猎人出动！','disguised':'&a已伪装为 {material}。','unsupported':'&c脚下方块不支持伪装。','converted':'&c{player} 被找到，变成了猎人。','cooldown':'&c技能仍在冷却。','hide-status':'&b猎人将在 {seconds} 秒后出动。','nearby-status':'&c检测到附近有对方玩家！剩余 {seconds} 秒，躲藏者 {hiders}。','status':'&b剩余 {seconds} 秒，躲藏者 {hiders}。','items':{'sword':'&c猎人之剑','magic':'&b魔法箭：右键，冷却20秒','stick':'&b击退棒','disguise':'&b模仿脚下方块：右键，冷却5秒','decoy':'&b放置假方块：右键，冷却20秒','dash':'&b冲刺飞行：右键，冷却20秒'}},['hunter-hold','hunter-spawns','decoys'],[])
output('deduction','killerever','密室杀手','&b杀手消灭其他阵营，警探和平民找出杀手；十金换弓箭，警探阵亡后可继承弓。',{'max-seconds':500,'weapon-delay-seconds':10,'normal-seconds':0,'final-seconds':60},{'killer-role':'&c身份：杀手。击杀所有非杀手玩家。','detective-role':'&b身份：警探。找出杀手并击杀。','normal-role':'&a身份：平民。收集金币保护自己。','killer-allies':'&c杀手队友：{players}。','armed':'&c杀手已获得武器，请小心！','finale':'&c最终决战开始，所有玩家发光！','wait-status':'&e武器将在 {seconds} 秒后发放。','status':'&b剩余 {seconds} 秒，场上 {remaining} 人。','not-enough-gold':'&c金锭不足。','shop-cooldown':'&c炼药锅正在冷却。','wrong-kill':'&c你击杀了同为平民阵营的玩家，因此也被淘汰。','bow-dropped':'&6一名警探阵亡，弓已掉落，可右键继承！','became-detective':'&b你继承了警探的弓。','items':{'knife':'&c铁剑：右键发射剑气，冷却10秒','crossbow':'&b警探弩','bow':'&a平民弓','mystery':'&d神秘药水',**{f'potion-{i}':v for i,v in enumerate(['&b速度药水','&7挖掘疲劳药水','&9夜视药水','&e急迫药水','&f隐身药水','&6无敌药水','&a跳跃药水'])}}},['gold-spawns','potion-shops','wind-shops'],['shops'])
output('deduction','endless_wolf','吸血鬼之夜','&b吸血鬼在夜晚获得武器，白天躲避阳光；幸存者在地图寻找道具，存活规定天数。',{'max-seconds':1800,'day-seconds':60,'night-seconds':60,'days':3,'loot-interval-seconds':4},{'vampire-role':'&c身份：吸血鬼。白天避开阳光，夜晚获得武器；圣物会灼伤你。','survivor-role':'&a身份：幸存者。寻找道具，存活到救援或消灭所有吸血鬼。','day':'&e天亮了，距离救援还剩 {days} 天。','night':'&c黑夜降临，吸血鬼已获得武器！','sunburn':'&c阳光正在灼伤你，寻找遮蔽物或头盔！','holy-burn':'&c你身上有圣物，每秒受到伤害，快丢弃！','night-status':'&c夜晚：距离天亮 {seconds} 秒。','day-status':'&e白天：距离天黑 {seconds} 秒。','items':{'symbol':'&c吸血鬼象征：右键装备于头部，白天遮阳，夜晚增强移动','sword':'&4吸血鬼之剑','compass':'&f追踪指南针：最近的幸存者','holy-sword':'&e被圣水浸泡过的石剑','cross':'&e充分吸收过阳光、浸泡过圣水、含有大蒜的银质十字架','holy-shield':'&e被圣水浸泡过的盾牌'}},['loot-spawns'],[])
print('Wrote six games, words:',len(words), 'palettes:',len(palette('build_guess')),len(palette('build_parkour')))
# Additional round-based games and approved Food Party reconstruction.
chaselang={'round-pause':'&e下一轮将在 {seconds} 秒后开始，被抓玩家将复活。','hunter-role':'&c本轮你是杀手，阻止逃跑者得分。','runner-role':'&e本轮你是逃跑者，小心杀手！','coin-change':'&6金币变化 {amount}，当前 {coins}。','caught':'&c{player} 被抓住了，下轮复活。','status':'&b本轮剩余 {seconds} 秒，金币 {coins}。','ranking':'&6第 {rank} 名：{player}（{coins} 金币）'}
paclang=copy.deepcopy(chaselang);paclang['items']={'red-helmet':'&c杀手红帽：移速增加、禁止跳跃、1.5格抓捕','yellow-helmet':'&e吃豆人黄帽：禁止跳跃、1.5格触碰'}
output('survival','pacman','吃豆人','&e踩金块收豆子，杀手抓到人得5豆，被抓者失去最多5豆。逃跑者反击杀手可将其送回出生点。每人最多一次杀手，按总豆子排名。',{'max-seconds':7200,'round-seconds':60,'between-round-seconds':6},paclang,['hunters'],['floor'])
livelang=copy.deepcopy(chaselang);livelang['items']={'riptide':'&b激流I三叉戟','loyalty':'&b忠诚I三叉戟','elytra':'&b鞘翅','sword':'&c追杀者利剑','axe':'&c追杀者之斧','chestplate':'&b追杀者胸甲','stick':'&e击退棒','plate':'&f重质压力板：仅放于史莱姆块','speed':'&f速度I（10秒）','jump':'&f跳跃III（10秒）'}
output('survival','live','追杀游戏','&b轮流担任追杀者，每人最多一次。活到轮末得4金币，最后3位被抓者得1/2/3金币。杀光所有逃跑者，追杀者各得4金币。',{'max-seconds':7200,'round-seconds':60,'between-round-seconds':6,'map-type':'rainbow'},livelang,['hunters','supply'],['play'])
recipe_data={
 'bread':('BREAD',1,{'WHEAT':3},'面包'),
 'baked_potato':('BAKED_POTATO',1,{'POTATO':1},'烤马铃薯'),
 'beetroot_soup':('BEETROOT_SOUP',1,{'BEETROOT':6,'BOWL':1},'甜菜汤'),
 'pumpkin_pie':('PUMPKIN_PIE',2,{'PUMPKIN':1,'SUGAR':1,'EGG':1},'南瓜派'),
 'cookie':('COOKIE',2,{'WHEAT':2,'COCOA_BEANS':1},'曲奇'),
 'mushroom_stew':('MUSHROOM_STEW',2,{'RED_MUSHROOM':1,'BROWN_MUSHROOM':1,'BOWL':1},'蘑菇煲'),
 'cake':('CAKE',3,{'WHEAT':3,'SUGAR':2,'EGG':1,'MILK_BUCKET':3},'蛋糕'),
 'rabbit_stew':('RABBIT_STEW',3,{'BAKED_POTATO':1,'COOKED_RABBIT':1,'CARROT':1,'BROWN_MUSHROOM':1,'BOWL':1},'兔肉煲'),
 'suspicious_stew':('SUSPICIOUS_STEW',3,{'RED_MUSHROOM':1,'BROWN_MUSHROOM':1,'BOWL':1,'DANDELION':1},'谜之炖菜'),
}
foodrules={'max-seconds':180,'order-seconds':60,'cook-seconds':5,'ingredient-cooldown-seconds':3,'recipes':{key:{'result':row[0],'tier':row[1],'ingredients':row[2]} for key,row in recipe_data.items()}}
ingredients=list(dict.fromkeys(material for row in recipe_data.values() for material in row[2]))
foodlang={'customer-name':'&e顾客 {number}','order':'&b顾客 {number}：{dish}，剩余 {seconds} 秒。','order-expired':'&e顾客 {number} 的订单已超时，已刷新。','wrong-dish':'&e顾客 {number} 需要 {dish}，剩余 {seconds} 秒；未扣除材料或成品。','served':'&a完成顾客 {number} 的订单，+{points} 分，总分 {score}。','ingredient-cooldown':'&e该材料站正在冷却，请稍候。','inventory-full':'&c背包已满，先腾出空间。','menu-title':'&6厨房：点击选择配方','recipe-value':'&e基础分：{points}','recipe-ingredient':'&7{material} ×{count}','already-cooking':'&e你已有一道菜正在制作。','missing-ingredients':'&c材料不足，未扣除任何材料。','cooking-started':'&e开始制作 {dish}，请等待5秒。','cooked':'&a{dish} 已制作完成！手持成品右键相应顾客交单。','status':'&6得分 {score}，剩余 {seconds} 秒，制作：{cooking}','idle':'空闲','ranking':'&6第 {rank} 名：{player}（{score}分）','recipes':{key:row[3] for key,row in recipe_data.items()}}
foodlang['ingredient-names']={'WHEAT':'小麦','POTATO':'马铃薯','BEETROOT':'甜菜根','BOWL':'碗','PUMPKIN':'南瓜','SUGAR':'糖','EGG':'鸡蛋','COCOA_BEANS':'可可豆','RED_MUSHROOM':'红蘑菇','BROWN_MUSHROOM':'棕蘑菇','MILK_BUCKET':'牛奶桶','BAKED_POTATO':'烤马铃薯','COOKED_RABBIT':'熟兔肉','CARROT':'胡萝卜','DANDELION':'蒲公英'}
output('cooperation','foodparty','美食派对','&b采集材料，在厨房点击配方制作美食，手持成品右键顾客交单。每人3个独立60秒订单，180秒内按总得分排名。',foodrules,foodlang,['kitchens','customers',*[f'ingredient-{material.lower()}' for material in ingredients]],[])
print('Wrote Pacman, Live and approved Food Party reconstruction.')
