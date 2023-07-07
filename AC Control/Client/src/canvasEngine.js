

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
    currentDrag = null;    
    editing = false;
    pointing = false;
    backgroundOriginalPosition = null;
    backgroundPosition = null;
    imageEffects = null;

    Init() {
        console.log("Init")
        this.canvas = document.getElementById("canvas")
        this.context = this.canvas.getContext("2d");
        this.RefreshDimensions()

        this.canvas.addEventListener("mousemove", event => {
            var mouse = this.PageToOriginalBackgroundCoordinates(event);
            if(this.dragging)
                this.DragItem(mouse);
            this.Update(mouse);
        })
    
        this.canvas.addEventListener("mouseup", event => {
            if(this.dragging) {
                this.dragging = false;
                this.currentDrag = null;
            }
    
            var mouse = this.PageToOriginalBackgroundCoordinates(event);
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
            this.Update(mouse);
        })
    
        this.canvas.addEventListener("mousedown", (event) => {
            this.dragging = this.editing && !this.currentShape && this.canDrag;
            this.Update(this.PageToOriginalBackgroundCoordinates(event));
        });
    
        this.canvas.addEventListener("wheel", (event) => {
            if(this.currentShape && this.currentShape.type === "ellipse"){
                var delta = event.deltaY / 20;
                this.currentShape.r1 = Math.max(this.currentShape.r1 - delta, 1);
                this.currentShape.r2 = Math.max(this.currentShape.r2 - delta, 1);
                this.Update(this.PageToOriginalBackgroundCoordinates(event));
            }
        });
    
        document.addEventListener("keydown", event => {
            if(event.key === "Escape" && this.currentShape) {
                this.shapes.pop()
                this.Reset();
                this.Update();
            }
            else if (event.key === "Delete" && this.currentShape) {
                if(this.currentShape.type == "poly" && this.currentShape.vertices.length > 0){
                    this.currentShape.vertices.pop();
                    this.Update();
                }
            }
        })

        window.onresize = () => {
            this.RefreshDimensions()
        };
    }

    RefreshDimensions() {
        var elem = document.getElementById("canvas-container")
        var dims = elem.getBoundingClientRect();
        this.context.canvas.width = dims.width;
        this.context.canvas.height = dims.height;
        this.Update();
    }

    Update(mouse) {
        if(!mouse) 
            mouse = this.lastMouse;

        this.context.clearRect(0, 0, this.canvas.width, this.canvas.height);
        this.canDrag = false;
        this.lastMouse = mouse;
        this.pointing = false;

        if(this.backgroundLoaded){
            this.context.save()
            this.context.translate(this.canvas.width/2, this.canvas.height/2);
            if(this.imageEffects){
                if(this.imageEffects.rotate){                    
                    this.context.rotate(this.imageEffects.rotate * Math.PI / 180);
                }
            }
            var scale = Math.min(this.canvas.width / this.background.width, this.canvas.height / this.background.height);
            //var x = (this.canvas.width / 2) - (this.background.width / 2) * scale;
            //var y = (this.canvas.height / 2) - (this.background.height / 2) * scale;
            var x =  - (this.background.width / 2) * scale;
            var y =  - (this.background.height / 2) * scale;
            this.context.drawImage(this.background, x, y, this.background.width * scale, this.background.height * scale);
            this.backgroundPosition = {'scale': scale, 'x': (this.canvas.width / 2) + x, 'y': (this.canvas.height / 2) + y};
            this.context.restore();
        }

        var canvasMouse = this.OriginalBackgroundToCanvasCoordinates(mouse);
        
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

        this.canvas.style.cursor = this.pointing ? "pointer" : (this.currentShape ? "crosshair" : (this.dragging ? "grabbing" : (this.canDrag ? "grab" : "default")));      
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
        var coord = this.OriginalBackgroundToCanvasCoordinates(shape);
        var scale = this.BackgroundScaleRatio();

        this.context.beginPath();
        p = {x: coord.x + shape.r1 * scale, y: coord.y};
        d = this.Dist(p, mouse) * scale;
        this.context.moveTo(p.x, p.y);
        this.context.ellipse(p.x, p.y, r, r, 0, 0, 2 * Math.PI)
        if(!this.currentShape && !this.dragging && d <= 15) {
            this.canDrag = true
            this.currentDrag = {'item': shape, 'type': 'radius-h'}
        }
        this.context.stroke();
        
        this.context.beginPath();
        p = {x: coord.x, y: coord.y - shape.r2 * scale};
        d = this.Dist(p, mouse) * scale;
        this.context.moveTo(p.x, p.y);
        this.context.ellipse(p.x, p.y, r, r, 0, 0, 2 * Math.PI)
        if(!this.currentShape && !this.dragging && d <= 15) {
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
        for(var vertex of shape.vertices) {
            this.context.beginPath();
            var coord = this.OriginalBackgroundToCanvasCoordinates(vertex);
            var scale = this.BackgroundScaleRatio();
            this.context.moveTo(coord.x, coord.y);
            var d = this.Dist(coord, mouse) * scale;
            var r = shape === this.currentShape && vertex.index == 0 && shape.vertices.length > 2  ? Math.max(Math.min(100 / d, 10), 2) : 2;
            this.context.ellipse(coord.x, coord.y, r, r, 0, 0, 2 * Math.PI)
            if(!this.currentShape && !this.dragging && d <= 15) {
                this.canDrag = true
                this.currentDrag = {'item': vertex, 'type': 'vertex'}
            }
            this.context.stroke();
        }
    }

    DrawCenter(shape, mouse) {
        if(shape.closed){
            this.context.beginPath();
            if(shape.type == 'poly') {
                var center = this.Center(shape);
                var coord = this.OriginalBackgroundToCanvasCoordinates(center);
                var scale = this.BackgroundScaleRatio();
                this.context.moveTo(coord.x, coord.y);
                var d = this.Dist(coord, mouse) * scale;
                var r = !this.currentShape && !(this.dragging && this.currentDrag.item !== shape) ? Math.max(Math.min(100 / d, 5), 2) : 2;
                this.context.ellipse(coord.x, coord.y, r, r, 0, 0, 2 * Math.PI)
                if(!this.currentShape && !this.dragging && d <= 15) {
                    this.canDrag = true
                    this.currentDrag = {'item': shape, 'type': 'poly'}
                }            
            }
            else if (shape.type == 'ellipse') {
                var coord = this.OriginalBackgroundToCanvasCoordinates(shape);
                var scale = this.BackgroundScaleRatio();
                this.context.moveTo(coord.x, coord.y);
                var d = this.Dist(coord, mouse) * scale;
                var r = !this.currentShape && !(this.dragging && this.currentDrag.item === shape && this.currentDrag.type !== 'ellipse') ? Math.max(Math.min(100 / d, 5), 2) : 2;
                this.context.ellipse(coord.x, coord.y, r, r, 0, 0, 2 * Math.PI)
                if(!this.currentShape && !this.dragging && d <= 15) {
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
        if(!this.backgroundPosition) return coordinates;
        var scale = this.BackgroundScaleRatio();
        var pos = this.canvas.getBoundingClientRect();
        return {
            x: ((coordinates.x - pos.x) - this.backgroundPosition.x) / scale, 
            y: ((coordinates.y - pos.y) - this.backgroundPosition.y) / scale
        };
    }

    OriginalBackgroundToCanvasCoordinates(coordinates) {
        if(!this.backgroundOriginalPosition) return coordinates;
        var scale = this.BackgroundScaleRatio();
        return {x: this.backgroundPosition.x + coordinates.x * scale, y: this.backgroundPosition.y + coordinates.y * scale};
    }

    BackgroundScaleRatio() {
        if(!(this.backgroundPosition && this.backgroundOriginalPosition)) return 1;
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

    SetEdit(state) {
        this.editing = state;
        this.shapes.forEach(x => x.highlight = false)
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
        }   
        else {
            this.backgroundLoaded = true;
            document.getElementById('spinner').style.display = "none";
            return;
        }
        
        await new Promise(resolve => {
            this.background.onload = () => { 
                console.log("load")
                resolve();
            }
        });        

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