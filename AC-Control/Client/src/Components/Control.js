import React from 'react'
import LoadingSpinner from './Spinners/loading1';
import Settings from './Settings';
import { fetchWithToken, delay, interpolateColors } from '../Utility';

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
        this.actionPromise = null;

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
            hasSensor: false,
            triggeringMacro: false
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
        window.addEventListener("resize", event => {
            this.QueueEvent(event, () => this.update())
        })

        window.addEventListener("mousemove", event => {
            if(this.state.dragging){
                this.QueueEvent(event, () => this.onDrag(event))
            }
        })

        window.addEventListener("touchmove", event => {
            if(this.state.dragging){
                this.QueueEvent(event, () => this.onDrag({x: event.touches[0].clientX, y: event.touches[0].clientY}))
            }
        })

        window.addEventListener("mouseup", event => {
            const dragging = this.state.dragging;
            this.state.dragging = false;
            if(dragging){
                this.QueueEvent(event, () => this.setTemperature(this.getTemperature()))
            }            
        })

        window.addEventListener("touchend", event => {
            const dragging = this.state.dragging;
            this.state.dragging = false;
            if(dragging){
                this.QueueEvent(event, () => this.setTemperature(this.getTemperature()))
            } 
        })

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
            <div className={"control " + (this.isOn() ? "on" : "off")} id="control" style={{background: this.getBackgroundColor()}}>                
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
        if(this.state.view == "settings") {
            return (<Settings Settings={this.state.EditSetting} Config={this.state.EditConfig} refresh={this.refreshState} complete={this.completeSettings} cancel={() => this.cancelEdit()} />)
        }
        else {
            return (
                <div className="btn-container vertical" id="normal">
                    <button className={"btn " + (this.state.loadingState ? "loading" : "")} onClick={this.refreshState}><i className="fa-solid fa-arrows-rotate"></i></button>                                 
                    <button className="btn" onClick={this.switchView}><i className="far fa-clone"></i></button>
                    { this.Settings ? <button className="btn" onClick={() => this.switchTo("settings")}><i className="fa-solid fa-gear"></i></button> : null }
                    <div className="spacer"></div>
                    { this.Settings && this.Config && this.Config.macros.length > 0 ? <button className={"btn macros " + (this.state.showMacros ? "retract" : "expand")} onClick={() => this.setState({showMacros: !this.state.showMacros})}><i className="fa-solid fa-angles-right"></i></button> : null }
                </div>
            )
        }
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
                    {this.isOn() && this.getTemperature() ?
                        <div className="arc">
                            <svg viewBox="0 0 110 110">
                                <path d={`M 19.645 90.355
                                    A 50 50, 0, ${this.calculateArc()}`} fill="none" stroke="#0087ff" strokeWidth="6" strokeLinecap="round">
                                </path>
                            </svg>
                        </div>
                        : null 
                    }
                    <div className={"point " + (this.isOn() && this.getTemperature() ? "" : "disabled")} id="point" style={this.state.pointCoordinates}></div>                  
                </div>
                <div className="temperature">
                    {this.isOn() || this.state.initalLoading ? (!this.state.loadingState && this.getTemperature() ? 
                         this.getTemperature()
                        : <LoadingSpinner id="spinner"/>) : null}
                    <div className="sensor">
                        <div>{this.getSensorTemperature()}</div>
                        <div>{this.getSensorHumidity()}</div>
                    </div>
                </div>
                <div className="mode-container">
                    <button className="mode" onClick={this.setDisplayModes}>
                        {this.isOn() ? (this.state.targetState && this.state.targetState.states && !this.state.loadingState && this.state.targetState.states.find(x => x.active) ? 
                            this.state.targetState.states.find(x => x.active).name 
                            : <LoadingSpinner id="spinner"/>) : null}
                    </button>    
                    <div className={"modes" + (this.state.displayModes ? "" : " disabled")} id="modes">
                        {this.state.currentState && this.state.currentState.states ? (this.state.currentState.states.map(x => 
                            <button className={(x.active ? "active" : "")} key={x.name} onClick={() => this.setMode(x)}><i className={x.properties.icon} style={{color: (this.Settings.useActiveColor ? x.properties.activeColor : "#757575")}}></i>{x.name}</button>
                        )) : null}
                    </div>
                </div>
            </div>
        )
    }    

    switchTo = (view) => {
        this.checkAuthorized(false)
        var settings = JSON.parse(JSON.stringify(this.Settings));
        var config = JSON.parse(JSON.stringify(this.Config));
        this.setState({view: view, EditConfig: config, EditSetting: settings})
    }

    completeSettings = async (settings) => {
        this.Settings = settings
        await this.saveSettings()
        await this.save()
        this.switchToMainView()
    }

    cancelEdit = () => {
        this.switchToMainView()
    }

    switchToMainView = () => {
        var menu = document.getElementById("menu");
        if(menu)
            menu.classList.add("closed")
        setTimeout(() => this.setState({view: "main", error: null, EditSetting: null, EditConfig: null}), 450)
    }

    getHeatIndex = () => {
        const T = this.getSensorTemperatureValue("F")
        const RH = this.getSensorHumidityValue()
        var HI_1 = -42.379 + 2.04901523*T + 10.14333127*RH - .22475541*T*RH - .00683783*T*T - .05481717*RH*RH + .00122874*T*T*RH + .00085282*T*RH*RH - .00000199*T*T*RH*RH
        const HI_2 = 0.5 * (T + 61.0 + ((T-68.0)*1.2) + (RH*0.094))
        const HI_A = (HI_1 + HI_2) / 2
        var HI = 0

        if(HI_A >= 80) {
            if(RH < 13 && T >= 80 && T <= 112)
                HI_1 += ((13-RH)/4) * Math.sqrt((17-Math.abs(T-95))/17)
            if(RH > 85 && T >= 80 && T <= 87)
                HI_1 += ((RH-85)/10) * [(87-T)/5]
            HI = HI_1
        }
        else {
            HI = HI_A        
        }

        if(this.Settings.temperatureUnit == "C")
            return Math.round((HI - 32) * (5 / 9))
        else 
            return HI 
    }

    getBackgroundColor = () => {
        if(this.Settings.useDynamicBackground) {
            var temperature = this.getSensorTemperatureValue()
            if(!temperature) return "#86c6ff"
            if(this.Settings.useHeatIndex) {
                temperature = this.getHeatIndex()
            }
            const percentage = Math.min(Math.max(temperature - 70, 0) / (85 - 70), 1)
            return interpolateColors(this.Settings.backgroundColors, percentage)//["#86c6ff", "#7bffb1", "#f77c7c"]
        }
        else {
            return "#86c6ff"
        }
    }

    setDisplayModes = () => {
        if(this.isOn())
            this.setState({displayModes: true})
    }

    onDrag = event => {
        const currentTemperature = this.getTemperature()
        const newTemperature = this.getClosestTemperature(event)
        if(currentTemperature != newTemperature) {
            navigator.vibrate(100)
            this.state.targetState.ocr.find(x => x.name == "Temperature").value = newTemperature
            this.update()            
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
        {
            var temperature = this.state.targetState.ocr.find(x => x.name == "Temperature").value
            if(temperature)
                return this.boundTemperature(temperature)
            else
                return null
        }
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
                this.setState({currentState: JSON.parse(JSON.stringify(json)), targetState: json, hasSensor: json.temperature != null || json.humidity != null})
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
        await delay(1000)
        const currentTemperature = this.state.currentState.ocr.find(x => x.name == "Temperature").value
        if(!this.state.dragging && target != currentTemperature && target == this.getTemperature())
            await this.setTargetState(["ocr"])
    }

    setMode = async (mode) => {
        navigator.vibrate(100)
        for(var state of this.state.targetState.states)
            state.active = state.name == mode.name
        this.setState({targetState: this.state.targetState, displayModes: false})
        await this.setTargetState(["states"])
    }

    togglePower = async () => {
        if(this.state.loadingState) return;
        navigator.vibrate(100)
        this.state.targetState.power.active = !this.state.targetState.power.active
        await this.setTargetState(["states"], this.state.targetState.power.active && !this.state.targetState.states.find(x => x.active))
    }

    setTargetState = async (types, forceRefresh = false) => {
        const lastAction = this.actionPromise;
        var action = null
        action = new Promise(async resolve => {
            try{
                if(lastAction)
                    await lastAction
                this.setState({loadingSetState: true})
                var temperature = this.state.targetState.ocr.find(x => x.name == "Temperature")
                var activeState = this.state.targetState.states.find(x => x.active)
                var target = {
                    power: this.state.targetState.power,
                    ocr: !types || types.includes("ocr") ? [{id: temperature.id, buttons: temperature.buttons, name: temperature.name, target: temperature.value}] : [],
                    states: !types || types.includes("states") && activeState ? [activeState] : []
                }
                var body = JSON.stringify(target)
                var response = await fetchWithToken(`api/setstate/${this.Config.id}`, "POST", body, {"Content-Type": "application/json"})
                if(response.status == 200){
                    if(forceRefresh)
                        await this.refreshState()
                    else
                        this.setState({currentState: JSON.parse(JSON.stringify(this.state.targetState))})
                } 
                else if(action == this.actionPromise) {
                    this.displayError(await response.text())
                    await this.refreshState()
                }
            }
            finally {
                this.setState({loadingSetState: false})
                resolve()
            }
        })        
        this.actionPromise = action;        
        await action     
    }

    triggerMacro = async (macro) => {
        if(this.state.triggeringMacro) return;

        try{
            this.setState({triggeringMacro: true})
            var body = JSON.stringify(macro.state)
            var response = await fetchWithToken(`api/setstate/${this.Config.id}`, "POST", body, {"Content-Type": "application/json"})
            if(response.status != 200){
                this.displayError(await response.text())
                this.setState({showMacros: false})
            }
            this.refreshState();
        }
        finally {
            this.setState({triggeringMacro: false})
        }
    }

    getSensorTemperatureValue = (unitOverride = null) => {
        const unit = unitOverride ? unitOverride : this.Settings.temperatureUnit
        if(!this.state.currentState || !this.state.currentState.humidity)
            return null
        else if(unit == "C")
            return this.state.currentState.temperature 
        else 
            return Math.round(this.state.currentState.temperature * (9 / 5) + 32)
    }

    getSensorTemperature = () => {
        var temperature = this.getSensorTemperatureValue()
        if(!temperature)
            return null
        else if(this.Settings.temperatureUnit == "C")
            return temperature + "ºC"
        else 
            return temperature + "ºF"
    }

    getSensorHumidityValue = () => {
        if(this.state.currentState && this.state.currentState.humidity)
            return this.state.currentState.humidity
    }

    getSensorHumidity = () => {
        var humidty = this.getSensorHumidityValue()
        if(!humidty)
            return null;
        else
            return humidty + "%"
    }

    displayError = async error => {
        this.setState({displayError: true, errorMessage: error})
        await delay(5000)
        this.setState({displayError: false})
    }

    saveSettings = async() => {
        try {
            const response = await fetchWithToken(`api/settings`, "POST", JSON.stringify(this.Settings), { "Content-Type": "application/json" });
        }
        catch(ex) {
            console.error(ex);
        }
    }

    save = async (name) => {
        if(name)
            this.state.EditConfig.name = name
        try{
            const response = await fetchWithToken(`api/save`, "POST", JSON.stringify(this.state.EditConfig), { "Content-Type": "application/json" });
            if(response.status == 200){
                this.Config = JSON.parse(JSON.stringify(this.state.EditConfig))
            }
        }
        catch(ex) {
            console.error(ex);
        }       
    }

    checkAuthorized = async(allowUnauthorized = true) => {
        try{
            const response = await fetchWithToken(`api/test/authorize`, "GET", null, {}, allowUnauthorized)
            return response.status == 200
        }
        catch {
            return false;
        }
    }
}

export default Control