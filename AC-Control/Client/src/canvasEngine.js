class CanvasEngine {
    context = null;
    canvas = null;
    background = null;
    backgroundLoaded = false;
    dragging = false;
    canDrag = false;
    shapes = [];
    currentShape = null;
    lastMouse = {x: 0, y: 0};
    mouseInside = false;
    currentDrag = null;    
    editing = false;
    pointing = false;
    backgroundOriginalPosition = null;
    backgroundPosition = null;
    imageEffects = null;
    center = null;
    magnify = false;
    sample = null;
    refreshRate = 1/60;
    eventQueue = {
        queue: {},
        actionQueued: false
    };

    Init() {
        console.log("Init")
        this.canvas = document.getElementById("canvas")
        this.context = this.canvas.getContext("2d");
        this.RefreshDimensions()

        this.canvas.addEventListener("mousemove", event => {
            this.QueueEvent(event, () => {
                    var mouse = this.PageToOriginalBackgroundCoordinates(this.RotateMouse(event));
                    if(this.dragging)
                        this.DragItem(mouse);
                    this.Update(mouse);
                }
            )
        })
    
        this.canvas.addEventListener("mouseup", event => {
            this.QueueEvent(event, () => {
                    if(this.dragging) {
                        this.dragging = false;
                        this.currentDrag = null;
                    }
            
                    var mouse = this.PageToOriginalBackgroundCoordinates(this.RotateMouse(event));
                    var scale = this.BackgroundScaleRatio();
                    if(this.editing){
                        if(this.currentShape){
                            if(this.currentShape.type == 'poly'){
                                var d = this.currentShape.vertices.length > 0 ? this.Dist(mouse, this.currentShape.vertices[0]) : -1;
                                if(d <= Math.min(100 / d * scale, 10) && this.currentShape.vertices.length > 2) {
                                    this.currentShape.closed = true;
                                    this.Reset();
                                }
                                else {
                                    this.currentShape.vertices.push({'x': mouse.x, 'y': mouse.y, 'index': this.shapes[this.shapes.length - 1].vertices.length});
                                }
                            }
                            else if (this.currentShape.type == 'ellipse') {
                                this.currentShape.x = mouse.x;
                                this.currentShape.y = mouse.y;
                                this.currentShape.closed = true;
                                this.Reset();
                            }
                        }
                    }
                    else {
                        for(var shape of this.shapes) {
                            if(this.Inside(shape, mouse)){
                                shape['function']()
                                break;
                            }
                        }
                    }

                    if(this.sample){
                        this.sample(this.SampleColor(event))
                    }

                    this.Update(mouse);
                }
            )
        })
    
        this.canvas.addEventListener("mousedown", event => {
            this.QueueEvent(event, () => {
                    if(this.dragging && this.currentDrag.onDragStop)
                        this.currentDrag.onDragStop(event)
                    this.dragging = this.editing && !this.currentShape && this.canDrag;
                    var mouse = this.PageToOriginalBackgroundCoordinates(this.RotateMouse(event));
                    this.Update(mouse);
                }
            )
        });
    
        this.canvas.addEventListener("wheel", event => {
            this.QueueEvent(event, () => {
                    if(this.currentShape && this.currentShape.type === "ellipse"){
                        var delta = event.deltaY / 20;
                        this.currentShape.r1 = Math.max(this.currentShape.r1 - delta, 1);
                        this.currentShape.r2 = Math.max(this.currentShape.r2 - delta, 1);
                        var mouse = this.PageToOriginalBackgroundCoordinates(this.RotateMouse(event));
                        this.Update(mouse);
                    }
                }
            )
        });

        this.canvas.addEventListener("mouseover", event => {
            this.QueueEvent(event, () => {
                    this.mouseInside = true
                }
            )
        });

        this.canvas.addEventListener("mouseleave", event => {
            this.QueueEvent(event, () => {
                    this.mouseInside = false;
                }
            )
        })
    
        document.addEventListener("keydown", event => {
            this.QueueEvent(event, () => {
                    if(event.key === "Escape") {
                        if(this.currentShape) {
                            if(this.currentShape.remove)
                                this.currentShape.remove()
                            this.shapes.pop()
                            this.Reset();
                            this.Update();
                        }
                        if(this.sample) {
                            this.sample(null);
                        }
                    }
                    else if (event.key === "Delete" && this.currentShape) {
                        if(this.currentShape.type == "poly" && this.currentShape.vertices.length > 0){
                            this.currentShape.vertices.pop();
                            this.Update();
                        }
                    }
                    else if (event.key === "m" && this.mouseInside) {
                        this.magnify = !this.magnify;
                        this.Update();
                    }
                }
            )
        })

        window.onresize = event => {
            this.QueueEvent(event, () => this.RefreshDimensions())
        };

        setInterval(this.ProcessEventQueue, this.refreshRate * 1000, this.eventQueue)
    }

    QueueEvent(event, action) {
        this.eventQueue.queue[event.type] = {
            time: Date.now(),
            action: action
        }
        this.eventQueue.actionQueued = true
    }

    ProcessEventQueue(eventQueue) {
        if(!eventQueue.actionQueued) return;
        eventQueue.actionQueued = false;
        var actions = Object.values(eventQueue.queue);
        for(var k in eventQueue.queue)
            delete eventQueue.queue[k]
        actions.sort((x,y) => x.time - y.time);
        for(var a of actions)
            a.action()
    }

    RefreshDimensions() {
        var elem = document.getElementById("canvas-container")
        var dims = elem.getBoundingClientRect();
        this.context.canvas.width = dims.width;
        this.context.canvas.height = dims.height;
        this.center = {x: dims.width / 2, y: dims.height / 2};
        this.Update();
    }

    Update(mouse) {
        this.context.reset();
        if(!mouse) 
            mouse = this.lastMouse;

        this.context.clearRect(0, 0, this.canvas.width, this.canvas.height);
        this.canDrag = false;
        this.lastMouse = mouse;
        this.pointing = false;

        if(this.backgroundLoaded){
            this.context.translate(this.canvas.width/2, this.canvas.height/2);
            if(this.imageEffects){
                if(this.imageEffects.rotate){                    
                    this.context.rotate(this.imageEffects.rotate * Math.PI / 180);
                }
            }
            var scale = Math.min(this.canvas.width / this.background.width, this.canvas.height / this.background.height);
            var x =  - (this.background.width / 2) * scale;
            var y =  - (this.background.height / 2) * scale;
            this.context.drawImage(this.background, x, y, this.background.width * scale, this.background.height * scale);
            this.backgroundPosition = {'scale': scale, 'x': (this.canvas.width / 2) + x, 'y': (this.canvas.height / 2) + y};
            this.context.translate(-this.canvas.width/2, -this.canvas.height/2);
        }

        var canvasMouse = this.OriginalBackgroundToCanvasCoordinates(mouse);
        
        if(!this.sample) {
            for(var shape of this.shapes){
                if(shape.type === "poly" && shape.vertices.length > 0){
                    this.DrawPoly(shape, mouse);
                    if(this.editing){
                        this.DrawVerticies(shape, canvasMouse);
                        this.DrawCenter(shape, canvasMouse);
                    }
                }
                else if(shape.type === "ellipse" && shape.closed) {
                    this.DrawEllipse(shape, mouse);
                    if(this.editing){
                        this.DrawRadii(shape, canvasMouse);
                        this.DrawCenter(shape, canvasMouse);                    
                    }
                }        
            }    

            if(this.currentShape){
                if(this.currentShape.type === "poly"){
                    this.context.beginPath();
                    var verts = this.currentShape.vertices
                    if(verts.length > 0){
                        var vert = this.OriginalBackgroundToCanvasCoordinates(verts[verts.length - 1]);
                        this.context.moveTo(vert.x, vert.y);
                        this.context.lineTo(canvasMouse.x, canvasMouse.y);
                    }
                    this.context.stroke();
                }
                else if(this.currentShape.type === "ellipse") {
                    var scale = this.BackgroundScaleRatio();
                    this.context.beginPath();
                    this.context.ellipse(canvasMouse.x, canvasMouse.y, this.currentShape.r1 * scale, this.currentShape.r2 * scale, 0, 0, 2 * Math.PI)
                    this.context.stroke();
                }
            }  
        }

        if(this.magnify) {
            var m = this.Rotate(canvasMouse, this.center, this.imageEffects && this.imageEffects.rotate ? -this.imageEffects.rotate : 0)
            var magnified = this.CreateMagnifyingEffect(m, 60, 2, 1, [0,0,0,255]);
            this.context.putImageData(magnified, m.x - magnified.width / 2, m.y - magnified.height / 2);
        }

        this.canvas.style.cursor = this.pointing ? "pointer" : (this.currentShape ? "crosshair" : (this.dragging ? "grabbing" : (this.canDrag ? "grab" :  (this.magnify ? "crosshair" : "default"))));      
    }

    CreateMagnifyingEffect(point, radius, scale, borderThickness, borderColor) {
        var imageData = this.context.getImageData(point.x - radius, point.y - radius, radius * 2, radius * 2)
        var result = this.context.createImageData(imageData.width, imageData.height);
        var center = {x: result.width / 2, y: result.height / 2};
        for (var row = 0; row < result.height; row++) {
            for (var col = 0; col < result.width; col++) {
                var d = this.Dist({x: col, y: row}, center);
                var index = (row * result.width + col) * 4;
                if(d > radius - borderThickness && d <= radius) {
                    result.data.set(borderColor, index)
                }
                else if(d > radius) {
                    result.data.set(imageData.data.subarray(index, index + 4), index)
                }
                else {
                    var sourceIndex = (Math.floor(radius - (radius - row) / scale) * imageData.height + Math.floor(radius - (radius - col) / scale)) * 4;
                    var sourcePixel = imageData.data.subarray(sourceIndex, sourceIndex + 4);
                    result.data.set(sourcePixel, index)
                }
            }
        }
    
        return result;
    } 

    DrawEllipse(shape, mouse){
        this.context.strokeStyle = shape.color;
        this.context.beginPath();
        var coord = this.OriginalBackgroundToCanvasCoordinates(shape);
        var scale = this.BackgroundScaleRatio();
        this.context.ellipse(coord.x, coord.y, shape.r1 * scale, shape.r2 * scale, 0, 0, 2 * Math.PI)
        if(this.editing)
            this.context.stroke();
        this.context.fillStyle = this.editing ? (shape.highlight ? "#e8f4ff99" : "#000000") : "#fafafa99";
        if(shape.highlight) {            
            this.context.fill();
        }
        if(!this.editing && this.Inside(shape, mouse)){
            this.context.fill();
            this.pointing = true;
        }
    }

    DrawRadii(shape, mouse) {
        var r = 2;
        var p = {};
        var d = 0;
        var reach = this.magnify ? 5 : 10;
        var coord = this.OriginalBackgroundToCanvasCoordinates(shape);
        var scale = this.BackgroundScaleRatio();

        this.context.beginPath();
        p = {x: coord.x + shape.r1 * scale, y: coord.y};
        d = this.Dist(p, mouse) * scale;
        this.context.moveTo(p.x, p.y);
        this.context.ellipse(p.x, p.y, r, r, 0, 0, 2 * Math.PI)        
        if(!this.currentShape && !this.dragging && d <= reach) {
            this.canDrag = true
            this.currentDrag = {'item': shape, 'type': 'radius-h'}
        }
        this.context.stroke();
        
        this.context.beginPath();
        p = {x: coord.x, y: coord.y - shape.r2 * scale};
        d = this.Dist(p, mouse) * scale;
        this.context.moveTo(p.x, p.y);
        this.context.ellipse(p.x, p.y, r, r, 0, 0, 2 * Math.PI)
        if(!this.currentShape && !this.dragging && d <= reach) {
            this.canDrag = true
            this.currentDrag = {'item': shape, 'type': 'radius-v'}
        }  
        this.context.stroke(); 
    }

    DrawPoly(shape, mouse) {
        this.context.strokeStyle = shape.color;
        this.context.beginPath();
        var coord = this.OriginalBackgroundToCanvasCoordinates(shape.vertices[0]);
        this.context.moveTo(coord.x, coord.y);
        for(var vertex of shape.vertices) {
            coord = this.OriginalBackgroundToCanvasCoordinates(vertex);
            this.context.lineTo(coord.x, coord.y);                   
        }
        if(shape.closed) {
            coord = this.OriginalBackgroundToCanvasCoordinates(shape.vertices[0]);
            this.context.lineTo(coord.x, coord.y);
        }     
        if(this.editing)
            this.context.stroke();
        this.context.fillStyle = this.editing ? (shape.highlight ? "#e8f4ff99" : "#000000") : "#fafafa99";
        if(shape.highlight) {            
            this.context.fill();
        }
        if(!this.editing && this.Inside(shape, mouse)){
            this.context.fill();
            this.pointing = true;
        }
    }

    DrawVerticies(shape, mouse) {
        var reach = this.magnify ? 5 : 10;
        for(var vertex of shape.vertices) {
            this.context.beginPath();
            var coord = this.OriginalBackgroundToCanvasCoordinates(vertex);
            var scale = this.BackgroundScaleRatio();
            this.context.moveTo(coord.x, coord.y);
            var d = this.Dist(coord, mouse) * scale;
            var r = shape === this.currentShape && vertex.index == 0 && shape.vertices.length > 2  ? Math.max(Math.min(100 / d, 10), 2) : 2;
            this.context.ellipse(coord.x, coord.y, r, r, 0, 0, 2 * Math.PI)
            if(!this.currentShape && !this.dragging && d <= reach) {
                this.canDrag = true
                this.currentDrag = {'item': vertex, 'type': 'vertex'}
            }
            this.context.stroke();
        }
    }

    DrawCenter(shape, mouse) {
        if(shape.closed){
            var reach = this.magnify ? 5 : 15;
            var expand = this.magnify ? 20 : 100;
            var expandRadius = this.magnify ? 3 : 5;
            this.context.beginPath();
            if(shape.type == 'poly') {
                var center = this.Center(shape);
                var coord = this.OriginalBackgroundToCanvasCoordinates(center);
                var scale = this.BackgroundScaleRatio();
                this.context.moveTo(coord.x, coord.y);
                var d = this.Dist(coord, mouse) * scale;
                var r = !this.currentShape && !(this.dragging && this.currentDrag.item !== shape) ? Math.max(Math.min(expand / d, expandRadius), 2) : 2;
                this.context.ellipse(coord.x, coord.y, r, r, 0, 0, 2 * Math.PI)
                if(!this.currentShape && !this.dragging && d <= reach) {
                    this.canDrag = true
                    this.currentDrag = {'item': shape, 'type': 'poly'}
                }            
            }
            else if (shape.type == 'ellipse') {
                var coord = this.OriginalBackgroundToCanvasCoordinates(shape);
                var scale = this.BackgroundScaleRatio();
                this.context.moveTo(coord.x, coord.y);
                var d = this.Dist(coord, mouse) * scale;
                var r = !this.currentShape && !(this.dragging && this.currentDrag.item === shape && this.currentDrag.type !== 'ellipse') ? Math.max(Math.min(expand / d, expandRadius), 2) : 2;
                this.context.ellipse(coord.x, coord.y, r, r, 0, 0, 2 * Math.PI)
                if(!this.currentShape && !this.dragging && d <= reach) {
                    this.canDrag = true
                    this.currentDrag = {'item': shape, 'type': 'ellipse'}
                }
            }
            this.context.fill()
        }
    }

    Dist(p1, p2){
        return Math.sqrt(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2));
    }

    DragItem(mouse) {
        if(!this.currentDrag) return;
        var delta = {x: mouse.x - this.lastMouse.x, y: mouse.y - this.lastMouse.y};

        if(this.currentDrag.type == 'vertex') {
            this.currentDrag.item.x = mouse.x;
            this.currentDrag.item.y = mouse.y;
        }
        else if(this.currentDrag.type == 'poly') {        
            for(var vertex of this.currentDrag.item.vertices){
                vertex.x += delta.x;
                vertex.y += delta.y;
            }
        }
        else if(this.currentDrag.type == 'ellipse') {
            this.currentDrag.item.x = mouse.x;
            this.currentDrag.item.y = mouse.y;
        }
        else if(this.currentDrag.type == 'radius-h') {
            this.currentDrag.item.r1 = Math.max(this.currentDrag.item.r1 + delta.x, 1);
        }
        else if(this.currentDrag.type == 'radius-v') {
            this.currentDrag.item.r2 = Math.max(this.currentDrag.item.r2 - delta.y, 1);
        }
    }

    PageToOriginalBackgroundCoordinates(coordinates) {
        if(!(this.backgroundPosition && this.backgroundPosition.x != null && this.backgroundPosition.y != null)) return coordinates;
        var scale = this.BackgroundScaleRatio();
        var pos = this.canvas.getBoundingClientRect();
        return {
            x: ((coordinates.x - pos.x) - this.backgroundPosition.x) / scale, 
            y: ((coordinates.y - pos.y) - this.backgroundPosition.y) / scale
        };
    }

    OriginalBackgroundToCanvasCoordinates(coordinates) {
        if(!(this.backgroundPosition && this.backgroundPosition.x != null && this.backgroundPosition.y != null)) return coordinates;
        var scale = this.BackgroundScaleRatio();
        return {x: this.backgroundPosition.x + coordinates.x * scale, y: this.backgroundPosition.y + coordinates.y * scale};
    }

    BackgroundScaleRatio() {
        if(!((this.backgroundPosition && this.backgroundPosition.x != null && this.backgroundPosition.y != null) && 
            (this.backgroundOriginalPosition &&this.backgroundOriginalPosition.x != null && this.backgroundOriginalPosition.y != null))) return 1;
        return this.backgroundPosition.scale / this.backgroundOriginalPosition.scale;
    }

    Reset() {
        this.currentShape = null;
    }

    Center(shape){
        if(shape.type == "poly"){
            var xsum = 0;
            var ysum = 0;
            var area = 0;

            for(var i = 0; i < shape.vertices.length; i++){
                var p0 = shape.vertices[i];
                var p1 = shape.vertices[(i+1)%shape.vertices.length];
                
                var areasum = p0.x*p1.y - p1.x*p0.y;
                xsum += (p0.x + p1.x) * areasum;
                ysum += (p0.y + p1.y) * areasum;
                area += areasum;
            }

            return {x: xsum / (area * 3), y: ysum / (area * 3)};
        }
        else if(shape.type == "ellipse") {
            return {x: shape.x, y: shape.y};
        }
    }

    Inside(shape, mouse) {
        if(shape.type === "poly"){
            var count = 0;
            var b_p1 = mouse;
            var b_p2 = {x: 0, y: mouse.y+Math.random()};
            for(var a = 0; a < shape.vertices.length; a++){
                var a_p1 = shape.vertices[a];
                var a_p2 = shape.vertices[(a+1) % shape.vertices.length];
                if(this.Intersection(a_p1, a_p2, b_p1, b_p2)) {
                    count++;    
                }
            }
            return count % 2 == 1;
        }
        else if(shape.type === "ellipse"){
            return Math.pow((mouse.x - shape.x)/shape.r1, 2) + Math.pow((mouse.y - shape.y)/shape.r2, 2) - 1 < 0;
        }
    }

    Intersection(p1, p2, p3, p4) {
        if ((p1.x === p2.x && p1.y === p2.y) || (p3.x === p4.x && p3.y === p4.y)) {
            return false
        }

        var denom = (p4.y - p3.y)*(p2.x - p1.x) - (p4.x - p3.x)*(p2.y - p1.y);
        if (denom == 0) {
            return false;
        }
        var ua = ((p4.x - p3.x)*(p1.y - p3.y) - (p4.y - p3.y)*(p1.x - p3.x))/denom;
        var ub = ((p2.x - p1.x)*(p1.y - p3.y) - (p2.y - p1.y)*(p1.x - p3.x))/denom;

        if (ua < 0 || ua > 1 || ub < 0 || ub > 1) {
            return false;
        }

        return {
            x: p1.x + ua * (p2.x - p1.x),
            y: p1.y + ua * (p2.y - p1.y)
        };
    }

    Rotate(point, center, angle) {
        var r = this.Dist(point, center);
        var a = Math.atan2((center.x - point.x), (center.y - point.y)) + angle * Math.PI / 180;
        var p = {x: center.x - Math.sin(a) * r, y: center.y - Math.cos(a) * r};
        return p;
    }

    RotateMouse(mouse) {
        return this.Rotate(mouse, this.center, this.imageEffects && this.imageEffects.rotate ? this.imageEffects.rotate : 0)
    }

    SetEdit(state) {
        this.editing = state;
        this.shapes.forEach(x => x.highlight = false)
        this.Update()
    }

    UserSampleColor() {
        var promise = new Promise(resolve => {
            this.sample = (value) => {
                this.magnify = false;
                this.sample = null;
                this.Update();
                resolve(value);
            }
        })
        this.magnify = true;
        this.Update();
        return promise;
    }

    SampleBackgroundColor(point) {
        var context = this.canvas.getContext("2d");
        var elem = document.getElementById("canvas-container")
        var dims = elem.getBoundingClientRect();
        context.canvas.width = dims.width;
        context.canvas.height = dims.height;
        if(this.backgroundLoaded){
            context.translate(context.canvas.width/2, context.canvas.height/2);
            if(this.imageEffects && this.imageEffects.rotate){
                context.rotate(this.imageEffects.rotate * Math.PI / 180);
            }
            var scale = Math.min(context.canvas.width / this.background.width, context.canvas.height / this.background.height);
            var x =  - (this.background.width / 2) * scale;
            var y =  - (this.background.height / 2) * scale;
            context.drawImage(this.background, x, y, this.background.width * scale, this.background.height * scale);
            this.backgroundPosition = {'scale': scale, 'x': (context.canvas.width / 2) + x, 'y': (context.canvas.height / 2) + y};
            context.translate(-context.canvas.width/2, -context.canvas.height/2);
        }
        return "#"+Array.from(context.getImageData(point.x, point.y, 1, 1).data).map(x => x.toString(16).padStart(2,'0')).join('');
    }

    SampleColor(point) {
        return "#"+Array.from(this.context.getImageData(point.x, point.y, 1, 1).data).map(x => x.toString(16).padStart(2,'0')).join('');
    }

    StartBackgroundLoad() {
        this.backgroundLoaded = false;
        document.getElementById('spinner').style.display = "block";
        this.background = new Image();
        this.Update()
    }

    async LoadBackground(file, position) {
        this.backgroundLoaded = false;
        document.getElementById('spinner').style.display = "block";
        this.background = new Image();
        if(Object.prototype.toString.call(file) === "[object File]"){
            const reader = new FileReader()
            var dataUrl = await new Promise(resolve => {
                reader.onload = ev => {
                    resolve(ev.target.result)
                }
                reader.readAsDataURL(file)
            })
            this.background.src = dataUrl
        }
        else if(Object.prototype.toString.call(file) === "[object String]") {
            this.background.src = file
            this.background.crossOrigin = "Anonymous"
        }   
        else {
            this.backgroundLoaded = true;
            document.getElementById('spinner').style.display = "none";
            return;
        }
        
        await new Promise(resolve => { this.background.onload = resolve });     

        if(position){
            this.backgroundOriginalPosition = position
        }
        else{
            var scale = Math.min(this.canvas.width / this.background.width, this.canvas.height / this.background.height);
            var x = (this.canvas.width / 2) - (this.background.width / 2) * scale;
            var y = (this.canvas.height / 2) - (this.background.height / 2) * scale;
            this.backgroundOriginalPosition = {'scale': scale, 'x': x, 'y': y};
        }        

        this.backgroundLoaded = true;
        document.getElementById('spinner').style.display = "none";
        this.Update();
        return this.backgroundPosition;
    }

    RemoveShape(shape) {
        var index = this.shapes.indexOf(shape);
        if (index > -1) {
            this.shapes.splice(index, 1);
            this.Update();
        }
    }
}

export default CanvasEngine