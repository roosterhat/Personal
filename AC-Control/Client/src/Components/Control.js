import React from 'react'
import LoadingSpinner from './Spinners/loading1';
import { fetchWithToken, delay } from '../Utility';

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
            dragging: false,
            on: false,
            loadingSetState: false,
            loadingSensor: false,
            displayModes: false,
            displayError: false,
            errorMessage: null,
            hasSensor: false
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
                this.QueueEvent(event, () => this.onDrag(event))
            }
        }

        window.ontouchmove = event => {
            if(this.state.dragging){
                this.QueueEvent(event, () => this.onDrag({x: event.touches[0].clientX, y: event.touches[0].clientY}))
            }
        }

        window.onmouseup = event => {
            this.setState({dragging: false})
        }

        window.ontouchend = event => {
            this.setState({dragging: false})
        }

        const pointElement = document.getElementById("point")

        pointElement.addEventListener("mousedown", event => {
            this.setState({dragging: true})
        })

        pointElement.addEventListener("touchstart", event => {
            this.setState({dragging: true})
        })

        const controlElement = document.getElementById("control")

        controlElement.addEventListener("click", event => {
            this.setState({displayModes: false})
        })

        const modesElement = document.getElementById("modes")

        modesElement.addEventListener("click", event => {
            if(event.target.tagName !== "BUTTON")
                event.stopPropagation()
        })

        setInterval(this.ProcessEventQueue, 1 / 24 * 1000, this.eventQueue)
        setInterval(this.refreshSensor, 5000)
    }

    render = () => {
        return (
            <div className={"control " + (this.isOn() ? "on" : "off")} id="control">                
                {this.renderTemperatureRing()}
                {this.renderMenu()}
                {this.renderMacros()}
                <button className={"power" + (this.isOn() ? " on" : " off") + (this.state.loadingSetState ? " loading" : "")} onClick={this.togglePower}>
                    {this.state.loadingSetState ? <i className="fa-solid fa-arrows-rotate"></i> : <i className="fa-solid fa-power-off"></i>}
                </button>  
                <div className={"error" + (this.state.displayError ? "" : " disabled")}>{this.state.errorMessage}</div>
            </div>
        );
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
                    {this.isOn() && this.state.targetState ?
                        <div className="arc">
                            <svg viewBox="0 0 110 110">
                                <path d={`M 19.645 90.355
                                    A 50 50, 0, ${this.calculateArc()}`} fill="none" stroke="#0087ff" strokeWidth="6" strokeLinecap="round">
                                </path>
                            </svg>
                        </div>
                        : null 
                    }
                    <div className={"point " + (this.isOn() && this.state.targetState ? "" : "disabled")} id="point" style={this.state.pointCoordinates}></div>                  
                </div>
                <div className="temperature">
                    {this.isOn() || this.state.initalLoading ? (!this.state.initalLoading && this.state.targetState && this.state.targetState.ocr && !this.state.loadingState ? 
                         this.getTemperature()
                        : <LoadingSpinner id="spinner"/>) : null}
                    <div className="sensor">
                        <div>{this.getSensorTemperature()}</div>
                        <div>{this.getSensorHumidity()}</div>
                    </div>
                </div>
                <div className="mode-container">
                    <button className="mode" onClick={this.setDisplayModes}>
                        {this.isOn() ? (this.state.targetState && this.state.targetState.states && !this.state.loadingState ? 
                            this.state.targetState.states.find(x => x.active).name 
                            : <LoadingSpinner id="spinner"/>) : null}
                    </button>    
                    <div className={"modes" + (this.state.displayModes ? "" : " disabled")} id="modes">
                        {this.state.currentState && this.state.currentState.states ? (this.state.currentState.states.map(x => 
                            <button key={x.name} onClick={() => this.setMode(x)}>{x.name}</button>
                        )) : null}
                    </div>
                </div>
            </div>
        )
    }    

    setDisplayModes = () => {
        if(this.isOn())
            this.setState({displayModes: true})
    }

    onDrag = event => {
        const currentTemperature = this.getTemperature()
        const newTemperature = this.getClosestTemperature(event)
        if(currentTemperature != newTemperature) {
            this.state.targetState.ocr.find(x => x.name == "Temperature").value = newTemperature
            this.setState({targetStateChanged: true})
            this.update()
            this.setTemperature(newTemperature)
        }
    }

    isOn = () => {
        return this.state.targetState && this.state.targetState.power.active
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
        const temperature = this.boundTemperature(Math.round((this.Settings.maxTemperature - this.Settings.minTemperature) * percent + this.Settings.minTemperature))
        return temperature;
    }

    update = () => {
        this.calculatePointCoordinates()
    }

    getTemperature = () => {
        if(this.state.targetState && this.state.targetState.ocr)
            return this.boundTemperature(this.state.targetState.ocr.find(x => x.name == "Temperature").value)
        else
            return null
    }

    boundTemperature = (temperature) => {
        return Math.min(Math.max(temperature, this.Settings.minTemperature), this.Settings.maxTemperature)
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
                this.setState({currentState: json, targetState: json, hasSensor: json.temperature != null || json.humidity != null})
            }
        }
        finally {
            this.UpdateQueue.push(this.update)
            this.setState({loadingState: false, initalLoading: false, targetStateChanged: false});
        }
    }

    refreshSensor = async () => {
        if(!this.state.hasSensor || this.state.loadingSensor) return;
        try {
            this.setState({loadingSensor: true});
            var response = await fetchWithToken(`api/state/${this.Config.id}/basic`)
            if(response.status == 200){
                var json = await response.json()
                this.state.currentState.temperature = json.temperature
            }
        }
        finally {
            this.setState({loadingSensor: false})
        }
    }

    setTemperature = async (target) => {
        await delay(2000)
        if(target == this.getTemperature())
            await this.setTargetState(["ocr"])
    }

    setMode = async (mode) => {
        for(var state of this.state.targetState.states)
            state.active = state.name == mode.name
        this.setState({targetState: this.state.targetState, displayModes: false})
        await this.setTargetState(["states"])
    }

    togglePower = async () => {
        if(this.state.loadingState) return;
        this.state.targetState.power.active = !this.state.targetState.power.active
        await this.setTargetState(["power"])
    }

    setTargetState = async (types) => {
        if(this.state.loadingState) return;
        try{
            this.setState({loadingSetState: true})
            var temperature = this.state.targetState.ocr.find(x => x.name == "Temperature")
            var target = {
                power: this.state.targetState.power,
                ocr: !types || types.includes("ocr") ? [{id: temperature.id, buttons: temperature.buttons, name: temperature.name, target: temperature.value}] : [],
                states: !types || types.includes("states") ? [this.state.targetState.states.find(x => x.active)] : []
            }
            var body = JSON.stringify(target)
            var response = await fetchWithToken(`api/setstate/${this.Config.id}`, "POST", body, {"Content-Type": "application/json"})
            if(response.status == 200){
                this.setState({currentState: this.state.targetState})
            } 
            else {
                this.displayError(await response.text())
                await this.refreshState()
            }
        }
        catch(ex) {
            console.log(ex)
        }
        finally {
            this.setState({loadingSetState: false})
        }
    }

    getSensorTemperature = () => {
        if(!this.state.currentState || !this.state.currentState.humidity)
            return null
        else if(this.Settings.temperatureUnit == "C")
            return this.state.currentState.temperature + "ºC"
        else 
            return Math.round(this.state.currentState.temperature * (9 / 5) + 32) + "ºF"
    }

    getSensorHumidity = () => {
        if(this.state.currentState && this.state.currentState.humidity)
            return this.state.currentState.humidity + "%"
        else
            return null
    }

    displayError = async error => {
        this.setState({displayError: true, errorMessage: error})
        await delay(5000)
        this.setState({displayError: false})
    }
}

export default Control