package llc.bokadev.kompass.core.util

import llc.bokadev.kompass.domain.model.GeoPoint

fun buildGuideMapHtml(
    placeName: String,
    destination: GeoPoint,
    currentLocation: GeoPoint?
): String {
    val destLat = destination.latitude
    val destLon = destination.longitude
    val hasMe = currentLocation != null
    val myLat = currentLocation?.latitude ?: destLat
    val myLon = currentLocation?.longitude ?: destLon
    val labelJs = placeName.jsEscaped()

    return """<!DOCTYPE html>
<html>
<head>
<meta name="viewport" content="width=device-width,initial-scale=1,maximum-scale=1,user-scalable=no">
<style>
*{margin:0;padding:0;box-sizing:border-box}
html,body{width:100%;height:100%;overflow:hidden;background:#aad3df}
#wrap{position:fixed;inset:0;overflow:hidden;background:#aad3df;touch-action:none;-webkit-user-select:none;user-select:none}
#tiles{position:absolute;top:0;left:0}
#ov{position:absolute;top:0;left:0;pointer-events:none;z-index:10}
.zbtn{position:absolute;right:12px;z-index:20;width:36px;height:36px;background:rgba(255,255,255,0.92);border-radius:8px;display:flex;align-items:center;justify-content:center;color:#102A43;font:700 22px -apple-system,system-ui,sans-serif;box-shadow:0 1px 5px rgba(0,0,0,0.18);border:1px solid rgba(16,42,67,0.1);cursor:pointer;-webkit-tap-highlight-color:transparent}
#zi{top:12px}
#zo{top:56px}
</style>
</head>
<body>
<div id="wrap">
  <div id="tiles"></div>
  <canvas id="ov"></canvas>
  <div class="zbtn" id="zi">+</div>
  <div class="zbtn" id="zo">−</div>
</div>
<script>
(function(){
var T=256,ZMIN=3,ZMAX=19;
var destLat=$destLat,destLon=$destLon,hasMe=$hasMe,myLat=$myLat,myLon=$myLon,label='$labelJs';
var wrap=document.getElementById('wrap');
var tilesEl=document.getElementById('tiles');
var ov=document.getElementById('ov');
var ctx=ov.getContext('2d');
var W=0,H=0,zoom=15,ox=0,oy=0;
var tiles={};
var initialized=false;

function proj(lat,lon,z){
  var n=1<<z;
  var x=(lon+180)/360*n*T;
  var s=Math.sin(lat*Math.PI/180);
  var y=(0.5-Math.log((1+s)/(1-s))/(4*Math.PI))*n*T;
  return[x,y];
}

function sub(tx,ty){return'abc'[(Math.abs(tx)+Math.abs(ty))%3];}

function addTile(tx,ty,z){
  var k=z+'/'+tx+'/'+ty;
  if(tiles[k])return;
  var n=1<<z,wx=((tx%n)+n)%n;
  if(ty<0||ty>=n)return;
  var img=new Image();
  img.style.cssText='position:absolute;width:256px;height:256px;';
  img.src='https://'+sub(wx,ty)+'.tile.openstreetmap.org/'+z+'/'+wx+'/'+ty+'.png';
  tiles[k]={img:img,tx:tx,ty:ty,z:z};
  tilesEl.appendChild(img);
}

function render(){
  var tx0=Math.floor(ox/T)-1,ty0=Math.floor(oy/T)-1;
  var tx1=Math.ceil((ox+W)/T)+1,ty1=Math.ceil((oy+H)/T)+1;
  for(var x=tx0;x<=tx1;x++)for(var y=ty0;y<=ty1;y++)addTile(x,y,zoom);
  for(var k in tiles){
    var e=tiles[k];
    if(e.z!==zoom){e.img.style.display='none';continue;}
    e.img.style.display='';
    e.img.style.left=(e.tx*T-ox)+'px';
    e.img.style.top=(e.ty*T-oy)+'px';
  }
  drawOverlay();
}

function drawOverlay(){
  ov.width=W;ov.height=H;
  if(!W||!H)return;
  var dp=proj(destLat,destLon,zoom);
  var dx=dp[0]-ox,dy=dp[1]-oy;
  if(hasMe){
    var mp=proj(myLat,myLon,zoom);
    var mx=mp[0]-ox,my=mp[1]-oy;
    ctx.strokeStyle='#102A43';ctx.lineWidth=2;ctx.setLineDash([7,6]);ctx.globalAlpha=0.6;
    ctx.beginPath();ctx.moveTo(mx,my);ctx.lineTo(dx,dy);ctx.stroke();
    ctx.globalAlpha=1;ctx.setLineDash([]);
    ctx.beginPath();ctx.arc(mx,my,11,0,2*Math.PI);ctx.fillStyle='rgba(16,42,67,0.14)';ctx.fill();
    ctx.beginPath();ctx.arc(mx,my,6,0,2*Math.PI);ctx.fillStyle='#102A43';ctx.fill();
    ctx.strokeStyle='#fff';ctx.lineWidth=2;ctx.stroke();
  }
  ctx.beginPath();ctx.arc(dx,dy,12,0,2*Math.PI);ctx.fillStyle='rgba(245,158,11,0.22)';ctx.fill();
  ctx.beginPath();ctx.arc(dx,dy,7.5,0,2*Math.PI);ctx.fillStyle='#F59E0B';ctx.fill();
  ctx.strokeStyle='#fff';ctx.lineWidth=2.5;ctx.stroke();
  ctx.font='600 13px -apple-system,BlinkMacSystemFont,system-ui,sans-serif';
  var tw=ctx.measureText(label).width;
  var lx=dx-tw/2,ly=dy-26;
  ctx.shadowColor='rgba(0,0,0,0.18)';ctx.shadowBlur=6;
  ctx.fillStyle='rgba(255,255,255,0.93)';
  ctx.beginPath();
  var bx=lx-8,by=ly-15,bw=tw+16,bh=22,r=6;
  ctx.moveTo(bx+r,by);ctx.arcTo(bx+bw,by,bx+bw,by+bh,r);ctx.arcTo(bx+bw,by+bh,bx,by+bh,r);
  ctx.arcTo(bx,by+bh,bx,by,r);ctx.arcTo(bx,by,bx+bw,by,r);ctx.closePath();ctx.fill();
  ctx.shadowBlur=0;ctx.fillStyle='#102A43';ctx.fillText(label,lx,ly+1);
}

function zoomTo(newZoom){
  if(newZoom<ZMIN||newZoom>ZMAX||newZoom===zoom)return;
  var f=Math.pow(2,newZoom-zoom);
  var cx=ox+W/2,cy=oy+H/2;
  ox=cx*f-W/2;oy=cy*f-H/2;
  zoom=newZoom;
  render();
}

window.zoomIn=function(){zoomTo(zoom+1);};
window.zoomOut=function(){zoomTo(zoom-1);};

function initView(){
  W=wrap.clientWidth||window.innerWidth||360;
  H=wrap.clientHeight||window.innerHeight||280;
  ov.width=W;ov.height=H;
  var dp=proj(destLat,destLon,zoom);
  ox=dp[0]-W/2;oy=dp[1]-H/2;
  if(hasMe){
    for(var z2=17;z2>=ZMIN;z2--){
      var d=proj(destLat,destLon,z2),m=proj(myLat,myLon,z2);
      if(Math.abs(d[0]-m[0])<W*0.55&&Math.abs(d[1]-m[1])<H*0.55){
        zoom=z2;ox=(d[0]+m[0])/2-W/2;oy=(d[1]+m[1])/2-H/2;break;
      }
    }
  }
  initialized=true;
  render();
}

function tryInit(){
  if(wrap.clientWidth>0&&wrap.clientHeight>0){initView();}
  else{setTimeout(tryInit,30);}
}
if(document.readyState==='loading'){document.addEventListener('DOMContentLoaded',function(){setTimeout(tryInit,0);});}
else{setTimeout(tryInit,0);}
window.addEventListener('resize',function(){if(initialized){W=wrap.clientWidth;H=wrap.clientHeight;render();}});

/* ── Zoom buttons ── */
function bindZoom(id,fn){
  var el=document.getElementById(id);
  // touchend: fire zoom + stop propagation so the map pan handler ignores it
  el.addEventListener('touchstart',function(e){e.stopPropagation();},{passive:false});
  el.addEventListener('touchend',function(e){e.stopPropagation();e.preventDefault();fn();},{passive:false});
  el.addEventListener('click',function(e){e.stopPropagation();fn();});
}
bindZoom('zi',window.zoomIn);
bindZoom('zo',window.zoomOut);

/* ── Touch: pan (single finger) ── */
var pts=[];
function evPts(e){var a=[];for(var i=0;i<e.touches.length;i++)a.push({x:e.touches[i].clientX,y:e.touches[i].clientY});return a;}

wrap.addEventListener('touchstart',function(e){e.preventDefault();pts=evPts(e);},{passive:false});
wrap.addEventListener('touchmove',function(e){
  e.preventDefault();
  var cur=evPts(e);
  if(cur.length===1&&pts.length>=1){
    ox-=cur[0].x-pts[0].x;oy-=cur[0].y-pts[0].y;render();
  }
  pts=cur;
},{passive:false});
wrap.addEventListener('touchend',function(e){e.preventDefault();pts=evPts(e);},{passive:false});
})();
</script>
</body>
</html>""".trimIndent()
}

private fun String.jsEscaped(): String =
    replace("\\", "\\\\")
        .replace("'", "\\'")
        .replace("\"", "\\\"")
        .replace("\n", "\\n")
        .replace("\r", "\\r")
