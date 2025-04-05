import React from 'react'
import FloatingMenu from './FloatingMenu.js';
import Layers from './Layers.js';
import { SVGDrawer } from '../lib/SVGEngine.js'
import { distPoint, EventQueue, convertFromOriginalSpace, request, hashCode, uuidv4, clearThumbnail, EventManager } from '../lib/Utility.js'
import { multiply, inv } from 'mathjs'

class OscilloscopeViewer extends React.Component {
    constructor(props) {
        console.log("constructor")
        super(props)

        this.dragging = false
        this.dragAction = null
        this.mousePos = { x: 0, y: 0 }
        this.cursor = 'default'
        this.cursorElement = null
        this.canvas = null
        this.buffer = null
        this.canvasContext = null
        this.bufferContext = null
        this.startX = null
        this.startY = null
        this.offset = 20
        this.elementData = {}
        this.dimensions = {}
        this.rotateRadius = 7
        this.shapes = []      
        this.cursorSource = null
        this.cursorAngle = null
        this.history = []
        this.historyIndex = 0
        this.globalTransform = null
        this.localTransform = null
        this.rerenderLayers = () => {}
        this.updateLayers = () => {}

        this.eventQueue = new EventQueue()
        this.drawQueue = new EventQueue()
        this.updateHistoryQueue = new EventQueue()
        this.thumbnailRenderQueue = new EventQueue()
        
        this.state = {
            selectedElement: null
        }
        
        setInterval(this.drawQueue.processEventQueue, (1 / 30) * 1000, this.drawQueue)
        setInterval(this.eventQueue.processEventQueue, (1 / 60) * 1000, this.eventQueue)
        setInterval(this.updateHistoryQueue.processEventQueue, (1 / 1) * 1000, this.updateHistoryQueue)
        props.setSetSelected(e => {
            this.setSelected(e)
            this.updateHistory()
        })
        this.updateHistory()
    }

    async componentDidMount() {        
        console.log("init")
        this.canvas = document.getElementById("osViewer");
        this.canvasContext = this.canvas.getContext('2d');
        this.buffer = new OffscreenCanvas(0,0)
        this.bufferContext = this.buffer.getContext('2d', { willReadFrequently: true});
        this.cursorElement = document.getElementById("cursor-icon")

        window.addEventListener('resize', this.resize)        

        EventManager.registerEvent('mousemove', this.canvas, this.mouseMove)
        EventManager.registerEvent('mousedown', this.canvas, this.mouseDown)
        EventManager.registerEvent('mouseup', this.canvas, this.mouseUp)
        EventManager.registerEvent('keydown', this.canvas, this.keyDown)
        EventManager.registerEvent('keyup', this.canvas, this.keyUp)
        EventManager.registerEvent('wheel', this.canvas, this.wheel)

        this.updateDimensions()
        this.draw()

        let response = await request("shapes")
        if (response.status == 200) {
            this.shapes = await response.json()
        }
    }

    componentDidUpdate() {
        if(!this.props.frame.elements && this.state.selectedElement)
            this.state.selectedElement = null
        this.draw()
    }

    resize = () => {
        this.eventQueue.queueEvent("resize", () => {
            this.updateDimensions()
            this.draw()
        })
    }

    onFrameUpdate = (frame) => {
        this.props.onFrameUpdate(frame || this.props.frame)
    }

    mouseMove = (e) => {
        this.eventQueue.queueEvent("mousemove", () => {
            let clientRect = this.canvas.getBoundingClientRect()
            let newMousePos = { x: e.pageX - clientRect.left, y: e.pageY - clientRect.top }
            this.cursor = "default"            

            if (this.state.selectedElement) {
                let buffer = 20
                let data = this.elementData[this.state.selectedElement.id]

                if (this.dragAction) {
                    if(e.ctrlKey && this.state.selectedElement && this.state.selectedElement.type == "draw") {
                        this.setDrawIcon(newMousePos)
                    }
                    else 
                        this.cursor = "grabbing"
                    this.dragAction(newMousePos.x - this.mousePos.x, newMousePos.y - this.mousePos.y, data)
                }
                else if(e.ctrlKey && this.state.selectedElement.type == "draw") {
                    this.setDrawIcon(newMousePos)
                }
                else {
                    if (data && data.bounds && this.inside(data.bounds, newMousePos)) {
                        this.cursor = this.dragging ? "grabbing" : "grab"
                        if (this.dragging) {
                            this.dragAction = (dx, dy, data) => {
                                this.translateElement(dx, dy)
                                this.onFrameUpdate()
                            }
                        }
                    }
                    if (data && data.center) {
                        let boundDists = []
                        for (let i = 0; i < data.bounds.length; i++) {
                            let int = this.intersection(newMousePos, data.center, data.bounds[i], data.bounds[(i + 1) % data.bounds.length])
                            boundDists.push(distPoint(int, newMousePos))
                        }
                        let p = { x: data.center.x + Math.sin(this.state.selectedElement.rotation) * data.radius, y: data.center.y - Math.cos(this.state.selectedElement.rotation) * data.radius }

                        if (distPoint(newMousePos, p) <= this.rotateRadius + 2) {
                            this.cursor = this.dragging ? "grabbing" : "grab"
                            if (this.dragging) {
                                this.dragAction = (dx, dy, data) => {
                                    const angle = -Math.atan2(-(this.mousePos.x - data.center.x), -(this.mousePos.y - data.center.y))
                                    this.updateAndRecenterElement(() => this.rotateElement(angle))
                                    this.onFrameUpdate()
                                }
                            }
                        }
                        else if (data.arrows && (this.inside(data.arrows[0].bounds, newMousePos) || this.inside(data.arrows[1].bounds, newMousePos))) {
                            this.cursor = "pointer"
                        }
                        else if (distPoint(newMousePos, data.bounds[0]) <= buffer) {
                            this.setArrowsCursor(315)
                            if (this.dragging) {
                                this.dragAction = (dx, dy, data) => {
                                    let ts = this.calculateDistanceScale(dx, dy, data.bounds[0], data.bounds[2], data.bounds[0])
                                    this.updateAndRecenterElement(() => this.scaleElementUniform(ts))
                                    this.onFrameUpdate()
                                }
                            }
                        }
                        else if (distPoint(newMousePos, data.bounds[1]) <= buffer) {
                            this.setArrowsCursor(45)
                            if (this.dragging) {
                                this.dragAction = (dx, dy, data) => {
                                    let b1 = { x: data.bounds[2].x, y: data.bounds[0].y }
                                    let b2 = { x: data.bounds[0].x, y: data.bounds[2].y }
                                    let ts = this.calculateDistanceScale(dx, dy, b1, b2, b1)                                    
                                    this.updateAndRecenterElement(() => this.scaleElementUniform(ts))
                                    this.onFrameUpdate()
                                }
                            }
                        }
                        else if (distPoint(newMousePos, data.bounds[2]) <= buffer) {
                            this.setArrowsCursor(135)
                            if (this.dragging) {
                                this.dragAction = (dx, dy, data) => {
                                    let ts = this.calculateDistanceScale(dx, dy, data.bounds[0], data.bounds[2], data.bounds[2])
                                    this.updateAndRecenterElement(() => this.scaleElementUniform(ts))
                                    this.onFrameUpdate()
                                }
                            }
                        }
                        else if (distPoint(newMousePos, data.bounds[3]) <= buffer) {
                            this.setArrowsCursor(225)
                            if (this.dragging) {
                                this.dragAction = (dx, dy, data) => {
                                    let b1 = { x: data.bounds[2].x, y: data.bounds[0].y }
                                    let b2 = { x: data.bounds[0].x, y: data.bounds[2].y }
                                    let ts = this.calculateDistanceScale(dx, dy, b1, b2, b2)                                    
                                    this.updateAndRecenterElement(() => this.scaleElementUniform(ts))
                                    this.onFrameUpdate()
                                }
                            }
                        }
                        else if (boundDists[0] <= buffer) {
                            this.setArrowsCursor(0)
                            if (this.dragging) {
                                this.dragAction = (dx, dy, data) => {
                                    const r = this.state.selectedElement.rotation
                                    const angle = -Math.atan2(Math.sin(r) * dy + Math.cos(r) * dx, Math.sin(r) * dx - Math.cos(r) * dy)                                    
                                    this.updateAndRecenterElement(() => this.scaleElementBounds(0, -distPoint({ x: 0, y: 0 }, { x: dx, y: dy }) * Math.cos(angle), data.bounds))
                                    this.onFrameUpdate()
                                }
                            }
                        }
                        else if (boundDists[1] <= buffer) {
                            this.setArrowsCursor(90)
                            if (this.dragging) {
                                this.dragAction = (dx, dy, data) => {                                    
                                    const r = this.state.selectedElement.rotation
                                    const angle = -Math.atan2(Math.sin(r) * dy + Math.cos(r) * dx, Math.sin(r) * dx - Math.cos(r) * dy)                                    
                                    this.updateAndRecenterElement(() => this.scaleElementBounds(-distPoint({ x: 0, y: 0 }, { x: dx, y: dy }) * Math.sin(angle), 0, data.bounds))
                                    this.onFrameUpdate()
                                }
                            }
                        }
                        else if (boundDists[2] <= buffer) {
                            this.setArrowsCursor(180)
                            if (this.dragging) {
                                this.dragAction = (dx, dy, data) => {
                                    const r = this.state.selectedElement.rotation
                                    const angle = -Math.atan2(Math.sin(r) * dy + Math.cos(r) * dx, Math.sin(r) * dx - Math.cos(r) * dy)                                    
                                    this.updateAndRecenterElement(() => this.scaleElementBounds(0, distPoint({ x: 0, y: 0 }, { x: dx, y: dy }) * Math.cos(angle), data.bounds))
                                    this.onFrameUpdate()
                                }
                            }
                        }
                        else if (boundDists[3] <= buffer) {
                            this.setArrowsCursor(270)
                            if (this.dragging) {
                                this.dragAction = (dx, dy, data) => {
                                    const r = this.state.selectedElement.rotation
                                    const angle = -Math.atan2(Math.sin(r) * dy + Math.cos(r) * dx, Math.sin(r) * dx - Math.cos(r) * dy)                                    
                                    this.updateAndRecenterElement(() => this.scaleElementBounds(distPoint({ x: 0, y: 0 }, { x: dx, y: dy }) * Math.sin(angle), 0, data.bounds))
                                    this.onFrameUpdate()
                                }
                            }
                        }
                    }
                }
            }        
            if(this.cursor == "icon") {
                this.cursor = "none"
                this.cursorElement.style.display = "block"                
                this.cursorElement.style.background = this.cursorSource
            }    
            else {
                this.cursorElement.style.display = "none"
            }
            document.body.style.cursor = this.cursor;
            this.mousePos = newMousePos
        })
    }

    mouseDown = (e) => {
        if (e.button == 0) {
            this.dragging = true
            if(e.ctrlKey && this.state.selectedElement && this.state.selectedElement.type == "draw") {
                this.setTransforms()
                let matrix = multiply(this.globalTransform, this.localTransform)
                let invMatrix = inv(matrix)

                let clientRect = this.canvas.getBoundingClientRect()
                let mouse = { x: e.pageX - clientRect.left, y: e.pageY - clientRect.top }

                let x = invMatrix[0][0] * mouse.x + invMatrix[0][1] * mouse.y + invMatrix[0][2]
                let y = invMatrix[1][0] * mouse.x + invMatrix[1][1] * mouse.y + invMatrix[1][2]

                let path = document.createElementNS("http://www.w3.org/1999/xhtml", "path")
                path.setAttribute("d", `m ${x} ${y}`)
                this.state.selectedElement.rootNode.appendChild(path)
                this.state.selectedElement.imageData = btoa("<svg>"+this.state.selectedElement.rootNode.innerHTML+"</svg>")
                this.dragAction = this.drawAction
                this.onFrameUpdate()
            }
            else {
                this.mouseMove(e)
            }
        }
        document.body.style.cursor = this.cursor;
    }

    mouseUp = (e) => {
        if (e.button == 0) {
            let data = this.state.selectedElement ? this.elementData[this.state.selectedElement.id] : null

            if (this.dragAction) {
                if (this.state.selectedElement && this.inside(data.bounds, this.mousePos, 40))
                    this.cursor = "grab"
                else
                    this.cursor = "default"

                if(this.state.selectedElement) 
                {
                    this.updateHistory()
                }
            }
            else if (this.state.selectedElement && data && data.arrows && this.inside(data.arrows[0].bounds, this.mousePos)) {
                this.updateAndRecenterElement(() => this.scaleElement(1, -1))                    
                this.onFrameUpdate()
                this.updateHistory()
            }
            else if (this.state.selectedElement && data && data.arrows && this.inside(data.arrows[1].bounds, this.mousePos)) {
                this.updateAndRecenterElement(() => this.scaleElement(-1, 1))  
                this.onFrameUpdate()
                this.updateHistory()
            }
            else {
                if(this.state.selectedElement)
                    this.setSelected(null)
                this.cursor = "default"
                for (let element of this.props.frame.elements.toSorted((a, b) => a.order - b.order)) {
                    if (this.elementData[element.id] && this.inside(this.elementData[element.id].hull, this.mousePos)) {
                        this.setSelected(element)
                        this.cursor = "grab"
                        break
                    }
                }
            }
            this.dragging = false
            this.dragAction = null
        }
        document.body.style.cursor = this.cursor;
    }

    keyDown = (e) => {
        this.eventQueue.queueEvent("keydown", () => {
            if(e.ctrlKey && !e.repeat) {
                if(this.state.selectedElement && this.state.selectedElement.type == "draw") {
                    this.setDrawIcon(this.mousePos)
                    document.body.style.cursor = "none"
                    this.cursorElement.style.background = this.cursorSource
                }
            }
        })
    }

    keyUp = (e) => {
        this.eventQueue.queueEvent("keyup", () => {
            if (document.activeElement.nodeName == "BODY") {          
                if(e.ctrlKey) {
                    switch (e.key) {
                        case "z":
                            this.stepHistory(-1)
                            break;
                        case "y":
                            this.stepHistory(1)
                            break;
                        case "c":
                        case "v":
                            if(this.state.selectedElement) {
                                let element = JSON.parse(JSON.stringify(this.state.selectedElement, (key, value) => key == "rootNode" ? null : value))
                                element.id = uuidv4()
                                element.rootNode = this.state.selectedElement.rootNode
                                element.translation.x += 5
                                element.translation.y += 5
                                this.state.selectedElement = element
                                this.props.frame.elements.push(element)
                                this.onFrameUpdate()
                                this.updateHistory()
                            }
                            break;
                    }
                }
                else if(this.state.selectedElement) {
                    switch (e.key) {
                        case "Delete":
                        case "Backspace":
                            this.props.frame.elements = this.props.frame.elements.filter(x => x.id != this.state.selectedElement.id)
                            this.setSelected(null)
                            this.onFrameUpdate()
                            this.updateHistory()
                            break;
                        case "ArrowUp":
                            this.translateElement(0, -10)
                            this.onFrameUpdate()
                            this.updateHistory()
                            break;
                        case "ArrowDown":
                            this.translateElement(0, 10)
                            this.onFrameUpdate()
                            this.updateHistory()
                            break;
                        case "ArrowLeft":
                            this.translateElement(-10, 0)
                            this.onFrameUpdate()
                            this.updateHistory()
                            break;
                        case "ArrowRight":
                            this.translateElement(10, 0)
                            this.onFrameUpdate()
                            this.updateHistory()
                            break;
                    }                    
                }
            }
            document.body.style.cursor = "default"
            this.cursorElement.style.display = "none"
        })
    }

    wheel = (e) => {
        if(this.state.selectedElement) {
            this.eventQueue.queueEvent("wheel", () => {
                this.updateAndRecenterElement(() => this.scaleElementUniform(1 - Math.sign(e.deltaY) * 0.1))
                this.onFrameUpdate()
            })
            this.updateHistoryQueue.queueEvent("history", () => this.updateHistory())
        }
    }

    setDrawIcon = (pos) => {
        this.cursor = "icon"
        this.cursorElement.style.display = "block"
        this.cursorElement.style.left = pos.x - 4 + "px"
        this.cursorElement.style.top = pos.y + 29 + "px"
        this.cursorSource = 'transparent url("pencil-solid.svg") center/75% no-repeat'        
    }

    drawAction = (dx, dy, data) => {
        let matrix = multiply(this.globalTransform, this.localTransform)
        let invMatrix = inv(matrix)

        let x = invMatrix[0][0] * dx + invMatrix[0][1] * dy
        let y = invMatrix[1][0] * dx + invMatrix[1][1] * dy

        let path = this.state.selectedElement.rootNode.lastChild
        path.setAttribute("d", path.getAttribute("d") + ` ${x} ${y}`)
        clearThumbnail(this.state.selectedElement)
        this.onFrameUpdate()
    }

    updateHistory = () => {  
        console.log("updateHistory")      
        let json = JSON.stringify(this.props.frame, (key, value) => key == "rootNode" ? null : value)
        let hash = hashCode(json)

        if(this.history.length == 0 || this.history[this.historyIndex].hash != hash) {
            var xmlSerializer = new XMLSerializer();
            var nodes = this.props.frame.elements.reduce((a,b) => a.concat(xmlSerializer.serializeToString(b.rootNode)), [])

            this.history = this.history.slice(-49, this.historyIndex + 1)
            this.history.push({ frame: json, rootNodes: nodes, hash: hash })
            this.historyIndex = this.history.length - 1
        }
    }

    stepHistory = (dir) => {
        let prevIndex = this.historyIndex
        this.historyIndex = Math.max(Math.min(this.historyIndex + dir, this.history.length - 1), 0)
        if(prevIndex != this.historyIndex) {
            let snapshot = this.history[this.historyIndex]
            let newFrame = JSON.parse(snapshot.frame)

            let parser = new DOMParser()
            for(let i in newFrame.elements) {
                newFrame.elements[i].rootNode = parser.parseFromString(snapshot.rootNodes[i], 'image/svg+xml').firstElementChild                
                clearThumbnail(newFrame.elements[i])
            }

            if(this.state.selectedElement)
                this.state.selectedElement = newFrame.elements.find(x => x.id == this.state.selectedElement.id)
            this.onFrameUpdate(newFrame)
            this.updateLayers()
        }
    }

    setArrowsCursor = (angle) => {
        angle = Math.round(angle + this.state.selectedElement.rotation / (Math.PI * 2) * 360)

        if(this.cursorAngle != angle) {
            this.cursorAngle = angle            
            let svg = `<svg xmlns="http://www.w3.org/2000/svg" width="28px" height="28px" viewBox="-1 0 26 24" stroke="#000"><path d="M4.24267 7.75732L5.65688 9.17154L3.82842 11H20.1716L18.3431 9.17154L19.7573 7.75732L24 12L19.7572 16.2426L18.343 14.8284L20.1714 13H3.82845L5.65685 14.8284L4.24264 16.2426L0 12L4.24267 7.75732Z" stroke="#000" fill="#fff" transform="rotate(${angle - 90} 13 12)"/></svg>`
            this.cursorSource = `transparent url("data:image/svg+xml;charset=utf-8,${encodeURIComponent(svg)}") center center no-repeat`            
        }
        this.cursorElement.style.left = this.mousePos.x - 14 + "px"
        this.cursorElement.style.top = this.mousePos.y + 40 + "px"
        this.cursor = "icon"
    }

    calculateDistanceScale = (dx, dy, b1, b2, t) => {
        let sl = (b2.y - b1.y) / (b2.x - b1.x)
        let b = (t.y + dy) - (t.x + dx) * (-1 / sl)
        let p1 = { x: 10000, y: 10000 * (-1 / sl) + b }
        let p2 = { x: -10000, y: -10000 * (-1 / sl) + b }
        b = b1.y - b1.x * sl
        let p3 = { x: 10000, y: 10000 * sl + b }
        let p4 = { x: -10000, y: -10000 * sl + b }
        let ip = this.intersection(p1, p2, p3, p4)
        let d = distPoint(ip, t)
        let bd = distPoint(b1, b2)
        let s = distPoint(ip, t == b1 ? b2 : b1) > bd ? 1 : -1
        let ts = (bd + d * s) / bd
        return ts
    }

    translateElement = (dx, dy) => {
        var p = convertFromOriginalSpace({ x: dx, y: dy }, this.dimensions)
        this.state.selectedElement.translation.x += p.x
        this.state.selectedElement.translation.y += p.y
    }

    scaleElement = (dx, dy) => {
        this.state.selectedElement.scale.x *= dx
        this.state.selectedElement.scale.y *= dy
    }

    scaleElementBounds = (dx, dy, bounds) => {
        this.state.selectedElement.scale.x *= (bounds[1].x - bounds[0].x + dx) / (bounds[1].x - bounds[0].x)
        this.state.selectedElement.scale.y *= (bounds[1].y - bounds[2].y + dy) / (bounds[1].y - bounds[2].y)
    }

    scaleElementUniform = (s) => {
        this.state.selectedElement.scale.x *= s
        this.state.selectedElement.scale.y *= s
    }

    rotateElement = (rotation) => {
        this.state.selectedElement.rotation = rotation % (Math.PI * 2)
    }

    setTransforms = () => {
        let s = Math.min(this.dimensions.viewWidth / 100, this.dimensions.viewHeight / 100)   
        let scale = [[s, 0, 0,], [0, s, 0], [0, 0, 1]]
        let rotation = [[1, 0, 0], [0, 1, 0], [0, 0, 1]]
        let translation = [[1, 0, Math.round((this.canvas.width - this.dimensions.viewWidth) / 2)], [0, 1, Math.round((this.canvas.height - this.dimensions.viewHeight - this.offset) / 2)], [0, 0, 1]]
        this.globalTransform = multiply(translation, rotation, scale)

        let sx = this.state.selectedElement.scale.x
        let sy = this.state.selectedElement.scale.y
        let r = this.state.selectedElement.rotation
        let x = this.state.selectedElement.translation.x
        let y = this.state.selectedElement.translation.y 
        scale = [[sx, 0, 0,], [0, sy, 0], [0, 0, 1]]
        rotation = [[Math.cos(r), -Math.sin(r), 0], [Math.sin(r), Math.cos(r), 0], [0, 0, 1]]
        translation = [[1, 0, x], [0, 1, y], [0, 0, 1]]
        this.localTransform = multiply(translation, rotation, scale)
    }

    updateAndRecenterElement = (updateAction) => {
        this.setTransforms()
        let matrix = multiply(this.globalTransform, this.localTransform)
        let invMatrix = inv(matrix)

        let data = this.elementData[this.state.selectedElement.id]        
        let cx = invMatrix[0][0] * data.center.x + invMatrix[0][1] * data.center.y + invMatrix[0][2]
        let cy = invMatrix[1][0] * data.center.x + invMatrix[1][1] * data.center.y + invMatrix[1][2]
        
        updateAction()

        let sx = this.state.selectedElement.scale.x
        let sy = this.state.selectedElement.scale.y
        let r = this.state.selectedElement.rotation
        let x = this.state.selectedElement.translation.x
        let y = this.state.selectedElement.translation.y
        let scale = [[sx, 0, 0,], [0, sy, 0], [0, 0, 1]]
        let rotation = [[Math.cos(r), -Math.sin(r), 0], [Math.sin(r), Math.cos(r), 0], [0, 0, 1]]
        let translation = [[1, 0, x], [0, 1, y], [0, 0, 1]]
        this.localTransform = multiply(translation, rotation, scale)  

        matrix = multiply(this.globalTransform, this.localTransform)

        let tx = matrix[0][0] * cx + matrix[0][1] * cy + matrix[0][2]
        let ty = matrix[1][0] * cx + matrix[1][1] * cy + matrix[1][2]       

        this.translateElement(data.center.x - tx, data.center.y - ty)
    }

    updateDimensions = () => {
        let dims = this.canvas.parentElement.getBoundingClientRect()
        this.canvas.width = dims.width
        this.canvas.height = dims.height
        this.buffer.width = dims.width
        this.buffer.height = dims.height
        this.dimensions.viewWidth = (this.canvas.width > this.canvas.height - this.offset ? (this.canvas.height - this.offset) * this.props.settings["aspectRatio"] : this.canvas.width) - 2
        this.dimensions.viewHeight = (this.canvas.height - this.offset > this.canvas.width ? this.canvas.width / this.props.settings["aspectRatio"] : this.canvas.height - this.offset) - 2
    }

    draw = async () => {
        console.log("draw")
        this.bufferContext.reset()
        this.bufferContext.clearRect(0, 0, this.canvas.width, this.canvas.height)
        this.bufferContext.beginPath();
        this.bufferContext.strokeStyle = '#000'
        this.bufferContext.fillStyle = this.props.settings["backgroundColor"]
        this.bufferContext.lineWidth = 2
        this.bufferContext.globalCompositeOperation = "source-over"
        this.bufferContext.rect(Math.round((this.canvas.width - this.dimensions.viewWidth) / 2), Math.round((this.canvas.height - this.dimensions.viewHeight - this.offset) / 2), this.dimensions.viewWidth, this.dimensions.viewHeight)
        this.bufferContext.stroke()
        this.bufferContext.fill()

        if (this.props.settings["displayGrid"]) {
            this.drawGridLines()
        }

        if (this.props.project && this.props.frame) {
            this.bufferContext.font = Math.min(14 * (this.canvas.width / 500), 14) + "px serif"
            this.bufferContext.strokeStyle = "#000"
            let dims = this.bufferContext.measureText(this.props.project.name)
            this.bufferContext.strokeText(this.props.project.name, Math.round((this.canvas.width - dims.width) / 2), Math.round((this.canvas.height + this.dimensions.viewHeight + this.offset / 2) / 2))    
                    
            for (let element of this.props.frame.elements)
                await this.drawSVG(element)

            for (let element of this.props.frame.elements)
                this.drawControlPoints(element)

            this.thumbnailRenderQueue.processEventQueue(this.thumbnailRenderQueue)
        }

        this.drawQueue.queueEvent("draw", () => {
            this.canvasContext.reset()
            this.canvasContext.putImageData(this.bufferContext.getImageData(0, 0, this.buffer.width, this.buffer.height), 0, 0)
        })            
    }

    drawGridLines = () => {
        this.bufferContext.beginPath();
        this.bufferContext.strokeStyle = this.props.settings["gridColor"]
        this.bufferContext.lineWidth = 1

        const midx = Math.round(this.canvas.width / 2)
        this.startX = Math.round((this.canvas.width - this.dimensions.viewWidth) / 2)
        const midy = Math.round((this.canvas.height - this.offset) / 2)
        this.startY = Math.round((this.canvas.height - this.dimensions.viewHeight - this.offset) / 2)

        this.bufferContext.moveTo(midx, this.startY)
        this.bufferContext.lineTo(midx, this.startY + this.dimensions.viewHeight)
        this.bufferContext.moveTo(this.startX, midy)
        this.bufferContext.lineTo(this.startX + this.dimensions.viewWidth, midy)

        let lineCount = (this.dimensions.viewWidth > this.dimensions.viewHeight ? this.props.settings["aspectRatio"] * 10 : 10) / 2
        let step = this.dimensions.viewWidth / lineCount / 2
        for (let i = 0; i < lineCount; i++) {
            this.bufferContext.moveTo(Math.round(midx + step * i), this.startY)
            this.bufferContext.lineTo(Math.round(midx + step * i), this.startY + this.dimensions.viewHeight)
            this.bufferContext.moveTo(Math.round(midx - step * i), this.startY)
            this.bufferContext.lineTo(Math.round(midx - step * i), this.startY + this.dimensions.viewHeight)
        }
        lineCount = (this.dimensions.viewWidth < this.dimensions.viewHeight ? 1 / this.props.settings["aspectRatio"] * 10 : 10) / 2
        step = this.dimensions.viewHeight / lineCount / 2
        for (let i = 0; i < lineCount; i++) {
            this.bufferContext.moveTo(this.startX, Math.round(midy + step * i))
            this.bufferContext.lineTo(this.startX + this.dimensions.viewWidth, Math.round(midy + step * i))
            this.bufferContext.moveTo(this.startX, Math.round(midy - step * i))
            this.bufferContext.lineTo(this.startX + this.dimensions.viewWidth, Math.round(midy - step * i))
        }
        this.bufferContext.stroke()
    }

    drawSVG = async (element) => {
        let scale = Math.min(this.dimensions.viewWidth / 100, this.dimensions.viewHeight / 100)
        let globalTransform = {
            translation: { x: Math.round((this.canvas.width - this.dimensions.viewWidth) / 2), y: Math.round((this.canvas.height - this.dimensions.viewHeight - this.offset) / 2) },
            scale: { x: scale, y: scale },
            rotation: 0
        }        

        let data
        if (element.rootNode) {
            data = await new SVGDrawer(this.bufferContext, this.props.settings, element.thumbnail == null).drawSVG(element.rootNode, element, globalTransform)
        }
        if(!data) {
            let size = 60
            data = {
                "bounds": [
                    { x: this.startX + element.translation.x + (this.dimensions.viewWidth - size) / 2, y: this.startY + element.translation.y + (this.dimensions.viewHeight - size) / 2 },
                    { x: this.startX + element.translation.x + (this.dimensions.viewWidth + size) / 2, y: this.startY + element.translation.y + (this.dimensions.viewHeight - size) / 2 },
                    { x: this.startX + element.translation.x + (this.dimensions.viewWidth + size) / 2, y: this.startY + element.translation.y + (this.dimensions.viewHeight + size) / 2 },
                    { x: this.startX + element.translation.x + (this.dimensions.viewWidth - size) / 2, y: this.startY + element.translation.y + (this.dimensions.viewHeight + size) / 2 }
                ]
            }
        }
        else {
            if(element.thumbnail == null) {
                element.thumbnail = data.thumbnail
                this.thumbnailRenderQueue.queueEvent("render", this.rerenderLayers)
            }
        }
        this.elementData[element.id] = data                
    }

    drawControlPoints = (element) => {
        let data = this.elementData[element.id]
        this.bufferContext.globalCompositeOperation = "source-over"
        if (this.state.selectedElement && this.state.selectedElement.id == element.id) {
            this.bufferContext.beginPath()
            this.bufferContext.strokeStyle = "#000"
            this.bufferContext.shadowBlur = 0
            this.bufferContext.setLineDash([4, 10]);
            this.bufferContext.moveTo(data.bounds[0].x, data.bounds[0].y)
            for (let i = 0; i < data.bounds.length; i++) {
                let p = data.bounds[(i + 1) % data.bounds.length]
                this.bufferContext.lineTo(p.x, p.y)
            }
            this.bufferContext.stroke()
            this.bufferContext.setLineDash([]);

            this.bufferContext.strokeStyle = "#888"
            this.bufferContext.fillStyle = "#888"
            if(data.center) {
                this.bufferContext.beginPath()                    
                this.bufferContext.ellipse(data.center.x + Math.sin(element.rotation) * data.radius, data.center.y - Math.cos(element.rotation) * data.radius, this.rotateRadius, this.rotateRadius, 0, 0, Math.PI * 2)
                this.bufferContext.fill()
            }

            if (this.props.settings.drawExtraPoints) {                
                if(data.center) {
                    this.bufferContext.beginPath()                    
                    this.bufferContext.ellipse(data.center.x, data.center.y, 2, 2, 0, 0, Math.PI * 2)
                    this.bufferContext.fill()
                }

                if(data.origin) {
                    this.bufferContext.beginPath()
                    this.bufferContext.ellipse(data.origin.x, data.origin.y, 2, 2, 0, 0, Math.PI * 2)
                    this.bufferContext.stroke()
                }

                if(data.points) {
                    for(let p of data.points)
                    {
                        this.bufferContext.beginPath()
                        this.bufferContext.ellipse(p[0], p[1], 2, 2, 0, 0, Math.PI * 2)
                        this.bufferContext.stroke()
                    }
                }
            }

            if (data.arrows) {
                for (let arrow of data.arrows) {
                    this.bufferContext.beginPath()
                    this.bufferContext.strokeStyle = "#888"
                    this.bufferContext.fillStyle = "#888"
                    this.bufferContext.moveTo(arrow.points[0].x, arrow.points[0].y)
                    for (let i = 0; i < arrow.points.length; i++) {
                        let p = arrow.points[(i + 1) % arrow.points.length]
                        this.bufferContext.lineTo(p[0], p[1])
                    }
                    this.bufferContext.fill()
                }
            }
        }

        if (this.props.settings["showHull"] && data.hull) {
            this.bufferContext.beginPath()
            this.bufferContext.strokeStyle = "#00000088"
            this.bufferContext.moveTo(data.hull[0].x, data.hull[0].y)
            for (let i = 0; i < data.hull.length; i++) {
                let p = data.hull[(i + 1) % data.hull.length]
                this.bufferContext.lineTo(p.x, p.y)
            }
            this.bufferContext.stroke()
        }
    }

    inside = (points, mouse) => {
        if (!points) return false
        var count = 0;
        var b_p1 = mouse;
        var b_p2 = { x: -100000, y: mouse.y + Math.random() };
        for (var a = 0; a < points.length; a++) {
            var a_p1 = points[a];
            var a_p2 = points[(a + 1) % points.length];
            while(true) {
                let i = this.intersection(a_p1, a_p2, b_p1, b_p2)
                if (i == null) {
                    b_p2.y += Math.random()
                }
                else {
                    count += i ? 1 : 0;
                    break
                }
            }
        }
        return count % 2 == 1;
    }

    intersection = (p1, p2, p3, p4) => {
        if ((p1.x === p2.x && p1.y === p2.y) || (p3.x === p4.x && p3.y === p4.y)) {
            return false
        }

        var denom = (p4.y - p3.y) * (p2.x - p1.x) - (p4.x - p3.x) * (p2.y - p1.y);
        if (denom == 0) {
            return null;
        }
        var ua = ((p4.x - p3.x) * (p1.y - p3.y) - (p4.y - p3.y) * (p1.x - p3.x)) / denom;
        var ub = ((p2.x - p1.x) * (p1.y - p3.y) - (p2.y - p1.y) * (p1.x - p3.x)) / denom;

        if (ua < 0 || ua > 1 || ub < 0 || ub > 1) {
            return false;
        }

        return {
            x: p1.x + ua * (p2.x - p1.x),
            y: p1.y + ua * (p2.y - p1.y)
        };
    }

    intersectionLine = (p1, p2, p3, p4) => {
        var ua, ub, denom = (p4.y - p3.y) * (p2.x - p1.x) - (p4.x - p3.x) * (p2.x - p1.y);
        if (denom == 0) {
            return null;
        }
        ua = ((p4.x - p3.x) * (p1.y - p3.y) - (p4.y - p3.y) * (p1.x - p3.x)) / denom;
        ub = ((p2.x - p1.x) * (p1.y - p3.y) - (p2.y - p1.y) * (p1.x - p3.x)) / denom;
        return {
            x: p1.x + ua * (p2.x - p1.x),
            y: p1.y + ua * (p2.y - p1.y)
        };
    }

    onElementUpdate = (element) => {
        if (element) {
            this.props.frame.elements = this.props.frame.elements.map(x => x.id == element.id ? element : x)
        }
        this.state.selectedElement = element
        this.updateLayers()
        this.onFrameUpdate(this.props.frame)
        this.updateHistory()
    }

    setSelected = (element) => {
        this.setState({selectedElement: element})
    }

    render = () => {
        return (
            <div className="viewer-container">
                <div className="canvas-container">
                    <div id="cursor-icon" ></div>
                    <canvas id="osViewer"></canvas>
                    {this.state.selectedElement != null ?
                        <FloatingMenu
                            frame={this.props.frame}
                            elements={[this.state.selectedElement]}
                            shapes={this.shapes}
                            onElementUpdate={this.onElementUpdate}
                            onClose={() => this.setSelected(null)} />
                        : null }                    
                </div>    
                <Layers 
                    frame={this.props.frame} 
                    selectedElement={this.state.selectedElement} 
                    settings={this.props.settings} 
                    onSetSelected={this.setSelected} 
                    onFrameUpdate={this.onFrameUpdate}
                    setRerenderTrigger={x => this.rerenderLayers = x}
                    setUpdateTrigger={x => this.updateLayers = x}/>
            </div>            
        )
    }
}

export default OscilloscopeViewer