import { endpointToCenterParameterization, a2c, transformPattern, pathPattern, numberPattern } from './Utility.js'

class SVGEngine {
    constructor() {
        this.transformStack = []
        this.currentTransform = { scale: { x: 1, y: 1 }, rotation: 0 }
        this.size
    }

    processNode(node) {    
        this.transformStack.push({ scale: { x: this.currentTransform.scale.x, y: this.currentTransform.scale.y}, rotation: this.currentTransform.rotation })
        this.saveState()
        try {
            if(!this.applyDefaultAttributes(node))
                return

            for(let child of node.children) {
                this.processNode(child)
            }
            
            switch(node.nodeName) {               
                case "circle":
                    this.processCircle(node)
                    break;
                case "ellipse":
                    this.processEllipse(node)
                    break;
                case "line":
                    this.processLine(node)
                    break;
                case "path":
                    this.processPath(node)
                    break;
                case "polygon":
                    this.processPolygon(node)
                    break;
                case "polyline":
                    this.processPolyline(node)
                    break;
                case "rect":
                    this.processRect(node)
                    break;
                case "text":
                    break;
                case "textPath":
                    break;
            }            
        }
        catch(ex) {
            console.log(ex)
        }        
        this.currentTransform = this.transformStack.pop()
        this.restoreState()
    }
    
    // translate(x,y) {}
    // scale(x,y) {}
    // rotate(deg) {}
    // setTransform(a, b, c, d, e, f) {}
    // ellipse(x, y, radiusX, radiusY, rotation, startAngle, endAngle) {}
    // bezierCurveTo(cp1x, cp1y, cp2x, cp2y, x, y) {}
    // lineTo(x, y) {}
    // moveTo(x, y) {}
    // rect(x, y, width, height) {}
    // saveState() {}
    // restoreState() {}
    // viewBoxUpdated(w, h) {}

    applyDefaultAttributes(node) {
        if(!node.attributes) return true

        for(let attribute of ['transform-origin', 'transform', 'display', 'viewBox']) {
            let params
            let attributeValue = node.attributes[attribute] ? node.attributes[attribute].value : null
            if(attributeValue) {                
                switch(attribute) {
                    case "transform-origin":
                        params = this.cleanParams(attributeValue)
                        this.translate(params[0], params[1])
                        break;
                    case "transform":                
                        for(let match of attributeValue.matchAll(transformPattern)) {
                            params = this.cleanParams(match.groups['params'])
                            switch(match.groups['action']) {
                                case "translate":
                                    this.translate(params[0], params[1])
                                    break;
                                case "translateX":
                                    this.translate(params[0], 0)
                                    break;
                                case "translateY":
                                    this.translate(0, params[0])
                                    break;
                                case "scale":
                                    this.currentTransform.scale.x *= params[0]
                                    this.currentTransform.scale.y *= params[1]
                                    this.scale(this.currentTransform.scale.x, this.currentTransform.scale.y)
                                    break;
                                case "scaleX":
                                    this.currentTransform.scale.x *= params[0]
                                    this.scale(this.currentTransform.scale.x, this.currentTransform.scale.y)
                                    break;
                                case "scaleY":
                                    this.currentTransform.scale.y *= params[0]
                                    this.scale(this.currentTransform.scale.x, this.currentTransform.scale.y)
                                    break;
                                case "rotate":
                                    this.currentTransform.rotation += params[0]
                                    this.rotate(this.currentTransform.rotation)
                                    break;
                                case "matrix":
                                    this.setTransform(params[0], params[1], params[2], params[3], params[4], params[5])
                                    break;
                            }
                        }
                        break;
                    case "display":
                        if(attributeValue == "none")
                            return false;
                        break;
                    case "viewBox":
                        const padding = 0.95                        
                        params = this.cleanParams(attributeValue, true)

                        const w = (params[2] - params[0])
                        const h = (params[3] - params[1])
                        const aspect = w / h
                        this.currentTransform.scale.x = (h > w ? this.size.h * aspect : this.size.w) / w * padding
                        this.currentTransform.scale.y = (w > h ? this.size.w / aspect : this.size.h) / h * padding
                        this.translate(Math.round((this.size.w - (w * this.currentTransform.scale.x))  / 2), Math.round((this.size.h - (h * this.currentTransform.scale.y)) / 2))
                        this.scale(this.currentTransform.scale.x, this.currentTransform.scale.y)   
                        this.viewBoxUpdated(w,h)                                    
                        break;
                }
            }
        }

        return true
    }   

    processCircle(node) {
        this.ellipse(getAttribute(node, 'cx', 0), getAttribute(node, 'cy', 0), getAttribute(node, 'r'), getAttribute(node, 'r'), 0, 0, 2 * Math.PI)
    }

    processEllipse(node) {
        this.ellipse(getAttribute(node, 'cx', 0), getAttribute(node, 'cy', 0), getAttribute(node, 'rx'), getAttribute(node, 'ry'), 0, 0, 2 * Math.PI)
    }

    processLine(node) {
        this.moveTo(getAttribute(node, 'x1'), getAttribute(node, 'y1'))
        this.lineTo(getAttribute(node, 'x2'), getAttribute(node, 'y2'))
    }

    processPolygon(node) {
        var points = getAttribute(node, 'points').split(" ").map(x => { let point = x.split(","); return { x: point[0], y: point[1] }})
        for(let i = 0; i < points.length; i++) {
            let p1 = points[i]
            let p2 = points[(i + 1) % points.length]
            this.moveTo(p1.x, p1.y)
            this.lineTo(p2.x, p2.y)
        }
    }

    processPolyline(node) {
        var points = getAttribute(node, 'points').split(" ").map(x => { let point = x.split(","); return { x: point[0], y: point[1] }})
        for(let i = 0; i < points.length - 1; i++) {
            let p1 = points[i]
            let p2 = points[(i + 1) % points.length]
            this.moveTo(p1.x, p1.y)
            this.lineTo(p2.x, p2.y)
        }
    }

    processRect(node) {
        let x = getAttribute(node, 'x', 0)
        let y = getAttribute(node, 'y', 0)
        let w = getAttribute(node, 'width')
        let h = getAttribute(node, 'height')
        this.rect(x, y, w, h)
    }

    processPath(node) {
        let params, control, lastControl, type, d, initialPoint, pos = { x: 0, y: 0 }, count = 0, totalCount = 0
        for(let match of this.getAttribute(node, 'd').matchAll(pathPattern)) {
            if(match.groups['params'])
                params = [...match.groups['params'].matchAll(numberPattern)].map(x => parseFloat(x[0]))
            type = match.groups['type'].toUpperCase()

            let absolute = type == match.groups['type']
            switch(type) {
                case "M":
                    for(let i = 0; i < params.length; i += 2) {
                        if(!absolute && (totalCount + i) !== 0)
                            convertToAbsoluteCoordinates(params, i, i + 2, pos)   
                            
                        if(i == 0) {
                            this.moveTo(params[i], params[i+1])                            
                            if (count == 0) {
                                initialPoint = { x: params[i], y: params[i+1] }      
                            }
                        }             
                        else {
                            this.lineTo(params[i], params[i+1])
                        }
                        pos = { x: params[i], y: params[i+1] }
                    }                         
                    lastControl = null               
                    break;
                case "L":
                    for(let i = 0; i < params.length; i += 2) {
                        if(!absolute)
                            convertToAbsoluteCoordinates(params, i, i + 2, pos)  

                        this.lineTo(params[i], params[i+1])
                        pos = { x: params[i], y: params[i+1] }
                    }
                    lastControl = null
                    break;
                case "H":
                    for(let i = 0; i < params.length; i++) {
                        d = absolute ? params[i] : pos.x + params[i]
                        this.lineTo(d, pos.y)
                        pos = { x: d, y: pos.y }
                    }     
                    lastControl = null               
                    break;
                case "V":
                    for(let i = 0; i < params.length; i++) {
                        d = absolute ? params[i] : pos.y + params[i]
                        this.lineTo(pos.x, d)
                        pos = { x: pos.x, y: d }
                    } 
                    lastControl = null
                    break;
                case "C":
                    for(let i = 0; i < params.length; i+=6) {
                        if(!absolute)
                            convertToAbsoluteCoordinates(params, i, i + 6, pos)  

                        this.bezierCurveTo(params[i], params[i+1], params[i+2], params[i+3], params[i+4], params[i+5])
                        pos = { x: params[i+4], y: params[i+5] }
                        lastControl = { x: params[i+2], y: params[i+3] }
                    }
                    break;
                case "S":
                    for(let i = 0; i < params.length; i+=4) {
                        if(!absolute)
                            convertToAbsoluteCoordinates(params, i, i + 4, pos)  

                        control = getReflectedControlPoint(pos, lastControl)                    
                        this.bezierCurveTo(control.x, control.y, params[i], params[i+1], params[i+2], params[i+3])
                        pos = { x: params[i+2], y: params[i+3] }
                        lastControl = { x: params[i], y: params[i+1] }
                    }                    
                    break;  
                case "Q":
                    for(let i = 0; i < params.length; i+=4) {
                        if(!absolute)
                            convertToAbsoluteCoordinates(params, i, i + 4, pos)  

                        this.bezierCurveTo(params[i], params[i+1], params[i], params[i+1], params[i+2], params[i+3])
                        pos = { x: params[i+2], y: params[i+3] }
                        lastControl = { x: params[i], y: params[i+1] }
                    }                      
                    break;
                case "T":
                    for(let i = 0; i < params.length; i+=2) {
                        if(!absolute)
                            convertToAbsoluteCoordinates(params, i, i + 2, pos)  

                        control = getReflectedControlPoint(pos, lastControl)
                        this.bezierCurveTo(control.x, control.y, control.x, control.y, params[i], params[i+1])
                        pos = { x: params[i], y: params[i+1] }
                        lastControl = { x: control.x, y: control.y }
                    }    
                    break;   
                case "A":                    
                    for(let i = 0; i < params.length; i+=7) {
                        if(!absolute)
                            convertToAbsoluteCoordinates(params, i+5, i+6, pos)

                        //var arc = endpointToCenterParameterization(pos.x, pos.y, params[i+5], params[i+6], params[i], params[i+1], params[i+2], params[i+3], params[i+4])
                        //ellipse(arc.cx, arc.cy, arc.rx, arc.ry, arc.xAxisRotation, arc.startAngle, arc.endAngle, arc.anitClockwise)
                        //pos = { x: p[0], y: p[1] }
                        var arcs = a2c(pos.x, pos.y, params[i+5], params[i+6], params[i], params[i+1], params[i+2], params[i+3], params[i+4])
                        for(let arc of arcs) {
                            this.bezierCurveTo(arc[2], arc[3], arc[4], arc[5], arc[6], arc[7])
                            pos = { x: arc[6], y: arc[7] }
                        }                        
                    }      
                    lastControl = null
                    break;        
                case "Z":
                    this.lineTo(initialPoint.x, initialPoint.y)   
                    pos = { x: initialPoint.x, y: initialPoint.y }
                    lastControl = null
                    count = -1
                    break;
            }
            count++
            totalCount++
        }

        function getReflectedControlPoint(p, c) {
            if(c)
                return { 
                    x: p.x + (p.x - c.x),
                    y: p.y + (p.y - c.y)
                }
            else
                return p
        }

        function convertToAbsoluteCoordinates(params, s, e, last) {
            for(let i = s; i < e; i += 2) {
                params[i] = params[i] + last.x
                params[i + 1] = params[i + 1] + last.y
            }
        }
    }    

    cleanParams(params, convertToNumber = false) {
        return params.split(/,|\s/).map(x => x.trim()).filter(x => x).map(x => convertToNumber ? parseFloat(x) : x)
    }

    getAttribute(node, key, defaultValue = null) {
        return node.attributes[key] ? node.attributes[key].value : defaultValue
    }
}

class SVGDrawer extends SVGEngine {
    constructor(context) {
        super()
        this.context = context
    }

    drawSVG(root, rotation, scale, size) {
        this.currentTransform = { scale: scale, rotation: rotation }
        this.size = size
        this.processNode(root)
    }    

    translate(x,y) {
        this.context.translate(x,y)
    }

    scale(x,y) {
        this.context.scale(x,y)
    }

    rotate(angle) {
        this.context.rotate(angle)
    }

    setTransform(a, b, c, d, e, f) {
        this.context.setTransform(a, b, c, d, e, f)
    }

    ellipse(x, y, radiusX, radiusY, rotation, startAngle, endAngle) {
        this.context.ellipse(x, y, radiusX, radiusY, rotation, startAngle, endAngle)
    }

    bezierCurveTo(cp1x, cp1y, cp2x, cp2y, x, y) {
        this.context.bezierCurveTo(cp1x, cp1y, cp2x, cp2y, x, y)
    }

    lineTo(x, y) {
        this.context.lineTo(x, y)
    }

    moveTo(x, y) {
        this.context.moveTo(x, y)
    }

    rect(x, y, width, height) {
        this.context.rect(x, y, width, height)
    }

    saveState() {
        this.context.save()
        this.context.beginPath()
    }

    restoreState() {
        this.context.stroke()   
        this.context.restore()
    }

    viewBoxUpdated(w, h) {
        this.context.lineWidth = 3 / Math.max(this.currentTransform.scale.x, this.currentTransform.scale.y)  
    }
}

class SVGToAudio extends SVGEngine {
    constructor() {
        super()
        this.audioData = {}
    }

    convertSVG(root, rotation, scale, size) {
        this.currentTransform = { scale: scale, rotation: rotation }
        this.size = size
        this.processNode(root)
    }    

    translate(x,y) {
    }

    scale(x,y) {
    }

    rotate(angle) {
    }

    setTransform(a, b, c, d, e, f) {
    }

    ellipse(x, y, radiusX, radiusY, rotation, startAngle, endAngle) {
    }

    bezierCurveTo(cp1x, cp1y, cp2x, cp2y, x, y) {
    }

    lineTo(x, y) {
    }

    moveTo(x, y) {
    }

    rect(x, y, width, height) {
    }

    saveState() {
    }

    restoreState() {
    }

    viewBoxUpdated(w, h) {
    }
}

export { SVGDrawer }