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

    Init() {
        console.log("Init")
        this.canvas = document.getElementById("canvas")
        this.context = this.canvas.getContext("2d");
        this.context.canvas.width = document.body.offsetWidth / 2;
        this.context.canvas.height = document.body.offsetHeight;

        this.canvas.addEventListener("mousemove", event => {
            var mouse = this.ToCanvasCoordinates(event);
            if(this.dragging)
                this.DragItem(mouse);
            this.Update(mouse);
        })
    
        this.canvas.addEventListener("mouseup", event => {
            if(this.dragging) {
                this.dragging = false;
                this.currentDrag = null;
            }
    
            var coord = this.ToCanvasCoordinates(event);
            if(this.editing && this.currentShape){
                if(this.currentShape.type == 'poly'){
                    var d = this.currentShape.vertices.length > 0 ? this.Dist(coord, this.currentShape.vertices[0]) : -1;
                    if(d <= Math.min(100 / d, 10) && this.currentShape.vertices.length > 2) {
                        this.currentShape.closed = true;
                        this.Reset();
                    }
                    else {
                        this.currentShape.vertices.push({'x': coord.x, 'y': coord.y, 'index': this.shapes[this.shapes.length - 1].vertices.length});
                    }
                }
                else if (this.currentShape.type == 'ellipse') {
                    this.currentShape.x = coord.x;
                    this.currentShape.y = coord.y;
                    this.currentShape.closed = true;
                    this.Reset();
                }
            }
            this.Update(coord);
        })
    
        this.canvas.addEventListener("mousedown", (event) => {
            this.dragging = this.editing && !this.currentShape && this.canDrag;
            this.Update(this.ToCanvasCoordinates(event));
        });
    
        this.canvas.addEventListener("wheel", (event) => {
            if(this.currentShape && this.currentShape.type === "ellipse"){
                var delta = event.deltaY / 10;
                this.currentShape.r1 = Math.max(this.currentShape.r1 - delta, 1);
                this.currentShape.r2 = Math.max(this.currentShape.r2 - delta, 1);
                this.Update(this.ToCanvasCoordinates(event));
            }
        });
    
        document.addEventListener("keydown", event => {
            if(event.key === "Escape" && this.currentShape) {
                this.shapes.pop()
                this.Reset();
                this.Update();
            }
            else if (event.key === "Delete" && this.currentShape) {
                if(this.currentShape.vertices.length > 0){
                    this.currentShape.vertices.pop();
                    this.Update(this.lastMouse);
                }
            }
        })
    }

    Update(mouse) {
        if(!mouse) 
            mouse = this.lastMouse;

        this.context.clearRect(0, 0, this.canvas.width, this.canvas.height);
        this.canDrag = false;
        this.lastMouse = mouse;
        this.pointing = false;

        if(this.backgroundLoaded){
            var scale = Math.min(this.canvas.width / this.background.width, this.canvas.height / this.background.height);
            var x = (this.canvas.width / 2) - (this.background.width / 2) * scale;
            var y = (this.canvas.height / 2) - (this.background.height / 2) * scale;
            this.context.drawImage(this.background, x, y, this.background.width * scale, this.background.height * scale);
        }
        
        for(var shape of this.shapes){
            if(shape.type === "poly" && shape.vertices.length > 0){
                this.DrawPoly(shape, mouse);
                if(this.editing){
                    this.DrawVerticies(shape, mouse);
                    this.DrawCenter(shape, mouse);
                }
            }
            else if(shape.type === "ellipse" && shape.closed) {
                this.DrawEllipse(shape, mouse);
                if(this.editing){
                    this.DrawCenter(shape, mouse);
                    this.DrawRadii(shape, mouse);
                }
            }        
        }    

        if(this.currentShape){
            if(this.currentShape.type === "poly"){
                this.context.beginPath();
                var verts = this.currentShape.vertices
                if(verts.length > 0){
                    this.context.moveTo(verts[verts.length - 1].x, verts[verts.length - 1].y);
                    this.context.lineTo(mouse.x, mouse.y);
                }
                this.context.stroke();
            }
            else if(this.currentShape.type === "ellipse") {
                this.context.beginPath();
                this.context.ellipse(mouse.x, mouse.y, this.currentShape.r1, this.currentShape.r2, 0, 0, 2 * Math.PI)
                this.context.stroke();
            }
        }  

        this.canvas.style.cursor = this.pointing ? "pointer" : (this.currentShape ? "crosshair" : (this.dragging ? "grabbing" : (this.canDrag ? "grab" : "default")));      
    }

    DrawEllipse(shape, mouse){
        this.context.strokeStyle = shape.color;
        this.context.beginPath();
        this.context.ellipse(shape.x, shape.y, shape.r1, shape.r2, 0, 0, 2 * Math.PI)
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
        
        this.context.beginPath();
        p = {x: shape.x + shape.r1, y: shape.y};
        d = this.Dist(p, mouse);
        this.context.moveTo(p.x, p.y);
        this.context.ellipse(p.x, p.y, r, r, 0, 0, 2 * Math.PI)
        if(!this.currentShape && !this.dragging && d <= 15) {
            this.canDrag = true
            this.currentDrag = {'item': shape, 'type': 'radius-h'}
        }
        this.context.stroke();
        
        this.context.beginPath();
        p = {x: shape.x, y: shape.y - shape.r2};
        d = this.Dist(p, mouse);
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
        this.context.moveTo(shape.vertices[0].x, shape.vertices[0].y);
        for(var vertex of shape.vertices) {
            this.context.lineTo(vertex.x, vertex.y);                   
        }
        if(shape.closed) {
            this.context.lineTo(shape.vertices[0].x, shape.vertices[0].y);
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
            this.context.moveTo(vertex.x, vertex.y);
            var d = this.Dist(vertex, mouse);
            var r = shape === this.currentShape && vertex.index == 0 && shape.vertices.length > 2  ? Math.max(Math.min(100 / d, 10), 2) : 2;
            this.context.ellipse(vertex.x, vertex.y, r, r, 0, 0, 2 * Math.PI)
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
                var c = this.Center(shape);
                this.context.moveTo(c.x, c.y);
                var d = this.Dist(c, mouse);
                var r = !this.currentShape && !(this.dragging && this.currentDrag.item !== shape) ? Math.max(Math.min(100 / d, 5), 2) : 2;
                this.context.ellipse(c.x, c.y, r, r, 0, 0, 2 * Math.PI)
                if(!this.currentShape && !this.dragging && d <= 15) {
                    this.canDrag = true
                    this.currentDrag = {'item': shape, 'type': 'poly'}
                }            
            }
            else if (shape.type == 'ellipse') {
                this.context.moveTo(shape.x, shape.y);
                var d = this.Dist(shape, mouse);
                var r = !this.currentShape && !(this.dragging && this.currentDrag.item === shape && this.currentDrag.type !== 'ellipse') ? Math.max(Math.min(100 / d, 5), 2) : 2;
                this.context.ellipse(shape.x, shape.y, r, r, 0, 0, 2 * Math.PI)
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
            this.currentDrag.item.x += delta.x;
            this.currentDrag.item.y += delta.y;
        }
        else if(this.currentDrag.type == 'radius-h') {
            this.currentDrag.item.r1 = Math.max(this.currentDrag.item.r1 + delta.x, 1);
        }
        else if(this.currentDrag.type == 'radius-v') {
            this.currentDrag.item.r2 = Math.max(this.currentDrag.item.r2 - delta.y, 1);
        }
    }

    ToCanvasCoordinates(coordinates) {
        return {x: coordinates.x - (this.canvas.offsetLeft + this.canvas.clientLeft), y: coordinates.y - (this.canvas.offsetTop + this.canvas.clientTop)};
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

    async LoadBackground(file) {
        this.backgroundLoaded = false;
        document.getElementById('spinner').style.display = "block";
        this.background = new Image();
        console.log(Object.prototype.toString.call(file))
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
            this.background.src = `http://${window.location.hostname}:3001/background/${file}`
        }        
        else {
            this.backgroundLoaded = true;
            document.getElementById('spinner').style.display = "none";
            this.Update();
        }
        
        this.background.onload = () => { 
            this.backgroundLoaded = true;
            document.getElementById('spinner').style.display = "none";
            this.Update();
        }
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