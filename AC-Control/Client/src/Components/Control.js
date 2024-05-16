import React from 'react'
import LoadingSpinner from './Spinners/loading1';
import { fetchWithToken } from '../Utility';

class Control extends React.Component {
    constructor(props){
        super(props)
        this.Config = props.Config
        this.Settings = props.Settings
        this.switchView = props.onSwitchView
        this.UpdateQueue = []
        this.eventQueue = {
            queue: {},
            actionQueued: false
        }

        this.state = {
            targetState: null,
            currentState: null,
            targetStateChanged: false,
            showMacros: false,
            loadingState: false,
            initalLoading: true,
            pointCoordinates: null,
            dragging: false
        }        
        this.UpdateQueue.push(this.init)
        this.refreshState()
    }    

    componentDidUpdate () {
        while(this.UpdateQueue.length > 0)
            this.UpdateQueue.pop()();
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

    init = () => {
        window.onresize = event => {
            this.QueueEvent(event, () => this.update())
        };

        window.onmousemove = event => {
            if(this.state.dragging){
                this.QueueEvent(event, () => {
                    const currentTemperature = this.getTemperature()
                    const newTemperature = this.getClosestTemperature(event)
                    if(currentTemperature != newTemperature) {
                        this.state.targetState.ocr.find(x => x.name == "Temperature").value = newTemperature
                        this.setState({targetStateChanged: true})
                        this.update()
                    }
                })
            }
        }

        window.onmouseup = event => {
            this.setState({dragging: false})
        }

        const pointElement = document.getElementById("point")

        pointElement.addEventListener("mousedown", event => {
            this.setState({dragging: true})
        })

        setInterval(this.ProcessEventQueue, 1 / 24 * 1000, this.eventQueue)
    }

    render = () => {
        if(this.initalLoading) {
            return (
                <div className="loading-container">
                    <LoadingSpinner id="spinner"/>
                </div>
            );
        }
        else {
            return (
                <div className="control">
                    {this.renderTemperatureRing()}
                    <button></button>
                    {this.renderMenu()}
                    {this.renderMacros()}
                </div>
            );
        }
    }

    renderMacros = () => {
        return this.Config && this.Config.macros.length > 0 ? 
            <div className={"btn-container macros-container " + (this.state.showMacros ? "show" : "hidden")}>
                <div className="macros-inner-container">
                    {this.Config.macros.map(x => 
                        <button className="btn" onClick={() => this.triggerMacro(x)} key={x.name}>
                            {this.state.triggeringMacro ? 
                                <LoadingSpinner id="spinner"/>
                                : <i className={x.icon}></i>
                            }                                        
                        </button>    
                    )}
                </div>
            </div>
            : null
    }

    renderMenu = () => {
        return (
            <div className="btn-container vertical" id="normal">
                <button className={"btn " + (this.state.loadingState ? "loading" : "")} onClick={this.refreshState}><i className="fa-solid fa-arrows-rotate"></i></button>                
                <button className="btn" onClick={this.switchView}><i className="far fa-clone"></i></button> 
                <div className="spacer"></div>
                { this.Settings && this.Config && this.Config.macros.length > 0 ? <button className={"btn macros " + (this.state.showMacros ? "retract" : "expand")} onClick={() => this.setState({showMacros: !this.state.showMacros})}><i className="fa-solid fa-angles-right"></i></button> : null }
            </div>
        )
    }

    renderTemperatureRing = () => {
        return (
            <div className="dial-container">
                <div className="dial">
                    <div className="track" id="track">
                        <svg viewBox="0 0 110 110">
                            <path d="M 19.645 90.355
                                A 50 50, 0, 1, 1, 90.355 90.355" fill="none" stroke="#eee" strokeWidth="5" strokeLinecap="round">
                            </path>
                        </svg>
                    </div>
                    {this.state.targetState ?
                        <div className="arc">
                            <svg viewBox="0 0 110 110">
                                <path d={`M 19.645 90.355
                                    A 50 50, 0, ${this.calculateArc()}`} fill="none" stroke="#0087ff" strokeWidth="6" strokeLinecap="round">
                                </path>
                            </svg>
                        </div>
                        : null 
                    }
                    <div className={"point " + (this.state.targetState ? "" : "disabled")} id="point" style={this.state.pointCoordinates}></div>
                </div>
                <div className="temperature">
                    {this.state.targetState && this.state.targetState.ocr && !this.state.loadingState ? 
                         this.getTemperature()
                        : <LoadingSpinner id="spinner"/>}
                </div>
                <button className="mode">
                    {this.state.targetState && this.state.targetState.states && !this.state.loadingState ? 
                        this.state.targetState.states.find(x => x.active).name 
                        : <LoadingSpinner id="spinner"/>}
                </button>
            </div>
        )
    }

    getClosestTemperature = (coordinates) => {
        const trackElement = document.getElementById("track")
        if(!trackElement)
            return

        const trackDims = trackElement.getBoundingClientRect()
        const dx = coordinates.x - (trackDims.x + trackDims.width / 2)
        const dy = coordinates.y - (trackDims.y + trackDims.height / 2)
        const angle = Math.atan(dy / dx) * (180 / Math.PI) + (dx > 0 ? 180 : 0)
        const percent = (angle + 45) / 270
        const temperature = Math.min(Math.max(Math.round((this.Settings.maxTemperature - this.Settings.minTemperature) * percent + this.Settings.minTemperature), this.Settings.minTemperature), this.Settings.maxTemperature)
        return temperature;
    }

    update = () => {
        this.calculatePointCoordinates()
    }

    getTemperature = () => {
        if(this.state.targetState && this.state.targetState.ocr)
            return this.state.targetState.ocr.find(x => x.name == "Temperature").value
        else
            return null
    }

    calculatePointCoordinates = () => {
        const trackElement = document.getElementById("track")
        const pointElement = document.getElementById("point")
        if(!trackElement || !pointElement)
            return

        const trackDims = trackElement.getBoundingClientRect()
        const pointDims = pointElement.getBoundingClientRect()
        const temperature = this.getTemperature();
        const temperaturePercent = (temperature - this.Settings.minTemperature) / (this.Settings.maxTemperature - this.Settings.minTemperature)    
        const angle = (270 * temperaturePercent - 135) * (Math.PI / 180)
        const x = (trackDims.width / 2) + Math.sin(angle) * (50 / 55 * trackDims.width / 2) - (pointDims.width / 2)
        const y = (trackDims.height / 2) - Math.cos(angle) * (50 / 55 * trackDims.height / 2) - (pointDims.height / 2)
        this.setState({pointCoordinates: { top: `${y}px`, left: `${x}px` }})
    }

    calculateArc = () => {
        const temperature = this.getTemperature();
        const temperaturePercent = (temperature - this.Settings.minTemperature) / (this.Settings.maxTemperature - this.Settings.minTemperature)    
        const angle = (270 * temperaturePercent - 135) * (Math.PI / 180)
        const x = 55 + Math.sin(angle) * 50
        const y = 55 - Math.cos(angle) * 50
        return `${angle > Math.PI / 4 ? 1 : 0}, 1, ${x} ${y}`
    }

    refreshState = async () => {
        if(this.state.loadingState) return;
        try {
            this.setState({loadingState: true});
            var response = await fetchWithToken(`api/state/${this.Config.id}`)
            if(response.status == 200){
                var json = await response.json()
                this.setState({currentState: json, targetState: json})
            }
        }
        finally {
            this.UpdateQueue.push(this.update)
            this.setState({loadingState: false, initalLoading: false, targetStateChanged: false});
        }
    }
}

export default Control