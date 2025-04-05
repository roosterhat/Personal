import React from 'react'
import { uuidv4, EventQueue, updateText, parseSVG, Fonts, clearThumbnail, EventManager } from '../lib/Utility.js' 

class FloatingMenu extends React.Component {
    constructor(props) {
        super(props);

        this.cursor = "default"
        this.dragAction = null
        this.dragging = false
        this.mousePos = null
        this.eventQueue = new EventQueue() 

        this.state = {
            pos: {x: window.innerWidth - 220, y: 60}
        }

        window.addEventListener("resize", this.resize)        

        setInterval(this.eventQueue.processEventQueue, (1/60) * 1000, this.eventQueue)
    }

    componentDidMount() {
        let elem = document.getElementById("floatingMenu")
        EventManager.registerEvent("mousemove", elem, this.onMouseMove)
        EventManager.registerEvent("mousedown", elem, this.onMouseDown)
        EventManager.registerEvent("mouseup", elem, this.onMouseUp)
    }

    onMouseMove = (e) => {
        this.eventQueue.queueEvent("mousemove", () => {
            this.cursor = "default"
            if(!this.mousePos) {
                this.mousePos = { x: e.pageX, y: e.pageY }
            }

            if(this.dragAction) {
                this.dragAction(e.pageX - this.mousePos.x, e.pageY - this.mousePos.y)
            }

            if(this.dragging) {
                this.cursor = "grabbing"
            }
            else if (this.insideGrabPoint(e)) {
                this.cursor = "grab"
            } 

            document.body.style.cursor = this.cursor;
            document.getElementById('cursor-icon').style.display = "none"
            this.mousePos.x = e.pageX
            this.mousePos.y = e.pageY
        })
    }

    onMouseDown = (e) => {
        if(e.button == 0 && this.insideGrabPoint(e)) {
            this.dragging = true
            this.cursor = "grabbing"
            this.dragAction = (dx, dy) => {
                this.state.pos.x += dx
                this.state.pos.y += dy
                this.setState({pos: this.state.pos})
            }
        }
        document.body.style.cursor = this.cursor;
    }

    onMouseUp = (e) => {
        if(e.button == 0) {
            this.dragging = false
            this.dragAction = null
            if (this.insideGrabPoint(e)) {
                this.cursor = "grab"
            }
        }
        document.body.style.cursor = this.cursor;
    }

    resize = () => {
        this.eventQueue.queueEvent("resize", () => {
            this.setState({pos: {x: Math.min(window.innerWidth, this.state.pos.x), y: Math.min(window.innerHeight, this.state.pos.y)}})
        })
    }

    insideGrabPoint = (e) => {
        var elem = document.getElementById("grab")
        if(elem) {
            var dims = elem.getBoundingClientRect()
            return e.pageX >= dims.left && e.pageX <= dims.right && e.pageY >= dims.top && e.pageY <= dims.bottom
        }
        else {
            return false
        }
    }

    onFontFamilySelect = async (e) => {
        let element = this.props.elements[0]
        if(element) {
            element.fontIndex = e.target.value
            element.variantIndex = 0
            await updateText(element)
            this.props.onElementUpdate(element)
        }
    }

    onFontVariantSelect = async (e) => {
        let element = this.props.elements[0]
        if(element) {
            element.variantIndex = e.target.value
            await updateText(element)
            this.props.onElementUpdate(element)
        }
    }

    onTextChange = async (e) => {
        let element = this.props.elements[0]
        if(element) {
            element.text = e.target.value 
            await updateText(element)
            clearThumbnail(element)
            this.props.onElementUpdate(element)
        }
    }    

    onSelectShape = async (shape) => {
        let response = await fetch(`http://${window.location.host}/shapes/${shape}`)
        if(response.status == 200) {
            let svg = await response.text()
            let element = this.props.elements[0]
            element.rootNode = await parseSVG(svg)
            element.imageData = btoa(svg)
            element.shape = shape
            clearThumbnail(element)
            this.props.onElementUpdate(element)
        }
    }

    onCopy = async () => {
        let newElements = JSON.parse(JSON.stringify(this.props.elements, (key, value) => key == "rootNode" ? null : value))
        for(let i in newElements) {
            let element = newElements[i]
            element.id = uuidv4()
            element.rootNode = this.props.elements[i].rootNode
            element.translation.x += 5
            element.translation.y += 5
            element.thumbnail = null
            this.props.frame.elements.push(element)
        }
        this.props.onElementUpdate(newElements[0])
    }

    onDelete = () => {
        this.props.frame.elements = this.props.frame.elements.filter(x => !this.props.elements.includes(x))
        this.props.onElementUpdate(null)
    }

    onGroup = () => {

    }

    onReset = () => {
        let element = this.props.elements[0]
        element.translation = {x: 0, y: 0}
        element.scale = {x: 1, y: 1}
        element.rotation = 0
        this.props.onElementUpdate(element)
    }

    render = () => {
        return (
            <div className="floating-menu" id="floatingMenu" style={{"left": this.state.pos.x+"px", "top": this.state.pos.y+"px"}}>
                <div className="handle" id="grab">
                    <div className="close-cover">
                        <button onClick={this.props.onClose} className="close noselect">-</button>
                    </div>        
                    <div className="grab-point">
                        <i className="fa-solid fa-border-none"></i>
                    </div>
                </div>
                <div className="body">
                    {this.props.elements.length == 1 ?
                        (this.props.elements[0].type == "text" ? 
                            <div className="text-container">
                                <div className="text-parameters">
                                    <select onChange={this.onFontFamilySelect}>
                                        {Fonts.items.map((font, index) => 
                                            <option value={index} selected={index == this.props.elements[0].fontIndex}>{font.family}</option>
                                        )}
                                    </select>
                                    <select onChange={this.onFontVariantSelect}>
                                        {Fonts.items[this.props.elements[0].fontIndex].variants.map((variant, index) =>
                                            <option value={index} selected={index == this.props.elements[0].variantIndex}>{variant}</option>
                                        )}
                                    </select>
                                </div>
                                <textarea onChange={this.onTextChange} value={this.props.elements[0].text || ""}></textarea>
                            </div>
                        : (this.props.elements[0].type == "shape" ? 
                            <div className="shapes noselect">
                                {this.props.shapes.map(shape =>
                                    <button className="shape" onClick={() => this.onSelectShape(shape)}><img src={`./shapes/${shape}`}/></button>
                                )}
                            </div>
                        : (this.props.elements[0].type == "draw" ?
                            <p>Hold CTRL to draw</p>
                            : null
                        )))
                    : null}
                    <div className="actions">
                        {this.props.elements.length > 1 ? <button onClick={this.onGroup}><i className="fa-solid fa-object-group"></i></button> : null}
                        <button onClick={this.onReset}><i className="fa-solid fa-rotate-left"></i></button>
                        <button onClick={this.onCopy}><i className="fa-regular fa-copy"></i></button>
                        <button onClick={this.onDelete}><i className="fa-solid fa-trash-can"></i></button>
                    </div>
                </div>
            </div>
        )
    }
}

export default FloatingMenu