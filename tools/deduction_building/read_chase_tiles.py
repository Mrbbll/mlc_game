from pathlib import Path
import struct,zlib,io,json
class Nbt:
 def __init__(self,b):self.f=io.BytesIO(b)
 def n(self,fmt):return struct.unpack('>'+fmt,self.f.read(struct.calcsize('>'+fmt)))[0]
 def text(self):return self.f.read(self.n('H')).decode('utf-8',errors='replace')
 def value(self,t):
  if t in range(1,7):return self.n({1:'b',2:'h',3:'i',4:'q',5:'f',6:'d'}[t])
  if t==7:return list(self.f.read(self.n('i')))
  if t==8:return self.text()
  if t==9:
   sub=self.n('B');return [self.value(sub) for _ in range(self.n('i'))]
  if t==10:
   v={}
   while (sub:=self.n('B'))!=0:
    name=self.text();v[name]=self.value(sub)
   return v
  if t in (11,12):return [self.n('i' if t==11 else 'q') for _ in range(self.n('i'))]
 def root(self):
  t=self.n('B');self.text();return self.value(t)
def chunks(path):
 raw=path.read_bytes()
 for i in range(1024):
  entry=int.from_bytes(raw[i*4:i*4+4],'big');sector=entry>>8
  if not sector:continue
  at=sector*4096;size=int.from_bytes(raw[at:at+4],'big');kind=raw[at+4];payload=raw[at+5:at+4+size]
  if kind==2:payload=zlib.decompress(payload)
  elif kind==1:import gzip;payload=gzip.decompress(payload)
  yield Nbt(payload).root()
root=Path('E:/map/Map-MiniGames/dimensions/minecraft/overworld/region')
rows=[]
for name in ['r.0.0.mca','r.-1.-1.mca']:
 for chunk in chunks(root/name):
  for tile in chunk.get('block_entities',chunk.get('Level',{}).get('TileEntities',[])):
   if not 'Command' in tile:continue
   x,z=tile.get('x',0),tile.get('z',0)
   if (150<=x<=230 and 220<=z<=270) or (-185<=x<=-110 and -290<=z<=-220):rows.append({k:tile[k] for k in ['x','y','z','Command'] if k in tile})
Path('docs/party/chase-command-evidence.json').write_text(json.dumps(rows,ensure_ascii=False,indent=2),encoding='utf-8')
for row in rows:print(json.dumps(row,ensure_ascii=False))
