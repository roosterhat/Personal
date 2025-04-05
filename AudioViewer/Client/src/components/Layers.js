import React from 'react'
import { EventQueue, EventManager } from '../lib/Utility.js' 


class Layers extends React.Component {
    constructor(props) {
        super(props)

        this.cursor = "default"
        this.dragAction = null
        this.dragging = false
        this.mousePos = null
        this.eventQueue = new EventQueue() 
        this.layerHeight = 100
        this.layerPadding = 8

        this.state = {
            selectedLayer: null,
            pos: {x: 0, y: 0}
        }

        window.addEventListener("resize", this.resize)        

        setInterval(this.eventQueue.processEventQueue, (1/60) * 1000, this.eventQueue)

        props.setRerenderTrigger(() => this.forceUpdate())
        props.setUpdateTrigger(() => this.updateOrder())

        if(props.frame && props.frame.elements)
            props.frame.elements.sort((a,b) => a.order - b.order)
    }

    componentDidMount() {
        let elem = document.getElementById("layers")
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

            if(this.props.frame) {
                if(this.dragAction) {
                    this.dragAction(e.pageX - this.mousePos.x, e.pageY - this.mousePos.y)
                }
                if(this.dragging) {
                    this.cursor = "grabbing"
                }
                else {
                    for(let layer of this.props.frame.elements) {
                        if (this.insideGrabPoint(e, layer)) {
                            this.cursor = "grab"
                            break
                        }
                    }
                }             
            }

            document.body.style.cursor = this.cursor;
            document.getElementById('cursor-icon').style.display = "none"
            this.mousePos.x = e.pageX
            this.mousePos.y = e.pageY
        })
    }

    onMouseDown = (e) => {
        this.eventQueue.queueEvent("mousedown", () => {
            if(e.button == 0 && this.props.frame && !this.dragAction) {
                for(let layer of this.props.frame.elements) {
                    if(this.insideGrabPoint(e, layer)) {
                        this.dragging = true
                        this.cursor = "grabbing"
                        this.dragAction = (dx, dy) => {
                            this.state.pos.x += dx
                            this.state.pos.y += dy
                            this.setState({pos: this.state.pos})
                            this.updateOrder()
                            this.scrollView()
                        }
                        let dims = document.getElementById(layer.id).getBoundingClientRect()
                        this.setState({selectedLayer: layer, pos: {x: dims.left, y: dims.top}})
                        break
                    }
                }
            }
            document.body.style.cursor = this.cursor;
        })
    }

    onMouseUp = (e) => {
        if(e.button == 0 && this.props.frame) {
            this.dragging = false
            this.dragAction = null
            this.setState({selectedLayer: null})
            for(let layer of this.props.frame.elements) {
                if (this.insideGrabPoint(e, layer)) {
                    this.cursor = "grab"
                    break
                }
            }
        }
        document.body.style.cursor = this.cursor;
    }

    insideGrabPoint = (e, layer) => {
        var dims = document.getElementById(`grab-${layer.id}`).getBoundingClientRect()
        return e.pageX >= dims.left && e.pageX <= dims.right && e.pageY >= dims.top && e.pageY <= dims.bottom
    }

    updateOrder = () => {
        let container = document.getElementById("layers")
        let containerDims = container.getBoundingClientRect()
        let selectedElement = document.getElementById("floatinglayer")
        let updated = false

        if(selectedElement) {
            let selectedDims = selectedElement.getBoundingClientRect()
            let middle = selectedDims.top + (selectedDims.bottom - selectedDims.top) / 2            
            
            for(let layer of this.props.frame.elements) {
                if(layer.id != this.state.selectedLayer.id) {
                    let top = layer.order * this.layerHeight + (layer.order + 1) * this.layerPadding + containerDims.top - container.scrollTop;
                    if(top <= middle && top + this.layerHeight >= middle) {
                        if(this.state.selectedLayer.order > layer.order) {
                            this.state.selectedLayer.order = layer.order
                            layer.order += 0.1
                        }
                        else {
                            this.state.selectedLayer.order = layer.order + 0.1
                        }
                        updated = true
                        break
                    }
                }
            }
        }

        let count = 0
        for(let layer of this.props.frame.elements.toSorted((a,b) => a.order - b.order)) {
            layer.order = count++
        }

        if(updated) {
            this.props.onFrameUpdate(this.props.frame)
        }
    }

    scrollView = () => {
        let container = document.getElementById("layers")
        let containerDims = container.getBoundingClientRect()
        let selectedElement = document.getElementById("floatinglayer")
        let selectedDims = selectedElement.getBoundingClientRect()

        if(selectedDims.top < containerDims.top) {
            container.scroll({ top: container.scrollTop + (selectedDims.top - containerDims.top), behavior: "smooth" })
        }
        else if(selectedDims.bottom > containerDims.bottom) {
            container.scroll({ top: container.scrollTop + (selectedDims.bottom - containerDims.bottom), behavior: "smooth" })
        }
    }

    render() {
        return (
            <div className="layers" id="layers">
                {this.props.frame ? this.props.frame.elements.map(x => 
                    <div key={x.id} id={x.id} className="layer-container noselect" style={{top: x.order * this.layerHeight + (x.order + 1) * this.layerPadding}}>
                        <div className={"layer" + (this.props.selectedElement && this.props.selectedElement.id == x.id ? " selected" : "") + (this.state.selectedLayer && this.state.selectedLayer.id == x.id ? " hidden" : "")}>
                            <button style={{background: this.props.settings.backgroundColor}} onClick={() => this.props.onSetSelected(x)}>
                                <img src={x.thumbnail} />                                
                            </button>
                            <div className="handle" id={`grab-${x.id}`}>    
                                <div className="grab-point">
                                    <i className="fa-solid fa-border-none"></i>
                                </div>
                            </div>
                        </div>
                        {this.state.selectedLayer && this.state.selectedLayer.id == x.id ? 
                            <div className={"layer floating" + (this.props.selectedElement && this.props.selectedElement.id == this.state.selectedLayer.id ? " selected" : "")} id="floatinglayer"
                                style={{background: this.props.settings.backgroundColor, left: this.state.pos.x, top: this.state.pos.y}}>
                                <img src={this.state.selectedLayer.thumbnail} />
                                <div className="handle">    
                                    <div className="grab-point">
                                        <i className="fa-solid fa-border-none"></i>
                                    </div>
                                </div>
                            </div>
                        : null}
                    </div>
                ) : null}                
            </div>
        )
    }
}

export default Layers