{var asteroidIndexBound = 10000;
var forward = false;
var down = false;
var left = false;
var right = false;
var shoot = false;
var mute = true;
var missile_trais = true;
var dev = false;
var debrisLifeTime = 4000;
var maxSpeed = 15;
var maxRotation = 10;
var shieldRadius = 20;
var maxNanobots = 10;
var defenseLaserRadius = 200;
var zap = 0;
var zapChain = 0;
var laserSpeed = 5;
var seed = random()*100000;
var asteroidVertices = 14;
var despawn = millis();
var dead = millis();
var powerup = millis();
var shot = millis();
var missile = millis();
var auto_tDelay = millis();
var arch = millis();
var fpsTimer = millis()+1000;
var emitterShot = millis();
var lastFrame = millis();
var defenseLaserTimer = millis();
var selectWait = millis();
var soundUpdate = millis() + 2000;
var magnetCounter = 0;
var frameAvg = 0;
var frameAvgCount = 0;
var emitterAngle = 0;
var missileCount = 0;
var fps = 0;
var resolution = 30;
var rotation = 0;
var rotAcc = 4.5;
var rotVel = 10;
var opaq = 255;
var reset = false;
var trip = false;
var invert = false;
var menu = true;
var mode = 'NORMAL';
var powerupToggle = true;
var asteroids = {};
var lasers = [];
var debris = [];
var powerups = [];
var missiles = [];
var nanobots = [];
var player = {
    x:width/2,
    y:height/2,
    xvel:0,
    yvel:0,
    angle:0,
	prev_angle:0,
	rotation:0,
    score:0,
    charge:0,
    megaLaserDist: 18,
    thrust:false,
    dead:false,
	playerPieces:[],
	powerups:{}
};
var auto_t = {
    angle:0, 
    rotation: 10, 
    radius: 4, 
    bLength: 7,
    scale: 15,
    fire: false
    
};
var powerup_types = [
    {   
        type:'shield',
        letter: 'S',
        duration: 8000,
        weight: 10              
    }, 
    {   
        type:'tri',
        letter:'T',
        duration: 8000,
        weight: 20
    }, 
    {
        type: 'reload',
        letter: 'R',
        duration: 10000,
        weight: 20
    }, 
    {
        type: 'missile', 
        letter: 'M',
        duration: 8000,
        weight: 10
    },
    {   type: 'explosive',
        letter: 'X',
        duration: 10000,
        weight: 10
    },
    {
        type: 'emitter',
        letter: 'E',
        duration: 10000,
        weight: 5
    },
    {
        type: 'magnet',
        letter: 'U',
        duration: 10000,
        weight: 15
    },
    {
        type: 'pause',
        letter: 'P',
        duration: 8000,
        weight: 10
    },
    {
        type: 'auto',
        letter: 'A',
        duration: 10000,
        weight: 15
    },
    {
        type: 'laser',
        letter: 'L',
        duration: 8000,
        weight: 5
    },
    {
        type: 'zap',
        letter: 'Z',
        duration: 13000,
        weight: 5
    },
    {
        type: 'nanobots',
        letter: 'N',
        duration: 18000,
        weight: 5
    },
    {
        type: 'defenselaser',
        letter: 'D',
        duration: 10000,
        weight: 5
    },];
var presets = {
'EASY':
    {'size':3,
    'asteroids':10,
    'powerups':5,
    'velocity': 3
    },
'NORMAL': 
    {'size':3,
    'asteroids':9,
    'powerups':4,
    'velocity': 4
    },
'HARD':
    {'size':3,
    'asteroids':11,
    'powerups':3,
    'velocity': 4
    },
'INSANE':
    {'size':4,
    'asteroids':10,
    'powerups':2,
    'velocity': 5
    },
'ORIGINAL':
    {'size':3,
    'asteroids':10,
    'powerups':0,
    'velocity': 4
    },
'SWARM (S)':
    {'size':1,
    'asteroids':30,
    'powerups':3,
    'velocity': 4
    },
'SWARM (M)':
    {'size':2,
    'asteroids':25,
    'powerups':3,
    'velocity': 3
    },
'GIANT':
    {'size':5,
     'asteroids': 8,
     'powerups':4,
     'velocity': 3
    },
'ENORMOUS':
    {'size':7,
     'asteroids': 5,
     'powerups':4,
     'velocity': 2
    }
};
var spawnSize;
var numAsteroids;
var numPowerups;
var velocity;
var totalWeight = 0;
var modeButtons;
var otherButtons;
var secretButtons;
var buttonGroups = [];
}

for(var p in powerup_types){
    var seqweight = powerup_types[p].weight+totalWeight;
    totalWeight += powerup_types[p].weight;
    powerup_types[p].seqWeight = seqweight;
    player.powerups[powerup_types[p].type]=0;   
}

frameRate(24);

var ButtonGroup = function(){
    this.buttons = [];
    this.multiToggle = true;
    this.tabSelect = true;
    this.bounds = {x1:0,y1:0,x2:0,y2:0};
    this.selected = [];
};

ButtonGroup.prototype.add = function(button){
    this.buttons[this.buttons.length] = button;   
    this.bounds.x1 = min(this.bounds.x1,button.x);
    this.bounds.y1 = min(this.bounds.y1,button.y);
    this.bounds.x2 = max(this.bounds.x2,button.x+button.width);
    this.bounds.y2 = max(this.bounds.y2,button.y+button.height);
};

ButtonGroup.prototype.drawAll = function(){
    for(var b in this.buttons){
        this.buttons[b].draw();   
    }
};

ButtonGroup.prototype.inside = function(x,y){
    return x >= this.bounds.x1 && x <= this.bounds.x2 &&
            y >= this.bounds.y1 && y <= this.bounds.y2;
};

ButtonGroup.prototype.pressed = function(x,y){
    for(var b in this.buttons){
        if(this.buttons[b].inside(x,y)){
            this.buttons[b].pressed();   
            break;
        }
    }
};

ButtonGroup.prototype.released = function(x,y){
    for(var b in this.buttons){
        if(this.buttons[b].inside(x,y)){
            this.buttons[b].released();
            if(this.buttons[b].toggle){
                if(!this.multiToggle){
                    var other = false;
                    for(var but in this.buttons){
                        if(but!==b && this.buttons[b].toggled){
                            this.buttons[but].toggled = false;  
                            other = true;
                        }
                    }
                    if(!other && !this.buttons[b].toggled){
                        this.buttons[b].toggled = true;
                    }
                    if(this.buttons[b].toggled){
                        this.selected = [b];
                    }
                }
                else{
                    for(var x in this.buttons){
                        if(this.buttons[x].toggled){
                            this.selected[this.selected.length] = x;   
                        }
                    }
                }
            }
        }
        else{
            this.buttons[b].clicked = false;   
        }
    }
};

ButtonGroup.prototype.hover = function(x,y){
    for(var b in this.buttons){
        this.buttons[b].hover = this.buttons[b].inside(x,y);   
    }
};

ButtonGroup.prototype.disableAll = function(state){
    for(var b in this.buttons){
        this.buttons[b].disabled = state;   
    }
};

var Button = function(text, x, y, w, h, func, c){
    this.text = text;
    this.font = createFont('Ariel',20);
    this.x = x;
    this.y = y;
    this.width = w;
    this.height = h;
    this.function = func;
    this.color = c ? c : color(255, 255, 255, 255 * 0.99);
    this.sec_color = color(lerpColor(this.color,color(255,255,255,255)-this.color,0.33));
    this.hoverable = true;
    this.hover = false;
    this.clicked = false;
    this.toggle = false;
    this.toggled = false;
    this.disabled = false;
};

Button.prototype.draw = function() {
    if(!this.disabled){
        noStroke();
        var c = (this.clicked || this.toggled) ? this.sec_color : this.color;
        fill(c);
        rect(this.x, this.y, this.width, this.height);
        if(this.hover && this.hoverable){
            noFill();
            stroke(blendColor(c,color(255,255,255,255),DIFFERENCE));
            var w_marg = 1;//this.width * 0.01;
            var h_marg = 1;//this.height * 0.01;
            rect(this.x+w_marg,this.y+h_marg,this.width-3*w_marg,this.height-3*h_marg);
        }
        fill(0, 0, 0);
        textFont(this.font);
        textAlign(CENTER, CENTER);
        text(this.text, this.x + this.width/2, this.y + this.height/2);
        textAlign(LEFT);
    }
};

Button.prototype.pressed = function(){
    if(!this.disabled){
        this.clicked = true;
    }
};

Button.prototype.released = function(){
    if(!this.disabled){
        this.clicked = false;
        this.toggled = this.toggle ? !this.toggled : false;
        this.function();
    }
};

Button.prototype.inside = function(x,y){
    return x >= this.x && x <= this.x+this.width && 
            y >= this.y && y <= this.y+this.height;    
};


var RNG = function (seed) {
  this.m = 0x80000000;
  this.a = 1103515245;
  this.c = 12345;
  this.state = seed ? seed : Math.floor(Math.random() * (this.m - 1));
};

RNG.prototype.nextInt = function() {
  this.state = (this.a * this.state + this.c) % this.m;
  return this.state;
};

var LinkedList = function(){
    this.value = null;
    this.index = 0;
    this.child = null;
    this.parent = null;
};

LinkedList.prototype.get = function(index){
    if(this.index===index){
        return this.value;   
    }
    else{
        if(this.hasChild()){
            return this.next.get(index);   
        }
    }
};

LinkedList.prototype.append = function(value){
    if(this.value===null){
        this.value = value;
    }
    else if(!this.hasChild()){
        this.child = new LinkedList();
        this.child.value = value;
        this.child.index = this.index+1;
        this.child.parent = this;
    }
    else{
        this.child.append(value);   
    }
};

LinkedList.prototype.insert = function(value){
    this.child = new LinkedList(value);
    this.child.value = value;
    this.child.index = this.index+1;
    this.child.parent = this;
};

LinkedList.prototype.remove = function(){
    if(this.parent === null){
        if(!this.hasChild()){
            delete this.value;
            delete this.index;
        }
        else{
            this.value = this.child.value;
            this.child = this.child.child;
            this.update();
        }
    }
    else{
        this.parent = this.child;   
        this.update();
    }
};

LinkedList.prototype.update = function(){
    if(this.hasChild()){
        this.child.index = this.index+1;
        this.child.update();
    }
};

LinkedList.prototype.length = function(){
    if(!this.hasChild()){
        return this.index + 1;
    }
    else{
        return this.child.length();   
    }
};

LinkedList.prototype.print = function(){
    print(this.value+(!this.hasChild()?'\n':','));
    if(this.hasChild()){
        this.child.print();   
    }
};

LinkedList.prototype.hasChild = function(){
	return this.child !== null;
};

LinkedList.prototype.end = function(){
    if(this.hasChild()){
        return this.child.end();   
    }
    else{
        return this;   
    }
};

var mod = function(n,m){
    return ((n%m)+m)%m;  
};

var sparkChain = function(chain){
    stroke(255, 255, 255);
    fill(255, 255, 255);
    for(var n = 0; n<chain.length-1;n++){
        if(random(0,10)>=2){
            var current = chain[n];
            var next = chain[n+1];
            var d = dist(next.x, next.y, current.x, current.y);
            var angle = asin((current.x-next.x)/d);
            angle += (next.y>current.y?2*((90 * (next.x>current.x?-1:1))-angle):0);
            var distance = dist(current.x, current.y, next.x, next.y);
            var points = random(chain[n].points[0],chain[n].points[1])*min(1,distance/chain[n].standardDist);
            var range = random(chain[n].intensity[0],chain[n].intensity[1])*min(1,distance/chain[n].standardDist);
            var step = distance/points;
            noFill();
            strokeWeight(random(1,3));
            beginShape();
            vertex(current.x, current.y);
            for(var i = 1;i<points;i++){
                var cPoint = {
                    x:current.x - step*i*sin(angle),
                    y:current.y - step*i*cos(angle)    
                };
                var maxSmooth = points/3;
                var smooth = pow(min(1,abs(points/2-abs(i-points/2))/(maxSmooth)),2);
                vertex(cPoint.x - random(-range,range)*sin(angle+90)*smooth, cPoint.y - random(-range,range)*cos(angle+90)*smooth);
            }
            vertex(next.x, next.y);
            endShape();
        }
    }
    strokeWeight(1);
};

var addChainNode = function(chain,p,a){
    var size = 7;
    chain[chain.length] = {
        x:p.x,
        y:p.y,
        intensity:[size, size+10],
        points:[8,12],
        standardDist:50,
        index: a
    };
};

var calculateCentroid = function(points){
    if(points.length === 2){
        return {x: (points[0].x-points[1].x)/2, y: (points[0].y-points[1].y)/2};   
    }
    
    var xsum = 0;
    var ysum = 0;
    var area = 0;
    for(var i = 0; i < points.length; i++){
        var p0 = points[i];
        var p1 = points[(i+1)%points.length];
        
        var areasum = p0.x*p1.y - p1.x*p0.y;
        xsum += (p0.x + p1.x) * areasum;
        ysum += (p0.y + p1.y) * areasum;
        area += areasum;
    }
    
    return {x: xsum / (area * 3), y: ysum / (area * 3)};   
};

var calculateArea = function(points){
    var sum = 0;
    for(var i = points.length - 1; i >= 0; i--){
        sum += points[i].x*points[mod(i-1,points.length)].y - points[i].y*points[mod(i-1,points.length)].x;
    }
    return abs(sum / 2);
};

var newAsteroid = function(xpos,ypos,s, xvel, yvel){
    if(asteroids.length>=asteroidIndexBound){
        return;
    }
    var temp = [];
    var minRad = 5;
    var maxRad = 10;
    var last = minRad+(maxRad-minRad)/2;
    var maxDiff = (maxRad-minRad)*0.81;
    for(var i=0;i<asteroidVertices;i++)
    {
        last = max(minRad,min(maxRad,last+random(-maxDiff, maxDiff)));
        var d = last*s;
        temp[i] = {
            x:d*sin(360/asteroidVertices*i)+xpos,
            y:d*cos(360/asteroidVertices*i)+ypos,
            radius:d,
            angle:360/asteroidVertices*i
        };
    }
    var cm = calculateCentroid(temp);
    for(var i in temp){
        temp[i].radius = dist(cm.x, cm.y, temp[i].x ,temp[i].y);
    }
    
    var index;
    do{
        index = round(random(asteroidIndexBound));
    }while(index in asteroids);
    asteroids[index] = {
        x:cm.x,
        y:cm.y,
        xvel:xvel,
        yvel:yvel,
        size:s,
        rotationSpeed:random(-7,7),
        angle:0,
        vertices:temp,
        index: index
    };
};

var drawAsteroids = function(){	
    var indicies = Object.keys(asteroids);
    indicies.sort(function(a,b){return asteroids[a].index-asteroids[b].index;});
	for(var index in indicies){
	    var ast = asteroids[indicies[index]];
		fill(0, 0, 0);
		stroke(255, 255, 255);
		beginShape();
		for(var i in ast.vertices){
			vertex(ast.vertices[i].x,ast.vertices[i].y);
		}
		vertex(ast.vertices[0].x,ast.vertices[0].y);
		endShape();
		if(dev){
            fill(255, 0, 0);
            stroke(255, 0, 0);
            strokeWeight(1);
            line(ast.x+ast.xvel*auto_t.scale,ast.y+ast.yvel*auto_t.scale,ast.x, ast.y);
            strokeWeight(2);
            stroke(0, 255, 0);
            point(ast.vertices[0].x,ast.vertices[0].y);
            stroke(255, 0, 0);
            point(ast.vertices[1].x,ast.vertices[1].y);
            strokeWeight(1);
		}
	}
};

var newLaser = function(xpos,ypos,a){
    lasers[lasers.length] = {
        x:xpos,
        y:ypos,
        angle:a,
        xvel: laserSpeed,
        yvel: laserSpeed,
        size: player.powerups.explosive>millis()?2:1
    };
};

var drawPlayer = function(){
    stroke(255, 255, 255);
    strokeWeight(1);
    player.vertices = [
        {x:player.x-10*sin(player.angle),         y:player.y-10*cos(player.angle)},
        {x:player.x-7.07*sin(player.angle-135),   y:player.y-7.07*cos(player.angle-135)},
        {x:player.x,                              y:player.y},
        {x:player.x-7.07*sin(player.angle+135),   y:player.y-7.07*cos(player.angle+135)},
        {x:player.x-10*sin(player.angle),         y:player.y-10*cos(player.angle)}
    ];
    var flame = [
        {x:player.x-3.5*sin(player.angle+135),   y:player.y-3.5*cos(player.angle+135)},
        {x:player.x+5*sin(player.angle), y:player.y+5*cos(player.angle)},
        {x:player.x-3.5*sin(player.angle-135),   y:player.y-3.5*cos(player.angle-135)}
    ];
    
    if(player.powerups.zap > millis()){
        var size = 17;
        var skin = [];
        for(var v in player.vertices){
            skin[skin.length] = {
                x:player.vertices[v].x,
                y:player.vertices[v].y,
                intensity:[size,size+10],
                points:[8,12],
                standardDist:50  
            };
        }
        skin[skin.length] = {
            x:player.vertices[0].x,
            y:player.vertices[0].y,
            intensity:[size,size+10],
            points:[8,12],
            standardDist:5  
        };
        sparkChain(skin);
    }
    
    fill(0, 0, 0);
    beginShape();
    for(var vert in player.vertices){
        vertex(player.vertices[vert].x, player.vertices[vert].y);
    }
    endShape();
    if(player.thrust){
        beginShape();
        for(var vert in flame){
            vertex(flame[vert].x, flame[vert].y);   
        }
        endShape();
    }
    noFill();
    if(player.powerups.shield > millis()){
        var c = 255 * exp((player.powerups.shield - millis()) / 8000)/Math.E;
        stroke(c, c, c);
        ellipse(player.x, player.y, shieldRadius*2, shieldRadius*2);
        stroke(255, 255, 255);
        //arc(player.x, player.y, shieldRadius*1.7, shieldRadius*1.7, 1, (player.powerups.shield - millis())/8000*360);
    }
    
    if(player.powerups.auto > millis()){
        var rad = auto_t.radius;
        var b = auto_t.bLength;
        fill(255, 255, 255);
        ellipse(player.x, player.y, rad,rad);
        strokeWeight(2);
        line(player.x-rad*sin(auto_t.angle) ,player.y-rad*cos(auto_t.angle), player.x-(rad+b)*sin(auto_t.angle), player.y-(rad+b)*cos(auto_t.angle));
        if(dev){
            stroke(255, 0, 0);
            strokeWeight(1);
            noFill();
            ellipse(player.x,player.y,120*2,120*2);
            stroke(255, 255, 255);
            fill(255, 255, 255);
        }
    }
    
    if(player.powerups.defenselaser > millis()){
        var d = 5;
        fill(255, 255, 255);
        stroke(255, 255, 255);
        ellipse(player.x, player.y, d, d);
        if(dev){
            stroke(255, 0, 0);
            fill(181, 181, 181, 20);
            ellipse(player.x, player.y, defenseLaserRadius*2, defenseLaserRadius*2);
        }
    }
    
    if(player.powerups.laser > millis() && player.charge===0){
        var rad = 2;
        stroke(255, 255, 255);
        fill(255, 255, 255);
        var x = player.x - player.megaLaserDist*sin(player.angle);
        var y = player.y - player.megaLaserDist*cos(player.angle);
        ellipse(x, y, rad, rad);
        var dv = 1.5;
        if(random(1,10)>3){
            debris[debris.length] = {
                x:x,
                y:y,
                xvel:random(-dv,dv),
                yvel:random(-dv,dv),
                alive_time:millis()-(debrisLifeTime-150),
                size:random(1,2)
            }; 
        }
    }
    if(player.powerups.magnet>millis()){
        var t = magnetCounter;
        magnetCounter -= 1;
        var maxDist = 50;
        var numRings = 5;
        var inter = maxDist/numRings;
        for(var i = 0; i < numRings; i++){
            var r = mod(t + inter * i,maxDist);
            var c = 140 * (1-r/maxDist);
            stroke(c,c,c,c);
            noFill();
            ellipse(player.x, player.y, r*3, r*3);
        }
    }else{
        magnetCounter = 0;   
    }
};

var drawLasers = function(){
    stroke(255, 255, 255);
    fill(255, 255, 255);
	for(var index in lasers){
	    var r = lasers[index].size*2.5;
		ellipse(lasers[index].x + random(), lasers[index].y, random(0,r), random(0,r));
	}
};

var moveAsteroids = function(){
	for(var index in asteroids){
		asteroids[index].x+=asteroids[index].xvel;
		asteroids[index].y+=asteroids[index].yvel;
		if(asteroids[index].x>width){asteroids[index].x=0;}
		if(asteroids[index].x<0){asteroids[index].x=width;}
		if(asteroids[index].y>height){asteroids[index].y=0;}
		if(asteroids[index].y<0){asteroids[index].y=height;}
		var temp = [];
		var step = 360/asteroids[index].vertices.length;
		asteroids[index].angle = (asteroids[index].angle+asteroids[index].rotationSpeed)%360;
		for(var i in asteroids[index].vertices){
		    asteroids[index].vertices[i].angle = (asteroids[index].vertices[i].angle+asteroids[index].rotationSpeed)%360;
		    var angle =asteroids[index].vertices[i].angle; 
		    var radius = asteroids[index].vertices[i].radius;
			asteroids[index].vertices[i].x =radius*sin(angle) +asteroids[index].x;
			asteroids[index].vertices[i].y =radius*cos(angle) +asteroids[index].y;
			
		}
	}
};

var movePlayer = function(){
	//player.angle = (player.angle + player.rotation)%360;
    player.x+=player.xvel;
    player.y+=player.yvel;
    if(player.x>width){player.x=0;}
    if(player.x<0){player.x=width;}
    if(player.y>height){player.y=0;}
    if(player.y<0){player.y=height; }
};

var moveLasers = function(){
	for(var index in lasers){
		lasers[index].x-=lasers[index].xvel*sin(lasers[index].angle);  
		lasers[index].y-=lasers[index].yvel*cos(lasers[index].angle);
		if(lasers[index].x>width || lasers[index].x<0 || 
			lasers[index].y>height || lasers[index].y<0)
		{
			lasers[index] = 0;
		}
	}
};

var condenseTable = function(table){
    var temp = [];
    var count = 0;
    for(var i in table)
    {
        if(table[i]!==0)
        {
            temp[count] = table[i];
            count++;
        }
    }
    return temp;
};

var condenseMap = function(map){
    var temp = {};
    for(var key in map){
        if(map[key]!==0){
            temp[key] = map[key];   
        }
    }
    return temp;
};

var turnTowards = function(obj, angle){
    var offset = 0;
    if(abs(obj.angle - angle) > 180){
        if(obj.angle > 180){
            offset = obj.rotation;
        }
        else{
            offset = -obj.rotation;   
        }
    }
    else{
        if(obj.angle > angle){
            offset = -min(abs(obj.angle-angle), obj.rotation);
        }
        else{
            offset = min(abs(obj.angle-angle),obj.rotation);
        }
    }
    return (obj.angle + offset + 360)%360;
};
 
var intersection = function(x1,y1,x2,y2,x3,y3,x4,y4){
    var slope1 = (y1-y2)/(x1-x2);
    var intercept1 = slope1*(-x1)+y1;
    var slope2 = (y3-y4)/(x3-x4);
    var intercept2 = slope2*(-x3)+y3;
    if(slope1===slope2)
    {
        return {x:0,y:0};
    }
    var intx = (intercept1-intercept2)/(slope2-slope1);
    var inty = slope1*intx+intercept1;
    return {x:intx,y:inty};
};

var getIntersections = function(p1,p2,obj){
    var ints = [];
    for(var b = 0;b<obj.length;b++){
        var p1_B = obj[b];
        var p2_B = obj[(b+1)%obj.length];
        var dx = dist(p1_B.x,p1_B.y,p2_B.x,p2_B.y);
        var int = intersection(
            p1.x, p1.y, p2.x, p2.y,
            p1_B.x, p1_B.y,p2_B.x, p2_B.y);
        if(dist(int.x,int.y,p1_B.x,p1_B.y)<=dx &&
            dist(int.x,int.y,p2_B.x,p2_B.y)<=dx){
            ints[ints.length] = int;          
        }
    }  
    return ints;
};

var intersect_circle = function(pA,pB,x,y,r){
    var p1 = {x:pA.x-x,y:pA.y-y};
    var p2 = {x:pB.x-x,y:pB.y-y};
    var dx = p2.x-p1.x;
    var dy = p2.y-p1.y;
    var dr = sqrt(sq(dx)+sq(dy));
    var D = p1.x*p2.y-p2.x*p1.y;
    var points = [];
    var sign = [1,-1];
    var delta = sq(r)*sq(dr)-sq(D);
    if(delta>=0){
        for(var i =0;i<2;i++){
            var int_x = (D*dy+sign[i]*(dy<0?-1:1)*dx*sqrt(delta))/sq(dr);
            var int_y = (-D*dx+sign[i]*abs(dy)*sqrt(delta))/sq(dr);
            points[points.length] = {x:int_x+x,y:int_y+y};
        }
    }
    return points;
};
 
var checkCollisions = function(objA, objB){
    for(var a = 0; a<objA.length; a++){
        var a_p1 = objA[a];
		var a_p2 = objA[(a+1)%objA.length];
        var a_p2_h = {x:a_p1.x+1,y:a_p1.y};
		var dA = dist(a_p1.x,a_p1.y,a_p2.x,a_p2.y);
        var count = 0;
        for(var b = 0; b<objB.length; b++){
            var b2 = (b+1)%objB.length;
            var b_p1 = objB[b];
            var b_p2 = objB[b2];
            var dB = dist(b_p1.x,b_p1.y,b_p2.x,b_p2.y);
			var int = intersection(a_p1.x,a_p1.y,a_p2.x,a_p2.y,b_p1.x,b_p1.y,b_p2.x,b_p2.y);
            var int_h = intersection(a_p1.x,a_p1.y,a_p2_h.x,a_p2_h.y,b_p1.x,b_p1.y,b_p2.x,b_p2.y);
            if(dist(int_h.x,int_h.y,b_p1.x,b_p1.y)<=dB && dist(int_h.x,int_h.y,b_p2.x,b_p2.y)<=dB &&
                int_h.x>=a_p1.x){
                count++;        
            }
			if(dist(int.x,int.y,a_p1.x,a_p1.y)<=dA && dist(int.x,int.y,a_p2.x,a_p2.y)<=dA && 
			dist(int.x,int.y,b_p1.x,b_p1.y)<=dB && dist(int.x,int.y,b_p2.x,b_p2.y)<=dB){
				return true;
			}
        }
        if(count%2===1){
            return true;   
        }
    }
    return false;
};

var checkCollisions_inter = function(vertA, vertB, inter){
    for(var a = 0;a<vertA.length; a++){
        var p1_A = vertA[a];
        var p2_A = vertA[(a+1)%vertA.length];
        var dx_A = abs(p1_A.x-p2_A.x);
        var dy_A = abs(p1_A.y-p2_A.y);
        for(var b = 0;b<vertB.length;b++){
            var p1_B = vertB[b];
            var p2_B = vertB[(b+1)%vertB.length];
            var dx_B = abs(p1_B.x-p2_B.x);
            var dy_B = abs(p1_B.y-p2_B.y);
            var int = intersection(
                p1_A.x, p1_A.y,p2_A.x, p2_A.y,
                p1_B.x, p1_B.y,p2_B.x, p2_B.y);
            if(abs(int.x-p1_A.x)<=dx_A && abs(int.x-p2_A.x)<=dx_A &&
                abs(int.y-p1_A.y)<=dy_A && abs(int.y-p2_A.y)<=dy_A &&
                abs(int.x-p1_B.x)<=dx_B && abs(int.x-p2_B.x)<=dx_B &&
                abs(int.y-p1_B.y)<=dy_B && abs(int.y-p2_B.y)<=dy_B){
                if(inter){
                    inter[0] = int;   
                }
                return true;        
            }
        }
    }
    return false;
};

var angleFromVertical = function(target, center){
    var distance = dist(target.x, target.y, center.x, center.y);
    var angle = abs(asin((center.x-target.x)/distance));
    //angle += (target.y>center.y?2*((90 * (target.x>center.x?-1:1))-angle):0); 
    if(target.x<center.x && target.y>center.y){angle = 180-angle;}
    if(target.x>center.x && target.y>center.y){angle += 180;}
    if(target.x>center.x && target.y<center.y){angle = 360-angle;}
    return Number.isNaN(angle) ? 0 : angle;
};

var angleBetween = function(a,b,c){
    var ac = {x: c.x-a.x, y: c.y-a.y};
    var bc = {x: c.x-b.x, y:c.y-b.y};
    var acbc = ac.x*bc.x + ac.y*bc.y;
    var mag = sqrt(sq(ac.x)+sq(ac.y)) * sqrt(sq(bc.x)+sq(bc.y));
    return acos(acbc/mag);
};

var getOffsetPoint = function(p1,p2,offset){
    var v = {x:p1.x-p2.x,y:p1.y-p2.y};
	var dv = dist(p1.x,p1.y,p2.x,p2.y);
	var u = {x:v.x/dv,y:v.y/dv};
	var x = p1.x + offset*u.x;
	var y = p1.y + offset*u.y;
	return {x:x,y:y};
};

var getIntersectPoints_C = function(p,ast,radius){
    var vert = ast.vertices;
    var nodes = [];
    for(var v =0; v<vert.length;v++){
        var p1 = vert[v];
        var p2 = vert[(v+1)%vert.length];
        var ints = intersect_circle(p1,p2,p.x,p.y,radius);
        for(var i in ints){
            var dx = abs(p1.x-p2.x);
            var dy = abs(p1.y-p2.y);
            if( abs(ints[i].x-p1.x)<=dx && abs(ints[i].x-p2.x)<=dx &&
                abs(ints[i].y-p1.y)<=dy && abs(ints[i].y-p2.y)<=dy){
                var angle = angleFromVertical(ints[i],p);
                var d = dist(ints[i].x, ints[i].y, p1.x, p1.y);
                var sd = dist(p1.x, p1.y, p2.x, p2.y);
                nodes[nodes.length] = {point:ints[i], index:v, angle:angle, d:d, sd:sd, sortValue: angle};
            }  
        }
    }
    nodes.sort(function(a,b){return a.angle-b.angle;});
    return nodes;
};

var getIntersectPoints_P = function(ast,poly_v){
	var vert = ast.vertices;
    var nodes = [];
	for(var i=0; i<poly_v.length; i++){
		for(var v =0; v<vert.length;v++){
			var p1 = vert[v];
			var p2 = vert[(v+1)%vert.length];
			var p3 = poly_v[i];
			var p4 = poly_v[(i+1)%poly_v.length];
			var int = intersection(p1.x,p1.y,p2.x,p2.y,p3.x,p3.y,p4.x,p4.y);
			var d_p1p2 = dist(p1.x,p1.y,p2.x,p2.y);
			var d_p3p4 = dist(p3.x,p3.y,p4.x,p4.y);
			if(dist(int.x,int.y,p1.x,p1.y)<=d_p1p2 && dist(int.x,int.y,p2.x,p2.y)<=d_p1p2 &&
				dist(int.x,int.y,p3.x,p3.y)<=d_p3p4 && dist(int.x,int.y,p4.x,p4.y)<=d_p3p4){
				var slope = (p3.y-p4.y)/(p3.x-p4.x);
				var d = dist(int.x, int.y, p1.x, p1.y);
				var sd = dist(p1.x, p1.y, p2.x, p2.y);
				nodes[nodes.length] = {point: int, index: v, slope: slope, p_index: i, d:d, sd: sd,  sortValue: i + dist(int.x, int.y, p3.x, p3.y)/dist(p3.x, p3.y, p4.x, p4.y)};
			}  
		}
	}
	nodes.sort(function(a,b){return a.sortValue - b.sortValue;});
    return nodes;
};

var createPairs_C = function(p,nodes,ast,radius){
    var pairs = [];
	var sign = [1,-1];
	var verts = ast.vertices;
	var n = 0;
	var p_count = 0;
	while(p_count < ceil(nodes.length/2)){
		var n1 = nodes[n];
		var found = false;
		for(var s in sign){
		    var d = sign[s];
            var n2 = nodes[mod(n+d,nodes.length)];
		    var a = angleBetween(n1.point,n2.point,p)/2;
            var test_p = [{
                x:p.x - radius*sin(n1.angle+d*a),
                y:p.y - radius*cos(n1.angle+d*a)
            }];
            if(checkCollisions(test_p,verts)){
                if(n1.index<n2.index || n1.index===n2.index && n1.d < n2.d){
                    pairs[pairs.length] = {p1:n1,p2:n2,dir:d,sortValue:n1.index+n1.d/n1.sd};
                }
                else{
                    pairs[pairs.length] = {p1:n2,p2:n1,dir:-d,sortValue:n2.index+n2.d/n2.sd};
                }
                var step = d > 0 ? 2 : 1;
                n = (n+step)%nodes.length;
                p_count++;
                found = true;
                break;
            }
		}
		if(!found){ n = (n+1)%nodes.length; p_count++;}
	}
	pairs.sort(function(a,b){return a.sortValue - b.sortValue;});
    return pairs;
};

var createPairs_P = function(nodes,ast,poly_v){
    var pairs = [];
    for(var n = 0; pairs.length < nodes.length/2;){
        var node = nodes[n];
        var next = poly_v[(node.p_index+1)%poly_v.length];
        var n2 = (n+1)%nodes.length;
        if(nodes[n2].p_index === node.p_index){
            next = nodes[n2].point;
        }
        var mid = {x: (node.point.x + next.x)/2, y: (node.point.y + next.y)/2};
        var dir = checkCollisions([mid], ast.vertices) ? 1 : -1;
        var node2 = nodes[mod(n+dir, nodes.length)];
        if(node.index<node2.index || node.index===node2.index && node.d < node2.d){
            pairs[pairs.length] = {p1: node, p2: node2 , dir: dir, sortValue: node.index + node.d/node.sd};
        }
        else{
            pairs[pairs.length] = {p1: node2, p2: node, dir: -dir, sortValue: node2.index + node2.d/node2.sd};
        }
        
        var step = dir > 0 ? 2 : 1;
        n = (n+step)%nodes.length;
    }
    pairs.sort(function(a,b){return a.sortValue - b.sortValue;});
    return pairs;
};

var createCut_C = function(start,end,dir,p,radius,rand){
    var step = 30;
    var diff;
    if(dir<0&&start.angle<end.angle){diff = start.angle+(360-end.angle);}
    else if(dir>0&&start.angle>end.angle){diff = end.angle+(360-start.angle);}
    else{diff = abs(start.angle-end.angle)%360;}
    var cut = [];
    cut[cut.length] = {x:start.point.x,y:start.point.y};
    for(var a=round(start.angle);diff>0; diff-=step){
        var x = p.x-(radius+random(-rand,0))*sin(a);
        var y = p.y-(radius+random(-rand,0))*cos(a);
        cut[cut.length] = {x:x,y:y};
        step = min(step,diff);
        a+=dir*step;
    }
    cut[cut.length] = {x:end.point.x,y:end.point.y};
    return cut;
};

var segmentLine = function(cut,p1,p2,rand,step){
    var d = dist(p1.x,p1.y,p2.x,p2.y);
    var iter = round(d/step);
    for(var i = 0;i<iter;i++){
        var p = getOffsetPoint(p1,p2,-step*i);
        cut[cut.length] = {x:p.x+random(-rand,rand),y:p.y+random(-rand,rand)};
    }
    return cut;
};

var createCut_P = function(start,end,dir,poly_v,rand){
	var cut = [];
	cut[cut.length] = {x:start.point.x,y:start.point.y};
	for(var i = (dir<0?start.p_index:(start.p_index+dir)%poly_v.length); 
		i!==(dir<0?end.p_index:(end.p_index+dir)%poly_v.length); 
		i = (i+dir<0?poly_v.length-1:(i+dir)%poly_v.length))
	{
	    var p1 = cut[cut.length-1];
	    var p2 = {x:poly_v[i].x,y:poly_v[i].y};
	    cut = segmentLine(cut,p1,p2,rand,15);
		cut[cut.length] = p2;
	}
	var p1 = cut[cut.length-1];
    var p2 = end.point;
    cut = segmentLine(cut,p1,p2,rand);
	cut[cut.length] = {x:end.point.x,y:end.point.y};
	//var n = cut[-1].p;
	return cut;
};

var getClosestPoint = function(A,B,P){
    var AP = {x: P.x - A.x, y: P.y - A.y};
    var AB = {x: B.x - A.x, y: B.y - A.y};
    var magAB = sq(dist(A.x,A.y,B.x,B.y));
    var prodABAP = AP.x * AB.x + AP.y * AB.y;
    var normDist = prodABAP/magAB;
    if(normDist < 0){
        return A;   
    } 
    else if(normDist > 1){
        return B;   
    }
    else{
        return {x: A.x + AB.x * normDist, y: A.y + AB.y * normDist};
    }
};

var optimizeBody = function(vert, tolerance){
    if(vert.length < 3){
        return vert;   
    }
    if(!tolerance){
        var avg = 0;
        for(var i = 0; i<vert.length; i++){
            var p1 = vert[i];
            var p2 = vert[(i+1)%vert.length];
            var d = dist(p1.x,p1.y,p2.x,p2.y);
            avg = (avg + d)/2;
        }
        tolerance = 1;//max(round(5*(-1/(pow(avg/40,3) + 1) + 1)),1);
    }
    var newVert = [];
    var s = 0;
    var e = 2;
    for(; e <= vert.length; e++){
        var a = vert[s];
        var p = vert[e-1];
        var b = vert[e%vert.length];
        var c = getClosestPoint(a,b,p);
        var ang = angleBetween(a,b,p);
        var t = 1.05631*(-1/(pow(ang/150,12) + 1) + 1) *tolerance;
        if(dist(c.x, c.y, p.x, p.y) > t){
            newVert[newVert.length] = a;
            s = e-1;
        }
    }
    if(s === vert.length-1){
        newVert[newVert.length] = vert[vert.length-1];   
    }
    return newVert;
};

var cutBody = function(body,cut,s,e,dir){
    var vert = [];
    var size = 0;
    if(s===e){
        var i = (s+1)%body.length;
        vert[vert.length] = {x:body[i].x,y:body[i].y}; 
        s++;
    }
    for(var i = (s+1)%body.length; i!==(e+1)%body.length; i = (i+1)%body.length){
        vert[vert.length] = {x:body[i].x,y:body[i].y};
    }
    for(var i = cut.length-1; i>=0; i--){
        vert[vert.length] = {x:cut[i].x,y:cut[i].y};
    }
    vert = optimizeBody(vert);
    if(vert.length>1){
        var center = calculateCentroid(vert);
        for(var v in vert){
            vert[v].radius = dist(center.x,center.y,vert[v].x,vert[v].y);
            vert[v].angle = angleFromVertical(center,vert[v]);
            size += vert[v].radius/10;
        }
        size /= vert.length;
        var ast = {
            x:center.x,
            y:center.y,
            xvel:0,
            yvel:0,
            rotationSpeed:0,
            vertices: vert,
            angle: 0,
            size: size
        };
        return ast;
    }
};

var deformBody = function(ast,shape,cuts,min){
    var minSize = !min ? 50 : min;
    var nodes = shape.getNodes(ast);
    var pairs = shape.getPairs(nodes,ast);
    var vert = [];
    var pair_i = 0;
    var astV = ast.vertices;
    var created = [];
    for(var v = 0;v<astV.length;v++){
		if(shape.outside(astV[v].x,astV[v].y)){
			vert[vert.length] = {x:astV[v].x,y:astV[v].y};
		}
		while(pair_i<pairs.length && pairs[pair_i].p1.index===v){
			var start = pairs[pair_i].p1;
			var end = pairs[pair_i].p2;
			var dir = pairs[pair_i].dir;
			var cut = shape.cut(start,end,dir);
			if(cuts){ cuts[cuts.length] = cut; }
	        var next_p = getOffsetPoint(start.point,astV[v],0.001);
			if(shape.outside(next_p.x,next_p.y)){
				var newAst = cutBody(astV,cut,start.index,end.index,dir);
				if(newAst && calculateArea(newAst.vertices)>minSize){
					var index;
					do{
						index = round(random(asteroidIndexBound));
					}while(index in asteroids);
					var dw = 2*PI*dist(ast.x,ast.y,newAst.x,newAst.y)*ast.rotationSpeed/360;
					var angle = angleFromVertical(newAst, ast);
					dw = 0;
					newAst.xvel = ast.xvel - dw*sin(angle+90);
					newAst.yvel = ast.yvel - dw*cos(angle+90);
					newAst.rotationSpeed = ast.rotationSpeed;
					newAst.index = ast.index;
					asteroids[index] = newAst;
					created[created.length] = index;
				}
				v = end.index;
				if(start.index>end.index){
					var temp = [];
					for(var i = end.index+1; i<vert.length; i++){
						temp[temp.length] = {x:vert[i].x,y:vert[i].y};   
					}
					vert = temp;
					v = astV.length;
				}
			}
			else{
				for(var i in cut){
					vert[vert.length] = {x:cut[i].x,y:cut[i].y};
				}
			}
			pair_i++;
		}
    }
    vert = optimizeBody(vert);
    if(vert.length>2){
        var center = calculateCentroid(vert);
        var size = 0;
        for(var v in vert){
            vert[v].radius = dist(center.x,center.y,vert[v].x,vert[v].y);
            vert[v].angle = angleFromVertical(center,vert[v]);
            size += vert[v].radius/10;
        }
        size /= vert.length;
        if(calculateArea(vert)>=minSize){
            var index;
			do{
				index = round(random(asteroidIndexBound));
			}while(index in asteroids);
			var dw = 2*PI*dist(ast.x,ast.y,center.x,center.y)*ast.rotationSpeed/360;
			var angle = angleFromVertical(center, ast);
			dw = 0;
            asteroids[index] = {
                x:center.x,
                y:center.y,
                xvel:ast.xvel - dw*sin(angle+90),
                yvel:ast.yvel - dw*cos(angle+90),
                rotationSpeed:ast.rotationSpeed,
                vertices: vert,
                angle: ast.angle,
                size: size,
                index: ast.index
            };
            created[created.length] = index;
        }
    }
    return created;
};

var makeExplosion = function(p,radius){
	var shape = {
		getNodes: function(a){return getIntersectPoints_C(p,a,radius);},
		getPairs: function(n,a){return createPairs_C(p,n,a,radius);},
		cut: function(s,e,d){return createCut_C(s,e,d,p,radius,8);},
		outside: function(px,py){return dist(px,py,p.x,p.y)>radius;}
	};
    for(var a in asteroids){
        var ast = asteroids[a];
        if(ast!==0){
            if(getIntersectPoints_C(p,ast,radius).length>0 || dist(ast.x, ast.y, p.x, p.y)<=radius){
                var created = deformBody(ast, shape);
                for(var i in created){
                    i = created[i];
                    var angle = angleFromVertical(asteroids[i],p);
                    var force = (radius/15)/asteroids[i].size;
                    asteroids[i].xvel -= force*sin(angle);
                    asteroids[i].yvel -= force*cos(angle);
                    var vert = asteroids[i].vertices;
                    for(var v in vert){
                        var ang_p = angleFromVertical(vert[v],p);
                        var ang_c = angleFromVertical(asteroids[i],vert[v]);
                        var d = dist(vert[v].x, vert[v].y, p.x, p.y);
                        var torque = vert[v].radius*radius*(1/sq(d))*sin(ang_p-ang_c);
					    asteroids[i].rotationSpeed -= torque/sq(asteroids[i].size);             
                    }
                }
                var amount = 10;
                var dv = (radius/10);
                var len = debris.length;
                for(var i = len; i<len+random(amount, amount+10); i++){
                    debris[i] = {
                        x:p.x, 
                        y:p.y, 
                        xvel: (player.powerups.pause>millis()?0:ast.xvel) + random(-dv,dv), 
                        yvel: (player.powerups.pause>millis()?0:ast.yvel) + random(-dv,dv), 
                        alive_time:millis(), 
                        size:random(1,2)};   
                }
                asteroids[a] = 0;
            }
        }
    }
    fill(166, 166, 166);
    noStroke();
    ellipse(p.x,p.y,2*(radius+2),2*(radius+2));
};

var drawPlayerPieces = function(){
    stroke(255, 255, 255);
    for(var p in player.playerPieces){
        line(player.playerPieces[p].x1, player.playerPieces[p].y1, player.playerPieces[p].x2, player.playerPieces[p].y2);       
    }
};

var movePlayerPieces = function(){
    for(var i in player.playerPieces){
        var p = player.playerPieces[i];
        var rad = dist(p.x1, p.y1, p.x2, p.y2)/2;
        var center ={x:p.x1-(p.x1-p.x2)/2, y: p.y1-(p.y1-p.y2)/2};
        if(center.x<0){center.x = width;}
        if(center.x>width){center.x = 0;}
        if(center.y<0){center.y = height;}
        if(center.y>height){center.y = 0;}
        p.angle = (p.angle + p.rotation)%360;
        p.x1 = center.x-rad*sin(p.angle)+p.xvel;
        p.y1 = center.y-rad*cos(p.angle)+p.yvel;
        p.x2 = center.x-rad*sin(p.angle + 180)+p.xvel;
        p.y2 = center.y-rad*cos(p.angle + 180)+p.yvel;
        player.playerPieces[i] = p;
    }
};

var drawPlayerDeath = function(r,l){
    var piece_c = [];
    var center = {x:0,y:0};
    for(var p in player.playerPieces){
        var piece = player.playerPieces[p];
        var m_x = piece.x1-(piece.x1-piece.x2);
        var m_y = piece.y1-(piece.y1-piece.y2);
        var pc = {x:m_x, y: m_y};
        piece_c[piece_c.length] = pc;
        center.x += pc.x;
        center.y += pc.y;
    }
    center.x /= piece_c.length;
    center.y /= piece_c.length;
    var avg = 0;
    for(var p in piece_c){
        var c = [];
        var d = dist(center.x, center.y, piece_c[p].x, piece_c[p].y);
        if(d<200){
            avg += d;
            addChainNode(c,center);
            addChainNode(c,piece_c[p]);
            sparkChain(c); 
        }
    }
    avg /= piece_c.length;
    var m = 6*(r/l);
    var extra = random(0,m);
    for(var i = 0; i<extra; i++){
        var c = [];
        
        var p = {
            x:center.x+random(-m*avg,m*avg),
            y:center.y+random(-m*avg,m*avg)
        };
        addChainNode(c,center);
        addChainNode(c,p);
        sparkChain(c);   
    }
    return avg > 50;
};

var moveDebris = function(){
    for(var d in debris){
        debris[d].x += debris[d].xvel;
        debris[d].y += debris[d].yvel;
        if(debris[d].x<0){debris[d].x = width;}
        if(debris[d].x>width){debris[d].x = 0;}
        if(debris[d].y<0){debris[d].y = height;}
        if(debris[d].y>height){debris[d].y = 0;}
    }
};

var despawnDebris = function(){
	for(var d in debris){
		if(debris[d]!==0 && debris[d].alive_time + debrisLifeTime + random(-2000, 2000) < millis()){
			debris[d] = 0;
		}
	}
};

var drawDebris = function(){
    for(var d in debris){
        strokeWeight(debris[d].size);
        point(debris[d].x, debris[d].y);  
    }
    strokeWeight(1);
};

var newPowerup = function(){
    if(powerupToggle){
        var index = floor(random(0,totalWeight));
        var t;
        for(var p in powerup_types){
            if(powerup_types[p].seqWeight>index && 
                (powerup_types[p].seqWeight - powerup_types[p].weight)<=index){
                t = powerup_types[p];   
            }
        }
        var start = [{x:0,y:random(0,height)},{x:width,y:random(0,height)},
                    {x:random(0,width),y:0},{x:random(0,width),y:height}];
        var index = floor(random(0, 4));
        powerups[powerups.length] = {
            x: start[index].x,
            y: start[index].y,
            dv: 2,
            angle: random(0, 360),
            type: t.type,
            letter: t.letter,
            radius: 15
        };
    }
};

var drawPowerups = function(){
    textSize(9);
    stroke(255, 255, 255);
    for(var p in powerups){
        fill(0, 0, 0);
        ellipse(powerups[p].x, powerups[p].y, powerups[p].radius, powerups[p].radius);
        fill(255, 255, 255);
        text(powerups[p].letter, powerups[p].x-3, powerups[p].y+4);
    }
};

var drawTimers = function(){
    var x = width - 20;
    var y = 20;
    var count = 0;
    textSize(9);
    stroke(158, 158, 158);
    strokeWeight(1);
    for(var p in player.powerups){
        if(player.powerups[p]>millis()){
            fill(158, 158, 158);
            arc(x, y, 20, 20, 1, (player.powerups[p] - millis())/powerup_types[count].duration*360);
            fill(255, 255, 255);
            text(powerup_types[count].letter, x - 3, y + 4);
            if(dev){
                var t = player.powerups[p] - millis();
                fill(255, 0, 0);
                text(t, x-textWidth(t)-15, y+5);
            }
            y += 30;
        }
        count += 1;
    }
};

var movePowerups = function(){
    for(var p in powerups){
        var boost = 0;
        if(!player.dead && player.powerups.magnet>millis()){
            var d = dist(player.x, player.y, powerups[p].x, powerups[p].y);
            var range = 100;
            var maxR = 45;
            var strength = exp(-d/range);
            var ap = angleFromVertical(player, powerups[p]);
            powerups[p].rotation = maxR*strength;
            powerups[p].angle = turnTowards(powerups[p], ap);
            boost = strength*3;
        }
        powerups[p].x -= (powerups[p].dv+boost) * sin(powerups[p].angle); 
        powerups[p].y -= (powerups[p].dv+boost) * cos(powerups[p].angle);
        if(powerups[p].x<0){powerups[p].x = width;}
        if(powerups[p].x>width){powerups[p].x = 0;}
        if(powerups[p].y<0){powerups[p].y = height;}
        if(powerups[p].y>height){powerups[p].y = 0;}
    }
};

var checkPowerupCollision = function(){
    var vert = player.vertices;
    for(var p in powerups){
        for(var v in vert){
            if(dist(vert[v].x,vert[v].y,powerups[p].x,powerups[p].y)<powerups[p].radius/2){
                for(var t in powerup_types){
                    if(powerups[p].type === powerup_types[t].type){
                        player.powerups[powerups[p].type] = millis() + powerup_types[t].duration;
                    }
                }
                powerups[p] = 0;
                if(!mute){playSound(getSound("retro/coin"));}
                break;
            }
        }
    }
};

var newMissile = function(side){
    var target = 0;
    for(var a in asteroids){
        if(target===0 || dist(target.x, target.y, player.x, player.y) > dist(asteroids[a].x, asteroids[a].y, player.x, player.y)){
            target = asteroids[a];    
        }
    }
    var spread = 45;
    //var ang = angleFromVertical(target, player) + random(-spread,spread);
    var ang = random(0, 360);
    var vel = 3;
    missiles[missiles.length] = {
        x: player.x,
        y: player.y,
        xvel: player.xvel - vel*sin(ang),
        yvel: player.yvel - vel*cos(ang),
        angle: ang,
        rotation: 15,
        dv: 0.5,
        vert:[],
    };
};

var moveMissiles = function(){
    for(var m in missiles){
        var target = 0;
        for(var a in asteroids){
            if(target===0 || dist(target.x, target.y, missiles[m].x, missiles[m].y) > dist(asteroids[a].x, asteroids[a].y, missiles[m].x, missiles[m].y)){
                target = asteroids[a];    
            }
        }
        var target = {x: target.x + target.xvel, y: target.y + target.yvel};
        var dp = {x: missiles[m].x + missiles[m].xvel, y: missiles[m].y + missiles[m].yvel};
        var ints = intersect_circle(target, missiles[m], dp.x, dp.y, missiles[m].dv);
        var targetAng = missiles[m].angle;
        if(ints.length === 0){
            var p = getClosestPoint(target, missiles[m], dp);
            targetAng = angleFromVertical(p, dp);
        }
        else{
            var p = ints[0];
            if(ints.length > 1){
                if(dist(ints[1].x, ints[1].y, target.x, target.y) < dist(p.x, p.y, target.x, target.y)){
                    p = ints[1];   
                }
            }
            targetAng = angleFromVertical(p, dp);
        }

        var angle = missiles[m].angle%360;
        if(dev){
            var cr = 30;
            var pr = 10;
            stroke(255, 0, 0, 100);
            line(missiles[m].x, missiles[m].y, target.x, target.y);
        }
        
        missiles[m].angle = turnTowards(missiles[m], targetAng);
        missiles[m].xvel += -missiles[m].dv*sin(missiles[m].angle);
        missiles[m].yvel += -missiles[m].dv*cos(missiles[m].angle);
        missiles[m].x += missiles[m].xvel;
        missiles[m].y += missiles[m].yvel;
        if(missiles[m].x<0){missiles[m].x = width;}
        if(missiles[m].x>width){missiles[m].x = 0;}
        if(missiles[m].y<0){missiles[m].y = height;}
        if(missiles[m].y>height){missiles[m].y = 0;}
        var x = missiles[m].x;
        var y = missiles[m].y;
        angle = missiles[m].angle;
        missiles[m].vert = [{x:x-5*sin(angle),y:y-5*cos(angle)},
                            {x:x-3*sin(angle-135),y:y-3*cos(angle-135)},
                            {x:x-3*sin(angle+135),y:y-3*cos(angle+135)}]; 
        var dv = 2;
        for(var i = 0;i<random(1,4);i++){
            debris[debris.length] = {
                x:missiles[m].x,
                y:missiles[m].y,
                xvel:random(-dv,dv)+missiles[m].dv*sin(missiles[m].angle)*2,
                yvel:random(-dv,dv)+missiles[m].dv*cos(missiles[m].angle)*2,
                alive_time:millis()-(debrisLifeTime-50),
                size:random(1,3)
            };
        }
    }
};

var drawMissiles = function(){
    fill(255,255,255);
    stroke(255, 255, 255);
    for(var m in missiles){
        beginShape();
        for(var v in missiles[m].vert){
            vertex(missiles[m].vert[v].x,missiles[m].vert[v].y);   
        }
        vertex(missiles[m].vert[0].x,missiles[m].vert[0].y);
        endShape();
    }
};

var checkMissileCollisions = function(){
    var collision = false;
    for(var m in missiles){
        for(var a in asteroids){
            if(asteroids[a]!==0){
                if(dist(missiles[m].vert[0].x,missiles[m].vert[0].y,asteroids[a].x,asteroids[a].y)<= asteroids[a].size * 20){
                    if(checkCollisions(missiles[m].vert,asteroids[a].vertices)){
                        if(!mute){playSound(getSound("retro/boom1"));}
                        makeExplosion(missiles[m],25);
                        missiles[m] = 0;
                        player.score+=5;
                        collision = true;
                        break;
                    }
                }
            }
        }
    }
    if(collision){
        asteroids = condenseMap(asteroids);
        missiles = condenseTable(missiles);
    }
};

var checkLaserCollisions = function(){
    var collision = false;
	for(var l in lasers){
        for(var a in asteroids){
            if(asteroids[a]!==0){
                if(dist(asteroids[a].x,asteroids[a].y,lasers[l].x,lasers[l].y) <= asteroids[a].size * 20){
                    if(checkCollisions([lasers[l]],asteroids[a].vertices)){
                        player.score += 10;
                        if(!mute){playSound(getSound("retro/boom1"));}
                        var exp = lasers[l].size*17;
                        makeExplosion(lasers[l],exp);  
                        lasers[l] = 0;
                        collision = true;
                        break;
                    }
                }
            }
        }
    }
    if(collision){
        asteroids = condenseMap(asteroids);
        lasers = condenseTable(lasers);
    }
};

var calculateIntercept = function(target, margin){
    var t = 0;
    var err = target.size * 10 * margin;
    var count = 0;
    do{
        for(var i = 0;i<1;i+=0.2){
            var d = dist(target.x + target.xvel*t*i, target.y+ target.yvel*t*i, player.x, player.y);
            t = d/laserSpeed;
            if(dist(target.x + target.xvel*t*i, target.y+ target.yvel*t*i,player.x + laserSpeed*t*i*sin(auto_t.angle), player.y + laserSpeed*t*i*cos(auto_t.angle))<err){
               return t;
            }
        }
        count+=1;
    }while(count<10); 
    return t;
};

var targetInterior = function(target, center, percent){
    var p = {x: target.vert[0].x, y: target.vert[0].y};
    var n;
    for(var v in target.vert){
        var a = target.vert[v].angle+target.rt*1;
        var tx = (target.x+target.xvel)+sin(a)*target.vert[v].radius;
        var ty = (target.y+target.yvel)+cos(a)*target.vert[v].radius;
        if(dist(p.x, p.y, center.x, center.y)> dist(tx, ty, center.x, center.y)){
            p.x = target.vert[v].x;
            p.y = target.vert[v].y;
            n = v;
        }
    }
    var c = {x:p.x, y:p.y};
    for(var v in target.vert){
        if(v!==n){
            var p1 = getOffsetPoint(p, target.vert[v], -0.1);
            var p2 = getOffsetPoint(target.vert[v], p, -0.1);
            if(checkCollisions([getOffsetPoint(p, target.vert[v], -0.1)], target.vert) && !checkCollisions_inter([p1,p2], target.vert) && dist(p.x,p.y,target.vert[v].x,target.vert[v].y) <= dist(p.x,p.y,target.x,target.y)){
                c.x = (c.x+target.vert[v].x)/2;
                c.y = (c.y+target.vert[v].y)/2;
            }
        }
    }
    return getOffsetPoint(p, target, -dist(p.x,p.y,c.x,c.y)*percent);
};

var moveAuto_t = function(){
    var target = 0;
    var scale = auto_t.scale;
    for(var a in asteroids){
        var size = asteroids[a].size*10;
        var xsign = abs(asteroids[a].xvel)/asteroids[a].xvel;
        var ysign = abs(asteroids[a].yvel)/asteroids[a].yvel;
        if(target===0 || 
        dist(target.x+target.xvel*scale, target.y+target.yvel*scale, player.x, player.y) > 
        max(dist(asteroids[a].x+max(xsign*size,asteroids[a].xvel*scale), asteroids[a].y+max(ysign*size,asteroids[a].yvel*scale), player.x, player.y),asteroids[a].size*10)){
            target = asteroids[a];    
        }
    }
    target = {
        x:target.x, 
        y: target.y, 
        xvel: player.powerups.pause>millis()?0:target.xvel, 
        yvel: player.powerups.pause>millis()?0:target.yvel, 
        size: target.size, 
        vert: target.vertices, 
        rt: target.rotationSpeed
    };
    if(dist(target.x, target.y, player.x, player.y)<120){
        var p = targetInterior(target, player, 0.8);
        target.x = p.x;
        target.y = p.y;
    }
    if(dev){
        stroke(255, 0, 0);
        line(target.x-2,target.y,target.x+2,target.y);
        line(target.x,target.y-2,target.x,target.y+2);
        stroke(255, 255, 255);
    }
    var margin = 0.95;
    var t = calculateIntercept(target, 1-margin);
    target.x += target.xvel*t;
    target.y += target.yvel*t;
    var aa = angleFromVertical(target,player);
    auto_t.angle = turnTowards(auto_t, aa);
    var a = atan((5*target.size*margin)/dist(target.x,target.y,player.x,player.y));
    auto_t.fire = abs(aa-auto_t.angle)<a;
    if(dev){
        var l = auto_t.bLength+auto_t.radius+5;
        stroke(255, 0, 0);
        line(player.x, player.y, player.x-l*sin(aa-a), player.y-l*cos(aa-a));
        line(player.x, player.y, player.x-l*sin(aa+a), player.y-l*cos(aa+a));
        aa = -1*(aa-180);
        noFill();
        arc(player.x,player.y,l*2,l*2,(aa-a)+90,(aa+a)+90);
    }
};

var spawnInitialAsteroids = function(){
    var dv = velocity;
	for(var i=0;i<numAsteroids;i++)
	{
		var start = [{x:0,y:random(0,height)},{x:width,y:random(0,height)},
					{x:random(0,width),y:0},{x:random(0,width),y:height}];
		var index = floor(random(0, 4));
		newAsteroid(start[index].x,start[index].y,spawnSize,random(-dv,dv), random(-dv,dv));
	}
};

var drawBackground = function(){
    background(0,0,0);
    stroke(255, 255, 255);
    fill(255, 255, 255);
    var rng = new RNG(seed);
    for(var i=0;i<70;i++){
        var w = rng.nextInt()%width;
        var h = rng.nextInt()%height;
        strokeWeight(rng.nextInt()%3);
        point(w,h);
    }
};

var distEdge = function(x,y,dir){
    var edges = [
        {x:0,y:0},
        {x:width+0.01,y:0},
        {x:width,y:height},
        {x:0-0.01,y:height}
    ];
    var laser = [
        {x: x, y: y},
        {x: x-1000*sin(dir), y:y-1000*cos(dir)}
    ];
    var int = [];
    checkCollisions_inter(edges, laser, int);
    return dist(x, y, int[0].x, int[0].y);
};

var drawMegaLaser = function(x,y,radius,vertices){
    stroke(255, 255, 255);
    fill(255, 255, 255);
    ellipse(x, y, radius*2, radius*2);
    beginShape();
    for(var v in vertices){
        vertex(vertices[v].x,vertices[v].y);
    }
    vertex(vertices[0].x,vertices[0].y);
    endShape();
    var dv = 0.9;
    var de = distEdge(player.x,player.y, player.angle+0.001)*0.9;
    for(var i=0;i<5*de/width*2;i++){
        var side = round(random(0,1));
        var d = random(0, de);
        debris[debris.length] = {
                    x:x - d*sin(player.angle),
                    y:y - d*cos(player.angle),
                    xvel:random(-dv,dv),
                    yvel:random(-dv,dv),
                    alive_time:millis()-(debrisLifeTime-900),
                    size:random(1,2)
                };
    }
    
};

var checkMegaLaserCollisions =function(x,y,vertices,radius){
    var condense = false;
    var point = {x:x, y:y};
	var polygon = {
		getNodes: function(a){return getIntersectPoints_P(a,vertices);},
		getPairs: function(n,a){return createPairs_P(n,a,vertices);},
		cut: function(s,e,d){return createCut_P(s,e,d,vertices,1);},
		outside: function(px,py){return !checkCollisions([{x:px,y:py}],vertices);}
	};
	var circle = {
		getNodes: function(a){return getIntersectPoints_C(point,a,radius);},
		getPairs: function(n,a){return createPairs_C(point,n,a,radius);},
		cut: function(s,e,d){return createCut_C(s,e,d,point,radius,0);},
		outside: function(px,py){return dist(px,py,x,y)>radius;}
	};
	var indexes = Object.keys(asteroids);
	
	for(var i in indexes){
	    var ast = asteroids[indexes[i]];
		if(ast!==0){
		    var cuts = [];
		    var contact = false;
			if(checkCollisions(ast.vertices, vertices)){
				deformBody(ast,polygon,cuts);
				contact = true;
			}
			else if(getIntersectPoints_C(point,ast,shieldRadius).length>0){
			    deformBody(ast,circle,cuts);
				contact = true;
			}
			if(contact){
                var dv = sqrt(sq(ast.xvel)+sq(ast.yvel))/1.5;
                var rdv = 1;
                for(var c in cuts){
                    for(var p = 0; p<cuts[c].length-1;p++){
                        var r = random(3,5);
                        var s = dist(cuts[c][p].x,cuts[c][p].y,cuts[c][p+1].x,cuts[c][p+1].y)/r;
                        var av = angleFromVertical(ast,player);
                        var angle = player.angle+(av>abs(player.angle)?-90:90) + random(-15,15);
                        for(var a = 0;a<r;a++){
                            var op= getOffsetPoint(cuts[c][p],cuts[c][p+1],-s*a); 
                            debris[debris.length] = {
                                x:op.x,
                                y:op.y, 
                                xvel: dv*sin(angle)+random(-rdv,rdv), 
                                yvel: dv*cos(angle)+random(-rdv,rdv), 
                                alive_time:millis()-3500+1000*random(-1,1), 
                                size:random(1,2)
                            };   
                        }
                    }
                }
                asteroids[indexes[i]] = 0;
                player.score+=5;
				contact = true;
				condense = true;
			}
		}
	}
    if(condense){
        asteroids = condenseMap(asteroids);
    }
    
};

var increaseIntensity = function(chain, intensity){
    for(var c in chain){
        chain[c].intensity = intensity;
    }
};

var inChain = function(index, chain){
    for(var i in chain){
        if(chain[i].index===index){
            return true;   
        }
    }
    return false;
};

var cutChain = function(chain, cut){
    var temp = [];
    for(var i = 0;i<cut; i++){
        temp[i] = chain[i];
    }
    return temp;
};

var checkChain = function(chain, maxDist){
    for(var i = 0; i<chain.length; i++){
        if(!(chain[i].index in asteroids)){
            return cutChain(chain, i);   
        }
        chain[i].x = asteroids[chain[i].index].x;
        chain[i].y = asteroids[chain[i].index].y;
        if(i+1<chain.length){
            if(!(chain[i+1].index in asteroids)){
                return cutChain(chain, i+1);   
            }
            chain[i+1].x = asteroids[chain[i+1].index].x;
            chain[i+1].y = asteroids[chain[i+1].index].y;
            if(dist(chain[i].x, chain[i].y, chain[i+1].x, chain[i+1].y)>maxDist){
                return cutChain(chain, i+1);
            }
        }
    }
    return chain;
};

var moveZap = function(maxDist){
    if(dist(zap.x, zap.y, player.x, player.y)<maxDist){
        zap.x -= zap.dv*sin(zap.angle);
        zap.y -= zap.dv*cos(zap.angle);
        var jumpDist = 30;
        for(var a in asteroids){
            if(dist(asteroids[a].x, asteroids[a].y, zap.x, zap.y)<jumpDist*asteroids[a].size){
                if(dev){
                    var d = jumpDist*asteroids[a].size;
                    stroke(255, 0, 0);
                    noFill();
                    ellipse(asteroids[a].x,asteroids[a].y,d,d);   
                }
                zapChain = [];
                addChainNode(zapChain, asteroids[a], a);
                return true;
            }
        }
    }else{
        zap = 0;
        return false;
    }
};

var zapAsteroids = function(chain){
    var cut = [{x:player.x,y:player.y}].concat(chain);
    var ang = angleFromVertical(cut[cut.length-1],cut[cut.length-2])+random(-15,15);
    var last = chain[chain.length-1];
    cut[cut.length] = {x:last.x-100*sin(ang), y: last.y-100*cos(ang)};
    for(var c = cut.length-2; c>0; c--){
        cut[cut.length] = {x: cut[c].x, y: cut[c].y};
    }
    var shape = {
        getNodes: function(a){return getIntersectPoints_P(a,cut);},
        getPairs: function(n,a){return createPairs_P(n,a,cut);},
        cut: function(s,e,d){return createCut_P(s,e,d,cut,0);},
        outside: function(px,py){return true;}
    };
    var explosions = [];
    var dx = 10;
    for(var c in chain){
        var ast = asteroids[chain[c].index];
        deformBody(ast,shape);
        explosions[explosions.length] = {x: ast.x + random(-dx,dx), y: ast.y + random(-dx,dx)};
        player.score+=10;
        asteroids[chain[c].index] = 0;
    }
    asteroids = condenseMap(asteroids);
    for(var e in explosions){
        makeExplosion(explosions[e], 20);   
    }
    asteroids = condenseMap(asteroids);
};

var chainZaps = function(maxDist, arch){
    zapChain = checkChain(zapChain, maxDist+200);
    if(zapChain.length===0 || dist(zapChain[0].x, zapChain[0].y, player.x, player.y)>maxDist+200){
        zapChain = 0;
        zap = 0;
        return false;
    }
    if(arch<millis()){
        zapAsteroids(zapChain);
        asteroids = condenseMap(asteroids);
        zapChain = 0;
        zap = 0;
        return false;
    }
    var found = false;
    var last = zapChain[zapChain.length-1];
    for(var a in asteroids){
        var ast = asteroids[a];
        if(!inChain(a,zapChain)){
            if(dist(last.x, last.y, ast.x, ast.y) <= maxDist){
                addChainNode(zapChain, asteroids[a], a);
                found = true;
                break;
            }
        }
    }
    if(zapChain.length >= 10){
        arch = millis();
        found = false;
    }
    return found;
};

var getTotalAsteroids = function(){
    var total = 0;
    for(var a in asteroids){
        total += asteroids[a].size;   
    }
    return total;
};

var drawGraph = function(points,res,x,y,times){
    textFont('Arial',10);
    var w = 150;
    var h = 50;
    var minValue = points.value;
    var avg = points.value;
    for(var cur = points;cur!==null; cur=cur.child){
        avg = (avg+cur.value)/2;
        minValue = max(0,min(minValue, cur.value));
    }
    minValue = round(min(3*avg/5, minValue));
    var maxValue = round(avg+(avg-minValue));
    avg = round(avg);
    
    stroke(255, 0, 0);
    fill(255, 0, 0);
    strokeWeight(1);
    text('FPS ['+points.end().value+']',x,y);
    text(mode,x+w-textWidth(mode),y+h+10);
    if(avg>0){
        text(avg,x-textWidth(avg)-1,y+h/2+4);
        line(x,y+h/2,x+4,y+h/2);
        text(maxValue,x-textWidth(avg)-1,y+4);
        line(x,y,x+4,y);
        text(minValue,x-textWidth(avg)-1,y+h+4);
        line(x,y+h,x+4,y+h);
    }
    noStroke();
    fill(255, 255, 255, 10);
    rect(x,y,w,h);
    stroke(255, 0, 0);
    fill(255, 0, 0);
    
    line(x,y,x,y+h);
    line(x,y+h,x+w,y+h);
    
    text(0,x+w,y+h-5);
    line(x+w,y+h,x+w,y+h-2);
    text(res/2,x+w/2-textWidth(res/2)/2,y+h-5);
    line(x+w/2,y+h,x+w/2,y+h-2);
    noFill();
    beginShape();
    var off = false;
    var l = points.length();
    var count = 0;
    for(var cur = points.end();cur!==null && count<=res;cur=cur.parent){
        var ypos = y+h-(h/(maxValue-minValue))*(cur.value-minValue);
        var xpos = x+w-(l-cur.index-1)*(w/res);
        if(ypos<y && !off){
            if(cur.hasChild()){
                var x2 = xpos;
                var x1 = x2 + (w/res);
                var y2 = ypos;
                var y1 = y+h-(h/(maxValue-minValue))*(cur.child.value-minValue);
                var inter = intersection(x,y,x+w,y+1,x1,y1,x2,y2);
                vertex(inter.x,inter.y);
            }
            endShape();
            off = true;
        }
        else if(ypos>=y && off){
            beginShape();
            if(cur.hasChild()){
                var x2 = xpos;
                var x1 = x2 + (w/res);
                var y2 = ypos;
                var y1 = y+h-(h/(maxValue-minValue))*(cur.child.value-minValue);
                var inter = intersection(x,y,x+w,y+1,x1,y1,x2,y2);
                vertex(inter.x,inter.y);
            }
            vertex(xpos,ypos);   
            off = false;
        }
        else if(ypos>=y && !off){
            vertex(xpos,ypos); 
        }
        count+=1;
    }
    endShape();
    
    var numVertices = 0;
    for(var a in asteroids){
        numVertices += asteroids[a].vertices.length;   
    }
    
    var stats = [
        '('+times.min +', '+ times.avg+', '+ times.max +') ms',
        Object.keys(asteroids).length+' ['+round(getTotalAsteroids())+' , '+numAsteroids*spawnSize+']',
        numVertices,
        debris.length,
        lasers.length,
    ];
    for(var i =0; i<stats.length; i++){
        text(stats[i],x,y+h+10+i*10);   
    }
};

var disableButtons = function(state){
    for(var g in buttonGroups){
        buttonGroups[g].disableAll(true);
    }
};

var drawMenu = function(){
    fill(255,255,255);
    textFont('Arial Bold',48);
    textAlign(CENTER, CENTER);
    text('ASTEROIDS',width/2,70);  
    textAlign(LEFT);
    for(var g in buttonGroups){
        buttonGroups[g].drawAll();
    }
};

var setMode = function(m){
    mode = m;
    asteroids = {};
    spawnSize = presets[m].size;
    numAsteroids = presets[m].asteroids;
    numPowerups = presets[m].powerups;
    velocity = presets[m].velocity;
    spawnInitialAsteroids();
};

var resetGame = function(){
    asteroids = {};
    lasers = [];
    debris = [];
    powerups = [];
    missiles = [];
    nanobots = [];
    player = {
        x:width/2,
        y:height/2,
        xvel:0,
        yvel:0,
        angle:0,
        rotation:0,
        score:0,
        charge:0,
        megaLaserDist: 18,
        thrust:false,
        dead:false,
        playerPieces:[],
        powerups:{}
    };
    auto_t = {
        angle:0, 
        rotation: 10, 
        radius: 4, 
        bLength: 7,
        scale: 15,
        fire: false
        
    };
    for(var p in powerup_types){
        player.powerups[powerup_types[p].type]=0;   
    }
    opaq = 255;
    spawnInitialAsteroids();
    reset = false;
    seed = random()*100000;
    for(var b in secretButtons.buttons){
        if(secretButtons.buttons[b].toggled && secretButtons.buttons[b].powerup){
            var type;
            for(var p in powerup_types){
                if(powerup_types[p].letter===secretButtons.buttons[b].text){
                    type = powerup_types[p].type;   
                }
            }
            player.powerups[type] = millis()+10000000;
        }
    }
    powerup = millis();
    loop();
};

var anonFunc = function(mode){
    return function(){setMode(mode);}; 
};

var checkPlayerCollision = function(i){
    if(checkCollisions(player.vertices,asteroids[i].vertices)){
        player.dead = true;
        if(!mute){
            playSound(getSound("retro/boom2"));
            playSound(getSound("retro/rumble"));
        }
        var extreme = player.powerups.laser>millis() || player.powerups.zap>millis() || player.powerups.emitter>millis() || player.powerups.defenselaser>millis();
        var dv = extreme ? 5:2;
        for(var k = 0; k<4; k++){
            var j = k+1%player.vertices.length;
            var p = {x1:player.vertices[k].x, y1:player.vertices[k].y,
                    x2:player.vertices[j].x, y2:player.vertices[j].y};
            var rad = dist(p.x1, p.y1, p.x2, p.y2)/2;
            var center ={x:p.x1-(p.x1-p.x2)/2, y: p.y1-(p.y1-p.y2)/2};
            var a = asin((center.x-p.x1)/rad);
            player.playerPieces[k] = {
                x1:p.x1, y1:p.y1, x2:p.x2, y2:p.y2,
                rotation:random(-30,30),
                angle:a,
                xvel:player.xvel-(player.xvel-asteroids[i].xvel)/1.05+random(-dv,dv),
                yvel:player.yvel-(player.yvel-asteroids[i].yvel)/1.05+random(-dv,dv)
            };
        }
        var dx = player.xvel - asteroids[i].xvel;
        var dy = player.yvel - asteroids[i].yvel;
        asteroids[i].xvel = asteroids[i].xvel*(1 - 1/pow(asteroids[i].size+1,2))+player.xvel*(1/pow(asteroids[i].size+1,3));
        asteroids[i].yvel = asteroids[i].yvel*(1 - 1/pow(asteroids[i].size+1,2))+player.yvel*(1/pow(asteroids[i].size+1,3));
        var r = extreme ? 25 : max(12,20 * sqrt(sq(dx)+sq(dy))/maxSpeed);
        makeExplosion(player,r);
        asteroids = condenseTable(asteroids);
        var len = debris.length;
        for(var d = len; d<len+15; d++){
            debris[d] = {
                x:player.x, 
                y:player.y, 
                xvel:player.xvel+random(-dv,dv), 
                yvel:player.yvel+random(-dv,dv), 
                alive_time:millis(), 
                size:random(1,3)};   
        }
        dead = millis();
    }  
};

var checkShieldCollision = function(i,radius){
    var vert = asteroids[i].vertices;
	var radius = shieldRadius+2;
	var shape = {
		getNodes: function(a){return getIntersectPoints_C(player,a,radius);},
		getPairs: function(n,a){return createPairs_C(player,n,a,radius);},
		cut: function(s,e,d){return createCut_C(s,e,d,player,radius,0);},
		outside: function(px,py){return dist(px,py,player.x,player.y)>radius;}
	};

    if(getIntersectPoints_C(player,asteroids[i],shieldRadius).length>0){
        var cuts = [];
        var newAsts = deformBody(asteroids[i],shape,cuts);
        var dv = 1.5 * sqrt(sq(player.xvel-asteroids[i].xvel)+sq(player.yvel-asteroids[i].yvel))/7;
        var fx=0;
        var fy=0;
        for(var c in cuts){
            for(var p = 0; p<cuts[c].length-2;p++){
                var r = random(1,3);
                var s = dist(cuts[c][p].x,cuts[c][p].y,cuts[c][p+1].x,cuts[c][p+1].y)/r;
                for(var a = 0;a<r;a++){
                    var op= getOffsetPoint(cuts[c][p],cuts[c][p+1],-s*a); 
                    var angle = angleFromVertical(player,op)+random(-15,15);
                    debris[debris.length] = {
                        x:op.x,
                        y:op.y, 
                        xvel: dv*sin(angle) + player.xvel, 
                        yvel: dv*cos(angle) + player.yvel, 
                        alive_time:millis()-4500+1000*random(-1,1), 
                        size:random(1,2)
                    };   
                }
                var resist = 80;
                var angle = angleFromVertical(player,cuts[c][p]);
                var p_angle = angleFromVertical(asteroids[i],cuts[c][p]);
                var v = 2*PI*dist(asteroids[i].x,asteroids[i].y,cuts[c][p].x,cuts[c][p].y)*asteroids[i].rotationSpeed/360;//2*pi*r*f
                fx -= sin(angle) * abs(player.xvel-(player.powerups.pause>millis()?0:(asteroids[i].xvel+v*sin(p_angle+90))))/resist ;
                fy -= cos(angle) *abs(player.yvel-(player.powerups.pause>millis()?0:(asteroids[i].yvel+v*cos(p_angle+90))))/resist ;
                
            }
            
        }
        player.xvel += fx;
        player.yvel += fy;
        for(var n in newAsts){
            asteroids[newAsts[n]].xvel -= fx/(asteroids[i].size);
            asteroids[newAsts[n]].yvel -= fy/(asteroids[i].size);
        }
        asteroids[i] = 0;
        player.score+=1;
        asteroids = condenseMap(asteroids);
    }
};

var thrust = function(position, angle, offset, intensity){
    angle = angle ? angle : 0.01;
    if(random(10)> 10 * (1-intensity)){
        var dv = 4;
        debris[debris.length] = {
            x:position.x-offset*sin(angle), 
            y:position.y-offset*cos(angle), 
            xvel:player.xvel-sin(angle)*random(-0.5,dv), 
            yvel:player.yvel-cos(angle)*random(-0.5,dv), 
            alive_time:millis()-(debrisLifeTime-700), 
            size:random(1,2)};   
    }    
};

var newNanobot = function(){
    var t = 0;
    for(var p in powerup_types){
        if(powerup_types[p].type==='nanobots'){
            t = powerup_types[p].duration;
            break;
        }
    }
    var d = random(0,360);
    var v = random(7,10);
    nanobots[nanobots.length] = {
        x: player.x,
        y: player.y,
        xvel: player.xvel + v * sin(d),
        yvel: player.yvel + v * cos(d),
        xacc: 0,
        yacc: 0,
        angle: d,
        radius: 8,
        aliveTill: millis() + t + random(-1000,1000),
        rotation: 130,
        maxVel: 6,
        acc: 1.50,
    };
};

var moveNanobot = function(n){
    var nano = nanobots[n];  
    nano.xacc = -nano.acc * sin(nano.angle);
    nano.yacc = -nano.acc * cos(nano.angle);
    var vx = nano.xvel + nano.xacc;
    var vy = nano.yvel + nano.yacc;
    var angle = angleFromVertical({x:0,y:0}, {x:vx,y:vy});
    angle = (angle ? angle : 0.00001);
    var mag = min(nano.maxVel, sqrt(sq(vx)+sq(vy)));
    nano.xvel = mag*sin(angle);
    nano.yvel = mag*cos(angle);
    nano.x += nano.xvel;
    nano.y += nano.yvel;
    if(nano.x>width){nano.x=0;}
    if(nano.x<0){nano.x=width;}
    if(nano.y>height){nano.y=0;}
    if(nano.y<0){nano.y=height; }
};

var controlSwarm = function(){
    if(nanobots.length !== 0){
        var notarget = true;
        var center;
        if(!player.dead){
            center = player;
        }
        else{
            center = {x:nanobots[0].x, y:nanobots[0].y};  
            for(var n in nanobots){
                center.x = (center.x + nanobots[n].x)/2;
                center.y = (center.y + nanobots[n].y)/2;
            }   
        }
        var targets = [];
        var range = Math.max(40 * spawnSize, 150);
        var scale = 2;
        for(var a in asteroids){
            var ast = asteroids[a];
            if(dist(ast.x + ast.xvel*scale, ast.y + ast.yvel*scale, center.x, center.y) < range){
                var t = {x:ast.x, y:ast.y, vert:ast.vertices, xvel: ast.xvel, yvel: ast.yvel};
                var point = targetInterior(t, center, 0.0);
                t.x = point.x;
                t.y = point.y;
                targets[targets.length] = t;
                notarget = false;
            }
        }
        if(dev){
            stroke(255, 0, 0);
            fill(255,0,0,10);
            ellipse(center.x, center.y, range * 2, range * 2);
        }
        if(notarget){
            targets[0] = player.dead ? {x:center.x, y:center.y, xvel:0,yvel:0} : {x:player.x, y:player.y, xvel:player.xvel, yvel:player.yvel};
        }
        var mult = 0.8;
        for(var n in nanobots){
            var nano = nanobots[n];
            var target = targets[0];
            for(var t=1; t<targets.length; t++){
                if(dist(nano.x, nano.y, targets[t].x, targets[t].y) < Math.min(mult * dist(center.x + center.xvel, center.y + center.yvel, targets[t].x, targets[t].y), dist(nano.x, nano.y, target.x, target.y))){
                    target = targets[t];   
                }
            }
            target.x += target.xvel;
            target.y += target.yvel;
            var a = angleFromVertical(target,nano);
            nano.angle = turnTowards(nano, a);
        }
    }
};

var drawNanobot = function(n){
    for(var i = 0; i < random(10,30); i++){
        var nano = nanobots[n];
        var r = nano.radius + 5;
        var c = random(100, 250);
        strokeWeight(1);
        stroke(c, c, c);
        var rad = random(0, r);
        var ang = random(0, 360);
        var accmod = sq(r);
        point(nano.x+rad*sin(ang), nano.y+rad*cos(ang));
    }
};

var defineNanobotDeformShape = function(nano){
    return {
        getNodes: function(a){return getIntersectPoints_C(nano,a,nano.radius);},
        getPairs: function(n,a){return createPairs_C(nano,n,a,nano.radius);},
        cut: function(s,e,d){return createCut_C(s,e,d,nano,nano.radius,0);},
        outside: function(px,py){return dist(px,py,nano.x,nano.y)>nano.radius;}
    };
};

var swarmCollisions = function(){
    for(var n in nanobots){
        var nano = nanobots[n];
        var s = defineNanobotDeformShape(nano);
        var condense = false;
        for(var a in asteroids){
            var ast = asteroids[a];
            if(ast !== 0){
                if(dist(ast.x, ast.y, nano.x, nano.y) < ast.size*20 &&
                getIntersectPoints_C(nano,ast,nano.radius).length>0){
                    deformBody(ast, s, [], 25);
                    if(!player.dead){
                        player.score += 1;
                    }
                    var dv = 1;
                    var n = random(2,5);
                    for(var i=0;i<n;i++){
                        debris[debris.length] = {
                            x:nano.x,
                            y:nano.y,
                            xvel:ast.xvel + random(-dv,dv),
                            yvel:ast.yvel + random(-dv,dv),
                            alive_time:millis()-(debrisLifeTime-1500),
                            size:random(1,2)
                        };
                    }
                    var reduce = 0.9;
                    nano.xvel *= reduce;
                    nano.yvel *= reduce;
                    asteroids[a] = 0;
                    condense = true;
                }
            }
        }
        if(condense){
            asteroids = condenseMap(asteroids);   
        }
    }
};

var calculateMegaLaser = function(x,y,radius, l, enda){
    if(abs(player.prev_angle-player.angle)<5){
		return [
		{x:x -radius*sin(player.angle+90), y:y -radius*cos(player.angle+90)},
		{x:x -radius*sin(player.angle-90), y:y -radius*cos(player.angle-90)},
		{x:x -l*sin(player.angle-enda), y:y -l*cos(player.angle-enda)},
		{x:x -l*sin(player.angle+enda), y:y -l*cos(player.angle+enda)}
		];
	}
	if(player.prev_angle>player.angle){
		return [
		{x:x -radius*sin(player.angle-90), y:y -radius*cos(player.angle-90)},
		{x:x -l*sin(player.angle-enda), y:y -l*cos(player.angle-enda)},
		{x:x -l*sin(player.prev_angle+enda), y:y -l*cos(player.prev_angle+enda)},
		{x:x -radius*sin(player.prev_angle+90), y:y -radius*cos(player.prev_angle+90)}
		];
	}
	else{
		return [
		{x:x -radius*sin(player.prev_angle-90), y:y -radius*cos(player.prev_angle-90)},
		{x:x -l*sin(player.prev_angle-enda), y:y -l*cos(player.prev_angle-enda)},
		{x:x -l*sin(player.angle+enda), y:y -l*cos(player.angle+enda)},
		{x:x -radius*sin(player.angle+90), y:y -radius*cos(player.angle+90)}
		];
	}   
};

var aimDefenseLaser = function(){
    var targets = [];
    for(var a in asteroids){
        var ast = asteroids[a];
        if(dist(player.x, player.y, ast.x, ast.y)-ast.size*10 <= defenseLaserRadius){
            targets[targets.length] = ast;
        }
    }
    if(targets.length === 0){
        return 0;
    }
    targets.sort(function(b,a){return dist(a.x,a.y,player.x,player.y) - dist(b.x,b.y,player.x,player.y);});
    var d = 2*(-1/(pow(random(defenseLaserRadius)/defenseLaserRadius,7) + 1) + 1) * defenseLaserRadius;
    var target = targets[targets.length-1];
    for(var t in targets){
        if(dist(targets[t].x, targets[t].y, player.x, player.y) < d){
            target = targets[t];
            break;
        }
    }
    target = {
        x: target.x, 
        y: target.y, 
        xvel: player.powerups.pause>millis()?0:target.xvel, 
        yvel: player.powerups.pause>millis()?0:target.yvel, 
        size: target.size, 
        vert: target.vertices,
        vertices: target.vertices,
        rt: 0
    };
    var point = targetInterior(target,player,random(0.8));
    var angle = angleFromVertical(point, player);
    var vert = [
        {x: player.x, y: player.y},
        {x: player.x - 1000*sin(angle), y: player.y - 1000*cos(angle)}
    ];
    var ints = getIntersectPoints_P(target, vert);
    if(ints.length === 0){
        return 0;   
    }
    ints.sort(function(a,b){return dist(a.point.x,a.point.y,player.x,player.y) - dist(b.point.x,b.point.y,player.x,player.y);});
    return ints[0].point;
};

var drawDefenseLaser = function(p,r){
    var rad = random(0,r);
    strokeWeight(2);
    var c = random(100, 255);
    stroke(255, 255, 255);
    line(player.x+player.xvel, player.y+player.yvel, p.x, p.y);
    stroke(c, c, c);
    ellipse(p.x, p.y, rad*2, rad*2);
    strokeWeight(1);
};

var defenseLaserCollision = function(point,radius){
    var shape = {
		getNodes: function(a){return getIntersectPoints_C(point,a,radius);},
		getPairs: function(n,a){return createPairs_C(point,n,a,radius);},
		cut: function(s,e,d){return createCut_C(s,e,d,point,radius,0);},
		outside: function(px,py){return dist(px,py,point.x,point.y)>radius;}
	};
    var condense = false;
    for(var a in asteroids){
        var ast = asteroids[a];
        if(ast !== 0){
            if(dist(ast.x, ast.y, point.x, point.y) < ast.size*20 &&
            getIntersectPoints_C(point,ast,radius).length>0){
                deformBody(ast, shape, [], 25);
                player.score += 1;
                var dv = 2;
                var n = random(2,5);
                for(var i=0;i<n;i++){
                    debris[debris.length] = {
                        x:point.x,
                        y:point.y,
                        xvel:ast.xvel + random(-dv,dv),
                        yvel:ast.yvel + random(-dv,dv),
                        alive_time:millis()-(debrisLifeTime-1500),
                        size:random(1,2)
                    };
                }
                asteroids[a] = 0;
                condense = true;
            }
        }
    }
    if(condense){
        asteroids = condenseMap(asteroids);   
    }
};

{modeButtons = new ButtonGroup();
modeButtons.multiToggle = false;
otherButtons = new ButtonGroup();
secretButtons = new ButtonGroup();
buttonGroups[buttonGroups.length] = modeButtons;
buttonGroups[buttonGroups.length] = secretButtons;
buttonGroups[buttonGroups.length] = otherButtons;
var frameData = {min: 0, avg:0, max: 0};
var frameTimer = millis();

var y = 130;
var w = 170;
var h = 30;
for(var p in presets){
    var b = new Button(p,width/2-w/2,y,w,h,anonFunc(p));
    b.toggle = true;
    b.toggled = p===mode;
    if(p===mode){
        modeButtons.selected = [modeButtons.buttons.length];   
    }
    modeButtons.add(b);
    y+=35;
}
w = 200;
var start_b = new Button(
    'Start', 
    width/2-w/2,
    height-40,
    w,
    h,
    function(){menu=false;disableButtons(true);resetGame();});
otherButtons.add(start_b);

var dev_b = new Button(
    '',
    width-20,
    height-20,
    10,
    10,
    function(){dev = !dev;},
    color(5, 5, 5));
dev_b.toggle = true;
dev_b.hoverable = false;
otherButtons.add(dev_b);


var secretButtonColor = color(170, 170, 170, 255 * (4/5));
y = height/2 - 25*powerup_types.length/2;
var togglePowerup = function(){powerupToggle = !powerupToggle;};

var b = new Button(
    "Spawn",
    width-110,
    y - 30,
    80,
    20,
    togglePowerup,
    secretButtonColor
);
b.disabled = true;
b.toggle = true;
b.toggled = true;

secretButtons.add(b);

var emptyFunc = function(){};
for(var p in powerup_types){
    var b = new Button(
        powerup_types[p].letter,
        width-50,
        y,
        20,
        20,
        emptyFunc,
        secretButtonColor
    );
    b.disabled = true;
    b.toggle = true;
    b.powerup = true;
    secretButtons.add(b);
    y+=25;
    
    var pause = false;
}


var fpsGraph = new LinkedList();
var frameTimeList = new LinkedList();
setMode(mode);
}

var draw = function(){
    if(reset && opaq===255){
        noLoop();
        resetGame();
    }
    if(fpsTimer<millis()){
        while(fpsGraph.length()>=resolution*2){
            fpsGraph.remove();
        }
        fpsGraph.append(fps);
        fps = 0;
        fpsTimer = millis()+1000;
    }
    if(frameTimer<millis()){
        var frameTotal = 0, frameMin = 9999, frameMax = 0;
        for(var c = frameTimeList; c !== null; c=c.child){
            frameTotal += c.value;
            frameMin = Math.min(frameMin, c.value);
            frameMax = Math.max(frameMax, c.value);
        }
        frameData = {
            min: frameMin, 
            avg: round(frameTotal/frameTimeList.length()),
            max: frameMax
        };
        frameTimer = millis() + 100;
    }
    if(!trip){
        drawBackground();
    }
    if(player.powerups.emitter>millis() && emitterShot<millis() && !player.dead){
        var count = 4;
        for(var i=0;i<count;i++){
            newLaser(player.x, player.y, player.angle+(360/count*i)+ emitterAngle);   
        }
        emitterShot = millis() + (player.powerups.reload > millis() ? 10 : 100);
        emitterAngle += (player.powerups.reload > millis() ? 5 : 15);
    }
    if(zap!==0 && !player.dead){
        var maxDist = 130;
        var wait = 300;
        if(zapChain===0){
            if(moveZap(maxDist+50)){
                arch = millis()+wait;
            }
            if(zap!==0){
                sparkChain([zap,{x:player.x-10*sin(player.angle), y:player.y-10*cos(player.angle)}]);
            }
        }else{
            if(chainZaps(maxDist, arch)){
                arch = millis()+wait;
            }
            if(zapChain!==0){
                zap.x = player.x-10*sin(player.angle);
                zap.y = player.y-10*cos(player.angle);
                sparkChain([zap].concat(zapChain));
            }
        }
    }
    if(player.powerups.nanobots>millis()){
        if(nanobots.length < maxNanobots){
            for(var i = 0; i < min(random(3,5), maxNanobots - nanobots.length); i++){
                newNanobot();   
            }
        }
    }
    else{
        if(nanobots.length > 0){
            nanobots = [];   
        }
    }
    
    if(player.powerups.defenselaser > millis() && !player.dead && defenseLaserTimer<millis()){
        var radius = 4;
        for(var i = 0 ; i < 4; i++){
            var impact = aimDefenseLaser();
            if(impact !== 0){
                drawDefenseLaser(impact, radius);
                defenseLaserCollision(impact, radius);
            }
        }
        defenseLaserTimer = millis() + 10;
    }
    
    
    strokeWeight(1);
    stroke(255, 255, 255);
    if(player.powerups.pause<millis()){
        moveAsteroids();
    }
    else{
        var opacity = 100;
        stroke(255, 255, 255, opacity);
        fill(255, 255, 255, opacity);
        if(player.powerups.pause-1000 < millis()){
            triangle(width/2 - 30, height/2 - 30, width/2 -30, height/2 + 30, width/2 + 25, height/2);
        }
        else{
            var w = 30;
            var h = 100;
            rect(width/2 - w/2 - 15, height/2 - h/2, w, h);
            rect(width/2 + w/2 + 15, height/2 - h/2, w, h);
        }
    }
    drawAsteroids();
    
    strokeWeight(1);
    stroke(255, 255, 255);
    moveDebris();
    drawDebris();
    
    moveLasers();
    drawLasers();
    
    if(!menu){
        movePowerups();
        drawPowerups();
    }
    
    if(player.dead){   
        var spark_l = 500;
        if(dead + 2000<millis()){
            fill(255, 255, 255);
            textSize(30);
            text("Game Over\nScore: "+player.score,width/2-70,height/2);
        }
        else if(dead + spark_l > millis() && (player.powerups.zap>millis() || player.powerups.laser>millis() || player.powerups.emitter>millis() || player.powerups.defenselaser>millis())){
            drawPlayerDeath(dead + spark_l - millis(), spark_l);   
        }
    }
    else if(!menu){
        movePlayer();
        drawPlayer();
        fill(255, 255, 255);
        text(player.score,1,10);
        textSize(10);
    }
    
    moveMissiles();
    drawMissiles();
    checkMissileCollisions();
    
    controlSwarm();
    for(var n in nanobots){
        moveNanobot(n);
        drawNanobot(n);
    }
    swarmCollisions();
    if(!menu){
        drawTimers();
        
        var delay = player.powerups.reload > millis() ? 100 : 200;
        
        if(!player.dead && player.powerups.missile > millis() && missile+delay*1.5 < millis()){
            missileCount += 1;
            newMissile(missileCount);
            missile = millis();
        }
        
        if(!player.dead){
            checkPowerupCollision();
            powerups = condenseTable(powerups);
        }
        
        if(powerup + 5000 < millis() && powerups.length<numPowerups && !player.dead){
            newPowerup();
            powerup = millis();   
        }
        
        if(!player.dead && player.powerups.auto > millis()){
            moveAuto_t();   
        }
        
        
        if(player.charge>0){
            var radius = max(6*player.charge/100 + (player.charge>80?random(-0.8,0.8):0),2);
            var x = player.x - player.megaLaserDist*sin(player.angle);
            var y = player.y - player.megaLaserDist*cos(player.angle);
            var l = 1000;
            var enda = 90-atan(l/radius);
			if(player.prev_angle>player.angle+180){
				player.prev_angle-=360;
			}
			else if(player.prev_angle+180<player.angle){
				player.prev_angle+=360;
			}
			var vertices = calculateMegaLaser(x,y,radius,l,enda);
            drawMegaLaser(x,y,radius,vertices);
            checkMegaLaserCollisions(x,y,vertices,radius);
        }
        
        if(!player.dead && auto_tDelay + delay * 1.5 < millis() && player.powerups.auto > millis() && auto_t.fire){
            newLaser(player.x-(auto_t.radius+auto_t.bLength)*sin(auto_t.angle),player.y-(auto_t.radius+auto_t.bLength)*cos(auto_t.angle), auto_t.angle); 
            auto_tDelay = millis();
            if(!mute){playSound(getSound("retro/hit1"));}
        }
        
        if(keyIsPressed && !player.dead){
            if(forward){
                var vx = player.xvel + 0.35*sin(player.angle + 180);
                var vy = player.yvel + 0.35*cos(player.angle + 180);
                var angle = angleFromVertical({x:0,y:0}, {x:vx,y:vy});
                angle = (angle ? angle : 0.00001);
                var mag = min(maxSpeed, sqrt(sq(vx)+sq(vy)));
                player.xvel = mag*sin(angle);
                player.yvel = mag*cos(angle);
                thrust(player, player.angle + 180, 5, 0.7);
            }
            if(left){
                rotation = round(min(100, rotation*rotAcc+1));
				player.prev_angle = player.angle;
                player.angle = mod(player.angle - (rotVel*rotation/100),360);
                var pos = {x: player.x - 7*sin(player.angle), y: player.y - 7*cos(player.angle)};
                //thrust(pos, player.angle + 90, 2, 0.7);
            }
            if(right){
                rotation = round(min(100, rotation*rotAcc+1));
				player.prev_angle = player.angle;
				
                player.angle = mod(player.angle + (rotVel*rotation/100),360);
            }
            if(shoot){
                if(player.powerups.laser > millis()){
                    player.charge = min(100, player.charge+8);
                }
                else{ 
                    if(player.powerups.zap > millis()){
                        if(zap === 0  && shot+delay*3 < millis()){
                            zap = {
                                x:player.x-10*sin(player.angle),
                                y:player.y-10*cos(player.angle),
                                dv:35,
                                angle:player.angle,
                                rotation: 20,
                                intensity:[5,15],
                                points:[8,12],
                                standardDist:100
                            };
                            shot = millis();
                        }
                    }
                    else{
                        if(shot+delay < millis())
                        {
                            var x = player.x - 10*sin(player.angle);
                            var y = player.y - 10*cos(player.angle);
                            newLaser(x,y,player.angle);
                            if(player.powerups.tri > millis()){ 
                                newLaser(x,y,player.angle+15);
                                newLaser(x,y,player.angle-15);
                            }
                            if(!mute){playSound(getSound("retro/hit1"));}
                            shot = millis();
                        }
                    
                    }
                }
            }
        }
        
        if(!shoot || player.dead || player.powerups.laser<millis()){
            player.charge = max(0, player.charge-30);   
        }
        
        if(!right && !left){
			player.prev_angle = player.angle;
            rotation = 0;   
        }
        
        if(!player.dead){
            var collision = false;
            var ast = asteroids;
            for(var i in ast){
                if(asteroids[i]!==0){
                    if(player.powerups.shield > millis()){
                        checkShieldCollision(i);
                        collision = true;
                    }
                    else if(dist(player.x, player.y, ast[i].x, ast[i].y)<15*ast[i].size){
                        checkPlayerCollision(i);
                        break;
                    }
                }
            }
            if(collision){
                asteroids = condenseMap(asteroids);   
            }
        }
        else{
            movePlayerPieces();
            drawPlayerPieces();   
        }
        
        if(despawn + 100 < millis()){
            despawn = millis();
            despawnDebris();
            debris = condenseTable(debris);
        }
        
        checkLaserCollisions();
        
        if(getTotalAsteroids()<numAsteroids*spawnSize){
            var dv = velocity;
            var start = [{x:0,y:random(0,height)},{x:width,y:random(0,height)},
                    {x:random(0,width),y:0},{x:random(0,width),y:height}];
            var index = floor(random(0, 4));
            newAsteroid(start[index].x,start[index].y,spawnSize,random(-dv,dv),random(-dv,dv));
        }
    }
    
    fill(0, 0, 0, opaq);
    stroke(0, 0, 0);
    rect(0,0,width,height);
    opaq = max(0,min(255,opaq+(reset?20:-20)));
    
    if(menu){
        drawMenu();
        despawn = millis();
        dead = millis();
        powerup = millis();
        shot = millis();
        missile = millis();
        auto_tDelay = millis();
        arch = millis();
        if(selectWait < millis()){
            if(down){
                var i = (modeButtons.selected[0]+1)%modeButtons.buttons.length;
                var b = modeButtons.buttons[i];
                modeButtons.released(b.x, b.y);
                selectWait = millis() + 100;
            }
            else if(forward){
                var i = mod(modeButtons.selected[0]-1, modeButtons.buttons.length);
                var b = modeButtons.buttons[i];
                modeButtons.released(b.x, b.y);
                selectWait = millis() + 100;
            }
        }
    }
    
    if(dev){
        drawGraph(fpsGraph, resolution,15,25, frameData);
    }
    
    fps += 1;
    frameTimeList.append(millis()-lastFrame);
    while(frameTimeList.length()>=100){
        frameTimeList.remove();
    }
    lastFrame = millis();
    
    if(invert){
        filter(INVERT);
    }
    
    if(soundUpdate-millis() > 0){
        textFont('Ariel',20);
        stroke(255, 255, 255, 255*(max(soundUpdate-millis(),0)/1000));
        fill(255, 255, 255, 255*(max(soundUpdate-millis(),0)/1000));
        text(mute?"Muted":"Unmuted",width-(mute?65:90), height-25, 100, 50);
    }
};

var keyPressed = function(){
    if(keyCode === 37){right = true;}
    if(keyCode === 39){left = true;}
    if(keyCode === UP){
        forward = true;
        player.thrust = true;
    }
    if(keyCode === 32){shoot = true;}
    if(keyCode === DOWN){down = true;}
};

var keyReleased = function(){
    if(keyCode === 37)
    {
        right = false;
    }
    if(keyCode === 39)
    {
        left = false;
    }
    if(keyCode===UP)
    {
        forward = false;
        player.thrust = false;
    }
    if(keyCode===DOWN){
        down = false;   
    }
    if(keyCode === 32)
    {
        shoot = false;
        if(menu){
           menu=false;
           disableButtons(true);
           resetGame();
        }
    }
    if(keyCode === 72 && menu){
        for(var b in secretButtons.buttons){
            secretButtons.buttons[b].disabled = !secretButtons.buttons[b].disabled;   
        }
    }
    if(keyCode === 82 || (player.dead && keyCode === 32 && dead + 2000<millis())){reset = true;}
    if(keyCode === 81){Program.restart();}
    if(keyCode === 84){trip=!trip;}
    if(keyCode === 80){
        pause = !pause;
        if(pause){noLoop();}else{loop();}
    }
    if(keyCode === 73){invert = !invert;}
    if(keyCode === 77){mute = !mute; soundUpdate = millis()+1500;}
};

var mouseMoved = function(){
    for(var g in buttonGroups){
        buttonGroups[g].hover(mouseX,mouseY);
    }
};

var mousePressed = function(){
    for(var g in buttonGroups){
        if(buttonGroups[g].inside(mouseX,mouseY)){
            buttonGroups[g].pressed(mouseX,mouseY);
        }
    }
};

var mouseReleased = function(){
    for(var g in buttonGroups){
        buttonGroups[g].released(mouseX,mouseY);
    }
};
