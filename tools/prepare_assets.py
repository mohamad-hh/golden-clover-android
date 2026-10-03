#!/usr/bin/env python3
"""Production extraction/encoding of original generated game sprite sheets.
No reference screenshot is used by this pipeline. Pillow is required only for authoring.
"""
import argparse,json,hashlib
from pathlib import Path
from PIL import Image,ImageFilter,ImageDraw
p=argparse.ArgumentParser();p.add_argument('--symbols',required=True);p.add_argument('--decor',required=True);p.add_argument('--ui',required=True);p.add_argument('--background',required=True);p.add_argument('--logo');p.add_argument('--pot');args=p.parse_args()
root=Path(__file__).resolve().parents[1]/'app/src/main/assets';(root/'symbols').mkdir(exist_ok=True);(root/'art').mkdir(exist_ok=True)
def cell(path,cols,rows,n):
 im=Image.open(path).convert('RGBA');w,h=im.size;return im.crop((round((n%cols)*w/cols),round((n//cols)*h/rows),round((n%cols+1)*w/cols),round((n//cols+1)*h/rows)))
def save(im,path,size=None):
 if size:im=im.resize(size,Image.Resampling.LANCZOS)
 im.save(root/path,'WEBP',lossless=True,method=6)
 # Verify each encoded production asset immediately.
 with Image.open(root/path) as test:test.load()
def trimmed(im):
 box=im.getchannel('A').point(lambda a:255 if a>20 else 0).getbbox();return im.crop(box) if box else im
names=['bell','seven','clover','lemon','orange','grapes','watermelon','diamond','pot']
for i,n in enumerate(names):
 im=Image.open(args.symbols).convert('RGBA');sw,sh=im.size; regions=[(0,0,1/3,.359),(1/3,0,2/3,.359),(2/3,0,1,.359),(0,.359,1/3,.662),(1/3,.359,2/3,.662),(2/3,.359,1,.650),(0,.662,1/3,1),(1/3,.662,2/3,1),(2/3,.650,1,1)];box=regions[i];im=im.crop(tuple(round(v*(sw if j%2==0 else sh)) for j,v in enumerate(box)))
 if n=='pot' and args.pot:im=Image.open(args.pot).convert('RGBA').resize((418,439),Image.Resampling.LANCZOS)
 save(im,Path('symbols')/(n+'.webp'))
 # Win sprites include a separate alpha glow layer, baked for predictable Android blending.
 glow=Image.new('RGBA',im.size,(255,213,64,0));glow.putalpha(im.getchannel('A').filter(ImageFilter.GaussianBlur(14)).point(lambda a:int(a*.85)));glow.alpha_composite(im);save(glow,Path('symbols')/(n+'_win.webp'))
decor=['tree_trunk','tree_blue','tree_red','tree_green','clover_clusters','coin','panel','spin_button','spark']
items={n:cell(args.decor,3,3,i) for i,n in enumerate(decor)}
for n in decor:
 if n!='panel':save(items[n],Path('art')/(n+'.webp'))
uinames=['logo','reel_frame','grand_panel','major_panel','mini_panel','rays']
for i,n in enumerate(uinames):
 im=trimmed(cell(args.ui,3,2,i))
 if n=='reel_frame':im=im.crop((32,0,im.width,im.height))
 if n=='logo' and args.logo:im=trimmed(Image.open(args.logo).convert('RGBA'))
 save(im,Path('art')/(n+'.webp'))
for n in ['bet_panel','credit_panel','win_panel','auto_button','sound_button','menu_button','bonus_button','minor_panel']:save(trimmed(items['panel']),Path('art')/(n+'.webp'))
save(Image.open(root/'art/reel_frame.webp'),Path('art/bonus_panel.webp'))
save(Image.open(root/'symbols/bell.webp'),Path('art/bonus_bell.webp'))
save(Image.open(args.background).convert('RGB'),Path('art/background.webp'))
# Lightweight independent cell and additive glow textures, never used as game symbols.
im=Image.new('RGBA',(256,224),(0,0,0,0));d=ImageDraw.Draw(im);d.rounded_rectangle((1,1,254,222),radius=12,fill=(4,26,17,245),outline=(100,100,44,150),width=2);save(im,Path('art/reel_cell.webp'))
im=Image.new('RGBA',(256,256));pix=im.load()
for y in range(256):
 for x in range(256):
  dist=((x-128)**2+(y-128)**2)**.5/128;pix[x,y]=(152,255,85,int(165*max(0,1-dist)**2))
save(im,Path('art/glow.webp'))
manifest={str(f.relative_to(root)):{'bytes':f.stat().st_size,'sha256':hashlib.sha256(f.read_bytes()).hexdigest()} for f in sorted(root.rglob('*.webp'))}
(root/'graphics-manifest.json').write_text(json.dumps(manifest,indent=2)+'\n')
print(f'{len(manifest)} separate graphics: {sum(x["bytes"] for x in manifest.values()):,} bytes')
