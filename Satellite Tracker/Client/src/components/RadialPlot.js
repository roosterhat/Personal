import React from 'react'
import { uuidv4 } from '../utility'

class RadialPlot extends React.Component {    
    ID = uuidv4()

    constructor(props) {
        super(props);        

        this.state = {
            context: null,
            center: {x: 0, y: 0},
            dims: null
        }
        
        this.dragging = false
        this.currentDrag = false
        this.currentShape = null
        this.grabRange = 5
        this.offset = 20
        this.state.dims = null
        this.eventQueue = {
            queue: {},
            actionQueued: false
        };

        this.props.setUpdateHandler(this.RefreshDimensions)
    }

    componentDidMount () {
        let container = document.getElementById(this.ID)
        let canvas = container.getElementsByTagName("canvas")[0]
        this.state.context = canvas.getContext("2d")        

        if(this.props.enableEditing) {
            canvas.addEventListener("mousemove", event => {                
                this.QueueEvent(event, () => {
                        let mouse = {x: event.offsetX, y: event.offsetY}
                        if(this.dragging){
                            let polarPoint = this.cartesianToPolar(mouse)
                            this.dragging.az = polarPoint.az;
                            this.dragging.el = polarPoint.el;
                        }
                            
                        this.Update(mouse);
                    }
                )
            })
        
            canvas.addEventListener("mouseup", event => {
                this.QueueEvent(event, () => {
                        let mouse = {x: event.offsetX, y: event.offsetY}
                        if(this.dragging) {
                            this.dragging = null;
                        }
                
                        if(this.props.currentZone != null) {
                            this.props.settings["keepOutZones"][this.props.currentZone].push(this.cartesianToPolar(mouse));
                            this.props.onSettingsChange()
                        }

                        this.Update(mouse);
                    }
                )
            })

            canvas.addEventListener("mousedown", event => {
                this.QueueEvent(event, () => {        
                        if(this.props.currentZone == null) {
                            let mouse = {x: event.offsetX, y: event.offsetY}
                            for(let zone of this.props.settings["keepOutZones"]) {
                                for(let point of zone) {
                                    if(this.Dist(mouse, this.polarToCartesian(point)) <= this.grabRange) {
                                        this.dragging = point
                                        return
                                    }
                                }
                            }
                        }
                    }
                )
            }) 

            window.onresize = event => {
                this.QueueEvent(event, () => this.RefreshDimensions())
            };

            setInterval(this.ProcessEventQueue, 1/30 * 1000, this.eventQueue)
        }

        this.RefreshDimensions()
    }

    componentDidUpdate(prevProps, prevState) {
        if(prevProps != this.props)
            this.RefreshDimensions()
    }

    RefreshDimensions = () => {
        if(!(this.state && this.state.context.canvas)) return

        var elem = document.getElementById(this.ID)
        this.state.dims = elem.getBoundingClientRect()
        this.state.context.canvas.width = this.state.dims.width
        this.state.context.canvas.height = this.state.dims.height
        this.state.center = {x: this.state.dims.width / 2, y: this.state.dims.height / 2}
        this.Update()
    }    

    Update = (mouse) => {
        if(!this.state.context) return

        this.state.context.reset()

        this.state.context.fillStyle = "#000"
        this.state.context.rect(0, 0, this.state.context.canvas.width, this.state.context.canvas.height)
        this.state.context.fill()

        this.state.context.strokeStyle = "green"

        let north = this.props.settings["offset"] * Math.PI / 180

        this.state.context.translate(this.state.center.x, this.state.center.y)
        this.state.context.rotate(north)

        this.state.context.beginPath()
        this.state.context.strokeText("N", -4, 15 - this.state.center.y)
        this.state.context.stroke()

        this.state.context.beginPath()
        this.state.context.moveTo(-4, 5 - this.state.center.y)
        this.state.context.lineTo(0, -this.state.center.y)
        this.state.context.lineTo(4, 5 - this.state.center.y)
        this.state.context.stroke()

        this.state.context.strokeStyle = "#00ff00aa"
        this.state.context.setLineDash([4, 4]);
        this.state.context.beginPath()
        this.state.context.moveTo(this.offset - this.state.center.x, 0)
        this.state.context.lineTo(this.state.center.x - this.offset, 0)
        this.state.context.moveTo(0, this.offset - this.state.center.y)
        this.state.context.lineTo(0, this.state.center.y - this.offset)
        this.state.context.stroke()

        this.state.context.resetTransform()
        this.state.context.strokeStyle = "green"
        this.state.context.setLineDash([]);

        this.state.context.beginPath()
        this.state.context.moveTo(0, this.state.center.y)
        this.state.context.lineTo(this.state.context.canvas.width, this.state.center.y)
        this.state.context.moveTo(this.state.center.x, 0)
        this.state.context.lineTo(this.state.center.x, this.state.context.canvas.height)
        this.state.context.stroke()

        let r = this.state.center.y - this.offset
        for (let i = 1; i <= 3; i++) {
            this.state.context.beginPath()
            this.state.context.ellipse(this.state.center.x, this.state.center.y, r * (i / 3), r * (i / 3), 0, 0, Math.PI * 2)
            this.state.context.stroke()
        }

        if(this.props.settings["displayPath"] && this.props.path && this.props.path.length > 0) {
            this.state.context.strokeStyle = "white"
            this.state.context.beginPath()
            for(let p = 0; p < this.props.path.length - 1; p++) {
                let p1 = this.props.path[p]
                let p2 = this.props.path[p + 1]
                let d = this.Dist(p1, p2)
                let cur = p1
                for(let i = 0; i < d; i += 1) {                   
                    let l = this.LerpPoint(p1, p2, i / d)

                    if(i % 3 != 0) { 
                        let point = this.polarToCartesian(cur)
                        this.state.context.moveTo(point.x, point.y)

                        point = this.polarToCartesian(l)
                        this.state.context.lineTo(point.x, point.y)
                    }
                    cur = l
                }                
            }
            this.state.context.stroke()
        }        

        if(this.props.settings && this.props.settings["keepOutZones"]) {
            for(let z in this.props.settings["keepOutZones"]) {
                let zone = this.props.settings["keepOutZones"][z]               

                this.state.context.beginPath()
                this.state.context.strokeStyle = this.props.highlightedZone == z ? "#ae00ff" : "#ffd900"
                this.state.context.fillStyle = this.props.highlightedZone == z ? "#ae00ff55" : "#ffd90033"

                if(zone.length > 0) {
                    let p = this.polarToCartesian(zone[0])
                    this.state.context.moveTo(p.x, p.y)
                }

                for(let i = 0; i < zone.length; i++) {
                    let p = this.polarToCartesian(zone[(i + 1) % zone.length])
                    this.state.context.lineTo(p.x, p.y)                
                }
                this.state.context.closePath()
                this.state.context.stroke()
                this.state.context.fill()                
                
                if(this.props.enableEditing) {
                    for(let i = 0; i < zone.length; i++) {
                        let p = this.polarToCartesian(zone[i])
                        let r = this.props.enableEditing && this.props.currentZone == null && mouse ? Math.max(Math.min(this.grabRange / this.Dist(mouse, p), 1) * this.grabRange, 2) : 2
                        this.state.context.beginPath()
                        this.state.context.fillStyle = this.props.highlightedZone == z ? (this.props.highlightedPoint == i ? "#ffffff" : "#ae00ff" ) : "#ffd900"
                        this.state.context.moveTo(p.x, p.y)
                        this.state.context.ellipse(p.x, p.y, r, r, 0, 0, Math.PI * 2)
                        this.state.context.fill()
                    }
                }
            }
        }

        if (this.props.target) {
            let d = 5

            let point = this.polarToCartesian(this.props.target)

            this.state.context.strokeStyle = "red"
            this.state.context.beginPath()
            this.state.context.moveTo(point.x - d, point.y)
            this.state.context.lineTo(point.x + d, point.y)
            this.state.context.moveTo(point.x, point.y - d)
            this.state.context.lineTo(point.x, point.y + d)
            this.state.context.stroke()
        }

        if(this.props.position) {
            let point = this.polarToCartesian(this.props.position)

            this.state.context.strokeStyle = "cyan"
            this.state.context.beginPath()
            this.state.context.ellipse(point.x, point.y, 5, 5, 0, 0, Math.PI * 2)
            this.state.context.stroke()
        }
        
        if(this.props.enableEditing && mouse) {
            this.state.context.strokeStyle = "#ffd900"
            if(this.props.currentZone != null) {
                this.state.context.beginPath()
                this.state.context.moveTo(mouse.x, mouse.y)
                this.state.context.ellipse(mouse.x, mouse.y, 3, 3, 0, 0, Math.PI * 2)
                this.state.context.stroke()
            }
            
            if(this.state.center) {
                let p = this.cartesianToPolar(mouse)
                this.state.context.strokeText(`${Math.round(p.az)},${Math.round(p.el)}`, 10, 15)
            }
        }
    }

    QueueEvent = (event, action) => {
        this.eventQueue.queue[event.type] = {
            time: Date.now(),
            action: action
        }
        this.eventQueue.actionQueued = true
    }

    ProcessEventQueue = (eventQueue) => {
        if(!eventQueue.actionQueued) return;
        eventQueue.actionQueued = false;
        var actions = Object.values(eventQueue.queue);
        for(var k in eventQueue.queue)
            delete eventQueue.queue[k]
        actions.sort((x,y) => x.time - y.time);
        for(var a of actions)
            a.action()
    }

    polarToCartesian = (point) => {
        let az = "az" in point ? point.az : ("azimuth" in point ? point.azimuth : point.x)
        let el = "el" in point ? point.el : ("elevation" in point ? point.elevation : point.y)
        let t = (az - 90) * Math.PI / 180
        let r = (90 - el) / 90 * (this.state.center.x - this.offset)
        return {x: r * Math.cos(t) + this.state.center.x, y: r * Math.sin(t) + this.state.center.y}
    }

    cartesianToPolar = (point) => {        
        return {
            az: (point.x < this.state.center.x ? 360 : 0) - Math.atan2(-(point.x - this.state.center.x), -(point.y - this.state.center.y)) * 180 / Math.PI, 
            el: (1 - this.Dist(point, this.state.center) / (this.state.center.x - this.offset)) * 90
        }
    }

    Dist = (p1, p2) => {
        return Math.sqrt(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2));
    }

    Lerp = (a, b, t) => {
        return a + (b - a) * t
    }

    LerpPoint = (p0, p1, t) => {
        let x = this.Lerp(p0.x, p1.x, t);
        let y = this.Lerp(p0.y, p1.y, t);
        return {x: x, y: y};
    }

    render = () => {
        return (
            <div className="canvas-container" id={this.ID}><canvas></canvas></div>
        )
    }
}

export default RadialPlot