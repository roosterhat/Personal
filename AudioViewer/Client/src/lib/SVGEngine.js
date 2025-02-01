import { transformPattern, pathPattern, numberPattern, delay, dist, distPoint, lerp, lerpPoint, convertFromOriginalSpace, convertToOriginalSpace } from './Utility.js'
import { a2c } from './a2c.js'
import { hull } from '$lib/Hulls.js'

class SVGEngine {
    constructor() {        
        this.gain = 1
        this.resetTransform()
    }

    resetTransform() {
        this.localTransformStack = [], this.globalTransformStack = []
        this.localTransform = { scale: { x: 1, y: 1 }, rotation: 0, translation: { x: 0, y: 0 } }
        this.globalTransform = { scale: { x: 1, y: 1 }, rotation: 0, translation: { x: 0, y: 0 } }
        this.t = { a: 1, b: 0, c: 0, d: 1, e: 0, f: 0}
        this.t_bounds = { a: 1, b: 0, c: 0, d: 1, e: 0, f: 0}
    }

    async processNode(node) {    
        if(!node) return;
        //console.log(node.nodeName)

        this.globalTransformStack.push(this.globalTransform)
        this.localTransformStack.push(this.localTransform)
        this.globalTransform = { 
            scale: { 
                x: this.globalTransform.scale.x * this.localTransform.scale.x, 
                y: this.globalTransform.scale.y * this.localTransform.scale.y 
            }, 
            translation: { 
                x: this.globalTransform.translation.x + this.localTransform.translation.x * this.globalTransform.scale.x, 
                y: this.globalTransform.translation.y + this.localTransform.translation.y * this.globalTransform.scale.y
            },
            rotation: this.globalTransform.rotation + this.localTransform.rotation        
        }
        this.localTransform = { scale: { x: 1, y: 1 }, rotation: 0, translation: { x: 0, y: 0 } }
        this.updateTransform()
        
        this.onStart()
        try {
            if(!this.applyDefaultAttributes(node))
                return
            
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
                case "g":
                    this.processGroup(node)
                    break;
            }            

            for(let child of node.children) {
                this.processNode(child)
            }
        }
        catch(ex) {
            console.log(ex)
        }        

        this.localTransform = this.localTransformStack.pop()
        this.globalTransform = this.globalTransformStack.pop()
        this.updateTransform()
        this.onEnd()
        await delay(1) //defer
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
    // onStart() {}
    // onEnd() {}

    applyDefaultAttributes(node) {
        if(!node || !node.attributes) return true

        for(let attribute of ['transform-origin', 'transform', 'display', 'viewBox', 'x', 'y', 'dx', 'dy']) {
            let params, dx, dy
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
                                    this.scale(params[0], params[1])
                                    break;
                                case "scaleX":
                                    this.scale(params[0], this.localTransform.scale.y)
                                    break;
                                case "scaleY":
                                    this.scale(this.localTransform.scale.x, params[0])
                                    break;
                                case "rotate":
                                    this.rotate(params[0])
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

                        let scale = (h > w ? 100 / h : 100 / w) * padding
                        this.scale(scale, scale)
                        this.translate(Math.round((100 - (w * scale))  / 2), Math.round((100 - (h * scale)) / 2))                           
                        break;                    
                }
            }
        }

        return true
    }   

    processCircle(node) {
        let r = this.getAttribute(node, 'r')
        this.ellipse(this.getAttribute(node, 'cx', 0), this.getAttribute(node, 'cy', 0), r, r, 0, 0, 2 * Math.PI)
    }

    processEllipse(node) {
        let rx = this.getAttribute(node, 'rx')
        let ry = this.getAttribute(node, 'ry')
        this.ellipse(this.getAttribute(node, 'cx', 0), this.getAttribute(node, 'cy', 0), rx, ry, 0, 0, 2 * Math.PI)
    }

    processLine(node) {
        this.moveTo(this.getAttribute(node, 'x1'), this.getAttribute(node, 'y1'))
        this.lineTo(this.getAttribute(node, 'x2'), this.getAttribute(node, 'y2'))
    }

    processPolygon(node) {
        var points = this.getAttribute(node, 'points', "", false).split(" ").map(x => { let point = x.split(","); return { x: point[0], y: point[1] }})
        for(let i = 0; i < points.length; i++) {
            let p1 = points[i]
            let p2 = points[(i + 1) % points.length]
            this.moveTo(p1.x, p1.y)
            this.lineTo(p2.x, p2.y)
        }
    }

    processPolyline(node) {
        var points = this.getAttribute(node, 'points', "", false).split(" ").map(x => { let point = x.split(","); return { x: point[0], y: point[1] }})
        for(let i = 0; i < points.length - 1; i++) {
            let p1 = points[i]
            let p2 = points[(i + 1) % points.length]
            this.moveTo(p1.x, p1.y)
            this.lineTo(p2.x, p2.y)
        }
    }

    processRect(node) {
        let x = this.getAttribute(node, 'x', 0)
        let y = this.getAttribute(node, 'y', 0)
        let w = this.getAttribute(node, 'width')
        let h = this.getAttribute(node, 'height')
        this.rect(x, y, w, h)
    }

    processGroup(node) {
        for(let attribute of ['x', 'y', 'dx', 'dy']) {
            let params, dx, dy
            let attributeValue = node.attributes[attribute] ? node.attributes[attribute].value : null
            if(attributeValue) {                
                switch(attribute) {
                    case "x":
                        params = this.cleanParams(attributeValue, true) 
                        dx = params[0] - this.localTransform.translation.x                        
                        this.translate(dx, 0)
                        break;
                    case "y":
                        params = this.cleanParams(attributeValue, true)
                        dy = params[0] - this.localTransform.translation.y
                        this.translate(0, dy)
                        break;
                    case "dx":
                        params = this.cleanParams(attributeValue, true)                        
                        this.translate(params[0], 0)
                        break;
                    case "dy":
                        params = this.cleanParams(attributeValue, true)                        
                        this.translate(0, params[0])
                        break;
                }
            }
        }
    }

    processPath(node) {
        let params, control, lastControl, type, d, initialPoint, pos = { x: 0, y: 0 }, count = 0, totalCount = 0
        for(let match of this.getAttribute(node, 'd', "", false).matchAll(pathPattern)) {
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

    translate(x,y) {
        this.localTransform.translation.x += x
        this.localTransform.translation.y += y
        this.updateTransform()
    }

    scale(x,y) {
        this.localTransform.scale.x = x
        this.localTransform.scale.y = y
        this.updateTransform()
    }

    rotate(angle) {
        this.localTransform.rotation += angle
        this.updateTransform()
    }

    setTransform(a, b, c, d, e, f) {
        // a c e
        // b d f
        // 0 0 1

        this.localTransform.translation.x = e
        this.localTransform.translation.y = f
        this.localTransform.scale.x = dist(0, 0, a, b)
        this.localTransform.scale.y = dist(0, 0, c, d)
        this.localTransform.rotation = Math.acos(a / this.localTransform.scale.x)

        this.updateTransform()
    }

    updateTransform() {
        let scaleX = this.globalTransform.scale.x * this.localTransform.scale.x
        let scaleY = this.globalTransform.scale.y * this.localTransform.scale.y
        let rotation = this.globalTransform.rotation + this.localTransform.rotation
        let translateX = this.globalTransform.translation.x + this.localTransform.translation.x * this.globalTransform.scale.x
        let translateY = this.globalTransform.translation.y + this.localTransform.translation.y * this.globalTransform.scale.y

        this.t = { 
            a: scaleX * Math.cos(rotation),
            b: scaleY * Math.sin(rotation),
            c: scaleX * -Math.sin(rotation),
            d: scaleY * Math.cos(rotation),
            e: translateX,
            f: translateY
        }

        this.t_bounds = {
            a: scaleX,
            b: 0,
            c: 0,
            d: scaleY,
            e: translateX,
            f: translateY
        }
    }

    transformCoordinates() {
        for(let i = 0; i < arguments.length; i += 2) {
            let x = (this.t.a * arguments[i] + this.t.c * arguments[i + 1] + this.t.e) * this.gain
            let y = (this.t.b * arguments[i] + this.t.d * arguments[i + 1] + this.t.f) * this.gain
            arguments[i] = x
            arguments[i + 1] = y
        }
        return arguments
    }

    cleanParams(params, convertToNumber = false) {
        return params.split(/,|\s/).map(x => x.trim()).filter(x => x).map(x => convertToNumber ? parseFloat(x) : x)
    }

    getAttribute(node, key, defaultValue = null, castToFloat = true) {
        let value = node.attributes[key] ? node.attributes[key].value : defaultValue
        return castToFloat ? parseFloat(value) : value
    }    
}

class SVGDrawer extends SVGEngine {
    constructor(context) {
        super()
        this.context = context                
    }

    drawSVG(root, element, globalTransform, dimensions) {
        this.pos = { x: 0, y: 0 }
        this.maxX = -Infinity
        this.minX = Infinity
        this.maxY = -Infinity
        this.minY = Infinity
        this.points = []
        this.resetTransform()
        this.globalTransform = { scale: globalTransform.scale, rotation: globalTransform.rotation, translation: globalTransform.translation }
        this.localTransform = { scale: element.scale, rotation: element.rotation, translation: element.translation, dimensions }
        this.processNode(root)

        let cosR = Math.cos(element.rotation)
        let sinR = Math.sin(element.rotation)
        let bounds = [[this.minX, this.minY], [this.maxX, this.minY], [this.maxX, this.maxY], [this.minX, this.maxY]].map(p => {
            let px = (cosR * p[0] - sinR * p[1])
            let py = (sinR * p[0] + cosR * p[1])
            return {x: px, y: py}
        })

        let tPoints = []
        for(let point of this.points) {
            let px = (cosR * point[0] - sinR * point[1])
            let py = (sinR * point[0] + cosR * point[1])
            tPoints.push([px, py])
        }

        let arrows = this.createArrows(bounds)
        for(let arrow of arrows) {
            for(let i in arrow.points) {
                let point = arrow.points[i]
                let px = (cosR * point[0] - sinR * point[1])
                let py = (sinR * point[0] + cosR * point[1])
                arrow.points[i] = [px, py]
            }

            for(let point of arrow.bounds) {
                point.x = (cosR * point[0] - sinR * point[1])
                point.y = (sinR * point[0] + cosR * point[1])
            }
        }

        return {
            "bounds": bounds,
            "arrows": arrows,
            "center": {x: bounds[0].x + (bounds[2].x - bounds[0].x) / 2, y: bounds[0].y + (bounds[2].y - bounds[0].y) / 2}, 
            "radius": (bounds[2].y - bounds[0].y) / 2 + 25,
            "hull": hull(tPoints, 1000).slice(0, -1).map(x => ({x: x[0], y: x[1]}))           
        }
    }   
    
    createArrows(bounds) {
        let invertOffset = 25, invertLength = 20, invertScale = 300, arrows = []
        let s = Math.min(1, Math.min(bounds[1].x - bounds[0].x, bounds[3].y - bounds[0].y) / invertScale)
        let x = bounds[1].x - invertOffset * s
        let y = bounds[0].y - invertOffset

        arrows.push({
            "points": [
                [x, y],
                [x - 5, y - 5],
                [x - 1, y - 5],
                [x - 1, y - invertLength + 5],
                [x - 5, y - invertLength + 5],
                [x, y - invertLength],
                [x + 5, y - invertLength + 5],
                [x + 1, y - invertLength + 5],
                [x + 1, y - 5],
                [x + 5, y - 5],
                [x, y]
            ],
            "bounds": [{x: x - 5, y: y - 5}, {x: x - 5, y: y - invertLength + 5}, {x: x + 5, y: y - invertLength + 5}, {x: x + 5, y: y - 5}]            
        })

        x = bounds[1].x + invertOffset
        y = bounds[0].y + invertOffset * s
        
        arrows.push({
            "points": [
                [x, y],
                [x + 5, y - 5],
                [x + 5, y - 1],
                [x + invertLength - 5, y - 1],
                [x + invertLength - 5, y - 5],
                [x + invertLength, y],
                [x + invertLength - 5, y + 5],
                [x + invertLength - 5, y + 1],
                [x + 5, y + 1],
                [x + 5, y + 5],
                [x, y]
            ],
            "bounds": [{x: x + 5, y:  y - 5}, {x: x + invertLength - 5, y:  y - 5}, {x: x + invertLength - 5, y:  y + 5}, {x: x + 5, y:  y + 5}]            
        }) 
        return arrows       
    }

    ellipse(x, y, radiusX, radiusY, rotation, startAngle, endAngle) {
        let step = Math.PI * 2 / 40
        this.context.moveTo(...this.transformCoordinates(x + radiusX, y))
        for(let i = step; i < Math.PI * 2; i += step) {    
            let px = x + radiusX * Math.cos(i+step), py = y + radiusY * Math.sin(i+step)
            this.trackPoint(px, py)        
            this.context.lineTo(...this.transformCoordinates(px, py))               
        }
        this.pos = { x: x, y: y }
    }

    bezierCurveTo(cp1x, cp1y, cp2x, cp2y, x, y) {
        let step = 1/20            
        this.trackPoint(this.pos.x, this.pos.y) 
        this.context.moveTo(...this.transformCoordinates(this.pos.x, this.pos.y)) 

        for(let p = step; p <= 1; p += step) {
            let p01 = lerpPoint(this.pos, {x: cp1x, y: cp1y}, p)
            let p12 = lerpPoint({x: cp1x, y: cp1y}, {x: cp2x, y: cp2y}, p)
            let p23 = lerpPoint({x: cp2x, y: cp2y}, {x: x, y: y}, p)

            let p01_12 = lerpPoint(p01, p12, p)
            let p12_23 = lerpPoint(p12, p23, p)

            let p0123 = lerpPoint(p01_12, p12_23, p)
            this.trackPoint(p0123.x, p0123.y)   
            this.context.lineTo(...this.transformCoordinates(p0123.x, p0123.y))               
        }    
        this.trackPoint(x, y)    
        this.context.lineTo(...this.transformCoordinates(x, y))        
        this.pos = { x: x, y: y }
    }

    lineTo(x, y) {
        this.trackPoint(this.pos.x, this.pos.y)
        this.trackPoint(x, y)
        this.context.lineTo(...this.transformCoordinates(x, y))        
        this.pos = {x: x, y: y}
    }

    moveTo(x, y) {
        this.context.moveTo(...this.transformCoordinates(x, y))
        this.pos = {x: x, y: y}
    }

    rect(x, y, width, height) {
        let points = [{x: x, y: y}, {x: x + width, y: y}, {x: x + width, y: y + height}, {x: x, y: y + height}]
        this.context.moveTo(...this.transformCoordinates(x, y))
        for(let i = 0; i < points.length; i++) {
            let p = points[(i + 1) % points.length]      
            this.trackPoint(p.x, p.y)
            this.context.lineTo(...this.transformCoordinates(p.x, p.y))            
        }
        this.pos = {x: x, y: y}
    }

    onStart() {
        this.context.beginPath()
    }

    onEnd() {
        this.context.stroke()   
    }

    trackPoint(x, y) {
        let px = (this.t_bounds.a * x + this.t_bounds.c * y + this.t_bounds.e)
        let py = (this.t_bounds.b * x + this.t_bounds.d * y + this.t_bounds.f)
        this.points.push([px,py])
        this.maxX = Math.max(this.maxX, px)
        this.minX = Math.min(this.minX, px)
        this.maxY = Math.max(this.maxY, py)
        this.minY = Math.min(this.minY, py)
    }
}

class SVGToAudio extends SVGEngine {
    constructor() {
        super()        
    }  

    async generateAudio(frame, targetFrequency, targetDuration, gain, sampleRate, corrections) {   
        this.gain = gain      
        this.stepSizes = {"line": 2, "bezier": 1, "ellipse": 40}
        
        this.channelData = [[], []]
        let repeatedData = [[], []]

        for(let element of frame.elements)
            await this.getChannelData(element)

        if(this.channelData[0].length > 0) {
            let totalSamples = targetDuration / 1000 * sampleRate
            let initFreq = totalSamples / this.channelData[0].length
            let stepMod = targetFrequency / initFreq
            for(let i in this.stepSizes)
                this.stepSizes[i] *= stepMod

            console.log(this.stepSizes)
            
            this.channelData = [[], []]
            for(let element of frame.elements)
                await this.getChannelData(element)

            this.globalTransform = { 
                scale: { x: corrections.scale.x, y: corrections.scale.y }, 
                translation: { x: corrections.translation.x, y: corrections.translation.y },
                rotation: corrections.rotation
            }
            this.updateTransform()
            for(var i = 0; i < this.channelData[0].length; i++) {
                let p = this.transformCoordinates(this.channelData[0][i], this.channelData[1][i])
                this.channelData[0][i] = p[0]
                this.channelData[1][i] = p[1]
            }
            
            let copies = Math.max(Math.floor(totalSamples / this.channelData[0].length), 1)
            console.log(copies)

            for(let i = 0; i < copies; i++) {
                repeatedData[0] = repeatedData[0].concat(this.channelData[0])
                repeatedData[1] = repeatedData[1].concat(this.channelData[1])
            }

            console.log(repeatedData)
        }

        return [
            new Float32Array(repeatedData[0]),   
            new Float32Array(repeatedData[1])   
        ]
    }       

    async getChannelData(element) {
        this.resetTransform()
        this.globalTransform = { 
            scale: { x: element.scale.x, y: element.scale.y }, 
            translation: { x: element.translation.x, y: element.translation.y },
            rotation: element.rotation
        }
        this.pos = { x: 0, y: 0 }        
        this.updateTransform()
        await this.processNode(element.rootNode)
    }

    ellipse(x, y, radiusX, radiusY, rotation, startAngle, endAngle) {
        let step = this.stepSizes["ellipse"] * (Math.PI / 40)
        for(let i = 0; i < Math.PI * 2; i += step) {
            this.pushChannelData({x: x + radiusX * Math.cos(i), y: y + radiusY * Math.sin(i)})
        }
    }

    bezierCurveTo(cp1x, cp1y, cp2x, cp2y, x, y) {
        let d = Math.max(dist(this.pos.x, this.pos.y, cp1x, cp1y), dist(cp1x, cp1y, cp2x, cp2y), dist(cp2x, cp2y, x, y))
        let step = this.stepSizes["bezier"] / d
        for(let p = 0; p < 1; p += step) {
            let p01 = lerpPoint(this.pos, {x: cp1x, y: cp1y}, p)
            let p12 = lerpPoint({x: cp1x, y: cp1y}, {x: cp2x, y: cp2y}, p)
            let p23 = lerpPoint({x: cp2x, y: cp2y}, {x: x, y: y}, p)

            let p01_12 = lerpPoint(p01, p12, p)
            let p12_23 = lerpPoint(p12, p23, p)

            this.pushChannelData(lerpPoint(p01_12, p12_23, p))
        }
        this.pos = {x: x, y: y}
    }

    lineTo(x, y) {
        let d = distPoint(this.pos, {x: x, y: y})
        let step = this.stepSizes["line"] / d
        for(let p = 0; p < 1; p += step) {
            this.pushChannelData(lerpPoint(this.pos, { x: x, y: y }, p))
        }
        this.pos = { x: x, y: y }
    }

    moveTo(x, y) {
        this.pos = {x: x, y: y}
    }

    rect(x, y, width, height) {
        let points = [{x: x, y: y}, {x: x + width, y: y}, {x: x + width, y: y + height}, {x: x, y: y + height}]        
        for(let i = 0; i < points.length; i++) {
            let p1 = points[i]
            let p2 = points[(i + 1) % points.length]
            let d = distPoint(p1, p2)
            let step = this.stepSizes["line"] / d
            for(let p = 0; p < 1; p += step) {
                this.pushChannelData(lerpPoint(p1, p2, p))
            }
        }
    }

    onStart() {
        //no action
    }

    onEnd() {
        //no action
    }

    pushChannelData(p) {        
        let _p = this.transformCoordinates(p.x, p.y)
        this.channelData[0].push(_p[0])
        this.channelData[1].push(_p[1])
    }   
}

export { SVGDrawer, SVGToAudio }