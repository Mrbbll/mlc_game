"""Maintainer-only extraction of geometric data. Never used by the plugin at runtime."""
from pathlib import Path
import re,struct,zlib,json

SOURCE=Path(r'E:\map\Map-MiniGames')
ROOT=Path(__file__).resolve().parents[2]
FUNCTIONS=SOURCE/'datapacks/map_all/data/minecraft/function/color/ran_fill'

def patterns():
    result={}
    for file in FUNCTIONS.glob('*_?.mcfunction'):
        if not re.fullmatch(r'(?:[6-8]|1[0-7])_[a-f]',file.stem):continue
        cells=[1]*1296
        for line in file.read_text(encoding='utf-8').splitlines():
            match=re.fullmatch(r'\$fill (-?\d+) 17 (-?\d+) (-?\d+) 17 (-?\d+) \$\(c(\d+)\)',line.strip())
            if not match:continue
            x,z,xx,zz,label=map(int,match.groups())
            for px in range(min(x,xx),max(x,xx)+1):
                for pz in range(min(z,zz),max(z,zz)+1):
                    assert -5<=px<=30 and 77<=pz<=112
                    cells[(pz-77)*36+px+5]=label
        assert max(cells)>1,file
        result[file.stem]=cells
    for width in range(1,5):
        cells=[0]*1296;label=0
        for line in (FUNCTIONS/'1_init.mcfunction').read_text(encoding='utf8').splitlines():
            m=re.fullmatch(r'execute positioned (-?\d+) 17 (-?\d+) if score color.ran.blockwidth board matches (\d+)(?:\.\.(\d+))? run function minecraft:color/ran_fill/2_place',line.strip())
            if not m:continue
            x,z,lo,hi=m.groups();x,z,lo=int(x),int(z),int(lo);hi=int(hi) if hi else lo
            if not lo<=width<=hi:continue
            label+=1
            for xx in range(x-width+1,x+1):
                for zz in range(z,z+width):
                    if -5<=xx<=30 and 77<=zz<=112:cells[(zz-77)*36+xx+5]=label
        result['1_'+str(width)]=cells
    for kind in (3,4):
        cells=[0]*1296;label=0
        for m in re.finditer(r'data merge storage minecraft:temp \{x1:(-?\d+),x2:(-?\d+),y1:17,y2:17,z1:(-?\d+),z2:(-?\d+)\}',(FUNCTIONS/f'{kind}_init.mcfunction').read_text(encoding='utf8')):
            x,xx,z,zz=map(int,m.groups());label+=1
            for px in range(min(x,xx),max(x,xx)+1):
                for pz in range(min(z,zz),max(z,zz)+1):cells[(pz-77)*36+px+5]=label
        result[str(kind)+'_1']=cells
    return result

class NBT:
    def __init__(self,b):self.b=b;self.p=0
    def take(self,n):v=self.b[self.p:self.p+n];self.p+=n;return v
    def unpack(self,f):return struct.unpack('>'+f,self.take(struct.calcsize('>'+f)))[0]
    def string(self):return self.take(self.unpack('H')).decode('utf8')
    def payload(self,t):
        if t==1:return self.unpack('b')
        if t==2:return self.unpack('h')
        if t==3:return self.unpack('i')
        if t==4:return self.unpack('q')
        if t==5:return self.unpack('f')
        if t==6:return self.unpack('d')
        if t==7:return self.take(self.unpack('i'))
        if t==8:return self.string()
        if t==9:
            subtype=self.unpack('B');return [self.payload(subtype) for _ in range(self.unpack('i'))]
        if t==10:
            d={}
            while (subtype:=self.unpack('B'))!=0:
                name=self.string();d[name]=self.payload(subtype)
            return d
        if t==11:return [self.unpack('i') for _ in range(self.unpack('i'))]
        if t==12:return [self.unpack('Q') for _ in range(self.unpack('i'))]
        raise ValueError(t)
    def root(self):t=self.unpack('B');self.string();return self.payload(t)

def gradients():
    regions={};cache={}
    def block(x,y,z):
        chunk=(x//16,z//16)
        if chunk not in cache:
            rk=(x//512,z//512)
            if rk not in regions:regions[rk]=(SOURCE/f'dimensions/minecraft/overworld/region/r.{rk[0]}.{rk[1]}.mca').read_bytes()
            region=regions[rk];idx=4*(chunk[0]%32+(chunk[1]%32)*32)
            off=int.from_bytes(region[idx:idx+3],'big')*4096
            size=int.from_bytes(region[off:off+4],'big');assert region[off+4]==2
            cache[chunk]=NBT(zlib.decompress(region[off+5:off+4+size])).root()
        section=next(s for s in cache[chunk]['sections'] if s['Y']==y//16)
        state=section['block_states'];palette=state['palette']
        def name(entry):return entry if isinstance(entry,str) else entry.get('Name',entry.get(''))
        if len(palette)==1:return name(palette[0])
        bits=max(4,(len(palette)-1).bit_length());per=64//bits;index=(y%16)*256+(z%16)*16+x%16
        return name(palette[(state['data'][index//per]>>((index%per)*bits))&((1<<bits)-1)])
    templates=[[block(x,y,z).removeprefix('minecraft:').upper() for z in range(75,115) for x in range(36,76)] for y in range(2,19)]
    palette=[block(x,35,z).removeprefix('minecraft:').upper() for x in range(-55,-51) for z in range(53,60)]+[block(-56,35,z).removeprefix('minecraft:').upper() for z in range(56,60)]
    return templates,palette

if __name__=='__main__':
    target=ROOT/'src/main/resources/party/reaction/color/pattern-data.json';target.parent.mkdir(parents=True,exist_ok=True)
    templates,palette=gradients()
    data={'schema-version':1,'patterns':patterns(),'gradients':templates,'source-palette':palette}
    target.write_text(json.dumps(data,separators=(',',':')),encoding='utf8')
    print('Extracted',len(data['patterns']),'geometries and',len(data['gradients']),'gradient templates')
