import React from 'react'
import Menu from './Menu';
import LoadingSpinner from './Spinners/loading1';
import { fetchWithToken, parseTime, delay } from '../Utility';

class Settings extends React.Component {
    constructor(props) {
        super(props);

        this.UpdateQueue = []

        this.state = {
            settings: props.Settings,
            config: props.Config,
            currentState: null,
            debugState: null,
            debugSetState: null,
            debugOCR: null,
            OCRDebugResult: null,
            selectedState: null,
            scheduleRuns: null,
            selectedOCR: null,
            targetState: {"power": {"active": true}, "states": [], "ocr": []},
            loadStateDebug: false,
            loadSetStateDebug: false,
            loadOCRDebug: false,
            loadingScheduleRuns: true,
            rebooting: false,
            saving: false,
            systemStartTime: null,
            serviceStartTime: null,
            systemUptime: null,
            serviceUptime: null,
            events: null,
            loadingEvents: false
        }

        setInterval(this.updateUptime, 1000)
        setTimeout(() => {
            this.getScheduleRuns()
            this.getState()
            this.getEvents()
        }, 10)
    }

    componentDidUpdate () {
        while(this.UpdateQueue.length > 0)
            this.UpdateQueue.pop()();
    }

    render = () => {
        return (
            <Menu>
                <div className="edit">
                    <div className="btn-container editing">
                        <div className="title">Settings</div>
                        <div className='divider'></div>
                        <button className='btn cancel' onClick={this.props.cancel}><i className="fa-solid fa-xmark"></i></button>
                        <button className="btn confirm" onClick={this.complete}>{this.state.saving ? <LoadingSpinner /> : <i className="fa-solid fa-check"></i>}</button>
                    </div>
                    <div className="settings">
                        <div className="setting">
                            <div className="setting-title">Camera Index</div>
                            <input type="number" min="0" value={this.state.settings["cameraIndex"]} onChange={e => this.updateSettings("cameraIndex", Number(e.target.value))}/>
                        </div>
                        <div className="setting">
                            <div className="setting-title">Camera Exposure Setting</div>
                            <input type="number" max="0" min="-13" value={this.state.settings["cameraExposure"]} onChange={e => this.updateSettings("cameraExposure", Number(e.target.value))}/>
                        </div>
                        <div className="setting">
                            <div className="setting-title">Frame Refresh Delay (ms)</div>
                            <input type="number" min="0" value={this.state.settings["frameRefreshDelay"]} onChange={e => this.updateSettings("frameRefreshDelay", Number(e.target.value))}/>
                        </div>
                        <div className="setting">
                            <div className="setting-title">IR Trigger Attempts</div>
                            <input type="number" min="1" value={this.state.settings["triggerAttempts"]} onChange={e => this.updateSettings("triggerAttempts", Number(e.target.value))}/>
                        </div>
                        <div className="setting">
                            <div className="setting-title">Set State Delay (ms)</div>
                            <input type="number" min="0" value={this.state.settings["setStateDelay"]} onChange={e => this.updateSettings("setStateDelay", Number(e.target.value))}/>
                        </div>
                        <div className="setting">
                            <div className="setting-title">Temperature</div>
                            <div className="setting-body">
                                <div>
                                    <div>Min</div>
                                    <input type="number" min="0" max={this.state.settings.maxTemperature} value={this.state.settings["minTemperature"]} onChange={e => this.updateSettings("minTemperature", Number(e.target.value))}/>
                                </div>
                                <div>
                                    <div>Max</div>
                                    <input type="number" min={this.state.settings.minTemperature} max="100" value={this.state.settings["maxTemperature"]} onChange={e => this.updateSettings("maxTemperature", Number(e.target.value))}/>
                                </div>
                            </div>
                        </div>
                        <div className="setting">
                            <div className="setting-title">Temperature Unit</div>
                            <button onClick={e => this.updateSettings("temperatureUnit", this.state.settings.temperatureUnit == "C" ? "F" : "C")}>{"º"+this.state.settings.temperatureUnit}</button>
                        </div>
                        <div className="setting">
                            <div className="setting-title">Max OCR Value Change</div>
                            <input type="number" min="1" value={this.state.settings["maxOCRValueChange"]} onChange={e => this.updateSettings("maxOCRValueChange", Number(e.target.value))}/>
                        </div>    
                        <div className="setting-group">
                            <div className="setting">
                                <div className="setting-title">Use Dynamic Background Color</div>
                                <label class="switch">
                                    <input type="checkbox" onChange={e => this.updateSettings("useDynamicBackground", !this.state.settings.useDynamicBackground)} checked={this.state.settings.useDynamicBackground}/>
                                    <span class="slider round"></span>
                                </label>
                            </div>
                            {this.state.settings.useDynamicBackground ? 
                                [
                                    this.renderBackgroundColorSelector(),
                                    <div className="setting">
                                        <div className="setting-title">Use Heat Index</div>
                                        <label class="switch">
                                            <input type="checkbox" onChange={e => this.updateSettings("useHeatIndex", !this.state.settings.useHeatIndex)} checked={this.state.settings.useHeatIndex}/>
                                            <span class="slider round"></span>
                                        </label>
                                    </div>   
                                ]
                                : null }
                        </div>     
                        {this.renderDebugState()}
                        {this.renderDebugSetState()}
                        {this.renderDebugOCR()}
                        {this.renderEvents()}
                        {this.renderScheduleRuns()}
                        <div className='system-control'>
                            <div style={{width: "100%"}}>
                                <div>Service: {this.state.serviceUptime ? this.state.serviceUptime : "-"}</div>
                                <div>System: {this.state.systemUptime ? this.state.systemUptime : "-"}</div>
                            </div>
                            Restart
                            <button className='restart' onClick={this.restart}>{this.state.rebooting ? <LoadingSpinner id="spinner" /> : <i class="fa-solid fa-arrows-rotate"></i>}</button>
                            Reboot
                            <button className='reboot' onClick={this.reboot}>{this.state.rebooting ? <LoadingSpinner id="spinner" /> : <i class="fa-solid fa-power-off"></i>}</button>
                        </div>
                    </div>
                </div>
            </Menu>
        )
    }

    renderEvents = () => {
        return (
            <div className="setting">
                <div className="setting-title">History
                <div className="refresh-container"><div className={"refresh" + (this.state.loadingEvents ? " loading" : "")} onClick={this.getEvents}><i className="fa-solid fa-arrows-rotate"></i></div></div></div>
                <div id="plot"></div>                
            </div>
        )
    }

    renderBackgroundColorSelector = () => {
        var pairs = []
        for(var i = 0; i < this.state.settings.backgroundColors.length; i++) {
            pairs.push({c1: this.state.settings.backgroundColors[i], c2: (i + 1) < this.state.settings.backgroundColors.length ? this.state.settings.backgroundColors[i + 1] : null})
        }
        return (
            <div className="setting">
                <div className="setting-title">Dynamic Background Colors</div>
                <div className="color-track">
                    {pairs.map((pair, i) => 
                        <div className={"color-container" + (pair.c2 ? "" : " end")}>
                            <div className="color-selector-container">
                                <input className="color-selector" type="color" value={pair.c1} onInput={e => this.updateColor(i, e.target.value)}/>
                                {pairs.length > 2 ? <button className="remove-color" onClick={() => this.removeColor(i)}><i className="fa-solid fa-xmark"></i></button> : null}
                            </div>
                            {pair.c2 ? 
                                <div className="color-gradient" style={{background: `linear-gradient(to right, ${pair.c1}, ${pair.c2})`}}>
                                    <label className="add-color" for={"color" + i} onClick={() => this.addColor(i)}><i className="fa-solid fa-plus"></i></label>
                                    <input id={"color" + i} type="color" value={pair.c1} onInput={e => this.updateColor(i + 1, e.target.value)}/>
                                </div> 
                                : null}
                        </div>
                    )}
                </div>
                <div className="range-input">
                    <input type="number" max={this.state.settings.backgroundRange.high} value={this.state.settings.backgroundRange.low} onInput={e => this.updateSettings("backgroundRange", {low: e.target.value, high: Number(this.state.settings.backgroundRange.high)})}/>
                    <input type="number" min={this.state.settings.backgroundRange.low} value={this.state.settings.backgroundRange.high} onInput={e => this.updateSettings("backgroundRange", {low: Number(this.state.settings.backgroundRange.low), high: e.target.value})}/>
                </div>
                <div style={{display: "flex", justifyContent: "end"}}>
                    <button onClick={() => { this.updateSettings("backgroundColors", ["#86c6ff", "#7bffb1", "#f77c7c"]); this.updateSettings("backgroundRange", {low: 70, height: 85});}}>Default</button>
                </div>
            </div>
        )
    }

    removeColor = index => {
        this.state.settings.backgroundColors.splice(index, 1)
        this.setState({settings: this.state.settings})
    }

    updateColor = (index, color) => {
        console.log(color)
        this.state.settings.backgroundColors.splice(index, 1, color)
        this.setState({settings: this.state.settings})
    }

    addColor = (index) => {
        const color = this.state.settings.backgroundColors[index]
        this.state.settings.backgroundColors.splice(index, 0, color)
        this.setState({settings: this.state.settings})
    }

    renderScheduleRuns = () => {
        if(this.state.config){
            return (
                <div className="setting">
                    <div className="setting-title schedule-run">Schedule Runs 
                        <div className="refresh-container"><div className={"refresh" + (this.state.loadingScheduleRuns ? " loading" : "")} onClick={this.getScheduleRuns}><i className="fa-solid fa-arrows-rotate"></i></div></div>
                    </div>
                    <div className="schedule-runs">
                        <table>
                            <thead><tr><th>Schedule</th><th className="date">Last Run</th><th className="date">Last Attempt</th><th>Duration</th><th>Error</th></tr></thead>
                            <tbody>
                                {this.state.loadingScheduleRuns ? <LoadingSpinner id="spinner" /> :
                                    this.state.config.schedules.map(schedule => 
                                        <tr className="run" key={schedule.id}>
                                            <td className="name">{schedule.name}</td>
                                            <td>
                                                {schedule.id in this.state.scheduleRuns ? 
                                                    new Date(this.state.scheduleRuns[schedule.id].lastRun).toLocaleDateString("en-US", {year: "numeric", month: "numeric", day: "numeric", hour: "2-digit", minute: "2-digit"}) 
                                                    : ""
                                                }
                                            </td>
                                            <td>
                                                {schedule.id in this.state.scheduleRuns ? 
                                                    new Date(this.state.scheduleRuns[schedule.id].lastAttempt).toLocaleDateString("en-US", {year: "numeric", month: "numeric", day: "numeric", hour: "2-digit", minute: "2-digit"}) 
                                                    : ""
                                                }
                                            </td>
                                            <td>
                                                {schedule.id in this.state.scheduleRuns && this.state.scheduleRuns[schedule.id].duration ? 
                                                    parseTime(this.state.scheduleRuns[schedule.id].duration).toLocaleTimeString("en-US", {minute: "2-digit", second: "2-digit", fractionalSecondDigits: 3}) 
                                                    : ""
                                                }
                                            </td>
                                            <td>
                                                {schedule.id in this.state.scheduleRuns ? this.state.scheduleRuns[schedule.id].error  : ""}
                                            </td>
                                        </tr>    
                                    )                        
                                }
                            </tbody>
                        </table>
                    </div>
                </div>
            )
        }
        else {
            return null
        }
    }

    renderDebugOCR = () => {
        if(this.state.config){
            return (
                <div className="setting">
                    <div className="setting-title">Debug OCR</div>
                    <div className="debug-container">
                        <div className="debug-header">
                            <select name="buttons" id="ocr-select" onChange={() => this.setState({OCRDebugResult: null})}>
                                {this.state.config.actions.ocr.map(x => <option key={x.id} value={x.id}>{x.name}</option>)}
                            </select>
                            <div>
                                <button className="debug" onClick={() => this.debugOCR()}>{this.state.loadOCRDebug ? <LoadingSpinner id="spinner" /> : "Debug"}</button>
                                <button onClick={() => this.setState({OCRDebugResult: null})}>Clear</button>
                            </div>
                        </div>
                        {this.state.OCRDebugResult ? 
                            <div>
                                <div className="debug-canvas-container">
                                    <div className="canvas-container">
                                        <div>{`Value (${this.state.OCRDebugResult.value}) - ${this.state.OCRDebugResult.executionTime}ms`}</div>
                                        <canvas id="debug-canvas-ocr"></canvas>
                                    </div>                               
                                </div>
                                <div className="debug-properties-container">
                                    <div>
                                        <div className="debug-property">
                                            <div>Gray Scale</div>
                                            <input type="checkbox"
                                                checked={this.state.selectedOCR.view.properties.grayscale}
                                                onChange={e => this.updateOCRProperty("grayscale", !this.state.selectedOCR.view.properties.grayscale)}/>
                                        </div>
                                        <div className="debug-property">
                                            <div>Invert</div>
                                            <input type="checkbox"
                                                checked={this.state.selectedOCR.view.properties.invert}
                                                onChange={e => this.updateOCRProperty("invert", !this.state.selectedOCR.view.properties.invert)}/>
                                        </div>
                                    </div>
                                    <button className="debug-test" onClick={() => this.debugOCR()}>{this.state.loadOCRDebug ? <LoadingSpinner id="spinner" /> : "Test"}</button>
                                </div>
                            </div>
                            : null
                        }
                    </div>
                </div>
            )
        }
        else {
            return null
        }
    }

    renderDebugSetState = () => {
        if(this.state.config) {
            return (
                <div className="setting">
                    <div className="setting-title">Debug Set State</div>
                    <div className="debug-container">
                        <div className="debug-properties-container debug-settings">
                            <div className="power-state">
                                <div className="name">Power</div>
                                <button className={"state " + (this.state.targetState.power.active ? "on" : "off")} onClick={() => this.toggleState(this.state.targetState.power)}>
                                    {this.state.targetState.power.active ? "On" : "Off"}
                                </button>
                            </div>
                            {this.state.targetState.power.active ? 
                                <div className="ocr-state">
                                    <select onChange={e => this.setCurrentOCR(e.target.value)}>
                                        <option value={null}></option>
                                        {this.state.config.actions.ocr.map(o =>
                                            <option value={o.id} selected={this.state.targetState.ocr.some(x => x.id == o.id)} key={o.id}>{o.name}</option>
                                        )}
                                    </select>
                                    {this.state.targetState.ocr.length > 0 ?
                                        <input type="number" value={this.state.targetState.ocr[0].target} 
                                            onChange={e => this.updateOCRTarget(Number(e.target.value))}/>
                                        : null
                                    }                                
                                </div>
                                : null
                            }
                            {this.state.targetState.power.active ? 
                                <div className="state-groups">
                                    {this.state.config.actions.stateGroups.map(group => 
                                        <div className="state-group" key={group.id}>
                                            <div className="name">{group.name}</div>
                                            <select onChange={e => this.toggleGroupState(group, e.target.value)}>
                                                <option value={null}></option>
                                                {group.states.map(s =>
                                                    <option value={s.id} selected={this.state.targetState.states.some(x => x.groupId == group.id && x.id == s.id)} key={s.id}>{s.name}</option>
                                                )}
                                            </select>
                                        </div>
                                    )}
                                </div>
                                : null
                            }
                        </div>
                        <div className="debug-properties-container">
                            <div>
                                <div className="debug-property">
                                    <div>IR Trigger Attempts</div>
                                    <input type="number" min="0" value={this.state.settings["triggerAttempts"]} onChange={e => this.updateSettings("triggerAttempts", Number(e.target.value))}/>
                                </div>
                                <div className="debug-property">
                                    <div>Set State Delay (ms)</div>
                                    <input type="number" min="0" value={this.state.settings["setStateDelay"]} onChange={e => this.updateSettings("setStateDelay", Number(e.target.value))}/>
                                </div>
                            </div>
                            <button className="debug-test" onClick={this.debugSetState}>{this.state.loadSetStateDebug ? <LoadingSpinner id="spinner" /> : "Test"}</button>
                        </div>
                        {this.state.debugSetState ?
                            <div className={"debug-result " + (this.state.debugSetState.success ? "success" : "error")}>
                                {(this.state.debugSetState.success ? "Success" : ("Error: " + this.state.debugSetState.error))}
                            </div>
                            : null
                        }
                    </div>
                </div>
            )
        }
        else {
            return null;
        }
    }

    renderDebugState = () => {
        if(this.state.config) {
            return (
                <div className="setting">
                    <div className="setting-title">Debug State</div>
                    <div className="debug-container">
                        <div className="debug-header">
                            <select name="buttons" id="toggle-select" onChange={() => this.setState({debugState: null})}>
                                {this.state.config.actions.stateGroups.map(x => <option key={x.id} value={x.id}>{x.name}</option>)}
                            </select>
                            <div>
                                <button className="debug" onClick={() => this.debugState()}>{this.state.loadStateDebug ? <LoadingSpinner id="spinner" /> : "Debug"}</button>
                                <button onClick={() => this.setState({debugState: null})}>Clear</button>
                            </div>
                        </div>
                        {this.state.debugState ? 
                            <div>
                                <div className="debug-state-results">
                                    <div className="debug-canvas-container">
                                        <div className="canvas-container">
                                            <div>{`View - ${this.state.debugState.executionTime}ms`}</div>
                                            <div className="debugstate-statecanvas-container">
                                                <div><canvas id="debug-canvas-state"></canvas></div>                                                
                                                <div className="state-container">
                                                    { 
                                                        this.state.debugState.states.map(x => 
                                                            <div className="state" key={x.name}>
                                                                <div className="active-color" style={{background: x.active ? x.properties.activeColor : "#505050"}}></div>
                                                                <div className="name">{x.name}</div>
                                                            </div>
                                                        )
                                                    }
                                                </div>
                                            </div>
                                        </div>                                 
                                    </div>                                    
                                </div>
                                <div className="debug-properties-container">
                                    <div />
                                    <button className="debug-test" onClick={() => this.debugState()}>{this.state.loadStateDebug ? <LoadingSpinner id="spinner" /> : "Test"}</button>
                                </div>
                            </div>
                            : null
                        }
                    </div>                            
                </div>
            );
        }
        else 
            return null;
    }

    debugOCR = async () => {
        try{
            this.setState({loadOCRDebug: true})
            var elem = document.getElementById("ocr-select")
            if(elem){
                var id = elem.value;
                if(id){
                    var action = this.state.config.actions.ocr.find(x => x.id == id)
                    var ocr = this.state.config.frame.ocr.find(x => x.id == action.view.id);
                    var body = JSON.stringify({"view": ocr, "action": action})
                    var start = Date.now()
                    var response = await fetchWithToken(`api/debug/ocr/${this.state.config.id}`, "POST", body, {"Content-Type": "application/json"})
                    if(response.status == 200){
                        var end = Date.now()
                        var result = await response.json()
                        result["executionTime"] = end - start
                        this.setState({OCRDebugResult: result, selectedOCR: action})
                        this.UpdateQueue.push(() => {
                            this.addImageToCanvas("debug-canvas-ocr", result["image"], false);
                        })
                    }
                }                    
            }            
        }
        finally {
            this.setState({loadOCRDebug: false})
        }
    }

    updateOCRProperty = (key, value) => {
        this.state.selectedOCR.view.properties[key] = value
        this.setState({config: this.state.config});
    }

    getScheduleRuns = async () => {
        try{
            this.setState({loadingScheduleRuns: true})
            var response = await fetchWithToken("api/scheduleruns")
            if(response.status == 200){
                this.setState({scheduleRuns: await response.json()})
            }
        }
        finally {
            this.setState({loadingScheduleRuns: false})
        }
    }

    getState = async () => {
        try {
            this.setState({loadingState: true});
            var response = await fetchWithToken(`api/state/${this.state.config.id}/basic`)
            if(response.status == 200){
                this.setState({currentState: await response.json()})
            }
        }
        finally {
            this.setState({loadingState: false});
        }
    }

    updateSettings = (key, value) => {
        this.state.settings[key] = value;
        this.setState({settings: this.state.settings})
    }

    complete = () => {
        this.setState({saving: true})
        this.props.complete(this.state.settings)
    }

    debugState = async (config) => {
        try{
            this.setState({loadStateDebug: true})
            var result = null
            var start = Date.now()
            if(config) {
                var body = JSON.stringify(this.state.selectedState)
                var response = await fetchWithToken(`api/debug/state/${this.state.config.id}/${this.state.selectedState.id}`, "POST", body, {"Content-Type": "application/json"})
                if(response.status == 200){
                    var end = Date.now()
                    result = await response.json()
                    result["executionTime"] = end - start
                    this.setState({debugState: result})
                }                        
            }
            else {
                var elem = document.getElementById("toggle-select")
                if(elem){
                    var id = elem.value;
                    if(id){
                        this.setState({selectedState: this.state.config.frame.states.find(x => x.id == id)})
                        var response = await fetchWithToken(`api/debug/state/${this.state.config.id}/${id}`)
                        if(response.status == 200){
                            var end = Date.now()
                            result = await response.json()
                            result["executionTime"] = end - start
                            this.setState({debugState: result})
                        }
                    }                    
                }
            }

            if(result){
                this.UpdateQueue.push(() => {
                    this.addImageToCanvas("debug-canvas-state", result["output"], false);
                })
            }
        }
        finally {
            this.setState({loadStateDebug: false})
        }
    }

    debugSetState = async () => {
        try{
            this.setState({loadSetStateDebug: true})
            var body = JSON.stringify({"state": this.state.targetState, "settings": this.state.settings})
            var response = await fetchWithToken(`api/debug/setstate/${this.state.config.id}`, "POST", body, {"Content-Type": "application/json"})
            if(response.status == 200){
                this.setState({debugSetState: await response.json()})
                this.props.refresh()
            } 
            else {
                this.setState({debugSetState: {"success": false, "error": "unable to trigger Set State: " + await response.text()}})
            }
        }
        catch(ex) {
            this.setState({debugSetState: {"success": false, "error": "unable to trigger Set State: " + ex}})
        }
        finally {
            this.setState({loadSetStateDebug: false})
        }
    }

    toggleGroupState = (group, id) => {
        var oldState = this.state.targetState.states.find(x => x.groupId == group.id)
        if(oldState){
            var index = this.state.targetState.states.indexOf(oldState)
            this.state.targetState.states.splice(index, 1)
        }
        if(id){
            this.state.targetState.states.push({
                "id": id,
                "groupId": group.id,
                "active": true
            })
        }
    }

    setCurrentOCR = (id) => {
        var action = this.state.config.actions.ocr.find(x => x.id == id)
        this.state.targetState.ocr = []
        if(action)
            this.state.targetState.ocr.push(action)
        this.setState({config: this.state.config})
    }

    updateOCRTarget = (target) => {
        this.state.targetState.ocr[0].target = target;        
        this.setState({config: this.state.config})
    }

    toggleState = (state) => {
        state.active = !state.active
        this.setState({config: this.state.config})
    }

    addImageToCanvas = (id, data, bmp) => {
        var canvas = document.getElementById(id);
        var dims = canvas.getBoundingClientRect();
        canvas.width = dims.width;
        canvas.height = dims.height;
        var context = canvas.getContext("2d");
        context.clearRect(0, 0, canvas.width, canvas.height);

        var scale = Math.min(context.canvas.width / data[0].length, context.canvas.height / data.length);
        var result = context.createImageData(data[0].length * scale, data.length * scale);      
        for (var row = 0; row < result.height; row++) {
            for (var col = 0; col < result.width; col++) {
                var index = (row * result.width + col) * 4;
                var srcY = Math.floor(row / scale);
                var srcX = Math.floor(col / scale);
                if(bmp)
                    result.data.set(Array(3).fill(data[srcY][srcX]), index);
                else
                    result.data.set(data[srcY][srcX], index);
                result.data[index + 3] = 255
            }
        }
        context.putImageData(result, (context.canvas.width - result.width) / 2, (context.canvas.height - result.height) / 2);
    }

    updateStateProperty = (key, value) => {
        this.state.selectedState.properties[key] = value;
        this.setState({selectedState: this.state.selectedState});
    }

    reboot = async () => {
        if(this.state.rebooting) return;
        try {
            this.setState({rebooting: true})
            var response = await fetchWithToken('api/reboot')
            if(response.status == 200){
                await delay(5000)
                while(true){
                    try{
                        await fetchWithToken('api/test/authorize')
                    }
                    catch(ex) {}
                }
            } 
        }
        finally {
            this.setState({rebooting: false})
        }
    }

    restart = async () => {
        if(this.state.rebooting) return;
        try {
            this.setState({rebooting: true})
            var response = await fetchWithToken('api/restart')
            if(response.status == 200){
                await delay(5000)
                while(true){
                    try{
                        response = await fetchWithToken('api/test/authorize')
                        if(response.status == 200)
                            return;
                    }
                    catch(ex) {}
                }
            } 
        }
        finally {
            this.setState({rebooting: false})
        }
    }

    updateUptime = () => {
        if(this.state.currentState) {
            if(!this.state.systemStartTime || !this.state.serviceStartTime){
                this.setState({systemStartTime: new Date(this.state.currentState["systemStartTime"])})
                this.setState({serviceStartTime: new Date(this.state.currentState["serviceStartTime"])})
            }

            if(this.state.systemStartTime && this.state.serviceStartTime) {
                this.setState({systemUptime: this.getUptime(this.state.systemStartTime)})
                this.setState({serviceUptime: this.getUptime(this.state.serviceStartTime)})
            }
        }
    }

    getUptime = (date) => {
        const time = Date.now() - date.getTime()
        const total_s = Math.floor(time / 1000) 
        const total_m = Math.floor(total_s / 60)
        const total_h = Math.floor(total_m / 60)
        const total_d = Math.floor(total_h / 24)
        return `${String(total_d).padStart(2, "0")}:${String(total_h % 24).padStart(2, "0")}:${String(total_m % 60).padStart(2, "0")}:${String(total_s % 60).padStart(2, "0")}`
    }

    convertTemperature = (temp) => {
        if(this.state.settings.temperatureUnit == "C")
            return temp 
        else 
            return Math.round(temp * (9 / 5) + 32)
    }

    getEvents = async () => {
        try{
            this.setState({loadingEvents: true})
            var response = await fetchWithToken("api/events")
            if(response.status == 200) {
                const data = await response.json()
                const events = {
                    temperature: { x: [], y: [] },
                    humidity: { x: [], y: [] },
                    states: []
                }

                let stateDiff, currentState = { ocr: [], states: [] }

                for(var event of data) {
                    if(event.type == "sensor") {
                        events.temperature.x.push(event.time)
                        events.temperature.y.push(this.convertTemperature(event.value.temperature))
                        events.humidity.x.push(event.time)
                        events.humidity.y.push(event.value.humidity)
                    }
                    else if (event.type == "state") {
                        [stateDiff, currentState] = this.getStateDifference(event.value, currentState)                        
                        if(stateDiff["ocr"].length > 0)
                            for(var ocr in stateDiff["ocr"])
                                events.states.push(
                                {
                                    type: "state", 
                                    x: event.time, 
                                    value: ocr.target + " º" + (this.state.settings.temperatureUnit == "F" ? "F" : "C"), 
                                    color: '#009fff'
                                })
                        if(stateDiff["states"].length > 0)
                            for(var state in stateDiff["states"]) {
                                console.log(state)
                                events.states.push(
                                {
                                    type: "state", 
                                    x: event.time, 
                                    value: state.name, 
                                    color: '#009fff'
                                })
                            }
                        if(stateDiff["power"])
                            events.states.push(
                            {
                                type: stateDiff["power"] ? "powerOn" : "powerOff", 
                                value: stateDiff["power"] ? "Power On" : "Power Off", 
                                x: event.time, 
                                color: stateDiff["power"] ? '#00ff00' : '#ff0000'
                            })
                    }
                    else if (event.type == "trigger") {
                        events.states.push({
                            type: 'trigger',
                            x: event.time,
                            value: event.value,
                            color: '#7e868d'
                        })
                    }
                }
                console.log(events)
                this.plotData(events)
            }
        }
        finally {
            this.setState({loadingEvents: false})
        }
    }

    getStateDifference = (state, currentState) => {
        if(!currentState)
            return [state, state]

        var diff = { ocr: [], states: [] }
        if(!currentState["power"] || (state["power"] && state["power"]["active"] != currentState["power"]["active"])) {
            diff["power"] = state["power"]
            currentState["power"] = state["power"]
            if(!state["power"]["active"]) 
                return [diff, { power: false, ocr: [], state: [] }]
        }

        if(state["ocr"]) {
            for(var ocr of state["ocr"]) {
                const currentValue = currentState["ocr"].find(x => x.id == ocr.id)
                if(!currentValue) {
                    diff["ocr"].push(ocr)
                    currentState["ocr"].push(ocr)
                }
                else if (currentValue.target != ocr.target) {
                    diff["ocr"].push(ocr)
                    currentValue.target = ocr.target
                }
            }
        }
        if(state["states"]) {
            for(var s of state["states"]) {                
                const currentValue = currentState["states"].find(x => x.id == s.id)
                if(!currentValue) {
                    diff["states"].push(s)
                    currentState["states"].push(s)
                } 
                else if(currentValue.active != s.active) {
                    diff["states"].push(s)
                    currentValue.active = s.active
                }
            }
        }

        return [diff, currentState]
    }

    plotData = (events) => { 
        const buffer = 20
        var humidityRange = null, temperatureRange = null

        if(events.temperature) {
            const average = events.temperature.y.reduce((a, b) => a + b) / events.temperature.y.length;
            temperatureRange = [Math.round(average - buffer), Math.round(average + buffer)]
        }

        if(events.humidity) {
            const average = events.humidity.y.reduce((a, b) => a + b) / events.humidity.y.length;
            humidityRange = [Math.round(average - buffer), Math.round(average + buffer)]
        }


        var data = []
        var layout = {
            autosize: true,
            paper_bgcolor: '#ffffff50',
            plot_bgcolor: '#ffffff50',
            legend: {x: 0.4, y: 1.2},
            shapes: [],
            yaxis: {
                title: "º" + (this.state.settings.temperatureUnit == "F" ? "F" : "C"),
                range: temperatureRange
            },
            yaxis2: {
                title: "%H",
                side: 'right',
                overlaying: 'y',
                range: humidityRange
            },
            yaxis3 : {
                visible: false,
                fixedrange: true, 
                range: [0,1],
                overlaying: 'y'
            }
        }
        var config = {
            displayModeBar: false
        }

        if(events.humidity){
            data.push({
                x: events.humidity.x,
                y: events.humidity.y,
                type: 'scatter',
                mode: 'lines',
                line: {
                    color: '#cdcdcd',
                    width: 3
                },
                yaxis: 'y2',
                name: 'humidity'
            })
        }

        if(events.temperature){
            data.push({
                x: events.temperature.x,
                y: events.temperature.y,
                type: 'scatter',
                mode: 'lines',
                line: {
                    color: '#3780bf',
                    width: 3
                },
                name: 'temperature'
            })
        }            
        
        var markers = {}
        for(var type of ['powerOn', 'powerOff', 'state', 'trigger']) {
            markers[type] = { mode: 'markers', hoverinfo: [], x: [], y: [], hovertext: [], marker: {}, showlegend: false, yaxis: 'y3' }
            data.push(markers[type])
        }

        if(events.states){
            for(var event of events.states) {
                layout.shapes.push({
                    yref: 'paper',
                    x0: event.x,
                    y0: 0,
                    x1: event.x,
                    y1: 0.33,
                    type: 'line',
                    line: {
                        color: event.color,
                        width: 3
                    }
                })

                markers[event.type].x.push(event.x)
                markers[event.type].y.push(0.33)
                markers[event.type].hovertext.push(event.value)
                markers[event.type].hoverinfo.push('text+x')
                markers[event.type].marker.color = event.color
            }
        }

        const plot = document.getElementById('plot')
        window.Plotly.newPlot(plot, {data: data, layout: layout, config: config});
    }
}

export default Settings